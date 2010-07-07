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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.gef.tools.DragTreeItemsTracker;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.adapter.IComponentAdapter;
import com.nokia.tools.resource.util.DebugHelper;

public class DefaultTreeEditPart extends AbstractTreeEditPart {
	private LabelProvider labelProvider;

	public DefaultTreeEditPart(EditObject bean) {
		setModel(bean);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractTreeEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		IComponentAdapter adapter = EditPartHelper.getComponentAdapter(this);

		if (adapter.supports(IComponentAdapter.MODIFY, null)) {
			installEditPolicy(EditPolicy.COMPONENT_ROLE,
					new DefaultComponentEditPolicy());
		}
		if (adapter.supports(IComponentAdapter.ADD_CHILD
				| IComponentAdapter.REMOVE_CHILD, null)) {
			installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE,
					new TreePrimaryDragRoleEditPolicy());
		}
		if (isContainer()) {
			installEditPolicy(EditPolicy.TREE_CONTAINER_ROLE,
					new DefaultTreeContainerEditPolicy());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		EditPartHelper.registerEditingAdapter(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#deactivate()
	 */
	@Override
	public void deactivate() {
		super.deactivate();
		if (labelProvider != null) {
			labelProvider.dispose();
		}
		EditPartHelper.deregisterEditingAdapter(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#isSelectable()
	 */
	@Override
	public boolean isSelectable() {
		return EditPartHelper.isSelectable(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.EditPart#getDragTracker(org.eclipse.gef.Request)
	 */
	public DragTracker getDragTracker(Request request) {
		return new DragTreeItemsTracker(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	@Override
	protected List getModelChildren() {
		List children = EditPartHelper.getModelChildren(this);
		if (children == null) {
			return Collections.EMPTY_LIST;
		}
		List<Object> listToRet = new ArrayList<Object>(children.size());
		for (Object element : children) {
			if (DebugHelper.debugUi()
					|| EditPartHelper.isSelectable((EObject) element)) {
				listToRet.add(element);
			}
		}
		return listToRet;
	}

	/**
	 * @return the labelProvider
	 */
	public LabelProvider getLabelProvider() {
		return labelProvider;
	}

	/**
	 * @param labelProvider the labelProvider to set
	 */
	public void setLabelProvider(LabelProvider labelProvider) {
		if (this.labelProvider != labelProvider) {
			if (this.labelProvider != null) {
				this.labelProvider.dispose();
			}
			this.labelProvider = labelProvider;
			if (isActive()) {
				refresh();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractTreeEditPart#getImage()
	 */
	@Override
	protected Image getImage() {
		return labelProvider == null ? super.getImage() : labelProvider
				.getImage(getModel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractTreeEditPart#getText()
	 */
	@Override
	protected String getText() {
		return labelProvider == null ? super.getText() : labelProvider
				.getText(getModel());
	}

	protected boolean isContainer() {
		return false;
	}
}
