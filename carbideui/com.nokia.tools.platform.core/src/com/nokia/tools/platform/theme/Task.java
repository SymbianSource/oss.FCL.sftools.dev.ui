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

import com.nokia.tools.platform.core.PlatformCorePlugin;

/**
 * The class hold information about a Task
 */

public class Task extends ThemeBasicData {

	/**
	 * Creates a Task with name
	 * 
	 * @param taskName name of the Task
	 */
	public Task(String taskName) {
		setThemeName(taskName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#isChildValid(java.lang.Object)
	 */
	@Override
	public boolean isChildValid(Object obj) {
		return obj instanceof ComponentGroup;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#addChild(int,
	 *      java.lang.Object)
	 */
	public void addChild(int position, Object objComponentGroup)
			throws ThemeException {

		try {
			ComponentGroup componentGroup = (ComponentGroup) objComponentGroup;
			String name = componentGroup.getThemeName();

			boolean duplicate = false;

			ComponentGroup existingComponentGroup = null;

			String existingCompGId = null;
			String compGId = null;

			if (link.children != null) {

				for (int i = 0; i < link.children.size(); i++) {
					existingComponentGroup = (ComponentGroup) link.children
							.get(i);

					if (componentGroup.getAttribute().containsKey(
							ThemeTag.ATTR_CHILD_NAME)) {
						compGId = componentGroup
								.getAttributeValue(ThemeTag.ATTR_CHILD_NAME);
						existingCompGId = existingComponentGroup
								.getAttributeValue(ThemeTag.ATTR_CHILD_NAME);

						if (existingCompGId != null) {
							if (existingCompGId.equalsIgnoreCase(compGId)) {
								return;
							}
						}
					} else {

						if ((existingComponentGroup.getThemeName())
								.equalsIgnoreCase(name)) {
							duplicate = true;
							break;
						}
					}
				}
			}

			if (!duplicate) {

				synchronized (this) {
					componentGroup.setParent(this);

					if (link.children == null)
						link.children = new ArrayList<Object>();

					if (position < 0) {
						link.children.add(componentGroup);
					} else {
						link.children.add(position, componentGroup);
					}
				}
			} else {
				// Debug.out(this,"merge data");
				if ((componentGroup.getAttributeValue("overwrite") != null)
						&& (componentGroup.getAttributeValue("overwrite")
								.equals("true")))
					existingComponentGroup.overWrite(componentGroup);
				else
					existingComponentGroup.merge(componentGroup);

				// existingComponentGroup.merge(componentGroup);
				// mergeData
			}
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
			throw new ThemeException("Adding componentgroup to task failed : "
					+ e.getMessage());
		}

	}

} // end of the Task class

