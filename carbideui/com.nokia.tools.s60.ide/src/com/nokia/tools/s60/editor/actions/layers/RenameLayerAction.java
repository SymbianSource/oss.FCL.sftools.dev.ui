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

package com.nokia.tools.s60.editor.actions.layers;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.s60.editor.GraphicsEditorInput;
import com.nokia.tools.s60.editor.ui.dialogs.LayerPropertiesDialog;
import com.nokia.tools.s60.editor.ui.views.Messages;

public class RenameLayerAction extends BaseLayerAction {

	public static final String ID = RenameLayerAction.class.getSimpleName();
	
	public RenameLayerAction(IImage image, ILayerActionsHost host, boolean lazy) {
		super(image, host, lazy);
		setId(ID);
		setText(Messages.Layers_RenameLayerAction_title);
	}

	@Override
	public void run() {
		IStructuredSelection sel = getHost().getSelection();
		if (!sel.isEmpty()) {
			IProject project = null;
			if (getWorkbenchPart() instanceof IEditorPart) {
				IEditorInput input = ((IEditorPart) getWorkbenchPart()).getEditorInput();
				if (input instanceof GraphicsEditorInput) {
					input =  ((GraphicsEditorInput) input).getParentEditor().getEditorInput();
				}
				if (input instanceof IFileEditorInput) {
					project = ((IFileEditorInput) input).getFile().getProject();
				}
			}
			LayerPropertiesDialog dialog = new LayerPropertiesDialog(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
			dialog.create();
			dialog.setLayer((ILayer) sel.getFirstElement());
			dialog.setProject(project);
			if (dialog.open() == IDialogConstants.OK_ID) {
				getHost().refresh();
			}
		}
	}

	@Override
	protected boolean calculateEnabled() {
		if (getImage() instanceof IAnimatedImage)
			return false;
		IStructuredSelection sel = getHost().getSelection();
		if (!sel.isEmpty() && sel.getFirstElement() instanceof ILayer) {
			return !((ILayer) sel.getFirstElement()).isBackground();
		}
		return false;
	}

}
