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
package com.nokia.tools.theme.s60.ui.actions;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.IDevice;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.screen.ui.actions.AbstractCreateProjectAction;
import com.nokia.tools.screen.ui.actions.AbstractNewProjectOperation;
import com.nokia.tools.screen.ui.actions.CreateProjectForExistingFilesOperation;
import com.nokia.tools.screen.ui.contribution.ResourcesContributionExtension;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IContributorDescriptor;
import com.nokia.tools.screen.ui.extension.IResourceContributionDescriptor;
import com.nokia.tools.theme.s60.IThemeConstants;
import com.nokia.tools.theme.s60.ui.wizards.CreateNewThemeOperation;
import com.nokia.tools.theme.s60.ui.wizards.ExportThemeOperation;

public class S60CreateProjectAction extends AbstractCreateProjectAction {
	
	private IProject parentProject = null;
	private String projectName = null;
	private String projectFolder = null;
	private String release = null;
	private String initialResolution = null;
	private String themeFileName = null;
	
	private boolean copyExisting = false;
	private boolean createDefault = false;

	@Override
	public String getDefaultResolution(String release) {
		String[] sizes = getSizes(release);
		if (sizes != null && sizes.length > 0) {
			return sizes[0];
		}
		return null;
	}

	@Override
	public String[] getReleases() {
		IThemeModelDescriptor[] descriptors = ThemePlatform
				.getThemeModelDescriptorsByContainer(IThemeConstants.THEME_CONTAINER_ID);
		String[] releases = new String[descriptors.length];
		for (int i = 0; i < descriptors.length; i++) {
			releases[i] = descriptors[i].getId();
		}
		return releases;
	}

	protected String[] getSizes(String release) {
		IDevice[] devices = ThemePlatform.getDevicesByThemeModelId(release);
		String[] sizes = new String[devices.length];
		if (null != devices) {
			for (int i = 0; i < devices.length; i++) {
				Display display = devices[i].getDisplay();
				sizes[i] = display.getWidth() + "x" + display.getHeight();
			}
		}
		return sizes;
	}
		
	private boolean isCopyExisting() {
		return copyExisting;
	}
	
	private boolean isCreateDefault() {
		return createDefault;
	}
	
	private void setCopyExisting(boolean b) {
		copyExisting = b;		
	}
	
	private void setCreateDefault(boolean b) {
		createDefault = b;		
	}
	
	
	@Override
	public boolean setCopyExistingThemeParameters(IProject project2,
			String projectName, String projectFolder, String release,
			String resolution, String fileName) {
		
		if(isCopyExisting() || isCopyExisting()){
			return false;
		}
		
		this.parentProject = project2;
		this.projectName = projectName;
		this.projectFolder = projectFolder;
		this.release = release;
		this.initialResolution = resolution;
		this.themeFileName = fileName;
		
		setCopyExisting(true);
		return true;
	}
	
	@Override
	public boolean setCreateDefaultThemeParameters(IProject project2,
			String projectName, String projectFolder, String release,
			String resolution) {
		
		if(isCopyExisting() || isCopyExisting()){
			return false;
		}
		
		this.parentProject = project2;
		this.projectName = projectName;
		this.projectFolder = projectFolder;
		this.release = release;
		this.initialResolution = resolution;
			
		setCreateDefault(true);
		return true;
	}
	
	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
			InvocationTargetException, InterruptedException {
				
		if(isCreateDefault()){
			createDefault(monitor);
		}else if(isCopyExisting()){
			copyExisting(monitor);
		}
	}
	
