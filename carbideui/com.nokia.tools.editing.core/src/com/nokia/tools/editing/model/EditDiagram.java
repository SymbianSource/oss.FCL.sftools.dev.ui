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
package com.nokia.tools.editing.model;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Edit Diagram</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link com.nokia.tools.editing.model.EditDiagram#getEditObjects <em>Edit Objects</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.nokia.tools.editing.model.ModelPackage#getEditDiagram()
 * @model
 * @generated
 */
public interface EditDiagram extends EObject {
	/**
	 * Returns the value of the '<em><b>Edit Objects</b></em>' containment reference list.
	 * The list contents are of type {@link com.nokia.tools.editing.model.EditObject}.
	 * It is bidirectional and its opposite is '{@link com.nokia.tools.editing.model.EditObject#getDiagram <em>Diagram</em>}'.
	 * <!-- begin-user-doc -->
	 * 
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Edit Objects</em>' containment reference list.
	 * @see com.nokia.tools.editing.model.ModelPackage#getEditDiagram_EditObjects()
	 * @see com.nokia.tools.editing.model.EditObject#getDiagram
	 * @model opposite="diagram" containment="true"
	 * @generated
	 */
	EList<EditObject> getEditObjects();

} // EditDiagram
