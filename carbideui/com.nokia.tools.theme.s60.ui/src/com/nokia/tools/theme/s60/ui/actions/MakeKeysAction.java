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
package com.nokia.tools.theme.s60.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.OpenResourceAction;

import com.nokia.tools.theme.s60.ui.dialogs.MakeKeysDialog;

public class MakeKeysAction extends Action implements
		IWorkbenchWindowActionDelegate {

	private Shell shell;

	private String keyDestination, cerDestination, password;

	private OpenResourceAction fWorkbenchAction;

	protected OpenResourceAction getWorkbenchAction() {
		if (null == fWorkbenchAction)
			fWorkbenchAction = new OpenResourceAction(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell());
		return fWorkbenchAction;
	}

	Shell getShell() {
		return shell;
	}

	public MakeKeysAction() {
	}

	public MakeKeysAction(Shell shell) {
		super();
		try {
			this.shell = shell;
			fWorkbenchAction = new OpenResourceAction(getShell());
		} catch (RuntimeException e) {
			
			e.printStackTrace();
		}
	}

	public void run() {
		MakeKeysDialog makeKeysDialog = new MakeKeysDialog(getShell());
		makeKeysDialog.create();
		makeKeysDialog.open();
		this.keyDestination = makeKeysDialog.getKeyDestination();
		this.cerDestination = makeKeysDialog.getCerDestination();
		this.password = makeKeysDialog.getPassword();
	}

	public String getKeyDestination() {
		return keyDestination;
	}

	public String getCerDestination() {
		return cerDestination;
	}

	public String getPassword() {
		return password;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		run();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	
	}
}
