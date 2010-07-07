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
package com.nokia.tools.ui.color;

import org.eclipse.swt.graphics.RGB;

import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.color.ValuedColors;

public class ColorDescriptor {
	int red;

	int green;

	int blue;

	String name;

	String description;

	public ColorDescriptor(int red, int green, int blue, String name) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.name = name;
	}

	public ColorDescriptor(RGB rgb, String name) {
		red = rgb.red;
		green = rgb.green;
		blue = rgb.blue;
		this.name = name;
	}

	public RGB getRGB() {
		return new RGB(red, green, blue);
	}

	public int getBlue() {
		return blue;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRed() {
		return red;
	}

	public void setRed(int red) {
		this.red = red;
	}

	public static boolean colorHasCssUsableName(String name) {
		if (name == null) {
			return false;
		} else
			return ValuedColors.isCssColorName(name);
	}
	
	public static boolean isColor(String hashColorFormat){
		return ColorUtil.isColor(hashColorFormat);
	}

	/*
	 * Converts colors from #xxxxxx to RGB
	 */
	public static RGB getRGB(String hashColorFormat) {
		return ColorUtil.getRGB(hashColorFormat);
	}

	public static String asHashString(RGB rgb) {
		return ColorUtil.asHashString(rgb);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}