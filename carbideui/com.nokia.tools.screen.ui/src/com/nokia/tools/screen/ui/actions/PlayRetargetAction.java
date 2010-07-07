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
package com.nokia.tools.screen.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.RetargetAction;

import com.nokia.tools.screen.ui.utils.EclipseUtils;

public class PlayRetargetAction extends RetargetAction implements
		ISelectionListener, IPageListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPageListener#pageActivated(org.eclipse.ui.IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
		page.addSelectionListener(PlayRetargetAction.this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPageListener#pageClosed(org.eclipse.ui.IWorkbenchPage)
	 */
	public void pageClosed(IWorkbenchPage page) {
		page.removeSelectionListener(PlayRetargetAction.this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPageListener#pageOpened(org.eclipse.ui.IWorkbenchPage)
	 */
	public void pageOpened(IWorkbenchPage page) {
	}

	AbstractPlayAction template;

	public PlayRetargetAction(AbstractPlayAction template) {
		super(template.getId(), null, template.getStyle());
		setId(template.getId());
		setToolTipText(template.getToolTipText());
		setImageDescriptor(template.getImageDescriptor());
		setDisabledImageDescriptor(template.getDisabledImageDescriptor());

		this.template = template;

		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage() != null) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().addSelectionListener(this);
		} else {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.addPageListener(this);
		}
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IAction handler = getActionHandler();
		if (handler != null && handler instanceof ISelectionListener) {
			((ISelectionListener) handler).selectionChanged(part, selection);
		}
	}

	@Override
	protected void setActionHandler(IAction newHandler) {
		if (newHandler == null) {
			if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
					&& PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage() != null) {
				IEditorPart editor = EclipseUtils.getActiveSafeEditor();
				if (editor != null) {
					newHandler = editor.getEditorSite().getActionBars()
							.getGlobalActionHandler(template.getId());
				}
			}
		}
		super.setActionHandler(newHandler);
	}

	@Override
	protected void propagateChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(Action.TOOL_TIP_TEXT)) {
			setToolTipText((String) event.getNewValue());
		} else {
			super.propagateChange(event);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.RetargetAction#dispose()
	 */
	@Override
	public void dispose() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.removePageListener(this);
		if (null != PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage())
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().removeSelectionListener(this);
		super.dispose();
	}
}
