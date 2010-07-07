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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.s60.editor.commands.Series60EditorCommand;
import com.nokia.tools.screen.core.IScreenElement;

/**
 * 
 * Action which executes given Command on activation through
 * editor's command Stack with undo support on widget
 */
public class GenericCommandAction extends AbstractAction {

	public static final String ID = "com.nokia.tools.s60.editor.GenericCommandAction";

	private Series60EditorCommand command;
	
	public GenericCommandAction(IWorkbenchPart part, ISelectionProvider provider,  CommandStack stack, Series60EditorCommand command) {
		super(part);
		setId(ID);
		setText(command.getLabel());
		if (provider != null)
			setSelectionProvider(provider);
		this.command = command;
		this.stack = stack;
	}

	@Override
	public void doRun(Object sel) {			
		IContentData data = getContentData(sel);
		//execute command thorugh stack
		if (data != null) {
			EditPart part = getEditPart(sel);
			execute(command, part);			
		}
	}

	@Override
	protected boolean doCalculateEnabled(Object element) {		
		return false;
	}
	
}
