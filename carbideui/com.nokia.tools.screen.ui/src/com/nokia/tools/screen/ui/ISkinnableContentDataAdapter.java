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

import java.awt.datatransfer.Clipboard;
import org.eclipse.gef.commands.Command;

public interface ISkinnableContentDataAdapter {

	/**
	 * Determine if element has changed in comarison to its default skin
	 */
	public boolean isElementSkinned();

	/**
	 * Returns true if the element can be copied
	 * 
	 * @return
	 */
	public boolean isCopyEnabled();


	/**
	 * perform copy to clipboard operation on given entity. return true if copy
	 * was performed succesfully. If the
	 * 
	 * @param clipboard
	 *            is null, the system clipboard is used.
	 * 
	 * @param clipboard
	 * @return
	 */
	public boolean copyToClipboard(Clipboard clip);

	/**
     * Command for clearing of the already skinned element
	 */
	public Command getClearSkinnedElementCommand();
}
