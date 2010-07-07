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
package com.nokia.tools.s60.editor.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.nokia.tools.s60.ide.S60WorkspacePlugin;

public class ExternalToolsDropdownAction extends Action implements IMenuCreator {

	private ISelectionProvider provider;
	private CommandStack stack;

	private Menu menu;
	private List<AbstractEditAction> actionsToDispose;

	/**
	 * Constructs an action.
	 * 
	 * @param view the current icon page.
	 */
	public ExternalToolsDropdownAction(ISelectionProvider provider,
			CommandStack stack) {
		this.provider = provider;
		this.stack = stack;
		setToolTipText(Messages.ExternalTools_name);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/external_editors16x16.png"));
		setMenuCreator(this);
		actionsToDispose = new ArrayList<AbstractEditAction>();
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
		for (AbstractEditAction action : actionsToDispose) {
			action.dispose();
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
				provider, stack);
		EditImageInSVGEditorAction editImageVectorEditorAction = new EditImageInSVGEditorAction(
				provider, stack);
		ConvertAndEditSVGInBitmapEditorAction convertVectorToBitmapAction = new ConvertAndEditSVGInBitmapEditorAction(
				provider, stack);
		EditInSystemEditorAction editInSystemEditorAction = new EditInSystemEditorAction(
				provider, stack);		
		EditMaskAction editMask = new EditMaskAction(provider, stack);
		EditMaskAction2 editMask2 = new EditMaskAction2(provider, stack);

		actionsToDispose.add(editImageBitmapEditorAction);
		actionsToDispose.add(editImageVectorEditorAction);
		actionsToDispose.add(convertVectorToBitmapAction);
		actionsToDispose.add(editInSystemEditorAction);
		actionsToDispose.add(editMask);
		actionsToDispose.add(editMask2);

		ActionContributionItem item1 = new ActionContributionItem(
				editImageBitmapEditorAction);
		ActionContributionItem item2 = new ActionContributionItem(
				editImageVectorEditorAction);
		ActionContributionItem item3 = new ActionContributionItem(
				convertVectorToBitmapAction);
		ActionContributionItem item4 = new ActionContributionItem(
				editInSystemEditorAction);		
		Separator item5 = new Separator();
		ActionContributionItem item6 = new ActionContributionItem(editMask);
		ActionContributionItem item7 = new ActionContributionItem(editMask2);

		item1.fill(menu, -1);
		item2.fill(menu, -1);
		item3.fill(menu, -1);
		item4.fill(menu, -1);
		item5.fill(menu, -1);
		item6.fill(menu, -1);
		item7.fill(menu, -1);

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
