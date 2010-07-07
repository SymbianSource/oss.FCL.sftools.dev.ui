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

package com.nokia.tools.packaging.commandline.util;

/**
 *
 * @author Bhanu
 * This exception class is thrown in case of missing mandatory parameter passed to the tool
 *
 */
public class MissingParameterException extends CommandLineException{
	private static final long serialVersionUID = -1653147108836290933L;

	public MissingParameterException(String message) {
		super(message);
	}

}
