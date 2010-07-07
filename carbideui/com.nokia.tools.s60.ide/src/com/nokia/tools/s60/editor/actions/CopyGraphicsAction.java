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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor;
import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.clipboard.JavaObjectTransferable;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.IToolBoxAdapter;
import com.nokia.tools.theme.core.MultiPieceManager;

/**
 * Copies all graphics definition of layer, not just image or mask
 * Operates on local clipboard, not system-wide.
 */
public class CopyGraphicsAction extends AbstractAction {

	public static final String ID = ActionFactory.COPY.getId() + "Graphics";

	private Clipboard clip = null;

	@Override
	protected void init() {
		setId(ID);
		setText(Messages.CopyImageAction_name + " Layers"); 
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/etool16/copy_edit.gif"));
		setDisabledImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/dtool16/copy_edit.gif"));
		setToolTipText(Messages.CopyImageAction_tooltip);
		setLazyEnablementCalculation(true);
	}

	public CopyGraphicsAction(IWorkbenchPart part, Clipboard clip) {
		super(part);
		this.clip = clip;
	}

	public CopyGraphicsAction(ISelectionProvider provider, Clipboard clip) {
		super(null);
		setSelectionProvider(provider);
		this.clip = clip;
	}

	@Override
	public void doRun(Object sel) {
		
		
		if (clip == null)
			clip = ClipboardHelper.APPLICATION_CLIPBOARD;

		IContentData data = getContentData(sel);
		if (data != null) {
			ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data
					.getAdapter(ISkinnableEntityAdapter.class);
			IToolBoxAdapter toolBoxAdapter = (IToolBoxAdapter) data
						.getAdapter(IToolBoxAdapter.class);
			
			if (skAdapter != null && !skAdapter.isMultiPiece()) {
				try {					
					Object graphic = skAdapter.getClone(skAdapter.getThemeGraphics(true));				
					if (graphic != null) {
						ClipboardContentDescriptor.ContentType type = ClipboardContentDescriptor.ContentType.CONTENT_GRAPHICS;
						if (toolBoxAdapter.isMultipleLayersSupport())
							type = ClipboardContentDescriptor.ContentType.CONTENT_GRAPHICS_MULTILAYER;
						ClipboardContentDescriptor content = new ClipboardContentDescriptor(graphic, type);
						
						boolean[] flags = {skAdapter.isColour(), toolBoxAdapter.isFile(), toolBoxAdapter.isText(), toolBoxAdapter.isMultipleLayersSupport(), toolBoxAdapter.isMultipleLayersSupport() && toolBoxAdapter.isEffectsSupport()};
						
						content.setAttribute(IToolBoxAdapter.class, flags);
						IImageAdapter imgAdapter = (IImageAdapter) skAdapter.getAdapter(IImageAdapter.class);
						if (imgAdapter != null) {
							IImage img = imgAdapter.getImage(true);
							boolean animated = img.isAnimated();
							boolean realtime = img.isAnimatedFor(TimingModel.RealTime);
							boolean relative = img.isAnimatedFor(TimingModel.Relative);
							boolean[] amimFlags = {animated, realtime, relative};
							content.setAttribute(TimingModel.class, amimFlags);
						}
						
						clip.setContents(new JavaObjectTransferable(content), ClipboardContentDescriptor.DummyClipOwner);					
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (skAdapter != null && skAdapter.isMultiPiece()) {
				//element is nine piece
				IImage img = ((IImageAdapter)skAdapter.getAdapter(IImageAdapter.class)).getImage(true);
				try {
					ClipboardContentDescriptor.ContentType type = null;
					List<IImage> parts = img.getPartInstances();
					List<Object> themeGraphics = new ArrayList<Object>();					
					for (IImage p: parts) {						
						themeGraphics.add(skAdapter.getEditedThemeGraphics(p, true));
					}
					
					int partCount = (parts != null) ? parts.size() : 0;
					if(skAdapter.isMultiPiece()){
						type = MultiPieceManager.getClipboardContentDescriptorType(partCount);
					}
					
										
					ClipboardContentDescriptor content = new ClipboardContentDescriptor(themeGraphics, type);
					clip.setContents(new JavaObjectTransferable(content), ClipboardContentDescriptor.DummyClipOwner);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
	}

	@Override
	protected boolean doCalculateEnabled(Object sel) {
		
		if (isMaskNode() || isLayerNode() || isImageNode() || isNodeOfType(TYPE_PART))
			return false;

		IContentData data = getContentData(sel);

		if (data != null) {
			IToolBoxAdapter toolBoxAdapter = (IToolBoxAdapter) data
					.getAdapter(IToolBoxAdapter.class);
			ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data
					.getAdapter(ISkinnableEntityAdapter.class);
			if (toolBoxAdapter == null || skAdapter == null)
				return false;
			if (toolBoxAdapter.isFile() || toolBoxAdapter.isText() || skAdapter.isColour())
				return false;
			return skAdapter.isCopyAllowed();			
		}

		return false;
	}

}
