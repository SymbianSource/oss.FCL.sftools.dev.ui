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
package com.nokia.tools.media.utils.svg;

public class ColorGroupItem {

	//id of the image
	private String itemId;
	
	//layer name for layered image, part name for nine piece image
	private String imagePartOrLayer=null;
	
	//id of the element in svg image
	private String itemPartId;
	
	
	
	public ColorGroupItem(String itemId, String itemPartId, String imagePartOrLayer){
		super();
		this.itemId = itemId;
		this.itemPartId = itemPartId;
		this.imagePartOrLayer=imagePartOrLayer;
	}
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getItemPartId() {
		return itemPartId;
	}
	public void setItemPartId(String itemPartId) {
		this.itemPartId = itemPartId;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ColorGroupItem){
			ColorGroupItem itemToCompare=(ColorGroupItem) obj;
			boolean hasSameId= this.getItemId().equals(itemToCompare.getItemId());
			boolean hasSameLayerOrPart=false;
			if(imagePartOrLayer==null){
				hasSameLayerOrPart=true;
			}else{
				hasSameLayerOrPart=imagePartOrLayer.equals(itemToCompare.getImagePartOrLayer());
			}
			if(this.getItemPartId()!=null){
				return hasSameId&&hasSameLayerOrPart&&this.getItemPartId().equals(itemToCompare.getItemPartId());
			}else{
				return hasSameId&&hasSameLayerOrPart;
			}
		}else{		
			return super.equals(obj);
		}
	}
	public String getImagePartOrLayer() {
		return imagePartOrLayer;
	}
	public void setImagePartOrLayer(String imagePartOrLayer) {
		this.imagePartOrLayer = imagePartOrLayer;
	}


	
}
