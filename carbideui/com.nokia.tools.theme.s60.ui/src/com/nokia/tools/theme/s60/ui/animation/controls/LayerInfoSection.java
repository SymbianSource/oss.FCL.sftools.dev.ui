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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.platform.theme.EffectConstants;
import com.nokia.tools.theme.s60.effects.EffectObject;

public class LayerInfoSection implements ISection, Listener {

	protected Composite parent;

	protected TabbedPropertySheetPage propertySheetPage;

//	protected Text nameText;

	protected ILayer layer;

	protected PropertyChangeListener layerPropertyChangeListener;

	protected Composite effectsGroup;

	protected List<Button> effectButtons = new ArrayList<Button>();

	@SuppressWarnings("unchecked")
	public void createControls(Composite parent,
			TabbedPropertySheetPage tabbedPropertySheetPage) {
		this.parent = parent;
		this.propertySheetPage = tabbedPropertySheetPage;

		// set scrollbar settings
		Composite comp = parent;
		while (comp != null && !(comp instanceof ScrolledComposite)) {
			comp.layout(true);
			comp = comp.getParent();
		}
		if (comp != null) {
			final ScrolledComposite scComp = (ScrolledComposite) comp;
			scComp.getVerticalBar().setIncrement(10);
			scComp.getHorizontalBar().setIncrement(10);
			scComp.addControlListener(new ControlListener() {
				public void controlResized(ControlEvent e) {
					scComp.getVerticalBar().setPageIncrement(
							scComp.getSize().y / 2);
					scComp.getHorizontalBar().setPageIncrement(
							scComp.getSize().x / 2);
				}

				public void controlMoved(ControlEvent e) {
				}
			});
		}

		Composite composite = getWidgetFactory()
				.createFlatFormComposite(parent);

		Composite group = getWidgetFactory().createFlatFormComposite(composite);
		FormData data = new FormData();
		data.top = new FormAttachment(0, 0);
		data.left = new FormAttachment(0, 0);
		group.setLayoutData(data);

		group.setLayout(new GridLayout(2, false));
		((GridLayout) group.getLayout()).marginLeft = ITabbedPropertyConstants.HSPACE;
		((GridLayout) group.getLayout()).marginRight = ITabbedPropertyConstants.HSPACE;
		((GridLayout) group.getLayout()).marginWidth = 0;
		((GridLayout) group.getLayout()).horizontalSpacing = ITabbedPropertyConstants.HSPACE;
		((GridLayout) group.getLayout()).verticalSpacing = ITabbedPropertyConstants.VSPACE;

//		// name
//		getWidgetFactory().createCLabel(group, Messages.Label_Name);
//
//		nameText = getWidgetFactory().createText(group, "", SWT.NONE);
//
//		GridData gd = new GridData();
//		gd.grabExcessHorizontalSpace = true;
//		gd.horizontalAlignment = SWT.FILL;
//		gd.minimumWidth = 200;
//		nameText.setLayoutData(gd);
//		nameText.addListener(SWT.FocusOut, this);
//		nameText.addKeyListener(enterAdapter);

		layerPropertyChangeListener = new PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent evt) {
				refresh();
			};
		};

		composite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (layer != null) {
					layer
							.removePropertyChangeListener(layerPropertyChangeListener);
				}
			}
		});

		effectsGroup = getWidgetFactory().createGroup(composite,
				Messages.Label_Effects);
		data = new FormData();
		data.top = new FormAttachment(group, 0);
		data.left = new FormAttachment(0, ITabbedPropertyConstants.HSPACE);
		effectsGroup.setLayoutData(data);

		effectsGroup.setLayout(new GridLayout(4, false));
		((GridLayout) effectsGroup.getLayout()).marginLeft = ITabbedPropertyConstants.HSPACE;
		((GridLayout) effectsGroup.getLayout()).marginRight = ITabbedPropertyConstants.HSPACE;
		((GridLayout) effectsGroup.getLayout()).marginWidth = 0;
		((GridLayout) effectsGroup.getLayout()).horizontalSpacing = ITabbedPropertyConstants.HSPACE;
		((GridLayout) effectsGroup.getLayout()).verticalSpacing = ITabbedPropertyConstants.VSPACE;

		List<EffectObject> layerEffects = EffectObject.getEffectList();
		for (ILayerEffect effect : layerEffects) {
			Button effectButton = new Button(effectsGroup, SWT.CHECK);
			effectButton.setBackground(effectsGroup.getBackground());
			effectButton.setText(effect.getName());
			effectButton.setData(effect);
			effectButton.addListener(SWT.Selection, this);
			effectButtons.add(effectButton);
		}
	}

	/**
	 * Get the widget factory for the property sheet page.
	 * 
	 * @return the widget factory.
	 */
	public TabbedPropertySheetWidgetFactory getWidgetFactory() {
		return propertySheetPage.getWidgetFactory();
	}

	public void setInput(IWorkbenchPart part, ISelection selection) {
		if (layer != null) {
			layer.removePropertyChangeListener(layerPropertyChangeListener);
		}
		layer = (ILayer) ((IStructuredSelection) selection).getFirstElement();
		layer.addPropertyListener(layerPropertyChangeListener);
	}

	public void aboutToBeShown() {
	}

	public void aboutToBeHidden() {
	}

	public void dispose() {
		if (layer != null) {
			layer.removePropertyChangeListener(layerPropertyChangeListener);
		}
	}

	public int getMinimumHeight() {
		return SWT.DEFAULT;
	}

	public boolean shouldUseExtraSpace() {
		return true;
	}

	public void refresh() {
//		nameText.setText(layer.getName());
//		nameText.setEnabled(!layer.isBackground());

		Map<String, ILayerEffect> availableEffectsMap = new HashMap<String, ILayerEffect>();
		List<ILayerEffect> availableEffects = layer.getAvailableLayerEffects();
		for (ILayerEffect effect : availableEffects) {
			availableEffectsMap.put(effect.getName(), effect);
		}

		Map<String, ILayerEffect> selectedEffectsMap = new HashMap<String, ILayerEffect>();
		List<ILayerEffect> selectedEffects = layer.getSelectedLayerEffects();
		for (ILayerEffect effect : selectedEffects) {
			selectedEffectsMap.put(effect.getName(), effect);
		}

		Set<String> availableEffectNames = availableEffectsMap.keySet();
		Set<String> selectedEffectNames = selectedEffectsMap.keySet();
		for (Button effectButton : effectButtons) {
			ILayerEffect buttonEffect = (ILayerEffect) effectButton.getData();
			effectButton.setSelection(selectedEffectNames.contains(buttonEffect
					.getName()));
			if (!EffectConstants.APPLYGRAPHICS.equalsIgnoreCase(buttonEffect.getName())) {
				if (availableEffectNames.contains(buttonEffect.getName())
						|| selectedEffectNames.contains(buttonEffect.getName())) {
					effectButton.setEnabled(true);
				} else {
					effectButton.setEnabled(false);
				}
			} else {
				effectButton.setEnabled(false);
			}
		}

		parent.layout(true, true);
	}

	public void handleEvent(Event event) {
//		if (event.widget == nameText) {
//			if (event.type == SWT.FocusOut) {
//				if ("background".equalsIgnoreCase(nameText.getText())) {
//					return;
//				}
//				if (!nameText.getText().equals(layer.getName())) {
//					try {
//						layer.setName(nameText.getText());
//					} catch (Exception e) {
//						System.err.println(e.getMessage());
//						nameText.setText(layer.getName());
//						MessageDialog.openError(parent.getShell(),
//								Messages.Error_Input, e.getLocalizedMessage());
//					}
//				}
//			}
//		}
		if (event.widget instanceof Button && event.type == SWT.Selection
				&& effectButtons.contains(event.widget)) {
			Button effectButton = (Button) event.widget;
			ILayerEffect effect = (ILayerEffect) effectButton.getData();
			if (effectButton.getSelection()) {
				layer.addLayerEffect(effect.getName());
			} else {
				layer.removeLayerEffect(effect.getName());
			}
			refresh();
		}
	}
}
