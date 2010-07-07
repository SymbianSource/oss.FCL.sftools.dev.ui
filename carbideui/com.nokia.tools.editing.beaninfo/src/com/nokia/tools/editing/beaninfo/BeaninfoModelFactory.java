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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.core.IEditingModelFactory;
import com.nokia.tools.editing.core.InvocationAdapter;
import com.nokia.tools.editing.model.EditDiagram;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.model.ModelFactory;

/**
 * Editing model factory implementation using the beaninfo-based introspection
 * mechanisms to populate features.
 * 
 */
public class BeaninfoModelFactory implements IEditingModelFactory {
	private static Map<Class, EClass> classMap = new HashMap<Class, EClass>();
	private static Set<EClass> resolvedClasses = new HashSet<EClass>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.core.IEditingModelFactory#createEditObject(java.lang.Object)
	 */
	public EditObject createEditObject(Object object) throws Exception {
		if (object == null) {
			return null;
		}
		if (object instanceof EditObject) {
			return (EditObject) object;
		}

		EClass ec = createEditClass(object.getClass(), true);
		EditObject eo = ModelFactory.eINSTANCE.createEditObject();
		((EObjectImpl) eo).eSetClass(ec);
		eo.eAdapters().add(createInvocationAdapter(object));

		return eo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.core.IEditingModelFactory#createDiagram()
	 */
	public EditDiagram createDiagram() {
		return ModelFactory.eINSTANCE.createEditDiagram();
	}

	/**
	 * Creates a new edit class by introspecting the given bean class.
	 * 
	 * @param clazz the bean class being introspected.
	 * @param resolveFeatures true to resolve features, false for references.
	 * @return the resolved {@link EClass}.
	 * @throws Exception if error occurred.
	 */
	@SuppressWarnings("unchecked")
	protected synchronized EClass createEditClass(Class clazz,
			boolean resolveFeatures) throws Exception {
		EClass ec = classMap.get(clazz);
		if (ec == null || !resolvedClasses.contains(ec)) {
			BeanInfo info = Introspector.getBeanInfo(clazz,
					Introspector.IGNORE_ALL_BEANINFO);

			ec = EcoreFactory.eINSTANCE.createEClass();
			ec.setName(clazz.getName());
			classMap.put(clazz, ec);

			if (resolveFeatures) {
				PropertyAdapter adapter = createPropertyAdapter();
				EditingUtil.initializeEditObjectClass(ec);

				for (PropertyDescriptor desc : info.getPropertyDescriptors()) {
					EStructuralFeature feature = EcoreFactory.eINSTANCE
							.createEReference();
					feature.setName(desc.getName());
					feature.setChangeable(desc.getWriteMethod() != null);
					feature.setUnsettable(feature.isChangeable());
					Class propertyClass = desc.getPropertyType();
					if (propertyClass == clazz) {
						feature.setEType(ec);
					} else if (propertyClass != null) {
						feature.setEType(createEditClass(propertyClass, false));
					}
					ec.getEStructuralFeatures().add(feature);
					adapter.register(feature, desc);
				}
				ec.eAdapters().add(adapter);
				resolvedClasses.add(ec);
			}
		}

		return ec;
	}

	/**
	 * @return a new property adapter.
	 */
	protected PropertyAdapter createPropertyAdapter() {
		return new PropertyAdapter();
	}

	/**
	 * Creates an invocation adapter.
	 * 
	 * @param object the target bean.
	 * @return the invocation adapter.
	 */
	protected InvocationAdapter createInvocationAdapter(Object object) {
		return new BeaninfoInvocationAdapter(object);
	}
}
