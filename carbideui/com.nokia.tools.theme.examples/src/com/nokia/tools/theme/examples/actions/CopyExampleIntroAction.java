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
package com.nokia.tools.theme.examples.actions;

import java.io.File;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;

import com.nokia.tools.screen.ui.wizards.NewProjectPage1;
import com.nokia.tools.screen.ui.wizards.NewProjectWizard;
import com.nokia.tools.theme.examples.ExampleThemePlugin;

/**
 * Action class which will be invoked when user wants to create a new theme
 * using sample themes, by clicking the link on the welcome page. This will
 * invoke the New project creation wizard with "Copy an existing theme" selected
 * and the theme path specified.
 * 
 */
public class CopyExampleIntroAction extends Action implements
		IWorkbenchWindowActionDelegate, IIntroAction {

	public CopyExampleIntroAction() {
		super();
	}

	public CopyExampleIntroAction(Shell shell) {
		super();
	}

	public void run(IIntroSite site, Properties params) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IViewReference[] viewRefs = page.getViewReferences();
		for (int i = 0; i < viewRefs.length; i++) {
			if (viewRefs[i].getId().equals("org.eclipse.ui.internal.introview")) {
				IViewPart part = page
						.findView("org.eclipse.ui.internal.introview");
				page.hideView(part);
			}
		}
		NewProjectWizard newProjectWizard = new NewProjectWizard();
		WizardDialog dialog = new WizardDialog(getShell(), newProjectWizard);
		String tdfPath = params.getProperty("tdfPath");
		if (tdfPath != null) {
			try {
				tdfPath = tdfPath.replace("%20", " ");
				// String installDir =
				// Platform.getInstallLocation().getURL().getFile();

				String themeFolderLocation = FileLocator.toFileURL(
						FileLocator.find(ExampleThemePlugin.getDefault()
								.getBundle(), new Path("/"), null)).getPath();
				tdfPath = new File(tdfPath.replace("${install.folder}",
						themeFolderLocation)).getCanonicalPath().toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		newProjectWizard.setFileName(tdfPath);
		newProjectWizard.setProjectName(params.getProperty("name"));
		newProjectWizard.setCreationMode(NewProjectPage1.COPYEXISTING);
		// newProjectWizard.setRelease(params.getProperty("release"));
		// newProjectWizard.setResolution(params.getProperty("resolution"));
		// newProjectWizard.performFinish();
		if (dialog.open() == Window.OK) {
			newProjectWizard.performFinish();
			notifyResult(false);
		}
	}

	Shell getShell() {
		return null;
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
	 * @seeorg.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.
	 * IWorkbenchWindow)
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
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
	 * .IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {

	}
}
