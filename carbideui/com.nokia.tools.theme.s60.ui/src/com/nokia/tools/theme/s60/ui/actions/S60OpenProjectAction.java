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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.nokia.tools.content.core.project.S60DesignProjectNature;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.ui.actions.AbstractOpenProjectAction;
import com.nokia.tools.screen.ui.actions.CreateProjectForExistingFilesOperation;
import com.nokia.tools.screen.ui.actions.OpenGUIForProjectOperation;
import com.nokia.tools.theme.s60.IThemeConstants;
import com.nokia.tools.theme.s60.ui.Activator;
import com.nokia.tools.theme.s60.ui.wizards.CreateNewThemeOperation;
import com.nokia.tools.theme.s60.ui.wizards.ExportThemeOperation;
import com.nokia.tools.theme.ui.Messages;
import com.nokia.tools.ui.dialog.MessageDialogWithTextContent;

public class S60OpenProjectAction extends AbstractOpenProjectAction {

	protected IProject createProjectFromTpf(String tpfPath) {
		String projectId = new Path(tpfPath).removeFileExtension()
				.lastSegment();
		String projectFolder = new Path(ResourcesPlugin.getWorkspace()
				.getRoot().getLocation().toFile().getAbsolutePath()).append(
				projectId).toString();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				projectId);
		
//		String release = getDefaultRelease();
//		
//		CreateNewThemeOperation themeCreationOp = new CreateNewThemeOperation(
//				project, projectId, projectFolder, release, null);
//		themeCreationOp.setTemplate(tpfPath);

		CreateProjectForExistingFilesOperation op = new CreateProjectForExistingFilesOperation(
				projectId, Arrays.asList(new String[] { tpfPath }));

		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true,
					false, op);

			// create theme content
//			if (null != themeCreationOp) {
//				PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(false,
//						false, themeCreationOp);
//			}
			project.refreshLocal(IResource.DEPTH_INFINITE, null);

			return project;

		} catch (InterruptedException e) {
			return null;
		} catch (InvocationTargetException e) {
			Activator.error(e);
			Throwable t = e.getTargetException();
			if (t instanceof CoreException) {
				ErrorDialog.openError(getShell(),
						Messages.Error_Opening_Project_Title,
						((CoreException) t).getLocalizedMessage(),
						((CoreException) t).getStatus());
			} else {
				MessageDialogWithTextContent.openError(getShell(),
						Messages.Error_Opening_Project_Title,
						Messages.Error_Opening_Project_Title, StringUtils
								.dumpThrowable(t));
			}
			return null;
		} catch (Throwable e) {
			Activator.error(e);
			if (e instanceof InvocationTargetException) {
				// ie.- one aof the steps resulted in a core exception
				e = ((InvocationTargetException) e).getTargetException();
			}
			if (e instanceof CoreException) {
				ErrorDialog.openError(getShell(),
						Messages.Error_Opening_Project_Title,
						((CoreException) e).getLocalizedMessage(),
						((CoreException) e).getStatus());

			} else {
				MessageDialogWithTextContent.openError(getShell(),
						Messages.Error_Opening_Project_Title,
						Messages.Error_Opening_Project_Title, StringUtils
								.dumpThrowable(e));
			}
			return null;
		}
	}

	protected String[] getReleases() {
		IThemeModelDescriptor[] descriptors = ThemePlatform
				.getThemeModelDescriptorsByContainer(IThemeConstants.THEME_CONTAINER_ID);
		String[] releases = new String[descriptors.length];
		for (int i = 0; i < descriptors.length; i++) {
			releases[i] = descriptors[i].getId();
		}
		return releases;
	}
	
	protected String getDefaultRelease() {
		String[] releases = getReleases();
		if(releases!=null && releases.length>0){
			return releases[releases.length-1];
		}
		return null;
	}

	@Override
	public String[] getFileExtensions() {
		return new String[] { ExportThemeOperation.THEME_EXT.substring(1) };
	}

	public static boolean isTpfFile(IPath projectPath) {
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			return (projectPath.getFileExtension()
					.equalsIgnoreCase(ExportThemeOperation.TPF_EXT.substring(1)));
		}
		return (projectPath.getFileExtension()
				.equals(ExportThemeOperation.TPF_EXT.substring(1)));
	}

	@Override
	public void run() {
		this.setProject(createProjectFromTpf(getThemeFile()));
	}

	@Override
	public void openProject(IProject openedProject) {
		WorkspaceModifyOperation op = null;
		op = new OpenGUIForProjectOperation(openedProject, (String) null,
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage());
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true,
					true, op);
		} catch (InterruptedException e) {
			return;
		} catch (InvocationTargetException e) {
			Activator.error(e);
			Throwable t = e.getTargetException();
			if (t instanceof CoreException) {
				ErrorDialog.openError(getShell(),
						Messages.Error_Opening_Project_Title,
						((CoreException) t).getLocalizedMessage(),
						((CoreException) t).getStatus());
			} else {
				MessageDialogWithTextContent.openError(getShell(),
						Messages.Error_Opening_Project_Title,
						Messages.Error_Opening_Project_Title, StringUtils
								.dumpThrowable(t));
			}
		}

	}

	@Override
	public boolean isSupportsNature(IProject project) {
		try {
			return project.hasNature(S60DesignProjectNature.NATURE_ID);
		} catch (CoreException e) {
		
			e.printStackTrace();
			return false;
		}
	}

}
