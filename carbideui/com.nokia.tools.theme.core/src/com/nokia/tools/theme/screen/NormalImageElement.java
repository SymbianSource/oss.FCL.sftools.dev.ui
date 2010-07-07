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

import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.platform.theme.preview.PreviewTagConstants;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.core.IScreenContext;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.content.ThemeScreenElementData;
import com.nokia.tools.theme.core.Activator;
import com.nokia.tools.widget.theme.NormalImage;
import com.nokia.tools.widget.theme.ThemeImage;

public class NormalImageElement extends ThemeElement {
	public NormalImageElement(ThemeData data) {
		super(data);
	}

	public NormalImageElement(ThemeScreenElementData data) {
		super(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.core.IScreenElement.ScreenElementAdapter#createWidget()
	 */
	@Override
	protected Object createWidget() {
		return new NormalImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.screen.ThemeElement#initWidgetSpi(com.nokia.tools.screen.core.IScreenContext)
	 */
	@Override
	protected void initWidgetSpi(IScreenContext context) {
		super.initWidgetSpi(context);

		ThemeImage bean = (ThemeImage) getBean();
		ThemeBasicData data = ((ThemeData) getData()).getData();
		Theme theme = (Theme) data.getRoot();
		String fileName = data
				.getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_IMAGE);
		fileName = theme.getFileName(fileName,
				data instanceof PreviewElement ? ((PreviewElement) data)
						.getDisplay() : null);
		if (fileName != null) {
			bean.setUrl(FileUtils.toURL(fileName).toExternalForm());
		} else {
			Activator
					.warn("File: "
							+ data
									.getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_IMAGE)
							+ " is not found.");
		}

		String color = data
				.getAttributeValue(PreviewTagConstants.ATTR_IMAGEFILE_COLOR);
		if (!StringUtils.isEmpty(color)) {
			bean.setColor(ColorUtil.toColor(color));
		}
		String paintScaleMode = data
				.getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_SCALE_MODE);
		bean.setScaleMode(paintScaleMode);
	}
}
