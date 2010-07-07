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
package com.nokia.tools.theme.ui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.screen.core.IScreenAdapter;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.content.ThemeUtil;

/**
 * The class is designed to provide support for Category view it is accessing
 * categories. It connects to base graphics and read 2 levels all in Base
 * graphics. In addition it provides images for right hand side of the same view
 * - editable places
 */
public class ThemeCategoryProvider {
	public static IContentData[] getRootLevelElements(IContent content) {
		List<IContentData> returnedData = new ArrayList<IContentData>();
		if (content != null) {
			for (IContentData element : content.getChildren()) {
				if (element.getAdapter(IScreenAdapter.class) == null) {
					returnedData.add(element);
				}
			}
		}
		return returnedData.toArray(new IContentData[returnedData.size()]);
	}

	/**
	 * The method is used to access the rectangle to be painted on each of the
	 * images in category view, Right side of Resources View
	 * 
	 * @param cat
	 * @return
	 */
	public static Rectangle getCategoryHighlightRect(IContentData cat) {
		if (cat instanceof ThemeData) {
			ThemeData category = (ThemeData) cat;
			return ThemeUtil.getLayoutRect(category.getData());
		}
		return null;
	}

	/**
	 * Get the resolution of theme that is the owner of the element It is used
	 * for category view (Resources view) to get dimensions of rectangle and
	 * than a call to getCategoryHighlightRect returns rectangle that have to be
	 * painted
	 * 
	 * @param data any element inside the theme
	 * @return
	 */
	public static Rectangle getResolution(IContentData data) {
		return ThemeUtil.getResolution(((ThemeData) data).getData().getRoot());
	}
}
