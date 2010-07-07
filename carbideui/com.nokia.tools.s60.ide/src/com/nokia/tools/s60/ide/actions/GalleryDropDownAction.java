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
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.views.GalleryPage;
import com.nokia.tools.screen.ui.gallery.IGalleryScreenProvider.IGalleryScreen;

/**
 * This action is used for showing all gallery screen names in the dropdown list
 * besides the action button.
 */
public class GalleryDropDownAction extends Action implements IMenuCreator {
	private GalleryPage view;
	private Menu menu;

	/**
	 * Construxts an action.
	 * 
	 * @param view the current gallery page.
	 */
	public GalleryDropDownAction(GalleryPage view) {
		this.view = view;
		setToolTipText(ActionMessages.GalleryDropDownAction_tooltip);
		setMenuCreator(this);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/gallery_screen_select16x16.png"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (menu != null) {
			menu.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		if (menu != null) {
			menu.dispose();
		}
		menu = new Menu(parent);
		for (IGalleryScreen screen : view.getScreens()) {
			GalleryAction action = new GalleryAction(view, screen);
			ActionContributionItem item = new ActionContributionItem(action);
			item.fill(menu, -1);
		}
		return menu;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		return null;
	}
}
