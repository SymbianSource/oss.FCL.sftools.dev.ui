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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.media.player.IPlayer;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.UiPlugin;

public class PlaySelectionAction extends AbstractPlayAction {
	public static final String ID = "PLAY_SELECTION";

	/**
	 * Constructs an action associated with the given part.
	 * 
	 * @param part the workbench part
	 */
	public PlaySelectionAction(IWorkbenchPart part) {
		super(part);
		setId(ID);
		setText(Messages.PlayMedia_PlaySelection);
		setToolTipText(Messages.PlayMedia_PlaySelection);
		setImageDescriptor(UiPlugin
				.getImageDescriptor("icons/startanimsel.gif"));
		setDisabledImageDescriptor(UiPlugin
				.getImageDescriptor("icons/startanimsel_disabled.gif"));
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
		if (controller != null && !controller.isPlaying()) {
			IStructuredSelection sel = (IStructuredSelection) getSelection();
			controller.playSelection(sel);
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

		if (controller.isPlaying() || controller.isPaused()) {
			return false;
		}

		IStructuredSelection sel = (IStructuredSelection) getSelection();

		if (sel == null) {
			return false;
		}

		for (Object element : sel.toArray()) {
			element = JEMUtil.getScreenElement(element);

			if (element != null && element instanceof IScreenElement) {
				IPlayer player = (IPlayer) ((IScreenElement) element)
						.getAdapter(IPlayer.class);
				if (player != null && player.isPlayable()) {
					return true;
				}
			}
		}

		return false;
	}

	protected ISelection getSelection() {
		return getWorkbenchPart().getSite().getSelectionProvider()
				.getSelection();
	}
}
