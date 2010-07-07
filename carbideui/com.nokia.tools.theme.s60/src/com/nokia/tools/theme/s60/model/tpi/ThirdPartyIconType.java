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
package com.nokia.tools.theme.s60.model.tpi;

/**
 * This enumeration defines the nature of the Third Party Icon instance being 
 * created. As of now there are 2 types of Third Party Icons [Tool specific and 
 * Theme Specific] and this enumeration defines the constants for these.
 */
public enum ThirdPartyIconType {
	THEME_SPECIFIC, /* Corresponding to Theme specific icon. */
	TOOL_SPECIFIC   /* Corresponding to Tool specific icon. */
}
