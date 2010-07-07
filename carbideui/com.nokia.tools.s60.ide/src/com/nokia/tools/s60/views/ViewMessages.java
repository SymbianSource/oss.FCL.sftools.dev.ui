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
package com.nokia.tools.s60.views;

import org.eclipse.osgi.util.NLS;

/**
 * NLS messages.
 */
public class ViewMessages extends NLS {
	/**
	 * System theme category
	 */
	public static String ResourceView_Theme_Category;

	/**
	 * Audio Category
	 */
	public static String ResourceView_Audio_Category;

	/**
	 * Application menu
	 */
	public static String ResourceView_ApplicationMenu;

	/**
	 * Active idle
	 */
	public static String ResourceView_ActiveIdle;

	public static String ResourceView_Misc;

	/**
	 * Default text shown when gallery is not available.
	 */
	public static String GalleryView_defaultText;
	/**
	 * Selects screen.
	 */
	public static String GalleryView_selectScreen;
	public static String GalleryView_selectScreen_tooltip;
	/**
	 * Generates preview.
	 */
	public static String GalleryView_generatePreview;

	// Icon view
	public static String IconView_btnClear_Tooltip;
	public static String IconView_Preview_MsgBox_Title;
	public static String IconView_Preview_MsgBox_Message;
	public static String IconView_Preview_MsgBox_ShowAgain;

	public static String IconView_SelectAll_text;

	public static String resourcesView_progress_taskName;

	public static String IconsView_partNameDefault;
	public static String IconViewDropDownToolBarAction_menu;

	public static String IconView_toggleSync_tooltip;

	// Referenced Colors view
	public static String RefColors_Delete_MsgBox_Title;
	public static String RefColors_Delete_MsgBox_Message;
	public static String RefColors_Duplicate_Name_Title;
	public static String RefColors_Duplicate_Name_Message;
	
	static {
		NLS
				.initializeMessages(ViewMessages.class.getName(),
						ViewMessages.class);
	}
}
