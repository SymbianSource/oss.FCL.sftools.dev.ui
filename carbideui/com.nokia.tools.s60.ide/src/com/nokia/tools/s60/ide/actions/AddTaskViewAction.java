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
package com.nokia.tools.s60.ide.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.views.tasklist.TaskPropertiesDialog;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.s60.editor.actions.AbstractAction;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.core.INamingAdapter;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;

/**
 * For adding Task from IconView
 */
public class AddTaskViewAction extends AbstractAction {

	public AddTaskViewAction(IWorkbenchPart part) {
		super(part);
	}

	public AddTaskViewAction(ISelectionProvider p) {
		super(null);
		setSelectionProvider(p);
	}

	@Override
	protected void init() {
		setId(IDEActionFactory.ADD_TASK.getId());
		IWorkbenchAction dummy = IDEActionFactory.ADD_TASK.create(PlatformUI
				.getWorkbench().getActiveWorkbenchWindow());
		setText(dummy.getText());
		setToolTipText(dummy.getToolTipText());
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/etool16/addtsk_tsk.gif"));
		super.init();
	}

	@Override
	protected void doRun(Object _element) {

		IContentData cdata = getContentData(_element);

		if (cdata != null) {

			Map<String, Object> attributes = new HashMap<String, Object>();
			// init attributes

			IScreenElement screenEl = getScreenElement(_element);
			INamingAdapter ina = (INamingAdapter) cdata
					.getAdapter(INamingAdapter.class);
			String elementName = ina == null ? cdata.getName() : ina.getName();

			if (screenEl != null && screenEl.getParent() != null) {
				String screenName = screenEl.getParent().getText();
				attributes.put(IMarker.MESSAGE, elementName + " (" + screenName
						+ ")");
			} else {
				String categoryName = cdata.getParent().getName();
				attributes.put(IMarker.MESSAGE, elementName + " ("
						+ categoryName + ")");
			}

			attributes.put(ContentAttribute.NAME.name(), elementName);
			attributes.put(ContentAttribute.ID.name(), cdata.getId());

			Object o = attributes.get(IMarker.MESSAGE); 
			String proposal = "TODO: " + ((o instanceof String) ? (String) o : ""); 
			attributes.put(IMarker.MESSAGE, proposal);

			TaskPropertiesDialog dialog = new TaskPropertiesDialog(PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getShell());
			dialog.setResource(getResource());
			dialog.setInitialAttributes(attributes);
			dialog.create();
			IBrandingManager branding = BrandingExtensionManager
					.getBrandingManager();
			Image image = null;
			if (branding != null) {
				image = branding.getIconImageDescriptor().createImage();
				dialog.getShell().setImage(image);
			}
			if (dialog.open() == 0) {
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
			if (image != null) {
				image.dispose();
			}
		}
	}

}
