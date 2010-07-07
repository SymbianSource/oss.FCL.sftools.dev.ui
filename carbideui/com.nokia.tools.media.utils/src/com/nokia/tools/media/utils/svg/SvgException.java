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
package com.nokia.tools.media.utils.svg;

public class SvgException extends Exception {

	private static final long serialVersionUID = 936048329769734079L;

	/**
	 * Constructor
	 */
	// Added to wrap another lower level exception - uma
	public SvgException(Throwable ex) {
		super(ex);
	}

	public SvgException() {
		super();
	}

	public SvgException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * Constructor
	 * 
	 * @param message
	 *            Error message
	 */
	public SvgException(String message) {
		super(message);
	}

	/**
	 * Overloaded toString function
	 * 
	 * @return Details about the exception.
	 */
	public String toString() {
		return "File parsing exception " + getMessage() + "\nStack trace:\n"
				+ getStackTrace();
	}
}
