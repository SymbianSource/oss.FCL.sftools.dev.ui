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
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.batik.css.dom.CSSOMComputedStyle;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGAElement;
import org.w3c.dom.svg.SVGAltGlyphElement;
import org.w3c.dom.svg.SVGAnimateColorElement;
import org.w3c.dom.svg.SVGAnimateElement;
import org.w3c.dom.svg.SVGCircleElement;
import org.w3c.dom.svg.SVGClipPathElement;
import org.w3c.dom.svg.SVGColor;
import org.w3c.dom.svg.SVGDefsElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGEllipseElement;
import org.w3c.dom.svg.SVGFEDiffuseLightingElement;
import org.w3c.dom.svg.SVGFEFloodElement;
import org.w3c.dom.svg.SVGFEImageElement;
import org.w3c.dom.svg.SVGFESpecularLightingElement;
import org.w3c.dom.svg.SVGFilterElement;
import org.w3c.dom.svg.SVGFontElement;
import org.w3c.dom.svg.SVGForeignObjectElement;
import org.w3c.dom.svg.SVGGElement;
import org.w3c.dom.svg.SVGGlyphElement;
import org.w3c.dom.svg.SVGGradientElement;
import org.w3c.dom.svg.SVGLineElement;
import org.w3c.dom.svg.SVGLinearGradientElement;
import org.w3c.dom.svg.SVGMarkerElement;
import org.w3c.dom.svg.SVGMaskElement;
import org.w3c.dom.svg.SVGMissingGlyphElement;
import org.w3c.dom.svg.SVGPaint;
import org.w3c.dom.svg.SVGPathElement;
import org.w3c.dom.svg.SVGPatternElement;
import org.w3c.dom.svg.SVGPolygonElement;
import org.w3c.dom.svg.SVGPolylineElement;
import org.w3c.dom.svg.SVGRadialGradientElement;
import org.w3c.dom.svg.SVGRectElement;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGStopElement;
import org.w3c.dom.svg.SVGSwitchElement;
import org.w3c.dom.svg.SVGSymbolElement;
import org.w3c.dom.svg.SVGTRefElement;
import org.w3c.dom.svg.SVGTSpanElement;
import org.w3c.dom.svg.SVGTextElement;
import org.w3c.dom.svg.SVGTextPathElement;
import org.w3c.dom.svg.SVGUseElement;

import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.color.ValuedColors;

/**
 * Helper class for SVGDocumentUtils , All the methods in this class is helper
 * methods to SVGDocumentUtils class , This class is not expected to use outside
 * the media.utils package
 */
final class SVGDocumentUtilsHelper {

	private static String XLINK_URI = "http://www.w3.org/1999/xlink";

	public static void copyAttributes(Element from, Element to) {
		NamedNodeMap attrMap = from.getAttributes();
		Attr atr = null;
		for (int i = 0; i < attrMap.getLength(); i++) {
			Node attr = attrMap.item(i);
			atr = (Attr) from.getAttributeNode(attr.getNodeName());
			if (atr != null && atr.getSpecified()) {
				String name = attr.getNodeName();
				String value = attr.getNodeValue();
				to.setAttribute(name, value);

			}
		}
	}

	public static HashMap<String, XmlNode> getAttributes(Element element,
			HashMap<String, XmlNode> map) {
		NamedNodeMap attrMap = element.getAttributes();
		Attr atr = null;
		String namespace = null;
		String nodeName = null;
		for (int i = 0; i < attrMap.getLength(); i++) {
			Node attr = attrMap.item(i);
			namespace = attr.getNamespaceURI();
			nodeName = attr.getNodeName();
			if (namespace != null) {
				nodeName = attr.getLocalName();
			}
			atr = (Attr) element.getAttributeNodeNS(namespace, nodeName);
			if (atr != null && atr.getSpecified()) {
				String value = atr.getNodeValue();
				if (!map.containsKey(nodeName)) {
					map.put(nodeName, new XmlNode(value, namespace));
				}
			}
		}
		return map;
	}

