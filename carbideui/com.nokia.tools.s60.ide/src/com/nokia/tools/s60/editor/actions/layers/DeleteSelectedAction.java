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

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.s60.editor.ui.views.Messages;

public class DeleteSelectedAction extends BaseLayerAction {
	
	public static final String ID = DeleteSelectedAction.class.getSimpleName();

	private String customTitle;
	
	public DeleteSelectedAction(ILayerActionsHost host, String title, boolean lazy) {
		super(null, host, lazy);
		setId(ID);
		setText(title == null ? Messages.Layers_DeleteAction_title : title);
		setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_DELETE));
		setDisabledImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_DELETE_DISABLED));
		this.customTitle = title;
		if (!lazy && host != null)
			setEnabled(calculateEnabled());
	}
	
	@Override
	public void run() {
		getHost().deleteSelected(null);
	}

	@Override
	protected boolean calculateEnabled() {
		boolean enabled = getHost().canDeleteSelected(null);
		if (customTitle == null)
			if (enabled) {
				Object sel = getHost().getSelection().getFirstElement();
				if (sel instanceof ILayer) 
					setText(Messages.Layers_RemoveLayerAction_title);
				else if (sel instanceof ILayerEffect) {
					setText(Messages.Layers_RemoveEffectAction_title);
				}
			} else {
				setText(Messages.Layers_DeleteAction_title);
			}
		return enabled;
	}
	
}
