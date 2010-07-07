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
/*
 * 
 */
package com.nokia.tools.s60.ide.actions.menu;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.RetargetAction;

/**
 */
public class GeneralRetargetAction extends RetargetAction {

	public GeneralRetargetAction(String id, String title, String tooltip) {
		this(id, title, tooltip, IAction.AS_PUSH_BUTTON);
	}
	
	public GeneralRetargetAction(IAction template) {
		this(template.getId(), template.getText(), template.getToolTipText(), template.getStyle());
		setImageDescriptor(template.getImageDescriptor());
		setDisabledImageDescriptor(template.getDisabledImageDescriptor());
		setDescription(template.getDescription());
	}

	public GeneralRetargetAction(String id, String title, String tooltip,
			int style) {
		super(id, null, style);
		setText(title);
		setToolTipText(tooltip);
	}

}
