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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.OpenResourceAction;

import com.nokia.tools.resource.util.ResourceUtils;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IContributorDescriptor;
import com.nokia.tools.screen.ui.wizards.OpenProjectDialog;


/**
 * Opening the project action Wraps standard OpenResourceAction and provides
 * custom dialog 
 * 
 */
public class OpenProjectAction extends Action implements
		IWorkbenchWindowActionDelegate {
	private Shell shell;

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

	public OpenProjectAction() {
		super();
	}

	public OpenProjectAction(Shell shell) {
		super();
		try {
			this.shell = shell;
			fWorkbenchAction = new OpenResourceAction(shell);
		} catch (RuntimeException e) {
			
			e.printStackTrace();
		}
	}

	public void run() {
		// if (true) {
		// new PluginInstallationDialog(getShell()).open();
		// return;
		// }

		OpenProjectDialog openProjectDialog = new OpenProjectDialog(getShell());
		openProjectDialog.create();
		if (openProjectDialog.open() == Dialog.OK) {
			String[] projectNames = openProjectDialog.getSelectedProjects();
			for (String projectName : projectNames) {
				String extractedDotProject = ResourceUtils
						.extractDotProjectPath(projectName);
				// System.out.println(extractedDotProject);
				IPath projectPath = new Path(extractedDotProject);
				IProject openedProject = null;
				if (projectPath.segmentCount() == 1
						&& ResourcesPlugin.getWorkspace().getRoot().getProject(
								projectName).exists()) {
					openedProject = openWorkspaceProject(projectName);
				} else if (isDotProject(projectPath)) {
					openedProject = getProjectUnderWorkspace(projectPath);
					if (openedProject == null) {
						// import it as it is not already in the workspace
						new ImportProjectAction(projectPath.toFile()).run();
						openedProject = getProjectUnderWorkspace(projectPath);
					}
				} else {
					for (IContributorDescriptor desc : ExtensionManager
							.getContributorDescriptors()) {
						AbstractOpenProjectAction operation = (AbstractOpenProjectAction) desc
								.createAction(IContributorDescriptor.OPERATION_OPEN_PROJECT);
						if (operation != null && operation.isProjectSupportsThisType(extractedDotProject)) {
							operation.setProject(openedProject);
							operation.setThemeFile(extractedDotProject);
							operation.setShell(getShell());
							operation.run();
							openedProject = operation.getProject();
						}
					}
				}
				if (null != openedProject) {					
					if (!openedProject.isOpen()) {
						try {
							openedProject.open(new NullProgressMonitor());
						} catch (CoreException ex) {
							ErrorDialog.openError(getShell(),
									Messages.Error_Opening_Project,
									ex.getLocalizedMessage(),
									ex.getStatus());
						}
					}
					for (IContributorDescriptor desc : ExtensionManager
							.getContributorDescriptors()) {
						AbstractOpenProjectAction operation = (AbstractOpenProjectAction) desc
								.createAction(IContributorDescriptor.OPERATION_OPEN_PROJECT);
						if (operation != null
								&& operation
										.isSupportsNature(openedProject)) {
//							System.out.println(operation.getClass().toString());
							operation.openProject(openedProject);
							continue;
						}
					}				
				}
			}
		} else
			notifyResult(false);
	}

	protected IProject openWorkspaceProject(String projectName) {
		// if already added to workspace, we need to open the project
		getWorkbenchAction().selectionChanged(
				new StructuredSelection(ResourcesPlugin.getWorkspace()
						.getRoot().getProject(projectName)));
		getWorkbenchAction().run();
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

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

	public static boolean isDotProject(IPath projectPath) {
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			return (projectPath.lastSegment()
					.equalsIgnoreCase(IProjectDescription.DESCRIPTION_FILE_NAME));
		}
		return (projectPath.lastSegment()
				.equals(IProjectDescription.DESCRIPTION_FILE_NAME));
	}

	public static IProject getProjectUnderWorkspace(IPath projectPath) {
		IProject project = ResourceUtils.findImportedProject(projectPath);
		if (project != null) {
			return project;
		}

		project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				projectPath.removeLastSegments(1).lastSegment());

		project = ResourceUtils.convertToRealProject(project);
		String workspace = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toFile().getAbsolutePath();
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			projectPath = new Path(projectPath.toFile().getAbsolutePath()
					.toLowerCase());
			workspace = workspace.toLowerCase();
		}

		IPath workspaceAbsPath = new Path(workspace);

		if (workspaceAbsPath.isPrefixOf(projectPath)
				&& workspaceAbsPath.equals(projectPath.removeLastSegments(2))
				&& project.exists()) {
			return project;
		}
		return null;
	}

}
