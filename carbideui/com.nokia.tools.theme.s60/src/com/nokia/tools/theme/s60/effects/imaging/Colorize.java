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
package com.nokia.tools.theme.s60.effects.imaging;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.FileWriter;
import java.util.HashMap;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.platform.theme.BitmapProperties;
import com.nokia.tools.platform.theme.ThemeConstants;

public class Colorize {
	public static final String TOOL_TYPE = "TOOL_TYPE";
	public static final String COLOUR_TYPE_COLOURLOOKUP = "COLOURLOOKUP";

	
	private int width = 0;
	private int height = 0;

	private BufferedImage b;
	private WritableRaster w;

	public void create(int x, int y, Color c) {

		int red = c.getRed();
		int green = c.getGreen();
		int blue = c.getBlue();
		int alpha = c.getAlpha();

		int co[] = new int[] { red, green, blue, alpha };

		w.setPixel(x, y, co);

	}

	public RenderedImage getImage() {
		return b;
	}

	private int quantize(int colour, int levels) {

		
		double noOfStates = (double) levels;
		double step = 255.0 / (noOfStates - 1);

		int upperLevel = (int) (Math.ceil(colour / step) * step);
		int lowerLevel = (int) (Math.floor(colour / step) * step);

		int threshold = (upperLevel + lowerLevel) / 2;

		if (colour >= threshold) {

			return upperLevel;
		} else {

			return lowerLevel;
		}
	}

	FileWriter f;

	private void adjustPixel(int re, int ge, int be, int x, int y,
			float factor, BufferedImage result) {
		

		int sample = b.getRGB(x, y);
		Color sam_col = new Color(sample);

		int r1 = sam_col.getRed();
		int g1 = sam_col.getGreen();
		int b1 = sam_col.getBlue();

		

		r1 += Math.round(re * factor);
		g1 += Math.round(ge * factor);
		b1 += Math.round(be * factor);

		if (r1 < 0)
			r1 = 0;
		if (g1 < 0)
			g1 = 0;
		if (b1 < 0)
			b1 = 0;

		if (r1 > 255)
			r1 = 255;
		if (g1 > 255)
			g1 = 255;
		if (b1 > 255)
			b1 = 255;

		Color res_col = new Color(r1, g1, b1);
		int ressample = res_col.getRGB();
		
		result.setRGB(x, y, ressample);

	}

