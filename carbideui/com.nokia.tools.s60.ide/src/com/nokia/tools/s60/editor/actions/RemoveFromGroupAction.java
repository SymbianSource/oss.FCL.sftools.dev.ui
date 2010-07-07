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

import java.util.List;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.RGB;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroups;
import com.nokia.tools.s60.editor.commands.RemoveFromGroupCommand;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.content.ThemeData;

/**
 * Removes item from the color group.
 */
public class RemoveFromGroupAction extends AbstractMultipleSelectionAction {
	public static final String ID = "removeFromGroup";

	public String text = "Remove from group:";

	// private ILayer layer;

	private ColorGroups grps;

	private TreeViewer treeViewerForRefreshing = null;

	private ColorGroup colorGroup;

	public RemoveFromGroupAction(TreeViewer treeViewer,
			ISelectionProvider provider, CommandStack commandStack) {
		super(null);
		setSelectionProvider(provider);
		this.treeViewerForRefreshing = treeViewer;
		this.stack = commandStack;
	}

	@Override
	protected void init() {
		setId(ID);
		setLazyEnablementCalculation(true);
	}

	private RGB getColor(IContentData cdata) {
		
		IColorAdapter color = (IColorAdapter) cdata
				.getAdapter(IColorAdapter.class);
		if (color != null)
			return ColorUtil.toRGB(color.getColor());
		return null;

	}

	@Override
	protected boolean doCalculateEnabled(Object sel) {
		IContentData data = getContentData(sel);

		if (data != null) {
			if (colorGroup == null) {
				ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data
						.getAdapter(ISkinnableEntityAdapter.class);
				if (skAdapter != null && !skAdapter.isColour()) {
					boolean hasColorEntity = false;
					for (IContentData data1 : ((ThemeData) data).getChildren()) {
						IColorAdapter color = (IColorAdapter) data1
								.getAdapter(IColorAdapter.class);
						if (color != null) {
							hasColorEntity = true;
							break;
						}
					}
					if (!hasColorEntity) {
						return false;
					}
				}

				colorGroup = grps.getGroupByItemId(data.getId());
				if (colorGroup == null) {
					for (IContentData data1 : data.getChildren()) {
						ColorGroup cg = grps.getGroupByItemId(data1.getId());
						if (colorGroup != null && cg != colorGroup) {
							return false;
						}
						colorGroup = cg;
					}
				}
				if (colorGroup != null) {
					setText(com.nokia.tools.s60.editor.ui.views.Messages.Colors_UnReferenceColor
							+ " '" + colorGroup.getName() + "'");
					return true;
				}
				return false;
			}

			ILayer layer = getLayer(false, sel);
			IStructuredSelection fakedSelection = null;
			if (grps.getGroupByItemId(data.getId()) == null
					&& data.getChildren().length > 0) {
				// from child inspection
				IContentData selectedChild = null;
				if (layer != null) {
					String id = layer.getParent().getId();
					if (id != null) {
						for (IContentData child : data.getChildren()) {
							if (id.equals(child.getId())) {
								selectedChild = child;
								break;
							}
						}
					}
				}
				if (selectedChild == null) {
					selectedChild = data.getChildren()[0];
				}
				fakedSelection = new StructuredSelection(selectedChild);
			}

			IStructuredSelection selection = fakedSelection != null ? fakedSelection
					: (treeViewerForRefreshing != null ? (IStructuredSelection) treeViewerForRefreshing
							.getSelection()
							: getSelection());
			ILayer tempLayer = layer = getLayer(false, selection
					.getFirstElement());
			IImageAdapter imageAdapter = (IImageAdapter) (fakedSelection == null ? data
					: (IContentData) fakedSelection.getFirstElement())
					.getAdapter(IImageAdapter.class);
			IImage image = imageAdapter.getImage();
			if ((layer == null)
					|| image.getName().equals(tempLayer.getParent().getName())) {
				layer = tempLayer;
			}

			if (layer != null) {

				if (layer.isBackground()) {
					return false;
				}

				String id = layer.getParent().getId();
				if (colorGroup != null) {
					if (colorGroup != null) {
						if (layer.getParent().getLayerCount() > 1) {
							if (colorGroup.containsItemWithIdAndLayerName(id,
									layer.getName())) {
								return true;
							}
						} else if (colorGroup.containsItemWithIdAndLayerName(
								id, null)) {
							return true;
						} else {
							return false;
						}
					}
				} else {
					return false;
				}

			} else if (selection.getFirstElement() instanceof Object[]) {
				Object[] obj = (Object[]) selection.getFirstElement();
				if (AbstractAction.TYPE_COLOR_GROUP.equals(obj[3])) {
					return true;
				}
			}

		}
		return false;
	}

	@Override
	protected void doRun(Object sel) {
		IContentData data = getContentData(sel);

		if (grps.getGroupByItemId(data.getId()) == null) {
			// from child inspection
			IContentData[] children = data.getChildren();
			if (children.length > 0) {
				for (IContentData child : children) {
					doRun(child);
				}
			} else {
				updateLayer(sel);
			}
		} else {
			updateLayer(sel);
		}
	}

	protected void updateLayer(Object sel) {
		ILayer layer = null;

		// layer = this.layer;
		if (layer == null) {
			layer = getLayer(false, sel);
		}

		// if (layer == null) { // for svg 9 piece
		// IStructuredSelection selection = treeViewerForRefreshing !=
		// null
		// ? (IStructuredSelection) treeViewerForRefreshing
		// .getSelection()
		// : getSelection();
		// layer = getLayer(false, selection.getFirstElement());
		// }

		if (layer != null) {

			if (layer.isBackground()) {
				return;
			}

			try {

				String id = layer.getParent().getId();

				boolean isMultiLayer = false;
				String layerName = null;
				if (layer.getParent().getLayerCount() > 1) {
					isMultiLayer = true;
				}
				if (isMultiLayer) {
					layerName = layer.getName();
				}

				if (colorGroup != null) {
					RemoveFromGroupCommand cmd = new RemoveFromGroupCommand(id,
							layerName, colorGroup, grps);
					execute(cmd, null);
				}

			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

	}

	protected void doRun(List<Object> element) {

		super.doRun(element);

		if (treeViewerForRefreshing != null) {
			treeViewerForRefreshing.refresh();
			treeViewerForRefreshing.expandAll();
		}
	}

	public void setColorGroup(ColorGroup group) {
		this.colorGroup = group;
	}

	public void setColorGroups(ColorGroups grps) {
		this.grps = grps;
	}

}
