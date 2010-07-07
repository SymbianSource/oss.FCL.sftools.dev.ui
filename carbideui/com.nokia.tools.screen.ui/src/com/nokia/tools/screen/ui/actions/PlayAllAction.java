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

import com.nokia.tools.media.player.IPlayer;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.utils.ScreenUtil;

/**
 * This action is used for controlling all animated elements of one preview
 * screen.
 * 
 */
public class PlayAllAction extends AbstractPlayAction {
	public static final String ID = "PLAY_ALL";

	/**
	 * Constructs an action associated with the given part.
	 * 
	 * @param part the workbench part
	 */
	public PlayAllAction(IWorkbenchPart part) {
		super(part);
		setId(ID);
		setText(Messages.PlayMedia_PlayAll);
		setToolTipText(Messages.PlayMedia_PlayAll);
		setImageDescriptor(UiPlugin
				.getImageDescriptor("icons/startanimall.gif"));
		setDisabledImageDescriptor(UiPlugin
				.getImageDescriptor("icons/startanimall_disabled.gif"));
		registerPlayAction();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	protected void doRun() {
		PlayerController controller = getController();
		if (!controller.isPlaying()) {
			if (!controller.isPaused()) {
				controller.play();
			} else {
				controller.resume();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
	 */
	@Override
	protected boolean calculateEnabled() {
		PlayerController controller = getController();
		if (controller == null) {
			return false;
		}

		if (controller.isPlaying()) {
			return false;
		}

		if (controller.isPaused()) {
			setText(Messages.PlayMedia_Resume);
			setToolTipText(Messages.PlayMedia_Resume);
		} else {
			setText(Messages.PlayMedia_PlayAll);
			setToolTipText(Messages.PlayMedia_PlayAll);
		}

		IScreenElement screen = ScreenUtil
				.getScreen((EditPartViewer) getWorkbenchPart().getAdapter(
						EditPartViewer.class));
		if (screen == null) {
			return false;
		}
		for (IScreenElement element : screen.getAllChildren()) {
			IPlayer player = (IPlayer) element.getAdapter(IPlayer.class);
			if (null != player && player.isPlayable()) {
				return true;
			}
		}
		return false;
	}
}
