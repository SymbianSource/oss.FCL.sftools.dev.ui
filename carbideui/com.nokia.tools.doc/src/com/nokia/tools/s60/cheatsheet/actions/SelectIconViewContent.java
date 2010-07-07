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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.ui.part.ISetSelectionTarget;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.s60.editor.actions.ShowInResourceViewAction;
import com.nokia.tools.screen.ui.utils.EclipseUtils;

public class SelectIconViewContent extends BaseAction implements
		ICheatSheetAction {

	public void run(String[] params, ICheatSheetManager manager) {
		String elementId = params[0];
		// create selection object and send it to icon view
		try {
			IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();
			IContentAdapter adapter = (IContentAdapter) activeEd
					.getAdapter(IContentAdapter.class);
			ActionRegistry registry = (ActionRegistry) activeEd
					.getAdapter(ActionRegistry.class);
			if (adapter != null && registry != null) {
				IContent[] cnt = adapter.getContents();
				for (IContent c : cnt) {
					IContentData data = c.findById(elementId);
					if (data != null) {
						//
						// find and run 'show in resource view'
						ShowInResourceViewAction act = (ShowInResourceViewAction) registry
								.getAction(ShowInResourceViewAction.ID);
						if (act != null) {
							act.doRun(data);
						}

						try {
							if (activeEd instanceof ISetSelectionTarget) {
								((ISetSelectionTarget) activeEd)
										.selectReveal(new StructuredSelection(
												data));
							}
						} catch (Exception e) {
							// element does not have preview screen
						}

						return;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		notifyResult(false);
	}

}
