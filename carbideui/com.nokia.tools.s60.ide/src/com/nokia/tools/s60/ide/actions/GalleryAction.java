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

import java.text.MessageFormat;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Control;

import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.views.GalleryPage;
import com.nokia.tools.screen.ui.gallery.IGalleryScreenProvider.IGalleryScreen;

/**
 * This action is used for showing the selected gallery screen.
 */
public class GalleryAction extends Action {
	private GalleryPage view;
	private IGalleryScreen screen;

	/**
	 * Constructs an action.
	 * 
	 * @param view the current gallery.
	 * @param screen the screen to be generated.
	 */
	public GalleryAction(GalleryPage view, IGalleryScreen screen) {
		this.view = view;
		this.screen = screen;
		setText(screen.getName());
		setDescription(MessageFormat.format(
				ActionMessages.GalleryAction_description, new Object[] { screen
						.getName() }));
		setToolTipText(MessageFormat.format(
				ActionMessages.GalleryAction_tooltip, new Object[] { screen
						.getName() }));
		Control control = screen.getControl();
		if (control != null && !control.isDisposed()) {
			setImageDescriptor(S60WorkspacePlugin
					.getImageDescriptor("icons/bullet_preview.gif"));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		view.showSingleScreen(screen);
		view.screenCreated(screen);
	}

	/**
	 * @return Returns the screen.
	 */
	public IGalleryScreen getScreen() {
		return screen;
	}
}
