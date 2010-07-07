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
package com.nokia.tools.carbide.ui.themeedition;

import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.nokia.tools.resource.util.FileUtils;

/**
 * The main plugin class, plugin is used as top level Theme Edition product
 */
public class ThemeeditionPlugin extends AbstractUIPlugin implements IStartup {
	// The shared instance.
	private static ThemeeditionPlugin plugin;

	/**
	 * The constructor.
	 */
	public ThemeeditionPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static ThemeeditionPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(getDefault()
				.getBundle().getSymbolicName(), path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {
		String productName = null;
		InputStream in = null;
		try {
			Properties props = new Properties();
			in = FileUtils.getURL(getBundle(), "plugin.properties")
					.openStream();
			props.load(in);
			productName = props.getProperty("carbide.ui.theme.edition")
					+ Platform.getProduct().getProperty("versionText")
							.substring(8);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
			}
		}
		if (productName == null) {
			System.exit(1);
		}
	}
}
