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
package com.nokia.tools.screen.ui.branding;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 */
public interface IBrandingManager {
	/**
	 * @return the banner image descriptor.
	 */
	ImageDescriptor getBannerImageDescriptor();

	/**
	 * The provided icon will be drawn on top of the banner image. The position
	 * will be determined by the branding manager.
	 * 
	 * @param icon the icon to be drawn.
	 * @return the descriptor for the composed image.
	 */
	ImageDescriptor getBannerImageDescriptor(ImageDescriptor icon);

	/**
	 * @return the icon image descriptor
	 */
	ImageDescriptor getIconImageDescriptor();

	/**
	 * Method to brand title area colors and fonts with fixed values.
	 * 
	 * @param shell the shell of the dialog/wizard
	 */
	void brandTitleArea(Shell shell);
	
	/**
	 * Get the  background color for all text areas/labels
	 * 
	 * @return - background color for all text areas/labels
	 */
	Color getBackgroundColor();
	
	/**
	 * Get the Foreground color and font for banner title
	 * 
	 * @return - Foreground color and font for banner title
	 */
	Color getForegroundTitleColor();
	
	/**
	 * Get the Foreground color and font for banner message
	 * 
	 * @return - Foreground color and font for banner message
	 */
	Color getForegroundMessageColor();
}
