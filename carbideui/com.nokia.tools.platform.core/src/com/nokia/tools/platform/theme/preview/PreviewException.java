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
package com.nokia.tools.platform.theme.preview;

import com.nokia.tools.platform.theme.ThemeConstants;
import com.nokia.tools.platform.theme.ThemeException;

/**
 * The class defines the exception thrown while generating the preview screen
 * 
 */
public class PreviewException extends ThemeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = ThemeConstants.SERIAL_ID;

	/**
	 * Constructor
	 * 
	 * @param message Error message
	 */
	public PreviewException(String message) {
		super(message);
	}

	public PreviewException(Throwable e) {
		super(e);
	}
}