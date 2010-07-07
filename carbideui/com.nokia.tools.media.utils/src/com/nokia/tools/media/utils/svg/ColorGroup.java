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

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.eclipse.swt.graphics.RGB;

/**
 * Class representing color group. Each group has color and name and contains items belonging to group.
 * 
 */

public class ColorGroup extends Observable{
	private RGB groupColor;
	private String name;
	
	private List<String> childrenGroupNames=new ArrayList<String>();
	private String parentGroupName=null;
	
	public void addChildrenGroup(String groupName){
		childrenGroupNames.add(groupName);		
	}
	
	public void setParentGroupName(String name){
		parentGroupName=name;
	}
	
	public String getParentGroupName(){
		if( null == parentGroupName) {
			if (name.length() >= 6) {
				if  ("tone".equals(name.substring(name.length() - 5, name
			
									.length() - 1))){
					return name.substring(0, name.length() - 6);
				}
			}
		}
			
		return parentGroupName;
	}
	
	public boolean hasChildrenGroup(){
		return (childrenGroupNames.size()>0)?true:false;
	}
	
	public List<String> getChildrenGroups(){
		return childrenGroupNames;
	}
	
	public boolean hasParent(){
		return ((parentGroupName!=null)&&(parentGroupName.length()>0))?true:false;
	}
	
	public void removeChildrenGroup(String grpName){
		for(String testedChild: childrenGroupNames){
			if(testedChild.equals(grpName)){
				childrenGroupNames.remove(testedChild);
				return;
			}
		}
	}
	
	
	private List<ColorGroupItem> groupItems= new ArrayList<ColorGroupItem>();

	public ColorGroup(RGB groupColor, String name, ColorGroups root){		
		this.groupColor=groupColor;
		this.name=name;
		//add observer for notifications when items are added or color of the group is changed
		this.addObserver(root);
	}
	
	private boolean containsItemWithId(String id){
		for(ColorGroupItem item:groupItems){
			if(item.getItemId().equals(id)){
				return true;
			}
		}
		return false;
	}
	

	public RGB getGroupColor() {
		return groupColor;
	}

	public void setGroupColor(RGB groupColor) {
		this.groupColor = groupColor;
		setChanged();
		notifyObservers(ColorGroups.ITEM_ADDED);
	}

	public List<ColorGroupItem> getGroupItems() {
		return groupItems;
	}

	public void setGroupItems(List<ColorGroupItem> groupItems) {
		this.groupItems = groupItems;
	}
	
	public void addItemToGroup(ColorGroupItem item){
		this.groupItems.add(item);		
		if(this.hasChanged()){
			//if changed flag is set to observable, let the one who set the flag
			//notify observers (in this case it is addAllItemsToGroup) to
			//prevent useless notifications
		}else{
			setChanged();
			notifyObservers(ColorGroups.ITEM_ADDED);
		}
	}
	
	public void addAllItemsToGroup(List<ColorGroupItem> items){
		setChanged();
		if(items!=null){
			for(ColorGroupItem item:items){
				this.addItemToGroup(item);
			}
		}
		
		notifyObservers(ColorGroups.ITEM_ADDED);
	}
	
	private void removeItemFromGroup(ColorGroupItem item){
		this.groupItems.remove(item);
		setChanged();
		notifyObservers(ColorGroups.ITEM_REMOVED);
	}
	
	public void removeItemFromGroup(String itemId,String layerName) {
		for (ColorGroupItem item : groupItems) {
			if (item.getItemId().equals(itemId)){
				if(layerName==null|| layerName.length()==0){
					removeItemFromGroup(item);
					return;
				}else if(layerName.equals(item.getImagePartOrLayer())){
					removeItemFromGroup(item);
					return;
				}
			}
				
			
		}
		setChanged();
		notifyObservers();
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		setChanged();
		notifyObservers(ColorGroups.NAME_CHANGED);
	}
	
	/**
	 * Checks whether all the items in the group still contain the group's color.
	 * If there exist a item without group color, the item is deleted from group	  
	 */
	private void updateGroup(){
		
	}
	
	public boolean isEmpty(){
		return groupItems.isEmpty();
	}

	/**
	 * Returns true if item is in the group else return false.
	 * 
	 * @param id  id of the element
	 * @param name name of the layer or part of the element
	 * @return
	 */
	
	public boolean containsItemWithIdAndLayerName(String id, String name) {	
			if (name==null){
				return containsItemWithId(id);
			}else{				
				for(ColorGroupItem item:groupItems){
					if(item.getItemId().equals(id)&&name.equals(item.getImagePartOrLayer())){
						return true;
					}
				}
				return false;
			}
		
		
	}
	
	/**
	 * Changes name of the layer in color group for given item if the item with layer is in the group.
	 * @param id id of the item
	 * @param oldName old name of the layer
	 * @param newName new name of the layer
	 */
	
	public void changeLayerOrPartName(String id, String oldName, String newName){
		if((oldName==null)||(id==null)||(newName==null)){
			//do nothing
		}else{
			for(ColorGroupItem item:groupItems){
				if(item.getItemId().equals(id)&&item.getImagePartOrLayer().equals(oldName)){
					item.setImagePartOrLayer(newName);
				}
			}
		}
	}
	
	
	
}
