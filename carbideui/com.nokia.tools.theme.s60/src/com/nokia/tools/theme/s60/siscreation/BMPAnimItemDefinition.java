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

import java.util.List;
import java.util.Map;

import com.nokia.tools.platform.theme.AnimatedThemeGraphic;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.ThemeTag.SkinCompliance;

/**
 * The file provides frame writing functionality 
 */ 

class BMPAnimItemDefinition extends BasicItemDefinition {

	/**
	 * Default constructor.
	 * @throws ThemeException
	 */
	protected BMPAnimItemDefinition(Map properties) throws ThemeException {
		super(properties);
	}
	
	/**
	 * Resets the internal data structures (As good as creating a new class and 
	 * starting over)
	 */
	protected void reset(){
		super.reset();	
	}
	
	/**
	 * Generates the icons string for the given entity data
	 * @param cEntity The SkinnableEntity containing the icon whose text output has to be generated.
	 */
	protected StringBuffer generateDefintion (SkinnableEntity cEntity, SpecialEntitiesHandler splEntitiesProcessorObj) throws ThemeException {
		
		reset();
		
		if (cEntity == null){
			errors.append("Input data is null");
			return null;
		}
		
		String itemId = (String)cEntity.getAttributeValue (ThemeTag.ATTR_ID);
		itemId = splEntitiesProcessorObj.returnCorrectedId(itemId);
	
		if (itemId == null)
			return null;
		
		StringBuffer animationDefn = null;
		
		SkinCompliance compliance = getComplianceLevel();
		switch (compliance){
		case BITMAP_SKIN: 
			animationDefn = generateAnimationStringForBitmapSkin(cEntity, splEntitiesProcessorObj);
			break;
		case SCALEABLE_SKIN:
			animationDefn = generateAnimationStringForScaleableSkin(cEntity);
			break;
		}
		
		return animationDefn;
	}

	/**
	 * Generates the animation string
	 */
	private StringBuffer generateAnimationStringForBitmapSkin(SkinnableEntity cEntity, SpecialEntitiesHandler splEntitiesProcessorObj) throws ThemeException {
		return generateAnimationString(cEntity, splEntitiesProcessorObj);
	}
	
	/**
	 * Generates the animation string
	 */
	private StringBuffer generateAnimationStringForScaleableSkin(SkinnableEntity cEntity) throws ThemeException {
		return generateAnimationString(cEntity,null);
	}

	private StringBuffer generateAnimationString (SkinnableEntity cEntity, SpecialEntitiesHandler splEntitiesProcessorObj) throws ThemeException {
		if (cEntity == null)
			return null;
		
		String itemId = (String)cEntity.getAttributeValue (ThemeTag.ATTR_ID);
		if(splEntitiesProcessorObj != null)
			itemId = splEntitiesProcessorObj.returnCorrectedId(itemId);
		if (itemId == null)
			return null;
		
		
		
		AnimatedThemeGraphic animGrp = cEntity.getAnimatedThemeGraphic();
		if (animGrp == null)
			return null;
		
		List animGrpList = ((AnimatedThemeGraphic)animGrp).getThemeGraphics();
		if (animGrpList == null || animGrpList.size() <= 0)
			return null;
		
		String animMode = (cEntity.getAttributeValue(ThemeTag.ATTR_ANIM_MODE) != null) ? cEntity.getAttributeValue(ThemeTag.ATTR_ANIM_MODE).toLowerCase() : "cycle";
		
		String animType = cEntity.getCurrentProperty();
		
		BitmapItemDefinition bmpDefObj = new BitmapItemDefinition(properties);
		StringBuffer animFrames = new StringBuffer();
		
		for (int i=0; i<animGrpList.size(); i++){
			ThemeGraphic nextGrp = (ThemeGraphic)animGrpList.get(i);
			String frTime = nextGrp.getAttribute(ThemeTag.ATTR_ANIMATE_TIME);
		
			bmpDefObj.reset();
			StringBuffer nextFrDef = bmpDefObj.generateBitmapDefinition(cEntity, nextGrp,true);
			errors.append(bmpDefObj.getErrorString()).append(NL);
			
			if (nextFrDef != null){
				animFrames.append(TAB);				
				if (frTime != null){
					animFrames.append ("TIME").append(EQUAL).append(frTime).append(SPACE);
				}				
				animFrames.append (nextFrDef).append(NL);
			}
			if(animType.equalsIgnoreCase(ThemeTag.ATTR_STILL))
				break;
		}
		
		StringBuffer animationDef = null;
		if (animFrames != null && animFrames.length() > 0){
			animationDef = new StringBuffer();
			animationDef.append("BMPANIM").append(SPACE);
			animationDef.append("IID").append(EQUAL).append(makeItemId(itemId)).append(SPACE);
			animationDef.append("INTERVAL").append(EQUAL).append(ThemeTag.ATTR_ANIMATE_DEFAULT_TIME).append(SPACE);
			
			if (animMode != null){
				animationDef.append("MODE").append(EQUAL).append(animMode).append(SPACE);
			}
			
			animationDef.append(NL).append(animFrames);
			animationDef.append("END");
		}
		
		return animationDef;
	}
}
