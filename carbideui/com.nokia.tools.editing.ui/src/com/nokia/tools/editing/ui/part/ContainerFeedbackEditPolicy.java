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
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;

import com.nokia.tools.editing.ui.figure.FeedbackFigure;

public class ContainerFeedbackEditPolicy extends SelectionFeedbackEditPolicy {
	private IFigure feedback = new FeedbackFigure();

	private IFigure getContainerFigure() {
		return ((GraphicalEditPart) getHost()).getFigure();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#eraseTargetFeedback(org.eclipse.gef.Request)
	 */
	@Override
	public void eraseTargetFeedback(Request request) {
		super.eraseTargetFeedback(request);

		if (feedback.getParent() != null) {
			removeFeedback(feedback);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#showTargetFeedback(org.eclipse.gef.Request)
	 */
	@Override
	public void showTargetFeedback(Request request) {
		super.showTargetFeedback(request);

		if (request.getType().equals(RequestConstants.REQ_MOVE)
				|| request.getType().equals(RequestConstants.REQ_ADD)
				|| request.getType().equals(RequestConstants.REQ_CLONE)
				|| request.getType().equals(RequestConstants.REQ_CREATE)) {
			feedback.setBounds(getContainerFigure().getBounds());
			addFeedback(feedback);
		}
	}
}
