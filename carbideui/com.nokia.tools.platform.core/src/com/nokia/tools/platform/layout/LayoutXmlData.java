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
package com.nokia.tools.platform.layout;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.ISafeRunnable;

import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.extension.ILayoutVariantDescriptor;
import com.nokia.tools.resource.util.DebugHelper;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.DebugHelper.SilentRunnable;

class LayoutXmlData {

	/*
	 * The layout xml parser will use the output xml files coming from the
	 * layout tool. The idea of using DOM is to make sure that we have the
	 * layout data (data of a component in releation to its parent) at all
	 * times. And since it is in DOM i am not storing the layout value any-
	 * where else. Hidden features: 1) The parser when reading the file will
	 * always look for the screen components width and height. It uses the
	 * screen dimensions for cacheing the data. 2) The document objects
	 * containing the DOM data from the layout xml files can be dropped
	 * internally. When a request is done to the object for a layout components
	 * details - it will check if the dom object is available, if not available
	 * it will reconstruct it.
	 */

	private LayoutInfo info;
	private Map<String, CompactElement> compDataNodes;
	private Map<String, String> compIdToName;
	private Map<String, CompactElement> attrSetNodes;
	private Map<String, String> layoutParameters;

	protected LayoutXmlData(LayoutInfo info) throws LayoutException {
		this.info = info;

		buildDocument();
	}

	/**
	 * @return the info
	 */
	public LayoutInfo getInfo() {
		return info;
	}

	/**
	 * Builds the document object from the xml file specified.
	 * 
	 * @throws LayoutException
	 */
	private void buildDocument() throws LayoutException {
		ILayoutVariantDescriptor variant = info.getVariantDescriptor();
		if (variant == null) {
			throw new LayoutException("No variant definition found for " + info);
		}

		// Initialize the local table structures
		compDataNodes = new HashMap<String, CompactElement>();
		compIdToName = new HashMap<String, String>();
		attrSetNodes = new HashMap<String, CompactElement>();

		try {
			URL[] compUrls = variant.getComponentPaths();
			URL[] attrUrls = variant.getAttributePaths();

			SAXParserFactory fac = SAXParserFactory.newInstance();
			SAXParser parser = fac.newSAXParser();

			CompactElementHandler handler = new CompactElementHandler();
			for (URL url : compUrls) {
				buildComponentDataMap(parse(url, parser, handler));
			}
			for (URL url : attrUrls) {
				buildAttributeDataMap(parse(url, parser, handler));
			}
		} catch (Throwable e) {
			PlatformCorePlugin.error(e);
			throw new LayoutException(e);
		}
	}

	private CompactElement parse(final URL url, final SAXParser parser,
			final CompactElementHandler handler) throws Exception {
		handler.reset();

		if (DebugHelper.debugParser()) {
			DebugHelper.debug(this, "parsing " + url);
		}

		ISafeRunnable job = new SilentRunnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.ISafeRunnable#run()
			 */
			public void run() throws Exception {
				InputStream in = null;
				try {
					in = new BufferedInputStream(url.openStream(),
							FileUtils.BUF_SIZE);
					parser.parse(in, handler);
				} finally {
					if (in != null) {
						in.close();
					}
				}
			}
		};
		if (DebugHelper.debugPerformance()) {
			DebugHelper.debugTime(this, "parsing " + url, job);
		} else {
			job.run();
		}

