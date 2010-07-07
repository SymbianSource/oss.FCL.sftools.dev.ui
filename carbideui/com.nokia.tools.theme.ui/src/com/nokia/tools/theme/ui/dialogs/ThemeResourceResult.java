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
/**
 * 
 */
package com.nokia.tools.theme.ui.dialogs;

import com.nokia.tools.screen.ui.dialogs.ResourceResult;

public class ThemeResourceResult<T> implements ResourceResult<T> {

	private final boolean isThemeResource;
	private final T value;

	/**
	 * @param isThemeResource
	 * @param value
	 */
	public ThemeResourceResult(boolean isThemeResource, T value) {
		this.isThemeResource = isThemeResource;
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see com.nokia.tools.screen.ui.dialogs.ResourceResult#isThemeResource()
	 */
	public boolean isThemeResource() {
		return isThemeResource;
	}

	/* (non-Javadoc)
	 * @see com.nokia.tools.screen.ui.dialogs.ResourceResult#value()
	 */
	public T value() {
		return value;
	}

}
