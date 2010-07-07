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

package com.nokia.tools.s60.editor.dnd;

import java.awt.datatransfer.Clipboard;

import com.nokia.tools.s60.editor.actions.CopyImageAction;
import com.nokia.tools.s60.editor.actions.PasteImageAction;
import com.nokia.tools.screen.ui.utils.SimpleSelectionProvider;

public class DNDUtil {

	public static boolean isSelectionDraggeable(Object dragSourceData){
		return new CopyImageAction(new SimpleSelectionProvider(dragSourceData), null).isEnabled();
	}
	
	public static boolean draggedObjectCanBeDropped(Object element, Clipboard clip){
		PasteImageAction pasteTest = new PasteImageAction(new SimpleSelectionProvider(element) , null, clip);
		return pasteTest.isEnabled();
	}
	
}
