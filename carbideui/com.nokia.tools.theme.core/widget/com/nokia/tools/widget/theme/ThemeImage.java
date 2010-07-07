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
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import com.nokia.tools.media.image.CoreImage;

public class ThemeImage extends BaseImage implements Colorizable {
	static final long serialVersionUID = -7967014983961820855L;

	private Color color;

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color the color to set
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
		RenderedImage image = getImage();
		if (image instanceof BufferedImage && color != null) {
			setImage(CoreImage.create(image).colorize(color).getAwt());
		}
		super.paintDefault(g);
	}
}
