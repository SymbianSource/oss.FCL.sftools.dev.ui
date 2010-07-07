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
package com.nokia.tools.platform.theme;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.util.List;

import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.platform.core.PlatformCorePlugin;

/**
 * The Class Defines the Color Object for a skinnableentity of type color.
 * 
 */
public class ColourGraphic extends ThemeGraphic {
	/**
	 * Constructor
	 */
	public ColourGraphic(ThemeBasicData data) {
		super(data);
	}

	public void setColour(String colour) {
		List ilList = getImageLayers();
		if ((ilList != null) && (ilList.size() > 0)) {
			ImageLayer il = (ImageLayer) ilList.get(0);
			il.setAttribute(ThemeTag.ATTR_COLOUR_RGB, colour);
		}
	}

	public String getColour() {
		List ilList = getImageLayers();
		if ((ilList != null) && (ilList.size() > 0)) {
			ImageLayer il = (ImageLayer) ilList.get(0);
			if (il.getAttribute(ThemeTag.ATTR_COLOUR_RGB) != null)
				return il.getAttribute(ThemeTag.ATTR_COLOUR_RGB);
			else if (il.getAttribute(ThemeTag.ATTR_COLOUR_IDX) != null)
				return ColorUtil.asHashString(Color.decode(il
						.getAttribute(ThemeTag.ATTR_COLOUR_IDX)), "0x");
		}
		return null;
	}

	public RenderedImage generateIcon(int width, int height) {
		try {
			Color c = null;
			String colour = ((ImageLayer) getImageLayers().get(0))
					.getAttribute(ThemeTag.ATTR_COLOUR_RGB);
			if (colour == null) {
				colour = ((ImageLayer) getImageLayers().get(0))
						.getAttribute(ThemeTag.ATTR_COLOUR_IDX);
				try {
					c = Color.decode(colour);
				} catch (Exception e) {
					c = Color.BLACK;
					PlatformCorePlugin.error(e);
				}
			} else {
				c = ColorUtil.toColor(colour);
			}
			return CoreImage.create().getBlankImage(width - 4, height - 4, c);
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
