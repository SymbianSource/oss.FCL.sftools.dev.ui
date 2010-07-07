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

package com.nokia.tools.screen.ui.views;

import java.util.ArrayList;
import java.util.List;



public class ResourceTableInput {
	String type;
	
	String name;
	public String getName(){
		return this.name;		
	}
	public void setName(String name){
		this.name=name;
	}
	
	List<ResourceTableMasterGroup> groups = new ArrayList<ResourceTableMasterGroup>();

	public List<ResourceTableMasterGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<ResourceTableMasterGroup> groups) {
		this.groups = groups;
	}

	public void addGroup(ResourceTableMasterGroup grp) {
		groups.add(grp);
	}
	
	public void removeGroup(ResourceTableMasterGroup grp){
		groups.remove(grp);
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

}
