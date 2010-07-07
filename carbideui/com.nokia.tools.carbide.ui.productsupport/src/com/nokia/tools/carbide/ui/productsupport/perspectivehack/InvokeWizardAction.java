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

package com.nokia.tools.carbide.ui.productsupport.perspectivehack;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class InvokeWizardAction extends Action {
	
	private Class<IWizard> wizard;
	private Shell shell;

	public InvokeWizardAction(Shell shell, Class<IWizard> wiz, String name, ImageDescriptor icon) {
		this.wizard = wiz;
		this.shell = shell;
		setText(name);
		setImageDescriptor(icon);		
	}

	public void run() {		
		if (shell == null)
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		WizardDialog dialog;
		try {
			dialog = new WizardDialog(shell, wizard.newInstance());
			dialog.open();
		} catch (InstantiationException e) {
			
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			
			e.printStackTrace();
		}		
	}

}


