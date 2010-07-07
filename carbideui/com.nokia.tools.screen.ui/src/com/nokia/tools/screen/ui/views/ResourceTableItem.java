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

import com.nokia.tools.content.core.IContentData;

public class ResourceTableItem {
	
	

	ResourceTableCategory parent;

	public ResourceTableItem(ResourceTableCategory cat) {
		this.parent = cat;
	}

	public boolean isVisible() {
		if (parent.isExtracted() && parent.isVisible()) {
			return true;
		} else {
			return false;
		}
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

	String itemName;

	public String getName() {
		return itemName;
	}

	public void setName(String itemName) {
		this.itemName = itemName;
	}

IContentData associatedContentData=null;
	
	public IContentData getAssociatedContentData() {
		return associatedContentData;
	}

	public void setAssociatedContentData(IContentData associatedContentData) {
		this.associatedContentData = associatedContentData;
	}

	public ResourceTableCategory getParent() {
		return parent;
	}

	

}