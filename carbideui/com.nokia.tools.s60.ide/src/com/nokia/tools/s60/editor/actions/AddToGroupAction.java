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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.image.RenderedImageDescriptor;
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
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.editing.BasicImageLayer;

/**
 * 
 * Adds items containing given color to the group, if the color of the items is
 * different from group, the color is changed to correspond with group color.
 * 
 */

public class AddToGroupAction extends AbstractMultipleSelectionAction implements
		IMenuCreator {

	public static final String ID = "addToGroup"; // ActionFactory.COPY.getId();

	private String targetGroupName;

	private RGB colorBeforeAddingToGroup;

	private ColorGroups grps;
	
	private Map<RGB, ColorGroup> createdGroups;

	// private ILayer layer;

	public AddToGroupAction(IWorkbenchPart part) {
		super(part);
	}
	
	

	@Override
	public void runWithEvent(Event event) {		
		run();
	}



	public AddToGroupAction(IStructuredSelection selection,
			ISelectionProvider provider, CommandStack commandStack) {
		super(null);
		setSelectionProvider(provider);
		this.stack = commandStack;
		this.selection = selection;
	}

	public AddToGroupAction(ISelectionProvider provider, CommandStack stack) {
		super(null);
		setSelectionProvider(provider);
		this.stack = stack;
		setGroupName(null);

	}

	@Override
	protected void init() {
		setId(ID);
		setText("Create New");
		setLazyEnablementCalculation(true);
		super.init();
		
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
					

				}
				return true;

			}

		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.AbstractMultipleSelectionAction#doRun(java.util.List)
	 */
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

	@Override
	protected void doRun(Object sel) {
		IContentData data = getContentData(sel);
		boolean isAColor = false;
		
		
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
			RGB layerColor = null;
			RGB formerColor = colorBeforeAddingToGroup;

			if (colorCategory || null == colorBeforeAddingToGroup) {
				formerColor = getColor(data1);
			}

			if (data1 != null) {
				ILayer layer = null;

				if (colorCategory) {
					layer = getLayer(false, data1);
				} else if (layer == null) {
					layer = getLayer(false, sel);
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
								
								if (colorBeforeAddingToGroup.equals(layerColor)) {	
									removeIfExists = true;
								}
								isAColor = true;
								
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

						IColorAdapter adapterColor = (IColorAdapter) data1
						.getAdapter(IColorAdapter.class);
						if ((adapterColor != null) &&  (adapterColor.getColor() != null))
							layerColor = ColorUtil.toRGB(adapterColor.getColor());							
					
					
							AddToGroupCommand cmd = new AddToGroupCommand(id,
									layerName, group, formerColor, grps,
									removeIfExists);
							if ((layerColor == null) || colorBeforeAddingToGroup.equals(layerColor))
								groupcommand.add(cmd);
							
							if (group == null) {
								group = cmd.getCreatedGroup();
								if (createdGroups != null) {
									createdGroups.put(formerColor, group);
								}
							}
												

						RGB groupColor = group.getGroupColor();

						//if (!formerColor.equals(groupColor)) {
							final ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data1
									.getAdapter(ISkinnableEntityAdapter.class);
							if (skAdapter.isColour()
									|| skAdapter.isColourIndication()) {
								IColorAdapter adapter = (IColorAdapter) data1
										.getAdapter(IColorAdapter.class);
								Command c = adapter.getApplyColorCommand(
										ColorUtil.toColor(groupColor), true);
								layerColor = ColorUtil.toRGB(adapter.getColor());
								if (isAColor && formerColor.equals(layerColor)) 
									groupcommand.add(c);
															
								
								
							} else {
								boolean canProceedWithSkinning = true;
								if ((layer != null) && (layer.getColors() != null)) {
									boolean isSelectedFormerColorUsed = layer.getColors().containsKey(formerColor);
									if (!isSelectedFormerColorUsed) canProceedWithSkinning = false;
								}
								
								if (canProceedWithSkinning) {
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
									updateGraphicWithCommand(layer, sel);
								}
							}
						
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		}
		groupcommand.setLabel(Messages.Colors_Reference2Color);
		if (!groupcommand.isEmpty())
			execute(groupcommand, null);
	}

	public void setColor(RGB color) {
		this.colorBeforeAddingToGroup = color;
	}

	public void setGroupName(String groupName) {
		this.targetGroupName = groupName;
		setText(((null == groupName) ? Messages.Colors_CreateNewLogicalColor
				: groupName));
	}

	public void setColorGroups(ColorGroups grps) {
		this.grps = grps;
		// adding the color icon in the menu
		ImageDescriptor desc = null;
		ColorGroup cg = grps == null ? null : grps
				.getGroupByName(targetGroupName);
		if (cg == null) {
			// otherwise npe destroys the context menu of layers page
			return;
		}
		RGB rgb = cg.getGroupColor();
		PaletteData paletteData = new PaletteData(new RGB[] { rgb });
		ImageData imageData = new ImageData(10, 10, 1, paletteData);
		desc = ((ImageDescriptor.createFromImageData(imageData)));

		if (grps.getGroupByName(targetGroupName).getGroupItems().size() == 0) {// unused
			// color
			setImageDescriptor(desc);
		} else {// used(linked) colour
			setImageDescriptor(getDescriptorForOvalImage(rgb));

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

	private ImageDescriptor getDescriptorForOvalImage(RGB rgb) {
		int transparentPixel = 0;
		int minSize = 0;
		int maxSize = 15;
		BufferedImage image = getOvalImage(rgb, minSize, maxSize);
		ImageDescriptor desc = new RenderedImageDescriptor(image, image
				.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB,
				transparentPixel);
		return desc;
	}

	private BufferedImage getOvalImage(RGB rgb, int minSize, int maxSize) {
		BufferedImage image = new BufferedImage(maxSize, maxSize,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		// draw an oval with color having the given rgb combination
		graphics.setColor(new Color(rgb.red, rgb.green, rgb.blue));
		graphics.fillOval(minSize, minSize, maxSize - 3, maxSize - 3);
		return image;
	}

	public Menu getMenu(Control parent) {
		return null;
	}

	/**
	 * Fill menu with created colors.
	 */
	public Menu getMenu(Menu parent) {
		addActions(parent);
		return null;
	}

	private void addActions(Menu menu) {

		IContentData data = JEMUtil.getContentData(getSelection());
		ColorGroups grps = BasicImageLayer.getAvailableColors(data);
		if (grps != null) {
			if (grps.getGroups().size() > 0) {
				new MenuItem(menu, SWT.SEPARATOR);
				for (ColorGroup group : grps.getGroups()) {
					AddToGroupAction addAction = new AddToGroupAction(
							getWorkbenchPart());
					addAction.setGroupName(group == null ? null : group
							.getName());
					addAction.setColorGroups(grps);
					addAction(menu, addAction);
				}

				RemoveFromGroupAction removeAction = new RemoveFromGroupAction(
						null, getSelectionProvider(), stack);
				removeAction.setColorGroups(grps);

				if (removeAction.isEnabled()) {
					new MenuItem(menu, SWT.SEPARATOR);
					addAction(menu, removeAction);
				}
				new MenuItem(menu, SWT.SEPARATOR);
			}

		}
	}

	private void addAction(Menu menu, AbstractMultipleSelectionAction action) {
		ActionContributionItem item = new ActionContributionItem(action);
		action.setMenuCreator(null);
		item.fill(menu, -1);
	}

}
