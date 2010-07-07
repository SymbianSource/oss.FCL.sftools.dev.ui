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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.EditorPart;

import com.nokia.tools.media.player.PlayState;
import com.nokia.tools.screen.ui.IScreenConstants;
import com.nokia.tools.screen.ui.UiPlugin;

public abstract class AbstractPlayAction extends WorkbenchPartAction implements
		ISelectionListener {
	private static final long DEFAULT_ANIMATE_ONCE_TIME = 5000;

	static Set<String> toRefresh = new HashSet<String>();

	// not static, holds reference to self and thus the workbench part
	private PlayerController.IPlayStateListener listener = null;

	private boolean isPlayOnce;

	/**
	 * Constructs an action associated with the given part.
	 * 
	 * @param part the workbench part
	 */
	public AbstractPlayAction(IWorkbenchPart part) {
		super(part);
	}

	/**
	 * @return the isPlayOnce
	 */
	public boolean isPlayOnce() {
		return isPlayOnce;
	}

	/**
	 * @param isPlayOnce the isPlayOnce to set
	 */
	public void setPlayOnce(boolean isPlayOnce) {
		this.isPlayOnce = isPlayOnce;
	}

	protected PlayerController getController() {
		return PlayerController.getInstance((EditPartViewer) getWorkbenchPart()
				.getAdapter(EditPartViewer.class));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	final public void run() {
		PlayerController controller = getController();

		if (controller != null) {
			if (listener == null) {
				listener = new PlayerController.IPlayStateListener() {
					public void stateChanged(PlayState state) {
						refreshActionStates();
					}
				};
			}
			controller.addPlayStateListener(listener);
			IPreferenceStore store = UiPlugin.getDefault().getPreferenceStore();
			if (isPlayOnce) {
				boolean disabled = store
						.getBoolean(IScreenConstants.PREF_AUTO_ANIMATION_DISABLED);
				if (disabled) {
					return;
				}
				long duration = store
						.getLong(IScreenConstants.PREF_AUTO_ANIMATION_DURATION);
				if (duration <= 0) {
					duration = DEFAULT_ANIMATE_ONCE_TIME;
				}
				controller.setDuration(duration);
				controller.setPlayOnce(true);
			} else {
				long duration = store
						.getLong(IScreenConstants.PREF_MAX_ANIMATION_DURATION);
				if (duration <= 0) {
					duration = Integer.MAX_VALUE;
				}
				controller.setDuration(duration);
				controller.setPlayOnce(false);
			}
		}

		doRun();
	}

	protected abstract void doRun();

	protected void registerPlayAction() {
		toRefresh.add(getId());
	}

	protected void refreshActionStates() {
		for (String id : toRefresh) {
			try {
				AbstractPlayAction action = (AbstractPlayAction) ((EditorPart) getWorkbenchPart())
						.getEditorSite().getActionBars()
						.getGlobalActionHandler(id);
				if (action != null) {
					action.setEnabled(action.calculateEnabled());
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		setEnabled(calculateEnabled());
	}
}
