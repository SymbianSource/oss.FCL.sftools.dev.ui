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
/**
 * 
 */
package com.nokia.tools.s60.editor.actions;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.svg.ColorGroups;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.views.ColorsView;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.editing.BasicImageLayer;

/**
 */
public class ShowInReferencedColors extends AbstractMultipleSelectionAction {

	public static final String COLORS_VIEW_ID = "com.nokia.tools.s60.views.ColorsView";
	public static final String ID = "ShowColors"; 

	/**
	 * @param part
	 */
	public ShowInReferencedColors(IWorkbenchPart part) {
		super(part);
		
	}

	@Override
	protected void init() {
		setId(ID);
		setText(Messages.ShowInMenu_Colors);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/colors.gif")); 
		setLazyEnablementCalculation(true);
		setEnabled(true);
		
		super.init();
		//multipleSelection = true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.AbstractAction#doRun(java.lang.Object)
	 */
	@Override
	protected void doRun(Object element) {
		IContentData data = getContentData(element);
		if (data != null) {
			try {
				IWorkbenchPage page = getWorkbenchPart() != null ? getWorkbenchPart()
						.getSite().getWorkbenchWindow().getActivePage()
						: PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage();
				IViewPart view = page.showView(COLORS_VIEW_ID, null,
						IWorkbenchPage.VIEW_ACTIVATE);
				if (view instanceof ColorsView) {
					((ColorsView) view).selectionChanged(getWorkbenchPart(),
							new StructuredSelection(data));
				}
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.AbstractAction#getContentData(java.lang.Object)
	 */
	@Override
	protected IContentData getContentData(Object element) {
		IContentData data = super.getContentData(element);
		if (data == null) {
			ISelection selection = null;
			if (getWorkbenchPart() != null) {
				selection = getWorkbenchPart().getSite().getPage()
						.getSelection();
			} else if (getSelectionProvider() != null) {
				selection = getSelectionProvider().getSelection();
			}
			if (selection instanceof IStructuredSelection) {
				data = super.getContentData(((IStructuredSelection) selection)
						.getFirstElement());
			}
		}
		return data;
	}

	@Override
	protected boolean doCalculateEnabled(Object element) {

		IContentData data = getContentData(element);

		if (data != null) {
			if (data instanceof ThemeData) {
				if (isColors(data)) {
					ColorGroups grps = BasicImageLayer.getAvailableColors(data);

					if (grps != null) {
						if (grps.getGroups().size() > 0) {
							return true;
							
						}
					}
				}
			}
		}
		return false;
	}

	private boolean isColors(IContentData data) {
		
		IColorAdapter color = (IColorAdapter) data.getAdapter(IColorAdapter.class);
			if (color != null) {
				return true;
			}
		
		
		for (IContentData data1 : ((ThemeData) data).getChildren()) {
			color = (IColorAdapter) data1.getAdapter(IColorAdapter.class);
			if (color != null) {
				return true;
			} 
			return false;
		}

		return false;
	}



}
