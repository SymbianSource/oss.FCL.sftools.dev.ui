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
package com.nokia.tools.editing.ui.figure;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.util.DiagramUtil;

public class FreeformElementFigure extends ContentPaneFigure implements
		IFreeformElementFigure {
	private EditObject model;
	private Rectangle designBounds;

	public FreeformElementFigure(EditObject model) {
		this.model = model;
	}

	public Rectangle getRealBounds() {
		java.awt.Rectangle bounds = EditingUtil.getBounds(model);
		java.awt.Rectangle realBounds = DiagramUtil.translateToDiagram(
				(EObject) model, bounds);
		return new Rectangle(realBounds.x, realBounds.y, realBounds.width,
				realBounds.height);
	}

	/**
	 * @return Returns the designBounds.
	 */
	public Rectangle getDesignBounds() {
		if (designBounds == null) {
			return getBounds();
		}
		return designBounds;
	}

	/**
	 * @param designBounds The designBounds to set.
	 */
	public void setDesignBounds(Rectangle editBounds) {
		this.designBounds = editBounds;
	}

	public void setBounds(Rectangle bounds) {
		super.setBounds(bounds);
		designBounds = null;
		getParent().repaint();
	}
}
