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
import java.io.IOException;

import org.eclipse.core.runtime.Path;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.symbian.mbm.BitmapConverter;
import com.nokia.tools.media.utils.RunnableWithParameter;
import com.nokia.tools.media.utils.editor.frameanimation.FrameAnimationContainer;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.screen.ui.utils.FileChangeWatchThread;
import com.nokia.tools.screen.ui.utils.FileChangeWatchThread.FileInfo;

public class EditImageInBitmapEditorAction extends CommonEditAction {

	public static final String EDIT_3RD_BITMAP_CONTEXT = "com.nokia.tools.s60.ide" + '.' + "edit3rdPartyBitmap_context"; 

	public static final String ID = FrameAnimationContainer.ACTION_ID_EDIT_BITMAP_IMAGE;

	public EditImageInBitmapEditorAction(ISelectionProvider provider,
			CommandStack stack) {
		super(provider, stack);
	}

	public EditImageInBitmapEditorAction(IWorkbenchPart part) {
		super(part);
	}

	@Override
	protected boolean doCalculateEnabled(Object element) {
		IImageHolder holder = getImageHolder(element);
		if (holder != null) {
			return holder.isBitmap() && holder.supportsBitmap();
		}

		final ILayer layer = getLayer(false, element);

		if (layer != null) {
			//if (layer.getParent().isNinePiece())
			if (layer.getParent().isMultiPiece())
				return false;
			// command activated in layers view
			return layer.isBitmapImage();
		} else if (element instanceof IAnimationFrame) {
			IAnimationFrame frame = (IAnimationFrame) element;
			return frame.isBitmap();
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
		setText(Messages.EditImageAction_name);
		setLazyEnablementCalculation(true);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				EditImageInBitmapEditorAction.EDIT_3RD_BITMAP_CONTEXT);
		super.init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.CommonEditAction#openEditor(java.lang.String,
	 *      com.nokia.tools.media.utils.layers.ILayer,
	 *      com.nokia.tools.content.core.RunnableWithParameter)
	 */
	@Override
	protected FileInfo openEditor(String imageAbsolutePath, ILayer layer,
			RunnableWithParameter callback) throws IOException {
		if (!BitmapConverter.isBMPType(new File(imageAbsolutePath))) {
			return FileChangeWatchThread.open3rdPartyEditor(null,
					imageAbsolutePath, layer.getParent().getId(), callback,
					true);
		}
		return FileChangeWatchThread.open3rdPartyEditor(null, layer
				.getRAWImage(), layer.getParent().getId(), callback, true,
				new Path(imageAbsolutePath).getFileExtension(), false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.CommonEditAction#openEditor(com.nokia.tools.media.utils.layers.IImageHolder,
	 *      java.lang.String,
	 *      com.nokia.tools.content.core.RunnableWithParameter)
	 */
	@Override
	protected FileInfo openEditor(IImageHolder holder, String prefix,
			RunnableWithParameter callback) throws IOException {
		return FileChangeWatchThread.open3rdPartyEditor(null, holder
				.getRAWImage(false), prefix, callback, true);

	}
}
