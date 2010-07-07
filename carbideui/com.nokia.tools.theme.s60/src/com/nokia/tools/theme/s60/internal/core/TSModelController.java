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
package com.nokia.tools.theme.s60.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.nokia.tools.content.core.ContentException;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.s60.effects.EffectsParser;
import com.nokia.tools.theme.s60.morphing.ValueModelXmlParser;

/**
 * Wrapping access to the Theme Studio model Class functionality is based on
 * com.nokia.themeapp.galleryview.gallerymain.ThemeController
 */
public class TSModelController {

	private static boolean isInitialized;

	public static synchronized void init(IProgressMonitor monitor)
			throws ContentException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		if (!isInitialized) {
			// moved here to prevent component store from initializing twice
			isInitialized = true;
			monitor.subTask(Messages.Task_Initializing);
			ThemePlatform.init();
			monitor.subTask(Messages.Task_ParsingEffects);
			EffectsParser.init();
			monitor.worked(10);
			monitor.subTask(Messages.Task_ParsingValueModels);
			ValueModelXmlParser.init();
			monitor.worked(30);
		}
	}

	public static synchronized void release() {
		if (isInitialized) {
			ValueModelXmlParser.release();
			EffectsParser.release();
			ThemePlatform.release();
			isInitialized = false;
		}
	}

	/**
	 * Cheching whether the file is storage for theme
	 * 
	 * @param filepath
	 * @return
	 */
	public static boolean isValidThemeFormat(String filepath) {
		return filepath.toLowerCase().endsWith(ThemeTag.SKN_FILE_EXTN);
	}
}
