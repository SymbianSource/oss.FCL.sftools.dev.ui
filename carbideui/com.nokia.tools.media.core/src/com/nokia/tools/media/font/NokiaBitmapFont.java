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

package com.nokia.tools.media.font;

/* java.util */
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.nokia.tools.media.core.Activator;
import com.nokia.tools.resource.util.FileUtils;

/**
 * This class represents Nokia bitmap font.
 * 
 */
public class NokiaBitmapFont implements IFontResource {
	private static final String GLYPH_EXT = ".wbmp";

	// attributes
	private static final String XML_ATTR_CODE = "code";
	private static final String XML_ATTR_WIDTH = "width";
	private static final String XML_ATTR_HEIGHT = "height";
	private static final String XML_ATTR_BASELINE = "baseline";
	private static final String XML_ATTR_OFFSET = "offset";
	private static final String XML_ATTR_SHIFT = "shift";

	// instance variables
	private IntMap glyphMap;
	private char minChar;
	private char maxChar;
	private int maxAscent;
	private int maxDescent;

	private File file;

	/**
	 * Creates the <code>NokiaBitmapFont</code> with the specified name.
	 * 
	 * @param name the font name, such as for example <code>"plain8"</code> or
	 *            <code>"bold8"</code>
	 * @throws IOException if font cannot be loaded
	 */
	protected NokiaBitmapFont(IFontDescriptor descriptor) throws IOException {
		file = FileUtils.getFile(descriptor.getPath());

		InputStream in = null;
		glyphMap = new IntArrayMap();
		try {
			in = descriptor.getPath().openStream();

			// Parse the font.xml file
			XMLReader parser = SAXParserFactory.newInstance().newSAXParser()
					.getXMLReader();
			Loader docHandler = new Loader();
			parser.setContentHandler(docHandler);
			parser.setErrorHandler(docHandler);
			parser.parse(new InputSource(in));
		} catch (IOException x) {
			throw x;
		} catch (Throwable y) {
			Activator.warn(y);
		} finally {
			FileUtils.close(in);
		}
	}

	/**
	 * Gets the free space between two adjacent lines of text.
	 * 
	 * @return the free space between two adjacent lines of text.
	 */
	public int getLeading() {
		return 2;
	}

	/**
	 * Gets the minimum character code for this font.
	 * 
	 * @return the minimum character code for this font
	 */
	public char getMinChar() {
		return minChar;
	}

	/**
	 * Gets the maximum character code for this font.
	 * 
	 * @return the maximum character code for this font
	 */
	public char getMaxChar() {
		return minChar;
	}

	/**
	 * Gets the number of glyphs in this font.
	 * 
	 * @return the number of glyphs in this font
	 */
	public int getCharCount() {
		return glyphMap.size();
	}

	/**
	 * Gets all characters in this font
	 * 
	 * @return all characters in this font
	 */
	public char[] getAllChars() {
		int[] keys = glyphMap.keys();
		int n = keys.length;
		char[] chars = new char[n];
		for (int i = 0; i < n; i++) {
			chars[i] = (char) keys[i];
		}
		return chars;
	}

	/**
	 * Determines the maximum ascent of the font described by this
	 * <code>NokiaBitmapFont</code> object. No character extends further above
	 * the font's baseline than this height.
	 * 
	 * @return the maximum ascent of any character in this font
	 */
	public int getMaxAscent() {
		return maxAscent;
	}

	/**
	 * Determines the maximum descent of the font described by this
	 * <code>FontMetrics</code> object. No character extends further below the
	 * font's baseline than this height.
	 * 
	 * @return the maximum descent of any character in this font
	 */
	public int getMaxDescent() {
		return maxDescent;
	}

	/**
	 * Gets the glyph for the specified character.
	 * 
	 * @param c the character to lookup the glyph for
	 * @return the glyph for the specified character, <code>null</code> if not
	 *         available.
	 */
	private Glyph getGlyph(char c) {
		Glyph g = (Glyph) glyphMap.get(((int) c) & 0xffff);
		return g;
	}

	/**
	 * Gets bounds of a single character.
	 * 
	 * @param c the character to get the bounds for
	 * @param bounds the object to store the bounds. If this parameter is not
	 *            <code>null</code>, it will be the return value. If it is
	 *            <code>null</code>, a new rectangle is allocated and
	 *            returned.
	 * @return the character bounds. If there is not glyph for this character,
	 *         returns an emptry rectangle.
	 */
	public Rectangle getCharBounds(char c, Rectangle bounds) {
		if (bounds == null)
			bounds = new Rectangle();
		Glyph glyph = getGlyph(c);
		if (glyph != null) {
			bounds.x = glyph.getOffset();
			bounds.y = maxAscent - glyph.getBaseLine();
			bounds.width = glyph.getWidth();
			bounds.height = glyph.getHeight();
		} else {
			bounds.setBounds(0, 0, 0, 0);
		}
		return bounds;
	}

