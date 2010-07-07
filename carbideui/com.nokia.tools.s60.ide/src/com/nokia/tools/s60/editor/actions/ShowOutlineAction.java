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

package com.nokia.tools.s60.editor.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.s60.ide.S60WorkspacePlugin;

public class ShowOutlineAction extends AbstractAction {

	public static final String ID = "ShowOutline";

	public ShowOutlineAction(IWorkbenchPart part) {
		super(part);
		setId(ID);
		setText(Messages.ShowOutlineAction_lbl);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/outline_co.gif"));
		setLazyEnablementCalculation(true);
	}

	@Override
	public void doRun(Object el) {
		try {
			IWorkbenchPage page = getWorkbenchPart().getSite()
					.getWorkbenchWindow().getActivePage();
			IViewPart view = page.showView(
					"org.eclipse.ui.views.ContentOutline", null,
					IWorkbenchPage.VIEW_VISIBLE);
			if (view != null && view instanceof ISelectionListener) {
				((ISelectionListener) view).selectionChanged(
						getWorkbenchPart(), getWorkbenchPart().getSite()
								.getSelectionProvider().getSelection());
			}
			if (view != null && view instanceof ISelectionProvider) {
				((ISelectionProvider) view).setSelection(getWorkbenchPart()
						.getSite().getSelectionProvider().getSelection());
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected boolean doCalculateEnabled(Object element) {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage()
				.findView("org.eclipse.ui.views.ContentOutline") == null;
	}
}
