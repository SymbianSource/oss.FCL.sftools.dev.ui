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

import org.eclipse.swt.graphics.ImageData;

import com.nokia.tools.content.core.IContentData;


public class ResourceTableCategory implements IResourceTableCategoryItem {

	private final int NORMAL_FIELD = 0;

	public int getType() {
		return NORMAL_FIELD;
	}

	ResourceTableMasterGroup parent;

	public ResourceTableCategory(ResourceTableMasterGroup grp) {
		this.parent = grp;
	}
	IContentData associatedContentData=null;
	
	public IContentData getAssociatedContentData() {
		return associatedContentData;
	}

	public void setAssociatedContentData(IContentData associatedContentData) {
		this.associatedContentData = associatedContentData;
	}
	
	
	public boolean isVisible() {
		if (parent.isExtracted()) {
			return true;
		} else {
			return false;
		}
	}

	boolean isExtracted = false;

	public boolean isExtracted() {
		return isExtracted;
	}

	public void setExtracted(boolean isExtracted) {
		this.isExtracted = isExtracted;
	}

	private int skinnedType = -1;

	public boolean isSkinned() {
		if(skinnedType==ResourceTableMasterGroup.SKINNED){
			return true;
		}else{return false;}
	}
	
	public boolean isHalfSkinned(){
		if(skinnedType==ResourceTableMasterGroup.HALF_SKINNED){
			return true;
		}else{return false;}
	}

	public void setSkinned(boolean isSkinned) {
		if(isSkinned){
			this.skinnedType = ResourceTableMasterGroup.SKINNED;
		}else{
			
				this.skinnedType=ResourceTableMasterGroup.NOT_SKINNED;
		}
	}
	
	public void setHalfSkinned(boolean isHalfSkinned){
		if(isHalfSkinned){
			this.skinnedType=ResourceTableMasterGroup.HALF_SKINNED;
		}else{		
			this.skinnedType=ResourceTableMasterGroup.NOT_SKINNED;
		}

	}

	String categoryName = "";

	List<ResourceTableItem> items = new ArrayList<ResourceTableItem>();

	private ImageData imageData;

	public List<ResourceTableItem> getItems() {
		return items;
	}

	public void setItems(List<ResourceTableItem> items) {
		this.items = items;
	}

	public String getName() {
		return categoryName;
	}

	public void setName(String categoryName) {
		this.categoryName = categoryName;
	}

	public void addItem(ResourceTableItem item) {
		this.items.add(item);
	}

	public ResourceTableMasterGroup getParent() {
		return parent;
	}

	public void setImageData(ImageData imageData){
		this.imageData = imageData;
	}
	/**
	 * TableCategory can have a Image.
	 * @return ImageData if has some. 
	 */
	public ImageData getImageData(){
		return this.imageData;
	}

}