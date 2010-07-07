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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;

import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.screen.ui.utils.EclipseUtils;

/**
 * perform action for cheatsheet 13 - edit layer animation
 * 
 *
 */
public class CheatSheet11Actions extends BaseAction implements
		ICheatSheetAction {

	public void run(String[] params, ICheatSheetManager manager) {
		if ("selectInEditor".equals(params[0])) {
			notifyResult(selectInEditor(params[1]));
		} else if ("doSave".equals(params[0])) {
			try {
				IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();
				if (activeEd.getAdapter(IContentAdapter.class) != null) {
					activeEd.doSave(new NullProgressMonitor());
				}
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
			notifyResult(false);
		}
	}

}
