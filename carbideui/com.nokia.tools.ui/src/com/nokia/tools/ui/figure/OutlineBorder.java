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
package com.nokia.tools.ui.figure;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;

/**
 * Copied partly from VE.
 * 
 */
public class OutlineBorder extends AbstractBorder {
	private static final Insets insets = new Insets(0, 0, 0, 0);

	private Color foreground = ColorConstants.black, background;
	private int alpha = 255;
	private int lineStyle = SWT.LINE_SOLID;
	private int lineWidth = 1;

	public OutlineBorder() {
	}

	public OutlineBorder(Color foreground, Color background) {
		this.foreground = foreground;
		this.background = background;
	}

	public OutlineBorder(int alpha, Color foreground, Color background) {
		this(foreground, background);
		setAlpha(alpha);
	}

	public OutlineBorder(Color foreground, Color background, int lineStyle) {
		this(foreground, background);
		this.lineStyle = lineStyle;
	}

	public OutlineBorder(int alpha, Color foreground, Color background,
			int lineStyle) {
		this(alpha, foreground, background);
		this.lineStyle = lineStyle;
	}

	public void setColors(Color foreground, Color background) {
		this.foreground = foreground;
		this.background = background;
	}

	public void setLineStyle(int aStyle) {
		lineStyle = aStyle;
	}

	/**
	 * @param alpha The alpha to set.
	 * 
	 * @since 1.1.0
	 */
	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	/**
	 * @return Returns the alpha.
	 * 
	 * @since 1.1.0
	 */
	public int getAlpha() {
		return alpha;
	}

	/**
	 * @return the lineWidth
	 */
	public int getLineWidth() {
		return lineWidth;
	}

	/**
	 * @param lineWidth the lineWidth to set
	 */
	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.Border#paint(org.eclipse.draw2d.IFigure,
	 *      org.eclipse.draw2d.Graphics, org.eclipse.draw2d.geometry.Insets)
	 */
	public void paint(IFigure aFigure, Graphics g, Insets insets) {
		Rectangle r = getPaintRectangle(aFigure, insets);
		r.resize(-1, -1); // Make room for the outline.
		try {
			g.setAlpha(getAlpha());
		} catch (SWTException e) {
			// Occurs if alpha's not available. No check on Graphics that tests
			// for this yet.
		}
		g.setForegroundColor(foreground);
		if (lineStyle != SWT.LINE_SOLID) {
			// Non-solid lines need a background color to be set. If we have one
			// use it, else compute it.
			if (background != null)
				g.setBackgroundColor(background);
			else {
				// If no background is set then make the background black
				// and set it to XOR true. This means the line will dash over
				// the background. The foreground will also XOR
				// so it only works well if the foreground is Black or Gray.
				// Colors
				// don't work well because they only paint true on black
				// areas
				g.setBackgroundColor(ColorConstants.black);
				g.setXORMode(true);
			}
		}
		g.setLineStyle(lineStyle);
		g.setLineWidth(lineWidth);
		g.drawRectangle(r);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.Border#getInsets(org.eclipse.draw2d.IFigure)
	 */
	public Insets getInsets(IFigure figure) {
		return insets;
	}
}
