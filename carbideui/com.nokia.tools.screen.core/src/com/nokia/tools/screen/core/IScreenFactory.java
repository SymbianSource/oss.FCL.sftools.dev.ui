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

import com.nokia.tools.content.core.IContentData;

/**
 * Interface related to screen preview model manipulation relevant to creating
 * screens for elements that dont have a screen in static screen model.
 */
public interface IScreenFactory {

	/**
	 * create and returns screen that displays content data. <br> NOTE: method
	 * creates new screens, do not call it for enumeration of elements and for
	 * layout information Combined with the old findScreenThatContainsData by
	 * providing an additional flag.
	 * 
	 * @param data data to be displayed on the created screen
	 * @param createIfNotFound true will force adapting new element to the
	 *            screen, false may do nothing.<br/><b>Note: the behavior is
	 *            implementation dependant, using true only when necessary to
	 *            avoid performance hit.</b>
	 * @return IContentData for the screen. use getAdapter(IScreenAdapter.class)
	 *         to access screen.
	 */
	IContentData getScreenForData(IContentData data, boolean createIfNotFound);

	/**
	 * @return list of available screens, rather than relying on the internal
	 *         structure of the content children and test on the IScreenAdapter
	 *         existence
	 */
	IContentData[] getScreens();
}
