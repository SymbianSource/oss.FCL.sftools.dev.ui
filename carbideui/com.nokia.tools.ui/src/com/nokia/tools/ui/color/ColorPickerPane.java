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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class ColorPickerPane extends Canvas implements PaintListener {

	static int[][] BorderData = { { 255, 0, 0 }, { 255, 255, 0 },

	{ 0, 255, 0 }, { 0, 255, 255 }, { 0, 0, 255 }, { 255, 0, 255 },

	{ 255, 0, 0 } };

	java.awt.Color selectedColor = java.awt.Color.WHITE;

	Point selectedColorPoint = new Point(0, 0);

	int minDiff = Integer.MAX_VALUE;

	Image lastImage;

	public ColorPickerPane(Composite parent, int style,
			java.awt.Color selectedColor) {
		super(parent, style);
		this.selectedColor = selectedColor;
		addPaintListener(this);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (lastImage != null) {
					lastImage.dispose();
				}
			}
		});
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return super.computeSize(wHint, hHint, changed);
	}

	public void paintControl(final PaintEvent e) {
		fastRepaint();
	}

	public void select(int x, int y, java.awt.Color selectedColor) {
		selectedColorPoint.x = x;
		selectedColorPoint.y = y;
		this.selectedColor = selectedColor;
		minDiff = 0;
		fastRepaint();
	}

	private void fastRepaint() {
		if (selectedColorPoint.x == -1 && selectedColorPoint.y == -1) {
			minDiff = Integer.MAX_VALUE;
			repaint();
			return;
		}

		if (lastImage == null || lastImage.isDisposed()) {
			repaint();
			return;
		}

		GC gc = new GC(ColorPickerPane.this);
		try {
			gc.drawImage(lastImage, 0, 0);

			RGB rgb = new RGB(selectedColor.getRed(), selectedColor.getGreen(),
					selectedColor.getBlue());
			if (rgb.red + rgb.blue + rgb.green < 384) {
				gc.setForeground(ColorConstants.white);
			} else {
				gc.setForeground(ColorConstants.black);
			}

			gc.drawLine(selectedColorPoint.x, selectedColorPoint.y - 2,
					selectedColorPoint.x, selectedColorPoint.y + 2);
			gc.drawLine(selectedColorPoint.x - 2, selectedColorPoint.y,
					selectedColorPoint.x + 2, selectedColorPoint.y);
		} finally {
			gc.dispose();
		}

	}

	private void repaint() {
		final int width = getSize().x;
		final int height = getSize().y;
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				if (lastImage != null) {
					lastImage.dispose();
				}
				if (width <= 0 || height <= 0) {
					return;
				}
				if (ColorPickerPane.this.isDisposed()) {
					return;
				}
				GC pgc = new GC(ColorPickerPane.this);
				lastImage = new Image(null, width, height);
				GC gc = new GC(lastImage);
				try {
					for (int x = 0; x < width; x++) {
						for (int y = 0; y < height; y++) {
							RGB rgb = compute(x, y, width, height);
							if (minDiff > 0) {
								int dr = rgb.red - selectedColor.getRed();
								int dg = rgb.green - selectedColor.getGreen();
								int db = rgb.blue - selectedColor.getBlue();
								int diff = dr * dr + dg * dg + db * db;
								if (diff < minDiff) {
									minDiff = diff;
									if (rgb.red == rgb.green
											&& rgb.green == rgb.blue) {
										int GRAY_SELECTOR_W = width / 12;
										int grayTreshold = width
												- GRAY_SELECTOR_W;
										if (rgb.red == 0) {
											selectedColorPoint.x = grayTreshold;
											selectedColorPoint.y = height / 2;
										} else {
											selectedColorPoint.x = width
													- ((width - grayTreshold) / 2);
											selectedColorPoint.y = y;
										}
									} else {
										selectedColorPoint.x = x;
										selectedColorPoint.y = y;
									}
								}
							}
							Color color = new Color(null, rgb);
							gc.setForeground(color);
							gc.drawPoint(x, y);
							color.dispose();
						}
					}

					pgc.drawImage(lastImage, 0, 0);

					RGB rgb = new RGB(selectedColor.getRed(), selectedColor
							.getGreen(), selectedColor.getBlue());
					if (rgb.red + rgb.blue + rgb.green < 384) {
						pgc.setForeground(ColorConstants.white);
					} else {
						pgc.setForeground(ColorConstants.black);
					}

					pgc.drawLine(selectedColorPoint.x,
							selectedColorPoint.y - 2, selectedColorPoint.x,
							selectedColorPoint.y + 2);
					pgc.drawLine(selectedColorPoint.x - 2,
							selectedColorPoint.y, selectedColorPoint.x + 2,
							selectedColorPoint.y);
				} finally {
					gc.dispose();
					pgc.dispose();
				}
			}
		});
	}

	public RGB getColorAt(int x, int y) {
		return compute(x, y, getSize().x, getSize().y);
	}

	private RGB compute(int x, int y, int width, int height) {
		int GRAY_SELECTOR_W = width / 12;

		int grayTreshold = width - GRAY_SELECTOR_W;

		if (x >= grayTreshold) {

			if (x == grayTreshold)

				return ColorConstants.black.getRGB();

			float ratio = 1 - y / (float) height;

			java.awt.Color awtColor = new java.awt.Color(ratio, ratio, ratio);

			return new RGB(awtColor.getRed(), awtColor.getGreen(), awtColor
					.getBlue());
		}

		width = grayTreshold;

		float treshold = 0.8f;

		int SEQ_W = width / 6;

		int seqNo = x / SEQ_W;

		int seqOffset = x - (seqNo * SEQ_W);

		if (seqOffset < 0)

			seqOffset = 0;

		if (seqOffset >= SEQ_W)

			seqOffset = SEQ_W - 1;

		// we have all data needed to interpolate

		int[] b1 = BorderData[seqNo];

		int[] b2 = BorderData[(seqNo + 1) % BorderData.length];

		float ratio = 1 - (seqOffset / (float) SEQ_W);

		int red = (int) ((b1[0] * ratio) + (b2[0] * (1 - ratio)));

		int green = (int) (b1[1] * ratio + b2[1] * (1 - ratio));

		int blue = (int) (b1[2] * ratio + b2[2] * (1 - ratio));

		float yRatio = (y - (height / 2)) / (float) (height / 2);

		if (yRatio < 0) {

			yRatio = (y / (float) (height / 2));

			yRatio = (yRatio * treshold) + 1 - treshold;

			// merge with white by this ratio

			red = (int) (255 * (1 - yRatio) + red * yRatio);

			green = (int) (255 * (1 - yRatio) + green * yRatio);

			blue = (int) (255 * (1 - yRatio) + blue * yRatio);

		} else {

			yRatio = 1 - yRatio;

			yRatio = (yRatio * treshold) + 1 - treshold;

			// merge with white by this ratio

			red = (int) (0 * (1 - yRatio) + red * yRatio);

			green = (int) (0 * (1 - yRatio) + green * yRatio);

			blue = (int) (0 * (1 - yRatio) + blue * yRatio);

		}

		return new RGB(red, green, blue);
	}
}
