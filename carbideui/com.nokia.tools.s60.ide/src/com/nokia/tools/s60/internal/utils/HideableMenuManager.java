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
package com.nokia.tools.s60.internal.utils;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

/**
 * MenuManager that shows itself only if contains at least on enabled and visible item
 */
public class HideableMenuManager extends MenuManager {	
	
	boolean dynamicVisible = true;
	
	public HideableMenuManager(String t){
		super(t,t);
	}
	
	public HideableMenuManager(String t, boolean dynamicEnable){
		super(t,t);
		dynamicVisible = !dynamicEnable;
	}
	
	@Override
	public boolean isDynamic() {
		return true;
	}
	
	
	@Override
	public boolean isVisible() {
		if (dynamicVisible) {
			IContributionItem[] items = super.getItems();
			for (int i = 0; i < items.length; i++) {
				if (items[i].isEnabled() && items[i].isVisible() && !(items[i] instanceof Separator)) {
					return true;
				}
			}
			return false;
		} else 
			return super.isVisible();
	}
}
