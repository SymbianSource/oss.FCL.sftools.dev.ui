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
package com.nokia.tools.s60.wizards;

import org.eclipse.osgi.util.NLS;

public class WizardMessages extends NLS {
	// plugin installation
	public static String Plugin_Installation_Title;
	public static String Plugin_Installation_Banner_Title;
	public static String Plugin_Installation_Banner_Message;
	public static String Plugin_Installation_File_Text;
	public static String Plugin_Installation_Browse_Text;
	public static String Plugin_Installation_File_Exist_Error;
	public static String Plugin_Installation_Plugin_Exist_Error;
	public static String Plugin_Installation_File_Name_Error;
	public static String Plugin_Installation_File_Read_Error;
	public static String Plugin_Installation_File_Error;
	public static String Plugin_Installation_Install_Button;
	public static String Plugin_Installation_Install_Task;
	public static String Plugin_Installation_Uninstall_Task;
	public static String Plugin_Installation_Install_Error_Title;
	public static String Plugin_Installation_Install_Error_Message;
	public static String Plugin_Installation_Configuration_Error;

	static {
		NLS.initializeMessages(WizardMessages.class.getName(),
				WizardMessages.class);
	}
}