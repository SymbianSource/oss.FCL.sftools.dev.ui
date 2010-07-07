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

public class AWTFont implements IFontResource {
	private Font font;

	protected AWTFont(Font font) {
		this.font = font;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.font.IFontResource#drawString(java.awt.Graphics,
	 *      java.lang.String, int, int)
	 */
	public void drawString(Graphics g, String text, int x, int y) {
		Font previous = g.getFont();
		g.setFont(font);
		g.drawString(text, x, y);
		g.setFont(previous);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.font.IFontResource#getAscent(java.awt.Graphics)
	 */
	public int getAscent(Graphics g) {
		Font previous = g.getFont();
		g.setFont(font);
		int ascent = g.getFontMetrics().getAscent();
		g.setFont(previous);
		return ascent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.font.IFontResource#getDescent(java.awt.Graphics)
	 */
	public int getDescent(Graphics g) {
		Font previous = g.getFont();
		g.setFont(font);
		int descent = g.getFontMetrics().getDescent();
		g.setFont(previous);
		return descent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.font.IFontResource#getSize(java.awt.Graphics)
	 */
	public int getSize(Graphics g) {
		return font.getSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.font.IFontResource#stringWidth(java.awt.Graphics,
	 *      java.lang.String)
	 */
	public int stringWidth(Graphics g, String text) {
		Font previous = g.getFont();
		g.setFont(font);
		int width = g.getFontMetrics().stringWidth(text);
		g.setFont(previous);
		return width;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.font.IFontResource#getFont()
	 */
	public Font getFont() {
		return font;
	}
}
