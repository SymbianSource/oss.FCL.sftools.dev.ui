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
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.ILayer;

/*******************************************************************************
 * extracts mask from SVG or PNG image
 */
public class ExtractMaskAction extends AbstractEditAction {

	public static final String HLP_CTX = "com.nokia.tools.s60.ide" + '.' + "ExtractMaskAction_context"; 

	public static final String ID = "ExtractMask"; 

	public ExtractMaskAction(ISelectionProvider provider, CommandStack stack) {
		super(provider, stack);
	}

	public ExtractMaskAction(IWorkbenchPart part) {
		super(part);
	}

	@Override
	public void doRun(Object element) {

		IImageHolder holder = getImageHolder(element);
		final boolean _holder = holder != null;

		ILayer layer = (ILayer) getLayer(true, element);
		final boolean _layer = layer != null;

		IAnimationFrame frame = (IAnimationFrame) (element instanceof IAnimationFrame ? element
				: null);
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
			CoreImage mask = CoreImage.create();

			if (_layer) {
				mask.init(layer.getMaskImage());
				rawImage = layer.getRAWImage();
			} else if (_holder) {
				mask.init(holder.getMask());
				rawImage = holder.getImage();
			} else if (_animation) {
				mask.init(frame.getMask());
				rawImage = frame.getRAWImage(false);
			}

			boolean softMask = _layer ? layer.supportSoftMask() : true;

			if (mask.getAwt() == null && rawImage != null)
				mask = CoreImage.create().init(rawImage).extractMask(softMask);

			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell();

			if (mask == null) {
				MessageDialog.openError(shell, "Error",
						Messages.ExtractMaskAction_error);
				return;
			}

			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			fd.setText(Messages.ExtractMaskAction_dialog_label);
			String[] filterExt = { "*.bmp" };
			fd.setFilterExtensions(filterExt);

			String elementName = null;
			if (_layer)
				elementName = layer.getParent().getId();
			if (_holder)
				elementName = holder.getImageFile().getName().substring(0,
						holder.getImageFile().getName().lastIndexOf('.'));
			if (_animation)
				elementName = frame.getParent().getId() + frame.getSeqNo();

			fd.setFileName(elementName + "_mask");
			String selected = fd.open();
			if (selected == null)
				return;
			if (mask != null) {
				if (!selected.toLowerCase().endsWith("bmp"))
					selected = selected + ".bmp";
				// save it
				try {
					mask.save(CoreImage.TYPE_BMP, new File(selected));
				} catch (Exception e) {
					e.printStackTrace();
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
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				ExtractMaskAction.HLP_CTX);
		super.init();
	}
}
