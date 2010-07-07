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

import org.apache.batik.util.SVGConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGGradientElement;
import org.w3c.dom.svg.SVGLinearGradientElement;
import org.w3c.dom.svg.SVGRadialGradientElement;
import org.w3c.dom.svg.SVGStopElement;

/**
 * Class which wraps the SVGGradientElement , which can be Linear Gradient or
 * Radial Gradient
 */
public class SvgGradient {
	/**
	 * SVGGradientElement
	 */
	private SVGGradientElement svgGradientElement;

	/**
	 * ID representing the gradoent
	 */
	private String id;

	public SvgGradient(SVGGradientElement svGradientElement) {
		super();
		this.svgGradientElement = svGradientElement;
		this.id = this.svgGradientElement
				.getAttribute(SVGConstants.SVG_ID_ATTRIBUTE);
	}

	/**
	 * Getting the gradient id
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(String id) {
		this.svgGradientElement.setAttribute(SVGConstants.SVG_ID_ATTRIBUTE,
				"id");
	}

	/**
	 * Getting the SVGGradientElement
	 * 
	 * @return
	 */
	public SVGGradientElement getSvgGradientElement() {
		return svgGradientElement;
	}

	/**
	 * Setting the SVGGradientElement
	 * 
	 * @param svgGradientElement
	 * @throws SvgException
	 */
	public void setSvgGradientElement(SVGGradientElement svgGradientElement)
			throws SvgException {
		try {
			SVGDocumentUtils.removeChildren(this.svgGradientElement);
			SVGDocumentUtilsHelper.copyAttributes(svgGradientElement,
					this.svgGradientElement);
			this.svgGradientElement.setId(this.id);
			SVGDocumentUtilsHelper.copyChildren(svgGradientElement,
					this.svgGradientElement);
		} catch (DOMException e) {
			throw new SvgException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * Getting the SvgStopElement's . returns an arrray of
	 * com.nokia.tools.media.utils.svg.SvgStopElement
	 * 
	 * @return
	 */
	public SvgStopElement[] getSVGStopElements() {
		SvgStopElement[] stopElements = null;
		if (svgGradientElement != null && svgGradientElement.hasChildNodes()) {
			NodeList nodeList = svgGradientElement
					.getElementsByTagName(SVGConstants.SVG_STOP_TAG);
			stopElements = new SvgStopElement[nodeList.getLength()];
			for (int index = 0; index < nodeList.getLength(); index++) {
				stopElements[index] = new SvgStopElement(
						(SVGStopElement) nodeList.item(index),
						svgGradientElement);
			}
		}
		return stopElements;
	}

	/**
	 * Function checks whether the gradient is Radialgradient
	 * 
	 * @return
	 */
	public boolean isSVGRadialGradientElement() {
		return this.svgGradientElement instanceof SVGRadialGradientElement;
	}

	/**
	 * Function checks whether the gradient is Lineargradient
	 * 
	 * @return
	 */
	public boolean isSVGLinearGradientElement() {
		return this.svgGradientElement instanceof SVGLinearGradientElement;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(SVGConstants.SVG_ID_ATTRIBUTE);
		builder.append(":");
		builder.append(getId());
		builder.append(":");

		if (isSVGLinearGradientElement()) {
			builder.append("Type : SVGLinearGradientElement");
		}

		if (isSVGRadialGradientElement()) {
			builder.append("Type : SVGRadialGradientElement");
		}

		return builder.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SvgGradient) {
			SvgGradient gradient = (SvgGradient) obj;
			return (this.id.trim().equalsIgnoreCase(gradient.getId().trim()));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (int) (super.hashCode() * (int) (this.id.hashCode() >> 32));
	}
}
