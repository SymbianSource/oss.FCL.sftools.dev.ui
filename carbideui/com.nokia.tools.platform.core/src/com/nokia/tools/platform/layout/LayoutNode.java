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

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nokia.tools.platform.core.PlatformCorePlugin;

public class LayoutNode implements Cloneable {
	public static final Pattern LAYOUT_PATTERN = Pattern
			.compile("(\\w+)@(?:v:(\\d+))?(?:#r:(\\d+))?(?:#c:(\\d+))?");

	private LayoutContext context;
	private LayoutNode parent;
	private LayoutNode child;
	private String name;
	private int variety;
	private int row = 1;
	private int column = 1;

	// cached info
	private int maxRow = -1;
	private int maxColumn = -1;
	private int[] varieties;
	private String description;

	public LayoutNode(LayoutContext context) {
		this.context = context;
	}

	public LayoutNode(LayoutContext context, String str) {
		this(context);

		Matcher m = LAYOUT_PATTERN.matcher(str);
		if (m.matches()) {
			String name = m.group(1);
			String varietyId = m.group(2);
			String row = m.group(3);
			String column = m.group(4);
			setName(name);
			if (varietyId != null) {
				setVariety(Integer.parseInt(varietyId));
			}
			if (row != null) {
				setRow(Integer.parseInt(row));
			}
			if (column != null) {
				setColumn(Integer.parseInt(column));
			}
		}
	}

	/**
	 * @return the context
	 */
	public LayoutContext getContext() {
		return context;
	}

	public boolean isChildValid(LayoutNode child) {
		return child != this && child != null;
	}

	public void addChild(LayoutNode child) {
		if (!isChildValid(child)) {
			throw new IllegalArgumentException("Child is invalid");
		}
		LayoutNode oldChild = this.child;
		this.child = child;
		child.setParent(this);
		if (oldChild != null) {
			getLeaf().addChild(oldChild);
		}
	}

	public void removeChild(LayoutNode child) {
		LayoutNode nextChild = child.getChild();
		this.child = null;
		if (nextChild != null) {
			addChild(nextChild);
		}
		child.setParent(null);
		child.child = null;
	}

	public LayoutNode getChild() {
		return child;
	}

	public LayoutNode getRoot() {
		LayoutNode node = this;
		while (node.getParent() != null) {
			node = node.getParent();
		}
		return node;
	}

	public LayoutNode getLeaf() {
		LayoutNode node = this;
		while (node.getChild() != null) {
			node = node.getChild();
		}
		return node;
	}

