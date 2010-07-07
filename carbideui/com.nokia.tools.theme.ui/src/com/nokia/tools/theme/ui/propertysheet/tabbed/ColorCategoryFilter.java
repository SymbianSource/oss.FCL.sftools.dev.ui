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
package com.nokia.tools.theme.ui.propertysheet.tabbed;

import org.eclipse.jface.viewers.IFilter;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;

public class ColorCategoryFilter implements IFilter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IFilter#select(java.lang.Object)
	 */
	public boolean select(Object toTest) {
		IContentData data = null;
		if (toTest instanceof IContentData) {
			data = (IContentData) toTest;
		} else {
			IScreenElement element = JEMUtil.getScreenElement(toTest);
			if (element != null) {
				IScreenElement target = (IScreenElement) element
						.getTargetAdapter();
				if (target != null) {
					element = target;
				}
				data = element.getData();
			}
		}
		if (data == null) {
			return false;
		}
		IColorAdapter adapter = (IColorAdapter) data
				.getAdapter(IColorAdapter.class);
		return adapter != null;
	}
}
