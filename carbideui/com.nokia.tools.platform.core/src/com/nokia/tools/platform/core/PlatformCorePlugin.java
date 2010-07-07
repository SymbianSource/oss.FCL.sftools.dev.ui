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
package com.nokia.tools.platform.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.nokia.tools.platform.layout.LayoutManager;
import com.nokia.tools.resource.util.DebugHelper;
import com.nokia.tools.resource.util.DebugHelper.SilentRunnable;

/**
 * The main plugin class to be used in the desktop.
 */
public class PlatformCorePlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "com.nokia.tools.platform.core";

	// The shared instance.
	private static PlatformCorePlugin plugin;

	/**
	 * The constructor.
	 */
	public PlatformCorePlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		SilentRunnable job = new SilentRunnable() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.ISafeRunnable#run()
			 */
			public void run() throws Exception {
				LayoutManager.loadCache();
			}
		};
		if (DebugHelper.debugPerformance()) {
			DebugHelper.debugTime(getClass(), "loading layout cache", job);
		} else {
			job.run();
		}
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		SilentRunnable job = new SilentRunnable() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.ISafeRunnable#run()
			 */
			public void run() throws Exception {
				LayoutManager.storeCache();
			}
		};
		if (DebugHelper.debugPerformance()) {
			DebugHelper.debugTime(getClass(), "storing layout cache", job);
		} else {
			job.run();
		}
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PlatformCorePlugin getDefault() {
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

	public static void info(String message) {
		log(new Status(IStatus.INFO, PLUGIN_ID, IStatus.INFO, message, null));
	}

	public static void warn(String message) {
		log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.WARNING, message,
				null));
	}

	public static void error(String message) {
		error(message, null);
	}

	public static void error(Throwable e) {
		error("", e);
	}

	public static void error(String message, Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, e));
	}

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
