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
package com.nokia.tools.ui.editor;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

public class BaseGraphicalContextMenuProvider extends BaseContextMenuProvider {

	public BaseGraphicalContextMenuProvider(EditPartViewer viewer) {
		super(viewer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.editor.BaseContextMenuProvider#buildContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	public void buildContextMenu(IMenuManager menu) {
		super.buildContextMenu(menu);

		IAction action = getActionRegistry().getAction(
				GEFActionConstants.ZOOM_IN);
		// bugfix#24649:
		// Removed ZoomIn and ZoomOut operations from context pop-up menu in Outline view and same are activated for Editor.
		boolean checkActivePage = (PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
				&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null);

		if (action != null && action.isEnabled() && checkActivePage == true && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart()instanceof IEditorPart)
			menu.appendToGroup(GEFActionConstants.GROUP_VIEW, action);

		action = getActionRegistry().getAction(GEFActionConstants.ZOOM_OUT);
		if (action != null && action.isEnabled() && checkActivePage == true && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart()instanceof IEditorPart)
			menu.appendToGroup(GEFActionConstants.GROUP_VIEW, action);

		// Alignment Actions
		// @Externalize
		MenuManager submenu = new MenuManager("Alignment Tools");

		action = getActionRegistry().getAction(GEFActionConstants.ALIGN_LEFT);
		if (action != null && action.isEnabled()) {
			submenu.add(action);
		}

		action = getActionRegistry().getAction(GEFActionConstants.ALIGN_CENTER);
		if (action != null && action.isEnabled()) {
			submenu.add(action);
		}

		action = getActionRegistry().getAction(GEFActionConstants.ALIGN_RIGHT);
		if (action != null && action.isEnabled()) {
			submenu.add(action);
		}

		submenu.add(new Separator());

		action = getActionRegistry().getAction(GEFActionConstants.ALIGN_TOP);
		if (action != null && action.isEnabled()) {
			submenu.add(action);
		}

		action = getActionRegistry().getAction(GEFActionConstants.ALIGN_MIDDLE);
		if (action != null && action.isEnabled()) {
			submenu.add(action);
		}

		action = getActionRegistry().getAction(GEFActionConstants.ALIGN_BOTTOM);
		if (action != null && action.isEnabled()) {
			submenu.add(action);
		}

		if (!submenu.isEmpty()) {
			menu.appendToGroup(GEFActionConstants.GROUP_VIEW, new Separator());
			menu.appendToGroup(GEFActionConstants.GROUP_VIEW, submenu);
		}

		// Sizing Actions
		action = getActionRegistry().getAction(GEFActionConstants.MATCH_WIDTH);
		if (action != null && action.isEnabled()) {
			menu.appendToGroup(GEFActionConstants.GROUP_VIEW, new Separator());
			menu.appendToGroup(GEFActionConstants.GROUP_VIEW, action);
			action = getActionRegistry().getAction(
					GEFActionConstants.MATCH_HEIGHT);
			if (action != null && action.isEnabled()) {
				menu.appendToGroup(GEFActionConstants.GROUP_VIEW, action);
			}
		} else {
			action = getActionRegistry().getAction(
					GEFActionConstants.MATCH_HEIGHT);
			if (action != null && action.isEnabled()) {
				menu.appendToGroup(GEFActionConstants.GROUP_VIEW,
						new Separator());
				menu.appendToGroup(GEFActionConstants.GROUP_VIEW, action);
			}
		}
	}
}
