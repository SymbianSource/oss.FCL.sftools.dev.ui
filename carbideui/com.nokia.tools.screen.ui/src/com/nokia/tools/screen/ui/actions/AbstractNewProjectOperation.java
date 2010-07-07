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
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.nokia.tools.platform.extension.IThemeDescriptor;

public abstract class AbstractNewProjectOperation extends
		WorkspaceModifyOperation {
	private IProject project;
	private IThemeDescriptor themeDescriptor;

	/**
	 * @return Returns the project.
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * @param project The project to set.
	 */
	public void setProject(IProject project) {
		this.project = project;
	}

	/**
	 * @return the themeDescriptor
	 */
	public IThemeDescriptor getThemeDescriptor() {
		return themeDescriptor;
	}

	/**
	 * @param themeDescriptor the themeDescriptor to set
	 */
	public void setThemeDescriptor(IThemeDescriptor themeDescriptor) {
		this.themeDescriptor = themeDescriptor;
	}
}
