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
package com.nokia.tools.media.utils.svg2svgt;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.util.SVGConstants;
import org.eclipse.swt.printing.PrintDialog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.nokia.svg2svgt.converter.NameSpaceRemover;
import com.nokia.svg2svgt.converter.NonReferredIdRemover;
import com.nokia.svg2svgt.converter.SVG2SVGTConverter;
import com.nokia.svg2svgt.util.PrintDOMTree;
import com.nokia.tools.media.utils.svg.SvgUtil;
import com.nokia.tools.resource.util.XmlUtil;

/**
 * Controller class , which do the conversion from SVG to SVGT
 */
public class SVGTController {
	private static final String TAG_TEXT = "text";

	private static final String TAG_TSPAN = "tspan";

	private static final String ATTR_ID = "id";

	private static final String ATTR_FILL = "fill";

	private Document doc;

	private Hashtable opts;

	public SVGTController(Document doc, Hashtable opts) {
		this.doc = doc;
		this.opts = opts;
	}

	/**
	 * Function do the conversion , returns SVG file as ByteArrayOutputStream
	 * 
	 * @throws SVGUtilException
	 */
	public Document doConvert() throws Exception {
		// Replacing style attributes with corresponding xml attributes
		process(doc);
		Element element = doc.getDocumentElement();
		String width = SvgUtil.getAttribute(element,
				SVGConstants.SVG_WIDTH_ATTRIBUTE);
		String height = SvgUtil.getAttribute(element,
				SVGConstants.SVG_HEIGHT_ATTRIBUTE);
		boolean keepWidthHeight = false;

		if (!SvgUtil.isValueIsPercentage(width)
				&& !SvgUtil.isValueIsPercentage(height)) {
			keepWidthHeight = true;
		}

		// converting svg to svgt
		SVG2SVGTConverter converter = new SVG2SVGTConverter(opts);
		Document svgDocument = XmlUtil.newDocument();
		convertSVGToSVGT(converter, svgDocument);
		svgDocument = NameSpaceRemover.removeSVGNameSpaces(svgDocument);

		// remove non-referenced identifiers
		NonReferredIdRemover.processNonReferencedIDs(svgDocument, converter);

		// merge similar gradient paint definitions
		// commented because same gradient definitions may have different ids
		// and if removed, the referencing part will fail
		// GradientDefinitionMerger.mergeGradientDefinitions(dst, converter);

		// remove any empty nodes if present
		// check for the empty SVGT document
		removeEmptyNodes(converter, svgDocument);
		if (keepWidthHeight) {
			element = svgDocument.getDocumentElement();
			element.setAttribute(SVGConstants.SVG_WIDTH_ATTRIBUTE, width);
			element.setAttribute(SVGConstants.SVG_HEIGHT_ATTRIBUTE, height);
		}
		SvgUtil.fixSvg(svgDocument);

		if (!keepWidthHeight) {
			// updating the width and height , values from viewBox
			SvgUtil.convertWidthHeightInPcToPxFrmViewBox(svgDocument);
		}

		fixReferencesToDeletedIdentifiers(svgDocument);
		// Converting Document to SVGDocument
		svgDocument = SvgUtil.convertToSVGDocument(svgDocument);
		// Normalizing Path Attribute
		normalizePaths(svgDocument);

		// inling linked gradients
		SvgUtil.inlineLinkedGradients(svgDocument);

		return svgDocument;
	}

	private void fixReferencesToDeletedIdentifiers(Document svgDocument) {
		List<String> listOfIds = new ArrayList<String>();
		// create visitors to visit the svg document and perform the task of
		// collecting and fixing id references
		visitSVG(svgDocument, new ReferenceCollector(listOfIds));
		visitSVG(svgDocument, new ReferenceFixer(listOfIds));
	}

