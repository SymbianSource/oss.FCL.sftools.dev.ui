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

import org.eclipse.jface.resource.ImageDescriptor;

import com.nokia.tools.platform.theme.IThemeManager;

/**
 * Descriptor for the theme container. A theme container has one
 * {@link IThemeManager}, which is responsible for managing themes. One theme
 * container can be shared by multiple theme systems if they use the same
 * models.
 * 
 */
public interface IThemeContainerDescriptor {
	/**
	 * @return the container id.
	 */
	String getId();

	/**
	 * @return the theme manager instance.
	 */
	IThemeManager createManager();

	String getName();

	String getDescription();

	ImageDescriptor getLargeIconDescriptor();

	ImageDescriptor getSmallIconDescriptor();
	
	boolean isDefault();
}
