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
 * To change the template for this generated file
 * go to Window - Preferences - Java - Code Style - Code Templates
 */
package com.nokia.tools.theme.s60.morphing;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.nokia.tools.theme.s60.general.ThemeAppBundle;

public class ValueModelXmlParser {

	private ValueModelXmlParser() {
	}

	public static synchronized void init() {
		String path = ThemeAppBundle.getPropBundle().getString(
				"VALUE_MODEL_DIR");
		String modelPath = ThemeAppBundle.getPropBundle().getString(
				"VALUEMODEL_XML_PATH");
		File f = new File(path);
		if (!f.exists())
			return;
		String[] fileList = f.list();
		try {
			for (int i = 0; i < fileList.length; i++) {
				String fileName = fileList[i];
				if (!(fileName.substring(fileName.lastIndexOf('.') + 1,
						fileName.length()).equalsIgnoreCase("xml")))
					continue;
				File file = new File(fileName);

				Document doc = parseFile(f.getAbsolutePath() + File.separator
						+ file.getName());
				Element root = doc.getDocumentElement();
				if (root.hasChildNodes()) {
					NodeList childList = root.getChildNodes();
					for (int j = 0; j < childList.getLength(); j++) {
						Node n = childList.item(j);
						if ((n instanceof Text) || (n instanceof Comment))
							continue;
						AnimationFactory.createValueModelObjects(n);
					}
				}
			}
			Document doc1 = parseFile(modelPath);
			AnimationFactory.createModelMap(doc1.getDocumentElement());
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

	public static synchronized void release() {
		AnimationFactory.release();
	}

	public static Document parseFile(String fileName)
			throws ParserConfigurationException, IOException, SAXException {

		
		DocumentBuilderFactory docBuilderF = DocumentBuilderFactory
				.newInstance();
		docBuilderF.setIgnoringElementContentWhitespace(true);
		DocumentBuilder docBuilder = docBuilderF.newDocumentBuilder();

		File inFile = new File((String) fileName);
		Document parsedDoc = docBuilder.parse(inFile);

		return parsedDoc;
	}
}
