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

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.nokia.tools.content.core.CoreMessages;
import com.nokia.tools.content.core.CorePlugin;
import com.nokia.tools.content.core.IContentSourceManager;

/**
 * Note: generic level utilities for adding UI design nature to project.
 * 
 */
public class S60DesignProjectNature implements IProjectNature {
	public static final String NATURE_ID = CorePlugin.getDefault().getBundle()
			.getSymbolicName()
			+ ".S60DesignNature"; //$NON-NLS-1$

	private IProject fProject;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return fProject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		fProject = project;
	}

	/**
	 * Utility copied from
	 * org.eclipse.jem.internal.beaninfo.adapters.BeaninfoNature.addNatureToProject
	 * 
	 * @param proj
	 * @param natureId - id of the neature that is to be added to proj
	 * @throws CoreException
	 */
	public static void addNatureToProject(IProject proj, String natureId)
			throws CoreException {
		IProjectDescription description = proj.getDescription();
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = natureId;
		description.setNatureIds(newNatures);
		proj.setDescription(description, null);

	}

	/**
	 *
	 */
	public static IContentSourceManager getUIDesignData(IProject project)
			throws CoreException {
		if (!isValidProject(project))
			throw new CoreException(new Status(IStatus.ERROR, CorePlugin
					.getDefault().getBundle().getSymbolicName(), 0,
					MessageFormat.format(CoreMessages.Error_Opening_Project,
							new Object[] { project.getName(),
									CoreMessages.Project_Not_S60_Data }), null));
		return new ProjectContentSourceManager(project);
	}

	/**
	 */
	public static boolean isValidProject(IProject project) {
		try {
			return project.hasNature(NATURE_ID);
		} catch (CoreException e) {
			return false;
		}
	}
}
