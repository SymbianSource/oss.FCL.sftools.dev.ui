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

package com.nokia.tools.packaging.commandline.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.nokia.tools.packaging.commandline.PackagerMessages;

public class CMDProgressMonitor extends NullProgressMonitor {
	/**
	 * Indicates whether cancel has been requested.
	 */
	private boolean cancelled = false;

	private int work = 0;

	private boolean done = false;

	private String taskName = "";

	public final static  int TOTAL = 100;

	/**
	 * Constructs a new progress monitor.
	 */
	public CMDProgressMonitor() {
		super();
		work = 0;
		done = false;
	}

	public CMDProgressMonitor(int work) {
		super();
		this.work = work;
		done = false;
	}

	/**
	 * This implementation does nothing. Subclasses may override this method to
	 * do interesting processing when a task begins.
	 * 
	 * @see IProgressMonitor#beginTask(String, int)
	 */
	public void beginTask(String name, int totalWork) {
		// do nothing
	}

	/**
	 * This implementation does nothing. Subclasses may override this method to
	 * do interesting processing when a task is done.
	 * 
	 * @see IProgressMonitor#done()
	 */
	public void done() {
		done = true;
	}

	/**
	 * This implementation does nothing. Subclasses may override this method.
	 * 
	 * @see IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(double work) {
		// do nothing
	}

	/**
	 * This implementation returns the value of the internal state variable set
	 * by <code>setCanceled</code>. Subclasses which override this method
	 * should override <code>setCanceled</code> as well.
	 * 
	 * @see IProgressMonitor#isCanceled()
	 * @see IProgressMonitor#setCanceled(boolean)
	 */
	public boolean isCanceled() {
		return cancelled;
	}

	/**
	 * This implementation sets the value of an internal state variable.
	 * Subclasses which override this method should override
	 * <code>isCanceled</code> as well.
	 * 
	 * @see IProgressMonitor#isCanceled()
	 * @see IProgressMonitor#setCanceled(boolean)
	 */
	public void setCanceled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	/**
	 * This implementation does nothing. Subclasses may override this method to
	 * do something with the name of the task.
	 * 
	 * @see IProgressMonitor#setTaskName(String)
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	/**
	 * This implementation does nothing. Subclasses may override this method to
	 * do interesting processing when a subtask begins.
	 * 
	 * @see IProgressMonitor#subTask(String)
	 */
	public void subTask(String name) {
		// do nothing
	}

	/**
	 * This implementation does nothing. Subclasses may override this method to
	 * do interesting processing when some work has been completed.
	 * 
	 * @see IProgressMonitor#worked(int)
	 */
	public void worked(int work) {
		this.work = work;
	}

	public void addWorked(int work) {
		this.work = this.work + work;
	}

	public int getWorked() {
		return work;
	}

	public String getTaskName() {
		return taskName;
	}

	/**
	 * To display progress bar
	 * 
	 * @param status
	 */
	public void progress(int status) {
		String empty = "[                    ]";
		String fill = "====================";

		StringBuffer strProgressBar = new StringBuffer(empty);
		String subfill = fill.substring(0, status / 5);
		if (subfill.length() >= 1)
			strProgressBar.delete(1, subfill.length() + 1);
		strProgressBar.insert(1, subfill);
		System.out.print(PackagerMessages.Packager_Commandline + "STATUS: "
				+ strProgressBar + " " + status + "%");
	}
}