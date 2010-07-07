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

package com.nokia.tools.theme.s60.cstore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.tools.media.utils.UtilsPlugin;


public class ComponentPoolConfig {
	
	public static String PREF_ADD_EXAMPLES = "cpc.addExTh";
	public static String PREF_ADD_WSPACE_TH = "cpc.addWsTh";
	public static String PREF_ADD_CUSTOM_TH = "cpc.addCtTh";
	public static String PREF_ADD_OPEN_TH = "cpc.addOpenTh";
	public static String PREF_ADD_USERTHEMECOUNT = "cpc.userCount";
	public static String PREF_ADD_USERTHEME_BASE = "cpc.userTheme_";
	
	public boolean addExampleThemes, addWorkspaceTheme, addOpenThemes, addCustomThemes;
	public List<String> userThemeList;
	
	public static ComponentPoolConfig load() {
		
		IPreferenceStore iPreferenceStore = UtilsPlugin.getDefault().getPreferenceStore();
		ComponentPoolConfig cfg = new ComponentPoolConfig();
		
		iPreferenceStore.setDefault(PREF_ADD_EXAMPLES, true);
		cfg.addExampleThemes = iPreferenceStore.getBoolean(PREF_ADD_EXAMPLES);		
		cfg.addOpenThemes= iPreferenceStore.getBoolean(PREF_ADD_OPEN_TH);
		cfg.addWorkspaceTheme = iPreferenceStore.getBoolean(PREF_ADD_WSPACE_TH);
		cfg.addCustomThemes = iPreferenceStore.getBoolean(PREF_ADD_CUSTOM_TH);
		int userCount = iPreferenceStore.getInt(PREF_ADD_USERTHEMECOUNT);
		cfg.userThemeList = new ArrayList<String>();
		for (int a = 0; a < userCount; a++) {
			String path = iPreferenceStore.getString(PREF_ADD_USERTHEME_BASE + (a));
			if (path.length() > 0 && new File(path).exists()) {
				cfg.userThemeList.add(path);
			}
		}
		
		return cfg;
		
	}
	
	public void save() {
		IPreferenceStore iStore = UtilsPlugin.getDefault().getPreferenceStore();
		
		iStore.setValue(PREF_ADD_EXAMPLES, addExampleThemes);
		iStore.setValue(PREF_ADD_OPEN_TH, addOpenThemes);
		iStore.setValue(PREF_ADD_WSPACE_TH, addWorkspaceTheme);
		iStore.setValue(PREF_ADD_CUSTOM_TH, addCustomThemes);
		iStore.setValue(PREF_ADD_USERTHEMECOUNT, userThemeList.size());
		int c = 0;
		for (String p: userThemeList) {
			iStore.setValue(PREF_ADD_USERTHEME_BASE + c++, p);
		}		
	}

}
