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
 * This exception class is thrown in case of any exception once the processor is started.
 * doesnt really care about the place and reason for it.
 *
 */
public class ProcessorException extends CommandLineException{

	private static final long serialVersionUID = -2727430442376734959L;

	public ProcessorException(String message) {
		super(message);
	}

}
