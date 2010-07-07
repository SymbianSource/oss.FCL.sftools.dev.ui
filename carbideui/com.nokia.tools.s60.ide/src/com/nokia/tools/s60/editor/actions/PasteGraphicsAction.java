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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.ForwardUndoCompoundCommand;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor;
import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.layers.IPasteTargetAdapter;
import com.nokia.tools.s60.editor.commands.PasteImageCommand;
import com.nokia.tools.s60.editor.commands.SetThemeGraphicsCommand;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.core.MultiPieceManager;


/**
 * Paste image action for screen elements that cannot have multiple layers
 */
public class PasteGraphicsAction extends AbstractAction {

	public static final String ID = ActionFactory.PASTE.getId() + "Graphics";

	private Clipboard clip = null;
	private Object themeGraphic = null;

	@Override
	protected void init() {
		setId(ID);
		setText(Messages.PasteImageAction_name + " Layers");
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/etool16/paste_edit.gif"));
		setDisabledImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/dtool16/paste_edit.gif"));
		setToolTipText(Messages.PasteImageAction_tooltip);
		setLazyEnablementCalculation(true);
	}

	public PasteGraphicsAction(IWorkbenchPart part, Clipboard clip) {
		super(part);
		this.clip = clip;
	}

	public PasteGraphicsAction(ISelectionProvider provider, CommandStack stack,
			Clipboard clip) {
		super(null);
		setSelectionProvider(provider);
		this.stack = stack;
		this.clip = clip;
	}

	@Override
	public void doRun(Object sel) {
		final Clipboard _clip = this.clip == null ? ClipboardHelper.APPLICATION_CLIPBOARD
				: clip;

		IContentData data = getContentData(sel);
		if (data != null) {

			ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data
					.getAdapter(ISkinnableEntityAdapter.class);
			if (skAdapter != null) {
				EditPart part = getEditPart(sel);

				if (themeGraphic != null) {
					if (themeGraphic instanceof List) {
						// nine piece
						// special case - nine piece graphics
						ForwardUndoCompoundCommand command = new ForwardUndoCompoundCommand(
								com.nokia.tools.s60.editor.commands.Messages.PasteImage_Label);
						PasteImageCommand cmd = new PasteImageCommand(data,
								part, themeGraphic);
						command.add(cmd);
						Command stretchModeCommand = skAdapter
								.getApplyStretchModeCommand(skAdapter
										.getStretchMode());
						if (stretchModeCommand.canExecute()) {
							command.add(stretchModeCommand);
						}
						execute(command, part);
					} else {
						SetThemeGraphicsCommand cmd = new SetThemeGraphicsCommand(
								data, skAdapter.getThemeGraphics(),
								themeGraphic, null);
						execute(cmd, part);
					}
					return;
				}

				for (DataFlavor fl : _clip.getAvailableDataFlavors()) {
					if (fl.getMimeType().startsWith(
							DataFlavor.javaJVMLocalObjectMimeType)) {
						try {
							ClipboardContentDescriptor content = (ClipboardContentDescriptor) _clip
									.getData(fl);
							if (MultiPieceManager.isMultiPiece(content.getType())) {
							/*if (content.getType() == ClipboardContentDescriptor.ContentType.CONTENT_GRAPHICS_NINEPIECE ||
								content.getType() == ClipboardContentDescriptor.ContentType.CONTENT_GRAPHICS_THREEPIECE	||
								content.getType() == ClipboardContentDescriptor.ContentType.CONTENT_GRAPHICS_ELEVENPIECE) {
							*/	// special case - nine piece graphics
								PasteImageCommand cmd = new PasteImageCommand(
										data, part, content);
								cmd
										.setLabel(com.nokia.tools.s60.editor.commands.Messages.PasteImage_Label);
								execute(cmd, part);
							} else {
								// single or multilayer GFX
								SetThemeGraphicsCommand cmd = new SetThemeGraphicsCommand(
										data,
										skAdapter.getThemeGraphics(),
										skAdapter
												.getClone(content.getContent()),
										null);
								execute(cmd, part);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						return;
					}
				}
			}
		}
	}

	@Override
	protected boolean doCalculateEnabled(Object sel) {

		if (clip == null)
			clip = ClipboardHelper.APPLICATION_CLIPBOARD;

		if (isMaskNode())
			return false;

		IPasteTargetAdapter pasteAdapter = getPasteTargetAdapter(sel);

		if (themeGraphic != null) {
			return pasteAdapter == null ? false : pasteAdapter
					.isPasteAvailable(themeGraphic, null);
		} else {
			return pasteAdapter == null ? false : pasteAdapter
					.isPasteAvailable(clip, null);
		}
	}

	public void setThemeGraphic(Object themeGraphic) {
		this.themeGraphic = themeGraphic;
	}

}
