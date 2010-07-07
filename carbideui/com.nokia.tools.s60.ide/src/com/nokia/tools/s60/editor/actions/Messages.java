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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	
	public static String EditInSystemEditorAction_Name;

	public static String NotANumber;

	public static String ShowInMenu_CStore;
	public static String ShowInMenu_Colors;

	public static String StretchModeAction_Normal;
	
	public static String EditLayersAction_name;

	public static String PasteImageAction_name;
	public static String PasteImageAction_tooltip;

	public static String CopyImageAction_name;
	public static String CopyImageAction_tooltip;

	public static String ContainmentAction_tooltip;
	public static String CategoryAction_tooltip;

	public static String EditImageAction_name;
	public static String EditSVGImageAction_name;
	public static String EditSoundAction_name;
	public static String ConvertAndEditAction_name;
	public static String ClearImageAction_name;
	public static String ClearImageAction_tooltip;
	public static String ConvertToFifAction_Name;
	
	public static String SetColorAction_name;
	public static String SetColorAction_tooltip;
	
	public static String EditMaskInBitmapEditorAction_name;

	public static String SetMaskAction_name;
	public static String SetMaskAction_dialog_label;

	public static String SetHardMaskAction_dialog_label;
	public static String SetHardMaskAction_msg_noBW;
	public static String SetHardMaskAction_msg_invert;

	public static String InvertMaskAction_name;
	public static String ClearMaskAction_name;
	public static String ExtractMaskAction_name;

	public static String ExtractMaskAction_error;
	public static String ExtractMaskAction_dialog_label;

	public static String NinePieceBitmapAction_name;

	public static String CreateAnimationFrameAction_name;
	public static String SetAnimateTimeAction_name;
	public static String DistributeAnimateTimeAction_name;
	public static String RemoveAnimationFrameAction_name;
	public static String SetAnimateTimeAction_AnimateTimeMustBe;
	public static String SetAnimateTimeAction_EnterTime;

	public static String ExternalTools_name;

	public static String ShowInResourceViewAction_name;

	public static String NinePieceConvertCommand_name;
	public static String ElevenPieceConvertCommand_name;
	public static String ThreePieceConvertCommand_name;	
	
	public static String SinglePieceConvertCommand_name;

	public static String EditInExternalAction_toolSettingsError;
	public static String EditInExternalAction_openToolError;
	public static String EditInExternalAction_configureTool;
	public static String EditInExternalAction_preferenceAddress;

	public static String EditInExternalAction_executionErrorTitle;
	public static String EditInExternalAction_executionErrorMessage;
	
	public static String AlignmentToolsDropDownAction_tooltip;

	public static String ShowLayersAction_lbl;
	public static String ShowPropertiesAction_lbl;
	public static String ShowOutlineAction_lbl;

	public static String CopyAction_9piece_msg;
	public static String CopyAction_9piece_title;
	public static String CopyAction_9piece_checkbox;
	
	public static String BrowseForFileAction_name;
	public static String BrowseForFileAction_tooltip;

	public static String BrowseForFileAction_notfound;

	public static String PasteContentDataAction_pasteFailed;
	public static String PasteContentDataAction_incompatibleContent;
	public static String PasteContentDataAction_incompatibleContentDetail;
	
	public static String ShowInComponentViewAction_name;
	public static String SelectImage_bannerMessage;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
