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
package com.nokia.tools.editing.beaninfo;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.nokia.tools.editing.core.InvocationAdapter;

/**
 * Invocation adapter using the beaninfo for accessing the feature values.
 * 
 */
public class BeaninfoInvocationAdapter extends InvocationAdapter {
	/**
	 * Creates a new adapter instance.
	 * 
	 * @param bean the target bean to invoke on.
	 */
	public BeaninfoInvocationAdapter(Object bean) {
		super(bean);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.core.InvocationAdapter#handlePropertyChange(org.eclipse.emf.common.notify.Notification)
	 */
	@Override
	protected void handlePropertyChange(Notification notification) {
		EStructuralFeature sf = (EStructuralFeature) notification.getFeature();
		if (sf == null
				|| !sf.isChangeable()
				|| (notification.getEventType() != Notification.SET && notification
						.getEventType() != Notification.UNSET)) {
			return;
		}
		EObject target = (EObject) notification.getNotifier();
		PropertyAdapter propertyAdapter = getPropertyAdapter(target);
		if (propertyAdapter == null) {
			return;
		}
		PropertyDescriptor desc = propertyAdapter.getPropertyDescriptor(sf);
		if (desc == null) {
			// static features
			return;
		}

		Method method = desc.getWriteMethod();
		if (method == null) {
			Activator.warn("Write method is not available: " + desc);
			return;
		}
		Object newValue = notification.getNewValue();
		try {
			method.invoke(getBean(), new Object[] { newValue });
		} catch (Exception e) {
			Activator.error(e);
		}
	}

	/**
	 * Finds the property adapter from the given edit object. The property
	 * adapter shall be registered to the target's metadata class.
	 * 
	 * @param target the edit object to query for the property adapter.
	 * @return the property adapter if available.
	 */
	protected PropertyAdapter getPropertyAdapter(EObject target) {
		PropertyAdapter propertyAdapter = (PropertyAdapter) EcoreUtil
				.getExistingAdapter(target.eClass(), PropertyAdapter.class);
		if (propertyAdapter == null) {
			Activator.warn("Property adapter is not available for: "
					+ target.eClass());
		}
		return propertyAdapter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.core.InvocationAdapter#getFeatureValue(org.eclipse.emf.ecore.EStructuralFeature)
	 */
	@Override
	public Object getFeatureValue(EStructuralFeature feature) {
		PropertyAdapter adapter = getPropertyAdapter((EObject) getTarget());
		if (adapter == null) {
			return null;
		}
		PropertyDescriptor descriptor = adapter.getPropertyDescriptor(feature);
		if (descriptor == null) {
			return null;
		}
		Method method = descriptor.getReadMethod();
		if (method == null) {
			return null;
		}
		try {
			return method.invoke(getBean(), (Object[]) null);
		} catch (Exception e) {
			Activator.error(e);
		}
		return null;
	}
}
