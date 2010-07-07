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
package com.nokia.tools.editing.ui.part;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gef.EditPart;
import org.eclipse.swt.widgets.Display;

import com.nokia.tools.editing.core.TypedAdapter;
import com.nokia.tools.editing.ui.Activator;

public class EditingAdapter extends TypedAdapter {
	private EditPart part;

	public EditingAdapter(EditPart part) {
		this.part = part;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 */
	public void notifyChanged(Notification notification) {
		if (Notification.REMOVING_ADAPTER == notification.getEventType()) {
			return;
		}
		if (part != null) {
			if (Display.getCurrent() != null) {
				try {
					part.refresh();
				} catch (Exception e) {
					Activator.error(e);
				}
			} else {
				// both visuals and children
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (part != null) {
							try {
								part.refresh();
							} catch (Exception e) {
								Activator.error(e);
							}
						}
					}
				});
			}
		}
	}

	/**
	 * @return the part
	 */
	public EditPart getPart() {
		return part;
	}
}
