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
package com.nokia.tools.s60.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.ui.actions.AbstractNewProjectOperation;
import com.nokia.tools.screen.ui.actions.CreateProjectForExistingFilesOperation;
import com.nokia.tools.screen.ui.actions.OpenGUIForProjectOperation;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IContributorDescriptor;
import com.nokia.tools.screen.ui.wizards.NewProjectPage1;
import com.nokia.tools.screen.ui.wizards.NewProjectWizard;
import com.nokia.tools.theme.s60.ui.wizards.CreateNewThemeOperation;
import com.nokia.tools.ui.dialog.MessageDialogWithTextContent;

public class NewProjectWizardIntro extends NewProjectWizard {

	String projectName;
	int creationMode;
	String release;
	String resolution;
	String fileName;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getCreationMode() {
		return creationMode;
	}

	public void setCreationMode(int creationMode) {
		this.creationMode = creationMode;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getRelease() {
		return release;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {
		List<String> files = new ArrayList<String>(1);
		
		CreateNewThemeOperation themeCreationOp = null;
		// obtain absolute path to the folder where contents will be created
		String projectFolder = new Path(ResourcesPlugin.getWorkspace()
				.getRoot().getLocation().toFile().getAbsolutePath()).append(
				getProjectName()).toString();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				getProjectName());
		switch (getCreationMode()) {
		case NewProjectPage1.CREATEDEFAULT:
			themeCreationOp = new CreateNewThemeOperation(project,
					getProjectName(), projectFolder, getRelease(),
					getResolution());
			break;
		case NewProjectPage1.COPYEXISTING:
			themeCreationOp = new CreateNewThemeOperation(project,
					getProjectName(), projectFolder, getRelease(),
					getResolution());
			themeCreationOp.setTemplate(getFileName());
			break;
		}
		CreateProjectForExistingFilesOperation op = new CreateProjectForExistingFilesOperation(
				getProjectName(), files);

		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(false,
					false, op);

			// create theme content
			if (null != themeCreationOp) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(false,
						false, themeCreationOp);
			}

			// try contributors
			for (IContributorDescriptor desc : ExtensionManager
					.getContributorDescriptors()) {
				AbstractNewProjectOperation operation = (AbstractNewProjectOperation) desc
						.createOperation(IContributorDescriptor.OPERATION_NEW_PROJECT);
				if (operation != null) {
					operation.setProject(project);
					operation.setThemeDescriptor(ThemePlatform
							.getThemeModelDescriptorByName(release)
							.getThemeDescriptor());
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(
							true, true, operation);
				}
			}

			// refresh created project and open editor
			try {
				project.refreshLocal(IProject.DEPTH_INFINITE, null);
				createdProject = project;

				final OpenGUIForProjectOperation openGuiOp = new OpenGUIForProjectOperation(
						project, (String) null, PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage());

				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						try {
							openGuiOp.run(new NullProgressMonitor());
						} catch (InvocationTargetException e) {
							
							e.printStackTrace();
						} catch (InterruptedException e) {
							
							e.printStackTrace();
						}

					}

				});

			} catch (Exception e) {
				throw new InvocationTargetException(e);
			}
		} catch (InterruptedException e) {
			System.out.println("Interpupted project refresh");
			return false;
		} catch (Throwable e) {
			S60WorkspacePlugin.error(e);
			if (e instanceof InvocationTargetException) {
				// ie.- one aof the steps resulted in a core exception
				e = ((InvocationTargetException) e).getTargetException();
			}
			if (e instanceof CoreException) {
				ErrorDialog
						.openError(
								getShell(),
								com.nokia.tools.screen.ui.wizards.WizardMessages.New_Project_Error_Create_Project,
								e.getMessage(), ((CoreException) e).getStatus());

			} else {
				MessageDialogWithTextContent
						.openError(
								getShell(),
								com.nokia.tools.screen.ui.wizards.WizardMessages.New_Project_Error_Create_Project,
								com.nokia.tools.screen.ui.wizards.WizardMessages.New_Project_Error_Create_Project,
								StringUtils.dumpThrowable(e));
			}
			return false;
		}
		return true;
	}
}