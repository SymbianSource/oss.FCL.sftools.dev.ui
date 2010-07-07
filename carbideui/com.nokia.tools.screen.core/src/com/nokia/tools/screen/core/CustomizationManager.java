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
package com.nokia.tools.screen.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import com.nokia.tools.editing.ui.adapter.IComponentAdapter;

public class CustomizationManager {
	/**
	 * Extension point id.
	 */
	private static final String CUSTOMIZER_ID = "com.nokia.tools.screen.core.customizers";
	private static final String EXT_CUSTOMIZER = "customizer";
	private static final String ATTR_CLASS = "class";

	public static IScreenCustomizer[] getCustomizers() {
		List<IScreenCustomizer> customizers = new ArrayList<IScreenCustomizer>();
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(CUSTOMIZER_ID);
		IExtension[] extensions = point.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
					.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (EXT_CUSTOMIZER.equals(element.getName())) {
					try {
						IScreenCustomizer customizer = (IScreenCustomizer) element
								.createExecutableExtension(ATTR_CLASS);
						customizers.add(customizer);
					} catch (CoreException e) {
						CorePlugin.error(e);
					}
				}
			}
		}
		return customizers.toArray(new IScreenCustomizer[customizers.size()]);
	}

	public static IComponentAdapter getComponentAdapter(IScreenElement element) {
		for (IScreenCustomizer cust : getCustomizers()) {
			IComponentAdapter adapter = cust.getComponentAdapter(element);
			if (adapter != null) {
				return adapter;
			}
		}
		return null;
	}
}
