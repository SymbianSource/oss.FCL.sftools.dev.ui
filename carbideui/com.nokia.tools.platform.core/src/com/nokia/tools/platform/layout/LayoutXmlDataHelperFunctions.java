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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 		   Helper functions to process layout xml data. The class
 *         assumes that working layout files are only supplied to the tool. The
 *         class does not do checks on the xml data.
 */
class LayoutXmlDataHelperFunctions {

	/**
	 * Reads the ParentInfo block in the component data layout xml node.
	 * 
	 * @param componentData The map containing all the xml nodes for the given
	 *            component.
	 * @return A map containing the parent details. (Key used is variety no)
	 * @throws LayoutException
	 */
	static Map readParentInfo(Map<String, Object> componentData)
			throws LayoutException {

		if (componentData == null)
			throw new LayoutException("Invalid input data");

		// Check if the data has already been processed
		if (componentData.containsKey(LayoutConstants.KEY_PARENT_DATA)) {
			Map parentData = (Map) componentData
					.get(LayoutConstants.KEY_PARENT_DATA);
			return parentData;
		}

		// At this point we are sure that this is the first time the
		// function is being called.

		CompactElement layoutData = (CompactElement) componentData
				.get(LayoutConstants.KEY_LAYOUT_XML_NODE);
		if (layoutData == null)
			throw new LayoutException("Invalid layout data in input");

		List<CompactElement> parentGroupNode = layoutData
				.getElementsByTagName(LayoutConstants.XMLTAG_PARENT_INFO);
		if ((parentGroupNode == null) || (parentGroupNode.size() != 1)) {
			throw new LayoutException("Invalid parent info in input data");
		}

		CompactElement parentNode = (CompactElement) parentGroupNode.get(0);
		Map<String, Object> parentInfo = new HashMap<String, Object>();

		// Process each <option> tag
		List<CompactElement> optionList = parentNode
				.getElementsByTagName(LayoutConstants.XMLTAG_OPTION);
		for (int i = 0; i < optionList.size(); i++) {
			CompactElement nextOption = (CompactElement) optionList.get(i);
			String varietyNo = nextOption
					.getAttribute(LayoutConstants.ATTR_VARIETY);

			// Get the <parent> tag data
			List<CompactElement> parentList = nextOption
					.getElementsByTagName(LayoutConstants.XMLTAG_PARENT);

			List<Object> parentDetailsList = new ArrayList<Object>();

			for (int j = 0; j < parentList.size(); j++) {
				Map parDetails = parentList.get(j).getAttributes();
				parentDetailsList.add(parDetails);
			}

			parentInfo.put(varietyNo, parentDetailsList);
		}

		// Add it to the component data structure
		componentData.put(LayoutConstants.KEY_PARENT_DATA, parentInfo);

		return parentInfo;
	}

	/**
	 * Reads the attribute information from the AttributeInfo block
	 * 
	 * @param componentData The map containing all the xml nodes for the given
	 *            component.
	 * @return A map containing the attribute details (Key used is variety no)
	 * @throws LayoutException
	 */
	private static Map<Object, Object> readAttributeInfo(Map componentData)
			throws LayoutException {

		if (componentData == null)
			throw new LayoutException("Invalid input data");

		CompactElement layoutData = (CompactElement) componentData
				.get(LayoutConstants.KEY_LAYOUT_XML_NODE);
		if (layoutData == null)
			throw new LayoutException("Invalid attribute data in input");

		List<CompactElement> attrInfoGroupNode = layoutData
				.getElementsByTagName(LayoutConstants.XMLTAG_ATTRIBUTE_INFO);
		if ((attrInfoGroupNode == null) || (attrInfoGroupNode.size() != 1)) {
			throw new LayoutException("Invalid attribute info in input data");
		}

		CompactElement attrInfoNode = (CompactElement) attrInfoGroupNode.get(0);

		Map<Object, Object> attrSetInfo = new HashMap<Object, Object>();

		// Process each <option> tag
		List<CompactElement> optionList = attrInfoNode
				.getElementsByTagName(LayoutConstants.XMLTAG_OPTION);
		// System.out.println ("OPTION LIST ---------------->" +
		// optionList.getLength());

		for (int i = 0; i < optionList.size(); i++) {
			CompactElement nextOption = (CompactElement) optionList.get(i);
			String varietyNo = nextOption
					.getAttribute(LayoutConstants.ATTR_VARIETY);

			// Get the <attributeset> tag data
			List<CompactElement> attrSetList = nextOption
					.getElementsByTagName(LayoutConstants.XMLTAG_ATTRIBUTE_SET);
			if (attrSetList.size() > 0) {
				List<Object> attrSetDetailsList = new ArrayList<Object>();

				for (int j = 0; j < attrSetList.size(); j++) {
					Map attrDetails = attrSetList.get(j).getAttributes();
					attrSetDetailsList.add(attrDetails);
				}

				// System.out.println ("ADDING DATA FOR VARIETY
				// ---------------->" + varietyNo);
				attrSetInfo.put(varietyNo, attrSetDetailsList);
			}
		}

		if (attrSetInfo.size() == 0)
			// meaning no attribute associated with the component
			return null;
		else
			return attrSetInfo;
	}

