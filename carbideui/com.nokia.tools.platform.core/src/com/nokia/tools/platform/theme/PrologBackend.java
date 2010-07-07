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

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

public class PrologBackend {

	public static void setTransferSelection(DefaultMutableTreeNode node,
			String prolog) {
		int count = node.getChildCount();
		for (int i = 0; i < count; i++) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node
					.getChildAt(i);
			TaskListDataObject myObject = (TaskListDataObject) childNode
					.getUserObject();
			ThemeBasicData tbd = myObject.getSkinData();
			if (myObject.isSelected()) {
				if (prolog == ThemeConstants.PROLOG)
					setSelected(tbd, true, childNode.getChildCount());
				else if (prolog == ThemeConstants.CHECKOUT)
					tbd.setSelectionForTransfer(true);
			} else {
				if (prolog == ThemeConstants.PROLOG)
					setSelected(tbd, false, childNode.getChildCount());
				else if (prolog == ThemeConstants.CHECKOUT)
					tbd.setSelectionForTransfer(false);
			}
			setTransferSelection(childNode, prolog);
		}
	}

	private static void setSelected(ThemeBasicData tbd, boolean bool,
			int children) {
		tbd.setSelected(bool);
		if (children == 0) {
			if ((tbd instanceof ComponentGroup) || (tbd instanceof Task)) {
				List l = tbd.getChildren();
				if (l == null || l.size() == 0)
					return;
				for (int i = 0; i < l.size(); i++) {
					ThemeBasicData tbd1 = (ThemeBasicData) l.get(i);
					int childCount = 0;
					if (tbd1 instanceof Component)
						childCount = 1;
					setSelected(tbd1, bool, childCount);
				}
			}
		}
		if (tbd instanceof Component) {
			List l = tbd.getChildren();
			if (l == null || l.size() == 0)
				return;
			for (int i = 0; i < l.size(); i++) {
				ThemeBasicData tbd1 = (ThemeBasicData) l.get(i);
				tbd1.setSelected(bool);
			}
		}
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// test code

	// public static void setDoneStatus (ThemeBasicData t) {
	// if( t == null) return;
	// List l = t.getChildren();
	// if( (l == null) || (l.size() == 0) ) {
	// t.setSkinned(true);
	// return;
	// }
	// for( int i = 0; i< l.size(); i++) {
	// ThemeBasicData s = (ThemeBasicData) l.get(i);
	// String type = "SE";
	// if( s instanceof Task)
	// type = "Task";
	// else if( s instanceof ComponentGroup)
	// type = "CG";
	// else if( s instanceof Component)
	// type = "Comp";
	// System.out.println(t.getIdentifier() + " GEtting status before = " +
	// s.getSkinnedStatus() + "type =" + type);
	// s.setSkinned(true);
	// System.out.println(t.getIdentifier() + " GEtting status after = " +
	// s.getSkinnedStatus()+ "type =" + type);
	// setDoneStatus(s);
	// }
	// }
	// public static void testDoneStatus (Object obj) {
	// S60Theme skin = (S60Theme)obj;
	// List l = skin.getChildren();
	// for( int i = 0; i<4; i++) {
	// ThemeBasicData s = (ThemeBasicData) l.get(i);
	// setDoneStatus(s);
	// }
	// }

	// test code
	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	/**
	 * This method returns a tree node. It is used to get the root node. Based
	 * on the value of the boolean variable 'expanded' child nodes under each
	 * Theme node are populated
	 */
	public static DefaultMutableTreeNode getRootNode(Object object)
			throws ThemeException {
		String rootNodeString = "My Skin";
		Theme skin = (Theme) object;
		TaskListDataObject myObject = new TaskListDataObject(rootNodeString,
				false);
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(myObject);
		getTaskNodes(rootNode, skin);
		return rootNode;
	}

	/**
	 * This method obtains all the task nodes from the ThemeInformation class
	 * and add them to the root node
	 */

	public static void getTaskNodes(DefaultMutableTreeNode node, Theme skin) {
		ThemeBasicData tasksbd = null;
		List list = skin.getChildren();
		Object[] tasksbdArray = list.toArray();
		TaskListDataObject myObject = null;
		DefaultMutableTreeNode childNode = null;
		for (int i = 0, j = 0; i < tasksbdArray.length; ++i) {
			tasksbd = (ThemeBasicData) tasksbdArray[i];
			boolean a;
			if (tasksbd.isAnyChildDone())
				a = true;
			else
				a = false;
			if (tasksbd != null) {
				if (tasksbd.isShown()) {
					++j;
					if (j < 10) {
						myObject = new TaskListDataObject("0" + (j) + "    "
								+ tasksbd.getThemeName(), a);
					} else
						myObject = new TaskListDataObject((j) + "    "
								+ tasksbd.getThemeName(), a);
					myObject.setSkinData(tasksbd);
					childNode = new DefaultMutableTreeNode(myObject);
					node.add(childNode);
					getChildren(childNode);
				}
			}
		}
	}

	/**
	 * Adds children to a node. It extracts information from XML file and forms
	 * the child nodes and adds it to the parent node. componentgroup
	 * 
	 * @param node, the parent node to which this will be added
	 */
	public static void getChildren(DefaultMutableTreeNode node) {
		TaskListDataObject myObject = (TaskListDataObject) node.getUserObject();
		ThemeBasicData parentsbd = (ThemeBasicData) myObject.getSkinData();
		ThemeBasicData sbd = null;
		if (parentsbd != null) {
			List list = parentsbd.getChildren();
			DefaultMutableTreeNode childNode = null;
			if (list != null) {

				Object[] sbdArray = list.toArray();

				for (int i = 0; sbdArray != null && i < sbdArray.length; ++i) {
					sbd = (ThemeBasicData) sbdArray[i];
					boolean b;
					if (sbd.isAnyChildDone())
						b = true;
					else
						b = false;
					if (sbd != null) {
						if (sbd.isShown()
								&& !(sbd.getThemeName()
										.equalsIgnoreCase(parentsbd
												.getThemeName()))) {
							myObject = new TaskListDataObject(sbd
									.getThemeName(), b);
							myObject.setSkinData(sbd);
							childNode = new DefaultMutableTreeNode(myObject);
							node.add(childNode);
							getComponentnode(childNode);
						}
					}
				}
			}
		}
	}

	public static void getComponentnode(DefaultMutableTreeNode node) {

		TaskListDataObject myObject = (TaskListDataObject) node.getUserObject();
		ThemeBasicData parentsbd = (ThemeBasicData) myObject.getSkinData();
		ToolBox tb = ((ComponentGroup) parentsbd).getToolBox();

		DefaultMutableTreeNode childNode = null;
		ThemeBasicData sbd = null;

		if (parentsbd != null) {
			java.util.List list = parentsbd.getChildren();
			Object[] sbdArray = list.toArray();
			for (int i = 0; sbdArray != null && i < sbdArray.length; ++i) {
				sbd = (ThemeBasicData) sbdArray[i];
				boolean c;
				if (sbd.isAnyChildDone())
					c = true;
				else
					c = false;
				if (sbd != null) {
					if ((sbd.isShown() && !(sbd.getThemeName()
							.equalsIgnoreCase(parentsbd.getThemeName())))) {
						myObject = new TaskListDataObject(sbd.getThemeName(), c);
						myObject.setSkinData(sbd);
						childNode = new DefaultMutableTreeNode(myObject);
						node.add(childNode);
					}
				}
				if (tb.SameComponent)
					break;
			}
		}
	}

	public static void setSelectionForAll(DefaultMutableTreeNode node,
			boolean bool) {
		int count = node.getChildCount();
		for (int i = 0; i < count; i++) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node
					.getChildAt(i);
			TaskListDataObject myObject = (TaskListDataObject) childNode
					.getUserObject();
			myObject.setSelected(bool);
			setSelectionForAll(childNode, bool);
		}
	}
}
