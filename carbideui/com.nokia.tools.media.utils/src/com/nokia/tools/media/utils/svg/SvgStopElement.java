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

import org.apache.batik.css.dom.CSSOMComputedStyle;
import org.apache.batik.util.SVGConstants;
import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGColor;
import org.w3c.dom.svg.SVGGradientElement;
import org.w3c.dom.svg.SVGPaint;
import org.w3c.dom.svg.SVGStopElement;

import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.color.ValuedColors;

/**
 * Class which wraps the SVGStopElement
 */
public class SvgStopElement {
	/**
	 * SVGStopElement
	 */

	private SVGStopElement svgStopElement;

	/**
	 * SVGGradientElement ,owner node for svgStopElement
	 */
	private SVGGradientElement ownerNode;

	/**
	 * Getting the owner node
	 * 
	 * @return
	 */
	public Node getOwnerNode() {
		return (Node) ownerNode;
	}

	public SvgStopElement(SVGStopElement svgStopElement,
			SVGGradientElement ownerNode) {
		super();
		this.svgStopElement = svgStopElement;
		this.ownerNode = ownerNode;
	}

	/**
	 * Getting the stop color
	 * 
	 * @return
	 */
	public SvgColor getStopColor() {
		CSSOMComputedStyle computedStyle = SvgUtil
				.getComputedStyle((Element) svgStopElement);
		SvgColor color = null;

		if (svgStopElement.hasAttribute(SVGConstants.SVG_STOP_COLOR_ATTRIBUTE)) {
			if (computedStyle
					.getPropertyCSSValue(SVGConstants.SVG_STOP_COLOR_ATTRIBUTE) instanceof SVGPaint) {
				SVGPaint svgColor = (SVGPaint) computedStyle
						.getPropertyCSSValue(SVGConstants.SVG_STOP_COLOR_ATTRIBUTE);
				if (svgColor.getPaintType() == SVGColor.SVG_COLORTYPE_RGBCOLOR) {
					color = new SvgColor(svgColor, svgStopElement,
							SVGConstants.SVG_STOP_COLOR_ATTRIBUTE);
				}
			} else {
				SVGColor SVGclr = (SVGColor) computedStyle
						.getPropertyCSSValue(SVGConstants.SVG_STOP_COLOR_ATTRIBUTE);
				if (SVGclr.getColorType() == SVGColor.SVG_COLORTYPE_RGBCOLOR) {
					color = new SvgColor(SVGclr, svgStopElement,
							SVGConstants.SVG_STOP_COLOR_ATTRIBUTE);
				}
			}

		}
		return color;
	}

	/**
	 * Getting the stop color as RGB
	 * 
	 * @return
	 */
	public RGB getStopColorAsRGB() {
		return getStopColor().getRGB();
	}

	/**
	 * Setting the stop color
	 * 
	 * @param colorString
	 */
	public void setStopColor(String colorString) {
		if (ValuedColors.isCssColorName(colorString)) {
			svgStopElement.setAttribute(SVGConstants.SVG_STOP_COLOR_ATTRIBUTE,
					ValuedColors.getNamedColorValue(colorString));
		} else {
			if (ColorUtil.isColor(colorString)) {
				svgStopElement.setAttribute(
						SVGConstants.SVG_STOP_COLOR_ATTRIBUTE, colorString);
			}
		}

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(SVGConstants.SVG_STOP_COLOR_ATTRIBUTE);
		builder.append(":");
		builder.append(getStopColor().getColorAsHashString());
		builder.append(";");
		builder.append(SVGConstants.SVG_OFFSET_ATTRIBUTE);
		builder.append(":");
		builder.append(getOffset());
		builder.append(";");
		builder.append(SVGConstants.SVG_STOP_OPACITY_ATTRIBUTE);
		builder.append(":");
		builder.append(getOpacity());
		builder.append(";");
		return builder.toString();
	}

	/**
	 * Getting the stop offset value
	 * 
	 * @return
	 */
	public String getOffset() {
		return svgStopElement.getAttribute(SVGConstants.SVG_OFFSET_ATTRIBUTE);
	}

	/**
	 * Setting the stop offset value
	 * 
	 * @param offset
	 */
	public void setOffset(String offset) {
		svgStopElement.setAttribute(SVGConstants.SVG_OFFSET_ATTRIBUTE, offset);

	}

	/**
	 * getting the stop opacity
	 * 
	 * @return
	 */
	public String getOpacity() {
		return svgStopElement
				.getAttribute(SVGConstants.SVG_STOP_OPACITY_ATTRIBUTE);
	}

	/**
	 * Setting stop opacity
	 * 
	 * @param opacity
	 */
	public void setOpacity(String opacity) {
		svgStopElement.setAttribute(SVGConstants.SVG_STOP_OPACITY_ATTRIBUTE,
				opacity);
	}

}
