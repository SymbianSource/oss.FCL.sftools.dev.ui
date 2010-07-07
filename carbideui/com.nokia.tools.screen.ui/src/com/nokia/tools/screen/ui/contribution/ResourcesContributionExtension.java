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
package com.nokia.tools.screen.ui.contribution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.util.BundleUtility;

import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.extension.IResourceContributionDescriptor;

/**
 * projectResourcesContribution -´extension point access
 * 
 * 
 */
public class ResourcesContributionExtension {

	public static final String EXTENSION_POINT_RESOURCES_CONTRIBUTION = UiPlugin
			.getDefault().getBundle().getSymbolicName()
			+ ".projectResourcesContribution"; //$NON-NLS-1$

	public static final String COMPONENT = "component"; //$NON-NLS-1$

	public static final ResourcesContributionExtension INSTANCE = new ResourcesContributionExtension();

	public static IResourceContributionDescriptor[] getContributorDescriptors() {
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(EXTENSION_POINT_RESOURCES_CONTRIBUTION);
		IExtension[] extensions = point.getExtensions();
		List<IResourceContributionDescriptor> contributors = new ArrayList<IResourceContributionDescriptor>();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
					.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (COMPONENT.equals(element.getName())) {
					contributors
							.add(new ResourceContributionDescriptor(element));
				}
			}
		}
		return contributors
				.toArray(new IResourceContributionDescriptor[contributors
						.size()]);
	}

	/**
	 * wrapping access to ResourceContribution extension point. class for
	 * internal usage only.
	 */
	static class ResourceContributionDescriptor implements
			IResourceContributionDescriptor {

		public static final String NAME = "name"; //$NON-NLS-1$

		public static final String PATH = "path"; //$NON-NLS-1$

		public static final String DESTINATION_PATH = "destinationPath"; //$NON-NLS-1$
		
		public static final String COMPULSORY = "compulsory"; //$NON-NLS-1$
		
		public static final String PLATFORM_SPEC = "forPlatforms"; //$NON-NLS-1$

		IConfigurationElement element;

		ResourceContributionDescriptor(IConfigurationElement element) {
			this.element = element;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.extension.IResourceContributionDescriptor#getDestinationPath()
		 */
		public String getDestinationPath() {
			return (element.getAttribute(DESTINATION_PATH) != null) ? element
					.getAttribute(DESTINATION_PATH) : "";
			
		}
		
		public boolean compulsory() {
			return "true".equalsIgnoreCase(element.getAttribute(COMPULSORY));
		}
		
		public String forPlatforms() {
			return element.getAttribute(PLATFORM_SPEC);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.extension.IResourceContributionDescriptor#getName()
		 */
		public String getName() {
			return element.getAttribute(NAME);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.extension.IResourceContributionDescriptor#getURL()
		 */
		public String getFilePath() throws IOException {

			String path = element.getAttribute(PATH);
			if (null == path)
				path = "";
			return FileLocator.toFileURL(
					BundleUtility.find(element.getDeclaringExtension()
							.getContributor().getName(), path)).getPath();
		}

	}
}
