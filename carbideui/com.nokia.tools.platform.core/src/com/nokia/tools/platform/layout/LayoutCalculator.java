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
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Class to perform layout calculations.
 */
class LayoutCalculator {

	private static final String NEW_LAYOUT_IDENTIFER = "$";

	/*
	 * Format of location identifier (loc_id):
	 * --------------------------------------- {
	 * <LC>layoutCompName@[v:varietyNo||o:
	 * <optionnumber>]1[#r:rowNo]1[#c:colNo]1 </LC>} e.g. <LC>Screen@v:0 </LC>
	 * <LC>main_pane@v:0 </LC> <LC>list_single_number_pane@v:1#r:1 </LC>
	 * <LC>Screen@v:0 </LC> <LC>main_pane@v:0 </LC>
	 * <LC>application_grid@v:0#r:1#c:1 </LC> Calculation System: General idea:
	 * --------------------------------- 1) The class first tries to find the
	 * default parent components (with their variety/option values) till screen.
	 * 2) If location identifier (loc_id) is provided, it tries to extract the
	 * parent hierarchy from the identfier string. 3) If the location identifer
	 * does not provide the path till screen, then the class will add the
	 * default parents for the left most component in the location id string.
	 */

	private static final String NONE = "NONE";

	public static final String SCREEN_STRING = (new StringBuffer().append(
			LayoutConstants.VALUE_COMPONENT_SCREEN).append(LayoutConstants.S1)
			.append("v:").append(LayoutConstants.DEFAULT_VARIETY_NO).append(
					LayoutConstants.S3).append("r:").append(
					LayoutConstants.DEFAULT_ROW_NO).append(LayoutConstants.S3)
			.append("c:").append(LayoutConstants.DEFAULT_COLUMN_NO)).toString();

	
	private DefaultMutableTreeNode calculationTree = new DefaultMutableTreeNode(
			new TreeNodeData(SCREEN_STRING));

	private LayoutContext context;

	private String layoutName = NONE;

	/**
	 * @param allowedLayoutSets
	 *            The list of layout set names in which the component data can
	 *            be looked up. The module assumes that the all the layout sets
	 *            supplied to it - belongs to the same display size and hence
	 *            will not check the same.
	 */
	public LayoutCalculator(LayoutContext context) {
		this.context = context;
	}

	protected void calculateLayout(ComponentInfo component)
			throws LayoutException {
		// Step 1. Process the location id string
		List locHier = processLocationData(component);

		// Step 2: Build the calculation tree
		DefaultMutableTreeNode layoutRefNode = addToCalculationTree(locHier);

		// printTree(layoutRefNode);

		// Step 3: Calculate the data in the tree
		calculate();

		TreeNodeData tnd = (TreeNodeData) layoutRefNode.getUserObject();
		Layout calculatedLayout = tnd.getLayout();

		// System.out.println(tnd.getId());
		component.setLayout(calculatedLayout);
	}

	/**
	 * This function calculates all the components given in the list in one
	 * calculation call. (For improving performance)
	 * 
	 * @param layoutCompNameList
	 *            The list of components to be calculated.
	 * @param varNoList
	 *            The list containing the variety numbers. (This list should be
	 *            a one to one match with the layoutCompNameList)
	 * @param locIdList
	 *            The list containing the locIdList numbers. (This list should
	 *            be a one to one match with the layoutCompNameList)
	 * @return A list containing all the calculated layouts. (The list matches
	 *         one to one with the layoutCompNameList)
	 * @throws LayoutException
	 */
	protected void calculateLayout(ComponentInfo[] components)
			throws LayoutException {
		List<Object> markedNodes = new ArrayList<Object>();
		for (ComponentInfo component : components) {
			// Step 1. Process the location id string
			List locHier = processLocationData(component);

			// Step 2: Build the calculation tree
			DefaultMutableTreeNode layoutRefNode = addToCalculationTree(locHier);

			// Note the marked nodes into the list
			markedNodes.add(layoutRefNode);
		}

		// Step 3: Calculate the data in the tree
		calculate();

		// Make a list to return the layout values
		for (int i = 0; i < markedNodes.size(); i++) {
			DefaultMutableTreeNode nextCalcNode = (DefaultMutableTreeNode) markedNodes
					.get(i);
			TreeNodeData tnd = (TreeNodeData) nextCalcNode.getUserObject();
			Layout calculatedLayout = tnd.getLayout();
			components[i].setLayout(calculatedLayout);
		}
	}

