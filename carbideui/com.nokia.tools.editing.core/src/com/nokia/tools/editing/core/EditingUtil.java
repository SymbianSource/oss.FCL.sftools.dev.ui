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

import java.awt.Rectangle;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.nokia.tools.editing.model.EditDiagram;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.model.ModelPackage;

/**
 * Utility methods for accessing the edit objects.
 * 

 
 */
public class EditingUtil {
	/**
	 * Built-in <code>bounds</code> property.
	 */
	public static final String SF_BOUNDS = "bounds";

	/**
	 * No instantiation.
	 */
	private EditingUtil() {
	}

	/**
	 * Extracts the normal JavaBean from the given edit object.
	 * 
	 * @param eo
	 *            the edit object to examine.
	 * @return the JavaBean if the edit object is a proxy for a bean, null
	 *         otherwise.
	 */
	public static Object getBean(EObject eo) {
		if (eo == null) {
			return null;
		}
		InvocationAdapter adapter = (InvocationAdapter) EcoreUtil
				.getExistingAdapter(eo, InvocationAdapter.class);
		if (adapter != null) {
			return adapter.getBean();
		}
		return null;
	}

	/**
	 * Finds the diagram which holds the root of the edit objec tree.
	 * 
	 * @param eo
	 *            the edit object in the tree.
	 * @return the diagram if it exists, null otherwise.
	 */
	public static EditDiagram getDiagram(EditObject eo) {
		EObject root = EcoreUtil.getRootContainer(eo);
		if (root instanceof EditDiagram) {
			return (EditDiagram) root;
		}
		return null;
	}

	/**
	 * Returns the feature value.
	 * 
	 * @param eo
	 *            the edit object.
	 * @param featureName
	 *            name of the feature.
	 * @return the value of the feature.
	 */
	public static Object getFeatureValue(EObject eo, String featureName) {
		EStructuralFeature feature = getFeature(eo, featureName);
		return getFeatureValue(eo, feature);
	}

	/**
	 * Returns the feature value.
	 * 
	 * @param eo
	 *            the edit object.
	 * @param feature
	 *            the structural feature.
	 * @return the value of the given feature.
	 */
	public static Object getFeatureValue(EObject eo, EStructuralFeature feature) {
		if (feature != null) {
			InvocationAdapter adapter = (InvocationAdapter) EcoreUtil
					.getExistingAdapter(eo, InvocationAdapter.class);
			if (adapter != null) {
				Object value = adapter.getFeatureValue(feature);
				if (value != null) {
					return value;
				}
			}
			return eo.eGet(feature);
		}
		return null;
	}

	/**
	 * Sets the value to the specific feature.
	 * 
	 * @param eo
	 *            the edit object.
	 * @param featureName
	 *            name of the feature.
	 * @param value
	 *            the feature value to set.
	 */
	public static void setFeatureValue(EObject eo, String featureName,
			Object value) {
		EStructuralFeature feature = getFeature(eo, featureName);
		setFeatureValue(eo, feature, value);
	}

	/**
	 * Sets the value to the specific feature.
	 * 
	 * @param eo
	 *            the edit object.
	 * @param feature
	 *            the feature.
	 * @param value
	 *            the feature value to set.
	 */
	public static void setFeatureValue(EObject eo, EStructuralFeature feature,
			Object value) {
		if (feature != null) {
			eo.eSet(feature, value);
		}
	}

	/**
	 * Finds the feature of the specific name.
	 * 
	 * @param eo
	 *            the edit object.
	 * @param featureName
	 *            name of the feature.
	 * @return the feature or null if doesn't exist.
	 */
	public static EStructuralFeature getFeature(EObject eo, String featureName) {
		if (eo == null || eo.eClass() == null) {

			return null;
		}
		return eo.eClass().getEStructuralFeature(featureName);
	}

	/**
	 * Finds the {@link SF_BOUNDS} feature.
	 * 
	 * @param eo
	 *            the edit object to examine.
	 * @return the bounds feature if exists, null otherwise.
	 */
	public static EStructuralFeature getBoundsFeature(EObject eo) {
		return eo.eClass().getEStructuralFeature(SF_BOUNDS);
	}

