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
 */
package com.nokia.tools.platform.layout;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.extension.ILayoutFontDescriptor;


class LayoutFontXmlData {
	private ILayoutFontDescriptor descriptor;
	private List<LayoutFontData> fonts = new ArrayList<LayoutFontData>();

	public LayoutFontXmlData(ILayoutFontDescriptor descriptor) {
		this.descriptor = descriptor;
		parseFonts();
	}

	/**
	 * Fetches the font type face based on the supplied hint (searches the
	 * properties file for a property value of the pattern "FONT_TYPE_<typeHint>"
	 * 
	 * @param typeHint The hint for finding the font type face (type hint will
	 *        be changed to upper case before searching for the result in the
	 *        properties file).If the hint is null then it will return the
	 *        default font face to be used.
	 * @return The string representing the font type face. Returns the default
	 *         font type face is no match is found using the type hint variable.
	 */
	public String getFontTypeFace(String typeHint) {
		Map<String, String> mappings = descriptor.getFontMappings();
		String mapped = mappings.get(typeHint);
		if (mapped == null) {
			return descriptor.getDefaultFont();
		}
		return mapped;
	}

	public int getBaseLineForFont(String fontName, String fontSize) {
		if (fonts.isEmpty()) {
			return -1;
		}

		int minVal = 0;
		int diff = 0;
		int baseLine = -1;
		int sizeVal = 0;
		// int val;
		for (int i = 0; i < fonts.size(); i++) {
			LayoutFontData font = fonts.get(i);
			if (font.getFontName().equals(fontName)) {
				sizeVal = Integer.parseInt(font.getFontSize());
				if (sizeVal == Integer.parseInt(fontSize)) {
					baseLine = Integer.parseInt(font.getBaseline());
					return baseLine;
				}
				// val = Integer.parseInt(font.getFontSize());
				diff = sizeVal - Integer.parseInt(fontSize);
				int signDiff = diff;
				diff = Math.abs(diff);
				if (i == 0) {
					minVal = diff;
				}
				if (diff < minVal) {
					minVal = diff;
					baseLine = Integer.parseInt(font.getBaseline());
				} else if (diff == minVal) {
					if (signDiff < 0) {
						minVal = diff;
						baseLine = Integer.parseInt(font.getBaseline());
					} else {
						baseLine = Integer.parseInt(font.getBaseline());
					}
				}
			}
		}
		return baseLine;
	}

	private void parseFonts() {
		URL fontPath = descriptor.getFontPath();
		if (fontPath == null) {
			return;
		}
		InputStream in = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			in = fontPath.openStream();
			Document document = builder.parse(in);
			NodeList nodeList = document
					.getElementsByTagName(LayoutConstants.XMLTAG_FONT);
			int length = nodeList.getLength();
			for (int i = 0; i < length; ++i) {
				Element element = (Element) nodeList.item(i);
				String name = (String) element
						.getAttribute(LayoutConstants.XML_FONT_ATTR_NAME);
				String fontSize = (String) element
						.getAttribute(LayoutConstants.XML_FONT_ATTR_SIZE);
				String baseLine = (String) element
						.getAttribute(LayoutConstants.XML_FONT_ATTR_BASELINE);
				String attrHint = (String) element
						.getAttribute(LayoutConstants.XML_FONT_ATTR_HINT);
				// System.out.println("AttrrHint" + attrHint);
				LayoutFontData fObj = new LayoutFontData(name, fontSize,
						baseLine, attrHint);
				fonts.add(fObj);
			}
		} catch (Throwable e) {
			PlatformCorePlugin.error(e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
			}
		}
	}
}
