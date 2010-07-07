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
package com.nokia.tools.editing.core;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.NotificationImpl;

import com.nokia.tools.editing.model.EditDiagram;
import com.nokia.tools.editing.model.EditObject;

/**
 * This adapter is meant primarily for the edit object in the active editing
 * diagram. It forwards all notifications triggered from the target changes to
 * the root diagram. The {@link #selfDispatch(Runnable)} method can be used to
 * suppress notifications, which is sometimes needed when the feature changes
 * are necessary during the notification handling and thus to avoid triggering
 * nested notifications.
 * 
 */
public abstract class EditObjectAdapter extends TypedAdapter {
	private boolean isSelfDispatching;

	/**
	 * The routine that updates widget in the
	 * {@link #handleNotification(Notification)} method should not trigger the
	 * notification events. This method can be used to perform such work safely.
	 * 
	 * @param runnable
	 *            the work that will be performed.
	 */
	public void selfDispatch(Runnable runnable) {
		if (isSelfDispatching) {
			runnable.run();
		} else {
			isSelfDispatching = true;
			try {
				runnable.run();
			} finally {
				isSelfDispatching = false;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 */
	public final void notifyChanged(Notification notification) {
		try {
			if (isSelfDispatching) {
				return;
			}
			notifyChangedSpi(notification);
		} finally {
			if (EditingUtil.isRemovingAdapter(notification, this)) {
				return;
			}
			// updates the diagram, here we update regardless of self
			// dispatching or not, the actual updates will be queued and
			// optimized by the adapter itself
			EditDiagram diagram = EditingUtil
					.getDiagram((EditObject) getTarget());
			if (diagram != null) {
				for (Adapter adapter : diagram.eAdapters()) {
					// change the notifier to self, the diagram only cares about
					// the notifier
					adapter.notifyChanged(new NotificationImpl(notification
							.getEventType(), notification.getOldValue(),
							notification.getNewValue()) {

						/*
						 * (non-Javadoc)
						 * 
						 * @see org.eclipse.emf.common.notify.impl.NotificationImpl#getNotifier()
						 */
						@Override
						public Object getNotifier() {
							return getTarget();
						}
					});
				}
			}
		}
	}

	/**
	 * Called when the notification happens.
	 * 
	 * @param notification
	 *            the event notification.
	 */
	protected abstract void notifyChangedSpi(Notification notification);
}
