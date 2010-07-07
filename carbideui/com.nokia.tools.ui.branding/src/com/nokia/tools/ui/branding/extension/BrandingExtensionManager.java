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
package com.nokia.tools.ui.branding.extension;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.ui.branding.defaultimpl.BrandingManager;

/**
 * The manager for providing the current brand manager 
 * 
 */
public class BrandingExtensionManager {

	/**
	 * 
	 * Branding id 
	 * 
	 */
	private static final String BRANDING_ID = "com.nokia.tools.ui.branding.branding";
	private static final String ATTR_CLASS = "class";
	
	/**
	 *  Branding id extension point
	 */
	private static final String EXT_BRANDING = "branding";	

	/**
	 *  Branding id extension point
	 */
	private static final IBrandingManager DEFAULT_MANAGER = new BrandingManager();
	
	/**
	 * Get the instance of the current branding manager 
	 * 
	 * @return - instance of <code> IBrandingManager </code>
	 */
	public static IBrandingManager getBrandingManager() 
	{
		//Get the extension point 
		IExtensionPoint point = Platform.getExtensionRegistry()
		.getExtensionPoint(BRANDING_ID);
		if(point != null)
		{
			//Get the extension
			IExtension[] extensions = point.getExtensions();
			for (IExtension extension : extensions)
			{
				for (IConfigurationElement element : extension
						.getConfigurationElements()) {
					if (EXT_BRANDING.equals(element.getName())) {
						try {
							return (IBrandingManager) element
									.createExecutableExtension(ATTR_CLASS);
						} catch (Exception e) {
							
						}
					}
				}
			}			
		}
		//If extension not defined then use the default branding
		return DEFAULT_MANAGER;
	}

}
