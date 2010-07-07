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
package com.nokia.tools.s60.editor.ui.views;

import java.util.List;

import org.eclipse.jface.action.Action;

import com.nokia.tools.media.utils.layers.ILayerEffect;

/**
 * Action for adding effects, with correct enablement computation
*/
public class AddEffectAction extends Action {

	ILayerEffect eff;

	List available;

	public AddEffectAction(ILayerEffect effect, List available) {
		eff = effect;
		this.available = available;
		setText(eff.getName());
	}

	@Override
	public void run() {
		eff.setSelected(true);
	}

	@Override
	public boolean isEnabled() {
		if (eff.isSelected())
			return false;
		for (Object x : available) {
			if (eff.getName().equals(((ILayerEffect) x).getName()))
				return true;
		}
		return false;
	}
}