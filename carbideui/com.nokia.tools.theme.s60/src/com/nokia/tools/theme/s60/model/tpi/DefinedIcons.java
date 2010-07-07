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

package com.nokia.tools.theme.s60.model.tpi;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class DefinedIcons
    extends AbstractList<ThirdPartyIcon> {

	protected List<ThirdPartyIcon> icons = new ArrayList<ThirdPartyIcon>();

	@Override
	public ThirdPartyIcon get(int index) {
		return icons.get(index);
	}

	@Override
	public int size() {
		return icons.size();
	}

	@Override
	public void add(int index, ThirdPartyIcon element) {
		icons.add(index, element);
	}

	/**
	 * The add method is overloaded to ensure that we do not add pure duplicates into
	 * the Third Party Icons list.
	 */
	@Override
	public boolean add(ThirdPartyIcon iconToBeAdded) {
		return icons.add(iconToBeAdded);
	}

	public void addAll(DefinedIcons other) {
		for (ThirdPartyIcon thirdPartyIcon : other.icons) {
			add(thirdPartyIcon);
		}
	}

	@Override
	public boolean remove(Object o) {
		return icons.remove(o);
	}

	@Override
	public ThirdPartyIcon remove(int index) {
		return icons.remove(index);
	}

	public ThirdPartyIcon getUid(String uid) {
		for (ThirdPartyIcon ic : this) {
			if (uid == null)
				continue;
			if (uid.equalsIgnoreCase(ic.getAppUid()))
				return ic;
		}
		return null;
	}

	public ThirdPartyIcon getMinorId(String minorId) {
		for (ThirdPartyIcon ic : this) {
			if (minorId == null)
				continue;
			if (minorId.equalsIgnoreCase(ic.getMinorId()))
				return ic;
		}
		return null;
	}
	
	public Set<String> getMajorMinorId(String majorId) {
		Set<String> majorMinorIds = new HashSet<String>();
		for (ThirdPartyIcon ic : this) {
			if (majorId == null)
				continue;
			if (majorId.equalsIgnoreCase(ic.getMajorId())){
				majorMinorIds.add(ic.getMajorId() + ic.getMinorId());
			}
		}
		return majorMinorIds;
	}

	public ThirdPartyIcon get(String name) {
		for (ThirdPartyIcon ic : this) {
			if (name == null)
				continue;
			if (name.equals(ic.getName()))
				return ic;
		}
		return null;
	}

	public ThirdPartyIcon getIconById(String id) {
		for (ThirdPartyIcon ic : this) {
			if (id == null)
				continue;
			if (ic.getId().contains(id))
				return ic;
		}
		return null;
	}

	public boolean nameExitsInModel(String value) {
		for (ThirdPartyIcon ic : this) {
			if (value.equals(ic.getName()))
				return true;
		}
		return false;
	}

	public boolean uidExitsInModel(String appUid) {
		for (ThirdPartyIcon ic : this) {
			if (appUid.equals(ic.getAppUid()))
				return true;
		}
		return false;
	}

	public boolean idExitsInModel(String id) {
		for (ThirdPartyIcon ic : this) {
			if (id.equals(ic.getId()))
				return true;
		}
		return false;
	}
	
	public ThirdPartyIcon getThirdPartyIconForId(String id){
		for (ThirdPartyIcon ic : this) {
			if (id.equals(ic.getId()))
				return ic;
		}
		return null;
	}

	/**
     * @param toolSpecificThirdPartyIconsModel
     * @return
     */
    public boolean containsAny(DefinedIcons other) {
    	for (ThirdPartyIcon thirdPartyIcon : other.icons) {
			if(uidExitsInModel(thirdPartyIcon.getAppUid())){
				return true;
			}
		}
    	return false;
    }

    public DefinedIcons clone(){
    	DefinedIcons clonedInstance = new DefinedIcons();
    	for(ThirdPartyIcon icon: this.icons){
    		clonedInstance.add(icon.clone());
    	}
    	return clonedInstance;
    }
    
    @Override
    public void clear(){
    	icons.clear();
    }
    
    @Override
    public Iterator<ThirdPartyIcon> iterator(){
    	return icons.iterator();
    }
    
    @Override
    public boolean isEmpty(){
    	return icons.isEmpty();
    }
}
