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
package com.nokia.tools.widget.theme;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.File;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.swing.SwingConstants;

import com.nokia.tools.media.font.FontUtil;
import com.nokia.tools.media.font.IFontResource;
import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.widget.SFont;

public abstract class BaseText extends ThemeElement {
	private static final int IMAGE_TO_TEXT_SPACING = 5;

	private String text;

	private RenderedImage image;

	private int alignment;

	private int imageAlignment = SwingConstants.LEFT;

	private int baseline;

	private int marginLeft;

	private int marginRight;

	private int marginTop;

	private int marginBottom;

	private IFontResource fontResource;

	private Color outlineColor;

	/**
	 * @return Returns the text.
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text The text to set.
	 */
	public void setText(String text) {
		this.text = text;
		updateBounds();
	}

	/**
	 * @param text The text to set.
	 */
	public void setImage(String path) {
		if (path == null || !new File(path).exists()) {
			this.image = null;
			return;
		}

		Rectangle2D rect = getFont().getStringBounds(
				getText(),
				new FontRenderContext(AffineTransform
						.getTranslateInstance(0, 0), true, false));
		Rectangle bounds = getBounds();
		bounds.height = Math.max(bounds.height, (int) rect.getHeight());

		try {
			this.image = CoreImage.create().load(new File(path), bounds.height,
					bounds.height, CoreImage.SCALE_DOWN_TO_FIT).getAwt();
		} catch (Exception e) {
			e.printStackTrace();
			this.image = null;
		}
		updateBounds();
	}

	/**
	 * @param alignment The alignment to set.
	 */
	public void setImageAlignment(int imageAlignment) {
		this.imageAlignment = imageAlignment;
	}

	/**
	 * @return the outlineColor
	 */
	public Color getOutlineColor() {
		return outlineColor;
	}

	/**
	 * @param outlineColor the outlineColor to set
	 */
	public void setOutlineColor(Color outlineColor) {
		this.outlineColor = outlineColor;
	}

	/**
	 * @return Returns the baseline.
	 */
	public int getBaseline() {
		return baseline;
	}

	/**
	 * @param baseline The baseline to set.
	 */
	public void setBaseline(int baseline) {
		this.baseline = baseline;
	}

	/**
	 * @return Returns the marginBottom.
	 */
	public int getMarginBottom() {
		return marginBottom;
	}

	/**
	 * @param marginBottom The marginBottom to set.
	 */
	public void setMarginBottom(int marginBottom) {
		this.marginBottom = marginBottom;
	}

	/**
	 * @return Returns the marginLeft.
	 */
	public int getMarginLeft() {
		return marginLeft;
	}

	/**
	 * @param marginLeft The marginLeft to set.
	 */
	public void setMarginLeft(int marginLeft) {
		this.marginLeft = marginLeft;
	}

	/**
	 * @return Returns the marginRight.
	 */
	public int getMarginRight() {
		return marginRight;
	}

	/**
	 * @param marginRight The marginRight to set.
	 */
	public void setMarginRight(int marginRight) {
		this.marginRight = marginRight;
	}

	/**
	 * @return Returns the marginTop.
	 */
	public int getMarginTop() {
		return marginTop;
	}

	/**
	 * @param marginTop The marginTop to set.
	 */
	public void setMarginTop(int marginTop) {
		this.marginTop = marginTop;
	}

	/**
	 * @return Returns the alignment.
	 */
	public int getAlignment() {
		return alignment;
	}

