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
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.s60.editor.commands.Convert2NineBitmapCommand;
import com.nokia.tools.s60.editor.commands.Convert2SingleBitmapCommand;
import com.nokia.tools.s60.editor.ui.dialogs.NinePieceOperationConfirmDialog;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.core.MultiPieceManager;
import com.nokia.tools.theme.core.NinePieceElement;

/*
 * single to nine piece conversion and vice versa action 
 */
public class NinePieceConvertAction extends AbstractAction {

	public static final int TYPE_CONVERT2SINGLE = 1;

	public static final int TYPE_CONVERT2NINE = 9;

	public static final String HLP_CTX = "com.nokia.tools.s60.ide" + '.' + "NinePieceOperationConfirm_context"; 

	public static final String ID_NINE = "2NinePieceConvertCommand"; 

	public static final String ID_SINGLE = "2SinglePieceConvertCommand"; 

	// cmd type
	private int type;

	protected void _init() {
		String ID = type == TYPE_CONVERT2NINE ? ID_NINE : ID_SINGLE;
		String text = type == TYPE_CONVERT2NINE ? Messages.NinePieceConvertCommand_name
				: Messages.SinglePieceConvertCommand_name;
		setId(ID);
		setText(text);
		setLazyEnablementCalculation(true);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				NinePieceConvertAction.HLP_CTX);
	}

	public NinePieceConvertAction(ISelectionProvider provider,
			CommandStack stack, int type) {
		super(null);
		this.stack = stack;
		this.type = type;
		setSelectionProvider(provider);
		_init();
	}

	public NinePieceConvertAction(IWorkbenchPart part, int type) {
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

			ISkinnableEntityAdapter ska = (ISkinnableEntityAdapter) data.getAdapter(ISkinnableEntityAdapter.class); 
			boolean isElementSkinned = false;
			if (ask) {
				int result = IDialogConstants.OK_ID;
				
				
				isElementSkinned = ska.isSkinned();
				
				if (isElementSkinned) {
					NinePieceOperationConfirmDialog dialog = new NinePieceOperationConfirmDialog(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getShell(),
							NinePieceOperationConfirmDialog.TYPE_2SINGLE);
					 result = dialog.open();
					 
					 if (result == IDialogConstants.OK_ID)
							replaceGfx = true;
						else
							return; // cancel pressed
					 
				} else {
					replaceGfx = false;
				}
				
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
			if (isElementSkinned && !replaceGfx) ska.clearThemeGraphics();
			// special way of update / undo / redo
			Convert2SingleBitmapCommand cmd = new Convert2SingleBitmapCommand(
					getContentData(element), ep, replaceGfx);
			execute(cmd, ep);
		} else {
			if (type == TYPE_CONVERT2NINE) {

				IPreferenceStore iPreferenceStore = UtilsPlugin.getDefault()
						.getPreferenceStore();
				boolean ask = !iPreferenceStore
						.getBoolean(IMediaConstants.PREF_SINGLE_PIECE_2NINE_ASK);

				boolean fillParts = iPreferenceStore
						.getBoolean(IMediaConstants.PREF_SINGLE_PIECE_2NINE);

				if (ask) {
					NinePieceOperationConfirmDialog dialog = new NinePieceOperationConfirmDialog(
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
							NinePieceOperationConfirmDialog.TYPE_2NINE);
					int result = dialog.open();
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
				Convert2NineBitmapCommand cmd = new Convert2NineBitmapCommand(
						data, ep, fillParts);
				execute(cmd, ep);
			}
		}
	}
	
	private boolean isAlreadySinglePiece(Object element) {
		boolean isAlreadySinglePiece = false; 
		String currentType = ((ThemeData) element).getSkinnableEntity().getCurrentProperty();
		if (ThemeConstants.PROPERTIES_BITMAP.equals(currentType)) {
			isAlreadySinglePiece = true;
		}
		return isAlreadySinglePiece;
	}

	@Override
	protected boolean doCalculateEnabled(Object element) {
		
		try {
			IImage img = getImage(false, element);
			
			if (img == null)
				return false;
			
			
			if (type == TYPE_CONVERT2SINGLE)
				if (isAlreadySinglePiece(element))
					return false;
				else
					return MultiPieceManager.supportsConversion(img.getPartCount(), 1);
			else {				
				String currentType = ((ThemeData) element).getSkinnableEntity().getCurrentProperty();
				if (ThemeConstants.PROPERTIES_BITMAP.equals(currentType)) {
					return (MultiPieceManager.supportsConversion(new NinePieceElement().getPartCount(), img.getPartCount()));
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
