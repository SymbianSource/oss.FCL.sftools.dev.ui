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

package com.nokia.tools.theme.ui.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String SVGConversionConfirmDialog_title;
	public static String SVGConversionConfirmDialog_banner_title;
	public static String SVGConversionConfirmDialog_banner_message;
	public static String SVGConversionConfirmDialog_text2;
	public static String SVGConversionConfirmDialog_dontAskAgain;
	public static String SVGConversionConfirmDialog_pixelsLabel;
	public static String SVGConversionConfirmDialog_checkTooltip;
	public static String SVGConversionConfirmDialog_maskCheckLabel;
	public static String SVGConversionConfirmDialog_maskCheckTooltip;
	public static String SVGConversionConfirmDialog_hardmaskCheckLabel;

	public static String ResourceSelectionDialog_Theme_Tab_Text;
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
