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

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.ui.adapter.IComponentAdapter;
import com.nokia.tools.editing.ui.command.ApplyFeatureCommand;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;

public abstract class ConstraintKeyHandler implements IKeyHandler {
	private IWorkbenchPart part;

	public ConstraintKeyHandler(IWorkbenchPart part) {
		this.part = part;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.actions.IKeyHandler#calculateEnabled()
	 */
	public boolean calculateEnabled() {
		return getCommandStack() != null && !getSelectedElements().isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.actions.IKeyHandler#run(int)
	 */
	public void run(int keyCode) {
		for (IScreenElement element : getSelectedElements()) {
			Rectangle bounds = EditingUtil.getBounds(element.getWidget());
			Rectangle newBounds = getNewConstraint(keyCode, bounds);
			applyConstraint(element, newBounds);
		}
	}

	protected CommandStack getCommandStack() {
		return (CommandStack) part.getAdapter(CommandStack.class);
	}

	protected void applyConstraint(IScreenElement element, Rectangle newBounds) {
		ApplyFeatureCommand command = new ApplyFeatureCommand();
		command.setFeature(EditingUtil.getBoundsFeature(element.getWidget()));
		command.setLabel(Messages.Command_ApplyBounds);
		command.setTarget(element.getWidget());
		command.setValue(newBounds);
		getCommandStack().execute(command);
	}

	protected abstract Rectangle getNewConstraint(int keyCode, Rectangle bounds);

	protected List<IScreenElement> getSelectedElements() {
		EditPartViewer viewer = (EditPartViewer) part
				.getAdapter(EditPartViewer.class);
		if (viewer == null) {
			return Collections.EMPTY_LIST;
		}
		List editParts = viewer.getSelectedEditParts();
		List<IScreenElement> elements = new ArrayList<IScreenElement>(editParts
				.size());
		for (Object editPart : editParts) {
			IScreenElement element = (IScreenElement) JEMUtil
					.getScreenElement(editPart);
			if (element != null) {
				IComponentAdapter adapter = (IComponentAdapter) element
						.getAdapter(IComponentAdapter.class);
				if (adapter != null
						&& adapter.supports(
								IComponentAdapter.CHANGE_CONSTRAINT, null)) {
					elements.add(element);
				}
			}
		}
		return elements;
	}
}
