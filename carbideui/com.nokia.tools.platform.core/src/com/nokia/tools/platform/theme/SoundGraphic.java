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

/**
 * Class to get the sound object for a skinnableentity
 */
public class SoundGraphic extends ThemeGraphic {
	/**
	 * Constructor
	 */
	public SoundGraphic(ThemeBasicData data) {
		super(data);
	}

	/**
	 * Method to get the value of an attribute.
	 * 
	 * @param attrName The name of the attribute whose value is required.
	 * @return A string containing the value of the attrName attribute. If the
	 *         attribute is not found then it returns a null
	 */
	public String getAttribute(String key) {
		ImageLayer il = (ImageLayer) getImageLayers().get(0);
		if (il.getAttributes().containsKey(key))
			return il.getAttribute(key);
		return null;
	}
}
