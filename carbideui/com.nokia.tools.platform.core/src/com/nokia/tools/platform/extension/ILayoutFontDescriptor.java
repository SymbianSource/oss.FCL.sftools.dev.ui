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
package com.nokia.tools.platform.extension;

import java.net.URL;
import java.util.Map;

/**
 * Font descriptor used for mapping layout font ids to the actual system fonts.
 * 
 */
public interface ILayoutFontDescriptor {
	/**
	 * @return the path to the font specification file.
	 */
	URL getFontPath();

	/**
	 * @return the default font name.
	 */
	String getDefaultFont();

	/**
	 * @return the font mappings, where key is the layout font id and value the
	 *         system font name.
	 */
	Map<String, String> getFontMappings();
}
