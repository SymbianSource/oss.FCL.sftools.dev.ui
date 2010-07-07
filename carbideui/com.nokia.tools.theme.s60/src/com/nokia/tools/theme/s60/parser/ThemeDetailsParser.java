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


package com.nokia.tools.theme.s60.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.IDevice;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.theme.AnimatedThemeGraphic;
import com.nokia.tools.platform.theme.ColourGraphic;
import com.nokia.tools.platform.theme.Component;
import com.nokia.tools.platform.theme.IThemeManager;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.LayerEffect;
import com.nokia.tools.platform.theme.ParameterModel;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.SoundGraphic;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeConstants;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.DebugHelper;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.theme.core.MultiPieceManager;
import com.nokia.tools.theme.s60.IThemeConstants;
import com.nokia.tools.theme.s60.S60ThemePlugin;
import com.nokia.tools.theme.s60.cstore.ComponentStore;
import com.nokia.tools.theme.s60.model.AnimatedParameterModel;
import com.nokia.tools.theme.s60.model.MorphedGraphic;
import com.nokia.tools.theme.s60.model.S60Theme;
import com.nokia.tools.theme.s60.morphing.AnimationFactory;
import com.nokia.tools.theme.s60.morphing.KErrArgument;
import com.nokia.tools.theme.s60.morphing.timemodels.BaseTimingModelInterface;
import com.nokia.tools.theme.s60.morphing.valuemodels.BaseValueModelInterface;


/**
 * The class defines the parser for parsing the skin-details xml file.
 */
public class ThemeDetailsParser {

	private static final String DTD_PATH = "/dtds/skindata.dtd";

	private Theme model;

	private Theme theme;

	private URL url;

	private Document document;

	private boolean currentElementSoftMask;

	private ThemeBasicData sbd;

	private boolean isModel;

	private Map<Object, Object> usedFiles = new HashMap<Object, Object>();

	private IProgressMonitor monitor;

	/**
	 * Constructor
	 */
	public ThemeDetailsParser(URL url, Theme model, IProgressMonitor monitor)
	    throws ThemeException {
		if (model == null) isModel = true; else isModel = false;
		init(url, model, monitor);
	}
	
	private void init(URL url, Theme model, IProgressMonitor monitor) throws ThemeException {
		this.model = model;
		this.url = url;
		this.monitor = monitor;
		// Initialize the document object
		initalizeParser();
	}

