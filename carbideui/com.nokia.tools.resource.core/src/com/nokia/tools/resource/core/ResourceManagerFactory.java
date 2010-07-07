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
package com.nokia.tools.resource.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import com.nokia.tools.resource.util.FileUtils;

/**
 * The factory class for providing the <code>ResourceManager</code> based on
 * the root path.
 *  
 */
public class ResourceManagerFactory {

	private static Map<String, ResourceManager> managers = new HashMap<String, ResourceManager>();

	/**
	 * 
	 * resource id
	 * 
	 */
	private static final String RESOURCE_ID = "com.nokia.tools.resource.core.resourcehelper";
	private static final String ATTR_CLASS = "class";

	/**
	 * Resource helper id extension point
	 */
	private static final String EXT_HELPER = "resourcehelper";

	/**
	 * Get the resource manager for a particular project for tools resources the
	 * project path can be null.
	 * 
	 * @param projectPath
	 * @return the resource manger
	 */
	public static ResourceManager getResourceManager(String projectName) {
		if (projectName == null) {
			projectName = ResourceConstants.ROOTPATH;
		}
		if (managers.get(projectName) == null) {
			ResourceEventHandler eventor = new ResourceEventHandler();
			// Global resources
			if (projectName.trim().equals(ResourceConstants.ROOTPATH)) {
				ResourceHelper recorder = getResourceHelper();
				if (recorder != null) {
					recorder
							.setRootFile(setRootFile(ResourceConstants.ROOTPATH));
					ResourceManager manager = new DefaultResourceManager(
							recorder, eventor);
					managers.put(ResourceConstants.ROOTPATH, manager);
				}
			}
		}
		return managers.get(projectName);

	}

	/**
	 * Get the extension for resource helper
	 * 
	 * @return ResourceHelper
	 */
	public static ResourceHelper getResourceHelper() {
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(RESOURCE_ID);
		if (point != null) {
			// Get the extension
			IExtension[] extensions = point.getExtensions();
			for (IExtension extension : extensions) {
				for (IConfigurationElement element : extension
						.getConfigurationElements()) {
					if (EXT_HELPER.equals(element.getName())) {
						try {
							return (ResourceHelper) element
									.createExecutableExtension(ATTR_CLASS);
						} catch (Exception e) {

						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * 
	 * @param rootPath
	 * @return
	 */
	private static File setRootFile(String rootPath) {
		return new File(FileUtils.getFile(Platform.getInstallLocation()
				.getURL()), rootPath);

	}
}
