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
 * A representation of the model object '<em><b>Edit Object</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link com.nokia.tools.editing.model.EditObject#getDiagram <em>Diagram</em>}</li>
 *   <li>{@link com.nokia.tools.editing.model.EditObject#getChildren <em>Children</em>}</li>
 *   <li>{@link com.nokia.tools.editing.model.EditObject#getParent <em>Parent</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.nokia.tools.editing.model.ModelPackage#getEditObject()
 * @model
 * @generated
 */
public interface EditObject extends EObject {
	/**
	 * Returns the value of the '<em><b>Diagram</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link com.nokia.tools.editing.model.EditDiagram#getEditObjects <em>Edit Objects</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Diagram</em>' container reference.
	 * @see #setDiagram(EditDiagram)
	 * @see com.nokia.tools.editing.model.ModelPackage#getEditObject_Diagram()
	 * @see com.nokia.tools.editing.model.EditDiagram#getEditObjects
	 * @model opposite="editObjects"
	 * @generated
	 */
	EditDiagram getDiagram();

	/**
	 * Sets the value of the '{@link com.nokia.tools.editing.model.EditObject#getDiagram <em>Diagram</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Diagram</em>' container reference.
	 * @see #getDiagram()
	 * @generated
	 */
	void setDiagram(EditDiagram value);

	/**
	 * Returns the value of the '<em><b>Children</b></em>' containment reference list.
	 * The list contents are of type {@link com.nokia.tools.editing.model.EditObject}.
	 * It is bidirectional and its opposite is '{@link com.nokia.tools.editing.model.EditObject#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * 
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children</em>' containment reference list.
	 * @see com.nokia.tools.editing.model.ModelPackage#getEditObject_Children()
	 * @see com.nokia.tools.editing.model.EditObject#getParent
	 * @model opposite="parent" containment="true"
	 * @generated
	 */
	EList<EditObject> getChildren();

	/**
	 * Returns the value of the '<em><b>Parent</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link com.nokia.tools.editing.model.EditObject#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent</em>' container reference.
	 * @see #setParent(EditObject)
	 * @see com.nokia.tools.editing.model.ModelPackage#getEditObject_Parent()
	 * @see com.nokia.tools.editing.model.EditObject#getChildren
	 * @model opposite="children"
	 * @generated
	 */
	EditObject getParent();

	/**
	 * Sets the value of the '{@link com.nokia.tools.editing.model.EditObject#getParent <em>Parent</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent</em>' container reference.
	 * @see #getParent()
	 * @generated
	 */
	void setParent(EditObject value);

} // EditObject
