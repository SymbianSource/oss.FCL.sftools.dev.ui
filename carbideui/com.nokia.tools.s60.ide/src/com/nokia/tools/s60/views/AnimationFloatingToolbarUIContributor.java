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

package com.nokia.tools.s60.views;

import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

import com.nokia.tools.screen.ui.actions.PausePlayingAction;
import com.nokia.tools.screen.ui.actions.PlayAllAction;
import com.nokia.tools.screen.ui.actions.PlaySelectionAction;
import com.nokia.tools.screen.ui.actions.SetPlayingSpeedAction;
import com.nokia.tools.screen.ui.actions.StopPlayingAction;

public class AnimationFloatingToolbarUIContributor extends
		AbstractFloatingToolbarUIContributor {
	
	public AnimationFloatingToolbarUIContributor() {
		super();
		setTitleText("Animation"); 
	}

	@Override
	protected void doCreateControls(Composite parent) {
		ToolBarManager playToolbar = contributeToolBarToCoolBar(
				getAction(PlayAllAction.ID), SWT.HORIZONTAL);
		playToolbar.add(getAction(PlaySelectionAction.ID));
		if (!isVertical()) {
			playToolbar.add(getAction(PausePlayingAction.ID));
			playToolbar.add(getAction(StopPlayingAction.ID));
			playToolbar.add(new Separator());
			playToolbar.add(getAction(SetPlayingSpeedAction.ID));
		}

		ToolBar tb = playToolbar.createControl(parent);
		tb.setBackground(parent.getBackground());

		GridData gd = new GridData(GridData.CENTER, GridData.CENTER, true,
				false);
		tb.setLayoutData(gd);

		if (isVertical()) {
			playToolbar = contributeToolBarToCoolBar(getAction(PausePlayingAction.ID), SWT.HORIZONTAL);
			playToolbar.add(getAction(StopPlayingAction.ID));
			
			tb = playToolbar.createControl(parent);
			tb.setBackground(parent.getBackground());

			gd = new GridData(GridData.CENTER, GridData.CENTER, true, false);
			tb.setLayoutData(gd);

			playToolbar = contributeToolBarToCoolBar(getAction(SetPlayingSpeedAction.ID));

			tb = playToolbar.createControl(parent);
			tb.setBackground(parent.getBackground());

			gd = new GridData(GridData.CENTER, GridData.CENTER, true, false);
			tb.setLayoutData(gd);
		}
	}
}
