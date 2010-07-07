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
package com.nokia.tools.platform.theme.preview;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeConstants;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.FileUtils;

/**
 * 		   To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ThemePreviewParser extends DefaultHandler {
	private static final String DTD_PATH = "/dtds/preview.dtd";

	// Holds DefaultPreview object
	private ThemePreview themePreview;

	// Holds an PreviewImage object
	private PreviewImage previewImage;

	// Holds PreviewElement object
	private PreviewElement previewElement;

	private PreviewRefer previewRefer;

	private HashMap<Object, Object> previewImages = new HashMap<Object, Object>();

	/**
	 * Holds the name and id of an Element node
	 */
	public ThemePreviewParser(Theme skinDetails, URL previewUrl)
			throws ThemeException {
		InputStream in = null;
		try {
			// Use the validating parser
			SAXParserFactory factory = SAXParserFactory.newInstance();

			factory.setValidating(true);

			// Parse the input
			SAXParser saxParser = factory.newSAXParser();

			this.themePreview = skinDetails.getThemePreview();

			if (this.themePreview == null) {
				this.themePreview = new ThemePreview(skinDetails);
				skinDetails.setThemePreview(this.themePreview);
			}

			in = new BufferedInputStream(previewUrl.openStream(),
					FileUtils.BUF_SIZE);

			saxParser.parse(in, this);

		} catch (Throwable spe) {
			PlatformCorePlugin.error(spe);
			throw new ThemeException(spe);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * SAX DocumentHandler method. This method identifies the start tag of an
	 * element and creates an object for it
	 */
	public void startElement(String namespaceURI, String lName, // local name
			String qName, // qualified name
			Attributes attrs) throws SAXException {

		String eName = lName; // element name

		if (eName.equals(""))
			eName = qName; // namespaceAware = false

		Map<Object, Object> mapAttr = new HashMap<Object, Object>();

		mapAttr = setAttributes(attrs);

		if (eName.equals(PreviewTagConstants.ELE_PREVIEW_IMAGE)) {
			String attrName = (String) mapAttr
					.get(PreviewTagConstants.ATTR_PREVIEW_NAME);

			previewImage = new PreviewImage(attrName);
			previewImage.setAttribute(mapAttr);
		}
		if (eName.equals(PreviewTagConstants.ELE_ELEMENT)) {

			previewElement = new PreviewElement();

			// String fileName = (String)
			// mapAttr.get(PreviewTagConstants.ATTR_ELEMENT_IMAGE);

			String text = (String) mapAttr
					.get(PreviewTagConstants.ATTR_ELEMENT_TEXT);

			String image = (String) mapAttr
					.get(PreviewTagConstants.ATTR_ELEMENT_IMAGE);

			if (image != null)
				previewImages.put(image, "0");

			String skinId = (String) mapAttr
					.get(PreviewTagConstants.ATTR_ELEMENT_ID);

			if (text != null && text.length() > 0) {
				mapAttr.put(ThemeConstants.ELEMENT_TYPE,
						ThemeConstants.ELEMENT_TYPE_TEXT + "");

			} else if (image != null && image.length() > 0) {
				mapAttr.put(ThemeConstants.ELEMENT_TYPE,
						ThemeConstants.ELEMENT_TYPE_IMAGEFILE + "");

			} else if (skinId != null && skinId.length() > 0) {
				
				mapAttr.put(ThemeConstants.ELEMENT_TYPE,
						ThemeConstants.ELEMENT_TYPE_GRAPHIC + "");

			} else {
				throw new SAXException(
						"Element has to have skinId/ image/ text");
			}

			previewElement.setAttribute(mapAttr);

		}
		if (eName.equals(PreviewTagConstants.ELE_REFERENCE)) {

			// String fileName = (String)
			// mapAttr.get(PreviewTagConstants.ATTR_ELEMENT_IMAGE);

			String referedScreen = (String) mapAttr
					.get(PreviewTagConstants.ATTR_REFER_SCREENNAME);

			if (referedScreen == null || referedScreen.trim().length() <= 0) {
				throw new SAXException("refer must specify a screenName");
			}
			previewRefer = new PreviewRefer();
			previewRefer.setAttribute(mapAttr);
		}
	}

	/**
	 * Method to get the id and name of an element
	 * 
	 * @param attr Map which holds the attributes of element
	 * @return String holds the name and id information of the element
	 */
	protected String findElementInfo(Map attr) {

		String name = (String) attr.get(PreviewTagConstants.ATTR_ELEMENT_NAME);

		// commented to handle when the element doesnt hav the name.
		/*
		 * if(name == null) { return null; }
		 */

		StringBuffer element = new StringBuffer();
		if (name != null) {
			element
					.append("<" + PreviewTagConstants.ATTR_ELEMENT_NAME + ">"
							+ name + "</"
							+ PreviewTagConstants.ATTR_ELEMENT_NAME + ">");
		}
		if (attr.containsKey(PreviewTagConstants.ATTR_ELEMENT_ID)) {
			element.append("<" + PreviewTagConstants.ATTR_ELEMENT_ID + ">"
					+ (String) attr.get(PreviewTagConstants.ATTR_ELEMENT_ID)
					+ "</" + PreviewTagConstants.ATTR_ELEMENT_ID + ">");

		}
		if (attr.containsKey(PreviewTagConstants.ATTR_ELEMENT_LOCID)) {
			element.append("<" + PreviewTagConstants.ATTR_ELEMENT_LOCID + ">"
					+ (String) attr.get(PreviewTagConstants.ATTR_ELEMENT_LOCID)
					+ "</" + PreviewTagConstants.ATTR_ELEMENT_LOCID + ">");
		} else {
			element.append("<" + PreviewTagConstants.ATTR_ELEMENT_LOCID + ">"
					+ ThemeTag.ATTR_LOC_ID_DEFAULT_VALUE + "</"
					+ PreviewTagConstants.ATTR_ELEMENT_LOCID + ">");
		}

		return element.toString();
	}

	/**
	 * Method to get the Attributes of an element in xml file and put in a Map
	 * 
	 * @param attrs Attributes of the element in xml file
	 * @return Map map which contains the attributes of an element in xml file
	 */
	protected Map<Object, Object> setAttributes(Attributes attrs) {

		Map<Object, Object> mapAttr = new HashMap<Object, Object>();

		if (attrs != null) {

			String aName = null;

			for (int i = 0; i < attrs.getLength(); i++) {
				aName = attrs.getLocalName(i); // Attr name
				if ("".equals(aName))
					aName = attrs.getQName(i);
				mapAttr.put(aName, (attrs.getValue(aName)).trim());
			}
		}

		return mapAttr;
	}

	/**
	 * SAX DocumentHandler method. This method identifies the end tag of an
	 * element and adds the corresponding object to its parent. If the end tag
	 * is for toolbox, it sets the toolbox to the corresponding object
	 */
	public void endElement(String namespaceURI, String sName, // simple name
			String qName // qualified name
	) throws SAXException {
		try {
			// if the closing tag is for element
			// add the element object to the list of elements
			if (qName.equals(PreviewTagConstants.ELE_ELEMENT)) {

				// add the element to the children list of the image in which it
				// is contained...
				previewImage.addChild(previewElement);
				previewElement = null;
			}
			if (qName.equals(PreviewTagConstants.ELE_REFERENCE)) {
				previewImage.addChild(previewRefer);
				previewRefer = null;
			}

			// if closing tag for image, add the previewImage object to the
			// children's list of the preview
			if (qName.equals(PreviewTagConstants.ELE_PREVIEW_IMAGE)) {
				// add the image object to the children's list of the preview
				themePreview.addChild(previewImage);
				previewImage = null;
			}
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
		}
	}

	// ===========================================================
	// SAX ErrorHandler methods
	// ===========================================================

	// treat validation errors as fatal
	public void error(SAXParseException e) throws SAXParseException {
		throw e;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws IOException, SAXException {
		return new InputSource(getClass().getClassLoader().getResourceAsStream(
				DTD_PATH));
	}
}
