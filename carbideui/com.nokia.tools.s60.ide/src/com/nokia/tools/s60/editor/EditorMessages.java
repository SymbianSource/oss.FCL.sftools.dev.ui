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
package com.nokia.tools.s60.editor;

import org.eclipse.osgi.util.NLS;

/**
 * 
 */
public class EditorMessages extends NLS {
	/**
	 * Error message - fetching themes failed.
	 */
	public static String View_Error_GetThemes;
	/**
	 * Error message - theme openning failed.
	 */
	public static String Editor_Error_OpenTheme;
	public static String Editor_Error_InvalidInput;
	public static String Editor_Error_MissingEmbeddedEditorDescriptor;
	public static String Editor_Error_InputNotExist;
	public static String Editor_Error_ContentNotAvailable_Title;
	public static String Editor_Error_ContentNotAvailable_Info;
	public static String Editor_Error_ScreenNotAvailable_Title;
	public static String Editor_Error_ScreenNotAvailable_Message;

	/**
	 * Opens a theme.
	 */
	public static String Editor_OpenTheme;
	/**
	 * Generating previews.
	 */
	public static String Editor_GeneratePreviews;

	/**
	 * Loading theme.
	 */
	public static String Editor_LoadingTheme;
	public static String Editor_Task_CreatingScreenContext;
	public static String Editor_Task_CreatingScreenElements;
	public static String Editor_Task_BuildingModel;
	public static String Editor_Task_CreatingGraphicalViewer;
	public static String Editor_Task_SyncingViewer;
	/**
	 * Applying a new theme.
	 */
	public static String Editor_ApplyTheme;
	public static String Editor_ReloadTheme;
	public static String Editor_SaveGraphicsEditorConfirmation;
	public static String Error_Editor_Save;
	public static String Editor_SaveAsDialog_text;
	public static String Editor_SaveAsDialog_title;
	public static String Editor_SaveAsDialog_message;
	public static String Editor_SaveAsDialog_projectContentsGroupLabel;
	public static String Editor_SaveAsDialog_useDefaultLabel;
	public static String Editor_SaveAsDialog_nameLabel;
	public static String Editor_SaveAsDialog_locationLabel;
	public static String Editor_SaveAsDialog_browseLabel;
	public static String Editor_SaveAsDialog_directoryLabel;
	public static String Error_Editor_SaveAsDialog_projectNameEmpty;
	public static String Error_Editor_SaveAsDialog__projectLocationEmpty;
	public static String Error_Editor_SaveAsDialog_locationError;
	public static String Error_Editor_SaveAsDialog_defaultLocationError;
	public static String Error_Editor_SaveAsDialog_projectExistsMessage;
	public static String Error_Editor_SaveAs;
	public static String Error_Editor_SaveAs_caseVariantExistsError;
	public static String Error_Editor_No_Screens;
	public static String Error_Editor_Initialize_Contents;

	public static String GraphicsEditor_Timeline_TimeModelLabel;
	public static String GraphicsEditor_Timeline_RealTime;

	public static String GraphicsEditor_Timeline_RelativeTime_Hour;
	public static String GraphicsEditor_Timeline_RelativeTime_Day;
	public static String GraphicsEditor_Timeline_RelativeTime_Week;
	public static String GraphicsEditor_Timeline_RelativeTime_Month;
	public static String GraphicsEditor_SavingConfirmation_Label;
	public static String Editor_Error_NoPreviewScreen;
	public static String GraphicsEditor_AnimationMode_Play;
	public static String GraphicsEditor_AnimationMode_Cycle;
	public static String GraphicsEditor_AnimationMode_Bounce;
	
	
	public static String GalleryPage_ReloadGallery;

	static {
		NLS.initializeMessages(EditorMessages.class.getName(),
				EditorMessages.class);
	}
}