	public static void copyChildren(Element from, Element to) {
		while (from.getFirstChild() != null) {
			Node childToMove = from.removeChild(from.getFirstChild());
			childToMove = to.getOwnerDocument().importNode(childToMove, true);
			to.appendChild(childToMove);
		}
	}

	public static SvgColorComposite getColor(org.w3c.dom.Node node,
			String type, boolean isDefaultValueNeededForAllElements)
			throws SvgException {
		List<Element> cachedNodeList = new ArrayList<Element>();
		List<SvgColor> svgColors = new ArrayList<SvgColor>();
		ArrayList<SvgGradient> svgGradients = new ArrayList<SvgGradient>();
		getColor(node, type, svgColors, svgGradients, cachedNodeList,
				isDefaultValueNeededForAllElements);
		SvgColorComposite composite = new SvgColorComposite();
		composite.setSvgColors(svgColors);
		composite.setSvgGradients(svgGradients);
		cachedNodeList.clear();
		return composite;
	}

	public static void getColor(org.w3c.dom.Node node, String type,
			java.util.List<SvgColor> svgColors, List<SvgGradient> svgGradients,
			List<Element> cachedNodeList,
			boolean isDefaultValueNeededForAllElements) throws SvgException {
		cachedNodeList=new ArrayList<Element>();
		getColorM(node, type, svgColors, svgGradients, cachedNodeList,
				isDefaultValueNeededForAllElements);
	}

	public static void getColorFromComputedStyleMap(Node node, Element element,
			CSSOMComputedStyle computedStyle, String type,
			List<SvgColor> svgColors, List<Element> cachedNodeList,
			List<SvgGradient> svgGradients) throws SvgException {
		SvgColor color = null;
		String referedId = null;
		Element referedElement = null;
		if (computedStyle == null)
			return;
		if (computedStyle.getPropertyCSSValue(type) instanceof SVGPaint) {
			SVGPaint svgColor = (SVGPaint) computedStyle
					.getPropertyCSSValue(type);
			if (svgColor.getPaintType() == SVGColor.SVG_COLORTYPE_RGBCOLOR) {
				color = new SvgColor(svgColor, element, type);
				svgColors.add(color);
				cachedNodeList.add(element);

			} else if (svgColor.getPaintType() == 107) {
				referedId = getReferedID(element.getAttribute(type));
				referedElement = node.getOwnerDocument().getElementById(
						referedId);
				if (referedElement instanceof SVGGradientElement) {
					referedElement = getReferedGradientElement((SVGGradientElement) referedElement);
				}
				if (!cachedNodeList.contains(referedElement)) {
					cachedNodeList.add(referedElement);
					if (referedElement instanceof SVGGradientElement) {
						svgGradients.add(new SvgGradient(
								(SVGGradientElement) referedElement));
					}
				}
			} else {
				// do nothing , what should i do , if color
				// attribute
				// value is none
			}
		} else {
			SVGColor SVGclr = (SVGColor) computedStyle
					.getPropertyCSSValue(type);
			if (SVGclr.getColorType() == SVGColor.SVG_COLORTYPE_RGBCOLOR) {
				color = new SvgColor(SVGclr, element, type);
				svgColors.add(color);
				cachedNodeList.add(element);
			} else if (SVGclr.getColorType() == 107) {
				referedId = getReferedID(element.getAttribute(type));
				referedElement = node.getOwnerDocument().getElementById(
						referedId);
				if (referedElement instanceof SVGGradientElement) {
					referedElement = getReferedGradientElement((SVGGradientElement) element);
				}
				if (!cachedNodeList.contains(referedElement)) {
					cachedNodeList.add(referedElement);
					if (referedElement instanceof SVGGradientElement) {
						svgGradients.add(new SvgGradient(
								(SVGGradientElement) referedElement));
					}
				}
			} else {
				// do nothing , what should i do , if color
				// attribute
				// value
				// is
				// none
			}

		}
	}

