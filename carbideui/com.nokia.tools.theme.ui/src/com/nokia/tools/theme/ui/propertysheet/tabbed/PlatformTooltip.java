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

package com.nokia.tools.theme.ui.propertysheet.tabbed;

import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.nokia.tools.ui.tooltip.CustomTooltip;


public class PlatformTooltip extends CustomTooltip implements
		CustomTooltip.ICustomTooltipControlCreator {

	protected String content;

	protected int minWidth = SWT.DEFAULT;

	protected int minHeight = SWT.DEFAULT;

	protected int maxWidth = SWT.DEFAULT;

	protected int maxHeight = SWT.DEFAULT;

	protected int minUnfocusedWidth = SWT.DEFAULT;

	protected int minUnfocusedHeight = SWT.DEFAULT;

	protected int maxUnfocusedWidth = SWT.DEFAULT;

	protected int maxUnfocusedHeight = SWT.DEFAULT;

	protected boolean showStatus = true;

	public PlatformTooltip() {
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

	public void setContent(String content) {
		this.content = content;
	}

	public void initControl(DefaultInformationControl informationControl) {

		Point point = control.getParent().toDisplay(
				control.getLocation().x + control.getSize().x,
				control.getLocation().y + control.getSize().y * 2 / 3);

		informationControl.setLocation(point);

		Display display = control.getDisplay();

		Rectangle trim = informationControl.computeTrim();

		int width = display.getBounds().width - point.x - trim.width - 20;
		int height = display.getBounds().height - point.y - trim.height - 16;

		if (focused) {

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

		GC gc = new GC(control);
		try {
			Point p = gc.textExtent(Messages.Label_FocusTooltip);
			height -= p.y * 2;
			if (height < 0) {
				height = 0;
			}
		} finally {
			gc.dispose();
		}

		informationControl.setSizeConstraints(width, height);

		informationControl.setInformation(content);

		if (focused) {
			Point sizeHint = informationControl.computeSizeHint();

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
			Point sizeHint = informationControl.computeSizeHint();

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

		informationControl.setSize(width, height);
	}

	public void showGainFocusMessage(boolean b) {
		showStatus = b;
	}

	protected void keyPressed(org.eclipse.swt.events.KeyEvent e) {
		if (e.keyCode == SWT.F2 && e.stateMask == 0) {
			forceFocus();
		}
	};

	protected void keyReleased(org.eclipse.swt.events.KeyEvent e) {

	};

	public IInformationControl getFocusedControl() {

		DefaultInformationControl informationControl = new DefaultInformationControl(
				PlatformTooltip.this.control.getShell(), SWT.RESIZE | SWT.TOOL,
				SWT.H_SCROLL | SWT.V_SCROLL, new HTMLTextPresenter(false), null);

		initControl(informationControl);

		return informationControl;
	}

	public IInformationControl getUnfocusedControl() {
		DefaultInformationControl informationControl = new DefaultInformationControl(
				PlatformTooltip.this.control.getShell(),
				SWT.NO_TRIM | SWT.TOOL, SWT.NULL, new HTMLTextPresenter(true),
				showStatus ? Messages.Label_FocusTooltip : null);

		initControl(informationControl);

		return informationControl;
	}
}
