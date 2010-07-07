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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.nokia.tools.ui.color.ColorBox;
import com.nokia.tools.ui.color.ColorDescriptor;

public class WebPaletteComposite extends Composite {

	int cellNumber = 18 * 12;

	ColorBox[] colorBoxes;

	ColorBox selectedColorBox = null;

	IColorPickerListener dialogClose;

	ColorChangedLabelWrapper colorChangedLabelWrapper;

	public WebPaletteComposite(Composite parent, int style,
			IColorPickerListener dialogClose) {
		super(parent, style);
		GridLayout layout = new GridLayout();
		layout.numColumns = 12;

		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 1;
		layout.horizontalSpacing = 1;
		this.setLayout(layout);
		this.dialogClose = dialogClose;
		createComposites();
	}

	public void createComposites() {

		colorBoxes = new ColorBox[cellNumber];
		for (int i = 0; i < cellNumber; i++) {
			RGB colorBoxesColorRGB = getWebColorRGB(i);

			colorBoxes[i] = new ColorBox(this, SWT.NONE, new ColorDescriptor(
					colorBoxesColorRGB, null), i);

			colorBoxes[i].addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					ColorBox colorBox = (ColorBox) e.widget;
					clearSelection();
					selectedColorBox = colorBox;
					updateAfterSelectionChange();
				}

				public void mouseDoubleClick(MouseEvent e) {
					ColorBox colorBox = (ColorBox) e.widget;
					clearSelection();
					selectedColorBox = colorBox;
					updateAfterSelectionChange();
					WebPaletteComposite.this.dialogClose.okCloseDialog();
				}

			});

			colorBoxes[i].getColorBox().addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					Composite colorBox = (Composite) e.widget;
					clearSelection();
					selectedColorBox = (ColorBox) colorBox.getParent();
					updateAfterSelectionChange();
				}

				public void mouseDoubleClick(MouseEvent e) {
					Composite colorBox = (Composite) e.widget;
					clearSelection();
					selectedColorBox = (ColorBox) colorBox.getParent();
					updateAfterSelectionChange();
					WebPaletteComposite.this.dialogClose.okCloseDialog();
				}

			});

			colorBoxes[i].addKeyListener(new KeyAdapter() {

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
						if ((index - 12) >= 0) {
							index = index - 12;
						}
					} else if (e.keyCode == SWT.ARROW_DOWN) {
						if ((index + 12) < cellNumber) {
							index = index + 12;
						}
					} else if (e.keyCode == SWT.ARROW_LEFT) {
						if ((index % 12) > 0) {
							index = index - 1;
						}
					} else if (e.keyCode == SWT.ARROW_RIGHT) {
						if ((index % 12) < 11) {
							index = index + 1;
						}
					} else {
						return;
					}

					clearSelection();
					selectedColorBox = colorBoxes[index];
					updateAfterSelectionChange();

				}

			});

			GridData gd = new GridData();

			gd.heightHint = 14;
			gd.widthHint = 24;
			colorBoxes[i].setLayoutData(gd);
		}
		selectedColorBox = colorBoxes[0];

		selectedColorBox.getParent().setFocus();
		selectedColorBox.setFocus();
		selectedColorBox.forceFocus();
	}

	private RGB getWebColorRGB(int i) {
		char firstByte = computeFirstChar(i);
		char secondByte = computeSecondChar(i);
		char thirdByte = computeThirdChar(i);
		return new RGB(firstByte, secondByte, thirdByte);
	}

	private char computeThirdChar(int i) {
		if (((i % 12) < 6)) {
			if ((i < 72)) {
				return (char) 0xff;
			} else if (i < 144) {
				return (char) 0xcc;
			} else if (i < 216) {
				return (char) 0x99;
			} else
				return 0;
		} else if ((i % 12 > 5)) {
			if ((i < 72)) {
				return (char) 0x66;
			} else if (i < 144) {
				return (char) 0x33;
			} else if (i < 216) {
				return (char) 0x00;
			} else
				return 0;
		}
		return 0;
	}

	private char computeSecondChar(int i) {

		switch (i % 12) {
		case 0:
			return (char) 0x00;
		case 1:
			return (char) 0x33;
		case 2:
			return (char) 0x66;
		case 3:
			return (char) 0x99;
		case 4:
			return (char) 0xcc;
		case 5:
			return (char) 0xff;
		case 6:
			return (char) 0xff;
		case 7:
			return (char) 0xcc;
		case 8:
			return (char) 0x99;
		case 9:
			return (char) 0x66;
		case 10:
			return (char) 0x33;
		case 11:
			return (char) 0x00;
		default:
			return 0;
		}
	}

	private char computeFirstChar(int i) {
		if (i < 0)
			return 0;
		if (i < 12)
			return (char) 0xff;
		else if (i < 24)
			return (char) 0xcc;
		else if (i < 36)
			return (char) 0x99;
		else if (i < 48)
			return (char) 0x66;
		else if (i < 60)
			return (char) 0x33;
		else if (i < 72)
			return (char) 0x00;
		else if (i < 84)
			return (char) 0x00;
		else if (i < 96)
			return (char) 0x33;
		else if (i < 108)
			return (char) 0x66;
		else if (i < 120)
			return (char) 0x99;
		else if (i < 132)
			return (char) 0xcc;
		else if (i < 144)
			return (char) 0xff;
		else if (i < 166)
			return (char) 0xff;
		else if (i < 178)
			return (char) 0xcc;
		else if (i < 192)
			return (char) 0x99;
		else if (i < 204)
			return (char) 0x33;
		else if (i < 216)
			return (char) 0x00;
		else
			return 0;
	}

	private void updateAfterSelectionChange() {
		selectedColorBox.setBackground(((ColorBox) selectedColorBox)
				.getBorderSelectionColor());
		RGB selectedColorBoxRGB = ((ColorBox) selectedColorBox)
				.getColorBoxColorDescriptor().getRGB();

		if (colorChangedLabelWrapper != null) {
			colorChangedLabelWrapper.getColorDescriptor().setRed(
					selectedColorBoxRGB.red);
			colorChangedLabelWrapper.getColorDescriptor().setGreen(
					selectedColorBoxRGB.green);
			colorChangedLabelWrapper.getColorDescriptor().setBlue(
					selectedColorBoxRGB.blue);
			colorChangedLabelWrapper.getColorDescriptor().setName(null);

			colorChangedLabelWrapper.getColorChangedLabel().setText(
					this.getProperPresentationString());
		}
	}

	private void clearSelection() {
		if (selectedColorBox != null) {
			selectedColorBox.setBackground(((ColorBox) selectedColorBox)
					.getNotSelectedColor());
			selectedColorBox = null;
		}
		if (selectedColorBox != null) {
			selectedColorBox = null;
		}
	}

	private String getProperPresentationString() {
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
			colorString = colorChangedLabelWrapper.getShortHashedColorString();
		} else {
			colorString = colorChangedLabelWrapper.getHashedColorString();
		}
		return colorString;
	}

	public void setColorChangedLabel(ColorChangedLabelWrapper colorChangedLabel) {
		this.colorChangedLabelWrapper = colorChangedLabel;
		colorChangedLabelWrapper.getColorChangedLabel().setText(
				this.getProperPresentationString());
	}

}