	/**
	 * Constructor
	 * isLoadingModel - indicates that we are loading a platform and not a
     * user defined theme.
	 */
	public ThemeDetailsParser(URL url, Theme model, boolean isLoadingModel, IProgressMonitor monitor)
	    throws ThemeException {
		this.isModel = isLoadingModel;
		init(url, model, monitor);
	}
	/**
	 * Initalizes the xml parser
	 * 
	 * @throws ThemeAppParserException If not successful in initalizing the xml
	 *             parser
	 */
	private void initalizeParser() throws ThemeException {
		InputStream in = null;
		try {
			// create the xml factory instance
			DocumentBuilderFactory factory = DocumentBuilderFactory
			    .newInstance();

			factory.setValidating(true);
			factory.setIgnoringElementContentWhitespace(true);

			DocumentBuilder builder = factory.newDocumentBuilder();

			builder.setErrorHandler(new ErrorHandler() {

				public void error(SAXParseException e) throws SAXException {
					throw new SAXException(e);
				}

				public void fatalError(SAXParseException e) throws SAXException {
					throw new SAXException(e);
				}

				public void warning(SAXParseException e) {

				}
			});

			builder.setEntityResolver(new EntityResolver() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
				 *      java.lang.String)
				 */
				public InputSource resolveEntity(String publicId,
				    String systemId) throws SAXException, IOException {
					return new InputSource(getClass().getResourceAsStream(
					    DTD_PATH));
				}

			});
			if (DebugHelper.debugParser()) {
				DebugHelper.debug(this, "parsing " + url);
			}
			in = new BufferedInputStream(url.openStream(), FileUtils.BUF_SIZE);
			document = builder.parse(in);
		} catch (Throwable e) {
			S60ThemePlugin.error(e);
			throw new ThemeException(e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
			}
		}
	}

	private Set<String> getModelIds() throws ThemeException {
		NodeList list = document.getElementsByTagName(ThemeTag.ELEMENT_PHONE);
		if (list.getLength() == 0) {
			return null;
		}
		list = ((Element) list.item(0))
		    .getElementsByTagName(ThemeTag.ELEMENT_MODEL);
		if (list.getLength() == 0) {
			throw new ThemeException("No model information in the theme.");
		}
		Set<String> ids = new LinkedHashSet<String>();
		for (int i = 0; i < list.getLength(); i++) {
			String id = ((Element) list.item(i)).getAttribute(ThemeTag.ATTR_ID);
			if (!StringUtils.isEmpty(id)) {
				ids.add(id);
			}
		}
		return ids;
	}

	/**
	 * Parses the xml file for general information. Called by both parseLean and
	 * parseFat functions
	 * 
	 * @param theme The S60Theme object in which the gathered information is
	 *            stored.
	 * @throws ThemeAppParserException If not able to successfully parse the xml
	 *             file.
	 */
	public Theme parseLean() throws ThemeException {
		Set<String> modelIds = getModelIds();
		if (model == null) {
			int i = 0;
			IThemeModelDescriptor descriptor = null;
			for (String modelId : modelIds) {
				descriptor = ThemePlatform.getThemeModelDescriptorById(modelId);
				if (descriptor != null) {
					break;
				}
				i++;
			}
			if (descriptor == null) {
				descriptor = ThemePlatform
				    .getDefaultThemeModelDescriptor(IThemeConstants.THEME_CONTAINER_ID);
			}
			if (descriptor == null) {
				// shows the error messages in the UI environment
				throw new ThemeException(Messages.Theme_Configuration_Error);
			}

			if (i != 0) {
				final String msg = NLS.bind(
				    Messages.Theme_Configuration_Warning_Message, new Object[] {
				        modelIds.isEmpty() ? "" : modelIds.iterator().next(),
				        descriptor.getId() });

			
				// this warning will be propagated to the caller
				final org.eclipse.swt.widgets.Display display = org.eclipse.swt.widgets.Display
				    .getDefault();
				display.syncExec(new Runnable() {

					public void run() {
						// make sure we're in the GUI mode
						Shell shell = display.getActiveShell();
						if (shell != null) {
							MessageDialog
							    .openWarning(shell,
							        Messages.Theme_Configuration_Warning_Title,
							        msg);
						}
					}
				});
			}

			model = ThemePlatform.getThemeManagerByThemeModelId(
			    descriptor.getId()).getModel(descriptor.getId(), monitor);

			theme = (Theme) model.clone();
		} else {
			theme = model;
		}
		theme.setThemeFile(FileUtils.getFile(url));

		// Get the name of the skin (it is an attribute of the root element
		// itself)
		Map<Object, Object> rootAttr = DomHelperFunctions
		    .getAttributes(document.getDocumentElement());
		theme.setAttribute(rootAttr);
		String name = (String) rootAttr.get(ThemeTag.ATTR_ENG_NAME);
		theme.setThemeName(name);
		return theme;
	}

	/**
	 * Fetches the name of the skin in different languages
	 * 
	 * @param theme The S60Theme object in which the gathered information is
	 *            stored.
	 * @throws ThemeAppParserException If not able to successfully parse the xml
	 *             file.
	 */
	private void getLanguageNames(Theme theme) {

		// Search the document for 'othernames' tag
		NodeList otherNameList = document
		    .getElementsByTagName(ThemeTag.ELEMENT_OTHERNAMES);

		// There should be only one tag with the name othernames. So taking the
		// first item for consideration
		if (otherNameList.getLength() > 0) {

			NodeList aliasList = ((Element) otherNameList.item(0))
			    .getElementsByTagName(ThemeTag.ELEMENT_ALIAS);

			for (int i = 0; i < aliasList.getLength(); i++) {
				Element alias = (Element) aliasList.item(i);
				Map attributes = DomHelperFunctions.getAttributes(alias);

				int langId = Integer.parseInt((String) attributes
				    .get(ThemeTag.ATTR_ID));
				String name = (String) attributes.get(ThemeTag.ATTR_NAME);

				theme.setLangName(langId, name);
			}
		}
	}

	/**
	 * Parses the xml file and records all the required data from the configured
	 * file
	 * 
	 * @param theme The S60Theme object in which the gathered information is
	 *            stored.
	 * @throws ThemeAppParserException If not able to successfully parse the xml
	 *             file.
	 */

	public Theme parseFat() throws ThemeException {
		parseLean();

		// get the langugage names
		getLanguageNames(theme);

		// Parse fat fuction will add the skin details information to the
		// existing
		// details in the theme object. The sequence of operation is
		// 1. For each element/part in the theme object find its
		// corresponding
		// details in the skin-details xml file.
		// 2. Record the new information in the theme file.

		Element root = document.getDocumentElement();
		String drmStr = root.getAttribute(ThemeTag.ADVANCE_DRM);
		String systemSkinStr = root.getAttribute(ThemeTag.ADVANCE_SYSTEM);
		String uid = root.getAttribute(ThemeTag.ADVANCE_UID);
		String width = root.getAttribute(ThemeTag.LAYOUT_WIDTH);
		String height = root.getAttribute(ThemeTag.LAYOUT_HEIGHT);
		String orientation = root.getAttribute(ThemeTag.ORIENTATION);
		String puid = root.getAttribute(ThemeTag.ADVANCE_PUID);

		boolean drm = new Boolean(drmStr);
		boolean systemSkin = new Boolean(systemSkinStr);
		IDevice[] devices = ThemePlatform.getDevicesByThemeId(theme
		    .getThemeId());
		Display display = null;
		if (!StringUtils.isEmpty(width) && !StringUtils.isEmpty(height)) {
			int w = Integer.parseInt(width);
			int h = Integer.parseInt(height);
			display = new Display(w, h);
			display.setOrientation(orientation);
		} else {
			// takes default device display
			if (devices.length > 0) {
				display = devices[0].getDisplay();
			}
		}
		theme.setDRM(drm);
		theme.setSystemTheme(systemSkin);
		theme.setUID(uid);

		if (!StringUtils.isEmpty(puid)) {
			// assume display type is used, then check the type sanity
			display.setType(puid);
			boolean isValid = false;
			for (IDevice device : devices) {
				if (display.equals(device.getDisplay())) {
					isValid = true;
					break;
				}
			}
			if (!isValid) {
				display.setType(Display.DEFAULT_TYPE);
			}
		}
		IDevice device = ThemePlatform.getDevice(theme.getThemeId(), display);
		if (device == null) {
			if (devices.length == 0) {
				throw new ThemeException(NLS.bind(
				    Messages.Theme_Configuration_Error, new Object[] { display
				        .toString() }));
			}
			device = devices[0];
		}
		theme.setDevice(device);

		HashMap<String, HashMap<Object, Object>> lineClrs = theme
		    .getSkinSettings();
		HashMap<Object, Object> lineColours = DomHelperFunctions
		    .getLineColourElements(document.getDocumentElement());
		Iterator it = lineColours.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next().toString();
			HashMap<Object, Object> temp = lineClrs.get(key);
			if (temp != null) {
				temp.put(ThemeTag.ATTR_VALUE, lineColours.get(key));
				lineClrs.put(key, temp);
			}
		}
		theme.setSkinSettings(lineClrs);

		Set<Element> elements = new HashSet<Element>();

		// traverses the document tree only once instead of in the big loop
		NodeList list = document.getElementsByTagName(ThemeTag.ELEMENT_ELEMENT);

		Map<String, Element> idMap = new HashMap<String, Element>();
		Map<String, Element> nameMap = new HashMap<String, Element>();
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			Element element = (Element) list.item(i);
			elements.add(element);
			String id = element.getAttribute(ThemeTag.ATTR_ID).toLowerCase();
			String name = element.getAttribute(ThemeTag.ATTR_NAME)
			    .toLowerCase();

			if (!idMap.containsKey(id) && id != null && id.length() > 0) {
				idMap.put(id, element);
			}
			if (!nameMap.containsKey(name) && name != null && name.length() > 0) {
				nameMap.put(name, element);
			}
		}
		list = document.getElementsByTagName(ThemeTag.ELEMENT_PART);
		// part id/names are not unique
		Map<String, Set<Element>> partIdMap = new HashMap<String, Set<Element>>();
		Map<String, Set<Element>> partNameMap = new HashMap<String, Set<Element>>();
		len = list.getLength();
		for (int i = 0; i < len; i++) {
			Element element = (Element) list.item(i);
			elements.add(element);
			String id = element.getAttribute(ThemeTag.ATTR_ID).toLowerCase();
			String name = element.getAttribute(ThemeTag.ATTR_NAME)
			    .toLowerCase();

			if (id.length() > 0) {
				Set<Element> parts = partIdMap.get(id);
				if (parts == null) {
					parts = new HashSet<Element>();
					partIdMap.put(id, parts);
				}
				parts.add(element);
			}
			if (name.length() > 0) {
				Set<Element> parts = partNameMap.get(id);
				if (parts == null) {
					parts = new HashSet<Element>();
					partNameMap.put(name, parts);
				}
				parts.add(element);
			}
		}

		Theme defaultModel = theme.getModel();
		IThemeManager manager = ThemePlatform
		    .getThemeManagerByThemeModelId(model.getModelId());

		// first processes the current model
		int oldSize = elements.size();
		processModel(elements, idMap, nameMap, partIdMap, partNameMap);
		int newSize = elements.size();
		if (!theme.isModel() && !elements.isEmpty()) {
			if (DebugHelper.debugParser()) {
				DebugHelper.debug(this, theme.getThemeName() + " ("
				    + model.getModelId() + ")" + " processed: "
				    + (oldSize - newSize) + " elements, remaining: " + newSize);
			}

			// then processes the other models
			boolean isChanged = false;
			for (String modelId : getModelIds()) {
				IThemeModelDescriptor modelDescriptor = ThemePlatform
				    .getThemeModelDescriptorById(modelId);
				if (modelDescriptor == null) {
					// not available
					continue;
				}
				Theme model = manager.getModel(modelId, monitor);
				if (model != defaultModel) {
					theme.setModel(model);
					isChanged = true;
					oldSize = elements.size();
					processModel(elements, idMap, nameMap, partIdMap,
					    partNameMap);
					newSize = elements.size();
					if (DebugHelper.debugParser()) {
						DebugHelper.debug(this, theme.getThemeName() + " ("
						    + model.getModelId() + ")" + " processed: "
						    + (oldSize - newSize) + " elements, remaining: "
						    + newSize);
					}
					if (elements.isEmpty()) {
						// all done
						break;
					}
				}
			}
			if (!elements.isEmpty() && DebugHelper.debugParser()) {
				StringBuilder sb = new StringBuilder(theme.getThemeName()
				    + ": Unknown items: ");
				for (Element element : elements) {
					sb.append(element.getAttribute(ThemeTag.ATTR_ID) + ",");
				}
				DebugHelper.debug(this, sb.toString());
			}
			if (isChanged) {
				// restores the default model
				theme.setModel(defaultModel);
			}
		}
		processComponentStore(idMap);
		theme.validateSkinned();
		return theme;
	}

	private void processModel(Set<Element> elements,
	    Map<String, Element> idMap, Map<String, Element> nameMap,
	    Map<String, Set<Element>> partIdMap,
	    Map<String, Set<Element>> partNameMap) throws ThemeException {
		Map<String, com.nokia.tools.platform.theme.Element> elementMap = theme
		    .getAllElements();
		if (elementMap != null) {
			for (com.nokia.tools.platform.theme.Element elementEntity : elementMap
			    .values()) {
				// set the element details
				String elementItemId = elementEntity
				    .getAttributeValue(ThemeTag.ATTR_ID);
				String elementName = elementEntity
				    .getAttributeValue(ThemeTag.ATTR_NAME);
				Element elementDetails = elementItemId == null ? null : idMap
				    .get(elementItemId.toLowerCase());
				if (elementDetails == null
				    && elementName != null
				    && elementEntity.getAttributeValue(ThemeTag.CHECKNAME) != null) {
					elementDetails = nameMap.get(elementName.toLowerCase());
				}

				boolean isHandled = !elements.remove(elementDetails);
				if (isHandled) {
					// processed by another model already
					elementDetails = null;
				}
				currentElementSoftMask = (elementEntity.getToolBox()).SoftMask;
				sbd = elementEntity;

				if (elementDetails != null) {
					setEntityDetails(elementEntity, elementDetails);
				}

				

				// parts are processed only when there is element info,
				// otherwise, the element has been processed already
				// check for parts if it has
				if (isHandled && !theme.isModel()) {
					// element has been handled already and no need to check the
					// parts
					continue;
				}

				List partsList = elementEntity.getChildren();
				if (partsList != null) {
					boolean isPartMatched = false;
					for (int j = 0; j < partsList.size(); j++) {
						ThemeBasicData partEntity = (ThemeBasicData) partsList
						    .get(j);
						String partName = partEntity
						    .getAttributeValue(ThemeTag.ATTR_NAME);
						String partItemId = partEntity
						    .getAttributeValue(ThemeTag.ATTR_ID);
						Element partDetails = null;
						Set<Element> ps = partIdMap.get(partItemId
						    .toLowerCase());
						if (ps != null) {
							for (Element p : ps) {
								if (p.getParentNode() == elementDetails) {
									partDetails = p;
									break;
								}
							}
						}

						if (partDetails == null
						    && partEntity.getAttributeValue(ThemeTag.CHECKNAME) != null) {
							ps = partNameMap.get(partName.toLowerCase());
							if (ps != null) {
								for (Element p : ps) {
									if (p.getParentNode() == elementDetails) {
										partDetails = p;
										break;
									}
								}
							}
						}

						if (!elements.remove(partDetails)) {
							// processed by another model already
							partDetails = null;
						}

						// The partDetails list must have one and only one
						// element
						if (partDetails != null) {
							isPartMatched = true;
							setEntityDetails(partEntity, partDetails);
							elementEntity.setSelected1(true);
							
						} else { // for this particular element parts in
							// tdf
							// will not be there
							// hence setThe property to singlebitmap
							if (elementEntity.getProperties() != null) {
								// when all the parts don't match
								if (!isPartMatched) {
									isPartMatched = false;
								}
							}
						}
					}

					if (!isPartMatched && elementDetails != null) {

						elementEntity
						    .setCurrentProperty(ThemeConstants.PROPERTIES_BITMAP);
					} else {
						MultiPieceManager.setCurrentProperty(elementEntity, partsList.size());
					}
				}

				if (theme.isModel()) {
					if (elementEntity.isEntityType().equals(
					    ThemeTag.ELEMENT_BMPANIM)) {
						elementEntity.getToolBox().Test = elementEntity
						    .getIdentifier();
						AnimatedThemeGraphic atg = elementEntity
						    .getAnimatedThemeGraphic();
						if (atg != null) {
							List l = atg.getThemeGraphics();
							if ((l != null) && (l.size() > 1)) {
								elementEntity
								    .setCurrentProperty(ThemeTag.ATTR_BMPANIM);
							} else {
								elementEntity
								    .setCurrentProperty(ThemeTag.ATTR_STILL);
							}
						}
					}
				}
			}
		}
	}

	private void processComponentStore(Map<String, Element> idMap)
	    throws ThemeException {
	
		// add elements that have unknown id to some component - needed for
		// component store
		com.nokia.tools.platform.theme.Element ref = theme
		    .getElementWithId("qsn_bg_screen");
		com.nokia.tools.platform.theme.Element ref9 = theme
		    .getElementWithId("S60_2_6%qsn_fr_grid");
		com.nokia.tools.platform.theme.Element refAnimated = theme
		    .getElementWithId("qgn_note_batt_charging_anim");
		Component refComponent = (Component) (ref != null ? ref.getParent()
		    : null);
		Component refComponent9 = (Component) (ref9 != null ? ref9.getParent()
		    : null);
		Component refComponentAnimated = (Component) (refAnimated != null ? refAnimated
		    .getParent()
		    : null);
		for (String id : idMap.keySet()) {
			if (ComponentStore.isComponentStoreElement(id)) {

				Element elementDetails = idMap.get(id);

				NodeList partChilds = elementDetails
				    .getElementsByTagName("part");
				boolean isMultiPiece = MultiPieceManager.isMultiPiece(partChilds.getLength());
				
				boolean isAnimated = ComponentStore.isAnimatedElement(id);

				if (isAnimated && refComponentAnimated != null) {

					com.nokia.tools.platform.theme.Element newElement = (com.nokia.tools.platform.theme.Element) refAnimated
					    .clone();
					newElement.setAttribute(ThemeTag.ATTR_ID, id);
					newElement.setAttribute(ThemeTag.ATTR_NAME, id);
					refComponentAnimated.addChild(newElement);

					// name is overwritten in setEntityDetails, need to
					// rememeber
					String name = elementDetails
					    .getAttribute(ThemeTag.ATTR_NAME);

					// toolbox is not correctly, override is needed to
					// create right type of ThemeGraphic instance
					// newElement.getToolBox().multipleLayersSupport =
					// ComponentStore.isMultilayerElement(newElement.getId());

					if (elementDetails != null) {
						currentElementSoftMask = (newElement.getToolBox()).SoftMask;
						sbd = newElement;
						setEntityDetails(newElement, elementDetails);
						newElement.setSelected1(true);
					}

					newElement.setAttribute(ThemeTag.ATTR_NAME, name);

				} else if (isMultiPiece && refComponent9 != null) {
					com.nokia.tools.platform.theme.Element newElement = (com.nokia.tools.platform.theme.Element) ref9
					    .clone();
					newElement.setAttribute(ThemeTag.ATTR_ID, id);
					newElement.setAttribute(ThemeTag.ATTR_NAME, id);
					refComponent9.addChild(newElement);

					// remember name attr
					String name = elementDetails
					    .getAttribute(ThemeTag.ATTR_NAME);

					// ***** parts parsing start *****
					List partsList = newElement.getChildren();

					if (partsList != null) {
						boolean isPartMatched = false;
						for (int j = 0; j < partsList.size(); j++) {

							ThemeBasicData partEntity = (ThemeBasicData) partsList
							    .get(j);
							String partName = partEntity
							    .getAttributeValue(ThemeTag.ATTR_NAME);
							String partItemId = partEntity
							    .getAttributeValue(ThemeTag.ATTR_ID);

							Element partDetails = (Element) partChilds.item(j);

							// The partDetails list must have one and only
							// one
							// element
							isPartMatched = true;
							setEntityDetails(partEntity, partDetails);
							partEntity.setSelected1(true);

							if (newElement.getProperties() != null) {
								if (!isPartMatched) {
									newElement
									    .setCurrentProperty(ThemeConstants.PROPERTIES_BITMAP);
								} else {
									newElement.setCurrentProperty( MultiPieceManager.getElementTypeId(partsList.size()));
									
								}
							}
						}
					}
					// ***** parts parsing end *****
					newElement.setAttribute(ThemeTag.ATTR_NAME, name);

				} else if (refComponent != null) {
					com.nokia.tools.platform.theme.Element newElement = (com.nokia.tools.platform.theme.Element) ref
					    .clone();
					newElement.setAttribute(ThemeTag.ATTR_ID, id);
					newElement.setAttribute(ThemeTag.ATTR_NAME, id);
					refComponent.addChild(newElement);

					// name is overwritten in setEntityDetails
					String name = elementDetails
					    .getAttribute(ThemeTag.ATTR_NAME);

					// toolbox is not correctly, override is needed to
					// create right type of ThemeGraphic instance
					newElement.getToolBox().multipleLayersSupport = ComponentStore
					    .isMultilayerElement(newElement.getId());

					if (elementDetails != null) {
						currentElementSoftMask = (newElement.getToolBox()).SoftMask;
						sbd = newElement;
						setEntityDetails(newElement, elementDetails);
						newElement.setSelected1(true);
					}

					newElement.setAttribute(ThemeTag.ATTR_NAME, name);
				}
			}
		}
	
	}

	private List getAnimateChilds(String tag, String attr, String value) {
		List matchList = null;
		if (value != null) {
			matchList = DomHelperFunctions.getElements(document
			    .getDocumentElement(), tag, attr, value);
			return matchList;
		}
		return matchList;
	}

	/**
	 * Sets the element/parts details in the element/parts object
	 * 
	 * @param entity The object in which the entity details will be stored
	 * @param node The node containing the entity information
	 */
	private void setEntityDetails(ThemeBasicData entity, Element node)
	    throws ThemeException {
		// reference to an element/part in the skin file means that the
		// elements/parts was selected for skinning

	
		Map<Object, Object> nodeAttr = DomHelperFunctions.getAttributes(node);

		// if the entity name is already set, to avoid the name from being
		// changed, its removed from the nodeAttr. done by sankarm
		if (entity.getAttributeValue(ThemeTag.ATTR_NAME) != null) {
			nodeAttr.remove(ThemeTag.ATTR_NAME);
		}
		if (entity.isEntityType().equals(ThemeTag.ELEMENT_BMPANIM)) {
			if (nodeAttr.containsKey(ThemeTag.ATTR_TYPE)) {
				entity.setCurrentProperty(nodeAttr.get(ThemeTag.ATTR_TYPE)
				    .toString());
			}
		}

		if ((nodeAttr.containsKey(ThemeTag.ATTR_ANIM_MODE))
		    && (nodeAttr.get(ThemeTag.ATTR_ANIM_MODE) != null)) {
			entity.setAttribute(ThemeTag.ATTR_ANIM_MODE, nodeAttr.get(
			    ThemeTag.ATTR_ANIM_MODE).toString());
		}
		entity.setAttribute(nodeAttr);

		patchAttributes(entity);

	

		boolean isRefer = setReferDetails(entity, node);
		if (isRefer == false) {
			// get the image details from the node
			setGraphicDetails(entity, node);
		}
	}

	/**
	 * Old themes that were saved with small caps on few element causing
	 * problems on several places.
	 * 
	 * @param element
	 */
	private void patchAttributes(ThemeBasicData element) {
		String id = element.getAttributeValue(ThemeTag.ATTR_ID);
		if (null == id)
			return;
		String updatedId = null;
		if (id.equals("qsn_bg_column_a"))
			updatedId = "qsn_bg_column_A";
		else if (id.equals("qsn_bg_column_ab"))
			updatedId = "qsn_bg_column_AB";
		else if (id.equals("qsn_bg_slice_list_a"))
			updatedId = "qsn_bg_slice_list_A";
		else if (id.equals("qsn_bg_slice_list_ab"))
			updatedId = "qsn_bg_slice_list_AB";
		else if (id.length() > 8 && id.substring(0, 8).equals("s60_2_6%"))
			updatedId = "S60_2_6%" + id.substring(8);
		if (null != updatedId)
			element.setAttribute(ThemeTag.ATTR_ID, updatedId);
	}

	/**
	 * Sets the actual and draft Graphic details
	 * 
	 * @param entity The object in which the entity details will be stored
	 * @param node The node containing the entity information
	 * @returns true if atleast one image tag has been set
	 */
	private boolean setGraphicDetails(ThemeBasicData entity, Element node)
	    throws ThemeException {

		// Check the class type. Only Element or Part must be processed
		if (!(entity instanceof com.nokia.tools.platform.theme.Element)
		    && !(entity instanceof com.nokia.tools.platform.theme.Part)) {
			return false;
		}

		// The function has to get all the childrens named 'image' in the given
		// 'node'
		
		NodeList imageList = node.getChildNodes();

		// Dont check if there is an image or not. Since it could be elements
		// having parts (which means part will have the images)
		// The DTD will take care of such errors
		
		AnimatedThemeGraphic atg = null;
		String entityType = entity.isEntityType();
		if (entityType.equals(ThemeTag.ELEMENT_BMPANIM)) {
			if (imageList.getLength() > 0) {
				
				atg = new AnimatedThemeGraphic(entity);
				atg.setAttribute(ThemeTag.ATTR_ID, entity.getIdentifier());
			}
			((SkinnableEntity) entity).clearThemeGraphic();
		}

		for (int i = 0; i < imageList.getLength(); i++) {
			// check if it is an element node
			short nodeType = imageList.item(i).getNodeType();
			if (nodeType != Element.ELEMENT_NODE)
				continue;

			// check if it is a image tag child (could be parts in case of
			// elements)
			Element image = (Element) imageList.item(i);
			String tagName = image.getTagName();

			if (tagName.equalsIgnoreCase(ThemeTag.ELEMENT_GRAPHIC)) {
				entity.setSkinned(false);
				Map<Object, Object> attributes = DomHelperFunctions
				    .getAttributes(image);
				if (!entity.supportDrafts())
					if (attributes.containsKey(ThemeTag.ATTR_STATUS))
						if (attributes.get(ThemeTag.ATTR_STATUS).equals(
						    ThemeTag.ATTR_VALUE_DRAFT))
							continue;

				ThemeGraphic tGraphic = null;
				if (entityType.equals(ThemeTag.ELEMENT_COLOUR))
					tGraphic = new ColourGraphic(entity);
				else if (entityType.equals(ThemeTag.ELEMENT_SOUND)
				    || entityType.equals(ThemeTag.ELEMENT_EMBED_FILE))
					tGraphic = new SoundGraphic(entity);
				else if ((entity.getToolBox() != null)
				    && (entity.getToolBox().multipleLayersSupport)) {
					tGraphic = new MorphedGraphic(entity);
				} else
					tGraphic = new ThemeGraphic(entity);
				tGraphic.setAttributes(attributes);
				tGraphic.setAttribute(ThemeTag.UNIQUE_ID, i + "");

				// /// FOR IMAGE
				Element node1 = (Element) imageList.item(i);
				NodeList imageList1 = node1.getChildNodes();

				for (int j = 0; j < imageList1.getLength(); j++) {
					Element image1 = (Element) imageList1.item(j);
					String tagName1 = image1.getTagName();

					if (tagName1.equalsIgnoreCase(ThemeTag.ELEMENT_IMAGE)) {
						// Image Tag
						Element node2 = (Element) imageList1.item(j);
						NodeList imageList2 = node2.getChildNodes();
						for (int k = 0; k < imageList2.getLength(); k++) {
							Element image2 = (Element) imageList2.item(k);
							String tagName2 = image2.getTagName();

							if (tagName2
							    .equalsIgnoreCase(ThemeTag.ELEMENT_LAYER)) {
								// Layer Tag
								ImageLayer imageLayer = new ImageLayer(tGraphic);
								Map<Object, Object> attr = DomHelperFunctions
								    .getAttributes(image2);

								// This adds fullpath if it is a phonemodel
								addFullPathIfPhoneModel(attr);

								// /// This is to add images to Used Map
								addToUsedImagesMap(attr);
								imageLayer.setAttributes(attr);

								Element node3 = (Element) imageList2.item(k);
								NodeList imageList3 = node3.getChildNodes();
								for (int l = 0; l < imageList3.getLength(); l++) {
									Element image3 = (Element) imageList3
									    .item(l);
									String tagName3 = image3.getTagName();

									if (tagName3
									    .equals(ThemeTag.ELEMENT_EFFECT)) {
										LayerEffect layerEffects = new LayerEffect(
										    tGraphic);
										Map effectsMap = DomHelperFunctions
										    .getAttributes(image3);
										layerEffects.setAttribute(
										    ThemeTag.ATTR_NAME, effectsMap.get(
										        ThemeTag.ATTR_NAME).toString());

										Element node4 = (Element) imageList3
										    .item(l);
										NodeList imageList4 = node4
										    .getChildNodes();
										Map pmMap = new HashMap();
										for (int m = 0; m < imageList4
										    .getLength(); m++) {
											Element image4 = (Element) imageList4
											    .item(m);
											String tagName4 = image4
											    .getTagName();

											if (tagName4
											    .equalsIgnoreCase(ThemeTag.ELEMENT_PARAM)) {
												Map<Object, Object> pmsMap = DomHelperFunctions
												    .getAttributes(image4);
												ParameterModel pm;
												if (pmsMap
												    .containsKey("valuemodelref")) {
													pm = new AnimatedParameterModel(
													    tGraphic);
												} else {
													pm = new ParameterModel(
													    tGraphic);
												}
												pm.setAttributes(pmsMap);
												layerEffects
												    .setParameterModel(pm);
												layerEffects.getAttributes()
												    .put(
												        pmsMap.get(
												            ThemeTag.ATTR_NAME)
												            .toString(), pm);
											}
										}
										if (pmMap.size() == 0) {
											Iterator it = effectsMap.keySet()
											    .iterator();
											while (it.hasNext()) {
												String key = it.next()
												    .toString();
												if (!key
												    .equalsIgnoreCase(ThemeTag.ATTR_NAME)) {
													Map<Object, Object> newMap = new HashMap<Object, Object>();
													newMap
													    .put(
													        ThemeTag.ATTR_NAME,
													        key);
													newMap.put(
													    ThemeTag.ATTR_VALUE,
													    effectsMap.get(key));
													ParameterModel pm = new ParameterModel(
													    tGraphic);
													pm.setAttributes(newMap);
													layerEffects
													    .setParameterModel(pm);
													layerEffects
													    .getAttributes()
													    .put(
													        newMap
													            .get(
													                ThemeTag.ATTR_NAME)
													            .toString(), pm);
												}
											}
										}
										imageLayer
										    .setLayerEffects(layerEffects);
									}
								}
								tGraphic.setImageLayers(imageLayer);
							} else if (tagName2
							    .equalsIgnoreCase(ThemeTag.ELEMENT_TIMINGMODELS)) { // this
								
								NodeList tmsList = image2.getChildNodes();
								for (int l = 0; l < tmsList.getLength(); l++) {
									Element nodeTM = (Element) tmsList.item(l);
									String tagNameTM = nodeTM.getTagName();
									if (tagNameTM
									    .equalsIgnoreCase(ThemeTag.ELEMENT_TIMINGMODEL)) {
										Map tmMap = DomHelperFunctions
										    .getAttributes(nodeTM);
										// create TIMING mOdel
										BaseTimingModelInterface btmi = AnimationFactory
										    .getTimingModelInstance(tmMap.get(
										        ThemeTag.ATTR_NAME).toString());
										try {
											btmi
											    .setParameters((HashMap<String, String>) tmMap);
										} catch (KErrArgument e) {
											e.printStackTrace();
										}
										// tGraphic = new MorphedGraphic();
										if (tGraphic instanceof MorphedGraphic) {
											((MorphedGraphic) tGraphic)
											    .addTimingModel(btmi);
										}
									}
								}
							} else if (tagName2
							    .equalsIgnoreCase(ThemeTag.ELEMENT_VALUEMODELS)) { // this
								
								NodeList tmsList = image2.getChildNodes();

								for (int l = 0; l < tmsList.getLength(); l++) {
									Element nodeVM = (Element) tmsList.item(l);
									String tagNameVM = nodeVM.getTagName();
									if (tagNameVM
									    .equalsIgnoreCase(ThemeTag.ELEMENT_VALUEMODEL)) {
										Map vmMap = DomHelperFunctions
										    .getAttributes(nodeVM);
										// create Value model
										BaseValueModelInterface bvmi = AnimationFactory
										    .getValueModelInstance(vmMap.get(
										        ThemeTag.FLAVOUR_NAME)
										        .toString());
										try {
											bvmi
											    .setParameters((HashMap<String, String>) vmMap);
										} catch (KErrArgument e) {
											e.printStackTrace();
										}
										// tGraphic = new MorphedGraphic();
										if (tGraphic instanceof MorphedGraphic) {
											((MorphedGraphic) tGraphic)
											    .addValueModel(bvmi);
										}
									}
								}
							}
						}
					}
				}
				if (entityType.equals(ThemeTag.ELEMENT_BMPANIM)) {
					if ((tGraphic.getAttribute(ThemeTag.ATTR_TYPE) != null)
					    && (tGraphic.getAttribute(ThemeTag.ATTR_TYPE)
					        .equals(ThemeTag.ATTR_STILL)))
						entity.setCurrentProperty(ThemeTag.ATTR_STILL);
						atg.addThemeGraphic(tGraphic);
						if (!isModel) atg.getData().setSkinned(true);
					
				} else {
					if (entity instanceof com.nokia.tools.platform.theme.Element) {						
						((com.nokia.tools.platform.theme.Element) entity)
						    .setThemeGraphic(tGraphic, !isModel); }
					else
						((com.nokia.tools.platform.theme.Part) entity)
						    .setThemeGraphic(tGraphic, !isModel);
				}
			} else if (tagName.equalsIgnoreCase(ThemeTag.ELEMENT_IMAGE)) {
				entity.setSkinned(false);
				Map<Object, Object> attributes = DomHelperFunctions
				    .getAttributes(image);
				if (!entity.supportDrafts())
					if (attributes.containsKey(ThemeTag.ATTR_STATUS))
						if (attributes.get(ThemeTag.ATTR_STATUS).equals(
						    ThemeTag.ATTR_VALUE_DRAFT))
							continue;
				if (attributes.get(ThemeTag.ATTR_HARDMASK) != null) {
					if (currentElementSoftMask) {
						String hardmaskimage = (String) attributes
						    .get(ThemeTag.ATTR_HARDMASK);
						attributes.remove(ThemeTag.ATTR_HARDMASK);
						String hardmaskimagepath = ((S60Theme) (sbd.getRoot()))
						    .getThemeDir()
						    + File.separator + hardmaskimage;
					

						try {
							CoreImage pi = CoreImage.create().load(
							    new File(hardmaskimagepath), 0, 0)
							    .invertSingleBandMask();
							File file = FileUtils.createFileWithExtension(
							    hardmaskimagepath, IFileConstants.FILE_EXT_BMP);
							pi.save(CoreImage.TYPE_BMP, file);
							attributes.put(ThemeTag.ATTR_SOFTMASK, file
							    .getName());
						} catch (Exception e) {
							S60ThemePlugin.error(e);
						}
					}
				}
				ThemeGraphic tGraphic = null;
				if (entityType.equals(ThemeTag.ELEMENT_COLOUR))
					tGraphic = new ColourGraphic(entity);
				else if (entityType.equals(ThemeTag.ELEMENT_SOUND)
				    || entityType.equals(ThemeTag.ELEMENT_EMBED_FILE))
					tGraphic = new SoundGraphic(entity);
				else
					tGraphic = new ThemeGraphic(entity);
				ImageLayer imageLayer = new ImageLayer(tGraphic);
				
				Map<Object, Object> attr = new HashMap<Object, Object>();
				if (entityType.equals(ThemeTag.ELEMENT_BMPANIM))
					attr.put(ThemeTag.ATTR_TYPE, attributes
					    .get(ThemeTag.ATTR_TYPE));
				attr.put(ThemeTag.ATTR_STATUS, attributes
				    .get(ThemeTag.ATTR_STATUS));

				addFullPathIfPhoneModel(attributes);
				addToUsedImagesMap(attributes);

				imageLayer.setAttributes(attributes);
				tGraphic.setAttributes(attr);
				tGraphic.setImageLayers(imageLayer);

				if (entityType.equals(ThemeTag.ELEMENT_BMPANIM)) {
					tGraphic.setAttribute(ThemeTag.UNIQUE_ID, "0");
					atg.addThemeGraphic(tGraphic);
				}

				// ANIMATION IN PREVIOUS VERSION (BACKWARD COMPATABILITY)

				String elementItemId = entity
				    .getAttributeValue(ThemeTag.ATTR_ID);
				if (entityType.equals(ThemeTag.ELEMENT_BMPANIM)) {
					List animateChilds = getAnimateChilds(
					    ThemeTag.ELEMENT_ELEMENT,
					    ThemeTag.ATTR_ANIMATE_PARENTID, elementItemId);
					if (animateChilds != null) {
						for (int l = 0; l < animateChilds.size(); l++) {
							
							Map childAttr = DomHelperFunctions
							    .getAttributes((Element) animateChilds.get(l));
							
							if (childAttr.containsKey(ThemeTag.ATTR_ID)) {
								List animateElementChilds = getAnimateChilds(
								    ThemeTag.ELEMENT_IMAGE, ThemeTag.ATTR_ID,
								    (String) childAttr.get(ThemeTag.ATTR_ID));
								for (int li = 0; li < animateElementChilds
								    .size(); li++) {
									Map<Object, Object> imageTagAttr = DomHelperFunctions
									    .getAttributes((Element) animateElementChilds
									        .get(li));

									if (imageTagAttr.get(ThemeTag.ATTR_STATUS)
									    .equals(ThemeTag.ATTR_VALUE_ACTUAL)) {
										ThemeGraphic tGraphic1 = new ThemeGraphic(
										    entity);
										ImageLayer imageLayer1 = new ImageLayer(
										    tGraphic1);
										tGraphic1.setAttributes(attr);
										int uid = li + 1;
										tGraphic1.setAttribute(
										    ThemeTag.UNIQUE_ID, uid + "");
										imageLayer1.setAttributes(imageTagAttr);
										tGraphic1.setImageLayers(imageLayer1);
										atg.addThemeGraphic(tGraphic1);

										addToUsedImagesMap(imageTagAttr);
									}
								}
							}
						}
					}
				}

				if (!entityType.equals(ThemeTag.ELEMENT_BMPANIM)) {
					if (entity instanceof com.nokia.tools.platform.theme.Element)
						((com.nokia.tools.platform.theme.Element) entity)
						    .setThemeGraphic(tGraphic, !isModel);
					else
						((com.nokia.tools.platform.theme.Part) entity)
						    .setThemeGraphic(tGraphic, !isModel);
				}
			} else {
				continue;
			}
		}

		if ((entityType.equals(ThemeTag.ELEMENT_BMPANIM)) && (atg != null)) {
			if (entity instanceof com.nokia.tools.platform.theme.Element)
				((com.nokia.tools.platform.theme.Element) entity)
				    .setAnimateGraphic(atg);
			else
				((com.nokia.tools.platform.theme.Part) entity)
				    .setAnimateGraphic(atg);
		}

		if (imageList.getLength() > 0)
			return true;
		else
			return false;
	}

	private void addToUsedImagesMap(Map attr) {
		if (!isModel) {
			if (attr.get(ThemeTag.FILE_NAME) != null)
				usedFiles.put(
				    (new File(attr.get(ThemeTag.FILE_NAME).toString()))
				        .getName(), "0");
			if (attr.get(ThemeTag.ATTR_SOFTMASK) != null)
				usedFiles.put((new File(attr.get(ThemeTag.ATTR_SOFTMASK)
				    .toString())).getName(), "0");
			if (attr.get(ThemeTag.ATTR_HARDMASK) != null)
				usedFiles.put((new File(attr.get(ThemeTag.ATTR_HARDMASK)
				    .toString())).getName(), "0");
		}
	}

	private void addFullPathIfPhoneModel(Map<Object, Object> attr) {
		if (!theme.isModel()) {
			return;
		}
		String path = theme.getThemeDir();
		if (attr.containsKey(ThemeTag.FILE_NAME)) {
			String filepath = path + File.separator
			    + attr.get(ThemeTag.FILE_NAME);
			attr.put(ThemeTag.FILE_NAME, filepath);
		}
		if (attr.containsKey(ThemeTag.ATTR_SOFTMASK)) {
			String softpath = path + File.separator
			    + attr.get(ThemeTag.ATTR_SOFTMASK);
			attr.put(ThemeTag.ATTR_SOFTMASK, softpath);
		}
		if (attr.containsKey(ThemeTag.ATTR_HARDMASK)) {
			String hardpath = path + File.separator
			    + attr.get(ThemeTag.ATTR_HARDMASK);
			attr.put(ThemeTag.ATTR_HARDMASK, hardpath);
		}
	}

	/**
	 * Sets the refer tag details
	 * 
	 * @param entity The object in which the entity details will be stored
	 * @param node The node containing the entity information
	 * @return true if the tag details has been found
	 */
	private boolean setReferDetails(ThemeBasicData entity, Element node) {

		// Check the class type. Only Element or Part must be processed
		if (!(entity instanceof com.nokia.tools.platform.theme.Element)
		    && !(entity instanceof com.nokia.tools.platform.theme.Part)) {
			return false; // neither it is element or part.So simply return
		}

		// The function has to get all the childrens named 'image' in the given
		// 'node'
		

		NodeList referList = node.getChildNodes();

		// There should be only one refer attribute
		// The DTD will take care of such errors

		for (int i = 0; i < referList.getLength(); i++) {

			// check if it is an element node
			short nodeType = referList.item(i).getNodeType();
			if (nodeType != Element.ELEMENT_NODE)
				continue;

			// check if it is a image tag child (could be parts in case of
			// elements)
			Element refer = (Element) referList.item(i);
			String tagName = refer.getTagName();
			if (!tagName.equalsIgnoreCase(ThemeTag.ELEMENT_REFER))
				continue;

			Map attributes = DomHelperFunctions.getAttributes(refer);
			String referId = (String) attributes.get(ThemeTag.ATTR_ID);
			String referName = (String) attributes.get(ThemeTag.ATTR_NAME);

			if (referId == null && referName == null) {
				return false;
			}

			if (entity instanceof com.nokia.tools.platform.theme.Element) {
				if (referId != null) {
					((com.nokia.tools.platform.theme.Element) entity)
					    .setAttribute(ThemeTag.ATTR_REFER_ID, referId
					        .toLowerCase().trim());
				} else if (referName != null) {
					((com.nokia.tools.platform.theme.Element) entity)
					    .setAttribute(ThemeTag.ATTR_REFER_NAME, referName
					        .toLowerCase().trim());
				}

			} else {
				if (referId != null) {
					((com.nokia.tools.platform.theme.Part) entity)
					    .setAttribute(ThemeTag.ATTR_REFER_ID, referId
					        .toLowerCase().trim());
				} else if (referName != null) {
					((com.nokia.tools.platform.theme.Part) entity)
					    .setAttribute(ThemeTag.ATTR_REFER_NAME, referName
					        .toLowerCase().trim());
				}
			}
			// there should be only one refer tag ... so break after setting it
			// for the first time
			return true;
		}

		// reaches here only when no 'refer' tag has been found
		return false;
	}
} // end of the ThemePhoneParser class

