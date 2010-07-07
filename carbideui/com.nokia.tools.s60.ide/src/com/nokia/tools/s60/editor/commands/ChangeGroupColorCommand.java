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

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.RGB;

import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroupItem;
import com.nokia.tools.media.utils.svg.ColorGroups;

public class ChangeGroupColorCommand extends Command {
			
	private boolean mergingGroups=false;
	
	
	String itemId;
	String layerName;
	RGB newColor;
	ColorGroup group;
	RGB oldColor;
	
	
	ColorGroups grps;
	
	public ChangeGroupColorCommand(ColorGroup group, RGB newColor, ColorGroups grps) {
		setLabel(Messages.ChangeGroupColor_Label);
		
		this.newColor=newColor;
		this.grps= grps;
		this.group=group;
		this.oldColor=group.getGroupColor();
		
	}

	@Override
	public boolean canExecute() {
		if( newColor!= null&&grps!=null&&group!=null&&(grps.getGroupByName(group.getName())!=null)){
			return true;
		}else{
			return false;
		}		
	}

	@Override
	public boolean canUndo() {
	
		return true;
	}


	public void execute() {		
		
		ColorGroup grp=grps.getGroupByRGB(newColor);
		
/*		if(grp!=null&&grp!=group){ // the each color has just one group		
			List<ColorGroupItem> items= group.getGroupItems();
			grp.addAllItemsToGroup(items);
			grps.removeGroup(group);
			mergingGroups=true;
		}else{*/
			group.setGroupColor(newColor);			
		//}			
		
		}


	public void redo() {
		
		execute();
	}

	
	public void undo() {
		if(mergingGroups){
			grps.addGroup(group);
			ColorGroup grp=grps.getGroupByRGB(newColor);
			for(ColorGroupItem item:group.getGroupItems()){
				grp.removeItemFromGroup(item.getItemId(),item.getImagePartOrLayer());
			}
		}else{
			group.setGroupColor(oldColor);
		}
	}



	
}
