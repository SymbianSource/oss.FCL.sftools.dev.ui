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
package com.nokia.tools.media.utils;

/**
 * Base implementation of the {@link RunnableWithParamter}.
 * 
 */
public abstract class BaseRunnable implements RunnableWithParameter {
	private Object param;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.RunnableWithParameter#setParameter(java.lang.Object)
	 */
	public void setParameter(Object data) {
		param = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.RunnableWithParameter#getParameter()
	 */
	public Object getParameter() {
		return param;
	}
}
