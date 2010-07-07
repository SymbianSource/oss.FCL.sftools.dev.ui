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
package com.nokia.tools.ui.dialog;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

/**
 * Interface for the resource manager UI that uses the resource pages
 * {@link IResourceSelectionPage}.
 * 
 */
public interface IResourcePageManager {
	/**
	 * Icon size.
	 */
	int ICON_SIZE = 24;

	/**
	 * Preview size - small
	 */
	int PREVIEW_SMALL_SIZE = 90;

	/**
	 * Preview size - medium
	 */
	int PREVIEW_LARGER_SIZE = 150;

	/**
	 * Preview size - large
	 */
	int PREVIEW_BIG_SIZE = 210;

	/**
	 * Checks if the mouse hovering is enabled for the specific page.
	 * 
	 * @param page the page to check.
	 * @return true if the hovering is enabled, false otherwise.
	 */
	boolean isHoverEnabled(IResourceSelectionPage page);

	/**
	 * Updates the layout of the control.
	 * 
	 * @param control the control to update.
	 */
	void updateLayout(Control control);

	/**
	 * Called when a set of resources are selected.
	 * 
	 * @param resources selected resources.
	 */
	void resourcesSelected(Object[] resources);

	/**
	 * Called when a set of resources are opened.
	 * 
	 * @param resources opened resources.
	 */
	void resourcesOpened(Object[] resources);

	/**
	 * Tests if the given resource is currently selected.
	 * 
	 * @param resource the resource to test.
	 * @return true if the given resource is selected, false otherwise.
	 */
	boolean isResourceSelected(Object resource);

	/**
	 * Refreshes this UI.
	 */
	void refresh();

	/**
	 * @return all registered resource pages.
	 */
	IResourceSelectionPage[] getPages();

	/**
	 * @return the current focused resource page.
	 */
	IResourceSelectionPage getCurrentPage();

	/**
	 * @return the default resource image, i.e. used when there is no associated
	 *         image for the resource.
	 */
	Image getDefaultResourceImage();

	/**
	 * Creates a new image.
	 * 
	 * @param data the resource data.
	 * @param width width of the image.
	 * @param height height of the image.
	 * @param keepAspectRatio true to keep aspect ratio, false for stretching.
	 * @return the created image.
	 */
	Image createImage(Object data, int width, int height,
			boolean keepAspectRatio);

	/**
	 * Enable or disable the preview check box.
	 * 
	 * @param enable true for enabled, false otherwise.
	 */
	void enablePreviewCheckBox(boolean enable);
}
