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
package com.nokia.tools.s60.editor;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;

/**
 * This operation saves the current content into a new project.
 *
 */
public class SaveAsOperation extends WorkspaceModifyOperation {
	private IContent[] source;
	private IProject destination;

	/**
	 * Constructs an operation.
	 * 
	 * @param source the source content.
	 * @param destination the destiation project. This project should already
	 *        exist created and is open.
	 */
	public SaveAsOperation(IContent[] source, IProject destination) {
		this.source = source;
		this.destination = destination;
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
			InvocationTargetException, InterruptedException {
		try {
			if(monitor instanceof SubProgressMonitor){
				monitor = ((SubProgressMonitor)monitor).getWrappedProgressMonitor();
			}
			if(monitor != null){
				monitor.beginTask("Please wait: Save As operation in progress...", IProgressMonitor.UNKNOWN);
			}
			
			for (IContent content : source) {
				
				content.saveAs(destination.getLocation().toOSString(), monitor);
			}
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					S60WorkspacePlugin.getDefault().getBundle()
							.getSymbolicName(), IStatus.ERROR,
					EditorMessages.Error_Editor_SaveAs, e));
		}
		destination.refreshLocal(IProject.DEPTH_INFINITE, monitor);
		monitor.done();
	}
}
