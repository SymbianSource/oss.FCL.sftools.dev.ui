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
import org.eclipse.emf.common.notify.impl.NotificationImpl;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.platform.theme.SkinnableEntity;

public class ArtificialNotification extends NotificationImpl {
	private ThemeData data;
	private SkinnableEntity part = null;

	public ArtificialNotification(ThemeData data) {
		super(Notification.SET, null, null);
		if (!(data.getData() instanceof SkinnableEntity)) {
			throw new IllegalArgumentException(
					"Skinnable entity required, check implementation");
		}
		this.data = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.emf.common.notify.impl.NotificationImpl#getFeature()
	 */
	@Override
	public Object getFeature() {
		return EditingUtil.getFeature(data.getResource(), "themeGraphic");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.emf.common.notify.impl.NotificationImpl#getNotifier()
	 */
	@Override
	public Object getNotifier() {
		return data.getResource();
	}
	
	public SkinnableEntity getPart() {
		return part;
	}
	
	public void setPart(SkinnableEntity part) {
		this.part = part;
	}
}
