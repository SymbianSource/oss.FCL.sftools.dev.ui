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

import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;

public interface IEmbeddedEditorContributor {

	void contributeActions(IEditorPart mainEditor);

	void contributeContextMenu(ActionRegistry registry, IMenuManager menu);

	IEmbeddedEditorActionBarContributor getActionBarContributor();

	void contributeContextMenu(IWorkbenchPart part, IMenuManager menu);

}
