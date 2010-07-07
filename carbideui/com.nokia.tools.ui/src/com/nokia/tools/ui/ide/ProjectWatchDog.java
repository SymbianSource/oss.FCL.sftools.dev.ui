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
package com.nokia.tools.ui.ide;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.EditorPart;

/**
 * This is a customized part listener which which notifies about the project
 * change
 */
public class ProjectWatchDog implements IPartListener {

	IProject project;
	
	public ProjectWatchDog() {
	
		project = null;
		
	}
	
	/**
	 * 
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		
		if ( part instanceof EditorPart ) {

			IEditorInput editorInput = ((EditorPart)part).getEditorInput();
			if ( editorInput instanceof IFileEditorInput ) {

				IFile file = ((IFileEditorInput)editorInput).getFile();
				if ( file.getProject()!=project ) {

					project = file.getProject();

				}
			}
		}
	}

	public void partBroughtToTop(IWorkbenchPart part) {

	}

	public void partClosed(IWorkbenchPart part) {

	}

	public void partDeactivated(IWorkbenchPart part) {

	}

	public void partOpened(IWorkbenchPart part) {

	}

	public IProject getActiveProject() {
		
		return project;
		
	}
}
