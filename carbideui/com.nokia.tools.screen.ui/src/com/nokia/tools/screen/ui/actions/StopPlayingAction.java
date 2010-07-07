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

public class StopPlayingAction extends AbstractPlayAction {
	public static final String ID = "STOP_PLAYING";

	/**
	 * Constructs an action associated with the given part.
	 * 
	 * @param part
	 *            the workbench part
	 */
	public StopPlayingAction(IWorkbenchPart part) {
		super(part);
		setId(ID);
		setImageDescriptor(UiPlugin.getImageDescriptor("icons/stopanim.gif"));
		setDisabledImageDescriptor(UiPlugin.getImageDescriptor("icons/stopanim_disabled.gif"));
		setText(Messages.PlayMedia_Stop);
		setToolTipText(Messages.PlayMedia_Stop);
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
		if (controller.isPlaying() || controller.isPaused()) {
			controller.stop();
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
		return controller.isPlaying() || controller.isPaused();
	}
}
