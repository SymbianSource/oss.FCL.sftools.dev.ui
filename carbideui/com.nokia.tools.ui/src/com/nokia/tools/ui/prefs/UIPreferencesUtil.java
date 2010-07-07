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

import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.tools.ui.Activator;

public class UIPreferencesUtil {
	/**
	 * Returns the logged user id , for eg: Unix/windows user name
	 * @return
	 */
	public static String getRegisteredUserName(){
		return getStringValue(UIPreferences.PREF_USER_LOGIN);
	}
	/**
	 * Returns the real name of windows/linux logged user 
	 * @return
	 */
	public static String getLoggedUserRealName(){
		return getStringValue(UIPreferences.PREF_USER_NAME);
	}
	
	public static String getStringValue(final String id) {
		final IPreferenceStore store = Activator.getDefault()
		    .getPreferenceStore();
		return store.getString(id);
	}
	
}
