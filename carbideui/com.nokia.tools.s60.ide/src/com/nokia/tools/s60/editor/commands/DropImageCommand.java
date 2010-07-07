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
import com.nokia.tools.media.utils.layers.ILayer;

public class DropImageCommand extends Series60EditorCommand {

	private Object image;

	private Object undoObject;

	private ILayer layer;

	public DropImageCommand(IContentData data, Object image, EditPart sourceEp) {
		super(data, sourceEp);
		setLabel(Messages.PasteImage_Label);
		this.image = image;
	}

	@Override
	public boolean canExecute() {
		return image != null && getAdapter() != null;
	}

	@Override
	public boolean canUndo() {
		return undoObject != null;
	}

	@Override
	protected void doExecute() {
		try {
			undoObject = getAdapter().paste(image,
					layer == null ? null : layer.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doRedo() {
		doExecute();
	}

	@Override
	public void doUndo() {
		if (undoObject != null) {
			getAdapter().setThemeGraphics(getAdapter().getClone(undoObject),
					null);
		}
	}

	public ILayer getLayer() {
		return layer;
	}

	public void setLayer(ILayer layer) {
		this.layer = layer;
	}
}
