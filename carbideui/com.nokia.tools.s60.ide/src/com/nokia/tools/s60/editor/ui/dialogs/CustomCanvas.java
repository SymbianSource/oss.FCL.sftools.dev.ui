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

package com.nokia.tools.s60.editor.ui.dialogs;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class CustomCanvas extends Canvas {

	private boolean selected = false;

	public CustomCanvas(final Composite parent, int style, final int rectWidth,
			final int rectHeight) {
		super(parent, style);

		this.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				int x = 10 - (((int) rectWidth / 2) + 1);
				int y = 10 - (((int) rectHeight / 2) + 1);

				GC gc = e.gc;
				if (selected) {
					gc.setForeground(ColorConstants.menuForegroundSelected);
					gc.setBackground(ColorConstants.menuForegroundSelected);
				} else {
					gc.setForeground(ColorConstants.black);
					gc.setBackground(ColorConstants.black);
				}
				Rectangle mainRect = new Rectangle(x, y, rectWidth + 1, rectHeight + 1);
				gc.fillRectangle(mainRect);
				gc.drawRectangle(mainRect);
				gc.dispose();
			}
		});
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
