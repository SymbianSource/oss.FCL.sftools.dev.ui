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
package com.nokia.tools.widget.theme;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class ThemeLineGraphic extends ThemeGraphic implements Colorizable {

	private static final long serialVersionUID = 6926990165070953917L;

	/**
	 * Category property - controls line visibility in entire theme. For use in
	 * elements of type: qgn_graf_line_primary_[orientation]_dashed
	 */
	private boolean drawLines;
	private Color color;

	public boolean isDrawLines() {
		return drawLines;
	}

	public void setDrawLines(boolean drawLines) {
		this.drawLines = drawLines;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.widget.theme.Colorizable#getColor()
	 */
	public Color getColor() {
		return color;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.widget.theme.Colorizable#setColor(java.awt.Color)
	 */
	public void setColor(Color color) {
		this.color = color;
		setForeground(color);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.widget.SComponent#paintDefault(java.awt.Graphics)
	 */
	@Override
	protected void paintDefault(Graphics g) {
		if (drawLines) {
			super.paintDefault(g);
		} else {
			if (null != color) {
				Rectangle bounds = g.getClipBounds();
				g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			}
		}
	}
}
