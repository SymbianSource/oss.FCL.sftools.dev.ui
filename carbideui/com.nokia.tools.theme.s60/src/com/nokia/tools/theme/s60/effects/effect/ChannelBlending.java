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
/*
 * To change the template for this generated file
 * go to Window - Preferences - Java - Code Style - Code Templates
 */
package com.nokia.tools.theme.s60.effects.effect;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.platform.theme.EffectConstants;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.LayerEffect;
import com.nokia.tools.platform.theme.ParameterModel;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.s60.editing.EditableImageLayer;
import com.nokia.tools.theme.s60.effects.EffectObject;
import com.nokia.tools.theme.s60.effects.EffectObjectUtils;
import com.nokia.tools.theme.s60.effects.EffectParameter;
import com.nokia.tools.theme.s60.effects.ImageProcessor;
import com.nokia.tools.theme.s60.siscreation.MorphingItemDefinition;

/*
 * Changes marked #change are in order to remove dependecy on
 * layer/effect dialog ui code. Fixed broken masking of SRC2. Now working all
 * but 'Darken' mode.
 */

/**
 * To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
public class ChannelBlending implements ImageProcessor {

	private final String effectUID = "0x10204AE0";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.effects.ImageProcessor#doProcessing(java.awt.image.RenderedImage,
	 *      java.util.HashMap)
	 */
	public RenderedImage doProcessing(RenderedImage src, HashMap map) {

		try {
			String mode = (String) map.get(EffectConstants.BLENDMODE);
			EffectObject eObj = (EffectObject) map
					.get(EffectConstants.EFFECTOBJECT);

			if (eObj == null) {
				eObj = EffectObject.getEffect(EffectConstants.CHANNELBLENDING);
			}
			EffectParameter blendMode = eObj
					.getParameter(EffectConstants.ATTR_BLEND);
			if (blendMode != null) {
				int value = blendMode.getValueAsInt();
				mode = EffectObjectUtils.getValueLiteral(
						EffectConstants.CHANNELBLENDING, blendMode.getName(),
						value);
			}

			ImageLayer iml = null;
			if (map.get(ImageProcessor.ATTR_IMAGELAYER) != null) {
				iml = (ImageLayer) map.get(ImageProcessor.ATTR_IMAGELAYER);

			}
			RenderedImage src1 = (RenderedImage) map
					.get(EffectConstants.SOURCE1);
			RenderedImage src2 = (RenderedImage) map
					.get(EffectConstants.SOURCE2);

			if (src1 == null)
				return src2;
			if (src2 == null)
				return src1;

			
			int x = 0, y = 0;
			if (map.get(EffectConstants.X) != null)
				x = ((Integer) map.get(EffectConstants.X)).intValue();
			if (map.get(EffectConstants.Y) != null)
				y = ((Integer) map.get(EffectConstants.Y)).intValue();

			EditableImageLayer lData = (EditableImageLayer) map
					.get(ImageProcessor.ATTR_LDATA);

			RenderedImage image = null;

			if (EffectConstants.MULTIPLY.equalsIgnoreCase(mode))
				image = Multiply(src1, src2, x, y);
			else if (EffectConstants.DODGE.equalsIgnoreCase(mode))
				image = Dodge(src1, src2, x, y);
			else if (EffectConstants.BURN.equalsIgnoreCase(mode))
				image = Burn(src1, src2, x, y);
			else if (EffectConstants.DIFFERENCE.equalsIgnoreCase(mode))
				image = Difference(src1, src2, x, y);
			else if (EffectConstants.LIGHTEN.equalsIgnoreCase(mode))
				image = Lighten(src1, src2, x, y);
			else if (EffectConstants.DARKEN.equalsIgnoreCase(mode))
				image = CoreImage.create(src1).darken(CoreImage.create(src2),
						x, y).getAwt();
			else if (EffectConstants.HARDLIGHT.equalsIgnoreCase(mode))
				image = HardLight(src1, src2, x, y);
			else if (EffectConstants.SOFTLIGHT.equalsIgnoreCase(mode))
				image = SoftLight(src1, src2, x, y);
			else if (EffectConstants.SCREEN.equalsIgnoreCase(mode))
				image = Screen(src1, src2, x, y);
			else if (EffectConstants.OVERLAY.equalsIgnoreCase(mode))
				image = OverLay(src1, src2, x, y);
			else {
				image = Normal(src1, src2);
			}

			if (lData != null || iml != null
					|| map.get(EffectConstants.EFFECTOBJECT) != null) {
				String blendfactor = "128";
				LayerEffect le = null;
				EffectObject e = null;
				if (map.get(EffectConstants.EFFECTOBJECT) instanceof LayerEffect)
					le = (LayerEffect) map.get(EffectConstants.EFFECTOBJECT);
				if (map.get(EffectConstants.EFFECTOBJECT) instanceof EffectObject)
					e = (EffectObject) map.get(EffectConstants.EFFECTOBJECT);

				if (e != null)
					blendfactor = (String) e
							.getAttributeValue(EffectConstants.BLENDFACTOR);
				if (le != null) {
					blendfactor = le.getAttribute(EffectConstants.BLENDFACTOR);
				}

				try {
					
					if (map.get(ThemeTag.DEFAULT) != null)
						blendfactor = "255";
				} catch (Exception ee) {
				}

				if (image != null && blendfactor != null) {
					
					image = CoreImage.create(src1).composite(
							CoreImage.create(image),
							Integer.parseInt(blendfactor)).getAwt();
				}
			}
			return image;
		} catch (Exception e) {
			e.printStackTrace();
			return src;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.effects.ImageProcessor#getOutputParams(java.util.HashMap,
	 *      java.util.HashMap)
	 */
	public HashMap<Object, Object> getOutputParams(Map uiMap, Map attrMap,
			String type) {

		EffectObject eObj = EffectObject
				.getEffect(EffectConstants.CHANNELBLENDING);

		HashMap<Object, Object> eMap = eObj.getOutputAttributeMap();
		Set set = eMap.keySet();
		Iterator iter = set.iterator();
		// String valueRef=null;
		while (iter.hasNext()) {
			String name = (String) iter.next();
			if (uiMap != null) {
				HashMap<Object, Object> map = (HashMap<Object, Object>) eMap
						.get(name);
				if (uiMap.get(name) instanceof String) {
					if (map != null)
						map.put(EffectConstants.ATTR_VALUE, (String) uiMap
								.get(name));
				} else {

					if (name.equalsIgnoreCase(EffectConstants.ATTR_BLEND)
							|| name
									.equalsIgnoreCase(EffectConstants.BLENDFACTOR)) {
						ParameterModel p = (ParameterModel) uiMap.get(name);
						if (p != null) {
							if (p.getAttribute(ThemeTag.ELEMENT_VALUEMODEL_REF) != null
									&& type.equals(ANIMATION)) {
								map
										.put(
												ThemeTag.ELEMENT_VALUEMODEL_REF,
												p
														.getAttribute(ThemeTag.ELEMENT_VALUEMODEL_REF));
								if (p.getValue(name) != null)
									map.put(EffectConstants.ATTR_DEFAULTVALUE,
											p.getValue(name));
							} else
								map.put(EffectConstants.ATTR_VALUE, p
										.getValue(name));
						}
					}
				}

			}
		}
		return eMap;
	}

	public HashMap<Object, Object> getEffectParameters(Map uiMap, Map attrMap,
			String type) {
		return getOutputParams(uiMap, attrMap, type);
	}

	public HashMap<String, String> getEffectProperties() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("INPUTA", "RGB");
		map.put("INPUTB", "RGB");
		map.put("OUTPUT", "RGB");
		return map;
	}

	private static CoreImage prepareSrc1(RenderedImage src1) {
		CoreImage srcImage1 = CoreImage.create(src1);
		if (srcImage1.getNumBands() < 3)
			srcImage1.convertToThreeBand();
		return srcImage1;
	}

	private static CoreImage prepareSrc2(RenderedImage src2, int x, int y) {
		CoreImage srcImage2 = CoreImage.create(src2);
		if (srcImage2.getNumBands() < 3)
			srcImage2.convertToThreeBand();
		if ((x != 0) || (y != 0))
			srcImage2.relocate(x, y);
		return srcImage2;
	}

	public static RenderedImage Burn(RenderedImage src1, RenderedImage src2,
			int x, int y) {

		if ((src1 == null))
			return src2;
		if ((src2 == null))
			return src1;

		CoreImage srcImage1 = prepareSrc1(src1);
		CoreImage srcImage2 = prepareSrc2(src2, x, y);

		int numBands1 = srcImage1.getNumBands();
		int numBands2 = srcImage2.getNumBands();

		BufferedImage buf1 = srcImage1.getBufferedImage();
		BufferedImage buf2 = srcImage2.getBufferedImage();

		src1 = srcImage1.getAwt();
		src2 = srcImage2.getAwt();

		int width = src1.getWidth();
		int height = src1.getHeight();
		BufferedImage outImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		WritableRaster wr = outImage.getRaster();

		int r1, r2, b1, b2, g1, g2, val1, val2, val3, val4 = 0;

		WritableRaster wr1 = buf1.getRaster();
		WritableRaster wr2 = buf2.getRaster();

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {

				if ((i < x) || (j < y)) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				}

				else if ((i >= src2.getWidth()) || (j >= src2.getHeight())) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
					// break;
				} else if ((i >= width) || (j >= height)) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
					// break;

				} else if (numBands1 == 4 && wr1.getSample(i, j, 3) == 0) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
				} else if (numBands2 == 4 && wr2.getSample(i, j, 3) == 0) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				}

				else {

					r1 = wr1.getSample(i, j, 0);
					g1 = wr1.getSample(i, j, 1);
					b1 = wr1.getSample(i, j, 2);

					r2 = wr2.getSample(i, j, 0);
					g2 = wr2.getSample(i, j, 1);
					b2 = wr2.getSample(i, j, 2);

					val1 = (int) (255 * ((r2 - (255.0 - r1)) / r2));
					val2 = (int) (255 * ((g2 - (255.0 - g1)) / g2));
					val3 = (int) (255 * ((b2 - (255.0 - b1)) / b2));
				}

				if (val1 >= 255.0)
					val1 = 255;
				else if (val1 < 0)
					val1 = 0;

				if (val2 >= 255.0)
					val2 = 255;
				else if (val2 < 0)
					val2 = 0;

				if (val3 >= 255.0)
					val3 = 255;
				else if (val3 < 0)
					val3 = 0;

				if (val4 > 255.0)
					val4 = 255;
				else if (val4 < 0)
					val4 = 0;

				wr.setSample(i, j, 0, val1);
				wr.setSample(i, j, 1, val2);
				wr.setSample(i, j, 2, val3);
				if (numBands1 == 4) {
					wr.setSample(i, j, 3, wr1.getSample(i, j, 3));
					if (wr1.getSample(i, j, 3) == 0 && numBands2 == 4
							&& i < src2.getWidth() && j < src2.getHeight()) {
						wr.setSample(i, j, 3, wr2.getSample(i, j, 3));
					}
					if (wr1.getSample(i, j, 3) != 0)
						wr.setSample(i, j, 3, wr1.getSample(i, j, 3));
				} else {
					wr.setSample(i, j, 3, 255);
				}
			}
		}
		return outImage;
	}

	private static RenderedImage Difference(RenderedImage src1,
			RenderedImage src2, int x, int y) {

		if ((src1 == null))
			return src2;
		if ((src2 == null))
			return src1;

		CoreImage srcImage1 = prepareSrc1(src1);
		CoreImage srcImage2 = prepareSrc2(src2, x, y);

		int numBands1 = srcImage1.getNumBands();
		int numBands2 = srcImage2.getNumBands();

		BufferedImage buf1 = srcImage1.getBufferedImage();
		BufferedImage buf2 = srcImage2.getBufferedImage();

		src1 = srcImage1.getAwt();
		src2 = srcImage2.getAwt();

		int width = src1.getWidth();
		int height = src1.getHeight();

		BufferedImage outImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		WritableRaster wr = outImage.getRaster();

		int r1, r2, b1, b2, g1, g2, val1, val2, val3, val4 = 0;

		WritableRaster wr1 = buf1.getRaster();
		WritableRaster wr2 = buf2.getRaster();
		for (int i = 0; i < src1.getWidth(); i++) {
			for (int j = 0; j < src1.getHeight(); j++) {
				if ((i < x) || (j < y)) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				} else if ((i >= src2.getWidth()) || (j >= src2.getHeight())) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
					// break;
				} else if ((i >= src1.getWidth()) || (j >= src1.getHeight())) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
					// break;

				} else if (numBands1 == 4 && wr1.getSample(i, j, 3) == 0) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
				} else if (numBands2 == 4 && wr2.getSample(i, j, 3) == 0) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				}

				else {

					r1 = wr1.getSample(i, j, 0);
					g1 = wr1.getSample(i, j, 1);
					b1 = wr1.getSample(i, j, 2);

					r2 = wr2.getSample(i, j, 0);
					g2 = wr2.getSample(i, j, 1);
					b2 = wr2.getSample(i, j, 2);

					val1 = (int) Math.abs((r1 - r2));
					val2 = (int) Math.abs((g1 - g2));
					val3 = (int) Math.abs((b1 - b2));
				}

				if (val1 >= 255.0)
					val1 = 255;
				else if (val1 < 0)
					val1 = 0;

				if (val2 >= 255.0)
					val2 = 255;
				else if (val2 < 0)
					val2 = 0;

				if (val3 >= 255.0)
					val3 = 255;
				else if (val3 < 0)
					val3 = 0;

				if (val4 > 255.0)
					val4 = 255;
				else if (val4 < 0)
					val4 = 0;

				wr.setSample(i, j, 0, val1);
				wr.setSample(i, j, 1, val2);
				wr.setSample(i, j, 2, val3);
				if (numBands1 == 4) {

					wr.setSample(i, j, 3, wr1.getSample(i, j, 3));
					if (wr1.getSample(i, j, 3) == 0 && numBands2 == 4
							&& i < src2.getWidth() && j < src2.getHeight()) {

						wr.setSample(i, j, 3, wr2.getSample(i, j, 3));

					}
					if (wr1.getSample(i, j, 3) != 0)
						wr.setSample(i, j, 3, wr1.getSample(i, j, 3));

				} else {
					wr.setSample(i, j, 3, 255);
				}

			}

		}
		return outImage;

	}

	private static RenderedImage Dodge(RenderedImage src1, RenderedImage src2,
			int x, int y) {

		if ((src1 == null))
			return src2;
		if ((src2 == null))
			return src1;

		CoreImage srcImage1 = prepareSrc1(src1);
		CoreImage srcImage2 = prepareSrc2(src2, x, y);

		int numBands1 = srcImage1.getNumBands();
		int numBands2 = srcImage2.getNumBands();

		BufferedImage buf1 = srcImage1.getBufferedImage();
		BufferedImage buf2 = srcImage2.getBufferedImage();

		src1 = srcImage1.getAwt();
		src2 = srcImage2.getAwt();

		int width = src1.getWidth();
		int height = src1.getHeight();

		BufferedImage outImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		WritableRaster wr = outImage.getRaster();

		int r1, r2 = 0, b1, b2 = 0, g1, g2 = 0, val1 = 0, val2 = 0, val3 = 0, val4 = 0;

		WritableRaster wr1 = buf1.getRaster();
		WritableRaster wr2 = buf2.getRaster();
		for (int i = 0; i < src1.getWidth(); i++) {
			for (int j = 0; j < src1.getHeight(); j++) {
				if ((i < x) || (j < y)) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				}

				else if ((i >= src2.getWidth()) || (j >= src2.getHeight())) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
					// break;
				} else if ((i >= src1.getWidth()) || (j >= src1.getHeight())) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
					// break;

				} else if ((wr1.getSample(i, j, 0) == 0)
						&& (wr2.getSample(i, j, 0) != 0)
						&& (wr1.getSample(i, j, 1) == 0)
						&& (wr2.getSample(i, j, 1) != 0)
						&& (wr1.getSample(i, j, 2) == 0)
						&& (wr2.getSample(i, j, 2) != 0)) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
				} else if (numBands1 == 4 && wr1.getSample(i, j, 3) == 0) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
				} else if (numBands2 == 4 && wr2.getSample(i, j, 3) == 0) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				}

				else {
					r1 = wr1.getSample(i, j, 0);
					g1 = wr1.getSample(i, j, 1);
					b1 = wr1.getSample(i, j, 2);

					r2 = wr2.getSample(i, j, 0);
					g2 = wr2.getSample(i, j, 1);
					b2 = wr2.getSample(i, j, 2);

					val1 = (int) (255.0 * r1 / (255.0 - r2));
					val2 = (int) (255.0 * g1 / (255.0 - g2));
					val3 = (int) (255.0 * b1 / (255.0 - b2));
				}

				if (val1 >= 255.0)
					val1 = 255;
				else if (val1 < 0)
					val1 = 0;

				if (val2 >= 255.0)
					val2 = 255;
				else if (val2 < 0)
					val2 = 0;

				if (val3 >= 255.0)
					val3 = 255;
				else if (val3 < 0)
					val3 = 0;

				if (val4 > 255.0)
					val4 = 255;
				else if (val4 < 0)
					val4 = 0;

				wr.setSample(i, j, 0, val1);
				wr.setSample(i, j, 1, val2);
				wr.setSample(i, j, 2, val3);
				if (numBands1 == 4) {

					wr.setSample(i, j, 3, wr1.getSample(i, j, 3));
					if (wr1.getSample(i, j, 3) == 0 && numBands2 == 4
							&& i < src2.getWidth() && j < src2.getHeight()) {
						wr.setSample(i, j, 3, wr2.getSample(i, j, 3));

					}
					if (wr1.getSample(i, j, 3) != 0)
						wr.setSample(i, j, 3, wr1.getSample(i, j, 3));

				} else {
					wr.setSample(i, j, 3, 255);
				}

			}

		}

		return outImage;
	}

	private static RenderedImage Multiply(RenderedImage src1,
			RenderedImage src2, int x, int y) {

		if ((src1 == null))
			return src2;
		if ((src2 == null))
			return src1;

		CoreImage srcImage1 = prepareSrc1(src1);
		CoreImage srcImage2 = prepareSrc2(src2, x, y);

		int numBands1 = srcImage1.getNumBands();
		int numBands2 = srcImage2.getNumBands();

		BufferedImage buf1 = srcImage1.getBufferedImage();
		BufferedImage buf2 = srcImage2.getBufferedImage();

		src1 = srcImage1.getAwt();
		src2 = srcImage2.getAwt();

		BufferedImage outImage = new BufferedImage(src1.getWidth(), src1
				.getHeight(), BufferedImage.TYPE_INT_ARGB);

		int r1, r2, b1, b2, g1, g2, val1, val2, val3 = 0;
		WritableRaster wr1 = buf1.getRaster();
		WritableRaster wr2 = buf2.getRaster();
		WritableRaster wr = outImage.getRaster();

		for (int i = 0; i < src1.getWidth(); i++) {

			for (int j = 0; j < src1.getHeight(); j++) {

				if ((i < x) || (j < y)) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				}

				else if ((i >= src2.getWidth()) || (j >= src2.getHeight())) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);

				} else if ((i >= src1.getWidth()) || (j >= src1.getHeight())) {

					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);

				} else if (numBands1 == 4 && wr1.getSample(i, j, 3) == 0) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
				} else if (numBands2 == 4 && wr2.getSample(i, j, 3) == 0) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				} else {

					r1 = wr1.getSample(i, j, 0);
					g1 = wr1.getSample(i, j, 1);
					b1 = wr1.getSample(i, j, 2);

					r2 = wr2.getSample(i, j, 0);
					g2 = wr2.getSample(i, j, 1);
					b2 = wr2.getSample(i, j, 2);

					val1 = r1 * r2 / 255;

					val2 = g1 * g2 / 255;

					val3 = b1 * b2 / 255;
				}
				if (val1 > 255)
					val1 = 255;
				else if (val1 < 0)
					val1 = 0;

				if (val2 > 255)
					val2 = 255;
				else if (val2 < 0)
					val2 = 0;

				if (val3 > 255)
					val3 = 255;
				else if (val3 < 0)
					val3 = 0;

				wr.setSample(i, j, 0, val1);
				wr.setSample(i, j, 1, val2);
				wr.setSample(i, j, 2, val3);

				if (numBands1 == 4) {

					wr.setSample(i, j, 3, wr1.getSample(i, j, 3));
					if (wr1.getSample(i, j, 3) == 0 && numBands2 == 4
							&& i < src2.getWidth() && j < src2.getHeight()) {

						wr.setSample(i, j, 3, wr2.getSample(i, j, 3));

					}
					if (wr1.getSample(i, j, 3) != 0)
						wr.setSample(i, j, 3, wr1.getSample(i, j, 3));

				} else {
					wr.setSample(i, j, 3, 255);
				}

			}

		}

		return outImage;
	}

	private static RenderedImage Screen(RenderedImage src1, RenderedImage src2,
			int x, int y) {

		if ((src1 == null))
			return src2;
		if ((src2 == null))
			return src1;

		CoreImage srcImage1 = prepareSrc1(src1);
		CoreImage srcImage2 = prepareSrc2(src2, x, y);

		int numBands1 = srcImage1.getNumBands();
		int numBands2 = srcImage2.getNumBands();

		BufferedImage buf1 = srcImage1.getBufferedImage();
		BufferedImage buf2 = srcImage2.getBufferedImage();

		src1 = srcImage1.getAwt();
		src2 = srcImage2.getAwt();

		int width = src1.getWidth();
		int height = src1.getHeight();

		BufferedImage outImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		WritableRaster wr = outImage.getRaster();

		int r1, r2, b1, b2, g1, g2, val1, val2, val3 = 0;

		WritableRaster wr1 = buf1.getRaster();
		WritableRaster wr2 = buf2.getRaster();
		for (int i = 0; i < src1.getWidth(); i++) {
			for (int j = 0; j < src1.getHeight(); j++) {

				if ((i < x) || (j < y)) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				}

				else if ((i >= src2.getWidth()) || (j >= src2.getHeight())) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
					// break;
				} else if ((i >= src1.getWidth()) || (j >= src1.getHeight())) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
					// break;

				} else if (numBands1 == 4 && wr1.getSample(i, j, 3) == 0) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
				} else if (numBands2 == 4 && wr2.getSample(i, j, 3) == 0) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				}

				else {

					r1 = wr1.getSample(i, j, 0);
					g1 = wr1.getSample(i, j, 1);
					b1 = wr1.getSample(i, j, 2);

					r2 = wr2.getSample(i, j, 0);
					g2 = wr2.getSample(i, j, 1);
					b2 = wr2.getSample(i, j, 2);

					val1 = (int) (255.0 - ((255.0 - r1) * ((255.0 - r2)) / 255.0));
					val2 = (int) (255.0 - ((255.0 - g1) * ((255.0 - g2)) / 255.0));
					val3 = (int) (255.0 - ((255.0 - b1) * ((255.0 - b2)) / 255.0));
				}

				wr.setSample(i, j, 0, val1);
				wr.setSample(i, j, 1, val2);
				wr.setSample(i, j, 2, val3);

				if (numBands1 == 4) {
					
					wr.setSample(i, j, 3, wr1.getSample(i, j, 3));
					if (wr1.getSample(i, j, 3) == 0 && numBands2 == 4
							&& i < src2.getWidth() && j < src2.getHeight()) {
						
						wr.setSample(i, j, 3, wr2.getSample(i, j, 3));

					}
					if (wr1.getSample(i, j, 3) != 0)
						wr.setSample(i, j, 3, wr1.getSample(i, j, 3));

					
				} else {
					wr.setSample(i, j, 3, 255);
				}

			}

		}

		return outImage;
	}

	public static RenderedImage Normal(RenderedImage src1, RenderedImage src2) {

		BufferedImage outImage = new BufferedImage(src1.getWidth(), src1
				.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D gd = (Graphics2D) outImage.createGraphics();
		if (src1 == null) {
			gd.drawRenderedImage(src2, CoreImage.TRANSFORM_ORIGIN);
			gd.dispose();
			return outImage;
		} else if (src2 == null) {
			gd.drawRenderedImage(src1, CoreImage.TRANSFORM_ORIGIN);
			gd.dispose();
			return outImage;
		}

		

		gd.drawRenderedImage(src1, CoreImage.TRANSFORM_ORIGIN);
		gd.drawRenderedImage(src2, CoreImage.TRANSFORM_ORIGIN);

		return outImage;
	}

	private static RenderedImage OverLay(RenderedImage src1,
			RenderedImage src2, int x, int y) {

		

		if ((src1 == null))
			return src2;
		if ((src2 == null))
			return src1;

		CoreImage srcImage1 = prepareSrc1(src1);
		CoreImage srcImage2 = prepareSrc2(src2, x, y);

		int numBands1 = srcImage1.getNumBands();
		int numBands2 = srcImage2.getNumBands();

		BufferedImage buf1 = srcImage1.getBufferedImage();
		BufferedImage buf2 = srcImage2.getBufferedImage();

		src1 = srcImage1.getAwt();
		src2 = srcImage2.getAwt();

		
		int width = src1.getWidth();
		int height = src1.getHeight();

		BufferedImage outImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		WritableRaster wr = outImage.getRaster();

		int r1, r2, b1, b2, g1, g2, val1, val2, val3, val4 = 0;

		WritableRaster wr1 = buf1.getRaster();
		WritableRaster wr2 = buf2.getRaster();
		for (int i = 0; i < src1.getWidth(); i++) {
			for (int j = 0; j < src1.getHeight(); j++) {

				if ((i < x) || (j < y)) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				}

				else if ((i >= src2.getWidth()) || (j >= src2.getHeight())) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
					// break;
				} else if ((i >= src1.getWidth()) || (j >= src1.getHeight())) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
					// break;

				} else if (numBands1 == 4 && wr1.getSample(i, j, 3) == 0) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
				} else if (numBands2 == 4 && wr2.getSample(i, j, 3) == 0) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				}

				else {

					r1 = wr1.getSample(i, j, 0);
					g1 = wr1.getSample(i, j, 1);
					b1 = wr1.getSample(i, j, 2);

					r2 = wr2.getSample(i, j, 0);
					g2 = wr2.getSample(i, j, 1);
					b2 = wr2.getSample(i, j, 2);

					if ((r1) <= 127)
						val1 = (int) ((r1 * r2 / 255.0));
					else
						val1 = (int) ((((r2) + (r1) - (r1 * r2 / 255))));

					if ((g1) <= 127)
						val2 = (int) ((g1 * g2 / 255.0));
					else
						val2 = (int) (((g2) + (g1) - (g1 * g2 / 255)));

					if ((b1) <= 127)
						val3 = (int) ((b1 * b2 / 255.0));
					else
						val3 = (int) (((b2) + (b1) - (b1 * b2 / 255)));
				}

				if (val1 >= 255.0)
					val1 = 255;
				else if (val1 < 0)
					val1 = 0;

				if (val2 >= 255.0)
					val2 = 255;
				else if (val2 < 0)
					val2 = 0;

				if (val3 >= 255.0)
					val3 = 255;
				else if (val3 < 0)
					val3 = 0;

				if (val4 > 255.0)
					val4 = 255;
				else if (val4 < 0)
					val4 = 0;

				wr.setSample(i, j, 0, val1);
				wr.setSample(i, j, 1, val2);
				wr.setSample(i, j, 2, val3);
				if (numBands1 == 4) {
					
					wr.setSample(i, j, 3, wr1.getSample(i, j, 3));
					if (wr1.getSample(i, j, 3) == 0 && numBands2 == 4
							&& i < src2.getWidth() && j < src2.getHeight()) {
						
						wr.setSample(i, j, 3, wr2.getSample(i, j, 3));

					}
					if (wr1.getSample(i, j, 3) != 0)
						wr.setSample(i, j, 3, wr1.getSample(i, j, 3));

					
				} else {
					wr.setSample(i, j, 3, 255);
				}

			}

		}

		return outImage;
	}

	private static RenderedImage HardLight(RenderedImage src1,
			RenderedImage src2, int x, int y) {

		// Color col=null;

		if ((src1 == null))
			return src2;
		if ((src2 == null))
			return src1;

		CoreImage srcImage1 = prepareSrc1(src1);
		CoreImage srcImage2 = prepareSrc2(src2, x, y);

		int numBands1 = srcImage1.getNumBands();
		int numBands2 = srcImage2.getNumBands();

		BufferedImage buf1 = srcImage1.getBufferedImage();
		BufferedImage buf2 = srcImage2.getBufferedImage();

		src1 = srcImage1.getAwt();
		src2 = srcImage2.getAwt();

		int width = src1.getWidth();
		int height = src1.getHeight();
		
		BufferedImage outImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		WritableRaster wr = outImage.getRaster();

		int r1, r2, b1, b2, g1, g2, val1, val2, val3, val4 = 0;

		WritableRaster wr1 = buf1.getRaster();
		WritableRaster wr2 = buf2.getRaster();
		for (int i = 0; i < src1.getWidth(); i++) {
			for (int j = 0; j < src1.getHeight(); j++) {
				if ((i < x) || (j < y)) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				}

				else if ((i >= src2.getWidth()) || (j >= src2.getHeight())) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
					// break;
				} else if ((i >= src1.getWidth()) || (j >= src1.getHeight())) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
					// break;

				} else if (numBands1 == 4 && wr1.getSample(i, j, 3) == 0) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
				} else if (numBands2 == 4 && wr2.getSample(i, j, 3) == 0) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				} else {

					r1 = wr1.getSample(i, j, 0);
					g1 = wr1.getSample(i, j, 1);
					b1 = wr1.getSample(i, j, 2);

					r2 = wr2.getSample(i, j, 0);
					g2 = wr2.getSample(i, j, 1);
					b2 = wr2.getSample(i, j, 2);

					if ((r2) <= 127)
						val1 = (int) ((r1 * r2 / 255.0));
					else
						val1 = (int) (((r2) + (r1) - (r1 * r2 / 255)));

					if ((g2) <= 127)
						val2 = (int) ((g1 * g2 / 255.0));
					else
						val2 = (int) ((((g2) + (g1) - (g1 * g2 / 255))));

					if ((b2) <= 127)
						val3 = (int) ((b1 * b2 / 255.0));
					else
						val3 = (int) ((((b2) + (b1) - (b1 * b2 / 255))));

					// bool=true;
				}

				if (val1 >= 255.0)
					val1 = 255;
				else if (val1 < 0)
					val1 = 0;

				if (val2 >= 255.0)
					val2 = 255;
				else if (val2 < 0)
					val2 = 0;

				if (val3 >= 255.0)
					val3 = 255;
				else if (val3 < 0)
					val3 = 0;

				if (val4 > 255.0)
					val4 = 255;
				else if (val4 < 0)
					val4 = 0;

				wr.setSample(i, j, 0, val1);
				wr.setSample(i, j, 1, val2);
				wr.setSample(i, j, 2, val3);
				if (numBands1 == 4) {
				
					wr.setSample(i, j, 3, wr1.getSample(i, j, 3));
					if (wr1.getSample(i, j, 3) == 0 && numBands2 == 4
							&& i < src2.getWidth() && j < src2.getHeight()) {
						
						wr.setSample(i, j, 3, wr2.getSample(i, j, 3));

					}
					if (wr1.getSample(i, j, 3) != 0)
						wr.setSample(i, j, 3, wr1.getSample(i, j, 3));

					
				} else {
					wr.setSample(i, j, 3, 255);
				}

			}

		}

		return outImage;
	}

	private static RenderedImage SoftLight(RenderedImage src1,
			RenderedImage src2, int x, int y) {

		// Color col=null;
		if ((src1 == null))
			return src2;
		if ((src2 == null))
			return src1;

		CoreImage srcImage1 = prepareSrc1(src1);
		CoreImage srcImage2 = prepareSrc2(src2, x, y);

		int numBands1 = srcImage1.getNumBands();
		int numBands2 = srcImage2.getNumBands();

		BufferedImage buf1 = srcImage1.getBufferedImage();
		BufferedImage buf2 = srcImage2.getBufferedImage();

		src1 = srcImage1.getAwt();
		src2 = srcImage2.getAwt();

		int width = src1.getWidth();
		int height = src1.getHeight();

		

		BufferedImage outImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		WritableRaster wr = outImage.getRaster();

		int r1, r2, b1, b2, g1, g2, val1, val2, val3, val4 = 0;

		WritableRaster wr1 = buf1.getRaster();
		WritableRaster wr2 = buf2.getRaster();
		for (int i = 0; i < src1.getWidth(); i++) {
			for (int j = 0; j < src1.getHeight(); j++) {
				if ((i < x) || (j < y)) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				}

				else if ((i >= src2.getWidth()) || (j >= src2.getHeight())) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
					// break;
				} else if ((i >= src1.getWidth()) || (j >= src1.getHeight())) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
					// break;

				} else if (numBands1 == 4 && wr1.getSample(i, j, 3) == 0) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
				} else if (numBands2 == 4 && wr2.getSample(i, j, 3) == 0) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				}

				else {

					r1 = wr1.getSample(i, j, 0);
					g1 = wr1.getSample(i, j, 1);
					b1 = wr1.getSample(i, j, 2);

					r2 = wr2.getSample(i, j, 0);
					g2 = wr2.getSample(i, j, 1);
					b2 = wr2.getSample(i, j, 2);
					val1 = (int) (((2 * r1 * r2) / 255.0) + ((r1 * r1) / 255.0) - ((2
							* r1 * r1 * r2) / (255.0 * 255.0)));

					val2 = (int) (((2 * g1 * g2) / 255.0) + ((g1 * g1) / 255.0) - ((2
							* g1 * g1 * g2) / (255.0 * 255.0)));

					val3 = (int) (((2 * b1 * b2) / 255.0) + ((b1 * b1) / 255.0) - ((2
							* b1 * b1 * b2) / (255.0 * 255.0)));
				}
				

				if (val1 > 255.0)
					val1 = 255;
				else if (val1 < 0)
					val1 = 0;

				if (val2 > 255.0)
					val2 = 255;
				else if (val2 < 0)
					val2 = 0;

				if (val3 > 255.0)
					val3 = 255;
				else if (val3 < 0)
					val3 = 0;

				if (val4 > 255.0)
					val4 = 255;
				else if (val4 < 0)
					val4 = 0;

				wr.setSample(i, j, 0, val1);
				wr.setSample(i, j, 1, val2);
				wr.setSample(i, j, 2, val3);
				if (numBands1 == 4) {
					
					wr.setSample(i, j, 3, wr1.getSample(i, j, 3));
					if (wr1.getSample(i, j, 3) == 0 && numBands2 == 4
							&& i < src2.getWidth() && j < src2.getHeight()) {
						
						wr.setSample(i, j, 3, wr2.getSample(i, j, 3));

					}
					if (wr1.getSample(i, j, 3) != 0)
						wr.setSample(i, j, 3, wr1.getSample(i, j, 3));

					
				} else {
					wr.setSample(i, j, 3, 255);
				}
			}

		}
		return outImage;
	}

	private static RenderedImage Lighten(RenderedImage src1,
			RenderedImage src2, int x, int y) {

		if ((src1 == null))
			return src2;
		if ((src2 == null))
			return src1;

		CoreImage srcImage1 = prepareSrc1(src1);
		CoreImage srcImage2 = prepareSrc2(src2, x, y);

		int numBands1 = srcImage1.getNumBands();
		int numBands2 = srcImage2.getNumBands();

		BufferedImage buf1 = srcImage1.getBufferedImage();
		BufferedImage buf2 = srcImage2.getBufferedImage();

		src1 = srcImage1.getAwt();
		src2 = srcImage2.getAwt();

		
		int width = src1.getWidth();
		int height = src1.getHeight();

		BufferedImage outImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		WritableRaster wr = outImage.getRaster();

		int r1, r2, b1, b2, g1, g2, val1, val2, val3, val4 = 0;

		WritableRaster wr1 = buf1.getRaster();
		WritableRaster wr2 = buf2.getRaster();
		for (int i = 0; i < src1.getWidth(); i++) {
			for (int j = 0; j < src1.getHeight(); j++) {
				if ((i < x) || (j < y)) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				} else if ((i >= src2.getWidth()) || (j >= src2.getHeight())) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
					// break;
				} else if ((i >= src1.getWidth()) || (j >= src1.getHeight())) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
					// break;

				} else if (numBands1 == 4 && wr1.getSample(i, j, 3) == 0) {
					val1 = wr2.getSample(i, j, 0);
					val2 = wr2.getSample(i, j, 1);
					val3 = wr2.getSample(i, j, 2);
				} else if (numBands2 == 4 && wr2.getSample(i, j, 3) == 0) {
					val1 = wr1.getSample(i, j, 0);
					val2 = wr1.getSample(i, j, 1);
					val3 = wr1.getSample(i, j, 2);
				}

				else {

					r1 = wr1.getSample(i, j, 0);
					g1 = wr1.getSample(i, j, 1);
					b1 = wr1.getSample(i, j, 2);
					

					r2 = wr2.getSample(i, j, 0);
					g2 = wr2.getSample(i, j, 1);
					b2 = wr2.getSample(i, j, 2);

					val1 = (int) Math.max(r1, r2);
					val2 = (int) Math.max(g1, g2);
					val3 = (int) Math.max(b1, b2);
				}

				if (val1 >= 255.0)
					val1 = 255;
				else if (val1 < 0)
					val1 = 0;

				if (val2 >= 255.0)
					val2 = 255;
				else if (val2 < 0)
					val2 = 0;

				if (val3 >= 255.0)
					val3 = 255;
				else if (val3 < 0)
					val3 = 0;

				if (val4 > 255.0)
					val4 = 255;
				else if (val4 < 0)
					val4 = 0;

				wr.setSample(i, j, 0, val1);
				wr.setSample(i, j, 1, val2);
				wr.setSample(i, j, 2, val3);
				if (numBands1 == 4) {
					
					wr.setSample(i, j, 3, wr1.getSample(i, j, 3));
					if (wr1.getSample(i, j, 3) == 0 && numBands2 == 4
							&& i < src2.getWidth() && j < src2.getHeight()) {
						
						wr.setSample(i, j, 3, wr2.getSample(i, j, 3));

					}
					if (wr1.getSample(i, j, 3) != 0)
						wr.setSample(i, j, 3, wr1.getSample(i, j, 3));

					
				} else {
					wr.setSample(i, j, 3, 255);
				}

			}

		}

		return outImage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.effects.ImageProcessor#getEffectString(int,
	 *      int, java.util.HashMap)
	 */
	public StringBuffer getEffectString(int prevLayerPosition,
			int currentPosition, Map effectValues) {

		if (effectValues == null || effectValues.size() <= 0)
			return null;
		boolean morphed = false;
		if (effectValues.get(MORPHED) != null) {
			morphed = new Boolean((String) effectValues.get("MORPHED"))
					.booleanValue();
			effectValues.remove(MORPHED);
		}
		StringBuffer effectStr = new StringBuffer();
		String inputA = "" + prevLayerPosition + FSLASH + RGB;
		String inputB = "" + currentPosition + FSLASH + RGB;

		Boolean writeBackToInputLayer = ((Boolean) effectValues
				.get(ThemeTag.KEY_OVERWRITE_INPUT));
		int outputPosition = (writeBackToInputLayer != null && writeBackToInputLayer
				.booleanValue()) ? currentPosition + 1 : currentPosition;
		String output = "" + outputPosition + FSLASH + RGB;

		
		if (morphed)
			effectStr.append(COMMAND).append(SPACE);
		else
			effectStr.append(EFFECT).append(SPACE);
		effectStr.append(UID_STR).append(EQUAL).append(effectUID).append(SPACE);
		effectStr.append(INPUTA).append(EQUAL).append(inputA).append(SPACE);
		effectStr.append(INPUTB).append(EQUAL).append(inputB).append(SPACE);
		effectStr.append(OUTPUT).append(EQUAL).append(output).append(NL);

		
		Map outputParametersMap = getOutputParams(effectValues, null,
				(morphed) ? ANIMATION : SCALEABLEITEM);
		Map[] effectOutputData = (HashMap[]) outputParametersMap.values()
				.toArray(new HashMap[1]);
		for (int i = 0; i < effectOutputData.length; i++) {
			Map nextParam = effectOutputData[i];
			String paramType = (String) nextParam
					.get(EffectConstants.ATTR_TYPE);
			String paramName = (String) nextParam
					.get(EffectConstants.ATTR_NAME);
			String paramValue = (String) nextParam
					.get(EffectConstants.ATTR_VALUE);
			String valueModelRef = (String) nextParam
					.get(ThemeTag.ELEMENT_VALUEMODEL_REF);
			
			if ((paramValue == null || paramValue.trim().length() == 0)
					&& (valueModelRef == null || valueModelRef.trim().length() == 0))
				continue;

			if (morphed && valueModelRef != null) {
				effectStr.append(TAB).append(TAB);
				effectStr.append(NAMEDREF).append(SPACE);
				effectStr.append(paramName).append(SPACE);
				effectStr.append(VALUEID).append(EQUAL);
				Map valueModelMap = (Map) effectValues
						.get(ThemeTag.KEY_VALUE_MODEL_DATA);
				String s = new Integer(MorphingItemDefinition.getValueModelId(
						valueModelRef, valueModelMap)).toString();
				effectStr.append(s).append(NL);

			
			} else {
				effectStr.append(TAB).append(TAB);
				effectStr.append(paramType).append(SPACE);
				effectStr.append(paramName).append(SPACE);
				effectStr.append(paramValue).append(NL);
			}
		}

		effectStr.append(TAB).append(END);

		return effectStr;
	}

}
