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
 * An exception to indicate the problem of storing of the Third Party Icons
 * to xml data contents present in either stored to .icons file or the icons.xml file.
 * This exception will be thrown from the store* methods defined in ThirdPartyIconManager. 
 */

public class ThirdPartyIconStoreException extends Exception {
	public ThirdPartyIconStoreException(String message) {
		super(message);
	}

	public ThirdPartyIconStoreException(Throwable cause) {
		super(cause);
	}
	
	public ThirdPartyIconStoreException(String message, Throwable cause) {
		super(message, cause);
	}

}
