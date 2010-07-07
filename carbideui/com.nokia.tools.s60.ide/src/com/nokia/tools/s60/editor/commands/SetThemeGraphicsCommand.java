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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.theme.editing.Messages;
import com.nokia.tools.ui.dialog.MessageDialogWithTextContent;

/**
 * Command realizing 'set theme graphic for SkinnableEntity' operation.
 */
public class SetThemeGraphicsCommand extends Series60EditorCommand {

	private Object newThemeGraphics, oldThemeGraphics;
	private String status;
	private IImage part;
	private boolean ninePieceStatus;

	private boolean showErrorMessages = true;

	public SetThemeGraphicsCommand(IImage editModel, IContentData data,
			EditPart part, String status) {
		super(data, part);
		setLabel(com.nokia.tools.s60.editor.commands.Messages.SetGraphics_Label);
		
		if(null != getAdapter()) {
			this.oldThemeGraphics = getAdapter()
					.getOriginalThemeGraphics(editModel);
			this.newThemeGraphics = getAdapter().getEditedThemeGraphics(editModel);
		}
		this.status = status;
		if (editModel.isPart())
			this.part = editModel;
	}

	public SetThemeGraphicsCommand(IContentData data, Object oldThemeGraphics,
			Object editedThemeGraphics, String status) {
		super(data, (EditPart) null);
		setLabel(com.nokia.tools.s60.editor.commands.Messages.SetGraphics_Label);
		this.oldThemeGraphics = oldThemeGraphics;
		this.newThemeGraphics = editedThemeGraphics;
		this.status = status;
	}

	@Override
	public boolean canExecute() {
		return getAdapter() != null && newThemeGraphics != null;
	}

	@Override
	public boolean canUndo() {
		return getAdapter() != null && oldThemeGraphics != null;
	}

	@Override
	protected void doRedo() {
		doExecute();
	}

	@Override
	protected void doUndo() {
		if (part == null) {
			getAdapter().setThemeGraphics(
					getAdapter().getClone(oldThemeGraphics), status);
			if (ninePieceStatus)
				//getAdapter().setNinePieceBitmap();
				getAdapter().setMultiPieceBitmap();
		} else
			getAdapter().setPartThemeGraphics(part,
					getAdapter().getClone(oldThemeGraphics), status);
	}

	@Override
	protected void doExecute() {
		try {
			if (part == null) {
				//ninePieceStatus = getAdapter().isNinePiece();
				ninePieceStatus = getAdapter().isMultiPiece();
				getAdapter().setThemeGraphics(newThemeGraphics, status);
				// make sure that nine-piece is switched to single iece mode
				//if (getAdapter().supportsNinePiece())
				if (getAdapter().supportsMultiPiece())
					getAdapter().setSinglePieceBitmap();
				// save copy of new theme graphics for potential REDO after
				
				if (!originalSkinned)
					newThemeGraphics = getAdapter().getClone(newThemeGraphics);
			} else {
				getAdapter().setPartThemeGraphics(part, newThemeGraphics,
						status);
			}
		} catch (Exception e) {
			e.printStackTrace();

			Throwable t = e;
			while (t.getCause() != null && t.getCause() != t) {
				t = t.getCause();
			}

			commandFailed(t, Messages.Paste_Error_Message);

			if (isShowErrorMessages()) {
				final Throwable ft = t;
				final Display display = Display.getDefault();
				display.syncExec(new Runnable() {
					public void run() {
						Shell shell = display.getActiveShell();
						if (shell != null) {
							MessageDialogWithTextContent.openError(shell,
									Messages.Paste_Error_Title,
									Messages.Paste_Error_Message, ft);
						}
					}
				});
			}
		}
	}

	public boolean isShowErrorMessages() {
		return showErrorMessages;
	}

	public void setShowErrorMessages(boolean showErrorMessages) {
		this.showErrorMessages = showErrorMessages;
	}

}
