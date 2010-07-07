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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.AlignmentRequest;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.ui.adapter.IComponentAdapter;
import com.nokia.tools.editing.ui.command.AddChildCommand;
import com.nokia.tools.editing.ui.command.ApplyFeatureCommand;
import com.nokia.tools.editing.ui.command.CommandBuilder;
import com.nokia.tools.editing.ui.command.Messages;

public class NullLayoutEditPolicy extends XYLayoutEditPolicy {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		EObject model = (EObject) getHost().getModel();
		java.awt.Rectangle bounds = EditingUtil.getBounds(model);
		if (bounds != null) {
			IFigure figure = ((GraphicalEditPart) getHost()).getFigure();
			figure.getParent().setConstraint(
					figure,
					new Rectangle(bounds.x, bounds.y, bounds.width,
							bounds.height));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart,
	 *      java.lang.Object)
	 */
	@Override
	protected Command createChangeConstraintCommand(EditPart child,
			Object constraint) {
		IComponentAdapter adapter = EditPartHelper
				.getComponentAdapter((EObject) child.getModel());
		if (!adapter.supports(IComponentAdapter.CHANGE_CONSTRAINT, null)) {
			return UnexecutableCommand.INSTANCE;
		}

		EObject target = (EObject) child.getModel();
		Rectangle rect = (Rectangle) constraint;
		ApplyFeatureCommand command = new ApplyFeatureCommand();
		command.setTarget(target);
		command.setFeature(EditingUtil.getBoundsFeature(target));
		command.setValue(new java.awt.Rectangle(rect.x, rect.y, rect.width,
				rect.height));
		return command;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
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

		Rectangle rect = (Rectangle) getConstraintFor(request);
		java.awt.Rectangle bounds = new java.awt.Rectangle(rect.x, rect.y,
				rect.width, rect.height);

		CommandBuilder builder = new CommandBuilder(Messages.Command_AddChild,
				false);
		// set bounds first, then attach to parent
		builder
				.applyFeature(child, EditingUtil.getBoundsFeature(child),
						bounds);
		builder.applyFeature(new AddChildCommand(), parent, containment, child);
		return builder.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.EditPolicy#getCommand(org.eclipse.gef.Request)
	 */
	public Command getCommand(Request request) {
		if (REQ_ALIGN_CHILDREN.equals(request.getType())) {
			GraphicalEditPart part = (GraphicalEditPart) ((AlignmentRequest) request)
					.getEditParts().get(0);
			Object constraint = getConstraintFor((AlignmentRequest) request,
					part);

			return createChangeConstraintCommand(part, constraint);
		}
		return super.getCommand(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.XYLayoutEditPolicy#getConstraintFor(org.eclipse.gef.requests.ChangeBoundsRequest,
	 *      org.eclipse.gef.GraphicalEditPart)
	 */
	protected Object getConstraintFor(ChangeBoundsRequest request,
			GraphicalEditPart child) {
		org.eclipse.draw2d.geometry.Rectangle rect = new PrecisionRectangle(
				child.getFigure().getBounds());
		child.getFigure().translateToAbsolute(rect);
		rect = request.getTransformedRectangle(rect);
		child.getFigure().translateToRelative(rect);
		rect.translate(getLayoutOrigin().getNegated());
		return getConstraintFor(rect);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChildEditPolicy(org.eclipse.gef.EditPart)
	 */
	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) {
		IComponentAdapter adapter = EditPartHelper.getComponentAdapter(child);
		if (adapter.supports(IComponentAdapter.CHANGE_CONSTRAINT, null)) {
			return new ResizableEditPolicy();
		}
		return new NonResizableEditPolicy();
	}
}
