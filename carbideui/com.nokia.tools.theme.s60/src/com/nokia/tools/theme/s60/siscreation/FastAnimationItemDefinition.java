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
package com.nokia.tools.theme.s60.siscreation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.LayerEffect;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.ThemeTag.SkinCompliance;
import com.nokia.tools.theme.s60.S60ThemePlugin;
import com.nokia.tools.theme.s60.effects.ClassLoader;
import com.nokia.tools.theme.s60.effects.EffectObject;
import com.nokia.tools.theme.s60.effects.ImageProcessor;
import com.nokia.tools.theme.s60.model.MorphedGraphic;
import com.nokia.tools.theme.s60.morphing.timemodels.BaseTimingModelInterface;
import com.nokia.tools.theme.s60.morphing.valuemodels.BaseValueModelInterface;

public class FastAnimationItemDefinition extends BasicItemDefinition {

	int previousLayerStackPos = 0;
	int currentLayerStackPos = 0;
	String lastLayerData = null;
	boolean fakeCall = false;
	private HashMap<Object, Object> modelMap = null;
	private boolean animatableStatus = false;

	/**
	 * Resets the internal data structures (As good as creating a new class and
	 * starting over)
	 */
	protected void reset() {
		super.reset();
		previousLayerStackPos = 0;
		currentLayerStackPos = 0;
		lastLayerData = null;

	}

	/*
	 * CONSTANTS DEFINITIONS USED IN SCALEABLE ITEM DEFINITION
	 */
	final String ANIMATION = "ANIMATION";
	final String IID = "IID";
	final String INPUT = "INPUT";
	final String INPUTA = "INPUTA";
	final String INPUTB = "INPUTB";
	final String OUTPUT = "OUTPUT";
	final String REFER = "REF";
	final String EFFECT = "EFFECT";
	final String RGB = "RGB";
	final String RGBA = "RGBA";
	final String A = "A";
	final String NONE = "none";
	final String REFIID = "REFIID";
	final String PREPROCESS = "PREPROCESS";
	final String MININTERVAL = "MININTERVAL";
	final String VALUE = "100";
	final String MORPHED = "MORPHED";
	final String MORPHING = "MORPHING";

	protected FastAnimationItemDefinition(Map properties) throws ThemeException {
		super(properties);
		this.animatableStatus = true;
		fakeCall = (Boolean.TRUE.equals((Boolean) properties
				.get(ThemeTag.KEY_FAKE_CALL)));

	}

	/**
	 * Generates the scaleable item defintion for the given skinnable enity.
	 * 
	 * @param sItem
	 * @return
	 * @throws ThemeException
	 */
	protected StringBuffer generateDefintion(SkinnableEntity sItem)
			throws ThemeException {

		errors.delete(0, errors.length());

		if (sItem == null) {
			errors.append("Input data is null");
			return null;
		}

		StringBuffer itemDefinition = null;
		SkinCompliance compliance = getComplianceLevel();

		switch (compliance) {
		case BITMAP_SKIN:
			itemDefinition = generateDefinitionForBitmapSkin(sItem);
			break;
		case SCALEABLE_SKIN:
			itemDefinition = generateDefinitionForAnimatedSkin(sItem);
			break;
		}

		return itemDefinition;
	}

