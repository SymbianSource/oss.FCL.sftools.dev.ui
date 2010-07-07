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

public class ComponentGroup extends ThemeBasicData {

	/**
	 * Creates a ComponentGroup with name
	 * 
	 * @param componentGroupName name of the ComponentGroup
	 */
	public ComponentGroup(String componentGroupName) {
		setThemeName(componentGroupName);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#isChildValid(java.lang.Object)
	 */
	@Override
	public boolean isChildValid(Object obj) {
		return obj instanceof Component;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#addChild(int,
	 *      java.lang.Object)
	 */
	public void addChild(int position, Object objComponent)
			throws ThemeException {
		try {
			Component component = (Component) objComponent;

			String name = component.getThemeName();

			boolean duplicate = false;

			Component existingComponent = null;

			String compId = null;
			String existingCompId = null;

			if (link.children != null) {
				for (int i = 0; i < link.children.size(); i++) {
					existingComponent = (Component) link.children.get(i);

					if (component.getAttribute().containsKey(
							ThemeTag.ATTR_CHILD_NAME)) {
						compId = component
								.getAttributeValue(ThemeTag.ATTR_CHILD_NAME);
						existingCompId = existingComponent
								.getAttributeValue(ThemeTag.ATTR_CHILD_NAME);

						if (existingCompId != null) {
							if (existingCompId.equalsIgnoreCase(compId)) {
								return;
							}
						}
					} else {
						if ((existingComponent.getThemeName())
								.equalsIgnoreCase(name)) {
							duplicate = true;
							break;
						}
					}
				}
			}

			if (!duplicate) {
				synchronized (this) {
					component.setParent(this);

					if (link.children == null)
						link.children = new ArrayList<Object>();
					if (position < 0) {
						link.children.add(component);
					} else {
						link.children.add(position, component);
					}
				}
			} else {
				if ((component.getAttributeValue("overwrite") != null)
						&& (component.getAttributeValue("overwrite")
								.equals("true")))
					existingComponent.overWrite(component);
				else
					existingComponent.merge(component);
			}

		} catch (Exception e) {
			// e.printStackTrace();
			throw new ThemeException(
					"Adding component to componentgroup  failed : "
							+ e.getMessage());
		}
	}

} // end of the ComponentGroup class

