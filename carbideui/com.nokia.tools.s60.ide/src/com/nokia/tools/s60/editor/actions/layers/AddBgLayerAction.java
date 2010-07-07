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

import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.s60.editor.ui.views.Messages;

public class AddBgLayerAction extends BaseLayerAction {

	public static final String ID = "AddBgLayerAction";

	public AddBgLayerAction(IImage image, ILayerActionsHost host, boolean lazy) {
		super(image, host, lazy);
		setId(ID);
		setText(Messages.Layers_AddBgLayerAction_title);
	}

	@Override
	public void run() {
		try {
			getImage().addLayer(0).setName(IMediaConstants.BackgroundLayer);
		} catch (Exception e) {
		}
	}

	@Override
	protected boolean calculateEnabled() {
		if (null == getImage() || getImage() instanceof IAnimatedImage)
			return false;

		if (!getImage().isBackground()) {
			if (getImage().getLayerCount() > 0
					&& !getImage().getLayer(0).isBackground()) {
				return getImage().getLayerCount() < getImage()
						.getMaximumLayerCount();
			}
		}
		return false;
	}

}