	public static void getColorM(org.w3c.dom.Node node, String type,
			java.util.List<SvgColor> svgColors, List<SvgGradient> svgGradients,
			List<Element> cachedNodeList,
			boolean isDefaultValueNeededForAllElements) throws SvgException {
		try {
			NodeList childNodes = null;
			if (node instanceof SVGElement) {
				Element element = (Element) node;

				if (element.hasAttribute("style")) {
					SvgUtil.extractStyleAttributes((Element) node);
				}
				if (!cachedNodeList.contains(element)) {
					CSSOMComputedStyle computedStyle = SvgUtil
							.getComputedStyle(element);
					if (isDefaultValueNeededForAllElements) {
						if (isDefaultElementPresnt(element, type)) {
							getColorFromComputedStyleMap(node, element,
									computedStyle, type, svgColors,
									cachedNodeList, svgGradients);
						}
					} else {
						if (element.hasAttribute(type)) {
							getColorFromComputedStyleMap(node, element,
									computedStyle, type, svgColors,
									cachedNodeList, svgGradients);
						}
					}
				}
			}
			if (node.hasChildNodes()) {
				childNodes = node.getChildNodes();
				int length = childNodes.getLength();
				for (int index = 0; index < length; index++) {
					getColorM(childNodes.item(index), type, svgColors,
							svgGradients, cachedNodeList,
							isDefaultValueNeededForAllElements);
				}
			}
		} catch (DOMException e) {
			throw new SvgException(e.getMessage(), e.getCause());
		}
	}

	public static String getColorValue(String colorString) {
		String color = null;
		if (ValuedColors.isCssColorName(colorString)) {
			color = ValuedColors.getNamedColorValue(colorString);
		} else if (ColorUtil.isColor(colorString)) {
			color = ColorUtil.asHashString(ColorUtil.getRGB(colorString));
		}
		return color;
	}

	public static void getGradient(Node node, String type,
			ArrayList<SvgGradient> svgGradients, List<Element> cachedNodeList)
			throws SvgException {
		try {
			NodeList childNodes = null;
			if (node instanceof SVGElement) {
				Element element = (Element) node;
				Element referedElement = null;
				if (element.hasAttribute("style")) {
					SvgUtil.extractStyleAttributes((Element) node);
				}
				if (!cachedNodeList.contains(element)) {
					if (type
							.equalsIgnoreCase(SVGConstants.SVG_LINEAR_GRADIENT_TAG)
							&& element instanceof SVGLinearGradientElement) {
						referedElement = getReferedGradientElement((SVGGradientElement) element);
						if (!cachedNodeList.contains(referedElement)) {
							svgGradients.add(new SvgGradient(
									(SVGGradientElement) referedElement));
							cachedNodeList.add(referedElement);
						}

					} else if (type
							.equalsIgnoreCase(SVGConstants.SVG_RADIAL_GRADIENT_TAG)
							&& element instanceof SVGRadialGradientElement) {
						referedElement = getReferedGradientElement((SVGGradientElement) element);
						if (!cachedNodeList.contains(referedElement)) {
							svgGradients.add(new SvgGradient(
									(SVGRadialGradientElement) referedElement));
							cachedNodeList.add(referedElement);
						}
					} else {
						getGradientsFromAttributes(element, svgGradients,
								cachedNodeList,
								SVGConstants.SVG_FILL_ATTRIBUTE, node
										.getOwnerDocument(), type);
						getGradientsFromAttributes(element, svgGradients,
								cachedNodeList,
								SVGConstants.SVG_STROKE_ATTRIBUTE, node
										.getOwnerDocument(), type);
					}
				}
			}
			if (node.hasChildNodes()) {
				childNodes = node.getChildNodes();
				int length = childNodes.getLength();
				for (int index = 0; index < length; index++) {
					getGradient(childNodes.item(index), type, svgGradients,
							cachedNodeList);
				}
			}
		} catch (DOMException e) {
			throw new SvgException(e.getMessage(), e.getCause());
		}
	}

