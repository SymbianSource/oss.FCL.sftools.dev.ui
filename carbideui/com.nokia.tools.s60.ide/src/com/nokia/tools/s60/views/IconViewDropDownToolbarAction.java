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
package com.nokia.tools.s60.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.nokia.tools.s60.editor.actions.ConvertAndEditSVGInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.EditImageInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.EditImageInSVGEditorAction;
import com.nokia.tools.s60.editor.actions.EditInSystemEditorAction;
import com.nokia.tools.s60.editor.actions.EditSoundInSoundEditorAction;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;

public class IconViewDropDownToolbarAction extends Action implements
		IMenuCreator {

	private IconViewPage view;

	private Menu menu;

	/**
	 * Construxts an action.
	 * 
	 * @param view
	 *            the current icon page.
	 */
	public IconViewDropDownToolbarAction(IconViewPage view) {
		this.view = view;
		setToolTipText(ViewMessages.IconViewDropDownToolBarAction_menu);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/external_editors16x16.png"));
		setMenuCreator(this);
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

		EditImageInBitmapEditorAction editImageBitmapEditorAction = new EditImageInBitmapEditorAction(
				view.parent);
		EditImageInSVGEditorAction editImageVectorEditorAction = new EditImageInSVGEditorAction(
				view.parent);
		ConvertAndEditSVGInBitmapEditorAction convertVectorToBitmapAction = new ConvertAndEditSVGInBitmapEditorAction(
				view.parent);
		EditSoundInSoundEditorAction editSoundInExternalEditor = new EditSoundInSoundEditorAction(
				view.parent);
		EditInSystemEditorAction editInSystemEditorAction = new EditInSystemEditorAction(
				view.parent);		
		ActionContributionItem item1 = new ActionContributionItem(
				editImageBitmapEditorAction);
		ActionContributionItem item2 = new ActionContributionItem(
				editImageVectorEditorAction);
		ActionContributionItem item3 = new ActionContributionItem(
				convertVectorToBitmapAction);
		ActionContributionItem item4 = new ActionContributionItem(
				editSoundInExternalEditor);
		ActionContributionItem item5 = new ActionContributionItem(
				editInSystemEditorAction);		

		item1.fill(menu, -1);
		item2.fill(menu, -1);
		item3.fill(menu, -1);
		item4.fill(menu, -1);
		item5.fill(menu, -1);

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
