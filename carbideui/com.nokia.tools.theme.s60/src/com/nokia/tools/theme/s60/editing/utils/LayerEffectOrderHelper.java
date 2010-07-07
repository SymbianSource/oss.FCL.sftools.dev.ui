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

import com.nokia.tools.platform.theme.EffectConstants;

/**
 * helps in determinign layer effect processin order
 */
public class LayerEffectOrderHelper {

	private static String defaultEffectOrder[] = {
		EffectConstants.APPLYGRAPHICS,
		EffectConstants.APPLYCOLOR,
		EffectConstants.ADJUSTCHANNELS,
		EffectConstants.BLACKANDWHITE,
		EffectConstants.CONTRAST,
		EffectConstants.CONVOLUTION,
		EffectConstants.GRAYSCALE,
		EffectConstants.INVERT,
		EffectConstants.MOVINGLAYER,
		EffectConstants.NOISE,
		EffectConstants.SATURATION,
		EffectConstants.SOLARIZE,			
		EffectConstants.ALPHABLENDING,
		EffectConstants.CHANNELBLENDING
		};
	
	/**
	 * returns list of effect names in order they 
	 * should be applied
	 * @return
	 */
	public static String[] getDefaultEffectProcessOrder() {
		return defaultEffectOrder;
	}
	
	public static int getDefaultEffectOrderNo(String name) {
		for (int i = 0; i < defaultEffectOrder.length; i++) {
			if (defaultEffectOrder[i].equalsIgnoreCase(name))
				return i;
		}
		return -1;
	}
	
	public static int getBetweenLayerEffectsStartPos() {
		for (int i = 0; i < defaultEffectOrder.length; i++) {
			String effectName = defaultEffectOrder[i];
			if (EffectConstants.ALPHABLENDING.equals(effectName) ||
					EffectConstants.CHANNELBLENDING.equals(effectName)) {
				return i;
			}
		}
		return -1;
	}
	
}
