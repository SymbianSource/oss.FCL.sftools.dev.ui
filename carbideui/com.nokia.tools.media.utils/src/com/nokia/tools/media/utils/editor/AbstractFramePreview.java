/*
* Copyright (c) 2006-2010 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description:
*
*/
package com.nokia.tools.media.utils.editor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.RenderedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.ImageAdapter;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.sun.imageio.plugins.common.ImageUtil;

/**
 * Refactored from IImagePreviewComposite
 */
public abstract class AbstractFramePreview extends Composite implements
		PropertyChangeListener {

	public IImage imageProcessed = null;

	private EntityImageCache cache = null;

	private ILayer selectedLayer;

	private RenderedImage awtPreviewImage = null;
	private Image previewImage = null;
	public Canvas previewCanvas = null;

	private long time;
	private TimingModel timing = TimingModel.RealTime;

	protected boolean previewOnlySelectedLayer;

	class PreviewRefreshRunnable implements Runnable {

		private boolean stop = false;

		private boolean refresh = false;

		private Object lock = new Object();

		private boolean refreshAnim;

		private long animTime;

		private TimingModel animTiming;

		public void run() {
			while (!stop) {
				try {
					synchronized (lock) {
						lock.wait();
					}
					while (refresh) {
						refresh = false;
						if (!refreshAnim)
							updatePreviewImage();
						else
							updatePreviewImage(animTiming, animTime);
					}
				} catch (InterruptedException ie) {

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void refresh() {
			System.out.println("PreviewRefreshRunnable.refresh()");
			refresh = true;
			refreshAnim = false;
			animTime = 0;
			synchronized (lock) {
				lock.notify();
			}
		}

		public void stop() {
			stop = true;
			synchronized (lock) {
				lock.notify();
			}
		}

		public void refresh(TimingModel animTiming, long newTime) {
			this.animTiming = animTiming;
			refresh = true;
			refreshAnim = true;
			animTime = newTime;
			synchronized (lock) {
				lock.notify();
			}
		}
	}

	public AbstractFramePreview(Composite parent, int style, IImage image) {
		super(parent, style);

		addDisposeListener(new DisposeListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				onDispose(e);
			}

		});
		imageProcessed = image;

		cache = new EntityImageCache(imageProcessed);

		imageProcessed.addPropertyListener(this);

		if (image.getLayerCount() > 0) {
			selectedLayer = image.getLayer(0);
		}

		previewRefreshRunnable = new PreviewRefreshRunnable();
		Thread previewUpdaterThread = new Thread(previewRefreshRunnable,
				"PreviewUpdatedThread");
		previewUpdaterThread.setDaemon(true);
		previewUpdaterThread.setPriority(Thread.MIN_PRIORITY);
		previewUpdaterThread.start();

		setBackground(getParent().getBackground());
		GridLayout lay = new GridLayout(1, false);
		lay.marginHeight = 0;
		lay.marginWidth = 0;
		setLayout(lay);
		createPreview(this);
		updatePreviewImage();
	}

	protected void onDispose(DisposeEvent e) {
		imageProcessed.removePropertyChangeListener(this);
		if (previewRefreshRunnable != null) {
			previewRefreshRunnable.stop();
		}
		if (previewImage != null) {
			previewImage.dispose();
		}
	}

	protected void createPreview(Composite parent) {
		previewCanvas = new Canvas(parent, SWT.NO_BACKGROUND);
		GridData gd = new GridData(GridData.FILL_BOTH);
		previewCanvas.setLayoutData(gd);
		previewCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (previewImage != null) {
					synchronized (previewImage) {
						GC gc = e.gc;
						gc.drawImage(previewImage, 0, 0);
					}
				} else {
					GC gc = e.gc;
					gc.setBackground(ColorConstants.white);
					gc.fillRectangle(0, 0, previewCanvas.getSize().x,
							previewCanvas.getSize().y);
					gc.setAntialias(SWT.ON);
					gc.drawLine(0, 0, previewCanvas.getSize().x, previewCanvas
							.getSize().y);
					gc.drawLine(previewCanvas.getSize().x, 0, 0, previewCanvas
							.getSize().y);
				}
			}
		});
	}

	private PreviewRefreshRunnable previewRefreshRunnable = null;

	public void refreshPreviewImage() {
		previewRefreshRunnable.refresh(timing, time);
	}

	public final ILayer getSelectedLayer() {
		return selectedLayer;
	}

	public void setSelectedLayer(ILayer selectedLayer) {
		this.selectedLayer = selectedLayer;
		if (previewOnlySelectedLayer && selectedLayer != null) {
			refreshPreviewImage();
		}
	}

	public boolean isPreviewOnlySelectedLayer() {
		return previewOnlySelectedLayer;
	}

	public void setPreviewOnlySelectedLayer(boolean previewOnlySelectedLayer) {
		this.previewOnlySelectedLayer = previewOnlySelectedLayer;
	}

	public void refreshPreviewImage(TimingModel animTiming, long newTime) {
		time = newTime;
		timing = animTiming;
		previewRefreshRunnable.refresh(animTiming, newTime);
	}

	private void updatePreviewImage() {
		if (previewOnlySelectedLayer && selectedLayer != null) {
			awtPreviewImage = selectedLayer.getProcessedImage();
		} else {
			awtPreviewImage = imageProcessed.getAggregateImage();
		}

		if (awtPreviewImage == null) {
			if (previewImage != null) {
				synchronized (previewImage) {
					previewImage.dispose();
				}
				previewImage = null;
			}

			Runnable updateRunnable = new Runnable() {
				public void run() {
					if (!previewCanvas.isDisposed()) {
						previewCanvas.redraw();
					}
				};
			};

			if (Display.getCurrent() != null) {
				updateRunnable.run();
			} else {
				Display.getDefault().syncExec(updateRunnable);
			}

			return;
		}

		ImageAdapter vpAdaptor = new ImageAdapter(awtPreviewImage.getWidth(),
				awtPreviewImage.getHeight());
		Graphics2D vpG = (Graphics2D) vpAdaptor.getGraphics();
		vpG.setColor(Color.WHITE);
		vpG.fillRect(0, 0, awtPreviewImage.getWidth(), awtPreviewImage
				.getHeight());
		vpG.drawRenderedImage(awtPreviewImage, CoreImage.TRANSFORM_ORIGIN);
		vpG.dispose();

		if (previewImage != null) {
			synchronized (previewImage) {
				previewImage.dispose();
				previewImage = vpAdaptor.toSwtImage();
			}
		} else {
			previewImage = vpAdaptor.toSwtImage();
		}

		if (Display.getCurrent() != null) {
			if (!previewCanvas.isDisposed()) {
				previewCanvas.setLayoutData(new GridData(previewImage
						.getImageData().width,
						previewImage.getImageData().height));

				previewCanvas.redraw();
			}
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (!previewCanvas.isDisposed()) {
						previewCanvas.setLayoutData(new GridData(previewImage
								.getImageData().width, previewImage
								.getImageData().height));
						previewCanvas.redraw();
					}
				};
			});
		}
	}

	private void updatePreviewImage(TimingModel animTiming, long timeOffset) {
		if (previewOnlySelectedLayer && selectedLayer != null) {
			awtPreviewImage = selectedLayer.getProcessedImage(timeOffset, true);
		} else {
			RenderedImage oldImage = awtPreviewImage;
			if (cache != null) {
				awtPreviewImage = cache.getImage(animTiming, timeOffset, true);
			} else {
				awtPreviewImage = imageProcessed.getAggregateImage(animTiming,
						timeOffset, true);
			}
			if (oldImage == awtPreviewImage) {
				return;
			}
		}

		if (awtPreviewImage == null) {
			if (previewImage != null) {
				synchronized (previewImage) {
					previewImage.dispose();
				}
				previewImage = null;
			}

			Runnable updateRunnable = new Runnable() {
				public void run() {
					if (!previewCanvas.isDisposed()) {
						previewCanvas.redraw();
					}
				};
			};

			if (Display.getCurrent() != null) {
				updateRunnable.run();
			} else {
				Display.getDefault().syncExec(updateRunnable);
			}

			return;
		}

		final int width = awtPreviewImage.getWidth();
		final int height = awtPreviewImage.getHeight();

		ImageAdapter vpAdaptor = new ImageAdapter(width, height);
		Graphics2D vpG = (Graphics2D) vpAdaptor.getGraphics();
		vpG.setColor(Color.WHITE);
		vpG.fillRect(0, 0, width, height);
		vpG.drawRenderedImage(awtPreviewImage, CoreImage.TRANSFORM_ORIGIN);
		vpG.dispose();
		if (previewImage != null) {
			synchronized (previewImage) {
				previewImage.dispose();
				previewImage = vpAdaptor.toSwtImage();
			}
		} else {
			previewImage = vpAdaptor.toSwtImage();
		}

		Runnable updateRunnable = new Runnable() {
			public void run() {
				if (!previewCanvas.isDisposed()) {
					previewCanvas.setLayoutData(new GridData(width, height));
					previewCanvas.redraw();
				}
			};
		};

		if (Display.getCurrent() != null) {
			updateRunnable.run();
		} else {
			Display.getDefault().asyncExec(updateRunnable);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == imageProcessed) {
			invalidateCache();
			refreshPreviewImage();
		}
	}

	public void invalidateCache() {
		if (cache != null) {
			cache.clear();
		}
	}

}