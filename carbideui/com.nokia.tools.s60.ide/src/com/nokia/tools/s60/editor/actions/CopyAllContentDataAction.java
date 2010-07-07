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
package com.nokia.tools.s60.editor.actions;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

/**
 */
public class CopyAllContentDataAction extends CopyContentDataAction {
	
	public static final String ID = ActionFactory.COPY.getId() + "AllElements";

	/**
	 * Constructor added due contributed actions functionality.
	 * @param part
	 */
	public CopyAllContentDataAction(IWorkbenchPart part) {
		super(part);
		setCopySkinnedOnly(false);
	}	
	
	@Override
	protected void init() {
		super.init();
		setId(ID);
		setText("All"); 
		
		
	}
}

