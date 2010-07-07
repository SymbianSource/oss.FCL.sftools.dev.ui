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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.AlignmentRequest;
import org.eclipse.gef.requests.GroupRequest;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.ui.adapter.IComponentAdapter;
import com.nokia.tools.editing.ui.command.Messages;
import com.nokia.tools.editing.ui.command.RemoveChildCommand;

public class DefaultComponentEditPolicy extends ComponentEditPolicy {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#understandsRequest(org.eclipse.gef.Request)
	 */
	@Override
	public boolean understandsRequest(Request req) {
		if (RequestConstants.REQ_ALIGN.equals(req.getType())) {
			return true;
		}
		return super.understandsRequest(req);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.ComponentEditPolicy#getCommand(org.eclipse.gef.Request)
	 */
	@Override
	public Command getCommand(Request request) {
		if (RequestConstants.REQ_ALIGN.equals(request.getType())) {
			return getAlignCommand((AlignmentRequest) request);
		}
		return super.getCommand(request);
	}

	/**
	 * Returns the command contribution to an alignment request
	 * 
	 * @param request the alignment request
	 * @return the contribution to the alignment
	 */
	protected Command getAlignCommand(AlignmentRequest request) {
		AlignmentRequest req = new AlignmentRequest(REQ_ALIGN_CHILDREN);
		req.setEditParts(getHost());
		req.setAlignment(request.getAlignment());
		req.setAlignmentRectangle(request.getAlignmentRectangle());
		return getHost().getParent().getCommand(req);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.ComponentEditPolicy#createDeleteCommand(org.eclipse.gef.requests.GroupRequest)
	 */
	@Override
	protected Command createDeleteCommand(GroupRequest deleteRequest) {
		EObject parent = (EObject) getHost().getParent().getModel();
		EStructuralFeature containment = EditingUtil
				.getContainmentFeature(parent);
		if (containment == null) {
			return UnexecutableCommand.INSTANCE;
		}
		IComponentAdapter adapter = EditPartHelper.getComponentAdapter(parent);
		// don't use the deleteRequest, it contains multiple eps and for each ep
		// the get command is called again, causing multiple deletes
		EditPart part = getHost();
		EObject child = (EObject) part.getModel();
		if (adapter.supports(IComponentAdapter.REMOVE_CHILD, child)) {
			RemoveChildCommand command = new RemoveChildCommand();
			command.setLabel(Messages.Command_RemoveChild);
			command.setTarget(parent);
			command.setFeature(containment);
			command.setValue(child);
			return command;
		}
		return UnexecutableCommand.INSTANCE;
	}
}
