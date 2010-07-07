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
package com.nokia.tools.editing.ui.command;

import java.util.List;

public class RemoveChildCommand extends ApplyFeatureCommand {
	private int oldIndex = -1;

	public RemoveChildCommand() {
		setLabel(Messages.Command_RemoveChild);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.command.SetPropertyCommand#canExecute()
	 */
	@Override
	public boolean canExecute() {
		return super.canExecute() && getFeature().isMany();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.command.SetPropertyCommand#execute()
	 */
	@Override
	public void execute() {
		List children = ((List) getTarget().eGet(getFeature()));
		oldIndex = children.indexOf(getValue());
		children.remove(getValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.command.SetPropertyCommand#undo()
	 */
	@Override
	public void undo() {
		List children = ((List) getTarget().eGet(getFeature()));
		if (oldIndex < 0) {
			children.add(getValue());
		} else {
			children.add(oldIndex, getValue());
		}
	}
}
