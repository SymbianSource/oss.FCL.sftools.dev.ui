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
package com.nokia.tools.s60.cheatsheet.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.s60.views.IconView;
import com.nokia.tools.screen.ui.IToolBoxAdapter;

//this action is used in editing frame animation cheatsheet
public class OpenGraphicsEditorAction extends BaseAction implements ICheatSheetAction {

	public void run(String[] params, ICheatSheetManager manager) {
		try {
		if ("IconView".equals(params[0])) {
				// open for selection in icons view
				IconView iw = (IconView) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage().findView(
								IconView.ID);
				if (iw != null && isSupportedForSelection()) {
					com.nokia.tools.s60.ide.actions.OpenGraphicsEditorAction act = new com.nokia.tools.s60.ide.actions.OpenGraphicsEditorAction(
							iw, iw);
					act.run();
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		notifyResult(false);
	}	
	
	private boolean isSupportedForSelection() {
		
		IconView iw = (IconView) PlatformUI.getWorkbench()
		.getActiveWorkbenchWindow().getActivePage().findView(
				IconView.ID);
		ISelection selection= iw.getSelection();
		
		if(selection instanceof StructuredSelection){
			StructuredSelection structuredSelection=(StructuredSelection) selection;
			Object contentData=structuredSelection.getFirstElement();
		
			if(contentData instanceof IContentData){
			
				IContentData data = (IContentData) contentData;
				
				if (data != null) {
					IToolBoxAdapter toolBoxAdapter = (IToolBoxAdapter) data
							.getAdapter(IToolBoxAdapter.class);
					if (toolBoxAdapter != null
							&& toolBoxAdapter.isMultipleLayersSupport())
						return true;

					IImageAdapter imageAdapter = (IImageAdapter) data
							.getAdapter(IImageAdapter.class);
					if (imageAdapter != null)
						if (imageAdapter.isAnimated())
							return true;
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Information", "Selected element("+data.toString()+") cannot be animated. Frames can be animated only for animated icons.");					
				}
			}
		
		}

	return false;
	}
	


}
