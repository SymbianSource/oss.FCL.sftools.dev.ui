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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.screen.ui.dialogs.MessageDialogWithCheckBox;
import com.nokia.tools.screen.ui.utils.FileChangeWatchThread.FileInfo;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;
import com.nokia.tools.ui.dialog.MessageDialogWithTextContent;

/**

 */
public abstract class AbstractEditAction extends
		AbstractMultipleSelectionAction {

	// list of watcher threads on paint processes
	protected List<FileInfo> paintProcessThreads = new ArrayList<FileInfo>();

	public AbstractEditAction(ISelectionProvider provider, CommandStack _stack) {
		super(null);
		setSelectionProvider(provider);
		this.stack = _stack;
	}

	public AbstractEditAction(IWorkbenchPart part) {
		super(part);
	}

	public AbstractEditAction(IWorkbenchPart part, int style) {
		super(part, style);
	}

	protected void handleProcessException(Throwable e) {
		if (e instanceof IllegalArgumentException) {
			displayMissingEditorInfo();
		} else {
			S60WorkspacePlugin.error(e);
			MessageDialogWithTextContent.openError(PlatformUI.getWorkbench()
					.getDisplay().getActiveShell(),
					Messages.EditInExternalAction_executionErrorTitle,
					Messages.EditInExternalAction_executionErrorMessage,
					StringUtils.dumpThrowable(e));
		}
	}

	protected void displayMissingEditorInfo() {
		IBrandingManager branding = BrandingExtensionManager
				.getBrandingManager();
		Image image = null;
		if (branding != null) {
			image = branding.getIconImageDescriptor().createImage();
		}
		MessageDialogWithCheckBox dialog = new MessageDialogWithCheckBox(
				PlatformUI.getWorkbench().getDisplay().getActiveShell(),
				Messages.EditInExternalAction_toolSettingsError, image,
				Messages.EditInExternalAction_openToolError, null, false, null,
				Messages.EditInExternalAction_configureTool,
				Messages.EditInExternalAction_preferenceAddress, 1,
				new String[] { IDialogConstants.OK_LABEL }, 0);
		dialog.setPendingAction(this);
		dialog.open();
		if (image != null) {
			image.dispose();
		}
	}

	@Override
	public void dispose() {
		for (FileInfo info : paintProcessThreads) {
			info.setCanUpdate(false);
		}
		paintProcessThreads.clear();
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#init()
	 */
	@Override
	protected void init() {
		setLazyEnablementCalculation(true);
	}

	@Override
	protected boolean doCalculateEnabled(Object element) {
		return false;
	}

	@Override
	protected void doRun(Object element) {
	}
}
