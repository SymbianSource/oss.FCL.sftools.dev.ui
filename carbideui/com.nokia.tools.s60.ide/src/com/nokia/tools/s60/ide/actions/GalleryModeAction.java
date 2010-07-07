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
package com.nokia.tools.s60.ide.actions;

import org.eclipse.jface.action.Action;

import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.views.GalleryPage;

/**
 * This action can be used to toggle the gallery modes: all or user defined.
 */
public class GalleryModeAction extends Action {
	private GalleryPage view;

	public GalleryModeAction(GalleryPage view) {
		this.view = view;

		setToolTipText(ActionMessages.GalleryModeAction_tooltip);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/gallery_full_to_user16x16.png"));
		setChecked(view.getMode() == GalleryPage.MODE_ALL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#getStyle()
	 */
	@Override
	public int getStyle() {
		return AS_CHECK_BOX;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		if (isChecked())
			view.setMode(GalleryPage.MODE_ALL);
		else
			view.setMode(GalleryPage.MODE_USER);
	}
}
