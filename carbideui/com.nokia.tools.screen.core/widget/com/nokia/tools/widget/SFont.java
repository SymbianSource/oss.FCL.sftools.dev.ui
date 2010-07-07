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
package com.nokia.tools.widget;

import com.nokia.tools.media.font.FontUtil;
import com.nokia.tools.media.font.IFontResource;

/**
 * This the customized font used in the actual devieces.
 * 
 * @author shji
 * @version $Revision: 1.4 $ $Date: 2010/04/21 14:45:18 $
 */
public class SFont {
	private String name;
	private int style;
	private int size;

	/**
	 * Constructs a new font.
	 * 
	 * @param name the font family, e.g. "Arial", etc.
	 * @param face the font face, value can be either Font.PLAIN, FONT.BOLD,
	 *            Font.ITALIC.
	 * @param size size of the font in number of pixcels.
	 */
	public SFont(String name, int face, int size) {
		this.name = name;
		this.style = face;
		this.size = size;
	}

	/**
	 * @return Returns the face.
	 */
	public int getStyle() {
		return style;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the size.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Converts this font to another one.
	 * 
	 * @returnt the newly created fonts.
	 */
	public IFontResource toFont() {
		return FontUtil.getFontResource(name, style, size);
	}
}
