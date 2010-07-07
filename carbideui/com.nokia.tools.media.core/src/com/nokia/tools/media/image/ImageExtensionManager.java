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
package com.nokia.tools.media.image;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import com.nokia.tools.media.core.Activator;
import com.nokia.tools.resource.util.StringUtils;

public class ImageExtensionManager {
	/**
	 * Player contributor id.
	 */
	public static final String IMAGE_CONTRIBUTOR_ID = Activator.PLUGIN_ID
			+ ".images";
	private static final String LOADER = "loader";
	private static final String IMAGE = "image";
	private static final String ATT_EXTENSION = "extension";
	private static final String ATT_CLASS = "class";

	private static Map<String, IImageLoader> cache = new HashMap<String, IImageLoader>();
	private static Class imageClass;

	/**
	 * No instantiation.
	 */
	private ImageExtensionManager() {
	}

	/**
	 * Creates a new player supporting the given file extension.
	 * 
	 * @param contentType the content type used for lookup.
	 * @return a new player instance if it supports the file extension.
	 */
	public synchronized static IImageLoader getLoaderByExtension(String ext) {
		if (ext == null) {
			return null;
		}
		ext = ext.toLowerCase();
		IImageLoader loader = cache.get(ext);
		if (loader != null) {
			return loader;
		}
		if (Platform.getExtensionRegistry() == null) {
			// no runtime, queries the system properites
			String className = System.getProperty(ext);
			if (!StringUtils.isEmpty(className)) {
				try {
					loader = (IImageLoader) Class.forName(className)
							.newInstance();
					cache.put(ext, loader);
					return loader;
				} catch (Throwable e) {
					Activator.error(e);
				}
			}
			return null;
		}
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(IMAGE_CONTRIBUTOR_ID);
		for (IExtension extension : point.getExtensions()) {
			for (IConfigurationElement element : extension
					.getConfigurationElements()) {
				if (LOADER.equals(element.getName())) {
					String type = element.getAttribute(ATT_EXTENSION);
					if (ext.equalsIgnoreCase(type)) {
						try {
							IImageLoader imageLoader = (IImageLoader) element
									.createExecutableExtension(ATT_CLASS);
							cache.put(ext, imageLoader);
							return imageLoader;
						} catch (Throwable e) {
							Activator.error(e);
						}
					}
				}
			}
		}
		return null;
	}

	public synchronized static CoreImage createImage() {
		if (imageClass != null) {
			try {
				return (CoreImage) imageClass.newInstance();
			} catch (Throwable e) {
				Activator.error(e);
				return new CoreImage();
			}
		}
		if (Platform.getExtensionRegistry() == null) {
			
			imageClass = CoreImage.class;
			return createImage();
		}
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(IMAGE_CONTRIBUTOR_ID);
		for (IExtension extension : point.getExtensions()) {
			for (IConfigurationElement element : extension
					.getConfigurationElements()) {
				if (IMAGE.equals(element.getName())) {
					try {
						CoreImage image = (CoreImage) element
								.createExecutableExtension(ATT_CLASS);
						imageClass = image.getClass();
						break;
					} catch (Throwable e) {
						Activator.error(e);
					}
				}
			}
		}
		if (imageClass == null) {
			imageClass = CoreImage.class;
		}
		return createImage();
	}
}
