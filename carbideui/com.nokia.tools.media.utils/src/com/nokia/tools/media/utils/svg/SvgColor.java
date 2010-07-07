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
package com.nokia.tools.media.utils.svg;

import java.awt.Color;

import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.svg.SVGColor;
import org.w3c.dom.svg.SVGPaint;

import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.color.ValuedColors;

/**
 * Class which wraps the SVGColor object
 */
@SuppressWarnings("unused")
public class SvgColor {
	/**
	 * SVGColor object
	 */
	private org.w3c.dom.svg.SVGColor svgColor;

	/**
	 * Owner Node
	 */
	private org.w3c.dom.Node ownerNode;

	/**
	 * Attribute type where the color is present
	 */
	private String attributeType;

	/**
	 * Getting the owner node
	 * 
	 * @return
	 */
	public org.w3c.dom.Node getOwnerNode() {
		return ownerNode;
	}

	public SvgColor(SVGColor svgColor, Node ownerNode, String attributeType) {
		super();
		this.svgColor = svgColor;
		this.ownerNode = ownerNode;
		this.attributeType = attributeType;
	}

	/**
	 * Getting the Color As Hexadecimal Represenattion
	 * 
	 * @return
	 */
	public String getColorAsHashString() {
		return ColorUtil.asHashString(getColorFromSVGColor(this.svgColor));
	};

	/**
	 * Getting the color as RGB
	 * 
	 * @return
	 */
	public RGB getRGB() {
		return ColorUtil.getRGB(ColorUtil
				.asHashString(getColorFromSVGColor(this.svgColor)));
	}

	/**
	 * Updating the color string
	 * 
	 * @param colorString
	 * @throws SvgException
	 */
	public void setColor(String colorString) throws SvgException {
		if (ColorUtil.isColor(colorString)) {
			updateNode(ColorUtil.asHashString(ColorUtil.getRGB(colorString)));
		} else {
			if (ValuedColors.isCssColorName(colorString)) {
				updateNode(ValuedColors.getNamedColorValue(colorString));
			}
		}
	}

	/**
	 * Updating the color , input is RGB color value
	 * 
	 * @param color
	 * @throws SvgException
	 */
	public void setColor(RGB color) throws SvgException {
		updateNode(ColorUtil.asHashString(color));

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(attributeType);
		builder.append(":");
		builder.append(ColorUtil.asHashString(getColorFromSVGColor(svgColor)));
		builder.append(";");
		builder.append("ownerNode");
		builder.append(":");
		builder.append(ownerNode.getNodeName());
		builder.append(";");
		return builder.toString();
	}

	private Color getColorFromSVGColor(SVGColor clr) {
		Color color = null;
		if (clr instanceof SVGPaint) {
			SVGPaint svgColor = (SVGPaint) clr;
			if (svgColor.getPaintType() == SVGColor.SVG_COLORTYPE_RGBCOLOR) {
				color = new Color(svgColor.getRGBColor().getRed()
						.getFloatValue(CSSValue.CSS_PRIMITIVE_VALUE) / 255,
						svgColor.getRGBColor().getGreen().getFloatValue(
								CSSValue.CSS_PRIMITIVE_VALUE) / 255, svgColor
								.getRGBColor().getBlue().getFloatValue(
										CSSValue.CSS_PRIMITIVE_VALUE) / 255);
			}
		} else if (clr instanceof SVGColor) {
			if (clr.getColorType() == SVGColor.SVG_COLORTYPE_RGBCOLOR) {
				color = new Color(svgColor.getRGBColor().getRed()
						.getFloatValue(CSSValue.CSS_PRIMITIVE_VALUE) / 255,
						svgColor.getRGBColor().getGreen().getFloatValue(
								CSSValue.CSS_PRIMITIVE_VALUE) / 255, svgColor
								.getRGBColor().getBlue().getFloatValue(
										CSSValue.CSS_PRIMITIVE_VALUE) / 255);
			}
		}
		return color;
	}

	/**
	 * Getting the SvgColor object
	 * 
	 * @return
	 */
	public org.w3c.dom.svg.SVGColor getSvgColor() {
		return svgColor;
	}

	private void updateNode(String color) throws SvgException {
		try {
			((Element) ownerNode).setAttribute(attributeType, color);
		} catch (DOMException e) {
			throw new SvgException(e.getMessage(), e.getCause());
		}
	}

	public void setOwnerNode(org.w3c.dom.Node ownerNode) {
		this.ownerNode = ownerNode;
	}
}
