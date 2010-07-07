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
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.nokia.tools.platform.extension.IThemeDescriptor;

public abstract class AbstractCreateProjectAction extends WorkspaceModifyOperation {

	private IProject project;

	private IThemeDescriptor themeDescriptor;

	public abstract String[] getReleases();

	public abstract String getDefaultResolution(String release);

	/**
	 * In create project action, checks whether action supports
	 * creation of theme which name equals to param release
	 * @param release
	 * @return
	 */
	public boolean isSupportsRelease(String release) {
		String[] temp = getReleases();
		if (temp != null) {
			for (String string : temp) {
				if (string.equals(release)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Used for copy existing theme, checks if existing file can be copied
	 * @param release
	 * @return
	 */
	public boolean isSupportsFileExtension(String fileName) {
		if (fileName == null)
			return false;
		IPath path = new Path(fileName);
		String[] extensions = getFileExtensions();
		for (String string : extensions) {
			if (string.equalsIgnoreCase(path.getFileExtension())) {
				return true;
			}
		}
		return false;
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public IThemeDescriptor getThemeDescriptor() {
		return themeDescriptor;
	}

	public void setThemeDescriptor(IThemeDescriptor themeDescriptor) {
		this.themeDescriptor = themeDescriptor;
	}

	public abstract void createLinked(String projectName, String filePath);

	public abstract String[] getFileExtensions();

	public ArrayList<String> getSupportedFileNames() {
		return null;
	};

	/**
	 * Specify opening of theme in editor
	 * 
	 * @return
	 */

	public boolean isOpenInDefaultEditor() {
		return false;
	}

	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {		
	}
	
	public boolean supportsPath(String fileName) {
		return isSupportsFileExtension(fileName);
	}

	public abstract boolean setCopyExistingThemeParameters(IProject project2,
			String projectName, String projectFolder, String release,
			String resolution, String fileName);

	public abstract boolean setCreateDefaultThemeParameters(IProject project2,
			String projectName, String projectFolder, String release,
			String resolution);
	
}
