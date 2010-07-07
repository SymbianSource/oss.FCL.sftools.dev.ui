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

import java.util.ArrayList;
import java.util.List;

import org.apache.batik.util.SVGConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGGradientElement;

/**
 * Convenient class to traverse,get colors/gradients from svg document
 * 
 * Caller should call
 * com.nokia.tools.media.utils.svg.SvgUtil.bootCSSandSVGDom(Document) on the
 * Document object before invoking any functions on this utility class
 * 
 */

public final class SVGDocumentUtils {

	public static final String SVG_FILL_ATTRIBUTE_INITIAL_VALUE = "#000000";

	public static final String SVG_FLOOD_COLOR_ATTRIBUTE_INITIAL_VALUE = "#000000";

	public static final String SVG_LIGHTING_COLOR_ATTRIBUTE_INITIAL_VALUE = "#FFFFFF";

	public static final String SVG_STOP_ATTRIBUTE_INITIAL_VALUE = "#000000";

	/**
	 * Given a Node, this function returns all the colors ( eg : 'fill',
	 * 'stroke', 'stop-color', 'flood-color', 'lighting-color' ) used as well as
	 * Gradients referenced in the node. This function returns a
	 * SvgColorComposite object , user can access the colors and gradients using
	 * the getter methods in SvgColorComposite
	 * SvgColorComposite.getSvgColors()/SvgColorComposite.getSvgGradients()
	 * 
	 * if isDefaultValueNeededForAllElements is true , this function will return
	 * the default/initial colors as well as colors explicitly defined for the
	 * elements present in the node
	 * 
	 * if isDefaultValueNeededForAllElements is false , this function returns
	 * the color value specified in the attribute list
	 * 
	 * @param node
	 * @return SvgColorComposite object
	 * @throws SvgException
	 */
	public static SvgColorComposite getAllColors(org.w3c.dom.Node node,
			boolean isDefaultValueNeededForAllElements) throws SvgException {
		List<Element> cachedNodeList = new ArrayList<Element>();
		List<SvgColor> svgColors = new ArrayList<SvgColor>();
		List<SvgGradient> svgGradients = new ArrayList<SvgGradient>();
		SVGDocumentUtilsHelper.getColor(node, SVGConstants.SVG_FILL_ATTRIBUTE,
				svgColors, svgGradients, cachedNodeList,
				isDefaultValueNeededForAllElements);
		SVGDocumentUtilsHelper.getColor(node,
				SVGConstants.SVG_STROKE_ATTRIBUTE, svgColors, svgGradients,
				cachedNodeList, isDefaultValueNeededForAllElements);
		SVGDocumentUtilsHelper.getColor(node,
				SVGConstants.SVG_FLOOD_COLOR_ATTRIBUTE, svgColors,
				svgGradients, cachedNodeList,
				isDefaultValueNeededForAllElements);
		SVGDocumentUtilsHelper.getColor(node,
				SVGConstants.SVG_LIGHTING_COLOR_ATTRIBUTE, svgColors,
				svgGradients, cachedNodeList,
				isDefaultValueNeededForAllElements);
		SvgColorComposite composite = new SvgColorComposite();
		composite.setSvgColors(svgColors);
		composite.setSvgGradients(svgGradients);
		cachedNodeList.clear();
		return composite;
	}

	/**
	 * Given a node , this function returns all the colors used as well as
	 * gradients referenced in the node , based on the input argument, type ( eg :
	 * 'fill', 'stroke', 'stop-color', 'flood-color', 'lighting-color' ).This
	 * function returns a SvgColorComposite object , user can access the colors
	 * and gradients using the getter methods in SvgColorComposite
	 * SvgColorComposite.getSvgColors()/SvgColorComposite.getSvgGradients()
	 * 
	 * if isDefaultValueNeededForAllElements is true , this function will return
	 * the default/initial colors as well as colors explicitly defined for the
	 * elements present in the node
	 * 
	 * if isDefaultValueNeededForAllElements is false , this function returns
	 * the color value specified in the attribute list
	 * 
	 * @param node
	 * @param type
	 * @return SvgColorComposite object
	 * @throws SvgException
	 */
	public static SvgColorComposite getColors(org.w3c.dom.Node node,
			String type, boolean isDefaultValueNeededForAllElements)
			throws SvgException {
		return SVGDocumentUtilsHelper.getColor(node, type,
				isDefaultValueNeededForAllElements);
	}

