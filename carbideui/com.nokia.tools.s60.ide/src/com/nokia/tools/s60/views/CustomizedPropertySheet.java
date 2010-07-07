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

package com.nokia.tools.s60.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertySheet;

public class CustomizedPropertySheet extends PropertySheet {

	public CustomizedPropertySheet() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {

		super.createPartControl(parent);
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
		menuManager.setVisible(false);
		menuManager.removeAll();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.updateAll(true);

		IToolBarManager toolBarManager = getViewSite().getActionBars()
				.getToolBarManager();
		toolBarManager.removeAll();
		menuManager.setRemoveAllWhenShown(true);
		toolBarManager.update(true);
	}

	@Override
	public boolean isPinned() {
		return false;
	}

}