	public static void getGradientsFromAttributes(Element element,
			ArrayList<SvgGradient> svgGradients, List<Element> cachedNodeList,
			String type, Document ownerDocument, String gradientType)
			throws SvgException {
		try {
			String referedId = null;
			Element referedElement = null;
			CSSOMComputedStyle computedStyle = SvgUtil
					.getComputedStyle(element);
			if (computedStyle == null)
				return;
			if (element.hasAttribute(type)) {
				if (computedStyle.getPropertyCSSValue(type) instanceof SVGPaint) {
					SVGPaint svgColor = (SVGPaint) computedStyle
							.getPropertyCSSValue(type);
					if (svgColor.getPaintType() == 107) {
						referedId = getReferedID(element.getAttribute(type));
						referedElement = ownerDocument
								.getElementById(referedId);

						if (!cachedNodeList.contains(referedElement)) {
							if (referedElement instanceof SVGGradientElement) {
								referedElement = getReferedGradientElement((SVGGradientElement) referedElement);
							}
							if (gradientType
									.equalsIgnoreCase(SVGConstants.SVG_LINEAR_GRADIENT_TAG)
									&& referedElement instanceof SVGLinearGradientElement) {
								if (!cachedNodeList.contains(referedElement)) {
									svgGradients
											.add(new SvgGradient(
													(SVGGradientElement) referedElement));
									cachedNodeList.add(referedElement);
								}
							} else if (gradientType
									.equalsIgnoreCase(SVGConstants.SVG_RADIAL_GRADIENT_TAG)
									&& referedElement instanceof SVGRadialGradientElement) {
								if (!cachedNodeList.contains(referedElement)) {
									svgGradients
											.add(new SvgGradient(
													(SVGRadialGradientElement) referedElement));
									cachedNodeList.add(referedElement);
								}
							}
						}

					}
				} else {
					SVGColor SVGclr = (SVGColor) computedStyle
							.getPropertyCSSValue(type);
					if (SVGclr.getColorType() == 107) {
						referedId = getReferedID(element.getAttribute(type));
						referedElement = ownerDocument
								.getElementById(referedId);
						if (referedElement instanceof SVGGradientElement) {
							referedElement = getReferedGradientElement((SVGGradientElement) element);
						}
						if (!cachedNodeList.contains(referedElement)) {
							if (gradientType
									.equalsIgnoreCase(SVGConstants.SVG_LINEAR_GRADIENT_TAG)
									&& referedElement instanceof SVGLinearGradientElement) {
								if (!cachedNodeList.contains(referedElement)) {
									svgGradients
											.add(new SvgGradient(
													(SVGGradientElement) referedElement));
									cachedNodeList.add(referedElement);
								}
							} else if (gradientType
									.equalsIgnoreCase(SVGConstants.SVG_RADIAL_GRADIENT_TAG)
									&& referedElement instanceof SVGRadialGradientElement) {
								if (!cachedNodeList.contains(referedElement)) {
									svgGradients
											.add(new SvgGradient(
													(SVGGradientElement) referedElement));
									cachedNodeList.add(referedElement);
								}
							}
						}
					}

				}

			}
		} catch (DOMException e) {
			throw new SvgException(e.getMessage(), e.getCause());
		}
	}

	public static SvgColor getParentColor(Element element, String type) {
		if (element == null)
			return null;
		CSSOMComputedStyle computedStyle = SvgUtil.getComputedStyle(element);
		if (computedStyle == null)
			return null;
		if (computedStyle.getPropertyCSSValue(type) instanceof SVGPaint) {
			SVGPaint svgColor = (SVGPaint) computedStyle
					.getPropertyCSSValue(type);
			if (svgColor.getPaintType() == SVGColor.SVG_COLORTYPE_RGBCOLOR) {
				return new SvgColor(svgColor, element, type);
			}
		} else {
			SVGColor SVGclr = (SVGColor) computedStyle
					.getPropertyCSSValue(type);
			if (SVGclr.getColorType() == SVGColor.SVG_COLORTYPE_RGBCOLOR) {
				return new SvgColor(SVGclr, element, type);

			}
		}
		return null;

	}

