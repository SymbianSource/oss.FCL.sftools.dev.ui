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
 * This adapter is used for retrieving the content that is managed by the
 * enclosing part and also adding/removing listeners.
 * 
  */
public interface IContentAdapter extends IContentService {
	/**
	 * @return the contents being managed.
	 */
	IContent[] getContents();

	/**
	 * @param type the content type.
	 * @return the contents of the given type
	 */
	IContent[] getContents(String type);

	/**
	 * Called when there is change made directly to the content. The client may
	 * act by reloading content.
	 * 
	 * @param hint the hint that is related to the content change, can be null.
	 */
	void updateContent(Object hint);
}