	/**
	 * Given an Element, this function locates the immediate child which is an
	 * Element and whose name matches the name given
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static org.w3c.dom.Element getElement(org.w3c.dom.Element element,
			java.lang.String name) {
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE
					&& child.getNodeName() == name) {
				return (Element) child;
			}
		}
		return null;
	}

	/**
	 * 
	 * Given an Element, this function locates all the immediate children which
	 * are Elements and whose name matches the name given.
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static java.util.List getElementList(org.w3c.dom.Element element,
			java.lang.String name) {
		NodeList children = element.getChildNodes();
		ArrayList resultList = new ArrayList();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE
					&& child.getNodeName() == name) {
				resultList.add(child);
			}
		}
		return resultList;
	}

	/**
	 * Given a node , this function returns all gradients( Radial gradients as
	 * well as Linear gradients ) present / referenced in the node. This
	 * function returns a Collection of SvgGradient objects .
	 * 
	 * @param node
	 * @return
	 * @throws SvgException
	 */
	public static List<SvgGradient> getGradients(Node node) throws SvgException {
		List<Element> cachedNodeList = new ArrayList<Element>();
		ArrayList<SvgGradient> svgGradients = new ArrayList<SvgGradient>();
		SVGDocumentUtilsHelper.getGradient(node,
				SVGConstants.SVG_LINEAR_GRADIENT_TAG, svgGradients,
				cachedNodeList);
		SVGDocumentUtilsHelper.getGradient(node,
				SVGConstants.SVG_RADIAL_GRADIENT_TAG, svgGradients,
				cachedNodeList);
		cachedNodeList.clear();
		return svgGradients;
	}

	/**
	 * Given a node , this function returns all the gradients
	 * referenced/present, depends on the input argument type. Type can be of
	 * value SVGConstants.SVG_LINEAR_GRADIENT_TAG or
	 * SVGConstants.SVG_RADIAL_GRADIENT_TAG This function returns a Collection
	 * of SvgGradient objects .
	 * 
	 * @param node
	 * @param type
	 * @return
	 * @throws SvgException
	 */
	public static List<SvgGradient> getGradients(Node node, String type)
			throws SvgException {
		List<Element> cachedNodeList = new ArrayList<Element>();
		ArrayList<SvgGradient> svgGradients = new ArrayList<SvgGradient>();
		SVGDocumentUtilsHelper.getGradient(node, type, svgGradients,
				cachedNodeList);
		cachedNodeList.clear();
		return svgGradients;
	}

	public static void removeChildren(Element element) {
		while (element.getFirstChild() != null) {
			element.removeChild(element.getFirstChild());
		}
	}

	/**
	 * 
	 * Given a node , this function sets the color for the attributeType (based
	 * on the input argument, attributeType eg : 'fill', 'stroke', 'stop-color',
	 * 'flood-color', 'lighting-color' ) present in the node reccursively, with
	 * the color value specified as argument , colorString
	 * 
	 * if shouldUpdateDefaultValue is true , this function will update the
	 * default/initial colors as well as colors defined for the elements present
	 * in the node
	 * 
	 * if shouldUpdateDefaultValue is false , this function will update the
	 * colors defined for visible elements
	 * 
	 * @param node
	 * @param attributeType
	 * @param colorString
	 * @param shouldUpdateDefaultValue
	 * @throws SvgException
	 */
	public static void setColor(Node node, String attributeType,
			String colorString, boolean shouldUpdateDefaultValue)
			throws SvgException {
		SvgColorComposite composite = getColors(node, attributeType,
				shouldUpdateDefaultValue);
		if (colorString == null)
			throw new SvgException("Color cann't be null");
		List<SvgColor> svgColors = composite.getSvgColors();
		for (SvgColor color : svgColors) {
			color.setColor(colorString);
		}
	}

