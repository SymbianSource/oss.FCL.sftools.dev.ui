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

package com.nokia.tools.screen.ui.extension;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public interface IContributorDescriptor {
	String OPERATION_NEW_PROJECT = "newProject";
	String OPERATION_OPEN_PROJECT = "openProject";
	String OPERATION_CREATE_PROJECT = "createProject";
	String OPERATION_IMPORT_PROJECT = "importProject";
	String OPERATION_EXPORT_PROJECT = "exportProject";

	WorkspaceModifyOperation createOperation(String type);

	String getClasspathContainer();

	Action createAction(String type);
}
