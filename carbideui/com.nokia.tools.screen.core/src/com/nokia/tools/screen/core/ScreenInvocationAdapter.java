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
package com.nokia.tools.screen.core;

import org.eclipse.emf.ecore.EObject;

import com.nokia.tools.editing.beaninfo.BeaninfoInvocationAdapter;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.widget.SComponent;
import com.nokia.tools.widget.SContainer;

public class ScreenInvocationAdapter extends BeaninfoInvocationAdapter {

	public ScreenInvocationAdapter(Object bean) {
		super(bean);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.beaninfo.InvocationAdapter#add(java.lang.Object,
	 *      int)
	 */
	@Override
	protected void add(Object child, int position) {
		Object childBean = EditingUtil.getBean((EObject) child);
		if (getBean() instanceof SContainer && childBean instanceof SComponent) {
			((SContainer) getBean()).addChild((SComponent) childBean, position);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.beaninfo.InvocationAdapter#remove(java.lang.Object)
	 */
	@Override
	protected void remove(Object child) {
		Object childBean = EditingUtil.getBean((EObject) child);
		if (getBean() instanceof SContainer && childBean instanceof SComponent) {
			((SContainer) getBean()).removeChild((SComponent) childBean);
		}
	}
}
