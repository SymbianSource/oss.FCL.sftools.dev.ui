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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.screen.ui.wizards.NewProjectWizard;

public class OpenProjectWizard extends BaseAction {
@Override
public void run() {
	IWorkbench workbench = PlatformUI.getWorkbench();
	Shell shell = workbench.getActiveWorkbenchWindow().getShell();
	NewProjectWizard wizard= new NewProjectWizard();
	wizard.init(workbench,new StructuredSelection());
	
	WizardDialog dialog= new WizardDialog(shell, wizard);
	dialog.create();
	dialog.open();

	notifyResult(dialog.getReturnCode()==Dialog.OK);
}
}
