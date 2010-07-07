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
package com.nokia.tools.media.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class UtilsPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "com.nokia.tools.media.utils";

	// The shared instance.
	private static UtilsPlugin plugin;

	/**
	 * The constructor.
	 */
	public UtilsPlugin() {
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
	public static UtilsPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Logs the INFO level message.
	 * 
	 * @param message the log message.
	 */
	public static void info(String message) {
		log(new Status(IStatus.INFO, PLUGIN_ID, IStatus.INFO, message, null));
	}

	/**
	 * Logs the WARNING level message.
	 * 
	 * @param message the error message.
	 */
	public static void warn(String message) {
		warn(message, null);
	}

	/**
	 * Logs the WARNING level messag.e
	 * 
	 * @param e the error cause.
	 */
	public static void warn(Throwable e) {
		warn("", e);
	}

	/**
	 * Logs the WARNING level message.
	 * 
	 * @param message the warning message.
	 * @param e the throwable.
	 */
	public static void warn(String message, Throwable e) {
		log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.WARNING, message, e));
	}

	/**
	 * Logs the ERROR level message.
	 * 
	 * @param message the error message.
	 */
	public static void error(String message) {
		error(message, null);
	}

	/**
	 * Logs the ERROR level messag.e
	 * 
	 * @param e the error cause.
	 */
	public static void error(Throwable e) {
		error("", e);
	}

	/**
	 * Logs the ERROR level message.
	 * 
	 * @param message the error message.
	 * @param e the throwable.
	 */
	public static void error(String message, Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, e));
	}

	/**
	 * Logs a status.
	 * 
	 * @param status the status.
	 */
	public static void log(IStatus status) {
		if (getDefault() != null) {
			getDefault().getLog().log(status);
		} else {
			if (status.getMessage() != null) {
				System.out.println(PLUGIN_ID + ": " + status.getMessage());
			}
			if (status.getException() != null) {
				status.getException().printStackTrace();
			}
		}
	}

	public static String getStretchModeLabel(String stretch) {
		if (IMediaConstants.STRETCHMODE_ASPECT.equals(stretch))
			return IMediaConstants.STRETCHMODE_ASPECT_LBL;
		if (IMediaConstants.STRETCHMODE_STRETCH.equals(stretch))
			return IMediaConstants.STRETCHMODE_STRETCH_LBL;
		if (IMediaConstants.STRETCHMODE_NORMAL.equals(stretch))
			return IMediaConstants.STRETCHMODE_NORMAL_LBL;
		return stretch;
	}

	public File getPluginFolder() throws IOException {
		URL url = getBundle().getEntry("/");
		url = FileLocator.toFileURL(url);
		return new File(url.getPath());
	}
}
