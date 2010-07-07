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

package com.nokia.tools.theme.s60.editing;

import com.nokia.tools.media.utils.layers.IEffectParameterDescriptor;

/**
 * Holds information about one effect attribute
 */
public class EffectParameterDescriptor implements IEffectParameterDescriptor {
	
	String caption;
	
	String uiName;
	
	String uiType;	
	
	String defaultVal, minVal, maxVal;	
	
	String options[];
	
	int pos;

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getDefaultVal() {
		return defaultVal;
	}

	public void setDefaultVal(String defaultVal) {
		this.defaultVal = defaultVal;
	}

	public String getMaxVal() {
		return maxVal;
	}

	public void setMaxVal(String maxVal) {
		this.maxVal = maxVal;
	}

	public String getMinVal() {
		return minVal;
	}

	public void setMinVal(String minVal) {
		this.minVal = minVal;
	}

	public String[] getOptions() {
		return options;
	}

	public void setOptions(String[] options) {
		this.options = options;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public String getUiName() {
		return uiName;
	}

	public void setUiName(String uiName) {
		this.uiName = uiName;
	}

	public String getUiType() {
		return uiType;
	}

	public void setUiType(String uiType) {
		this.uiType = uiType;
	}
	
	/**
	 * returns position of value in options
	 * @return
	 */
	public int getLiteralValueNumber(String literalValue) {
		if (options != null) {
			for (int i = 0; i < options.length; i++) {
				if (options[i].equalsIgnoreCase(literalValue))
					return i;
			}
		}		
		throw new IllegalArgumentException();
	}
	
	public String getLiteralValue(int number) {
		if (options != null) {
			return options[number];
		}
		return "UknownOption!";
	}
	
}
