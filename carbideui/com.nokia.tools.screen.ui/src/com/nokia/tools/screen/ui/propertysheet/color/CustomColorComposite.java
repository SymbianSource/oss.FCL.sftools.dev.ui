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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;

import com.nokia.tools.ui.color.ColorBox;
import com.nokia.tools.ui.color.ColorDescriptor;
import com.nokia.tools.ui.color.TooltipMessages;

public class CustomColorComposite
    extends Composite {

	private static final int MIN_VALUE = 0;

	private static final int MAX_VALUE = 255;

	ColorBox colorBox;

	ColorChangedLabelWrapper colorChangedLabelWrapper;

	Slider redSlider;

	Slider greenSlider;

	Slider blueSlider;
	
	Text hexColor;

	Text redText;

	Text greenText;

	Text blueText;

	IColorPickerListener dialogClose;

	protected Composite selectedColorBox = null;

	public CustomColorComposite(Composite parent, int style,
	    IColorPickerListener dialogClose) {
		super(parent, style);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 9;
		layout.marginHeight = 9;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;

		this.dialogClose = dialogClose;
		this.setLayout(layout);
		
		createHexColorComposite();

		Control[] controls = new Control[3];

		controls = createSlider(Messages.CustomColorComposite_Label_Red);
		redSlider = (Slider) controls[1];
		redText = (Text) controls[2];

		controls = createSlider(Messages.CustomColorComposite_Label_Green);
		greenSlider = (Slider) controls[1];
		greenText = (Text) controls[2];

		controls = createSlider(Messages.CustomColorComposite_Label_Blue);
		blueSlider = (Slider) controls[1];
		blueText = (Text) controls[2];
		createPreview();

		customColors = ColorPickerComposite.CustomColorBoxesComposite
		    .getCustomColors();
		Composite customGroupComposite = new Composite(this, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		customGroupComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 16;
		customGroupComposite.setLayout(layout);

		Label customColorLabel = new Label(customGroupComposite, SWT.NONE);
		customColorLabel
		    .setText(TooltipMessages.ColorPickerTooltip_Label_Custom);
		gd = new GridData();
		gd.horizontalSpan = 16;
		gd.verticalIndent = 14;
		customColorLabel.setLayoutData(gd);

		fillCustomColorsAndBorders(customGroupComposite);
	}

	ColorDescriptor[] customColors = new ColorDescriptor[15];

	protected ColorBox[] customColorBoxes;

	protected int colorBoxSize = 16;

	protected void fillCustomColorsAndBorders(Composite parent) {
		customColorBoxes = new ColorBox[customColors.length];

		for (int i = 0; i < customColors.length; i++) {

			customColorBoxes[i] = new ColorBox(parent, SWT.NONE,
			    customColors[i], i);

			GridData gd = new GridData();

			gd.heightHint = colorBoxSize;
			gd.widthHint = colorBoxSize;
			customColorBoxes[i].setLayoutData(gd);

			customColorBoxes[i].addMouseListener(new MouseAdapter() {

				@Override
				public void mouseDown(MouseEvent e) {
					Composite colorBox = (Composite) e.widget;
					clearSelection(colorBox);
					selectedColorBox = colorBox;
					updateAfterSelectionChange();
				}

				@Override
				public void mouseDoubleClick(MouseEvent e) {
					Composite colorBox = (Composite) e.widget;
					clearSelection(colorBox);
					selectedColorBox = (ColorBox) colorBox.getParent();
					updateAfterSelectionChange();
					// CustomColorBoxesComposite.this.dialogClose
					// .okCloseDialog();
				}

			});

			customColorBoxes[i].getColorBox().addMouseListener(
			    new MouseAdapter() {

				    @Override
				    public void mouseDown(MouseEvent e) {
					    Composite colorBox = (Composite) e.widget;
					    clearSelection(colorBox);
					    selectedColorBox = (ColorBox) colorBox.getParent();
					    updateAfterSelectionChange();
				    }

				    @Override
				    public void mouseDoubleClick(MouseEvent e) {
					    Composite colorBox = (Composite) e.widget;
					    clearSelection(colorBox);
					    selectedColorBox = (ColorBox) colorBox.getParent();
					    updateAfterSelectionChange();
					    // CustomColorBoxesComposite.this.dialogClose
					    // .okCloseDialog();
				    }

			    });

			customColorBoxes[i].addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent e) {

					if ((e.keyCode != SWT.ARROW_UP)
					    && (e.keyCode != SWT.ARROW_DOWN)
					    && (e.keyCode != SWT.ARROW_LEFT)
					    && (e.keyCode != SWT.ARROW_RIGHT)) {
						return;
					}
					int index = ((ColorBox) selectedColorBox)
					    .getColorPosition();

					if (e.keyCode == SWT.ARROW_UP) {
					} else if (e.keyCode == SWT.ARROW_DOWN) {
					} else if (e.keyCode == SWT.ARROW_LEFT) {
						if ((index % 16) > 0) {
							index = index - 1;
						}
					} else if (e.keyCode == SWT.ARROW_RIGHT) {
						if (index + 1 < 16) {
							index = index + 1;
						}

					} else {
						return;
					}

					clearSelection(customColorBoxes[index]);
					selectedColorBox = customColorBoxes[index];

					updateAfterSelectionChange();

				}

			});

		}

	}

	private void clearSelection(Composite newColorBox) {
		if (selectedColorBox == newColorBox) {
			// do nothing the same selection
		} else {
			if (selectedColorBox != null) {
				selectedColorBox.setBackground(((ColorBox) selectedColorBox)
				    .getNotSelectedColor());
				selectedColorBox = null;
			}
		}
	}

	private void updateAfterSelectionChange() {

		// select the item, synchronize the sliders and preview
		selectedColorBox.forceFocus();
		selectedColorBox.setBackground(((ColorBox) selectedColorBox)
		    .getBorderSelectionColor());
		RGB selectedColorBoxRGB = ((ColorBox) selectedColorBox)
		    .getColorBoxColorDescriptor().getRGB();

		redText.setText(Integer.toString(selectedColorBoxRGB.red));
		greenText.setText(Integer.toString(selectedColorBoxRGB.green));
		blueText.setText(Integer.toString(selectedColorBoxRGB.blue));
		updatePreview();

		if (colorChangedLabelWrapper != null) {
			colorChangedLabelWrapper.getColorDescriptor().setRed(
			    selectedColorBoxRGB.red);
			colorChangedLabelWrapper.getColorDescriptor().setGreen(
			    selectedColorBoxRGB.green);
			colorChangedLabelWrapper.getColorDescriptor().setBlue(
			    selectedColorBoxRGB.blue);

			if (colorChangedLabelWrapper.getColorChangedLabel() != null) {
				colorChangedLabelWrapper.getColorChangedLabel().setText(
				    this.getProperPresentationString());
			}
		}
		dialogClose.selectionChanged();
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

	private void createPreview() {
		GridData gd = new GridData();
		Label label = new Label(this, SWT.NONE);
		label.setText(Messages.CustomColorComposite_Label_Preview);
		gd.verticalIndent = 8;
		gd.verticalAlignment = SWT.TOP;
		label.setLayoutData(gd);

		colorBox = new ColorBox(this, SWT.NONE, new ColorDescriptor(10, 10, 10,
		    null), 0);
		colorBox.getColorBox().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent event) {
				CustomColorComposite.this.dialogClose.okCloseDialog();
			}
		});

		gd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		gd.heightHint = 100;
		gd.widthHint = 100;
		gd.verticalIndent = 55;
		colorBox.setLayoutData(gd);
	}
	
	private void createHexColorComposite() {
		Label label = new Label(this, SWT.NONE);
		label.setText(Messages.CustomColorComposite_Label_Hex);

		Composite hashPanel = new Composite(this, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		hashPanel.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		hashPanel.setLayoutData(gd);

		hexColor = new Text(hashPanel, SWT.BORDER);
		hexColor.setTextLimit(7);

		hexColor.setLayoutData(new GridData(hexColor.computeSize(computeTextWidth(hexColor,
		    7), SWT.DEFAULT).x, SWT.DEFAULT));
		
		hexColor.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String newSlidersValue = hexColor.getText();
				
				if(newSlidersValue.length() != 7 || !newSlidersValue.startsWith("#")) {
					return;
				}
				
				try {
					int newRedSliderIntValue = Integer.parseInt(newSlidersValue.substring(1, 3), 16);
					int newGreenSliderIntValue = Integer.parseInt(newSlidersValue.substring(3, 5), 16);
					int newBlueSliderIntValue = Integer.parseInt(newSlidersValue.substring(5, 7), 16);
					
					handleRangeControl(Messages.CustomColorComposite_Label_Red, redText, redSlider, newRedSliderIntValue);
					handleRangeControl(Messages.CustomColorComposite_Label_Green, greenText, greenSlider, newGreenSliderIntValue);
					handleRangeControl(Messages.CustomColorComposite_Label_Blue, blueText, blueSlider, newBlueSliderIntValue);
					
				} catch (Exception ex) {
					hexColor.setText("");
				}
			}
		});
	}
	
	private void handleRangeControl(String labelText, Text text, Slider slider, Integer value) {
		if (value < MIN_VALUE) {
			text.setText(Integer.toString(MIN_VALUE));
		}
		if (value > MAX_VALUE) {
			text.setText(Integer.toString(MAX_VALUE));
		}
		if (value != slider.getSelection()) {
			text.setText(value.toString());
			slider.setSelection(value + Math.abs(MIN_VALUE));
			updatePreview(labelText, value);
		}
	}
	
	public void fillHexColor() {
		String red = Integer.toHexString(redSlider.getSelection());
		String green = Integer.toHexString(greenSlider.getSelection());
		String blue = Integer.toHexString(blueSlider.getSelection());
		
		if(red.length() == 1) {
			red = "0" + red;
		}
		if(green.length() == 1) {
			green = "0" + green;
		}
		if(blue.length() == 1) {
			blue = "0" + blue;
		}
		
		hexColor.setText( "#" + red	+ green + blue );
	}

	private Control[] createSlider(final String labelText) {
		Label label = new Label(this, SWT.NONE);
		label.setText(labelText);

		Composite sliderPanel = new Composite(this, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		sliderPanel.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		sliderPanel.setLayoutData(gd);

		final Slider slider = new Slider(sliderPanel, SWT.HORIZONTAL);
		slider.setLayoutData(gd);
		final Text text = new Text(sliderPanel, SWT.BORDER);
		text.setTextLimit(3);

		int range = MAX_VALUE - MIN_VALUE;

		slider.setMinimum(0);
		slider.setMaximum(range + slider.getThumb());
		// slider.setLayoutData(new GridData(Math.min(128, range / 2),
		// SWT.DEFAULT));

		text.setLayoutData(new GridData(text.computeSize(computeTextWidth(text,
		    3), SWT.DEFAULT).x, SWT.DEFAULT));

		slider.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int newPropertyValue = slider.getSelection()
				    - Math.abs(MIN_VALUE);

				String newSliderValue = new Integer(newPropertyValue)
				    .toString();

				if (!newSliderValue.equals(text.getText())) {
					text.setText(newSliderValue);
					updatePreview(labelText, newPropertyValue);
					fillHexColor();
				}
			}

		});

		text.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String newSliderValue = text.getText();
				try {
					int newSliderIntValue = Integer.parseInt(newSliderValue);

					// range control
					if (newSliderIntValue < MIN_VALUE) {
						text.setText(Integer.toString(MIN_VALUE));
					}
					if (newSliderIntValue > MAX_VALUE) {
						text.setText(Integer.toString(MAX_VALUE));
					}

					if (newSliderIntValue != slider.getSelection()) {
						slider.setSelection(newSliderIntValue
						    + Math.abs(MIN_VALUE));
						updatePreview(labelText, newSliderIntValue);
						fillHexColor();
					}
				} catch (Exception ex) {
					text.setText("0");
				}
			}
		});

		return new Control[] { label, slider, text };
	}

	public void updatePreview(String labelText, int value) {
		if (Messages.CustomColorComposite_Label_Red.equals(labelText)) {
			colorBox.getColorBoxColorDescriptor().setRed(value);
			colorChangedLabelWrapper.getColorDescriptor().setRed(value);
		} else if (Messages.CustomColorComposite_Label_Green.equals(labelText)) {
			colorBox.getColorBoxColorDescriptor().setGreen(value);
			colorChangedLabelWrapper.getColorDescriptor().setGreen(value);
		} else if (Messages.CustomColorComposite_Label_Blue.equals(labelText)) {
			colorBox.getColorBoxColorDescriptor().setBlue(value);
			colorChangedLabelWrapper.getColorDescriptor().setBlue(value);
		}
		colorBox.updateBackground();
		updateLabel();
	}

	public void updatePreview() {
		int red = colorChangedLabelWrapper.getColorDescriptor().getRed();
		int green = colorChangedLabelWrapper.getColorDescriptor().getGreen();
		int blue = colorChangedLabelWrapper.getColorDescriptor().getBlue();
		colorBox.getColorBoxColorDescriptor().setRed(red);
		colorBox.getColorBoxColorDescriptor().setGreen(green);
		colorBox.getColorBoxColorDescriptor().setBlue(blue);
		colorBox.updateBackground();
	}

	private void updateLabel() {
		// RGB
		// selectedColorBoxRGB=(colorBox.getColorBoxColorDescriptor().getRGB());

		if (colorChangedLabelWrapper != null) {
			String colorString;
			if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.HASHED_PRESENTATION_TYPE) {
				colorString = colorChangedLabelWrapper.getHashedColorString();
			} else if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.RGB_FUNCTION_PRESENTATION_TYPE) {
				colorString = colorChangedLabelWrapper
				    .getFunctionStyleColorString();
			} else if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.RGB_FUNCTION_PERCENTAGE_PRESENTATION_TYPE) {
				colorString = colorChangedLabelWrapper
				    .getFunctionAndPercentageStyleColorString();
			} else if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.HASHED_PRESENTATION_SHORT_TYPE) {
				colorString = colorChangedLabelWrapper
				    .getShortHashedColorString();
			} else {
				colorString = colorChangedLabelWrapper.getHashedColorString();
			}
			colorChangedLabelWrapper.getColorChangedLabel()
			    .setText(colorString);
			// clears the previously stored name
			colorChangedLabelWrapper.getColorDescriptor().setName(null);
		}
	}

	public void updateSliders() {
		RGB rgb = colorChangedLabelWrapper.getColorDescriptor().getRGB();

		colorBox.getColorBoxColorDescriptor().setRed(rgb.red);
		colorBox.getColorBoxColorDescriptor().setGreen(rgb.green);
		colorBox.getColorBoxColorDescriptor().setBlue(rgb.blue);

		redSlider.setSelection(rgb.red);
		redText.setText(Integer.toString(rgb.red));
		greenSlider.setSelection(rgb.green);
		greenText.setText(Integer.toString(rgb.green));
		blueSlider.setSelection(rgb.blue);
		blueText.setText(Integer.toString(rgb.blue));

		colorBox.updateBackground();
	}

	private int computeTextWidth(Drawable drawable, int textLength) {
		GC gc = new GC(drawable);
		try {
			return gc.getFontMetrics().getAverageCharWidth() * textLength;
		} finally {
			gc.dispose();
		}
	}

	public void setColorChangedLabel(ColorChangedLabelWrapper colorChangedLabel) {
		this.colorChangedLabelWrapper = colorChangedLabel;
		updateSliders();
	}
}
