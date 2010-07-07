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
package com.nokia.tools.theme.ui.bitmap.propertysheet;

import java.util.Map;

import org.eclipse.gef.commands.ForwardUndoCompoundCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.platform.theme.BitmapProperties;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.propertysheet.tabbed.MultipleSelectionWidgetSection;

public class OptimizeSection extends MultipleSelectionWidgetSection {

	Label optimizeLabel;

	Button optimize;

	Label ditherLabel;

	Button dither;

	Label qualityLabel;

	Button qualityHigh;

	Button qualityLow;

	Button qualityPaletteDependant;

	Widget lastEventSouce;

	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);

		Composite composite = getWidgetFactory()
				.createFlatFormComposite(parent);

		Group group = getWidgetFactory().createGroup(composite,
				Messages.Label_Section_Optimize);
		group.setLayout(new FillLayout());

		FormData data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		group.setLayoutData(data);

		composite = getWidgetFactory().createFlatFormComposite(group);

		int leftCoordinate = getLabelWidth(composite, new String[] {
				Messages.Label_Colorize, Messages.Label_Optimize,
				Messages.Label_Dither });

		int rightCoordinate = getLabelWidth(composite, new String[] {
				Messages.Label_Color, Messages.Label_Quality });

		optimizeLabel = getWidgetFactory().createLabel(composite,
				Messages.Label_Optimize);
		data = new FormData();
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		data.left = new FormAttachment(0);
		optimizeLabel.setLayoutData(data);

		optimize = getWidgetFactory().createButton(composite, null, SWT.CHECK);
		optimize.addListener(SWT.Selection, this);
		optimize.addListener(SWT.Modify, this);
		data = new FormData();
		data.top = new FormAttachment(optimizeLabel, 0, SWT.CENTER);
		data.left = new FormAttachment(0, ITabbedPropertyConstants.HSPACE
				+ leftCoordinate);
		optimize.setLayoutData(data);

		ditherLabel = getWidgetFactory().createLabel(composite,
				Messages.Label_Dither);
		data = new FormData();
		data.top = new FormAttachment(optimize);
		data.left = new FormAttachment(0);
		ditherLabel.setLayoutData(data);

		dither = getWidgetFactory().createButton(composite, null, SWT.CHECK);
		dither.addListener(SWT.Selection, this);
		dither.addListener(SWT.Modify, this);
		data = new FormData();
		data.top = new FormAttachment(ditherLabel, 0, SWT.CENTER);
		data.left = new FormAttachment(0, ITabbedPropertyConstants.HSPACE
				+ leftCoordinate);
		dither.setLayoutData(data);

		qualityLabel = getWidgetFactory().createLabel(composite,
				Messages.Label_Quality);
		data = new FormData();
		data.top = new FormAttachment(dither, 0, SWT.CENTER);
		data.left = new FormAttachment(dither, ITabbedPropertyConstants.HSPACE);
		qualityLabel.setLayoutData(data);

		qualityHigh = getWidgetFactory().createButton(composite,
				Messages.Label_Quality_High, SWT.RADIO);
		qualityHigh.addListener(SWT.Selection, this);
		qualityHigh.addListener(SWT.Modify, this);
		data = new FormData();
		data.top = new FormAttachment(dither, 0, SWT.CENTER);
		data.left = new FormAttachment(dither, ITabbedPropertyConstants.HSPACE
				* 2 + rightCoordinate);
		qualityHigh.setLayoutData(data);

		qualityLow = getWidgetFactory().createButton(composite,
				Messages.Label_Quality_Low, SWT.RADIO);
		qualityLow.addListener(SWT.Selection, this);
		qualityLow.addListener(SWT.Modify, this);
		data = new FormData();
		data.top = new FormAttachment(dither, 0, SWT.CENTER);
		data.left = new FormAttachment(qualityHigh);
		qualityLow.setLayoutData(data);

		qualityPaletteDependant = getWidgetFactory().createButton(composite,
				Messages.Label_Quality_PaletteDependant, SWT.RADIO);
		qualityPaletteDependant.addListener(SWT.Selection, this);
		qualityPaletteDependant.addListener(SWT.Modify, this);
		data = new FormData();
		data.top = new FormAttachment(dither, 0, SWT.CENTER);
		data.left = new FormAttachment(qualityLow);
		qualityPaletteDependant.setLayoutData(data);

		syncWithOther(group, BitmapPropertiesSection.class.getName());
		parent.getParent().pack();
	}

	@Override
	protected void doHandleEvent(Event e) {
		if (e.type == SWT.Selection) {
			lastEventSouce = e.widget;
			ForwardUndoCompoundCommand command = new ForwardUndoCompoundCommand(
					"Set bitmap properties");
			for (IContentData data : getContents()) {
				ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) data
						.getAdapter(ISkinnableEntityAdapter.class);
				BitmapProperties bitmap = new BitmapProperties(adapter
						.getAttributes());
				bitmap.setOptimize(optimize.getSelection());
				bitmap.setDither(dither.getSelection());
				if (e.widget == qualityHigh) {
					bitmap.setQuality(BitmapProperties.OPT_QUALITY_HIGH);
				} else if (e.widget == qualityLow) {
					bitmap.setQuality(BitmapProperties.OPT_QUALITY_LOW);
				} else if (e.widget == qualityPaletteDependant) {
					bitmap.setQuality(BitmapProperties.OPT_QUALITY_PALETTE_DEP);
				}
				if (qualityPaletteDependant.getSelection()) {
					bitmap.setDither(false);
				}
				command.add(adapter.getApplyBitmapPropertiesCommand(bitmap));
			}
			execute(command);
			updateControlsEnablementState();
			doRefresh();
		}
	}

	@Override
	protected void doRefresh() {
		IContentData data = getFirstContent();
		if (data == null) {
			return;
		}
		ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) data
				.getAdapter(ISkinnableEntityAdapter.class);
		Map attributes = adapter.getAttributes();
		Boolean optimizeSelected = (Boolean) attributes
				.get(BitmapProperties.IS_OPTIMIZE_SELECTED);
		Boolean ditherSelected = (Boolean) attributes
				.get(BitmapProperties.DITHER_SELECTED);
		String quality = (String) attributes
				.get(BitmapProperties.OPTIMIZE_SELECTION);
		boolean optimize = optimizeSelected != null && optimizeSelected;

		this.optimize.setSelection(optimize);

		boolean dither = ditherSelected != null && ditherSelected;

		this.dither.setSelection(dither);

		if (BitmapProperties.OPT_QUALITY_HIGH.equalsIgnoreCase(quality)) { //$NON-NLS-1$
			this.qualityHigh.setSelection(true);
		} else {
			this.qualityHigh.setSelection(false);
		}

		if (BitmapProperties.OPT_QUALITY_LOW.equalsIgnoreCase(quality)) { //$NON-NLS-1$
			this.qualityLow.setSelection(true);
		} else {
			this.qualityLow.setSelection(false);
		}

		if (BitmapProperties.OPT_QUALITY_PALETTE_DEP.equalsIgnoreCase(quality)) { //$NON-NLS-1$
			this.qualityPaletteDependant.setSelection(true);
		} else {
			this.qualityPaletteDependant.setSelection(false);
		}

		updateControlsEnablementState();
	}

	protected void updateControlsEnablementState() {
		ditherLabel.setEnabled(optimize.getSelection());
		dither.setEnabled(optimize.getSelection());
		qualityLabel.setEnabled(optimize.getSelection());
		qualityHigh.setEnabled(optimize.getSelection());
		qualityLow.setEnabled(optimize.getSelection());
		qualityPaletteDependant.setEnabled(optimize.getSelection());
	}
}
