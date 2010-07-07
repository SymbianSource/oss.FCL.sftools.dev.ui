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
package com.nokia.tools.screen.ui.actions;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;

public class MoveElementKeyHandler implements IKeyHandler {
	private IWorkbenchPart part;

	public MoveElementKeyHandler(IWorkbenchPart part) {
		this.part = part;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.actions.IKeyHandler#calculateEnabled()
	 */
	public boolean calculateEnabled() {
		return getSelectedElement() != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.actions.IKeyHandler#run(int)
	 */
	public void run(int keyCode) {
		IScreenElement element = getSelectedElement();
		IMovingAdapter adapter = (IMovingAdapter) element.getData().getAdapter(
				IMovingAdapter.class);
		IContentData data = adapter.move(keyCode);
		if (data != null) {
			element.updateWidget();
			EditPartViewer viewer = (EditPartViewer) part
					.getAdapter(EditPartViewer.class);
			if (viewer != null) {
				for (Object obj : viewer.getEditPartRegistry().values()) {
					if (obj instanceof EditPart) {
						EditPart part = (EditPart) obj;
						if (part.getModel() instanceof EObject) {
							IScreenElement e = (IScreenElement) JEMUtil
									.getScreenElement((EObject) part.getModel());
							if (e != null && e.getData().equals(data)) {
								viewer.select(part);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * @return the movable screen element from the current selected editpart, or
	 *         null if the selection is not a single selection or there is no
	 *         {@link IMovingAdapter} associated with the content.
	 */
	protected IScreenElement getSelectedElement() {
		EditPartViewer viewer = (EditPartViewer) part
				.getAdapter(EditPartViewer.class);
		if (viewer == null) {
			return null;
		}
		List editParts = viewer.getSelectedEditParts();
		if (editParts.size() != 1) {
			return null;
		}
		EditPart part = (EditPart) editParts.get(0);
		IScreenElement element = (IScreenElement) JEMUtil
				.getScreenElement((EObject) part.getModel());
		if (element == null) {
			return null;
		}
		IMovingAdapter adapter = (IMovingAdapter) element.getData().getAdapter(
				IMovingAdapter.class);
		if (adapter == null && element.getParent() != null) {
			adapter = (IMovingAdapter) element.getParent().getData()
					.getAdapter(IMovingAdapter.class);
			if (adapter == null) {
				return null;
			}
			return element.getParent();
		}

		return element;
	}
}
