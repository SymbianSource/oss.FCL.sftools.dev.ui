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
package com.nokia.tools.content.core;

/**
 * This exception will be thrown when the content operation fails.
 *
 */
public class ContentException extends Exception {
	static final long serialVersionUID = -3700190636334369554L;

	/**
	 * Constructor.
	 * 
	 * @param msg
	 *            the short explanation.
	 */
	public ContentException(String msg) {
		super(msg);
	}

	/**
	 * Constructor.
	 * 
	 * @param msg
	 *            the short explanation.
	 * @param e
	 *            the underlying cause.
	 */
	public ContentException(String msg, Throwable e) {
		super(msg, e);
	}

	/**
	 * Constructor.
	 * 
	 * @param e
	 *            the underlying cause.
	 */
	public ContentException(Throwable e) {
		super(e);
	}
}
