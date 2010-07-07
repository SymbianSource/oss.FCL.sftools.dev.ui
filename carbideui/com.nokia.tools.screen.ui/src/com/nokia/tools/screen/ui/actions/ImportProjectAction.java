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

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;


/**
 * Functionality copied from org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage
 * The reason from copy is internal and private implementation of it
 * 
 * Changed the constructor to public
 *
 */
public class ImportProjectAction extends Action {

	File dotprojectFile;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		super.run();
		ProjectRecord project = new ProjectRecord(dotprojectFile);
		if( createExistingProject(project))
			System.out.println("Now open the editor");
	}

	public void setProjectFileToImportFrom(File projectFile) {
		this.dotprojectFile = projectFile;
	}

	public ImportProjectAction(File projectFile) {
		setProjectFileToImportFrom(projectFile);
	}

	/**
	 * Copied from org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage.ProjectRecord
	 *
	 */
	class ProjectRecord {
		File projectSystemFile;

		Object projectArchiveFile;

		String projectName;

		Object parent;

		int level;

		IProjectDescription description;

		/**
		 * Create a record for a project based on the info in the file.
		 * 
		 * @param file
		 */
		ProjectRecord(File file) {
			projectSystemFile = file;
			setProjectName();
		}

		/**
		 * Set the name of the project based on the projectFile.
		 */
		private void setProjectName() {
			IProjectDescription newDescription = null;
			try {
				IPath path = new Path(projectSystemFile.getPath());
				newDescription = ResourcesPlugin.getWorkspace()
						.loadProjectDescription(path);

			} catch (CoreException e) {
				// no good couldn't get the name
			}

			if (newDescription == null) {
				this.description = null;
				projectName = ""; //$NON-NLS-1$
			} else {
				this.description = newDescription;
				projectName = this.description.getName();
			}
		}

		/**
		 * Get the name of the project
		 * 
		 * @return String
		 */
		public String getProjectName() {
			return projectName;
		}
	}
	
	/**
	 * @see boolean org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage.createExistingProject(ProjectRecord record)
	 * Functionality is copied from org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage
	 * with difference that importing from archive is not implemented
	 * Create the project described in record. If it is successful return true.
	 * 
	 * @param record
	 * @return boolean <code>true</code> of successult
	 */
	private boolean createExistingProject(final ProjectRecord record) {

		String projectName = record.getProjectName();
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(projectName);
		if (record.description == null) {
			record.description = workspace.newProjectDescription(projectName);
			IPath locationPath = new Path(record.projectSystemFile
					.getAbsolutePath());
			// IPath locationPath = new
			// Path(record.projectFile.getFullPath(record.projectFile.getRoot()));

			// If it is under the root use the default location
			if (Platform.getLocation().isPrefixOf(locationPath))
				record.description.setLocation(null);
			else
				record.description.setLocation(locationPath);
		} else
			record.description.setName(projectName);

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor)
					throws CoreException {
				monitor.beginTask("", 2000); //$NON-NLS-1$
				project.create(record.description, new SubProgressMonitor(
						monitor, 1000));
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				project.open(IResource.BACKGROUND_REFRESH,
						new SubProgressMonitor(monitor, 1000));
			}
		};
		// run the new project creation operation
		IWorkbenchWindow window = PlatformUI.getWorkbench()
		.getActiveWorkbenchWindow();

		try {
			window.run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			// ie.- one of the steps resulted in a core exception
			Throwable t = e.getTargetException();
			if (((CoreException) t).getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
				MessageDialog.openError(window.getShell(), 
						Messages.Error_Opening_Project_Title,
						NLS.bind(
								Messages.Error_Opening_Project_AlreadyExists,
								record.description.getName())
				);
			} else {
				ErrorDialog.openError(window.getShell(), 
						Messages.Error_Opening_Project_Title,
						((CoreException) t).getLocalizedMessage(), 
						((CoreException) t).getStatus());
			}
			return false;
		}
		return true;
	}



}
