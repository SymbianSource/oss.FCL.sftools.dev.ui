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
package com.nokia.tools.media.font;

import java.awt.Font;
import java.awt.Graphics;

public interface IFontResource {
	int getAscent(Graphics g);

	int getDescent(Graphics g);

	int getSize(Graphics g);
	
	int stringWidth(Graphics g, String text);

	/**
	 * Draws the text given by the specified string, using this graphics
	 * context's current color. The baseline of the leftmost character is at
	 * position (<i>x</i>,&nbsp;<i>y</i>) in this graphics context's
	 * coordinate system.
	 * 
	 * @param g the graphics to paint to
	 * @param s the string to be drawn.
	 * @param x0 the <i>x</i> coordinate.
	 * @param y0 the <i>y</i> coordinate.
	 */
	void drawString(Graphics g, String text, int x, int y);
	
	Font getFont();
}
