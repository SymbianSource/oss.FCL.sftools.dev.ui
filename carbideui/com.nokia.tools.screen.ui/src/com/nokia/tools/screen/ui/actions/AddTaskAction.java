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

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.tasklist.TaskPropertiesDialog;

import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;

/**
 * Screen editor action, adding metedata markers and notes to VE (S60 UI)
 * graphical elements. This elements have ID and it is other than the file
 * itself (xml) a link to the place in editor
 * 
 */
public class AddTaskAction extends AddBookmarkAction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {

		// from the editor get the resources and selection attributes
		IResource resource = getResource();
		Map<String, Object> attributes = getSelectionAttributes();

		Object o = attributes.get(IMarker.MESSAGE); //$NON-NLS-1$
		String proposal = "TODO: " + ((o instanceof String) ? (String) o : ""); //$NON-NLS-1$
		attributes.put(IMarker.MESSAGE, proposal);

		TaskPropertiesDialog dialog = new TaskPropertiesDialog(
				getActiveEditorPart().getSite().getShell());
		dialog.setResource(resource);
		dialog.setInitialAttributes(attributes);
		dialog.create();
		IBrandingManager branding = BrandingExtensionManager
				.getBrandingManager();
		Image image = null;
		if (branding != null) {
			image = branding.getIconImageDescriptor().createImage();
			dialog.getShell().setImage(image);
		}
		int ret = dialog.open();
		if (image != null) {
			image.dispose();
		}
		if (ret == Window.OK) {
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			if (page != null) {
				try {
					page.showView(IPageLayout.ID_TASK_LIST, null,
							IWorkbenchPage.VIEW_CREATE);
					page.showView(IPageLayout.ID_TASK_LIST, null,
							IWorkbenchPage.VIEW_ACTIVATE);

				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public AddTaskAction(IEditorPart part) {
		super(part);
	}
}
