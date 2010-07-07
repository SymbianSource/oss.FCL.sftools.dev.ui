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

import org.eclipse.swt.graphics.Image;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;

public class WidgetSectionLabelProvider extends DisposableLabelProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.propertysheet.tabbed.DisposableLabelProvider#createImage(java.lang.Object)
	 */
	@Override
	protected Image createImage(Object element) {
		IScreenElement adapter = JEMUtil.getScreenElement(element);
		if (adapter != null) {
			return adapter.getImage();
		}
		IContentData data = JEMUtil.getContentData(element);
		if (data != null) {
			return data.getIcon(false);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		IScreenElement adapter = JEMUtil.getScreenElement(element);
		if (adapter != null) {
			return adapter.getText();
		}
		IContentData data = JEMUtil.getContentData(element);
		if (data != null) {
			return data.getName();
		}
		return null;
	}
}
