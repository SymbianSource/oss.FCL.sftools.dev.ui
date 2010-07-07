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
package com.nokia.tools.packaging;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PackagingPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "com.nokia.tools.packaging";

	// The shared instance.
	private static PackagingPlugin plugin;

	/**
	 * The constructor.
	 */
	public PackagingPlugin() {
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
	public static PackagingPlugin getDefault() {
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
		return AbstractUIPlugin.imageDescriptorFromPlugin(getDefault()
				.getBundle().getSymbolicName(), path);
	}

	/**
	 * Logs the INFO level message.
	 * 
	 * @param message the log message.
	 */
	public static void info(String message) {
		log(new Status(IStatus.INFO,
				getDefault().getBundle().getSymbolicName(), IStatus.INFO,
				message, null));
	}

	/**
	 * Logs the ERROR level message.
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
	 */
	public static void error(String message) {
		error(message, null);
	}

	/**
	 * Logs the ERROR level message.
	 * 
	 * @param message the error message.
	 * @param e the throwable.
	 */
	public static void error(String message, Throwable e) {
		log(new Status(IStatus.ERROR, getDefault().getBundle()
				.getSymbolicName(), IStatus.ERROR, message, e));
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
}