	/**
	 * @return the parent
	 */
	public LayoutNode getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(LayoutNode parent) {
		this.parent = parent;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the variety
	 */
	public int getVariety() {
		return variety;
	}

	/**
	 * @param variety the variety to set
	 */
	public void setVariety(int variety) {
		if (Arrays.binarySearch(getVarieties(), variety) < 0) {
			throw new IllegalArgumentException("Variety is not valid: "
					+ getName() + "@v:" + variety);
		}
		this.variety = variety;
	}

	/**
	 * @return the row
	 */
	public int getRow() {
		return row;
	}

	/**
	 * @param row the row to set
	 */
	public void setRow(int row) {
		if (row < 0 || row > getMaxRow()) {
			throw new IllegalArgumentException("Row is out of range: (0, "
					+ getMaxRow() + ")");
		}
		this.row = row;
	}

	/**
	 * @return the column
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * @param column the column to set
	 */
	public void setColumn(int column) {
		if (column < 0 || column > getMaxColumn()) {
			throw new IllegalArgumentException("Row is out of range: (0, "
					+ getMaxColumn() + ")");
		}
		this.column = column;
	}

	public ComponentInfo getComponentInfo() {
		if (getRow() != 1 || getColumn() != 1) {
			return new ComponentInfo(getName(), getVariety(), getLocId(true));	
		} else {
			return new ComponentInfo(getName(), getVariety(), getLocId(false));
		}
	}

	public Rectangle getBounds() throws LayoutException {
		ComponentInfo component = getComponentInfo();
		context.calculate(component, false);
		if (component.getLayout() != null) {
			return component.getLayout().getBounds();
		}
		return new Rectangle();
	}

	public boolean isContainer() {
		try {
			for (LayoutXmlData data : context.getXmlData()) {
				Map<String, Object> compData = data.getComponentData(getName());
				if (compData != null) {
					CompactElement element = (CompactElement) compData
							.get(LayoutConstants.KEY_LAYOUT_XML_NODE);
					if (element != null) {
						String type = element
								.getAttribute(LayoutConstants.ATTR_COMPONENT_TYPE);
						if (LayoutConstants.VALUE_COMP_TYPE_TEXT
								.equalsIgnoreCase(type)
								|| LayoutConstants.VALUE_COMP_TYPE_GRAPHIC
										.equalsIgnoreCase(type)) {
							return false;
						}
					}
				}
			}
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
		}
		return true;
	}

	public synchronized int[] getVarieties() {
		if (varieties == null) {
			Set<Integer> set = new TreeSet<Integer>();
			try {
				for (LayoutXmlData data : context.getXmlData()) {
					Map<String, Object> compData = data
							.getComponentData(getName());
					if (compData != null) {
						Map layoutInfo = LayoutXmlDataHelperFunctions
								.readLayoutInfo(compData);
						for (Object variety : layoutInfo.keySet()) {
							set.add(new Integer((String) variety));
						}
					}
				}
			} catch (Exception e) {
				PlatformCorePlugin.error(e);
			}
			if (set.isEmpty()) {
				set.add(0);
			}
			varieties = new int[set.size()];
			int i = 0;
			for (int variety : set) {
				varieties[i++] = variety;
			}
		}
		return varieties;
	}

	public synchronized int getMaxRow() {
		if (maxRow < 0) {
			try {
				for (LayoutXmlData data : context.getXmlData()) {
					Map<String, Object> compData = data
							.getComponentData(getName());
					if (compData != null) {
						maxRow = LayoutXmlDataHelperFunctions.getRowCount(
								Integer.toString(variety), compData);
						break;
					}
				}
			} catch (Exception e) {
				PlatformCorePlugin.error(e);
			}
			if (maxRow < 0) {
				maxRow = 1;
			}
		}
		return maxRow;
	}

	public synchronized int getMaxColumn() {
		if (maxColumn < 0) {
			try {
				for (LayoutXmlData data : context.getXmlData()) {
					Map<String, Object> compData = data
							.getComponentData(getName());
					if (compData != null) {
						maxColumn = LayoutXmlDataHelperFunctions
								.getColumnCount(Integer.toString(variety),
										compData);
						break;
					}
				}
			} catch (Exception e) {
				PlatformCorePlugin.error(e);
			}
			if (maxColumn < 0) {
				maxColumn = 1;
			}
		}
		return maxColumn;
	}

	public LayoutNode copy() {
		LayoutNode root = getRoot();
		LayoutNode parent = null;
		LayoutNode node = null;
		while (root != null) {
			String locId = root.getLocId();
			LayoutNode clone = (LayoutNode) root.clone();
			clone.parent = null;
			clone.child = null;
			if (parent != null) {
				parent.addChild(clone);
			}
			parent = clone;
			if (root == this) {
				node = parent;
			}
			root = root.getChild();
		}
		return node;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

	public LayoutNode getNode(int variety) {
		try {
			LayoutNode node = (LayoutNode) clone();
			node.setVariety(variety);
			return node;
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
			return null;
		}
	}

	public LayoutNode getNode(int row, int column) {
		try {
			LayoutNode node = (LayoutNode) clone();
			node.setRow(row);
			node.setColumn(column);
			return node;
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
			return null;
		}
	}

	public LayoutNode getNode(int variety, int row, int column) {
		try {
			LayoutNode node = (LayoutNode) clone();
			node.setVariety(variety);
			node.setRow(row);
			node.setColumn(column);
			return node;
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
			return null;
		}
	}

	public synchronized String getDescription() {
		if (description == null) {
			try {
				for (LayoutXmlData data : context.getXmlData()) {
					Map<String, Object> compData = data
							.getComponentData(getName());
					if (compData != null) {
						CompactElement element = (CompactElement) compData
								.get(LayoutConstants.KEY_LAYOUT_XML_NODE);
						if (element != null) {
							List<CompactElement> info = element
									.getElementsByTagName(LayoutConstants.XMLTAG_ATTRIBUTE_INFO);
							if (!info.isEmpty()) {
								List<CompactElement> options = info.get(0)
										.getElementsByTagName(
												LayoutConstants.XMLTAG_OPTION);
								for (CompactElement option : options) {
									String str = option
											.getAttribute(LayoutConstants.ATTR_VARIETY);
									String name = option
											.getAttribute(LayoutConstants.ATTR_ATTR_NAME);
									if (str != null
											&& Integer.parseInt(str) == getVariety()) {
										description = name;
									}
								}
							}
						}
					}
				}
				if (description == null) {
					description = "";
				}
			} catch (Exception e) {
				PlatformCorePlugin.error(e);
			}
		}
		return description;
	}

	public String getLocId() {
		return getLocId(true);
	}

	public String getLocId(boolean includeLeaf) {
		StringBuilder sb = new StringBuilder();
		LayoutNode root = getRoot();
		while (root != null) {
			if (!includeLeaf && root == this) {
				break;
			}
			sb.append("<LC>" + root.getId() + "</LC>");
			if (root == this) {
				break;
			}
			root = root.getChild();
		}
		return sb.toString();
	}

	public String getId() {
		String shortId = getShortId();
		if (shortId.indexOf('@') < 0) {
			shortId += "@v:0";
		}
		return shortId;
	}

	public String getShortId() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName() + "@");
		if (getVariety() != 0) {
			sb.append("v:" + getVariety());
		}
		if (getRow() != 1 || getColumn() != 1) {
			sb.append("#r:" + getRow());
			sb.append("#c:" + getColumn());
		}
		if (sb.charAt(sb.length() - 1) == '@') {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	public String toString() {
		return getId();
	}

	public Map<String, LayoutNode> getAllNodes() {
		return context.getAllNodes();
	}

	private CompactElement getElementById(String id) {
		try {
			for (LayoutXmlData data : context.getXmlData()) {
				Collection<CompactElement> compData = data.getAllComponents();
				for (CompactElement compactElement : compData) {
					String cid = compactElement
							.getAttribute(LayoutConstants.ATTR_COMPONENT_ID);
					if (cid != null && cid.equals(id)) {
						return compactElement;
					}
				}
			}
		} catch (LayoutException e) {
			PlatformCorePlugin.error(e);
		}
		return null;
	}

	public LayoutNode getDefaultLayoutTree() {
		List<LayoutNode> nodes = new ArrayList<LayoutNode>();
		LayoutNode clone = (LayoutNode) this.clone();
		clone.setRow(Integer.parseInt(LayoutConstants.DEFAULT_ROW_NO));
		clone.setColumn(Integer.parseInt(LayoutConstants.DEFAULT_COLUMN_NO));
		clone.child = null;
		nodes.add(clone);

		String id = null;
		try {
			for (LayoutXmlData data : context.getXmlData()) {
				Map<String, Object> compData = data.getComponentData(getName());
				if (compData != null) {
					CompactElement element = (CompactElement) compData
							.get(LayoutConstants.KEY_LAYOUT_XML_NODE);
					if (element != null) {
						id = element
								.getAttribute(LayoutConstants.ATTR_COMPONENT_ID);
						if (id != null) {
							int variety = getVariety();

							boolean _continue = true;
							while (_continue) {
								_continue = false;

								List<CompactElement> parentGroupNode = element
										.getElementsByTagName(LayoutConstants.XMLTAG_PARENT_INFO);
								if (parentGroupNode.size() != 1) {
									// screen root, ignores the exception
									break;
								}

								Map parentInfo = LayoutXmlDataHelperFunctions
										.readParentInfo(compData);
								List values = (List) parentInfo.get(""
										+ variety);
								if (values != null && values.size() == 1) {
									Map map = (Map) values.get(0);
									String pId = (String) map
											.get(LayoutConstants.ATTR_COMPONENT_ID);
									String pVariety = (String) map
											.get(LayoutConstants.ATTR_VARIETY);
									CompactElement parentElem = getElementById(pId);
									if (parentElem != null) {
										String pName = parentElem
												.getAttribute(LayoutConstants.ATTR_COMPONENT_NAME);
										LayoutNode node = new LayoutNode(
												context);
										node.setName(pName);
										node.setVariety(Integer
												.parseInt(pVariety));

										compData = data.getComponentData(node
												.getName());
										element = parentElem;
										variety = node.getVariety();

										LayoutNode child = nodes.get(0);
										node.addChild(child);

										nodes.add(0, node);

										_continue = true;
									}
								}
							}

							break;
						}
					}
				}
			}
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
		}

		return nodes.get(0);
	}

	public Map<String, LayoutNode> getAllChildNodes() {
		Map<String, LayoutNode> nodes = new TreeMap<String, LayoutNode>();
		try {
			String id = null;
			for (LayoutXmlData data : context.getXmlData()) {
				Map<String, Object> compData = data.getComponentData(getName());
				if (compData != null) {
					CompactElement element = (CompactElement) compData
							.get(LayoutConstants.KEY_LAYOUT_XML_NODE);
					if (element != null) {
						id = element
								.getAttribute(LayoutConstants.ATTR_COMPONENT_ID);
						if (id != null) {
							break;
						}
					}
				}
			}
			if (id != null) {
				for (LayoutXmlData data : context.getXmlData()) {
					for (CompactElement element : data.getAllComponents()) {
						String name = element
								.getAttribute(LayoutConstants.ATTR_COMPONENT_NAME);
						if (nodes.containsKey(name)) {
							continue;
						}
						Map<String, Object> compData = data
								.getComponentData(name);
						List<CompactElement> parentGroupNode = element
								.getElementsByTagName(LayoutConstants.XMLTAG_PARENT_INFO);
						if (parentGroupNode.size() != 1) {
							continue;
						}
						Map layout = LayoutXmlDataHelperFunctions
								.readLayoutInfo(compData);
						if (layout == null || layout.isEmpty()) {
							continue;
						}
						Map parentInfo = LayoutXmlDataHelperFunctions
								.readParentInfo(compData);
						boolean isChild = false;
						for (Object details : parentInfo.values()) {
							for (Object map : (List) details) {
								String parentId = (String) ((Map) map)
										.get(LayoutConstants.ATTR_COMPONENT_ID);
								if (id.equals(parentId)) {
									isChild = true;
									break;
								}
							}
						}
						if (isChild) {
							LayoutNode node = new LayoutNode(context);
							node.setName(name);
							String[] varieties = (String[]) layout.keySet()
									.toArray(new String[layout.size()]);
							Arrays.sort(varieties);
							node.setVariety(Integer.parseInt(varieties[0]));
							nodes.put(name, node);
						}
					}
				}
			}
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
		}
		return nodes;
	}

	public boolean equalsTo(LayoutNode node) {
		if (node == null) {
			return false;
		}

		if (!getShortId().equals(node.getShortId())) {
			return false;
		}

		if (getChild() != null) {
			return getChild().equalsTo(node.getChild());

		}

		if (node.getChild() != null) {
			return false;
		}

		return true;
	}
}
