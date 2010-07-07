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

package com.nokia.tools.packaging.commandline.commands;

import com.nokia.tools.packaging.commandline.util.CommandLineException;

/**
 * This class is the base interface for Command pattern heirarchy in this tool
 * All command should implement this class.
 * 
 * @author surmathe
 */
public interface ICommand {
	/**
	 * The mandatory function to be implemented by inherited classes. This
	 * functions should execute the command.
	 * 
	 * @throws CommandLineException
	 */
	public abstract void execute() throws CommandLineException;
}