	/**
	 * Returns the bounds of the specified <code>String</code>. The bounds is
	 * used to layout the <code>String</code>.
	 * 
	 * @param str the specified <code>String</code>
	 * 
	 * @return a {@link Rectangle} that is the bounding box of the specified
	 *         <code>String</code>.
	 */
	public Rectangle getStringBounds(String str) {
		return getStringBounds(str, 0, str.length(), null);
	}

	/**
	 * Returns the bounds of the specified <code>String</code>. The bounds is
	 * used to layout the <code>String</code>.
	 * 
	 * @param str the specified <code>String</code>
	 * @param bounds the object to store the bounds. If this parameter is not
	 *            <code>null</code>, it will be the return value. If it is
	 *            <code>null</code>, a new rectangle is allocated and
	 *            returned.
	 * 
	 * @return a {@link Rectangle} that is the bounding box of the specified
	 *         <code>String</code>.
	 */
	public Rectangle getStringBounds(String str, Rectangle bounds) {
		return getStringBounds(str, 0, str.length(), bounds);
	}

	/**
	 * Returns the bounds of the substring of the specified <code>String</code>.
	 * The bounds is used to layout the <code>String</code>.
	 * 
	 * @param str the specified <code>String</code>
	 * @param off the offset of the first character
	 * @param len the number of characters
	 * @param bounds the object to store the bounds. If this parameter is not
	 *            <code>null</code>, it will be the return value. If it is
	 *            <code>null</code>, a new rectangle is allocated and
	 *            returned.
	 * 
	 * @return a {@link Rectangle} that is the bounding box of the specified
	 *         <code>String</code>.
	 */
	public Rectangle getStringBounds(String str, int off, int len,
			Rectangle bounds) {
		char[] chars = null;
		if (len > 0) {
			chars = new char[len];
			str.getChars(off, off + len, chars, 0);
		}
		return getStringBounds(chars, 0, len, bounds);
	}

	/**
	 * Returns the bounds of the specified character string. The bounds is used
	 * to layout the text.
	 * 
	 * @param chars the string characters
	 * @param bounds the object to store the bounds. If this parameter is not
	 *            <code>null</code>, it will be the return value. If it is
	 *            <code>null</code>, a new rectangle is allocated and
	 *            returned.
	 * 
	 * @return a {@link Rectangle} that is the bounding box of the specified
	 *         <code>String</code>.
	 */
	public Rectangle getStringBounds(char[] chars, Rectangle bounds) {
		return getStringBounds(chars, 0, chars.length, bounds);
	}

