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
package com.nokia.tools.carbide.ui.productsupport.actions.intro;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.intro.config.IIntroContentProvider;
import org.eclipse.ui.intro.config.IIntroContentProviderSite;

import com.nokia.tools.carbide.ui.productsupport.ProductsupportPlugin;
import com.nokia.tools.resource.util.FileUtils;

public class DontShowThisAgainContentProvider implements IIntroContentProvider {

	static IIntroContentProviderSite lastSite;

	static IIntroContentProvider lastInstance;

	public void init(IIntroContentProviderSite site) {
		lastSite = site;
		lastInstance = this;
	}

	public void createContent(String id, PrintWriter out) {
		String actionUrl = "http://org.eclipse.ui.intro/runAction?pluginId=com.nokia.tools.carbide.ui.productsupport&amp;class=com.nokia.tools.carbide.ui.productsupport.actions.DontShowWelcomePageAction";
		String imagePath = null;
		IPreferencesService preferencesService = Platform.getPreferencesService();
		IEclipsePreferences preferences = preferencesService.getRootNode();
		boolean showIntro = preferences.getBoolean(IWorkbenchPreferenceConstants.SHOW_INTRO, true);
		if (showIntro) {
			imagePath = "/icons/checkboxenabledoff.gif";

		} else {
			imagePath = "/icons/checkboxenabledon.gif";
		}
		PrefUtil.getAPIPreferenceStore().setValue(
				IWorkbenchPreferenceConstants.SHOW_INTRO, showIntro);
		try {
			URL url = FileUtils.getURL(ProductsupportPlugin.getDefault(), imagePath);
			imagePath = new File(FileLocator.resolve(url).getFile()).getAbsolutePath();

		} catch (Exception e) {
			ProductsupportPlugin.getDefault().logException(e);
		}

		String content = "<form action=\"" + actionUrl + "\" method=\"post\">"
				+ "<input type=\"image\" src=\"" + imagePath
				+ "\" value=\"Send\" align=\"center\"> "
				+ Messages.DontShowThisAgain + "</form>";

		out.print(content);
	}

	public void createContent(String id, Composite parent, FormToolkit toolkit) {
	}

	public void dispose() {

	}

	public static void reloadPage() {
		if (lastSite != null) {
			lastSite.reflow(lastInstance, false);
		}
	}
}
