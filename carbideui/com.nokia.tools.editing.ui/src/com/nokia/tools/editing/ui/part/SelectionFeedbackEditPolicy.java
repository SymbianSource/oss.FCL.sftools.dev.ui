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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.eclipse.gef.requests.SelectionRequest;

public class SelectionFeedbackEditPolicy extends GraphicalEditPolicy {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#showTargetFeedback(org.eclipse.gef.Request)
	 */
	@Override
	public void showTargetFeedback(Request request) {
		if (request instanceof SelectionRequest) {
			if ("DropSelectionRequest".equals(((SelectionRequest) request)
					.getType())) {
				((GraphicalEditPart) getHost()).getFigure().setBorder(
						new LineBorder(ColorConstants.blue, 2));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#eraseTargetFeedback(org.eclipse.gef.Request)
	 */
	@Override
	public void eraseTargetFeedback(Request request) {
		if (request instanceof SelectionRequest) {
			if ("DropSelectionRequest".equals(((SelectionRequest) request)
					.getType())) {
				((GraphicalEditPart) getHost()).getFigure().setBorder(null);
			}
		}
	}
}
