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
import org.eclipse.emf.ecore.EStructuralFeature;

import com.nokia.tools.editing.core.EditObjectAdapter;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.adapter.IComponentAdapter;
import com.nokia.tools.platform.layout.LayoutNode;
import com.nokia.tools.theme.core.LayoutComponentAdapter;

public class LayoutEditObjectAdapter extends EditObjectAdapter implements
		IAdaptable {
	private EditObject previewEditObject;

	public LayoutEditObjectAdapter(EditObject previewEditObject) {
		this.previewEditObject = previewEditObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.core.EditObjectAdapter#notifyChangedSpi(org.eclipse.emf.common.notify.Notification)
	 */
	@Override
	protected void notifyChangedSpi(Notification notification) {
		if (notification.getNotifier() instanceof EditObject
				&& notification.getFeature() instanceof EStructuralFeature
				&& EditingUtil.isValidFeature((EStructuralFeature) notification
						.getFeature())) {
			Object bean = EditingUtil.getBean((EObject) notification
					.getNotifier());
			if (bean instanceof LayoutNode) {
				EditingUtil.setFeatureValue(previewEditObject, "layoutNode",
						bean);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IComponentAdapter.class == adapter) {
			return new LayoutComponentAdapter((EObject) getTarget());
		}
		return null;
	}
}
