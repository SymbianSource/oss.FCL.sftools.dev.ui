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
package com.nokia.tools.platform.theme;

import java.util.ArrayList;

/**
 * The class hold a Component information
 * 
 */

public class Component extends ThemeBasicData {

	/**
	 * Creates a Component with name
	 * 
	 * @param componentName name of the Component
	 */
	public Component(String componentName) {
		setThemeName(componentName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#isChildValid(java.lang.Object)
	 */
	@Override
	public boolean isChildValid(Object obj) {
		return obj instanceof Element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#addChild(int,
	 *      java.lang.Object)
	 */
	public void addChild(int position, Object objElement) throws ThemeException {
		try {
			Element element = (Element) objElement;

			// String name = element.getThemeName();
			// Debug.out(this," name : " + name);

			boolean duplicateId = false;
			boolean duplicateName = false;

			Element existingElement = null;

			String identifier = null;
			String existingIdentifier = null;

			if (link.children != null) {

				// loop thro each child in the children list
				for (int i = 0; i < this.children.size(); i++) {
					existingElement = (Element) link.children.get(i);

					if (element.getAttribute().containsKey(
							ThemeTag.ATTR_CHILD_ID)) {
						identifier = element
								.getAttributeValue(ThemeTag.ATTR_CHILD_ID);
						existingIdentifier = existingElement
								.getAttributeValue(ThemeTag.ATTR_CHILD_ID);

						if (existingIdentifier != null) {
							if (existingIdentifier.equalsIgnoreCase(identifier)) {
								return;
							}
						}
					} else if (element.getAttribute().containsKey(
							ThemeTag.ATTR_CHILD_NAME)) {
						identifier = element
								.getAttributeValue(ThemeTag.ATTR_CHILD_NAME);
						existingIdentifier = existingElement
								.getAttributeValue(ThemeTag.ATTR_CHILD_NAME);

						if (existingIdentifier != null) {
							if (existingIdentifier.equalsIgnoreCase(identifier)) {
								return;
							}
						}
					} else if (element.attributes.containsKey(ThemeTag.ATTR_ID)) {
						// Debug.out(this,"in if block");

						String existingElementId = existingElement
								.getAttributeValue(ThemeTag.ATTR_ID);
						String elementId = element
								.getAttributeValue(ThemeTag.ATTR_ID);

						if (elementId.equalsIgnoreCase(existingElementId)) {
							duplicateId = true;
							break;
						}

					} else {
						// the presence of name for an element is taken care by
						// the dtd

						// Debug.out(this,"in else block");

						String elementName = existingElement.getThemeName();

						// check the name of the existing element and the new
						// element
						if ((element.getThemeName())
								.equalsIgnoreCase(elementName)) {
							duplicateName = true;
							// Debug.out(this,"merge data with the elem name ");
							// merge the data
							break;
						}

					}
				}
			}

			if ((!duplicateId) && (!duplicateName)) {

				synchronized (this) {
					element.setParent(this);

					if (link.children == null)
						link.children = new ArrayList<Object>();

					if (position < 0) {
						link.children.add(element);
					} else {
						link.children.add(position, element);
					}
				}
			} else if (duplicateId) {

				// merge the data

				if ((element.getAttributeValue("overwrite") != null)
						&& (element.getAttributeValue("overwrite")
								.equals("true")))
					existingElement.overWrite(element);
				else
					existingElement.merge(element);

			} else if (duplicateName) {

				// merge the data
				if ((element.getAttributeValue("overwrite") != null)
						&& (element.getAttributeValue("overwrite")
								.equals("true")))
					existingElement.overWrite(element);
				else
					existingElement.merge(element);
				// existingElement.merge(element);

			}
		} catch (Exception e) {
			throw new ThemeException("Adding element to component failed : "
					+ e.getMessage());
			// e.printStackTrace();
		}
	}

} // end of Component class

