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

package com.nokia.tools.resource.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class provides a collection of convenient methods for handling XML
 * documents.
 */
public class XmlUtil {
	/**
	 * Default encoding - UTF-8
	 */
	public static final String DEFAULT_ENCODING = "UTF-8";

	/**
	 * No instantiation.
	 */
	private XmlUtil() {
	}

	/**
	 * Serializes the given xml document to the file.
	 * 
	 * @param doc the xml document to serialize.
	 * @param file the destination file.
	 * @throws Exception if any I/O or xml related exceptions occurred during
	 *             serialization.
	 */
	public static void write(Document doc, File file) throws Exception {
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			write(doc, out);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Serializes the given xml document to the file with the specific options.
	 * 
	 * @param doc the xml document to serialize.
	 * @param file the destination file.
	 * @param props the serialization properties.
	 * @throws Exception if any I/O or xml related exceptions occurred during
	 *             serialization.
	 */
	public static void write(Document doc, File file, Map<String, String> props)
	    throws Exception {
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			write(doc, out, props);
		} finally {
			FileUtils.close(out);
		}
	}

	/**
	 * Serializes the given xml document to the specific output stream.
	 * 
	 * @param doc the xml document to serialize.
	 * @param out the output stream.
	 * @throws Exception if any I/O or xml related exceptions occurred during
	 *             serialization.
	 */
	public static void write(Document doc, OutputStream out) throws Exception {
		write(doc, out, null);
	}

	/**
	 * Serializes the given xml document to the specific output stream.
	 * 
	 * @param doc the xml document to serialize.
	 * @param out the output stream.
	 * @param props the serialization properties.
	 * @throws Exception if any I/O or xml related exceptions occurred during
	 *             serialization.
	 */
	public static void write(Document doc, OutputStream out,
	    Map<String, String> props) throws Exception {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(out,
		    DEFAULT_ENCODING));
		// Prepare the output file
		StreamResult result = new StreamResult(writer);

		// Write the DOM document to the file
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setAttribute("indent-number", 4);
		Transformer xformer = factory.newTransformer();
		xformer.setOutputProperty(OutputKeys.INDENT, "yes");
		xformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		// overwrites the properties in the parameter
		if (props != null) {
			for (String name : props.keySet()) {
				String value = props.get(name);
				if (value != null) {
					xformer.setOutputProperty(name, value);
				}
			}
		}
		xformer.transform(new DOMSource(doc), result);
	}

	/**
	 * Creates a new empty XML document.
	 * 
	 * @return the newly created document.
	 * @throws Exception if document creation failed.
	 */
	public static Document newDocument() throws Exception {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder()
		    .newDocument();
	}

	/**
	 * Fetches the attributes associated with the given node
	 * 
	 * @param element The element whose attributes need to be fetched
	 * @return A map containing all the attributes
	 */
	public static Map<String, String> getAttributes(Node element) {
		NamedNodeMap attributes = element.getAttributes();
		if (attributes == null) {
			return Collections.EMPTY_MAP;
		}
		int len = attributes.getLength();
		Map<String, String> map = new HashMap<String, String>(len);

		for (int i = 0; i < len; i++) {
			Node n = attributes.item(i);
			map.put(n.getNodeName(), n.getNodeValue());
		}

		return map;
	}

	/**
	 * Removes empty lines from the xml document.
	 * 
	 * @param xml the xml document.
	 */
	public static void removeEmptyLines(Document xml) {
		/* remove white spaces from document */
		Element root = xml.getDocumentElement();
		Stack<Element> nds = new Stack<Element>();
		nds.push(root);
		while (!nds.isEmpty()) {
			Element el = nds.pop();
			org.w3c.dom.NodeList childs = el.getChildNodes();
			int c = 0;
			while (c < childs.getLength()) {
				Node node = childs.item(c);
				if (node instanceof Element)
					nds.push((Element) node);
				else if (node instanceof Text) {
					Text t = (Text) node;
					if (t.getTextContent().trim().length() == 0) {
						// if this text node contains more than one line-break,
						// replace with one line break
						String content = t.getTextContent();
						Node nextSibling = t.getNextSibling();
						if (content.indexOf("\n") != -1) {
							t.setTextContent(nodeLineBreak(nextSibling) ? ""
							    : "\n");
						}
					}
				}
				c++;
			}
		}
	}

	/**
	 * Checks if the node content is just a line break.
	 * 
	 * @param n the node to test.
	 * @return true if the node's content is purely line break, false otherwise.
	 */
	private static boolean nodeLineBreak(Node n) {
		if (n instanceof Text) {
			String s = ((Text) n).getTextContent();
			return s.trim().length() == 0 && s.indexOf("\n") != -1;
		}
		return false;
	}

	/**
	 * Simple parser without any validation.
	 * 
	 * @param in the input stream to read xml content.
	 * @return the DOM document.
	 * @throws Exception if any error occurred in the parsing.
	 */
	public static Document parse(InputStream in) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringElementContentWhitespace(true);
		factory.setCoalescing(true);
		factory.setNamespaceAware(true);
		factory.setIgnoringComments(true);
		factory.setExpandEntityReferences(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setEntityResolver(new EntityResolver() {

			/*
			 * (non-Javadoc)
			 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
			 * java.lang.String)
			 */
			public InputSource resolveEntity(String publicId, String systemId)
			    throws SAXException, IOException {
				return new InputSource(new StringReader(""));
			}

		});
		return builder.parse(in);
	}

	/**
	 * Parses the given file to an xml document without validating.
	 * 
	 * @param file the file to be parsed.
	 * @return the parsed xml document.
	 * @throws Exception if any error occurred in the parsing.
	 */
	public static Document parse(File file) throws Exception {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			return parse(in);
		} finally {
			FileUtils.close(in);
		}
	}

	/**
	 * Parses the given url to an xml document without validating.
	 * 
	 * @param file the file to be parsed.
	 * @return the parsed xml document.
	 * @throws Exception if any error occurred in the parsing.
	 */
	public static Document parse(URL url) throws Exception {
		InputStream in = null;
		try {
			in = url.openStream();
			return parse(in);
		} finally {
			FileUtils.close(in);
		}
	}

	/**
	 * @param list - the input list (it wont be modified)
	 * @param type - the type to be ensured
	 * @param mandate - if this is true, then it will return an empty list if
	 *            there is a dirty element. else it will exclude the dirty
	 *            element and return the rest.
	 * @return This function strips out the non "type" objects and adds it to
	 *         the new List The formal parameter List is NOT modified
	 */
	public List convertToSpecifiedList(List list, Class type, boolean mandate) {
		List retList = new ArrayList();

		for (Object elem : list) {
			if (type.isAssignableFrom(elem.getClass())) {
				retList.add(elem);
			} else {
				if (mandate) {
					retList.clear();
					return retList;
				}
			}
		}
		return retList;
	}

}
