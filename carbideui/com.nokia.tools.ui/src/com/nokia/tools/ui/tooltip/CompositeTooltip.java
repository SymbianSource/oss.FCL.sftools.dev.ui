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

package com.nokia.tools.ui.tooltip;

import java.util.Arrays;

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class CompositeTooltip extends CustomTooltip implements
		CustomTooltip.ICustomTooltipControlCreator {

	protected int minWidth = SWT.DEFAULT;

	protected int minHeight = SWT.DEFAULT;

	protected int maxWidth = SWT.DEFAULT;

	protected int maxHeight = SWT.DEFAULT;

	protected int minUnfocusedWidth = SWT.DEFAULT;

	protected int minUnfocusedHeight = SWT.DEFAULT;

	protected int maxUnfocusedWidth = SWT.DEFAULT;

	protected int maxUnfocusedHeight = SWT.DEFAULT;

	protected boolean ignoreMouseExit;
	
	protected MouseExitTimerThread mouseExitTimerThread = null;

	public CompositeTooltip() {
		super();
		setControlCreator(this);
	}

	public void setMinimumSize(int minWidth, int minHeight) {
		this.minWidth = minWidth;
		this.minHeight = minHeight;
	}

	public void setMinimumUnfocusedSize(int minWidth, int minHeight) {
		this.minUnfocusedWidth = minWidth;
		this.minUnfocusedHeight = minHeight;
	}

	public void setMaximumUnfocusedSize(int maxWidth, int maxHeight) {
		this.maxUnfocusedWidth = maxWidth;
		this.maxUnfocusedHeight = maxHeight;
	}

	public void initControl(CompositeInformationControl informationControl) {
		Point location = getLocation();
		informationControl.setLocation(location);

		Point prefferedSize = informationControl.computeSizeHint();
		Point size = getSize(prefferedSize.x, prefferedSize.y);
		informationControl.setSize(size.x, size.y);
	}

	protected Point getLocation() {
		Point location = control.getParent().toDisplay(
				getControlLocation().x + getControlSize().x / 2,
				getControlLocation().y + getControlSize().y / 2);

		return location;
	}

	protected Point getSize(int prefferedWidth, int prefferedHeight) {
		Point location = getLocation();

		Point sizeHint = new Point(prefferedWidth, prefferedHeight);

		int width = getShell().getDisplay().getBounds().width - location.x
				- sizeHint.x;
		int height = getShell().getDisplay().getBounds().height - location.y
				- sizeHint.y;

		if (focused) {
			width = sizeHint.x;
			height = sizeHint.y;

			if (minWidth != SWT.DEFAULT) {
				width = Math.max(width, minWidth);
			}

			if (minHeight != SWT.DEFAULT) {
				height = Math.max(height, minHeight);
			}

			if (maxWidth != SWT.DEFAULT) {
				width = Math.min(width, maxWidth);
			}

			if (maxHeight != SWT.DEFAULT) {
				height = Math.min(height, maxHeight);
			}
		} else {
			width = sizeHint.x;
			height = sizeHint.y;

			if (minUnfocusedWidth != SWT.DEFAULT) {
				width = Math.max(width, minUnfocusedWidth);
			}

			if (minUnfocusedHeight != SWT.DEFAULT) {
				height = Math.max(height, minUnfocusedHeight);
			}

			if (maxUnfocusedWidth != SWT.DEFAULT) {
				width = Math.min(width, maxUnfocusedWidth);
			}

			if (maxUnfocusedHeight != SWT.DEFAULT) {
				height = Math.min(height, maxUnfocusedHeight);
			}
		}

		return new Point(width, height);
	}

	protected void keyPressed(org.eclipse.swt.events.KeyEvent e) {
		if (e.keyCode == SWT.F2 && e.stateMask == 0) {
			forceFocus();
		}
	};

	protected void keyReleased(org.eclipse.swt.events.KeyEvent e) {

	};
	
	@Override
	protected void mouseEnter(MouseEvent e) {
	}

	@Override
	protected void mouseHover(MouseEvent e) {
		show();
	}
	
	@Override
	protected void mouseExit(MouseEvent e) {
		mouseExit(e, 500);
	}
	
	protected void mouseExit(MouseEvent e, int delay) {
		if (ignoreMouseExit) {
			return;
		}

		if (this.informationControl != null) {
			CompositeInformationControl control = ((CompositeInformationControl) this.informationControl);
			Rectangle bounds = control.getBounds();
			Point mousePos = ((Control) e.widget).toDisplay(e.x, e.y);
			if (!bounds.contains(mousePos)) {
				
				// try cic children
				Shell[] shells = control.getComposite()
						.getShell().getShells();
				for (Shell shell : shells) {
					if (shell.getBounds().contains(
							((Control) e.widget).toDisplay(
									e.x, e.y))) {
						return;
					}
				}
				
				if (delay > 0) {
					// super.mouseExit(e) will be called in
					// MouseExitTimerThread after specified delay
					if (mouseExitTimerThread == null) {
						mouseExitTimerThread = new MouseExitTimerThread();
						mouseExitTimerThread.me = e;
						mouseExitTimerThread.delay = delay;
						mouseExitTimerThread.start();
					}	
				} else {
					super.mouseExit(e);
				}
		
			}
			return; 
		} else {
			super.mouseExit(e);
		}
	}

	public IInformationControl getFocusedControl() {
		CompositeInformationControl informationControl = createFocusedControl();

		initControl(informationControl);

		return informationControl;
	}

	public IInformationControl getUnfocusedControl() {
		CompositeInformationControl informationControl = createUnfocusedControl();

		initControl(informationControl);

		return informationControl;
	}

	protected CompositeInformationControl createFocusedControl() {
		return new CompositeInformationControl(getShell(),
				SWT.PRIMARY_MODAL | SWT.RESIZE | SWT.TOOL /*| SWT.ON_TOP*/, SWT.NONE, null);
	}

	protected CompositeInformationControl createUnfocusedControl() {
		return new CompositeInformationControl(getShell(), SWT.NO_TRIM
				| SWT.TOOL /*| SWT.ON_TOP*/, SWT.NONE, null);
	}
	
	protected class MouseExitTimerThread extends Thread implements Listener {
		int delay = 500;

		boolean hideTooltip = true;

		MouseEvent me;

		@Override
		public void run() {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					Display.getDefault().addFilter(SWT.MouseMove,
							MouseExitTimerThread.this);
					Display.getDefault().addFilter(SWT.MouseHover,
							MouseExitTimerThread.this);
					Display.getDefault().addFilter(SWT.MouseDown,
							MouseExitTimerThread.this);
				}
			});
			try {
				try {
					sleep(delay);
				} catch (InterruptedException e) {
				}
				mouseExitTimerThread = null;
			} finally {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						Display.getDefault().removeFilter(SWT.MouseMove,
								MouseExitTimerThread.this);
						Display.getDefault().removeFilter(SWT.MouseHover,
								MouseExitTimerThread.this);
						Display.getDefault().removeFilter(SWT.MouseDown,
								MouseExitTimerThread.this);
					}
				});
			}

			if (hideTooltip) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						CompositeTooltip.super.mouseExit(me);
					}
				});
			}
		}

		public void handleEvent(Event event) {
			if (event.type == SWT.MouseDown || event.type == SWT.MouseHover) {
				// immediately hide tooltip if user click outside tooltip area or hover on another control
				mouseExitTimerThread = null;
				hideTooltip = false;
				CompositeTooltip.super.mouseExit(me);
				interrupt();
			}
			if (event.type == SWT.MouseMove) {
				if (event.widget instanceof Control) {
					if (control != null && !control.isDisposed()) {
						if (CompositeTooltip.this.informationControl != null) {
							CompositeInformationControl cic = (CompositeInformationControl) CompositeTooltip.this.informationControl;
							if (cic.getComposite() != null
									&& !cic.getComposite().isDisposed()
									&& cic.getComposite().getShell() != null
									&& !cic.getComposite().getShell()
											.isDisposed()) {
								Control eventSource = (Control) event.widget;
								if (eventSource.getShell() == 
										getShell()
										|| eventSource.getShell() == cic
												.getComposite().getShell()
										|| Arrays.asList(
												cic.getComposite().getShell()
														.getShells()).contains(
												eventSource.getShell())) {
									Rectangle bounds = getControlBounds();
									Point absoluteLocation = control
											.getParent().toDisplay(bounds.x,
													bounds.y);
									Rectangle absoluteBounds = new Rectangle(
											absoluteLocation.x,
											absoluteLocation.y, bounds.width,
											bounds.height);

									if (absoluteBounds.contains(eventSource
											.toDisplay(event.x, event.y))
											|| cic.getBounds().contains(
													eventSource.toDisplay(
															event.x, event.y))) {
										hideTooltip = false;
										mouseExitTimerThread = null;
										interrupt();
									} else {
										// try cic children
										Shell[] shells = cic.getComposite()
												.getShell().getShells();
										for (Shell shell : shells) {
											if (shell.getBounds().contains(
													eventSource.toDisplay(
															event.x, event.y))) {
												hideTooltip = false;
												mouseExitTimerThread = null;
												interrupt();
												break;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

}
