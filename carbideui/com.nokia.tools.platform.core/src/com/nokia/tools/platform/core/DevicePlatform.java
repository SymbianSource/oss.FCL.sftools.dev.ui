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
package com.nokia.tools.platform.core;

import com.nokia.tools.platform.extension.PlatformExtensionManager;


/**
 * This class defines the common platforms and also provides the static methods
 * for retrieving platform information.
 */
public class DevicePlatform {
	private static IPlatform[] platforms;

	public static final IPlatform S60_5_0 = getPlatformById("s60_5.0");
	
	public static final IPlatform SF_2 = getPlatformById("Symbian^2");
	
	public static final IPlatform[] SUPPORTED_S60_PLATFORMS = { S60_5_0, SF_2 };

	/**
	 * Unspecified platform.
	 */
	public static final IPlatform UNSPECIFIED_PLATFORM = new IPlatform() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IPlatform#getId()
		 */
		public String getId() {
			return "UNSPECIFIED";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IPlatform#getName()
		 */
		public String getName() {
			return "UNSPECIFIED PLATFORM";
		}

	};

	/**
	 * No instantiation.
	 */
	private DevicePlatform() {
	}

	/**
	 * @return all registered platforms.
	 */
	public synchronized static IPlatform[] getPlatforms() {
		if (platforms == null) {
			platforms = PlatformExtensionManager.getPlatforms();
		}
		return platforms;
	}

	/**
	 * @return all devices.
	 */
	public static IDevice[] getDevices() {
		return PlatformExtensionManager.getDevices();
	}

	/**
	 * Finds platform by name (case insensitive).
	 * 
	 * @param name name of the platform.
	 * @return the platform with the specific name or
	 *         {@link #UNSPECIFIED_PLATFORM} if not found.
	 */
	public static IPlatform getPlatformByName(String name) {
		if (name != null) {
			for (IPlatform platform : getPlatforms()) {
				if (name.equalsIgnoreCase(platform.getName())) {
					return platform;
				}
			}
		}
		return UNSPECIFIED_PLATFORM;
	}

	/**
	 * Finds platform by id.
	 * 
	 * @param id id of the platform.
	 * @return the platform with the specific id or
	 *         {@link #UNSPECIFIED_PLATFORM} if not found.
	 */
	public static IPlatform getPlatformById(String id) {
		if (id != null) {
			for (IPlatform platform : getPlatforms()) {
				if (id.equals(platform.getId())) {
					return platform;
				}
			}
		}
		return UNSPECIFIED_PLATFORM;
	}
}