	private void removeEmptyNodes(SVG2SVGTConverter converter, Document document)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		Method method;
		if (null != document.getDocumentElement()) {
			method = SVG2SVGTConverter.class.getDeclaredMethod(
					"removeEmptyNodes", new Class[] { Node.class });
			method.setAccessible(true);
			method.invoke(converter, document.getDocumentElement());
		}
	}

	private void convertSVGToSVGT(SVG2SVGTConverter converter, Document dst)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		Method method = SVG2SVGTConverter.class.getDeclaredMethod(
				"convertSVGToSVGT", new Class[] { Document.class,
						Document.class });
		method.setAccessible(true);
		method.invoke(converter, new Object[] { doc, dst });
	}

	private interface SVGVisitor {

		void visitElement(Element element);

	}

	private class ReferenceCollector implements SVGVisitor {

		private final List<String> listOfIds;

		public ReferenceCollector(List<String> listOfIds) {
			this.listOfIds = listOfIds;
		}

		public void visitElement(Element element) {
			if (element.hasAttribute(ATTR_ID)) {
				listOfIds.add(element.getAttribute(ATTR_ID));
			}
		}

	}

	private class ReferenceFixer implements SVGVisitor {

		private final List<String> listOfReferredIds;

		public ReferenceFixer(List<String> listOfReferredIds) {
			this.listOfReferredIds = listOfReferredIds;

		}

		public void visitElement(Element element) {
			fixFillAttribute(listOfReferredIds, element);
		}

	}

	private void visitSVG(Node node, SVGVisitor svgVisitor) {
		if (node instanceof Element) {
			Element element = (Element) node;
			svgVisitor.visitElement(element);
		}
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			visitSVG(list.item(i), svgVisitor);
		}
	}

	private void fixFillAttribute(List<String> listOfReferredIds,
			Element element) {
		if (element.hasAttribute(ATTR_FILL)) {
			SvgUtil.deleteUnusedReferenceFromFillAttribute(element,
					listOfReferredIds);
		}
	}

	/**
	 * Replacing Style attributes with corresponding XML presentation attributes
	 * Converting style attributes to corresponding xml presentation attributes
	 * 
	 * @param node
	 *            Document object
	 * @throws IOException
	 */
	private void process(Node node) {
		if (node instanceof Element) {
			Element element = (Element) node;
			SvgUtil.extractStyleAttributes(element);
			convertNamedColorsToHexString(element);
			if (TAG_TSPAN.equals(element.getTagName())
					&& TAG_TEXT.equals(((Element) element.getParentNode())
							.getTagName())) {
				Node text = element.getFirstChild();
				if (text instanceof Text) {
					String content = text.getTextContent();
					element.getParentNode().setTextContent(content);
					element.setTextContent(null);
				}
			}
		}

		NodeList list = node.getChildNodes();
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			process(list.item(i));
		}
	}

	private void normalizePaths(Node node) throws IOException {
		if (node instanceof SVGOMPathElement) {
			SVGOMPathElement element = (SVGOMPathElement) node;
			String path = SvgUtil.normalizePath(element);
			if (path != null && element.hasAttribute("d")) {
				element.removeAttribute("d");
				element.setAttribute("d", path);
			}
		}

		NodeList list = node.getChildNodes();
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			normalizePaths(list.item(i));
		}
	}

	private void convertNamedColorsToHexString(Element element) {
		SvgUtil.convertNamedColorsToHexString(element,
				SVGConstants.SVG_FILL_ATTRIBUTE);
		SvgUtil.convertNamedColorsToHexString(element,
				SVGConstants.SVG_STROKE_ATTRIBUTE);
		SvgUtil.convertNamedColorsToHexString(element,
				SVGConstants.SVG_STOP_COLOR_ATTRIBUTE);
		SvgUtil.convertNamedColorsToHexString(element,
				SVGConstants.SVG_FLOOD_COLOR_ATTRIBUTE);
		SvgUtil.convertNamedColorsToHexString(element,
				SVGConstants.SVG_LIGHTING_COLOR_ATTRIBUTE);
	}

}