	/**
	 * Reads the attribute info data and picks the attribute sets for the
	 * component
	 * 
	 * @param componentData The map containing all the xml nodes for the given
	 *            component.
	 * @return A map containing the attribute set ids for each variety (Key used
	 *         is variety no)
	 * @throws LayoutException
	 */
	static Map<Object, Object> readAttributeSetIds(Map componentData)
			throws LayoutException {

		Map<Object, Object> attrSetForComp = LayoutXmlDataHelperFunctions
				.readAttributeInfo(componentData);
		if (attrSetForComp == null)
			// meaning no attribute associated with this component
			return null;

		// Get the variety keys and process the attribute sets for each variety
		String[] keys = (String[]) attrSetForComp.keySet().toArray(
				new String[1]);

		Set<Object> attrSetAssociatedWithComp = new HashSet<Object>();
		Map<Object, Object> attrSetIdsForComp = new HashMap<Object, Object>();

		for (int i = 0; i < keys.length; i++) {
			List allAttrSetDataForPickedVariety = (List) attrSetForComp
					.get(keys[i]);
			if (allAttrSetDataForPickedVariety == null)
				// meaning no attrsets for this option
				continue;

			List<Object> attrSetForVariety = new ArrayList<Object>();

			for (int j = 0; j < allAttrSetDataForPickedVariety.size(); j++) {
				Map oneAttrSetData = (Map) allAttrSetDataForPickedVariety
						.get(j);

				String attrSetName = (String) oneAttrSetData
						.get(LayoutConstants.ATTR_ATTRSET_NAME);
				// attrSetAssociatedWithComp.add(attrSetId);
				attrSetAssociatedWithComp.add(attrSetName);
				// attrSetForVariety.add(attrSetId);
				attrSetForVariety.add(attrSetName);
			}

			// Put the data into a map containing the attr set ids for the
			// variety
			attrSetIdsForComp.put(keys[i], attrSetForVariety);
		}
		return attrSetIdsForComp;
	}

