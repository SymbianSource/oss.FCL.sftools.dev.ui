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
package com.nokia.tools.s60.cheatsheet.actions;

import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;

import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.layers.IEffectParameter;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.s60.editor.GraphicsEditorPart;
import com.nokia.tools.s60.ide.actions.OpenGraphicsEditorAction;
import com.nokia.tools.screen.ui.utils.EclipseUtils;

/**
 * perform action for cheatsheet 13 - edit layer animation
 * 
 */
public class CheatSheet13Actions extends BaseAction implements
		ICheatSheetAction {

	public void run(String[] params, ICheatSheetManager manager) {
		if ("openInEditor".equals(params[0])) {
			doOpenInEditor(params);
		} else if ("runAnimation".equals(params[0])) {
			doRunAnimation();
		} else if ("doSave".equals(params[0])) {
			try {
				IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();
				if (activeEd instanceof GraphicsEditorPart) {
					GraphicsEditorPart sep = (GraphicsEditorPart) activeEd;
					sep.doSave(new NullProgressMonitor());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("addEffects".equals(params[0])) {
			doAddEffects();
		} else if ("addAnimation".equals(params[0])) {
			try {
				IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();
				if (activeEd instanceof GraphicsEditorPart) {
					GraphicsEditorPart sep = (GraphicsEditorPart) activeEd;
					IImage current = sep.getActiveImage();
					// find apply color effect in last layer
					ILayer l = current.getLayer(current.getLayerCount() - 1);
					ILayerEffect appc = l
							.getEffect(IMediaConstants.APPLY_COLOR);
					IEffectParameter param = appc.getParameters().get(0);
					if (param.isAnimated()) {
						// set animation props - 100 at '0' and 200 at '100'
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void doOpenInEditor(String[] params) {
		try {
			String elementId = params[1];
			IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();
			EditPartViewer viewer = getEditPartViewer();
			if (viewer != null) {
				// step one - maek sure that there is selected screen that
				// contains 'elementID' element
				new SelectIconViewContent().run(new String[] { elementId },
						null);

				List childrens = viewer.getRootEditPart().getChildren();
				EditPart rootScreen = (EditPart) ((EditPart) childrens.get(0))
						.getChildren().get(0);

				// iterate through all editparts on screen until we find desired
				// element
				Stack<EditPart> stack = new Stack<EditPart>();
				stack.push(rootScreen);
				while (stack.isEmpty() == false) {
					EditPart ep = stack.pop();
					if (isModelElement(ep, elementId)) {
						viewer.setSelection(new StructuredSelection(ep));
						// execute open in graphics editor action
						OpenGraphicsEditorAction act = new OpenGraphicsEditorAction(
								activeEd, viewer);
						if (act.isEnabled())
							act.run();
						return;
					}
					for (Object _p : ep.getChildren()) {
						stack.push((EditPart) _p);
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		notifyResult(false);
	}

	private void doAddEffects() {
		try {
			IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();
			if (activeEd instanceof GraphicsEditorPart) {
				GraphicsEditorPart sep = (GraphicsEditorPart) activeEd;
				IImage current = sep.getActiveImage();

				ILayerEffect eff = current.addLayer().addLayerEffect(
						IMediaConstants.APPLY_COLOR);
				eff.getParameters().get(0).setAnimated(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
