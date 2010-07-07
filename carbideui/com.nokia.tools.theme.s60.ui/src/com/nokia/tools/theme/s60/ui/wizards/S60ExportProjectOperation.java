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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.nokia.tools.resource.util.ResourceUtils;
import com.nokia.tools.screen.ui.actions.AbstractExportProjectOperation;

public class S60ExportProjectOperation extends AbstractExportProjectOperation {

	ExportThemeOperation operation = null;

	private IProject project;

	private String exportType;

	private String destination;
	
	@Override
	public LinkedHashMap<String, String> getExportAsOptions() {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		
		map.put("tpf", "&TPF Archive");
		map.put("zip", "&ZIP Archive");
		map.put("dir", "&Folder");
		return map;

	}

	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
			InvocationTargetException, InterruptedException {
		operation =new ExportThemeOperation();
		operation.setExportType(exportType);
		operation.setSource(this.project);
		operation.setTarget(destination);
		try {
			operation.run(monitor);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getExportType(String type) {
		return getExportAsOptions().get(type);
	}

	@Override
	public boolean supportsProject(String projectName) {
		if(projectName==null)return false;
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				projectName);
		List<IFile> files=ResourceUtils.getAllProjectFiles(project);
		ArrayList<String> list=new ArrayList<String>();
		for (IFile file : files) {
			if(file.getName().equals(IProjectDescription.DESCRIPTION_FILE_NAME)){
				list.add(file.getName());
			}else{
				if(ExportThemeOperation.THEME_EXT.substring(1).equals(file.getFileExtension())){
					list.add(file.getName());
				}
			}
		}
		
		if(list.size()>=2){
			return true;
		}else{
			return false;
		}
		
	}

	@Override
	public void setThemeExportParameters(String exportType, IProject project,
			String destination) {
		this.project=project;
		this.exportType=exportType;
		this.destination=destination;
		
	}

}
