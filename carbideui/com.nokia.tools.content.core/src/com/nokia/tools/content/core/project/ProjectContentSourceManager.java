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
package com.nokia.tools.content.core.project;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.nokia.tools.content.core.AbstractContentSourceManager;
import com.nokia.tools.content.core.ContentException;
import com.nokia.tools.content.core.CoreMessages;
import com.nokia.tools.content.core.CorePlugin;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.resource.util.ResourceUtils;

/**
 * This manager will traverse through all project files to find {@link IContent}.
 * 
 */
public class ProjectContentSourceManager extends AbstractContentSourceManager {

	private IProject project;

	/**
	 * Constructs a project source manager.
	 * 
	 * @param project the project from where the contents will be fetched.
	 */
	public ProjectContentSourceManager(IProject project) {
		this.project = project;
	}

	/**
	 * Loading whole project BAsed on requirements to display all screens on the
	 * screen with no loading from files, here is loading the whole theme
	 * project
	 * 
	 * @param project
	 */
	protected List<IFile> getProjectContents() throws CoreException {
		if (!project.isAccessible()) {
			throw new CoreException(new Status(IStatus.ERROR, CorePlugin
					.getDefault().getBundle().getSymbolicName(), 0,
					MessageFormat.format(CoreMessages.Project_NotAccessible,
							new Object[] { project.getName() }), null));
		}
		return ResourceUtils.getAllProjectFiles(project);// ProjectUtilities.project.members();
	}

	/**
	 * @return the project
	 */
	public IProject getProject() {
		return project;
	}

	/*
	 * From all the possible root contents here we returns only one defined by
	 * the input list
	 * 
	 * @see com.nokia.tools.content.core.AbstractContentSourceManager#getRootContents(java.lang.String)
	 */
	@Override
	public List<IContent> getRootContents(String type, IProgressMonitor monitor)
			throws IOException, ContentException {
		try {
			List<IContent> contents = new ArrayList<IContent>();
			for (IFile file : getProjectContents()) {
				List<IContent> loadedInputs = getRootContents(type, file,
						monitor);
				contents.addAll(loadedInputs);
			}
			return contents;
		} catch (CoreException e) {
			throw new ContentException(e);
		}
	}
}
