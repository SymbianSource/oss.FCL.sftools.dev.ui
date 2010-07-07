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

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.TreeEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.TreeContainerEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.ui.adapter.IComponentAdapter;
import com.nokia.tools.editing.ui.command.AddChildCommand;
import com.nokia.tools.editing.ui.command.CommandBuilder;
import com.nokia.tools.editing.ui.command.Messages;
import com.nokia.tools.editing.ui.command.RemoveChildCommand;

public class DefaultTreeContainerEditPolicy extends TreeContainerEditPolicy {
	private static final java.awt.Rectangle DEFAULT_BOUNDS = new java.awt.Rectangle(
			0, 0, 20, 20);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.TreeContainerEditPolicy#eraseTargetFeedback(org.eclipse.gef.Request)
	 */
	public void eraseTargetFeedback(Request req) {
		getTree().setInsertMark(null, true);
	}

	/**
	 * Get the tree widget
	 * 
	 * @return tree widget
	 */
	private Tree getTree() {
		Widget widget = ((TreeEditPart) getHost()).getWidget();
		if (widget instanceof Tree)
			return (Tree) widget;

		return ((TreeItem) widget).getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.TreeContainerEditPolicy#getAddCommand(org.eclipse.gef.requests.ChangeBoundsRequest)
	 */
	@Override
	protected Command getAddCommand(ChangeBoundsRequest request) {
		EObject newParent = (EObject) getHost().getModel();
		EStructuralFeature newContainment = EditingUtil
				.getContainmentFeature(newParent);
		IComponentAdapter adapter = EditPartHelper
				.getComponentAdapter(newParent);
		int index = findIndexOfTreeItemAt(request.getLocation());
		CommandBuilder builder = new CommandBuilder(Messages.Command_AddChild);
		for (Object obj : request.getEditParts()) {
			EObject child = (EObject) ((EditPart) obj).getModel();
			EObject oldParent = child.eContainer();
			IComponentAdapter oldAdapter = (IComponentAdapter) EditPartHelper
					.getComponentAdapter(oldParent);
			if (oldAdapter.supports(IComponentAdapter.REMOVE_CHILD, child)
					&& adapter.supports(IComponentAdapter.ADD_CHILD, child)) {
				EStructuralFeature oldContainment = EditingUtil
						.getContainmentFeature(oldParent);
				if (oldContainment != null && newContainment != null) {
					builder.applyFeature(new RemoveChildCommand(), oldParent,
							oldContainment, child);
					builder.applyFeature(new AddChildCommand(index), newParent,
							newContainment, child);
				}
			}
		}
		return builder.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.TreeContainerEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
	 */
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		EObject child = (EObject) request.getNewObject();
		EObject parent = (EObject) getHost().getModel();
		IComponentAdapter adapter = EditPartHelper.getComponentAdapter(parent);
		if (!adapter.supports(IComponentAdapter.ADD_CHILD, child)) {
			return UnexecutableCommand.INSTANCE;
		}

		EStructuralFeature containment = EditingUtil
				.getContainmentFeature(parent);
		if (containment == null) {
			return UnexecutableCommand.INSTANCE;
		}

		int index = findIndexOfTreeItemAt(request.getLocation());

		CommandBuilder builder = new CommandBuilder(Messages.Command_AddChild,
				false);
		// set bounds first, then attach to parent
		builder.applyFeature(child, EditingUtil.getBoundsFeature(child),
				DEFAULT_BOUNDS);
		builder.applyFeature(new AddChildCommand(index), parent, containment,
				child);
		return builder.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.TreeContainerEditPolicy#getMoveChildrenCommand(org.eclipse.gef.requests.ChangeBoundsRequest)
	 */
	@Override
	protected Command getMoveChildrenCommand(ChangeBoundsRequest request) {
		EObject parent = (EObject) getHost().getModel();
		EStructuralFeature containment = EditingUtil
				.getContainmentFeature(parent);
		List children = EditingUtil.getChildren(parent);
		int newIndex = findIndexOfTreeItemAt(request.getLocation());
		CommandBuilder builder = new CommandBuilder(false);
		for (Object obj : request.getEditParts()) {
			EObject child = (EObject) ((EditPart) obj).getModel();
			int tempIndex = newIndex;
			int oldIndex = children.indexOf(child);
			if (oldIndex == tempIndex || oldIndex + 1 == tempIndex) {
				return UnexecutableCommand.INSTANCE;
			}
			if (oldIndex <= tempIndex) {
				// moves forward
				tempIndex--;
			}
			builder.applyFeature(new RemoveChildCommand(), parent, containment,
					child);
			builder.applyFeature(new AddChildCommand(tempIndex), parent,
					containment, child);
		}
		CompoundCommand command = builder.getCommand();
		command.setLabel(command.size() > 1 ? Messages.Command_MoveChildren
				: Messages.Command_MoveChild);
		return command;
	}

}
