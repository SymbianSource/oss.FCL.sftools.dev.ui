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

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.ui.adapter.IComponentAdapter;
import com.nokia.tools.widget.SComponent;
import com.nokia.tools.widget.SContainer;


public class ScreenComponentAdapter extends IComponentAdapter.Stub {
	private IScreenElement element;

	public ScreenComponentAdapter(IScreenElement element) {
		this.element = element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.adapter.IComponentAdapter.Stub#getSupportedTypes()
	 */
	@Override
	protected int getSupportedTypes() {
		return MODIFY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.adapter.IComponentAdapter.Stub#supportsAddChild(java.lang.Object)
	 */
	@Override
	protected boolean supportsAddChild(Object child) {
		if (!(element.getBean() instanceof SContainer)
				|| !(child instanceof EObject)) {
			return false;
		}
		SComponent component = (SComponent) EditingUtil
				.getBean((EObject) child);
		return ((SContainer) element.getBean()).isChildValid(component);
	}
}
