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

import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.s60.editor.ui.views.Messages;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;

public class ClearLayerAction extends BaseLayerAction {
	
	public static final String ID = ClearLayerAction.class.getSimpleName();
	
	public ClearLayerAction(IImage image, ILayerActionsHost host, boolean lazy) {
		super(image, host, lazy);
		setId(ID);
		setText(Messages.Layers_ClearLayerAction_title);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/etool16/clear_co.gif"));
		setDisabledImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/dtool16/clear_co.gif"));
		setToolTipText(Messages.Layers_ClearLayerAction_title);
	}
	
	@Override
	public void run() {
		Object sel = getHost().getSelection().getFirstElement();
		if (sel instanceof ILayer) {
			((ILayer)sel).clearLayer();
		}
	}

	@Override
	protected boolean calculateEnabled() {
		Object sel = getHost().getSelection().getFirstElement();
		if (sel instanceof ILayer) {
			return !((ILayer)sel).isBackground();
		}
		return false;
	}

}
