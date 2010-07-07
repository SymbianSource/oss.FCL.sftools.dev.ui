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

package com.nokia.tools.s60.editor.ui.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String MaskDialog_Title;
	public static String MaskDialog_Banner_Title;
	public static String MaskDialog_Message;
	public static String MaskDialog_Preview_Label;
	public static String MaskDialog_Mask_Label;
	public static String MaskDialog_Input_Label;
	public static String MaskDialog_Result_Label;
	public static String MaskDialog_Info_Label;
	public static String MaskDialog_Wand_Tooltip;
	public static String MaskDialog_Pen_Tooltip;
	public static String MaskDialog_Eraser_Tooltip;
	public static String MaskDialog_Clear_Tooltip;
	public static String MaskDialog_ClearConfirmation_Title;
	public static String MaskDialog_ClearConfirmation_Message;
	public static String MaskDialog_CloseConfirmation_Title;
	public static String MaskDialog_CloseConfirmation_Message;
	
	public static String FailureListDialog_message;
	public static String FailureListDialog_element_lbl;
	public static String FailureListDialog_description_lbl;
	public static String FailureListDialog_detail_lbl;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
