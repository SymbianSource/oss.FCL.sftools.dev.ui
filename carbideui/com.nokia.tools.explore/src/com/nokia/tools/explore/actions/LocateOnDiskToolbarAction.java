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

package com.nokia.tools.explore.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.nokia.tools.explore.Activator;
import com.nokia.tools.explore.Explorer;

/**
 * Our Explore action implements workbench action delegate. The action will be
 * created by the workbench and shown in the UI ( In a separate menu). When the
 * user tries to use the action, this delegate will be created and execution
 * will be delegated to it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */

public class LocateOnDiskToolbarAction implements IWorkbenchWindowActionDelegate {

	List<String> selectionList = new ArrayList<String>();

	/**
	 * The constructor for Explore in Menu Action
	 */
	public LocateOnDiskToolbarAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		Explorer.locateOnDisk(selectionList);
	}

	/**
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	@SuppressWarnings("unchecked")
	public void selectionChanged(IAction action, ISelection selection) {

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;
			clearSelectionList();
			List selections = (sSelection).toList();
			for (Object sel : selections) {
				if (sel instanceof IResource) {
					updateSelectionList((IResource) sel);
					continue;
				}
				if (sel instanceof IAdaptable) {
					IResource resource = (IResource) ((IAdaptable) sel)
							.getAdapter(IResource.class);
					updateSelectionList(resource);
				}
			}
			if (this.selectionList.size() > 0) {
				action.setEnabled(true);
			} else {
				action.setEnabled(false);
			}
			return;
		}
		clearSelectionList();
		action.setEnabled(false);
	}

	private void clearSelectionList() {
		selectionList.clear();
	}

	private void updateSelectionList(IResource resource) {
		if (resource != null) {
			IPath fullPath = resource.getFullPath();
			IPath location = Platform.getLocation();
			fullPath = location.append(fullPath);
			File file = new File(fullPath.toOSString());
			if (file != null) {
				try {
					selectionList.add(file.getCanonicalPath());
				} catch (IOException ioe) {
					Activator.getDefault().getLog().log(
							new Status(IStatus.OK, Activator.PLUGIN_ID, ioe
									.getMessage()));
				}
			}
		}
	}

	/**
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
	}

}