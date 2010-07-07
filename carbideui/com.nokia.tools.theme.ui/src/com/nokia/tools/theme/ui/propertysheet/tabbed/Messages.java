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
package com.nokia.tools.theme.ui.propertysheet.tabbed;

import org.eclipse.osgi.util.NLS;

/**
 * NLS messages.
 * 
 */
public class Messages extends NLS {
	public static String SetColorCategoryInfo;
	public static String Label_Color;

	public static String MediaFile_err_title;
	public static String MediaFile_err_msg;
	public static String MediaFile_file_label;
	public static String MediaFile_browse_label;
	public static String MediaFile_dialog_label;
	
	public static String Label_FocusTooltip ;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
