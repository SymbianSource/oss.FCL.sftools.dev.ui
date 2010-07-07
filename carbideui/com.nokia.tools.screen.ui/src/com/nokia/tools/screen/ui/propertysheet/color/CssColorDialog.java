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
package com.nokia.tools.screen.ui.propertysheet.color;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.nokia.tools.ui.color.ColorDescriptor;

public class CssColorDialog extends Dialog implements IColorPickerListener {

	public void selectionChanged() {

	}

	String colorChangingString;

	ColorChangedLabelWrapper colorChangedLabelWrapper;

	TabFolder tabFolder = null;
	TabItem customColorTabItem = null;

	public CssColorDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.getShell().setText(Messages.CssColorDialog_DialogTitle);
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout layout = (GridLayout) composite.getLayout();
		layout.numColumns = 2;
		layout.marginRight = 0;

		tabFolder = new TabFolder(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		tabFolder.setLayoutData(gd);
		tabFolder.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				TabFolder x = (TabFolder) e.widget;
				int index = x.getSelectionIndex();

				if (index == 3) {
					TabItem tabItem = x.getItem(3);
					if( tabItem.getControl() instanceof CustomColorComposite )
					{
					CustomColorComposite ccc = (CustomColorComposite) tabItem
							.getControl();
					ccc.updatePreview();
					ccc.updateSliders();
					}
				}

			}

		});
		customColorTabItem = new TabItem(tabFolder, SWT.NULL);
		customColorTabItem.setText(Messages.CssColorDialog_CustomColorTab);

		TabItem webPaletteTabItem = new TabItem(tabFolder, SWT.NULL);
		webPaletteTabItem.setText(Messages.CssColorDialog_WebPaletteTab);

		TabItem namedColorTabItem = new TabItem(tabFolder, SWT.NULL);
		namedColorTabItem.setText(Messages.CssColorDialog_NamedColorTab);

		TabItem systemColorTabItem = new TabItem(tabFolder, SWT.NULL);
		systemColorTabItem.setText(Messages.CssColorDialog_SystemColorTab);

		CustomColorComposite customColorComposite = new CustomColorComposite(
				tabFolder, SWT.NONE, this);
		customColorTabItem.setControl(customColorComposite);

		WebPaletteComposite webPaletteComposite = new WebPaletteComposite(
				tabFolder, SWT.NONE, this);

		webPaletteTabItem.setControl(webPaletteComposite);

		NamedColorComposite namedColorComposite = new NamedColorComposite(
				tabFolder, SWT.NONE, this);

		namedColorTabItem.setControl(namedColorComposite);

		SystemColorComposite systemColorComposite = new SystemColorComposite(
				tabFolder, SWT.NONE, this);

		systemColorTabItem.setControl(systemColorComposite);

		Label colorLabel = new Label(composite, SWT.NONE);
		colorLabel.setText(Messages.CssColorDialog_Label_Colour);
		gd = new GridData();
		colorLabel.setLayoutData(gd);

		Label colorChangingLabel = new Label(composite, SWT.NONE);
		colorChangedLabelWrapper = new ColorChangedLabelWrapper();
		colorChangedLabelWrapper.setColorChangedLabel(colorChangingLabel);
		colorChangedLabelWrapper.setColorString(colorChangingString);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		colorChangingLabel.setLayoutData(gd);

		Label formatLabel = new Label(composite, SWT.NONE);
		formatLabel.setText(Messages.CssColorDialog_Label_Format);
		gd = new GridData(SWT.END, SWT.CENTER, false, false);
		gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
		formatLabel.setLayoutData(gd);

		final Combo comboBox = new Combo(composite, SWT.READ_ONLY
				| SWT.DROP_DOWN);
		comboBox.setItems(ColorChangedLabelWrapper.getPresentationTypes());
		comboBox.setText(colorChangedLabelWrapper
				.getCurrentPresentationStyleName());

		gd = new GridData();
		gd.horizontalAlignment = SWT.BEGINNING;
		comboBox.setLayoutData(gd);
		comboBox.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				String presentationStyle = comboBox.getText();
				if (ColorChangedLabelWrapper.HASHED_PRESENTATION_TYPE_NAME
						.equals(presentationStyle)) {
					colorChangedLabelWrapper
							.setCurrentPresentationType(ColorChangedLabelWrapper.HASHED_PRESENTATION_TYPE);
				} else if (ColorChangedLabelWrapper.RGB_FUNCTION_PRESENTATION_TYPE_NAME
						.equals(presentationStyle)) {
					colorChangedLabelWrapper
							.setCurrentPresentationType(ColorChangedLabelWrapper.RGB_FUNCTION_PRESENTATION_TYPE);
				} else if (ColorChangedLabelWrapper.RGB_FUNCTION_PERCENTAGE_PRESENTATION_TYPE_NAME
						.equals(presentationStyle)) {
					colorChangedLabelWrapper
							.setCurrentPresentationType(ColorChangedLabelWrapper.RGB_FUNCTION_PERCENTAGE_PRESENTATION_TYPE);
				} else if (ColorChangedLabelWrapper.HASHED_PRESENTATION_SHORT_TYPE_NAME
						.equals(presentationStyle)) {
					colorChangedLabelWrapper
							.setCurrentPresentationType(ColorChangedLabelWrapper.HASHED_PRESENTATION_SHORT_TYPE);
				} else {
					colorChangedLabelWrapper
							.setCurrentPresentationType(ColorChangedLabelWrapper.NAMED_PRESENTATION_TYPE);
				}
				String colorString;
				if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.HASHED_PRESENTATION_TYPE) {
					if (colorChangedLabelWrapper.getColorDescriptor().getName() != null) {
						colorString = colorChangedLabelWrapper
								.getColorNameColorStringWithHash();
					} else {
						colorString = colorChangedLabelWrapper
								.getHashedColorString();
					}
				} else if (colorChangedLabelWrapper
						.getCurrentPresentationType() == ColorChangedLabelWrapper.RGB_FUNCTION_PRESENTATION_TYPE) {
					if (colorChangedLabelWrapper.getColorDescriptor().getName() != null) {
						colorString = colorChangedLabelWrapper
								.getColorNameColorStringWithFunction();
					} else {
						colorString = colorChangedLabelWrapper
								.getFunctionStyleColorString();
					}
				} else if (colorChangedLabelWrapper
						.getCurrentPresentationType() == ColorChangedLabelWrapper.RGB_FUNCTION_PERCENTAGE_PRESENTATION_TYPE) {
					if (colorChangedLabelWrapper.getColorDescriptor().getName() != null) {
						colorString = colorChangedLabelWrapper
								.getColorNameColorStringWithFunctionAndPercentage();
					} else {
						colorString = colorChangedLabelWrapper
								.getFunctionAndPercentageStyleColorString();
					}
				} else if (colorChangedLabelWrapper
						.getCurrentPresentationType() == ColorChangedLabelWrapper.HASHED_PRESENTATION_SHORT_TYPE) {
					if (colorChangedLabelWrapper.getColorDescriptor().getName() != null) {
						colorString = colorChangedLabelWrapper
								.getShortHashedColorWithApproximatedColorName();
					} else {
						colorString = colorChangedLabelWrapper
								.getShortHashedColorString();
					}
				} else {
					if (colorChangedLabelWrapper.getColorDescriptor().getName() != null) {
						colorString = colorChangedLabelWrapper
								.getColorNameColorStringWithHash();
					} else {
						colorString = colorChangedLabelWrapper
								.getHashedColorString();
					}

				}
				colorChangedLabelWrapper.getColorChangedLabel().setText(
						colorString);

			}

		});
		comboBox.setVisible(false);
		formatLabel.setVisible(false);

		webPaletteComposite.setColorChangedLabel(colorChangedLabelWrapper);
		namedColorComposite.setColorChangedLabel(colorChangedLabelWrapper);
		customColorComposite.setColorChangedLabel(colorChangedLabelWrapper);
		systemColorComposite.setColorChangedLabel(colorChangedLabelWrapper);

		customColorComposite.fillHexColor();

		return composite;
	}

	@Override
	protected void okPressed() {
		String outputColorString;
		if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.HASHED_PRESENTATION_TYPE) {
			outputColorString = colorChangedLabelWrapper.getHashedColorString();
		} else if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.RGB_FUNCTION_PRESENTATION_TYPE) {
			outputColorString = colorChangedLabelWrapper
					.getFunctionStyleColorString();
		} else if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.NAMED_PRESENTATION_TYPE) {
			if (ColorDescriptor.colorHasCssUsableName(colorChangedLabelWrapper
					.getColorDescriptor().getName())) {
				outputColorString = colorChangedLabelWrapper
						.getColorNameColorString();
			} else {
				outputColorString = colorChangedLabelWrapper
						.getHashedColorString();
			}
		} else if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.RGB_FUNCTION_PERCENTAGE_PRESENTATION_TYPE) {
			outputColorString = colorChangedLabelWrapper
					.getFunctionAndPercentageStyleColorString();
		} else if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.HASHED_PRESENTATION_SHORT_TYPE) {
			outputColorString = colorChangedLabelWrapper
					.getShortHashedColorString();
		} else {
			outputColorString = "";
			System.out.println("No color on output");
		}
		if (tabFolder != null && tabFolder.getSelection() != null
				&& tabFolder.getSelection().length > 0
				&& tabFolder.getSelection()[0] == customColorTabItem) {

			ColorDescriptor[] customColors = ColorPickerComposite.CustomColorBoxesComposite
					.getCustomColors();

			System.arraycopy(customColors, 0, customColors, 1,
					customColors.length - 1);

			customColors[0] = new ColorDescriptor(colorChangedLabelWrapper
					.getColorDescriptor().getRGB(), "");

			// Fix for the issue where the a color is used multiple times, adds
			// to the custom colors even if the
			// color is same.
			for (int i = 1; i < customColors.length; i++) {
				if (customColors[i].getRGB().equals(
						colorChangedLabelWrapper.getColorDescriptor().getRGB())) {
					System.arraycopy(customColors, 1, customColors, 0,
							customColors.length - 2);
				}
			}

			ColorPickerComposite.CustomColorBoxesComposite
					.updateAndStoreCustomColors(customColors);

		}

		this.colorChangingString = outputColorString;
		super.okPressed();
	}

	public void setRGBString(String rgbString) {
		this.colorChangingString = rgbString;

	}

	public String getRGBString() {
		return this.colorChangingString;

	}

	@Override
	protected void cancelPressed() {
		String outputColorString = "";
		this.colorChangingString = outputColorString;
		super.cancelPressed();
	}

	public void okCloseDialog() {
		okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}

	public RGB getRGB() {
		return this.colorChangedLabelWrapper.getColorDescriptor().getRGB();
	}

}
