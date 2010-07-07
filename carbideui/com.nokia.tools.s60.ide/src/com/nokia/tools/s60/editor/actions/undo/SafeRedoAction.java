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
package com.nokia.tools.s60.editor.actions.undo;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.ui.IWorkbenchPart;

/**
 */
public class SafeRedoAction extends RedoAction {

	public SafeRedoAction(IWorkbenchPart p) {
		super(p);
	}
	
	@Override
	protected boolean calculateEnabled() {
		if (getCommandStack() == null)
			return false;
		return super.calculateEnabled();
	}
	
	@Override
	protected void refresh() {
		if (getCommandStack() == null)
			return;
		super.refresh();
	}
	
	@Override
	protected void execute(Command command) {
		if (getCommandStack() == null)
			return;
		super.execute(command);
	}

}
