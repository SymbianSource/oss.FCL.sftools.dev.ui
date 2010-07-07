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

package com.nokia.tools.startuptip;

import org.eclipse.osgi.util.NLS;

public class Settings extends NLS {

	public static String STARTUP_TIP_DIALOG_TITLEBAR_IMAGE;
	public static String STARTUP_TIP_DIALOG_WIDTH;
	public static String STARTUP_TIP_DIALOG_HEIGHT;
	public static String STARTUP_TIPS_ROOT_FOLDER;
	
	static{
		NLS.initializeMessages(Settings.class.getName(), Settings.class);
	}
}
