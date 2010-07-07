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
 * ======================================================================================
 * The file provides colour text file writing functionality
 * ======================================================================================
 */

package com.nokia.tools.theme.s60.siscreation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.nokia.tools.platform.theme.ColourGraphic;
import com.nokia.tools.platform.theme.Component;
import com.nokia.tools.platform.theme.ComponentGroup;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Task;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.ThemeTag.SkinCompliance;

/**
 * The file provides colour text file writing functionality
 */
class ColourItemDefinition
    extends BasicItemDefinition {

	Map<Object, Object> colourMap = new HashMap<Object, Object>();

	Map<String, ColourGroup> colourGroups = new HashMap<String, ColourGroup>();


	static class ColourGroup {

		String id;

		String majorid;

		String minorid;

		Set<Colour> colours = new TreeSet<Colour>();

		ColourGroup(String id) {
			this.id = id;
		}

		void setMajorMinorId(String majorId, String minorId) {
			if (majorId != null && majorId.length() > 0) {
				this.majorid = majorId;
			}

			if (minorId != null && minorId.length() > 0) {
				this.minorid = minorId;
			}
		}
	}


	static class Colour
	    implements Comparable<Colour> {

		String id;

		Integer idx;

		String definition;

		Colour(String id, Integer idx, String definition) {
			this.id = id;
			this.idx = idx;
			this.definition = definition;
		}

		public int compareTo(Colour o) {
			return this.idx.compareTo(o.idx);
		}
		
		public String toString(){
			return id + " , " + idx + " , " + definition;
		}
	}

	/**
	 * Default constructor.
	 * 
	 * @throws ThemeException
	 */
	protected ColourItemDefinition(Map properties) throws ThemeException {
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
	 * Generates the scaleable item defintion for the given skinnable enity.
	 * 
	 * @param sItem
	 * @return
	 * @throws ThemeException
	 */
	protected StringBuffer generateDefintion(Task cTask) throws ThemeException {

		super.reset();

		if (cTask == null) {
			errors.append("Input data is null");
			return null;
		}

		accumulateColourData(cTask);
		if (colourMap == null || colourMap.size() <= 0)
			return null;

		StringBuffer colDef = null;
		SkinCompliance compliance = getComplianceLevel();
		switch (compliance) {
			case BITMAP_SKIN:
				colDef = generateColourStringForBitmapSkin();
				break;
			case SCALEABLE_SKIN:
				colDef = generateColourStringForScaleableSkin();
				break;
		}

		return colDef;
	}

	final String COLORTABLE = "COLORTABLE";

	/**
	 * Generates the colour string
	 */
	private StringBuffer generateColourStringForBitmapSkin() {
		StringBuffer qsnComponentColour = generateOldFormatColourString("QsnComponentColors");
		return qsnComponentColour;
	}

	/**
	 * Generates the colour string
	 */
	private StringBuffer generateColourStringForScaleableSkin() {
		StringBuffer colDef = new StringBuffer();

		Map<String, String> iidMap = new HashMap<String, String>();

		StringBuffer qsnComponentColour = generateOldFormatColourString("QsnComponentColors");
		if (qsnComponentColour != null) {
			iidMap.put("QsnComponentColors", qsnComponentColour.toString());
		}

		StringBuffer qsnIconColour = generateIconColourString("QsnIconColors");
		if (qsnIconColour != null) {
			iidMap.put("QsnIconColors", qsnIconColour.toString());
		}

		StringBuffer qsnTextColour = generateTextColourString("QsnTextColors");
		if (qsnTextColour != null) {
			iidMap.put("QsnTextColors", qsnTextColour.toString());
		}

		StringBuffer qsnLineColour = generateLineColourString("QsnLineColors");
		if (qsnLineColour != null) {
			iidMap.put("QsnLineColors", qsnLineColour.toString());
		}

		StringBuffer qsnOtherColour = generateOtherColourString("QsnOtherColors");
		if (qsnOtherColour != null) {
			iidMap.put("QsnOtherColors", qsnOtherColour.toString());
		}

		StringBuffer qsnHighlightColour = generateHighlightedItemColourString("QsnHighlightColors");
		if (qsnHighlightColour != null) {
			iidMap.put("QsnHighlightColors", qsnHighlightColour.toString());
		}

		generateCustomColourString(iidMap);
		
		colDef = makeString(iidMap);
		return colDef;
	}

	/**
	 * @param iidMap
	 * @return
	 */
	private StringBuffer makeString(Map<String, String> iidMap) {
		StringBuffer result = new StringBuffer();
		for (String iidString : iidMap.values()) {
			result.append(iidString).append(NL);
		}
		return result;
	}

	/**
	 * Function gathers all the entities within the colour components reads the
	 * colour data and puts them in the colourMap
	 */
	private void accumulateColourData(Task colourTask) throws ThemeException {
		List<ComponentGroup> colourCompGroups = colourTask.getChildren();
		for (ComponentGroup nextGroup : colourCompGroups) {
			if (nextGroup == null)
				continue;

			List<Component> colourComps = nextGroup.getChildren();
			for (Component nextComp : colourComps) {
				if (nextComp == null)
					continue;

				List<SkinnableEntity> colourEntities = nextComp.getChildren();
				for (SkinnableEntity nextEntity : colourEntities) {
					if (nextEntity == null)
						continue;

					readColourValue(nextEntity);

				}// end of Skinnable entity loop
			}// end of Component loop
		}// end of ComponentGroup loop
	}

	/**
	 * Reads the colour information from the given nextEntity parameter and sets
	 * it in colourMap.
	 */
	private void readColourValue(SkinnableEntity nextEntity)
	    throws ThemeException {
		/*
		 * Colour data stores the colour information within the first layer -
		 * like any other non-layering bitmap element. So pick the first layer
		 * data and get the colour value out of it.
		 */

		if (!ThemeTag.ELEMENT_COLOUR
		    .equalsIgnoreCase(nextEntity.isEntityType()))
			return;

		String colourId = (String) nextEntity
		    .getAttributeValue(ThemeTag.ATTR_ID);
		if (colourId == null) {
			errors.append("Item id is null for input entity");
			return;
		}
		ColourGraphic entityColourData;
		entityColourData = (ColourGraphic) nextEntity
			    .getPhoneThemeGraphic();
		
		String colourString = null;
		if (entityColourData != null) {
			String userSetRGBColour = (String) entityColourData.getColour();
			String userSetIdxColour = null; // Currently not supported

			CTYPE colType = null;
			String colValue = null;

			if (userSetRGBColour != null) {
				colType = CTYPE.RGB;
				colValue = userSetRGBColour;

			} else if (userSetIdxColour != null) {
				colType = CTYPE.IDX;
				colValue = userSetIdxColour;
			}

			colourString = THEME_COLOURS.colourString(colType, colValue);
			if (colourString != null) {
				colourMap.put(colourId, colourString);
			}
		}

		String colourGroupId = nextEntity
		    .getAttributeValue(ThemeTag.ATTR_COLOUR_GROUP_ID);
		String colourGroupIdx = nextEntity
		    .getAttributeValue(ThemeTag.ATTR_COLOUR_GROUP_IDX);

		String colourGroupMajorId = nextEntity
		    .getAttributeValue(ThemeTag.ATTR_COLOUR_GROUP_MAJOR_ID);
		String colourGroupMinorId = nextEntity
		    .getAttributeValue(ThemeTag.ATTR_COLOUR_GROUP_MINOR_ID);

		if (colourGroupId != null && colourGroupIdx != null
		    && colourGroupId.length() > 0 && colourGroupIdx.length() > 0) {

			ColourGroup group = colourGroups.get(colourGroupId);
			if (group == null) {
				group = new ColourGroup(colourGroupId);
				colourGroups.put(colourGroupId, group);
			}

			group.setMajorMinorId(colourGroupMajorId, colourGroupMinorId);

			if (colourString == null) {
				// use default value
				String defaultColor = nextEntity
				    .getAttributeValue(ThemeTag.ATTR_DEF_COLOUR_RGB);
				colourString = THEME_COLOURS.colourString(CTYPE.RGB,
				    defaultColor);
			}

			group.colours.add(new Colour(colourId, Integer
			    .parseInt(colourGroupIdx), colourString));
		}

	}

	/**
	 * Generates the QsnIconColours string
	 * 
	 */
	private StringBuffer generateIconColourString(String iid) {
		THEME_COLOURS[] iconColourIds = { THEME_COLOURS.icon1,
		    THEME_COLOURS.icon2, THEME_COLOURS.icon3, THEME_COLOURS.icon4,
		    THEME_COLOURS.icon5, THEME_COLOURS.icon6, THEME_COLOURS.icon7,
		    THEME_COLOURS.icon8, THEME_COLOURS.icon9, THEME_COLOURS.icon10,
		    THEME_COLOURS.icon11, THEME_COLOURS.icon12, THEME_COLOURS.icon13,
		    THEME_COLOURS.icon14, THEME_COLOURS.icon15, THEME_COLOURS.icon16,
		    THEME_COLOURS.icon17, THEME_COLOURS.icon18, THEME_COLOURS.icon19,
		    THEME_COLOURS.icon20, THEME_COLOURS.icon21, THEME_COLOURS.icon22,
		    THEME_COLOURS.icon23, THEME_COLOURS.icon24, THEME_COLOURS.icon25,
		    THEME_COLOURS.icon26, THEME_COLOURS.icon27, THEME_COLOURS.icon28,
		    THEME_COLOURS.icon29 };

		StringBuffer qsnIconColour = generateColourString(iid, iconColourIds);
		return qsnIconColour;
	}

	/**
	 * Generates the QsnTextColours string
	 * 
	 */
	private StringBuffer generateTextColourString(String iid) {
		
		// THEME_COLOURS.text51,THEME_COLOURS.text52,THEME_COLOURS.text53
		// are added to support Call Handling and MIDP COlours.
		
		THEME_COLOURS[] textColourIds = { THEME_COLOURS.text1,
		    THEME_COLOURS.text2, THEME_COLOURS.text3, THEME_COLOURS.text4,
		    THEME_COLOURS.text5, THEME_COLOURS.text6, THEME_COLOURS.text7,
		    THEME_COLOURS.text8, THEME_COLOURS.text9, THEME_COLOURS.text10,
		    THEME_COLOURS.text11, THEME_COLOURS.text12, THEME_COLOURS.text13,
		    THEME_COLOURS.text14, THEME_COLOURS.text15, THEME_COLOURS.text16,
		    THEME_COLOURS.text17, THEME_COLOURS.text18, THEME_COLOURS.text19,
		    THEME_COLOURS.text20, THEME_COLOURS.text21, THEME_COLOURS.text22,
		    THEME_COLOURS.text23, THEME_COLOURS.text24, THEME_COLOURS.text25,
		    THEME_COLOURS.text26, THEME_COLOURS.text27, THEME_COLOURS.text28,
		    THEME_COLOURS.text29, THEME_COLOURS.text30, THEME_COLOURS.text31,
		    THEME_COLOURS.text32, THEME_COLOURS.text33, THEME_COLOURS.text34,
		    THEME_COLOURS.text35, THEME_COLOURS.text36, THEME_COLOURS.text37,
		    THEME_COLOURS.text38, THEME_COLOURS.text39, THEME_COLOURS.text40,
		    THEME_COLOURS.text41, THEME_COLOURS.text42, THEME_COLOURS.text43,
		    THEME_COLOURS.text44, THEME_COLOURS.text45, THEME_COLOURS.text46,
		    THEME_COLOURS.text47, THEME_COLOURS.text48, THEME_COLOURS.text49,
		    THEME_COLOURS.text50, THEME_COLOURS.text51, THEME_COLOURS.text52,
		    THEME_COLOURS.text53, THEME_COLOURS.text54, THEME_COLOURS.text55,
		    THEME_COLOURS.text56, THEME_COLOURS.text57, THEME_COLOURS.text58,
		    THEME_COLOURS.text59, THEME_COLOURS.text60, THEME_COLOURS.text61,
		    THEME_COLOURS.text62 };

		StringBuffer qsnTextColour = generateColourString(iid, textColourIds);
		return qsnTextColour;
	}

	/**
	 * Generates the QsnLineColours string
	 * 
	 */
	private StringBuffer generateLineColourString(String iid) {
		THEME_COLOURS[] lineColourIds = { THEME_COLOURS.line1,
		    THEME_COLOURS.line2, THEME_COLOURS.line3, THEME_COLOURS.line4,
		    THEME_COLOURS.line5, THEME_COLOURS.line6, THEME_COLOURS.line7,
		    THEME_COLOURS.line8, THEME_COLOURS.line9, THEME_COLOURS.line10,
		    THEME_COLOURS.line11, THEME_COLOURS.line12, THEME_COLOURS.line13,
		    THEME_COLOURS.line14 };

		StringBuffer qsnLineColour = generateColourString(iid, lineColourIds);
		return qsnLineColour;
	}

	/**
	 * Generates the QsnOtherColours string
	 * 
	 */
	private StringBuffer generateOtherColourString(String iid) {
		THEME_COLOURS[] otherColourIds = { THEME_COLOURS.other1,
		    THEME_COLOURS.other2, THEME_COLOURS.other3, THEME_COLOURS.other4,
		    THEME_COLOURS.other5, THEME_COLOURS.other6, THEME_COLOURS.other7,
		    THEME_COLOURS.other8, THEME_COLOURS.other9, THEME_COLOURS.other10,
		    THEME_COLOURS.other11, THEME_COLOURS.other12,
		    THEME_COLOURS.other13, THEME_COLOURS.other14, THEME_COLOURS.other15 };

		StringBuffer qsnOtherColour = generateColourString(iid, otherColourIds);
		return qsnOtherColour;
	}

	/**
	 * Generates the QsnHighlightedItemColours string
	 * 
	 */
	private StringBuffer generateHighlightedItemColourString(String iid) {
		THEME_COLOURS[] highlightColourIds = { THEME_COLOURS.highlight1,
		    THEME_COLOURS.highlight2, THEME_COLOURS.highlight3 };

		StringBuffer qsnHighlightColour = generateColourString(iid,
		    highlightColourIds);
		return qsnHighlightColour;
	}

	/**
	 * Generates the QsnScrollColours String
	 */
	public StringBuffer generateScrollColourString() {
		THEME_COLOURS[] scrollColourIds = { THEME_COLOURS.scroll_1,
		    THEME_COLOURS.scroll_2, THEME_COLOURS.scroll_3,
		    THEME_COLOURS.scroll_4, THEME_COLOURS.scroll_5,
		    THEME_COLOURS.scroll_6, THEME_COLOURS.scroll_7,
		    THEME_COLOURS.scroll_8, THEME_COLOURS.scroll_9,
		    THEME_COLOURS.scroll_10, THEME_COLOURS.scroll_11,
		    THEME_COLOURS.scroll_12, THEME_COLOURS.scroll_13,
		    THEME_COLOURS.scroll_14, THEME_COLOURS.scroll_15,
		    THEME_COLOURS.scroll_16, THEME_COLOURS.scroll_17,
		    THEME_COLOURS.scroll_18 };

		StringBuffer qsnScrollColour = generateColourString("QsnScrollColors",
		    scrollColourIds);
		return qsnScrollColour;
	}

	/**
	 * Generates the QsnComponentColours string
	 * 
	 */
	private StringBuffer generateOldFormatColourString(String iid) {
		THEME_COLOURS[] componentColourIds = { THEME_COLOURS.icon1,
		    THEME_COLOURS.icon4, THEME_COLOURS.icon2, THEME_COLOURS.icon10,
		    THEME_COLOURS.icon11, THEME_COLOURS.icon8, THEME_COLOURS.icon9,
		    THEME_COLOURS.line1, THEME_COLOURS.line2, THEME_COLOURS.oldColour9,
		    THEME_COLOURS.text1, THEME_COLOURS.oldColour11,
		    THEME_COLOURS.text13, THEME_COLOURS.text19,
		    THEME_COLOURS.oldColour14, THEME_COLOURS.oldColour15,
		    THEME_COLOURS.text21, THEME_COLOURS.highlight2,
		    THEME_COLOURS.text25, THEME_COLOURS.oldColour19,
		    THEME_COLOURS.other1, THEME_COLOURS.other2, THEME_COLOURS.other3,
		    THEME_COLOURS.other4, THEME_COLOURS.oldColour24 };

		StringBuffer qsnOldFormatColour = generateColourString(iid,
		    componentColourIds);
		return qsnOldFormatColour;
	}

	/**
	 * Runs through the id array and generates the "RGB=0xFFFFFF" for each entry
	 */
	private StringBuffer generateColourString(String colourGroupId,
	    THEME_COLOURS[] colourIds) {

		StringBuffer colourDefn = new StringBuffer();
		for (int i = 0; i < colourIds.length; i++) {
			THEME_COLOURS nextColourId = colourIds[i];
			String colourIdString = nextColourId.name();
			String userData = (String) colourMap.get(colourIdString);
			String colourString = nextColourId.colourString(userData);
			colourDefn.append(TAB).append(colourString).append(NL);
		}

		StringBuffer returnString = null;
		if (colourDefn != null) {
			returnString = new StringBuffer();
			returnString.append(COLORTABLE).append(SPACE);
			returnString.append(IID).append(EQUAL).append(colourGroupId);
			returnString.append(NL).append(colourDefn);
			returnString.append(END);
		}

		return returnString;
	}

	/**
	 * Generates the Custom colors string
	 * 
	 * @param iidMap
	 */
	private StringBuffer generateCustomColourString(Map<String, String> iidMap) {
		StringBuffer returnString = null;
		for (ColourGroup group : colourGroups.values()) {
			StringBuffer colourDefn = new StringBuffer();

			Colour[] colours = group.colours.toArray(new Colour[group.colours
			    .size()]);

			for (int i = 0; i < colours.length; i++) {
				Colour nextColour = colours[i];
				String colourString = nextColour.definition;
				colourDefn.append(TAB).append(colourString).append(NL);
			}

			if (colourDefn != null) {
				returnString = new StringBuffer();
				returnString.append(COLORTABLE).append(SPACE);
				returnString.append(IID).append(EQUAL).append(group.id);
				returnString.append(NL).append(colourDefn);
				returnString.append(END);
				returnString.append(NL);

				iidMap.put(group.id, returnString.toString());
			}
		}

		return returnString;
	}
}


/**
 * FULL_MODIFY_ADAPTER COLOUR DEFINTION DONE BELOW
 */
enum CTYPE {
	IDX, RGB
};


enum THEME_COLOURS {
	icon1(CTYPE.IDX, 215), icon2(CTYPE.IDX, 215), icon3(CTYPE.IDX, 215), icon4(
	    CTYPE.IDX, 0), icon5(CTYPE.IDX, 0), icon6(CTYPE.IDX, 0), icon7(
	    CTYPE.IDX, 0), icon8(CTYPE.IDX, 215), icon9(CTYPE.IDX, 243), icon10(
	    CTYPE.IDX, 210), icon11(CTYPE.IDX, 215), icon12(CTYPE.IDX, 215), icon13(
	    CTYPE.IDX, 215), icon14(CTYPE.IDX, 215), icon15(CTYPE.IDX, 215), icon16(
	    CTYPE.IDX, 215), icon17(CTYPE.IDX, 215), icon18(CTYPE.IDX, 0), icon19(
	    CTYPE.IDX, 215), icon20(CTYPE.IDX, 0), icon21(CTYPE.IDX, 0), icon22(
	    CTYPE.IDX, 0), icon23(CTYPE.IDX, 0), icon24(CTYPE.IDX, 0), icon25(
	    CTYPE.IDX, 0), icon26(CTYPE.IDX, 215), icon27(CTYPE.RGB, 0x4b5879), icon28(
	    CTYPE.RGB, 0x4b5879), icon29(CTYPE.RGB, 0x4b5879),

	text1(CTYPE.IDX, 215), text2(CTYPE.IDX, 0), text3(CTYPE.IDX, 215), text4(
	    CTYPE.IDX, 243), text5(CTYPE.IDX, 0), text6(CTYPE.IDX, 215), text7(
	    CTYPE.IDX, 215), text8(CTYPE.IDX, 215), text9(CTYPE.IDX, 215), text10(
	    CTYPE.IDX, 215), text11(CTYPE.IDX, 215), text12(CTYPE.IDX, 0), text13(
	    CTYPE.IDX, 215), text14(CTYPE.IDX, 215), text15(CTYPE.IDX, 215), text16(
	    CTYPE.IDX, 215), text17(CTYPE.IDX, 215), text18(CTYPE.IDX, 215), text19(
	    CTYPE.IDX, 215), text20(CTYPE.IDX, 215), text21(CTYPE.IDX, 221), text22(
	    CTYPE.IDX, 215), text23(CTYPE.IDX, 215), text24(CTYPE.IDX, 0), text25(
	    CTYPE.IDX, 215), text26(CTYPE.IDX, 215), text27(CTYPE.IDX, 215), text28(
	    CTYPE.IDX, 215), text29(CTYPE.IDX, 215), text30(CTYPE.IDX, 210), text31(
	    CTYPE.IDX, 219), text32(CTYPE.IDX, 215), text33(CTYPE.IDX, 215), text34(
	    CTYPE.IDX, 215), text35(CTYPE.IDX, 215), text36(CTYPE.IDX, 215), text37(
	    CTYPE.IDX, 0), text38(CTYPE.IDX, 0), text39(CTYPE.IDX, 215), text40(
	    CTYPE.IDX, 215), text41(CTYPE.IDX, 215), text42(CTYPE.IDX, 215), text43(
	    CTYPE.IDX, 0), text44(CTYPE.IDX, 0), text45(CTYPE.IDX, 0), text46(
	    CTYPE.IDX, 0), text47(CTYPE.IDX, 0), text48(CTYPE.IDX, 0), text49(
	    CTYPE.IDX, 0), text50(CTYPE.IDX, 0), text51(CTYPE.IDX, 215), text52(
	    CTYPE.IDX, 221), text53(CTYPE.IDX, 221), text54(CTYPE.IDX, 215), text55(
	    CTYPE.IDX, 215), text56(CTYPE.IDX, 215), text57(CTYPE.IDX, 215), text58(
	    CTYPE.IDX, 215), text59(CTYPE.IDX, 215), text60(CTYPE.IDX, 215), text61(
	    CTYPE.IDX, 129), text62(CTYPE.IDX, 0),

	line1(CTYPE.IDX, 215), line2(CTYPE.IDX, 221), line3(CTYPE.IDX, 215), line4(
	    CTYPE.IDX, 221), line5(CTYPE.IDX, 221), line6(CTYPE.IDX, 215), line7(
	    CTYPE.IDX, 215), line8(CTYPE.IDX, 215), line9(CTYPE.IDX, 215), line10(
	    CTYPE.IDX, 215), line11(CTYPE.IDX, 221), line12(CTYPE.IDX, 0), line13(
	    CTYPE.IDX, 35), line14(CTYPE.IDX, 120),

	other1(CTYPE.IDX, 35), other2(CTYPE.IDX, 210), other3(CTYPE.IDX, 35), other4(
	    CTYPE.IDX, 215), other5(CTYPE.IDX, 215), other6(CTYPE.IDX, 221), other7(
	    CTYPE.IDX, 0), other8(CTYPE.RGB, 0x0046b7), other9(CTYPE.IDX, 193), other10(
	    CTYPE.IDX, 215), other11(CTYPE.IDX, 215), other12(CTYPE.IDX, 215), other13(
	    CTYPE.IDX, 215), other14(CTYPE.IDX, 215), other15(CTYPE.IDX, 215),

	highlight1(CTYPE.IDX, 244), highlight2(CTYPE.IDX, 210), highlight3(
	    CTYPE.IDX, 210),

	scroll_1(CTYPE.RGB, 0xffffff), scroll_2(CTYPE.RGB, 0xbbeeff), scroll_3(
	    CTYPE.RGB, 0x99ddff), scroll_4(CTYPE.RGB, 0x88bbee), scroll_5(
	    CTYPE.RGB, 0x77aaee), scroll_6(CTYPE.RGB, 0x6699dd), scroll_7(
	    CTYPE.RGB, 0x5588bb), scroll_8(CTYPE.RGB, 0x4477cc), scroll_9(
	    CTYPE.RGB, 0x4466bb), scroll_10(CTYPE.RGB, 0x3355bb), scroll_11(
	    CTYPE.RGB, 0x2244aa), scroll_12(CTYPE.RGB, 0x1122aa), scroll_13(
	    CTYPE.RGB, 0x001166), scroll_14(CTYPE.RGB, 0x000077), scroll_15(
	    CTYPE.RGB, 0x000055), scroll_16(CTYPE.RGB, 0x001133), scroll_17(
	    CTYPE.RGB, 0x000022), scroll_18(CTYPE.RGB, 0x000022),

	// to support old colours
	oldColour9(CTYPE.RGB, 0x000000), oldColour11(CTYPE.RGB, 0x000000), oldColour14(
	    CTYPE.RGB, 0x000000), oldColour15(CTYPE.RGB, 0x000000), oldColour19(
	    CTYPE.RGB, 0x000000), oldColour24(CTYPE.RGB, 0xFF9999);

	CTYPE colourType;

	int colourValue;

	THEME_COLOURS(CTYPE colType, int colValue) {
		colourType = colType;
		colourValue = colValue;
	}

	/**
	 * Returns the colour string that can be written into the text file
	 * 
	 * @param colType If it is IDX type colour or RGB type colour
	 * @param colValue The value set by the user
	 * @return the colour string. If any of the input values is null then the
	 *         default value will be returned.
	 */
	static String colourString(CTYPE colType, String colValue) {
		/*
		 * If the value set in the layer data is null, then send the default
		 * values.
		 */
		if (colType == null || colValue == null) {
			return null;
		}

		if (colType == CTYPE.RGB) {
			try {
				colValue = colValue.toLowerCase();
				if (colValue.startsWith("0x")) {
					colValue = colValue.substring(2);
				} else if (colValue.startsWith("#")) {
					colValue = colValue.substring(1);
				}
				colValue = Integer.toHexString(Integer.parseInt(colValue,16));
				while (colValue.length() < 6) {
					colValue = "0".concat(colValue);
				}
				colValue = "0x" + colValue;

			} catch (NumberFormatException e) {
				e.printStackTrace();
				return null;
			}

		}

		String colString = colType + "=" + colValue;
		return colString;
	}

	/**
	 * Returns the default string if the input user data is null
	 */
	String colourString(String userData) {
		if (userData == null || userData.trim().length() <= 0) {
			userData = colourString(colourType, Integer.toString(colourValue));
		}

		return userData;
	}
}
