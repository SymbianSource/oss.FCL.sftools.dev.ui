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

package com.nokia.tools.resource.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.resource.core.Activator;

/**
 * This class provides a collection of methods for eclipse resource
 * manipulation.
 *  
 */
public class ResourceUtils {
	/**
	 * No construction.
	 */
	private ResourceUtils() {
	}

	public static IProject findImportedProject(IPath path) {
		// tests the location first in case the project is imported and
		// resides outside workspace folder

		if ((Platform.OS_WIN32.equals(Platform.getOS()) && path.lastSegment()
		    .equalsIgnoreCase(IProjectDescription.DESCRIPTION_FILE_NAME))
		    || (path.lastSegment()
		        .equalsIgnoreCase(IProjectDescription.DESCRIPTION_FILE_NAME))) {
			path = path.removeLastSegments(1);
		}
		File location = path.toFile();
		for (IProject prj : ResourcesPlugin.getWorkspace().getRoot()
		    .getProjects()) {
			if (location.equals(prj.getLocation().toFile())) {
				return prj;
			}
		}
		return null;
	}

	public static IProject convertToRealProject(IProject project) {
		IProject realProject = getProjectByName(project.getName());
		if (realProject != null) {
			return realProject;
		}
		return project;
	}

	public static IProject getProjectByName(String projectName) {
		for (IProject prj : ResourcesPlugin.getWorkspace().getRoot()
		    .getProjects()) {
			if ((Platform.OS_WIN32.equals(Platform.getOS()) && prj.getName()
			    .equalsIgnoreCase(projectName))
			    || (prj.getName().equals(projectName))) {
				return prj;
			}
		}
		return null;
	}

	public static String extractDotProjectPath(String path) {
		IPath p = new Path(path);
		if (p.segmentCount() == 1) {
			return path;
		}
		String convertedName = p.lastSegment();
		if (convertedName == null)
			return null;
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			convertedName = convertedName.toLowerCase();
		}

		if (!convertedName.equals(IProjectDescription.DESCRIPTION_FILE_NAME)) {
			// here is some workaround to find the enclosing project of the .tdf
			// file.
			File tdf = new File(path);
			File projectDir = tdf.getParentFile().getParentFile();
			File projectFile = new File(projectDir,
			    IProjectDescription.DESCRIPTION_FILE_NAME);
			if (projectFile.exists()) {
				return projectFile.getAbsolutePath();
			}
		}
		return path;
	}

	public static URL toURL(IResource resource) {
		return resource == null ? null : FileUtils.toURL(resource.getLocation()
		    .makeAbsolute().toFile());
	}

	public static File getPluginFile(Plugin plugin, String path) {
		if (plugin != null) {
			try {
				URL url = FileLocator.find(plugin.getBundle(), new Path(path),
				    null);
				return new File(FileLocator.resolve(url).getFile());
			} catch (Exception e) {
				Activator.error(e);
			}
		}
		return null;
	}

	/**
	 * List of all files in the project. <p> Note: A more efficient way to do
	 * this is to use
	 * {@link IResource#accept(org.eclipse.core.resources.IResourceProxyVisitor, int)}
	 * 
	 * @param 1.0.0
	 * @return list of files in the project
	 * @see IResource#accept(org.eclipse.core.resources.IResourceProxyVisitor,
	 *      int)
	 * @since 1.0.0
	 */
	public static List<IFile> getAllProjectFiles(IProject project) {
		List<IFile> result = new ArrayList<IFile>();
		if (project == null)
			return result;
		try {
			collectFiles(project.members(), result);
		} catch (CoreException e) {
		}
		return result;
	}

	private static void collectFiles(IResource[] members, List<IFile> result)
	    throws CoreException {
		// recursively collect files for the given members
		for (int i = 0; i < members.length; i++) {
			IResource res = members[i];
			if (res instanceof IFolder) {
				collectFiles(((IFolder) res).members(), result);
			} else if (res instanceof IFile) {
				result.add((IFile) res);
			}
		}
	}

	/**
	 * Inverse lookup in respect to getLocation() of IFile In this case we are
	 * based on the absolute file system location of the file finding the
	 * Workspace relative path. The procedure also analyze linked files
	 * 
	 * @return relative path in current workspace or relative path to resource
	 *         that is linked and pointing to absolutePath parameter
	 */
	public static IFile getProjectResourceByAbsolutePath(
	    IProject parentProject, IPath absolutePath) {
		List<IFile> files = getAllProjectFiles(parentProject);
		for (IFile file : files) {
			if (absolutePath.equals(file.getLocation().makeAbsolute())) {
				return file;
			}
		}
		return null;
	}

	/**
	 * the <code>IFolder.createFolder</code> does not create the whole
	 * <code>IPath</code> just the last one, this method ensures, that all
	 * segments are created, if they dont exist
	 * 
	 * @param project the project, where the folder has to be created
	 * @param path the full <code>IPath</code> of the needed folder, relative to
	 *            the project for separation of the segments use
	 *            <code>IPath.SEPARATOR</code>
	 * @param monitor monitor can be null
	 * @return returns the <code>IFolder</code> of the last path segment, if the
	 *         count of the segments is 0, returns null
	 * @throws CoreException
	 */
	public static IFolder getProjectFolder(IProject project, IPath path,
	    IProgressMonitor monitor) throws CoreException {
		if (!project.isOpen()) {
			project.open(monitor);
		}
		String[] segments = path.segments();
		if (segments.length == 0) {
			return null;
		}
		IContainer folder = project;
		for (int i = 0; i < segments.length; i++) {
			path = new Path(segments[i]);

			folder = folder.getFolder(path);
			if (!folder.exists()) {
				((IFolder) folder).create(true, false, monitor);
			}
		}

		return (IFolder) folder;
	}

	public static IProject getCurrentProject() {
		IEditorInput editorInput = PlatformUI.getWorkbench()
		    .getActiveWorkbenchWindow().getActivePage().getActiveEditor()
		    .getEditorInput();
		if (editorInput instanceof IPathEditorInput) {
			IPath location = ((IPathEditorInput) editorInput).getPath();
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IFile[] files = workspace.getRoot().findFilesForLocation(location);
			int x = 0;
			while (files[x] == null) {
				x++;
				continue;
			}
			return files[x].getProject();
		} else {
			// contingency code
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
			    .getProjects();
			return projects[0];
		}
	}

	/**
	 * Method which finds out if there is at least one open project in
	 * workspace.
	 * 
	 * @return true if there is at least one open project in workspace. False
	 *         otherwise.
	 */
	public static boolean isAnyProjectOpen() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
		    .getProjects();
		for (IProject project : projects) {
			if (project.isOpen()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method which counts open projects in workspace.
	 * 
	 * @return open projects in workspace.
	 */
	public static int countOpenProject() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
		    .getProjects();
		int count = 0;
		for (IProject project : projects) {
			if (project.isOpen()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Method try to find first open project in workspace
	 * 
	 * @return first open project or null if not found
	 */
	public static IProject getFirstOpenProject() {
		IProject[] i = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		if (i.length == 0) {
			return null;
		}
		ArrayList<String> projectNames = new ArrayList<String>();
		for (IProject project : i) {
			if (project.isOpen()) {
				projectNames.add(project.getName().toLowerCase());
			}

		}
		Collections.sort(projectNames);
		if (projectNames.size() == 0) {
			return null;
		}
		String firstProject = projectNames.get(0);
		return ResourceUtils.getProjectByName(firstProject);
	}
}
