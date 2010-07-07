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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.WorkbenchPart;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroups;
import com.nokia.tools.s60.editor.actions.AbstractAction;
import com.nokia.tools.s60.editor.actions.AddToGroupAction;
import com.nokia.tools.s60.editor.actions.BrowseForFileAction;
import com.nokia.tools.s60.editor.actions.ClearImageEditorAction;
import com.nokia.tools.s60.editor.actions.ConvertAndEditSVGInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.EditImageInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.EditImageInSVGEditorAction;
import com.nokia.tools.s60.editor.actions.EditInSystemEditorAction;
import com.nokia.tools.s60.editor.actions.EditMaskAction;
import com.nokia.tools.s60.editor.actions.EditMaskAction2;
import com.nokia.tools.s60.editor.actions.ElevenPieceConvertAction;
import com.nokia.tools.s60.editor.actions.NinePieceConvertAction;
import com.nokia.tools.s60.editor.actions.PasteContentDataAction;
import com.nokia.tools.s60.editor.actions.PasteImageAction;
import com.nokia.tools.s60.editor.actions.RemoveFromGroupAction;
import com.nokia.tools.s60.editor.actions.SetColorAction;
import com.nokia.tools.s60.editor.actions.SetStretchModeDDown;
import com.nokia.tools.s60.editor.actions.ShowInReferencedColors;
import com.nokia.tools.s60.editor.actions.ThreePieceConvertAction;
import com.nokia.tools.s60.editor.ui.views.Messages;
import com.nokia.tools.s60.ide.ContributedActionsResolver;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.ide.actions.OpenGraphicsEditorAction;
import com.nokia.tools.s60.internal.utils.HideableMenuManager;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.actions.PausePlayingAction;
import com.nokia.tools.screen.ui.actions.PlayAllAction;
import com.nokia.tools.screen.ui.actions.PlaySelectionAction;
import com.nokia.tools.screen.ui.actions.StopPlayingAction;
import com.nokia.tools.screen.ui.editor.ShowInContributionResolver;
import com.nokia.tools.screen.ui.editor.embedded.IEmbeddedEditorContributor;
import com.nokia.tools.theme.editing.BasicImageLayer;
import com.nokia.tools.ui.editor.BaseGraphicalContextMenuProvider;

