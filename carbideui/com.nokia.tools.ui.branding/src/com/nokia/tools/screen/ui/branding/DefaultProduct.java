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
package com.nokia.tools.screen.ui.branding;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.ui.branding.IProductConstants;
import org.osgi.framework.Bundle;

import com.nokia.tools.ui.branding.Activator;

/**
 * Default branding informations of the product
 *
 */
public abstract class DefaultProduct implements IProduct {

	private  Map<String, String> propertiesMap = new HashMap<String, String>(8);
	
	/**
	 * Initialize the properties 
	 */
	public DefaultProduct() {
		propertiesMap.put(IProductConstants.WINDOW_IMAGES, Messages.WINDOW_IMAGES);
		propertiesMap.put(IProductConstants.ABOUT_TEXT, Messages.PRODUCT_BLURB);
		propertiesMap.put(IProductConstants.ABOUT_IMAGE, Messages.ABOUT_IMAGE);
		propertiesMap.put(IProductConstants.APP_NAME, getName());
		propertiesMap.put(IProductConstants.PREFERENCE_CUSTOMIZATION, getPreferenceCustomization());
		propertiesMap.put(IProductConstants.STARTUP_FOREGROUND_COLOR, Messages.STARTUP_FOREGROUND_COLOR);
		propertiesMap.put(IProductConstants.STARTUP_MESSAGE_RECT, Messages.STARTUP_MESSAGE_RECT);
		propertiesMap.put(IProductConstants.STARTUP_PROGRESS_RECT, Messages.STARTUP_PROGRESS_RECT);	
		
	}
	
	/**
	 * Get the path of Preference customization xml
	 * @return -path of Preference customization xml
	 */
	protected abstract String getPreferenceCustomization();
	
	/**
	 * Get the version text
	 * 
	 * @return - the version text
	 */
	protected abstract String getVersionText();

	public String getApplication() {
		return getName() +getVersionText();
	}

	public Bundle getDefiningBundle() {
		return Activator.getDefault().getBundle();
	}

	public String getDescription() {
		return Messages.PRODUCT_BLURB;
	}

	public String getId() {
		return getApplication();
	}

	public String getProperty(String key) {
		return propertiesMap.get(key);
	}

}
