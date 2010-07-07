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
package com.nokia.tools.s60.editor.graphics;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.editor.AbstractFramePreview;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.ILayer;

/**
 * Composite capable of showing IImage preview image, with capability of updates
 * by BG thread;
 */
public class IImagePreviewComposite extends AbstractFramePreview {

	public IImagePreviewComposite(Composite parent, int style, IImage image) {
		super(parent, style, image);
	}

	protected void createPreview(Composite parent) {
		super.createPreview(parent);

		/* create context menu */
		ISharedImages sharedImages = PlatformUI.getWorkbench()
				.getSharedImages();

		MenuManager manager = new MenuManager();

		final Action copyAction = new Action(Messages.Preview_copyImage) {
			@Override
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						handleCopyImage();
					};
				});
			}
		};
		copyAction.setImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		manager.add(copyAction);

		final Action copyLayerWEffectsAction = new Action(
				Messages.Preview_copyImageWEffects) {
			@Override
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						handleCopyLayerWEffectsImage();
					};
				});
			}
		};
		copyLayerWEffectsAction.setImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		manager.add(copyLayerWEffectsAction);

		final Action copyAggregateAction = new Action(
				Messages.Preview_copyResultImage) {
			@Override
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						handleCopyResultImage();
					};
				});
			}
		};
		copyAggregateAction.setImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		manager.add(copyAggregateAction);

		// paste
		final Action pasteItem = new Action(Messages.Preview_pasteImage) {
			@Override
			public void run() {
				// check if valid layer is selected
				Clipboard clip = Toolkit.getDefaultToolkit()
						.getSystemClipboard();
				if (getSelectedLayer() != null) {
					handlePasteImage(getSelectedLayer(), clip);
				}
		
			}
		};
		pasteItem.setImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		pasteItem.setDisabledImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
		manager.add(pasteItem);

		
		Menu menu = manager.createContextMenu(previewCanvas);
		previewCanvas.setMenu(menu);

		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {

				pasteItem.setEnabled(false);
				pasteItem.setText(Messages.Preview_pasteImage);
				Clipboard clip = Toolkit.getDefaultToolkit()
						.getSystemClipboard();
				if (ClipboardHelper.clipboardContainsData(
						ClipboardHelper.CONTENT_TYPE_SINGLE_IMAGE, clip)) {
					// check if valid layer is selected
					if (getSelectedLayer() != null)
						if (getSelectedLayer().isEnabled()
								&& !getSelectedLayer().isBackground()) {
							pasteItem.setEnabled(true);
							pasteItem.setText(Messages.Preview_pasteImage
									+ " into \"" + getSelectedLayer().getName()
									+ "\"");
						}
				}

				// enable of copy actions
				if (getSelectedLayer() != null) {
					copyAction
							.setEnabled(getSelectedLayer().getRAWImage() != null);
					copyLayerWEffectsAction.setEnabled(getSelectedLayer()
							.getRAWImage() != null);
				} else {
					copyAction.setEnabled(false);
					copyLayerWEffectsAction.setEnabled(false);
				}
			}
		});

	}

	/*
	 * copies layer image to CB
	 */
	protected void handleCopyImage() {
		try {
			ClipboardHelper.copyImageToClipboard(null, getSelectedLayer()
					.getRAWImage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * copies layer image with effects applied to CB to CB
	 */
	protected void handleCopyLayerWEffectsImage() {
		try {
			ClipboardHelper.copyImageToClipboard(null, getSelectedLayer()
					.getProcessedImage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * copies iimage result image to CB
	 */
	protected void handleCopyResultImage() {
		try {
			ClipboardHelper.copyImageToClipboard(null, imageProcessed
					.getAggregateImage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called when user invokes paste image command
	 * 
	 * @throws IOException
	 * @throws UnsupportedFlavorException
	 */
	private void handlePasteImage(ILayer layer, Clipboard cl) {
		try {
			Object data = ClipboardHelper.getClipboardContent(
					ClipboardHelper.CONTENT_TYPE_SINGLE_IMAGE, cl);
			layer.paste(data);
			layer.disableNonDefaultEffects();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
