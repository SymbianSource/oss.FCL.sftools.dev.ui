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
 * File Name ThemeAppBundle.java Description File loads the properties related
 * to Skin Application
 */

package com.nokia.tools.theme.s60.general;

import java.io.File;
import java.util.Enumeration;
import java.util.ResourceBundle;

import com.nokia.tools.resource.util.ResourceUtils;
import com.nokia.tools.theme.s60.S60ThemePlugin;

/**
 * Class for resource management in Skin Application
 * 
 */
public class ThemeAppBundle {
	/**
	 * This holds the ResourceBundle used
	 */
	private static ResourceBundle settingsResourceBundle;

	private static File dir;

	static {
		try {
			String staticURL = System.getProperty("theme.dir");
			if (staticURL != null) {
				dir = new File(staticURL);
			} else {
				dir = ResourceUtils.getPluginFile(S60ThemePlugin.getDefault(),
						"");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		settingsResourceBundle = getBundle("ThemeSettings");
		// EffectBundle = ResourceBundle.getBundle("Effect");
	}

	/**
	 * Default constructor
	 */
	private ThemeAppBundle() {
	}

	private static ResourceBundle getBundle(String baseName) {
		final ResourceBundle bundle = ResourceBundle.getBundle(baseName);
		return new ResourceBundle() {
			public Enumeration<String> getKeys() {
				return bundle.getKeys();
			}

			public Object handleGetObject(String key) {
				String value = bundle.getString(key);
				if (value != null && value.startsWith("PLUGIN_ROOT")) {
					return new File(dir, value.replaceAll("PLUGIN_ROOT/", ""))
							.getAbsolutePath();
				}
				return value;
			}
		};
	}

	/**
	 * @return The handle to the properties bundle for handling Skin properties
	 */
	public static ResourceBundle getPropBundle() {
		return ThemeAppBundle.settingsResourceBundle;
	}
}
