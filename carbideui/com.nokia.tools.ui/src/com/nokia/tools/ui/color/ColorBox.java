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
package com.nokia.tools.ui.color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class ColorBox extends Composite {
	private static final Color BORDER_SELECTION_COLOR = new Color(null, 0, 0, 0);
	private static final Color NOT_SELECTED_COLOR = new Color(null, 200, 200,
			200);
	private Color borderSelectionColor;
	private Color notSelectedColor;
	private boolean isSelected;
	private ColorDescriptor colorBoxColorDescriptor;
	private int colorPosition;
	private Composite colorBox;

	private Color color;

	public Color getBorderSelectionColor() {
		return borderSelectionColor;
	}

	public void setBorderSelectionColor(Color borderSelectionColor) {
		this.borderSelectionColor = borderSelectionColor;
	}

	public Composite getColorBox() {
		return colorBox;
	}

	public void setColorBox(Composite colorBox) {
		this.colorBox = colorBox;
	}

	public ColorDescriptor getColorBoxColorDescriptor() {
		return colorBoxColorDescriptor;
	}

	public void setColorBoxColorDescriptor(ColorDescriptor colorBoxColor) {
		this.colorBoxColorDescriptor = colorBoxColor;
		updateBackground();
	}

	public int getColorPosition() {
		return colorPosition;
	}

	public void setTooltipText(String tooltip) {
		colorBox.setToolTipText(tooltip);
	}

	public void setColorPosition(int colorPosition) {
		this.colorPosition = colorPosition;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public ColorBox(Composite parent, int style,
			ColorDescriptor colorDescriptor, int position) {
		super(parent, style);

		addDisposeListener(new DisposeListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				onDispose(e);
			}

		});
		borderSelectionColor = BORDER_SELECTION_COLOR;
		notSelectedColor = NOT_SELECTED_COLOR;
		isSelected = false;
		colorBoxColorDescriptor = colorDescriptor;
		colorPosition = position;

		this.setBackground(notSelectedColor);
		colorBox = new Composite(this, SWT.NONE);

		updateBackground();
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 2;
		layout.marginHeight = 2;
		layout.marginWidth = 2;

		this.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.heightHint = 10;

		colorBox.setLayoutData(gd);
	}

	protected void onDispose(DisposeEvent e) {
		if (color != null) {
			color.dispose();
		}
	}

	public Color getNotSelectedColor() {
		return notSelectedColor;
	}

	public void setNotSelectedColor(Color notSelectedColor) {
		this.notSelectedColor = notSelectedColor;
	}

	public void updateBackground() {
		if (color != null) {
			color.dispose();
		}
		color = new Color(null, this.colorBoxColorDescriptor.getRGB());
		colorBox.setBackground(color);
	}

	public void updateColor(int red, int green, int blue) {
		colorBoxColorDescriptor.setRed(red);
		colorBoxColorDescriptor.setGreen(green);
		colorBoxColorDescriptor.setBlue(blue);
		updateBackground();
	}

	public void updateRed(int red) {
		colorBoxColorDescriptor.setRed(red);
		updateBackground();
	}

	public void updateGreen(int green) {
		colorBoxColorDescriptor.setGreen(green);
		updateBackground();
	}

	public void updateBlue(int blue) {
		colorBoxColorDescriptor.setBlue(blue);
		updateBackground();
	}

}
