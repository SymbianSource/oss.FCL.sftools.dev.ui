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
package com.nokia.tools.packaging;

/**
 * Constants used in the packaging process.
 * 
 */
public interface PackagingConstants {
	/**
	 * Plugin id.
	 */
	String PLUGIN_ID = "com.nokia.tools.packaging";

	/**
	 * Key generation algorithm - DSA
	 */
	String ALGORITHM_DSA = "DSA";
	/**
	 * Key generation algorithm - RSA
	 */
	String ALGORITHM_RSA = "RSA";

	/**
	 * Flag enables halt in packaging just before running makesis It enables
	 * changing of pkg file for testing and debugging purposes By default, the
	 * false is used.
	 * <h4>Possible values:</h4>
	 * <ul>
	 * <li><code>true</code> - Packaging will break with possibility to
	 * continue</li>
	 * <li><code>false or non true</code> - no break before makesis invoked</li>
	 * </ul>
	 */
	String PREF_BREAK_PACKAGING_BEFORE_MAKESIS = PLUGIN_ID + ".makesis.break";
}