	public static SVGGradientElement getReferedGradientElement(
			SVGGradientElement element) throws SvgException {
		String referedId = null;
		try {
			if (isReferedSomeOtherGradients(element)) {
				referedId = getReferedGradientID(element.getAttributeNS(
						XLINK_URI, "href"));
				element = (SVGGradientElement) element.getOwnerDocument()
						.getElementById(referedId);

				if (isReferedSomeOtherGradients(element)) {
					return getReferedGradientElement(element);
				} else {
					return element;
				}

			}
		} catch (DOMException e) {
			throw new SvgException(e.getMessage(), e.getCause());
		}
		return element;
	}

	public static String getReferedGradientID(String rfd) {
		return rfd.substring(rfd.indexOf("#") + 1, rfd.length());
	}

	public static String getReferedID(String rfd) {
		return rfd.substring(rfd.indexOf("#") + 1, rfd.length() - 1);
	}

	public static boolean isReferedSomeOtherGradients(SVGGradientElement element) {
		return element.hasAttributeNS(XLINK_URI,
				SVGConstants.SVG_HREF_ATTRIBUTE);
	}

	public static void setColorRecursively(Node node, String type,
			String colorString, List<Element> cachedNodeList)
			throws SvgException {
		try {
			String color = null;
			if (node instanceof Element) {
				Element element = (Element) node;
				if (!cachedNodeList.contains(element)) {
					if (element.hasAttribute(type)) {
						color = getColorValue(colorString);
						element.setAttribute(type, color);
						cachedNodeList.add(element);
					}
				}
			}
			NodeList childNodes = node.getChildNodes();
			for (int index = 0; index < childNodes.getLength(); index++) {
				setColorRecursively(childNodes.item(index), type, colorString,
						cachedNodeList);
			}
		} catch (DOMException e) {
			throw new SvgException(e.getMessage(), e.getCause());
		}

	}

	private static boolean isDefaultElementPresnt(Element element, String type) {

		if (type.equalsIgnoreCase(SVGConstants.SVG_FILL_ATTRIBUTE)) {
			return isDefaultPresentForFill(element);
		} else if (type.equalsIgnoreCase(SVGConstants.SVG_STROKE_ATTRIBUTE)) {
			return isDefaultPresentForSTROKE(element);
		} else if (type.equalsIgnoreCase(SVGConstants.SVG_STOP_COLOR_ATTRIBUTE)) {
			return isDefaultPresentForStopColor(element);
		} else if (type
				.equalsIgnoreCase(SVGConstants.SVG_LIGHTING_COLOR_ATTRIBUTE)) {
			return isDefaultPresentForLigtningColor(element);
		} else if (type
				.equalsIgnoreCase(SVGConstants.SVG_FLOOD_COLOR_ATTRIBUTE)) {
			return isDefaultPresentForFloodColor(element);
		}

		return false;
	}

	private static boolean isDefaultPresentForFill(Element element) {
		if (element instanceof SVGPathElement) {
			return true;
		} else if (element instanceof SVGRectElement) {
			return true;
		} else if (element instanceof SVGRectElement) {
			return true;
		} else if (element instanceof SVGCircleElement) {
			return true;
		} else if (element instanceof SVGLineElement) {
			return true;
		} else if (element instanceof SVGEllipseElement) {
			return true;
		} else if (element instanceof SVGPolylineElement) {
			return true;
		} else if (element instanceof SVGPolygonElement) {
			return true;
		} else if (element instanceof SVGTextElement) {
			return true;
		} else if (element instanceof SVGTRefElement) {
			return true;
		} else if (element instanceof SVGTextPathElement) {
			return true;
		} else if (element instanceof SVGTSpanElement) {
			return true;
		} else if (element instanceof SVGAltGlyphElement) {
			return true;
		} else if (element instanceof SVGClipPathElement) {
			return true;
		} else if (element instanceof SVGAnimateElement) {
			return true;
		} else if (element instanceof SVGAnimateColorElement) {
			return true;
		} else
			return false;
	}

