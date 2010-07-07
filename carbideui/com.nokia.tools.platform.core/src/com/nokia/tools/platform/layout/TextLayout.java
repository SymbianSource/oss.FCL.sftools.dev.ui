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
 * File name : TextLayout.java Project :  Description : Provides the layout information
 * for text elements
 * ======================================================================================
 */

package com.nokia.tools.platform.layout;

import java.awt.Font;
import java.util.Map;

import com.nokia.tools.media.font.FontUtil;

public class TextLayout extends Layout {
	private static final long serialVersionUID = 1L;

	private String justification = LayoutConstants.VALUE_ALIGN_LEFT;
	private String typeFace;
	private String style1;
	private String style2;
	private int baseLine;
	private int fontSize;
	private int bottomMargin;
	private int topMargin;
	private int leftMargin;
	private int rightMargin;

	/**
	 * Constructor for multiline / mutlicolumn layouts
	 * 
	 * @param rowCount
	 * @param columnCount
	 */
	
	public TextLayout(int rowCount, int columnCount, String justification,
			String type, LayoutContext context, Map layoutAttribData) {
		super(rowCount, columnCount);

		if (justification != null) {
			this.justification = justification;
		}

		if (layoutAttribData != null) {
			style1 = (String) layoutAttribData.get(LayoutConstants.ATTR_STYLE1);
			style2 = (String) layoutAttribData.get(LayoutConstants.ATTR_STYLE2);
			fontSize = getInt(layoutAttribData, LayoutConstants.ATTR_FONT_SIZE,
					0);
			bottomMargin = getInt(layoutAttribData,
					LayoutConstants.ATTR_MARGIN_BOTTOM,
					LayoutConstants.DEFAULT_MARGIN_VALUE);
			topMargin = getInt(layoutAttribData,
					LayoutConstants.ATTR_MARGIN_TOP,
					LayoutConstants.DEFAULT_MARGIN_VALUE);
			leftMargin = getInt(layoutAttribData,
					LayoutConstants.ATTR_MARGIN_LEFT,
					LayoutConstants.DEFAULT_MARGIN_VALUE);
			rightMargin = getInt(layoutAttribData,
					LayoutConstants.ATTR_MARGIN_RIGHT,
					LayoutConstants.DEFAULT_MARGIN_VALUE);
			baseLine = getInt(layoutAttribData, LayoutConstants.ATTR_BASELINE,
					-1);

			if (baseLine >= 0) {
				int tempBaseLine = context.getLayoutSet().getFontData()
						.getBaseLineForFont(getTypeFace(),
								Integer.toString(getFontSize()));
				if (tempBaseLine >= 0) {
					baseLine = tempBaseLine;
				}
			}
		}
		typeFace = getTypeFace(context, type, layoutAttribData);
	}

	private int getInt(Map map, String key, int defaultValue) {
		if (map != null) {
			String str = (String) map.get(key);
			if (str != null && str.trim().length() > 0) {
				try {
					return Integer.parseInt(str.trim());
				} catch (Exception e) {
				}
			}
		}
		return defaultValue;
	}

	/**
	 * @return the justification
	 */
	public String getJustification() {
		return justification;
	}

	/**
	 * @return Returns the fontSize.
	 */
	public int getFontSize() {
		return fontSize <= 0 ? height[0][0]
				- (getBottomMargin() + getTopMargin()) : fontSize;
	}

	/**
	 * @return Returns the baseline.
	 */
	public int getBaseline() {
		return baseLine;
	}

	/**
	 * @return Returns the style1.
	 */
	public String getStyle1() {
		return style1;
	}

	/**
	 * @return Returns the style2.
	 */
	public String getStyle2() {
		return style2;
	}

	private boolean validFont(String typeFace, int style, int fontSize) {

		if (typeFace == null)
			return false;

		if (FontUtil.isFontAvailable(typeFace)) {
			return true;
		}
		Font f = new Font(typeFace, style, fontSize);
		return (f != null && f.getFamily().equalsIgnoreCase(typeFace));
	}

	/**
	 * @return Returns the typeFace.
	 */
	public String getTypeFace() {
		return typeFace;
	}

	String getTypeFace(LayoutContext context, String type, Map layoutAttribData) {
		// Check if we have already computed the type face
		if (layoutAttribData != null) {
			String typeFace = (String) layoutAttribData
					.get(LayoutConstants.ATTR_USEABLE_FONT_FACE);
			if (validFont(typeFace, Font.PLAIN, getFontSize())) {
				return typeFace;
			}

			// Execution reaches here only the first time when this function is
			// called.
			typeFace = (String) layoutAttribData.get(LayoutConstants.ATTR_FONT);
			if (validFont(typeFace, Font.PLAIN, getFontSize())) {
				return typeFace;
			}
		}

		// Search for the type name data -- if type name is present then check
		// if
		// it can be used as a hint and font name determined
		if (type != null) {
			String typeFace = context.getLayoutSet().getFontData()
					.getFontTypeFace(type);

			// Check if this is usable
			if (validFont(typeFace, Font.PLAIN, getFontSize())) {
				return typeFace;
			}
		}

		// Search for the category name - if category name is present use it as
		// a hint while searching for the font name in the properties file.
		if (layoutAttribData != null) {
			String categoryType = (String) layoutAttribData
					.get(LayoutConstants.CATEGORY_TYPE);
			String typeFace = context.getLayoutSet().getFontData()
					.getFontTypeFace(categoryType);

			// Check if this is usable
			if (validFont(typeFace, Font.PLAIN, getFontSize())) {
				return typeFace;
			}
		}

		if (layoutAttribData == null || layoutAttribData.isEmpty()) {

			String typeFace = context.getLayoutSet().getFontData()
					.getFontTypeFace(null);

			// Check if this is usable
			if (validFont(typeFace, Font.PLAIN, getFontSize())) {
				return typeFace;
			}
		}
		return LayoutConstants.DEFAULT_FONT;
	}

	/**
	 * @return Returns implementation hints if any specified for the font
	 */
	/*
	 * public String getImplHint() { String strImplHint = null; if
	 * (layoutAttribData != null && layoutAttribData.size() > 0) { strImplHint =
	 * (String) layoutAttribData.get(LayoutConstants.ATTR_FONT_IMPL_HINT); }
	 * return strImplHint; } 
	 */
	/**
	 * @return Returns the bottomMargin.
	 */
	public int getBottomMargin() {
		return bottomMargin;
	}

	/**
	 * @return Returns the leftMargin.
	 */
	public int getLeftMargin() {
		return leftMargin;

	}

	/**
	 * @return Returns the rightMargin.
	 */
	public int getRightMargin() {
		return rightMargin;
	}

	/**
	 * @return Returns the topMargin.
	 */
	public int getTopMargin() {
		return topMargin;
	}
}
