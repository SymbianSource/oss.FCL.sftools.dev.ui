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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;

import com.nokia.tools.content.core.ContentException;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.project.S60DesignProjectNature;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.theme.content.ThemeUtil;
import com.nokia.tools.theme.s60.S60ThemeProvider;
import com.nokia.tools.theme.s60.ui.Activator;

/**
 * 
 */
public class CreateNewThemeOperation extends WorkspaceModifyOperation {

	public static final String THEME_DEFINITION_FILE_EXTENSION = ".tdf";

	public static final String PACKED_THEME_FILE_EXTENSION = ".tpf";

	private String name;

	private String folder;

	private String release;

	private String resolution;

	private String template;

	private IProject parentProject;

	public CreateNewThemeOperation(IProject parentProject, String name,
			String folder, String release, String initialResolution) {
		this.name = name;
		this.folder = folder;
		this.release = release;
		this.resolution = initialResolution;
		this.parentProject = parentProject;
	}

	/**
	 * seting the theme template path, template is .tdf file path template is
	 * optional, if not set theme is created from default template
	 * 
	 * @param themeTemplate
	 */
	public void setTemplate(String themeTemplate) {
		template = themeTemplate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
			InvocationTargetException, InterruptedException {
		// @Externalize
		if(monitor instanceof SubProgressMonitor){
			monitor = ((SubProgressMonitor)monitor).getWrappedProgressMonitor();
		}
		if(monitor != null){
			monitor.beginTask("Creating Theme", IProgressMonitor.UNKNOWN);
		}
		
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(ThemeUtil.TAG_THEME_FOLDER, folder);
		attributes.put(ThemeUtil.TAG_THEME_NAME, name);
		attributes.put(ThemeUtil.TAG_THEME_PLATFORM, release);
		attributes.put(ThemeUtil.TAG_THEME_RESOLUTION, resolution);

		List<File> filesToRemove = null;
		try {
			if (null != template) {
				// extract files from tpf file to temporary directory
				if (template.toLowerCase()
						.endsWith(PACKED_THEME_FILE_EXTENSION)) {
					try {
						File dummyFile = File.createTempFile("tpf", null);
						File tempDir = new File(dummyFile.getAbsolutePath());
						dummyFile.delete();
						filesToRemove = FileUtils.unzip(new File(template),
								tempDir);
						filesToRemove.add(tempDir);

						for (File file : filesToRemove) {
							if (file.getName().toLowerCase().endsWith(
									THEME_DEFINITION_FILE_EXTENSION)) {
								attributes.put(ThemeUtil.TAG_THEME_TEMPLATE,
										file.getAbsolutePath());
								break;
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					attributes.put(ThemeUtil.TAG_THEME_TEMPLATE, template);
				}
			}
		

			try {
				IPath containerPath;
				containerPath = ResourcesPlugin.getWorkspace().getRoot()
						.getFullPath()
						.append(new Path(parentProject.getName())).append(
								new Path(name));

				ContainerGenerator generator = new ContainerGenerator(
						containerPath.removeLastSegments(1));

				generator
						.generateContainer(new SubProgressMonitor(monitor, 10));

			} catch (CoreException e1) {
				
				e1.printStackTrace();
			}

			IThemeModelDescriptor descriptor = ThemePlatform
					.getThemeModelDescriptorById(release);
			String contentType = descriptor.getThemeDescriptor()
					.getContentType();

			// for reading old plugins where the content type is missing
			if (contentType == null) {
				contentType = S60ThemeProvider.CONTENT_TYPE;
			}

			List<IContent> contents;
			try {
				contents = S60DesignProjectNature
						.getUIDesignData(parentProject).createRootContents(
								contentType, attributes, monitor);
			} catch (ContentException e) {
				
				throw new CoreException(new Status(IStatus.ERROR,
						Activator.PLUGIN_ID, 0, e.getMessage(), e));

			}

			parentProject.refreshLocal(IProject.DEPTH_INFINITE, monitor);
			for (IContent cont : contents) {
				IPath path = (IPath) cont.getAdapter(IPath.class);
				path.makeAbsolute();
				if (null != path) {
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
							.getRoot();
					IPath workspacePath = root.getLocation().makeAbsolute();
					path = path.removeFirstSegments(path
							.matchingFirstSegments(workspacePath));
					path.setDevice(root.getFullPath().getDevice());
					IFile file = root.getFile(path);

					int numSegments = path.segmentCount();
					if (numSegments > 2
							&& !root.getFolder(path.removeLastSegments(1))
									.exists()) {
						// If the direct parent of the path doesn't exist, try
						// to
						// create the
						// necessary directories.
						for (int i = numSegments - 2; i > 0; i--) {
							IFolder folder = root.getFolder(path
									.removeLastSegments(i));

							if (!folder.exists()) {
								try {
									folder.create(false, true, monitor);
								} catch (CoreException e) {
									
									e.printStackTrace();
								}
							}
							folder.refreshLocal(2, monitor);
						}
					}
					try {
						if (!file.exists())
							file.create(null, false, monitor);
					} catch (CoreException e) {
						
						e.printStackTrace();
					}

					file.refreshLocal(0, monitor);

				}
			}
		} finally {
			if (null != filesToRemove) {
				for (int i = filesToRemove.size() - 1; i >= 0; i--) {
					filesToRemove.get(i).delete();
				}
			}
		}
		monitor.done();

	}

}
