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
package com.nokia.tools.platform.layout;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is the optimized data structure for holding the large number of layout
 * elements. 1) With linked list and reduced number of fields, the total memory
 * saving is about 200 MB (proved by JMP profiler). 2) Removed the map and
 * linked list to use primitive arrays to reduce memory usage by another 80 MB.
 * Total time used for parsing 41 layout files is 4.7 seconds.
 * 
 */
public class CompactElement {
	private static final int CHILD_CAPACITY = 8;
	private static final int ATTRIBUTE_CAPACITY = 8;
	private String name;
	private String[] attributes;
	private CompactElement[] children;
	int childIndex;
	int attributeIndex;

	/**
	 * Construxts an instance of the compact element with the given tag name.
	 * 
	 * @param name the xml tag name.
	 */
	public CompactElement(String name) {
		this.name = name;
	}

	/**
	 * Adds the child element.
	 * 
	 * @param element the child element to be added.
	 */
	public void add(CompactElement element) {
		if (children == null) {
			children = new CompactElement[CHILD_CAPACITY];
		}
		if (childIndex >= children.length) {
			CompactElement[] newChildren = new CompactElement[children.length
					+ CHILD_CAPACITY];
			System.arraycopy(children, 0, newChildren, 0, children.length);
			children = newChildren;
		}
		children[childIndex++] = element;
	}

	public void done() {
		if (children != null) {
			if (children.length != childIndex) {
				CompactElement[] newChildren = new CompactElement[childIndex];
				System.arraycopy(children, 0, newChildren, 0, childIndex);
				children = newChildren;
			}
		}
		if (attributes != null) {
			if (attributes.length != attributeIndex) {
				String[] newAttributes = new String[attributeIndex];
				System.arraycopy(attributes, 0, newAttributes, 0,
						attributeIndex);
				attributes = newAttributes;
			}
		}
	}

	/**
	 * Sets the attribute.
	 * 
	 * @param name name of attribute.
	 * @param value value of attribute.
	 */
	public void setAttribute(String name, String value) {
		if (attributes == null) {
			attributes = new String[ATTRIBUTE_CAPACITY];
		}
		if (attributeIndex >= attributes.length) {
			String[] newAttributes = new String[attributes.length
					+ ATTRIBUTE_CAPACITY];
			System
					.arraycopy(attributes, 0, newAttributes, 0,
							attributes.length);
			attributes = newAttributes;
		}
		attributes[attributeIndex++] = name;
		attributes[attributeIndex++] = value;
	}

	/**
	 * Returns the attribute value.
	 * 
	 * @param name name of attribute.
	 * @return the value of attribute.
	 */
	public String getAttribute(String name) {
		// mimic override
		for (int i = attributes.length - 1; i >= 0; i -= 2) {
			if (attributes[i - 1].equals(name)) {
				return attributes[i];
			}
		}
		return null;
	}

	/**
	 * @return all attributes.
	 */
	public Map<String, String> getAttributes() {
		Map<String, String> map = new HashMap<String, String>(
				attributeIndex / 2);
		for (int i = 0; i < attributeIndex; i += 2) {
			map.put(attributes[i], attributes[i + 1]);
		}
		return map;
	}

	/**
	 * Finds all elements with the given name.
	 * 
	 * @param name name of element to retrieve.
	 * @return all elements with the given name.
	 */
	public List<CompactElement> getElementsByTagName(String name) {
		List<CompactElement> elements = new LinkedList<CompactElement>();
		getElementsByName(elements, this, name);
		return elements;
	}

	/**
	 * Recurses down the tree to find all elements with the specified name.
	 * 
	 * @param list the list to where the element will be added.
	 * @param element the starting element.
	 * @param name the tag name to match.
	 */
	private void getElementsByName(List<CompactElement> list,
			CompactElement element, String name) {
		if (name.equals(element.name)) {
			list.add(element);
		}
		if (element.children != null) {
			for (CompactElement child : element.children) {
				getElementsByName(list, child, name);
			}
		}
	}
}