	/**
	 * @param componentData The component data map containing the component
	 *            information
	 * @param varNo The variety numbers of the component whose attributes are
	 *            being requested.
	 * @return A map containing the attributes associated with the components
	 *         variety entry
	 * @throws LayoutException
	 */
	static Map getLayoutAttributes(Map<String, Object> componentData,
			String varNo) throws LayoutException {

		if (componentData == null) {
			throw new LayoutException("Invalid data input");
		}

		// Checking if already the attributes was already processed.
		if (componentData.containsKey(LayoutConstants.KEY_ATTRIBUTE_DATA)) {
			Map attrDetails = (Map) componentData
					.get(LayoutConstants.KEY_ATTRIBUTE_DATA);
			if (attrDetails == null)
				return null;
			else {
				Map attrSetForVariety = (Map) attrDetails.get(varNo);
				return (Map) attrSetForVariety;
			}
		}

		/*
		 * At this point we are sure that this is the first time attributes are
		 * being processed (i.e this funciton is called for the first time) This
		 * function breaks the attributeSet nodes - reads the attribute xml tag -
		 * fetches the attribute name and value and puts it into a map.
		 */
		Map attributeSetNodes = (Map) componentData
				.get(LayoutConstants.KEY_ATTR_SET_XML_NODE);
		if (attributeSetNodes == null) // no attributes
			return null;

		Set keySet = attributeSetNodes.keySet();

		Map<Object, Object> keyAttributeDataMap = new Hashtable<Object, Object>();
		Iterator iter = keySet.iterator();

		while (iter.hasNext()) {

			String currVarNo = (String) iter.next();
			List attrSetForVariety = (List) attributeSetNodes.get(currVarNo);
			
			if (attrSetForVariety == null || attrSetForVariety.size() < 1)
				continue;
			// return null;

			Map<Object, Object> attrDetails = new Hashtable<Object, Object>();

			for (int i = 0; i < attrSetForVariety.size(); i++) {
				CompactElement nextAttrSet = (CompactElement) attrSetForVariety
						.get(i);

				if (nextAttrSet == null)
					continue;
				// from the attributeset node get the nodelist of <attribute>
				// tags
				List<CompactElement> attrList = nextAttrSet
						.getElementsByTagName(LayoutConstants.XMLTAG_ATTRIBUTE);

				for (int j = 0; j < attrList.size(); j++) {
					CompactElement nextAttr = (CompactElement) attrList.get(j);
					String attrName = (String) nextAttr
							.getAttribute(LayoutConstants.ATTR_ATTR_NAME);

					// Value of the attribute must be present in the calc tag
					List<CompactElement> calcList = nextAttr
							.getElementsByTagName(LayoutConstants.XMLTAG_CALC);
					if (calcList == null || calcList.size() == 0)
						continue;

					for (int k = 0; k < calcList.size(); k++) {
						CompactElement nextCalc = (CompactElement) calcList
								.get(k);
						// Map calcAttributes =
						// (Map)DomHelperFunctions.getAttributes(nextCalc);

						/*
						 * The following piece of code that rejects zoom values
						 * must be modified when zoom will be supporeted.
						 * Currently if we have the zoom attribute then we are
						 * rejecting the value
						 */
						String zoomId = (String) nextCalc
								.getAttribute(LayoutConstants.ZOOM);
						if (zoomId != null && zoomId.length() > 0)
							continue;

						String attrValue = (String) nextCalc
								.getAttribute(LayoutConstants.ATTR_ATTR_VALUE);
						// String attrValue = (String)
						// calcAttributes.get(LayoutConstants.ATTR_ATTR_VALUE);

						attrDetails.put(attrName, attrValue);
						break; // Got the value so no more processing required
					}
				}
			}

			if (attrDetails.size() < 1) {
				attrDetails = null;
			}


			if (attrDetails != null)
				keyAttributeDataMap.put(currVarNo, attrDetails);
		}

		// Add it to the componentData object so that the process need not be
		// repeated
		// in the next call to this function.
		
		componentData.put(LayoutConstants.KEY_ATTRIBUTE_DATA,
				keyAttributeDataMap);

		Map returnMap = (Map) keyAttributeDataMap.get(varNo);

		return returnMap;
		
	}