public class ScreenEditorContextMenuProvider extends
		BaseGraphicalContextMenuProvider {
	private List<IEmbeddedEditorContributor> contributors;

	public ScreenEditorContextMenuProvider(EditPartViewer viewer) {
		super(viewer);
		contributors = new ArrayList<IEmbeddedEditorContributor>();
	}

	public void addEmbeddedEditorContributor(
			IEmbeddedEditorContributor contributor) {
		contributors.add(contributor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.editor.BaseContextMenuProvider#buildContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	public void buildContextMenu(IMenuManager menu) {
		super.buildContextMenu(menu);

		Separator spr = new Separator();

		IAction action = getActionRegistry().getAction(
				PasteContentDataAction.ID);
		if (action != null && action.isEnabled())
			menu.insertAfter(ActionFactory.PASTE.getId(), action);

		action = getActionRegistry().getAction(
				IDEActionFactory.BOOKMARK.getId());
		if (action != null && action.isEnabled()) {
			action.setImageDescriptor(S60WorkspacePlugin
					.getImageDescriptor("icons/etool16/bkmrk_nav.gif"));
			spr = _addOrNot(menu, action, spr);
		}

		action = getActionRegistry().getAction(
				IDEActionFactory.ADD_TASK.getId());
		if (action != null && action.isEnabled()) {
			action.setImageDescriptor(S60WorkspacePlugin
					.getImageDescriptor("icons/etool16/addtsk_tsk.gif"));
			spr = _addOrNot(menu, action, spr);
		}

		spr = new Separator();

		action = getActionRegistry().getAction(BrowseForFileAction.ID);
		spr = _addOrNot(menu, action, spr);

		spr = new Separator();

		action = getActionRegistry()
				.getAction(EditImageInBitmapEditorAction.ID);
		spr = _addOrNot(menu, action, spr);

		action = getActionRegistry().getAction(
				ConvertAndEditSVGInBitmapEditorAction.ID);
		spr = _addOrNot(menu, action, spr);
		
		action = getActionRegistry().getAction(
				EditInSystemEditorAction.ID);
		spr = _addOrNot(menu, action, spr);		

		action = getActionRegistry().getAction(EditImageInSVGEditorAction.ID);
		spr = _addOrNot(menu, action, spr);

		spr = new Separator();

		// mask actions
		action = getActionRegistry().getAction(EditMaskAction.ID);
		spr = _addOrNot(menu, action, spr);

		action = getActionRegistry().getAction(EditMaskAction2.ID);
		spr = _addOrNot(menu, action, spr);

		/* clear image action */
		action = getActionRegistry().getAction(ClearImageEditorAction.ID);
		_addOrNot(menu, action, new Separator());

		/* convert to 9-1-piece bitmap */
		spr = new Separator();

		action = getActionRegistry()
				.getAction(NinePieceConvertAction.ID_SINGLE);
		spr = _addOrNot(menu, action, spr);

		action = getActionRegistry().getAction(NinePieceConvertAction.ID_NINE);
		spr = _addOrNot(menu, action, spr);
		
		//Support for 11 Pic
		spr = new Separator();

		action = getActionRegistry()
				.getAction(ElevenPieceConvertAction.ID_SINGLE);
		spr = _addOrNot(menu, action, spr);

		action = getActionRegistry().getAction(ElevenPieceConvertAction.ID_ELEVEN);
		spr = _addOrNot(menu, action, spr);

	
		// stretch mode for bitmaps:
		SetStretchModeDDown ddown = new SetStretchModeDDown(null, getViewer(),
				getViewer().getEditDomain().getCommandStack());
		if (ddown.isEnabled()) {
			menu.add(new Separator());
			menu.add(ddown);
		}

		action = getActionRegistry().getAction(OpenGraphicsEditorAction.ID);
		_addOrNot(menu, action, new Separator());

				
		action = getActionRegistry().getAction(SetColorAction.ID);
		_addOrNot(menu, action, new Separator());

		
		HideableMenuManager showInSubmenu = new HideableMenuManager(
				Messages.showinActionTitle);
		String[] ids = ShowInContributionResolver.INSTANCE
				.getDeclaredActionIds();
		for (String element : ids) {
			if (element == null)
				continue;
			action = getActionRegistry().getAction(element);
			if (action != null && action.isEnabled()) {
				showInSubmenu.add(action);
			}
		}
		if (showInSubmenu.isEnabled()) {
			menu.add(new Separator());
			menu.add(showInSubmenu);
		}

		// update paste action
		((UpdateAction) getActionRegistry().getAction(PasteImageAction.ID))
				.update();

		for (IEmbeddedEditorContributor contributor : contributors) {
			contributor.contributeContextMenu(getActionRegistry(), menu);
		}

		spr = new Separator();

		action = getActionRegistry().getAction(PlaySelectionAction.ID);
		spr = _addOrNot(menu, action, spr);

		action = getActionRegistry().getAction(PlayAllAction.ID);
		spr = _addOrNot(menu, action, spr);

		action = getActionRegistry().getAction(PausePlayingAction.ID);
		spr = _addOrNot(menu, action, spr);

		action = getActionRegistry().getAction(StopPlayingAction.ID);
		spr = _addOrNot(menu, action, spr);

		// group for contributions
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		menu.add(new Separator());
		//WorkbenchPart activePart = (WorkbenchPart) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		ContributedActionsResolver.getInstance().contributeActions(menu, "editor", getActionRegistry());
		
	}

	/**
	 * adds action to manager if enabled and add separator if not null.
	 * 
	 * @param manager
	 * @param action
	 * @param spr
	 * @return NULL or separator, if was not used
	 */
	private Separator _addOrNot(IMenuManager manager, IAction action,
			Separator spr) {
		if (action != null && action.isEnabled()) {
			if (spr != null) {
				manager.add(spr);
				spr = null;
			}
			manager.add(action);
		}
		return spr;
	}
}
