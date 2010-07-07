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
package com.nokia.tools.s60.editor.actions;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroups;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.editing.BasicImageLayer;

public class ColorGroupAction extends AbstractMultipleSelectionAction {

	private IWorkbenchPart part;

	private final String ID = "colorGroups";

	private String targetGroupName;

	private ColorGroups grps;

	public ColorGroupAction(IWorkbenchPart part) {
		super(part);
		setId(ID);
		this.part = part;
		setMenuCreator(new MyMenuCreator());
		setText("Colors");
	}

	@Override
	protected void doRun(Object element) {
		

	}

	@Override
	protected boolean doCalculateEnabled(Object sel) {
		IContentData data = getContentData(sel);
		if (data == null) {
			return false;
		}
		ColorGroups grps = BasicImageLayer.getAvailableColors(data);
		if(grps == null || grps.getGroups() == null ||
				grps.getGroups().size() <= 0){
			return false;
		}

		
		if (data.getAllChildren().length > 0) {
			ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data
					.getAllChildren()[0]
					.getAdapter(ISkinnableEntityAdapter.class);
			if (skAdapter != null
					&& (skAdapter.isColour() || skAdapter.isColourIndication()))
				return true;
		}

		RGB colorBeforeAddingToGroup = null;
		// for Colors
		if (null == colorBeforeAddingToGroup) {
			if (data != null) {
				ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data
						.getAdapter(ISkinnableEntityAdapter.class);
				if (skAdapter != null
						&& !(skAdapter.isColour() || skAdapter
								.isColourIndication())) {
					return false;
				}
			}

			colorBeforeAddingToGroup = getColor(data);
		}

		if (null == grps) {
			grps = BasicImageLayer.getAvailableColors(data);
		}

		if (data != null) {
			ILayer layer;

			layer = getLayer(false, sel);
			if (layer != null) {

				if (layer.isBackground()) {
					return false;
				}

				String id = layer.getParent().getId();
				if (targetGroupName != null) {
					ColorGroup group = grps.getGroupByName(targetGroupName);

					if (group != null) {
						if (layer.getParent().getLayerCount() > 1) {
							if (group.containsItemWithIdAndLayerName(id, layer
									.getName())
									&& group.getGroupColor().equals(
											colorBeforeAddingToGroup)) {
								return false; 
							}
						} else if (group.containsItemWithIdAndLayerName(id,
								null)
								&& group.getGroupColor().equals(
										colorBeforeAddingToGroup)) {
							return false; // image already exists in group
						}
					}
				} else {
					

				}
				return true;

			}

		}
		return false;

	}
	private RGB getColor(IContentData cdata) {
		
		IColorAdapter color = (IColorAdapter) cdata
				.getAdapter(IColorAdapter.class);
		if (color != null)
			return ColorUtil.toRGB(color.getColor());
		return null;

	}
	class MyMenuCreator implements IMenuCreator {

		public void dispose() {

		}

		public Menu getMenu(Control parent) {
			return null;
		}

		/**
		 * Fill menu with created colors.
		 */
		public Menu getMenu(Menu parent) {
			
			Menu m = addActions(parent);
			
			return m;
		}

		private Menu addActions(Menu menu) {
			
			IContentData data = JEMUtil.getContentData(getSelection());
			ColorGroups grps = BasicImageLayer.getAvailableColors(data);
			
			Menu m = null;
			
			if (grps != null) {
				m = new Menu(menu);
				if (grps.getGroups().size() > 0) {
					new MenuItem(m, SWT.SEPARATOR);
					for (ColorGroup group : grps.getGroups()) {
						AddToColorGrpsAction addAction = new AddToColorGrpsAction(
								getWorkbenchPart());
						addAction.setGroupName(group == null ? null : group
								.getName());
						addAction.setColorGroups(grps);
						addAction(m, addAction);
					}

					RemoveFromGroupAction removeAction = new RemoveFromGroupAction(
							null, getSelectionProvider(), stack);
					removeAction.setColorGroups(grps);

					if (removeAction.isEnabled()) {
						new MenuItem(m, SWT.SEPARATOR);
						addAction(m, removeAction);
					}
					new MenuItem(m, SWT.SEPARATOR);
				}

			}
			return m;
		}

		private void addAction(Menu menu, AbstractMultipleSelectionAction action) {
			ActionContributionItem item = new ActionContributionItem(action);
			action.setMenuCreator(null);
			item.fill(menu, -1);
		}

	}

}
