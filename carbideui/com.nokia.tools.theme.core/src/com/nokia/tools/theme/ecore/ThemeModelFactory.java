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
package com.nokia.tools.theme.ecore;

import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;

import com.nokia.tools.editing.beaninfo.BeaninfoModelFactory;
import com.nokia.tools.editing.core.InvocationAdapter;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.preview.ThemePreview;

public class ThemeModelFactory extends BeaninfoModelFactory {
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.beaninfo.BeaninfoModelFactory#createInvocationAdapter(java.lang.Object)
	 */
	@Override
	protected InvocationAdapter createInvocationAdapter(Object object) {
		return new ThemeInvocationAdapter(object);
	}

	public static EditObject createEditTree(ThemeBasicData data)
			throws Exception {
		EditObject parent = new ThemeModelFactory().createEditObject(data);
		InvocationAdapter adapter = (InvocationAdapter) EcoreUtil
				.getExistingAdapter(parent, InvocationAdapter.class);
		if (adapter != null) {
			parent.eAdapters().remove(adapter);
		}
		List children = data.getChildren();
		if (children != null) {
			for (Object child : children) {
				if (child instanceof ThemeBasicData) {
					EditObject childObject = createEditTree((ThemeBasicData) child);
					parent.getChildren().add(childObject);
				}
			}
		}
		if (data instanceof Theme) {
			ThemePreview preview = ((Theme) data).getThemePreview();
			EditObject previewObject = createEditTree(preview);
			parent.getChildren().add(previewObject);
		}
		parent.eAdapters().add(adapter);
		parent.eAdapters().add(new ThemeEditObjectAdapter());
		return parent;
	}
}
