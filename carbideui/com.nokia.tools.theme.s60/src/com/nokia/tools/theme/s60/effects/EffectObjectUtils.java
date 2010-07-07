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
package com.nokia.tools.theme.s60.effects;

import com.nokia.tools.platform.theme.EffectConstants;


/**
 * Helper class for EffectObject and effects.
  */
public class EffectObjectUtils {

	/**
	 * maps int. param value to literal value, e.g. '0' -> 'Normal' in ChannelBlending effect
	 * @param effectName
	 * @param paramName
	 * @param value
	 * @return
	 */
	public static String getValueLiteral(String effectName, String paramName,
			int value) {
		if (EffectConstants.CHANNELBLENDING.equals(effectName)
				|| EffectConstants.ALPHABLENDING.equals(effectName)
				|| EffectConstants.CONVOLUTION.equals(effectName)) {
			EffectObject effect = EffectObject.getEffect(effectName);
			return effect.getDescriptor().getParameterLiteralValue(
					paramName, value);
		}
		try {
			EffectObject effect = EffectObject.getEffect(effectName);
			return effect.getDescriptor().getParameterLiteralValue(
					paramName, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Effect " + effectName
				+ " does not have literal value map! ("+paramName+"/"+value+")");
	}

	/**
	 * maps literal value to it's int. value
	 * @param effectName
	 * @param paramName
	 * @param literal
	 * @return
	 */
	public static int getIntegerValue(String effectName, String paramName,
			String literal) {
		if (EffectConstants.CHANNELBLENDING.equals(effectName)
				|| EffectConstants.ALPHABLENDING.equals(effectName)
				|| EffectConstants.CONVOLUTION.equals(effectName)) {
			EffectObject effect = EffectObject.getEffect(effectName);
			return effect.getDescriptor().getParameterLiteralValueNumber(
					paramName, literal);
		}
		try {
			EffectObject effect = EffectObject.getEffect(effectName);
			return effect.getDescriptor().getParameterLiteralValueNumber(
					paramName, literal);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Illegal literal value: " + literal);
		} catch (Exception e) {
			throw new RuntimeException("Effect " + effectName
					+ " does not have literal value map!");
		}		
	}
	
	/* value -> literal */
	
	public static String getChannelBlendMode(int value) {
		return getValueLiteral(EffectConstants.CHANNELBLENDING, EffectConstants.ATTR_MODE, value);
	}
	
	public static String getAlphaBlendMode(int value) {
		return getValueLiteral(EffectConstants.ALPHABLENDING, EffectConstants.ATTR_MODE, value);
	}
	
	public static String getConvolveMode(int value) {
		return getValueLiteral(EffectConstants.CONVOLUTION, EffectConstants.ATTR_MODE, value);
	}
	
	public static String getScaleMode(int value) {
		return getValueLiteral(EffectConstants.APPLYGRAPHICS, EffectConstants.SCALEMODE, value);
	}
	
	/* literal -> value */
	
	public static int getChannelBlendModeValue(String mode) {
		return getIntegerValue(EffectConstants.CHANNELBLENDING, EffectConstants.ATTR_MODE, mode);
	}
	
	public static int getAlphaBlendModeValue(String mode) {
		return getIntegerValue(EffectConstants.ALPHABLENDING, EffectConstants.ATTR_MODE, mode);
	}
	
	public static int getConvolveModeValue(String mode) {
		return getIntegerValue(EffectConstants.CONVOLUTION, EffectConstants.ATTR_MODE, mode);
	}
	
	public static int getScaleModeValue(String mode) {
		return getIntegerValue(EffectConstants.APPLYGRAPHICS, EffectConstants.SCALEMODE, mode);
	}

}
