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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.nokia.tools.editing.core.TypedAdapter;

/**
 * This adapter stores the mapping from the structural feature to the bean
 * property descriptor.
 * 
 */
public class PropertyAdapter extends TypedAdapter {
	private Map<EStructuralFeature, PropertyDescriptor> properties = new HashMap<EStructuralFeature, PropertyDescriptor>();

	/**
	 * Registers a new mapping.
	 * 
	 * @param sf the structural feature.
	 * @param desc the corresponding property descriptor.
	 */
	public void register(EStructuralFeature sf, PropertyDescriptor desc) {
		properties.put(sf, desc);
	}

	/**
	 * Returns the property descriptor matching the given structural feature.
	 * 
	 * @param sf the structural feature.
	 * @return the property descriptor if a matched one found, null otherwise.
	 */
	public PropertyDescriptor getPropertyDescriptor(EStructuralFeature sf) {
		return properties.get(sf);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 */
	public void notifyChanged(Notification notification) {
	}
}
