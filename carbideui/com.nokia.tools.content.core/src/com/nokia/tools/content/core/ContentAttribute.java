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
package com.nokia.tools.content.core;

/**
 * This enumeration holds common attributes whose names can be used as the keys
 * in {@link IContentData#setAttribute(String, Object)} and
 * {@link IContentData#getAttribute(String)}.
 */
public enum ContentAttribute {
	/**
	 * Screen display: com.nokia.tools.platform.core.Display
	 */
	DISPLAY,
	/**
	 * Element bounds, type: java.awt.Rectangle
	 */
	BOUNDS,
	/**
	 * Application name
	 */
	APPLICATION_NAME,
	/**
	 * Model
	 */
	MODEL,
	/**
	 * Author
	 */
	AUTHOR,
	/**
	 * Author
	 */
	VENDOR_NAME,
	/**
	 * Author
	 */
	VENDOR_LOGO,
	/**
	 * Copyright notice
	 */
	COPYRIGHT,
	/**
	 * Allow copying?
	 */
	ALLOW_COPYING,
	/**
	 * Platform
	 */
	PLATFORM,
	/**
	 * 24 pixel support
	 */
	BITS_24PIXEL_SUPPORT,
	/**
	 * DRM protection
	 */
	DRM_PROTECTION,
	/**
	 * Included in packaging?
	 */
	PACKAGING,
	/**
	 * Packaging task
	 */
	PACKAGING_TASK,
	/**
	 * Packaing resource
	 */
	PACKAGING_RESOURCE,
	/**
	 * Id as returned with getId()
	 */
	ID,
	/**
	 * Name as returned with getName()
	 */
	NAME,
	/**
	 *
	 */
	MODIFIED,
	/**
	 * Project: IProject
	 */
	PROJECT,
	/**
	 * Application uid
	 */
	APP_UID,
	/**
	 * Provider uid
	 */
	PROVIDER_UID,
	/**
	 * Theme uid
	 */
	THEME_UID,
	/**
	 * Version
	 */
	VERSION,
	/**
	 * Text
	 */
	TEXT,
	/**
	 * Image url
	 */
	IMAGE_PATH,
	/**
	 * Mask url
	 */
	MASK_PATH,
	/**
	 * Theme id
	 */
	THEME_ID,
	
	/** This marker is introduces in order to differrentiate the changes made by user and the changes made by optimalization packager */
	MODIFIED_BEFORE_THEME_OPTIMALIZTION;
	
	
}
