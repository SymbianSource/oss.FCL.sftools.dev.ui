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

import com.nokia.tools.platform.core.IDevice;

/**
 * Descriptor for the layout contribution.
 * 
 */
public interface ILayoutDescriptor {
	/**
	 * @return the unique identifier
	 */
	String getId();

	/**
	 * @return the descriptive name
	 */
	String getName();

	/**
	 * @return the device id
	 */
	String getDeviceId();

	/**
	 * @return the device with the defined id, or null if no such device can be
	 *         found.
	 */
	IDevice getDevice();

	/**
	 * @return all variant descriptors.
	 */
	ILayoutVariantDescriptor[] getVariants();

	/**
	 * Returns the variant descriptor for the given variant id.
	 * 
	 * @param id
	 *            id of the variant.
	 * @return the variant descriptor.
	 */
	ILayoutVariantDescriptor getVariant(String id);
}
