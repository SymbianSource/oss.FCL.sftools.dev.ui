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
package com.nokia.tools.s60.ide.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

import com.nokia.tools.s60.ide.IS60IDEConstants;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.ui.views.ViewIDs;

/**
*/
public class MobileCustomizationPerspective implements IPerspectiveFactory {
	public static final String NEW_WIZARD_EXTENSION_POINT_ID = PlatformUI.PLUGIN_ID
			+ "." + IWorkbenchRegistryConstants.PL_NEW;

	public static final String WIZARD_ELEMENT = "wizard";

	public static final String CATEGORY_ELEMENT = "category";

	public static final String ID_ATTR = "id";

	public static final String CARBIDE_NEW_PROJECT_WIZARD_CATEGORY = "com.nokia.tools.s60.mobile";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
		// Save current workbench state and size
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell();
		Boolean stateMaximized = shell.getMaximized();
		Point currentSize = shell.getSize();

		// Set workbench size to be 1024x768 until reseting is done
		shell.setSize(1024, 768);

		IFolderLayout navFolder = layout.createFolder("s60left",
				IPageLayout.LEFT, (float) 0.27, layout.getEditorArea());
		navFolder.addView(ViewIDs.RESOURCE_VIEW2_ID);
		navFolder.addView(IPageLayout.ID_RES_NAV);

		IFolderLayout leftbottomFolder = layout.createFolder("s60leftbottom",
				IPageLayout.BOTTOM, (float) 0.73, "s60left");
		leftbottomFolder.addView(IPageLayout.ID_OUTLINE);

		IFolderLayout rightFolder = layout.createFolder("s60right",
				IPageLayout.RIGHT, (float) 0.71, layout.getEditorArea());
		rightFolder.addView(IS60IDEConstants.ID_GALLERY_VIEW);
		rightFolder.addView(IS60IDEConstants.ID_COMPONENT_STORE_VIEW);

		IPreferenceStore prefs = S60WorkspacePlugin.getDefault()
				.getPreferenceStore();
		boolean firstLaunch = prefs
				.getBoolean(IS60IDEConstants.PREF_FIRST_TIME_LAUNCH);
		float propertiesViewRatio;
		float iconViewRatio;
		if (firstLaunch) {
			propertiesViewRatio = (float) 0.72;
			iconViewRatio = (float) 0.79;
			prefs.setValue(IS60IDEConstants.PREF_FIRST_TIME_LAUNCH, false);
		} else {
			propertiesViewRatio = (float) 0.70;
			iconViewRatio = (float) 0.76;
		}

		// Bottom
		IFolderLayout bottom = layout.createFolder(
				"s60bottom", IPageLayout.BOTTOM, propertiesViewRatio,
				layout.getEditorArea());
		bottom.addView(IPageLayout.ID_PROP_SHEET);
		bottom.addPlaceholder(IPageLayout.ID_TASK_LIST);
		bottom.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		bottom.addPlaceholder(IS60IDEConstants.ID_SEARCH_VIEW);

		IFolderLayout bottomMiddle = layout.createFolder(
				"s60bottommiddle", IPageLayout.BOTTOM, iconViewRatio,
				layout.getEditorArea());
		bottomMiddle.addView(IS60IDEConstants.ID_ICON_VIEW);

		// layers
		IFolderLayout bottomRight = layout.createFolder(
				"s60bottomright", IPageLayout.RIGHT, (float) 0.70,
				IPageLayout.ID_PROP_SHEET);
		bottomRight.addView(IS60IDEConstants.ID_LAYERS_VIEW);

		// Restore current workbench size or state
		if (!stateMaximized)
			shell.setSize(currentSize);
		shell.setMaximized(stateMaximized);

		setContentsOfShowViewMenu(layout);
	}

	/**
	 * Sets the intial contents of the "Show View" menu.
	 */
	protected void setContentsOfShowViewMenu(IPageLayout layout) {
		layout.addShowViewShortcut(ViewIDs.RESOURCE_VIEW2_ID);
		layout.addShowViewShortcut(IS60IDEConstants.ID_GALLERY_VIEW);
		layout.addShowViewShortcut(IS60IDEConstants.ID_ICON_VIEW);
		layout.addShowViewShortcut(IS60IDEConstants.ID_LAYERS_VIEW);
		layout.addShowViewShortcut(IS60IDEConstants.ID_COMPONENT_STORE_VIEW);
		layout.addShowViewShortcut(IS60IDEConstants.ID_SEARCH_VIEW);
		layout.addShowViewShortcut(IS60IDEConstants.ID_COLORS_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IPageLayout.ID_BOOKMARKS);

		IConfigurationElement[] elems = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(NEW_WIZARD_EXTENSION_POINT_ID);
		for (IConfigurationElement child : elems) {
			if (WIZARD_ELEMENT.equals(child.getName())) {
				String id = child.getAttribute(ID_ATTR);
				String category = child.getAttribute(CATEGORY_ELEMENT);
				if (CARBIDE_NEW_PROJECT_WIZARD_CATEGORY.equals(category)) {
					layout.addNewWizardShortcut(id);
				}
			}
		}
	}
}
