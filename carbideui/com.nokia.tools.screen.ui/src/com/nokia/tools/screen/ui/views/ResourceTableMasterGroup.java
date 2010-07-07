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

import com.nokia.tools.content.core.IContentData;


public class ResourceTableMasterGroup {
	public static final int SKINNED=0;
	public static final int HALF_SKINNED=1;
	public static final int NOT_SKINNED=-1;
	
	boolean emphasizeCategories=false;
	
	IContentData associatedContentData=null;
		
		public IContentData getAssociatedContentData() {
			return associatedContentData;
		}

		public void setAssociatedContentData(IContentData associatedContentData) {
			this.associatedContentData = associatedContentData;
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
			if(skinnedType==SKINNED){
				return true;
			}else{return false;}
		}
		
		public boolean isHalfSkinned(){
			if(skinnedType==HALF_SKINNED){
				return true;
			}else{return false;}
		}

		public void setSkinned(boolean isSkinned) {
			if(isSkinned){
				this.skinnedType = SKINNED;
			}else{
				this.skinnedType=NOT_SKINNED;
			}
		}
		
		public void setHalfSkinned(boolean isHalfSkinned){
			if(isHalfSkinned){
				this.skinnedType=HALF_SKINNED;
			}else{
				this.skinnedType=NOT_SKINNED;
			}
		}

		List<IResourceTableCategoryItem> categories = new ArrayList<IResourceTableCategoryItem>();

		String masterGroupName = "";

		public String getMasterGroupName() {
			return masterGroupName;
		}

		public void setMasterGroupName(String masterGroupName) {
			this.masterGroupName = masterGroupName;
		}

		public List<IResourceTableCategoryItem> getCategories() {
			return categories;
		}

		public void setCategories(List<IResourceTableCategoryItem> categories) {
			this.categories = categories;
		}

		public void addResourceTableCagetory(IResourceTableCategoryItem cat) {
			this.categories.add(cat);
		}

		public boolean isEmphasizeCategories() {
			return emphasizeCategories;
		}

		public void setEmphasizeCategories(boolean emphasizeCategories) {
			this.emphasizeCategories = emphasizeCategories;
		}

	}