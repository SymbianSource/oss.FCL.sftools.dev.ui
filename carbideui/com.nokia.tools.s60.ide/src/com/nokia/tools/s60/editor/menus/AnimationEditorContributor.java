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
package com.nokia.tools.s60.editor.menus;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import com.nokia.tools.s60.editor.actions.layers.AddBgLayerAction;
import com.nokia.tools.s60.editor.actions.layers.AddLayerAction;
import com.nokia.tools.s60.editor.actions.layers.BaseLayerAction;
import com.nokia.tools.s60.editor.actions.layers.ChangeOrderAction;
import com.nokia.tools.s60.editor.actions.layers.ClearLayerAction;
import com.nokia.tools.s60.editor.actions.layers.CustomizeAction;
import com.nokia.tools.s60.editor.actions.layers.DeleteSelectedAction;
import com.nokia.tools.s60.editor.actions.layers.RenameLayerAction;
import com.nokia.tools.s60.editor.ui.views.Messages;
import com.nokia.tools.s60.ide.actions.menu.GeneralRetargetAction;
import com.nokia.tools.screen.ui.editor.BaseEditorContributor;

/**
 * This contributor contributes the zooming and resolution change actions to the
 * main action bar and the menu.
 */
public class AnimationEditorContributor extends BaseEditorContributor {

	private static final String LAEYRS_MENU_ID = "layers";

	private MenuManager viewMenu;

	private ComboContributionItem[] items;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#buildActions()
	 */
	protected void buildActions() {

		BaseLayerAction action = new AddBgLayerAction(null, null, true);
		addAction(new GeneralRetargetAction(action.getId(), action.getText(),
				action.getToolTipText()));

		action = new AddLayerAction(null, null, true);
		addAction(new GeneralRetargetAction(action.getId(), action.getText(),
				action.getToolTipText()));

		action = new RenameLayerAction(null, null, true);
		addAction(new GeneralRetargetAction(action.getId(), action.getText(),
				action.getToolTipText()));

		action = new DeleteSelectedAction(null, null, true);
		addAction(new GeneralRetargetAction(action.getId(), action.getText(),
				action.getToolTipText()));

		// add effects submenu
		/*
		 * viewMenu.add(new Separator()); viewMenu.add(new
		 * AddEffectDropDown(GEPActionContributor.this.editor));
		 */
		action = new CustomizeAction(null, null, null, true);
		addAction(new GeneralRetargetAction(action.getId(), action.getText(),
				action.getToolTipText()));

		/* up / down */
		action = new ChangeOrderAction(null, ChangeOrderAction.UP, true);
		addAction(new GeneralRetargetAction(action.getId(), action.getText(),
				action.getToolTipText()));
		action = new ChangeOrderAction(null, ChangeOrderAction.DOWN, true);
		addAction(new GeneralRetargetAction(action.getId(), action.getText(),
				action.getToolTipText()));

		/* clear action */
		action = new ClearLayerAction(null, null, true);
		addAction(new GeneralRetargetAction(action.getId(), action.getText(),
				action.getToolTipText()));
	}

	public void contributeToMenu(IMenuManager menubar) {

		viewMenu = new MenuManager(Messages.LayersMenu_title, LAEYRS_MENU_ID);

		viewMenu.add(getAction(AddBgLayerAction.ID));
		viewMenu.add(getAction(AddLayerAction.ID));
		viewMenu.add(getAction(RenameLayerAction.ID));

		viewMenu.add(new Separator());
		viewMenu.add(getAction(DeleteSelectedAction.ID));

		// add effects submenu
		/*
		 * viewMenu.add(new Separator()); viewMenu.add(new
		 * AddEffectDropDown(GEPActionContributor.this.editor));
		 */
		viewMenu.add(getAction(CustomizeAction.ID));

		/* up / down */
		viewMenu.add(new Separator());
		viewMenu.add(getAction(ChangeOrderAction.ID_UP));
		viewMenu.add(getAction(ChangeOrderAction.ID_DOWN));

		/* clear action */
		viewMenu.add(new Separator());
		viewMenu.add(getAction(ClearLayerAction.ID));

		try {
			menubar.add(viewMenu);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.menus.BaseEditorContributor#refresh(boolean)
	 */
	protected void refresh(int state) {
		if (items != null) {
			for (ComboContributionItem item : items) {
				item.update();
			}
		}
	}

	@Override
	protected void declareGlobalActionKeys() {
	}

}
