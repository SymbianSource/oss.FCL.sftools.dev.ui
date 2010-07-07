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

package com.nokia.tools.screen.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.actions.AbstractImportProjectOperation;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;

public class ImportWizard extends Wizard implements IImportWizard {

	protected IWorkbench workbench;

	protected IStructuredSelection selection;

	private ImageDescriptor bannerIcon;

	ImportWizardPage1 page1;

	public ImportWizard() {
		setWindowTitle(WizardMessages.ImportWizard_title);
		IBrandingManager manager = BrandingExtensionManager
				.getBrandingManager();
		if (manager != null) {
			bannerIcon = UiPlugin
					.getImageDescriptor("icons/wizban/import_theme.png");
			setDefaultPageImageDescriptor(manager
					.getBannerImageDescriptor(bannerIcon));
		}
		setNeedsProgressMonitor(true);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}

	@Override
	public void addPages() {
		page1 = new ImportWizardPage1(WizardMessages.ImportWizard_title);
		addPage(page1);
	}

	@Override
	public boolean performFinish() {
		try {
			AbstractImportProjectOperation operation = page1
					.getImportAsOperation();
			getContainer().run(true, false, operation);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			Throwable t = e;
			String message = e.getLocalizedMessage();
			if (message == null)
				message = WizardMessages.ImpWizPg1_importproblem;
			while (t instanceof InvocationTargetException) {
				t = ((InvocationTargetException) t).getTargetException();
				MessageDialog.openError(getShell(),
						WizardMessages.ImportWizard_errMsgTitle, message);
				return true;
			}
			if (t instanceof CoreException) {
				ErrorDialog.openError(getShell(),
						WizardMessages.ImportWizard_errMsgTitle, message,
						((CoreException) t).getStatus());
				return true;
			} else {
				MessageDialog.openError(getShell(),
						WizardMessages.ImportWizard_errMsgTitle, message);
			}
			refreshProject();
		} catch (InterruptedException e) {
			e.printStackTrace();
			refreshProject();
		}
		return true;
	}

	private void refreshProject() {
		String projetName = page1.getImportAsOperation().getProjectName();
		if (projetName != null) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projetName);
			if (project != null && project.exists()) {
				try {
					project.refreshLocal(IResource.DEPTH_INFINITE,
							new NullProgressMonitor());
				} catch (CoreException e) {
					e.printStackTrace();
					ErrorDialog.openError(getShell(),
							WizardMessages.ImportWizard_errMsgTitle, e
									.getLocalizedMessage(), e.getStatus());
				}
			}
		}
	}

}
