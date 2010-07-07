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

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.s60.editor.ui.views.LayersView;
import com.nokia.tools.s60.ide.IS60IDEConstants;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;

public class ShowInLayersViewAction extends AbstractAction {

	public static final String ID = "ShowLayers";

	public void setIsMultipleSelection(){
		this.multipleSelection = true;
	}
	
	public ShowInLayersViewAction(IWorkbenchPart part) {
		super(part);
		setId(ID);
		setText(Messages.ShowLayersAction_lbl);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/layer_view16x16.png"));
	}

	@Override
	public void doRun(Object el) {
		try {
			IWorkbenchPage page = getWorkbenchPart().getSite()
					.getWorkbenchWindow().getActivePage();
			IViewPart view = page.showView(IS60IDEConstants.ID_LAYERS_VIEW,
					null, IWorkbenchPage.VIEW_VISIBLE);
			if (view instanceof LayersView) {
				if(el instanceof IContentData){ 
					((LayersView) view).selectionChanged(getWorkbenchPart(),
							new StructuredSelection(el));
				}else{
					((LayersView) view).selectionChanged(getWorkbenchPart(),
							getWorkbenchPart().getSite().getSelectionProvider()
								.getSelection());
				}
			}
			if(view != null && page != null){
				page.activate(view);
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
