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

package com.nokia.tools.s60.editor.commands;

import org.eclipse.gef.commands.Command;

import com.nokia.tools.media.utils.layers.IAnimationFrame;

public class RemoveAnimationFrameCommand extends Command {

	protected IAnimationFrame animationFrame;

	public RemoveAnimationFrameCommand(IAnimationFrame frame) {
		setLabel(Messages.RemoveAnimationFrame_Label);
		this.animationFrame = frame;
	}

	@Override
	public boolean canExecute() {
		return animationFrame != null;
	}

	@Override
	public boolean canUndo() {
		return animationFrame != null;
	}

	@Override
	public void execute() {
		redo();
	}

	@Override
	public void undo() {
		if (animationFrame != null) {
			int oldSeqNo = animationFrame.getSeqNo();
			animationFrame.getParent().addAnimationFrame(animationFrame);
			
			animationFrame.getParent().moveAnimationFrame(animationFrame,
					oldSeqNo);
		}
	}

	@Override
	public void redo() {
		animationFrame.getParent().removeAnimationFrame(animationFrame);
	}

}
