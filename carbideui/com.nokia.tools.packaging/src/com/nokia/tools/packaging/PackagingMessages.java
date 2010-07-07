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
package com.nokia.tools.packaging;

import org.eclipse.osgi.util.NLS;

public class PackagingMessages extends NLS {
	/**
	 * Error message - the target platform is undefined.
	 */
	public static String Error_platformMissing;
	/**
	 * Error message - the target platform is not supported.
	 */
	public static String Error_platformNotSupported;
	/**
	 * Error message - the symbian package description file is undefined.
	 */
	public static String Error_symbianPackageFileMissing;
	/**
	 * Error message - the SIS file is undefined.
	 */
	public static String Error_sisFileMissing;
	/**
	 * Error message - the certificate file is undefined.
	 */
	public static String Error_certificateFileMissing;
	/**
	 * Error message - the private key file is undefined.
	 */
	public static String Error_privateKeyFileMissing;
	/**
	 * Error message - the packaging processor failed to process the input
	 */
	public static String Error_packagingProcessorFailed;
	/**
	 * Error message - the theme description file is undefined.
	 */
	public static String Error_themeDescriptionFileMissing;
	/**
	 * Error message - the theme content is undefined.
	 */
	public static String Error_themeMissing;
	/**
	 * Error message - the theme name is undefined.
	 */
	public static String Error_themeNameMissing;
	/**
	 * Error message - the MIF name is undefined.
	 */
	public static String Error_mifNameMissing;
	/**
	 * Error message - the bitmap name is undefined.
	 */
	public static String Error_bitmapNameMissing;
	/**
	 * Error message - the OS is not supported.
	 */
	public static String Error_osNotSupported;
	/**
	 * Error message - the SIS file format is not correct
	 */
	public static String Error_sisFileFormat;
	/**
	 * Error message - the command execution failed
	 */
	public static String Error_commandExecutionFailed;
	/**
	 * Error message - the password is required
	 */
	public static String Error_PasswordNeeded;

	static {
		NLS.initializeMessages(PackagingMessages.class.getName(),
				PackagingMessages.class);
	}
}
