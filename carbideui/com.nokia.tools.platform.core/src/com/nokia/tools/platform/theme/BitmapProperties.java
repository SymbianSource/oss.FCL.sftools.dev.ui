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

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class BitmapProperties {
	/* Colorize related constants */
	public static final String DITHER_SELECTED = "IS_DITHER_SELECTED"; //$NON-NLS-1$

	public static final String IS_OPTIMIZE_SELECTED = "IS_OPTIMIZE_SELECTED"; //$NON-NLS-1$

	public static final String COLORIZE_SELECTED = "IS_COLORIZE_SELECTED"; //$NON-NLS-1$

	public static final String OPTIMIZE_SELECTION = "OPTIMIZESELECTION"; //$NON-NLS-1$

	public static final String COLOR = "COLOR"; //$NON-NLS-1$

	public static final String COLORIZE = "COLORIZE"; //$NON-NLS-1$

	public static final String RETURN_IMAGE = "RETURNIMAGE"; //$NON-NLS-1$

	public static final String OPT_QUALITY_HIGH = "high"; //$NON-NLS-1$

	public static final String OPT_QUALITY_LOW = "low"; //$NON-NLS-1$

	public static final String OPT_QUALITY_PALETTE_DEP = "palette dependant"; //$NON-NLS-1$

	public static final String BITMAP_WIDTH = "bitmapWidth"; //$NON-NLS-1$

	public static final String BITMAP_HEIGHT = "bitmapHeight"; //$NON-NLS-1$

	private Map<Object, Object> attributes = new HashMap<Object, Object>();

	public BitmapProperties() {
		setColorize(false);
		setOptimize(false);
		setDither(false);
		setQuality(null);
	}

	public BitmapProperties(Map<Object, Object> attributes) {
		setAttributes(attributes);
	}

	/**
	 * @return the isColorize
	 */
	public boolean isColorize() {
		return (Boolean) attributes.get(COLORIZE_SELECTED);
	}

	/**
	 * @param isColorize
	 *            the isColorize to set
	 */
	public void setColorize(boolean isColorize) {
		attributes.put(COLORIZE_SELECTED, isColorize);
		if (isColorize && getColor() == null) {
			setColor(Color.WHITE);
		}
	}

	public Color getColor() {
		return (Color) attributes.get(COLOR);
	}

	public void setColor(Color color) {
		attributes.put(COLOR, color);
	}

	/**
	 * @return the isOptimize
	 */
	public boolean isOptimize() {
		return (Boolean) attributes.get(IS_OPTIMIZE_SELECTED);
	}

	/**
	 * @param isOptimize
	 *            the isOptimize to set
	 */
	public void setOptimize(boolean isOptimize) {
		attributes.put(IS_OPTIMIZE_SELECTED, isOptimize);
		if (!isOptimize) {
			setQuality(null);
		} else {
			setQuality(getQuality());
		}
	}

	/**
	 * @return the isDither
	 */
	public boolean isDither() {
		return (Boolean) attributes.get(DITHER_SELECTED);
	}

	/**
	 * @param isDither
	 *            the isDither to set
	 */
	public void setDither(boolean isDither) {
		attributes.put(DITHER_SELECTED, isDither);
	}

	public void setQuality(String quality) {
		attributes.put(OPTIMIZE_SELECTION, quality == null ? "" : quality);
		if (OPT_QUALITY_HIGH.equals(quality)) {
			attributes.put(COLORIZE, ThemeTag.ATTR_VALUE_COLOUR_DEPTH_DEFAULT);
		} else if (OPT_QUALITY_PALETTE_DEP.equals(quality)) {
			attributes.put(COLORIZE, ThemeTag.ATTR_VALUE_COLOUR_DEPTH_C8);
			setDither(false);
		} else if (OPT_QUALITY_LOW.equals(quality)) {
			attributes.put(COLORIZE, ThemeTag.ATTR_VALUE_COLOUR_DEPTH_C8);
		} else {
			attributes.put(OPTIMIZE_SELECTION, null);
			attributes.put(COLORIZE, null);
		}
	}

	public String getQuality() {
		return (String) attributes.get(OPTIMIZE_SELECTION);
	}

	public Map<Object, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<Object, Object> attributes) {
		setQuality((String) attributes.get(OPTIMIZE_SELECTION));
		Boolean optimize = (Boolean) attributes.get(IS_OPTIMIZE_SELECTED);
		Boolean dither = (Boolean) attributes.get(DITHER_SELECTED);
		Boolean colorize = (Boolean) attributes.get(COLORIZE_SELECTED);
		setOptimize(optimize != null && optimize);
		setDither(dither != null && dither);
		setColorize(colorize != null && colorize);
		setColor((Color) attributes.get(COLOR));
	}
}
