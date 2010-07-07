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
package com.nokia.tools.theme.editing;


import java.awt.Rectangle;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.content.ThemeData;


public class ImageAdapter implements IImageAdapter {
	private ThemeData data;

	public ImageAdapter(ThemeData data) {
		this.data = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImageAdapter#getImage()
	 */
	public IImage getImage() {
		return getImage(false);
	}

	public IImage getImage(boolean supressImg) {
		return getImage(0, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImageAdapter#getImage(int, int)
	 */
	public IImage getImage(int width, int height) {
		SkinnableEntity element = data.getSkinnableEntity();
		IImage tempImage = getEntityImageFactory().createEntityImage(element,
				data.getData(), width, height);
		return tempImage;
	}

	protected IEntityImageFactory getEntityImageFactory() {
		return BasicEntityImageFactory.getInstance();
	}

	public boolean isAnimated() {
		SkinnableEntity element = data.getSkinnableEntity();

		if (element != null) {
			String type = element.isEntityType();
			if (ThemeTag.ELEMENT_BMPANIM.equalsIgnoreCase(type)) {
				return true;
			}
		}

		return false;
	}
	
	public boolean canBeAnimated() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImageAdapter#getContainerScreenResolution()
	 */
	public Rectangle getContainerScreenResolution() {
		try {
			Display d = (Display) data.getRoot().getAttribute(
					ContentAttribute.DISPLAY.name());
			return new Rectangle(d.getWidth(), d.getHeight());
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
		}
		return null;
	}

	public ThemeData getData() {
		return data;
	}
}
