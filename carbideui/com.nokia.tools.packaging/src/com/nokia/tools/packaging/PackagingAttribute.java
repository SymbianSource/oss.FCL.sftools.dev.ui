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

/**
 * Attributes used in the packaging context.
 * 
 */
public enum PackagingAttribute {
	/**
	 * Input
	 */
	input,
	/**
	 * Output
	 */
	output,
	/**
	 * Working directory
	 */
	workingDir,
	/**
	 * Target platform
	 * 
	 * @see PackagingConstants
	 */
	platform,
	/**
	 * Theme content
	 */
	theme,
	/**
	 * Theme name
	 */
	themeName,
	/**
	 * Theme package name
	 */
	themePackageName,
	/**
	 * Theme item list file
	 */
	themeItemListFile,
	/**
	 * Theme DRM protection, value can be either "true" or "false"
	 */
	themeDRM,
	/**
	 * Theme normal selection, value can be either "true" or "false"
	 */
	themeNormalSelection,
	/**
	 * Absolute path of the generated SIS file.
	 */
	sisFile,
	/**
	 * Absolute path of the temporary SIS file.
	 */
	sisTempFile,
	/**
	 * Signs the package or not, value can be either "true" or "false"
	 */
	signPackage,
	/**
	 * Keeps the input sis file after new signed package has been created,
	 * "true" or "false"
	 */
	keepInputAfterSigning,
	/**
	 * Absolute path of the certificate file.
	 */
	certificateFile,
	/**
	 * Absolute path of the private key file.
	 */
	privateKeyFile,
	/**
	 * Passphrase of the private key
	 */
	passphrase,
	/**
	 * Key generation algorithm.
	 */
	algorithm,
	/**
	 * Embedded files
	 */
	embeddedFiles,
	/**
	 * Flag for standalone packaging or part of other packaging process
	 */
	isStandalone,
	/**
	 * Package statements
	 */
	packageStatements,
	/**
	 * Compression
	 */
	isCompressed,
	/**
	 * Vendor
	 */
	vendor,
	/**
	 * Vendor Icon
	 */
	vendorIcon,
	
	/**
	 * Primary Model Id.
	 */
	primaryModelId,
	/**
	 * Secondary Model Id.
	 */
	secondaryModelId
}
