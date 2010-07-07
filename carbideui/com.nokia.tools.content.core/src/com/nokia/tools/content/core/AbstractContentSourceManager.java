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
package com.nokia.tools.content.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

/**
 * This class manages the content source contributions and provides methods for
 * fetching and creating contents.
 */
public abstract class AbstractContentSourceManager implements
		IContentSourceManager {
	/**
	 * Extension point id.
	 */
	private static final String PROVIDER_ID = "com.nokia.tools.content.core.providers";

	/**
	 * @return all known content types.
	 */
	public static String[] getContentTypes() {
		Set<String> types = new HashSet<String>();
		for (IContentProvider provider : findContentProviders()) {
			types.add(provider.getContentType());
		}
		return types.toArray(new String[types.size()]);
	}

	public static void initContent() {
		try {
			// initializes the content subsystems after the ui has been properly
			// initialized
			for (String type : AbstractContentSourceManager.getContentTypes()) {
				ContentSourceManager.getGlobalInstance().getRootContents(type,
						null);
			}
		} catch (Exception e) {
			CorePlugin.error(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentSourceManager#getContentProvider(java.lang.String)
	 */
	protected IContentProvider getContentProvider(String type) {
		if (type == null) {
			return null;
		}
		List<IContentProvider> handlers = findContentProviders();
		for (IContentProvider handler : handlers) {
			if (type.equals(handler.getContentType())) {
				return handler;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentSourceManager#getRootContents(java.lang.String,
	 *      java.lang.Object)
	 */
	public List<IContent> getRootContents(String type, Object input,
			IProgressMonitor monitor) throws IOException, ContentException {
		List<IContentProvider> handlers = findContentProviders();
		List<IContent> themes = new ArrayList<IContent>();
		if (type != null) {
			for (IContentProvider handler : handlers) {
				if (type.equals(handler.getContentType())) {
					List<IContent> rootContents = handler.getRootContents(
							input, monitor);
					if (null != rootContents) {
						themes.addAll(rootContents);
					}
				}
			}
		}
		return themes;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentSourceManager#getRootContents(java.lang.String,
	 *      java.lang.Object)
	 */
	public List<IContent> getRootContents(String type, IProgressMonitor monitor)
			throws IOException, ContentException {
		List<IContentProvider> handlers = findContentProviders();
		List<IContent> themes = new ArrayList<IContent>();
		if (type != null) {
			for (IContentProvider handler : handlers) {
				if (type.equals(handler.getContentType())) {
					List<IContent> rootContents = handler.getRootContents(null,
							monitor);
					if (null != rootContents) {
						themes.addAll(rootContents);
					}
				}
			}
		}
		return themes;

	}

	/**
	 * Finds all content providers.
	 * 
	 * @return the list of content providers.
	 */
	public static List<IContentProvider> findContentProviders() {
		List<IContentProvider> providers = new ArrayList<IContentProvider>();

		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(PROVIDER_ID);
		IExtension[] extensions = point.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
					.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				IContentProvider handler = null;
				try {
					handler = (IContentProvider) element
							.createExecutableExtension("class");
				} catch (CoreException e) {
					CorePlugin.error(
							CoreMessages.Error_InstantiateContentProvider, e);
				}

				if (handler != null) {
					providers.add(handler);
				}
			}
		}
		return providers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentSourceManager#createRootContents(java.lang.String,
	 *      java.lang.Object)
	 */
	public List<IContent> createRootContents(String type,
			Map<String, Object> creationData, IProgressMonitor monitor)
			throws ContentException {
		List<IContentProvider> handlers = findContentProviders();
		List<IContent> themes = new ArrayList<IContent>();
		if (type != null) {
			for (IContentProvider handler : handlers) {
				if (type.equals(handler.getContentType())) {
					List<IContent> rootContents = handler.createRootContent(
							creationData, monitor);
					if (null != rootContents) {
						themes.addAll(rootContents);
					}
				}
			}
		}
		return themes;
	}
}
