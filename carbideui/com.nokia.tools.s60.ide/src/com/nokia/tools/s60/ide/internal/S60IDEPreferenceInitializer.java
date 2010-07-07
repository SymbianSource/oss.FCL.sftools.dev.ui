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
package com.nokia.tools.s60.ide.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.tools.editing.ui.prefs.EditingPreferences;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.MediaPreferenceInitializer;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.s60.ide.IS60IDEConstants;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.views.GalleryLayout;
import com.nokia.tools.s60.views.GalleryPage;
import com.nokia.tools.screen.ui.IScreenConstants;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IGalleryDescriptor;

/**
 */
public class S60IDEPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore prefs = S60WorkspacePlugin.getDefault()
				.getPreferenceStore();

		for (IGalleryDescriptor descriptor : ExtensionManager
				.getGalleryDescriptors()) {
			prefs.setDefault(IS60IDEConstants.PREF_SCREEN_DEFAULT + "."
					+ descriptor.getContentType(), descriptor.getDefault());
			prefs.setDefault(IS60IDEConstants.PREF_GALLERY_SCREENS + "."
					+ descriptor.getContentType(), StringUtils.formatArray(
					descriptor.getScreens(), ","));
		}

		prefs.setDefault(IS60IDEConstants.PREF_GALLERY_LAYOUT_TYPE,
				GalleryLayout.MAXIMUM_STRETCH);
		prefs.setDefault(IS60IDEConstants.PREF_GALLERY_MODE,
				GalleryPage.MODE_USER);

		prefs.setDefault(IS60IDEConstants.PREF_SHOW_TITLE_TEXTS, true);
		prefs.setDefault(IS60IDEConstants.PREF_FIRST_TIME_LAUNCH, true);
		prefs.setDefault(IS60IDEConstants.PREF_SHOW_EDITOR_TOOLTIP, false);

		prefs = UiPlugin.getDefault().getPreferenceStore();
		prefs.setDefault(IScreenConstants.PREF_ZOOMING_GLOBAL, true);
		prefs.setDefault(IScreenConstants.PREF_ZOOMING_FACTOR, "1.0");
		prefs.setDefault(IScreenConstants.PREF_AUTO_ANIMATION_DISABLED, false);
		String defaultDeploymentTool = MediaPreferenceInitializer
				.getExternalEditorCommandForExtension("sis");
		if (defaultDeploymentTool != "")
			prefs.setDefault(IScreenConstants.PREF_DEPLOYMENT_TOOL,
					defaultDeploymentTool);
		prefs.setDefault(IScreenConstants.PREF_LAUCNH_DEPLOYMENT_TOOL, false);
		prefs = UtilsPlugin.getDefault().getPreferenceStore();
		prefs.setDefault(IMediaConstants.PREF_NINE_PIECE_2SINGLE, true);
		prefs.setDefault(IMediaConstants.PREF_SINGLE_PIECE_2NINE, true);
		prefs
				.setDefault(
						IScreenConstants.PREF_LAST_COLUMNRATIO_THEME_RESOURCE_PAGE,
						0.6);

		prefs = EditingPreferences.getStore();
		prefs.setDefault(EditingPreferences.PREF_DESIGN_AID, true);
	}
}
