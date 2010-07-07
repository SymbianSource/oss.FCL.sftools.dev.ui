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

import com.nokia.tools.screen.core.IPropertyAdapter;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;

public class AdvancedFilter extends GeneralFilter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.propertysheet.tabbed.GeneralFilter#select(java.lang.Object)
	 */
	@Override
	public boolean select(Object toTest) {
		if (!super.select(toTest)) {
			return false;
		}
		IScreenElement element = JEMUtil.getScreenElement(toTest);
		if (element == null) {
			return false;
		}
		if (element.getTargetAdapter() != null) {
			element = element.getTargetAdapter();
		}

		AbstractPropertyTabCustomizer advancedAdapter = (AbstractPropertyTabCustomizer) element
				.getAdapter(AbstractPropertyTabCustomizer.class);
		if (null != advancedAdapter)
			return advancedAdapter.accept(this.getClass());

		IPropertyAdapter adapter = (IPropertyAdapter) element
				.getAdapter(IPropertyAdapter.class);
		return adapter.supports(IPropertyAdapter.ADVANCED);
	}
}