	/**
	 * Given a node ,this function will update the occurrences of oldColor with
	 * colorToBeReplaced , for the type specified as argument
	 * 
	 * if shouldUpdateDefaultValue is true , this function will update the
	 * default/initial colors as well as colors defined for the elements present
	 * in the node
	 * 
	 * if shouldUpdateDefaultValue is false , this function will update the
	 * colors defined for visible elements
	 * 
	 * @param node
	 * @param attributeType
	 * @param oldColor
	 * @param colorToBeReplaced
	 * @param shouldUpdateDefaultValue
	 * @throws SvgException
	 */
	public static void setColor(Node node, String attributeType,
			String oldColor, String colorToBeReplaced,
			boolean shouldUpdateDefaultValue) throws SvgException {
		SvgColorComposite composite = SVGDocumentUtilsHelper.getColor(node,
				attributeType, shouldUpdateDefaultValue);
		List<SvgColor> colors = composite.getSvgColors();
		if (oldColor == null || colorToBeReplaced == null)
			throw new SvgException("Color cann't be null");
		String existingColor = SVGDocumentUtilsHelper.getColorValue(oldColor);
		String newColor = SVGDocumentUtilsHelper
				.getColorValue(colorToBeReplaced);

		for (SvgColor color : colors) {
			if (color.getColorAsHashString().equalsIgnoreCase(existingColor)) {
				color.setColor(newColor);
			}

		}

	}

	/**
	 * Given a node , this function will update the occurences of oldColor
	 * present in attributes (fill,stroke,flood-color,lighting-color) with
	 * colorToBeReplaced
	 * 
	 * if shouldUpdateDefaultValue is true , this function will update the
	 * default/initial colors as well as colors defined for the elements present
	 * in the node
	 * 
	 * if shouldUpdateDefaultValue is false , this function will update the
	 * colors defined for visible elements
	 * 
	 * if shouldUpdateStopColor value is true , it will update the stop colors
	 * also.
	 * 
	 * @param node
	 * @param oldColor
	 * @param colorToBeReplaced
	 * @param shouldUpdateDefaultValue
	 * @param shouldUpdateStopColor
	 * @throws SvgException
	 */
	public static void setColorAll(Node node, String oldColor,
			String colorToBeReplaced, boolean shouldUpdateDefaultValue,
			boolean shouldUpdateStopColor) throws SvgException {
		SvgColorComposite composite = getAllColors(node,
				shouldUpdateDefaultValue);
		List<SvgColor> colors = composite.getSvgColors();

		if (oldColor == null || colorToBeReplaced == null)
			throw new SvgException("Color cann't be null");

		String existingColor = SVGDocumentUtilsHelper.getColorValue(oldColor);
		String newColor = SVGDocumentUtilsHelper
				.getColorValue(colorToBeReplaced);

		for (SvgColor color : colors) {
			if (color.getColorAsHashString().equalsIgnoreCase(existingColor)) {
				color.setColor(newColor);
			}

		}
		if (shouldUpdateStopColor) {
			composite = getColors(node, SVGConstants.SVG_STOP_COLOR_ATTRIBUTE,
					shouldUpdateDefaultValue);
			colors = composite.getSvgColors();
			for (SvgColor color : colors) {
				if (color.getColorAsHashString()
						.equalsIgnoreCase(existingColor)) {
					color.setColor(newColor);
				}
			}
		}

	}

	/**
	 * Given a node , this function will update the
	 * fill,stroke,flood-color,lighting-color values with the colorString passed
	 * as argument
	 * 
	 * if shouldUpdateDefaultValue is true , this function will update the
	 * default/initial colors as well as colors defined for the elements present
	 * in the node
	 * 
	 * if shouldUpdateDefaultValue is false , this function will update the
	 * colors defined for visible elements
	 * 
	 * if shouldUpdateStopColor value is true , it will update the stop colors
	 * also.
	 * 
	 * @param node
	 * @param shouldUpdateDefaultValue
	 * @param shouldUpdateStopColor
	 * @param oldColor
	 * @param colorToBeReplaced
	 * @throws SvgException
	 */
	public static void setColorAll(Node node, String colorString,
			boolean shouldUpdateDefaultValue, boolean shouldUpdateStopColor)
			throws SvgException {
		SvgColorComposite composite = getAllColors(node,
				shouldUpdateDefaultValue);
		List<SvgColor> colors = composite.getSvgColors();

		if (colorString == null)
			throw new SvgException("Color cann't be null");

		String newColor = SVGDocumentUtilsHelper.getColorValue(colorString);

		for (SvgColor color : colors) {
			color.setColor(newColor);
		}
		if (shouldUpdateStopColor) {
			composite = getColors(node, SVGConstants.SVG_STOP_COLOR_ATTRIBUTE,
					shouldUpdateDefaultValue);
			colors = composite.getSvgColors();
			for (SvgColor color : colors) {
				color.setColor(newColor);
			}
		}

	}

