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

package com.nokia.tools.packaging.commandline.util;

/**
 * @author surmathe
 * This class provides the datastructure for holding the argument data
 */
public class Parameter {
	private String strSwitch = "";
	private String strValue = "";
	
	public Parameter(String parSwitch,String parValue){
		setStrSwitch(parSwitch);
		setStrValue(parValue);
	}
	
	/**
	 * @return switch
	 */
	public String getStrSwitch() {
		return strSwitch;
	}
	/**
	 * @param strSwitch
	 */
	public void setStrSwitch(String strSwitch) {
		this.strSwitch = strSwitch;
	}
	/**
	 * @return value
	 */
	public String getStrValue() {
		return strValue;
	}
	/**
	 * @param strValue
	 */
	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 * This is to meaningfully execute contains() fnction in array list
	 */
	public boolean equals(Object parameter){				
		return ((Parameter)parameter).getStrSwitch().equalsIgnoreCase(strSwitch);
	}
}
