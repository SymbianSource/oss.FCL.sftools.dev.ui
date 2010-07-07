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

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Display;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.adapter.IComponentAdapter;
import com.nokia.tools.editing.ui.prefs.EditingPreferences;

public class DefaultGraphicalEditPart extends AbstractGraphicalEditPart {
	public DefaultGraphicalEditPart(EditObject bean) {
		setModel(bean);
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
		EditPartHelper.deregisterEditingAdapter(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		IFigure figure = new Figure() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.draw2d.Figure#validate()
			 */
			public void validate() {
				// updates self, only bounds is relevant here
				validateFigure(false);
				super.validate();
			}
		};
		figure.setOpaque(false);
		figure.setLayoutManager(new XYLayout());
		if (EditingPreferences.isDesignAidEnabled()) {
			Border border = (Border) EditingUtil.getAdapter(
					(EObject) getModel(), Border.class);
			if (border != null) {
				figure.setBorder(border);
			}
		}
		return figure;
	}

	protected void validateFigure(boolean revalidate) {
		java.awt.Rectangle bounds = (java.awt.Rectangle) EditingUtil
				.getBounds((EObject) getModel());
		if (bounds == null) {
			bounds = new java.awt.Rectangle();
		}
		Rectangle rect = new Rectangle(bounds.x, bounds.y, bounds.width,
				bounds.height);
		IFigure figure = getFigure();
		IFigure parent = figure.getParent();
		if (bounds != null) {
			if (isActive()) {
				if (revalidate) {
					parent.setConstraint(figure, rect);
				} else {
					if (parent.getLayoutManager() != null) {
						parent.getLayoutManager().setConstraint(figure, rect);
					}
				}
			} else {
				figure.setBounds(rect);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		IComponentAdapter adapter = EditPartHelper.getComponentAdapter(this);

		if (adapter.supports(IComponentAdapter.MODIFY, null)) {
			installEditPolicy(EditPolicy.COMPONENT_ROLE,
					new DefaultComponentEditPolicy());
		}
		if (adapter.supports(IComponentAdapter.DIRECT_EDIT, null)) {
			installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,
					new DefaultDirectEditPolicy());
		}
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE,
				isContainer() ? new ContainerFeedbackEditPolicy()
						: new SelectionFeedbackEditPolicy());
		if (isContainer()) {
			installEditPolicy(EditPolicy.LAYOUT_ROLE,
					new NullLayoutEditPolicy());
		}
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
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	@Override
	protected List getModelChildren() {
		return EditPartHelper.getModelChildren(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
	 */
	@Override
	protected void refreshVisuals() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				validateFigure(true);
				getFigure().repaint();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#performRequest(org.eclipse.gef.Request)
	 */
	@Override
	public void performRequest(Request req) {
		if (RequestConstants.REQ_DIRECT_EDIT == req.getType()
				&& getEditPolicy(EditPolicy.DIRECT_EDIT_ROLE) != null) {
			performDirectEdit();
		}
		super.performRequest(req);
	}

	protected EStructuralFeature getDirectEditFeature() {
		// tries text and label features, others may override
		EStructuralFeature textFeature = EditingUtil.getFeature(
				(EObject) getModel(), "text");
		if (textFeature != null
				&& String.class.getName().equals(
						textFeature.getEType().getName())) {
			return textFeature;
		}
		textFeature = EditingUtil.getFeature((EObject) getModel(), "label");
		if (textFeature != null
				&& String.class.getName().equals(
						textFeature.getEType().getName())) {
			return textFeature;
		}
		return null;
	}

	protected void performDirectEdit() {
		EStructuralFeature feature = getDirectEditFeature();
		if (feature == null) {
			return;
		}
		DirectEditManager manager = new DefaultDirectEditManager(this,
				TextCellEditor.class,
				new DefaultCellEditorLocator(getFigure()), feature);
		manager.show();
	}

	protected boolean isContainer() {
		return false;
	}
}
