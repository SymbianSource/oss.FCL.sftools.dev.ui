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

package com.nokia.tools.theme.s60.parser;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	public static String Theme_Configuration_Warning_Title;
	public static String Theme_Configuration_Warning_Message;
	public static String Theme_Configuration_Error;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
