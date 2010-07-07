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
package com.nokia.tools.theme.s60.packaging;

/**
 * Defines the constants for the various executables that will be used
 * during the packaging process for an S60 Theme. These constants will
 * be used to fetch the appropriate executables path to be used, by dynamically
 * fetching the path information from the extension point processing.
 * 
 * These executables are then used in the specific packaging processor.
 * 
 *
 */
public enum PackagingExecutableType {

	/* Constant for the executable for Bitmap converter. */
	BITMAP_CONVERTER,
	/* Constant for the executable for the skin compiler */
	SKIN_COMPILER,
	/* Constant for the executable for the creation of the SIS file */
	SIS_CREATOR,
	/* Constant for the executable for signing the created sis file */
	SIS_SIGNER,
	/* Constant for the executable which is used for creation of MIF file */
	MIF_CONVERTER,
	/* Constant for the executable which is used for SVG to SVGT binary conversion*/
	SVGT_BIN_ENCODE
}