	private static boolean isDefaultPresentForSTROKE(Element element) {
		if (element instanceof SVGPathElement) {
			return true;
		} else if (element instanceof SVGRectElement) {
			return true;
		} else if (element instanceof SVGRectElement) {
			return true;
		} else if (element instanceof SVGCircleElement) {
			return true;
		} else if (element instanceof SVGLineElement) {
			return true;
		} else if (element instanceof SVGEllipseElement) {
			return true;
		} else if (element instanceof SVGPolylineElement) {
			return true;
		} else if (element instanceof SVGPolygonElement) {
			return true;
		} else if (element instanceof SVGTextElement) {
			return true;
		} else if (element instanceof SVGTRefElement) {
			return true;
		} else if (element instanceof SVGTextPathElement) {
			return true;
		} else if (element instanceof SVGTSpanElement) {
			return true;
		} else if (element instanceof SVGAltGlyphElement) {
			return true;
		} else if (element instanceof SVGClipPathElement) {
			return true;
		}
		return false;
	}

	private static boolean isDefaultPresentForStopColor(Element element) {

		if (element instanceof SVGSVGElement) {
			return true;
		} else if (element instanceof SVGGElement) {
			return true;
		} else if (element instanceof SVGDefsElement) {
			return true;
		} else if (element instanceof SVGSymbolElement) {
			return true;
		} else if (element instanceof SVGUseElement) {
			return true;
		} else if (element instanceof SVGSwitchElement) {
			return true;
		} else if (element instanceof SVGMarkerElement) {
			return true;
		} else if (element instanceof SVGPatternElement) {
			return true;
		} else if (element instanceof SVGMaskElement) {
			return true;
		} else if (element instanceof SVGFilterElement) {
			return true;
		} else if (element instanceof SVGFEImageElement) {
			return true;
		} else if (element instanceof SVGAElement) {
			return true;
		} else if (element instanceof SVGFontElement) {
			return true;
		} else if (element instanceof SVGGlyphElement) {
			return true;
		} else if (element instanceof SVGMissingGlyphElement) {
			return true;
		} else if (element instanceof SVGForeignObjectElement) {
			return true;
		}
		return false;
	}

	private static boolean isDefaultPresentForFloodColor(Element element) {
		if (element instanceof SVGSVGElement) {
			return true;
		} else if (element instanceof SVGGElement) {
			return true;
		} else if (element instanceof SVGDefsElement) {
			return true;
		} else if (element instanceof SVGSymbolElement) {
			return true;
		} else if (element instanceof SVGUseElement) {
			return true;
		} else if (element instanceof SVGSwitchElement) {
			return true;
		} else if (element instanceof SVGMarkerElement) {
			return true;
		} else if (element instanceof SVGPatternElement) {
			return true;
		} else if (element instanceof SVGMaskElement) {
			return true;
		} else if (element instanceof SVGFilterElement) {
			return true;
		} else if (element instanceof SVGAElement) {
			return true;
		} else if (element instanceof SVGFontElement) {
			return true;
		} else if (element instanceof SVGGlyphElement) {
			return true;
		} else if (element instanceof SVGMissingGlyphElement) {
			return true;
		} else if (element instanceof SVGForeignObjectElement) {
			return true;
		} else if (element instanceof SVGFEFloodElement) {
			return true;
		}
		return false;
	}

