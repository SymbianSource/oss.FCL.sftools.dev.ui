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
package com.nokia.tools.theme.s60.ui.cstore;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.theme.s60.cstore.ComponentPoolBackend.NamedResult;

public interface IComponentPool {

	/**
	 * Returns list of named results. returns IContentData as data object in
	 * NamedResult
	 * 
	 * @param id
	 * @return
	 * @throws ThemeException
	 */
	public abstract NamedResult[] getElementListFromPool(String id,
			String parentId) throws ThemeException;

	public abstract boolean isSkinned(String elementId, String parentElementId,
			String sourceThemeName);

	public abstract int getThemeCount();

	public abstract IContentData getComponentFromPool(String elementId,
			String parentElementId, String sourceThemeName);

	public abstract String[] getThemeNames();

}