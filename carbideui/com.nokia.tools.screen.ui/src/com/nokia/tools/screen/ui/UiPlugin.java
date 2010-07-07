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
package com.nokia.tools.screen.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.nokia.tools.screen.ui.propertysheet.tabbed.ICellEditorFactory;

/**
 * The main plugin class to be used in the desktop.
 */
public class UiPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "com.nokia.tools.screen.ui";

	private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");

	// The shared instance.
	private static UiPlugin plugin;
	private ICellEditorFactory celleditorFactory;

	/**
	 * The constructor.
	 */
	public UiPlugin() {
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
	public static UiPlugin getDefault() {
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
	 * Returns an image descriptor for the image file that is for the given
	 * status.
	 * 
	 * @param name name of the image.
	 * @param isEnabled true for the image in the enabled status, false
	 *            otherwise.
	 * @return the image descriptor for the give image file in the given status.
	 */
	public static ImageDescriptor getIconImageDescriptor(String name,
			boolean isEnabled) {
		return getImageDescriptor("icons/" + (isEnabled ? "e" : "d")
				+ "tool16/" + name);
	}

	public static void info(String message) {
		log(new Status(IStatus.INFO, PLUGIN_ID, IStatus.INFO, message, null));
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

	// for advanced properties view
	public CellEditor getCellEditor(String className, Composite parent) {
		className = className.equalsIgnoreCase("boolean") ? "java.lang.Boolean"
				: className;
		if (celleditorFactory == null) {
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
					.getExtensionPoint(
							"com.nokia.tools.screen.ui.advancedproperties");
			IExtension[] extensions = extensionPoint.getExtensions();
			IConfigurationElement[] configurationElements = extensions[0]
					.getConfigurationElements();

			try {
				Object createExecutableExtension = configurationElements[0]
						.createExecutableExtension("class");
				celleditorFactory = (ICellEditorFactory) createExecutableExtension;
			} catch (CoreException e) {
				// handle exception
			}

		}
		return celleditorFactory.getCellEditor(className, parent);

	}
}
