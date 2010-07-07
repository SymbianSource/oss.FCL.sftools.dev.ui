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

import org.eclipse.swt.graphics.RGB;

public class ResourceColorBoxesLine implements IResourceTableCategoryItem {

	RGB selectedRGB;
	
	
	List<RGB> rgbList= new ArrayList<RGB>();
	
	public void addRGB(RGB rgb){
		rgbList.add(rgb);
	}
	
	public List<RGB> getRGBList(){
		return rgbList;
	}	
	
	ResourceTableMasterGroup parent;

	private final int SEPARATOR_FIELD = 1;

	public int getType() {
		return SEPARATOR_FIELD;
	}

	public ResourceColorBoxesLine(ResourceTableMasterGroup grp) {
		parent = grp;

	}

	List<RGB> linkedRGBList = new ArrayList<RGB>();
	
	public List<RGB> getLinkedRGBList() {
		return linkedRGBList;
	}

	public void setLinkedRGBList(List<RGB> linkedRGBList) {
		this.linkedRGBList = linkedRGBList;
	}

	public boolean isVisible() {
		if (parent.isExtracted()) {
			return true;
		} else {
			return false;
		}
	}

	private String name = "";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ResourceTableMasterGroup getParent() {
		return parent;
	}

	public RGB getSelectedRGB() {
			return selectedRGB;
	}

	public void setSelectedRGB(RGB selectedRGB) {
		this.selectedRGB = selectedRGB;
	}

}