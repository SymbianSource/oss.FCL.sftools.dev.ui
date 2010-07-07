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
 * Generic exception class for the tool
 * 
 * @author surmathe
 */
public class CommandLineException extends Exception {
	private static final long serialVersionUID = 8237715186433646413L;

	StringBuffer errMsg = new StringBuffer("");

	public CommandLineException(String message) {
		super(message);
		errMsg.append(message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#toString() to print it out on the console
	 */
	public String toString() {
		return errMsg.toString();
	}

	/**
	 * @param str
	 *            appends the parameter to the error message
	 */
	public void appendToMessage(String str) {
		errMsg.append(str);
	}
}
