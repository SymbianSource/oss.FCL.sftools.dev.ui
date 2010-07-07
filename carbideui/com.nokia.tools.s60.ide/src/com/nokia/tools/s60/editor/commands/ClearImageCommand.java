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

import java.util.List;

import org.eclipse.gef.EditPart;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.layers.IImage;

public class ClearImageCommand extends Series60EditorCommand {

	/** undo object for unod op. */
	private Object undoObject;

	/** when clear op. turned entity 9piece -> single piece, true */
	private boolean originaly9Piece;

	/** if invoking on part instance of some entity */
	private IImage partEntity;

	private boolean fireImmediateContentModified = true;
	
	public ClearImageCommand(IContentData data, EditPart part, IImage partEntity) {
		super(data, part);
		setLabel(Messages.Clear_Label);
		this.partEntity = partEntity;
	}

	public ClearImageCommand(IContentData data, EditPart part, IImage partEntity, boolean fireContentChanged) {
		super(data, part);
		setLabel(Messages.Clear_Label);
		this.partEntity = partEntity;
		this.fireImmediateContentModified = fireContentChanged;
	}
	
	@Override
	public boolean canExecute() {
		return getAdapter() != null && originalSkinned;
	}

	@Override
	public boolean canUndo() {
		// note - when 9-piece, entity does not have to have
		// single-piece TG present
		return undoObject != null || originaly9Piece;
	}

	@Override
	protected void doExecute() {

		//originaly9Piece = getAdapter().isNinePiece();
		
		originaly9Piece = getAdapter().isMultiPiece();
		
		if (!originalSkinned && !originaly9Piece) {
			// already clear, not need to clear
			return;
		}
		try {
			if (partEntity != null)
				undoObject = getAdapter().clearThemeGraphics(partEntity);
			else {
				undoObject = getAdapter().clearThemeGraphics();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doRedo() {
		doExecute();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void undo() {
		if (undoObject != null || originalSkinned) {
			if (partEntity != null) {
				getAdapter().setPartThemeGraphics(partEntity, undoObject, null);
			} else {
				if (undoObject instanceof Object[]) {
					Object[] undo = (Object[]) undoObject;
					List list = (List) undo[0];
					for (Object o : list) {
						getAdapter().setThemeGraphics(o, null);
					}
					// set part theme graphics
					List partUndos = (List) undo[1];
					try {
						getAdapter().setPartsThemeGraphics(partUndos.toArray(),
								null);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					for (Object o : (List) undoObject) {
						getAdapter().setThemeGraphics(o, null);
					}
				}
			}
			
			if (originaly9Piece) {
				getAdapter().setMultiPieceBitmap();
				originaly9Piece = false;
			}
		}
	}

	public boolean fireImmediateContentModified(){
		return fireImmediateContentModified;
	}
	
	@Override
	protected void doUndo() {
	}

}
