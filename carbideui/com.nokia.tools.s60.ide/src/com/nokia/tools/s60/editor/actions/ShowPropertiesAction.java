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

import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.properties.PropertySheet;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.ui.propertysheet.tabbed.WidgetTabbedPropertySheetPage;

public class ShowPropertiesAction extends AbstractAction {

	public static final String ID = "ShowProperties";

	public ShowPropertiesAction(IWorkbenchPart part) {
		super(part);
		setId(ID);
		setText(Messages.ShowPropertiesAction_lbl);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/properties_view16x16.png"));
		setLazyEnablementCalculation(true);
	}

	@Override
	public void doRun(Object el) {
		try {
			IWorkbenchPage page = getWorkbenchPart().getSite()
					.getWorkbenchWindow().getActivePage();
			IViewPart properties= page.showView("org.eclipse.ui.views.PropertySheet", null,
					IWorkbenchPage.VIEW_ACTIVATE);
			if(properties instanceof PropertySheet){
				PropertySheet sheet=((PropertySheet)properties);
				
				if(sheet.getCurrentPage() instanceof WidgetTabbedPropertySheetPage){
					WidgetTabbedPropertySheetPage tabbedPropertyPage=(WidgetTabbedPropertySheetPage)sheet.getCurrentPage();
					if(el instanceof IContentData){
						IContentData data= (IContentData) el;		
						tabbedPropertyPage.selectionChanged(getWorkbenchPart(),new StructuredSelection(data));
					}else if(el instanceof EditPart){
						tabbedPropertyPage.selectionChanged(getWorkbenchPart(), new StructuredSelection(el));
					}
				}
			}
						
			
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected boolean doCalculateEnabled(Object element) {
		return true;
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("org.eclipse.ui.views.PropertySheet")
		// == null;
	}
}
