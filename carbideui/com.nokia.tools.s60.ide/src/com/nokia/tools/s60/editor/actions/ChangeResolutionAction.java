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
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.s60.editor.commands.ChangeResolutionCommand;

/**
 * This action is used to change the screen display.
 */
public class ChangeResolutionAction extends CommandStackAction {
	private Display display;

	/**
	 * Constructs an action.
	 * 
	 * @param part the current workbench part.
	 * @param display the new display to apply.
	 */
	public ChangeResolutionAction(IWorkbenchPart part, Display resolution) {
		super(part);
		this.display = resolution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.CommandStackAction#createCommand(com.nokia.tools.content.core.IContentAdapter)
	 */
	@Override
	protected Command createCommand(IContentAdapter adapter) {
		return new ChangeResolutionCommand(adapter, display);
	}
}
