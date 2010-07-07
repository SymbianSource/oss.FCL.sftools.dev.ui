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
package com.nokia.tools.screen.ui.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.tools.media.utils.UtilsPlugin;

public class PathHandlingConfig {

	public static String PREF_USE_PREDEFINED = "cpc.usePredefined";

	public static String PREF_PREDEFINED_COUNT = "cpc.predefinedCount";
	public static String PREF_RECENT_COUNT = "cpc.recentCount";

	public static String PREF_ADD_PREDEFINED = "cpc.predefined_";
	public static String PREF_ADD_RECENTLY = "cpc.recently_";

	public boolean usePredefined;
	public String recentlyUsedPage;
	public int predefinedCount, recentCount;
	public List<String> predefinedPathList, recentPathList;

	public static PathHandlingConfig load() {

		IPreferenceStore iPreferenceStore = UtilsPlugin.getDefault()
				.getPreferenceStore();
		PathHandlingConfig cfg = new PathHandlingConfig();

		// Defaults
		iPreferenceStore.setDefault(PREF_USE_PREDEFINED, false);
		iPreferenceStore.setDefault(PREF_RECENT_COUNT, 5);

		cfg.usePredefined = iPreferenceStore.getBoolean(PREF_USE_PREDEFINED);
		cfg.recentCount = iPreferenceStore.getInt(PREF_RECENT_COUNT);

		int defCount = iPreferenceStore.getInt(PREF_PREDEFINED_COUNT);
		int recCount = iPreferenceStore.getInt(PREF_RECENT_COUNT);

		cfg.predefinedPathList = new ArrayList<String>();

		for (int i = 0; i < defCount; i++) {
			String path = iPreferenceStore.getString(PREF_ADD_PREDEFINED + (i));
			if (path.length() > 0 && new File(path).exists()) {
				cfg.predefinedPathList.add(path);
			}
		}

		cfg.recentPathList = new ArrayList<String>();

		for (int i = 0; i < recCount; i++) {
			String path = iPreferenceStore.getString(PREF_ADD_RECENTLY + (i));
			if (path.length() > 0 && new File(path).exists()) {
				cfg.recentPathList.add(path);
			}
		}
		return cfg;
	}

	public void save() {
		IPreferenceStore iStore = UtilsPlugin.getDefault().getPreferenceStore();
		iStore.setValue(PREF_USE_PREDEFINED, usePredefined);
		iStore.setValue(PREF_PREDEFINED_COUNT, predefinedPathList.size());
		iStore.setValue(PREF_RECENT_COUNT, recentCount);
		int i = 0;
		for (String p : predefinedPathList) {
			iStore.setValue(PREF_ADD_PREDEFINED + i++, p);
		}
	}

	public void saveRecentPathList() {
		IPreferenceStore iStore = UtilsPlugin.getDefault().getPreferenceStore();
		int i = 0;
		for (String p : recentPathList) {
			iStore.setValue(PREF_ADD_RECENTLY + i++, p);
		}
	}

	public void clearRecentPathList() {
		IPreferenceStore iStore = UtilsPlugin.getDefault().getPreferenceStore();
		int i = 0;
		for (String p : recentPathList) {
			iStore.setToDefault(PREF_ADD_RECENTLY + i++);
		}
	}
}
