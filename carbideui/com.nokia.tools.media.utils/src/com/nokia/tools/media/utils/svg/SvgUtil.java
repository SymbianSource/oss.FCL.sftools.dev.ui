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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.css.dom.CSSOMComputedStyle;
import org.apache.batik.dom.svg.AbstractSVGPathSegList;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPathSeg;

import com.nokia.svg2svgt.converter.ConversionConstants;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.color.ValuedColors;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.XmlUtil;

/**
 * SVG util class . which contains utility function for pre post processing svg.
 *  
 */
public final class SvgUtil {
	private static final String ATTR_VIEWBOX = "viewBox";

	private static final String ATTR_VIEWBOX_VALUE = "0 0 416 416";

	private static final String ATTR_STYLE = "style";

	private static final String ATTR_FILL = "fill";

	private static final String NONE_VALUE = "none";

	private static Pattern PATTERN_ID = Pattern.compile("url[(]#(\\w+)[)]");

	/**
	 * @param doc
	 *            SVG document object
	 */
	public static void fixSvg(Document doc) {
		
		fixSvgViewBoxIssue(doc);
	}

	/**
	 * If viewBox attribute is not present , adding default one
	 * 
	 * @param doc
	 */
	private static void fixSvgViewBoxIssue(Document doc) {
		Element element = doc.getDocumentElement();
		boolean viewBoxPresent = true;
		if (!element.hasAttribute(ATTR_VIEWBOX)) {
			viewBoxPresent = false;
			String width = SvgUtil.getAttribute(element,
					SVGConstants.SVG_WIDTH_ATTRIBUTE);
			String height = SvgUtil.getAttribute(element,
					SVGConstants.SVG_HEIGHT_ATTRIBUTE);
			String viewBoxValue = ATTR_VIEWBOX_VALUE;
			if (!SvgUtil.isValueIsPercentage(width)
					&& !SvgUtil.isValueIsPercentage(height)) {
				width = removeQualifier(width, "px");
				height = removeQualifier(height, "px");
				viewBoxValue = "0 0 " + width + " " + height;
			}
			Attr attr = doc.createAttribute(ATTR_VIEWBOX);
			attr.setNodeValue(viewBoxValue);
			element.setAttributeNode(attr);
		}
	}

	private static String removeQualifier(String value, String pattern) {
		if (value == null) {
			return null;
		}
		value = value.toLowerCase();

		if (value.endsWith("px")) {
			value = value.substring(0, value.indexOf(pattern));
		}
		return value;
	}

	public static SVGDocument parseSvg(File file) throws Exception {
		return parseSvg(FileUtils.toURL(file).toURI().toString(),
				new FileInputStream(file));
	}

	public static SVGDocument parseSvg(InputStream in) throws Exception {
		return parseSvg(null, in);
	}

	public static SVGDocument parseSvg(String uri, InputStream in)
			throws Exception {
		String parser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
		return (SVGDocument) f.createDocument(uri, in);
	}

