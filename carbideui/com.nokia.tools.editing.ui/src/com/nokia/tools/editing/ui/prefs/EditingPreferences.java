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
package com.nokia.tools.editing.ui.prefs;

import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.tools.editing.ui.Activator;

public class EditingPreferences {
	/**
	 * Design aid
	 */
	public static final String PREF_DESIGN_AID = "design.aid";

	public static IPreferenceStore getStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	public static boolean isDesignAidEnabled() {
		return getStore().getBoolean(PREF_DESIGN_AID);
	}

	public static void setDesignAidEnabled(boolean isDesignAidEnabled) {
		getStore().setValue(PREF_DESIGN_AID, isDesignAidEnabled);
	}
}
