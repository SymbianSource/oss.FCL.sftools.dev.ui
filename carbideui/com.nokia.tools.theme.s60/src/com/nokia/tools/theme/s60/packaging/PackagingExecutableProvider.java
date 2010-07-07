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
package com.nokia.tools.theme.s60.packaging;

import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.nokia.tools.packaging.PackagingContext;
import com.nokia.tools.packaging.PackagingPlugin;
import com.nokia.tools.platform.extension.PlatformExtensionManager;

/**
 * This class provides ability to fetch the path for the executables to be used
 * for various steps in the packaging process.
 * 
 * This class is more of a utility class which provides utility methods for
 * this.
 */
public class PackagingExecutableProvider {

	/**
	 * The constant for the extension point id to be used for processing the
	 * extension contributions which provide the information on the executables
	 * to be used for the packaging process.
	 */
	private static String PACKAGING_EXECUTABLE_EXTENSION_POINT_ID = "com.nokia.tools.theme.s60.platformExecutables";

	/**
	 * The constant for the extension point attribute name which defines the
	 * model id for which the definition is to be used.
	 */
	private static String ATTRIBUTE_MODEL_ID = "modelID";

	/**
	 * The constant for the extension point's child element name which defines
	 * the platform executables for a platform and a model/theme plugin for the
	 * platform.
	 */
	private static String PACKAGING_EXECUTABLE_ELEMENT_NAME = "platformExecutables";

	/**
	 * The constant for the attribute name in the executable definition
	 * contribution element whose value would define the type of the executable.
	 */
	private static String ATTRIBUTE_EXECUTABLE_NAME = "executableName";

	/**
	 * The constant for the attribute name in the executable definition
	 * contribution element whose value would define the path for the
	 * executable.
	 */
	private static String ATTRIBUTE_EXECUTABLE_PATH = "executablePath";

	/**
	 * The constant for the default path for the executable for a type defined
	 * for a platform.
	 */
	private static String DEFAULT_MODEL_ID = "default";

	/**
	 * The constant for the plugin specific parameters for the executable for a
	 * plugin.
	 */
	private static String ATTRIBUTE_PACKAGING_PARAMETER_PROVIDER = "parameterProvider";

	/**
	 * Private constructor to prevent any initialization of this class.
	 */
	private PackagingExecutableProvider() {
	}

	/**
	 * Returns the URL for the packaging executable for the specific model Id
	 * and for the specified type of the executable needed. If no specific one
	 * found, then the one defined as default for the platform is returned. If
	 * the default for the platform is also not defined then this method would
	 * return null.
	 * 
	 * @param modelID
	 *            : the model ID for which the executable has to be fetched.
	 * @param executableType
	 *            : the type of the executable.
	 * @return: the URL defining the path for the packaging executable.
	 */
	public static URL getPackagingExecutablePath(String modelID,
			PackagingExecutableType executableType, boolean fetchDefault) {
		URL defaultExecutablePath = null;
		URL executablePath = null;
		IExtension[] extensions = PlatformExtensionManager
				.getExtensions(PACKAGING_EXECUTABLE_EXTENSION_POINT_ID);
		if (extensions != null) {
			for (IExtension extension : extensions) {
				if (extension != null) {
					IConfigurationElement[] configurationElements = extension
							.getConfigurationElements();

					if (configurationElements != null) {

						for (IConfigurationElement configurationElement : configurationElements) {

							if (configurationElement != null
									&& PACKAGING_EXECUTABLE_ELEMENT_NAME
											.equals(configurationElement
													.getName())) {

								String tempModelID = configurationElement
										.getAttribute(ATTRIBUTE_MODEL_ID);
								if ((modelID.equalsIgnoreCase(tempModelID) || DEFAULT_MODEL_ID
										.equalsIgnoreCase(tempModelID))
										&& executableType
												.name()
												.equalsIgnoreCase(
														configurationElement
																.getAttribute(ATTRIBUTE_EXECUTABLE_NAME))) {

									String path = configurationElement
											.getAttribute(ATTRIBUTE_EXECUTABLE_PATH);
									Bundle extensionBundle = Platform
											.getBundle(extension
													.getNamespaceIdentifier());
									if (extensionBundle != null) {
										URL url = extensionBundle
												.getEntry(path);
										if (url != null) {
											if (DEFAULT_MODEL_ID
													.equalsIgnoreCase(tempModelID)) {
												defaultExecutablePath = url;
											} else {
												executablePath = url;
											}
										}
									}
								}
							}
						}
					}

					if (executablePath != null)
						break;
				}
			}
		}
		if (!fetchDefault)
			return executablePath;

		return executablePath == null ? defaultExecutablePath : executablePath;
	}

	/**
	 * Returns the list of updated parameter that are fetched from the plugin
	 * for executing the executables.
	 * 
	 * @param modelID
	 * @param executableType
	 * @param context
	 * @param currentParameterList
	 * @return
	 */
	public static List<String> getUpdatedParameterListForExecutable(
			String modelID, PackagingExecutableType executableType,
			PackagingContext context, List<String> currentParameterList) {
		IExtension[] extensions = PlatformExtensionManager
				.getExtensions(PACKAGING_EXECUTABLE_EXTENSION_POINT_ID);
		if (extensions != null) {
			for (IExtension extension : extensions) {
				if (extension != null) {
					IConfigurationElement[] configurationElements = extension
							.getConfigurationElements();

					if (configurationElements != null) {

						for (IConfigurationElement configurationElement : configurationElements) {

							if (configurationElement != null
									&& PACKAGING_EXECUTABLE_ELEMENT_NAME
											.equals(configurationElement
													.getName())) {

								String tempModelID = configurationElement
										.getAttribute(ATTRIBUTE_MODEL_ID);
								if ((modelID.equalsIgnoreCase(tempModelID))
										&& executableType
												.name()
												.equalsIgnoreCase(
														configurationElement
																.getAttribute(ATTRIBUTE_EXECUTABLE_NAME))) {

									String path = configurationElement
											.getAttribute(ATTRIBUTE_PACKAGING_PARAMETER_PROVIDER);

									if (path != null) {

										try {

											IPackagingExecutableParameterProvider packagingParameterProvider = (IPackagingExecutableParameterProvider) configurationElement
													.createExecutableExtension(ATTRIBUTE_PACKAGING_PARAMETER_PROVIDER);
											if (packagingParameterProvider != null) {
												List<String> updatedParameterList = packagingParameterProvider
														.getUpdatedParameterList(
																currentParameterList,
																context);
												return updatedParameterList == null ? currentParameterList
														: updatedParameterList;
											}

										} catch (CoreException e) {
											PackagingPlugin.error(e);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return currentParameterList;

	}
}
