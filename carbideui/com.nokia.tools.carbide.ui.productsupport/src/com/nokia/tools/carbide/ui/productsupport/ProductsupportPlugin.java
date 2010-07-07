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

package com.nokia.tools.carbide.ui.productsupport;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.nokia.tools.carbide.ui.productsupport.perspectivehack.CarbideMenuCustomizer;
import com.nokia.tools.s60.ide.IS60IDEConstants;
import com.nokia.tools.ui.ide.ActionSetCustomizer;
import com.nokia.tools.ui.ide.IDEConstants;
import com.nokia.tools.ui.ide.MenuCustomizer;
import com.nokia.tools.ui.ide.PerspectiveHackManager;
import com.nokia.tools.ui.ide.ToolbarHider;

/**
 * The main plugin class to be used in the desktop.
 */
public class ProductsupportPlugin extends AbstractUIPlugin
    implements IDEConstants {

	// The shared instance.
	private static ProductsupportPlugin plugin;
	private PerspectiveHackManager perspectiveHackManager;
	private String MENU_SOFTWARE_UPDATES = "org.eclipse.equinox.p2.ui.sdk.update";

	/**
	 * The constructor.
	 */
	public ProductsupportPlugin() {
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		createPerspectiveManager();
		perspectiveHackManager.init();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		perspectiveHackManager.deinit();
		perspectiveHackManager = null;
		super.stop(context);
		plugin = null;
	}

	protected void createPerspectiveManager() {
		perspectiveHackManager = new PerspectiveHackManager();
		perspectiveHackManager
		    .setPerspectiveId(IS60IDEConstants.CARBIDE_UI_PERSPECTIVE_ID);
		ToolbarHider toolbarHider = new ToolbarHider();
		toolbarHider.addToolbarId(TOOLBAR_NEW_WIZARD_DROP_DOWN);
		toolbarHider.addToolbarId(TOOLBAR_BUILD_GROUP);
		toolbarHider.addToolbarId(TOOLBAR_BUILD);
		toolbarHider.addToolbarId(TOOLBAR_BUILD_EXT);
		toolbarHider.addToolbarGroup(TOOLBAR_GROUP_LAUNCH);
		toolbarHider.addToolbarGroup(TOOLBAR_GROUP_NAVIGATE);
		toolbarHider.addToolbarGroup(TOOLBAR_GROUP_SEARCH);
		toolbarHider.addToolbarGroup(TOOLBAR_GROUP_NAVIGATION);
		toolbarHider.addToolbarGroup(TOOLBAR_GROUP_WEBBROWSER);
		toolbarHider.addToolbarGroup(TOOLBAR_GROUP_WORKINGSET);
		ActionSetCustomizer actionSetCustomizer = new ActionSetCustomizer();
		actionSetCustomizer.addId(ACTION_SET_NAVIGATION);
		actionSetCustomizer.addId(ACTION_SET_EXTERNAL_TOOLS);
		actionSetCustomizer.addId(ACTION_SET_EXTERNAL_FILE);
		actionSetCustomizer.addId(ACTION_SET_LINE_DELIMITER);
		actionSetCustomizer.addId(ACTION_SET_WEBBROWSER);
		actionSetCustomizer.addId(ACTION_SET_KEYBINDINGS);
		actionSetCustomizer.addId(ACTION_SET_NAVIGATE);
		actionSetCustomizer.addId(ACTION_SET_SEARCH);
		actionSetCustomizer.addId(ACTION_SET_WORKINGSET);
		MenuCustomizer menuCustomizer = new CarbideMenuCustomizer();
		
		menuCustomizer.addHiddenMenuItem(MENU_HELP, MENU_SOFTWARE_UPDATES);
		menuCustomizer.addHiddenMenu(MENU_REFACTORING);
		menuCustomizer.addHiddenMenu(MENU_NAVIGATE);
		menuCustomizer.addHiddenMenu(MENU_SEARCH);
		menuCustomizer.addHiddenMenu(MENU_PROJECT);
		menuCustomizer.addHiddenMenu(MENU_ADDITIONS);
		menuCustomizer.addHiddenMenu(MENU_RUN);
		menuCustomizer.addHiddenMenuItem(MENU_WINDOW, MENU_WINDOW_NEWWINDOW);
		menuCustomizer.addHiddenMenuItem(MENU_WINDOW, MENU_WINDOW_NEWEDITOR);
		menuCustomizer.addHiddenMenuItem(MENU_WINDOW, MENU_WINDOW_WORKINGSET);
		menuCustomizer.addHiddenMenuItem(MENU_WINDOW,
		    MENU_WINDOW_OPENPERSPECTIVE);
		menuCustomizer.addHiddenMenuItem(MENU_WINDOW,
		    MENU_WINDOW_SAVEPERSPECTIVE);
		menuCustomizer.addHiddenMenuItem(MENU_WINDOW,
		    MENU_WINDOW_CLOSEPERSPECTIVE);
		menuCustomizer.addHiddenMenuItem(MENU_WINDOW,
		    MENU_WINDOW_CLOSEALLPERPECTIVE);
		menuCustomizer.addHiddenMenuItem(MENU_WINDOW,
		    MENU_WINDOW_CUSTOMIZEPERSPECTIVE);
		menuCustomizer.addHiddenMenuItem(MENU_WINDOW, MENU_WINDOW_NAVIGATION);
		menuCustomizer.addHiddenMenuItem(MENU_FILE, MENU_FILE_NEW);
		menuCustomizer.addHiddenMenuItem(MENU_FILE, MENU_FILE_OPEN_FILE);
		menuCustomizer.addHiddenMenuItem(MENU_FILE, MENU_FILE_REVERT);
		menuCustomizer.addHiddenMenuItem(MENU_FILE, MENU_FILE_MOVE);
		menuCustomizer.addHiddenMenuItem(MENU_FILE, MENU_FILE_RENAME);
		menuCustomizer.addHiddenMenuItem(MENU_FILE, MENU_FILE_REFRESH);
		menuCustomizer.addHiddenMenuItem(MENU_EDIT, MENU_EDIT_CUT);
		menuCustomizer.addHiddenMenuItem(MENU_EDIT, MENU_EDIT_SELECTALL);
		menuCustomizer.addHiddenMenuItem(MENU_EDIT, MENU_EDIT_FIND);
		
		menuCustomizer.addHiddenMenuItem(MENU_HELP, MENU_HELP_INSTALL);

		perspectiveHackManager.setToolbarHider(toolbarHider);
		perspectiveHackManager.setActionSetCustomizer(actionSetCustomizer);
		perspectiveHackManager.setMenuCustomizer(menuCustomizer);
	}

	/**
	 * @return the perspectiveHackManager
	 */
	public PerspectiveHackManager getPerspectiveHackManager() {
		return perspectiveHackManager;
	}

	/**
	 * Returns the shared instance.
	 */
	public static ProductsupportPlugin getDefault() {
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
	
	public void logException(Throwable exception){
		IStatus status = new Status(IStatus.ERROR,getBundle().getSymbolicName(), exception.getLocalizedMessage(), exception);
		getLog().log(status);
	}
}
