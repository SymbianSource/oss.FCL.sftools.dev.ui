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

package com.nokia.tools.s60.editor.menus;

import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPartService;

public class CustomZoomComboContributionItem extends ZoomComboContributionItem {

	public CustomZoomComboContributionItem(IPartService partService) {
		super(partService);
	}

	public CustomZoomComboContributionItem(IPartService partService,
			String[] initStrings) {
		super(partService, initStrings);
	}

	protected Control createControl(Composite parent) {
		super.createControl(parent);
		Control[] items = parent.getChildren();
		Combo combo = (Combo) items[0];
		combo.setVisibleItemCount(11);
		return combo;
	}
}