	private StringBuffer generateDefinitionForAnimatedSkin(SkinnableEntity sItem)
			throws ThemeException {

		String itemId = (String) sItem.getAttributeValue(ThemeTag.ATTR_ID);
		if (itemId == null) {
			errors.append("Item id is null for input entity");
			return null;
		}

		ThemeGraphic itemGraphics = sItem.getPhoneThemeGraphic();
		if (itemGraphics == null) {
			return null;
		}
		String colourDepth = getColourDepth(sItem);

		List layerList = itemGraphics.getImageLayers();
		if (layerList == null || layerList.size() <= 0) {
			errors.append("Theme graphics has no layer data definition");
			return null;
		}

		StringBuffer effectsString = new StringBuffer();
		boolean usesBackground = false;
		StringBuffer valueModelString = getValueModelString(itemGraphics);
		StringBuffer timingModelString = getTimingModelString(itemGraphics);
		StringBuffer preProcessString = new StringBuffer();
		preProcessString.append(TAB).append(PREPROCESS).append(NL);

		for (int i = 0; i < layerList.size(); i++) {
			ImageLayer nextLayer = (ImageLayer) layerList.get(i);
			if (nextLayer == null) {
				errors.append(NL).append("Layer " + i + " data is null");
				continue;
			}

			boolean isBackground = nextLayer.isBackground();


			if (!isBackground) {
				String layerColourDepth = (String) nextLayer
						.getAttribute(ThemeTag.ATTR_COLOURDEPTH);

				nextLayer.setAttribute(ThemeTag.ATTR_COLOURDEPTH, colourDepth);

				// preProcessString
				StringBuffer applyGrpData = makeApplyEffectString(nextLayer,
						itemGraphics.isMorphedGraphic());

				if (applyGrpData != null) {
					boolean isValid = validateEffectString(applyGrpData);
					if (isValid == true) {
						preProcessString.append(TAB).append(TAB).append(
								applyGrpData).append(NL);
					}
					
				}

			} else { 
				// has a background layer
				usesBackground = true;
				
			}
			if (i == layerList.size() - 1) {
				preProcessString.append(NL).append(TAB).append(END).append(NL);
			}

			// Other effects
			List layerEffectList = nextLayer.getLayerEffects();
			for (int j = 0; layerEffectList != null
					&& j < layerEffectList.size(); j++) {
				LayerEffect nextEffect = (LayerEffect) layerEffectList.get(j);

				String nextEffectName = nextEffect
						.getAttribute(ThemeTag.ATTR_NAME);
				if (nextEffectName == null
						|| nextEffectName.trim().length() <= 0) {
					errors.append(NL).append(
							"Layer " + i + " effect :" + j + " has no name");
					continue;
				}

				Map nextEffectData = nextEffect.getAttributes();
				nextEffectData = (HashMap) ((HashMap) nextEffectData).clone();

				StringBuffer nextEffectString = makeEffectString(
						nextEffectName, nextEffectData);

				boolean isValid = validateEffectString(nextEffectString);
				if (isValid == true)
					effectsString.append(TAB).append(nextEffectString).append(
							NL);
			}

			previousLayerStackPos = currentLayerStackPos;
			currentLayerStackPos++;
		}

		// Create the scaleable item definition here
		StringBuffer scaleableItemDef = new StringBuffer();
		scaleableItemDef.append(ANIMATION).append(SPACE);
		scaleableItemDef.append(IID).append(EQUAL).append(makeItemId(itemId))
				.append(SPACE);

		if ((effectsString == null || effectsString.length() <= 0)) {
			
			return null;
		} else // meaning no see through effect
		{
			scaleableItemDef.append(INPUT).append(EQUAL);
			if (usesBackground) {
				scaleableItemDef.append("0/RGB");
			} else {
				scaleableItemDef.append(NONE);
			}

			if (animatableStatus) {
				scaleableItemDef.append(SPACE);
				scaleableItemDef.append(OUTPUT).append(EQUAL).append(
						modifyLayerInfoString(lastLayerData)).append(SPACE);
				scaleableItemDef.append(MININTERVAL).append(EQUAL)
						.append(VALUE).append(SPACE).append(NL);
				scaleableItemDef.append(preProcessString).append(NL);
				scaleableItemDef.append(effectsString);
				scaleableItemDef.append(valueModelString);
				scaleableItemDef.append(timingModelString);
				
				scaleableItemDef.append(END);
			} else {
				scaleableItemDef.append(SPACE);
				scaleableItemDef.append(OUTPUT).append(EQUAL).append(
						modifyLayerInfoString(lastLayerData)).append(SPACE);
				scaleableItemDef.append(MININTERVAL).append(EQUAL)
						.append(VALUE).append(SPACE).append(MORPHING)
						.append(NL);
				scaleableItemDef.append(preProcessString).append(NL);
				scaleableItemDef.append(effectsString);
				scaleableItemDef.append(valueModelString);
				scaleableItemDef.append(timingModelString);
				
				scaleableItemDef.append(END);
			}

		}

		return scaleableItemDef;
	}

