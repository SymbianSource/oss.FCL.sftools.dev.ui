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

import org.eclipse.gef.EditPartViewer;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.screen.ui.UiPlugin;

public class PausePlayingAction extends AbstractPlayAction {
	public static final String ID = "PAUSE_PLAYING";

	/**
	 * Constructs an action associated with the given part.
	 * 
	 * @param part
	 *            the workbench part
	 */
	public PausePlayingAction(IWorkbenchPart part) {
		super(part);
		setId(ID);
		setText(Messages.PlayMedia_Pause);
		setToolTipText(Messages.PlayMedia_Pause);
		setImageDescriptor(UiPlugin.getImageDescriptor("icons/pauseanim.gif"));
		setDisabledImageDescriptor(UiPlugin
				.getImageDescriptor("icons/pauseanim_disabled.gif"));
		registerPlayAction();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	protected void doRun() {
		PlayerController controller = PlayerController
				.getInstance((EditPartViewer) getWorkbenchPart().getAdapter(
						EditPartViewer.class));
		if (controller.isPlaying()) {
			controller.pause();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
	 */
	@Override
	protected boolean calculateEnabled() {
		EditPartViewer viewer = (EditPartViewer) getWorkbenchPart().getAdapter(
				EditPartViewer.class);
		if (viewer == null) {
			return false;
		}
		PlayerController controller = PlayerController.getInstance(viewer);
		if (controller == null) {
			return false;
		}

		return controller.isPlaying();
	}
}
