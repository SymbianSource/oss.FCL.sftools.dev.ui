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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.model.EditDiagram;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.adapter.IFreeformDecorator;
import com.nokia.tools.editing.ui.adapter.IFreeformElementAdapter;
import com.nokia.tools.editing.ui.command.ApplyFeatureCommand;
import com.nokia.tools.editing.ui.figure.FreeformElementFigure;
import com.nokia.tools.editing.ui.util.DiagramUtil;

public class DiagramGraphicalEditPart extends AbstractGraphicalEditPart {
	public DiagramGraphicalEditPart(EditDiagram diagram) {
		setModel(diagram);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		IFigure contentPane = new FreeformLayer() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.draw2d.Figure#paintChildren(org.eclipse.draw2d.Graphics)
			 */
			public void paintChildren(Graphics g) {
				super.paintChildren(g);

				for (EditObject eo : ((EditDiagram) getModel())
						.getEditObjects()) {
					IFreeformElementAdapter adapter = (IFreeformElementAdapter) EditingUtil
							.getAdapter(eo, IFreeformElementAdapter.class);
					if (adapter != null) {
						for (IFreeformDecorator dec : adapter
								.getFreeformDecorators()) {
							List children = DiagramGraphicalEditPart.this
									.getChildren();
							dec.paint(g, (EditPart[]) children
									.toArray(new EditPart[children.size()]));
						}
					}
				}
			}
		};
		contentPane.setBackgroundColor(ColorConstants.white);
		contentPane.setOpaque(true);
		return contentPane;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new NullLayoutEditPolicy() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.editing.ui.part.NullLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart,
			 *      java.lang.Object)
			 */
			@Override
			protected Command createChangeConstraintCommand(EditPart child,
					Object constraint) {
				Rectangle rect = (Rectangle) constraint;
				IFigure figure = ((GraphicalEditPart) child).getFigure();
				if (!(figure instanceof FreeformElementFigure)) {
					return super.createChangeConstraintCommand(child,
							constraint);
				}
				FreeformElementFigure fig = (FreeformElementFigure) figure;
				fig.setDesignBounds(new Rectangle(rect.x, rect.y, rect.width,
						rect.height));

				EditPart freeform = child.getParent();
				((GraphicalEditPart) freeform).getFigure().repaint();

				EditPart screenEditPart = null;
				for (Object obj : freeform.getChildren()) {
					if (!(obj instanceof FreeformElementGraphicalEditPart)) {
						screenEditPart = (EditPart) obj;
						break;
					}
				}

				int originX = 0, originY = 0;
				if (screenEditPart instanceof GraphicalEditPart) {
					Rectangle bounds = ((GraphicalEditPart) screenEditPart)
							.getFigure().getBounds();
					originX = bounds.x;
					originY = bounds.y;
				}

				EditObject target = (EditObject) child.getModel();
				java.awt.Rectangle translated = DiagramUtil.translateToElement(
						target, new java.awt.Rectangle(rect.x - originX, rect.y
								- originY, rect.width, rect.height));

				ApplyFeatureCommand cmd = new ApplyFeatureCommand();
				cmd.setTarget(target);
				cmd.setFeature(EditingUtil.getBoundsFeature(target));
				cmd.setValue(translated);
				return cmd;
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	protected List getModelChildren() {
		List list = new ArrayList();
		for (Object obj : EditPartHelper.getModelChildren(this)) {
			list.add(obj);
			IFreeformElementAdapter adapter = (IFreeformElementAdapter) EditingUtil
					.getAdapter((EObject) obj, IFreeformElementAdapter.class);
			if (adapter != null) {
				for (EditObject child : adapter.getFreeformElements()) {
					list.add(child);
				}
			}
		}
		return list;
	}
}
