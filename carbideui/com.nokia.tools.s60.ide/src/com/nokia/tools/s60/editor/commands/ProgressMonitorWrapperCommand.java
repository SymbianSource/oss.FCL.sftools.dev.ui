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

package com.nokia.tools.s60.editor.commands;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.widgets.Display;

public class ProgressMonitorWrapperCommand extends
		org.eclipse.gef.commands.Command {

	private Command command;

	private IProgressMonitor monitor;

	private String subtask;

	private int workUnit;

	public ProgressMonitorWrapperCommand(Command command,
			IProgressMonitor monitor, String subtask, int workUnit) {
		this.command = command;
		this.monitor = monitor;
		this.subtask = subtask;
		this.workUnit = workUnit;
	}

	@Override
	public boolean canExecute() {
		return command != null && command.canExecute();
	}

	@Override
	public boolean canUndo() {
		return command != null && command.canUndo();
	}

	@Override
	public void dispose() {
		if (command != null) {
			command.dispose();
		}
	}

	@Override
	public void execute() {
		if (monitor != null && monitor.isCanceled()) {
			return;
		}

		try {
			if (monitor != null) {
				if (subtask != null) {
					monitor.subTask(subtask);
				}
			}
			command.execute();
		} finally {
			if (monitor != null) {
				monitor.worked(workUnit);
				if (Display.getCurrent() != null) {
					while (Display.getCurrent().readAndDispatch())
						;
				}
			}
		}
	}

	@Override
	public String getDebugLabel() {
		return command.getDebugLabel();
	}

	@Override
	public String getLabel() {
		return command.getLabel();
	}

	@Override
	public void redo() {
		command.redo();
	}

	@Override
	public void setDebugLabel(String label) {
		command.setDebugLabel(label);
	}

	@Override
	public void setLabel(String label) {
		command.setLabel(label);
	}

	@Override
	public String toString() {
		return command.toString();
	}

	@Override
	public void undo() {
		command.undo();
	}

	public Command getCommand() {
		return command;
	}
}
