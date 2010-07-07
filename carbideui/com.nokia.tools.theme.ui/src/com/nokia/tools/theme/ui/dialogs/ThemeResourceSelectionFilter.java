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
package com.nokia.tools.theme.ui.dialogs;

import org.eclipse.jface.viewers.IFilter;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContentData;

public class ThemeResourceSelectionFilter implements IFilter {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IFilter#select(java.lang.Object)
	 */
	public boolean select(Object toTest) {
		if (toTest instanceof IContentData) {
			if (((IContentData) toTest)
					.getAttribute(ContentAttribute.APP_UID
							.name()) != null) {
				return true;
			}
		}
		return false;
	}
}