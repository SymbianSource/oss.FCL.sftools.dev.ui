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

import java.awt.datatransfer.Clipboard;
import java.io.File;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.svg.ColorizeSVGFilter;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.s60.editor.commands.PasteImageCommand;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;

/**
 * Colorizes SVG by altering SVG colors definition.
 */
public class ColorizeSvgAction2 extends AbstractMultipleSelectionAction {

	public static final String PREF_SVG_COLORIZING = "svgColorizing";

	public static boolean IS_ENABLED = true;

	static {
		IS_ENABLED = "true".equalsIgnoreCase(S60WorkspacePlugin.getDefault()
				.getPreferenceStore().getString(PREF_SVG_COLORIZING));
	}

	public static final String ID = "ColorizeSvgAction2";

	private RGB newColor;
	private RGB oldColor;
	private int alpha;

	private ILayer layer;

	@Override
	protected void init() {
		setId(ID);
		setText("Change color");
		setLazyEnablementCalculation(true);
	}

	public ColorizeSvgAction2(IWorkbenchPart part) {
		super(part);
	}

	public ColorizeSvgAction2(ISelectionProvider provider, CommandStack stack) {
		super(null);
		setSelectionProvider(provider);
		this.stack = stack;
	}

	public ColorizeSvgAction2(ISelectionProvider provider, CommandStack stack,
			ILayer layer) {
		super(null);
		setSelectionProvider(provider);
		this.stack = stack;
		this.layer = layer;
	}

	@Override
	public void doRun(final Object sel) {
		if (sel instanceof IAnimationFrame) {
			IAnimationFrame frame = (IAnimationFrame) sel;
			String imageAbsolutePath = frame.getImageFile().getAbsolutePath();
			String outputFile = FileUtils.generateUniqueFileName(frame.
					getParent().getId(), IFileConstants.FILE_EXT_SVG);

			// colorize using SVGColorizeFilter
			ColorizeSVGFilter.colorizeSVGChangeSingleColor2(new File(
					imageAbsolutePath), new File(outputFile), newColor,
					oldColor, alpha);
			try {
				ISelectionProvider sp = new ISelectionProvider() {
					public void addSelectionChangedListener(
							ISelectionChangedListener listener) {
					}

					public ISelection getSelection() {
						return new StructuredSelection(sel);
					}

					public void removeSelectionChangedListener(
							ISelectionChangedListener listener) {
					}

					public void setSelection(ISelection selection) {
					}
				};

				Clipboard clip = new Clipboard("");
				ClipboardHelper.copyImageToClipboard(clip, outputFile);
				
				new PasteImageAction(sp, stack, clip).run();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return;
		}
		
		IContentData data = getContentData(sel);

		if (data != null) {
			final ILayer layer;
			if (this.layer != null) {
				layer = this.layer;
			} else {
				layer = getLayer(false, sel);
			}

			if (layer != null) {
				// background layers are not colorized
				if (layer.isBackground()) {
					return;
				}

				try {
					final ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data
							.getAdapter(ISkinnableEntityAdapter.class);
					if (skAdapter.isColour()) { 
						// differently from SVG colors
						IColorAdapter adapter = (IColorAdapter) data
								.getAdapter(IColorAdapter.class);
						if (oldColor == null
								|| oldColor.equals(ColorUtil.toRGB(adapter
										.getColor()))) {
							Command c = adapter.getApplyColorCommand(ColorUtil
									.toColor(newColor), true);
							execute(c, null);
						}
						return;

					}

					// returns absolute path
					String imageAbsolutePath = layer.getFileName(true);
					String outputFile = FileUtils.generateUniqueFileName(layer
							.getParent().getId(), IFileConstants.FILE_EXT_SVG);

					
					// colorize using SVGColorizeFilter
					ColorizeSVGFilter.colorizeSVGChangeSingleColor2(new File(
							imageAbsolutePath), new File(outputFile), newColor,
							oldColor, alpha);
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
		if (!IS_ENABLED) {
			return false;
		}
		
		if (sel instanceof IAnimationFrame) {
			return ((IAnimationFrame) sel).isSvg();
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
		this.newColor = changingColor;
		this.oldColor = changedColor;
		this.alpha = alpha;
	}

}