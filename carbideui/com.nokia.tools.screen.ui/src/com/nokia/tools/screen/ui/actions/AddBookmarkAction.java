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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.screen.ui.utils.ScreenElementMarker;

public class AddBookmarkAction extends EditorPartAction {

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
		if (askForLabel(attributes, getActiveEditorPart().getSite().getShell())) {
			try {
				MarkerUtilities.createMarker(resource, attributes,
						IMarker.BOOKMARK);
			} catch (CoreException x) {
				UiPlugin.error(x);
			}

			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			if (page != null) {
				try {
					page.showView(IPageLayout.ID_BOOKMARKS, null,
							IWorkbenchPage.VIEW_CREATE);
					page.showView(IPageLayout.ID_BOOKMARKS, null,
							IWorkbenchPage.VIEW_ACTIVATE);

				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
	 */
	@Override
	protected boolean calculateEnabled() {
		return true;
	}

	public AddBookmarkAction(IEditorPart part) {
		super(part);
	}

	/**
	 * Get current selection's resource as
	 */
	protected IResource getResource() {
		ISelection selection = null;
		IResource resource = null;
		if (null != (selection = getActiveEditorPart().getEditorSite()
				.getSelectionProvider().getSelection())) {
			if (selection instanceof IAdaptable) {
				resource = (IResource) ((IAdaptable) selection)
						.getAdapter(IResource.class);
			}
		}
		if (null == resource
				&& getActiveEditorPart().getEditorInput() instanceof IFileEditorInput) {
			resource = ((IFileEditorInput) getActiveEditorPart()
					.getEditorInput()).getFile();
		}
		return resource;
	}

	
	protected Map<String, Object> getSelectionAttributes() {

		IStructuredSelection selection = null;
		
		if (null != (selection = (IStructuredSelection) getActiveEditorPart()
				.getEditorSite().getSelectionProvider().getSelection())) {
			if (selection.getFirstElement() instanceof EditPart) {

				IScreenElement screenElement = JEMUtil
						.getScreenElement(selection.getFirstElement());

				if (screenElement != null) {
					try {
						return new ScreenElementMarker(screenElement)
								.getAttributes();
					} catch (CoreException e) {
						e.printStackTrace();
					}

				}

			}
		}
		Map<String, Object> attribs = new HashMap<String, Object>(11);
		attribs.put(IMarker.MESSAGE, "S60 Theme");
		return attribs;
	}

	protected IEditorPart getActiveEditorPart() {
		if (EclipseUtils.getActiveSafeEditor() != null)
			return EclipseUtils.getActiveSafeEditor();
		return super.getEditorPart();
	}

	// changed to static, as is used also from s60.ide plugin action
	public static boolean askForLabel(Map<String, Object> attributes,
			Shell shell) {

		Object o = attributes.get(IMarker.MESSAGE); //$NON-NLS-1$
		String proposal = (o instanceof String) ? (String) o : ""; //$NON-NLS-1$
		if (proposal == null)
			proposal = ""; //$NON-NLS-1$

		String title = "Add Bookmark";
		String message = "Enter Bookmark name:";
		IInputValidator inputValidator = new IInputValidator() {
			public String isValid(String newText) {
				return (newText == null || newText.trim().length() == 0) ? " " : null; //$NON-NLS-1$
			}
		};
		InputDialog dialog = new InputDialog(shell, title, message, proposal,
				inputValidator);

		String label = null;
		if (dialog.open() != Window.CANCEL)
			label = dialog.getValue();

		if (label == null)
			return false;

		label = label.trim();
		if (label.length() == 0)
			return false;

		attributes.put(IMarker.MESSAGE, label); //$NON-NLS-1$
		return true;
	}

}
