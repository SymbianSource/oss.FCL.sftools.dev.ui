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
package com.nokia.tools.platform.core;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a device display, which is identified by the width,
 * height, orientation and variant.
 */
public class Display implements Cloneable {
	public static final Pattern PATTERN = Pattern
			.compile("^\\s*([0-9]+)\\s*x\\s*([0-9]+)\\s*(.*)");

	public static final String DEFAULT_VARIANT = "eur";
	public static final String DEFAULT_TYPE = "";

	private int width;
	private int height;
	private String variant = DEFAULT_VARIANT;
	private String type = DEFAULT_TYPE;

	/**
	 * Constructs a new display object.
	 * 
	 * @param width width of the display.
	 * @param height height of the display
	 * @param variant the variant.
	 * @param type the type.
	 * @exception IllegalArgumentException if either width or height is less
	 *                than or equal to zero.
	 */
	public Display(int width, int height, String variant, String type) {
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException("Invalid width/height: " + width
					+ "x" + height);
		}
		this.width = width;
		this.height = height;
		setVariant(variant);
		setType(type);
	}

	/**
	 * Constructs a new display object with default variant.
	 * 
	 * @param width width of the display.
	 * @param height height of the display
	 * @exception IllegalArgumentException if either width or height is less
	 *                than or equal to zero.
	 */
	public Display(int width, int height) {
		this(width, height, null, null);
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		if (height <= 0) {
			throw new IllegalArgumentException("Invalid height: " + height);
		}
		this.height = height;
	}

	/**
	 * @return the orientation
	 */
	public Orientation getOrientation() {
		return width > height ? Orientation.LANDSCAPE
				: (width == height ? Orientation.SQUARE : Orientation.PORTRAIT);
	}

	/**
	 * @param orientation the orientation to set
	 */
	public void setOrientation(Orientation orientation) {
		int w, h;
		if (Orientation.PORTRAIT == orientation) {
			w = Math.min(width, height);
			h = Math.max(width, height);
		} else if (Orientation.LANDSCAPE == orientation) {
			w = Math.max(width, height);
			h = Math.min(width, height);
		} else if (Orientation.SQUARE == orientation) {
			w = h = Math.max(width, height);
		} else {
			w = width;
			h = height;
		}
		width = w;
		height = h;
	}

	/**
	 * @param orientation the orientation to set
	 */
	public void setOrientation(String orientation) {
		setOrientation(parseOrientation(orientation));
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		if (width <= 0) {
			throw new IllegalArgumentException("Invalid width: " + width);
		}
		this.width = width;
	}

	/**
	 * @return the variant
	 */
	public String getVariant() {
		return variant;
	}

	/**
	 * @param variant the variant to set
	 */
	public void setVariant(String variant) {
		if (variant != null) {
			this.variant = variant;
		}
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		if (type != null) {
			this.type = type;
		}
	}

	public boolean supportsOrientation(Orientation orientation) {
		return orientation == null || getOrientation() == orientation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return width + "x" + height + type + "@" + variant;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Display) {
			Display b = (Display) obj;
			return width == b.width && height == b.height
					&& variant.equals(b.variant) && type.equals(b.type);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return width ^ height ^ variant.hashCode() ^ type.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

	/**
	 * Parses the string to the display.
	 * 
	 * @param str the string representation of display in the format:
	 *            {@link #PATTERN}
	 * @return the display or null if the string is not valid.
	 */
	public static Display valueOf(String str) {
		Matcher matcher = PATTERN.matcher(str);
		if (matcher.matches()) {
			int width = Integer.parseInt(matcher.group(1));
			int height = Integer.parseInt(matcher.group(2));
			String type = matcher.group(3);
			return new Display(width, height, null, type);
		}
		return null;
	}

	/**
	 * Formats the display according to the format provided.
	 * 
	 * @param format the display format.
	 * @return the display format.
	 */
	public String format(String format) {
		return MessageFormat.format(format, new Object[] { width, height, type,
				variant });
	}

	/**
	 * Parses the string to orientation.
	 * 
	 * @param orientation the orientation string to parse.
	 * @return the orientation or null if not matched.
	 */
	public static Orientation parseOrientation(String orientation) {
		if (Orientation.LANDSCAPE.name().equalsIgnoreCase(orientation)) {
			return Orientation.LANDSCAPE;
		}
		if (Orientation.PORTRAIT.name().equalsIgnoreCase(orientation)) {
			return Orientation.PORTRAIT;
		}
		if (Orientation.SQUARE.name().equalsIgnoreCase(orientation)) {
			return Orientation.SQUARE;
		}
		return null;
	}
}