	/**
	 * Given a node , this function updates the gradients present / referenced
	 * in the node , from the gradient values specified as Collection of
	 * SvgGradient . This function uses gradient id as reference to update the
	 * respective gradients
	 * 
	 * @param node
	 * @param svgGradients
	 * @throws SvgException
	 */
	public static void setGradients(Node node, List<SvgGradient> svgGradients)
			throws SvgException {
		try {
			List<SvgGradient> gradients = getGradients(node);
			SvgGradient svgGradient = null;
			for (SvgGradient gradient : svgGradients) {
				for (int innerIndex = 0; innerIndex < gradients.size(); innerIndex++) {
					svgGradient = gradients.get(innerIndex);
					if (svgGradient.equals(gradient)) {
						svgGradient.setSvgGradientElement(gradient
								.getSvgGradientElement());
					}
				}
			}
		} catch (DOMException e) {
			throw new SvgException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * Given a node , this function updates the gradient present / referenced in
	 * the node , from the gradient value specified as SvgGradient . This
	 * function uses gradient id as reference to update the respective gradient
	 * 
	 * @param node
	 * @param svgGradient
	 * @throws SvgException
	 */
	public static void setGradients(Node node, SvgGradient svgGradient)
			throws SvgException {
		try {
			List<SvgGradient> gradients = getGradients(node);
			SvgGradient gradient = null;
			for (int innerIndex = 0; innerIndex < gradients.size(); innerIndex++) {
				gradient = gradients.get(innerIndex);
				if (gradient.equals(svgGradient)) {
					gradient.setSvgGradientElement(svgGradient
							.getSvgGradientElement());
				}
			}
		} catch (DOMException e) {
			throw new SvgException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * Given a node , this function updates the gradients present / referenced
	 * in the node , from the gradient values specified as input array of
	 * SvgGradient . This function uses gradient id as reference to update the
	 * respective gradients
	 * 
	 * @param node
	 * @param svgGradients
	 * @throws SvgException
	 */
	public static void setGradients(Node node, SvgGradient[] svgGradients)
			throws SvgException {
		try {
			List<SvgGradient> gradients = getGradients(node);
			SvgGradient svgGradient = null;
			for (int outerIndex = 0; outerIndex < svgGradients.length; outerIndex++) {
				for (int innerIndex = 0; innerIndex < gradients.size(); innerIndex++) {
					svgGradient = gradients.get(innerIndex);
					if (svgGradient.equals(svgGradients[outerIndex])) {
						svgGradient
								.setSvgGradientElement(svgGradients[outerIndex]
										.getSvgGradientElement());
					}
				}
			}
		} catch (DOMException e) {
			throw new SvgException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * Given a node , this function updates the gradient present / referenced in
	 * the node , from the gradient value specified as SVGGradientElement . This
	 * function uses gradient id as reference to update the respective gradient
	 * 
	 * @param node
	 * @param svgGradient
	 * @throws SvgException
	 */
	public static void setGradients(Node node, SVGGradientElement svgGradient)
			throws SvgException {
		List<SvgGradient> gradients = getGradients(node);
		SvgGradient gradient = null;
		for (int innerIndex = 0; innerIndex < gradients.size(); innerIndex++) {
			gradient = gradients.get(innerIndex);
			if (gradient.getId().equalsIgnoreCase(
					svgGradient.getAttribute(SVGConstants.SVG_ID_ATTRIBUTE))) {
				gradient.setSvgGradientElement(svgGradient);
			}
		}
	}

	/**
	 * Given a node , this function updates the gradients present / referenced
	 * in the node , from the gradient values specified as input array of
	 * SVGGradientElement . This function uses gradient id as reference to
	 * update the respective gradients
	 * 
	 * @param node
	 * @param svgGradients
	 * @throws SvgException
	 */
	public static void setGradients(Node node, SVGGradientElement[] svgGradients)
			throws SvgException {
		List<SvgGradient> gradients = getGradients(node);
		SvgGradient svgGradient = null;
		for (int outerIndex = 0; outerIndex < svgGradients.length; outerIndex++) {
			for (int innerIndex = 0; innerIndex < gradients.size(); innerIndex++) {
				svgGradient = gradients.get(innerIndex);
				if (svgGradient.getId().equals(
						svgGradients[outerIndex]
								.getAttribute(SVGConstants.SVG_ID_ATTRIBUTE))) {
					svgGradient.setSvgGradientElement(svgGradients[outerIndex]);
				}
			}
		}
	}

}
