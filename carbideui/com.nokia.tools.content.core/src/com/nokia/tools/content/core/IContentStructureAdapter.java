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
 * Adapter used to return the theme resources, main purpose is to decouple the
 * domain model structure from content internal structures
 * {@link IContentData#getChildren()}.
 */
public interface IContentStructureAdapter {
	/**
	 *  
	 * @return the logical children, maybe a subset or superset of the actual
	 *         content data.
	 */
	IContentData[] getChildren();
}
