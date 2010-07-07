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
 * Created on Dec 23, 2004 This class parses the IdMappings XML files and
 * maintains the datastructure for retrieving Skin-> Layout mappings. It
 * provides methods to perform parsing and for retrieving the layout mappings
 * given skin id. NOTE: Performing Skin mappings given layout id is postponed
 * until there is such a need
 */
package com.nokia.tools.platform.theme;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.layout.ComponentInfo;
import com.nokia.tools.resource.util.DebugHelper;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.StringUtils;

/**
 * 		   This class parses the IdMappings XML files and maintains the
 *         datastructure for retrieving Skin-> Layout mappings. It provides
 *         methods to perform parsing and for retrieving the layout mappings
 *         given skin id. NOTE: Performing Skin mappings given layout id is
 *         postponed until there is such a need
 */
public class IdMappingsHandler {
	private static final String DTD_PATH = "/dtds/idmapping.dtd";

	private Map<String, Map<String, Set<String>>> skinIdsMap = new HashMap<String, Map<String, Set<String>>>();
	// if map were changed until editing, this will contain true
	private boolean dirty = false;

	public IdMappingsHandler(URL[] urls) {
		parse(urls);
	}

	private void parse(URL[] urls) {
		for (URL url : urls) {
			new IdMappingsParser().doParsing(url);
		}
	}

