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

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.model.ModelPackage;

/**
 * This adapter intercepts the model changes and applies to the Java bean.
 */
public class InvocationAdapter extends TypedAdapter {
	private Object bean;

	/**
	 * Constructs an adapter instance.
	 * 
	 * @param bean
	 *            the target Java bean.
	 */
	public InvocationAdapter(Object bean) {
		this.bean = bean;
	}

	/**
	 * @return the bean
	 */
	public Object getBean() {
		return bean;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 */
	public void notifyChanged(Notification notification) {
		if (Notification.REMOVING_ADAPTER == notification.getEventType()) {
			return;
		}
		if (notification.getNotifier() != getTarget()) {
			return;
		}
		if (!(notification.getFeature() instanceof EStructuralFeature)) {
			Activator.warn("Invalid notification feature: " + notification);
			return;
		}
		if (!(notification.getNotifier() instanceof EditObject)) {
			Activator.warn("Invalid notifier: " + notification);
			return;
		}
		EObject eo = (EObject) notification.getNotifier();
		if (eo.eClass().getEStructuralFeature(
				ModelPackage.EDIT_OBJECT__CHILDREN) == notification
				.getFeature()) {
			handleStructuralChange(notification);
		} else {
			handlePropertyChange(notification);
		}
	}

	/**
	 * Handles the simple property changes.
	 * 
	 * @param notification
	 *            notification containing the changed property.
	 */
	protected void handlePropertyChange(Notification notification) {
	}

	/**
	 * Handles the structural changes.
	 * 
	 * @param notification
	 *            notification containing the changed structure.
	 */
	protected void handleStructuralChange(Notification notification) {
		switch (notification.getEventType()) {
		case Notification.ADD:
			add(notification.getNewValue(), notification.getPosition());
			break;
		case Notification.ADD_MANY:
			add((Collection<?>) notification.getNewValue(), notification
					.getPosition());
			break;
		case Notification.REMOVE:
			remove(notification.getOldValue());
			break;
		case Notification.REMOVE_MANY:
			remove((Collection<?>) notification.getOldValue());
			break;
		}
	}

	/**
	 * Called when a child has been added.
	 * 
	 * @param child
	 *            the child instance.
	 * @param position
	 *            the position of the added child.
	 */
	protected void add(Object child, int position) {
	}
	/**
	 * Adds multiple children at starting position.
	 * 
	 * @param children
	 *            the children to be added.
	 * @param position
	 *            the starting position.
	 */
	protected void add(Collection<?> children, int position) {
		for (Object child : children) {
			add(child, position++);
		}
	}

	/**
	 * Removes a child.
	 * 
	 * @param child
	 *            child object to be removed.
	 */
	protected void remove(Object child) {
	}

	/**
	 * Removes multiple children.
	 * 
	 * @param children
	 *            children to be removed.
	 */
	protected void remove(Collection<?> children) {
		for (Object child : children) {
			remove(child);
		}
	}

	/**
	 * Queries the current value for the specific feature.
	 * 
	 * @param feature
	 *            the feature with what the value is assoicated.
	 * @return the feature value.
	 */
	public Object getFeatureValue(EStructuralFeature feature) {
		return null;
	}
}
