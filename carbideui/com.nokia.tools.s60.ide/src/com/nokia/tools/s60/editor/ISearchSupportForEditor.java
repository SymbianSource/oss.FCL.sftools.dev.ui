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

package com.nokia.tools.s60.editor;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.Page;

public interface ISearchSupportForEditor {
	public Page createSearchPage(IEditorPart part);
}
