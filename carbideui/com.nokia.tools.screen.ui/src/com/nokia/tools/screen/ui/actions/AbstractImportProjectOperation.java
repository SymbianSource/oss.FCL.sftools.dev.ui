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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public abstract class AbstractImportProjectOperation extends
		WorkspaceModifyOperation {
	public static final String DIR = "dir";

	/**
	 * Function getting import from options
	 * 
	 * @return
	 */
	public abstract LinkedHashMap<String, String> getImportFromOptions();

	/**
	 * Decide which component should handle the import , eg: s60 
	 * 
	 * @param projectName
	 * @return
	 */
	public abstract boolean supportsPath(String projectpath, String importType);

	/**
	 * Check whether the source folder is a valid source project directory
	 * 
	 * @param source
	 * @return
	 */
	public static boolean isValidDotProjectDirectory(String source) {
		if (source != null) {
			File srcFile = new File(source);
			if (srcFile.exists() && srcFile.isDirectory()) {
				String[] projectDescription = srcFile
						.list(new FilenameFilter() {
							public boolean accept(File dir, String name) {
								return (IProjectDescription.DESCRIPTION_FILE_NAME
										.equals(name));
							}
						});
				return projectDescription.length == 1;
			}
		}
		return false;
	}

	/**
	 * Checking whether the argument passed is a valid zipped project file
	 * 
	 * @param source
	 * @return
	 */
	public boolean isValidZippedProjectFile(String source) {
		if (source != null && new File(source).exists()) {
			try {
				ZipFile zf = new ZipFile(source);
				try {
					for (Enumeration entries = zf.entries(); entries
							.hasMoreElements();) {
						ZipEntry entry = (ZipEntry) entries.nextElement();

						if (!entry.isDirectory()
								&& (new Path(entry.getName()).lastSegment()
										.toLowerCase()
										.equals(IProjectDescription.DESCRIPTION_FILE_NAME))) {
							return true;
						}
					}
				} finally {
					zf.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Function for validation , The sub classes should override this function
	 * to display error messages in Import Dialog
	 * 
	 * @param projectPath
	 * @param importType
	 * @return
	 */
	public abstract HashMap<Boolean, String> validate(String projectPath,
			String importType);

	public abstract String getProjectName();
	/**
	 * Setting theme import parameters
	 * @param importType
	 * @param source
	 */
	public abstract void setThemeImportParameters(String importType,
			String source);
	/**
	 * getting the file extensions eg: zip 
	 * @return
	 */
	public abstract ArrayList<String> getAlternateImportFileExtensions(String importType);
}