	public ComponentInfo getComponentInfo(ThemeBasicData data, Display display,
			String skinId) {
		
		if (display == null) {
			// finds preferred display first
			Display displayRoot = data.getRoot().getDisplay();
			if (displayRoot != null) {
				ComponentInfo component = getComponentInfo(data, displayRoot, skinId);
				if (component != null) {
					return component;
				}
			}
		}
		Map<String, Set<String>> map = skinIdsMap.get(skinId.toLowerCase());
		if (map != null) {
			for (String value : map.keySet()) {
				if (Theme.supportsDisplay(value, display)) {
					Set<String> layouts = map.get(value);
					if (!layouts.isEmpty()) {
						String layout = layouts.iterator().next();
						DisplayComponentInfo component = getDetails(layout);
						component.setDisplay(display);
						return component;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * This method adds new element to map.
	 * Use this method if you're creating new Entity (Icon etc.) and
	 * you don't set the DerivedLayoutId attribute.
	 */
	public void addMapping(String skinId, String display, Set<String> layouts) {
		dirty = true;
		if (display == null) {
			display = "";
		}
		Map<String, Set<String>> subMap = null;
		if (skinIdsMap.containsKey(skinId)) {
			subMap = skinIdsMap.get(skinId);
		} else {
			subMap = new HashMap<String, Set<String>>(2);		
		}
		if (subMap.containsKey(display)) {
			Set<String> mapLayouts = subMap.get(display);
			for (String layout : layouts) {
				if (!mapLayouts.contains(layout)) {
					mapLayouts.add(layout);
				}
			}		
		} else {
			subMap.put(display, layouts);
		}			
	}
	
	/**
	 * This method adds new element to map.
	 * @see IdMappingsHandler#addMapping(String, String, Set)
	 */
	public void addMapping(String skinId, String display, String layout) {
		Set<String> layouts = new HashSet<String>(1);
		layouts.add(layout);
		addMapping(skinId, display, layouts);
	}
	
	/**
	 * Return the map of skin ids.
	 * @return
	 */
	public Map<String, Map<String, Set<String>>> getSkinIdsMap() {
		return skinIdsMap;
	}

	/**
	 * Returns true if map is dirty.
	 * Dirty means, that map were changed via methods of this class after
	 * it were loaded.
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * This class parses the IdMappingXXX.xml files and stores the layout
	 * mappings.
	 * 
	 * 			To change the template for this generated type comment go to
	 *          Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
	 *          Comments
	 */
	class IdMappingsParser extends DefaultHandler {
		String eleId;
		String valId;
		String display;
		Set<String> layoutValuesSet;
		Map<String, Set<String>> map;

		/**
		 * This methods parses the specified fileName and puts the data into the
		 * supplied idMap.
		 * 
		 * @param idMap HashMap for storing the parsed data.
		 * @param fileName Filename to be parsed
		 * @return
		 * @throws ThemeAppParserException
		 */
		public void doParsing(URL url) {

			InputStream in = null;
			try {
				DefaultHandler handler = this;

				// Use the validating parser
				SAXParserFactory factory = SAXParserFactory.newInstance();

				factory.setValidating(true);

				// Parse the input
				SAXParser saxParser = factory.newSAXParser();

				if (DebugHelper.debugParser()) {
					DebugHelper.debug(this, "parsing " + url);
				}
				in = new BufferedInputStream(url.openStream(),
						FileUtils.BUF_SIZE);
				saxParser.parse(in, handler);

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

		/**
		 * SAX DocumentHandler method. This method identifies the start tag of
		 * an element and creates an object for it
		 */
		public void startElement(String namespaceURI, String lName,
				String qName, Attributes attrs) throws SAXException {

			String eName = lName;

			if (eName.equals(""))
				eName = qName; // namespaceAware = false

			Map<String, String> mapAttr = new HashMap<String, String>();
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if ("".equals(aName))
						aName = attrs.getQName(i);
					mapAttr.put(aName.toLowerCase(), (attrs.getValue(aName))
							.trim());
				}
			}

			if (eName.equals(IdMappingsConstants.IDMAPPING_ROOT)) {
			} else if (eName.equals(IdMappingsConstants.IDMAPPING_LAYOUTENTITY)) {
				eleId = (String) mapAttr
						.get(IdMappingsConstants.IDMAPPING_LAYOUTENTITY_ATTR_NAME);
				eleId = eleId.toLowerCase();
				display = (String) mapAttr
						.get(IdMappingsConstants.IDMAPPING_LAYOUTENTITY_ATTR_DISPLAY);
				if (StringUtils.isEmpty(display)) {
					display = "";
				}
			} else if (eName.equals(IdMappingsConstants.IDMAPPING_SKINENTITY)) {
				eleId = (String) mapAttr
						.get(IdMappingsConstants.IDMAPPING_SKINENTITY_ATTR_ID);
				eleId = eleId.toLowerCase();
				layoutValuesSet = new LinkedHashSet<String>();
			} else if (eName.equals(IdMappingsConstants.IDMAPPING_LAYOUT)) {
				valId = (String) mapAttr
						.get(IdMappingsConstants.IDMAPPING_LAYOUTENTITY_ATTR_NAME);
				valId = valId.toLowerCase();
				display = mapAttr
						.get(IdMappingsConstants.IDMAPPING_LAYOUTENTITY_ATTR_DISPLAY);
				if (StringUtils.isEmpty(display)) {
					display = "";
				}
			} else if (eName.equals(IdMappingsConstants.IDMAPPING_SKIN)) {
				valId = (String) mapAttr
						.get(IdMappingsConstants.IDMAPPING_SKIN_ATTR_ID);
				valId = valId.toLowerCase();
			}
		}

		/**
		 * SAX DocumentHandler method. This method identifies the end tag of an
		 * element and adds the corresponding object to its parent. If the end
		 * tag is for toolbox, it sets the toolbox to the corresponding object
		 */
		public void endElement(String namespaceURI, String sName, String qName)
				throws SAXException {

			String eName = sName;

			if (eName.equals(""))
				eName = qName; // namespaceAware = false

			if (eName.equals(IdMappingsConstants.IDMAPPING_ROOT)) {
			} else if (eName.equals(IdMappingsConstants.IDMAPPING_LAYOUTENTITY)) {
			} else if (eName.equals(IdMappingsConstants.IDMAPPING_SKINENTITY)) {
			} else if (eName.equals(IdMappingsConstants.IDMAPPING_SKIN)) {
				add(valId, display, eleId);
			} else if (eName.equals(IdMappingsConstants.IDMAPPING_LAYOUT)) {
				add(eleId, display, valId);
			}
		}

		private void add(String elementId, String display, String layoutId) {
			Map<String, Set<String>> map = skinIdsMap.get(elementId);
			if (map == null) {
				map = new HashMap<String, Set<String>>();
				skinIdsMap.put(elementId, map);
			}
			Set<String> set = map.get(display);
			if (set == null) {
				set = new LinkedHashSet<String>();
				map.put(display, set);
			}
			set.add(layoutId);
		}

		// ===========================================================
		// SAX ErrorHandler methods
		// ===========================================================

		// treat validation errors as fatal
		public void error(SAXParseException e) throws SAXParseException {
			PlatformCorePlugin.error(e);
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
			return new InputSource(getClass().getResourceAsStream(DTD_PATH));
		}
	}

	public static DisplayComponentInfo getDetails(String str) {

		int delimiterIndex = str.indexOf("@v:");

		if (delimiterIndex != -1) {
			String name = str.substring(0, delimiterIndex);
			String withoutCompName = str.substring(delimiterIndex + 3);

			String variety;
			int hashIndex = withoutCompName.indexOf("#");
			if (hashIndex == -1) {
				variety = withoutCompName;
			} else {
				variety = withoutCompName.substring(0, hashIndex);
				PlatformCorePlugin
						.warn("IdMappings loc_id shall not contain the specific row/col number");
			}

			// loc_id shall not be set otherwise duplicate nodes occur
			return new DisplayComponentInfo(name, variety, null);
		}
		return null;
	}
}
