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
package com.nokia.tools.screen.ui.editor;

import org.eclipse.ui.IEditorPart;

/**
 * marks editor, which offers advanced editing of some element
 * in 'parent' editor content. 
 * 
 * Used for identifying relation between parent and child editor, 
 * for example for showing right content in views.
 * 
 * Similar functionality like IContributedContentsView, but for editors.
 *
 */
public interface IContentDependentEditor {
	
	IEditorPart getParentEditor();

}
