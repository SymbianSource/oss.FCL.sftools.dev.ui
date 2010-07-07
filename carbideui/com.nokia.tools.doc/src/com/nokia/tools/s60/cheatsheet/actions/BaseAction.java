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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.s60.editor.GraphicsEditorPart;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.utils.EclipseUtils;

public abstract class BaseAction extends Action {

	protected boolean isModelElement(EditPart part, String id) {
		try {
			IScreenElement el = JEMUtil.getScreenElement(part);
			if (id.equals(el.getData().getId()))
				return true;
		} catch (Exception e) {
		}
		return false;
	}

	protected void doRunAnimation() {
		try {
			IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();
			if (activeEd instanceof GraphicsEditorPart) {
				GraphicsEditorPart sep = (GraphicsEditorPart) activeEd;
				sep.control_startAnimation();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected EditPartViewer getEditPartViewer() {
		IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();
		return (EditPartViewer) activeEd.getAdapter(EditPartViewer.class);
	}

	protected boolean selectInEditor(String elementId) {
		try {
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

						// activate editor part
						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage().activate(activeEd);

						return true;
					}
					for (Object _p : ep.getChildren()) {
						stack.push((EditPart) _p);
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
