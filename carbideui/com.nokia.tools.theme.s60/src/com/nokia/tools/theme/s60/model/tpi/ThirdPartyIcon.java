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
import com.nokia.tools.platform.theme.Element;


public class ThirdPartyIcon extends Element {

	private String id, appUid, majorId, minorId;

	private ThirdPartyIconType thirdPartyIconType;
	
	public ThirdPartyIcon(String uid, String name, String majorId,String minorId, ThirdPartyIconType thirdPartyIconType) {
		super(name);
		this.appUid = uid;
		this.majorId = majorId;
		this.minorId = minorId;
		this.thirdPartyIconType = thirdPartyIconType;
	}

	public ThirdPartyIcon(String uid, String id, String name, String majorId,
	    String minorId,  ThirdPartyIconType thirdPartyIconType) {
		super(name);
		this.id = id;
		this.appUid = uid;
		this.majorId = majorId;
		this.minorId = minorId;
		this.thirdPartyIconType = thirdPartyIconType;
	}

	public String getAppUid() {
		return appUid;
	}

	public void setAppUid(String appUid) {
		this.appUid = appUid;
	}

	public String getId() {
		if (null == id) {
			id = makeIdFromName();
		}
		return id;
	}

	private String makeIdFromName() {
		return getName().replace(' ', '_') + '$' + System.nanoTime();
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ThirdPartyIcon) {
			ThirdPartyIcon other = (ThirdPartyIcon) obj;
			return compareStr(toString(), other.toString());
			
		}
		return false;
	}

	private boolean compareStr(String a, String b) {
		return a == null ? b == null ? true : false : a.equals(b);
	}

	public String getMajorId() {
		return majorId;
	}

	public void setMajorId(String majorId) {
		this.majorId = majorId;
	}

	public String getMinorId() {
		return minorId;
	}

	public void setMinorId(String minorId) {
		this.minorId = minorId;
	}

	public Boolean isApplication() {
		return (null != appUid);
	}

	public String toString() {
	
		StringBuffer returnValue = new StringBuffer();
		returnValue.append("Name: " + getName());
		returnValue.append(" ID: " + id);
		if (appUid != null) {
			returnValue.append(" App UID: " + appUid);
		}
		if (majorId != null && minorId != null) {
			returnValue.append(" Version: " + majorId + minorId);
		}

		return returnValue.toString();
	}

	@Override
	public ThirdPartyIcon clone() {
		ThirdPartyIcon clonedInstance = (ThirdPartyIcon) super.clone();
		clonedInstance.appUid = appUid;
		clonedInstance.id = id;
		clonedInstance.majorId = majorId;
		clonedInstance.minorId = minorId;
		clonedInstance.setName(getName());
		clonedInstance.thirdPartyIconType = thirdPartyIconType;
		return clonedInstance;
	}
	
	public ThirdPartyIconType getThirdPartyIconType(){
		return thirdPartyIconType;
	}
	
	/**
	 * Copies all the properties from the passed element [skinning information]
	 * into this Third Party Icon. This is used during loading the themes tdf file
	 * which would be loading TPI definitions as pure Element. We overwrite it with
	 * the TPI object by copy all other properties defined at the element level into it.
	 * @param element
	 */
	public void copyProperties(Element element){
		if(element != null)
			super.copyProperties(element);
	}
	
	public void updateThirdPartyIconProperties(ThirdPartyIcon thirdPartyIcon){
		if(thirdPartyIcon != null){
			this.appUid = thirdPartyIcon.appUid;
			this.id = thirdPartyIcon.id;
			this.majorId = thirdPartyIcon.majorId;
			this.minorId = thirdPartyIcon.minorId;
			setName(thirdPartyIcon.getName());
		}
	}
	
	long getLongAppUID(){
		if(appUid != null && appUid.trim().length() != 0){
			return Long.decode(appUid);
		}
		return -1;
	}
	
	long getLongMajorID(){
		if(majorId != null && majorId.trim().length() != 0){
			return Long.decode(majorId);
		}
		return -1;
	}

	long getLongMinorID(){
		if(minorId != null && minorId.trim().length() != 0){
			return Long.decode(minorId);
		}
		return -1;
	}

}
