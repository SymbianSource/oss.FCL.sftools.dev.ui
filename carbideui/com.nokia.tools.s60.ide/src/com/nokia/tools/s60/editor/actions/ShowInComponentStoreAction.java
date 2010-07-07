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
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.s60.editor.ui.views.ComponentStoreView;
import com.nokia.tools.s60.ide.IS60IDEConstants;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.ui.IToolBoxAdapter;

public class ShowInComponentStoreAction extends AbstractAction {

	public static final String ID = "ShowComponentStore"; 

	public void setIsMultipleSelection(){
		this.multipleSelection = false;
	}
	
	public ShowInComponentStoreAction(IWorkbenchPart part) {
		super(part);
		setId(ID);
		setText(Messages.ShowInMenu_CStore);		
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/cascade.png")); 
	}

	@Override
	public void doRun(Object el) {
		try {
			IWorkbenchPage page = getWorkbenchPart().getSite()
					.getWorkbenchWindow().getActivePage();
			IViewPart view = page.showView(IS60IDEConstants.ID_COMPONENT_STORE_VIEW,
					null, IWorkbenchPage.VIEW_ACTIVATE);
			if (view instanceof ComponentStoreView) {
				((ComponentStoreView) view).selectionChanged(getWorkbenchPart(),
						new StructuredSelection(getContentData(el)));
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected boolean doCalculateEnabled(Object element) {
		
		IWorkbenchPage page = getWorkbenchPart().getSite()
			.getWorkbenchWindow().getActivePage();
		IViewPart view = page.findView(IS60IDEConstants.ID_COMPONENT_STORE_VIEW);
		if (view != null)
			return false;
		
		IContentData cdata = getContentData(element);
		if (cdata != null) {
			//only regular skinnable entities can be shown in store
			IColorAdapter color = (IColorAdapter) cdata.getAdapter(IColorAdapter.class);
			if (color != null)
				return false;
			IToolBoxAdapter toolbox = (IToolBoxAdapter) cdata.getAdapter(IToolBoxAdapter.class);
			if (toolbox != null) {
				return !toolbox.isFile() && !toolbox.isText();
			}
		}
		return false;
	}
}
