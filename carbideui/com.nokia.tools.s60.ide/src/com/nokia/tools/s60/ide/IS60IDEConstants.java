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
package com.nokia.tools.s60.ide;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
*/
public interface IS60IDEConstants {
	String PLUGIN_ID = "com.nokia.tools.s60.ide";

	QualifiedName CONTENTS_NAME = new QualifiedName(PLUGIN_ID, "contents");

	
	String ID_GALLERY_VIEW = "com.nokia.tools.s60.views.Gallery";
	String ID_NEW_WIZ = "com.nokia.tools.s60.ide.mobileUIProject";
	String ID_ICON_VIEW = "com.nokia.tools.s60.views.IconView";
	String ID_LAYERS_VIEW = "com.nokia.tools.s60.editor.ui.views.LayersView";
	String ID_COMPONENT_STORE_VIEW = "com.nokia.tools.theme.s60.ui.views.ComponentStore";
	String ID_SEARCH_VIEW = "com.nokia.tools.s60.views.SearchView";
	String ID_COLORS_VIEW = "com.nokia.tools.s60.views.ColorsView";
	
	String CARBIDE_UI_PERSPECTIVE_ID = "com.nokia.tools.s60.ide.perspective";



	/**
	 * Color of border used to highlight selected during Drag and Drop
	 */
	int DRAG_HIGHLIGHT_COLOR = SWT.COLOR_BLUE;

	int IMAGE_LABEL_TEXT_LINES = 2;

	// allocated colors

	Color BACKGROUND_ALL_RGB = new Color(null, 209, 209, 211);

	Color FOREGROUND_TITLE_RGB = new Color(null, 254, 254, 254);

	Color FOREGROUND_MESSAGE_RGB = new Color(null, 26, 26, 26);

	/**
	 * Preferences
	 */
	String PREF_ACTIVATE_PERSPECTIVE = PLUGIN_ID + ".activate.perspective";

	/**
	 * 
	 */
	String PREF_TOOLBARIDSTOHIDE = "";

	/**
	 * Default screen name
	 */
	String PREF_SCREEN_DEFAULT = PLUGIN_ID + ".screen.default";
	

	/**
	 * Default gallery screen names
	 */
	String PREF_GALLERY_SCREENS = PLUGIN_ID + ".gallery.screens";

	
	/**
	 * Layout type of gallery, fill all to screen or stretch
	 */
	String PREF_GALLERY_LAYOUT_TYPE = PLUGIN_ID + ".gallery.layoutType";


	/**
	 * Gallery mode, all or user defined
	 */
	String PREF_GALLERY_MODE = PLUGIN_ID + ".gallery.mode";

	/**
	 * Hide no preview messagebox or not?
	 */
	String PREF_NO_PREVIEW_HIDE_MESSAGEBOX = PLUGIN_ID + ".preview.info.hide";

	/**
	 * Show texts in component view on or off?
	 */
	String PREF_SHOW_TITLE_TEXTS = PLUGIN_ID + ".activate.texts";

	/**
	 * Synchronization on or off?
	 */
	String PREF_SYNC_WITH_EDITOR = PLUGIN_ID + ".activate.texts";

	/**
	 * First time launch or not?
	 */
	String PREF_FIRST_TIME_LAUNCH = PLUGIN_ID + ".first.launch";
	
	/**
	 * Show editor floating toolbar?
	 */
	String PREF_SHOW_EDITOR_TOOLTIP = PLUGIN_ID + ".editor.tooltip.show";
}
