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
package com.nokia.tools.ui.action;

import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ZoomInAction;

/**
 * Customized zoom-in action that allows changing the {@link ZoomManager} at
 * runtime.
 * 
 * @version 1.0
 */
public class ShareableZoomInAction extends ZoomInAction {
	/**
	 * Constructs a zoom-in action.
	 * 
	 * @param zoomManager
	 *            the zoom manager.
	 */
	public ShareableZoomInAction(ZoomManager zoomManager) {
		super(zoomManager);
	}

	/**
	 * Sets a new zoom manager.
	 * 
	 * @param zoomManager
	 *            the new zoom manager.
	 */
	public void setZoomManager(ZoomManager zoomManager) {
		if (this.zoomManager != null) {
			this.zoomManager.removeZoomListener(this);
		}
		this.zoomManager = zoomManager;
		this.zoomManager.addZoomListener(this);
	}
}
