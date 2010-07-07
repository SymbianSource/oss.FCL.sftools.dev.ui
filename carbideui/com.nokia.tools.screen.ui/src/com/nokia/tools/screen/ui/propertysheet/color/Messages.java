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

package com.nokia.tools.screen.ui.propertysheet.color;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String CssColorDialog_DialogTitle;
	public static String CssColorDialog_WebPaletteTab;
	public static String CssColorDialog_SystemColorTab;
	public static String CssColorDialog_NamedColorTab;
	public static String CssColorDialog_CustomColorTab;
	public static String CssColorDialog_Label_Colour;
	public static String CssColorDialog_Label_Format;
	
	public static String NamedColorComposite_Label_Basic;
	public static String NamedColorComposite_Label_Additional;
	
	public static String CustomColorComposite_Label_Preview;
	public static String CustomColorComposite_Label_Red;
	public static String CustomColorComposite_Label_Green;
	public static String CustomColorComposite_Label_Blue;
	public static String CustomColorComposite_Label_Hex;
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