	private static boolean isDefaultPresentForLigtningColor(Element element) {
		if (element instanceof SVGSVGElement) {
			return true;
		} else if (element instanceof SVGGElement) {
			return true;
		} else if (element instanceof SVGDefsElement) {
			return true;
		} else if (element instanceof SVGSymbolElement) {
			return true;
		} else if (element instanceof SVGUseElement) {
			return true;
		} else if (element instanceof SVGSwitchElement) {
			return true;
		} else if (element instanceof SVGMarkerElement) {
			return true;
		} else if (element instanceof SVGPatternElement) {
			return true;
		} else if (element instanceof SVGMaskElement) {
			return true;
		} else if (element instanceof SVGFilterElement) {
			return true;
		} else if (element instanceof SVGAElement) {
			return true;
		} else if (element instanceof SVGFontElement) {
			return true;
		} else if (element instanceof SVGGlyphElement) {
			return true;
		} else if (element instanceof SVGMissingGlyphElement) {
			return true;
		} else if (element instanceof SVGForeignObjectElement) {
			return true;
		} else if (element instanceof SVGFEDiffuseLightingElement) {
			return true;
		} else if (element instanceof SVGFESpecularLightingElement) {
			return true;
		}
		return false;

	}

	static class XmlNode {
		private String value;

		private String uri;

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public XmlNode(String value, String uri) {
			super();
			this.value = value;
			this.uri = uri;
		}

	}

	public static void inlineLinkedGradients(Document document)
			throws SvgException {
		ArrayList<SVGGradientElement> svgGradients = new ArrayList<SVGGradientElement>();
		NodeList linearList = document
				.getElementsByTagName(SVGConstants.SVG_LINEAR_GRADIENT_TAG);
		NodeList radialList = document
				.getElementsByTagName(SVGConstants.SVG_RADIAL_GRADIENT_TAG);
		for (int i = 0; i < linearList.getLength(); i++) {
			svgGradients.add((SVGGradientElement) linearList.item(i));
		}
		for (int i = 0; i < radialList.getLength(); i++) {
			svgGradients.add((SVGGradientElement) radialList.item(i));
		}
		for (SVGGradientElement element : svgGradients) {
			if (SVGDocumentUtilsHelper.isReferedSomeOtherGradients(element)) {
				SVGDocumentUtilsHelper.getReferedGradient(element);
			}
		}

	}

	public static SVGGradientElement getReferedGradient(
			SVGGradientElement element) throws SvgException {
		HashMap<String, XmlNode> map = new HashMap<String, XmlNode>();
		getAttributes(element, map);
		NodeList list = element.getChildNodes();
		boolean hasChildNodes = element.hasChildNodes();
		list = getReferedGradient1(element, map, list, hasChildNodes);
		Set<String> set = map.keySet();
		XmlNode node = null;
		for (String string : set) {
			node = map.get(string);
			element.setAttributeNS(node.getUri(), string, node.getValue());
		}
		int length = list.getLength();
		Node emt = null;
		if (!hasChildNodes) {
			for (int i = 0; i < length; i++) {
				if (list.item(i) instanceof SVGElement) {
					emt = ((SVGDocument) element.getOwnerDocument())
							.createElementNS(SVGConstants.SVG_NAMESPACE_URI,
									SVGConstants.SVG_STOP_TAG);
					copyAttributes((SVGStopElement) list.item(i), (Element) emt);
					element.appendChild(emt);
				}
			}

		}
		element.removeAttributeNS(XLINK_URI, SVGConstants.SVG_HREF_ATTRIBUTE);
		return element;
	}

	public static NodeList getReferedGradient1(SVGGradientElement element,
			HashMap<String, XmlNode> map, NodeList nodeList,
			boolean hasChildNodes) throws SvgException {
		String referedId = null;
		try {
			if (isReferedSomeOtherGradients(element)) {
				referedId = getReferedGradientID(element.getAttributeNS(
						XLINK_URI, "href"));
				element = (SVGGradientElement) element.getOwnerDocument()
						.getElementById(referedId);
				getAttributes(element, map);
				if (!hasChildNodes) {
					nodeList = element.getChildNodes();
					hasChildNodes = element.hasChildNodes();
				}
				if (isReferedSomeOtherGradients(element)) {
					getReferedGradient1(element, map, nodeList, hasChildNodes);
				}
			}

		} catch (DOMException e) {
			throw new SvgException(e.getMessage(), e.getCause());
		}
		return nodeList;

	}

}
