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

package com.nokia.tools.screen.ui.editor.embedded;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.screen.ui.gef.PaletteManager;

public interface IEmbeddedEditorCustomizer {
	boolean isDefaultForContent(IContent content);
	
	void customizePalette(PaletteManager manager, IContent content);
	
	String getCategoryName(IContent content);
}
