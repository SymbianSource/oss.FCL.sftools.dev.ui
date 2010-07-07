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

import org.eclipse.ui.ISelectionListener;

/**
 * 
 * This interface is itended to be used as an adapter in the editor.
 * 
 * The purpose of this class is to provide from editor an object
 * which will be responsible for synchronization of the PaletteViewer
 * according to the selection changes made in the UI.
 * 
s */
public interface IResourceViewPartSelectionListenerSupport {
	/**
	 * Returns a listener which take care of the synchronization of the PaletteViewer's 
	 * state with other views and editor. So when user selects some sound in sound editor, 
	 * the PaletteViewer will show the appropriate category of it and so on.
	 * 
	 * @param viewer PaletteViewer which displays the Theme Content. 
	 * @return SelectionListener which takes care of synchronizing of PaletteViewer 
	 * the selection in the workbench. 
	 */
	public ISelectionListener getSelectionListenerForResourceView(ResourceViewer viewer);
}
