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
package com.nokia.tools.screen.ui.actions;

import com.nokia.tools.content.core.IContentData;

/**
 * The content data that can be moved shall provide this interface.
 * 
 */
public interface IMovingAdapter {
	/**
	 * Moves the enclosing data in the given direction.
	 * 
	 * @param keyCode arrow key code.
	 * @return the data at the new location
	 */
	IContentData move(int keyCode);
}
