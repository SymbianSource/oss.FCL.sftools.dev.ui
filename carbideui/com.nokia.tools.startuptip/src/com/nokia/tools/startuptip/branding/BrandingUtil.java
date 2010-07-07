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

package com.nokia.tools.startuptip.branding;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.nokia.tools.startuptip.Settings;
import com.nokia.tools.startuptip.StartupTipPlugin;

/**
 * Utility class for startup tip dialog branding.
 * 
 */
public class BrandingUtil {

	public static Image getDialogTitleBarImage() {
		ImageDescriptor iDesc = StartupTipPlugin
				.getImageDescriptor(Settings.STARTUP_TIP_DIALOG_TITLEBAR_IMAGE);
		return iDesc.createImage();
	}
}