		return handler.getRoot();
	}

	private void buildComponentDataMap(CompactElement compDataDocument)
			throws LayoutException {
		if (layoutParameters == null || layoutParameters.isEmpty()) {
			// Read and get the root element parameters
			layoutParameters = compDataDocument.getAttributes();
		}

		// Process component data document
		List<CompactElement> elements = compDataDocument
				.getElementsByTagName(LayoutConstants.XMLTAG_COMPONENT);

		for (CompactElement element : elements) {
			String layoutCompName = element
					.getAttribute(LayoutConstants.ATTR_COMPONENT_NAME);
			String layoutCompId = element
					.getAttribute(LayoutConstants.ATTR_COMPONENT_ID);

			compDataNodes.put(layoutCompName, element);
			compIdToName.put(layoutCompId, layoutCompName);
		}
	}

	private void buildAttributeDataMap(CompactElement attrDataDocument) {
		// Process attributes document
		List<CompactElement> alist = attrDataDocument
				.getElementsByTagName(LayoutConstants.XMLTAG_ATTRIBUTE_SET);

		for (CompactElement element : alist) {
			String attrSetName = element
					.getAttribute(LayoutConstants.ATTR_ATTRSET_NAME);

			String compId = element
					.getAttribute(LayoutConstants.ATTR_ATTR_COMPID);

			attrSetNodes.put(compId + "_" + attrSetName, element);
		}
	}

	/**
	 * Fetches the component data (component data, attributes data) for the
	 * requested component
	 * 
	 * @param componentName The layout component name
	 * @return A hashmap containing the component data - Keys used are
	 *         COMPONENT_DATA, ATTRIBUTE_DATA
	 * @throws LayoutException
	 */
	protected Map<String, Object> getComponentData(String componentName)
			throws LayoutException {

		// Get the component layout data
		CompactElement layoutDataNode = getComponentSimpleElement(componentName);
		if (layoutDataNode == null)
			// data for the component is not present in this xml file.
			return null;

		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put(LayoutConstants.KEY_LAYOUT_XML_NODE, layoutDataNode);

		// Get the attribute sets associated with the component
		Map<Object, Object> attrSetsForComp = LayoutXmlDataHelperFunctions
				.readAttributeSetIds(dataMap);
		if (attrSetsForComp != null) {
			String[] varietyKey = (String[]) attrSetsForComp.keySet().toArray(
					new String[1]);
			Map<Object, Object> attrSetSimpleElementsForComp = new Hashtable<Object, Object>();

			for (int i = 0; i < varietyKey.length; i++) {
				List attrSetIds = (List) attrSetsForComp.get(varietyKey[i]);
				List<Object> attrSetSimpleElements = getAttributeSetSimpleElement(attrSetIds);
				attrSetSimpleElementsForComp.put(varietyKey[i],
						attrSetSimpleElements);
			}

			dataMap.put(LayoutConstants.KEY_ATTR_SET_XML_NODE,
					attrSetSimpleElementsForComp);
		}

		return dataMap;
	}

	/**
	 * @param compId The string representing the component id.
	 * @return The component name of the component whose id is given by compId
	 * @throws LayoutException
	 */
	protected String getComponentName(String compId) throws LayoutException {
		return (String) compIdToName.get(compId);
	}

	/**
	 * @param componentName The name of the component file as specified in the
	 *            layout files.
	 * @return The xml node object containing the details for the required
	 *         component. The object returned by this function must never be put
	 *         in any hash map or cache (it will prevent the document from being
	 *         garbage collected)
	 */
	protected CompactElement getComponentSimpleElement(String componentName)
			throws LayoutException {
		return (CompactElement) compDataNodes.get(componentName);
	}

	/**
	 * @param attrSetIds A list containing the ids of the required attribute
	 *            sets.
	 * @return A list containing the requested attribute set nodes.
	 */
	protected List<Object> getAttributeSetSimpleElement(List attrSetIds)
			throws LayoutException {
		List<Object> reqList = new ArrayList<Object>();

		for (int i = 0; i < attrSetIds.size(); i++) {
			String attrSetId = (String) attrSetIds.get(i);
			CompactElement n = (CompactElement) attrSetNodes.get(attrSetId);
			reqList.add(n);
		}

		return reqList;
	}

	/**
	 * @return The layout set name associated with the data. (If data is not
	 *         built - it returns the name of the zip file; otherwise it returns
	 *         the layoutset name read from the xml data -- both should match as
	 *         per the specification of the output xml package of the layout
	 *         tool)
	 */
	protected String getLayoutSetName() {
		return getParameter(LayoutConstants.ATTR_LAYOUT_NAME);
	}

	/**
	 * @param attrName The name of the attribute whose value is required
	 * @return The value of the attribute from the layout data (attribute of the
	 *         root node)
	 */
	protected String getParameter(String attrName) {
		return (String) layoutParameters.get(attrName);
	}

	public float getLayoutPPI() {

		float result = 0;

		String strPPI = getParameter(LayoutConstants.ATTR_PPI);

		if (strPPI != null && strPPI.length() > 0) {
			try {
				result = Float.parseFloat(strPPI);
			} catch (NumberFormatException exNum) {
				PlatformCorePlugin.error(exNum);
			}
		}
		return result;
	}

	public Collection<CompactElement> getAllComponents() {
		return compDataNodes.values();
	}
}
