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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.platform.theme.ThemeConstants;
import com.nokia.tools.s60.editor.commands.Convert2ElevenBitmapCommand;
import com.nokia.tools.s60.editor.commands.Convert2NineBitmapCommand;
import com.nokia.tools.s60.editor.commands.Convert2SingleBitmapCommand;
import com.nokia.tools.s60.editor.ui.dialogs.ElevenPieceOperationConfirmDialog;
import com.nokia.tools.s60.editor.ui.dialogs.NinePieceOperationConfirmDialog;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.core.ElevenPieceElement;
import com.nokia.tools.theme.core.MultiPieceManager;

/*
 * single to nine piece conversion and vice versa action 
 */
public class ElevenPieceConvertAction extends AbstractAction {

	public static final int TYPE_CONVERT2SINGLE = 1;

	public static final int TYPE_ELEVEN_PIECE = 11;

	public static final String HLP_CTX = "com.nokia.tools.s60.ide" + '.' + "NinePieceOperationConfirm_context"; 

	public static final String ID_ELEVEN = "2ElevenPieceConvertCommand"; 

	public static final String ID_SINGLE = "2SinglePieceConvertCommand"; 

	// cmd type
	private int type;

	protected void _init() {
		String ID = type == TYPE_ELEVEN_PIECE ? ID_ELEVEN : ID_SINGLE;
		String text = type == TYPE_ELEVEN_PIECE ? Messages.ElevenPieceConvertCommand_name
				: Messages.SinglePieceConvertCommand_name;
		setId(ID);
		setText(text);
		setLazyEnablementCalculation(true);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				NinePieceConvertAction.HLP_CTX);
	}

	public ElevenPieceConvertAction(ISelectionProvider provider,
			CommandStack stack, int type) {
		super(null);
		this.stack = stack;
		this.type = type;
		setSelectionProvider(provider);
		_init();
	}

	public ElevenPieceConvertAction(IWorkbenchPart part, int type) {
		super(part);
		this.type = type;
		_init();
	}

	@Override
	protected void doRun(Object element) {
		IContentData data = getContentData(element);
		if (data == null) {
			return;
		}
		if (type == TYPE_CONVERT2SINGLE) {

			IPreferenceStore iPreferenceStore = UtilsPlugin.getDefault()
					.getPreferenceStore();
			boolean ask = !iPreferenceStore
					.getBoolean(IMediaConstants.PREF_NINE_PIECE_2SINGLE_ASK);

			boolean replaceGfx = iPreferenceStore
					.getBoolean(IMediaConstants.PREF_NINE_PIECE_2SINGLE);

			if (ask) {
				int result = IDialogConstants.YES_ID;
				
				ISkinnableEntityAdapter ska = (ISkinnableEntityAdapter) data.getAdapter(ISkinnableEntityAdapter.class);
				boolean isElementSkinned = ska.isSkinned();
				
				if (isElementSkinned) {
					ElevenPieceOperationConfirmDialog dialog = new ElevenPieceOperationConfirmDialog(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getShell(),
									ElevenPieceOperationConfirmDialog.TYPE_2SINGLE);
					result = dialog.open();
					
					if (result == IDialogConstants.OK_ID)
						replaceGfx = true;
					else
						return; // cancel pressed
				} else {
					replaceGfx = false;
				}
				
				/*if (result == IDialogConstants.YES_ID)
					replaceGfx = true;
				else if (result == IDialogConstants.NO_ID)
						replaceGfx = false;*/
				
			}

			if (ask
					&& iPreferenceStore
							.getBoolean(IMediaConstants.PREF_NINE_PIECE_2SINGLE_ASK)) {
				// user selects to remeber settings this time - store
				// selected option
				iPreferenceStore.setValue(
						IMediaConstants.PREF_NINE_PIECE_2SINGLE, replaceGfx);
			}

			EditPart ep = getEditPart(element);
			// special way of update / undo / redo
			Convert2SingleBitmapCommand cmd = new Convert2SingleBitmapCommand(
					getContentData(element), ep, replaceGfx);
			execute(cmd, ep);
		} else {
			if (type == TYPE_ELEVEN_PIECE) {

				IPreferenceStore iPreferenceStore = UtilsPlugin.getDefault()
						.getPreferenceStore();
				boolean ask = !iPreferenceStore
						.getBoolean(IMediaConstants.PREF_SINGLE_PIECE_2NINE_ASK);

				boolean fillParts = iPreferenceStore
						.getBoolean(IMediaConstants.PREF_SINGLE_PIECE_2NINE);

				if (ask) {
					ElevenPieceOperationConfirmDialog dialog = new ElevenPieceOperationConfirmDialog(
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
									ElevenPieceOperationConfirmDialog.TYPE_2ELEVEN);
					int result = dialog.open();
					/*if (result == IDialogConstants.YES_ID)
						fillParts = true;
					else if (result == IDialogConstants.NO_ID)
						fillParts = false;*/
					if (result == IDialogConstants.OK_ID)
						fillParts = true;
					else
						return; // cancel pressed
				}

				if (ask
						&& iPreferenceStore
								.getBoolean(IMediaConstants.PREF_SINGLE_PIECE_2NINE_ASK)) {
					// user selects to remeber settings this time - store
					// selected option
					iPreferenceStore.setValue(
							IMediaConstants.PREF_SINGLE_PIECE_2NINE, fillParts);
				}

				EditPart ep = getEditPart(element);
				// special way of update / undo / redo
				Convert2ElevenBitmapCommand cmd = new Convert2ElevenBitmapCommand(
						data, ep, fillParts);
				execute(cmd, ep);
			}
		}
	}


	@Override
	protected boolean doCalculateEnabled(Object element) {
		try {
			/*if (element instanceof ThemeData)
				System.out.println(((ThemeData) element).getSkinnableEntity().getCurrentProperty());*/
			
			IImage img = getImage(false, element);
			if (img == null)
				return false;
			
			if (type == TYPE_CONVERT2SINGLE)
			{				
				String currentType = ((ThemeData) element).getSkinnableEntity().getCurrentProperty();
				if ((new ElevenPieceElement()).getElementTypeId().equals(currentType)) {
					if (img.getPartInstances().size() == TYPE_ELEVEN_PIECE)
						return true;
				}
				return false;
				//return true;
				//return img.isElevenPiece();
			}
			else {
				String currentType = ((ThemeData) element).getSkinnableEntity().getCurrentProperty();
				if (ThemeConstants.PROPERTIES_BITMAP.equals(currentType)) {
					if (img.getPartInstances().size() == TYPE_ELEVEN_PIECE)
						return true;
				}
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}
