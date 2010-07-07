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
package com.nokia.tools.editing.ui.part;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.model.EditDiagram;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.adapter.IComponentAdapter;

public class EditPartHelper {
	private EditPartHelper() {
	}

	public static List getModelChildren(EditPart part) {
		if (part.getModel() == null) {
			// not initialized
			return Collections.EMPTY_LIST;
		}
		if (part.getModel() instanceof EditObject) {
			EditObject bean = (EditObject) part.getModel();
			return bean.getChildren();
		} else if (part.getModel() instanceof EditDiagram) {
			EditDiagram bean = (EditDiagram) part.getModel();
			return bean.getEditObjects();
		}
		throw new IllegalArgumentException("Part has wrong data");
	}

	public static IComponentAdapter getComponentAdapter(EditPart part) {
		return getComponentAdapter((EObject) part.getModel());
	}

	public static IComponentAdapter getComponentAdapter(EObject eo) {
		IComponentAdapter adapter = (IComponentAdapter) EditingUtil.getAdapter(
				eo, IComponentAdapter.class);
		if (adapter == null) {
			adapter = IComponentAdapter.FULL_MODIFY_ADAPTER;
		}
		return adapter;
	}

	public static boolean isSelectable(EditPart part) {
		return getComponentAdapter(part).supports(IComponentAdapter.SELECTION,
				null);
	}

	public static boolean isSelectable(EObject eo) {
		return getComponentAdapter(eo).supports(IComponentAdapter.SELECTION,
				null);
	}

	public static void registerEditingAdapter(EditPart part) {
		EObject eo = (EObject) part.getModel();
		eo.eAdapters().add(new EditingAdapter(part));
	}

	public static void deregisterEditingAdapter(EditPart part) {
		EObject eo = (EObject) part.getModel();
		for (Iterator<Adapter> i = eo.eAdapters().iterator(); i.hasNext();) {
			Adapter adapter = i.next();
			if (adapter instanceof EditingAdapter
					&& ((EditingAdapter) adapter).getPart() == part) {
				i.remove();
			}
		}
	}
}
