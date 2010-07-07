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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.media.player.IPlayer;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.utils.ScreenUtil;

public class SetPlayingSpeedAction extends AbstractPlayAction implements SelectionListener  {
	public static final String ID = "PLAYING_SPEED";
	private int newPlayingSpeed = 50;
	
	private Scale scale;
	
	/**
	 * Constructs an action associated with the given part.
	 * 
	 * @param part the workbench part
	 */
	public SetPlayingSpeedAction(IWorkbenchPart part) {
		super(part);
		setId(ID);
		setToolTipText(Messages.PlayMedia_PlayingSpeed);
		setImageDescriptor(UiPlugin.getImageDescriptor("icons/animspeed.gif"));
		setDisabledImageDescriptor(UiPlugin.getImageDescriptor("icons/animspeed_disabled.gif"));
		registerPlayAction();
	}
	
	@Override
	public void runWithEvent(Event event) {
		if (event.widget instanceof ToolItem) {
			displayScale((ToolItem) event.widget);
		}
		else {
			super.runWithEvent(event);
		}
	}
	
	public void displayScale(ToolItem item) {
		if (scale != null) {
			return;
		}

		final Shell shell = new Shell(item.getParent().getShell(), SWT.TOOL);
		shell.setLayout(new FillLayout());

		final Listener filter = new Listener() {
			public void handleEvent(Event event) {
				if (event.widget != scale) {
					shell.dispose();
				}
			}
		};

		Display.getCurrent().addFilter(SWT.MouseDown, filter);

		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				Display.getCurrent().removeFilter(SWT.MouseDown, filter);
				scale = null;
			}
		});

		scale = new Scale(shell, SWT.HORIZONTAL);
		scale.setMaximum(100);
		scale.setPageIncrement(10);
		scale.setSelection(getPlayingSpeed());
		scale.addSelectionListener(this);

		shell.setSize(scale.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		shell.setLocation(item.getParent().toDisplay(
				item.getBounds().x - shell.getBounds().width / 2
						+ item.getBounds().width / 2,
				item.getBounds().y + item.getBounds().height));
		shell.open();
	}
	
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		if (e.widget == scale) {
			setPlayingSpeed(scale.getSelection());
			if (isEnabled()) {
				run();
			}
		}
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
		controller.setSpeedFactor(getSpeedFactor());
	}

	protected float getSpeedFactor() {
		float speedFactor = 1.0f;
		if (newPlayingSpeed > 50) {
			speedFactor = 1 + (newPlayingSpeed - 50) / 10f;
		}
		if (newPlayingSpeed < 50) {
			speedFactor = (newPlayingSpeed) / 50f;	
		}
		
		return speedFactor;
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

		IScreenElement screen = ScreenUtil.getScreen(viewer);
		if (screen == null) {
			return false;
		}
		boolean isEnabled = false;
		for (IScreenElement element : screen.getAllChildren()) {
			IPlayer player = (IPlayer) element.getAdapter(IPlayer.class);
			if (player != null && player.isPlayable()) {
				isEnabled = true;
				break;
			}
		}
		if (!isEnabled) {
			return false;
		}

		return true;
	}

	public int getPlayingSpeed() {
		int playingSpeed = 50;
		
		PlayerController controller = PlayerController
			.getInstance((EditPartViewer) getWorkbenchPart().getAdapter(
					EditPartViewer.class));
		if (controller != null) {
			float speedFactor = controller.getSpeedFactor();
			if (speedFactor <= 1.0f) {
				playingSpeed = (int) (speedFactor * 50f);
			} else {
				playingSpeed = (int) ((speedFactor + 4f) * 10f);
			}
		}
		return playingSpeed;
	}

	public void setPlayingSpeed(int playingSpeed) {
		this.newPlayingSpeed = playingSpeed;
	}
}
