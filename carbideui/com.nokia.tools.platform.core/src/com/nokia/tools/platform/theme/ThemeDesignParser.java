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
package com.nokia.tools.platform.theme;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.resource.util.DebugHelper;

/**
 * The class to parse the default skin design details xml file
 * 
 * 
 */

public class ThemeDesignParser {

	/**
	 * The instance of the ElementCreator to be used for the parsing purpose.
	 * Set to DefaultElementCreator instance by default.
	 */
	private static ElementCreator elementCreator = DefaultElementCreator.getInstance();

	/**
	 * Retrieves all the required data from the configured file
	 * 
	 * @param skinDetails The Theme object to which is the retrieved data is to
	 *            put into
	 * @throws ThemeAppParserException If not able to successfully parse the xml
	 *             file.
	 */
	
	public static void parse(Theme skinDetails, URL url) throws ThemeException {
		parse(skinDetails, url, null);
	}
	
	/**
	 * Retrieves all the required data from the configured file.
	 * This method will make use of the passed ElementCreator to create the instances.
	 * 
	 * 
	 * @param skinDetails The Theme object to which is the retrieved data is to
	 *            put into
	 * @throws ThemeAppParserException If not able to successfully parse the xml
	 *             file.
	 */
	public static void parse(Theme skinDetails, URL url, ElementCreator elementCreator) throws ThemeException {
		
		if(elementCreator != null){
			ThemeDesignParser.elementCreator = elementCreator;
		}
		else{
			ThemeDesignParser.elementCreator = DefaultElementCreator.getInstance();
		}
		
		ToolParser parser = new ToolParser(skinDetails, url);

		// this.series60Skin = skinDetails;
		parser.parse();
		try {
			updateLinks(skinDetails);
			updateToolBox(skinDetails);
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
			throw new ThemeException("happens here : " + e.getMessage());
		}
	}

	
	/**
	 * To update the links
	 * 
	 * @param skinDetails The Theme object to which is the retrieved data is to
	 *            put into
	 * @throws ThemeAppParserException If not able to successfully set the links
	 */
	static void updateLinks(Theme skinDetails) throws ThemeException {

		ThemeBasicData link = null;
		// for loop for all the tasks
		List tasks = skinDetails.getChildren();
		// we here resolve only duplicate parts, this map is used to improve the
		// performance because getAllElements() is not cheap
		Map<String, ThemeBasicData> partMap = new HashMap<String, ThemeBasicData>();
		if (tasks != null) {

			for (int i = 0; i < tasks.size(); i++) {

				ThemeBasicData task = (ThemeBasicData) tasks.get(i);

				if (task.getAttributeValue(ThemeTag.ATTR_CHILD_NAME) != null) {

					link = skinDetails.getTask(task
					    .getAttributeValue(ThemeTag.ATTR_CHILD_NAME));

					if (link == null) {
						throw new ThemeException(
						    "The link is not found for the task : "
						        + task
						            .getAttributeValue(ThemeTag.ATTR_CHILD_NAME));
					}

					task.getAttribute().remove(ThemeTag.ATTR_CHILD_NAME);
					task.setLink(link);

					link = null;
					continue;
				}

				// for loop for getting the component group for each task
				List componentGroups = task.getChildren();

				if (componentGroups != null) {
					for (int j = 0; j < componentGroups.size(); j++) {

						ThemeBasicData compG = (ThemeBasicData) componentGroups
						    .get(j);

						if (compG.getAttributeValue(ThemeTag.ATTR_CHILD_NAME) != null) {

							link = skinDetails.getComponentGroup(compG
							    .getAttributeValue(ThemeTag.ATTR_CHILD_NAME));
							if (link == null) {
								throw new ThemeException(
								    "The link is not found for the componentGroup : "
								        + compG
								            .getAttributeValue(ThemeTag.ATTR_CHILD_NAME));
							}
							compG.getAttribute().remove(
							    ThemeTag.ATTR_CHILD_NAME);
							compG.setLink(link);
							link = null;

							continue;
						}

						// for loop for getting the component of each group
						List components = compG.getChildren();

						if (components != null) {
							for (int k = 0; k < components.size(); k++) {

								ThemeBasicData comp = (ThemeBasicData) components
								    .get(k);

								if (comp
								    .getAttributeValue(ThemeTag.ATTR_CHILD_NAME) != null) {

									link = skinDetails
									    .getComponent(comp
									        .getAttributeValue(ThemeTag.ATTR_CHILD_NAME));
									if (link == null) {
										throw new ThemeException(
										    "The link is not found for the component : "
										        + comp
										            .getAttributeValue(ThemeTag.ATTR_CHILD_NAME));
									}
									comp.getAttribute().remove(
									    ThemeTag.ATTR_CHILD_NAME);
									comp.setLink(link);
									link = null;

									continue;
								}

								// for loop for getting the elements
								List elements = comp.getChildren();

								if (elements != null) {
									for (int l = 0; l < elements.size(); l++) {

										ThemeBasicData elem = (ThemeBasicData) elements
										    .get(l);

										String identifier = elem
										    .getAttributeValue(ThemeTag.ATTR_CHILD_ID);
										identifier = (identifier == null) ? elem
										    .getAttributeValue(ThemeTag.ATTR_CHILD_NAME)
										    : identifier;
										if (identifier != null) {
											link = skinDetails
											    .getSkinnableEntity(identifier);

											if (link == null) {
												throw new ThemeException(
												    "The link is not found for the element : "
												        + identifier);
											}

											if (!(link instanceof Element)) {
												throw new ThemeException(
												    "The link is found for the element : "
												        + identifier
												        + " is not of type Element");
											}

											elem.getAttribute().remove(
											    ThemeTag.ATTR_CHILD_NAME);
											elem.getAttribute().remove(
											    ThemeTag.ATTR_CHILD_ID);
											elem.setLink(link);
											link = null;
											continue;
										}

										// for loop for getting the elements
										List parts = elem.getChildren();

										if (parts != null) {
											for (int m = 0; m < parts.size(); m++) {

												ThemeBasicData part = (ThemeBasicData) parts
												    .get(m);
												String id = part
												    .getIdentifier();
												// part is not unique in the
												// tree
												ThemeBasicData existingPart = partMap
												    .get(id);
												if (existingPart != null) {
													part.setLink(existingPart);
												} else {
													partMap.put(id, part);
												}

												identifier = part
												    .getAttributeValue(ThemeTag.ATTR_CHILD_ID);

												if (identifier != null) {
													link = skinDetails
													    .getSkinnableEntity(identifier);

													if (link == null) {
														throw new ThemeException(
														    "The link is not found for the part : "
														        + identifier);
													}
													if (!(link instanceof Part)) {
														throw new ThemeException(
														    "The link is found for the part : "
														        + identifier
														        + " is not of type Part");
													}

													part.getAttribute().remove(
													    ThemeTag.ATTR_CHILD_ID);
													part.setLink(link);
													link = null;
													continue;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * To update the toolbox
	 * 
	 * @param skinDetails The Theme object to which is the retrieved data is to
	 *            put into
	 * @throws ThemeAppParserException If not able to successfully set the
	 *             toolbox
	 */

	static void updateToolBox(Theme skinDetails) throws ThemeException {

		// for loop for all the tasks
		List tasks = skinDetails.getChildren();

		if (tasks != null) {
			for (int i = 0; i < tasks.size(); i++) {

				ThemeBasicData task = (ThemeBasicData) tasks.get(i);

				task.forcedSetToolBox();

				// for loop for getting the component group for each task
				List componentGroups = task.getChildren();

				if (componentGroups != null) {
					for (int j = 0; j < componentGroups.size(); j++) {

						ThemeBasicData compG = (ThemeBasicData) componentGroups
						    .get(j);

						compG.forcedSetToolBox();

						// for loop for getting the component of each group
						List components = compG.getChildren();

						if (components != null) {
							for (int k = 0; k < components.size(); k++) {

								ThemeBasicData comp = (ThemeBasicData) components
								    .get(k);

								comp.forcedSetToolBox();

								// for loop for getting the elements
								List elements = comp.getChildren();

								if (elements != null) {
									for (int l = 0; l < elements.size(); l++) {
										ThemeBasicData elem = (ThemeBasicData) elements
										    .get(l);

										elem.forcedSetToolBox();

										// for loop for getting the elements
										List parts = elem.getChildren();

										if (parts != null) {
											for (int m = 0; m < parts.size(); m++) {
												ThemeBasicData part = (ThemeBasicData) parts
												    .get(m);

												part.forcedSetToolBox();
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}


	/**
	 * The class parses the default skin design details xml file
	 */

	static class ToolParser
	    extends DefaultHandler {

		/**
		 * Holds the Theme object
		 */
		private Theme skinDetails = null;

		private URL url;

//		List<Object> listOfthirdPartyicons = new ArrayList<Object>();

		/**
		 * Holds a Task object
		 */
		private Task task = null;

		/**
		 * Holds a ComponentGroup object
		 */
		private ComponentGroup componentGroup = null;

		/**
		 * Holds a Component object
		 */
		private Component component = null;

		private List<Object> propertiesList = null;

		private List<Object> designrectanglesList = null;

		/**
		 * Holds an Element object
		 */
		private Element element = null;

		/**
		 * Holds a Part object
		 */
		private Part part = null;

		/**
		 * Holds a ToolBox object
		 */
		private ToolBox toolBox = null;

		// private Properties property = null;

		private String phoneName = null;

		private HashMap<String, HashMap<Object, Object>> lineColours = null;

		private HashMap<Object, Object> dimensions = null;

		public ToolParser(Theme skinDetails, URL url) {
			this.skinDetails = skinDetails;
			this.url = url;
		}

		/**
		 * This method initializes the SAX parser and invoke the parse on the
		 * given xml file
		 * 
		 * @param skinDetails Theme object in which the parsing information is
		 *            added
		 * @param fileName String name of the xml to be parsed
		 */
		public void parse() throws ThemeException {
			InputStream in = null;
			try {
				// Use the validating parser
				SAXParserFactory factory = SAXParserFactory.newInstance();

				factory.setValidating(true);

				// Parse the input
				SAXParser saxParser = factory.newSAXParser();

				if (DebugHelper.debugParser()) {
					DebugHelper.debug(this, "parsing: " + url);
				}

				in = url.openStream();
				saxParser.parse(in, this);
			} catch (Throwable e) {
				PlatformCorePlugin.error(e);
				throw new ThemeException("Unknown Exception :" + e.getMessage());
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (Exception e) {
				}
			}
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
			return new InputSource(getClass().getResourceAsStream(
			    IThemeManager.DTD_FOLDER + new File(systemId).getName()));
		}

		/**
		 * SAX DocumentHandler method. This method identifies the start tag of
		 * an element and creates an object for it
		 */

		public void startElement(String namespaceURI, String lName, // local
		    // name
		    String qName, // qualified name
		    Attributes attrs) throws SAXException {

			String eName = lName; // element name

			if ("".equals(eName))
				eName = qName; // namespaceAware = false

			HashMap<Object, Object> mapAttr = new HashMap<Object, Object>();

			mapAttr = setAttributes(attrs);

			String attrName = (String) mapAttr.get(ThemeTag.ATTR_NAME);

			// For DrawLinessssssss

			if (eName.equals(ThemeTag.SKN_TAG_DRAWLINES)) {
				lineColours = new HashMap<String, HashMap<Object, Object>>();
			}
			if (eName.equals(ThemeTag.ATTR_ENTITY)) {
				// System.out.println("ATTR : " + mapAttr);
				lineColours.put(mapAttr.get(ThemeTag.ATTR_ID).toString(),
				    mapAttr);
			}
			if (eName.equals(ThemeTag.ATTR_DIMENSIONS)) {
				dimensions = new HashMap<Object, Object>();
			}
			if (eName.equals(ThemeTag.ATTR_DIMENSION)) {
				dimensions.put(mapAttr.get(ThemeTag.ATTR_ID), "0");
			}

			if (eName.equals(ThemeTag.ELEMENT_TASK)) {
				task = new Task(attrName);
				task.setAttribute(mapAttr);
			}

			if (eName.equals(ThemeTag.ELEMENT_COMPONENT_GROUP)) {
				componentGroup = new ComponentGroup(attrName);
				componentGroup.setAttribute(mapAttr);
			}

			if (eName.equals(ThemeTag.ELEMENT_COMPONENT)) {
				component = new Component(attrName);
				component.setAttribute(mapAttr);
			}

			if (eName.equals(ThemeTag.ELEMENT_ELEMENT)) {
				element = elementCreator.createElement(attrName);

				// the above section of code commented by sankarm. the
				// background
				// info
				// for all the nodes(from task to part) is available as picture
				// attribute.
				// and setting the info is handled in setAttribute(Map) of
				// ThemeBasicData
				element.setAttribute(mapAttr);
//				if (url.getFile().indexOf("icons.xml") != -1) {
//					listOfthirdPartyicons.add(mapAttr);
//					// element.setThirdPartyIcon(mapAttr);
//					// System.out.println(element.isShown());
//					// System.out.println("Third part icon list" + mapAttr);
//				}
				if (mapAttr.containsKey(ThemeTag.ATTR_MASK)) {
					HashMap<Object, Object> default_property = new HashMap<Object, Object>();
					default_property.put(phoneName, (String) mapAttr
					    .get(ThemeTag.ATTR_MASK));
					element.setDefaultProperties(default_property);
				} else {

				}
			}

			if (eName.equals(ThemeTag.ELEMENT_PART)) {
				part = new Part(attrName);
				part.setAttribute(mapAttr);
			}

			// when the refer tag is come across...
			// the info is set in the respective element/part
			if (eName.equals(ThemeTag.ELEMENT_REFER)) {

				String referId = (String) mapAttr.get(ThemeTag.ATTR_ID);
				String referName = (String) mapAttr.get(ThemeTag.ATTR_NAME);

				if (referId != null || referName != null) {

					if (part != null) {
						if (referId != null) {
							part.setAttribute(ThemeTag.ATTR_REFER_ID, referId
							    .toLowerCase().trim());
						} else if (referName != null) {
							part.setAttribute(ThemeTag.ATTR_REFER_NAME,
							    referName.toLowerCase().trim());
						}
					} else if (element != null) {
						if (referId != null) {
							element.setAttribute(ThemeTag.ATTR_REFER_ID,
							    referId.toLowerCase().trim());
						} else if (referName != null) {
							element.setAttribute(ThemeTag.ATTR_REFER_NAME,
							    referName.toLowerCase().trim());
						}
					}
				}
			}

			if (eName.equals(ThemeTag.ELEMENT_TOOLBOX)) {

				toolBox = new ToolBox();
				toolBox.setAttribute(mapAttr);
			}
			if (eName.equals(ThemeTag.ELEMENT_PROPERTIES)) {
				propertiesList = new ArrayList<Object>();
			}

			if (eName.equals(ThemeTag.ELEMENT_DESIGNRECTANGLES)) {
				designrectanglesList = new ArrayList<Object>();
			}

			if (eName.equals(ThemeTag.ELEMENT_PROPERTY)) {
				propertiesList.add(mapAttr);
			}

			if (eName.equals(ThemeTag.ELEMENT_DESIGNRECTANGLE)) {
				designrectanglesList.add(mapAttr.get(ThemeTag.ATTR_NAME));
			}

			if (eName.equals(ThemeTag.ELEMENT_CHILD)) {

				if (attrs == null) {
					throw new SAXException(
					    " The element \"child\" should have name or id ");
				}

				if (attrName == null)
					attrName = "link";

				if (element != null) {
					part = new Part(attrName);
					part.setAttribute(mapAttr);
				} else if (component != null) {
					element = new Element(attrName);
					element.setAttribute(mapAttr);
				} else if (componentGroup != null) {
					component = new Component(attrName);
					component.setAttribute(mapAttr);
				} else if (task != null) {
					componentGroup = new ComponentGroup(attrName);
					componentGroup.setAttribute(mapAttr);
				} else {
					task = new Task(attrName);
					task.setAttribute(mapAttr);
				}
			}
		}

		/**
		 * Method to get the Attributes of an element in xml file and put in a
		 * Map
		 * 
		 * @param attrs Attributes of the element in xml file
		 * @return Map map which contains the attributes of an element in xml
		 *         file
		 */
		protected HashMap<Object, Object> setAttributes(Attributes attrs) {
			HashMap<Object, Object> mapAttr = new HashMap<Object, Object>();

			if (attrs != null) {

				String aName = null;

				for (int i = 0; i < attrs.getLength(); i++) {
					aName = attrs.getLocalName(i); // Attr name
					if ("".equals(aName))
						aName = attrs.getQName(i);

					mapAttr.put(aName.toLowerCase(), (attrs.getValue(aName))
					    .trim());
				}
			}
			return mapAttr;
		}

		/**
		 * SAX DocumentHandler method. This method identifies the end tag of an
		 * element and adds the corresponding object to its parent. If the end
		 * tag is for toolbox, it sets the toolbox to the corresponding object
		 */
		public void endElement(String namespaceURI, String sName, // simple
		    // name
		    String qName // qualified name
		) throws SAXException {

			try {

				// // / For Line Colours (Themesettings)
				if (qName.equals(ThemeTag.SKN_TAG_DRAWLINES)) {
					skinDetails.setSkinSettings(lineColours);
				}
				// For dimension
				if (qName.equals(ThemeTag.ATTR_DIMENSIONS)) {
					skinDetails.setDimensions(dimensions);
				}

				if (qName.equals(ThemeTag.ELEMENT_TOOLBOX)) {
					// sets the toolbox to the corresponding node
					if (part != null) {
						part.setToolBox(toolBox);
					} else if (element != null) {
						element.setToolBox(toolBox);
					} else if (component != null) {
						component.setToolBox(toolBox);
					} else if (componentGroup != null) {
						componentGroup.setToolBox(toolBox);
					} else if (task != null) {
						task.setToolBox(toolBox);
					}
					toolBox = null;
				}

				// to set the child to its respective parent
				if (qName.equals(ThemeTag.ELEMENT_CHILD)) {

					if (part != null) {
						element.addChild(part);
						part = null;
					} else if (element != null) {
						component.addChild(element);
						element = null;
					} else if (component != null) {
						componentGroup.addChild(component);
						component = null;
					} else if (componentGroup != null) {
						task.addChild(componentGroup);
						componentGroup = null;
					} else if (task != null) {
						skinDetails.addChild(task);
						task = null;
					}
				}

				// adds a node to its parent
				if (qName.equals(ThemeTag.ELEMENT_PART)) {
					element.addChild(part);
					part = null;
				}
				if (qName.equals(ThemeTag.ELEMENT_ELEMENT)) {
					component.addChild(element);
					element = null;
				}
				if (qName.equals(ThemeTag.ELEMENT_COMPONENT)) {
					componentGroup.addChild(component);
					component = null;
				}
				if (qName.equals(ThemeTag.ELEMENT_COMPONENT_GROUP)) {
					task.addChild(componentGroup);
					componentGroup = null;
				}
				if (qName.equals(ThemeTag.ELEMENT_TASK)) {

					skinDetails.addChild(task);
					task = null;
				}

				if (qName.equals(ThemeTag.ELEMENT_PROPERTIES)) {

					if (part != null) {
						part.setProperties(propertiesList);
					} else if (element != null) {
						element.setProperties(propertiesList);
					} else if (component != null) {
						component.setProperties(propertiesList);
					} else if (componentGroup != null) {
						componentGroup.setProperties(propertiesList);
					} else if (task != null) {
						task.setProperties(propertiesList);
					}
					propertiesList = null;
				}

				if (qName.equals(ThemeTag.ELEMENT_DESIGNRECTANGLES)) {

					if (part != null) {
						part.setDesignRectangles(designrectanglesList);
					} else if (element != null) {
						element.setDesignRectangles(designrectanglesList);
					} else if (component != null) {
						component.setDesignRectangles(designrectanglesList);
					} else if (componentGroup != null) {
						componentGroup
						    .setDesignRectangles(designrectanglesList);
					} else if (task != null) {
						task.setDesignRectangles(designrectanglesList);
					}
					designrectanglesList = null;
				}
//				if ((listOfthirdPartyicons != null)
//				    && (listOfthirdPartyicons.size() > 0)) {
//					skinDetails.setThirdPartyIcon(listOfthirdPartyicons);
//				}
			}

			catch (ThemeException e) {
				// e.printStackTrace();
				throw new SAXException(e.getMessage());
			}
		}

		// ===========================================================
		// SAX ErrorHandler methods
		// ===========================================================

		/**
		 * Method to handle validation error
		 * 
		 * @param
		 * @throws SAXParseException If there is validation error while parsing
		 *             the xml file
		 */
		public void error(SAXParseException e) throws SAXParseException {
			throw e;
		}

	}


	static class PropertiesTag {

		Map attributes = null;

		public void setAttributes(Map attributes) {
			this.attributes = attributes;
		}

		public Map getAttributes() {
			return attributes;
		}
	}
}
