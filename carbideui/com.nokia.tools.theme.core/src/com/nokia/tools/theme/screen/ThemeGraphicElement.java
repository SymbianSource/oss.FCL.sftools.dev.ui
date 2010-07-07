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
package com.nokia.tools.theme.screen;

import java.awt.image.RenderedImage;

import com.nokia.tools.screen.core.IScreenContext;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.content.ThemeScreenElementData;
import com.nokia.tools.widget.theme.BaseImage;
import com.nokia.tools.widget.theme.ImageProvider;
import com.nokia.tools.widget.theme.ThemeGraphic;

public class ThemeGraphicElement extends ThemeElement {
	public ThemeGraphicElement(ThemeData data) {
		super(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.core.IScreenElement.ScreenElementAdapter#createWidget()
	 */
	@Override
	protected Object createWidget() {
		return new ThemeGraphic();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.screen.ThemeElement#initWidgetSpi(com.nokia.tools.screen.core.IScreenContext)
	 */
	@Override
	protected void initWidgetSpi(IScreenContext context) {
		super.initWidgetSpi(context);
		ThemeData data = (ThemeData) getData();
		
		BaseImage bean = (BaseImage) getBean();
		ImageProvider provider = data.getImageProvider();
		if (data instanceof ThemeScreenElementData) {
			// for preview element, in most cases image will be generated and
			// displayed, thus it's better to generate here, which is probably
			// in a non-display, non-awt thread for faster image loading and
			// better responsiveness
			RenderedImage img = provider.getImage();
			bean.setImage(img);
		} else {
			// other elements will not be displayed on the awt screen, thus
			// image generation is probably not needed, however, we still give
			// the provider here just in case..
			bean.setProvider(provider);
		}
	}
}
