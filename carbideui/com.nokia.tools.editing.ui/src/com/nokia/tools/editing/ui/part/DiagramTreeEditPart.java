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

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.gef.editpolicies.TreeContainerEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

import com.nokia.tools.editing.model.EditDiagram;

public class DiagramTreeEditPart extends AbstractTreeEditPart {
	public DiagramTreeEditPart(EditDiagram diagram) {
		setModel(diagram);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractTreeEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.TREE_CONTAINER_ROLE,
				new TreeContainerEditPolicy() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.gef.editpolicies.TreeContainerEditPolicy#getAddCommand(org.eclipse.gef.requests.ChangeBoundsRequest)
					 */
					@Override
					protected Command getAddCommand(ChangeBoundsRequest request) {
						return UnexecutableCommand.INSTANCE;
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.gef.editpolicies.TreeContainerEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
					 */
					@Override
					protected Command getCreateCommand(CreateRequest request) {
						return UnexecutableCommand.INSTANCE;
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.gef.editpolicies.TreeContainerEditPolicy#getMoveChildrenCommand(org.eclipse.gef.requests.ChangeBoundsRequest)
					 */
					@Override
					protected Command getMoveChildrenCommand(
							ChangeBoundsRequest request) {
						return UnexecutableCommand.INSTANCE;
					}

				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	@Override
	protected List getModelChildren() {
		return EditPartHelper.getModelChildren(this);
	}
}
