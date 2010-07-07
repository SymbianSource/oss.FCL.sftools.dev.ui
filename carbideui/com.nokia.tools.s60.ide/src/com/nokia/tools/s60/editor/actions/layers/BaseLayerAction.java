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

import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import com.nokia.tools.media.utils.layers.IImage;

public abstract class BaseLayerAction extends WorkbenchPartAction implements ISelectionChangedListener {
	
	private IImage image;
	private ILayerActionsHost host;
	private boolean listenSelection;
	
	public BaseLayerAction(IImage image, ILayerActionsHost host, boolean lazy) {
		super(host == null ? null : host.getWorkbenchPart());
		setLazyEnablementCalculation(lazy);
		this.image = image;
		this.host = host;
		if (!lazy && image != null && host != null)
			setEnabled(calculateEnabled());
	}
	
	protected IImage getImage() {
		return image != null ? image : host.getActiveImage();
	}
	
	protected ILayerActionsHost getHost() {
		return host;
	}
	
	public void selectionChanged(SelectionChangedEvent event) {		
		update();		
	}

	public boolean isListenSelection() {
		return listenSelection;
	}

	public void listenSelection() {
		this.listenSelection = true;
		host.addSelectionListener(this);
	}
	
	@Override
	public void dispose() {
		if (listenSelection)
			host.removeSelectionListener(this);
		super.dispose();
	}

}
