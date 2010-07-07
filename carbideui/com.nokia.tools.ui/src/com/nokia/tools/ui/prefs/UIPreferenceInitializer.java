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

package com.nokia.tools.ui.prefs;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.tools.media.image.RegQueryUtil;

/**
 * Initializes the default preferences.
 * 
 */
public class UIPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = UIPreferences.getStore();
		store.setDefault(UIPreferences.PREF_LAST_ACTIVE_RESOURCE_PAGE, 0);
		store.setDefault(UIPreferences.PREF_PREVIEW_SIZE_RESOURCE_PAGE, 150);
		store.setDefault(UIPreferences.PREF_LAST_WIDTH_RESOURCE_PAGE, 501);
		store.setDefault(UIPreferences.PREF_LAST_HEIGHT_RESOURCE_PAGE, 362);
		store.setDefault(UIPreferences.PREF_LAST_XPOS_IMAGESELECTION_DIALOG,
		    -9999);
		store.setDefault(UIPreferences.PREF_LAST_YPOS_IMAGESELECTION_DIALOG,
		    -9999);

		store.setDefault(UIPreferences.DEFAULT_METRIC_SELECTED, "px");
		store.setDefault(UIPreferences.PREF_USER_LOGIN, System
		    .getProperty("user.name"));
		store.setDefault(UIPreferences.PREF_USER_NAME, RegQueryUtil
		    .getUserName());

	}
}