	/**
	 * @param alignment The alignment to set.
	 */
	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}

	/**
	 * @param font The sfont to set.
	 */
	public void setFont(SFont font) {
		fontResource = font.toFont();
		updateBounds();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#setFont(java.awt.Font)
	 */
	public void setFont(Font font) {
		super.setFont(font);
		if (font == null) {
			font = getFont();
		}
		fontResource = FontUtil.getFontResource(font);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.widget.SComponent#paintDefault(java.awt.Graphics)
	 */
	@Override
	protected void paintDefault(Graphics g) {
		Graphics2D tg2d = (Graphics2D) g;

		tg2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// int width = getBounds().width;

		// width = width - (marginRight + marginLeft);

		if (fontResource == null) {
			fontResource = FontUtil.getFontResource(tg2d.getFont());
		}

		int x = computeJustification(fontResource.stringWidth(tg2d, text));
		int descent = fontResource.getDescent(tg2d);
		int y = marginTop + fontResource.getSize(tg2d) - baseline - descent;
		int fontSize = fontResource.getAscent(tg2d)
				+ fontResource.getDescent(tg2d);
		int diff = Math.max(0, fontSize - getBounds().height);
		y += diff / 2;

		if (outlineColor == null) {
			tg2d.setColor(getForeground());
			fontResource.drawString(tg2d, text, x, y);
		} else {
			FontRenderContext frc = tg2d.getFontRenderContext();
			AttributedString as = new AttributedString(text);
			as.addAttribute(TextAttribute.FONT, fontResource.getFont(), 0, text
					.length());
			AttributedCharacterIterator aci = as.getIterator();
			TextLayout tl = new TextLayout(aci, frc);
			Shape sha = tl.getOutline(AffineTransform
					.getTranslateInstance(x, y));
			tg2d.setColor(outlineColor);
			tg2d.setStroke(new BasicStroke(1.2f));
			tg2d.draw(sha);
			tg2d.setColor(getForeground());
			tg2d.fill(sha);
		}

		if (image != null) {
			if (imageAlignment == SwingConstants.LEFT) {
				x = x - IMAGE_TO_TEXT_SPACING - image.getWidth();
			} else {
				x = x + fontResource.stringWidth(tg2d, text)
						+ IMAGE_TO_TEXT_SPACING;
			}
			diff = Math.max(0, image.getHeight() - getBounds().height);
			y = diff / 2;
			tg2d.drawRenderedImage(image, AffineTransform.getTranslateInstance(
					x, y));
		}

		tg2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
	}

	/**
	 * Expands the bounds if necessary, the texts shall not be clipped.
	 */
	protected void updateBounds() {
		Rectangle2D rect = getFont().getStringBounds(
				getText(),
				new FontRenderContext(AffineTransform
						.getTranslateInstance(0, 0), true, false));

		int width = (int) rect.getWidth();
		int height = (int) rect.getHeight();

		if (image != null) {
			width += image.getWidth() + IMAGE_TO_TEXT_SPACING;
			height = Math.max(height, image.getHeight());
		}

		Rectangle bounds = getBounds();
		bounds.width = Math.max(bounds.width, (int) width);
		bounds.height = Math.max(bounds.height, (int) height);
		setBounds(bounds);
	}

	public int computeJustification(int textWidth) {
		int result = 0;
		int compWidth = getBounds().width;

		if (alignment == SwingConstants.LEFT) {
			result = 0;

			if (image != null && imageAlignment == SwingConstants.LEFT) {
				result = image.getWidth() + IMAGE_TO_TEXT_SPACING;
			}
		}
		if (textWidth > compWidth) {
			result = 0;

			if (image != null && imageAlignment == SwingConstants.LEFT) {
				result = image.getWidth() + IMAGE_TO_TEXT_SPACING;
			}
		} else if (alignment == SwingConstants.CENTER) {
			if (image != null) {
				compWidth -= image.getWidth() + IMAGE_TO_TEXT_SPACING;
			}
			result = Math.round((compWidth - textWidth) / 2);
		} else if (alignment == SwingConstants.RIGHT) { // Right
			result = (compWidth - textWidth);
			if (image != null && imageAlignment == SwingConstants.RIGHT) {
				result -= image.getWidth() + IMAGE_TO_TEXT_SPACING;
			}
		}
		if (result < 0) {
			result = 0;
		}
		return result;
	}
}
