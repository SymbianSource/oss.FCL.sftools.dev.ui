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
package com.nokia.tools.s60.editor.actions;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.IContentAdapter;

/**
 */
public abstract class CommandStackAction extends WorkbenchPartAction {
	public CommandStackAction(IWorkbenchPart part) {
		super(part);
	}

	protected abstract Command createCommand(IContentAdapter adapter);

	protected Command createCommand() {
		IContentAdapter adapter = (IContentAdapter) getWorkbenchPart()
				.getAdapter(IContentAdapter.class);
		if (adapter != null) {
			return createCommand(adapter);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		execute(createCommand());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
	 */
	@Override
	protected boolean calculateEnabled() {
		Command command = createCommand();
		return (getCommandStack() != null && command != null && command
				.canExecute());
	}
}
