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
package com.nokia.tools.s60.views.menu;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.WorkbenchPart;

import com.nokia.tools.s60.editor.actions.AbstractAction;
import com.nokia.tools.s60.editor.actions.BrowseForFileAction;
import com.nokia.tools.s60.editor.actions.ClearImageEditorAction;
import com.nokia.tools.s60.editor.actions.ConvertAndEditSVGInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.CopyContentDataAction;
import com.nokia.tools.s60.editor.actions.CopyGraphicsAction;
import com.nokia.tools.s60.editor.actions.CopyImageAction;
import com.nokia.tools.s60.editor.actions.EditImageInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.EditImageInSVGEditorAction;
import com.nokia.tools.s60.editor.actions.EditInSystemEditorAction;
import com.nokia.tools.s60.editor.actions.EditMaskAction;
import com.nokia.tools.s60.editor.actions.EditMaskAction2;
import com.nokia.tools.s60.editor.actions.EditSoundInSoundEditorAction;
import com.nokia.tools.s60.editor.actions.ElevenPieceConvertAction;
import com.nokia.tools.s60.editor.actions.NinePieceConvertAction;
import com.nokia.tools.s60.editor.actions.PasteContentDataAction;
import com.nokia.tools.s60.editor.actions.PasteGraphicsAction;
import com.nokia.tools.s60.editor.actions.PasteImageAction;
import com.nokia.tools.s60.editor.actions.SetColorAction;
import com.nokia.tools.s60.editor.actions.SetStretchModeDDown;
import com.nokia.tools.s60.editor.actions.ThreePieceConvertAction;
import com.nokia.tools.s60.editor.ui.views.Messages;
import com.nokia.tools.s60.ide.ContributedActionsResolver;
import com.nokia.tools.s60.ide.actions.AddBookmarkViewAction;
import com.nokia.tools.s60.ide.actions.AddTaskViewAction;
import com.nokia.tools.s60.ide.actions.OpenGraphicsEditorAction;
import com.nokia.tools.s60.internal.utils.HideableMenuManager;
import com.nokia.tools.s60.views.IconViewPage;
import com.nokia.tools.screen.ui.editor.ShowInContributionResolver;
import com.nokia.tools.screen.ui.menu.IIconMenuProvider;
import com.nokia.tools.theme.s60.ui.preferences.ThirdPartyIconsPrefPage;

public class GraphicalIconMenuProvider implements IIconMenuProvider {

	/**
	 * adds action to manager if enabled and add separator if not null.
	 * 
	 * @param manager
	 * @param action
	 * @param spr;
	 * @return NULL or separator, if was not used
	 */
	private Separator internalAdd(IMenuManager manager, IAction action,
			Separator spr) {
		if (action.isEnabled()) {
			if (spr != null) {
				manager.add(spr);
				spr = null;
			}
			manager.add(action);
		}
		return spr;
	}

