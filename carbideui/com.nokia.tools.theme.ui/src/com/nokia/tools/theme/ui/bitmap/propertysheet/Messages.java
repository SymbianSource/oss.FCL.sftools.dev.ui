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
package com.nokia.tools.theme.ui.bitmap.propertysheet;

import org.eclipse.osgi.util.NLS;

/**
 * NLS messages.
 * 
 */
public class Messages extends NLS {
	public static String Label_Color;

	public static String Label_Optimize;
	public static String Label_Dither;
	public static String Label_Colorize;
	public static String Label_Quality;
	public static String Label_Quality_Low;
	public static String Label_Quality_High;
	public static String Label_Quality_PaletteDependant;

	public static String Label_Section_Optimize;
	public static String Label_Section_Colorize;

	public static String Conversion_Section_banner_message;
	public static String Conversion_Section_banner_message_svg;

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

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
