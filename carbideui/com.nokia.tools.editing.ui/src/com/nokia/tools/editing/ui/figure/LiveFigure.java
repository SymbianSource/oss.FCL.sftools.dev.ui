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
package com.nokia.tools.editing.ui.figure;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.draw2d.EventDispatcher;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.nokia.tools.editing.ui.Activator;
import com.nokia.tools.resource.util.DebugHelper;
import com.nokia.tools.resource.util.DebugHelper.SilentRunnable;

public abstract class LiveFigure extends ImageFigure {
	// delay between each update, main purpose is to reduce number of captures
	private static final long DEFAULT_DELAY = 500;
	// global timer to execute image capturing tasks
	private static Timer timer;

	private GraphicalViewer viewer;
	private Canvas canvas;
	private EventDispatcher dispatcher;
	private boolean isLive;
	private boolean isAnimating;
	private volatile boolean isDirty;
	private volatile TimerTask task;
	private long delay = DEFAULT_DELAY;

	public LiveFigure(GraphicalViewer viewer) {
		this(viewer, false);
	}

	public LiveFigure(GraphicalViewer viewer, boolean isLive) {
		this.viewer = viewer;
		try {
			Field f = GraphicalViewerImpl.class.getDeclaredField("lws");
			f.setAccessible(true);
			LightweightSystem lws = (LightweightSystem) f.get(viewer);
			f = LightweightSystem.class.getDeclaredField("dispatcher");
			f.setAccessible(true);
			dispatcher = (EventDispatcher) f.get(lws);
		} catch (Exception e) {
			Activator.error(e);
		}
		setLive(isLive);
		init();
	}

	private static synchronized void init() {
		if (timer == null) {
			timer = new Timer("Image update", true);
		}
	}

	/**
	 * @return the isLive
	 */
	public boolean isLive() {
		return isLive;
	}

