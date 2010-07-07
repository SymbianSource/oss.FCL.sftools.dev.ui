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

package com.nokia.tools.startuptip.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String SHOW_FROM_GROUP;
	public static String STARTUP_TIP_DIALOG_TITLE;
	public static String STARTUP_TIP_DIALOG_MESSAGE;
	public static String STARTUP_TIP_DIALOG_NEXT_LABEL;
	public static String STARTUP_TIP_PREFERENCE_PAGE_DESCRIPTION;
	public static String SHOW_TIP_ON_STARTUP;
	
	static{
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