	/**
	 * Reads the LayoutInfo block in the component data layout xml node.
	 * 
	 * @param componentData The map containing all the xml nodes for the given
	 *            component.
	 * @return A map containing the layout details. (Key used is variety no)
	 * @throws LayoutException
	 */
	static Map readLayoutInfo(Map<String, Object> componentData)
			throws LayoutException {

		if (componentData == null)
			throw new LayoutException("Invalid input data");

		// Check we have processed the data already
		if (componentData.containsKey(LayoutConstants.KEY_LAYOUT_DATA)) {
			Map layoutInfo = (Map) componentData
					.get(LayoutConstants.KEY_LAYOUT_DATA);
			return layoutInfo;
		}

		// At this point we are sure that this is the first call to this
		// function

		CompactElement layoutData = (CompactElement) componentData
				.get(LayoutConstants.KEY_LAYOUT_XML_NODE);
		if (layoutData == null)
			throw new LayoutException("Invalid layout data in input");

		List<CompactElement> layoutInfoGroupNode = layoutData
				.getElementsByTagName(LayoutConstants.XMLTAG_LAYOUT_INFO);
		if ((layoutInfoGroupNode == null) || (layoutInfoGroupNode.size()) != 1) {
			throw new LayoutException("Invalid layout info in input data");
		}

		CompactElement layoutInfoNode = (CompactElement) layoutInfoGroupNode
				.get(0);
		Map<Object, Object> layoutInfoResult = new HashMap<Object, Object>();

		// Get the <Param> block and process each <Param> block
		List<CompactElement> paramNodeList = layoutInfoNode
				.getElementsByTagName(LayoutConstants.XMLTAG_PARAM);

		for (int i = 0; i < paramNodeList.size(); i++) {
			CompactElement nextParam = (CompactElement) paramNodeList.get(i);
			// System.out.println (nextParam);
			String paramName = nextParam
					.getAttribute(LayoutConstants.ATTR_PARAM_NAME);
			// System.out.println ("processed param ------------->" +
			// paramName);

			// Get the <value> block and work on each <value> block
			List<CompactElement> valueNodeList = nextParam
					.getElementsByTagName(LayoutConstants.XMLTAG_VALUE);

			// Processing each <value> node. A value node stores value for one
			// variety
			for (int j = 0; j < valueNodeList.size(); j++) {
				CompactElement nextValue = (CompactElement) valueNodeList
						.get(j);
				CompactElement varietyElement = (CompactElement) nextValue
						.getElementsByTagName(
								LayoutConstants.XMLTAG_VARIETY_INDEX).get(0);
				String varietyNo = varietyElement
						.getAttribute(LayoutConstants.ATTR_VALUE);
				// System.out.println ("VAR NO ----------------------->" +
				// varietyNo);

				// check if the layoutInfoResult map contains an entry for this
				// variety
				if (!layoutInfoResult.containsKey(varietyNo)) {
					Map m = new HashMap();
					layoutInfoResult.put(varietyNo, m);
				}

				// Processing each <calc> element
				List<CompactElement> calcElementList = nextValue
						.getElementsByTagName(LayoutConstants.XMLTAG_CALC);
				List<Object> calcList = new ArrayList<Object>();
				// System.out.println ("no of calc found --------->" +
				// calcElementList.size());
				for (int k = 0; k < calcElementList.size(); k++) {
					CompactElement nextCalcElement = (CompactElement) calcElementList
							.get(k);
					Map calcData = nextCalcElement.getAttributes();
					// System.out.println ("Calc data :" + calcData);
					calcList.add(calcData);
				}
				// at this point we have a calcList that contains the data of
				// all rows/column for the given parameter

				// add the calcList to the value map for the given variety in
				// the layoutInfoResult map.
				Map layoutInfoForVariety = (Map) layoutInfoResult
						.get(varietyNo);
				if (calcList.size() > 0) {
					layoutInfoForVariety.put(paramName, calcList);
					// System.out.println ("ADded for " + paramName);
				}
			}
		}

		// Add it to the component data
		componentData.put(LayoutConstants.KEY_LAYOUT_DATA, layoutInfoResult);

		return layoutInfoResult;
	}

	/**
	 * Fetches the parameter values (for each row and column) for the required
	 * variety number
	 * 
	 * @param param The parameter whose values are required.
	 * @param varNo The variety number for which the param values are required.
	 * @param compLayoutData The component data map containing the xml nodes for
	 *            the component.
	 * @return A string matrix containing the parameter value for each row and
	 *         column (for non multi row/column components, the return array
	 *         will be of size 1x1. The matrix will have null value for those
	 *         row/column which has not been defined in the xml file.
	 * @throws LayoutException
	 */
	static String[][] getParamValue(String param, String varNo,
			Map<String, Object> compLayoutData) throws LayoutException {
		Map layoutData = readLayoutInfo(compLayoutData);
		if (layoutData == null || !layoutData.containsKey(varNo)) {
			throw new LayoutException("Invalid input data");
		}

		int noRows = getRowCount(varNo, compLayoutData);
		int noCols = getColumnCount(varNo, compLayoutData);
		Map layoutDataForVariety = (Map) layoutData.get(varNo);

		return getParamValue(param, noRows, noCols, layoutDataForVariety);
	}

