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
package com.nokia.tools.s60.ide;

import org.eclipse.osgi.util.NLS;

/**
 */
public class S60IDEMessages extends NLS {
	public static String Project_Not_S60_Data;
	public static String ConfirmPerspectiveSwitchTitle;
	public static String InfoWhyToLounchPerspective;

	static {
		NLS.initializeMessages(S60IDEMessages.class.getName(),
				S60IDEMessages.class);
	}
}
