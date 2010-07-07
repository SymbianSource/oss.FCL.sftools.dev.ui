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
import org.eclipse.swt.graphics.RGB;

import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroupItem;
import com.nokia.tools.media.utils.svg.ColorGroups;

public class AddToGroupCommand extends Command {

	private static final int ADD_WITH_CREATING_NEW_GROUP = 0;

	private static final int ADD_WITHOUT_CREATING_NEW_GROUP = 1;

	private int addToGroupActionType = -1;

	private boolean itemPresentBeforeAdding = false;

	String itemId;

	String layerName;

	RGB newColor;

	ColorGroup group;

	ColorGroups grps;

	ColorGroup previousGroup;

	public ColorGroup getCreatedGroup() {
		if (AddToGroupCommand.ADD_WITH_CREATING_NEW_GROUP == addToGroupActionType) {
			return this.group;
		} else
			return null;
	}

	public AddToGroupCommand(String itemId, String layerName, ColorGroup group,
			RGB newColor, ColorGroups grps, boolean removeIfExists) {
		setLabel(Messages.AddToGroup_Label);
		this.itemId = itemId;
		this.layerName = layerName;
		this.newColor = newColor;
		this.grps = grps;
		this.group = group;
		if (this.group == null) {
			addToGroupActionType = AddToGroupCommand.ADD_WITH_CREATING_NEW_GROUP;
			this.group = this.grps.getNewGroup(this.newColor);
			this.grps.addGroup(this.group);

		} else {
			addToGroupActionType = AddToGroupCommand.ADD_WITHOUT_CREATING_NEW_GROUP;
		}
		if (removeIfExists) {
			previousGroup = grps.getGroupByItemId(itemId, layerName);
		}
	}

	@Override
	public boolean canExecute() {
		if (itemId != null && newColor != null && grps != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	public void execute() {
		if (!group.containsItemWithIdAndLayerName(itemId, layerName)) {
			if (previousGroup != null) {
				previousGroup.removeItemFromGroup(itemId, layerName);
			}
			group.addItemToGroup(new ColorGroupItem(itemId, null, layerName));
		} else {
			this.itemPresentBeforeAdding = true;
		}
	}

	public void redo() {
		if (grps.getGroupByName(group.getName()) == null) {
			grps.addGroup(group);
		}
		execute();
	}

	public void undo() {
		if (this.itemPresentBeforeAdding) {
			return;
		}
		if (addToGroupActionType == AddToGroupCommand.ADD_WITH_CREATING_NEW_GROUP) {
			grps.removeGroup(group);
			if (group.containsItemWithIdAndLayerName(itemId, layerName)
					&& !itemPresentBeforeAdding) {
				group.removeItemFromGroup(itemId, layerName);
			}
		} else if (addToGroupActionType == AddToGroupCommand.ADD_WITHOUT_CREATING_NEW_GROUP) {
			if (group.containsItemWithIdAndLayerName(itemId, layerName)
					&& !itemPresentBeforeAdding) {
				group.removeItemFromGroup(itemId, layerName);
			}
		}
		if (previousGroup != null) {
			previousGroup.addItemToGroup(new ColorGroupItem(itemId, null,
					layerName));
		}
	}
}
