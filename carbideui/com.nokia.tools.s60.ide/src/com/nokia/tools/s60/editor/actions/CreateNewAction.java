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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroups;
import com.nokia.tools.media.utils.svg.ColorizeSVGFilter;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.s60.editor.commands.AddToGroupCommand;
import com.nokia.tools.s60.editor.ui.views.Messages;
import com.nokia.tools.s60.views.ColorsView;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.editing.BasicImageLayer;

public class CreateNewAction extends AbstractMultipleSelectionAction {

	private static final String ID = "createNew";
	private Map<RGB, ColorGroup> createdGroups;
	private ColorGroups grps;
	RGB colorBeforeAddingToGroup;
	private String targetGroupName;

	public CreateNewAction(IWorkbenchPart part) {
		super(part);
		setId(ID);
		setText("Create New");
		setLazyEnablementCalculation(true);
		super.init();
		
	}

	@Override
	protected void doRun(Object element) {

		IContentData data = getContentData(element);
		IContentData[] elements = null;
		CompoundCommand groupcommand = new CompoundCommand(
				Messages.Colors_Reference2Color);
		boolean colorCategory = false;
		if (data.getAllChildren().length > 0) {

			elements = data.getAllChildren();
			ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) elements[0]
					.getAdapter(ISkinnableEntityAdapter.class);

			if (skAdapter != null
					&& (skAdapter.isColour() || skAdapter.isColourIndication())) {
				colorCategory = true;
			}
		}
		if (!colorCategory) {
			elements = new IContentData[1];
			elements[0] = data;
		}

		if (null == grps) {
			grps = BasicImageLayer.getAvailableColors(data);
		}
		ColorGroup group = null;

		for (int i = 0; i < elements.length; i++) {
			IContentData data1 = elements[elements.length - i - 1];
			RGB formerColor = colorBeforeAddingToGroup;
			
			//Doing color checking. If the element selected is a color element, assign to new color.
			ISkinnableEntityAdapter skColorAdapter = (ISkinnableEntityAdapter) data1
                                              					.getAdapter(ISkinnableEntityAdapter.class);

   			if (skColorAdapter != null
   					&& (skColorAdapter.isColour() || skColorAdapter.isColourIndication())) {
   				formerColor = getColor(data1);	
   			}
       		//Color checking over
							
			

			/*if (colorCategory || null == colorBeforeAddingToGroup) {
				formerColor = getColor(data1);				 
			}*/

			if (data1 != null) {
				ILayer layer = null;

				if (colorCategory) {
					layer = getLayer(false, data1);
				} else if (layer == null) {
					layer = getLayer(false, element);
				}

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

						if (targetGroupName != null) {
							group = grps.getGroupByName(targetGroupName);
						} else {
							// group =
							// grps.getGroupByRGB(colorBeforeAddingToGroup);
						}

						if (isMultiLayer) {
							layerName = layer.getName();
						}

						boolean removeIfExists = false;
						if (data1 != null) {
							ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data1
									.getAdapter(ISkinnableEntityAdapter.class);
							if (skAdapter != null
									&& (skAdapter.isColour() || skAdapter
											.isColourIndication())) {
								removeIfExists = true;
							}
						}

						// open colors view when creating new color group
						if (group == null) {
							if (createdGroups != null) {
								group = createdGroups.get(formerColor);
							}
							if (group == null) {
								PlatformUI.getWorkbench()
										.getActiveWorkbenchWindow()
										.getActivePage().showView(
												ColorsView.ID, null,
												IWorkbenchPage.VIEW_VISIBLE);
							}
						}

						AddToGroupCommand cmd = new AddToGroupCommand(id,
								layerName, group, formerColor, grps,
								removeIfExists);
						groupcommand.add(cmd);
						// executeWithStack(null, null, cmd, false, null);

						if (group == null) {
							group = cmd.getCreatedGroup();
							if (createdGroups != null) {
								createdGroups.put(formerColor, group);
							}
						}

						RGB groupColor = group.getGroupColor();

						if (formerColor.equals(groupColor)) {
							final ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data1
									.getAdapter(ISkinnableEntityAdapter.class);
							if (skAdapter.isColour()
									|| skAdapter.isColourIndication()) {
								IColorAdapter adapter = (IColorAdapter) data1
										.getAdapter(IColorAdapter.class);
								Command c = adapter.getApplyColorCommand(
										ColorUtil.toColor(groupColor), false);
								groupcommand.add(c);
								// executeWithStack(null,
								// getScreenElement(data1), c,
								// false, null);
								// }

							} else {

								// returns absolute path
								String imageAbsolutePath = layer
										.getFileName(true);
								String outputFile = FileUtils
										.generateUniqueFileName(layer
												.getParent().getId(),
												IFileConstants.FILE_EXT_SVG);

								// colorize using SVGColorizeFilter
								ColorizeSVGFilter
										.colorizeSVGChangeSingleColor2(
												new File(imageAbsolutePath),
												new File(outputFile),
												groupColor, formerColor, 100);
								layer.paste(outputFile);
								updateGraphicWithCommand(layer, element);
							}
						}
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		}
		groupcommand.setLabel(Messages.Colors_Reference2Color);
		execute(groupcommand, null);
	}


	@Override
	protected boolean doCalculateEnabled(Object sel) {
		
		IContentData data = getContentData(sel);
		if (data == null) {
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
			//
			// if (layer == null) {
			// layer = getLayer(false, selection.getFirstElement());
			// }

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
								return false; // layer already exists in group
							}
						} else if (group.containsItemWithIdAndLayerName(id,
								null)
								&& group.getGroupColor().equals(
										colorBeforeAddingToGroup)) {
							return false; // image already exists in group
						}
					}
				} else {
					// if new one to create disable if the same rgb exist
					// ColorGroup group = grps
					// .getGroupByRGB(colorBeforeAddingToGroup);
					// if (null != group) {
					// return false;
					// }

				}
				return true;

			}

		}
		return false;
	}

	@Override
	protected void doRun(List<Object> element) {
		// checks the already created group in order to avoid create multiple
		// groups referencing the same color
		createdGroups = new HashMap<RGB, ColorGroup>();
		try {
			super.doRun(element);
		} finally {
			createdGroups = null;
		}
	}

	private RGB getColor(IContentData cdata) {
		// only regular skinnable entities can be shown in store
		IColorAdapter color = (IColorAdapter) cdata
				.getAdapter(IColorAdapter.class);
		if (color != null)
			return ColorUtil.toRGB(color.getColor());
		return null;

	}

}
