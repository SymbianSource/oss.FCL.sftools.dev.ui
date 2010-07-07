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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.OpenResourceAction;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.packaging.PackagingContext;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.theme.s60.ui.dialogs.KeyPairsDialog;

public class KeyPairsAction extends Action implements
		IWorkbenchWindowActionDelegate, IPartListener {

	private Shell shell;
	private String selectedKeyPair;
	private PackagingContext context;

	private OpenResourceAction fWorkbenchAction;

	/**
	 * The action used to render this delegate.
	 */
	private IAction fAction = null;

	protected OpenResourceAction getWorkbenchAction() {
		if (null == fWorkbenchAction)
			fWorkbenchAction = new OpenResourceAction(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell());
		return fWorkbenchAction;
	}

	Shell getShell() {
		return shell;
	}

	public KeyPairsAction() {
	}

	public KeyPairsAction(String selectedKeyPair, PackagingContext context) {
		this.selectedKeyPair = selectedKeyPair;
		this.context = context;
	}

	public void run() {
		KeyPairsDialog keyPairsDialog = new KeyPairsDialog(getShell(),
				selectedKeyPair, context);
		keyPairsDialog.create();
		keyPairsDialog.open();
		this.selectedKeyPair = keyPairsDialog.getDefaultKeyPair();
	}

	public String getSelectedKeyPair() {
		return selectedKeyPair;
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
		if (fAction != action) {
			fAction = action;
		}
		recalculateAndEnable();
	}

	public void partActivated(IWorkbenchPart part) {
		recalculateAndEnable();
	}

	public void partBroughtToTop(IWorkbenchPart part) {
	

	}

	public void partClosed(IWorkbenchPart part) {
		recalculateAndEnable();
	}

	public void partDeactivated(IWorkbenchPart part) {
	

	}

	public void partOpened(IWorkbenchPart part) {
	

	}

	/**
	 * Takes the active editor and calculate whether to enable key pairs action
	 * and then do that enabling/disabling
	 */
	private void recalculateAndEnable() {
		fAction.setEnabled(true);
	}
}
