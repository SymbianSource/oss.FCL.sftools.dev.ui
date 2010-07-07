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

import java.beans.PropertyChangeListener;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.ITimeLine;

public class LayerEffectAnimatedControlsSection implements ISection,
		IPropertyChangeListener, ITimeModelChangeListener {

	protected ILayerEffect effect;

	protected Composite parent;

	protected Composite animatedContextContainer;

	protected ILayerEffectPropertySheetPage propertySheetPage;

	protected PropertyChangeListener effectPropertyChangeListener;

	public void createControls(Composite parent,
			TabbedPropertySheetPage tabbedPropertySheetPage) {
		this.parent = parent;
		this.propertySheetPage = (ILayerEffectPropertySheetPage) tabbedPropertySheetPage;

		this.propertySheetPage.addTimeModelChangedListener(this);

		animatedContextContainer = new Composite(parent, SWT.NONE);
		animatedContextContainer.setLayout(new FillLayout());

		animatedContextContainer.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
				try {
					if (getAnimatedContextControls() != null) {
						getAnimatedContextControls().dispose();
					}

					propertySheetPage
							.removeTimeModelChangedListener(LayerEffectAnimatedControlsSection.this);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				if (effect != null) {
					effect.removePropertyChangeListener(effectPropertyChangeListener);
				}
			};
		});

		effectPropertyChangeListener = new EffectPropertyChangeListener();
	}

	public void setInput(IWorkbenchPart part, ISelection selection) {
		if (effect != null) {
			effect.removePropertyChangeListener(effectPropertyChangeListener);
		}
		effect = (ILayerEffect) ((IStructuredSelection) selection)
				.getFirstElement();
		effect.addPropertyListener(effectPropertyChangeListener);
	}

	public void aboutToBeShown() {
	}

	public void aboutToBeHidden() {
	}

	public void dispose() {
		if (effect != null) {
			effect.removePropertyChangeListener(effectPropertyChangeListener);
		}
		if (animatedContextContainer != null
				&& !animatedContextContainer.isDisposed()) {
			clearAnimatedContextContainer();
			animatedContextContainer.dispose();
		}
	}

	public int getMinimumHeight() {
		if (animatedContextContainer == null) {
			return SWT.DEFAULT;
		}
		return animatedContextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
	}

	public boolean shouldUseExtraSpace() {
		return true;
	}

	public void refresh() {
		updateAnimatedContext(effect, propertySheetPage.getTimeModel(),
				propertySheetPage.getTimeSpan(), propertySheetPage
						.getTimeLine());
	}

	protected EffectAnimationUI getAnimatedContextControls() {
		if (animatedContextContainer != null
				&& !animatedContextContainer.isDisposed()) {
			Control[] children = animatedContextContainer.getChildren();
			return (EffectAnimationUI) (children.length == 1 ? children[0]
					: null);
		} else {
			return null;
		}
	}

	protected void updateAnimatedContext(ILayerEffect effect, TimingModel tm,
			TimeSpan ts, ITimeLine timeLine) {
		if (getAnimatedContextControls() != null) {
			getAnimatedContextControls().removePropertyChangedListener(this);
		}		
		clearAnimatedContextContainer();
		if (effect != null) {
			EffectAnimationUI ctrls = new EffectAnimationUI(
					animatedContextContainer, SWT.NONE, tm, ts);
			ctrls.init(effect, timeLine);
			if (timeLine.getCurrentControlPoint() != null) {
				ctrls.timeChanged(timeLine.getCurrentControlPoint().getTime());
			} else {
				ctrls.timeChanged(timeLine.getCurrentTime());
			}
			ctrls.addPropertyChangedListener(this);
		}
		updateBackgroundColor(animatedContextContainer);
		animatedContextContainer.layout(true);
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

	protected void clearAnimatedContextContainer() {
		Control[] children = animatedContextContainer.getChildren();
		for (Control control : children) {
			control.dispose();
		}
	}

	public void propertyChange(PropertyChangeEvent event) {
		propertySheetPage.propertyChange(event);
	}

	class EffectPropertyChangeListener implements PropertyChangeListener {
		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					refresh();					
				};
			});
		}
	}

	public void timeModelChanged(TimingModel timeModel, TimeSpan timeSpan) {
		refresh();
	}
}
