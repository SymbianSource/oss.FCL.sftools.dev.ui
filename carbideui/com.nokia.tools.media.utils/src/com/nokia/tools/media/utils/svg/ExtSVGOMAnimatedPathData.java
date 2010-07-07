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

import org.apache.batik.dom.svg.AbstractElement;
import org.apache.batik.dom.svg.AbstractSVGNormPathSegList;
import org.apache.batik.dom.svg.SVGOMAnimatedPathData;
import org.apache.batik.dom.svg.SVGOMElement;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGPathSegList;

public class ExtSVGOMAnimatedPathData extends SVGOMAnimatedPathData {
	/**
	 * SVGNormPathSegList mapping the static 'd' attribute.
	 */
	protected AbstractSVGNormPathSegList normalizedPathSegs;

	public ExtSVGOMAnimatedPathData(AbstractElement arg0, String arg1,
			String arg2, String arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public class SVGOMNormalizedPathSegList extends AbstractSVGNormPathSegList {

		/**
		 * Create a DOMException.
		 */
		protected DOMException createDOMException(short type, String key,
				Object[] args) {
			return element.createDOMException(type, key, args);
		}

		/**
		 * Create a SVGException.m
		 */
		protected SVGException createSVGException(short type, String key,
				Object[] args) {

			return ((SVGOMElement) element).createSVGException(type, key, args);
		}

		/**
		 * Retrieve the normalized value of the attribute 'points'.
		 */
		protected String getValueAsString() throws SVGException {
			Attr attr = element.getAttributeNodeNS(namespaceURI, localName);
			if (attr == null) {
				return defaultValue;
			}
			return attr.getValue();
		}

		/**
		 * Set the value of the attribute 'points'
		 */
		protected void setAttributeValue(String value) {
			try {
				changing = true;
				element.setAttributeNS(namespaceURI, localName, value);
			} finally {
				changing = false;
			}
		}
	}

	public SVGPathSegList getNormalizedPathSegList() {
		if (normalizedPathSegs == null) {
			normalizedPathSegs = new SVGOMNormalizedPathSegList();

		}
		return normalizedPathSegs;
	}

}