	public void ditherfor16bit() {

		BufferedImage output = b;

		int[] level16 = new int[] { 32, 64, 32 };

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				floydSteinberg(x, y, level16, output);
			}
		}

		b = output;

	}

	public void ditherfor8bit() {

		BufferedImage output = b;

		int[] level8 = new int[] { 6, 6, 6 };
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				floydSteinberg(x, y, level8, output);
			}
		}

		b = output;
	}

	public void ditherforPaletteIndex() {

		BufferedImage output = b;

		
		int[] levelgrey = new int[] { 12, 12, 12 };

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				floydSteinberg(x, y, levelgrey, output);
			}
		}

		b = output;
	}

	public void quantizeTo16BitImage() {

		int[] level16 = new int[] { 32, 64, 32 };

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {

				
				int sample = b.getRGB(x, y);
				Color sam_col = new Color(sample);

				int r1 = sam_col.getRed();
				int g1 = sam_col.getGreen();
				int b1 = sam_col.getBlue();

				int r2 = quantize(r1, level16[0]);
				int g2 = quantize(g1, level16[1]);
				int b2 = quantize(b1, level16[2]);

				

				Color res_col = new Color(r2, g2, b2);
				int ressample = res_col.getRGB();
				b.setRGB(x, y, ressample);
			}
		}
	}

	private void floydSteinberg(int x, int y, int[] level, BufferedImage result) {
		

		int sample = b.getRGB(x, y);
		Color sam_col = new Color(sample);

		int rv = sam_col.getRed();
		int gv = sam_col.getGreen();
		int bv = sam_col.getBlue();

		int rq = quantize(rv, level[0]);
		int gq = quantize(gv, level[1]);
		int bq = quantize(bv, level[2]);

		int re = rv - rq;
		int ge = gv - gq;
		int be = bv - bq;

		if (y < height - 1) {
			adjustPixel(re, ge, be, x, y + 1, (float) (5.0 / 16.0), result);
		}

		// assuming raster type as normal
		{
			// right pixel
			if (x < width - 1) {
				adjustPixel(re, ge, be, x + 1, y, (float) (7.0 / 16.0), result);

				// bottom-right pixel
				if (y < height - 1) {
					adjustPixel(re, ge, be, x + 1, y + 1, (float) (1.0 / 16.0),
							result);
				}
			}

			// bottom-left pixel
			if (x > 0 && y < height - 1) {
				adjustPixel(re, ge, be, x - 1, y + 1, (float) (3.0 / 16.0),
						result);
			}
		}

	}

	public void createBitmap() {
		b = createPaletteBitmap(b);
	}

	public void createWebSafeBitmap() {
		b = createWebSafeBitmap(b);
	}

	public static RenderedImage colourNonWhite(RenderedImage image, Color color) {

		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();

		if ((red == 255) && (green == 255) && (blue == 255))
			blue = 254;

		int whiteValue = Color.WHITE.getRGB();
		int newColourValue = (new Color(red, green, blue)).getRGB();

		BufferedImage bim = CoreImage.getBufferedImage(image);

		int bimWid = bim.getWidth();
		int bimHgt = bim.getHeight();

		for (int i = 0; i < bimWid; i++) {
			for (int j = 0; j < bimHgt; j++) {
				int bim_col = bim.getRGB(i, j);
				if (bim_col != whiteValue)
					bim.setRGB(i, j, newColourValue);
			}
		}

		return bim;
	}

	/**
	 * Creates a bitmap that has palette identification set
	 */
	public static BufferedImage createPaletteBitmap(BufferedImage src) {

		int width = src.getWidth();
		int height = src.getHeight();

		IndexColorModel bitmapPaletteModel = ColourPalette.BITMAP_PALETTE;
		BufferedImage pim = new BufferedImage(width, height,
				BufferedImage.TYPE_BYTE_INDEXED, bitmapPaletteModel);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int src_rgb = src.getRGB(i, j);
				pim.setRGB(i, j, src_rgb);
			}
		}

		return pim;
	}

	/**
	 * Creates a bitmap that has only web safe colours
	 */
	public static BufferedImage createWebSafeBitmap(BufferedImage src) {

		int width = src.getWidth();
		int height = src.getHeight();

		IndexColorModel webSafePalette = ColourPalette.WEB_SAFE_PALETTE;
		BufferedImage pim = new BufferedImage(width, height,
				BufferedImage.TYPE_BYTE_INDEXED, webSafePalette);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int src_rgb = src.getRGB(i, j);
				pim.setRGB(i, j, src_rgb);
			}
		}

		return pim;
	}

	/**
	 * This method initiates the operation that user selected in the ToolBox.
	 */
	public void initOperation() {

	}

	/**
	 * This method resets the operation table (i.e HashMap) to the original
	 * state.
	 */
	public void resetOperation() {
	}

	/**
	 * This method is a generic method which will be implemented for generic
	 * operation(which generally not associated with mouse operations) like
	 * Tile,Brightness etc.
	 */

	public HashMap manipulate(HashMap<Object, Object> map) {

		RenderedImage p = (RenderedImage) map
				.get(ThemeConstants.RENDERED_IMAGE);
		if (p == null) {
			return map;
		}

		width = p.getWidth();
		height = p.getHeight();

		// extract mask
		CoreImage ci = CoreImage.create(p);
		CoreImage _mask = ci.getNumBands() == 4 ? ci.copy().extractMask()
				: null;

		Boolean ditherStatus = (Boolean) map
				.get(BitmapProperties.DITHER_SELECTED);
		Boolean colorizeStatus = (Boolean) map
				.get(BitmapProperties.COLORIZE_SELECTED);
		String option = (String) map.get(BitmapProperties.OPTIMIZE_SELECTION);
		Color color = (Color) map.get(BitmapProperties.COLOR);
		String colourToolType = (String) map.get(TOOL_TYPE);

		

		b = ci.getBufferedImage();

		w = b.getRaster();

	
		if (colourToolType != null
				&& colourToolType.equalsIgnoreCase(COLOUR_TYPE_COLOURLOOKUP)) {
			b = CoreImage.getBufferedImage(colourNonWhite(b, color));

		} else if (option.equals("high")) {

		

			if (colorizeStatus.booleanValue()) {
				
			}

			if (ditherStatus.booleanValue()) {
				ditherfor16bit();
			}

			quantizeTo16BitImage();

		} else if (option.equals("palette dependant")) {
			
			if (colorizeStatus.booleanValue())
				

				if (ditherStatus.booleanValue()) {
					ditherforPaletteIndex();
				}

			
			createBitmap();

		} else if (option.equals("low")) {
			

			if (colorizeStatus.booleanValue()) {

				

			}

			if (ditherStatus.booleanValue()) {
				ditherfor8bit();
			}

			createWebSafeBitmap();

		}

		p = b;

		if (colorizeStatus) {
			p = CoreImage.create().init(p.getWidth(), p.getHeight(), color)
					.composite(CoreImage.create(p), 160).getAwt();
		}

		RenderedImage returnImage = p;

		
		if (_mask != null) {
			returnImage = CoreImage.create(returnImage).applyMask(_mask, true)
					.getAwt();
		}

		map.put("RETURNIMAGE", returnImage);

		Integer i = (Integer) map.get("IMAGEX");
		map.put("RETURNIMAGEX", i);

		i = (Integer) map.get("IMAGEY");
		map.put("RETURNIMAGEY", i);

		return map;

	}

}