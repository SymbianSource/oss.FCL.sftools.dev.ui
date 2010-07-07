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

import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.nokia.tools.s60.ide.S60WorkspacePlugin;

/**
 * This action is used for showing all aligment tool items in a dropdown list
 */
public class AlignmentToolsDropDownAction extends Action implements
		IMenuCreator {
	/**
	 * Action id for the aligment toolbar.
	 */
	public static final String ID = AlignmentToolsDropDownAction.class
			.getPackage().getName()
			+ ".alignment";

	private ActionRegistry actionRegistry;

	private Menu menu;

	public AlignmentToolsDropDownAction(ActionRegistry actionRegistry) {
		this.actionRegistry = actionRegistry;
		setToolTipText(Messages.AlignmentToolsDropDownAction_tooltip);
		setMenuCreator(this);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/alignment_tools16x16.png"));
		setId(ID);
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

		ActionContributionItem alignLeft = new ActionContributionItem(
				actionRegistry.getAction(GEFActionConstants.ALIGN_LEFT));
		ActionContributionItem alignCenter = new ActionContributionItem(
				actionRegistry.getAction(GEFActionConstants.ALIGN_CENTER));
		ActionContributionItem alignRight = new ActionContributionItem(
				actionRegistry.getAction(GEFActionConstants.ALIGN_RIGHT));
		ActionContributionItem alignTop = new ActionContributionItem(
				actionRegistry.getAction(GEFActionConstants.ALIGN_TOP));
		ActionContributionItem alignMiddle = new ActionContributionItem(
				actionRegistry.getAction(GEFActionConstants.ALIGN_MIDDLE));
		ActionContributionItem alignBottom = new ActionContributionItem(
				actionRegistry.getAction(GEFActionConstants.ALIGN_BOTTOM));
		Separator separator = new Separator();

		alignLeft.fill(menu, -1);
		alignCenter.fill(menu, -1);
		alignRight.fill(menu, -1);
		separator.fill(menu, -1);
		alignTop.fill(menu, -1);
		alignMiddle.fill(menu, -1);
		alignBottom.fill(menu, -1);

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
