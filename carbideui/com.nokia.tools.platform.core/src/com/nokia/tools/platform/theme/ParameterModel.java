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
/*
 * File Name ParameterModel.java Description File contains the class that holds
 * the skin image details 
 * 
 */
package com.nokia.tools.platform.theme;

import java.util.HashMap;
import java.util.Map;

public class ParameterModel implements Cloneable {
	private Map<Object, Object> attrMap;
	private ThemeGraphic tg = null;

	/**
	 * Constructor
	 * 
	 * @param tg
	 */
	public ParameterModel(ThemeGraphic tg) {
		attrMap = new HashMap<Object, Object>();
		this.tg = tg;
	}

	public void setAttributes(Map<Object, Object> mapAttribute) {
		attrMap.putAll(mapAttribute);
	}

	public ThemeGraphic getThemeGraphic() {
		return this.tg;
	}

	/**
	 * Method to get the attribute list for a node from param Map
	 * 
	 * @return Map
	 */
	public Map<Object, Object> getAttributes() {
		return this.attrMap;
	}

	public String getAttribute(String name) {
		if (attrMap.get(name) != null)
			return (String) this.attrMap.get(name);
		else
			return null;
	}

	public String getValue(String parameterName) {
		if (this.attrMap.get(ThemeTag.ATTR_VALUE) != null)
			return this.attrMap.get(ThemeTag.ATTR_VALUE).toString();
		return null;
	}

	public void setValue(String val) {
		this.attrMap.put(ThemeTag.ATTR_VALUE, val);
	}
	
	public boolean isAnimatedModel() {
		return false;
	}

	/**
	 * Method to clone the ParameterModel object
	 * 
	 * @return Object object of the clone ParameterModel
	 * @param obj1
	 */
	public Object clone1(ThemeGraphic obj1) throws CloneNotSupportedException {

		ParameterModel obj = null;
		obj = (ParameterModel) super.clone();
		obj.tg = obj1;
		// clone the attributes Map
		HashMap<Object, Object> m = new HashMap<Object, Object>(this.attrMap);
		m = (HashMap) m.clone();
		obj.attrMap = (Map<Object, Object>) m;

		return (Object) obj;
	}

}
