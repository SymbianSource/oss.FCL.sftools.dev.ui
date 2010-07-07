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
package com.nokia.tools.screen.ui.actions;

import org.eclipse.osgi.util.NLS;

/**
 * NLS messages.
 * 
 */
public class Messages extends NLS {
	public static String PlayMedia_PlayAll;
	public static String PlayMedia_PlaySelection;
	public static String PlayMedia_Pause;
	public static String PlayMedia_Resume;
	public static String PlayMedia_Stop;
	public static String PlayMedia_PlayingSpeed;
	
	public static String OpenProjectAction_Error_LoadingContent_Title;
	public static String OpenProjectAction_Error_LoadingContent_Message;
	public static String OpenProjectAction_Error_ContentMissing_Message;

	public static String DeployPackageAction_Error_MsgBox_Title;
	public static String DeployPackageAction_Error_MsgBox_Message;
	public static String DeployPackageAction_Error_MsgBox_Message_Link;
	public static String DeployPackageAction_Error_MsgBox_preferenceAddress;
	public static String DeployPackageAction_Error_Execution_MsgBox_Title;
	public static String DeployPackageAction_Error_Execution_MsgBox_Message;

	public static String NewPackageAction_Save_MsgBox_Title;
	public static String NewPackageAction_Save_MsgBox_Message;
	public static String NewPackageAction_Save_MsgBox_Confirmation_text;
	
	public static String Error_Opening_Project;
	public static String Error_Opening_Project_Title;
	public static String Error_Opening_Project_AlreadyExists;
	
	public static String Error_Animation_Title;
	public static String Error_Animation_Message;
	
	public static String Command_ApplyBounds;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
