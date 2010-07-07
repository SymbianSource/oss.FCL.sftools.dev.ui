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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.ui.tooltip.CompositeInformationControl;

public class FloatingToolbar extends DynamicTooltip {

	protected boolean stayOnTop = false;

	protected boolean restoreOnLastPosition = true;

	protected Rectangle lastPosition;

	protected Point oldControlLocation = null;

	protected Listener listener;

	private Listener filter;

	public FloatingToolbar(Object element, Object uiContainer, Object context,
			EStyle style) {
		super(element, uiContainer, context, style);
	}

	@Override
	protected CompositeInformationControl createFocusedControl() {
		return createUnfocusedControl();
	}

	protected CompositeInformationControl createUnfocusedControl() {
		informationControl = new ExtendedCompositeInformationControl(
				getShell(), SWT.NO_TRIM /* | SWT.TOOL */, SWT.NONE, null);

		final ExtendedCompositeInformationControl cic = (ExtendedCompositeInformationControl) informationControl;
		cic.setBackground(ColorConstants.white);
		if (filter == null) {
			// called many times, creates one only
			filter = new Listener() {
				public void handleEvent(Event event) {
					if ((event.widget instanceof Shell)
							&& cic != null
							&& cic.getPopupDialog() != null
							&& ((Shell) event.widget) == cic.getPopupDialog()
									.getShell()) {
						event.type = SWT.None;
					}
				}
			};

			// disable tooltip deactivation
			getShell().getDisplay().addFilter(SWT.Deactivate, filter);
		}

		// cic.addTitleAction(cic.new CloseAction());

		Action stayOnTopAction = new Action(Messages.ToolbarStyle_StayOnTop,
				Action.AS_CHECK_BOX) {
			@Override
			public void run() {
				setChecked(!isChecked());
				stayOnTop = isChecked();
			}

			@Override
			public void setChecked(boolean checked) {
				super.setChecked(checked);
				if (checked) {
					setImageDescriptor(UtilsPlugin
							.getImageDescriptor("icons/stayontop_checked.gif"));
					setHoverImageDescriptor(UtilsPlugin
							.getImageDescriptor("icons/stayontop_checked_hover.gif"));
				} else {
					setImageDescriptor(UtilsPlugin
							.getImageDescriptor("icons/stayontop.gif"));
					setHoverImageDescriptor(UtilsPlugin
							.getImageDescriptor("icons/stayontop_hover.gif"));
				}
			}
		};

		stayOnTopAction.setChecked(stayOnTop);

		cic.addTitleAction(stayOnTopAction);
		cic.addTitleAction(cic.new TooltipHelpAction(null));

		cic.getTitleMenu().addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Action(Messages.ToolbarStyle_Horizontal,
						Action.AS_CHECK_BOX) {
					@Override
					public void run() {
						update(selection, uiContainer, context,
								EStyle.HORIZONTAL_HORIZONTAL);
						super.run();
					}

					@Override
					public boolean isEnabled() {
						return style != EStyle.HORIZONTAL_HORIZONTAL;
					}
				});

