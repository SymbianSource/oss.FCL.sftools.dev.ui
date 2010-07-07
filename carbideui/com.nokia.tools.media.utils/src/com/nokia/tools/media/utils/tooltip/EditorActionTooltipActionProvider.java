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

package com.nokia.tools.media.utils.tooltip;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

public class EditorActionTooltipActionProvider implements
		ITooltipActionProvider {

	public IAction getAction(Bundle contributor, String actionId) {

		if (actionId != null) {
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			
			IWorkbenchPage page = null;
			
			if (window != null) {
				page = window.getActivePage();
			}
			
			IEditorPart editor = null;
			
			if (page != null) {
				editor = page.getActiveEditor();
			}
			
			if (editor != null) {
				if (editor instanceof IAdaptable) {
					ActionRegistry registry = (ActionRegistry) ((IAdaptable) editor).getAdapter(ActionRegistry.class);
					if (registry != null) {
						IAction editorAction =  registry.getAction(actionId);
						if (editorAction != null) {
							return editorAction;
						}
					}
				}
				
				IAction editorAction = editor.getEditorSite().getActionBars()
						.getGlobalActionHandler(actionId);
	
				return editorAction;
			}
		}

		return null;
	}

}
