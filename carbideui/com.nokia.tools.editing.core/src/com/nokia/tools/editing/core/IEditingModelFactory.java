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
package com.nokia.tools.editing.core;

import com.nokia.tools.editing.model.EditDiagram;
import com.nokia.tools.editing.model.EditObject;

/**
 * Factory interface for creating the edit objects and diagrams.
 * 
 */
public interface IEditingModelFactory {
	/**
	 * Creates a new edit object for a non-Ecore object. Usually the
	 * implementation will initialize the edit object with proper adapters.
	 * 
	 * @param object
	 *            a non-Ecore object.
	 * @return the corresponding Ecore object.
	 * @throws Exception
	 *             if creation failed.
	 */
	EditObject createEditObject(Object object) throws Exception;

	/**
	 * Creates a new diagram instance.
	 * 
	 * @return the new diagram instance.
	 * @throws Exception
	 *             if creation failed.
	 */
	EditDiagram createDiagram() throws Exception;
}
