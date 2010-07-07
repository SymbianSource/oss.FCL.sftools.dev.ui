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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;

import com.nokia.tools.editing.core.EditObjectAdapter;
import com.nokia.tools.editing.ui.adapter.IComponentAdapter;
import com.nokia.tools.theme.core.ThemeComponentAdapter;

public class ThemeEditObjectAdapter extends EditObjectAdapter implements
		IAdaptable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.core.EditObjectAdapter#notifyChangedSpi(org.eclipse.emf.common.notify.Notification)
	 */
	@Override
	protected void notifyChangedSpi(Notification notification) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IComponentAdapter.class == adapter) {
			return new ThemeComponentAdapter((EObject) getTarget());
		}
		return null;
	}
}
