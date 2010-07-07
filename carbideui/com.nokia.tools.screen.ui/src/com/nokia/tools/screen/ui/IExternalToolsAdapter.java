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
 * Implementation of this adapter should know, how to haldle itself in the external tools
 *
 */
public interface IExternalToolsAdapter {

	/**
	 * Implementation should know how to open itself in the external tool.
	 * @param pathToFile path to the file, which has to be opened in external tool
	 * @return true if nothing obstructs it to run and the operation was successful
	 */
	public boolean openInExternalTool(String pathToFile);
	
	/**
	 * Determinates if the file specified by the path can be opened
	 * @param pathToFile
	 * @return
	 */
	public boolean canBeOpened(String pathToFile);
}
