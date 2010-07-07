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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.IMediaFileAdapter;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.ui.ISkinnableContentDataAdapter;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.IToolBoxAdapter;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.screen.ui.dialogs.MessageDialogWithCheckBox;
import com.nokia.tools.theme.core.MultiPieceManager;
import com.nokia.tools.theme.s60.S60SkinnableEntityAdapter;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;

/**
 * Copy image action for screen elements
 */
public class CopyImageAction extends AbstractAction {

	public static final String ID = ActionFactory.COPY.getId();

	private Clipboard clip = null;

	private boolean silent;

	@Override
	protected void init() {
		setId(ID);
		setText(Messages.CopyImageAction_name); 
		ImageDescriptor copyImage = PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY);
		ImageDescriptor copyImageDisabled = PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_COPY_DISABLED);
		setImageDescriptor(copyImage);
		setDisabledImageDescriptor(copyImageDisabled);
		setToolTipText(Messages.CopyImageAction_tooltip);

		setLazyEnablementCalculation(true);
	}

	public CopyImageAction(IWorkbenchPart part, Clipboard clip) {
		super(part);
		this.clip = clip;
	}

	public CopyImageAction(ISelectionProvider provider, Clipboard clip) {
		super(null);
		setSelectionProvider(provider);
		this.clip = clip;
	}

	@Override
	public void doRun(Object sel) {

		if (clip == null)
			clip = Toolkit.getDefaultToolkit().getSystemClipboard();

		IContentData data = getContentData(sel);
		ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data
				.getAdapter(ISkinnableEntityAdapter.class);

		if (sel instanceof IAnimationFrame) {
			if (isMaskNode()) {
				IAnimationFrame frame = (IAnimationFrame) sel;
				if (frame.getMaskFile() != null) {
					ClipboardHelper.copyImageToClipboard(clip, frame
							.getMaskFile());
				} else {
					ClipboardHelper.copyImageToClipboard(clip, frame.getMask());
				}
			} else if (isImageNode()) {
				IAnimationFrame frame = (IAnimationFrame) sel;
				if (frame.getImageFile() != null) {
					ClipboardHelper.copyImageToClipboard(clip, frame
							.getImageFile());
				} else {
					ClipboardHelper.copyImageToClipboard(clip, frame
							.getRAWImage(false));
				}
			} else {
				((IAnimationFrame) sel).copyImageToClipboard(clip);
			}
			return;
		}

		// when layer is selected in tree
		if (sel instanceof ILayer) {

			try {
				ILayer l = (ILayer) sel;
				if (isLayerNode() || isNodeOfType(AbstractAction.TYPE_PART)) {

					if (skAdapter instanceof S60SkinnableEntityAdapter) {
						// copy image + mask
						l.copyImageToClipboard(clip);
					}
				} else if (isImageNode()) {
					// copy image only
					Object param = l.getFileName(false) == null ? l
							.getRAWImage() : l.getFileName(true);
					ClipboardHelper.copyImageToClipboard(clip, param);
				} else if (isMaskNode()) {
					// copy mask only
					if (l.getMaskFileName(true) != null) {
						ClipboardHelper.copyImageToClipboard(clip, l
								.getMaskFileName(true));
					} else {
						ClipboardHelper.copyImageToClipboard(clip, l
								.getMaskImage());
					}
				} else {
					l.copyImageToClipboard(clip);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return;
		}

		
		IImageHolder holder = getImageHolder(sel);
		if (holder != null) {
			holder.copyImageToClipboard(clip);
			return;
		}

		// IContentData data = getContentData(sel);

		if (data != null) {

			ISkinnableContentDataAdapter skinableAdapter = (ISkinnableContentDataAdapter) data
					.getAdapter(ISkinnableContentDataAdapter.class);
			if (skinableAdapter != null) {
				skinableAdapter.copyToClipboard(null);
				return;
			}

			/*
			 * ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter)
			 * data .getAdapter(ISkinnableEntityAdapter.class);
			 */
			if (skAdapter != null) {

				if (skAdapter.hasMask()) {
				
				}

				if (skAdapter instanceof S60SkinnableEntityAdapter)
					skAdapter.copyImageToClipboard(clip);
				
				else {
					copyIdAndFileToClipboard(data, skAdapter);
				}
				// if (skAdapter.isNinePiece()) {
				if (skAdapter.isMultiPiece()) {
					// copy from 9-piece was performed, show info dialog
					IPreferenceStore store = S60WorkspacePlugin.getDefault()
							.getPreferenceStore();
					Boolean showState = store.getBoolean(skAdapter
							.getCopyPieceInfo());
					// .getBoolean(IMediaConstants.NINE_PIECE_COPY_INFO);
					if (showState || silent)
						return;

					IBrandingManager branding = BrandingExtensionManager
							.getBrandingManager();
					Image image = null;
					if (branding != null) {
						image = branding.getIconImageDescriptor().createImage();
					}
					MessageDialog messageDialog = new MessageDialogWithCheckBox(
							PlatformUI.getWorkbench().getDisplay()
									.getActiveShell(),
							Messages.CopyAction_9piece_title,
							image,
							MultiPieceManager.getCopyMessageText(),
							// MultipieceHelper.getCopyMessageText(),
							Messages.CopyAction_9piece_checkbox, false, null,
							null, null, 2,
							new String[] { IDialogConstants.OK_LABEL }, 0);
					messageDialog.open();
					if (image != null) {
						image.dispose();
					}

					// store.setValue(IMediaConstants.NINE_PIECE_COPY_INFO,
					store.setValue(skAdapter.getCopyPieceInfo(),
							((MessageDialogWithCheckBox) messageDialog)
									.getCheckBoxValue());
				}
			}
			return;
		}

		ILayer layer = getLayer(true, sel);
		if (layer != null) {
			layer.copyImageToClipboard(clip);
		}
	}

	/**
	 * @param data
	 * @param skAdapter
	 */
	private void copyIdAndFileToClipboard(IContentData data,
			ISkinnableEntityAdapter skAdapter) {
		List<File> clipboardFile = null;
		skAdapter.copyImageToClipboard(clip);
		try {
			clipboardFile = (List<File>) clip
					.getData(DataFlavor.javaFileListFlavor);
		} catch (UnsupportedFlavorException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}

		if (clipboardFile instanceof List) {
			clipboardFile = (List<File>) clipboardFile;
		}
		String clipboardIds = null;
		clipboardIds = data.getId();
	}

	@Override
	protected boolean doCalculateEnabled(Object sel) {

		if (isNodeOfType(TYPE_COLOR) || isNodeOfType((String) TYPE_COLOR_GROUP))
			return false;

		// when layer is selected in tree
		if (sel instanceof ILayer) {
			if (isMaskNode())
				return ((ILayer) sel).hasMask();
			else
				return ((ILayer) sel).hasImage();
		}
		if (sel instanceof IAnimationFrame) {
			return true;
		}

		IImageHolder holder = getImageHolder(sel);
		if (holder != null) {
			return holder.getImageFile() != null;
		}

		IContentData data = getContentData(sel);

		if (data != null) {

			ISkinnableContentDataAdapter skinableAdapter = (ISkinnableContentDataAdapter) data
					.getAdapter(ISkinnableContentDataAdapter.class);
			if (skinableAdapter != null) {
				return skinableAdapter.isCopyEnabled();
			}

			IToolBoxAdapter toolBoxAdapter = (IToolBoxAdapter) data
					.getAdapter(IToolBoxAdapter.class);
			ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data
					.getAdapter(ISkinnableEntityAdapter.class);

			if (toolBoxAdapter != null && toolBoxAdapter.isFile()) {
				IMediaFileAdapter soundAdapter = (IMediaFileAdapter) data
						.getAdapter(IMediaFileAdapter.class);
				if (soundAdapter != null
						&& soundAdapter.getFileName(true) != null) {
					return new File(soundAdapter.getFileName(true)).exists();
				}
				return false;
			}

			if (skAdapter != null) {
				if (skAdapter.isColour()) {
					return (skAdapter.isBitmap() || skAdapter.isSVG());
				}
				return skAdapter.isCopyAllowed();
			}
		}

		ILayer layer = getLayer(false, sel);
		return layer == null ? false : (layer.isSvgImage() || layer
				.isBitmapImage());
	}

	public void setSilent(boolean b) {
		silent = b;
	}

}
