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
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.Request;
import org.eclipse.gef.tools.DragEditPartsTracker;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.figure.FreeformElementFigure;
import com.nokia.tools.editing.ui.prefs.EditingPreferences;

public class FreeformElementGraphicalEditPart extends DefaultGraphicalEditPart {
	public FreeformElementGraphicalEditPart(EditObject model) {
		super(model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		FreeformElementFigure cfig = new FreeformElementFigure(
				(EditObject) getModel());

		IFigure content = (IFigure) EditingUtil.getAdapter(
				(EditObject) getModel(), IFigure.class);
		if (content != null) {
			cfig.setContentPane(content);
		}
		cfig.setVisible(EditingPreferences.isDesignAidEnabled());
		return cfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getDragTracker(org.eclipse.gef.Request)
	 */
	@Override
	public DragTracker getDragTracker(Request request) {
		return new DragEditPartsTracker(this) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.gef.tools.DragEditPartsTracker#isMove()
			 */
			@Override
			protected boolean isMove() {
				if (getSourceEditPart() instanceof FreeformElementGraphicalEditPart) {
					return true;
				}
				return super.isMove();
			}
		};
	}
}