				manager.add(new Action(Messages.ToolbarStyle_Vertical,
						Action.AS_CHECK_BOX) {
					@Override
					public void run() {
						update(selection, uiContainer, context,
								EStyle.VERTICAL_VERTICAL);
						super.run();
					}

					@Override
					public boolean isEnabled() {
						return style != EStyle.VERTICAL_VERTICAL;
					}
				});
			}
		});

		addContributedControls(cic.getComposite(), false);

		return cic;
	}

	@Override
	protected Point getLocation() {
		if (restoreOnLastPosition && lastPosition != null) {
			Point absLoc = control.toDisplay(lastPosition.x, lastPosition.y);
			Rectangle pos = new Rectangle(absLoc.x, absLoc.y,
					lastPosition.width, lastPosition.height);
			pos = validateCicBounds(pos);
			return new Point(pos.x, pos.y);
		}

		Point location = control.getParent().toDisplay(
				getControlLocation().x + 5, getControlLocation().y + 5);

		return location;
	}

	@Override
	protected void mouseExit(MouseEvent e) {
		if (stayOnTop) {
			return;
		}

		super.mouseExit(e);
	}

	@Override
	protected void mouseEnter(MouseEvent e) {
		show();
	}

	@Override
	public boolean show(Point location) {
		boolean showed = super.show(location);

		if (showed && this.informationControl != null) {

			Point loc = control.toDisplay(new Point(0, 0));
			Point size = control.getSize();
			((ExtendedCompositeInformationControl) this.informationControl)
					.setMoveArea(new Rectangle(loc.x, loc.y, size.x, size.y));

			this.informationControl.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					Shell shell = (Shell) e.widget;
					if (shell != null && !shell.isDisposed()) {
						Rectangle rect = shell.getBounds();
						if (rect != null) {
							// save CIC position relative from control
							Point relativePos = control.toControl(rect.x,
									rect.y);
							lastPosition = new Rectangle(relativePos.x,
									relativePos.y, rect.width, rect.height);
						}
					}
				}
			});
		}

		return showed;
	}

	public boolean isRestoreOnLastPosition() {
		return restoreOnLastPosition;
	}

	public void setRestoreOnLastPosition(boolean restoreOnLastPosition) {
		this.restoreOnLastPosition = restoreOnLastPosition;
	}

	public boolean isStayOnTop() {
		return stayOnTop;
	}

	public void setStayOnTop(boolean stayOnTop) {
		this.stayOnTop = stayOnTop;
	}

	@Override
	public void setControl(Control control) {
		super.setControl(control);
		if (control != null) {
			oldControlLocation = control.toDisplay(new Point(0, 0));

			// add resize & move listener
			listener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
					case SWT.Resize:
					case SWT.Move:
						checkBounds(event);
					default:
					}
				}

				void checkBounds(Event e) {
					if (FloatingToolbar.this.control == null
							|| FloatingToolbar.this.control.isDisposed()) {
						((Control) e.widget).removeListener(SWT.Resize, this);
						((Control) e.widget).removeListener(SWT.Move, this);
						return;
					}

					Point newControlLocation = FloatingToolbar.this.control
							.toDisplay(new Point(0, 0));

					if (informationControl != null) {
						// preserve CIC relative position from control

						Point newControlSize = FloatingToolbar.this.control
								.getSize();

						Rectangle cicBounds = ((CompositeInformationControl) informationControl)
								.getComposite().getShell().getBounds();

						Point relativePos = new Point(cicBounds.x
								- oldControlLocation.x, cicBounds.y
								- oldControlLocation.y);

						Rectangle newCicBounds = new Rectangle(
								newControlLocation.x + relativePos.x,
								newControlLocation.y + relativePos.y,
								cicBounds.width, cicBounds.height);

						newCicBounds = validateCicBounds(newCicBounds);

						if (newCicBounds != cicBounds) {
							((CompositeInformationControl) informationControl)
									.getComposite().getShell().setBounds(
											newCicBounds);
							((CompositeInformationControl) informationControl)
									.getComposite().getShell().update();
						}

						((ExtendedCompositeInformationControl) informationControl)
								.setMoveArea(new Rectangle(
										newControlLocation.x,
										newControlLocation.y, newControlSize.x,
										newControlSize.y));
					}

					oldControlLocation = newControlLocation;
				}
			};

			Control c = control;
			while (c != null) {
				c.addListener(SWT.Move, listener);
				c.addListener(SWT.Resize, listener);
				c = c.getParent();
			}
		}
	}

	protected Rectangle validateCicBounds(Rectangle bounds) {
		Point controlLocation = control.toDisplay(new Point(0, 0));
		Point controlSize = control.getSize();

		Rectangle cicBounds = new Rectangle(bounds.x, bounds.y, bounds.width,
				bounds.height);

		if (cicBounds.x + cicBounds.width > controlLocation.x + controlSize.x) {
			cicBounds.x = controlLocation.x + controlSize.x - cicBounds.width
					- 1;
		}

		if (cicBounds.y + cicBounds.height > controlLocation.y + controlSize.y) {
			cicBounds.y = controlLocation.y + controlSize.y - cicBounds.height
					- 1;
		}

		if (cicBounds.x < controlLocation.x) {
			cicBounds.x = controlLocation.x + 1;
		}

		if (cicBounds.y < controlLocation.y) {
			cicBounds.y = controlLocation.y + 1;
		}

		return cicBounds;
	}

	@Override
	public void dispose() {
		if (control != null && !control.isDisposed() && listener != null) {
			Control c = control;
			while (c != null) {
				c.removeListener(SWT.Move, listener);
				c.removeListener(SWT.Resize, listener);
				c = c.getParent();
			}
			listener = null;
		}
		// filter holds the reference to self and then the editor, so removes
		// this as well
		if (filter != null) {
			getShell().getDisplay().removeFilter(SWT.Deactivate, filter);
		}
		super.dispose();
	}
}
