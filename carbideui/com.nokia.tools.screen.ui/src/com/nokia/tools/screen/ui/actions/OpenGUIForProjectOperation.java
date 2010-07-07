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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;

import com.nokia.tools.content.core.AbstractContentSourceManager;
import com.nokia.tools.content.core.ContentException;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentSourceManager;
import com.nokia.tools.content.core.project.S60DesignProjectNature;
import com.nokia.tools.resource.util.ResourceUtils;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.views.IIDEConstants;
import com.nokia.tools.ui.dialog.MessageDialogWithTextContent;

public class OpenGUIForProjectOperation extends WorkspaceModifyOperation {

	private IProject project;
	private String type;
	private IContent content;
	private IWorkbenchPage workbenchPageRef;

	/**
	 * Active Workbench page is provided outside for editor activation context
	 * editor and UI are opened in scope of this page. Page should be set before
	 * running the action in order to display UI
	 * 
	 * @param project
	 * @param workbenchPage
	 */
	public OpenGUIForProjectOperation(IProject project, IContent content,
			IWorkbenchPage workbenchPage) {
		this.project = project;
		this.content = content;
		workbenchPageRef = workbenchPage;
	}

	public OpenGUIForProjectOperation(IProject project, String type,
			IWorkbenchPage workbenchPage) {
		this(project, (IContent) null, workbenchPage);
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
			InvocationTargetException, InterruptedException {

		List<IContent> contents = new ArrayList<IContent>();
		try {
			if (!project.isOpen()) {
				project.open(monitor);
			}

			IContentSourceManager manager = S60DesignProjectNature
					.getUIDesignData(project);
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			createContent(monitor, contents, manager);
			findRootContent(contents);
			findContentPath(contents);
			if (content == null) {
				showErrorMessageDialog(monitor);
				return;
			}
		} catch (final Exception e) {
			handleExceptions(e);
		}
		if (content != null) {
			IPath path = (IPath) content.getAdapter(IPath.class);

			if (path == null) {
				return;
			}

			loadContentsInEditor(monitor, contents, path);
		}
	}

	private void findContentPath(List<IContent> contents) {
		if (content == null && !contents.isEmpty()) {
			for (IContent ctx : contents) {
				content = ctx;
				IPath path = (IPath) content.getAdapter(IPath.class);

				if (path != null) {
					break;
				}
			}
		}
	}

	private void findRootContent(List<IContent> contents) {
		for (IContent c : contents) {
			if (c.getType().equals(type)) {
				content = c;
				break;
			}
		}
	}

	private void createContent(IProgressMonitor monitor,
			List<IContent> contents, IContentSourceManager manager)
			throws IOException, ContentException {
		for (String type : AbstractContentSourceManager.getContentTypes()) {
			contents.addAll(manager.getRootContents(type, monitor));
		}
	}

	private void loadContentsInEditor(IProgressMonitor monitor,
			List<IContent> contents, IPath path) {
		path.makeAbsolute();

		final IFile file = ResourceUtils.getProjectResourceByAbsolutePath(
				project, path);

		monitor.done();
		if (file.exists()) {
			final List<IContent> allContents = contents;
			loadContentInEditor(file, allContents);
		}
	}

	private void loadContentInEditor(final IFile file,
			final List<IContent> allContents) {
		workbenchPageRef.getWorkbenchWindow().getShell().getDisplay().syncExec(
				new Runnable() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see java.lang.Runnable#run()
					 */
					public void run() {
						try {
							project.setSessionProperty(
									IIDEConstants.CONTENTS_NAME, allContents);
							IDE.openEditor(workbenchPageRef, file, true);
						} catch (Throwable e) {
							UiPlugin.error(e);
						} finally {
							// clears the session property
							try {
								project.setSessionProperty(
										IIDEConstants.CONTENTS_NAME, null);
							} catch (Exception e) {
								UiPlugin.error(e);
							}
						}
					}
				});
	}

	private void handleExceptions(final Exception e) {
		UiPlugin.error(e);
		workbenchPageRef.getWorkbenchWindow().getShell().getDisplay().syncExec(
				new Runnable() {
					public void run() {
						MessageDialogWithTextContent
								.openError(
										workbenchPageRef.getWorkbenchWindow()
												.getShell(),
										Messages.OpenProjectAction_Error_LoadingContent_Title,
										MessageFormat
												.format(
														Messages.OpenProjectAction_Error_LoadingContent_Message,
														new Object[] { project
																.getName() }),
										e);
					}
				});
	}

	private void showErrorMessageDialog(IProgressMonitor monitor) {
		workbenchPageRef.getWorkbenchWindow().getShell().getDisplay().syncExec(
				new Runnable() {
					public void run() {
						MessageDialog
								.openError(
										workbenchPageRef.getWorkbenchWindow()
												.getShell(),
										Messages.OpenProjectAction_Error_LoadingContent_Title,
										Messages.OpenProjectAction_Error_ContentMissing_Message);
					}
				});
		monitor.done();
	}
}
