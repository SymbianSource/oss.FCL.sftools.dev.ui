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
package com.nokia.tools.media.utils.tooltip;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import com.nokia.tools.media.utils.UtilsPlugin;

public class TitlebarComposite extends Composite {

	public enum EActionLocation {
		ABOVE_TITLE, BELOV_TITLE, BEGINNING, END
	};

	public static final Image TITLEBAR_NORMAL = UtilsPlugin.getImageDescriptor(
			"icons/titlebar.gif").createImage();

	public static final Image TITLEBAR_TINY = UtilsPlugin.getImageDescriptor(
			"icons/titlebar2.gif").createImage();

	protected Composite title;

	protected String titleText = "";

	protected int titleTextAlignment = SWT.CENTER;

	protected MenuManager titleMenuManager;

	protected Composite titlebar;

	protected List<Resource> resourcesToDispose;

	protected Rectangle moveArea = null;

	protected boolean moveable = true;

	protected Image titlebarImage = TITLEBAR_NORMAL;

	private boolean titleTextTransparent = true;

	public TitlebarComposite(Composite parent) {
		super(parent, SWT.NONE);
		createControls(this);
	}

	public void setTitleText(String text) {
		if (text == null)
			text = "";
		titleText = text;
	}

	public void setTitleTextAlignment(int alignment) {
		titleTextAlignment = alignment;
	}

	public void setTitleTextTransparent(boolean transparent) {
		titleTextTransparent = transparent;
	}

