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
 * File Name DomHelperFunction.java Description File contains static classes
 * useful for DOM objects. 
 */

package com.nokia.tools.theme.s60.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nokia.tools.platform.theme.ThemeTag;


public class DomHelperFunctions {

	/**
	 * Parses the document for a particular element containing the specified
	 * attribute
	 * 
	 * @param document The document to be parsed
	 * @param elementName The tag name of the element
	 * @param attrName The name of the attribute
	 * @param attrValue The value of the attribute
	 * @return A list containing all the choosen elements.
	 */
	public static List getElements(Element document, String elementName,
			String attrName, String attrValue) {

		List<Object> choosenElements = new ArrayList<Object>();

		NodeList elementList = document.getElementsByTagName(elementName);

		for (int i = 0; i < elementList.getLength(); i++) {
			Map attr = getAttributes(elementList.item(i));

			String value = (String) attr.get(attrName);

			if (value != null && value.equalsIgnoreCase(attrValue)) {
				choosenElements.add(elementList.item(i));
			}
		}

		return choosenElements;
	}

	public static HashMap<Object, Object> getLineColourElements(Element document) {

		HashMap<Object, Object> choosenElements = new HashMap<Object, Object>();

		NodeList elementList = document
				.getElementsByTagName(ThemeTag.SKN_TAG_DRAWLINES);

		for (int i = 0; i < elementList.getLength(); i++) {
			Map attr = getAttributes(elementList.item(i));

			String value = (String) attr.get(ThemeTag.ATTR_ID);

			if (value != null) {
				choosenElements.put(value, attr.get(ThemeTag.ATTR_VALUE));
			} else {
				value = (String) attr.get(ThemeTag.ATTR_NAME);
				choosenElements.put(value, attr.get(ThemeTag.ATTR_VALUE));
			}
		}

		return choosenElements;
	}

	/**
	 * Fetches the attributes associated with the given node
	 * 
	 * @param element The element whose attributes need to be fetched
	 * @return A map containing all the attributes
	 */
	public static Map<Object, Object> getAttributes(Node element) {

		Map<Object, Object> attr = new HashMap<Object, Object>();
		attr.clear();

		NamedNodeMap attributes = element.getAttributes();

		if (attributes != null) { // has attributes
			for (int i = 0; i < attributes.getLength(); i++) {
				Node n = attributes.item(i);
				attr.put(n.getNodeName(), n.getNodeValue());
			}
		}

		return attr;
	}
}