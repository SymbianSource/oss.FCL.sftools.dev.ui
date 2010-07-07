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
package com.nokia.tools.ui.widgets;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import com.nokia.tools.ui.Activator;

public class ImageLabel extends Canvas {
	public static final int OVERFLOW_NONE = 0;

	public static final int OVERFLOW_ELLIPSIS_MIDDLE = 1;

	public static final int OVERFLOW_ELLIPSIS_END = 2;

	public static final int OVERFLOW_CLIP = 3;

	private static final String ELLIPSIS = "...";

	private static final String DELIMITER = " ";

	private static final int SPACING = 2;

	private static final int TEXT_AREA = 20;

	private static final Color BACKGROUND_COLOR = new Color(null, 113, 111, 100);

	private static final Image IMAGE_MODIFIED = Activator.getImageDescriptor(
			"icons/check.png").createImage();

	private Image image;

	private ImageDescriptor imageDescriptor;

	private String text;

	private boolean isSelected;

	private boolean isDisabled = false;

	private boolean modified;

	private Color selectedTextColor = ColorConstants.black;

	private Color unselectedTextColor = ColorConstants.black;

	private Color disabledTextColor = ColorConstants.black;

	private Color unselectedBackground = ColorConstants.listBackground;

	private Color selectedBackground = ColorConstants.menuBackgroundSelected;

	private Color disabledBackground = ColorConstants.listBackground;

	private int lines = 2;

	private int overflowMode = OVERFLOW_ELLIPSIS_END;

	private boolean fillBackground;

	private int textAlignment = 1; // 0 = right side, 1 = bottom

	private boolean isTextTransparent = false;

	private boolean observeFontSize = false;

	private Set<SelectedListener> listeners = new HashSet<SelectedListener>();

