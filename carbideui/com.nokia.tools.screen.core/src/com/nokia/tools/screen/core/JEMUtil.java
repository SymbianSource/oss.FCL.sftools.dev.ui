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
package com.nokia.tools.screen.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.internal.ui.palette.editparts.ToolEntryEditPart;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.core.TypedAdapter;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.widget.IEnum;

/**
 * This class provides convenient methods for accessing target VM and JEM bean
 * objects.
 * 
 */
public class JEMUtil {

	public static IScreenElement getScreenElement(Object object) {
		if (object instanceof IStructuredSelection) {
			object = ((IStructuredSelection) object).getFirstElement();
		}
		if (object instanceof EditPart) {
			object = ((EditPart) object).getModel();
		}
		if (object instanceof IScreenElement) {
			return (IScreenElement) object;
		}
		if (object instanceof EObject) {
			return (IScreenElement) EcoreUtil.getExistingAdapter(
					(EObject) object, IScreenElement.class);
		}
		return null;
	}

	/**
	 * tries to lookup content data from given object
	 */
	public static IContentData getContentData(Object param) {
		if (param instanceof IStructuredSelection
				&& !((IStructuredSelection) param).isEmpty()) {
			param = ((IStructuredSelection) param).getFirstElement();
		}
		if (param instanceof IContentData) {
			return (IContentData) param;
		}
		if (param instanceof IScreenElement) {
			return ((IScreenElement) param).getData();
		}
		/* selection from layers view */
		if (param instanceof Object[]) {
			Object array[] = (Object[]) param;
			if (array.length > 2) {
				if (array[2] instanceof IContentData)
					return (IContentData) array[2];
				else if (array[2] instanceof EditPart)
					return getContentData(array[2]);
			}
		}

		/* code for palette viewer */
		if (param instanceof ToolEntryEditPart) {
			Object model = ((ToolEntryEditPart) param).getModel();
			if (model instanceof CombinedTemplateCreationEntry) {
				Object template = ((CombinedTemplateCreationEntry) model)
						.getTemplate();
				if (template instanceof IContentData)
					return (IContentData) template;
			}
		}
		IScreenElement element = getScreenElement(param);
		if (element != null) {
			return element.getData();
		}

		if (param instanceof EditPart) {
			Object model = ((EditPart) param).getModel();
			if (model instanceof EditObject) {
				IContentDataAdapter cd = (IContentDataAdapter) EcoreUtil
						.getExistingAdapter((EObject) model,
								IContentDataAdapter.class);
				if (cd != null) {
					return cd.getData();
				}
			}
		}

		return null;
	}

	public static IEnum getEnumeredObject(Class enumClass, String value) {
		if (value == null) {
			return null;
		}
		for (Field f : enumClass.getFields()) {
			if (Modifier.isPublic(f.getModifiers())
					&& Modifier.isStatic(f.getModifiers())
					&& Modifier.isFinal(f.getModifiers())
					&& f.getType() == enumClass) {
				try {
					IEnum enumObject = (IEnum) f.get(null);
					if (enumObject != null
							&& enumObject.toString().equals(value)) {
						return enumObject;
					}
				} catch (Exception e) {
					CorePlugin.error(e);
				}
			}
		}
		return null;
	}
}