	protected void createControls(Composite parent) {
		titlebar = parent;
		GridData gd = new GridData(GridData.BEGINNING
				| GridData.FILL_HORIZONTAL);
		titlebar.setLayoutData(gd);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = layout.marginHeight = 3;
		layout.horizontalSpacing = layout.verticalSpacing = 1;
		titlebar.setLayout(layout);

		titlebar.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				Rectangle bounds = ((Control) e.widget).getBounds();
				Image img = createTitlebarBackground(new Rectangle(0, 0,
						bounds.width, bounds.height));
				try {
					gc.drawImage(img, 0, 0);
				} finally {
					img.dispose();
				}
			}
		});

		title = new Composite(titlebar, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = titlebarImage.getBounds().height;
		gd.widthHint = 0;
		gd.minimumWidth = 5;
		title.setLayoutData(gd);

		Listener titleListener = new Listener() {
			boolean movementMode = false;

			protected Point lastMxy;

			public void handleEvent(Event event) {
				if (SWT.Paint == event.type) {
					GC gc = event.gc;

					Rectangle bounds = ((Control) event.widget).getBounds();

					Image bgImg = createTitlebarBackground(bounds);
					try {
						gc.drawImage(bgImg, 0, 0);
					} finally {
						bgImg.dispose();
					}

					String text = titleText;
					Point extent = gc.textExtent(text);

					if (extent.x > bounds.width) {
						// text is too long, cut it
						while (text.length() > 1 && extent.x > bounds.width) {
							text = text.substring(0, text.length() - 1);
							extent = gc.textExtent(text + "...");
						}
						text = text + "...";
					}

					if (extent.x <= bounds.width) {
						int x, y;
						switch (titleTextAlignment) {
						case SWT.RIGHT:
							x = bounds.width - extent.x;
							y = (bounds.height - extent.y) / 2;
							break;
						case SWT.CENTER:
							x = (bounds.width - extent.x) / 2;
							y = (bounds.height - extent.y) / 2;
							break;
						case SWT.LEFT:
						default:
							x = 0;
							y = (bounds.height - extent.y) / 2;
						}

						gc.setBackground(getBackground());
						gc.setForeground(ColorConstants.lightGray);
						gc.drawText(text, x + 1, y, titleTextTransparent);
						gc.drawText(text, x, y + 1, true);
						gc.setForeground(ColorConstants.black);
						gc.drawText(text, x, y, true);
					}
				}

				if (SWT.MouseMove == event.type) {
					if (movementMode) {
						Control control = ((Control) event.widget);
						Point currMxy = control.toDisplay(event.x, event.y);
						if (!currMxy.equals(lastMxy)) {
							int x = currMxy.x - lastMxy.x
									+ titlebar.getShell().getLocation().x;
							int y = currMxy.y - lastMxy.y
									+ titlebar.getShell().getLocation().y;

							Shell sh = getShell();

							Rectangle newBounds = new Rectangle(x, y, sh
									.getSize().x, sh.getSize().y);

							if (moveArea != null) {
								if (newBounds.x + newBounds.width > moveArea.x
										+ moveArea.width) {
									newBounds.x = moveArea.x + moveArea.width
											- newBounds.width;
								}

								if (newBounds.y + newBounds.height > moveArea.y
										+ moveArea.height) {
									newBounds.y = moveArea.y + moveArea.height
											- newBounds.height;
								}

								if (newBounds.x < moveArea.x) {
									newBounds.x = moveArea.x;
								}

								if (newBounds.y < moveArea.y) {
									newBounds.y = moveArea.y;
								}
							}

							lastMxy = currMxy;

							sh.setBounds(newBounds);

							Stack<Shell> shells = new Stack<Shell>();
							shells.push(sh);
							while (sh.getParent() != null) {
								sh = (Shell) sh.getParent();
								shells.push(sh);
							}

							while (!shells.isEmpty()) {
								sh = shells.pop();
								sh.update();
								break;
							}
						}
					}
				}

				if (SWT.MouseDown == event.type) {
					if (moveable && event.button == 1) {
						Control control = ((Control) event.widget);
						lastMxy = control.toDisplay(event.x, event.y);
						movementMode = true;
					}
				}

				if (SWT.MouseUp == event.type) {
					if (event.button == 1) {
						movementMode = false;
					}
				}
			}
		};

		title.addListener(SWT.Paint, titleListener);
		title.addListener(SWT.MouseMove, titleListener);
		title.addListener(SWT.MouseDown, titleListener);
		title.addListener(SWT.MouseUp, titleListener);

		final Shell parentShell = (Shell) getShell().getParent();

		titlebar.getShell().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				Shell sh = parentShell;

				while (sh != null && (!sh.isDisposed())
						&& sh.getParent() != null) {
					sh = (Shell) sh.getParent();
				}

				if (sh != null) {
					Rectangle bounds = getShell().getBounds();
					Point p = sh.toControl(bounds.x, bounds.y);
					sh.redraw(p.x, p.y, bounds.width, bounds.height, true);
					sh.update();
				}
			}
		});
		titlebar.addDisposeListener(new DisposeListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {

				if (resourcesToDispose != null) {
					for (Resource resource : resourcesToDispose) {
						try {
							resource.dispose();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					resourcesToDispose.clear();
				}

				if (cachedImage != null) {
					cachedImage.dispose();
					cachedImage = null;
				}
			}

		});

		createTitleMenu();
	}

	Image cachedImage = null;

	protected Image createTitlebarBackground(Rectangle cropArea) {
		if (cachedImage != null) {
			if (cachedImage.getBounds().width == titlebar.getBounds().width
					&& cachedImage.getBounds().height == titlebar.getBounds().height) {
				Image toRetImg = new Image(null, cropArea.width,
						cropArea.height);
				GC toRetImgGc = new GC(toRetImg);
				try {
					toRetImgGc.drawImage(cachedImage, cropArea.x, cropArea.y,
							cropArea.width, cropArea.height, 0, 0,
							cropArea.width, cropArea.height);
					return toRetImg;
				} finally {
					toRetImgGc.dispose();
				}
			}

			cachedImage.dispose();
			cachedImage = null;
		}

		cachedImage = new Image(null, titlebar.getBounds().width, titlebar
				.getBounds().height);
		GC imgGc = new GC(cachedImage);
		try {
			if (titlebarImage != null) {
				int x = 0;
				while (x < titlebar.getBounds().width) {
					imgGc.drawImage(titlebarImage, 0, 0, titlebarImage
							.getBounds().width,
							titlebarImage.getBounds().height, x, 0,
							titlebarImage.getBounds().width, titlebar
									.getBounds().height);
					x += titlebarImage.getImageData().width;
				}
			} else {
				imgGc.fillRectangle(0, 0, titlebar.getBounds().width, titlebar
						.getBounds().height);
			}

			return createTitlebarBackground(cropArea);
		} finally {
			imgGc.dispose();
		}
	}

	protected void createTitleMenu() {
		titleMenuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		titleMenuManager.setRemoveAllWhenShown(true);

		Menu titleMenu = titleMenuManager.createContextMenu(title);
		title.setMenu(titleMenu);
	}

	public MenuManager getTitleMenu() {
		return titleMenuManager;
	}

	public void addTitleAction(final IAction action) {
		addTitleAction(action, EActionLocation.BELOV_TITLE);
	}

	public void addTitleAction(final IAction action, EActionLocation location) {
		final Canvas canvas = new Canvas(titlebar, SWT.NONE);
		switch (location) {
		case BEGINNING:
			canvas.moveAbove(null);
			break;
		case END:
			canvas.moveBelow(null);
			break;
		case ABOVE_TITLE:
			canvas.moveAbove(title);
			break;
		case BELOV_TITLE:
		default:
			canvas.moveBelow(title);
			break;
		}

		((GridLayout) titlebar.getLayout()).numColumns++;

		if (resourcesToDispose == null) {
			resourcesToDispose = new ArrayList<Resource>();
		}

		final CanvasListener canvasListener = new CanvasListener(action, canvas);

		final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (IAction.IMAGE == event.getProperty()) {
					ImageDescriptor imgDesc = action.getImageDescriptor();
					ImageDescriptor hoverImgDesc = action
							.getHoverImageDescriptor();
					ImageDescriptor disabledImgDesc = action
							.getDisabledImageDescriptor();

					canvasListener.setImg(imgDesc);
					canvasListener.setHoverImg(hoverImgDesc);
					canvasListener.setDisabledImg(disabledImgDesc);
				}

				if (IAction.TEXT == event.getProperty()
						|| IAction.TOOL_TIP_TEXT == event.getProperty()) {
					if (action.getToolTipText() != null) {
						canvas.setToolTipText(action.getToolTipText());
					} else if (action.getText() != null) {
						canvas.setToolTipText(action.getText());
					}
				}

				canvas.redraw();
				canvas.update();
			}
		};

		action.addPropertyChangeListener(propertyChangeListener);

		canvas.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				action.removePropertyChangeListener(propertyChangeListener);
			}
		});

		propertyChangeListener.propertyChange(new PropertyChangeEvent(this,
				IAction.IMAGE, null, null));
		propertyChangeListener.propertyChange(new PropertyChangeEvent(this,
				IAction.TEXT, null, null));

		Rectangle bounds = canvasListener.img.getBounds();

		GridData gd = new GridData(bounds.width, bounds.height);
		canvas.setLayoutData(gd);

		canvas.addListener(SWT.Paint, canvasListener);
		canvas.addListener(SWT.MouseEnter, canvasListener);
		canvas.addListener(SWT.MouseExit, canvasListener);
		canvas.addListener(SWT.MouseUp, canvasListener);
	}

	protected class CanvasListener implements Listener {
		boolean hover = false;

		IAction action;

		Canvas canvas;

		Image img;

		Image hoverImg;

		Image disabledImg;

		public CanvasListener(IAction action, Canvas canvas) {
			this.action = action;
			this.canvas = canvas;
		}

		public void handleEvent(Event event) {
			if (SWT.Paint == event.type) {
				GC gc = event.gc;

				Rectangle bounds = ((Control) event.widget).getBounds();

				Image bgImg = createTitlebarBackground(bounds);
				try {
					gc.drawImage(bgImg, 0, 0);
				} finally {
					bgImg.dispose();
				}

				if (action.isEnabled()) {
					if (hover && hoverImg != null) {
						gc.drawImage(hoverImg, 0, 0);
					} else {
						gc.drawImage(img, 0, 0);
					}
				} else {
					gc.drawImage(disabledImg, 0, 0);
				}
			}

			if (SWT.MouseEnter == event.type) {
				if (!hover) {
					hover = true;
					canvas.redraw();
					canvas.update();
				}
			}

			if (SWT.MouseExit == event.type) {
				if (hover) {
					hover = false;
					canvas.redraw();
					canvas.update();
				}
			}

			if (SWT.MouseUp == event.type) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (action.isEnabled()) {
							action.run();
						}
					}
				});
			}
		}

		public void setHoverImg(ImageDescriptor hoverImg) {
			if (this.hoverImg != null) {
				this.hoverImg.dispose();
			}
			if (hoverImg != null) {
				this.hoverImg = hoverImg.createImage();
				resourcesToDispose.add(this.hoverImg);
			} else {
				this.hoverImg = null;
			}
		}

		public void setImg(ImageDescriptor img) {
			if (this.img != null) {
				this.img.dispose();
			}
			if (img != null) {
				this.img = img.createImage();
				resourcesToDispose.add(this.img);
			} else {
				this.img = ImageDescriptor.getMissingImageDescriptor()
						.createImage();
				resourcesToDispose.add(this.img);
			}
		}

		public void setDisabledImg(ImageDescriptor disabledImg) {
			if (this.disabledImg != null) {
				this.disabledImg.dispose();

			}
			if (disabledImg != null) {
				this.disabledImg = disabledImg.createImage();
				resourcesToDispose.add(this.disabledImg);
			} else {
				if (img != null) {
					this.disabledImg = new Image(null, img, SWT.IMAGE_DISABLE);
					resourcesToDispose.add(this.disabledImg);
				} else {
					this.disabledImg = null;
				}

			}
		}
	}

	public void setMoveArea(Rectangle moveArea) {
		this.moveArea = moveArea;
	}

	public boolean isMoveable() {
		return moveable;
	}

	public void setMoveable(boolean moveable) {
		this.moveable = moveable;
	}

	public Image getTitlebarImage() {
		return titlebarImage;
	}

	public void setTitlebarImage(Image titlebarImage) {
		this.titlebarImage = titlebarImage;
	}
}
