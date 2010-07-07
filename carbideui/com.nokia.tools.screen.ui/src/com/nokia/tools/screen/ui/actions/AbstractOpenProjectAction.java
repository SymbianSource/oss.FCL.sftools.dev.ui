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
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;

import com.nokia.tools.platform.extension.IThemeDescriptor;

public abstract class AbstractOpenProjectAction extends Action {
	
	private IProject project;

	private IThemeDescriptor themeDescriptor;
	
	private Shell shell;
	
	private String themeFile;
	
	public String getThemeFile() {
		return themeFile;
	}

	public void setThemeFile(String themeFile) {
		this.themeFile = themeFile;
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

	/**
	 * Checks if file path can be opened, checks if file is 
	 * with supported extension
	 * @param path
	 * @return
	 */
	public boolean isProjectSupportsThisType(String path){
		for (String ext : getFileExtensions() ){
			if (path.toLowerCase().endsWith(ext)) {
				return true;
			}
		}
		return false;
	}

	public Shell getShell() {
		return shell;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}
	public abstract String[] getFileExtensions();
	
	public abstract void openProject(IProject openedProject);
	
	public abstract boolean isSupportsNature(IProject project);

}