	/**
	 * Creates / Appends data to the calculation tree.
	 * 
	 * @param locationHierarchy
	 *            The containment hierarchy list.
	 */
	private DefaultMutableTreeNode addToCalculationTree(List locationHierarchy) {

		/*
		 * The locationHierarchy list will contain string entries in the form
		 * <Layout Component Name>@v: <varietyNo>#r: <rowNo>#c: <colNo>. The
		 * list will have it content according to the containment hierarchy
		 * starting from the 'SCREEN' component (the first element in the list).
		 */

		DefaultMutableTreeNode screenNode = (DefaultMutableTreeNode) calculationTree
				.getRoot();

		DefaultMutableTreeNode currentNode = screenNode;
		locationHierarchy.remove(0); // The first element is screen data

		while (locationHierarchy.size() != 0) {
			String nextElemInHierStr = (String) locationHierarchy.get(0);
			if (nextElemInHierStr.startsWith(NEW_LAYOUT_IDENTIFER)) {
				System.out.println(nextElemInHierStr);
			}
			// System.out.println ("Next elem in hier " + nextElemInHierStr);

			TreeNodeData reqNode = new TreeNodeData(nextElemInHierStr);

			DefaultMutableTreeNode matchNode = null;

			Enumeration children = currentNode.children();
			while (children.hasMoreElements()) {
				DefaultMutableTreeNode nextChild = (DefaultMutableTreeNode) children
						.nextElement();
				// String childData = (String)nextChild.getUserObject();
				TreeNodeData childData = (TreeNodeData) nextChild
						.getUserObject();
				if (childData.equals(reqNode)) {
					matchNode = nextChild;
					break;
				}
			}

			// at this point if matchNode is null - it means a match has not
			// been found
			if (matchNode == null) {
				matchNode = new DefaultMutableTreeNode(reqNode);
				currentNode.add(matchNode);
			}

			currentNode = matchNode;
			locationHierarchy.remove(0);
		}

		// printTree((DefaultMutableTreeNode) calculationTree.getRoot());

		// At this point the currentNode object holds the leaf node
		// corresponding to the
		// layout that we are trying to determine.
		return currentNode;
	}

	/**
	 * Process the entire localisation data - finds if there is any missing
	 * parents and inserts them
	 * 
	 * @param compName
	 *            The name of the component whose localisation id is being
	 *            processed
	 * @param varNo
	 *            The variety number of the component
	 * @param locationId
	 *            The location id string
	 * @return A list containing the parent hierarchy starting with Screen (the
	 *         format is <compname>@ <v:varno>
	 * @throws LayoutException
	 */
	protected List processLocationData(ComponentInfo component)
			throws LayoutException {
		/*
		 * The localisation id string will be in the format {
		 * <LC>layoutCompName@{v:varietyNo || o:
		 * <optionnumber>}1[#r:rowNo]1[#c:colNo]1 </LC>} e.g. <LC>Screen@v:0
		 * </LC> <LC>main_pane@v:0 </LC> <LC>list_single_number_pane@v:1#r:1
		 * </LC> <LC>Screen@v:0 </LC> <LC>main_pane@v:0 </LC>
		 * <LC>application_grid@v:0#r:1#c:1 </LC> NOTE: locationId can be null
		 */

		LinkedList<Object> hierarchyList = new LinkedList<Object>();

		if (component.getLocId() == null) {
			List<Object> missingParentList = getDefaultParentHierarchy(
					component.getName(), component.getVariety());
			hierarchyList.addAll(missingParentList);
			// System.out.println("hier list: " + hierarchyList);

		} else { // location id not null

			// String sTg = "<" + LayoutConstants.TAG_LOCID_LAY_COMP_SEPERATOR
			// + ">";
			// String eTg = "</" + LayoutConstants.TAG_LOCID_LAY_COMP_SEPERATOR
			// + ">";
			String[] locationHierarchyArray = getLCTokensFromLocId(component
					.getLocId());

			if ((locationHierarchyArray == null)
					|| (locationHierarchyArray.length < 1))
				throw new LayoutException("Invalid location identifier");

			/*
			 * Process first token. 1. The first token is checked if its parent
			 * is null or is screen (both means the same). If not the default
			 * parents (hierarchy till screen) for the given component is picked
			 * and inserted.
			 */

			String firstLC = locationHierarchyArray[0].toLowerCase().trim();
			String topComName = handleMoreThanOneLayouts(firstLC);
			topComName = getCompNameFromLocId(topComName);
			String topVarNo = getNofromLocId(firstLC, "v",
					LayoutConstants.DEFAULT_VARIETY_NO);

			List<Object> missingParentList = getDefaultParentHierarchy(
					topComName, topVarNo);
			hierarchyList.addAll(missingParentList);
			// System.out.println("FULL_MODIFY_ADAPTER PAR TREE: " +
			// missingParentList);

			// add the other location id tokens
			for (int i = 0; i < locationHierarchyArray.length; i++) {
				String nextLcToken = locationHierarchyArray[i].toLowerCase()
						.trim();
				String name = getCompNameFromLocId(nextLcToken);
				String vNo = getNofromLocId(nextLcToken, "v",
						LayoutConstants.DEFAULT_VARIETY_NO);
				String rNo = getNofromLocId(nextLcToken, "r",
						LayoutConstants.DEFAULT_ROW_NO);
				String cNo = getNofromLocId(nextLcToken, "c",
						LayoutConstants.DEFAULT_COLUMN_NO);

				StringBuffer sb = new StringBuffer().append(name).append(
						LayoutConstants.S1).append("v:").append(vNo).append(
						LayoutConstants.S3).append("r:").append(rNo).append(
						LayoutConstants.S3).append("c:").append(cNo);
				hierarchyList.addLast(sb.toString());
			}
		}

		// Add the processed component also to the list
		StringBuffer processedCompStringBuffer = new StringBuffer().append(
				component.getName()).append(LayoutConstants.S1).append("v:")
				.append(component.getVariety()).append(LayoutConstants.S3)
				.append("r:").append(LayoutConstants.DEFAULT_ROW_NO).append(
						LayoutConstants.S3).append("c:").append(
						LayoutConstants.DEFAULT_COLUMN_NO);

		hierarchyList.addLast(processedCompStringBuffer.toString());

		return hierarchyList;
	}

