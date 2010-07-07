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
package com.nokia.tools.screen.ui.propertysheet.tabbed;

import org.eclipse.jface.viewers.IFilter;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.screen.core.IPropertyAdapter;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;

/**
 * Takes care of general functionality like 'KeyPoint' does not support font,
 * background, etc.
 */
public class GeneralFilter implements IFilter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IFilter#select(java.lang.Object)
	 */
	public boolean select(Object toTest) {
		IContentData data = JEMUtil.getContentData(toTest);
		if (data != null) {
			IPropertyAdapter adapter = (IPropertyAdapter) data
					.getAdapter(IPropertyAdapter.class);
			if (adapter != null) {
				return adapter.supports(IPropertyAdapter.GENERAL);
			}
		}
		IScreenElement element = JEMUtil.getScreenElement(toTest);
		if (element != null) {
			IPropertyAdapter adapter = (IPropertyAdapter) element
					.getAdapter(IPropertyAdapter.class);
			return adapter.supports(IPropertyAdapter.GENERAL);
		}
		return false;
	}
}
