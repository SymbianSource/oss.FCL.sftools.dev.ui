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
package com.nokia.tools.screen.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.osgi.framework.Bundle;

import com.nokia.tools.screen.ui.UiPlugin;

/**
 * Loads action class defined by contributions for showing in
 * editor's show in -> submenu
 */
public class ShowInContributionResolver {

	public static final String EXTENSION_POINT_VALIDATORS = UiPlugin
			.getDefault().getBundle().getSymbolicName()
			+ ".showInSubmenu"; //$NON-NLS-1$

	public static final String ELEMENT = "contributionItem"; //$NON-NLS-1$

	public static final String CLASS_ATTRIBUTE = "workbenchPartAction"; //$NON-NLS-1$

	public static final String POS_ATTRIBUTE = "pos"; //$NON-NLS-1$

	public static final ShowInContributionResolver INSTANCE = new ShowInContributionResolver();

	private List<Class<WorkbenchPartAction>> contributedActions = new ArrayList<Class<WorkbenchPartAction>>();

	private Map<String, String> idmapping = new HashMap<String, String>();

	public ShowInContributionResolver() {
		initialize();
	}

	private void initialize() {
		Map<Class, Integer> posmap = new HashMap<Class, Integer>();
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getExtensionPoint(EXTENSION_POINT_VALIDATORS)
				.getConfigurationElements();
		for (IConfigurationElement element : elements) {
			if (ELEMENT.equalsIgnoreCase(element.getName())) {
				String _clazz = element.getAttribute(CLASS_ATTRIBUTE);
				Bundle declaringPlugin = Platform.getBundle(element
						.getNamespaceIdentifier());
				try {
					Class clazz = declaringPlugin.loadClass(_clazz);
					if (!contributedActions.contains(clazz))
						contributedActions.add(clazz);
					else
						continue;

					if (element.getAttribute(POS_ATTRIBUTE) != null) {
						try {
							int position = Integer.parseInt(element
									.getAttribute(POS_ATTRIBUTE));
							posmap.put(clazz, position);
						} catch (Exception e) {
							posmap.put(clazz, Integer.MAX_VALUE);
						}
					} else
						posmap.put(clazz, Integer.MAX_VALUE);
				} catch (ClassNotFoundException e) {
					UiPlugin.error("Error loading Show In ... action", e);
				}
			}
		}

		//sort contributed actions according to info in posmap
		List<Class<WorkbenchPartAction>> result = new ArrayList<Class<WorkbenchPartAction>>();
		for (Class clz : contributedActions) {
			int pos = posmap.get(clz);

			for (int i = 0; i < result.size(); i++) {
				int posNext = posmap.get(result.get(i));
				boolean start = i == 0;
				//boolean end = i == result.size() - 1;
				if (!start) {
					int posLast = posmap.get(result.get(i - 1));
					if (pos >= posLast && pos <= posNext) {
						result.add(i, clz);
						break;
					}
				} else {
					//we are on first position
					if (pos <= posNext) {
						result.add(i, clz);
						break;
					}
				}

			}
			if (!result.contains(clz))
				result.add(clz);
		}
		contributedActions.clear();
		contributedActions.addAll(result);
	}

	public List<Class<WorkbenchPartAction>> getContributedActions() {
		return contributedActions;
	}

	String[] declaredActionsIds = null;

	public void addIdMapping(Class<WorkbenchPartAction> cl, String id) {
		if (!idmapping.containsKey(cl.getName())) {
			idmapping.put(cl.getName(), id);
			declaredActionsIds = new String[contributedActions.size()];
			for (int i = 0; i < contributedActions.size(); i++) {
				declaredActionsIds[i] = idmapping.get(contributedActions.get(i)
						.getName());
			}
		}
	}

	public String[] getDeclaredActionIds() {
		return declaredActionsIds;
	}

}
