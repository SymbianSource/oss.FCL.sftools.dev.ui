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

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public abstract class CustomTooltip {

	protected Control control;

	protected IInformationControl informationControl;

	protected ICustomTooltipControlCreator controlCreator;

	protected boolean focused;

	protected boolean disabled;

	protected CustomTooltip parent;

	private boolean mouseDown;

	private boolean hideOnMouseUp;

	private boolean hideWithParents;

	// private Control oldFocusedControl;

	private Listener mouseListener, keyListener;

	private Shell shell;

	public CustomTooltip() {
	}

	public void setControlCreator(ICustomTooltipControlCreator creator) {
		assert creator != null;

		this.controlCreator = creator;
	}

	public void setControl(Control control) {
		assert control != null;
		// assert this.control == null;

		if (this.control != null) {
			unregisterListeners();
		}

		this.control = control;
		shell = control.getShell();

		registerListeners();
	}

	public void forceFocus() {
		assert control != null;

		if (informationControl != null) {
			informationControl.dispose();
			informationControl = null;
		}
		focused = true;
		show();
		informationControl.setFocus();
		informationControl.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
				focused = false;
			}
		});
	}

	public void dispose() {
		unregisterListeners();

		if (informationControl != null) {
			informationControl.dispose();
			informationControl = null;
		}
	}

	protected void unregisterListeners() {
		if (control != null) {
			if (mouseListener != null) {
				getShell().getDisplay().removeFilter(SWT.MouseDown,
						mouseListener);
				getShell().getDisplay()
						.removeFilter(SWT.MouseUp, mouseListener);
				getShell().getDisplay().removeFilter(SWT.MouseHover,
						mouseListener);
				getShell().getDisplay().removeFilter(SWT.MouseMove,
						mouseListener);
				mouseListener = null;
			}
			if (keyListener != null) {
				getShell().getDisplay().removeFilter(SWT.KeyDown, keyListener);
				if (control != null && !control.isDisposed()) {
					control.removeListener(SWT.Dispose, keyListener);
				}
				keyListener = null;
			}
		}
	}

	protected void registerListeners() {
		if (control != null) {
			mouseListener = new MouseMoveListener();

			control.getDisplay().addFilter(SWT.MouseDown, mouseListener);
			control.getDisplay().addFilter(SWT.MouseUp, mouseListener);
			control.getDisplay().addFilter(SWT.MouseMove, mouseListener);
			control.getDisplay().addFilter(SWT.MouseHover, mouseListener);

			keyListener = new Listener() {
				public void handleEvent(Event event) {
					if (SWT.KeyDown == event.type) {
						if (event.character == 0x1B) // ESC
							hide();
					}
					if (SWT.Dispose == event.type) {
						if (event.widget == control) {
							dispose();
						}
					}
				}
			};

			control.getDisplay().addFilter(SWT.KeyDown, keyListener);
			control.addListener(SWT.Dispose, keyListener);
		}
	}

	protected abstract void keyPressed(KeyEvent e);

	protected abstract void keyReleased(KeyEvent e);

	protected void mouseEnter(MouseEvent e) {
		show();
	}

	protected void mouseHover(MouseEvent e) {
	}

	protected void mouseExit(MouseEvent e) {
		if (focused) {
			return;
		}
		hide();
	}

	public void hide() {
		hide(false);
	}

	public void hide(boolean parents) {
		if (informationControl != null) {
			// do not hide tooltip while mouse is down !!!
			// because mouse up event is fired by SWT to control under tooltip
			// :-((
			if (mouseDown) {
				hideOnMouseUp = true;
				hideWithParents = parents;
				return;
			}

			try {
				informationControl.dispose();
				informationControl = null;
				focused = false;

				// // try to restore old focus
				// if (oldFocusedControl != null &&
				// !oldFocusedControl.isDisposed()) {
				// try {
				// oldFocusedControl.setFocus();
				// } catch (Exception e) {
				// // control already disposed
				// }
				// }
			} finally {
				if (parents && parent != null) {
					parent.hide(parents);
				}
			}
		}
	}

	public boolean show() {
		return show(null);
	}

	public boolean show(Point location) {
		if (control == null || informationControl != null || disabled
				|| !isControlEnabled()) {
			return false;
		}

		// oldFocusedControl = control.getDisplay().getFocusControl();

		if (focused) {
			informationControl = controlCreator.getFocusedControl();
		} else {
			informationControl = controlCreator.getUnfocusedControl();
		}

		if (location != null) {
			informationControl.setLocation(location);
		}

		informationControl.setVisible(true);

		final KeyAdapter adapter = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				CustomTooltip.this.keyPressed(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				CustomTooltip.this.keyReleased(e);
			}
		};

		final Control focusedControl = getShell().getDisplay()
				.getFocusControl();

		informationControl.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (focusedControl != null && !focusedControl.isDisposed()) {
					focusedControl.removeKeyListener(adapter);
				}
				informationControl = null;
				focused = false;
			}
		});

		if (focusedControl != null) {
			focusedControl.addKeyListener(adapter);
		}

		return true;
	}

	protected class MouseMoveListener implements Listener {
		public void handleEvent(Event event) {
			if (event.type == SWT.MouseDown) {
				mouseDown = true;
				return;
			}

			if (event.type == SWT.MouseUp || event.type == SWT.MouseHover) {
				mouseDown = false;
				if (hideOnMouseUp) {
					hideOnMouseUp = false;
					hide(hideWithParents);
					return;
				}
			}

			if (event.type == SWT.MouseMove || event.type == SWT.MouseHover) {
				if (event.widget instanceof Control) {
					if (control != null && !control.isDisposed()
							&& !event.widget.isDisposed()) {
						Control eventSource = (Control) event.widget;
						Rectangle bounds = getControlBounds();
						Point absoluteLocation = control.getParent().toDisplay(
								bounds.x, bounds.y);
						Rectangle absoluteBounds = new Rectangle(
								absoluteLocation.x, absoluteLocation.y,
								bounds.width, bounds.height);

						if (!absoluteBounds.contains(eventSource.toDisplay(
								event.x, event.y))) {

							if (informationControl != null) {
								CustomTooltip.this.mouseExit(new MouseEvent(
										event));
							}
						} else {
							if (informationControl == null) {
								// test if event is from control or it children
								while (eventSource != null
										&& eventSource != control) {
									eventSource = eventSource.getParent();
								}
								// if yes, the fire mouseMove or mouseHover
								if (eventSource != null) {
									if (event.type == SWT.MouseMove) {
										CustomTooltip.this
												.mouseEnter(new MouseEvent(
														event));
									}
									if (event.type == SWT.MouseHover) {
										CustomTooltip.this
												.mouseHover(new MouseEvent(
														event));
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public void disable() {
		disabled = true;
	}

	public void enable() {
		disabled = false;
	}

	public CustomTooltip getParent() {
		return parent;
	}

	public void setParent(CustomTooltip parent) {
		this.parent = parent;
	}

	public interface ICustomTooltipControlCreator {
		public IInformationControl getUnfocusedControl();

		public IInformationControl getFocusedControl();
	}

	protected Rectangle getControlBounds() {
		return control.getBounds();
	}

	protected Point getControlSize() {
		return new Point(getControlBounds().width, getControlBounds().height);
	}

	protected Point getControlLocation() {
		return new Point(getControlBounds().x, getControlBounds().y);
	}

	protected boolean isControlEnabled() {
		return control.isEnabled();
	}

	protected Shell getShell() {
		if (control == null || control.isDisposed()) {
			// this can happen
			return shell;
		}
		return control.getShell();
	}

}
