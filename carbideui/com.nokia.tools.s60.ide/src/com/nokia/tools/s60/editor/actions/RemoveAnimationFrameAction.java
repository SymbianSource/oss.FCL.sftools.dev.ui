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

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.s60.editor.commands.RemoveAnimationFrameCommand;
import com.nokia.tools.theme.s60.ui.Activator;

/**
 */
public class RemoveAnimationFrameAction extends AbstractAction {

	private final static ImageDescriptor DELETE_IMAGE_DESCRIPTOR = Activator
			.getImageDescriptor("icons/Remove.png"); 

	public static final String ID = "Remove Animation Frame Action"; 

	@Override
	protected void init() {
		setId(ID);
		setText(Messages.RemoveAnimationFrameAction_name);
		setLazyEnablementCalculation(true);
		setImageDescriptor(DELETE_IMAGE_DESCRIPTOR);
	}

	public RemoveAnimationFrameAction(IWorkbenchPart part) {
		super(part);
	}

	public RemoveAnimationFrameAction(ISelectionProvider provider,
			CommandStack stack) {
		super(null);
		setSelectionProvider(provider);
		this.stack = stack;
	}

	@Override
	public void doRun(Object sel) {
		if (sel instanceof IAnimationFrame) {
			IAnimationFrame animationFrame = (IAnimationFrame) sel;

			Command command = new RemoveAnimationFrameCommand(animationFrame);
			command.setLabel(getText());
			execute(command, null);
		}
	}

	@Override
	protected boolean doCalculateEnabled(Object sel) {
		if (sel instanceof IAnimationFrame) {
			return true;
		}

		return false;
	}
}