	/**
	 * Gets the bounds feature value.
	 * 
	 * @param eo
	 *            the edit object.
	 * @return the bounds.
	 */
	public static Rectangle getBounds(EObject eo) {
		return (Rectangle) getFeatureValue(eo, SF_BOUNDS);
	}

	/**
	 * Finds the containment feature of the given edit object.
	 * 
	 * @param eo
	 *            the edit object.
	 * @return the containment feature or null if the object is not an edit
	 *         object.
	 */
	public static EStructuralFeature getContainmentFeature(EObject eo) {
		return eo.eClass().getEStructuralFeature(
				ModelPackage.EDIT_OBJECT__CHILDREN);
	}

	/**
	 * Finds the children of the given edit object.
	 * 
	 * @param eo
	 *            the parent edit object.
	 * @return the children of the given edit object.
	 */
	public static List getChildren(EObject eo) {
		EStructuralFeature feature = getContainmentFeature(eo);
		if (feature != null) {
			return (List) eo.eGet(feature);
		}
		return null;
	}

	/**
	 * Finds adapter for the given type by querying all existing EMF adapters of
	 * the specific edit object.
	 * 
	 * @param eo
	 *            the edit object.
	 * @param type
	 *            type of the adapter.
	 * @return the adapter instance or null if not found.
	 */
	public static Object getAdapter(EObject eo, Class type) {
		for (Adapter adapter : eo.eAdapters()) {
			if (adapter instanceof IAdaptable) {
				Object value = ((IAdaptable) adapter).getAdapter(type);
				if (value != null) {
					return value;
				}
			}
		}
		return null;
	}

	/**
	 * Tests whether the specific feature is dynamic feature or not.
	 * 
	 * @param feature
	 *            the feature to test.
	 * @return true if the feature is dynamic, false otherwise.
	 */
	public static boolean isDynamicFeature(EStructuralFeature feature) {
		return feature != null
				&& feature.getFeatureID() >= ModelPackage.EDIT_OBJECT_FEATURE_COUNT;
	}

	/**
	 * Tests if the given feature is a valid editing feature.
	 * 
	 * @param feature
	 *            the feature to test.
	 * @return true if the feature is either a dynamic feature or the
	 *         containment feature.
	 */
	public static boolean isValidFeature(EStructuralFeature feature) {
		return feature != null
				&& (isDynamicFeature(feature) || ModelPackage.EDIT_OBJECT__CHILDREN == feature
						.getFeatureID());
	}

	/**
	 * Tests if the notification is triggered from adapter being removed.
	 * 
	 * @param notification
	 *            the event notification.
	 * @param adapter
	 *            the adapter to test.
	 * @return true if the adapter is removed from the target.
	 */
	public static boolean isRemovingAdapter(Notification notification,
			Adapter adapter) {
		return (Notification.REMOVING_ADAPTER == notification.getEventType()
				&& notification.getNewValue() == null && notification
				.getOldValue() == adapter);
	}

	/**
	 * Creates a dynamic {@link EClass} for use with the edit objects. This
	 * class inherits the static features and ensures they are in the proper
	 * positions. This should be called before adding new dynamic features to
	 * the edit object class.
	 * 
	 * @return a new {@link EClass}.
	 */
	public static void initializeEditObjectClass(EClass ec) {
		ec.getEStructuralFeatures().clear();

		EClass editObjectClass = ModelPackage.eINSTANCE.getEditObject();
		// this is purely dynamic class but we still model it, thus we
		// need to inherit the features from the static class
		for (int i = 0; i < ModelPackage.EDIT_OBJECT_FEATURE_COUNT; i++) {
			EStructuralFeature feature = editObjectClass
					.getEStructuralFeature(i);
			ec.getEStructuralFeatures().add(
					(EStructuralFeature) EcoreUtil.copy(feature));
		}
	}
}
