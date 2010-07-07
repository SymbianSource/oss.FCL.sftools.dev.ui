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

import org.eclipse.jface.action.Action;

import com.nokia.tools.theme.s60.cstore.ComponentStore.StoredElement;

public class TagFilterAction extends Action {

	private StoredElement element;

	public TagFilterAction(String tagName, StoredElement el) {
		super(tagName);
		element = el;
	}
	
	@Override
	public void run() {
		if (element.tags.contains(getText())) {
			element.tags.remove(getText());
		} else {			
			element.tags.add(getText());
		}
	}
	
}
