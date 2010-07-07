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

import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.s60.editor.commands.CreateAnimationFrameCommand;
import com.nokia.tools.theme.s60.ui.Activator;

/**
 */
public class CreateAnimationFrameAction extends AbstractAction {

	private final static ImageDescriptor CREATE_IMAGE_DESCRIPTOR = Activator
			.getImageDescriptor("icons/New.png"); 

	public static final String ID = "Create_Animation_Frame"; 

	@Override
	protected void init() {
		setId(ID);
		setText(Messages.CreateAnimationFrameAction_name); 
		setLazyEnablementCalculation(true);
		setImageDescriptor(CREATE_IMAGE_DESCRIPTOR);
	}

	public CreateAnimationFrameAction(IWorkbenchPart part) {
		super(part);
	}

	public CreateAnimationFrameAction(ISelectionProvider provider,
			CommandStack stack) {
		super(null);
		setSelectionProvider(provider);
		this.stack = stack;
	}

	@Override
	public void doRun(Object sel) {
		if (sel instanceof IAnimatedImage) {
			IAnimatedImage image = (IAnimatedImage) sel;

			Command command = new CreateAnimationFrameCommand(image);
			command.setLabel(getText());
			execute(command, null);
		}
	}

	@Override
	protected boolean doCalculateEnabled(Object sel) {
		if (sel instanceof IAnimatedImage) {
			return true;
		}

		return false;
	}
}
