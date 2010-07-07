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
package com.nokia.tools.theme.s60.effects.effect;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.EffectConstants;
import com.nokia.tools.platform.theme.LayerEffect;
import com.nokia.tools.platform.theme.ParameterModel;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.s60.effects.EffectObject;
import com.nokia.tools.theme.s60.effects.ImageProcessor;
import com.nokia.tools.theme.s60.siscreation.MorphingItemDefinition;

public class MovingLayer implements ImageProcessor {

	private String effectUID = "0x1020762D";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.effects.ImageProcessor#doProcessing(java.awt.image.RenderedImage,
	 *      java.util.HashMap)
	 */
	public RenderedImage doProcessing(RenderedImage src, HashMap map) {

		int x = 0;
		int y = 0;
		int xy = 0;
		if (src == null)
			return null;
		int w = src.getWidth();
		int h = src.getHeight();
		
		int width = 0;
		int height = 0;

		if (map.get(EffectConstants.LAYOUT) != null) {
			Layout l = (Layout) map.get(EffectConstants.LAYOUT);
			width = l.W();
			height = l.H();
		}
		if (src != null) {
			width = src.getWidth();
			height = src.getHeight();
		}
		EffectObject eObj = null;
		LayerEffect le = null;
		try {
			if (map.get(EffectConstants.EFFECTOBJECT) instanceof EffectObject) {
				eObj = (EffectObject) map.get(EffectConstants.EFFECTOBJECT);
				if (eObj == null)
					eObj = EffectObject.getEffect(EffectConstants.MOVINGLAYER);
				xy = new Integer((String) eObj.getAttributeValue("xy"))
						.intValue();
				
				x = xy;
				y = xy;

			} else {
				le = (LayerEffect) map.get(EffectConstants.EFFECTOBJECT);
				if (le.getAttribute("xy") != null) {
					String s = le.getAttribute("xy");
					int index = s.indexOf(",");
					if (index == -1) {
						x = new Integer(s).intValue();
						y = new Integer(s).intValue();
					} else {
						String xs = s.substring(0, index);
						String ys = s.substring(index + 1);
						x = new Integer(xs).intValue();
						y = new Integer(ys).intValue();
					}

				}

				
			}
		} catch (Exception e) {

			e.printStackTrace();
			

			if (eObj != null) {
				eObj.invalidData = true;
			}

			return src;
		}

		CoreImage main = CoreImage.create(src);
		BufferedImage buf = CoreImage.create().init(width, height, Color.WHITE,
				0, 4).getBufferedImage();
		Graphics2D g2d = (Graphics2D) buf.createGraphics();

		RenderedImage img1 = main.copy().crop(0, 0, w - x, h - y).getAwt();
		RenderedImage img2 = main.copy().crop(w - x, 0, x, h - y).getAwt();
		RenderedImage img3 = main.copy().crop(0, h - y, w - x, y).getAwt();
		RenderedImage img4 = main.copy().crop(w - x, h - y, x, y).getAwt();

		g2d.drawRenderedImage(img4, CoreImage.TRANSFORM_ORIGIN);
		g2d.drawRenderedImage(img3, AffineTransform.getTranslateInstance(x, 0));
		g2d.drawRenderedImage(img2, AffineTransform.getTranslateInstance(0, y));
		g2d.drawRenderedImage(img1, AffineTransform.getTranslateInstance(x, y));
		g2d.dispose();

		return buf;
	}

	public HashMap getOutputParams(Map uiMap, Map attrMap, String type) {

		
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		
		map.put("bxy", getOutputMap("bxy", "INT", null));

		HashMap eMap = map;
		Set set = map.keySet();
		Iterator iter = set.iterator();
		String valueRef = null;
		ParameterModel pm = null;
		while (iter.hasNext()) {
			String name = (String) iter.next();
			String value = "";
			if (uiMap.get(name) instanceof String)
				value = (String) uiMap.get(name);
			else if (uiMap.get("xy") instanceof ParameterModel) {
				pm = (ParameterModel) uiMap.get("xy");
				if (pm.isAnimatedModel() && type.equalsIgnoreCase(ANIMATION))
					valueRef = pm.getAttribute(ThemeTag.ELEMENT_VALUEMODEL_REF);
				else
					value = pm.getValue("xy");
			}
			HashMap<Object, Object> map1 = (HashMap) eMap.get(name);
			if (map1 != null) {
				if (valueRef != null) {
					map1.put(ThemeTag.ELEMENT_VALUEMODEL_REF, new String(
							valueRef));
					if (pm.getValue("xy") != null)
						map1.put(EffectConstants.ATTR_DEFAULTVALUE, pm
								.getValue("xy"));
					valueRef = null;
				} else {
					map1.remove(ThemeTag.ELEMENT_VALUEMODEL_REF);
					map1.put(EffectConstants.ATTR_VALUE, value);
				}
			}
		}

		return eMap;
	}

	private HashMap getOutputMap(String name, String type, String value) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(EffectConstants.ATTR_NAME, name);
		map.put(EffectConstants.ATTR_TYPE, type);
		map.put(EffectConstants.ATTR_VALUE, value);
		return map;

	}

	public HashMap getEffectParameters(Map uiMap, Map attrMap, String type) {
		return getOutputParams(uiMap, attrMap, type);
	}

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
		/*
		 * inputA has to be RGB only
		 */
		String inputA = "" + prevLayerPosition + FSLASH + RGB;
		String inputB = "" + currentPosition + FSLASH + RGBA;

		/*
		 * Moving layer has to be always written to the next layer Boolean
		 */
		int outputPosition = currentPosition + 1;
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

	public static void main(String args[]) {
		new MovingLayer();
	}
}