	/**
	 * @param compName
	 *            The name of the component whose parent details has to be
	 *            picked.
	 * @param varNo
	 *            The variety number of the component whose parent details has
	 *            to be picked
	 * @return A list containing the default parents for the given component
	 *         (and varid)
	 */
	private List<Object> getDefaultParentHierarchy(String compName, String varNo)
			throws LayoutException {

		String topCompName = compName;
		String topVarNo = varNo;
		String screenName = LayoutConstants.VALUE_COMPONENT_SCREEN
				.toLowerCase();

		LinkedList<Object> resultList = new LinkedList<Object>();
		// Build the parent hierarchy till screen.
		while (!topCompName.equalsIgnoreCase(screenName)) {
			Map<String, Object> topCompData = null;
			if (topCompName != null
					&& topCompName
							.equalsIgnoreCase(LayoutConstants.VALUE_COMPONENT_SCREEN)) {
				topCompData = getDataForComponentName(topCompName);
			} else {
				topCompData = getDataForComponentName(topCompName, topVarNo);
			}

			if (topCompData == null)
				throw new LayoutException("Insufficient layout data for "
						+ compName + "@v:" + topVarNo);

			Map parentInfo = LayoutXmlDataHelperFunctions
					.readParentInfo(topCompData);

			// Procesing only the first parent data
			if (!parentInfo.containsKey(topVarNo)) {
				throw new LayoutException("Insufficient layout data for "
						+ compName + "@v:" + topVarNo);
			}
			Map reqParentDetails = (Map) ((List) parentInfo.get(topVarNo))
					.get(0);
			String reqParId = (String) reqParentDetails
					.get(LayoutConstants.ATTR_COMPONENT_ID);
			String reqParVa = (String) reqParentDetails
					.get(LayoutConstants.ATTR_VARIETY);

			topCompName = getNameForComponentId(reqParId, (String) topCompData
					.get(LayoutConstants.LAYOUT_SET_NAME));
			topVarNo = reqParVa;
			StringBuffer sb = new StringBuffer().append(topCompName).append(
					LayoutConstants.S1).append("v:").append(topVarNo).append(
					LayoutConstants.S3).append("r:").append(
					LayoutConstants.DEFAULT_ROW_NO).append(LayoutConstants.S3)
					.append("c:").append(LayoutConstants.DEFAULT_COLUMN_NO);

			resultList.addFirst(sb.toString());
		}

		return resultList;
	}

