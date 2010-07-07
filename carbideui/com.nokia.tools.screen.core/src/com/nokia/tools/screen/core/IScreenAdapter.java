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

import org.eclipse.core.runtime.IProgressMonitor;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.content.core.IContentDelta;

/**
 * This adapter can be associated with the {@link IContentData} and used to
 * build the screen elements. 
 */
public interface IScreenAdapter {
	/**
	 * Builds the screen that contains child elements, each element has a JEM
	 * widget object.
	 * 
	 * @param context the current screen context.
	 * @return the top-level screen element.
	 */
	IScreenElement buildScreen(IScreenContext context);

	/**
	 * update screen based on delta
	 * 
	 * @param delta
	 * @param monitor
	 * @return updated screen
	 */
	IScreenElement updateScreen(IContentDelta delta, IProgressMonitor monitor);

	/**
	 * Determines if the screen associated with this adapter is a model screen.
	 * 
	 * @return true if the screen associated with this adapter is a model
	 *         screen.
	 */
	boolean isModelScreen();
}
