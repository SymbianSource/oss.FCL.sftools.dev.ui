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

/**
 * Modeling specific device family or device
 */
public interface IDevice {
	/**
	 * @return the unique identifier.
	 */
	String getId();

	/**
	 * @return the descriptive name.
	 */
	String getName();

	/**
	 * @return the associated display.
	 */
	Display getDisplay();
}
