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
/**
 * To change the template for this generated file
 * go to Window - Preferences - Java - Code Style - Code Templates
 */
package com.nokia.tools.theme.s60.effects.effect;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.platform.theme.EffectConstants;
import com.nokia.tools.platform.theme.ParameterModel;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.s60.effects.EffectObject;
import com.nokia.tools.theme.s60.effects.EffectObjectUtils;
import com.nokia.tools.theme.s60.effects.EffectParameter;
import com.nokia.tools.theme.s60.effects.ImageProcessor;
import com.nokia.tools.theme.s60.siscreation.MorphingItemDefinition;
import com.sun.imageio.plugins.common.ImageUtil;

/**
 * To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
public class AlphaBlending implements ImageProcessor {

	private final String effectUID = "0x10204ADD";

	public RenderedImage doProcessing(RenderedImage src, HashMap map) {
		RenderedImage image = null;
		try {
			String mode = (String) map.get(EffectConstants.BLENDMODE);
			EffectObject eObj = (EffectObject) map
					.get(EffectConstants.EFFECTOBJECT);

			if (eObj == null) {
				eObj = EffectObject.getEffect(EffectConstants.ALPHABLENDING);
			}
			EffectParameter blendMode = eObj
					.getParameter(EffectConstants.ATTR_BLEND);
			if (blendMode != null) {
				int value = blendMode.getValueAsInt();
				mode = EffectObjectUtils.getValueLiteral(
						EffectConstants.ALPHABLENDING, blendMode.getName(),
						value);
			}

			
			RenderedImage src1 = (RenderedImage) map
					.get(EffectConstants.SOURCE1);
			RenderedImage src2 = (RenderedImage) map
					.get(EffectConstants.SOURCE2);

			if (src2 == null)
				return src1;

			CoreImage srcImage1 = CoreImage.create(src1);
			CoreImage srcImage2 = CoreImage.create(src2);
			String inputA = (String) getEffectProperties().get("INPUTA");
			if (srcImage1.getNumBands() == 4 && inputA.equalsIgnoreCase("RGB")) {
				srcImage1.reduceToThreeBand();
			}
			String inputB = (String) getEffectProperties().get("INPUTB");

			if (srcImage2.getNumBands() == 4 && inputB.equalsIgnoreCase("RGB")) {
				srcImage2.reduceToThreeBand();
			}
			src1 = srcImage1.getAwt();
			src2 = srcImage2.getAwt();

			int x = 0, y = 0;
			if (map.get(EffectConstants.Y) != null)
				y = ((Integer) map.get(EffectConstants.Y)).intValue();
			if (map.get(EffectConstants.Y) != null)
				x = ((Integer) map.get(EffectConstants.X)).intValue();

			if (mode.equalsIgnoreCase(EffectConstants.AOVERB))
				image = AOverB(src1, src2, x, y);
			else if (mode.equalsIgnoreCase(EffectConstants.BOVERA))
				image = BOverA(src1, src2, x, y);
			else if (mode.equalsIgnoreCase(EffectConstants.AOUTB))
				image = AOutB(src1, src2, x, y);
			else if (mode.equalsIgnoreCase(EffectConstants.BOUTA))
				image = BOutA(src1, src2, x, y);
			else if (mode.equalsIgnoreCase(EffectConstants.ATOPB))
				image = ATopB(src1, src2, x, y);
			else if (mode.equalsIgnoreCase(EffectConstants.BTOPA))
				image = BTopA(src1, src2, x, y);
			else if (mode.equalsIgnoreCase(EffectConstants.AINB))
				image = AInB(src1, src2, x, y);
			else if (mode.equalsIgnoreCase(EffectConstants.BINA))
				image = BInA(src1, src2, x, y);
			else if (mode.equalsIgnoreCase(EffectConstants.AXORB))
				image = AXorB(src1, src2, x, y);
			else
				// sometimes, it can happen that mode is missing, in this case,
				// default
				image = BOverA(src1, src2, x, y);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return image;
	}

	public HashMap<Object, Object> getOutputParams(Map uiMap, Map attrMap,
			String type) {

		EffectObject eObj = EffectObject
				.getEffect(EffectConstants.ALPHABLENDING);
		HashMap<Object, Object> eMap = eObj.getOutputAttributeMap();
		Set set = eMap.keySet();
		Iterator iter = set.iterator();
		while (iter.hasNext()) {
			String name = (String) iter.next();
			if (uiMap != null) {
				HashMap<Object, Object> map = (HashMap) eMap.get(name);
				if (uiMap.get(name) instanceof String) {
					if (map != null)
						map.put(EffectConstants.ATTR_VALUE, (String) uiMap
								.get(name));
				} else {
					if (name.equalsIgnoreCase(EffectConstants.ATTR_BLEND)) {
						ParameterModel p = (ParameterModel) uiMap.get(name);
						if (p != null) {
							if (p.getAttribute(ThemeTag.ELEMENT_VALUEMODEL_REF) != null)
								map
										.put(
												EffectConstants.ATTR_VALUE,
												p
														.getAttribute(ThemeTag.ELEMENT_VALUEMODEL_REF));
							else
								map.put(EffectConstants.ATTR_VALUE, p
										.getValue(name));
						}
					}
				}
			}
		}
		return eMap;
	}

	public HashMap getEffectParameters(Map uiMap, Map attrMap, String type) {
		return getOutputParams(uiMap, attrMap, type);
	}

	public HashMap getEffectProperties() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("INPUTA", "RGBA");
		map.put("INPUTB", "RGBA");
		map.put("OUTPUT", "RGB");
		return map;
	}

	private static RenderedImage AOverB(RenderedImage src1, RenderedImage src2,
			int posx, int posy) {

		int width = src1.getWidth();
		int height = src1.getHeight();
		BufferedImage buf = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D a = (Graphics2D) buf.getGraphics();
		a.drawRenderedImage(src1, CoreImage.TRANSFORM_ORIGIN);
		AlphaComposite g = AlphaComposite.getInstance(AlphaComposite.DST_OVER);
		a.setComposite(g);
		a.drawRenderedImage(src2, AffineTransform.getTranslateInstance(posx,
				posy));
		a.dispose();
		return buf;
	}

	/**
	 * API that takes 2 RenderedImages and draws Image B over Image A. The
	 * images are mask passed after applying the mask on the image
	 */

	private static RenderedImage BOverA(RenderedImage src1, RenderedImage src2,
			int posx, int posy) {

		int width = src1.getWidth();
		int height = src1.getHeight();

		BufferedImage buf = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D a = (Graphics2D) buf.getGraphics();
		a.drawRenderedImage(src1, CoreImage.TRANSFORM_ORIGIN);
		AlphaComposite g = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
		a.setComposite(g);
		a.drawRenderedImage(src2, AffineTransform.getTranslateInstance(posx,
				posy));
		a.dispose();
		return buf;
	}

	/**
	 * API that takes 2 RenderedImage and draws portion of Image A in Image B.
	 * The images are mask passed after applying the mask on the image
	 */

	private static RenderedImage AInB(RenderedImage src1, RenderedImage src2,
			int posx, int posy) {

		int width = src1.getWidth();
		int height = src1.getHeight();

		BufferedImage buf = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D a = (Graphics2D) buf.getGraphics();

		a.drawRenderedImage(src1, CoreImage.TRANSFORM_ORIGIN);
		AlphaComposite g = AlphaComposite.getInstance(AlphaComposite.DST_IN);
		a.setComposite(g);
		a.drawRenderedImage(src2, AffineTransform.getTranslateInstance(posx,
				posy));
		a.dispose();
		return buf;
	}

	/**
	 * API that takes 2 RenderedImage and draws the portion of Image B in Image
	 * A. The images are mask passed after applying the mask on the image
	 */

	private static RenderedImage BInA(RenderedImage src1, RenderedImage src2,
			int posx, int posy) {

		int width = src1.getWidth();
		int height = src1.getHeight();

		BufferedImage buf = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D a = (Graphics2D) buf.getGraphics();
		a.drawRenderedImage(src1, CoreImage.TRANSFORM_ORIGIN);
		AlphaComposite g = AlphaComposite.getInstance(AlphaComposite.SRC_IN);
		a.setComposite(g);
		a.drawRenderedImage(src2, AffineTransform.getTranslateInstance(posx,
				posy));
		a.dispose();
		return buf;
	}

	/**
	 * API that takes 2 RenderedImages and draws Image B with portion of image A
	 * in B. The images are mask passed after applying the mask on the image
	 */

	private static RenderedImage ATopB(RenderedImage src1, RenderedImage src2,
			int posx, int posy) {
		int width = src1.getWidth();
		int height = src1.getHeight();

		BufferedImage buf = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D a = (Graphics2D) buf.getGraphics();
		a.drawRenderedImage(src1, CoreImage.TRANSFORM_ORIGIN);
		AlphaComposite g = AlphaComposite.getInstance(AlphaComposite.DST_ATOP);
		a.setComposite(g);
		a.drawRenderedImage(src2, AffineTransform.getTranslateInstance(posx,
				posy));
		a.dispose();
		return buf;
	}

	/**
	 * API that takes 2 RenderedImages and draws Image A with portion of image B
	 * in A. The images are passed after applying the mask on the image
	 * 
	 * @param src1
	 * @param src2
	 * @return
	 */
	private static RenderedImage BTopA(RenderedImage src1, RenderedImage src2,
			int posx, int posy) {
		int width = src1.getWidth();
		int height = src1.getHeight();

		BufferedImage buf = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D a = (Graphics2D) buf.getGraphics();
		a.drawRenderedImage(src1, CoreImage.TRANSFORM_ORIGIN);
		AlphaComposite g = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP);
		a.setComposite(g);
		a.drawRenderedImage(src2, AffineTransform.getTranslateInstance(posx,
				posy));
		a.dispose();
		return buf;
	}

	/**
	 * API that takes 2 RenderedImages and draws image A without the porion of A
	 * in B
	 * 
	 * @param src1
	 * @param src2
	 * @return
	 */
	private static RenderedImage AOutB(RenderedImage src1, RenderedImage src2,
			int posx, int posy) {
		int width = src1.getWidth();
		int height = src1.getHeight();

		BufferedImage buf = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D a = (Graphics2D) buf.getGraphics();
		a.drawRenderedImage(src1, CoreImage.TRANSFORM_ORIGIN);
		AlphaComposite g = AlphaComposite.getInstance(AlphaComposite.DST_OUT);
		a.setComposite(g);
		a.drawRenderedImage(src2, AffineTransform.getTranslateInstance(posx,
				posy));
		a.dispose();
		return buf;
	}

	/**
	 * API that takes 2 RenderedImages and draws image B without the porion of B
	 * in A
	 * 
	 * @param src1
	 * @param src2
	 * @return
	 */
	private static RenderedImage BOutA(RenderedImage src1, RenderedImage src2,
			int posx, int posy) {
		int width = src1.getWidth();
		int height = src1.getHeight();

		BufferedImage buf = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D a = (Graphics2D) buf.getGraphics();
		a.drawRenderedImage(src1, CoreImage.TRANSFORM_ORIGIN);
		AlphaComposite g = AlphaComposite.getInstance(AlphaComposite.SRC_OUT);
		a.setComposite(g);
		a.drawRenderedImage(src2, AffineTransform.getTranslateInstance(posx,
				posy));
		a.dispose();
		return buf;
	}

	/**
	 * API that takes 2 RenderedImages and draws image A and Image B without the
	 * common portion
	 * 
	 * @param src1
	 * @param src2
	 * @return
	 */
	private static RenderedImage AXorB(RenderedImage src1, RenderedImage src2,
			int posx, int posy) {
		int width = src1.getWidth();
		int height = src1.getHeight();

		BufferedImage buf = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D a = (Graphics2D) buf.getGraphics();

		a.drawRenderedImage(src1, CoreImage.TRANSFORM_ORIGIN);
		AlphaComposite g = AlphaComposite.getInstance(AlphaComposite.XOR);
		a.setComposite(g);
		a.drawRenderedImage(src2, AffineTransform.getTranslateInstance(posx,
				posy));
		a.dispose();
		return buf;
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

		String inputA = "" + prevLayerPosition + FSLASH + RGBA;
		String inputB = "" + currentPosition + FSLASH + RGBA;
		
		int outputPosition = currentPosition + 1;
		String output = "" + outputPosition + FSLASH + RGBA;

		if (morphed)
			effectStr.append(COMMAND).append(SPACE);
		else
			effectStr.append(EFFECT).append(SPACE);
		effectStr.append(UID_STR).append(EQUAL).append(effectUID).append(SPACE);
		effectStr.append(INPUTA).append(EQUAL).append(inputA).append(SPACE);
		effectStr.append(INPUTB).append(EQUAL).append(inputB).append(SPACE);
		effectStr.append(OUTPUT).append(EQUAL).append(output).append(NL);

		Map<Object, Object> outputParametersMap = getOutputParams(effectValues,
				null, (morphed) ? ANIMATION : SCALEABLEITEM);
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
