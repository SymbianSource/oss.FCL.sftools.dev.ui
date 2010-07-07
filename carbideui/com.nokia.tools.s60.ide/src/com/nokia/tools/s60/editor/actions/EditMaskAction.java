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

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.BaseRunnable;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.RunnableWithParameter;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.s60.editor.commands.UndoableImageHolderActionCommand;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.ui.utils.FileChangeWatchThread;

/*******************************************************************************
 * edit mask in external editor
 */
public class EditMaskAction extends AbstractEditAction {

	public static final String EDIT_3RD_BITMAP_CONTEXT = "com.nokia.tools.s60.ide" + '.' + "EditMask_context"; 

	public static final String ID = "EditMask"; 

	public EditMaskAction(ISelectionProvider provider, CommandStack stack) {
		super(provider, stack);
	}

	public EditMaskAction(IWorkbenchPart part) {
		super(part);
	}

	@Override
	public void doRun(Object element) {

		final IImageHolder holder = getImageHolder(element);
		final boolean _holder = holder != null;

		final ILayer layer = (ILayer) getLayer(true, element);
		final boolean _layer = layer != null;
		final boolean animatedParent = layer != null
				&& layer.getParent() instanceof IAnimatedImage;
		final IAnimationFrame frame = (IAnimationFrame) (element instanceof IAnimationFrame ? element
				: null);
		final boolean _animation = frame != null && !_holder;

		Rectangle bounds = new Rectangle();

		if (_layer) {
			bounds.width = layer.getParent().getWidth();
			bounds.height = layer.getParent().getHeight();
		}

		if (_layer && getGraphicalEditPart(element) != null) {
			bounds = ((AbstractGraphicalEditPart) getGraphicalEditPart(element))
					.getFigure().getBounds();
		} else if (_holder) {
			bounds.width = holder.getWidth();
			bounds.height = holder.getHeight();
		} else if (_animation) {
			bounds.width = frame.getWidth();
			bounds.height = frame.getHeight();
		}

		try {

			final Object _element = element;

			RunnableWithParameter callback = new BaseRunnable() {
				public void run() {

					if (_holder) {
						if (getParameter() != null) {
							EditPart ep = getEditPart(_element);
							IScreenElement screenElement = getScreenElement(_element);

							execute(new UndoableImageHolderActionCommand(
									holder, new Runnable() {
										public void run() {
											try {
												holder
														.pasteMask(getParameter());
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}), ep);
						}
					}
					if (_layer) {
						try {

							if (animatedParent) {
								((IAnimatedImage) layer.getParent())
										.getAnimationFrames()[0]
										.pasteMask(getParameter());
							} else {
								layer.pasteMask(getParameter());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (getContentData(_element) != null) {
							updateGraphicWithCommand(layer, _element);
						}
					}
					if (_animation) {
						if (getParameter() != null) {
							try {
								frame.pasteMask(getParameter());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			};

			RenderedImage rawImage = null;
			RenderedImage mask = null;

			if (_layer) {
				if (animatedParent) {
					mask = ((IAnimatedImage) layer.getParent())
							.getAnimationFrames()[0].getMask();
					rawImage = ((IAnimatedImage) layer.getParent())
							.getAnimationFrames()[0].getRAWImage(false);
				} else {
					mask = layer.getMaskImage();
					rawImage = layer.getRAWImage();
				}
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

			if (_layer) {
				paintProcessThreads.add(FileChangeWatchThread
						.open3rdPartyEditor(UtilsPlugin.getDefault()
								.getPreferenceStore().getString(
										IMediaConstants.PREF_BITMAP_EDITOR),
								mask, layer.getParent().getId() + "_mask",
								callback, true, "bmp", true));
			} else if (_holder) {
				String prefix = holder.getImageFile().getName().substring(0,
						holder.getImageFile().getName().lastIndexOf('.'));
				paintProcessThreads
						.add(FileChangeWatchThread.open3rdPartyEditor(null,
								mask, prefix, callback, true));
			} else if (_animation) {
				String prefix = frame.getParent().getId() + frame.getSeqNo();
				paintProcessThreads
						.add(FileChangeWatchThread.open3rdPartyEditor(null,
								mask, prefix, callback, true));
			}
		} catch (Throwable e) {
			handleProcessException(e);
		}
	}

	@Override
	protected boolean doCalculateEnabled(Object element) {

		// if (true)
		// return true;

		IImageHolder holder = getImageHolder(element);
		if (holder != null) {
			return holder.isBitmap() && holder.supportsMask();
		}

		final ILayer layer = getLayer(false, element);

		//if (layer != null && !layer.getParent().isNinePiece()) {
		if (layer != null && !layer.getParent().isMultiPiece()) {
			if (layer.isBitmapImage()) {
				return layer.supportMask() || layer.supportSoftMask();
			}
		} else if (element instanceof IAnimationFrame) {
			IAnimationFrame x = (IAnimationFrame) element;
			return x.isBitmap();
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.EditInExternalAbstractAction#init()
	 */
	@Override
	protected void init() {
		setId(ID);
		setText(Messages.EditMaskInBitmapEditorAction_name);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				EditMaskAction.EDIT_3RD_BITMAP_CONTEXT);
		super.init();
	}

}
