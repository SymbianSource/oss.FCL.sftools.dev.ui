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

import java.awt.image.RenderedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.platform.theme.EffectConstants;
import com.nokia.tools.platform.theme.LayerEffect;
import com.nokia.tools.platform.theme.ParameterModel;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.s60.effects.EffectObject;
import com.nokia.tools.theme.s60.effects.ImageProcessor;
import com.nokia.tools.theme.s60.siscreation.MorphingItemDefinition;

/**
 * To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
public class AdjustChannels implements ImageProcessor {

	private String effectUID = "0x10204ADC";

	
	HashMap<String, String> attributes;

	public AdjustChannels() {
		attributes = new HashMap<String, String>();
		attributes.put("INPUTA", "RGB");
		attributes.put("INPUTB", "none");
		attributes.put("OUTPUT", "RGBA");
	}

	public RenderedImage doProcessing(RenderedImage src, HashMap map) {
		if (src == null)
			return null;
		CoreImage mask = null;
		String inputA = (String) getEffectProperties().get("INPUTA");
		if (src == null)
			return null;
		CoreImage srcImage = CoreImage.create(src);
		if (srcImage.getNumBands() == 4 && inputA.equalsIgnoreCase("RGB")) {
			mask = srcImage.copy().extractMask();
			srcImage.reduceToThreeBand();
		}
		int r = 255;
		int g = 0;
		int b = 0;
		int blendfactor = 255;
		EffectObject eObj = null;
		try {
			if (map.get(EffectConstants.EFFECTOBJECT) instanceof EffectObject) {

				eObj = (EffectObject) map.get(EffectConstants.EFFECTOBJECT);

				if (eObj == null)
					eObj = EffectObject
							.getEffect(EffectConstants.ADJUSTCHANNELS);
				r = new Integer((String) eObj
						.getAttributeValue(EffectConstants.REDFACTOR))
						.intValue();
				g = new Integer((String) eObj
						.getAttributeValue(EffectConstants.GREENFACTOR))
						.intValue();
				b = new Integer((String) eObj
						.getAttributeValue(EffectConstants.BLUEFACTOR))
						.intValue();
				blendfactor = new Integer((String) eObj
						.getAttributeValue(EffectConstants.BLENDFACTOR))
						.intValue();

			} else {
				LayerEffect le = (LayerEffect) map
						.get(EffectConstants.EFFECTOBJECT);
				if (le.getAttribute(EffectConstants.REDFACTOR) != null)
					r = new Integer(le.getAttribute(EffectConstants.REDFACTOR))
							.intValue();
				if (le.getAttribute(EffectConstants.GREENFACTOR) != null)
					g = new Integer(le
							.getAttribute(EffectConstants.GREENFACTOR))
							.intValue();
				if (le.getAttribute(EffectConstants.BLUEFACTOR) != null)
					b = new Integer(le.getAttribute(EffectConstants.BLUEFACTOR))
							.intValue();
				if (le.getAttribute(EffectConstants.BLENDFACTOR) != null)
					blendfactor = new Integer(le
							.getAttribute(EffectConstants.BLENDFACTOR))
							.intValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return src;
		}

		CoreImage image = srcImage.copy().colorize(r, g, b);

		image = srcImage.copy().composite(image, blendfactor);
		String output = (String) getEffectProperties().get(OUTPUT);
		if (mask != null && output.equalsIgnoreCase("RGBA"))
			image.bandMerge(mask);

		return image.getAwt();
	}

	public HashMap getEffectParameters(Map uiMap, Map attrMap, String type) {
		return getOutputParams(uiMap, attrMap, type);
	}

	public HashMap getEffectProperties() {
		return attributes;
	}

	public HashMap<Object, Object> getOutputParams(Map uiMap, Map attrMap,
			String type) {

		EffectObject eObj = EffectObject
				.getEffect(EffectConstants.ADJUSTCHANNELS);
		HashMap<Object, Object> eMap = eObj.getOutputAttributeMap();
		Set set = uiMap.keySet();
		Iterator iter = set.iterator();
		String valueRef = null;
		ParameterModel pm = null;
		while (iter.hasNext()) {
			String name = (String) iter.next();
			String value = "";
			if (uiMap.get(name) instanceof String)
				value = (String) uiMap.get(name);
			else if (uiMap.get(name) instanceof ParameterModel) {
				pm = (ParameterModel) uiMap.get(name);
				if (pm.isAnimatedModel() && type.equals(ANIMATION))
					valueRef = pm.getAttribute(ThemeTag.ELEMENT_VALUEMODEL_REF);
				else
					value = pm.getValue(name);
			}
			
			Map<Object, Object> map1 = ((HashMap) eMap.get(name));
			if (map1 != null) {
				if (valueRef != null) {
					map1.put(ThemeTag.ELEMENT_VALUEMODEL_REF, new String(
							valueRef));
					if (pm.getValue(name) != null)
						map1.put(EffectConstants.ATTR_DEFAULTVALUE, pm
								.getValue(name));
					valueRef = null;
				} else {
					map1.remove(ThemeTag.ELEMENT_VALUEMODEL_REF);
					map1.put(EffectConstants.ATTR_VALUE, value);
				}
			}
		}

		return eMap;
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
		String inputA = "" + currentPosition + FSLASH + RGB;
		String inputB = "none";
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