	/**
	 * Returns the bounds of the specified character string. The bounds is used
	 * to layout the text.
	 * 
	 * @param chars the string characters
	 * @param off the offset of the first character
	 * @param len the number of characters
	 * @param bounds the object to store the bounds. If this parameter is not
	 *            <code>null</code>, it will be the return value. If it is
	 *            <code>null</code>, a new rectangle is allocated and
	 *            returned.
	 * 
	 * @return a {@link Rectangle} that is the bounding box of the specified
	 *         <code>String</code>.
	 */
	public Rectangle getStringBounds(char[] chars, int off, int len,
			Rectangle bounds) {
		if (bounds == null) {
			bounds = new Rectangle(0, 0, 0, 0);
		} else {
			bounds.setBounds(0, 0, 0, 0);
		}

		if (len > 0) {
			int xmin = 0; // inclusive
			int ymin = 0; // inclusive
			int xmax = 0; // exclusive
			int ymax = 0; // exclusive
			int x = 0;

			// the first character
			char c = chars[off];
			Glyph firstGlyph = getGlyph(c);
			if (firstGlyph != null) {
				xmin = firstGlyph.getOffset();
				ymin = (-firstGlyph.getBaseLine());
				xmax = xmin + firstGlyph.getWidth();
				ymax = ymin + firstGlyph.getHeight();
				x = firstGlyph.getShift();
			}

			// all other characters
			Glyph glyph = firstGlyph;
			for (int i = 1; i < len; i++) {
				c = chars[off + i];
				glyph = getGlyph(c);
				if (glyph != null) {
					int x1 = x + glyph.getOffset();
					int y1 = (-glyph.getBaseLine());
					int x2 = x1 + glyph.getWidth();
					int y2 = y1 + glyph.getHeight();
					if (x1 < xmin)
						xmin = x1;
					if (y1 < ymin)
						ymin = y1;
					if (x2 > xmax)
						xmax = x2;
					if (y2 > ymax)
						ymax = y2;
					x += glyph.getShift();
				}
			}

			
			if (firstGlyph != null && firstGlyph.isEmptyFirstColumn()) {
				xmax--;
			}
			if (glyph != null && glyph.isEmptyLastColumn()) {
				xmax--;
			}

			// fill in the result
			bounds.x = xmin;
			bounds.y = ymin;
			bounds.width = xmax - xmin;
			bounds.height = ymax - ymin;
		}

		return bounds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.font.IFontResource#getAscent(java.awt.Graphics)
	 */
	public int getAscent(Graphics g) {
		return getMaxAscent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.font.IFontResource#getDescent(java.awt.Graphics)
	 */
	public int getDescent(Graphics g) {
		return getMaxDescent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.font.IFontResource#getSize(java.awt.Graphics)
	 */
	public int getSize(Graphics g) {
		return getAscent(g) + getDescent(g);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.font.IFontResource#stringWidth(java.awt.Graphics,
	 *      java.lang.String)
	 */
	public int stringWidth(Graphics g, String text) {
		return getStringBounds(text).width;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.font.IFontResource#getFont()
	 */
	public Font getFont() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.font.IFontResource#drawString(java.awt.Graphics,
	 *      java.lang.String, int, int)
	 */
	public void drawString(Graphics g, String s, int x0, int y0) {
		drawString(g, s, 0, s.length(), x0, y0);
	}

	/**
	 * Draws the text given by the specified string, using this graphics
	 * context's current color. The baseline of the leftmost character is at
	 * position (<i>x</i>,&nbsp;<i>y</i>) in this graphics context's
	 * coordinate system.
	 * 
	 * @param g the graphics to paint to
	 * @param s the string to be drawn.
	 * @param off the offset of the first character to draw.
	 * @param len the number of characters to draw.
	 * @param x0 the <i>x</i> coordinate.
	 * @param y0 the <i>y</i> coordinate.
	 */
	public void drawString(Graphics g, String s, int off, int len, int x0,
			int y0) {
		int x = x0;
		Color color = g.getColor();
		int opaque = new Color(color.getRed(), color.getGreen(), color
				.getBlue(), 0xff).getRGB();
		boolean firstGlyph = true;
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i + off);
			Glyph glyph = getGlyph(c);
			if (glyph != null) {
				if (firstGlyph) {
					firstGlyph = false;
					if (glyph.isEmptyFirstColumn()) {
						x--;
					}
				}
				int y = y0 - glyph.getBaseLine();
				int[] pixels = glyph.getPixels(opaque);
				putDirectPixels(g, x, y, pixels, glyph.getWidth());
				x += glyph.getShift();
			}
		}
	}

	/**
	 * Draw an array of pixels at a location. The pixels are direct color values
	 * in the default sRGB space.
	 * 
	 * @param x0
	 * @param y0
	 * @param data
	 * @param scan
	 */
	public void putDirectPixels(Graphics g, int x0, int y0, int[] data, int scan) {
		int h = data.length / scan;
		int w = scan;
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		bi.setRGB(0, 0, w, h, data, 0, w);
		g.drawImage(bi, x0, y0, null, null);
	}

	/**
	 * Glyph information and shape. Note that glyph icon is being loaded the
	 * first time ist's requested. Do it only when it's necessary.
	 */
	private class Glyph {
		static final int TRANSPARENT = 0x00FFFFFF;

		private char ch;
		private int color;
		private int width;
		private int height;
		private int baseline;
		private int offset;
		private int shift;
		private int[] pixels;
		private int[] pixels2;

		Glyph(char ch, int w, int h, int base, int offset, int shift) {
			this.ch = ch;
			this.width = w;
			this.height = h;
			this.baseline = base;
			this.offset = offset;
			this.shift = shift;
		}

		char getChar() {
			return ch;
		}

		int getWidth() {
			return width;
		}

		int getHeight() {
			return height;
		}

		int getBaseLine() {
			return baseline;
		}

		int getOffset() {
			return offset;
		}

		int getShift() {
			return shift;
		}

		int[] getPixels(int c) {
			if (pixels2 == null || color != c) {
				int[] p = getPixels();
				int n = p.length;
				pixels2 = new int[n];
				for (int i = 0; i < n; i++) {
					pixels2[i] = ((p[i] == 0) ? c : TRANSPARENT);
				}
				color = c;
			}
			return pixels2;
		}

		int[] getPixels() {
			if (pixels == null) {
				int code = ((int) ch) & 0xffff;
				String hexCode = Integer.toHexString(code).toUpperCase();
				switch (hexCode.length()) {
				case 1:
					hexCode = "000" + hexCode;
					break;
				case 2:
					hexCode = "00" + hexCode;
					break;
				case 3:
					hexCode = "0" + hexCode;
					break;
				}
				File path = new File(file.getParentFile(), hexCode + GLYPH_EXT);
				InputStream in = null;

				try {
					in = new FileInputStream(path);
					WBMPImage bmp = new WBMPImage(in);
					if (bmp.getWidth() == width && bmp.getHeight() == height) {
						pixels = bmp.getPixels();
					} else {
						Activator.warn("bitmap doesn't match the glyph ("
								+ path + ")");
					}
				} catch (Exception x) {
					Activator.warn(x);
				} finally {
					FileUtils.close(in);
				}
				if (pixels == null)
					pixels = new int[0];
			}
			return pixels;
		}

		boolean isEmptyFirstColumn() {
			getPixels();
			boolean empty = true;
			for (int i = 0; i < height && empty; i++) {
				empty = (pixels[i * width] == 1);
			}
			return empty;
		}

		boolean isEmptyLastColumn() {
			getPixels();
			boolean empty = true;
			for (int i = 0; i < height && empty; i++) {
				empty = (pixels[(i + 1) * width - 1] == 1);
			}
			return empty;
		}
	}

	/**
	 * SAX event handler
	 */
	private class Loader extends DefaultHandler {
		private int depth;
		private int defaultWidth;
		private int defaultHeight;
		private int defaultBaseline;

		/**
		 * Receives notification of the beginning of an element.
		 * 
		 * @param uri the namespace URI.
		 * @param lname the local name (without prefix), or the empty string if
		 *            namespace processing is not being performed.
		 * @param qname the qualified name (with prefix), or the empty string if
		 *            qualified names are not available.
		 * @param a the attributes attached to the element. If there are no
		 *            attributes, it shall be an empty Attributes object.
		 */
		public void startElement(String uri, String lname, String qname,
				Attributes a) {
			// not checking the tag for performance reasons...
			// relying on the assumption that the file structure is correct
			depth++;
			try {
				if (depth == 1) {
					// default values of width, height and baseline attributes
					String widthStr = a.getValue(XML_ATTR_WIDTH);
					String heightStr = a.getValue(XML_ATTR_HEIGHT);
					String baseStr = a.getValue(XML_ATTR_BASELINE);
					if (widthStr != null) {
						defaultWidth = Integer.parseInt(widthStr);
					}
					if (heightStr != null) {
						defaultHeight = Integer.parseInt(heightStr);
					}
					if (baseStr != null) {
						defaultBaseline = Integer.parseInt(baseStr);
					}
				} else if (depth == 2) {
					// required attributes
					String codeStr = a.getValue(XML_ATTR_CODE);
					char ch = (char) Integer.parseInt(codeStr, 16);

					// optional attributes
					String widthStr = a.getValue(XML_ATTR_WIDTH);
					String heightStr = a.getValue(XML_ATTR_HEIGHT);
					String baseStr = a.getValue(XML_ATTR_BASELINE);
					String offStr = a.getValue(XML_ATTR_OFFSET);
					String shiftStr = a.getValue(XML_ATTR_SHIFT);

					int off = 0;
					int w = defaultWidth;
					int h = defaultHeight;
					int base = defaultBaseline;
					if (offStr != null)
						off = Integer.parseInt(offStr);
					if (baseStr != null)
						base = Integer.parseInt(baseStr);
					if (widthStr != null)
						w = Integer.parseInt(widthStr);
					if (heightStr != null)
						h = Integer.parseInt(heightStr);

					int shift = w;
					if (shiftStr != null)
						shift = Integer.parseInt(shiftStr);

					// create the glyph
					Glyph glyph = new Glyph(ch, w, h, base, off, shift);
					glyphMap.put(((int) ch) & 0xffff, glyph);

					// update the font statistics
					if (glyphMap.isEmpty()) {
						minChar = ch;
						maxChar = ch;
					} else {
						if (ch < minChar)
							minChar = ch;
						if (ch > maxChar)
							maxChar = ch;
					}
					int descent = (h - base);
					if (descent > maxDescent)
						maxDescent = descent;
					if (base > maxAscent)
						maxAscent = base;
				}
			} catch (Exception x) {
				Activator.warn(x);
			}
		}

		/**
		 * Receives notification of the end of an element.
		 * 
		 * @param uri the namespace URI
		 * @param lname the local name (without prefix), or the empty string if
		 *            namespace processing is not being performed.
		 * @param qname the qualified name (with prefix), or the empty string if
		 *            qualified names are not available.
		 */
		public void endElement(String uri, String lname, String qname) {
			depth--;
		}
	}
}
