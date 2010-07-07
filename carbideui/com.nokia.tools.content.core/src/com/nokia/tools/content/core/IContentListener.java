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
 * Interface for listener classes that are interested to receive the content
 * related changes.
 */
public interface IContentListener {
	/**
	 * Called when the root content is changed.
	 * 
	 * @param content the new content.
	 */
	void rootContentChanged(IContent content);
	
	/**
	 * Called when the content is modified
	 * @param delta the change delta
	 */
	void contentModified ( IContentDelta delta );
}
