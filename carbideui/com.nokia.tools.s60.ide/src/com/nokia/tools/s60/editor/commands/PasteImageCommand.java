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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.clipboard.IClipboardContentType;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.IPasteTargetAdapter;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.editing.Messages;
import com.nokia.tools.ui.dialog.MessageDialogWithTextContent;

public class PasteImageCommand extends Series60EditorCommand {

	private Object clipboardContent;

	private Object undoObject;

	private Object editedObject;

	private boolean originalSinglePiece;

	// for nine piece paste
	private Object[] undoObjects;

	private Object[] editedObjects;

	private boolean showErrorMessages = true;

	private IPasteTargetAdapter getPasteTargetAdapter() {
		ISkinnableEntityAdapter skAdapter = getAdapter();
		if (null != skAdapter) {
			return (IPasteTargetAdapter) skAdapter
					.getAdapter(IPasteTargetAdapter.class);
		}
		return (IPasteTargetAdapter) getContentData().getAdapter(
				IPasteTargetAdapter.class);
	}

	public PasteImageCommand(IContentData data, EditPart part,
			Object clipboardContent) {
		super(data, part);
		setLabel(com.nokia.tools.s60.editor.commands.Messages.PasteImage_Label);
		this.clipboardContent = clipboardContent;
	}

	@Override
	public boolean canExecute() {
		return clipboardContent != null && getPasteTargetAdapter() != null;
	}

	@Override
	public boolean canUndo() {
		return undoObject != null || undoObjects != null;
	}

	@Override
	protected void doExecute() {
		IPasteTargetAdapter pasteAdapter = getPasteTargetAdapter();
		ISkinnableEntityAdapter skAdapter = getAdapter();
		if (null != skAdapter) {
			if (!skAdapter.isMultiPiece() && skAdapter.supportsMultiPiece()
					&& pasteAdapter.isPasteAvailable(clipboardContent, null)) {
				if (skAdapter.getClipboardContentType(clipboardContent) == IClipboardContentType.MULTI_PIECE_GRAPHICS) {
					originalSinglePiece = true;
					skAdapter.setMultiPieceBitmap();
					/*if(skAdapter.isNinePiece()){
						skAdapter.setNinePieceBitmap();
					}else if(skAdapter.isElevenPiece()){
						skAdapter.setElevenPieceBitmap();
					}else if(skAdapter.isThreePiece()){
						skAdapter.setThreePieceBitmap();
					}*/
					//skAdapter.setNinePieceBitmap();
				}
			}
	
			//if (skAdapter.isNinePiece() || skAdapter.isElevenPiece() || skAdapter.isThreePiece()) {
			if (skAdapter.isMultiPiece()) {
				int numberOfPieces = 0;
				numberOfPieces = skAdapter.getMultiPiecePartCount();
				/*if(skAdapter.isNinePiece()){
					numberOfPieces= 9;
				}else if(skAdapter.isElevenPiece()){
					numberOfPieces= 11;
				}else if(skAdapter.isThreePiece()){
					numberOfPieces= 3;
				}*/
				
				try {
					undoObjects = (Object[]) skAdapter.paste(clipboardContent,
							null);
					// acquire edited objects
					IImageAdapter ia = (IImageAdapter) skAdapter
							.getAdapter(IImageAdapter.class);
					IImage edited = ia.getImage(true);
					List<IImage> parts = edited.getPartInstances();
					
					/*if(skAdapter.isNinePiece()){
						editedObjects = new Object[9];
					}else if(skAdapter.isElevenPiece()){
						editedObjects = new Object[11];
					}*/
					
					editedObjects = new Object[numberOfPieces];
					
					int i = 0;
					for (IImage p : parts) {
						editedObjects[i++] = skAdapter.getClone(skAdapter
								.getEditedThemeGraphics(p));
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
									MessageDialogWithTextContent.openError(
											shell, Messages.Paste_Error_Title,
											Messages.Paste_Error_Message, ft);
								}
							}
						});
					}
				}
				return;
			}

		}
		if (null != pasteAdapter) {
			try {
				undoObject = pasteAdapter.paste(clipboardContent, null);
				if (null != skAdapter) {
					editedObject = skAdapter.getClone(skAdapter
							.getThemeGraphics());
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
	}

	private boolean isShowErrorMessages() {
		return showErrorMessages;
	}

	public void setShowErrorMessages(boolean showErrorMessages) {
		this.showErrorMessages = showErrorMessages;
	}

	@Override
	protected void doRedo() {
		if (null != editedObject) {
			try {
				getAdapter().setThemeGraphics(
						getAdapter().getClone(editedObject), null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (null != editedObjects) {
			try {
				
				Object[] cloned = new Object[editedObjects.length];
				for (int i = 0; i < cloned.length; i++) {
					cloned[i] = getAdapter().getClone(editedObjects[i]);
				}
				getAdapter().setPartsThemeGraphics(cloned, null);

				/*if (originalSinglePiece)
					getAdapter().setNinePieceBitmap();*/
				if (originalSinglePiece) {
					//getAdapter().setSinglePieceBitmap();
					getAdapter().setMultiPieceBitmap();
					/*if(getAdapter().isNinePiece()){
						getAdapter().setNinePieceBitmap();
					}else if(getAdapter().isElevenPiece()){
						getAdapter().setElevenPieceBitmap();
					}else if(getAdapter().isThreePiece()){
						getAdapter().setThreePieceBitmap();
					}*/
				}
			

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			doExecute();
		}
	}

	@Override
	protected void doUndo() {
		if (undoObject != null) {
			if (null != getAdapter()) {
				// refactor
				getAdapter().setThemeGraphics(
						getAdapter().getClone(undoObject), null);
			} else {
				IPasteTargetAdapter pasteAdapter = getPasteTargetAdapter();
				try {
					pasteAdapter.paste(undoObject, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (undoObjects != null) {
			try {
				//if (getAdapter().isNinePiece() || getAdapter().isElevenPiece() || getAdapter().isThreePiece()) {
				if (getAdapter().isMultiPiece()) {
					// acquire edited objects
					Object[] cloned = new Object[undoObjects.length];
					for (int i = 0; i < cloned.length; i++) {
						cloned[i] = getAdapter().getClone(undoObjects[i]);
					}
					getAdapter().setPartsThemeGraphics(cloned, null);
				}

				if (originalSinglePiece) {
					//getAdapter().setSinglePieceBitmap();
					getAdapter().setMultiPieceBitmap();
					/*if(getAdapter().isNinePiece()){
						getAdapter().setNinePieceBitmap();
					}else if(getAdapter().isElevenPiece()){
						getAdapter().setElevenPieceBitmap();
					}else if(getAdapter().isThreePiece()){
						getAdapter().setThreePieceBitmap();
					}*/
				}
				} catch (Exception e) {
			}
		}
	}
}