	static String[][] getParamValue(String param, int noRows, int noCols,
			Map layoutDataForVariety) throws LayoutException {

		// Allocate values for the return matrix.
		String[][] paramValues = new String[noRows][noCols];
		// note that java automatically assign nulls to all the cells in the
		// above matrix.

		List paramDataForVariety = (List) layoutDataForVariety.get(param);
		// the above list contains all the <calc> data for the required param.

		if (paramDataForVariety == null || paramDataForVariety.size() < 1) {
			return null;
		}

		boolean atLeastOneDataPresent = false;

		for (int i = 0; i < paramDataForVariety.size(); i++) {
			Map nextCalcData = (Map) paramDataForVariety.get(i);
			// System.out.print("Param:" + param + "D:" + nextCalcData);

			String isShownStr = (String) nextCalcData
					.get(LayoutConstants.ATTR_SHOWN_IN_TABLE);

			if (isShownStr != null) {
				boolean isShown = new Boolean(isShownStr).booleanValue();
				if (!isShown) {
					continue;
				}
			}

			atLeastOneDataPresent = true;

			String rowNoStr = (String) nextCalcData
					.get(LayoutConstants.ATTR_ROW_NUMBER);
			String colNoStr = (String) nextCalcData
					.get(LayoutConstants.ATTR_COL_NUMBER);
			String valueStr = (String) nextCalcData
					.get(LayoutConstants.ATTR_VALUE);

			String zoomId = (String) nextCalcData.get(LayoutConstants.ZOOM);
			if (zoomId != null && zoomId.length() > 0)
				continue;

			int rowPos = (rowNoStr != null && rowNoStr.trim().length() > 0) ? (Integer
					.parseInt(rowNoStr) - 1)
					: 0;
			int colPos = (colNoStr != null && colNoStr.trim().length() > 0) ? (Integer
					.parseInt(colNoStr) - 1)
					: 0;

			// to avoid situations were the number of rows/columns are not the
			// same
			// in the data of the compData.xml file of LCt and the
			// attributes.xml
			// this is a situation not to happen... but having this check just
			// in case

		
			if (rowPos >= noRows || colPos >= noCols) {
				continue;
			}

			paramValues[rowPos][colPos] = valueStr;

			// System.out.println(
			// " r:"
			// + rowPos
			// + " c:"
			// + colPos
			// + " detV:"
			// + paramValues[rowPos][colPos]);
		}
		if (atLeastOneDataPresent)
			return paramValues;
		else
			return null;
	}

	/**
	 * Fetches the number of rows in the given component
	 * 
	 * @param varNo The variety number for which the param values are required.
	 * @param compLayoutData The component data map containing the xml nodes for
	 *            the component.
	 * @return
	 */
	static int getRowCount(String varNo, Map<String, Object> compLayoutData)
			throws LayoutException {

		/*
		 * The ideal system is to simply read the 'No of rows/ No of column'
		 * attribute. However, the layout system has found a way by which the
		 * attribute cannot be totally relied on. The fix in the layout data is
		 * labourious and does not guarantee a permanent solution. Instead till
		 * a better solution is found, the layout system will run throw the row
		 * and column values and find the maximum value and fit it in as the
		 * row/ col size.
		 */

		
		Map layoutData = readLayoutInfo(compLayoutData);
		if (layoutData == null || !layoutData.containsKey(varNo)) {
			throw new LayoutException("Invalid input data");
		}

		
		/*
		 * For handling the row count in the wrong and labourious way 0. Check
		 * if the calculated value is already there. 1. Read the 'top' param
		 * value 2. Read all the values under calc tag and get the maximum rowNo
		 * value 3. Repeate the procedure for 'bottom' param 4. Get the max
		 * value of the values found in 2 and 3. 5. And to avoid redoing the
		 * process - add the new value to the attributes list
		 */

		// Check if we had already calculated the row count
		Integer calcRowCount = (Integer) compLayoutData
				.get(LayoutConstants.KEY_ROW_COUNT);
		if (calcRowCount != null)
			return calcRowCount.intValue();

		Map layoutDataForVariety = (Map) layoutData.get(varNo);

		// get the list that contains all the <calc> data for the 'Top' param
		List paramDataForVariety = (List) layoutDataForVariety
				.get(LayoutConstants.VALUE_PARAM_TOP);
		int rowNoDeterminedFromTop = getMaxAttrValue(paramDataForVariety,
				LayoutConstants.ATTR_ROW_NUMBER);

		// get the list that contains all the <calc> data for the 'Bottom' param
		paramDataForVariety = (List) layoutDataForVariety
				.get(LayoutConstants.VALUE_PARAM_BOTTOM);
		int rowNoDeterminedFromBottom = getMaxAttrValue(paramDataForVariety,
				LayoutConstants.ATTR_ROW_NUMBER);

		int rowCount = Math.max(rowNoDeterminedFromTop,
				rowNoDeterminedFromBottom);

		// Set the calculated value back into the layout data map
		compLayoutData
				.put(LayoutConstants.KEY_ROW_COUNT, new Integer(rowCount));

		return rowCount;
	}