	public ImageLabel(Composite parent, int style) {
		super(parent, style);
		addDisposeListener(new DisposeListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse
			 * .swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				onDispose(e);
			}

		});
		addPaintListener(new PaintListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.events.PaintListener#paintControl(org.eclipse
			 * .swt.events.PaintEvent)
			 */
			public void paintControl(PaintEvent event) {
				onPaint(event);
			}
		});
		addMouseListener(new MouseAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt
			 * .events.MouseEvent)
			 */
			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					setSelected(true);
					for (SelectedListener listener : listeners) {
						listener.selected(new EventObject(ImageLabel.this));
					}
				}
			}
		});
	}

	/**
	 * @return Returns the image.
	 */
	protected Image getImage() {
		if ((image == null || image.isDisposed()) && imageDescriptor != null) {
			image = imageDescriptor.createImage();
		}
		return image;
	}

	/**
	 * @param imageDescriptor
	 *            The imageDescriptor to set.
	 */
	public void setImageDescriptor(ImageDescriptor imageDescriptor) {
		if (imageDescriptor != null && image != null) {
			image.dispose();
			image = null;
		}
		if (this.imageDescriptor != imageDescriptor) {
			this.imageDescriptor = imageDescriptor;
			redraw();
		}
	}

	/**
	 * @return the lines
	 */
	public int getLines() {
		return lines;
	}

	/**
	 * @param lines
	 *            the lines to set
	 */
	public void setLines(int lines) {
		if (this.lines != lines) {
			this.lines = lines;
			redraw();
		}
	}

	public boolean isShowText() {
		return text != null && lines > 0;
	}

	/**
	 * @return the overflowMode
	 */
	public int getOverflowMode() {
		return overflowMode;
	}

	/**
	 * @param overflowMode
	 *            the overflowMode to set
	 */
	public void setOverflowMode(int overflowMode) {
		switch (overflowMode) {
		case OVERFLOW_NONE:
		case OVERFLOW_ELLIPSIS_MIDDLE:
		case OVERFLOW_ELLIPSIS_END:
		case OVERFLOW_CLIP:
			this.overflowMode = overflowMode;
			break;
		}
	}

	/**
	 * @return the selectedBgColor
	 */
	public Color getSelectedBackground() {
		return selectedBackground;
	}

	/**
	 * @param selectedBgColor
	 *            the selectedBgColor to set
	 */
	public void setSelectedBackground(Color selectedBgColor) {
		this.selectedBackground = selectedBgColor;
	}

	/**
	 * @return the unselectedBgColor
	 */
	public Color getUnselectedBackground() {
		return unselectedBackground;
	}

	/**
	 * @param unselectedBgColor
	 *            the unselectedBgColor to set
	 */
	public void setUnselectedBackground(Color unselectedBgColor) {
		this.unselectedBackground = unselectedBgColor;
	}

	/**
	 * @return the disabledBgColor
	 */
	public Color getDisabledBackground() {
		return disabledBackground;
	}

	/**
	 * @param disabledBgColor
	 *            the disabledBgColor to set
	 */
	public void setDisabledBackground(Color disabledBgColor) {
		this.disabledBackground = disabledBgColor;
	}

	/**
	 * @param isTextTransparent
	 *            the isTextTransparent to set
	 */
	public void setTextTransparent(boolean isTextTransparent) {
		this.isTextTransparent = isTextTransparent;
	}

	/**
	 * @return the isTextTransparent
	 */
	public boolean getTextTransparent() {
		return isTextTransparent;
	}

	/**
	 * @return Returns the text.
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 *            The text to set.
	 */
	public void setText(String text) {
		this.text = text;
		redraw();
	}

	/**
	 * @return Returns the isSelected.
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * @param isSelected
	 *            The isSelected to set.
	 */
	public void setSelected(boolean isSelected) {
		if (this.isSelected != isSelected && !this.isDisposed()) {
			this.isSelected = isSelected;
			redraw();
		}
	}

	/**
	 * @param isDisabled
	 *            The isDisabled to set.
	 */
	public void setDisabledState(boolean isDisabled) {
		if (this.isDisabled != isDisabled) {
			this.isDisabled = isDisabled;
			redraw();
		}
	}

	/**
	 * @param color
	 *            The color to set.
	 */
	public void setSelectedTextColor(Color color) {
		this.selectedTextColor = color;
		redraw();
	}

	/**
	 * @return the textColor
	 */
	public Color getSelectedTextColor() {
		return selectedTextColor;
	}

	/**
	 * @param color
	 *            The color to set.
	 */
	public void setUnselectedTextColor(Color color) {
		this.unselectedTextColor = color;
		redraw();
	}

	/**
	 * @return the textColor
	 */
	public Color getUnselectedTextColor() {
		return unselectedTextColor;
	}

	/**
	 * @return the textColor
	 */
	public Color getDisabledTextColor() {
		return disabledTextColor;
	}

	/**
	 * @param color
	 *            The color to set.
	 */
	public void setDisabledTextColor(Color color) {
		this.disabledTextColor = color;
		redraw();
	}

	/**
	 * @param textAlignment
	 *            The textAlignment to set. Value 0 to align text on the right
	 *            side and 1 to align text on bottom (default).
	 */
	public void setTextAlignment(int textAlignment) {
		this.textAlignment = textAlignment;
	}

	/**
	 * @return the textAlignment
	 */
	public int getTextAlignment() {
		return textAlignment;
	}

	/**
	 * @param observeFontSize
	 *            The observeFontSize to set. Value true to observe roughly size
	 *            of the used font when the size of the needed text area is
	 *            defined and false not to observe font size (default).
	 */
	public void setObserveFontSize(boolean observeFontSize) {
		this.observeFontSize = observeFontSize;
	}

	public boolean getObserveFontSize() {
		return observeFontSize;
	}

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	/**
	 * @return the fillBackground
	 */
	public boolean isFillBackground() {
		return fillBackground;
	}

	/**
	 * @param fillBackground
	 *            the fillBackground to set
	 */
	public void setFillBackground(boolean fillBackground) {
		this.fillBackground = fillBackground;
	}

	public void addSelectionListener(SelectedListener listener) {
		listeners.add(listener);
	}

	public void removeSelectionListener(SelectedListener listener) {
		listeners.remove(listener);
	}

	protected void onDispose(DisposeEvent e) {
		if (image != null) {
			image.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		int fontHeight = getFont().getFontData()[0].getHeight();
		int text_area = TEXT_AREA;
		
		if (observeFontSize && fontHeight > 8)
			text_area = (fontHeight - 8) * 10 + TEXT_AREA;

		Point size = new Point((isShowText() ? text_area : 0) + 2 * SPACING,
				3 * SPACING);

		ImageData data = null;
		if (image != null && !image.isDisposed()) {
			data = image.getImageData();
		} else if (imageDescriptor != null) {
			data = imageDescriptor.getImageData();
		}
		if (data != null) {
			int w = Math.max((int) data.width, (int) data.height);
			size.x += w;
			size.y += w + SPACING;
		}

		if (isShowText()) {
			size.y += getTextHeight();
			if (OVERFLOW_NONE == overflowMode) {
				size.x = Math.max(size.x, FigureUtilities.getTextExtents(text,
						getFont()).width
						+ 2 * SPACING);
			}
			if (data != null && textAlignment == 0) {
				size.y = data.height + 2 * SPACING;
				size.x = data.width
						+ FigureUtilities.getTextExtents(text, getFont()).width
						+ 6 * SPACING;
			}

		} else if (data != null) {
			size.x = data.height + 2 * SPACING;
			size.y = data.width + 2 * SPACING;
		}

		return size;
	}

	protected int getTextHeight() {
		StringBuilder sb = new StringBuilder("a");
		for (int i = 1; i < lines; i++) {
			sb.append("\nb");
		}
		return FigureUtilities.getTextExtents(sb.toString(), getFont()).height;
	}

	public void paint(GC gc) {
		if (isDisabled) {
			setSelected(false);
			setBackground(disabledBackground);
		} else if (isSelected()) {
			setBackground(selectedBackground);
		} else {
			setBackground(unselectedBackground);
		}

		Point size = getSize();

		// creates image only on paint
		Image image = getImage();
		if (!isFillBackground() && isSelected() && image != null) {
			Color prevForeground = gc.getForeground();

			int w;
			if (textAlignment == 1)
				w = size.x;
			else
				w = image.getBounds().width + 2 * SPACING;

			int h;
			if (textAlignment == 1)
				h = Math.max(image.getBounds().width, image.getBounds().height)
						+ 2 * SPACING;
			else
				h = image.getBounds().height + 2 * SPACING;

			gc.setLineWidth(1);
			gc.setLineStyle(Graphics.LINE_SOLID);
			gc.setXORMode(false);

			int top = 0;
			int left = 0;
			int bottom = h - 1;
			int right = w - 1;
			Color color = ColorConstants.white;

			gc.setForeground(color);
			gc.drawLine(right, bottom, right, top);
			gc.drawLine(right, bottom, left, bottom);

			right--;
			bottom--;

			color = BACKGROUND_COLOR;
			gc.setForeground(color);
			gc.drawLine(left, top, right, top);
			gc.drawLine(left, top, left, bottom);

			gc.setForeground(ColorConstants.white);

			for (int j = 1; j < h - 2; j++) {
				for (int i = 1; i < w - 2; i += 2) {
					gc.drawPoint(i + (j) % 2, j);
				}
			}
			gc.setForeground(prevForeground);
		}

		if (image != null) {
			if (textAlignment == 1)
				gc.drawImage(image, (size.x - image.getBounds().width) / 2,
						(size.y - (isShowText() ? getTextHeight() : -5) - 2
								* SPACING - image.getBounds().height) / 2);
			else
				gc.drawImage(image, SPACING,
						(size.y - image.getBounds().height) / 2);
		}

		int modY = SPACING;
		if (image != null) {
			modY = Math.max(image.getBounds().width, image.getBounds().height)
					+ 3 * SPACING;
		}

		if (isShowText()) {
			int y = modY;
			if (isDisabled)
				gc.setForeground(disabledTextColor);
			else
				gc.setForeground(isSelected() ? selectedTextColor
						: unselectedTextColor);
			String[] words = text.split(DELIMITER);
			int currentLine = 1;
			while (currentLine <= lines) {
				Line line = getLine(words);

				if (line.index == words.length - 1 || currentLine < lines) {
					paintLine(gc, line.line, y);
				} else {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < words.length; i++) {
						sb.append(words[i]);
						if (i < words.length - 1) {
							sb.append(DELIMITER);
						}
					}
					String str = shortenText(sb.toString());
					paintLine(gc, str, y);
				}
				if (line.index >= words.length - 1) {
					break;
				}
				String[] restWords = new String[words.length - line.index - 1];
				System.arraycopy(words, line.index + 1, restWords, 0,
						restWords.length);
				words = restWords;
				currentLine++;
				y += FigureUtilities.getTextExtents(line.line, getFont()).height;
			}
		}
		// draws the modification indication
		if (isModified()) {
			if (image == null) {
				gc.drawImage(IMAGE_MODIFIED, getSize().x
						- IMAGE_MODIFIED.getImageData().width - 1, modY);
			} else {
				if (isShowText())
					gc
							.drawImage(IMAGE_MODIFIED, getSize().x
									- IMAGE_MODIFIED.getImageData().width - 1,
									modY - 7);
				else
					gc.drawImage(IMAGE_MODIFIED, getSize().x
							- IMAGE_MODIFIED.getImageData().width - 1,
							getSize().y - IMAGE_MODIFIED.getImageData().height
									- 1);
			}
		}
		//Dispose image here
	    if (image != null) {
	    	image.dispose();
	    	image = null;
	    }
	}

	protected void onPaint(PaintEvent e) {
		paint(e.gc);
	}

	private void paintLine(GC gc, String line, int y) {
		if (textAlignment == 1) {
			int width = FigureUtilities.getTextWidth(line, getFont());
			gc
					.drawString(line, (getSize().x - width) / 2, y,
							isTextTransparent);
		} else {
			gc.drawString(line, image.getImageData().width + 4 * SPACING,
					(getSize().y - getTextHeight()) / 2, isTextTransparent);
		}
	}

	private String shortenText(String text) {
		int index = 0;
		String line = ELLIPSIS;
		while (FigureUtilities.getTextWidth(line, getFont()) < getSize().x - 2
				* SPACING
				&& index < text.length() - 1) {
			line = text.substring(0, ++index) + ELLIPSIS;
		}
		return text.substring(0, index - 1) + ELLIPSIS;
	}

	private Line getLine(String[] words) {
		int index = 1;
		String line = words[0];
		boolean found = true;
		while (true) {
			if (FigureUtilities.getTextWidth(line, getFont()) > getSize().x - 2
					* SPACING) {
				found = false;
				break;
			}
			if (index >= words.length) {
				break;
			}
			line += DELIMITER + words[index++];
		}
		if (found) {
			return new Line(line, words.length - 1);
		}

		if (index == 1) {
			line = shortenText(words[0]);
			return new Line(line, 0);
		}
		index -= 2;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i <= index; i++) {
			sb.append(words[i]);
			if (i < words.length - 1) {
				sb.append(DELIMITER);
			}
		}
		return new Line(sb.toString(), index);
	}

	class Line {
		String line;

		int index;

		Line(String line, int index) {
			this.line = line;
			this.index = index;
		}
	}

	public interface SelectedListener {
		void selected(EventObject e);
	}
}
