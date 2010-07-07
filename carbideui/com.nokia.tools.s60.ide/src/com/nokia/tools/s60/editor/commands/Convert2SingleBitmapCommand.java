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

import org.eclipse.gef.EditPart;

import com.nokia.tools.content.core.IContentData;

/**
 * Commands capable of correct undo when convert2single bitmap action take
 * place.
 */
public class Convert2SingleBitmapCommand extends Series60EditorCommand {
	private boolean replaceGraphics;

	public Convert2SingleBitmapCommand(IContentData data, EditPart part,
			boolean replaceGraphics) {
		super(data, part);
		setLabel(Messages.Convert2Single_Label);
		this.replaceGraphics = replaceGraphics;
	}

	@Override
	public boolean canExecute() {
		return getAdapter() != null;
	}

	@Override
	public boolean canUndo() {
		return getAdapter() != null;
	}

	@Override
	protected void doRedo() {
		doExecute();
	}

	@Override
	public void undo() {
		
		int childenCount = getAdapter().getContentData().getChildren().length;
		getAdapter().setMultiPieceBitmap();
	
		

		if (!originalSkinned && getAdapter() != null) {
			try {
				System.out.println("Undo");
				getAdapter().clearThemeGraphics();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void doExecute() {
		try {
			getAdapter().convertToSinglePieceBitmap(replaceGraphics);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doUndo() {
	}

}
