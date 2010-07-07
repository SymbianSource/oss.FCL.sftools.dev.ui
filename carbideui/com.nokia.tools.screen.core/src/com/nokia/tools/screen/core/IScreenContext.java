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
package com.nokia.tools.screen.core;

import org.eclipse.core.runtime.IAdaptable;

import com.nokia.tools.platform.core.Display;

/**
 * This class provides the necessary information about the current screen
 * rendering context, such as resolution etc. and will be used by the
 * {@link IScreenAdapter}.
 * 
 */
public interface IScreenContext extends IAdaptable {
	/**
	 * Returns the current display.
	 * 
	 * @return the current display.
	 */
	Display getDisplay();
}
