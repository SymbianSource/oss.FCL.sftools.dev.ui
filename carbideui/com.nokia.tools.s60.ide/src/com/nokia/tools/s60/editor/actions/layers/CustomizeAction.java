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

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.s60.editor.ui.views.Messages;

public class CustomizeAction extends BaseLayerAction {
	
	public static final String ID = CustomizeAction.class.getSimpleName();

	public CustomizeAction(IImage image, ILayerActionsHost host, String title, boolean lazy) {
		super(image, host, lazy);
		setId(ID);
		setText(title == null ? Messages.Layers_AdjustEffect_title : title);
	}
	
	@Override
	public void run() {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if (page.findView("org.eclipse.ui.views.PropertySheet") == null) {
				page.showView("org.eclipse.ui.views.PropertySheet");
				page.showView("org.eclipse.ui.views.PropertySheet", null, IWorkbenchPage.VIEW_ACTIVATE);
			} else {
				page.showView("org.eclipse.ui.views.PropertySheet", null, IWorkbenchPage.VIEW_ACTIVATE);
			}
		} catch (Exception e) {
		}
	}

	@Override
	protected boolean calculateEnabled() {
		/* needs only be enabled when effect is in selection */
		if (getHost().getSelection().getFirstElement() instanceof ILayerEffect) {
			return true;
		}
		return false;
	}

}
