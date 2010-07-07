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
package com.nokia.tools.theme.content;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.core.TypedAdapter;
import com.nokia.tools.editing.model.EditObject;

public abstract class ContentDataAdapter extends TypedAdapter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 */
	public void notifyChanged(Notification notification) {
		if (EditingUtil.isRemovingAdapter(notification, this)) {
			return;
		}
		if (notification.getFeature() instanceof EStructuralFeature) {
			IContentData modified = ContentAdapter
					.getContentData((EditObject) notification.getNotifier());
			if (modified != null) {
				contentDataModified(modified);
			}
		}
	}

	protected abstract void contentDataModified(IContentData modified);
}
