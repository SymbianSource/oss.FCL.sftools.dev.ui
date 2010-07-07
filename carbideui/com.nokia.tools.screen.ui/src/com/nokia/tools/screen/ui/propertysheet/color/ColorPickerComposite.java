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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.ui.color.ColorBox;
import com.nokia.tools.ui.color.ColorDescriptor;
import com.nokia.tools.ui.color.ColorPickerPane;
import com.nokia.tools.ui.color.TooltipMessages;

public class ColorPickerComposite extends Composite {

	boolean canHover = false;

	public ColorPickerComposite(Composite parent, int style,
			final ColorChangedLabelWrapper colorChangedLabelWrapper,
			final IColorPickerListener colorPickerListener) {
		super(parent, style);

		Composite cPickerComposite = this;
		GridLayout gl = new GridLayout(2, false);
		gl.marginWidth = gl.marginHeight = 5;
		gl.horizontalSpacing = 0;
		cPickerComposite.setLayout(gl);

		final CustomColorBoxesComposite namedColorComposite = new CustomColorBoxesComposite(
				cPickerComposite, SWT.NONE, colorPickerListener, 12);
		namedColorComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				false, false));
		namedColorComposite.setColorChangedLabel(colorChangedLabelWrapper);

		final Composite customColorComposite = new Composite(cPickerComposite,
				SWT.NONE);
		customColorComposite.setVisible(false);
		customColorComposite.setLayoutData(new GridData(0, 0));

		final Image imageExpand = UtilsPlugin.getImageDescriptor(
				"icons/arrowRR.png").createImage();

		final Image imagePack = UtilsPlugin.getImageDescriptor(
				"icons/arrowLL.png").createImage();

		namedColorComposite.getSetCustomColorButton().setImage(imageExpand);
		namedColorComposite.getSetCustomColorButton().addDisposeListener(
				new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						imageExpand.dispose();
						imagePack.dispose();
					}
				});

		namedColorComposite.getSetCustomColorButton().addSelectionListener(
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						canHover = false;

						if (!customColorComposite.isVisible()) {
							customColorComposite.setVisible(true);
							namedColorComposite.getSetCustomColorButton()
									.setImage(imagePack);
							customColorComposite.setLayoutData(new GridData(
									SWT.FILL, SWT.FILL, false, false));
						} else {
							customColorComposite.setVisible(false);
							namedColorComposite.getSetCustomColorButton()
									.setImage(imageExpand);
							customColorComposite.setLayoutData(new GridData(0,
									0));
						}

						Point prefSize = customColorComposite.getShell()
								.computeSize(SWT.DEFAULT, SWT.DEFAULT);
						Point loc = customColorComposite.getShell()
								.getLocation();
						Rectangle bounds = new Rectangle(loc.x, loc.y,
								prefSize.x, prefSize.y);
						Rectangle displayBounds = customColorComposite
								.getDisplay().getBounds();
						if (bounds.x + bounds.width >= displayBounds.width) {
							bounds.x = displayBounds.width - bounds.width;
						}
						if (bounds.y + bounds.height >= displayBounds.height) {
							bounds.y = displayBounds.height - bounds.height;
						}
						customColorComposite.getShell().setBounds(bounds);
					};
				});

		namedColorComposite.getSetCustomColorButton().addMouseTrackListener(
				new MouseTrackAdapter() {
					@Override
					public void mouseEnter(MouseEvent e) {
						canHover = true;
					}

					public void mouseExit(MouseEvent e) {
						canHover = false;
					}

					@Override
					public void mouseHover(MouseEvent e) {
						if (!canHover) {
							return;
						}

						canHover = false;

						if (!customColorComposite.isVisible()) {
							customColorComposite.setVisible(true);
							namedColorComposite.getSetCustomColorButton()
									.setImage(imagePack);
							customColorComposite.setLayoutData(new GridData(
									SWT.FILL, SWT.FILL, false, false));
						} else {
							customColorComposite.setVisible(false);
							namedColorComposite.getSetCustomColorButton()
									.setImage(imageExpand);
							customColorComposite.setLayoutData(new GridData(0,
									0));
						}
						Point prefSize = customColorComposite.getShell()
								.computeSize(SWT.DEFAULT, SWT.DEFAULT);
						Point loc = customColorComposite.getShell()
								.getLocation();
						Rectangle bounds = new Rectangle(loc.x, loc.y,
								prefSize.x, prefSize.y);
						Rectangle displayBounds = customColorComposite
								.getDisplay().getBounds();
						if (bounds.x + bounds.width >= displayBounds.width) {
							bounds.x = displayBounds.width - bounds.width;
						}
						if (bounds.y + bounds.height >= displayBounds.height) {
							bounds.y = displayBounds.height - bounds.height;
						}
						customColorComposite.getShell().setBounds(bounds);
					}
				});

		gl = new GridLayout(2, false);
		gl.marginWidth = gl.marginHeight = 0;
		gl.marginLeft = gl.horizontalSpacing = 5;
		customColorComposite.setLayout(gl);

		RGB rgb = colorChangedLabelWrapper.getColorDescriptor().getRGB();
		java.awt.Color color = new java.awt.Color(rgb.red, rgb.green, rgb.blue);

		final ColorPickerPane pickerPane = new ColorPickerPane(
				customColorComposite, SWT.NONE, color);
		pickerPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		((GridData) pickerPane.getLayoutData()).widthHint = 200;

		Composite rgbComp = new Composite(customColorComposite, SWT.NONE);
		gl = new GridLayout(2, false);
		gl.marginWidth = gl.marginHeight = gl.horizontalSpacing = 0;
		rgbComp.setLayout(gl);
		rgbComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label label = new Label(rgbComp, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		label.setText(TooltipMessages.ColorPickerTooltip_Label_Red);
		label.setAlignment(SWT.RIGHT);
		final Text redText = new Text(rgbComp, SWT.NONE);
		redText.setText("" + color.getRed());
		redText.setLayoutData(new GridData(20, SWT.DEFAULT));

		label = new Label(rgbComp, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		label.setText(TooltipMessages.ColorPickerTooltip_Label_Green);
		label.setAlignment(SWT.RIGHT);
		final Text greenText = new Text(rgbComp, SWT.NONE);
		greenText.setText("" + color.getGreen());
		greenText.setLayoutData(new GridData(20, SWT.DEFAULT));

		label = new Label(rgbComp, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		label.setText(TooltipMessages.ColorPickerTooltip_Label_Blue);
		label.setAlignment(SWT.RIGHT);
		final Text blueText = new Text(rgbComp, SWT.NONE);
		blueText.setText("" + color.getBlue());
		blueText.setLayoutData(new GridData(20, SWT.DEFAULT));

		final ColorBox colorBox = new ColorBox(rgbComp, SWT.NONE,
				colorChangedLabelWrapper.getColorDescriptor(), 0);
		GridData gd = new GridData(60, 60);
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.RIGHT;
		colorBox.setLayoutData(gd);

		Label separator = new Label(rgbComp, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 2;
		separator.setLayoutData(gd);

		Button apply = new Button(rgbComp, SWT.FLAT);
		apply.setText(TooltipMessages.ColorPickerTooltip_Button_Apply);
		gd = new GridData(SWT.RIGHT, SWT.NONE, false, false);
		gd.horizontalSpan = 2;
		apply.setLayoutData(gd);

		pickerPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				RGB color = pickerPane.getColorAt(e.x, e.y);

				redText.setText("" + color.red);
				greenText.setText("" + color.green);
				blueText.setText("" + color.blue);

				pickerPane.select(e.x, e.y, new java.awt.Color(color.red,
						color.green, color.blue));

				colorBox.setColorBoxColorDescriptor(new ColorDescriptor(color,
						""));
				colorBox.redraw();
			}
		});

		apply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RGB color = new RGB(Integer.parseInt(redText.getText()),
						Integer.parseInt(greenText.getText()), Integer
								.parseInt(blueText.getText()));

				colorChangedLabelWrapper
						.setColorDescriptor(new ColorDescriptor(color, ""));

				// add color to custom colors

				System.arraycopy(CustomColorBoxesComposite.customColors, 0,
						CustomColorBoxesComposite.customColors, 1,
						CustomColorBoxesComposite.customColors.length - 1);

				CustomColorBoxesComposite.customColors[0] = new ColorDescriptor(
						color, "");
				
				// Fix for the issue where the a color is used multiple times, adds
				// to the custom colors even if the
				// color is same.
				for (int i = 1; i < CustomColorBoxesComposite.customColors.length; i++) {
					if (CustomColorBoxesComposite.customColors[i].getRGB().equals(
							color)) {
						System.arraycopy(CustomColorBoxesComposite.customColors, 1, CustomColorBoxesComposite.customColors, 0,
								CustomColorBoxesComposite.customColors.length - 2);
					}
				}


				CustomColorBoxesComposite.storeCustomColors();

				colorPickerListener.okCloseDialog();
			}
		});

		FocusAdapter focusAdapter = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				Text control = (Text) e.widget;
				String text = control.getText();
				try {
					int num = Integer.parseInt(text);
					if (num < 0) {
						control.setText("0");
					}
					if (num > 255) {
						control.setText("255");
					}
				} catch (NumberFormatException nfe) {
					control.setText("0");
					return;
				}
				RGB color = new RGB(Integer.parseInt(redText.getText()),
						Integer.parseInt(greenText.getText()), Integer
								.parseInt(blueText.getText()));
				pickerPane.select(-1, -1, new java.awt.Color(color.red,
						color.green, color.blue));
				colorBox.setColorBoxColorDescriptor(new ColorDescriptor(color,
						""));
				colorBox.redraw();
			}
		};

		redText.addFocusListener(focusAdapter);
		greenText.addFocusListener(focusAdapter);
		blueText.addFocusListener(focusAdapter);
	}

	public static class CustomColorBoxesComposite extends NamedColorComposite {

		static ColorDescriptor[] customColors = new ColorDescriptor[15];

		public static ColorDescriptor[] getCustomColors() {
			return customColors;
		}

		public static void updateAndStoreCustomColors(
				ColorDescriptor[] descriptors) {
			if (descriptors.length == customColors.length) {
				for (int i = 0; i < customColors.length; i++) {
					customColors[i] = descriptors[i];
				}
			}
			storeCustomColors();
		}

		static {
			for (int i = 0; i < customColors.length; i++) {
				String hashString = UtilsPlugin.getDefault()
						.getPreferenceStore().getString(
								"ColorPicker_color_" + i);
				if (hashString != null && hashString.length() > 0) {
					customColors[i] = new ColorDescriptor(ColorUtil
							.getRGB(hashString), "");
				} else {
					customColors[i] = new ColorDescriptor(255, 255, 255, "");
				}
			}
		}

		protected ColorBox[] customColorBoxes;

		protected Button setCustomColorButton;

		public CustomColorBoxesComposite(Composite parent, int style,
				IColorPickerListener dialogClose, int colorBoxSize) {
			super(parent, style, dialogClose, colorBoxSize);
			GridLayout layout = (GridLayout) getLayout();
			layout.marginWidth = 0;
			layout.marginHeight = 0;
		}

		public CustomColorBoxesComposite(Composite parent, int style,
				IColorPickerListener dialogClose) {
			super(parent, style, dialogClose);
			GridLayout layout = (GridLayout) getLayout();
			layout.marginWidth = 0;
			layout.marginHeight = 0;
		}

		public static void storeCustomColors() {
			for (int i = 0; i < customColors.length; i++) {
				UtilsPlugin.getDefault().getPreferenceStore().putValue(
						"ColorPicker_color_" + i,
						ColorDescriptor.asHashString(customColors[i].getRGB()));
			}
		}

		public void createBasicComposites() {
			basicColorBoxes = new ColorBox[basicColorCellNumber];
			additionalColorBoxes = new ColorBox[additionalColorCellNumber];
			fillBasicColorsAndBorders();
			fillAdditionalColorsAndBorders();
			Label customColorLabel = new Label(this, SWT.NONE);
			customColorLabel
					.setText(TooltipMessages.ColorPickerTooltip_Label_Custom);
			GridData gd = new GridData();
			gd.horizontalSpan = 16;
			gd.verticalIndent = 14;
			customColorLabel.setLayoutData(gd);
			fillCustomColorsAndBorders();
			selectedColorBox = basicColorBoxes[0];
		}

		protected void fillCustomColorsAndBorders() {
			customColorBoxes = new ColorBox[customColors.length];

			for (int i = 0; i < customColors.length; i++) {

				customColorBoxes[i] = new ColorBox(this, SWT.NONE,
						customColors[i], i);

				customColorBoxes[i].addMouseListener(new MouseAdapter() {

					public void mouseDown(MouseEvent e) {
						Composite colorBox = (Composite) e.widget;
						clearSelection();
						selectedColorBox = colorBox;
						updateAfterSelectionChange();
					}

					public void mouseDoubleClick(MouseEvent e) {
						Composite colorBox = (Composite) e.widget;
						clearSelection();
						selectedColorBox = (ColorBox) colorBox.getParent();
						updateAfterSelectionChange();
						CustomColorBoxesComposite.this.dialogClose
								.okCloseDialog();
					}

				});

				customColorBoxes[i].getColorBox().addMouseListener(
						new MouseAdapter() {
							public void mouseDown(MouseEvent e) {
								Composite colorBox = (Composite) e.widget;
								clearSelection();
								selectedColorBox = (ColorBox) colorBox
										.getParent();
								updateAfterSelectionChange();
							}

							public void mouseDoubleClick(MouseEvent e) {
								Composite colorBox = (Composite) e.widget;
								clearSelection();
								selectedColorBox = (ColorBox) colorBox
										.getParent();
								updateAfterSelectionChange();
								CustomColorBoxesComposite.this.dialogClose
										.okCloseDialog();
							}

						});

				customColorBoxes[i].addKeyListener(new KeyAdapter() {

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

						clearSelection();
						selectedColorBox = customColorBoxes[index];

						updateAfterSelectionChange();

					}

				});

				GridData gd = new GridData();

				gd.heightHint = colorBoxSize;
				gd.widthHint = colorBoxSize;
				customColorBoxes[i].setLayoutData(gd);
			}

			setCustomColorButton = new Button(this, SWT.FLAT);

			GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
			gd.heightHint = colorBoxSize + 4;
			gd.widthHint = colorBoxSize + 4;
			setCustomColorButton.setLayoutData(gd);
		}

		@Override
		public void setColorChangedLabel(
				ColorChangedLabelWrapper colorChangedLabel) {
			super.setColorChangedLabel(colorChangedLabel);
			if (this.colorChangedLabelWrapper.getColorDescriptor() != null) {
				ColorDescriptor desc = colorChangedLabelWrapper
						.getColorDescriptor();
				for (ColorBox box : customColorBoxes) {
					if (desc.getRed() == box.getColorBoxColorDescriptor()
							.getRed()
							&& desc.getGreen() == box
									.getColorBoxColorDescriptor().getGreen()
							&& desc.getBlue() == box
									.getColorBoxColorDescriptor().getBlue()) {
						box.setSelected(true);
						box.setBackground(box.getBorderSelectionColor());
						box.redraw();
						break;
					}
				}
			}
		}

		public Button getSetCustomColorButton() {
			return setCustomColorButton;
		}

	}
}