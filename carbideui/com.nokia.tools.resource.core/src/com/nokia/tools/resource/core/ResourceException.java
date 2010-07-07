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
package com.nokia.tools.resource.core;

/**
 * A <code>ResourceException</code> is thrown if any resource issues 
 * happened during the operation
 */
public class ResourceException extends Exception{
 
	String errorMessage;
	
	public ResourceException(String message)
	{
		errorMessage = message;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	
}
