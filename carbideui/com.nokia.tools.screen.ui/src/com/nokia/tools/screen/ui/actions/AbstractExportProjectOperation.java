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

import java.util.LinkedHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public abstract class AbstractExportProjectOperation extends
		WorkspaceModifyOperation {
	public static final String DIR = "dir";
	
	/**
	 * Function returning the export as options  eg :  dir=Directory , zip=Zip Archive
	 * @return
	 */
	public abstract LinkedHashMap<String, String> getExportAsOptions();

	/**
	 * Decide which component should handle the export , eg: s60 
	 * @param projectName
	 * @return
	 */
	public abstract boolean supportsProject(String projectName);

	/**
	 * Setting theme export parameters
	 * @param exportType
	 * @param project
	 * @param destination
	 */
	public abstract void setThemeExportParameters(String exportType,IProject project, String destination);

}
