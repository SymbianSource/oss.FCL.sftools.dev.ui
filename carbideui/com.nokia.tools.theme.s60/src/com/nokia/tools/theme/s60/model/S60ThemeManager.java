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

package com.nokia.tools.theme.s60.model;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;

import com.nokia.tools.platform.theme.AbstractThemeManager;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconLoadException;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconManager;
import com.nokia.tools.theme.s60.parser.ThemeDetailsParser;


public class S60ThemeManager
    extends AbstractThemeManager {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.IThemeManager#openTheme(java.io.File,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Theme openTheme(File file, IProgressMonitor monitor)
	    throws ThemeException {
		Theme theme = new ThemeDetailsParser(FileUtils.toURL(file), null, false, monitor).parseLean();
		S60Theme s60Theme = (S60Theme)theme;
		try {
			ThirdPartyIconManager.loadToolSpecificThirdPartyIcons(s60Theme);		
			ThirdPartyIconManager.loadThemeSpecificIcons(s60Theme);
		} catch (ThirdPartyIconLoadException e) {
				throw new ThemeException(e);
		}
		theme.refreshElementList();
		theme = new ThemeDetailsParser(FileUtils.toURL(file), s60Theme, monitor).parseFat();
		return theme;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.AbstractThemeManager#createModelSpi(java.lang.String,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Theme createModelSpi(String modelId, IProgressMonitor monitor)
	    throws ThemeException {
		return new S60Theme(modelId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.AbstractThemeManager#processModelSpi(com.nokia.tools.platform.theme.Theme,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void processModelSpi(Theme model, IProgressMonitor monitor)
	    throws ThemeException {
		URL modelURL = ThemePlatform.getThemeModelDescriptorById(
		    model.getModelId()).getModelPath();
		new ThemeDetailsParser(modelURL, model, true, monitor).parseFat();
		S60Theme s60Theme = (S60Theme)model;
		
		try {
			ThirdPartyIconManager.loadToolSpecificThirdPartyIcons(s60Theme);
			ThirdPartyIconManager.loadThemeSpecificIcons(s60Theme);
		} catch (ThirdPartyIconLoadException e) {
			throw new ThemeException(e);
		}
		if(s60Theme.getThemeSpecificThirdPartyIcons().size() > 0){
			s60Theme.refreshElementList();
		}
	}
}
