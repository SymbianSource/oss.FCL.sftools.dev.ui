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

package com.nokia.tools.theme.s60.model.tpi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.nokia.tools.platform.extension.IThemeDescriptor;
import com.nokia.tools.platform.extension.IThemeDesignDescriptor;
import com.nokia.tools.platform.theme.ElementCreator;
import com.nokia.tools.platform.theme.IThemeManager;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeDesignParser;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.XmlUtil;
import com.nokia.tools.theme.s60.IThemeConstants;
import com.nokia.tools.theme.s60.S60ThemePlugin;
import com.nokia.tools.theme.s60.model.S60Theme;
import com.nokia.tools.theme.s60.model.tpi.TPIconConflictEntry.TPIconConflitingField;
import com.sun.org.apache.xpath.internal.XPathAPI;

/**
 * This is a helper class that has been introduced to centralize the already
 * spread across implementation for the logic part [no UI] of the Third Party
 * Icon implementation. All the subsequent code related to third party icons
 * handling will be moved into this class so as to centralize the place where
 * the handling for the third party icons would be done.
 
 */
public class ThirdPartyIconManager {

	/** default preview screen for 3rd party icons */
	public static final String DEFAULT_TPI_PREVIEW_SCREEN = "Application Grid";

	/** icons will be derived from this element */
	public static final String DEFAULT_TPI_TEMPLATE_ELEMENT = "qgn_menu_appsgrid_cxt";

	public static final String DTD_RESOURCE = "design.dtd";
	

	
	/**
	 * The list of tool specific third party icons which are loaded on the load
	 * of this class.
	 */
	private static DefinedIcons toolSpecificThirdPartyIcons;
	
	static {
		try {
			getToolSpecificThirdPartyIcons(true);
		} catch (ThirdPartyIconLoadException e) {
			// Just ignore this exception.
		}
	}

	/**
	 * Loads and returns the current set of tool specific Third Party Icons. If
	 * it has been already loaded, it will return just the loaded value. In case
	 * it is to ensure that the latest from the icons.xml is to be reloaded for
	 * force. pass true to the reload parameter.
	 */

	public static DefinedIcons getToolSpecificThirdPartyIcons(boolean reload)
	    throws ThirdPartyIconLoadException {
		if (toolSpecificThirdPartyIcons == null || reload) {
			synchronized (ThirdPartyIconManager.class) {
				if (toolSpecificThirdPartyIcons == null || reload) {
					URL url = getToolSpecificThirdPartyIconUrl();
					if (url != null) {
						toolSpecificThirdPartyIcons = loadThirdPartyIcons(url, ThirdPartyIconType.TOOL_SPECIFIC);
						return toolSpecificThirdPartyIcons.clone();
					}
					else return new DefinedIcons();
				}
			}
		}
		return toolSpecificThirdPartyIcons.clone();
	}

	/**
	 * Returns the url of the icons.xml stored in the s60 plugins folder which
	 * contains the definition of the current set of TPI
	 * 
	 * @return - url of the icons.xml stored in the s60 plugins folder which
	 *         contains the definition of the current set of TPI
	 */
	public static URL getToolSpecificThirdPartyIconUrl() {
		for (IThemeDescriptor descriptor : ThemePlatform
		    .getThemeDescriptorsByContainer(IThemeConstants.THEME_CONTAINER_ID)) {
			for (IThemeDesignDescriptor desc : descriptor.getDesigns()) {
				if (desc.isCustomizable()) {
					return desc.getPath();
				}
			}
		}
		return null;
	}

	/**
	 * Loads the theme specific TPI into to the theme from the icons.xml file
	 * present in the theme and also returns the loaded TPIs.
	 * 
	 * @param theme - the S60Theme into which the Theme specific TPI needs to be
	 *            populated.
	 * @throws ThemeException
	 */
	public static void loadThemeSpecificIcons(S60Theme theme)
	    throws ThirdPartyIconLoadException {
		URL themeSpecificThirdPartyURL = theme.themeSpecificThirdPartyIconsUrl();
		try {
			if (themeSpecificThirdPartyURL != null) {
				ThirdPartyIconElementCreator elementCreator = 
					new ThirdPartyIconElementCreator(themeSpecificThirdPartyURL, ThirdPartyIconType.THEME_SPECIFIC, theme);
				DefinedIcons themeSpecificTPIs = elementCreator
				    .getCreatedThirdPartyIcons();
				ThemeDesignParser.parse(theme, themeSpecificThirdPartyURL,
				    elementCreator);
				theme.setThemeSpecificThirdPartyIcons(themeSpecificTPIs);
			}
			else{
				String themeSpecificThirdPartyIconsPath = theme.themeSpecificThirdPartyIconsPath();
				storeThirdParyIcons(new DefinedIcons(), new FileOutputStream(themeSpecificThirdPartyIconsPath));
			}
		} catch (ThemeException e) {
			throw new ThirdPartyIconLoadException(e);
		} catch (FileNotFoundException e) {
			throw new ThirdPartyIconLoadException(e);
		} catch (ThirdPartyIconStoreException e) {
			throw new ThirdPartyIconLoadException(e);
		}
	}

