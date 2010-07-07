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

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.RunnableWithParameter;
import com.nokia.tools.media.utils.editor.frameanimation.FrameAnimationContainer;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.screen.ui.utils.FileChangeWatchThread;
import com.nokia.tools.screen.ui.utils.FileChangeWatchThread.FileInfo;

public class EditImageInSVGEditorAction extends CommonEditAction {

	public static final String EDIT_3RD_VECTOR_CONTEXT = "com.nokia.tools.s60.ide" + '.' + "edit3rdPartyVector_context"; 

	public static final String ID = FrameAnimationContainer.ACTION_ID_EDIT_SVG_IMAGE;

	public EditImageInSVGEditorAction(ISelectionProvider provider, CommandStack stack) {
		super(provider, stack);
	}

	public EditImageInSVGEditorAction(IWorkbenchPart part) {
		super(part);
	}

	@Override
	protected boolean doCalculateEnabled(Object element) {
		IImageHolder holder = getImageHolder(element);
		if (holder != null) {
			return holder.isSvg() && holder.supportsSvg();
		}

		final ILayer layer = getLayer(false, element);

		if (layer != null) {
			//if (layer.getParent().isNinePiece())
			if (layer.getParent().isMultiPiece())
				return false;
			return layer.isSvgImage();
		} else if (element instanceof IAnimationFrame) {
			IAnimationFrame frame = (IAnimationFrame) element;
			return frame.isSvg();
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
		setText(Messages.EditSVGImageAction_name);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				EditImageInSVGEditorAction.EDIT_3RD_VECTOR_CONTEXT);
		super.init();
	}

	@Override
	protected FileInfo openEditor(String imageAbsolutePath, ILayer layer, RunnableWithParameter callback) throws IOException {		
		return FileChangeWatchThread
		.open3rdPartyEditor(IMediaConstants.PREF_VECTOR_EDITOR,
				imageAbsolutePath, layer.getParent()
						.getId(), callback, true);
	}

	@Override
	protected FileInfo openEditor(IImageHolder holder, String prefix, RunnableWithParameter callback) throws IOException {		
		return FileChangeWatchThread
			.open3rdPartyVectorEditor(null, holder.getImageFile()
				.toString(), prefix, holder.getImage(),
				callback, true);
	}

}
