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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ISetSelectionTarget;

import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.views.ViewIDs;

public class ShowInResourceViewAction extends AbstractAction {

	public static final String ID = "ShowInResourceView";

	public ShowInResourceViewAction(IWorkbenchPart part) {
		super(part);
		setId(ID);
		setText(Messages.ShowInResourceViewAction_name);
		setImageDescriptor(UiPlugin
				.getImageDescriptor("icons/resource_view16x16.png"));
	}

	@Override
	public void doRun(Object el) {
		try {
			IWorkbenchPage page = getWorkbenchPart().getSite()
					.getWorkbenchWindow().getActivePage();
			IViewPart view = page.showView(
					ViewIDs.RESOURCE_VIEW2_ID, null,
					IWorkbenchPage.VIEW_VISIBLE);
			if (view instanceof ISetSelectionTarget) {
				ISelection selection = new StructuredSelection(el);
				((ISetSelectionTarget) view).selectReveal(selection);
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected boolean doCalculateEnabled(Object element) {
		return true;
	}
}
