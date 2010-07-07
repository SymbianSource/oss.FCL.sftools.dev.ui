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
/*
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.nokia.tools.theme.s60.effects;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.nokia.tools.platform.theme.EffectConstants;
import com.nokia.tools.resource.util.DebugHelper;
import com.nokia.tools.theme.s60.general.ThemeAppBundle;

// import Layer.Layer.Effect;

/**
 * To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EffectsParser {
	private EffectsParser() {
	}

	public static void init() {
		String path = ThemeAppBundle.getPropBundle().getString("EFFECTS_DIR");

		File f = new File(path);
		if (!f.exists())
			return;

		String[] fileList = f.list();
		for (int i = 0; i < fileList.length; i++) {
			EffectObject effect = null;
			String fileName = fileList[i];
			if (!(fileName.substring(fileName.lastIndexOf('.') + 1, fileName
					.length()).equalsIgnoreCase("xml")))
				continue;
			File file = new File(fileName);
			try {
				Document doc = parseFile(f.getAbsolutePath() + File.separator
						+ file.getName());
				Element root = doc.getDocumentElement();
				if (root.hasChildNodes()) {
					NodeList childList = root.getChildNodes();
					for (int j = 0; j < childList.getLength(); j++) {
						Node n = childList.item(j);
						if ((n instanceof Text) || (n instanceof Comment))
							continue;
						if (n.getNodeName().equalsIgnoreCase(
								EffectConstants.TAG_VALUEMODEL)) {
							if (effect != null) {
								NamedNodeMap nmap = n.getAttributes();
								for (int k = 0; k < nmap.getLength(); k++) {
									Node value = nmap.item(k);
									if (value.getNodeName().equalsIgnoreCase(
											EffectConstants.ATTR_PREFERRED))
										effect.setPrefferedValueModel(value
												.getNodeValue());
									else if (value
											.getNodeName()
											.equalsIgnoreCase(
													EffectConstants.ATTR_ALLOWED)) {
										
										effect
												.setValueModelMap(createAllowedList(value
														.getNodeValue()));
									}
								}
							}

						} else {
							if (effect == null)
								effect = new EffectObject(n);
							if (n.getNodeName().equalsIgnoreCase(
									EffectConstants.TAG_USAGE)) {
								NodeList nList = n.getChildNodes();
								if (nList == null || nList.getLength() <= 0) {
									continue;
								}
								for (int k = 0; k < nList.getLength(); k++) {
									Node node1 = nList.item(k);
									if (node1.getNodeName().equalsIgnoreCase(
											EffectConstants.TAG_USAGE_ALLOWED)) {
										NamedNodeMap allowedEntitiesMap = node1
												.getAttributes();
										Node entityTypeNode = allowedEntitiesMap
												.getNamedItem(EffectConstants.ATTR_ENTITY_TYPE);
										ArrayList<String> allowedEntitiesList = getTokenizedValues(entityTypeNode
												.getNodeValue());
										effect
												.setAllowedEntitiesList(allowedEntitiesList);
									} else if (node1
											.getNodeName()
											.equalsIgnoreCase(
													EffectConstants.TAG_USAGE_RESTRICTED)) {
										NamedNodeMap restrictedEntitiesMap = node1
												.getAttributes();
										Node entityTypeNode = restrictedEntitiesMap
												.getNamedItem(EffectConstants.ATTR_ENTITY_TYPE);
										ArrayList<String> restrictedEntitiesList = getTokenizedValues(entityTypeNode
												.getNodeValue());
										effect
												.setRestrictedEntitiesList(restrictedEntitiesList);
									}
								}
								effect = null;
							}
						}
					}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (FactoryConfigurationError e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
	}

	public static synchronized void release() {
		EffectObject.release();
	}

	private static HashMap<String, String> createAllowedList(String value) {
		StringTokenizer tokenizer = new StringTokenizer(value, ",");
		HashMap<String, String> map = new HashMap<String, String>();
		while (tokenizer.hasMoreTokens()) {
			map.put(tokenizer.nextToken().trim(), null);
		}
		return map;
	}

	private static ArrayList<String> getTokenizedValues(String value) {
		StringTokenizer tokenizer = new StringTokenizer(value, ",");
		ArrayList<String> list = new ArrayList<String>();

		while (tokenizer.hasMoreTokens()) {
			list.add(tokenizer.nextToken().trim());
		}

		return list;

	}

	public static Document parseFile(String fileName)
			throws FactoryConfigurationError, ParserConfigurationException,
			IOException, SAXException, IllegalArgumentException

	{
		DocumentBuilderFactory docBuilderF = DocumentBuilderFactory
				.newInstance();
		docBuilderF.setIgnoringElementContentWhitespace(true);
		DocumentBuilder docBuilder = docBuilderF.newDocumentBuilder();

		File inFile = new File(fileName);
		if (DebugHelper.debugParser()) {
			DebugHelper.debug(EffectsParser.class, "parsing " + inFile);
		}
		Document parsedDoc = docBuilder.parse(inFile);

		return parsedDoc;
	}
}