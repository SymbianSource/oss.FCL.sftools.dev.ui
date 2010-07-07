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
import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Comment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.nokia.tools.resource.util.DebugHelper;
import com.nokia.tools.theme.s60.effects.ClassLoader;
import com.nokia.tools.theme.s60.effects.EffectObject;
import com.nokia.tools.theme.s60.morphing.timemodels.BaseTimingModelInterface;
import com.nokia.tools.theme.s60.morphing.valuemodels.BaseValueModelInterface;

public class AnimationFactory {

	private static HashMap<String, FlavorValueModel> ValueObjectMap = new HashMap<String, FlavorValueModel>();
	private static HashMap<String, HashMap> valueModelClassMap = new HashMap<String, HashMap>();
	private static HashMap<String, HashMap> timingModelClassMap = new HashMap<String, HashMap>();
	private static FlavorValueModel vModel = null;

	public AnimationFactory() {
		super();
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	

	}

	public static void release() {
		ValueObjectMap.clear();
		valueModelClassMap.clear();
		timingModelClassMap.clear();
	}

	public static void createValueModelObjects(Node node) {

		if (node.getNodeName().equalsIgnoreCase(AnimationConstants.VALUEMODEL)) {
			vModel = new FlavorValueModel(node);
			ValueObjectMap.put(vModel.getName(), vModel);
		}
		if (node.getNodeName().equalsIgnoreCase(
				AnimationConstants.NODE_TIMINGMODEL)
				&& vModel != null) {
			NamedNodeMap nMap = node.getAttributes();
			Node child = nMap.getNamedItem(AnimationConstants.ATTR_NAME);
			vModel.setTimeModelRecommended(child.getNodeValue());
			vModel = null;
		}
	}

	public static HashMap getValueObjectMap() {
		return ValueObjectMap;
	}

	public static void setValueObjectMap(
			HashMap<String, FlavorValueModel> valueObjectMap) {
		ValueObjectMap = valueObjectMap;
	}

	public static FlavorValueModel getFlavorModel(String name) {
		return (FlavorValueModel) ValueObjectMap.get(name);
	}

	/**
	 * This method returns a Value model instance (instance of the class which
	 * implements the specified value model) for the specified value model name.
	 * 
	 * @param baseValueModelName String indicating the name of the value model
	 *        whose instance has to be returned
	 * @return A class that implements the <code>BaseValueModelInterface</code>
	 *         and which implements the given <code>baseValueModelName</code>
	 */
	public static BaseValueModelInterface getValueModelInstance(
			String baseValueModelName) {
		BaseValueModelInterface result = null;
		if (baseValueModelName == null)
			result = null;
		String className = (String) ((HashMap) valueModelClassMap
				.get(baseValueModelName)).get(AnimationConstants.ATTR_CLASS);
		result = ClassLoader.getFlavourInstance(className);
		setParameters(result, baseValueModelName);

		return result;
	}

	/**
	 * This method returns a timing model instance (instance of the class which
	 * implements the specified timing model) for the specified timing model
	 * name.
	 * 
	 * @param baseTimingModelName String indicating the name of the timing model
	 *        whose instance has to be returned
	 * @return A class that implements the <code>BaseTimingModelInterface</code>
	 *         and which implements the given <code>baseTimingModelName</code>
	 * @param timingModelName
	 */

	public static BaseTimingModelInterface getTimingModelInstance(
			String timingModelName) {
		BaseTimingModelInterface result = null;
		if (timingModelName == null)
			return null;

		try {
			String className = (String) ((HashMap) timingModelClassMap
					.get(timingModelName)).get(AnimationConstants.ATTR_CLASS);

			result = ClassLoader.getTimingModelInstance(className);
			
			return result;
		} catch (Exception e) {
			System.out.println(timingModelClassMap);
			return null;
		}
	}

	

	private static void setParameters(BaseValueModelInterface result,
			String baseValueModelName) {
		FlavorValueModel vObj = AnimationFactory
				.getFlavorModel(baseValueModelName);
		HashMap map = vObj.getOutputParamMap();
		
		Set set = map.keySet();
		Iterator iter = set.iterator();
		HashMap<String, String> param = new HashMap<String, String>();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			HashMap value = (HashMap) map.get(key);
			param.put(key, (String) value.get(AnimationConstants.DEFAULTVALUE));
		}
		
		try {
			result.setParameters(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static FlavorValueModel getModelObject(String uiname,
			EffectObject currObject) {
		Set set = ValueObjectMap.keySet();
		Iterator iter = set.iterator();
		while (iter.hasNext()) {
			String name = (String) iter.next();
			FlavorValueModel model = (FlavorValueModel) ValueObjectMap
					.get(name);
			HashMap map = model.getAttributeMap();
			String uiName = (String) map.get(AnimationConstants.ATTR_UINAME);
			if ((uiName).equalsIgnoreCase(uiname)) {
				FlavorValueModel flv = (FlavorValueModel) ValueObjectMap
						.get(name);
				HashMap valueMap = currObject.getValueModelMap();
				Set valueSet = valueMap.keySet();
				Iterator valueIter = valueSet.iterator();
				while (valueIter.hasNext()) {
					String valueModelName = (String) valueIter.next();
					if (valueModelName.equalsIgnoreCase(name)) {
						return flv;
					}
				}
			}
		}
		return null;
	}

	public static void createModelMap(Node root) {
		NodeList childList = root.getChildNodes();
		for (int i = 0; i < childList.getLength(); i++) {
			Node child = childList.item(i);
			for (int k = 0; k < child.getChildNodes().getLength(); k++) {
				Node child1 = child.getChildNodes().item(k);
				if (child1 instanceof Text || child1 instanceof Comment)
					continue;
				NamedNodeMap nMap = child1.getAttributes();
				HashMap<Object, Object> map = new HashMap<Object, Object>();
				for (int j = 0; j < nMap.getLength(); j++) {
					Node node = nMap.item(j);
					map.put(node.getNodeName(), node.getNodeValue());
				}
				if (child.getNodeName().equalsIgnoreCase(
						AnimationConstants.TAG_VALUEMODELS)) {
					valueModelClassMap.put((String) map
							.get(AnimationConstants.ATTR_NAME), map);
				}
				if (child.getNodeName().equalsIgnoreCase(
						AnimationConstants.TAG_TIMINGMODELS)) {
					timingModelClassMap.put((String) map
							.get(AnimationConstants.ATTR_NAME), map);
				}
			}
		}
		if (DebugHelper.debugAnimation()) {
			DebugHelper.debug(AnimationFactory.class, "Value class map: "
					+ valueModelClassMap);
			DebugHelper.debug(AnimationFactory.class, "Timing clas map: "
					+ timingModelClassMap);
		}
	}

	public static HashMap getTimingModelClassMap() {
		return timingModelClassMap;
	}

	public static HashMap getValueModelClassMap() {
		return valueModelClassMap;
	}

	public static String getTimeModelName(String uiName) {
		Set set = timingModelClassMap.keySet();
		Iterator iter = set.iterator();
		while (iter.hasNext()) {
			String name = (String) iter.next();
			HashMap map = (HashMap) timingModelClassMap.get(name);
			if (((String) map.get(AnimationConstants.ATTR_UINAME))
					.equalsIgnoreCase(uiName))
				return name;
		}
		return null;
	}
}
