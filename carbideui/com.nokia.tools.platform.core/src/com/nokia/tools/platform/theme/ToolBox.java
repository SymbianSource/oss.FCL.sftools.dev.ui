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
package com.nokia.tools.platform.theme;

import java.util.Map;

/**
 * The class hold ToolBox information associated with the component/element/part
 * of skin design
 * 
 *
 */
public class ToolBox implements Cloneable {
	public static final int MOVE_SET = 1;
	public static final int SCALE_SET = 1 << 1;
	public static final int SPLIT_SET = 1 << 2;
	public static final int MIRROR_SET = 1 << 3;
	public static final int TILE_SET = 1 << 4;
	public static final int MASK_SET = 1 << 5;
	public static final int COLOURIZE_SET = 1 << 6;
	public static final int INTENSITY_SET = 1 << 7;
	public static final int CREATEANOTHER_SET = 1 << 8;
	public static final int MULTIPLEELEMENTS_SET = 1 << 9;
	public static final int MULTIPLECOMPONENTS_SET = 1 << 10;
	public static final int SAMECOMPONENT_SET = 1 << 11;
	public static final int DESIGNAIDS_SET = 1 << 12;
	public static final int ROTATE_SET = 1 << 13;
	public static final int BITMAPBLOCKED_SET = 1 << 14;
	public static final int COLOURLOOKUP_SET = 1 << 15;
	public static final int SOFTMASK_SET = 1 << 16;
	public static final int MULTIPLELAYERSSUPPORT_SET = 1 << 17;
	public static final int EFFECTSSUPPORT_SET = 1 << 18;
	public static final int SKINFORMAT_SET = 1 << 19;
	public static final int TEST_SET = 1 << 20;

	public boolean Move = false;
	public boolean Scale = false;
	public boolean Split = false;
	public boolean Mirror = false;
	public boolean Tile = false;
	public boolean Mask = false;
	public boolean Colourize = false;
	public boolean Intensity = false;
	public boolean Createanother = false;
	public boolean Multipleelements = false;
	public boolean Multiplecomponents = false;
	public boolean SameComponent = false;
	public boolean Designaids = false;
	public boolean Rotate = false;
	public boolean Bitmapblocked = false;
	public boolean ColourLookup = false;
	public boolean SoftMask = false;
	public boolean multipleLayersSupport = false;
	public boolean effectsSupport = false;
	public String skinformat = null;
	public String Test = null;

	private int set;

	/**
	 * Method to set the attributes of the toolbox
	 * 
	 * @param attrs Map holding the attributes
	 */

