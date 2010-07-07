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
package com.nokia.tools.packaging;

/**
 * This exception will be thrown by the packaging processors when error occurred
 * in the packaging process and prevented the processors from continuing.
 * 
 */
public class PackagingException extends Exception {
	static final long serialVersionUID = -7606802949946614399L;

	private String details;

	/**
	 * Constructs an instance.
	 * 
	 * @param msg the short description.
	 */
	public PackagingException(String msg) {
		super(msg);
	}

	/**
	 * Constructs an instance.
	 * 
	 * @param msg the short description.
	 * @param e the actual error cause.
	 */
	public PackagingException(String msg, Throwable e) {
		super(msg, e);
	}

	/**
	 * Constructs an instance.
	 * 
	 * @param e the actual error cause.
	 */
	public PackagingException(Throwable e) {
		super(e);
	}

	/**
	 * @return Returns the details.
	 */
	public String getDetails() {
		return details;
	}

	/**
	 * @param details The details to set.
	 */
	public void setDetails(String details) {
		this.details = details;
	}
}