	/**
	 * Converts the document to SVGDocument, will do nothing if the document is
	 * already an SVGDocument
	 * 
	 * @param document
	 *            the document to be converted.
	 * @return the converted SVGDocument.
	 */
	public static SVGDocument convertToSVGDocument(Document document)
			throws Exception {
		if (document == null) {
			return null;
		}
		if (document instanceof SVGDocument) {
			return (SVGDocument) document;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XmlUtil.write(document, out);
		return parseSvg(new ByteArrayInputStream(out.toByteArray()));
	}

	/**
	 * Extracting style attributes to xml presentation attributes
	 * 
	 * @param element
	 */
	public static void extractStyleAttributes(Element element) {

		if (element.hasAttribute(ATTR_STYLE)) {

			String value = element.getAttribute(ATTR_STYLE);

			element.removeAttribute(ATTR_STYLE);
			Map<String, String> hashMap = getAttributesAsIndividualStyles(value);
			for (String key : hashMap.keySet()) {
				element.setAttribute(key, hashMap.get(key));
			}
		}
	}

	public static void deleteUnusedReferenceFromFillAttribute(Element element,
			List<String> listOfReferredIds) {
		String value = element.getAttribute(ATTR_FILL);
		if (isADeletedReference(value, listOfReferredIds)) {
			element.setAttribute(ATTR_FILL, NONE_VALUE);
		}
	}

	private static boolean isADeletedReference(String valueOfReference,
			List<String> listOfReferredIds) {
		Matcher matcher = PATTERN_ID.matcher(valueOfReference);
		boolean result = false;
		if (matcher.matches()) {
			// if matched id is not present in the current list of referred Ids
			result = !listOfReferredIds.contains(matcher.group(1));
		}
		return result;
	}

	// Getting the style attribute value as individual xml styles , return
	// hashmap as key/value
	private static HashMap<String, String> getAttributesAsIndividualStyles(
			String value) {
		HashMap<String, String> map = new HashMap<String, String>();
		Scanner scanner = new Scanner(value);
		scanner.useDelimiter(";");
		String tempString = null;
		int index = 0;
		while (scanner.hasNext()) {
			tempString = scanner.next();
			index = tempString.indexOf(':');
			if (index != -1) {
				map.put(tempString.substring(0, index).trim(), tempString
						.substring(index + 1, tempString.length()).trim());
			}
		}
		return map;
	}

	/**
	 * Converting named colors to corresponding hexacecimal string
	 * 
	 * @param element
	 * @param type
	 */
	public static void convertNamedColorsToHexString(Element element,
			String type) {
		if (element.hasAttribute(type)) {
			if (element.hasAttribute("style")) {
				extractStyleAttributes(element);
			}
			String value = element.getAttribute(type);
			if (value != null)
				value = value.trim();
			if (value != null && ValuedColors.isCssColorName(value)) {
				element.setAttribute(type, ValuedColors
						.getNamedColorValue(value));
			} else if (value != null && ColorUtil.isColor(value)) {
				element.setAttribute(type, ColorUtil.asHashString(ColorUtil
						.getRGB(value)));
			}
		}
	}

	/**
	 * Normalising the path element , converting to arc to curve etc
	 */
	public static String normalizePath(SVGOMPathElement element) {
		StringBuilder pathBuilder = new StringBuilder();
		NamedNodeMap map = element.getAttributes();
		String path = null;
		Node pathNode = null;
		for (int i = 0; i < map.getLength(); i++) {
			pathNode = map.item(i);
			if (pathNode != null
					&& pathNode.getNodeName().equalsIgnoreCase("d")) {
				path = pathNode.getNodeValue();
			} else {
				pathNode = null;
			}
		}
		if (path == null)
			return path;

		ExtSVGOMAnimatedPathData pathData = new ExtSVGOMAnimatedPathData(
				element, element.getNamespaceURI(), element.getLocalName(),
				path);
		AbstractSVGPathSegList pathSegmentList = (AbstractSVGPathSegList) pathData
				.getNormalizedPathSegList();
		short type = 0;
		SVGPathSeg pathSeg = null;
		for (int index = 0; index < pathSegmentList.getNumberOfItems(); index++) {
			pathSeg = pathSegmentList.getItem(index);
			type = pathSeg.getPathSegType();
			switch (type) {
			case SVGPathSeg.PATHSEG_ARC_ABS:
			case SVGPathSeg.PATHSEG_ARC_REL:
				break;
			case SVGPathSeg.PATHSEG_CLOSEPATH:
				pathBuilder.append(pathSeg.getPathSegTypeAsLetter());
				pathBuilder
						.append(AbstractSVGPathSegList.SVG_PATHSEG_LIST_SEPARATOR);
				break;
			case SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS:
			case SVGPathSeg.PATHSEG_CURVETO_CUBIC_REL:
				pathBuilder
						.append(pathSegmentList.new SVGPathSegCurvetoCubicItem(
								pathSeg).getValueAsString());
				pathBuilder
						.append(AbstractSVGPathSegList.SVG_PATHSEG_LIST_SEPARATOR);
				break;
			case SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_ABS:
			case SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_REL:
				pathBuilder
						.append(pathSegmentList.new SVGPathSegCurvetoCubicSmoothItem(
								pathSeg).getValueAsString());
				pathBuilder
						.append(AbstractSVGPathSegList.SVG_PATHSEG_LIST_SEPARATOR);
				break;
			case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_ABS:
			case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_REL:
				pathBuilder
						.append(pathSegmentList.new SVGPathSegCurvetoQuadraticItem(
								pathSeg).getValueAsString());
				pathBuilder
						.append(AbstractSVGPathSegList.SVG_PATHSEG_LIST_SEPARATOR);
				break;
			case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_ABS:
			case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_REL:
				pathBuilder
						.append(pathSegmentList.new SVGPathSegCurvetoQuadraticSmoothItem(
								pathSeg).getValueAsString());
				pathBuilder
						.append(AbstractSVGPathSegList.SVG_PATHSEG_LIST_SEPARATOR);
				break;
			case SVGPathSeg.PATHSEG_LINETO_ABS:
			case SVGPathSeg.PATHSEG_LINETO_REL:
			case SVGPathSeg.PATHSEG_MOVETO_ABS:
			case SVGPathSeg.PATHSEG_MOVETO_REL:
				pathBuilder
						.append(pathSegmentList.new SVGPathSegMovetoLinetoItem(
								pathSeg).getValueAsString());
				pathBuilder
						.append(AbstractSVGPathSegList.SVG_PATHSEG_LIST_SEPARATOR);
				break;
			case SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_REL:
			case SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_ABS:
				pathBuilder
						.append(pathSegmentList.new SVGPathSegLinetoHorizontalItem(
								pathSeg).getValueAsString());
				pathBuilder
						.append(AbstractSVGPathSegList.SVG_PATHSEG_LIST_SEPARATOR);
				break;
			case SVGPathSeg.PATHSEG_LINETO_VERTICAL_REL:
			case SVGPathSeg.PATHSEG_LINETO_VERTICAL_ABS:
				pathBuilder
						.append(pathSegmentList.new SVGPathSegLinetoVerticalItem(
								pathSeg).getValueAsString());
				pathBuilder
						.append(AbstractSVGPathSegList.SVG_PATHSEG_LIST_SEPARATOR);
				break;
			default:
			}
		}
		return pathBuilder.toString();

	}

	/**
	 * When first create an SVG Document from Java it will only support the DOM
	 * Core calls (basically traversal of the tree and accessing attributes).
	 * the CSS and SVG specific DOM interfaces will not work properly until
	 * initialize them. Code to initialize the CSS and DOM interfaces
	 * 
	 * @param document
	 */
	public static void bootCSSandSVGDom(Document document) {
		UserAgent userAgent;
		DocumentLoader loader;
		BridgeContext ctx;
		GVTBuilder builder;
		userAgent = new UserAgentAdapter();
		loader = new DocumentLoader(userAgent);
		ctx = new BridgeContext(userAgent, loader);
		ctx.setDynamicState(BridgeContext.DYNAMIC);
		builder = new GVTBuilder();
		builder.build(ctx, document);
	}

	/**
	 * Getting the computed CSS style from Element .
	 * 
	 * @param element
	 * @return
	 */
	public static CSSOMComputedStyle getComputedStyle(Element element) {
		ViewCSS viewCSS = (ViewCSS) element.getOwnerDocument()
				.getDocumentElement();
		CSSOMComputedStyle computedStyle = (CSSOMComputedStyle) viewCSS
				.getComputedStyle(element, null);
		return computedStyle;
	}

	/**
	 * Inlining the gradients, removing the xlink:href references
	 * 
	 * @param document
	 * @throws SvgException
	 */
	public static void inlineLinkedGradients(Document document)
			throws SvgException {
		SVGDocumentUtilsHelper.inlineLinkedGradients(document);
	}

	/**
	 * Updating width and height of the svg element to the real width and height
	 * from viewbox
	 * 
	 * @param document
	 */
	public static void convertWidthHeightInPcToPxFrmViewBox(Document document) {
		Element element = document.getDocumentElement();
		if (element.hasAttribute(ATTR_VIEWBOX)) {
			Attr attr = element.getAttributeNode(ATTR_VIEWBOX);
			if (attr == null)
				return;
			String nodeValue = attr.getNodeValue();
			if (nodeValue == null)
				return;
			String[] viewBoxValues = nodeValue.trim().split("\\s*,\\s*|\\s+|,");
			if (viewBoxValues != null && viewBoxValues.length == 4) {
				element.setAttribute(SVGConstants.SVG_WIDTH_ATTRIBUTE,
						viewBoxValues[2]);
				element.setAttribute(SVGConstants.SVG_HEIGHT_ATTRIBUTE,
						viewBoxValues[3]);

			}
		}
	}

	public static String getAttribute(Element element, String type) {
		if (element == null)
			return null;
		if (element.hasAttribute(type)) {
			return element.getAttribute(type);
		}
		return null;
	}

	public static boolean isValueIsPercentage(String value) {
		if (value != null) {
			if (value.indexOf("%") != -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If baseProfile is tiny , the
	 * illustrator convertts the gradients to rasterized format causing image
	 * data loss and distortion . The profile is set to default
	 * 
	 * @param document
	 */
	public static void fixDocTypeProblem(Document document) {
		Element element = document.getDocumentElement();
		// Attr
		// xmlNameSpace=element.getAttributeNodeNS("http://www.w3.org/2000/xmlns/",
		// ConversionConstants.XML_NAMESPACE_ID);
		Attr baseProfile = element
				.getAttributeNode(ConversionConstants.BASE_PROFILE_ATTRIBUTE);
		if (baseProfile != null
				&& ConversionConstants.BASE_PROFILE_TINY.equals(baseProfile
						.getNodeValue())) {
			element.removeAttribute(ConversionConstants.BASE_PROFILE_ATTRIBUTE);
		}
	}

	/**
	 * Parsing the view box value to get the individual values
	 * 
	 * @param viewBox
	 * @return
	 */
	public static String[] parseViewBox(String viewBox) {
		if (viewBox == null)
			return new String[0];
		return viewBox.trim().split("\\s*,\\s*|\\s+|,");
	}

	public static double getDouble(String value) {
		if (value == null)
			return 0;

		value = value.trim();
		return Double.parseDouble(value);
	}

}
