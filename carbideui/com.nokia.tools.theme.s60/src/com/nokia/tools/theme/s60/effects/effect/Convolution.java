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
public class Convolution implements ImageProcessor {

	private final String effectUID = "0x10204AE2";
	
	private HashMap<String, String> map1 = new HashMap<String, String>();
	private float divideFactor = 14.0F;
	private float eDetail = 5.0F;
	private float eFocus = 3.0F;

	float[] EdgeData = { -1.0F, -1.0F, -1.0F, -1.0F, 8.0F, -1.0F, -1.0F, -1.0F,
			-1.0F };
	float[] BlurData = { 1 / divideFactor, 2 / divideFactor, 1 / divideFactor,
			2 / divideFactor, 2 / divideFactor, 2 / divideFactor,
			1 / divideFactor, 2 / divideFactor, 1 / divideFactor };
	float[] EmbossData = { -1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F,
			1.0F };
	float[] EmbossHardData = { -1.0F, -1.0F, 0.0F, -1.0F, 0.0F, 1.0F, 0.0F,
			1.0F, 1.0F };
	float[] SharpenData = { 0.0F, -1.0F, 0.0F, -1.0F, 5.0F, -1.0F, 0.0F, -1.0F,
			0.0F };
	float[] SharpenMore = { -1.0F, -1.0F, -1.0F, -1.0F, 9.0F, -1.0F, -1.0F,
			-1.0F, -1.0F };
	float[] gauss = { 0F, 0F, 1F, 1F, 1F, 1F, 1F, 0F, 0F, 0F, 1F, 2F, 3F, 3F,
			3F, 2F, 1F, 0F, 1F, 2F, 3F, 6F, 7F, 6F, 3F, 2F, 1F, 1F, 3F, 6F, 9F,
			11F, 9F, 6F, 3F, 1F, 1F, 3F, 7F, 11F, 12F, 11F, 7F, 3F, 1F, 1F, 3F,
			6F, 9F, 11F, 9F, 6F, 3F, 1F, 1F, 2F, 3F, 6F, 7F, 6F, 3F, 2F, 1F,
			0F, 1F, 2F, 3F, 3F, 3F, 2F, 1F, 0F, 0F, 0F, 1F, 1F, 1F, 1F, 1F, 0F,
			0F };
	float[] Enhance = { 0.0F, -1.0F / eDetail, 0.0F, -1.0F / eDetail,
			9.0F / eDetail, -1.0F / eDetail, 0.0F, -1.0F / eDetail, 0.0F };
	float[] EnhanceFocus = { -1 / eFocus, 0.0F, -1.0F / eFocus, 0.0F,
			7.0F / eFocus, 0.0F, -1.0F / eFocus, 0.0F, -1.0F / eFocus };
	float[] BlurGauss = { 0.0F, 1.0F / 8, 0.0F, 1.0F / 8, 4.0F / 8, 1.0F / 8,
			0.0F, 1.0F / 8, 0.0F };

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.effects.ImageProcessor#doProcessing(java.awt.image.RenderedImage,
	 *      java.util.HashMap)
	 */
	public RenderedImage doProcessing(RenderedImage src, HashMap map) {
		int blendfactor = 255;

		String inputA = (String) getEffectProperties().get("INPUTA");
		if (src == null)
			return null;

		CoreImage main = CoreImage.create(src);
		CoreImage mask = null;
		if (main.getNumBands() == 4 && inputA.equalsIgnoreCase("RGB")) {
			mask = main.copy().extractMask();
			main.reduceToThreeBand();
		}

		String convConst = EffectConstants.EDGEDETECT;
		if (map.get(EffectConstants.EFFECTOBJECT) instanceof EffectObject) {

			EffectObject eObj = (EffectObject) map
					.get(EffectConstants.EFFECTOBJECT);
			if (eObj == null)
				eObj = EffectObject.getEffect(EffectConstants.CONVOLUTION);
			convConst = (String) eObj
					.getAttributeValue(EffectConstants.CONVOLUTIONCONSTANT);
			if (convConst == null)
				convConst = ((String) map
						.get(EffectConstants.CONVOLUTIONCONSTANT));

			blendfactor = new Integer((String) eObj
					.getAttributeValue(EffectConstants.BLENDFACTOR)).intValue();
		} else {
			LayerEffect le = (LayerEffect) map
					.get(EffectConstants.EFFECTOBJECT);
			if (le.getAttribute(EffectConstants.CONVOLUTIONCONSTANT) != null)
				convConst = le
						.getAttribute(EffectConstants.CONVOLUTIONCONSTANT);
			blendfactor = new Integer(le
					.getAttribute(EffectConstants.BLENDFACTOR)).intValue();
		}
		if (convConst == null)
			convConst = EffectConstants.EDGEDETECT;

		if (convConst.trim() == "")
			return null;
		float[] data = null;
		CoreImage copy = main.copy();
		if (convConst.equalsIgnoreCase(EffectConstants.EDGEDETECT))
			data = EdgeData;
		else if (convConst.equalsIgnoreCase(EffectConstants.BLUR))
			data = BlurData;
		else if (convConst.equalsIgnoreCase(EffectConstants.BLURGAUSS))
			data = BlurGauss;
		else if (convConst.equalsIgnoreCase(EffectConstants.EMBOSSHARD))
			data = EmbossHardData;
		else if (convConst.equalsIgnoreCase(EffectConstants.EMBOSSSOFT))
			data = EmbossData;
		else if (convConst.equalsIgnoreCase(EffectConstants.SHARPEN))
			data = SharpenData;
		else if (convConst.equalsIgnoreCase(EffectConstants.SHARPENMORE))
			data = SharpenMore;
		else if (convConst.equalsIgnoreCase(EffectConstants.ENHANCEDETAIL))
			data = Enhance;
		else if (convConst.equalsIgnoreCase(EffectConstants.ENHANCEFOCUS))
			data = EnhanceFocus;

		else if (convConst.equalsIgnoreCase(EffectConstants.SOFTEN)) {
			float[] data1 = new float[9];
			for (int k = 0; k < data1.length; k++)
				data1[k] = 1.0f / (3 * 3);
			data = data1;
		} else if (convConst.equalsIgnoreCase(EffectConstants.MEAN))
			copy.mean();
		else if (convConst.equalsIgnoreCase(EffectConstants.MEDIAN))
			copy.median();
		else if (convConst.equalsIgnoreCase(EffectConstants.DILATE))
			copy.dilate();
		else if (convConst.equalsIgnoreCase(EffectConstants.ERODE))
			copy.erode();
		if (data != null)
			copy.convolve(data);

		if (data == EmbossHardData || data == EmbossData) {
			double[] constants = new double[3];
			constants[0] = 127.0;
			constants[1] = 127.0;
			constants[2] = 127.0;
			copy.addConst(constants);
		}
		CoreImage image = main.copy().composite(copy, blendfactor);
		String output = (String) getEffectProperties().get(OUTPUT);
		if (mask != null && output.equalsIgnoreCase("RGBA"))
			image.bandMerge(mask);
		return image.getAwt();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.effects.ImageProcessor#setAttributeValues(java.util.HashMap)
	 */
	public HashMap<Object, Object> getOutputParams(Map uiMap, Map attrMap,
			String type) {

		EffectObject eObj = EffectObject.getEffect(EffectConstants.CONVOLUTION);
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
			HashMap<Object, Object> map1 = (HashMap<Object, Object>) eMap
					.get(name);
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

	public HashMap getEffectParameters(Map uiMap, Map attrMap, String type) {
		return getOutputParams(uiMap, attrMap, type);
	}

	public HashMap getEffectProperties() {
		map1.put("INPUTA", "RGB");
		map1.put("INPUTB", "none");
		map1.put("OUTPUT", "RGBA");
		return map1;
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
