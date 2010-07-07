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
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.platform.theme.ThemeTag;

/**
 * Commands capable of correct unde when convert2single bitmap action take
 * place.
*/
public class Convert2NineBitmapCommand extends Series60EditorCommand {

	private Object originalTg;

	private Object[] partUndoObjects;
	private boolean fillParts;

	public Convert2NineBitmapCommand(IContentData data, EditPart part,
			boolean fillParts) {
		super(data, part);
		setLabel(Messages.Convert2Nine_Label);
		this.fillParts = fillParts;
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
		if (fillParts && partUndoObjects != null) {
			try {
				getAdapter().setPartsThemeGraphics(partUndoObjects,
						ThemeTag.ATTR_VALUE_ACTUAL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		getAdapter().setSinglePieceBitmap();
		getAdapter().setThemeGraphics(originalTg, null);

		if (!originalSkinned && getAdapter() != null) {
			try {
				getAdapter().clearThemeGraphics();
				// when clearing colour, special action is needed
			} catch (Exception e) {
				e.printStackTrace();
			}
			originalTg = getAdapter().getClone(originalTg);
		}
	}

	@Override
	protected void doExecute() {
		try {
			IImageAdapter adapter = (IImageAdapter) getContentData()
					.getAdapter(IImageAdapter.class);
			IImage image = adapter.getImage(false);
			originalTg = getAdapter().getOriginalThemeGraphics(image);
			//partUndoObjects = getAdapter().convertToNinePieceBitmap(fillParts);
			partUndoObjects = getAdapter().convertToMultiPieceBitmap(fillParts);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doUndo() {
	}

}
