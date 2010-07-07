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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nokia.tools.platform.theme.Element;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.core.MultiPieceManager;
import com.nokia.tools.theme.s60.general.ThemeUtils;
import com.nokia.tools.theme.s60.model.S60Theme;
import com.sun.org.omg.CORBA.ParDescriptionSeqHelper;

/**
 * The file provides frame writing functionality
 */

class FrameItemDefinition
    extends BasicItemDefinition {

	/**
	 * Default constructor.
	 * 
	 * @throws ThemeException
	 */
	protected FrameItemDefinition(Map properties) throws ThemeException {
		super(properties);
	}

	/**
	 * Resets the internal data structures (As good as creating a new class and
	 * starting over)
	 */
	protected void reset() {
		super.reset();
	}

	/**
	 * Generates the icons string for the given entity data
	 * 
	 * @param cEntity The SkinnableEntity containing the icon whose text output
	 *            has to be generated.
	 * @param voidElements parts that are already writen and need to be void
	 */
	protected StringBuffer generateDefintion(SkinnableEntity cEntity,
	    Set<String> voidElements) throws ThemeException {

		reset();

		if (cEntity == null) {
			errors.append("Input data is null");
			return null;
		}

		SkinnableEntity cEntityToGenerate = cEntity;
		String masterId = (String) cEntity.getAttribute().get(
		    ThemeTag.ATTR_MASTERID);
		if (masterId != null) {
			cEntityToGenerate = ((S60Theme) cEntity.getRoot())
			    .getSkinnableEntity(masterId);
		}

		String itemId = (String) cEntity.getAttributeValue(ThemeTag.ATTR_ID);
		if (itemId == null)
			return null;

		boolean isFrame = checkIsFrame(cEntityToGenerate);
		if (isFrame == false)
			return null;

		/*
		 * Check the property to see if it should be written as a single bitmap
		 * entity or a 9 piece entity. If it is a single piece entry then it
		 * should be written as a normal bitmap.
		 */
		boolean writeAsOnePieceFrame = isToBeWrittenAsOnePieceFrame(cEntity);

		if (writeAsOnePieceFrame == true && cEntity.isSkinned()) {
			
			StringBuffer graphicsDef = null;
			BitmapItemDefinition bmpDef = new BitmapItemDefinition(properties);
	
			
			//if((isElevenPiece(cEntity) || isThreePiece(cEntity)) ){
			if (!isFrame) {
				graphicsDef = writeMultiPieceElementsAsSingleElements(
                    cEntityToGenerate, bmpDef);
			}else{
				/*
				 * If the frame is a one piece item then it should be written as a
				 * bitmap element.
				 */
				graphicsDef = bmpDef.generateDefinition(cEntity);
				errors.append(bmpDef.getErrorString());
			}
			return graphicsDef;

		} else {
			/*
			 * The nine piece frame definition.
			 */
			Map<Object, Object> partDefMap = new HashMap<Object, Object>();
			Map<Object, Object> partIdMap = new HashMap<Object, Object>();
			boolean isAllCorrect = true;

			List partsList = cEntityToGenerate.getChildren();

			for (int i = 0; i < partsList.size(); i++) {
				SkinnableEntity nextPart = (SkinnableEntity) partsList.get(i);
				String partId = (String) nextPart
				    .getAttributeValue(ThemeTag.ATTR_ID);
				String position = ((String) nextPart.getAttributeValue(ThemeTag.ATTR_NAME)).toLowerCase();
                StringBuffer partDef = null;
				if ((position.equalsIgnoreCase("tl"))
				    || (position.equalsIgnoreCase("tr"))
				    || (position.equalsIgnoreCase("bl"))
				    || (position.equalsIgnoreCase("br"))) {
					partDef = generateCorner(position, nextPart);
				} else if ((position.equalsIgnoreCase("l"))
				    || (position.equalsIgnoreCase("r"))) {
					partDef = generateLorRside(position, nextPart);
				} else if ((position.equalsIgnoreCase("t"))
				    || (position.equalsIgnoreCase("b"))) {
					partDef = generateTorBside(position, nextPart);
				} else if (position.equalsIgnoreCase("c")) {
					partDef = generateCenter(nextPart);
				}

				if (partDef == null || position == null) {
					isAllCorrect = false;
					break;
				}

				partDefMap.put(position, partDef);
				partIdMap.put(position, partId);
			}

			if (isAllCorrect == false) {
				return null;
			}

			StringBuffer frameDef = new StringBuffer();

			// Add the bitmap entry for each part
			for (int i = 0; i < ThemeUtils.partsOrder.length; i++) {
				String partId = (String) partIdMap
				    .get(ThemeUtils.partsOrder[i]);
				if (!voidElements.contains(partId)) {
					StringBuffer partDef = (StringBuffer) partDefMap
					    .get(ThemeUtils.partsOrder[i]);
					frameDef.append(partDef);
				}
			}

			frameDef.append(NL);
			// Create the frame structure
			frameDef.append(ThemeTag.SKN_TAG_FRAME).append(SPACE);
			frameDef.append(ThemeTag.SKN_TAG_IID).append(EQUAL);
			frameDef.append(makeItemId(itemId)).append(NL);

			for (int i = 0; i < ThemeUtils.partsOrder.length; i++) {
				String partId = (String) partIdMap
				    .get(ThemeUtils.partsOrder[i]);
				String partIidDef = ThemeTag.SKN_TAG_IID + EQUAL
				    + makeItemId(partId);
				frameDef.append(TAB).append(partIidDef).append(NL);
			}

			frameDef.append(ThemeTag.SKN_TAG_END);

			return frameDef;
		}
	}

	

	private boolean isToBeWrittenAsOnePieceFrame(SkinnableEntity cEntity) {
		
	    return ThemeTag.ATTR_SINGLE_BITMAP
		    .equalsIgnoreCase(cEntity.getCurrentProperty())
		    || !MultiPieceManager.isFrameRequired(cEntity.getCurrentProperty());
		  
    }

	private StringBuffer writeMultiPieceElementsAsSingleElements(
        SkinnableEntity cEntityToGenerate, BitmapItemDefinition bmpDef)
        throws ThemeException {
	    StringBuffer graphicsDef = new StringBuffer();
	    List partsList = cEntityToGenerate.getChildren();
	    
	    for (int i = 0; i < partsList.size(); i++) {
	    	SkinnableEntity part = (SkinnableEntity) partsList.get(i);
	    	graphicsDef.append(bmpDef.generateDefinition(part).toString());
	    	errors.append(bmpDef.getErrorString());
	    }
	    return graphicsDef;
    }

	/**
	 * Checks if it is a frame data. The function checks if the element has nine
	 * parts with predefined name pattern
	 * 
	 * @param parts A list containing the parts
	 */
	protected static boolean checkIsFrame(SkinnableEntity element) {

		List parts = element.getChildren();
		return MultiPieceManager.isFrameRequired(parts.size());
		
		
	}

	/**
	 * Generates corner defintion
	 */
	private StringBuffer generateCorner(String position,
	    SkinnableEntity cornerEntity) throws ThemeException {
		BitmapItemDefinition bmpDef = new BitmapItemDefinition(properties);

		

		StringBuffer entAttrName = new StringBuffer();
		StringBuffer entAttrValue = new StringBuffer();
		setEntityAttribute(position, cornerEntity, entAttrName, entAttrValue);
		bmpDef.attributeString(entAttrName.toString(), entAttrValue.toString());

		StringBuffer graphicsDef = bmpDef.generateDefinition(cornerEntity);
		errors.append(bmpDef.getErrorString());
		return graphicsDef;
	}

	/**
	 * Generates side piece defintion
	 */
	private StringBuffer generateLorRside(String position,
	    SkinnableEntity sideEntity) throws ThemeException {
		BitmapItemDefinition bmpDef = new BitmapItemDefinition(properties);

		StringBuffer entAttrName = new StringBuffer();
		StringBuffer entAttrValue = new StringBuffer();
		setEntityAttribute(position, sideEntity, entAttrName, entAttrValue);
		bmpDef.attributeString(entAttrName.toString(), entAttrValue.toString());

		StringBuffer graphicsDef = bmpDef.generateDefinition(sideEntity);
		errors.append(bmpDef.getErrorString()).append(NL);
		return graphicsDef;
	}

	/**
	 * Generates side piece defintion
	 */
	private StringBuffer generateTorBside(String position,
	    SkinnableEntity sideEntity) throws ThemeException {
		BitmapItemDefinition bmpDef = new BitmapItemDefinition(properties);

		StringBuffer entAttrName = new StringBuffer();
		StringBuffer entAttrValue = new StringBuffer();
		setEntityAttribute(position, sideEntity, entAttrName, entAttrValue);
		bmpDef.attributeString(entAttrName.toString(), entAttrValue.toString());

		StringBuffer graphicsDef = bmpDef.generateDefinition(sideEntity);
		errors.append(bmpDef.getErrorString()).append(NL);
		return graphicsDef;
	}

	/**
	 * Generates center defintion
	 */
	private StringBuffer generateCenter(SkinnableEntity centerEntity)
	    throws ThemeException {
		BitmapItemDefinition bmpDef = new BitmapItemDefinition(properties);

		StringBuffer entAttrName = new StringBuffer();
		StringBuffer entAttrValue = new StringBuffer();
		setEntityAttribute("center", centerEntity, entAttrName, entAttrValue);
		bmpDef.attributeString(entAttrName.toString(), entAttrValue.toString());

		StringBuffer graphicsDef = bmpDef.generateDefinition(centerEntity);
		errors.append(bmpDef.getErrorString()).append(NL);
		return graphicsDef;
	}

	/**
	 * Generates the attribute defintion for the entity The function will read
	 * the attribute setting from the correct special entites file (so that the
	 * attributes can be differentiated for each S60 version) and sets its into
	 * the itemDefinition object used. The function returns the atribute and its
	 * value through the object references
	 * 
	 * @param position The position of the frame entity
	 * @param entAttrName The name of the chosen attribute (that has to be
	 *            written along with the entity definition)
	 * @param entAttrValue The value of the chosen attribute (that has to be
	 *            written along with the entity definition)
	 */
	private void setEntityAttribute(String position, SkinnableEntity entity,
	    StringBuffer entAttrName, StringBuffer entAttrValue)
	    throws ThemeException {
		/*
		 * The function does the following 1. Reads the properties file and get
		 * the name of the special entities class. 2. Creates a reference object
		 * to the class (to access its static members) 3. Reads the default
		 * attribute for the particular frame position from the created entity
		 * class reference 4. Returns the attribute name and its value back to
		 * the calling function.
		 */

		entAttrName.delete(0, entAttrName.length());
		entAttrValue.delete(0, entAttrValue.length());

		// Create the reference to the special entities class
		Map entityAttrProp = (Map) properties.get(ThemeTag.KEY_ENTITY_PROP);
		if (entityAttrProp == null)
			return;

		String lookupStr = "frame_" + position;
		String attrName = (String) entityAttrProp.get(lookupStr);
		System.out.println("New stuff is " + attrName + " for "
		    + entity.getIdentifier());

		if (attrName == null)
			return;

		entAttrName.append(attrName);

		// Now set the appropriate atribute value
		if (attrName.equalsIgnoreCase(ThemeTag.SKN_TAG_ALIGN))
			entAttrValue.append(position);

		return;
	}
}
