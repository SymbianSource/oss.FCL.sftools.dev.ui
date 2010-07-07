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

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

import com.nokia.tools.resource.util.FileUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.nokia.tools.resource.core";

	// The shared instance
	private static Activator plugin;

	private BundleContext context;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		this.context = context;
		// ensures the system temp directory exists
		FileUtils.getTemporaryDirectory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		FileUtils.performCleanup();
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
	 * Finds bundle with the id.
	 * 
	 * @param id id of the bundle.
	 * @return the bundle.
	 */
	public Bundle find(long id) {
		if (context != null) {
			return context.getBundle(id);
		}
		return null;
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
	 * Notifies that the bundle is active. The bundle must have been copied to
	 * the proper place before calling this method.
	 * 
	 * @param fileName name of the bundle file, can be a directory.
	 * @throws BundleException if bundle installation failed.
	 */
	public void installBundle(String fileName) throws BundleException {
		String location = "update@plugins/" + fileName;
		Bundle bundle = null;
		try {
			URL bundleURL = new URL("reference:file:plugins/" + fileName); //$NON-NLS-1$
			bundle = context.installBundle(location, bundleURL.openStream());
		} catch (Exception e) {
			throw new BundleException(e.getMessage());
		}
		if (bundle == null) {
			throw new BundleException("Bundle installation failed: " + location);
		}
		refreshPackages(new Bundle[] { bundle });
	}

	/**
	 * Notifies a bundle has been removed.
	 * 
	 * @param bundle the bundle to be uninstalled.
	 * @throws BundleException if bundle uninstallation failed.
	 */
	public void uninstallBundle(Bundle bundle) throws BundleException {
		bundle.uninstall();
		refreshPackages(new Bundle[] { bundle });
	}

	/**
	 * Copied from the update plugin. <br/>Do PackageAdmin.refreshPackages() in
	 * a synchronous way. After installing all the requested bundles we need to
	 * do a refresh and want to ensure that everything is done before returning.
	 * 
	 * @param bundles bundles to be refreshed.
	 */
	private void refreshPackages(Bundle[] bundles) {
		if (bundles.length == 0)
			return;
		ServiceReference packageAdminRef = context
				.getServiceReference(PackageAdmin.class.getName());
		PackageAdmin packageAdmin = null;
		if (packageAdminRef != null) {
			packageAdmin = (PackageAdmin) context.getService(packageAdminRef);
			if (packageAdmin == null)
				return;
		}

		final boolean[] flag = new boolean[] { false };
		FrameworkListener listener = new FrameworkListener() {
			public void frameworkEvent(FrameworkEvent event) {
				if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED)
					synchronized (flag) {
						flag[0] = true;
						flag.notifyAll();
					}
			}
		};
		context.addFrameworkListener(listener);
		packageAdmin.refreshPackages(bundles);
		synchronized (flag) {
			while (!flag[0]) {
				try {
					flag.wait();
				} catch (InterruptedException e) {
				}
			}
		}
		context.removeFrameworkListener(listener);
		context.ungetService(packageAdminRef);
	}
}
