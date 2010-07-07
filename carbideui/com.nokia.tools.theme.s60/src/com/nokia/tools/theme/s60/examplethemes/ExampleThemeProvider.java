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
package com.nokia.tools.theme.s60.examplethemes;

import org.eclipse.core.runtime.IExtension;

import com.nokia.tools.platform.extension.PlatformExtensionManager;

/**
 * A utility class which provides utility methods for checking and getting example theme contributor.
 */
public class ExampleThemeProvider {

	/**
	 * The constant for the extension point id to be used for processing the
	 * extension contributions which provide the information on the example
	 * themes to be used for the Sample Themes section.
	 */
	private static String EXAMPLE_THEMES_EXTENSION_POINT_ID = "com.nokia.tools.theme.s60.exampleThemes";

	private ExampleThemeProvider() {
	}

	public static boolean isExampleThemeAvailable() {

		IExtension[] extensions = PlatformExtensionManager
				.getExtensions(EXAMPLE_THEMES_EXTENSION_POINT_ID);
		if (extensions != null & extensions.length > 0) {
			return true;
		}
		return false;
	}

	public static String getContributorName() {
		IExtension[] extensions = PlatformExtensionManager
				.getExtensions(EXAMPLE_THEMES_EXTENSION_POINT_ID);
		if (extensions != null & extensions.length > 0) {
			return extensions[0].getContributor().getName();
		}
		return null;
	}
}