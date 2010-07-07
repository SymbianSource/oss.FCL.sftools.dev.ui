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

public class ThemeText extends BaseText implements Colorizable {
	static final long serialVersionUID = 5940142417842064681L;

	private Color color;

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
	 * @see com.nokia.tools.widget.theme.BaseText#updateBounds()
	 */
	@Override
	protected void updateBounds() {
	}
}
