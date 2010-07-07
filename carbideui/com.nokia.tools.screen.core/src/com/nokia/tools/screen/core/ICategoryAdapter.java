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
 * This adapter is used for retrieving the content data that is category of the
 * host data.
 */
public interface ICategoryAdapter {
	/**
	 * @return the categorized peer or null if no such category exists.
	 */
	IContentData[] getCategorizedPeers();
}