	/**
	 * Fetches the number of columns in the given component
	 * 
	 * @param varNo The variety number for which the param values are required.
	 * @param compLayoutData The component data map containing the xml nodes for
	 *            the component.
	 * @return
	 */
	static int getColumnCount(String varNo, Map<String, Object> compLayoutData)
			throws LayoutException {

		Map layoutData = readLayoutInfo(compLayoutData);
		if (layoutData == null || !layoutData.containsKey(varNo)) {
			throw new LayoutException("Invalid input data");
		}


		/*
		 * For handling the row count in the wrong and labourious way 0. Check
		 * if the calculated value is already there. 1. Read the 'top' param
		 * value 2. Read all the values under calc tag and get the maximum rowNo
		 * value 3. Repeate the procedure for 'bottom' param 4. Get the max
		 * value of the values found in 2 and 3. 5. And to avoid redoing the
		 * process - add the new value to the attributes list
		 */

		// Check if we had already calculated the row count
		Integer calcColCount = (Integer) compLayoutData
				.get(LayoutConstants.KEY_COLUMN_COUNT);
		if (calcColCount != null)
			return calcColCount.intValue();

		Map layoutDataForVariety = (Map) layoutData.get(varNo);

		// get the list that contains all the <calc> data for the 'Top' param
		List paramDataForVariety = (List) layoutDataForVariety
				.get(LayoutConstants.VALUE_PARAM_LEFT);
		int colNoDeterminedFromLeft = getMaxAttrValue(paramDataForVariety,
				LayoutConstants.ATTR_COL_NUMBER);

		// get the list that contains all the <calc> data for the 'Bottom' param
		paramDataForVariety = (List) layoutDataForVariety
				.get(LayoutConstants.VALUE_PARAM_RIGHT);
		int colNoDeterminedFromRight = getMaxAttrValue(paramDataForVariety,
				LayoutConstants.ATTR_COL_NUMBER);

		int colCount = Math.max(colNoDeterminedFromLeft,
				colNoDeterminedFromRight);

		// Set the calculated value back into the layout data map
		compLayoutData.put(LayoutConstants.KEY_COLUMN_COUNT, new Integer(
				colCount));

		return colCount;
	}

	/**
	 * This is a private function to find the maximum value for rowNo and
	 * columnNo attributes
	 * 
	 * @param paramDataForVariety The param data block for one parameter
	 * @param keyAttribute The attribute to be look for (rowNo / columnNo)
	 * @throws LayoutException
	 */
	private static int getMaxAttrValue(List paramDataForVariety,
			String keyAttribute) throws LayoutException {

		if (paramDataForVariety == null || paramDataForVariety.size() < 1) {
			return -1;
		}

		int maxValue = 1; // The default value

		for (int i = 0; i < paramDataForVariety.size(); i++) {
			Map nextCalcData = (Map) paramDataForVariety.get(i);
			// System.out.print("Param:" + param + "D:" + nextCalcData);

			String isShownStr = (String) nextCalcData
					.get(LayoutConstants.ATTR_SHOWN_IN_TABLE);

			if (isShownStr != null) {
				boolean isShown = new Boolean(isShownStr).booleanValue();
				if (!isShown) {
					continue;
				}
			}

			String valStr = (String) nextCalcData.get(keyAttribute);
			// int newValue = (valStr != null && (valStr.trim().length()>0)) ?
			// (Integer.parseInt(valStr) - 1) : -1;
			int newValue = (valStr != null && (valStr.trim().length() > 0)) ? (Integer
					.parseInt(valStr))
					: -1;
			maxValue = Math.max(maxValue, newValue);
		}

		return maxValue;
	}

	/**
	 * @param compLayoutData
	 * @param attrName The name of the attribute whose value is required
	 * @return The value of the xml attribute given with the component xml tag.
	 */
	static String getXmlCompNodeAttrValue(Map compLayoutData, String attrName) {
		if (compLayoutData == null)
			return null;

		CompactElement layoutData = (CompactElement) compLayoutData
				.get(LayoutConstants.KEY_LAYOUT_XML_NODE);
		if (layoutData == null)
			return null;

		Map compAttr = layoutData.getAttributes();
		
		String compName = (String) compAttr.get(attrName);
		return compName;
	}