	/**
	 * Does the calculation for all the components in the calculation tree
	 * 
	 * @throws LayoutException
	 */
	private void calculate() throws LayoutException {

		Map<String, Object> scrData = getDataForComponentName(LayoutConstants.VALUE_COMPONENT_SCREEN);
		String[][] widthStr = LayoutXmlDataHelperFunctions.getParamValue(
				LayoutConstants.VALUE_PARAM_WIDTH,
				LayoutConstants.DEFAULT_VARIETY_NO, scrData);
		String[][] heightStr = LayoutXmlDataHelperFunctions.getParamValue(
				LayoutConstants.VALUE_PARAM_HEIGHT,
				LayoutConstants.DEFAULT_VARIETY_NO, scrData);
		int scr_width = Integer.parseInt(widthStr[0][0]);
		int scr_height = Integer.parseInt(heightStr[0][0]);

		Layout dummyScrPar = new Layout();
		dummyScrPar.setLayout(0, 0, 0, 0, scr_width, scr_height);

		TreeNodeData dummyPaTnd = new TreeNodeData("DUMMY");
		dummyPaTnd.setLayout(dummyScrPar);
		DefaultMutableTreeNode dummyTreeNode = new DefaultMutableTreeNode(
				dummyPaTnd);
		recursivelyCalculate(
				(DefaultMutableTreeNode) calculationTree.getRoot(),
				dummyTreeNode);
	}

