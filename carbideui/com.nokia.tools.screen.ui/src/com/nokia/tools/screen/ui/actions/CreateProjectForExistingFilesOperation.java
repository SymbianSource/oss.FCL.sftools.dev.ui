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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;

import com.nokia.tools.content.core.project.S60DesignProjectNature;

/**
 * Based on existing file list creates project in current workspace and links
 * files to it
 * 
 */
public class CreateProjectForExistingFilesOperation extends
		WorkspaceModifyOperation {

	List<String> filesToLink;

	String projectName;

	public CreateProjectForExistingFilesOperation(String projectName,
			List<String> files) {
		filesToLink = files;
		this.projectName = projectName;
	}

	public String getProjectName() {
		return projectName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
			InvocationTargetException, InterruptedException {
//		@Externalize
		
		if(monitor instanceof SubProgressMonitor){
			monitor = ((SubProgressMonitor)monitor).getWrappedProgressMonitor();
		}
		if(monitor != null){
			monitor.beginTask("Creating project", IProgressMonitor.UNKNOWN);
		}
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				getProjectName());
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace
				.newProjectDescription(project.getName());
		
		description.setLocation(null);
		if(!project.exists())
			project.create(description, new SubProgressMonitor(monitor, 300));
		
		project.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(
				monitor, 300));
		S60DesignProjectNature.addNatureToProject(project, S60DesignProjectNature.NATURE_ID);
		// link files to the project
		for (String linkPath : filesToLink) {
			IPath linkTargetPath = new Path(linkPath);
            ContainerGenerator generator = new ContainerGenerator(
            		project.getFullPath() );
            generator.generateContainer(new SubProgressMonitor(monitor,
                    1000));
			IFile fileHandle = ResourcesPlugin.getWorkspace().getRoot()
					.getFile( project.getFullPath().append(linkTargetPath.lastSegment()) );
			
			fileHandle.createLink(linkTargetPath,
					IResource.ALLOW_MISSING_LOCAL, monitor);
		}
		project.refreshLocal(IProject.DEPTH_INFINITE, monitor);
		
		if (monitor.isCanceled())
			throw new OperationCanceledException();
		monitor.done();
	}
	

}