	public void setAttribute(Map attr) {

		if (attr.containsKey(ThemeTag.ATTR_MOVE)) {
			Move = Boolean.valueOf((String) (attr.get(ThemeTag.ATTR_MOVE)))
					.booleanValue();
			set |= MOVE_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_SCALE)) {
			Scale = Boolean.valueOf((String) (attr.get(ThemeTag.ATTR_SCALE)))
					.booleanValue();
			set |= SCALE_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_SPLIT)) {
			Split = Boolean.valueOf((String) (attr.get(ThemeTag.ATTR_SPLIT)))
					.booleanValue();
			set |= SPLIT_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_MIRROR)) {
			Mirror = Boolean.valueOf((String) (attr.get(ThemeTag.ATTR_MIRROR)))
					.booleanValue();
			set |= MIRROR_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_MASK)) {
			Mask = Boolean.valueOf((String) (attr.get(ThemeTag.ATTR_MASK)))
					.booleanValue();
			set |= MASK_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_TILE)) {
			Tile = Boolean.valueOf((String) (attr.get(ThemeTag.ATTR_TILE)))
					.booleanValue();
			set |= TILE_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_COLOURIZE)) {
			Colourize = Boolean.valueOf(
					(String) (attr.get(ThemeTag.ATTR_COLOURIZE)))
					.booleanValue();
			set |= COLOURIZE_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_INTENSITY)) {
			Intensity = Boolean.valueOf(
					(String) (attr.get(ThemeTag.ATTR_INTENSITY)))
					.booleanValue();
			set |= INTENSITY_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_CREAANOTHER)) {
			Createanother = Boolean.valueOf(
					(String) (attr.get(ThemeTag.ATTR_CREAANOTHER)))
					.booleanValue();
			set |= CREATEANOTHER_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_MULTIELEMS)) {
			Multipleelements = Boolean.valueOf(
					(String) (attr.get(ThemeTag.ATTR_MULTIELEMS)))
					.booleanValue();
			set |= MULTIPLEELEMENTS_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_MULTICOMPS)) {
			Multiplecomponents = Boolean.valueOf(
					(String) (attr.get(ThemeTag.ATTR_MULTICOMPS)))
					.booleanValue();
			set |= MULTIPLECOMPONENTS_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_SAMECOMP)) {
			SameComponent = Boolean.valueOf(
					(String) (attr.get(ThemeTag.ATTR_SAMECOMP))).booleanValue();
			set |= SAMECOMPONENT_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_DESIGN)) {
			Designaids = Boolean.valueOf(
					(String) (attr.get(ThemeTag.ATTR_DESIGN))).booleanValue();
			set |= DESIGNAIDS_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_ROTATE)) {
			Rotate = Boolean.valueOf((String) (attr.get(ThemeTag.ATTR_ROTATE)))
					.booleanValue();
			set |= ROTATE_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_SOFTMASK)) {
			SoftMask = Boolean.valueOf(
					(String) (attr.get(ThemeTag.ATTR_SOFTMASK))).booleanValue();
			set |= SOFTMASK_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_BITMAPBLOCKED)) {
			Bitmapblocked = Boolean.valueOf(
					(String) (attr.get(ThemeTag.ATTR_BITMAPBLOCKED)))
					.booleanValue();
			set |= BITMAPBLOCKED_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_COLOURLOOKUP)) {
			ColourLookup = Boolean.valueOf(
					(String) (attr.get(ThemeTag.ATTR_COLOURLOOKUP)))
					.booleanValue();
			set |= COLOURLOOKUP_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_MULTIPLE_LAYERS_SUPPORT)) {
			multipleLayersSupport = Boolean.valueOf(
					(String) (attr.get(ThemeTag.ATTR_MULTIPLE_LAYERS_SUPPORT)))
					.booleanValue();
			set |= MULTIPLELAYERSSUPPORT_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_EFFECTS_SUPPORT)) {
			effectsSupport = Boolean.valueOf(
					(String) (attr.get(ThemeTag.ATTR_EFFECTS_SUPPORT)))
					.booleanValue();
			set |= EFFECTSSUPPORT_SET;
		}
		if (attr.containsKey(ThemeTag.ATTR_SKINFORMAT)) {
			skinformat = (String) attr.get(ThemeTag.ATTR_SKINFORMAT);
			set |= SKINFORMAT_SET;
		}

		if (attr.containsKey(ThemeTag.ATTR_TEST)) {
			Test = (String) (attr.get(ThemeTag.ATTR_TEST));
			set |= TEST_SET;
		}
	}

	/**
	 * Method to update the ToolBox with the new information
	 * 
	 * @param toolBox ToolBox tool box with the new information
	 */
	public void update(ToolBox toolBox) {
		if (toolBox == null) {
			return;
		}
		// update is used to merge the bits with the parent, therefore we don't
		// allow parent to overwrite our own
		if ((set & MOVE_SET) == 0) {
			Move = toolBox.Move;
		}
		if ((set & SCALE_SET) == 0) {
			Scale = toolBox.Scale;
		}
		if ((set & SPLIT_SET) == 0) {
			Split = toolBox.Split;
		}
		if ((set & MIRROR_SET) == 0) {
			Mirror = toolBox.Mirror;
		}
		if ((set & TILE_SET) == 0) {
			Tile = toolBox.Tile;
		}
		if ((set & MASK_SET) == 0) {
			Mask = toolBox.Mask;
		}
		if ((set & COLOURIZE_SET) == 0) {
			Colourize = toolBox.Colourize;
		}
		if ((set & INTENSITY_SET) == 0) {
			Intensity = toolBox.Intensity;
		}
		if ((set & CREATEANOTHER_SET) == 0) {
			Createanother = toolBox.Createanother;
		}
		if ((set & MULTIPLEELEMENTS_SET) == 0) {
			Multipleelements = toolBox.Multipleelements;
		}
		if ((set & MULTIPLECOMPONENTS_SET) == 0) {
			Multiplecomponents = toolBox.Multiplecomponents;
		}
		if ((set & DESIGNAIDS_SET) == 0) {
			Designaids = toolBox.Designaids;
		}
		if ((set & ROTATE_SET) == 0) {
			Rotate = toolBox.Rotate;
		}
		if ((set & BITMAPBLOCKED_SET) == 0) {
			Bitmapblocked = toolBox.Bitmapblocked;
		}
		if ((set & COLOURLOOKUP_SET) == 0) {
			ColourLookup = toolBox.ColourLookup;
		}
		if ((set & SOFTMASK_SET) == 0) {
			SoftMask = toolBox.SoftMask;
		}
		if ((set & MULTIPLELAYERSSUPPORT_SET) == 0) {
			multipleLayersSupport = toolBox.multipleLayersSupport;
		}
		if ((set & EFFECTSSUPPORT_SET) == 0) {
			effectsSupport = toolBox.effectsSupport;
		}
		if ((set & SKINFORMAT_SET) == 0) {
			skinformat = toolBox.skinformat;
		}
		if ((set & TEST_SET) == 0) {
			Test = toolBox.Test;
		}
	}

	/**
	 * Method to clone the ToolBox object
	 * 
	 * @return Object object of the clone ToolBox
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

	/**
	 * Gives the string representation of the data in the part
	 */
	public String toString() {

		StringBuffer s = new StringBuffer("Toolbox : \n");
		s.append("[move : " + Move + "] [scale : " + Scale + "] [split : "
				+ Split + "]");
		s.append("\n[mirror : " + Mirror + "] [tile : " + Tile + "] [mask :"
				+ Mask + "]");
		s.append("\n[colourize : " + Colourize + "] [colourlookup : "
				+ ColourLookup + "] [intensity : " + Intensity
				+ "] [createanother : " + Createanother + "]");
		s.append("\n[multipleelements : " + Multipleelements
				+ "] [multiplecomponents : " + Multiplecomponents
				+ "]\n[designaids : " + Designaids + "] [rotate : " + Rotate);

		s.append("] [softMask : " + SoftMask + "]");

		return s.toString();
	}

	public int getSet() {
		return set;
	}

} // end of the ToolBox class
