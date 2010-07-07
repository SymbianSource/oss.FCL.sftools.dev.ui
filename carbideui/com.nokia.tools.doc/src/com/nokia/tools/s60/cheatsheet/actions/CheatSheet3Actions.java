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
package com.nokia.tools.s60.cheatsheet.actions;

import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.nokia.tools.s60.editor.actions.EditImageInSVGEditorAction;
import com.nokia.tools.screen.ui.utils.EclipseUtils;

/**
 * perform action for cheatsheet 13 - edit layer animation
 * 
 */
public class CheatSheet3Actions extends BaseAction implements ICheatSheetAction {

	public void run(String[] params, ICheatSheetManager manager) {
		if ("selectInEditor".equals(params[0])) {
			notifyResult(selectInEditor(params[1]));
		} else if ("showExtToolsConfig".equals(params[0])) {
			PreferenceDialog prefdlg = PreferencesUtil
					.createPreferenceDialogOn(
							Display.getCurrent().getActiveShell(),
							"com.nokia.tools.s60.preferences.ExternalToolsPreferencePage",
							new String[] { "com.nokia.tools.s60.preferences.ExternalToolsPreferencePage" },
							null);
			prefdlg.open();
		} else if ("editInExternalEditor".equals(params[0])) {
			try {
				IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();
				ActionRegistry registry = (ActionRegistry) activeEd
						.getAdapter(ActionRegistry.class);
				if (registry != null) {
					IAction action = registry
							.getAction(EditImageInSVGEditorAction.ID);
					if (action != null) {
						action.run();
					}
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			notifyResult(false);
		}
	}

}
