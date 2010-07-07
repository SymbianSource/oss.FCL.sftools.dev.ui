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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.nokia.tools.media.color.NamedColors;
import com.nokia.tools.media.color.ValuedColors;
import com.nokia.tools.ui.color.ColorBox;
import com.nokia.tools.ui.color.ColorDescriptor;

public class SystemColorComposite extends Composite {
	static Color[] systemColors = new Color[28];
	static {
		systemColors[0] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.ACTIVE_BORDER_WINDOWS));
		systemColors[1] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.ACTIVE_CAPTION_WINDOWS));
		systemColors[2] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.APP_WORKSPACE_WINDOWS));
		systemColors[3] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.BACKGROUND_WINDOWS));
		systemColors[4] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.BUTTON_FACE_WINDOWS));
		systemColors[5] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.BUTTON_HIGHLIGHT_WINDOWS));
		systemColors[6] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.BUTTON_SHADOW_WINDOWS));
		systemColors[7] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.BUTTON_TEXT_WINDOWS));
		systemColors[8] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.CAPTION_TEXT_WINDOWS));
		systemColors[9] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.GRAY_TEXT_WINDOWS));
		systemColors[10] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.HIGHLIGHT_TEXT_WINDOWS));
		systemColors[11] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.HIGHLIGHT_WINDOWS));
		systemColors[12] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.INACTIVE_BORDER_WINDOWS));
		systemColors[13] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.INACTIVE_CAPTION_TEXT_WINDOWS));
		systemColors[14] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.INACTIVE_CAPTION_WINDOWS));
		systemColors[15] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.INFO_BACKGROUND_WINDOWS));
		systemColors[16] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.INFO_TEXT_WINDOWS));
		systemColors[17] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MENU_TEXT_WINDOWS));
		systemColors[18] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MENU_WINDOWS));
		systemColors[19] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.SCROLLBAR_WINDOWS));
		systemColors[20] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.THREE_D_DARK_SHADOW_WINDOWS));
		systemColors[21] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.THREE_D_FACE_WINDOWS));
		systemColors[22] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.THREE_D_HIGHLIGHT_WINDOWS));
		systemColors[23] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.THREE_D_LIGHT_SHADOW_WINDOWS));
		systemColors[24] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.THREE_D_SHADOW_WINDOWS));
		systemColors[25] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.WINDOW_FRAME_WINDOWS));
		systemColors[26] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.WINDOW_TEXT_WINDOWS));
		systemColors[27] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.WINDOW_WINDOWS));
	}
	private ListViewer listViewer;
	// private String[] systemColorNames;
	// private String[] systemColorDescriptions;
	private ColorDescriptor[] colors;
	ColorBox currentlySelectedBox;
	int currentlySelectedIndex = 0;
	Label description;
	IColorPickerListener dialogClose;

	ColorChangedLabelWrapper colorChangedLabelWrapper;

	public SystemColorComposite(Composite parent, int style,
			IColorPickerListener dialogClose) {
		super(parent, style);
		this.dialogClose = dialogClose;
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 10;
		layout.marginWidth = 9;
		layout.makeColumnsEqualWidth = true;
		this.setLayout(layout);

		fillColors();
		listViewer = new ListViewer(this, SWT.SINGLE | SWT.BORDER
				| SWT.V_SCROLL);
		listViewer.setLabelProvider(new SystemColorListLabelProvider());
		listViewer.setContentProvider(new ArrayContentProvider());
		listViewer.setInput(this.colors);

		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				ColorDescriptor descriptor = (ColorDescriptor) selection
						.getFirstElement();
				// System.out.println(selection.getFirstElement());
				currentlySelectedBox.setColorBoxColorDescriptor(descriptor);
				currentlySelectedBox.updateBackground();
				// =new
				// ColorBox(SystemColorComposite.this,SWT.NONE,descriptor,0);
				description.setText(currentlySelectedBox
						.getColorBoxColorDescriptor().getDescription());
				currentlySelectedBox.redraw();
				updateAfterSelectionChange();
			}
		});

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalSpan = 2;
		gd.heightHint = 262;
		listViewer.getControl().setLayoutData(gd);

		currentlySelectedBox = new ColorBox(this, SWT.NONE, colors[0], 0);
		gd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		gd.heightHint = 100;
		gd.widthHint = 100;
		gd.verticalIndent = 14;
		currentlySelectedBox.setLayoutData(gd);
		currentlySelectedBox.getColorBox().addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent event) {
				SystemColorComposite.this.dialogClose.okCloseDialog();
			}
		});

		description = new Label(this, SWT.WRAP);
		gd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		gd.heightHint = 100;
		gd.widthHint = 100;
		gd.verticalIndent = 14;
		description.setText(currentlySelectedBox.getColorBoxColorDescriptor()
				.getDescription());
		description.setLayoutData(gd);

	}

	public class SystemColorListLabelProvider extends LabelProvider {
		public Image getImage(Object object) {
			return null;
		}

		public String getText(Object element) {
			return ((ColorDescriptor) element).getName();
		}
	}

	private void fillColors() {
		colors = new ColorDescriptor[28];
		colors[0] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.ACTIVE_BORDER_WINDOWS),
				NamedColors.ACTIVE_BORDER_NAME);
		colors[0].setDescription(NamedColors.ACTIVE_BORDER_DESCRIPTION);
		colors[1] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.ACTIVE_CAPTION_WINDOWS),
				NamedColors.ACTIVE_CAPTION_NAME);
		colors[1].setDescription(NamedColors.ACTIVE_CAPTION_DESCRIPTION);
		colors[2] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.APP_WORKSPACE_WINDOWS),
				NamedColors.APP_WORKSPACE_NAME);
		colors[2].setDescription(NamedColors.APP_WORKSPACE_DESCRIPTION);
		colors[3] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.BACKGROUND_WINDOWS),
				NamedColors.BACKGROUND_NAME);
		colors[3].setDescription(NamedColors.BACKGROUND_DESCRIPTION);
		colors[4] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.BUTTON_FACE_WINDOWS),
				NamedColors.BUTTON_FACE_NAME);
		colors[4].setDescription(NamedColors.BUTTON_FACE_DESCRIPTION);
		colors[5] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.BUTTON_HIGHLIGHT_WINDOWS),
				NamedColors.BUTTON_HIGHLIGHT_NAME);
		colors[5].setDescription(NamedColors.BUTTON_HIGHLIGHT_DESCRIPTION);
		colors[6] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.BUTTON_SHADOW_WINDOWS),
				NamedColors.BUTTON_SHADOW_NAME);
		colors[6].setDescription(NamedColors.BUTTON_SHADOW_DESCRIPTION);
		colors[7] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.BUTTON_TEXT_WINDOWS),
				NamedColors.BUTTON_TEXT_NAME);
		colors[7].setDescription(NamedColors.BUTTON_TEXT_DESCRIPTION);
		colors[8] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.CAPTION_TEXT_WINDOWS),
				NamedColors.CAPTION_TEXT_NAME);
		colors[8].setDescription(NamedColors.CAPTION_TEXT_DESCRIPTION);
		colors[9] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.GRAY_TEXT_WINDOWS),
				NamedColors.GRAY_TEXT_NAME);
		colors[9].setDescription(NamedColors.GRAY_TEXT_DESCRIPTION);
		colors[10] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.HIGHLIGHT_TEXT_WINDOWS),
				NamedColors.HIGHLIGHT_TEXT_NAME);
		colors[10].setDescription(NamedColors.HIGHLIGHT_TEXT_DESCRIPTION);
		colors[11] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.HIGHLIGHT_WINDOWS),
				NamedColors.HIGHLIGHT_NAME);
		colors[11].setDescription(NamedColors.HIGHLIGHT_DESCRIPTION);
		colors[12] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.INACTIVE_BORDER_WINDOWS),
				NamedColors.INACTIVE_BORDER_NAME);
		colors[12].setDescription(NamedColors.INACTIVE_BORDER_DESCRIPTION);
		colors[13] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.INACTIVE_CAPTION_WINDOWS),
				NamedColors.INACTIVE_CAPTION_NAME);
		colors[13].setDescription(NamedColors.INACTIVE_CAPTION_DESCRIPTION);
		colors[14] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.INACTIVE_CAPTION_TEXT_WINDOWS),
				NamedColors.INACTIVE_CAPTION_TEXT_NAME);
		colors[14]
				.setDescription(NamedColors.INACTIVE_CAPTION_TEXT_DESCRIPTION);
		colors[15] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.INFO_BACKGROUND_WINDOWS),
				NamedColors.INFO_BACKGROUND_NAME);
		colors[15].setDescription(NamedColors.INFO_BACKGROUND_DESCRIPTION);
		colors[16] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.INFO_TEXT_WINDOWS),
				NamedColors.INFO_TEXT_NAME);
		colors[16].setDescription(NamedColors.INFO_TEXT_DESCRIPTION);
		colors[17] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.MENU_TEXT_WINDOWS),
				NamedColors.MENU_TEXT_NAME);
		colors[17].setDescription(NamedColors.MENU_TEXT_DESCRIPTION);
		colors[18] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.MENU_WINDOWS), NamedColors.MENU_NAME);
		colors[18].setDescription(NamedColors.MENU_DESCRIPTION);
		colors[19] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.SCROLLBAR_WINDOWS),
				NamedColors.SCROLLBAR_NAME);
		colors[19].setDescription(NamedColors.SCROLLBAR_DESCRIPTION);
		colors[20] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.THREE_D_DARK_SHADOW_WINDOWS),
				NamedColors.THREE_D_DARK_SHADOW_NAME);
		colors[20].setDescription(NamedColors.THREE_D_DARK_SHADOW_DESCRIPTION);
		colors[21] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.THREE_D_FACE_WINDOWS),
				NamedColors.THREE_D_FACE_NAME);
		colors[21].setDescription(NamedColors.THREE_D_FACE_DESCRIPTION);
		colors[22] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.THREE_D_HIGHLIGHT_WINDOWS),
				NamedColors.THREE_D_HIGHLIGHT_NAME);
		colors[22].setDescription(NamedColors.THREE_D_HIGHLIGHT_DESCRIPTION);
		colors[23] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.THREE_D_LIGHT_SHADOW_WINDOWS),
				NamedColors.THREE_D_LIGHT_SHADOW_NAME);
		colors[23].setDescription(NamedColors.THREE_D_LIGHT_SHADOW_DESCRIPTION);
		colors[24] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.THREE_D_SHADOW_WINDOWS),
				NamedColors.THREE_D_SHADOW_NAME);
		colors[24].setDescription(NamedColors.THREE_D_SHADOW_DESCRIPTION);
		colors[25] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.WINDOW_FRAME_WINDOWS),
				NamedColors.WINDOW_FRAME_NAME);
		colors[25].setDescription(NamedColors.WINDOW_FRAME_DESCRIPTION);
		colors[26] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.WINDOW_TEXT_WINDOWS),
				NamedColors.WINDOW_TEXT_NAME);
		colors[26].setDescription(NamedColors.WINDOW_TEXT_DESCRIPTION);
		colors[27] = new ColorDescriptor(ColorDescriptor
				.getRGB(ValuedColors.WINDOW_WINDOWS), NamedColors.WINDOW_NAME);
		colors[27].setDescription(NamedColors.WINDOW_DESCRIPTION);

	}

	private void updateAfterSelectionChange() {

		if (colorChangedLabelWrapper != null) {
			ColorDescriptor descriptor = this.currentlySelectedBox
					.getColorBoxColorDescriptor();

			colorChangedLabelWrapper.getColorDescriptor().setRed(
					descriptor.getRed());
			colorChangedLabelWrapper.getColorDescriptor().setGreen(
					descriptor.getGreen());
			colorChangedLabelWrapper.getColorDescriptor().setBlue(
					descriptor.getBlue());

			colorChangedLabelWrapper.getColorDescriptor().setName(
					descriptor.getName());

			colorChangedLabelWrapper.getColorChangedLabel().setText(
					this.getProperPresentationString());

		}
	}

	private String getProperPresentationString() {
		String colorString;
		if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.HASHED_PRESENTATION_TYPE) {
			colorString = colorChangedLabelWrapper
					.getColorNameColorStringWithHash();
		} else if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.RGB_FUNCTION_PRESENTATION_TYPE) {
			colorString = colorChangedLabelWrapper
					.getColorNameColorStringWithFunction();
		} else if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.RGB_FUNCTION_PERCENTAGE_PRESENTATION_TYPE) {
			colorString = colorChangedLabelWrapper
					.getColorNameColorStringWithFunctionAndPercentage();
		} else if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.HASHED_PRESENTATION_SHORT_TYPE) {
			colorString = colorChangedLabelWrapper
					.getShortHashedColorWithApproximatedColorName();
		} else {
			colorString = colorChangedLabelWrapper
					.getColorNameColorStringWithHash();
		}
		return colorString;
	}

	public void setColorChangedLabel(ColorChangedLabelWrapper colorChangedLabel) {

		this.colorChangedLabelWrapper = colorChangedLabel;

	}

}
