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
package com.nokia.tools.editing.jfc;

import java.awt.Component;
import java.awt.Container;

import javax.swing.SwingUtilities;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gef.GraphicalViewer;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.model.EditDiagram;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.model.ModelPackage;
import com.nokia.tools.editing.ui.figure.LiveDiagramAdapter;

/**
 *
 */
public class JFCDiagramAdapter extends LiveDiagramAdapter {
	public JFCDiagramAdapter(GraphicalViewer viewer) {
		super(viewer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.figure.LiveDiagramAdapter#init()
	 */
	protected void init() {
		final Container container = getContainer();
		if (container != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					container.removeAll();
					for (EditObject eo : getDiagram().getEditObjects()) {
						Object bean = EditingUtil.getBean(eo);
						if (bean instanceof Component) {
							container.add((Component) bean);
						}
					}
					notifyChanged(null);
				}
			});
		}
	}

	/**
	 * @return the container
	 */
	public Container getContainer() {
		JFCFigure figure = (JFCFigure) getFigure();
		if (figure == null) {
			return null;
		}
		return figure.getRoot();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.figure.LiveDiagramAdapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 */
	public void notifyChanged(final Notification notification) {
		final Container container = getContainer();
		if (container == null) {
			return;
		}
		if (notification != null
				&& notification.getNotifier() instanceof EditDiagram) {
			EditDiagram diagram = (EditDiagram) notification.getNotifier();
			if (diagram.eClass().getEStructuralFeature(
					ModelPackage.EDIT_DIAGRAM__EDIT_OBJECTS) == notification
					.getFeature()) {
				switch (notification.getEventType()) {
				case Notification.ADD:
					if (notification.getNewValue() instanceof Component) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								container.add((Component) notification
										.getNewValue());
							}
						});
					}
					break;
				case Notification.REMOVE:
					if (notification.getOldValue() instanceof Component) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								container.remove((Component) notification
										.getOldValue());
							}
						});
					}
					break;
				}
			}
		}
		super.notifyChanged(notification);
	}
}
