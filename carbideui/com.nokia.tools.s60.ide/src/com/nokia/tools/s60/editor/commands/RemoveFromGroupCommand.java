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

import org.eclipse.gef.commands.Command;

import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroupItem;
import com.nokia.tools.media.utils.svg.ColorGroups;

public class RemoveFromGroupCommand extends Command {

	String itemId;

	String layerName;

	ColorGroup group;

	ColorGroups grps;

	public RemoveFromGroupCommand(String itemId, String layerName,
			ColorGroup group, ColorGroups grps) {
		setLabel(Messages.RemoveFromGroup_Label);
		this.itemId = itemId;
		this.layerName = layerName;
		this.grps = grps;
		this.group = group;
	}

	@Override
	public boolean canExecute() {
		if (itemId != null && group != null && grps != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canUndo() {

		return canExecute();
	}

	public void execute() {
		if (null != group
				&& group.containsItemWithIdAndLayerName(itemId, layerName)) {
			group.removeItemFromGroup(itemId, layerName);
		}
	}

	public void redo() {
		execute();
	}

	public void undo() {
		if (null != group
				&& !group.containsItemWithIdAndLayerName(itemId, layerName)) {
			group.addItemToGroup(new ColorGroupItem(itemId, null, layerName));
		}
	}
}
