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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see com.nokia.tools.editing.model.ModelFactory
 * @model kind="package"
 * @generated
 */
public interface ModelPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "model";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http:///com/nokia/tools/editing/model.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "com.nokia.tools.editing.model";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ModelPackage eINSTANCE = com.nokia.tools.editing.model.impl.ModelPackageImpl.init();

	/**
	 * The meta object id for the '{@link com.nokia.tools.editing.model.impl.EditObjectImpl <em>Edit Object</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see com.nokia.tools.editing.model.impl.EditObjectImpl
	 * @see com.nokia.tools.editing.model.impl.ModelPackageImpl#getEditObject()
	 * @generated
	 */
	int EDIT_OBJECT = 0;

	/**
	 * The feature id for the '<em><b>Diagram</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDIT_OBJECT__DIAGRAM = 0;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDIT_OBJECT__CHILDREN = 1;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDIT_OBJECT__PARENT = 2;

	/**
	 * The number of structural features of the '<em>Edit Object</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDIT_OBJECT_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link com.nokia.tools.editing.model.impl.EditDiagramImpl <em>Edit Diagram</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see com.nokia.tools.editing.model.impl.EditDiagramImpl
	 * @see com.nokia.tools.editing.model.impl.ModelPackageImpl#getEditDiagram()
	 * @generated
	 */
	int EDIT_DIAGRAM = 1;

	/**
	 * The feature id for the '<em><b>Edit Objects</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDIT_DIAGRAM__EDIT_OBJECTS = 0;

	/**
	 * The number of structural features of the '<em>Edit Diagram</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDIT_DIAGRAM_FEATURE_COUNT = 1;


	/**
	 * Returns the meta object for class '{@link com.nokia.tools.editing.model.EditObject <em>Edit Object</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Edit Object</em>'.
	 * @see com.nokia.tools.editing.model.EditObject
	 * @generated
	 */
	EClass getEditObject();

	/**
	 * Returns the meta object for the container reference '{@link com.nokia.tools.editing.model.EditObject#getDiagram <em>Diagram</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Diagram</em>'.
	 * @see com.nokia.tools.editing.model.EditObject#getDiagram()
	 * @see #getEditObject()
	 * @generated
	 */
	EReference getEditObject_Diagram();

	/**
	 * Returns the meta object for the containment reference list '{@link com.nokia.tools.editing.model.EditObject#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Children</em>'.
	 * @see com.nokia.tools.editing.model.EditObject#getChildren()
	 * @see #getEditObject()
	 * @generated
	 */
	EReference getEditObject_Children();

	/**
	 * Returns the meta object for the container reference '{@link com.nokia.tools.editing.model.EditObject#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Parent</em>'.
	 * @see com.nokia.tools.editing.model.EditObject#getParent()
	 * @see #getEditObject()
	 * @generated
	 */
	EReference getEditObject_Parent();

	/**
	 * Returns the meta object for class '{@link com.nokia.tools.editing.model.EditDiagram <em>Edit Diagram</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Edit Diagram</em>'.
	 * @see com.nokia.tools.editing.model.EditDiagram
	 * @generated
	 */
	EClass getEditDiagram();

	/**
	 * Returns the meta object for the containment reference list '{@link com.nokia.tools.editing.model.EditDiagram#getEditObjects <em>Edit Objects</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Edit Objects</em>'.
	 * @see com.nokia.tools.editing.model.EditDiagram#getEditObjects()
	 * @see #getEditDiagram()
	 * @generated
	 */
	EReference getEditDiagram_EditObjects();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ModelFactory getModelFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link com.nokia.tools.editing.model.impl.EditObjectImpl <em>Edit Object</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see com.nokia.tools.editing.model.impl.EditObjectImpl
		 * @see com.nokia.tools.editing.model.impl.ModelPackageImpl#getEditObject()
		 * @generated
		 */
		EClass EDIT_OBJECT = eINSTANCE.getEditObject();

		/**
		 * The meta object literal for the '<em><b>Diagram</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EDIT_OBJECT__DIAGRAM = eINSTANCE.getEditObject_Diagram();

		/**
		 * The meta object literal for the '<em><b>Children</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EDIT_OBJECT__CHILDREN = eINSTANCE.getEditObject_Children();

		/**
		 * The meta object literal for the '<em><b>Parent</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EDIT_OBJECT__PARENT = eINSTANCE.getEditObject_Parent();

		/**
		 * The meta object literal for the '{@link com.nokia.tools.editing.model.impl.EditDiagramImpl <em>Edit Diagram</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see com.nokia.tools.editing.model.impl.EditDiagramImpl
		 * @see com.nokia.tools.editing.model.impl.ModelPackageImpl#getEditDiagram()
		 * @generated
		 */
		EClass EDIT_DIAGRAM = eINSTANCE.getEditDiagram();

		/**
		 * The meta object literal for the '<em><b>Edit Objects</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EDIT_DIAGRAM__EDIT_OBJECTS = eINSTANCE.getEditDiagram_EditObjects();

	}

} //ModelPackage
