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

package com.nokia.tools.ui.resource;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.nokia.tools.ui.Activator;

public class SharedImages {
	/**
	 * A 24x24 Icon representing the Zoom Tool
	 */
	public static final ImageDescriptor DESC_ZOOM_24;

	/**
	 * A 16x16 Icon representing the Zoom Tool
	 */
	public static final ImageDescriptor DESC_ZOOM_16;

	public static final ImageDescriptor HAND_16;

	public static final ImageDescriptor HAND_24;

	public static final ImageDescriptor HAND_CLOSED_24;

	public static final ImageDescriptor DESC_ZOOM_IN = createAndCache("icons/zoomplus.gif");
	public static final ImageDescriptor DESC_ZOOM_OUT = create("icons/zoomminus.gif");

	public static final ImageDescriptor DESC_ZOOM_100 = createAndCache("icons/zoom100.gif");
	public static final ImageDescriptor DESC_ZOOM_TO_FIT = create("icons/zoomtofit.gif");

	public static final ImageDescriptor DESC_ZOOM_1_TO_1 = create("icons/zoom1to1.png");

	static {
		DESC_ZOOM_24 = createDescriptor("icons/icon_magnify_24.gif"); //$NON-NLS-1$
		DESC_ZOOM_16 = createDescriptor("icons/icon_magnify_16.gif"); //$NON-NLS-1$
		HAND_16 = createDescriptor("icons/icon_hand_tool_16.png"); //$NON-NLS-1$
		HAND_24 = createDescriptor("icons/icon_hand_tool_24.png"); //$NON-NLS-1$
		HAND_CLOSED_24 = createDescriptor("icons/icon_hand_tool_closed_24.png"); //$NON-NLS-1$
	}

	private static ImageDescriptor createDescriptor(String filename) {
		return Activator.getImageDescriptor(filename);
	}

	private static ImageDescriptor create(String imageName) {
		return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
		    imageName);
	}

	private static ImageDescriptor createAndCache(String imageName) {
		ImageDescriptor result = create(imageName);
		Activator.getDefault().getImageRegistry().put(imageName, result);
		return result;
	}

	public static Image get(String imageName) {
		return Activator.getDefault().getImageRegistry().get(imageName);
	}

}
