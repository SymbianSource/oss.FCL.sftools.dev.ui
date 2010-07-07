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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nokia.svg2svgt.SVG2SVGTConstants;
import com.nokia.svg2svgt.ServiceRegistry;
import com.nokia.svg2svgt.converter.ConversionConstants;
import com.nokia.svg2svgt.converter.Conversions;
import com.nokia.svg2svgt.converter.NameSpaceAnalyzer;
import com.nokia.svg2svgt.log.Logger;

public class LinearGradientPercentageConverter implements Conversions,
		ConversionConstants, SVG2SVGTConstants {
	/**
	 * Logger instance.
	 */
	private Logger myLogger = null;

	public void doConversion(Node svgNode, Document svgDoc, Node svgtNode,
			Document svgtDoc, Logger logger, String nsURL) throws DOMException {
		myLogger = logger;
		if (Node.ATTRIBUTE_NODE == svgNode.getNodeType()) {
			return;
		} else if (Node.ELEMENT_NODE == svgNode.getNodeType()) {
			Element svgtParentElement = (Element) svgtNode;
			String elemName = svgNode.getNodeName();
			String nsName = null;
			int index = elemName.indexOf(":");
			if (-1 != index) {
				nsName = elemName.substring(0, index - 1);
				elemName = elemName.substring(index + 1, elemName.length());
			}
			String attrName=null;
			String atNsName=null;
			// get all attribute values
			NamedNodeMap svgNodesAttr = svgNode.getAttributes();
			if (null != svgNodesAttr) {
				for (int i = 0; i < svgNodesAttr.getLength(); i++) {
					Attr attr = (Attr) svgNodesAttr.item(i);
					if ((attr.getName().endsWith(XML_TAG_X1))
							|| (attr.getName().endsWith(XML_TAG_X2))
							|| (attr.getName().endsWith(XML_TAG_Y1))
							|| (attr.getName().endsWith(XML_TAG_Y2))
							|| (attr.getName().endsWith(XML_TAG_OFFSET))) {
						String value = attr.getValue();
						if (-1 != value.indexOf(TAG_PERCENTAGE)) {
							value = value.substring(0, value
									.indexOf(TAG_PERCENTAGE));
							float intValue = Float.parseFloat(value);
							intValue = intValue / 100;
							attr.setValue(Float.toString(intValue));
						}
					}
						
					attrName=attr.getNodeName();
					index = attrName.indexOf(":");
					if (-1 != index) {
						atNsName = attrName.substring(0, index);
						if(atNsName!=null && !atNsName.equals("xlink")){
							if(!isAttrAllowed(atNsName, attr.getNamespaceURI(), attr.getLocalName())){
								if(svgNode instanceof Element){
									((Element)svgNode).removeAttributeNode(attr);
								}
								
							}
						}
					}
				}
			}
			NodeList childNodes = svgNode.getChildNodes();
			LinkedHashMap stopTable = new LinkedHashMap();
			int length = childNodes.getLength();
			if (length > 0) {
				for (int i = 0; i < length;) {
					// Element childNode = ( Element )childNodes.item( i );
					// Element childNode = ( Element )childNodes.item(i);
					Node childNode = (Node) childNodes.item(i);
					if (!Element.class.isInstance(childNode)) {
						i++;
						continue;
					}
					if (childNode.getNodeName().equalsIgnoreCase(XML_TAG_STOP)) {
						Attr attr = ((Element) childNode)
								.getAttributeNode(XML_TAG_OFFSET);
						String value = attr.getNodeValue();
						if (-1 != value.indexOf("%")) {
							value = value.substring(0, value.indexOf("%"));
							float floatValue = (float) (((Integer
									.parseInt(value)) * 1.0) / (100.0));
							value = Float.toString(floatValue);
							attr.setValue(value);
						}
						float floatValue = (float) (((Float.parseFloat(value))));
						value = Float.toString(floatValue);
						if (stopTable.containsKey(value)) {
							List list = (List) stopTable.get(value);
							list.add(childNode);
						} else {
							ArrayList list = new ArrayList();
							list.add(childNode);
							stopTable.put(value, list);
						}
						// stopTable.put( value , childNode );
						svgNode.removeChild(childNode);
						length--;
					} else if (-1 != childNode.getNodeName().indexOf(":")) {
						svgNode.removeChild(childNode);
						length--;
					}
				}
				Set keys = stopTable.keySet();
				Iterator enum1 = keys.iterator();
				float min = 1;
				float max = 0;
				while (enum1.hasNext()) {
					float recent = Float.parseFloat((String) enum1.next());
					if (recent < min)
						min = recent;
					if (recent > max)
						max = recent;
				}
				enum1 = keys.iterator();

				if (min != 0) {
					// Element node = ( Element )stopTable.get( Float.toString(
					// min ) );
					List list = (List) stopTable.get(Float.toString(min));
					for (int i = 0; i < list.size(); i++) {
						Element node = (Element) list.get(i);
						Element node1 = (Element) node.cloneNode(false);
						NamedNodeMap attrs = node1.getAttributes();
						for (int h = 0; h < attrs.getLength(); h++) {
							Attr attr = (Attr) attrs.item(h);
							if (attr.getName().equalsIgnoreCase(XML_TAG_OFFSET))
								attr.setValue(TAG_ZERO);
						}
						svgNode.appendChild(node1);
					}
				}

				while (enum1.hasNext()) {

					// Element elem = (Element)stopTable.get(enum.next());
					List list = (List) stopTable.get(enum1.next());
					for (int i = 0; i < list.size(); i++) {
						Element elem = (Element) list.get(i);
						NamedNodeMap attrsOfelem = elem.getAttributes();
						for (int k = 0; k < attrsOfelem.getLength(); k++) {
							Attr attrOfelem = (Attr) attrsOfelem.item(k);
						}
						svgNode.appendChild(elem);
					}
				}
				if (max != 1) {
					// Element node = ( Element )stopTable.get( Float.toString(
					// max ) );
					// Element node1 = ( Element )node.cloneNode( false );
					List list = (List) stopTable.get(Float.toString(max));
					for (int i = 0; i < list.size(); i++) {
						Element node = (Element) list.get(i);
						Element node1 = (Element) node.cloneNode(false);
						NamedNodeMap attrs = node1.getAttributes();
						for (int h = 0; h < attrs.getLength(); h++) {
							Attr attr = (Attr) attrs.item(h);
							if (attr.getName().equalsIgnoreCase(XML_TAG_OFFSET))
								attr.setValue(TAG_ONE);
						}
						svgNode.appendChild(node1);
					}
				}
			} else {
				
				logWarning(SVG2SVGTConstants.MISSING_STOP_ELEMENTS, null);
			}
			if (true == retainNode(nsName, nsURL, svgNode.getNodeName())) {
				Node sampleNode = svgtDoc.importNode(svgNode, true);
				svgtParentElement.appendChild(sampleNode);
			}
		}

	}

	// PRIVATE METHODS

	/**
	 * Checks if the converted not has to be retained in the output SVGT
	 * document or not.
	 * 
	 * @param node
	 *            Node to be checked.
	 * @return True if node is to be retained, else false.
	 */
	private boolean retainNode(String nsName, String nsURL, String nodeName) {
		if (true == isTagAllowed(nsName, nsURL, nodeName)) {
			// logEvent( SVG2SVGTConstants.BLACK_TAG_REMOVED,
			// new String[]{ nodeName } );
			isWarningRequired(nsName, nsURL, nodeName);
			return true;
		}

		return true;
	}

	/**
	 * Checks if this node is present in black list or not.
	 * 
	 * @param node
	 *            Node to be searched.
	 * @return True if found, else false.
	 */
	private boolean isTagAllowed(String nsName, String nsURL, String nodeName) {
		NameSpaceAnalyzer nameSpaceA = (NameSpaceAnalyzer) ServiceRegistry
				.getService("com.nokia.svg2svgt.converter.NameSpaceAnalyzer");
		if (null == nameSpaceA) {
			return false;
		}

		return nameSpaceA.isNodeAllowed(nsName, nsURL, nodeName,
				Node.ELEMENT_NODE);
	}

	/**
	 * Checks if this node is present in grey list or not.
	 * 
	 * @param node
	 *            Node to be searched.
	 * @return True if found, else false.
	 */

	private boolean isWarningRequired(String nsName, String nsURL,
			String nodeName) {
		NameSpaceAnalyzer nameSpaceA = (NameSpaceAnalyzer) ServiceRegistry
				.getService("com.nokia.svg2svgt.converter.NameSpaceAnalyzer");
		if (null == nameSpaceA) {
			return false;
		}
		if (true == nameSpaceA.isWarningTag(nsName, nsURL, nodeName,
				Node.ATTRIBUTE_NODE)) {
			logWarning(SVG2SVGTConstants.GREY_TAG_FOUND,
					new String[] { nodeName });
		}
		return true;
	}

	/**
	 * Logs an log event.
	 * 
	 * @param errorCode
	 *            Message code for the log message.
	 * @param params
	 *            Parameters for the message.
	 */
	private void logEvent(long msgCode, Object[] params) {
		if (null != myLogger) {
			myLogger.logEvent(msgCode, params);
		}
	}

	/**
	 * Logs a warning.
	 * 
	 * @param msgCode
	 *            Message code for the log message.
	 * @param params
	 *            Parameters for the message.
	 */
	private void logWarning(long msgCode, Object[] params) {
		if (null != myLogger) {
			myLogger.logEvent(msgCode, params);
		}
	}
	/**
	 * 
	 * @param nsName
	 * @param nsURL
	 * @param nodeName
	 * @return
	 */
	private boolean isAttrAllowed(String nsName, String nsURL, String nodeName) {
		NameSpaceAnalyzer nameSpaceA = (NameSpaceAnalyzer) ServiceRegistry
				.getService("com.nokia.svg2svgt.converter.NameSpaceAnalyzer");
		if (null == nameSpaceA) {
			return false;
		}
		
		System.out.println("Node allowd :"+nodeName);

		return nameSpaceA.isNodeAllowed(nsName, nsURL, nodeName,
				Node.ATTRIBUTE_NODE);
	}
}
