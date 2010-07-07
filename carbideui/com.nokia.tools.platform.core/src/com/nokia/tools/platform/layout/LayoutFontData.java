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
 */
package com.nokia.tools.platform.layout;

public final class LayoutFontData {

	private String fontName = null;

	private String fontSize = null;

	private String baseline = null;

	//private String hintValue = null;

	public LayoutFontData(String Name, String FontSize, String BaseLine,
			String Value) {
		fontName = Name;
		fontSize = FontSize;
		baseline = BaseLine;
		//hintValue = Value;
	}

	/**
	 * @return
	 */
	public String getBaseline() {
		return baseline;
	}

	/**
	 * @return
	 */
	public String getFontName() {
		return fontName;
	}

	/**
	 * @return
	 */
	public String getFontSize() {
		return fontSize;
	}

/*
	*//**
	 * @return
	 *//*
	public String getHintValue() {
		return hintValue;
	}

	*//**
	 * @param string
	 *//*
	public void setBaseline(String string) {
		baseline = string;
	}

	*//**
	 * @param string
	 *//*
	public void setFontName(String string) {
		fontName = string;
	}
	*//**
	 * @param string
	 *//*
	public void setFontSize(String string) {
		fontSize = string;
	}

	*//**
	 * @param string
	 *//*
	public void setHintValue(String string) {
		hintValue = string;
	}


*/
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		int j = Integer.parseInt(((LayoutFontData) o).getFontSize());
		int size = Integer.parseInt(fontSize);
		if (size > j) {
			return 1;
		} else if (size == j) {
			return 0;
		} else {
			return -1;
		}
	}

	public boolean isEqualsTo(LayoutFontData comparewith) {
		boolean isEqual = false;
		if (this.fontName.equalsIgnoreCase(comparewith.getFontName())
				&& fontSize.equalsIgnoreCase(comparewith.getFontSize())) {
			isEqual = true;
		}
		return isEqual;
	}

}