	/**
	 * Loads into the passed S60Theme the tool specific third party icons to be used for performing the skinning.
	 * @param theme - the S60Theme instance into which the Tool specific Third Party Icons 
	 * @throws ThirdPartyIconLoadException
	 */
	public static void loadToolSpecificThirdPartyIcons(S60Theme theme)
	    throws ThirdPartyIconLoadException {
		URL toolSpecificThirdPartyURL = getToolSpecificThirdPartyIconUrl();
		try {
			if (toolSpecificThirdPartyURL != null) {
				ThirdPartyIconElementCreator elementCreator = 
					new ThirdPartyIconElementCreator(toolSpecificThirdPartyURL, ThirdPartyIconType.TOOL_SPECIFIC, theme);
				ThemeDesignParser.parse(theme, toolSpecificThirdPartyURL, elementCreator);
			}
		} catch (ThemeException e) {
			throw new ThirdPartyIconLoadException(e);
		}
	}

	/**
	 * Loads and returns the list of Third Party Icons from the xml data present
	 * in the InputStream.
	 * 
	 * @param stream - the stream containing the xml data for the third part
	 *            icons
	 * @return - the list of Third Party Icons loaded from the xml data
	 */
	public static DefinedIcons loadThirdParyIcons(InputStream stream, ThirdPartyIconType thirdPartyIconType)
	    throws ThirdPartyIconLoadException {
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
			    .newDocumentBuilder();
			db.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId,
				    String systemId) throws SAXException, IOException {
					return new InputSource(IThemeDescriptor.class
					    .getResourceAsStream(IThemeManager.DTD_FOLDER
					        + new File(systemId).getName()));
				}
			});

			Document document = db.parse(stream);
			return loadThirdParyIcons(document, thirdPartyIconType);
		} catch (Exception e) {
			throw new ThirdPartyIconLoadException(e);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				// Simply ignoring any IOException that can be caused.
			}
		}
	}

	/**
	 * Loads and returns the list of Third Party Icons from the xml data present
	 * in the provided URL.
	 * 
	 * @param stream - the stream containing the xml data for the third party
	 *            icons
	 * @return - the list of Third Party Icons loaded from the xml data
	 */
	public static DefinedIcons loadThirdPartyIcons(URL url, ThirdPartyIconType thirdPartyIconType)
	    throws ThirdPartyIconLoadException {

		try {
			return loadThirdParyIcons(url.openStream(), thirdPartyIconType);
		} catch (IOException e) {
			throw new ThirdPartyIconLoadException(e);
		}
	}

	/**
	 * Loads and returns the list of Third Party Icons from the xml data present
	 * in the provided Docment.
	 * 
	 * @param stream - the document containing the DOM data for the third part
	 *            icons
	 * @return - the list of Third Party Icons loaded from the DOM data
	 */
	public static DefinedIcons loadThirdParyIcons(Document document, ThirdPartyIconType thirdPartyIconType)
	    throws ThirdPartyIconLoadException {
		try {
			NodeList nodeList = XPathAPI.selectNodeList(document
			    .getDocumentElement(), "//" + ThemeTag.ELEMENT_ELEMENT);
			DefinedIcons thirdPartyIcons = new DefinedIcons();

			for (int i = 0; i < nodeList.getLength(); i++) {
				Element item = (Element) nodeList.item(i);
				thirdPartyIcons.add(new ThirdPartyIcon(item.getAttribute(
				    ThemeTag.ATTR_APPUID).equals("") ? null : item
				    .getAttribute(ThemeTag.ATTR_APPUID), item
				    .getAttribute(ThemeTag.ATTR_ID), item
				    .getAttribute(ThemeTag.ATTR_NAME), item.getAttribute(
				    ThemeTag.ATTR_MAJORID).equals("") ? null : item
				    .getAttribute(ThemeTag.ATTR_MAJORID), item.getAttribute(
				    ThemeTag.ATTR_MINORID).equals("") ? null : item
				    .getAttribute(ThemeTag.ATTR_MINORID), 
				    thirdPartyIconType));
			}
			return thirdPartyIcons;
		} catch (TransformerException e) {
			throw new ThirdPartyIconLoadException(e);
		}
	}

	/**
	 * Stores the list of TPI to the passed OutputStream.
	 * 
	 * @param thirdPartyIcons - the list of TPI to be stored
	 * @param stream - the stream to which the TPI are persisted
	 * @return - none
	 */
	public static void storeThirdParyIcons(DefinedIcons thirdPartyIcons,
	    OutputStream stream) throws ThirdPartyIconStoreException {
		try {

			// Fetching the template document instance from default tool
			// specific TPI location and
			// into this remove the existing defined TPI and put the ones
			// present in the thirdPartyIcons
			// parameters
			URL toolSpecificThirdPartyIconUrl = getToolSpecificThirdPartyIconUrl();
			if(toolSpecificThirdPartyIconUrl == null){
				return;
			}
			InputStream inputStream = toolSpecificThirdPartyIconUrl.openStream();
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			db.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId,
				    String systemId) throws SAXException, IOException {
					return new InputSource(IThemeDescriptor.class
					    .getResourceAsStream(IThemeManager.DTD_FOLDER
					        + new File(systemId).getName()));
				}
			});
			Document document = db.parse(inputStream);
			
			storeThirdParyIcons(thirdPartyIcons, document);
			XmlUtil.removeEmptyLines(document);
			Map<String, String> props = new HashMap<String, String>();
			props.put(OutputKeys.DOCTYPE_SYSTEM, DTD_RESOURCE);
			XmlUtil.write(document, stream, props);
		} catch (Exception e) {
			throw new ThirdPartyIconStoreException(e);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				// Simply ignoring any IOException that can be caused.
			}
		}
	}

	/**
	 * Stores the list of TPI to the passed URL location.
	 * 
	 * @param thirdPartyIcons - the list of TPI to be stored
	 * @param url - the URL to which the TPI are persisted
	 * @return - none
	 */
	public static void storeThirdPartyIcons(DefinedIcons thirdPartyIcons, URL url)
	    throws ThirdPartyIconStoreException {

		if(url == null){
			return;
		}
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			storeThirdParyIcons(thirdPartyIcons, outStream);
			FileOutputStream stream = new FileOutputStream(FileUtils.getFile(url));
			stream.write(outStream.toByteArray());
			stream.close();
		} catch (IOException e) {
			throw new ThirdPartyIconStoreException(e);
		}
	}

	/**
	 * Stores the list of TPI to the passed Document.
	 * 
	 * @param thirdPartyIcons - the list of TPI to be stored
	 * @param document - the document to which the TPI are persisted
	 * @return - none
	 */
	public static void storeThirdParyIcons(DefinedIcons thirdPartyIcons,
	    Document document) throws ThirdPartyIconStoreException {
		try {
			NodeList icEls = XPathAPI.selectNodeList(document
			    .getDocumentElement(), "//" + ThemeTag.ELEMENT_ELEMENT);
			for (int i = 0; i < icEls.getLength(); i++) {
				Element item = (Element) icEls.item(i);
				item.getParentNode().removeChild(item);
			}
			String path = "//$task/$componentgroup/$component";
			path = path.replace("$task", ThemeTag.ELEMENT_TASK);
			path = path.replace("$componentgroup",
			    ThemeTag.ELEMENT_COMPONENT_GROUP);
			path = path.replace("$component", ThemeTag.ELEMENT_COMPONENT);
			Element nest = (Element) XPathAPI.selectSingleNode(document
			    .getDocumentElement(), path);
			for (ThirdPartyIcon icon : thirdPartyIcons) {
				Element newEl = document
				    .createElement(ThemeTag.ELEMENT_ELEMENT);
				newEl.setAttribute(ThemeTag.ATTR_SHOW, "true");
				newEl.setAttribute(ThemeTag.ATTR_PREVIEWSCREEN,
				    DEFAULT_TPI_PREVIEW_SCREEN);
				newEl.setAttribute(ThemeTag.ATTR_DERIVED_LAYOUT_ID,
				    DEFAULT_TPI_TEMPLATE_ELEMENT);
				if (null != icon.getAppUid())
					newEl.setAttribute(ThemeTag.ATTR_APPUID, icon.getAppUid());
				else {
					newEl.setAttribute(ThemeTag.ATTR_MAJORID, icon.getMajorId());
					newEl.setAttribute(ThemeTag.ATTR_MINORID, icon.getMinorId());
				}
				newEl.setAttribute(ThemeTag.ATTR_ID, icon.getId());
				newEl.setAttribute(ThemeTag.ATTR_NAME, icon.getName());
				nest.appendChild(newEl);
			}
		} catch (TransformerException e) {
			throw new ThirdPartyIconStoreException(e);
		}
	}

	/**
	 * Find the list of the conflicting icons for the theme. 
	 * [includes the conflicts for:
	 * 1. Amongst tool specific icons
	 * 2. Between Tool specific and theme specific icons.
	 * 3. Amongst theme specific icons.
	 * 
	 * @param theme
	 * @return
	 */
	public static Map<ThirdPartyIconWrapper, List<TPIconConflictEntry>> getConflictingIconList(final S60Theme theme) {

		// Get the list of Theme specific TP Icons
		List<ThirdPartyIcon> themeTPIcons = theme.getThemeSpecificThirdPartyIcons();
		
		// Reload the Tool Specific icons in case it was changed and stored from the last load time.
		// Improve on removing the reloading, but this would mean that in all places
		// where the tool specific icons will get persisted because of the change, we would need to 
		// update the information here.
		try {
			getToolSpecificThirdPartyIcons(true);
		} catch (ThirdPartyIconLoadException e) {
			S60ThemePlugin.error(e);
		}
		DefinedIcons allThemeApplicableTPIs = new DefinedIcons();
		if(toolSpecificThirdPartyIcons != null)
			allThemeApplicableTPIs.addAll(toolSpecificThirdPartyIcons);
		
		if(themeTPIcons != null){
			allThemeApplicableTPIs.addAll(themeTPIcons);
		}
		
		return getConflictingIconList(allThemeApplicableTPIs);
	}

	public static Map<ThirdPartyIconWrapper, List<TPIconConflictEntry>> getConflictingIconList(List<ThirdPartyIcon> thirdPartyIcons) {
		Map<ThirdPartyIconWrapper, List<TPIconConflictEntry>> conflictingIcons = new HashMap<ThirdPartyIconWrapper, List<TPIconConflictEntry>>();
	
		if(thirdPartyIcons != null && !thirdPartyIcons.isEmpty()){
			for (ThirdPartyIcon iconToVerify : thirdPartyIcons) {
				List<TPIconConflictEntry> conflictEntryList = processForConflicts(iconToVerify, thirdPartyIcons);

				if (conflictEntryList != null && !conflictEntryList.isEmpty()) {
					Iterator<TPIconConflictEntry> iterator = conflictEntryList.iterator();
					while(iterator.hasNext()){
						// If there is a conflict between tool specific and theme specific TPI on the name only then 
						// do not report it as a conflict and exclude it from being added to the list of conflicts.
						TPIconConflictEntry conflicts = iterator.next();
						if( conflicts.getConflictingFields().size() == 1 &&
							conflicts.getConflictingFields().get(0) == TPIconConflitingField.TP_ICON_NAME &&
							iconToVerify.getThirdPartyIconType() != conflicts.getConflictThirdPartyIcon().getThirdPartyIconType()){
							
							iterator.remove();
						}	
					}
					
					if(!conflictEntryList.isEmpty()){
						conflictingIcons.put(new ThirdPartyIconWrapper(iconToVerify), conflictEntryList);
					}
				}
			}
		}
		return conflictingIcons;
	}
	
	/**
	 * Get Conflicting icon list For packaging, we consider only the skinned
	 * elements
	 * 
	 * @param theme
	 * @return
	 */
	public static Map<ThirdPartyIconWrapper, List<TPIconConflictEntry>> getConflictingIconListForPackaging(final S60Theme theme) {

		List<ThirdPartyIcon> thirdPartyIcons = new ArrayList<ThirdPartyIcon>();
		for (Object obj : theme.getAllElements().values()) {
			if (obj instanceof SkinnableEntity) {
				SkinnableEntity entity = (SkinnableEntity) obj;

				if (entity.isSkinned() && entity instanceof ThirdPartyIcon) {
					thirdPartyIcons.add((ThirdPartyIcon)entity);
				}
			}
		}
		return getConflictingIconList(thirdPartyIcons);		
	}


	/**
	 * Process Icons for conflicts
	 * 
	 * @param iconToVerify
	 * @param tpIcons
	 * @param isBetweenThemeAndTool
	 * @return
	 */
	private static List<TPIconConflictEntry> processForConflicts(final ThirdPartyIcon iconToVerify, final List<ThirdPartyIcon> tpIcons) {

		List<TPIconConflictEntry> conflictEntryList = new ArrayList<TPIconConflictEntry>();
		
		for (ThirdPartyIcon icon : tpIcons) {
			if(iconToVerify != icon){
				
				TPIconConflictEntry tpIconConflictEntry = null;
				
				if(iconToVerify.getName() != null && icon.getName() != null 
				   && iconToVerify.getName().equals(icon.getName())){

					if(tpIconConflictEntry == null){
						tpIconConflictEntry = new TPIconConflictEntry(icon);
						conflictEntryList.add(tpIconConflictEntry);
					}
					tpIconConflictEntry.addConflitingField(TPIconConflitingField.TP_ICON_NAME);

				}

				if(iconToVerify.getAppUid() != null && icon.getAppUid() != null
				   && ( iconToVerify.getAppUid().equals(icon.getAppUid()) || 
					    iconToVerify.getLongAppUID() == icon.getLongAppUID()) ){

					if(tpIconConflictEntry == null){
						tpIconConflictEntry = new TPIconConflictEntry(icon);
						conflictEntryList.add(tpIconConflictEntry);
					}					
					tpIconConflictEntry.addConflitingField(TPIconConflitingField.TP_ICON_ID);
				}

				if(iconToVerify.getMajorId() != null && icon.getMajorId() != null
				   && ( iconToVerify.getMajorId().equals(icon.getMajorId())  
						|| iconToVerify.getLongMajorID() == icon.getLongMajorID() )
				   && iconToVerify.getMinorId() != null && icon.getMinorId() != null
				   && (iconToVerify.getMinorId().equals(icon.getMinorId()) 
						|| iconToVerify.getLongMinorID() == icon.getLongMinorID()) ){
					
					if(tpIconConflictEntry == null){
						tpIconConflictEntry = new TPIconConflictEntry(icon);
						conflictEntryList.add(tpIconConflictEntry);
					}					
					tpIconConflictEntry.addConflitingField(TPIconConflitingField.TP_ICON_MAJORID_MINORID);
				}
			}
		}

		return conflictEntryList;
	}

	/**
	 * This inner class provides a provision for the creation of ThirdPartyIcon
	 * Element for the ThemeSpecific third party icons. It would basically have
	 * loaded and created and cached all the theme specific third party icons
	 * and when an element is being requested for creation, it will be retrieved
	 * from the cached set of elements which are loaded in the constructor and
	 * returned. If particular requested element is not already created, the it
	 * is newly created and returned.
	 */
	private static class ThirdPartyIconElementCreator
	    implements ElementCreator {

		private DefinedIcons thirdPartyIcons, copyThirdPartyIcons;
		
		private ThirdPartyIconType thirdPartyIconType;
		
		ThirdPartyIconElementCreator(URL thirdPartyIconFileURL, ThirdPartyIconType thirdPartyIconType, Theme theme)
		    throws ThirdPartyIconLoadException {
			this.thirdPartyIconType = thirdPartyIconType;
			thirdPartyIcons = loadThirdPartyIcons(thirdPartyIconFileURL, thirdPartyIconType);
			if(thirdPartyIcons != null){
				copyThirdPartyIcons = new DefinedIcons();
				for(ThirdPartyIcon icon: thirdPartyIcons){
					icon.copyProperties(theme.getElementWithId(icon.getId().toLowerCase()));
					copyThirdPartyIcons.add(icon);
				}
				
			}
		}

		public DefinedIcons getCreatedThirdPartyIcons() {
			return copyThirdPartyIcons;
		}

		public com.nokia.tools.platform.theme.Element createElement(
		    String elementName) {
			if (thirdPartyIcons != null) {
				ThirdPartyIcon thirdPartyIcon = thirdPartyIcons
				    .get(elementName);
				if (thirdPartyIcon != null) {
					thirdPartyIcons.remove(thirdPartyIcon);
					return thirdPartyIcon;
				}
			}
			return new ThirdPartyIcon(null, elementName, null, null, thirdPartyIconType);
		}

	}

	public static void loadAllPredefinedAppUIDSInToolsModels(){

	}
	

}
