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

import java.io.IOException;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.BaseRunnable;
import com.nokia.tools.media.utils.RunnableWithParameter;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.IPasteTargetAdapter;
import com.nokia.tools.s60.editor.commands.UndoableImageHolderActionCommand;
import com.nokia.tools.screen.ui.utils.FileChangeWatchThread.FileInfo;

public abstract class CommonEditAction extends AbstractEditAction {

	public CommonEditAction(ISelectionProvider provider, CommandStack stack) {
		super(provider, stack);
	}

	public CommonEditAction(IWorkbenchPart part) {
		super(part);
	}

	@Override
	public void doRun(Object element) {

		// remember element
		final Object _element = element;

		if (getImageHolder(_element) != null) {

			final IImageHolder holder = getImageHolder(_element);

			RunnableWithParameter callback = new BaseRunnable() {
				public void run() {
					if (getParameter() != null) {
						EditPart ep = getEditPart(_element);
						execute(new UndoableImageHolderActionCommand(holder,
								new Runnable() {
									public void run() {
										try {
											holder.paste(getParameter(), null);
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}), ep);
					}
				};
			};

			String prefix = holder.getImageFile().getName().substring(0,
					holder.getImageFile().getName().lastIndexOf('.'));

			try {

				FileInfo info = openEditor(holder, prefix, callback);
				paintProcessThreads.add(info);

			} catch (Throwable e) {
				handleProcessException(e);
			}
		} else {

			final ILayer layer = getLayer(false, _element);
			final String oldStretchMode = layer.getStretchMode();
			// skinnableEntityAdapter extension, not directly to layer
			IPasteTargetAdapter pasteAdapter = getPasteTargetAdapter(getContentData(element));
			if (null == pasteAdapter)
				pasteAdapter = getPasteTargetAdapter(layer);
			final IPasteTargetAdapter paster = pasteAdapter;
			if (layer != null) {

				if (layer.isBackground()) {
					return;
				}

				try {

					RunnableWithParameter callback = new BaseRunnable() {

						public void run() {
							// called from animation editor
							IContentData data = getContentData(_element);
							// paste triggers refresh, thus keeps the current
							// data, this shall be integrated with the
							// setThemeGraphicCommand
							try {
								if (layer.getParent() instanceof IAnimatedImage) {
									((IAnimatedImage) layer.getParent())
											.getAnimationFrames()[0].paste(
											getParameter(), null);
								} else {
									
									paster.paste(getParameter(), layer);
									// bugfix for restoring old stretch mode after operation is done.
									layer.setStretchMode(oldStretchMode);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

							if (data != null) {
					
								updateGraphicWithCommand(layer, data);
							}
						}

					};

					// returns absolute path
					String imageAbsolutePath = layer.getFileName(true);

					FileInfo info = openEditor(imageAbsolutePath, layer,
							callback);
					paintProcessThreads.add(info);

				} catch (Throwable e) {
					handleProcessException(e);
				}
			}
		}
	}

	/**
	 * editor run method for selection = editable entity image / layer
	 * 
	 * @param imageAbsolutePath
	 * @param layer
	 * @param callback
	 * @return
	 */
	protected abstract FileInfo openEditor(String imageAbsolutePath,
			ILayer layer, RunnableWithParameter callback) throws IOException;

	/**
	 * editor run method for selection
	 * 
	 * @param imageAbsolutePath
	 * @param layer
	 * @param callback
	 * @return
	 */
	protected abstract FileInfo openEditor(IImageHolder holder, String prefix,
			RunnableWithParameter callback) throws IOException;

}
