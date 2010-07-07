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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.s60.editor.commands.UndoableImageHolderActionCommand;

/**
 * reset mask to default mask
 * 
 */
public class ClearMaskAction extends AbstractEditAction {

	public static final String EDIT_3RD_BITMAP_CONTEXT = "com.nokia.tools.s60.ide" + '.' + "ClearMaskEditorAction_context"; 

	public static final String ID = "ClearMask"; 

	public ClearMaskAction(ISelectionProvider provider, CommandStack stack) {
		super(provider, stack);
	}

	public ClearMaskAction(IWorkbenchPart part) {
		super(part);
	}

	@Override
	protected void doRun(Object element) {
		final IImageHolder holder = getImageHolder(element);
		final ILayer layer = (ILayer) getLayer(false, element);
		if (holder != null) {
			EditPart ep = getEditPart(element);
			execute(new UndoableImageHolderActionCommand(holder,
					new Runnable() {
						public void run() {
							holder.clearMask();
						}
					}), ep);
		} else if (layer != null) {
			try {
				layer.clearMask();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (getContentData(element) != null) {
				updateGraphicWithCommand(layer, element);
			}

		} else if (element instanceof IAnimationFrame) {
			IAnimationFrame frame = (IAnimationFrame) element;
			frame.clearMask();
		}
	}

	@Override
	protected boolean doCalculateEnabled(Object element) {
		IImageHolder holder = getImageHolder(element);
		if (holder != null) {
			return holder.hasMask() && holder.supportsMask();
		}

		final ILayer layer = (ILayer) getLayer(false, element);
		
		if (layer != null && !layer.getParent().isMultiPiece()) {
			return layer.hasMask();
		} else if (element instanceof IAnimationFrame) {
			IAnimationFrame frame = (IAnimationFrame) element;
			return frame.hasMask();
		}

		return false;
	}

	@Override
	protected void init() {
		setId(ID);
		setText(Messages.ClearMaskAction_name); 
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				ClearMaskAction.EDIT_3RD_BITMAP_CONTEXT);
		super.init();
	}
}
