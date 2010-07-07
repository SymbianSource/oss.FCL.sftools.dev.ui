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
package com.nokia.tools.s60.editor;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.s60.editor.actions.AbstractEditAction;
import com.nokia.tools.s60.editor.actions.EditImageInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.EditImageInSVGEditorAction;
import com.nokia.tools.s60.ide.actions.OpenGraphicsEditorAction;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.IToolBoxAdapter;
import com.nokia.tools.screen.ui.utils.SimpleSelectionProvider;

/**
 * Common functionality for opening external editors for given IContentData
 * element & common actions functionality
 * 
 * 
 */
public class ExternalEditorSupport {

	public static ExternalEditorSupport INSTANCE = new ExternalEditorSupport();

	/**
	 * Opens SVG, Bitmap, Sound (all external) or Animation Editor for given element
	 * 
	 * @param element
	 */
	public AbstractEditAction openDefaultEditor(IContentData element, IWorkbenchPart part) {
		IToolBoxAdapter tba = (IToolBoxAdapter) element
				.getAdapter(IToolBoxAdapter.class);
		IImageAdapter ia = (IImageAdapter) element
				.getAdapter(IImageAdapter.class);
		boolean openAnimationEditor = false;
		if (tba != null) {
			openAnimationEditor = tba.isMultipleLayersSupport();
		}
		if (ia != null) {
			openAnimationEditor = openAnimationEditor || ia.isAnimated();
		}
		if (openAnimationEditor) {			
			try {
				OpenGraphicsEditorAction action = new OpenGraphicsEditorAction(
						part, getSelectionProvider(element));
				action.run();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// open default editor based on type
			ISkinnableEntityAdapter ska = (ISkinnableEntityAdapter) element
					.getAdapter(ISkinnableEntityAdapter.class);
			if (ska != null) {
				if (ska.isSVG()) {
					EditImageInSVGEditorAction action = new EditImageInSVGEditorAction(
							getSelectionProvider(element), (CommandStack) part.getAdapter(CommandStack.class));
					try {
						action.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return action;
				} else if (ska.isBitmap()) {
					EditImageInBitmapEditorAction action = new EditImageInBitmapEditorAction(
							getSelectionProvider(element), (CommandStack) part.getAdapter(CommandStack.class));
					try {
						action.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return action;
				}
				
			}
		}
		return null;
	}

	public static ISelectionProvider getSelectionProvider(Object selectionData) {		
		return new SimpleSelectionProvider(selectionData);		
	}

}
