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

package com.nokia.tools.carbide.ui.productsupport.actions;

import java.util.Properties;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.osgi.service.prefs.BackingStoreException;

import com.nokia.tools.carbide.ui.productsupport.actions.intro.DontShowThisAgainContentProvider;

public class DontShowWelcomePageAction implements IIntroAction {
	public void run(IIntroSite site, Properties params) {
		IPreferencesService preferencesService = Platform.getPreferencesService();
		IEclipsePreferences preferences = preferencesService.getRootNode();
		preferences.putBoolean(IWorkbenchPreferenceConstants.SHOW_INTRO, !preferences.getBoolean(IWorkbenchPreferenceConstants.SHOW_INTRO, true));
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			// Ignore the exception that might happen storing into the preference store.
		}
		DontShowThisAgainContentProvider.reloadPage();
	}
}
