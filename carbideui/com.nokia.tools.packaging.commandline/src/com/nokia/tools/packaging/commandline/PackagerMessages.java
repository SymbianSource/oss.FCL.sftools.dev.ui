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

package com.nokia.tools.packaging.commandline;

import org.eclipse.osgi.util.NLS;

/**
 * This class holds the toll specific messages shown to the user populates it
 * from PackagerMessages.properties file
 * 
 * @author Bhanu
 */
public class PackagerMessages extends NLS {

	public static String Packager_Commandline;

	public static String Packager_Flag_Input;

	public static String Packager_Flag_Output;

	public static String Packager_Flag_ID;

	public static String Packager_Flag_Version;

	public static String Packager_Flag_Themename;

	public static String Packager_Flag_Notice;

	public static String Packager_Flag_Copy;

	// public static String Packager_Flag_ConfigFile;
	public static String Packager_Flags;

	public static String Packager_DefaultConfigFile;

	public static String Packager_Flag_invalid;

	public static String Packager_Error;

	public static String Packager_Warning;

	public static String Packager_Usage;

	public static String Packager_Command;

	public static String Packager_Error_input;

	public static String Packager_Error_invalid_input;

	public static String Packager_Error_FileNotExist;

	public static String Packager_Error_ConfigFileNotExist;

	public static String Packager_Error_ConfigFileCorrupted;

	public static String Packager_Error_OutFolderNotValid;

	public static String Packager_Error_Themename;

	public static String Packager_Error_Copy;

	public static String Packager_TPF;

	public static String Packager_TDF;

	public static String Packager_Error_Invalidtpf;

	public static String Packager_FileSeperator;

	public static String Packager_TempFolder;

	public static String Packager_DefaultOutFolder;

	public static String Packager_Error_InvalidTarget;

	public static String Packager_HexStartsWith;

	public static String Packager_Error_Themename_Illegal;

	public static String Packager_Error_UIDNotHex;

	public static String Packager_Error_UIDRange;

	public static String Packager_Error_UIDProtectedRange;

	public static String Packager_Error_AuthorMaxLen;

	public static String Packager_Error_CopyrightMaxLen;

	public static String Packager_Error_Conversion_Error_From_TDF_To_Skn;

	public static String Packager_Successful;

	public static String Packager_UnSuccessful;

	public static String Packager_S60_versions;

	public static String Packager_S60;

	public static String Packager_Error_Duplicate_Parameter;

	public static String Packager_Error_Missing_Parameter;

	public static String Packager_Error_xmlparsing;

	public static String Packager_Error_packaging;

	public static String Packager_Error_tpf2tdf;

	public static String Packager_Error_skincompiler;

	public static String Packager_Error_IO;

	public static String Packager_Error_content_read_failed;

	public static String Packager_Error_no_content;

	public static String Packager_Error_NO_TDF_FILE_PRESENT;
	
	public static String Packager_Error_MULTIPLE_TDF_FILES_PRESENT;
	
	public static String Packager_Process_Started;
	
	static {
		NLS.initializeMessages(PackagerMessages.class.getName(),
				PackagerMessages.class);
	}

	public static void printMessageOnConsole(String arg) {
		System.out.println(arg);
	}
}
