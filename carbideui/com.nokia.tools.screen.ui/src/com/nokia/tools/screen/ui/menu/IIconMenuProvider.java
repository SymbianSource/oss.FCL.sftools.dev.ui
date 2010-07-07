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
package com.nokia.tools.screen.ui.menu;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.WorkbenchPart;

/**
 * Implementor must provide a methods to fill the context menus etc.
 *
 */
public interface IIconMenuProvider {

	public void fillIconContextMenu(IMenuManager manager, WorkbenchPart parent, String uiContext, CommandStack commandStack, IActionBars bars);

}