	private StringBuffer getValueModelString(ThemeGraphic itemGraphics) {
		modelMap = new HashMap<Object, Object>();
		ArrayList list = (ArrayList) ((MorphedGraphic) itemGraphics)
				.getValueModels();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			BaseValueModelInterface valueModel = (BaseValueModelInterface) list
					.get(i);
			buf.append(valueModel.getValueModelString(0, i, null)).append(NL);
			modelMap.put(new Integer(i), valueModel);
			
		}

		return buf;
	}

	private StringBuffer getTimingModelString(ThemeGraphic itemGraphics) {
		ArrayList list = (ArrayList) ((MorphedGraphic) itemGraphics)
				.getTimingModels();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			BaseTimingModelInterface timeModel = (BaseTimingModelInterface) list
					.get(i);
			buf.append(timeModel.getTimingModelString(0, 0));
		}

		return buf;
	}

	/**
	 * Function that generates the bitmap item defintion from scaleable skin
	 * definition
	 */
	private StringBuffer generateDefinitionForBitmapSkin(SkinnableEntity sItem)
			throws ThemeException {

		String itemId = (String) sItem.getAttributeValue(ThemeTag.ATTR_ID);
		if (itemId == null) {
			errors.append("Item id is null for input entity");
			return null;
		}

		ThemeGraphic itemGraphics = sItem.getPhoneThemeGraphic();
		if (itemGraphics == null) {
			return null;
		}
		String colourDepth = getColourDepth(sItem);

		/*
		 * Steps followed here 1. Get the planarimage that represents the 'all
		 * layer data' processed image. 2. Save the image and its mask (if any)
		 * 3. Write the bitmap definition for the entity.
		 */

		CoreImage finalPlImage = CoreImage.create(itemGraphics
				.getProcessedImage(sItem, (String) null, true, true));
		if (finalPlImage.getAwt() == null)
			return null;

		CoreImage finalPlMask = finalPlImage.copy().extractMask();

		// HERE SAVE THE GENERATED IMAGES
		String sourceName = itemId.toLowerCase().trim() + BMP_EXT;
		String maskName = generateMaskFileName(sourceName, true);

		String outputDir = (String) properties.get(ThemeTag.KEY_OUTPUT_DIR);
		outputDir = (outputDir == null) ? "." : outputDir;

		try {
			finalPlImage.save(CoreImage.TYPE_BMP, new File(outputDir
					+ File.separator + sourceName));
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		}
		try {
			finalPlMask.save(CoreImage.TYPE_BMP, new File(outputDir
					+ File.separator + maskName));
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		}

		// WRITE THE BITMAP DEFINTION
		StringBuffer itemDefinition = new StringBuffer();
		itemDefinition.append(ThemeTag.SKN_TAG_BITMAP).append(SPACE);
		itemDefinition.append(IID).append(EQUAL).append(makeItemId(itemId))
				.append(SPACE);
		itemDefinition.append(colourDepth).append(SPACE);
		itemDefinition.append(sourceName).append(SPACE);
		if (finalPlMask != null)
			itemDefinition.append(ThemeTag.SKN_TAG_SOFTMASK).append(EQUAL)
					.append(maskName).append(SPACE);
		itemDefinition.append(NL);

		return itemDefinition;
	}

	/**
	 * Returns the stringbuffer defining 'ApplyGraphics' or 'ApplyColor' effect.
	 * In skin tool a layer can be represented either by ApplyGraphics or
	 * ApplyColor effect only. So every layer (except background layer) passes
	 * through this function to convert the layer data to either 'ApplyGraphics'
	 * or 'ApplyColor' effect. To differentiate between the two effects if the
	 * layer has a file associated with it then it is treated as 'ApplyGraphics'
	 * otherwise it is treated as 'ApplyColor'
	 * 
	 * @param layerData The layer data
	 * @param colourDepth The colour depth string
	 * @return The string buffer containing the apply graphics definition.
	 * @throws ThemeException
	 */
	private StringBuffer makeApplyEffectString(ImageLayer layerData,
			boolean isMorphed) throws ThemeException {

		String fileName = (String) layerData.getAttribute(ThemeTag.FILE_NAME);
		if (!fakeCall && fileName != null) {
			copyFile(fileName, null);
		}

		String hardMaskFileName = (String) layerData
				.getAttribute(ThemeTag.ATTR_HARDMASK);
		String softMaskFileName = (String) layerData
				.getAttribute(ThemeTag.ATTR_SOFTMASK);

		if (!fakeCall && softMaskFileName != null) {
			copyFile(softMaskFileName, null);
		}

		if (!fakeCall && hardMaskFileName != null) {
			copyFile(hardMaskFileName, null);
		}

		StringBuffer applyEffectStr = null;
		Map applyEffValues = layerData.getAttributes();
		applyEffValues = (HashMap) ((HashMap) applyEffValues).clone();

		if (fileName != null) { // Then it is an ApplyGraphics effect
			applyEffectStr = makeEffectString("ApplyGraphics", applyEffValues);
		}
		

		return applyEffectStr;
	}

	/**
	 * Returns the EFFECT definition in a stringbuffer data
	 * 
	 * @param effectName The name of the effect
	 * @param effectValues The values set for this effect
	 * @throws ThemeException
	 */
	private StringBuffer makeEffectString(String effectName,
			Map<Object, Object> effectValues) throws ThemeException {

		EffectObject effObj = EffectObject.getEffect(effectName);
		// #jpeknik #change
		ImageProcessor im = ClassLoader.getConversionsInstance(effObj
				.getAttributeAsString("className"));
		effectValues.put(MORPHED, "true");
		effectValues.put(ThemeTag.KEY_OVERWRITE_INPUT, true);
		effectValues.put(ThemeTag.KEY_VALUE_MODEL_DATA, modelMap);
		StringBuffer effectStr = im.getEffectString(previousLayerStackPos,
				currentLayerStackPos, effectValues);
		return effectStr;
	}

	/**
	 * This function validates if the effect string supplied by the effects
	 * implementation classes can be accepted. Checks done are 1. If the output
	 * layer is using a position number that is either the current position or
	 * one more than the current position. 2. If the inputA if used is the
	 * current position value. The function also determines the output channel
	 * set in the effects string.
	 * 
	 * @param effectString The effect string
	 * @return true if it the effect string is acceptable
	 */
	private boolean validateEffectString(StringBuffer effectString) {
		String keyWord = " OUTPUT=";
		int outputPos = effectString.indexOf(keyWord);
		if (outputPos == -1)
			return false;

		int nextSpace = effectString.indexOf(NL, outputPos + keyWord.length());
		String outputString = effectString.substring(outputPos
				+ keyWord.length(), nextSpace);

		int slashPos = outputString.indexOf(FSLASH);
		String effectStackString = outputString.substring(0, slashPos);

		int effectStackPos = Integer.parseInt(effectStackString);
		lastLayerData = outputString;
		if (effectStackPos != currentLayerStackPos
				&& effectStackPos != (currentLayerStackPos + 1))
			return false;

		currentLayerStackPos = effectStackPos;

		return true;
	}

	private String modifyLayerInfoString(String layerInfoString) {
		if (layerInfoString == null) {
			return null;
		}
		String s = layerInfoString.substring(0, layerInfoString
				.lastIndexOf("/") + 1);
		return s.concat(RGB);
	}
}
