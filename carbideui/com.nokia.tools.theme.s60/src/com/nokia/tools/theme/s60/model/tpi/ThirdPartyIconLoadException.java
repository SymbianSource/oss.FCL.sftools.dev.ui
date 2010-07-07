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
package com.nokia.tools.theme.s60.model.tpi;

/**
 * An exception to indicate the problem of loading of the Third Party Icons
 * from the xml data contents present in either the .icons file or the icons.xml file.
 * This exception will be thrown from the load* methods defined in ThirdPartyIconManager. 
 */

public class ThirdPartyIconLoadException extends Exception {

	public ThirdPartyIconLoadException(String message) {
		super(message);
	}

	public ThirdPartyIconLoadException(Throwable cause) {
		super(cause);
	}
	
	public ThirdPartyIconLoadException(String message, Throwable cause) {
		super(message, cause);
	}

}