	public void fillIconContextMenu(IMenuManager manager, WorkbenchPart parent,
			String uiContext, CommandStack commandStack, IActionBars bars) {

		UndoAction undoAction = new UndoAction(parent);
		undoAction.update();
		manager.add(undoAction);

		RedoAction redoAction = new RedoAction(parent);
		redoAction.update();
		manager.add(redoAction);

		manager.add(new Separator());
		manager.add(new CopyImageAction(parent, null));
		manager.add(new PasteImageAction(parent, null));

		Separator spr = new Separator();
		spr = internalAdd(manager, new AddBookmarkViewAction(parent), spr);
		spr = internalAdd(manager, new AddTaskViewAction(parent), spr);

		/* edit actions */
		spr = new Separator();

		if (bars != null) {

			IAction action = bars
					.getGlobalActionHandler(PasteContentDataAction.ID);
			if (action != null && action.isEnabled())
				manager.insertAfter(ActionFactory.PASTE.getId(), action);

			spr = internalAdd(manager, new BrowseForFileAction(parent), spr);

			spr = new Separator();

			action = bars
					.getGlobalActionHandler(EditImageInBitmapEditorAction.ID);
			spr = internalAdd(manager, action, spr);

			action = bars
					.getGlobalActionHandler(ConvertAndEditSVGInBitmapEditorAction.ID);
			spr = internalAdd(manager, action, spr);

			action = bars
					.getGlobalActionHandler(EditInSystemEditorAction.ID);
			spr = internalAdd(manager, action, spr);			

			action = bars.getGlobalActionHandler(EditImageInSVGEditorAction.ID);
			spr = internalAdd(manager, action, spr);

			action = bars
					.getGlobalActionHandler(EditSoundInSoundEditorAction.ID);
			spr = internalAdd(manager, action, spr);

		}

		/* mask actions */
		spr = new Separator();

		if (bars != null) {

			IAction action = bars.getGlobalActionHandler(EditMaskAction.ID);
			spr = internalAdd(manager, action, spr);

			action = bars.getGlobalActionHandler(EditMaskAction2.ID);
			spr = internalAdd(manager, action, spr);

		}

		/* set color action */
		IAction action = new SetColorAction(parent.getSite()
				.getSelectionProvider(), commandStack);
		internalAdd(manager, action, new Separator());

		/* clear action */
		action = new ClearImageEditorAction(parent.getSite()
				.getSelectionProvider(), commandStack);
		internalAdd(manager, action, new Separator());

		/* 9-piece related actions */
		spr = new Separator();
		action = new NinePieceConvertAction(parent.getSite()
				.getSelectionProvider(), commandStack,
				NinePieceConvertAction.TYPE_CONVERT2SINGLE);
		spr = internalAdd(manager, action, spr);

		action = new NinePieceConvertAction(parent.getSite()
				.getSelectionProvider(), commandStack,
				NinePieceConvertAction.TYPE_CONVERT2NINE);
		spr = internalAdd(manager, action, spr);

		/* 11-piece related actions */
		spr = new Separator();
		action = new ElevenPieceConvertAction(parent.getSite()
				.getSelectionProvider(), commandStack,
				ElevenPieceConvertAction.TYPE_CONVERT2SINGLE);
		spr = internalAdd(manager, action, spr);

		action = new ElevenPieceConvertAction(parent.getSite()
				.getSelectionProvider(), commandStack,
				ElevenPieceConvertAction.TYPE_ELEVEN_PIECE);
		spr = internalAdd(manager, action, spr);
		
		/* 3-piece related actions */
		/*spr = new Separator();
		action = new ThreePieceConvertAction(parent.getSite()
				.getSelectionProvider(), commandStack,
				ThreePieceConvertAction.TYPE_CONVERT2SINGLE);
		spr = internalAdd(manager, action, spr);

		action = new ThreePieceConvertAction(parent.getSite()
				.getSelectionProvider(), commandStack,
				ThreePieceConvertAction.TYPE_CONVERT2THREE);
		spr = internalAdd(manager, action, spr);*/
		
		
		
		// stretch mode for bitmaps:
		SetStretchModeDDown ddown = new SetStretchModeDDown(null, parent
				.getSite().getSelectionProvider(), commandStack);
		internalAdd(manager, ddown, new Separator());

		/* edit/animate actions */
		action = new OpenGraphicsEditorAction(parent, parent.getSite()
				.getSelectionProvider());
		((AbstractAction) action).update();
		internalAdd(manager, action, new Separator());

		/* create actions from contributors for 'show in' submenu */
		HideableMenuManager showInSubmenu = new HideableMenuManager(
				Messages.showinActionTitle);
		for (Class<WorkbenchPartAction> cl : ShowInContributionResolver.INSTANCE
				.getContributedActions()) {
			Object[] params = new Object[] { parent };
			try {
				WorkbenchPartAction act = cl.getConstructor(
						new Class[] { IWorkbenchPart.class }).newInstance(
						params);
//				System.out.println(act);
				if (act != null && act.isEnabled()) {
					showInSubmenu.add(act);
				}
			} catch (Exception e) {
				System.out.println("Cannot instantiate: " + cl.getName());
				e.printStackTrace();
			}
		}
		if (showInSubmenu.isEnabled()) {
			manager.add(new Separator());
			manager.add(showInSubmenu);
		}

		/* EXTENDED copy/paste */
		{
			HideableMenuManager cpSubmenu = new HideableMenuManager(
					"Extended Copy/Paste");
			AbstractAction cg = new CopyGraphicsAction(parent, null);
			cpSubmenu.add(cg);
			cg = new CopyContentDataAction(parent.getSite()
					.getSelectionProvider(), null);
			cg.setText("Copy Element(s)");
			cpSubmenu.add(cg);
			cg = new PasteGraphicsAction(parent.getSite()
					.getSelectionProvider(), commandStack, null);
			cpSubmenu.add(cg);
			cg = new PasteContentDataAction(parent.getSite()
					.getSelectionProvider(), commandStack, null);
			cpSubmenu.add(cg);
			if (cpSubmenu.isEnabled()) {
				manager.add(new Separator());
				manager.add(cpSubmenu);
			}
		}

		IconViewPage page = null;

		if (parent instanceof PageBookView) {
			PageBookView pv = (PageBookView) parent;
			if (pv.getCurrentPage() instanceof IconViewPage)
				page = (IconViewPage) pv.getCurrentPage();
		}

		if (page != null
				&& page.getCurrentCategory() != null
				&& "Context Pane Icons".equals(page.getCurrentCategory()
						.getName())) {
			
			action = new Action(Messages.Action_Manage3rdIcons) {
				@Override
				public void run() {
					String linkAddress = ThirdPartyIconsPrefPage.class
							.getName();
					PreferenceDialog prefdlg = PreferencesUtil
							.createPreferenceDialogOn(Display.getCurrent()
									.getActiveShell(), linkAddress,
									new String[] { linkAddress }, null);
					prefdlg.open();
				}

				@Override
				public boolean isEnabled() {
					return true;
				}
			};
			internalAdd(manager, action, new Separator());
		}

		/* contributed actions */

		manager.add(new Separator());
		
		ContributedActionsResolver.getInstance().contributeActions(manager,
				uiContext, parent);
	}

}
