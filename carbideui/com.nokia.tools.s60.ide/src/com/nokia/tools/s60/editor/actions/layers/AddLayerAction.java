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

import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.s60.editor.ui.views.Messages;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;

public class AddLayerAction extends BaseLayerAction {

	public static final String ID = "AddLayerAction";

	public AddLayerAction(IImage image, ILayerActionsHost host, boolean lazy) {
		super(image, host, lazy);
		setId(ID);
		setText(Messages.Layers_AddLayerAction_title);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/add_layer16x16.gif"));
		setToolTipText(Messages.Layers_AddLayerAction_title);
	}

	@Override
	public void run() {
		// Add another layer
		ILayer layer=getImage().addLayer();		
		getHost().selectActiveLayer(layer);
	}

	@Override
	protected boolean calculateEnabled() {	
		return !(getImage() instanceof IAnimatedImage)
				&& getImage() != null
				&& getImage().getLayerCount() < getImage()
						.getMaximumLayerCount();
	}

}
