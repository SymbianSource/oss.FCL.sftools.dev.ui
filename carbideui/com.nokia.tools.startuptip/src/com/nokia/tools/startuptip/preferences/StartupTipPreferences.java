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

package com.nokia.tools.startuptip.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.tools.startuptip.StartupTipPlugin;

/**
 * A singleton class to access the preferences.
 * 
 */
public class StartupTipPreferences {

	public static String SHOW_TIPS_ON_STARTUP = "st.showTipOnStartup";
	public static String SHOW_FROM_CATEGORY = "st.showFromCategory";
	public static String TIPS_SHOWN = "st.tipsShown";

	private IPreferenceStore prefStore;

	private StartupTipPreferences() {
		load();
	}

	private static StartupTipPreferences instance;

	public static StartupTipPreferences getInstance() {
		if (instance == null) {
			instance = new StartupTipPreferences();
		}
		return instance;
	}

	private void load() {
		prefStore = StartupTipPlugin.getDefault().getPreferenceStore();
		prefStore.setDefault(SHOW_TIPS_ON_STARTUP, true);
		prefStore.setDefault(SHOW_FROM_CATEGORY, "All");
	}

	/**
	 * 
	 * @return <code>true</code> if Show on startup preference is enabled and
	 *         <code>false</code> otherwise
	 */
	public boolean showTipOnStartup() {
		return prefStore.getBoolean(SHOW_TIPS_ON_STARTUP);
	}

	/**
	 * Sets the preference show on startup.
	 * 
	 * @param value
	 *            <code>true</code> or <code>false</code>
	 */
	public void setShowTipOnStartup(boolean value) {
		prefStore.setValue(SHOW_TIPS_ON_STARTUP, value);
	}

	/**
	 * Returns <code>category</code> from the stored preferences.
	 * 
	 * @return <code>category</code>
	 */
	public String getCategory() {
		return prefStore.getString(SHOW_FROM_CATEGORY);
	}

	/**
	 * Sets the <code>category</code>. Startup tips belonging to this
	 * <code>category</code> will be shown.
	 * 
	 * @param category
	 */
	public void setCategory(String category) {
		prefStore.setValue(SHOW_FROM_CATEGORY, category);
	}

	/**
	 * Set <code>shownList</code> to the preferences.
	 * 
	 * @param shownList
	 */
	public void setTipsShownList(List<String> shownList) {
		prefStore.setValue(TIPS_SHOWN, shownList.toString());
	}

	/**
	 * Fetches the shown tips from the stored preferences and returns it as a
	 * list.
	 * 
	 * @return an <code>List&ltString&gt</code> of shown tips.
	 */
	public List<String> getTipsShownList() {
		String shownList = prefStore.getString(TIPS_SHOWN);
		List<String> tipList = new ArrayList<String>();
		if (shownList.length() > 2) {
			shownList = shownList.substring(1, shownList.length() - 1);
			StringTokenizer tknizer = new StringTokenizer(shownList, ",");
			while (tknizer.hasMoreElements()) {
				tipList.add(tknizer.nextToken());
			}
		}
		return tipList;
	}
}
