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

package com.nokia.tools.theme.s60.ui.animation.controls;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.theme.s60.ui.animation.EffectParameterUIContributor;

public class LayerEffectStaticControlsSection implements ISection {

	protected ILayerEffect effect;

	protected Composite parent;

	protected Composite staticContextContainer;

	public void createControls(Composite parent,
			TabbedPropertySheetPage tabbedPropertySheetPage) {
		this.parent = parent;
		
		staticContextContainer = new Composite(parent, SWT.NONE);
		staticContextContainer.setLayout(new GridLayout(2, false));
		((GridLayout) staticContextContainer.getLayout()).marginLeft = 10;
		((GridLayout) staticContextContainer.getLayout()).marginRight = 10;
		((GridLayout) staticContextContainer.getLayout()).marginTop = 5;
		((GridLayout) staticContextContainer.getLayout()).marginBottom = 0;
		((GridLayout) staticContextContainer.getLayout()).marginHeight = 0;
		((GridLayout) staticContextContainer.getLayout()).marginWidth = 0;
		((GridLayout) staticContextContainer.getLayout()).verticalSpacing = 0;
		((GridLayout) staticContextContainer.getLayout()).horizontalSpacing = 5;
	}

	public void setInput(IWorkbenchPart part, ISelection selection) {
		effect = (ILayerEffect) ((IStructuredSelection) selection)
				.getFirstElement();
	}

	public void aboutToBeShown() {
	}

	public void aboutToBeHidden() {
	}

	public void dispose() {
		if (staticContextContainer != null
				&& !staticContextContainer.isDisposed()) {
			clearStaticContextContainer();
			staticContextContainer.dispose();
		}
	}

	public int getMinimumHeight() {
		if (staticContextContainer == null) {
			return SWT.DEFAULT;
		}
		return staticContextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
	}

	public boolean shouldUseExtraSpace() {
		return true;
	}

	public void refresh() {
		updateStaticContext(effect);
	}

	protected void updateStaticContext(ILayerEffect effect) {
		clearStaticContextContainer();
		if (effect != null) {
			EffectParameterUIContributor contributor = new EffectParameterUIContributor(
					effect);
			contributor.createDialogControls(staticContextContainer);
			staticContextContainer.layout(true);
			updateStaticContextState(effect.isSelected());
		}
		updateBackgroundColor(staticContextContainer);
		staticContextContainer.layout(true);
	}

	protected void updateBackgroundColor(Composite composite) {
		composite.setBackground(composite.getParent().getBackground());
		Control[] controls = composite.getChildren();
		for (Control control : controls) {
			if (control instanceof Composite) {
				updateBackgroundColor((Composite) control);
			} else {
				control.setBackground(composite.getBackground());
			}
		}
	}

	protected void clearStaticContextContainer() {
		Control[] children = staticContextContainer.getChildren();
		for (Control control : children) {
			control.dispose();
		}
	}

	protected void updateStaticContextState(boolean enabled) {
		Control[] controls = staticContextContainer.getChildren();
		for (Control control : controls) {
			if (!control.isDisposed() && control.getEnabled() != enabled)
				control.setEnabled(enabled);
		}
	}

}
