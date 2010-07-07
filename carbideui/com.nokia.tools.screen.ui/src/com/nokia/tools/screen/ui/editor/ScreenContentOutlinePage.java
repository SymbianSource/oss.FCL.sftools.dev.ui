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
package com.nokia.tools.screen.ui.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Tree;

import com.nokia.tools.editing.model.EditDiagram;
import com.nokia.tools.editing.ui.part.DefaultEditPartFactory;
import com.nokia.tools.editing.ui.part.DiagramTreeEditPart;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.ui.IPartCustomizer;
import com.nokia.tools.screen.ui.gef.ScreenEditPartFactory;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.ui.view.BaseContentOutlinePage;

/**
 * This page displays the tree editparts in a tree viewer.
 * 
 */
public class ScreenContentOutlinePage extends BaseContentOutlinePage implements
		IPartCustomizer {
	private List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>(
			1);

	/**
	 * Constructs an instance.
	 * 
	 * @param editor the associated editor.
	 * @param viewer the tree viewer.
	 */
	public ScreenContentOutlinePage(EditPartViewer viewer) {
		super(viewer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.view.BaseContentOutlinePage#configureOutlineViewer()
	 */
	@Override
	protected void configureOutlineViewer() {
		super.configureOutlineViewer();
		getViewer().setEditPartFactory(
				new ScreenEditPartFactory(DefaultEditPartFactory.TYPE_TREE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.view.BaseContentOutlinePage#initializeOutlineViewer()
	 */
	public void initializeOutlineViewer() {
		super.initializeOutlineViewer();

		GraphicalViewer viewer = (GraphicalViewer) getEditorPart().getAdapter(
				GraphicalViewer.class);
		if (viewer != null) {
			getViewer().setContents(
					new DiagramTreeEditPart((EditDiagram) viewer.getContents()
							.getModel()));
		}
		Tree tree = (Tree) getViewer().getControl();
		if (tree.getItemCount() > 0) {
			tree.getItem(0).setExpanded(true);
		}
		if (viewer != null) {
			firePropertyChanged(PROP_NAME);

			// outline viewer is created asynchronously and thus needs to
			// synchronize the selection from the main editor
			SelectionSynchronizer synchronizer = (SelectionSynchronizer) getEditorPart()
					.getAdapter(SelectionSynchronizer.class);
			if (synchronizer != null) {
				ISelection selection = viewer.getSelection();
				if (selection != null) {
					SelectionChangedEvent event = new SelectionChangedEvent(
							viewer, selection);
					synchronizer.selectionChanged(event);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.IPartCustomizer#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.IPartCustomizer#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.IPartCustomizer#getProperty(java.lang.String)
	 */
	public String getProperty(String name) {
		if (PROP_NAME.equals(name)) {
			IScreenElement screen = ScreenUtil.getScreen(getEditorPart());
			if (screen != null) {
				return MessageFormat.format(Messages.Outline_Elements,
						new Object[] { screen.getData().getName() });
			}
		}
		return null;
	}

	protected void firePropertyChanged(String name) {
		PropertyChangeEvent event = new PropertyChangeEvent(this, name, null,
				getProperty(name));
		for (PropertyChangeListener listener : listeners) {
			listener.propertyChange(event);
		}
	}
}