	/**
	 * @param isLive the isLive to set
	 */
	public void setLive(boolean isLive) {
		this.isLive = isLive;
		if (Display.getCurrent() != null) {
			initFigure();
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					initFigure();
				}
			});
		}
	}

	/**
	 * @return the isAnimating
	 */
	public boolean isAnimating() {
		return isAnimating;
	}

	/**
	 * @param isAnimating the isAnimating to set
	 */
	public void setAnimating(boolean isAnimating) {
		this.isAnimating = isAnimating;
	}

	/**
	 * @return the delay
	 */
	public long getDelay() {
		return delay;
	}

	/**
	 * @param delay the delay to set
	 */
	public void setDelay(long delay) {
		if (delay < 0) {
			throw new IllegalArgumentException("The delay cannot be negative: "
					+ delay);
		}
		this.delay = delay;
	}

	/**
	 * @return the canvas
	 */
	public Canvas getCanvas() {
		return canvas;
	}

	/**
	 * @return the viewer
	 */
	public GraphicalViewer getViewer() {
		return viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
	 */
	@Override
	protected void paintFigure(Graphics graphics) {
		initFigure();
		if (isLive()) {
			paintLive();
		}
		super.paintFigure(graphics);
	}

	protected boolean shouldInitFigure() {
		return getOffscreenComponent() == null || (canvas == null && isLive)
				|| (canvas != null && !isLive);
	}

	protected Object getOffscreenComponent() {
		return null;
	}

	protected void initFigure() {
	}

	protected void paintLive() {
	}

	protected void createCanvas() {
		if (viewer.getControl() != null && !viewer.getControl().isDisposed()) {
			canvas = new Canvas((Composite) viewer.getControl(), SWT.EMBEDDED);
			EventHandler handler = new EventHandler();
			canvas.addMouseListener(handler);
			canvas.addMouseMoveListener(handler);
			canvas.addMouseTrackListener(handler);
			canvas.addKeyListener(handler);
			canvas.addTraverseListener(handler);
			canvas.addFocusListener(handler);
			canvas.addListener(SWT.MouseWheel, handler);
			canvas.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
		}
	}

	public void dispose() {
		Runnable runnable = new Runnable() {
			public void run() {
				if (getImage() != null) {
					getImage().dispose();
					setImage(null);
				}
				if (canvas != null) {
					canvas.dispose();
					canvas = null;
				}
			}
		};
		if (Display.getCurrent() != null) {
			runnable.run();
		}
		Display.getDefault().syncExec(runnable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.Figure#setBounds(org.eclipse.draw2d.geometry.Rectangle)
	 */
	@Override
	public void setBounds(final Rectangle rect) {
		if (rect.x < 0 || rect.y < 0) {
			return;
		}
		Rectangle prevRect = getBounds().getCopy();
		super.setBounds(rect);

		if (getCanvas() != null && !getCanvas().isDisposed()) {
			getCanvas().setBounds(
					new org.eclipse.swt.graphics.Rectangle(rect.x, rect.y,
							rect.width, rect.height));
		}
		if (!prevRect.getSize().equals(rect.getSize())) {
			revalidateRoot();
		}
	}

	/**
	 * Redraws the figure, this doesn't need to run in display thread.
	 * 
	 * @param notifier the actual notifier, used for optimizing the refresh
	 */
	public void revalidateRoot() {
		if (task != null) {
			// these updates may still change the actual components but were
			// missed during the image capturing, thus we capture image again
			// after major capturing job just in case it's dirty
			isDirty = true;
			return;
		}

		if (!isImageCaptureNecessary()) {
			return;
		}

		task = new TimerTask() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.TimerTask#run()
			 */
			@Override
			public void run() {
				Runnable job = new ImageCaptureJob();
				runImageCaptureJob(job);
			}
		};
		if (isAnimating) {
			task.run();
		} else {
			timer.schedule(task, delay);
		}
	}

	protected boolean isImageCaptureNecessary() {
		if (isLive() && isAnimating()) {
			paintLive();
			return false;
		}
		final Rectangle bounds = getBounds().getCopy();
		if (bounds.width <= 0 || bounds.height <= 0) {
			return false;
		}
		if (getOffscreenComponent() == null) {
			return false;
		}
		return true;
	}

	protected void runImageCaptureJob(Runnable job) {
	}

	protected Image captureImage() {
		return null;
	}

	class ImageCaptureJob implements Runnable {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			final Image[] images = new Image[1];
			// sets this to false so all requests happen during the delay will
			// be ignored
			isDirty = false;
			if (DebugHelper.debugPerformance()) {
				DebugHelper.debugTime(LiveFigure.this, "capturing image",
						new SilentRunnable() {

							/*
							 * (non-Javadoc)
							 * 
							 * @see org.eclipse.core.runtime.ISafeRunnable#run()
							 */
							public void run() throws Exception {
								images[0] = captureImage();
							}

						});
			} else {
				try {
					images[0] = captureImage();
				} catch (Exception e) {
					System.out.println("Memory problem detected.");
					e.printStackTrace();
					return;
				}
			}
			final Image image = images[0];
			// sets task to null to indicate the capture completion so next
			// request will invoke capturing again
			task = null;
			if (isDirty) {
				revalidateRoot();
				isDirty = false;
			}

			Runnable runnable = new Runnable() {
				public void run() {
					Image img = getImage();
					if (img != null) {
						img.dispose();
					}
					setImage(image);

					// updates the other layers, e.g. selection
					
					for (Object obj : viewer.getEditPartRegistry().values()
							.toArray()) {
						((EditPart) obj).refresh();
					}
				}
			};
			if (Display.getCurrent() != null) {
				runnable.run();
			} else {
				Display.getDefault().asyncExec(runnable);
			}
		}
	}

	class EventHandler implements MouseMoveListener, MouseListener,
			KeyListener, TraverseListener, FocusListener, MouseTrackListener,
			Listener {
		/** @see FocusListener#focusGained(FocusEvent) */
		public void focusGained(FocusEvent e) {
			dispatcher.dispatchFocusGained(e);
		}

		/** @see FocusListener#focusLost(FocusEvent) */
		public void focusLost(FocusEvent e) {
			dispatcher.dispatchFocusLost(e);
		}

		/**
		 * @see Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 * @since 3.1
		 */
		public void handleEvent(Event event) {
			// Mouse wheel events
			if (event.type == SWT.MouseWheel)
				dispatcher.dispatchMouseWheelScrolled(event);
		}

		/** @see KeyListener#keyPressed(KeyEvent) */
		public void keyPressed(KeyEvent e) {
			dispatcher.dispatchKeyPressed(e);
		}

		/** @see KeyListener#keyReleased(KeyEvent) */
		public void keyReleased(KeyEvent e) {
			dispatcher.dispatchKeyReleased(e);
		}

		/** @see TraverseListener#keyTraversed(TraverseEvent) */
		public void keyTraversed(TraverseEvent e) {
			/*
			 * Doit is almost always false by default for Canvases with
			 * KeyListeners. Set to true to allow normal behavior. For example,
			 * in Dialogs ESC should close.
			 */
			e.doit = true;
			dispatcher.dispatchKeyTraversed(e);
		}

		/** @see MouseListener#mouseDoubleClick(MouseEvent) */
		public void mouseDoubleClick(MouseEvent e) {
			translateMouseEvent(e);
			dispatcher.dispatchMouseDoubleClicked(e);
		}

		/** @see MouseListener#mouseDown(MouseEvent) */
		public void mouseDown(MouseEvent e) {
			translateMouseEvent(e);
			dispatcher.dispatchMousePressed(e);
		}

		/** @see MouseTrackListener#mouseEnter(MouseEvent) */
		public void mouseEnter(MouseEvent e) {
			translateMouseEvent(e);
			dispatcher.dispatchMouseEntered(e);
		}

		/** @see MouseTrackListener#mouseExit(MouseEvent) */
		public void mouseExit(MouseEvent e) {
			translateMouseEvent(e);
			dispatcher.dispatchMouseExited(e);
		}

		/** @see MouseTrackListener#mouseHover(MouseEvent) */
		public void mouseHover(MouseEvent e) {
			translateMouseEvent(e);
			dispatcher.dispatchMouseHover(e);
		}

		/** @see MouseMoveListener#mouseMove(MouseEvent) */
		public void mouseMove(MouseEvent e) {
			translateMouseEvent(e);
			dispatcher.dispatchMouseMoved(e);
		}

		/** @see MouseListener#mouseUp(MouseEvent) */
		public void mouseUp(MouseEvent e) {
			translateMouseEvent(e);
			dispatcher.dispatchMouseReleased(e);
		}

		private void translateMouseEvent(MouseEvent e) {
			e.x += canvas.getBounds().x;
			e.y += canvas.getBounds().y;
		}
	}
}
