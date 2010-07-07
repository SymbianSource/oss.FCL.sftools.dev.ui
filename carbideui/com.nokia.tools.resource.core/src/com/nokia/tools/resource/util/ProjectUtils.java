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

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.resource.core.Activator;

/**
 * Class encapsulates general-purpose project utilities.
 */
public class ProjectUtils {

    /**
     * Get the project interface for operations by project name. The caller
     * needs to check if the returned project actually exists.
     * 
     * @param projectName
     *            Project name to look up
     * @return Project interface (never <code>null</code>).
     */

    public static IProject getProjectByName(String projectName) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    }

    /**
     * Try to look up the project for the given path. If the path is relative,
     * then the project is calculated by the workspace root. Otherwise it is
     * calculated from the root of the file system and checked if the path
     * prefix matches the workspace root.
     * 
     * @param path
     *            Path to test
     * @return Project or <code>null</code> if the path is absolute and
     *         outside the workspace root.
     */
    public static IProject findResourceProject(IPath path) {
        if (path.isAbsolute()) {
            // if the path is absolute, we first check if the path start belongs
            // to the location
            IPath location = ResourcesPlugin.getWorkspace().getRoot()
                    .getLocation();
            if (location.segmentCount() > path.segmentCount()) {
                // the workspace location is deeper then the given path
                return null;
            }
            for (int i = 0; i < location.segmentCount(); i++) {
                if (!location.segment(i).equals(path.segment(i))) {
                    // the segment does not match...
                    return null;
                }
            }
            // the location for the workspace root matches the path prefix
            // strip the prefix and continue
            path = path.removeFirstSegments(location.segmentCount());
        }

        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
                .findMember(path);
        return resource.getProject();
    }

    
    /**
     * Method attempts to guess the current project depending on the active
     * editor's input
     * 
     * @return "Active" project.
     *  
     */
    public static IProject getCurrentProject() {
        try {
            IEditorInput editorInput = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage()
                    .getActiveEditor().getEditorInput();

            final IWorkspace workspace = ResourcesPlugin.getWorkspace();

            if (editorInput instanceof IPathEditorInput) {
                IPath location = ((IPathEditorInput) editorInput).getPath();
                IFile[] files = workspace.getRoot().findFilesForLocation(
                        location);
                if (files.length == 0) {
                    return null;
                } else {
                    return files[0].getProject();
                }
            } else if (editorInput instanceof IFileEditorInput) {
                IFile file = ((IFileEditorInput) editorInput).getFile();
                return file.getProject();
            } else if (editorInput instanceof IStorageEditorInput) {
                IPath path = ((IStorageEditorInput) editorInput).getStorage()
                        .getFullPath();
                return workspace.getRoot().getFile(path).getProject();
            } else if (editorInput instanceof IURIEditorInput) {
                URI uri = ((IURIEditorInput) editorInput).getURI();
                IFile file = workspace.getRoot().getFileForLocation(
                        new Path(uri.toString()));
                return file.getProject();
            } else {
                // contingency code
                IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
                        .getProjects();
                return projects.length == 0 ? null : projects[0];
            }
        } catch (Exception ex) {
            Activator.error(ex);
            return null;
        }
    }

    
    /**
     * Private constructor. The class exports only static methods.
     */
    private ProjectUtils() {
        super();
    }

}
