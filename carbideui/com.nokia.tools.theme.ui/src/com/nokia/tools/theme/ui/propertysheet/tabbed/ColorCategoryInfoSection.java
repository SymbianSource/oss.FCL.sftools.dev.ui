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
/**
 * 
 */
package com.nokia.tools.theme.ui.propertysheet.tabbed;

import java.awt.Color;

import org.eclipse.gef.commands.ForwardUndoCompoundCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.color.ValuedColors;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.tooltip.DynamicTooltip;
import com.nokia.tools.screen.ui.propertysheet.color.ColorChangedLabelWrapper;
import com.nokia.tools.screen.ui.propertysheet.color.ColorPickerComposite;
import com.nokia.tools.screen.ui.propertysheet.color.ColoredButtonUtility;
import com.nokia.tools.screen.ui.propertysheet.color.CssColorDialog;
import com.nokia.tools.screen.ui.propertysheet.color.IColorPickerListener;
import com.nokia.tools.screen.ui.propertysheet.tabbed.MultipleSelectionWidgetSection;
import com.nokia.tools.ui.color.ColorDescriptor;
import com.nokia.tools.ui.tooltip.CompositeInformationControl;
import com.nokia.tools.ui.tooltip.CompositeTooltip;

/**
 * Color category General property tab implementation
 * 
 */
public class ColorCategoryInfoSection extends MultipleSelectionWidgetSection {

	private CCombo inputBackgroundColorText;

	private Button backgroundColorButton;

	private String cachedColor;

	private CompositeTooltip createTooltip(Control control) {
		if (!DynamicTooltip.IS_ENABLED) {
			return null;
		}

		final CompositeTooltip tooltip = new CompositeTooltip() {
			@Override
			protected CompositeInformationControl createUnfocusedControl() {
				CompositeInformationControl control = super
						.createUnfocusedControl();
				Composite parent = control.getComposite();

				parent.setLayout(new GridLayout());

				final ColorChangedLabelWrapper colorChangedLabelWrapper = new ColorChangedLabelWrapper();
				colorChangedLabelWrapper.setColorString(cachedColor);

				IColorPickerListener colorPickerListener = new IColorPickerListener() {
					public void selectionChanged() {
						okCloseDialog();
					}

					public void okCloseDialog() {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								hide(true);
							}
						});

						RGB newColor = colorChangedLabelWrapper
								.getColorDescriptor().getRGB();

						if (newColor != null) {
							inputBackgroundColorText.setText(ColorUtil
									.asHashString(newColor));
							applyColor(ColorUtil.asHashString(newColor));
						}
					}
				};

				Composite cPickerComposite = new ColorPickerComposite(parent,
						SWT.NONE, colorChangedLabelWrapper, colorPickerListener);

				parent.setBackground(cPickerComposite.getBackground());

				cPickerComposite.setLayoutData(new GridData(SWT.CENTER,
						SWT.NONE, true, false));

