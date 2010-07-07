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

import java.awt.Color;
import java.util.Map;

import org.eclipse.gef.commands.ForwardUndoCompoundCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.media.color.ValuedColors;
import com.nokia.tools.platform.theme.BitmapProperties;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.propertysheet.color.ColorChangedLabelWrapper;
import com.nokia.tools.screen.ui.propertysheet.color.ColoredButtonUtility;
import com.nokia.tools.screen.ui.propertysheet.color.CssColorDialog;
import com.nokia.tools.screen.ui.propertysheet.tabbed.MultipleSelectionWidgetSection;
import com.nokia.tools.ui.color.ColorDescriptor;

public class ColorizeSection extends MultipleSelectionWidgetSection {

	public static final int COLOR_IMG_SIZE = 12;

	protected Label colorizeLabel;

	protected Button colorize;

	protected Label colorLabel;

	protected CCombo colorText;

	protected Button colorButton;

	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);

		Composite composite = getWidgetFactory()
				.createFlatFormComposite(parent);

		Group group = getWidgetFactory().createGroup(composite,
				Messages.Label_Section_Colorize);
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

		colorizeLabel = getWidgetFactory().createLabel(composite,
				Messages.Label_Colorize);
		data = new FormData();
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		data.left = new FormAttachment(0);
		colorizeLabel.setLayoutData(data);

		colorize = getWidgetFactory().createButton(composite, null, SWT.CHECK);
		colorize.addListener(SWT.Selection, this);
		colorize.addListener(SWT.Modify, this);
		data = new FormData();
		data.top = new FormAttachment(colorizeLabel, 0, SWT.CENTER);
		data.left = new FormAttachment(0, ITabbedPropertyConstants.HSPACE
				+ leftCoordinate);
		colorize.setLayoutData(data);

		// color
		colorLabel = getWidgetFactory().createLabel(composite,
				Messages.Label_Color);
		data = new FormData();
		data.top = new FormAttachment(colorize, 0, SWT.CENTER);
		data.left = new FormAttachment(colorize,
				ITabbedPropertyConstants.HSPACE);
		colorLabel.setLayoutData(data);

		colorButton = getWidgetFactory().createButton(composite, "", SWT.NONE); //$NON-NLS-1$
		ColoredButtonUtility.setButtonColor(colorButton, COLOR_IMG_SIZE,
				COLOR_IMG_SIZE, new RGB(255, 255, 255));
		colorButton.addListener(SWT.Selection, this);
		colorButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = ((Control) e.widget).getShell();
				CssColorDialog dialog = new CssColorDialog(shell);
				RGB rgb = getRGB();
				if (rgb != null) {
					dialog.setRGBString(ColorDescriptor.asHashString(rgb));
					dialog.open();
				}
				rgb = dialog.getRGB();
				if (rgb == null) {
					return;
				}
				Color color = new Color(rgb.red, rgb.green, rgb.blue);
				ForwardUndoCompoundCommand command = new ForwardUndoCompoundCommand(
						"Colorize");
				for (IContentData data : getContents()) {
					ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) data
							.getAdapter(ISkinnableEntityAdapter.class);
					BitmapProperties bitmap = new BitmapProperties(adapter
							.getAttributes());
					bitmap.setColorize(true);
					bitmap.setColor(color);

					command
							.add(adapter
									.getApplyBitmapPropertiesCommand(bitmap));
				}
				execute(command);

				ColoredButtonUtility.setButtonColor(colorButton,
						COLOR_IMG_SIZE, COLOR_IMG_SIZE, rgb);

				ColorChangedLabelWrapper colorLabelWrapper = new ColorChangedLabelWrapper();
				colorLabelWrapper.setColorString("rgb(" + rgb.red + "," //$NON-NLS-1$ //$NON-NLS-2$
						+ rgb.green + "," + rgb.blue + ")"); //$NON-NLS-1$ //$NON-NLS-2$

				colorText.setText(colorLabelWrapper.getHashedColorString());
			}

			private RGB getRGB() {
				String col = colorText.getText();
				if (ValuedColors.isCssColorName(col))
					col = ValuedColors.getNamedColorValue(col);

				ColorChangedLabelWrapper colorLabelWrapper = new ColorChangedLabelWrapper();
				colorLabelWrapper.setColorString(col);
				RGB rgb = colorLabelWrapper.getColorDescriptor().getRGB();

				return rgb;
			}

		});
		data = new FormData();
		data.top = new FormAttachment(colorize, 0, SWT.CENTER);
		data.left = new FormAttachment(colorize,
				ITabbedPropertyConstants.HSPACE * 2 + rightCoordinate);
		colorButton.setLayoutData(data);

		colorText = getWidgetFactory().createCCombo(composite, SWT.FLAT);
		colorText.setItems(ValuedColors.BASIC_CSS_COLOR_NAMES);
		colorText.addListener(SWT.FocusOut, this);
		colorText.addListener(SWT.Selection, this);
		colorText.addListener(SWT.Modify, this);
		colorText.addKeyListener(enterAdapter);
		// inputBackgroundColorText.setToolTipText(Messages.Hint_Color);
		data = new FormData();
		data.top = new FormAttachment(colorButton, 0, SWT.CENTER);
		data.left = new FormAttachment(colorButton,
				ITabbedPropertyConstants.HSPACE);
		data.width = 100; 
		colorText.setLayoutData(data);
		getWidgetFactory().paintBordersFor(composite);

		syncWithOther(group, BitmapPropertiesSection.class.getName());
		parent.getParent().pack();
	}

	@Override
	protected void doHandleEvent(Event e) {
		if (e.type == SWT.Selection) {
			if (e.widget == colorize) {
				ForwardUndoCompoundCommand command = new ForwardUndoCompoundCommand(
						"Colorize");
				for (IContentData data : getContents()) {
					ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) data
							.getAdapter(ISkinnableEntityAdapter.class);
					BitmapProperties bitmap = new BitmapProperties(adapter
							.getAttributes());
					bitmap.setColorize(colorize.getSelection());
					command
							.add(adapter
									.getApplyBitmapPropertiesCommand(bitmap));
				}
				execute(command);
				updateControlsEnablementState();
			}
		}
		if (e.type == SWT.FocusOut || e.type == SWT.Selection) {
			if (e.widget == colorText) {
				String col = colorText.getText();
				if (ValuedColors.isCssColorName(col))
					col = ValuedColors.getNamedColorValue(col);

				if (ColorChangedLabelWrapper.isSupportedColor(col)) {
					ColorChangedLabelWrapper colorLabelWrapper = new ColorChangedLabelWrapper();
					colorLabelWrapper.setColorString(col);
					RGB rgb = colorLabelWrapper.getColorDescriptor().getRGB();

					Color color = new Color(rgb.red, rgb.green, rgb.blue);
					Color oldColor = (Color) EditingUtil.getFeatureValue(
							getFirstTarget(), "color"); //$NON-NLS-1$

					if (!color.equals(oldColor)) {
						ForwardUndoCompoundCommand command = new ForwardUndoCompoundCommand(
								"Colorize");
						for (IContentData data : getContents()) {
							ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) data
									.getAdapter(ISkinnableEntityAdapter.class);
							BitmapProperties bitmap = new BitmapProperties(
									adapter.getAttributes());
							bitmap.setColorize(true);
							bitmap.setColor(color);
							command.add(adapter
									.getApplyBitmapPropertiesCommand(bitmap));
						}
						execute(command);
						ColoredButtonUtility.setButtonColor(colorButton,
								COLOR_IMG_SIZE, COLOR_IMG_SIZE, rgb);

						colorText.setText(colorLabelWrapper
								.getHashedColorString());

						updateControlsEnablementState();
					}
				} else {
					/*
					 * If some selection (target) is there then only update the
					 * section.
					 */
					Color oldColor = (Color) EditingUtil.getFeatureValue(
							getFirstTarget(), "color");
					if (getFirstTarget() != null && oldColor != null) {
						RGB rgb = new RGB(oldColor.getRed(), oldColor
								.getGreen(), oldColor.getBlue());
						String hashedOldColor = ColorDescriptor
								.asHashString(rgb);
						colorText.setText(hashedOldColor);
						updateControlsEnablementState();
					}
				}

			}
		}
	}

	@Override
	protected void doRefresh() {
		if (getFirstContent() == null) {
			return;
		}
		ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) getFirstContent()
				.getAdapter(ISkinnableEntityAdapter.class);
		Map attributes = adapter.getAttributes();
		Boolean selected = (Boolean) attributes
				.get(BitmapProperties.COLORIZE_SELECTED);
		Color color = (Color) attributes.get(BitmapProperties.COLOR);
		colorize.setSelection(selected != null && selected);

		if (color != null) {
			ColorChangedLabelWrapper colorLabelWrapper = new ColorChangedLabelWrapper();
			colorLabelWrapper.setColorString("rgb(" + color.getRed() + "," //$NON-NLS-1$ //$NON-NLS-2$
					+ color.getGreen() + "," + color.getBlue() + ")"); //$NON-NLS-1$ //$NON-NLS-2$

			ColoredButtonUtility.setButtonColor(colorButton, COLOR_IMG_SIZE,
					COLOR_IMG_SIZE, colorLabelWrapper.getColorDescriptor()
							.getRGB());

			colorText.setText(colorLabelWrapper.getHashedColorString());

		} else {
			ColoredButtonUtility.setButtonColor(colorButton, COLOR_IMG_SIZE,
					COLOR_IMG_SIZE, new RGB(255, 255, 255));

			colorText.setText(""); //$NON-NLS-1$
		}

		updateControlsEnablementState();
	}

	protected void updateControlsEnablementState() {
		colorLabel.setEnabled(colorize.getSelection());
		colorText.setEnabled(colorize.getSelection());
		colorButton.setEnabled(colorize.getSelection());
	}
}
