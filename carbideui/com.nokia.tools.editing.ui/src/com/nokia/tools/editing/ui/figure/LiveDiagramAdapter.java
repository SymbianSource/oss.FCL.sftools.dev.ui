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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;

import com.nokia.tools.editing.core.TypedAdapter;
import com.nokia.tools.editing.model.EditDiagram;
import com.nokia.tools.editing.model.EditObject;

public abstract class LiveDiagramAdapter extends TypedAdapter {
	private GraphicalViewer viewer;

	public LiveDiagramAdapter(GraphicalViewer viewer) {
		this.viewer = viewer;
		init();
	}

	protected void init() {
	}

	protected EditDiagram getDiagram() {
		return (EditDiagram) viewer.getContents().getModel();
	}

	protected LiveFigure getFigure() {
		if (viewer == null) {
			// not attached to viewer yet
			return null;
		}
		EditPart root = viewer.getContents();
		if (root == null) {
			return null;
		}
		EditDiagram diagram = (EditDiagram) root.getModel();
		if (diagram == null) {
			return null;
		}
		EList<EditObject> objects = diagram.getEditObjects();
		if (objects == null || objects.isEmpty()) {
			return null;
		}
		GraphicalEditPart part = (GraphicalEditPart) viewer
				.getEditPartRegistry().get(objects.get(0));
		if (part == null) {
			return null;
		}
		if (part.getFigure() instanceof LiveFigure) {
			return (LiveFigure) part.getFigure();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 */
	public void notifyChanged(Notification notification) {
		LiveFigure figure = getFigure();
		if (figure != null) {
			figure.revalidateRoot();
		}
	}

	public void setAnimating(boolean isAnimating) {
		LiveFigure figure = getFigure();
		if (figure != null) {
			// figure.setLive(isAnimating);
			figure.setAnimating(isAnimating);
		}
	}

	public boolean isAnimating() {
		LiveFigure figure = getFigure();
		if (figure != null) {
			return figure.isAnimating();
		}
		return false;
	}
}
