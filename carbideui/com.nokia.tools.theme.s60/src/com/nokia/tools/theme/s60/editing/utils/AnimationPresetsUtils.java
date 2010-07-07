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
package com.nokia.tools.theme.s60.editing.utils;

/**
 * Factory for retrieveing animation presets, later can be preset loaded from
 * XML files
 */
public class AnimationPresetsUtils {

	private static double[][] RAPID_RISE_PRESET_DATA = new double[][] {
			{ 0.0, 1 - 1.0 }, { 0.07333333333333333, 1 - 0.62 },
			{ 0.16333333333333333, 1 - 0.365 },
			{ 0.2833333333333333, 1 - 0.21 }, { 0.4666666666666667, 1 - 0.11 },
			{ 0.65, 1 - 0.05 }, { 1.0, 1 - 0.0 } };

	private static double[][] SLOW_RISE_PRESET_DATA = new double[][] {
			{ 1 - 1.0, 0.0 }, { 1 - 0.65, 0.05 },
			{ 1 - 0.4666666666666667, 0.11 }, { 1 - 0.2833333333333333, 0.21 },
			{ 1 - 0.16333333333333333, 0.365 },
			{ 1 - 0.07333333333333333, 0.62 }, { 1 - 0.0, 1.0 } };

	private static double[][] RAPID_FALL_PRESET_DATA = new double[][] {
			{ 0.0, 1.0 }, { 0.07333333333333333, 0.62 },
			{ 0.16333333333333333, 0.365 }, { 0.2833333333333333, 0.21 },
			{ 0.4666666666666667, 0.11 }, { 0.65, 0.05 }, { 1.0, 0.0 } };

	private static double[][] SLOW_FALL_PRESET_DATA = new double[][] {
			{ 1 - 1.0, 1 - 0.0 }, { 1 - 0.65, 1 - 0.05 },
			{ 1 - 0.4666666666666667, 1 - 0.11 },
			{ 1 - 0.2833333333333333, 1 - 0.21 },
			{ 1 - 0.16333333333333333, 1 - 0.365 },
			{ 1 - 0.07333333333333333, 1 - 0.62 }, { 1 - 0.0, 1 - 1.0 } };

	private static double[][] LINEAR_RISE_PRESET_DATA = new double[][] {
			{ 0.0, 0.0 }, { 0.2, 0.2 }, { 0.4, 0.4 }, { 0.6, 0.6 },
			{ 0.8, 0.8 }, { 1.0, 1.0 } };

	static double[][] LINEAR_FALL_PRESET_DATA = new double[][] { { 0.0, 1.0 },
			{ 0.2, 0.8 }, { 0.4, 0.6 }, { 0.6, 0.4 }, { 0.8, 0.2 },
			{ 1.0, 0.0 } };

	private static String LINEAR_RISE = "Linear Rise";

	private static String LINEAR_FALL = "Linear Fall";

	private static String RAPID_RISE = "Rapid Rise";

	private static String SLOW_RISE = "Slow Rise";

	private static String RAPID_FALL = "Rapid Fall";

	private static String SLOW_FALL = "Slow Fall";

	private static double[][][] PRESETS = new double[][][] {
			LINEAR_RISE_PRESET_DATA, LINEAR_FALL_PRESET_DATA,
			RAPID_RISE_PRESET_DATA, SLOW_RISE_PRESET_DATA,
			RAPID_FALL_PRESET_DATA, SLOW_FALL_PRESET_DATA };

	static String[] PRESET_NAMES = new String[] { LINEAR_RISE, LINEAR_FALL,
			RAPID_RISE, SLOW_RISE, RAPID_FALL, SLOW_FALL };

	public static String[] getAvailablePresetNames() {
		return PRESET_NAMES;
	}

	public static double[][] getAnimationPreset(String preset) {
		if (RAPID_RISE.equals(preset)) {
			return RAPID_RISE_PRESET_DATA;
		}
		if (SLOW_RISE.equals(preset)) {
			return SLOW_RISE_PRESET_DATA;
		}

		if (RAPID_FALL.equals(preset)) {
			return RAPID_FALL_PRESET_DATA;
		}
		if (SLOW_FALL.equals(preset)) {
			return SLOW_FALL_PRESET_DATA;
		}
		if (LINEAR_RISE.equals(preset)) {
			return LINEAR_RISE_PRESET_DATA;
		}
		if (LINEAR_FALL.equals(preset)) {
			return LINEAR_FALL_PRESET_DATA;
		}

		return null;
	}

	public static String getPresetName(double[] presetDataX,
			double[] presetDataY) {
		for (int i = 0; i < PRESETS.length; i++) {
			if (matches(PRESETS[i], presetDataX, presetDataY))
				return PRESET_NAMES[i];
		}
		return null;
	}

	private static boolean matches(double[][] presetData, double[] presetDataX,
			double[] presetDataY) {
		for (int i = 0; i < presetData.length; i++) {
			boolean xPreset = Math.abs(presetData[i][0] - presetDataX[i]) < 1e-2;
			boolean yPreset = Math.abs(presetData[i][1] - presetDataY[i]) < 1e-2;
			if (!xPreset || !yPreset)
				return false;
		}
		return true;
	}
}
