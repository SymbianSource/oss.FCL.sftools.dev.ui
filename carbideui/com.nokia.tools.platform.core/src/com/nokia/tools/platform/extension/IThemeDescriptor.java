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

package com.nokia.tools.platform.extension;

import java.net.URL;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;

import com.nokia.tools.platform.core.IDevice;
import com.nokia.tools.platform.core.IPlatform;

public interface IThemeDescriptor {
	String getId();

	String getContentType();

	String getContainerId();

	String getLayoutSet();

	String getDefaultDeviceId();

	IDevice getDefaultDevice();
	
	IPlatform getDefaultPlatform();

	IPlatform[] getPlatforms();

	IThemeDesignDescriptor[] getDesigns();

	URL[] getPreviewPaths();

	URL[] getIdMappingPaths();

	URL[] getDimensions();

	URL[] getSettings();

	Map<String, String> getSoundFormats();

	IThemeLayoutGroupDescriptor[] getLayoutGroupDescriptors();

	IThemeLayoutGroupDescriptor getLayoutGroupDescriptor(IDevice device);
	
	ImageDescriptor getLargeIconDescriptor();
	
	ImageDescriptor getSmallIconDescriptor();
	
	String getDescription();

    URL getExtendedDefaultDesignPath();

    URL getExtendedDefaultPreviewPath();
}
