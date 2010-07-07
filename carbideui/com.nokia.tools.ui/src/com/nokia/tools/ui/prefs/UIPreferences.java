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

/**
 * Class for handling UI preferences, all UI preference keys shall be defiend
 * here as constants.
 * 
 */
public class UIPreferences {
	/**
	 * Default metrics selected
	 */
	public static final String DEFAULT_METRIC_SELECTED = "default.metric.selected";

	/**
	 * Hover enabled in Select Image -dialog
	 */
	public static final String PREF_HOVER_ENABLED_RESOURCE_PAGE = "hover.enabled.resource.page";

	/**
	 * Recent resource page used in Select Image -dialog
	 */
	public static final String PREF_LAST_ACTIVE_RESOURCE_PAGE = "last.active.resource.page";

	/**
	 * Recent height used in Select Image -dialog
	 */
	public static final String PREF_LAST_HEIGHT_RESOURCE_PAGE = "last.height.resource.page";

	/**
	 * Recent width used in Select Image -dialog
	 */
	public static final String PREF_LAST_WIDTH_RESOURCE_PAGE = "last.width.resource.page";

	/**
	 * Recent x- coordinate for Select Image -dialog
	 */
	public static final String PREF_LAST_XPOS_IMAGESELECTION_DIALOG = "last.xpos.imageselection.dialog";

	/**
	 * Recent y- coordinate for Select Image -dialog
	 */
	public static final String PREF_LAST_YPOS_IMAGESELECTION_DIALOG = "last.ypos.imageselection.dialog";

	/**
	 * Recent preview size used in Select Image -dialog
	 */
	public static final String PREF_PREVIEW_SIZE_RESOURCE_PAGE = "preview.size.resource.page";

	/**
	 * Default user login
	 */
	public static final String PREF_USER_NAME = "user.real.name";

	/**
	 * Default user login
	 */
	public static final String PREF_USER_LOGIN = "default.user.login";

	/**
	 * @return the preference store.
	 */
	public static IPreferenceStore getStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	/**
	 * No instantiation.
	 */
	private UIPreferences() {
	}

}