				return control;
			}

			@Override
			protected CompositeInformationControl createFocusedControl() {
				return super.createUnfocusedControl();
			}
		};

		return tooltip;
	}

	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);

		Composite composite = getWidgetFactory()
				.createFlatFormComposite(parent);

		int leftCoordinate = getStandardLabelWidth(composite,
				new String[] { Messages.Label_Color });

		// color
		CLabel colorLabel = getWidgetFactory().createCLabel(composite,
				Messages.Label_Color);

		backgroundColorButton = getWidgetFactory().createButton(composite, "",
				SWT.NONE);
		// backgroundColorButton.setToolTipText(Messages.SetColorCategoryInfo);
		FormData data = new FormData(22, 22);
		data.left = new FormAttachment(0, leftCoordinate);
		backgroundColorButton.setLayoutData(data);

		inputBackgroundColorText = getWidgetFactory().createCCombo(composite,
				SWT.FLAT);
		inputBackgroundColorText.addListener(SWT.FocusOut, this);
		inputBackgroundColorText.addListener(SWT.Modify, this);
		inputBackgroundColorText.addKeyListener(enterAdapter);
		inputBackgroundColorText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Event evt = new Event();
				evt.type = SWT.FocusOut;
				evt.widget = e.widget;
				inputBackgroundColorText
						.setText(ValuedColors
								.getNamedColorValue(inputBackgroundColorText
										.getText()));
				doHandleEvent(evt);
			}
		});
		inputBackgroundColorText.setItems(ValuedColors.BASIC_CSS_COLOR_NAMES);

		data = new FormData();
		data.left = new FormAttachment(backgroundColorButton, 1);
		data.top = new FormAttachment(backgroundColorButton, 0, SWT.CENTER);
		data.width = getStandardLabelWidth(composite,
				new String[] { "#FFFFFF" });

		inputBackgroundColorText.setLayoutData(data);
		inputBackgroundColorText.setToolTipText(Messages.SetColorCategoryInfo);

		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(backgroundColorButton,
				-ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(backgroundColorButton, 0, SWT.CENTER);
		colorLabel.setLayoutData(data);

		// backgroundColorButton.setImage(fontColorImage);
		// backgroundColorButton.addListener(SWT.Selection, this);
		backgroundColorButton.setToolTipText("");

		CompositeTooltip tooltip = createTooltip(backgroundColorButton);
		if (tooltip != null) {
			tooltip.setControl(backgroundColorButton);
		}

		backgroundColorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String text = openDialogBox(inputBackgroundColorText
						.getParent());
				if (text.length() > 0) {
					inputBackgroundColorText.setText(text);
					applyColor(text);
				}
			}

			private String openDialogBox(Control cellEditorWindow) {
				CssColorDialog dialog = new CssColorDialog(cellEditorWindow
						.getShell());

				String inputText = inputBackgroundColorText.getText();

				if (inputText == null || "".equals(inputText)) {
					inputText = "#000000";
				}

				dialog.setRGBString(inputText);
				dialog.open();

				return dialog.getRGBString();
			}

		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.propertysheet.tabbed.WidgetSection#doHandleEvent(org.eclipse.swt.widgets.Event)
	 */
	@Override
	protected void doHandleEvent(Event e) {
		if (SWT.FocusOut == e.type && e.widget == inputBackgroundColorText) {
			applyColor(inputBackgroundColorText.getText());
		}
	}

	protected void applyColor(String colorText) {
		if (ColorDescriptor.isColor(colorText)) {
			cachedColor = colorText;
			RGB rgb = ColorDescriptor.getRGB(colorText);
			if (backgroundColorButton.getImage() != null)
				addResourceToDispose(backgroundColorButton.getImage());
			ColoredButtonUtility.setButtonColor(backgroundColorButton, 12, 12,
					rgb);
			ForwardUndoCompoundCommand command = new ForwardUndoCompoundCommand(
					"Set color");
			Color color = ColorUtil.toColor(rgb);
			for (IContentData data : getContents()) {
				IColorAdapter adapter = (IColorAdapter) data
						.getAdapter(IColorAdapter.class);
				if (adapter != null) {
					command.add(adapter.getApplyColorCommand(color, true));
				}
			}
			execute(command);
		} else {
			inputBackgroundColorText.setText(cachedColor);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.propertysheet.tabbed.WidgetSection#doRefresh()
	 */
	@Override
	protected void doRefresh() {
		if (getFirstContent() != null) {
			IColorAdapter adapter = (IColorAdapter) getFirstContent()
					.getAdapter(IColorAdapter.class);
			if (adapter != null) {
				RGB color = ColorUtil.toRGB(adapter.getColor());
				ColoredButtonUtility.setButtonColor(backgroundColorButton, 12,
						12, color);
				inputBackgroundColorText.setText(ColorDescriptor
						.asHashString(color));
				cachedColor = inputBackgroundColorText.getText();
				return;
			}
		}
		inputBackgroundColorText.setText("");
	}
}
