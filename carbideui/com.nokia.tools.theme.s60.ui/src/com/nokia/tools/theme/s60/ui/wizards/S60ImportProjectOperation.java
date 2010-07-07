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

package com.nokia.tools.theme.s60.ui.wizards;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.screen.ui.actions.AbstractImportProjectOperation;

public class S60ImportProjectOperation extends AbstractImportProjectOperation {
	private ImportThemeOperation themeOperation = new ImportThemeOperation();

	private String importType;

	private String source;

	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
			InvocationTargetException, InterruptedException {
		themeOperation.setImportType(this.importType);
		themeOperation.setSource(this.source);
		themeOperation.run(monitor);

	}

	@Override
	public LinkedHashMap<String, String> getImportFromOptions() {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("zip", "&ZIP or TPF Archive");
		map.put("dir", "&Folder");
		return map;
	}

	@Override
	public boolean supportsPath(String projectpath, String importType) {
		if (AbstractImportProjectOperation.DIR.equals(importType)) {
			return isValidS60DotProjectDirectory(projectpath);
		} else {
			if (ImportThemeOperation.ZIP.equals(importType)) {
				if (projectpath.endsWith(ImportThemeOperation.TPF_EXT)) {
					return isValidTpfArchiveFile(projectpath);
				} else {
					return isValidZippedProjectFile(projectpath);
				}
			} else {
				return false;
			}

		}

	}

	public static boolean isValidS60DotProjectDirectory(String source) {
		if (source != null) {
			boolean validDir = AbstractImportProjectOperation
					.isValidDotProjectDirectory(source);
			if (validDir) {
				File srcFile = new File(source);
				if (srcFile.exists() && srcFile.isDirectory()) {
					List<File> files = FileUtils.getFilesRealWorking(new File(source),
							new FilenameFilter() {
								public boolean accept(File dir, String name) {
									return (IProjectDescription.DESCRIPTION_FILE_NAME
											.equals(name) || name
											.endsWith(ImportThemeOperation.THEME_EXT));
								}
							});
					return files.size() >= 2;
				}
			}
		}
		return false;
	}

	public static boolean isValidTpfArchiveFile(String source) {
		if (source != null && new File(source).exists()) {
			try {
				ZipFile zf = new ZipFile(source);
				try {
					for (Enumeration entries = zf.entries(); entries
							.hasMoreElements();) {
						ZipEntry entry = (ZipEntry) entries.nextElement();
						if (!entry.isDirectory()
								&& new Path(entry.getName()).getFileExtension()
										.toLowerCase().equals(
												ImportThemeOperation.THEME_EXT
														.substring(1))) {													
							return true;
						}
					}
				} finally {
					zf.close();
				}
				return false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public HashMap<Boolean, String> validate(String projectPath,
			String importType) {
		HashMap<Boolean, String> map = null;
		if (AbstractImportProjectOperation.DIR.equals(importType)) {
			boolean valid = AbstractImportProjectOperation
					.isValidDotProjectDirectory(projectPath);
			map = new HashMap<Boolean, String>();
			if (!valid) {
				if (!new File(projectPath).exists()) {
					map.put(false, WizardMessages.ImpWizPg1_errNoDir);
				} else if (!new File(projectPath).isDirectory()) {
					map.put(false, WizardMessages.ImpWizPg1_errNoDir);
				} else {
					map.put(false, WizardMessages.ImpWizPg1_errInvalidDir);
				}
			} else {
				map.put(true, "");
			}
			return map;
		} else if (ImportThemeOperation.ZIP.equals(importType)) {
			map = new HashMap<Boolean, String>();
			if (projectPath.endsWith(ImportThemeOperation.TPF_EXT)) {
				boolean valid = isValidTpfArchiveFile(projectPath);
				map = new HashMap<Boolean, String>();
				if (!valid) {
					if (!new File(projectPath).exists()) {
						map.put(false, WizardMessages.ImpWizPg1_errInvalidFile);
					} else {
						map.put(false, WizardMessages.ImpWizPg1_errInvalidTpf);
					}
				} else {
					map.put(true, "");
				}
			} else {
				boolean valid = isValidZippedProjectFile(projectPath);
				if (!valid) {
					if (!new File(projectPath).exists()) {
						map.put(false, WizardMessages.ImpWizPg1_errInvalidFile);
					} else {
						map.put(false, WizardMessages.ImpWizPg1_errInvalidZip);
					}
				} else {
					map.put(true, "");
				}
			}
			return map;
		} else {
			map = new HashMap<Boolean, String>();
			map.put(false, WizardMessages.ImpWizPg1_errInvalidFile);
			return map;
		}

	}

	public boolean isValidZippedProjectFile(String source) {
		if (source != null && new File(source).exists()) {
			try {
				ZipFile zf = new ZipFile(source);
				try {
					int filesfound = 0;
					for (Enumeration entries = zf.entries(); entries
							.hasMoreElements();) {
						ZipEntry entry = (ZipEntry) entries.nextElement();

						if (!entry.isDirectory()
								&& (new Path(entry.getName())
										.lastSegment()
										.toLowerCase()
										.equals(
												IProjectDescription.DESCRIPTION_FILE_NAME) || new Path(
										entry.getName()).getFileExtension()
										.toLowerCase().equals(
												ImportThemeOperation.THEME_EXT
														.substring(1)))) {
							filesfound++;
							if (filesfound >= 2) {
								return true;
							}							
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

	@Override
	public String getProjectName() {
		themeOperation.setImportType(this.importType);
		themeOperation.setSource(this.source);
		return themeOperation.getProjectName();
	}

	@Override
	public void setThemeImportParameters(String importType, String source) {
		this.importType = importType;
		this.source = source;

	}

	@Override
	public ArrayList<String> getAlternateImportFileExtensions(String importType) {
		ArrayList<String> list = new ArrayList<String>();
		if ("zip".equals(importType)) {
			list.add("tpf");
		}
		return list;
	}

}
