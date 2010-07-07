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
package com.nokia.tools.theme.s60.ui.propertysheet.tabbed;

import org.eclipse.osgi.util.NLS;

/**
 * NLS messages.
 * 
 */
public class Messages extends NLS {
	public static String Label_SupportedPlatforms;
	public static String Label_FocusTooltip;
	public static String Label_DrawLines;

	public static String Tooltip_DrawLines;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
