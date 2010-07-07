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

import org.eclipse.osgi.util.NLS;

/**
 * NLS messages.
 *
 */
public class CoreMessages extends NLS {
	/**
	 * Error message - content provider failed to initialize.
	 */
	public static String Error_InstantiateContentProvider;
	/**
	 * Error message - the project is not accessible.
	 */
	public static String Project_NotAccessible;
	/**
	 * Error message - opening project failed.
	 */
	public static String Error_Opening_Project;
	/**
	 * Error message 
	 * 	 */
	public static String Project_Not_S60_Data;

	static {
		NLS
				.initializeMessages(CoreMessages.class.getName(),
						CoreMessages.class);
	}
}
