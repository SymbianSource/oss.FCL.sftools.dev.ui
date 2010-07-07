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

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.svg.ColorizeSVGFilter;
import com.nokia.tools.resource.util.FileUtils;

/**
 * Colorizes SVG by altering SVG colors definition.
 */
public class ColorizeSvgAction extends AbstractAction {

	public static final String ID = "ColorizeSvgAction";
	
	private RGB color;
	private int alpha;
	
	@Override
	protected void init() {
		setId(ID);
		setText("Colorize SVG");
		setLazyEnablementCalculation(true);
	}

	public ColorizeSvgAction(IWorkbenchPart part) {
		super(part);
	}

	public ColorizeSvgAction(ISelectionProvider provider, CommandStack stack) {
		super(null);
		setSelectionProvider(provider);
		this.stack = stack;
	}

	@Override
	public void doRun(final Object sel) {
		IContentData data = getContentData(sel);

		if (data != null) {

			final ILayer layer = getLayer(false, sel);

			if (layer != null) {

				if (layer.isBackground()) {
					return;
				}

				try {

					// returns absolute path
					String imageAbsolutePath = layer.getFileName(true);
					String outputFile = FileUtils.generateUniqueFileName(layer
							.getParent().getId(), IFileConstants.FILE_EXT_SVG);

					// colorize using SVGColorizeFilter
					ColorizeSVGFilter.colorizeSVG(new File(imageAbsolutePath),
							new File(outputFile), color, alpha);
					layer.paste(outputFile);
					updateGraphicWithCommand(layer, sel);
					
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}

		}
	}

	@Override
	protected boolean doCalculateEnabled(Object sel) {
		if (!ColorizeSvgAction2.IS_ENABLED) {
			return false;
		}
		
		return getLayer(false, sel).isSvgImage();
	}
	
	public void setColor(RGB color, int alpha) {
		this.color = color;
		this.alpha = alpha;
	}

}
