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
package com.nokia.tools.resource.util;

import com.nokia.tools.resource.core.Activator;


/**
 * This interface is used for clients that will provide the return value during
 * asynchronous method invocation.
 */
public interface IInvokable<T> {
	/**
	 * Invocation method.
	 * 
	 * @return the return value from the invocation.
	 * @throws Throwable if invocation failed.
	 */
	T invoke() throws Throwable;

	/**
	 * Handles the invocation error.
	 * 
	 * @param e the invocation error.
	 */
	void handleException(Throwable e);

	/**
	 * This is the convenient implementation of the {@link IInovakable}
	 * interface that will simply report the invocation error in the platform
	 * log.
	 */
	abstract class Adapter<T> implements IInvokable<T> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IInvokable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable e) {
			Activator.error(e);
		}
	}
}
