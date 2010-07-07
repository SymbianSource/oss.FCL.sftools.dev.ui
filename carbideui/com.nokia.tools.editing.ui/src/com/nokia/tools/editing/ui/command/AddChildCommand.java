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

public class AddChildCommand extends ApplyFeatureCommand {
	private int index = -1;

	public AddChildCommand() {
		this(-1);
	}

	public AddChildCommand(int index) {
		this.index = index;
		setLabel(Messages.Command_AddChild);
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
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
		if (index < 0) {
			children.add(getValue());
		} else {
			children.add(index, getValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.command.SetPropertyCommand#undo()
	 */
	@Override
	public void undo() {
		((List) getTarget().eGet(getFeature())).remove(getValue());
	}
}
