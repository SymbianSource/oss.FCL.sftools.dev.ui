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

import java.awt.image.RenderedImage;
import java.io.File;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.s60.editor.commands.UndoableImageHolderActionCommand;
import com.nokia.tools.s60.editor.ui.dialogs.MaskDialog;

/*******************************************************************************
 * extracts mask from PNG image using color/wand mask dialog
 */
public class EditMaskAction2 extends AbstractEditAction {

	public static final String HLP_CTX = "com.nokia.tools.s60.ide" + '.' + "ExtractMaskAction2_context"; 

	public static final String ID = "ExtractMaskActionWand"; 

	private String maskPath = null;

	public EditMaskAction2(ISelectionProvider provider, CommandStack stack) {
		super(provider, stack);
	}

	public EditMaskAction2(IWorkbenchPart part) {
		super(part);
	}

	@Override
	public void doRun(Object element) {
		final IImageHolder holder = getImageHolder(element);
		final boolean _holder = holder != null;

		ILayer layer = (ILayer) getLayer(true, element);
		if (layer != null && layer.getParent() instanceof IAnimatedImage) {
			IAnimationFrame[] frames = ((IAnimatedImage) layer.getParent())
					.getAnimationFrames();
			if (frames.length > 0) {
				element = frames[0];
			}
		}

		final boolean _layer = layer != null
				&& !(layer.getParent() instanceof IAnimatedImage);

		IAnimationFrame frame = element instanceof IAnimationFrame ? (IAnimationFrame) element
				: null;

		final boolean _animation = frame != null;

		Rectangle bounds = new Rectangle();

		if (_layer) {
			bounds.width = layer.getParent().getWidth();
			bounds.height = layer.getParent().getHeight();
		}

		if (_layer && getGraphicalEditPart(element) != null) {
			bounds = ((AbstractGraphicalEditPart) getGraphicalEditPart(element))
					.getFigure().getBounds();
			if (bounds.width != layer.getParent().getWidth()
					|| bounds.height != layer.getParent().getHeight()) {
				// get proper scaled instance
				IImage image = layer.getParent().getAnotherInstance(
						bounds.width, bounds.height);
				layer = image.getLayer(layer.getName());
			}
		} else if (_holder) {
			bounds.width = holder.getWidth();
			bounds.height = holder.getHeight();
		} else if (_animation) {
			bounds.width = frame.getWidth();
			bounds.height = frame.getHeight();
		}

		try {

			RenderedImage rawImage = null;
			RenderedImage mask = null;

			if (_layer) {
				mask = layer.getMaskImage();
				rawImage = layer.getRAWImage();
			} else if (_holder) {
				mask = holder.getMask();
				rawImage = holder.getImage();
			} else if (_animation) {
				mask = frame.getMask();
				rawImage = frame.getRAWImage(false);
			}

			boolean softMask = _layer ? layer.supportSoftMask() : true;
			if (mask == null && rawImage != null)
				mask = CoreImage.create().init(rawImage).extractMask(softMask)
						.getAwt();

			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell();

			if (mask == null) {
				MessageDialog.openError(shell, "Error",
						Messages.ExtractMaskAction_error);
				return;
			}

			String imagePath = null;

			if (_layer) {
				imagePath = layer.getFileName(true);
				if (layer.hasMask())
					maskPath = layer.getMaskFileName(true);
			}
			if (_holder) {
				imagePath = holder.getImageFile().getAbsolutePath();
				if (holder.getMaskFile() != null)
					maskPath = holder.getMaskFile().getAbsolutePath();
			}
			if (_animation) {
				imagePath = frame.getImageFile().getAbsolutePath();
				if (frame.getMaskFile() != null)
					maskPath = frame.getMaskFile().getAbsolutePath();
			}

			boolean invertMask = false;
			if (_layer && layer.supportMask() && !layer.supportSoftMask()) {
				invertMask = true;
			}
			MaskDialog md = new MaskDialog(shell, imagePath, maskPath,
					invertMask);
			if (md.open() == Window.OK) {

				String elementName = null;
				if (_layer)
					elementName = layer.getParent().getId();
				if (_holder)
					elementName = holder.getImageFile().getName().substring(0,
							holder.getImageFile().getName().lastIndexOf('.'));
				if (_animation)
					elementName = frame.getParent().getId() + frame.getSeqNo();

				ImageData maskImageData = md.getMaskImageData();

				if (maskImageData == null) {
					return;
				} else {

					String tempDir = FileUtils.getTemporaryDirectory() + "mask"
							+ System.currentTimeMillis();
					maskPath = tempDir + File.separator
							+ new File(elementName).getName() + "." + "bmp";
					File file = new File(maskPath);
					file.getParentFile().mkdirs();
					file.deleteOnExit();

					ImageLoader imageLoader = new ImageLoader();
					imageLoader.data = new ImageData[] { maskImageData };
					imageLoader.save(maskPath, SWT.IMAGE_BMP);

					try {
						CoreImage.create().load(new File(maskPath)).save(
								CoreImage.TYPE_BMP, new File(maskPath));
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

				if (_holder) {
					if (maskPath != null) {
						EditPart ep = getEditPart(element);
						execute(new UndoableImageHolderActionCommand(holder,
								new Runnable() {
									public void run() {
										try {
											holder.pasteMask(maskPath);
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}), ep);
						return;
					}
				}
				if (layer != null) {
					try {
						if (_animation) {
							((IAnimatedImage) layer.getParent())
									.getAnimationFrames()[0]
									.pasteMask(maskPath);
						} else {
							layer.pasteMask(maskPath);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (getContentData(element) != null) {
						updateGraphicWithCommand(layer, element);
					}
				}
			}
		} catch (Throwable e) {
			handleProcessException(e);
		}

	}

	@Override
	protected boolean doCalculateEnabled(Object element) {
		ILayer layer = getLayer(false, element);
		//if (layer != null && !layer.getParent().isNinePiece()) {
		if (layer != null && !layer.getParent().isMultiPiece()) {
			boolean bitmap = layer.isBitmapImage();
			return bitmap && layer.supportMask();
		} else if (element instanceof IAnimationFrame) {
			IAnimationFrame frame = (IAnimationFrame) element;
			return frame.isBitmap() || frame.hasMask();
		}
		IImageHolder holder = getImageHolder(element);
		if (holder != null) {
			return holder.isBitmap() && holder.supportsMask();
		}
		return false;
	}

	@Override
	protected void init() {
		setId(ID);
		setText(Messages.ExtractMaskAction_name);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, HLP_CTX);
		super.init();
	}
}