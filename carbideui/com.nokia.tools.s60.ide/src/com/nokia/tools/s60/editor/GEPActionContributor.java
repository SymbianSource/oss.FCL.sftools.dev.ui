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
package com.nokia.tools.s60.editor;

import org.eclipse.gef.ui.actions.RedoRetargetAction;
import org.eclipse.gef.ui.actions.UndoRetargetAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.RetargetAction;

import com.nokia.tools.s60.editor.actions.layers.AddBgLayerAction;
import com.nokia.tools.s60.editor.actions.layers.AddLayerAction;
import com.nokia.tools.s60.editor.actions.layers.ChangeOrderAction;
import com.nokia.tools.s60.editor.actions.layers.ClearLayerAction;
import com.nokia.tools.s60.editor.actions.layers.CustomizeAction;
import com.nokia.tools.s60.editor.actions.layers.DeleteSelectedAction;
import com.nokia.tools.s60.editor.actions.layers.RenameLayerAction;
import com.nokia.tools.s60.editor.ui.views.Messages;
import com.nokia.tools.s60.ide.actions.menu.GeneralRetargetAction;
import com.nokia.tools.screen.ui.editor.BaseEditorContributor;

public class GEPActionContributor extends BaseEditorContributor {

	private static final String LAYERS_MENU = "layers";

	private static final String EDIT_MENU_ID = "edit";

	private IMenuManager editMenu;

	private MenuManager layersMenu;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#dispose()
	 */
	@Override
	public void dispose() {
		getPage().removePartListener(this);
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToCoolBar(org.eclipse.jface.action.ICoolBarManager)
	 */
	@Override
	public void contributeToCoolBar(ICoolBarManager coolBarManager) {
		super.contributeToCoolBar(coolBarManager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#buildActions()
	 */
	@Override
	protected void buildActions() {
		addRetargetAction(new UndoRetargetAction());
		addRetargetAction(new RedoRetargetAction());

		addRetargetAction(getRetarget(new AddLayerAction(null, null, false)));
		addRetargetAction(getRetarget(new AddBgLayerAction(null, null, false)));
		addRetargetAction(getRetarget(new RenameLayerAction(null, null, false)));
		addRetargetAction(getRetarget(new DeleteSelectedAction(null, null,
				false)));
		addRetargetAction(getRetarget(new CustomizeAction(null, null, null,
				false)));
		addRetargetAction(getRetarget(new ChangeOrderAction(null,
				ChangeOrderAction.UP, false)));
		addRetargetAction(getRetarget(new ChangeOrderAction(null,
				ChangeOrderAction.DOWN, false)));
		addRetargetAction(getRetarget(new ClearLayerAction(null, null, false)));
	}

	private RetargetAction getRetarget(IAction a) {
		return new GeneralRetargetAction(a);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void contributeToMenu(IMenuManager menubar) {
		editMenu = (IMenuManager) menubar.find(EDIT_MENU_ID);
		layersMenu = new MenuManager(Messages.LayersMenu_title, LAYERS_MENU);

		layersMenu.add(getAction(AddLayerAction.ID));
		layersMenu.add(getAction(AddBgLayerAction.ID));
		layersMenu.add(getAction(RenameLayerAction.ID));
		layersMenu.add(new Separator());
		layersMenu.add(getAction(DeleteSelectedAction.ID));

		layersMenu.add(new Separator());
		layersMenu.add(getAction(ChangeOrderAction.ID_UP));
		layersMenu.add(getAction(ChangeOrderAction.ID_DOWN));
		layersMenu.add(new Separator());
		layersMenu.add(getAction(ClearLayerAction.ID));

		editMenu.add(new Separator());
		editMenu.add(layersMenu);
		editMenu.update(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#declareGlobalActionKeys()
	 */
	@Override
	protected void declareGlobalActionKeys() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.menus.BaseEditorContributor#refresh(boolean)
	 */
	protected void refresh(int state) {
	}
}
