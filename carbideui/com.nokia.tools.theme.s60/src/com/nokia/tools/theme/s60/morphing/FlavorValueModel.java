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
 * To change the template for this generated file
 * go to Window - Preferences - Java - Code Style - Code Templates
 */
package com.nokia.tools.theme.s60.morphing;

import java.util.HashMap;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nokia.tools.resource.util.DebugHelper;

public class FlavorValueModel {

	private HashMap<Object, Object> attributeMap;
	private HashMap<Object, Object> uiAttributeMap;
	private HashMap<Object, Object> outputParamMap;
	private String timeModelRecommended = null;

	public FlavorValueModel() {
		super();
	}

	public FlavorValueModel(Node node) {
		attributeMap = new HashMap<Object, Object>();
		uiAttributeMap = new HashMap<Object, Object>();
		outputParamMap = new HashMap<Object, Object>();
		putAttributes(node, attributeMap);
		if (node.hasChildNodes()) {
			NodeList childNodes = node.getChildNodes();
			for (int j = 0; j < childNodes.getLength(); j++) {
				Node child = childNodes.item(j);
				if (child.getNodeName().equalsIgnoreCase(
						AnimationConstants.TAG_PARAM)) {
					HashMap map = putAttributes(child, null);
					outputParamMap.put((String) map
							.get(AnimationConstants.ATTR_NAME), map);
				}
				
			}
		}

		if (DebugHelper.debugAnimation()) {
			DebugHelper.debug(this, getName());
			DebugHelper.debug(this, "AttributeMap " + attributeMap);
			DebugHelper.debug(this, "OutputAttributeMap " + outputParamMap);
		}
	}

	private HashMap putAttributes(Node child, HashMap<Object, Object> map) {

		NamedNodeMap nmap = child.getAttributes();
		if (map == null)
			map = new HashMap<Object, Object>();
		for (int i = 0; i < nmap.getLength(); i++) {
			Node attr = nmap.item(i);
			map.put(attr.getNodeName(), attr.getNodeValue());
		}
		return map;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	

	}

	public HashMap getAttributeMap() {
		return attributeMap;
	}

	public void setAttributeMap(HashMap<Object, Object> attributeMap) {
		this.attributeMap = attributeMap;
	}

	public HashMap<Object, Object> getOutputParamMap() {
		return outputParamMap;
	}

	public void setOutputParamMap(HashMap<Object, Object> outputParamMap) {
		this.outputParamMap = outputParamMap;
	}

	public HashMap<Object, Object> getUiAttributeMap() {
		return uiAttributeMap;
	}

	public void setUiAttributeMap(HashMap<Object, Object> uiAttributeMap) {
		this.uiAttributeMap = uiAttributeMap;
	}

	public String getName() {
		return (String) attributeMap.get(AnimationConstants.ATTR_NAME);
	}

	public HashMap getOutputParams(String name) {
		return (HashMap) outputParamMap.get(name);
	}

	public HashMap getParams(String uiname) {
		return (HashMap) outputParamMap.get(uiname);
	}

	public HashMap getUIParams(String name) {
		return (HashMap) uiAttributeMap.get(name);
	}

	public String getTimeModelRecommended() {
		return timeModelRecommended;
	}

	public void setTimeModelRecommended(String timeModelRecommended) {
		this.timeModelRecommended = timeModelRecommended;
	}
}
