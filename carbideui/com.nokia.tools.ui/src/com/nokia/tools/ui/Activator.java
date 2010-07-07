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
package com.nokia.tools.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.nokia.tools.ui";

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
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

}
