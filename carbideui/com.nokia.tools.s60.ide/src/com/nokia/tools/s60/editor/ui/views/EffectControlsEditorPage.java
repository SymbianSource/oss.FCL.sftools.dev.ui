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
/*
 * 
 */
package com.nokia.tools.s60.editor.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.Page;

import com.nokia.tools.media.utils.layers.ILayerEffect;

/**
 */
public class EffectControlsEditorPage extends Page {

	private Composite container;

	private EffectControlsComposite controls;

	/**
	 * The constructor.
	 */
	public EffectControlsEditorPage() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (controls != null) {
			controls.setFocus();
		} else {
			container.setFocus();
		}
	}

	/**
	 * Called when the editor input is changed and the view must be refreshed.
	 * 
	 * @param part
	 *            the editor part.
	 */
	public void inputChanged(final ILayerEffect effect) {
		if (Display.getCurrent() == null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					inputChanged(effect);
				}
			});
			return;
		}

		clearArea(container);

		if (effect != null) {
			controls = new EffectControlsComposite(container, SWT.NONE);
			controls.setLayerEffect(effect);
			container.layout(true);
			container.redraw();
		} else {
			controls = null;
		}
	}

	/**
	 * Clears the composite area (disposes all it's children)
	 */
	private void clearArea(Composite comp) {
		Control[] controls = comp.getChildren();
		for (int i = 0; i < controls.length; i++) {
			controls[i].dispose();
		}
		comp.layout();
	};

	@Override
	public Control getControl() {
		return container;
	}

	public ILayerEffect getEffect() {
		if (controls != null) {
			return controls.getLayerEffect();
		}
		return null;
	}
}