	/**
	 * @param compLayoutData The component data map.
	 * @param optionId The option id whose variety number has to be determined.
	 * @return The variety no string
	 */
	static String getVarietyNo(Map compLayoutData, String optionId) {

		if (optionId == null)
			return null;

		if (compLayoutData == null)
			return null;

		CompactElement layoutData = (CompactElement) compLayoutData
				.get(LayoutConstants.KEY_LAYOUT_XML_NODE);
		if (layoutData == null)
			return null;

		// Get the <LayoutInfo> block
		List<CompactElement> dataList = layoutData
				.getElementsByTagName(LayoutConstants.XMLTAG_LAYOUT_INFO);
		CompactElement layoutBlock = (CompactElement) dataList.get(0);

		// Get the <Param> block
		dataList = layoutBlock
				.getElementsByTagName(LayoutConstants.XMLTAG_PARAM);

		// For each <Param> block get the <Value> block and get the variety no
		// and
		// option index mentioned there. The first match for the requrired index
		// will break the loop.
		for (int i = 0; i < dataList.size(); i++) {
			CompactElement nextParam = (CompactElement) dataList.get(i);

			// Get the <Value> block
			List<CompactElement> valuesList = nextParam
					.getElementsByTagName(LayoutConstants.XMLTAG_VALUE);
			for (int j = 0; j < valuesList.size(); j++) {
				CompactElement nextValue = (CompactElement) valuesList.get(j);

				CompactElement optionIndexEl = (CompactElement) nextValue
						.getElementsByTagName(LayoutConstants.XMLTAG_OPTION_INDEX);
				String optionIndex = optionIndexEl
						.getAttribute(LayoutConstants.ATTR_VALUE);

				CompactElement varietyNoEl = (CompactElement) nextValue
						.getElementsByTagName(LayoutConstants.XMLTAG_VARIETY_INDEX);
				String varietyNo = varietyNoEl
						.getAttribute(LayoutConstants.ATTR_VALUE);

				if (optionId.equals(optionIndex)) {
					return varietyNo;
				}
			}
		}

		return null;
	}

	/**
	 * @param compLayoutData The component data map.
	 * @param varietyNo The variety no whose option id has to be determined.
	 * @return The option id string.
	 */
	static String getOptionIndex(Map compLayoutData, String varietyNumber) {

		if (varietyNumber == null)
			return null;

		if (compLayoutData == null)
			return null;

		CompactElement layoutData = (CompactElement) compLayoutData
				.get(LayoutConstants.KEY_LAYOUT_XML_NODE);
		if (layoutData == null)
			return null;

		// Get the <LayoutInfo> block
		List<CompactElement> dataList = layoutData
				.getElementsByTagName(LayoutConstants.XMLTAG_LAYOUT_INFO);
		CompactElement layoutBlock = (CompactElement) dataList.get(0);

		// Get the <Param> block
		dataList = layoutBlock
				.getElementsByTagName(LayoutConstants.XMLTAG_PARAM);

		// For each <Param> block get the <Value> block and get the variety no
		// and
		// option index mentioned there. The first match for the requrired index
		// will break the loop.
		for (int i = 0; i < dataList.size(); i++) {
			CompactElement nextParam = (CompactElement) dataList.get(i);

			// Get the <Value> block
			List<CompactElement> valuesList = nextParam
					.getElementsByTagName(LayoutConstants.XMLTAG_VALUE);
			for (int j = 0; j < valuesList.size(); j++) {
				CompactElement nextValue = (CompactElement) valuesList.get(j);

				CompactElement optionIndexEl = (CompactElement) nextValue
						.getElementsByTagName(LayoutConstants.XMLTAG_OPTION_INDEX);
				String optionIndex = optionIndexEl
						.getAttribute(LayoutConstants.ATTR_VALUE);

				CompactElement varietyNoEl = (CompactElement) nextValue
						.getElementsByTagName(LayoutConstants.XMLTAG_VARIETY_INDEX);
				String varietyNo = varietyNoEl
						.getAttribute(LayoutConstants.ATTR_VALUE);

				if (varietyNumber.equals(varietyNo)) {
					return optionIndex;
				}
			}
		}

		return null;
	}

}