	/**
	 * Caculates the layout values for the given component node (and recursively
	 * calls calculation for its children)
	 */
	private void recursivelyCalculate(DefaultMutableTreeNode compNode,
			DefaultMutableTreeNode parNode) throws LayoutException {
		// Do the calculation for the input component node
		try {
			calculateLayoutValue(compNode, parNode);
		} catch (Exception ex) {
			System.out.println("DEBUG: exception " + ex.getMessage()
					+ " while trying to calculate layout of "
					+ ((TreeNodeData) compNode.getUserObject()).getId());
			return;
		}

		// Call the same function recursively for each of its children.
		Enumeration children = compNode.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode nextChild = (DefaultMutableTreeNode) children
					.nextElement();
			recursivelyCalculate(nextChild, compNode);
		}
	}

	private boolean isValid(int v) {
		return v != Integer.MIN_VALUE && v != Integer.MAX_VALUE;
	}

	/**
	 * Makes the layout calculation for the given component node
	 */
	private void calculateLayoutValue(DefaultMutableTreeNode compNode,
			DefaultMutableTreeNode parNode) throws LayoutException {
		TreeNodeData parTnd = (TreeNodeData) parNode.getUserObject();


		TreeNodeData ptnd = (TreeNodeData) parNode.getUserObject();
		String pcompNodeData = (String) ptnd.getId();
		String preqRowNo = getNofromLocId(pcompNodeData, "r",
				LayoutConstants.DEFAULT_ROW_NO);

		String preqColNo = getNofromLocId(pcompNodeData, "c",
				LayoutConstants.DEFAULT_COLUMN_NO);

		int iParRowNo = Integer.parseInt(preqRowNo);
		int iParColNo = Integer.parseInt(preqColNo);

		Layout parLayout = parTnd.getLayout();
		int pWidth = parLayout.W();
		int pHeight = parLayout.H();
		int pLeft = parLayout.L(iParRowNo - 1, iParColNo - 1);
		int pRight = parLayout.R(iParRowNo - 1, iParColNo - 1);
		int pTop = parLayout.T(iParRowNo - 1, iParColNo - 1);
		int pBottom = parLayout.B(iParRowNo - 1, iParColNo - 1);

		// Get the name and variety no of the component
		TreeNodeData tnd = (TreeNodeData) compNode.getUserObject();
		String compNodeData = (String) tnd.getId();
		compNodeData = handleMoreThanOneLayouts(compNodeData);
		String reqComName = getCompNameFromLocId(compNodeData);
		String reqVarNo = getNofromLocId(compNodeData, "v",
				LayoutConstants.DEFAULT_VARIETY_NO);

		Map<String, Object> compData = getDataForComponentName(reqComName);

		if (reqComName != null
				&& reqComName
						.equalsIgnoreCase(LayoutConstants.VALUE_COMPONENT_SCREEN)) {
			compData = getDataForComponentName(reqComName);
		} else {
			compData = getDataForComponentName(reqComName, reqVarNo);
		}

		Map layoutData = LayoutXmlDataHelperFunctions.readLayoutInfo(compData);
		if (layoutData == null || !layoutData.containsKey(reqVarNo)) {
			throw new LayoutException("Invalid input data");
		}

		Map layoutDataForVariety = (Map) layoutData.get(reqVarNo);
		int rows = LayoutXmlDataHelperFunctions.getRowCount(reqVarNo, compData);
		int cols = LayoutXmlDataHelperFunctions.getColumnCount(reqVarNo,
				compData);
		// Process the layout parameters
		String[][] leftStr = LayoutXmlDataHelperFunctions.getParamValue(
				LayoutConstants.VALUE_PARAM_LEFT, rows, cols,
				layoutDataForVariety);

		String[][] rightStr = LayoutXmlDataHelperFunctions.getParamValue(
				LayoutConstants.VALUE_PARAM_RIGHT, rows, cols,
				layoutDataForVariety);
		String[][] topStr = LayoutXmlDataHelperFunctions.getParamValue(
				LayoutConstants.VALUE_PARAM_TOP, rows, cols,
				layoutDataForVariety);
		String[][] bottomStr = LayoutXmlDataHelperFunctions.getParamValue(
				LayoutConstants.VALUE_PARAM_BOTTOM, rows, cols,
				layoutDataForVariety);
		String[][] widthStr = LayoutXmlDataHelperFunctions.getParamValue(
				LayoutConstants.VALUE_PARAM_WIDTH, rows, cols,
				layoutDataForVariety);
		String[][] heightStr = LayoutXmlDataHelperFunctions.getParamValue(
				LayoutConstants.VALUE_PARAM_HEIGHT, rows, cols,
				layoutDataForVariety);
		String[][] justStr = LayoutXmlDataHelperFunctions.getParamValue(
				LayoutConstants.VALUE_PARAM_JUSTIFICATION, rows, cols,
				layoutDataForVariety);
		// String[][] colourStr = LayoutXmlDataHelperFunctions.getParamValue(
		// LayoutConstants.VALUE_PARAM_COLOUR, reqVarNo, compData);
		String[][] typeStr = LayoutXmlDataHelperFunctions.getParamValue(
				LayoutConstants.VALUE_PARAM_TYPE, rows, cols,
				layoutDataForVariety);
		int[][] left = new int[rows][cols];
		int[][] right = new int[rows][cols];
		int[][] top = new int[rows][cols];
		int[][] bottom = new int[rows][cols];
		int[][] width = new int[rows][cols];
		int[][] height = new int[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				left[i][j] = leftStr == null ? Integer.MAX_VALUE
						: Integer.MIN_VALUE;
				right[i][j] = rightStr == null ? Integer.MAX_VALUE
						: Integer.MIN_VALUE;
				top[i][j] = topStr == null ? Integer.MAX_VALUE
						: Integer.MIN_VALUE;
				bottom[i][j] = bottomStr == null ? Integer.MAX_VALUE
						: Integer.MIN_VALUE;
				width[i][j] = widthStr == null ? Integer.MAX_VALUE
						: Integer.MIN_VALUE;
				height[i][j] = heightStr == null ? Integer.MAX_VALUE
						: Integer.MIN_VALUE;
				if (leftStr != null) {
					if (leftStr[i][j] != null) {
						left[i][j] = Integer.parseInt(leftStr[i][j]);
					}
				}
				if (rightStr != null) {
					if (rightStr[i][j] != null) {
						right[i][j] = Integer.parseInt(rightStr[i][j]);
					}
				}
				if (topStr != null) {
					if (topStr[i][j] != null) {
						top[i][j] = Integer.parseInt(topStr[i][j]);
					}
				}
				if (bottomStr != null) {
					if (bottomStr[i][j] != null) {
						bottom[i][j] = Integer.parseInt(bottomStr[i][j]);
					}
				}
				if (widthStr != null) {
					if (widthStr[i][j] != null) {
						width[i][j] = (int) evaluateExpression(widthStr[i][j],
								pWidth);
					}
				}
				if (heightStr != null) {
					if (heightStr[i][j] != null) {
						height[i][j] = (int) evaluateExpression(
								heightStr[i][j], pHeight);
					}
				}

				if (left[i][j] == Integer.MIN_VALUE && (i > 0 || j > 0)) {
					left[i][j] = i == 0 ? left[i][j - 1] : left[i - 1][j];
				}
				if (right[i][j] == Integer.MIN_VALUE && (i > 0 || j > 0)) {
					right[i][j] = i == 0 ? right[i][j - 1] : right[i - 1][j];
				}
				if (width[i][j] == Integer.MIN_VALUE && (i > 0 || j > 0)) {
					width[i][j] = i == 0 ? width[i][j - 1] : width[i - 1][j];
				}
				if (top[i][j] == Integer.MIN_VALUE && (i > 0 || j > 0)) {
					top[i][j] = j == 0 ? top[i - 1][j] : top[i][j - 1];
				}
				if (bottom[i][j] == Integer.MIN_VALUE && (i > 0 || j > 0)) {
					bottom[i][j] = j == 0 ? bottom[i - 1][j] : bottom[i][j - 1];
				}
				if (height[i][j] == Integer.MIN_VALUE && (i > 0 || j > 0)) {
					height[i][j] = j == 0 ? height[i - 1][j] : height[i][j - 1];
				}

				if (left[i][j] == Integer.MAX_VALUE) {
					if (isValid(width[i][j]) && isValid(right[i][j])) {
						left[i][j] = pWidth - width[i][j] - right[i][j];
					}
				}
				if (right[i][j] == Integer.MAX_VALUE) {
					if (isValid(width[i][j]) && isValid(left[i][j])) {
						right[i][j] = pWidth - width[i][j] - left[i][j];
					}
				}
				if (width[i][j] == Integer.MAX_VALUE) {
					if (isValid(left[i][j]) && isValid(right[i][j])) {
						width[i][j] = pWidth - left[i][j] - right[i][j];
					}
				}
				if (top[i][j] == Integer.MAX_VALUE) {
					if (isValid(height[i][j]) && isValid(bottom[i][j])) {
						top[i][j] = pHeight - height[i][j] - bottom[i][j];
					}
				}
				if (bottom[i][j] == Integer.MAX_VALUE) {
					if (isValid(height[i][j]) && isValid(top[i][j])) {
						bottom[i][j] = pHeight - height[i][j] - top[i][j];
					}
				}
				if (height[i][j] == Integer.MAX_VALUE) {
					if (isValid(top[i][j]) && isValid(bottom[i][j])) {
						height[i][j] = pHeight - top[i][j] - bottom[i][j];
					}
				}

				// sanity check
				if (!isValid(left[i][j]) || !isValid(right[i][j])
						|| !isValid(width[i][j]) || !isValid(top[i][j])
						|| !isValid(bottom[i][j]) || !isValid(height[i][j])) {
					throw new LayoutException("No layout data available for: "
							+ reqComName + "@v" + reqVarNo);
				}

				int diffW = pWidth - left[i][j] - right[i][j];
				int diffH = pHeight - top[i][j] - bottom[i][j];
				if (diffW > 0) {
					width[i][j] = Math.min(width[i][j], diffW);
				}
				width[i][j] = Math.max(0, width[i][j]);
				if (diffH > 0) {
					height[i][j] = Math.min(height[i][j], diffH);
				}
				height[i][j] = Math.max(0, height[i][j]);
				if (width[i][j] == 0 || height[i][j] == 0) {
					throw new LayoutException("Width/Height is zero for: "
							+ reqComName + "@v" + reqVarNo);
				}
			}
		}

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				left[i][j] += pLeft;
				right[i][j] += pRight;
				top[i][j] += pTop;
				bottom[i][j] += pBottom;
			}
		}

		String just = (justStr != null) ? justStr[0][0]
				: LayoutConstants.VALUE_ALIGN_LEFT;

		String type = (typeStr != null) ? typeStr[0][0] : null;

		// Process the layout attributes
		Map layoutAttributeMap = LayoutXmlDataHelperFunctions
				.getLayoutAttributes(compData, reqVarNo);

		// Create the layout object and set it into the tree node object for the
		// component

		String componentType = LayoutXmlDataHelperFunctions
				.getXmlCompNodeAttrValue(compData,
						LayoutConstants.ATTR_COMPONENT_TYPE);
		Layout translatedLayout = null;

		if (componentType.equals(LayoutConstants.VALUE_COMP_TYPE_TEXT)) {
			translatedLayout = new TextLayout(rows, cols, just, type, context,
					layoutAttributeMap);

		} else {
			translatedLayout = new Layout(rows, cols);
		}

		translatedLayout.setLayout(left, right, top, bottom, width, height);

		tnd.setLayout(translatedLayout);
	}

	/**
	 * @param compNodeData
	 */
	private String handleMoreThanOneLayouts(String compNodeData) {
		setNewLayoutName(NONE);
		String modifiedComponentName = compNodeData;
		if (compNodeData.startsWith(NEW_LAYOUT_IDENTIFER)) {
			modifiedComponentName = extractComponentName(compNodeData);
			setNewLayoutName(extractNewLayoutName(compNodeData));
		}
		return modifiedComponentName;
	}

	private String extractNewLayoutName(String compNodeData) {
		return compNodeData.substring(1, compNodeData
				.lastIndexOf(NEW_LAYOUT_IDENTIFER));
	}

	/**
	 * @param none2
	 */
	private void setNewLayoutName(String layoutName) {
		this.layoutName = layoutName;
	}

	private String extractComponentName(String compNodeData) {
		int newLayoutEnd = compNodeData.lastIndexOf(NEW_LAYOUT_IDENTIFER);
		return compNodeData.substring(newLayoutEnd + 1);
	}

	/**
	 * Evaluates a string expression.
	 * 
	 * @param exprString
	 *            The mathematical expression string.
	 * @param pValue
	 *            The value of P for the context.
	 * @return The calculated value.
	 */
	private static double evaluateExpression(String exprString, int pValue) {

		double exprValue = 0;

		Pattern p = Pattern.compile("[+-/*]");
		Matcher m = p.matcher(exprString);
		int pos = 0;
		char prevOp = '+';

		while (m.find()) {
			String piece = (exprString.substring(pos, m.start())).trim();
			pos = m.end();

			if (piece == null || piece.length() < 1)
				continue;

			if (piece.equals(LayoutConstants.P))
				piece = "" + pValue;

			int pieceValue = Integer.parseInt(piece);

			switch (prevOp) {
			case '+':
				exprValue += pieceValue;
				break;
			case '-':
				exprValue -= pieceValue;
				break;
			case '*':
				exprValue *= pieceValue;
				break;
			case '/':
				exprValue /= pieceValue;
				break;
			default:
				break;
			}

			prevOp = m.group().charAt(0);
			;
		}

		String lastPiece = exprString.substring(pos).trim();
		if (lastPiece != null) {

			if (lastPiece.equals(LayoutConstants.P))
				lastPiece = "" + pValue;

			int pieceValue = Integer.parseInt(lastPiece);

			switch (prevOp) {
			case '+':
				exprValue += pieceValue;
				break;
			case '-':
				exprValue -= pieceValue;
				break;
			case '*':
				exprValue *= pieceValue;
				break;
			case '/':
				exprValue /= pieceValue;
				break;
			default:
				break;
			}
		}

		return exprValue;
	}

	/**
	 * @param locId
	 *            The location identifier string
	 * @return An array of localisation strings with each element contianing the
	 *         details of one component.
	 */
	private String[] getLCTokensFromLocId(String locId) {

		String sTg = "<" + LayoutConstants.TAG_LOCID_LAY_COMP_SEPERATOR + ">";
		String eTg = "</" + LayoutConstants.TAG_LOCID_LAY_COMP_SEPERATOR + ">";

		String[] tokens = locId.split(eTg);

		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = tokens[i].replaceAll(sTg, "");
		}

		return tokens;
	}

	/**
	 * @param locIdToken
	 *            One components data token - starts with <LC>and ends with
	 *            </LC>
	 * @param tokenName
	 *            The name of the token to be searched for
	 * @return The various numbers that the loc id string stores.
	 */
	private String getCompNameFromLocId(String locIdToken) {
		String reqData = null;
		int index = locIdToken.indexOf(LayoutConstants.S1);
		if (index != -1) {
			reqData = locIdToken.substring(0, index);
		}
		return reqData;
	}

	/**
	 * @param locIdToken
	 *            One components data token - starts with <LC>and ends with
	 *            </LC>
	 * @param tokenName
	 *            The name of the token to be searched for
	 * @param defaultValue
	 *            Returns the default value if no value is found in the given
	 *            string
	 * @return The various numbers that the loc id string stores.
	 */
	private String getNofromLocId(String locIdToken, String tokenName,
			String defaultValue) {
		String reqData = null;
		String optionIdentifer = tokenName + LayoutConstants.S2;
		int startIndex = locIdToken.indexOf(optionIdentifer);
		if (startIndex != -1) {
			int endIndex = locIdToken.indexOf(LayoutConstants.S3, startIndex);
			endIndex = (endIndex == -1) ? locIdToken.length() : endIndex;
			reqData = locIdToken.substring(startIndex + 2, endIndex);
		}

		if (reqData == null || reqData.trim().length() < 1)
			reqData = defaultValue;

		return reqData;
	}

	/**
	 * Private function to fetch the component data from the available xml data
	 * 
	 * @param The
	 *            component name whose details has to be fetched.
	 * @return The map returned by xml component data object for the given
	 *         component.
	 */
	private Map<String, Object> getDataForComponentName(String componentName)
			throws LayoutException {
		for (LayoutXmlData data : context.getXmlData()) {
			Map<String, Object> compData = data.getComponentData(componentName);
			if (compData != null && matchesLayoutName(data.getLayoutSetName())) {
				compData.put(LayoutConstants.LAYOUT_SET_NAME, data
						.getLayoutSetName());
				return compData;
			}
		}
		return null;
	}

	private boolean matchesLayoutName(String layoutName) {
		return this.layoutName.equals(NONE)
				|| layoutName.matches(layoutPattern());
	}

	private String layoutPattern() {
		return this.layoutName.replace("*", ".*");
	}

	private Map<String, Object> getDataForComponentName(String componentName,
			String topVarNo) throws LayoutException {
		for (LayoutXmlData data : context.getXmlData()) {
			Map<String, Object> compData = data.getComponentData(componentName);
			if (compData != null && matchesLayoutName(data.getLayoutSetName())) {
				Map parentInfo = LayoutXmlDataHelperFunctions
						.readParentInfo(compData);

				if (parentInfo != null && !parentInfo.containsKey(topVarNo)) {
					continue;
				}
				compData.put(LayoutConstants.LAYOUT_SET_NAME, data
						.getLayoutSetName());
				return compData;
			}
		}
		return null;
	}

	/**
	 * Private function to fetch the component data from the available xml data
	 * 
	 * @param componentId
	 *            The component id whose details has to be fetched.
	 * @return The map returned by xml component data object for the given
	 *         component.
	 */
	/*
	 * private Map<Object,Object> getDataForComponentId(String
	 * componentId) throws LayoutException { Map <Object,Object>compData = null;
	 * for (int i = 0; i < xmlDataReference.size(); i++) { LayoutXmlData
	 * nextData = (LayoutXmlData) xmlDataReference.get(i); String compName =
	 * nextData.getComponentName(componentId); if (compName != null) {
	 * compData.put(LayoutConstants.LAYOUT_SET_NAME,
	 * nextData.getLayoutSetName()); compData =
	 * nextData.getComponentData(compName); return compData; } } return null; }
	 * 
	 */
	/**
	 * Private function to fetch the component name from component id
	 * 
	 * @param componentId
	 *            The component id whose details has to be fetched.
	 * @param layoutSetName
	 * @return The name of the component whose id is given by componentId
	 */
	private String getNameForComponentId(String componentId,
			String layoutSetName) throws LayoutException {
		for (LayoutXmlData data : context.getXmlData()) {
			if (layoutSetName != null
					&& !data.getLayoutSetName().equals(layoutSetName))
				continue;

			String compName = data.getComponentName(componentId);
			if (compName != null) {
				return compName;
			}
		}

		return null;
	}

	/*
	 * 
	 *//**
	 * Debug function. Used to print the calculation tree hierarchy
	 * 
	 * @param The
	 *            node whose hierarchy has to be printed.
	 */

	private void printTree(DefaultMutableTreeNode n) {
		String padding = "";
		int depth = n.getLevel();
		for (int i = 0; i < depth; i++)
			padding += "\t";
		TreeNodeData tnd = (TreeNodeData) n.getUserObject();
		System.out.println(padding + tnd.getId());
		int childcount = n.getChildCount();
		for (int j = 0; j < childcount; j++) {
			DefaultMutableTreeNode nextChild = (DefaultMutableTreeNode) n
					.getChildAt(j);
			TreeNodeData tnd1 = (TreeNodeData) nextChild.getUserObject();
			System.out.println(padding + "\t" + tnd1.getId());
			if (nextChild.isLeaf() == false)
				printTree(nextChild);
		}
	}
	


}

/**
 * Data structure to hold component data in a tree
 */

class TreeNodeData {

	String id;

	Layout layoutData;

	/**
	 * Constructor
	 * 
	 * @param identifier
	 *            The identifier used to comparision (location id)
	 */
	TreeNodeData(String identifier) {
		this.id = identifier;
	}

	protected String getId() {
		return this.id;
	}

	protected void setLayout(Layout lData) {
		this.layoutData = lData;
	}

	protected Layout getLayout() {
		return this.layoutData;
	}

	public boolean equals(Object obj) {

		if (TreeNodeData.class.isInstance(obj)) {
			TreeNodeData param = (TreeNodeData) obj;

			String paramIdentifier = param.getId();
			if (this.getId().equalsIgnoreCase(paramIdentifier))
				return true;
		}

		return false;
	}

}