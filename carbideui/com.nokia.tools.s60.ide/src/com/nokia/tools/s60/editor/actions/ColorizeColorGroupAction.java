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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroupItem;
import com.nokia.tools.media.utils.svg.ColorGroups;
import com.nokia.tools.media.utils.svg.ColorizeSVGFilter;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.s60.editor.commands.ChangeGroupColorCommand;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;

/**
 * Colorizes ColorGroup by altering colors definition.
 */
public class ColorizeColorGroupAction extends AbstractMultipleSelectionAction {

	public static final String ID = "ColorizeSvgAction2";

	private RGB changingColor;
	private RGB changedColor;
	private int alpha;
	private ColorGroup group;
	private ColorGroups grps;
	private ISelectionProvider elementForRefreshProvider;

	private List<IContentData> parent4refresh = new ArrayList<IContentData>();

	private boolean executedColorChanges;

	@Override
	protected void init() {
		setId(ID);
		setText("Change color");
		setLazyEnablementCalculation(true);
	}

	public ColorizeColorGroupAction(IWorkbenchPart part) {
		super(part);
	}

	public ColorizeColorGroupAction(ISelectionProvider provider,
			CommandStack stack, ISelectionProvider elementToRefresh) {
		super(null);
		setSelectionProvider(provider);
		this.stack = stack;
		this.elementForRefreshProvider = elementToRefresh;
	}

	@Override
	public void doRun(final Object sel) {

		if (!executedColorChanges) {
			
			ChangeGroupColorCommand cmd = new ChangeGroupColorCommand(group,
					changingColor, grps);
			execute(cmd, null);
			executedColorChanges = true;
		}

		IContentData data = getContentData(sel);

		if (data != null) {
			IImageAdapter imageAdapter = (IImageAdapter) data
					.getAdapter(IImageAdapter.class);
			IImage image = imageAdapter.getImage();
			final ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data
			.getAdapter(ISkinnableEntityAdapter.class);
			List<String> layerIds = new ArrayList<String>();
			String contentDataId = data.getId();
			if (contentDataId == null) {
				contentDataId = data.getName();
			}
			for (ColorGroupItem item : group.getGroupItems()) {
				if (contentDataId.equals(item.getItemId())) {
					layerIds.add(item.getImagePartOrLayer());
				}
			}
			List<ILayer> layers = new ArrayList<ILayer>();
						
			for (String layerId : layerIds) {
				
				if (image.getLayerCount() > 1) {
					layers.add(image.getLayer(layerId));
				} else {					
						layers.add(getLayer(false, sel));
				}
			}

			try {

				for (ILayer layer : layers) {
					if (layer != null) {

						if (layer.isBackground()) {
							return;
						}

						
						if (skAdapter.isColour()) {
							IColorAdapter adapter = (IColorAdapter) data
									.getAdapter(IColorAdapter.class);							
							Command c = adapter.getApplyColorCommand(ColorUtil
									.toColor(changingColor), true);
							execute(c, null);
							
							return;

						}

						// returns absolute path
						String imageAbsolutePath = layer.getFileName(true);
						String outputFile = FileUtils.generateUniqueFileName(
								layer.getParent().getId(),
								IFileConstants.FILE_EXT_SVG);

						// colorize using SVGColorizeFilter
						ColorizeSVGFilter.colorizeSVGChangeSingleColor2(
								new File(imageAbsolutePath), new File(
										outputFile), changingColor,
								changedColor, alpha);
						layer.paste(outputFile);
					}
				}

				if (layers.size() > 0) {
					updateGraphicWithCommand(layers.get(0), data);
				
					ILayer layer = layers.get(0);
					if (layer.getParent().isPart()) {
						if (!parent4refresh.contains(data.getParent()))
							parent4refresh.add(data.getParent());
					}
					parent4refresh.remove(data);
				}

			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	protected boolean doCalculateEnabled(Object sel) {
		if (!ColorizeSvgAction2.IS_ENABLED) {
			return false;
		}

		if (getLayer(false, sel) != null) {
			boolean isSvgImage = getLayer(false, sel).isSvgImage();
			boolean isColor = false;
			IContentData data = getContentData(sel);
			if (data != null) {
				isColor = ((ISkinnableEntityAdapter) data
						.getAdapter(ISkinnableEntityAdapter.class)).isColour();
			}
			return isSvgImage || isColor;
		} else
			return true;
	}

	public void setColor(RGB changingColor, RGB changedColor, int alpha) {
		this.changingColor = changingColor;
		this.changedColor = changedColor;
		this.alpha = alpha;
	}

	public void setColorGroup(ColorGroup grp) {
		this.group = grp;
	}

	public void setColorGroups(ColorGroups grps) {
		this.grps = grps;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.AbstractAction#getSelection()
	 */
	@Override
	protected IStructuredSelection getSelection() {
		IStructuredSelection selection = super.getSelection();
		if (selection.isEmpty()) {
			
			return new StructuredSelection(new Object());
		}
		return selection;
	}

}