	public void copyExisting(IProgressMonitor monitor) {
		if(monitor instanceof SubProgressMonitor){
			monitor = ((SubProgressMonitor)monitor).getWrappedProgressMonitor();
		}
		if(monitor != null){
			monitor.beginTask("", IProgressMonitor.UNKNOWN);
		}
		
		List<String> files = new ArrayList<String>(1);
		CreateNewThemeOperation themeCreationOp = null;
		themeCreationOp = new CreateNewThemeOperation(parentProject, projectName,
				projectFolder, release, getDefaultResolution(release));
		themeCreationOp.setTemplate(themeFileName);
		CreateProjectForExistingFilesOperation op = new CreateProjectForExistingFilesOperation(
				projectName, files);
		try {
			op.run(monitor);
			
			// copy theme folder contents
			copyThemeFolderContents(projectName, themeFileName);
			
			// create theme content
			if (null != themeCreationOp) {
				themeCreationOp.run(monitor);
			}

			// try contributors
			for (IContributorDescriptor desc : ExtensionManager
					.getContributorDescriptors()) {

				AbstractNewProjectOperation operation = (AbstractNewProjectOperation) desc
						.createOperation(IContributorDescriptor.OPERATION_NEW_PROJECT);
				if (operation != null) {
					operation.setProject(parentProject);
					operation.setThemeDescriptor(ThemePlatform
							.getThemeModelDescriptorById(release)
							.getThemeDescriptor());
					operation.run(monitor);
				}
			}
		} catch (InvocationTargetException e) {			
			e.printStackTrace();
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
		
		monitor.done();

	}

	public void createDefault(IProgressMonitor monitor) {
		if(monitor instanceof SubProgressMonitor){
			monitor = ((SubProgressMonitor)monitor).getWrappedProgressMonitor();
		}
		if(monitor != null){
			monitor.beginTask("", IProgressMonitor.UNKNOWN);
		}
		
		List<String> files = new ArrayList<String>(1);
		CreateNewThemeOperation themeCreationOp = null;
		themeCreationOp = new CreateNewThemeOperation(parentProject, projectName,
				projectFolder, release, getDefaultResolution(release));
		CreateProjectForExistingFilesOperation op = new CreateProjectForExistingFilesOperation(
				projectName, files);
		try {
			op.run(monitor);
			// create theme content
			if (null != themeCreationOp) {
				themeCreationOp.run(monitor);
			}

			// try contributors
			for (IContributorDescriptor desc : ExtensionManager
					.getContributorDescriptors()) {

				AbstractNewProjectOperation operation = (AbstractNewProjectOperation) desc
						.createOperation(IContributorDescriptor.OPERATION_NEW_PROJECT);

				if (operation != null) {
					operation.setProject(parentProject);
					operation.setThemeDescriptor(ThemePlatform
							.getThemeModelDescriptorById(release)
							.getThemeDescriptor());
					operation.run(monitor);
				}
			}
		} catch (InvocationTargetException e) {
			
			e.printStackTrace();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
	}

	@Override
	public void createLinked(String projectName, String filePath) {
		List<String> files = new ArrayList<String>(1);
		files.add(filePath);
		CreateProjectForExistingFilesOperation op = new CreateProjectForExistingFilesOperation(
				projectName, files);
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true,
					false, op);
		} catch (InvocationTargetException e) {
			
			e.printStackTrace();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}

	}

	@Override
	public String[] getFileExtensions() {
		return new String[] { ExportThemeOperation.THEME_EXT.substring(1) };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.actions.AbstractCreateProjectAction#isOpenInDefaultEditor()
	 */
	@Override
	public boolean isOpenInDefaultEditor() {
		
		return true;
	}
	private void copyThemeFolderContents(final String projectName ,String themeFile){
		if(themeFile!=null){
			IResourceContributionDescriptor[] desc=ResourcesContributionExtension.getContributorDescriptors();
			final ArrayList<String> contrbpaths=new ArrayList<String>();
			for (int i = 0; i < desc.length; i++) {
				if(!contrbpaths.contains(desc[i].getDestinationPath())){
					contrbpaths.add(desc[i].getDestinationPath());
				}
			}
			IPath newPath=new Path(themeFile).removeLastSegments(2);
			final String oldProjectName=newPath.lastSegment();
			File file=newPath.toFile();
			File[] fileList=file.listFiles(new FileFilter() {
		        public boolean accept(File file) {
		            return (file.isDirectory() && (!file.getName().equals(oldProjectName)) && contrbpaths.contains(file.getName()));
		        }
		    });
			IProject project=ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			IPath projectLocation=project.getLocation();
			if(projectLocation==null)return;
			for (File fileName : fileList) {
				try {
					FileUtils.copyDir(fileName,new File(projectLocation.toString()+"\\"+fileName.getName()));
				} catch (IOException e) {
				
					e.printStackTrace();
				}
			}
		}
	}
}
