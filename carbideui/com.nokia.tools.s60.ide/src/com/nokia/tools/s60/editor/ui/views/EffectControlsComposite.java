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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.layers.ILayerEffectDialogContributor;
import com.nokia.tools.theme.s60.ui.animation.EffectParameterUIContributor;

public class EffectControlsComposite extends Composite implements PropertyChangeListener{

	private ILayerEffect layerEffect;

	
	public EffectControlsComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new org.eclipse.swt.layout.GridLayout(2, false));
	}

	public ILayerEffect getLayerEffect() {
		return layerEffect;
	}

	public void setLayerEffect(ILayerEffect layerEffect) {
		this.layerEffect = layerEffect;
		layerEffect.addPropertyListener(this);
		updateComposite();
	}

	private void updateComposite() {
		ILayerEffectDialogContributor contributor = new EffectParameterUIContributor(
				layerEffect);
		contributor.createDialogControls(this);
		layout(true);
		updateEffectControlsState(layerEffect.isSelected());
	}
	
	private void updateEffectControlsState(boolean enabled) {
		Control[] controls = this.getChildren();
		for (Control control : controls) {
			if (control.getEnabled() != enabled)
				control.setEnabled(enabled);
		}
	}

	public void propertyChange(final PropertyChangeEvent evt) {
	}
	
	@Override
	public void dispose() {	
		super.dispose();
		layerEffect.removePropertyChangeListener(this);
	}
	
}
