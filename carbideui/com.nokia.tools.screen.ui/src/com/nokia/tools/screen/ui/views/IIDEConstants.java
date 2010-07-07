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
package com.nokia.tools.screen.ui.views;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;

public interface IIDEConstants {	
	
	String PLUGIN_ID = "com.nokia.tools.screen.ui";
	
	QualifiedName CONTENTS_NAME = new QualifiedName(PLUGIN_ID, "contents");
	
	/**
	 * Syncronization on or off?
	 */
	String PREF_SYNC_WITH_EDITOR = PLUGIN_ID + ".activate.sync";
	
	/**
	 * Color of border used to mark the focused items.
	 */
	int BORDER_COLOR = SWT.COLOR_BLACK;
	
	/**
	 * Color of overriden elements.
	 */
	int COLOR_OVERRIDE = SWT.COLOR_BLUE;
	
}
