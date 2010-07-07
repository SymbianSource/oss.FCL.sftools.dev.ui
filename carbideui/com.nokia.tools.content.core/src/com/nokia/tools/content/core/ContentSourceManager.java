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
 * This class provides useful static methods for retrieving registered content
 * providers.
 */
public final class ContentSourceManager extends AbstractContentSourceManager {

	private static ContentSourceManager globalInstance;

	/**
	 * No instantiation.
	 */
	private ContentSourceManager() {
	}

	/**
	 * Access to global instance  
	 * @return
	 */
	public static ContentSourceManager getGlobalInstance() {
		if (null == globalInstance)
			globalInstance = new ContentSourceManager();
		return globalInstance;
	}

	/**
	 * Providing the access to global level content provider for purpose of
	 * accessing attributes
	 * 
	 * @param type
	 * @return
	 */
	public static IContentProvider getGlobalContentProvider(String type) {
		return getGlobalInstance().getContentProvider(type);
	}
}
