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
package com.nokia.tools.theme.s60.internal.core;

import org.eclipse.osgi.util.NLS;

/**
 * NLS messages.
 */
public class Messages extends NLS {
	public static String Task_ParsingEffects;
	public static String Task_ParsingValueModels;
	public static String Task_Initializing;
	public static String Scaleable_Def_Layer_Limit_Msg;
	public static String Morphing_Def_Layer_Limit_Msg;
	public static String FastAnim_Def_Layer_Limit_Msg;
	public static String Select_Local_Storage;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
