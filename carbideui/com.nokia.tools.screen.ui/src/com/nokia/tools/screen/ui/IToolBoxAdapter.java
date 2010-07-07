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
package com.nokia.tools.screen.ui;

/**
 * Accessibility interface for theme studio model's Toolbox object
 */
public interface IToolBoxAdapter {

	boolean isMultipleLayersSupport();

	boolean isEffectsSupport();
	
	boolean isFile();
	
	boolean isText();
	
	boolean supportMask();
	
	boolean supportSoftMask();

}
