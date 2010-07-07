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
package com.nokia.tools.screen.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ImportResourcesAction;

import com.nokia.tools.screen.ui.wizards.ImportWizard;

public class ImportAction extends ImportResourcesAction {
	private IWorkbenchWindow window;

	public ImportAction(IWorkbenchWindow window) {
		super(window);
		this.window = window;
	}

	public void run() {
		ImportWizard importWizard = new ImportWizard();
		WizardDialog dialog = new WizardDialog(window.getShell(), importWizard);
		dialog.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		run();
	}
}
