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
package com.nokia.tools.ui.view;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;

public class BaseContentOutlinePage extends ContentOutlinePage implements
		IAdaptable {
	public BaseContentOutlinePage(EditPartViewer viewer) {
		super(viewer);
	}
	
	
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
	   
	    super.addSelectionChangedListener(listener);
	}

	protected ActionRegistry getActionRegistry(){
		return (ActionRegistry) getEditorPart().getAdapter(
			ActionRegistry.class);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.Page#setActionBars(org.eclipse.ui.IActionBars)
	 */
	@Override
	public void setActionBars(IActionBars actionBars) {
		super.setActionBars(actionBars);

		ActionRegistry registry = getActionRegistry();

		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
				registry.getAction(ActionFactory.DELETE.getId()));
		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), registry
				.getAction(ActionFactory.UNDO.getId()));
		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), registry
				.getAction(ActionFactory.REDO.getId()));
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), registry
				.getAction(ActionFactory.COPY.getId()));
		actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), registry
				.getAction(ActionFactory.PASTE.getId()));
		
		
		

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.ContentOutlinePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		configureOutlineViewer();
		hookOutlineViewer();
		initializeOutlineViewer();
	}

	/**
	 * Initializes the outline viewer.
	 */
	public void initializeOutlineViewer() {
		IEditorPart ePart = getEditorPart();
		if(null != ePart)
		{
			ContextMenuProvider provider = (ContextMenuProvider) ePart
					.getAdapter(ContextMenuProvider.class);
			if (provider != null) {
				getViewer().setContextMenu(provider);
			}
		}
	}

	public void clearSelections() {
		// this is to avoid calling setSelection in the setContents call after
		// the tree has been disposed
		getViewer().deselectAll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.Page#dispose()
	 */
	@Override
	public void dispose() {
		unhookOutlineViewer();
		super.dispose();
	}

	/**
	 * Configures the outline viewer.
	 */
	protected void configureOutlineViewer() {
	}

	/**
	 * Hooks outline viewer.
	 */
	protected void hookOutlineViewer() {
		SelectionSynchronizer synchronizer = (SelectionSynchronizer) getEditorPart()
				.getAdapter(SelectionSynchronizer.class);
		if (synchronizer != null) {
			synchronizer.addViewer(getViewer());
		}
		getSite().setSelectionProvider(getViewer());
	}

	/**
	 * Unhooks the outline viewer.
	 */
	protected void unhookOutlineViewer() {
		getViewer().setContents(null);
		getViewer().setKeyHandler(null);
		getViewer().setContextMenu(null);
		getSite().setSelectionProvider(null);

		if (getEditorPart() != null) {
			SelectionSynchronizer synchronizer = (SelectionSynchronizer) getEditorPart()
					.getAdapter(SelectionSynchronizer.class);
			if (synchronizer != null) {
				synchronizer.removeViewer(getViewer());
			}
		}
		if (getViewer().getEditDomain() != null) {
			getViewer().getEditDomain().removeViewer(getViewer());
		}
	}

	protected EditDomain getEditDomain() {
		return ((DefaultEditDomain) getViewer().getEditDomain());
	}

	protected IEditorPart getEditorPart() {
		DefaultEditDomain domain = (DefaultEditDomain) getEditDomain();
		if (domain == null) {
			return null;
		}
		return domain.getEditorPart();
	}

	public Object getAdapter(Class adapter) {
		if (adapter == EditDomain.class) {
			return getEditDomain();
		}
		if (adapter == CommandStack.class) {
			EditDomain domain = getEditDomain();
			if (domain == null) {
				return null;
			}
			return domain.getCommandStack();
		}
		if (adapter == EditPartViewer.class) {
			return getViewer();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.ContentOutlinePage#setFocus()
	 */
	@Override
	public void setFocus() {
		if (getControl() != null && !getControl().isDisposed()) {
			getControl().setFocus();
		}
	}
}
