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

package com.nokia.tools.s60.ide.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class LaunchThirdPartyIconDefinitionAction implements
		IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	
	public void dispose() {
		// Simply do nothing as this is a stateless action.
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;

	}

	public void run(IAction action) {
		String linkAddress = "com.nokia.tools.theme.s60.ui.preferences.ThirdPartyIconsPrefPage";
		PreferenceDialog prefdlg = PreferencesUtil.createPreferenceDialogOn(
				window.getShell(), linkAddress,
		    new String[] { linkAddress }, null);
		prefdlg.open();		

	}

	public void selectionChanged(IAction action, ISelection selection) {
		// Simply do nothing as this is a stateless action.

	}

}
