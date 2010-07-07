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
package com.nokia.tools.theme.ecore;

import org.eclipse.emf.ecore.util.EcoreUtil;

import com.nokia.tools.editing.beaninfo.BeaninfoModelFactory;
import com.nokia.tools.editing.core.EditObjectAdapter;
import com.nokia.tools.editing.core.InvocationAdapter;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.platform.layout.LayoutNode;

public class LayoutModelFactory extends BeaninfoModelFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.beaninfo.BeaninfoModelFactory#createInvocationAdapter(java.lang.Object)
	 */
	@Override
	protected InvocationAdapter createInvocationAdapter(Object object) {
		return new LayoutInvocationAdapter(object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.beaninfo.BeaninfoModelFactory#createEditObject(java.lang.Object)
	 */
	@Override
	public EditObject createEditObject(Object object) throws Exception {
		EditObject target = super.createEditObject(object);
		return target;
	}

	public static EditObject createEditTree(LayoutNode node,
			EditObject previewEditObject) throws Exception {
		final EditObject parent = new LayoutModelFactory()
				.createEditObject(node);

		InvocationAdapter adapter = (InvocationAdapter) EcoreUtil
				.getExistingAdapter(parent, InvocationAdapter.class);
		if (adapter != null) {
			parent.eAdapters().remove(adapter);
		}

		LayoutNode child = node.getChild();
		if (child != null) {
			final EditObject childObject = createEditTree(child,
					previewEditObject);
			EditObjectAdapter childAdapter = (EditObjectAdapter) EcoreUtil
					.getExistingAdapter(childObject, EditObjectAdapter.class);
			childAdapter.selfDispatch(new Runnable() {
				public void run() {
					parent.getChildren().add(childObject);
				}
			});
		}
		parent.eAdapters().add(adapter);
		parent.eAdapters().add(new LayoutEditObjectAdapter(previewEditObject));
		return parent;
	}
}
