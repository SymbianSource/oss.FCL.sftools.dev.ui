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

import org.eclipse.gef.editparts.ZoomListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.screen.ui.IScreenConstants;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.utils.EclipseUtils;

public class ZoomFloatingToolbarUIContributor extends
		AbstractFloatingToolbarUIContributor implements ZoomListener {

	public ZoomFloatingToolbarUIContributor() {
		super();
		setTitleText("Zoom"); 
	}

	@Override
	protected void doCreateControls(Composite parent) {
		String[] zoomStrings = new String[] { ZoomManager.FIT_ALL,
				ZoomManager.FIT_HEIGHT, ZoomManager.FIT_WIDTH };

		ZoomComboContributionItem zoom = new ZoomComboContributionItem(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage(), zoomStrings);

		final ZoomManager manager = (ZoomManager) EclipseUtils
				.getActiveSafeEditor().getAdapter(ZoomManager.class);
		if (manager != null) {
			zoom.setZoomManager(manager);
			manager.addZoomListener(this);
		}

		ToolBarManager zoomToolBar = contributeToolBarToCoolBar(zoom);

		if (!isVertical()) {
			zoomToolBar.add(getAction(GEFActionConstants.ZOOM_IN));
			zoomToolBar.add(getAction(GEFActionConstants.ZOOM_OUT));
		}

		ToolBar tb = zoomToolBar.createControl(parent);
		tb.setBackground(parent.getBackground());
		GridData gd = new GridData(GridData.CENTER, GridData.CENTER, true,
				false);
		tb.setLayoutData(gd);

		tb.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (manager != null) {
					manager
							.removeZoomListener(ZoomFloatingToolbarUIContributor.this);
				}
			}
		});

		if (isVertical()) {
			ToolBarManager zoomToolBar2 = contributeToolBarToCoolBar(
					getAction(GEFActionConstants.ZOOM_IN), SWT.HORIZONTAL);
			zoomToolBar2.add(getAction(GEFActionConstants.ZOOM_OUT));
			ToolBar tb2 = zoomToolBar2.createControl(parent);
			tb2.setBackground(parent.getBackground());
			gd = new GridData(GridData.CENTER, GridData.CENTER, true, false);
			tb2.setLayoutData(gd);
		}
	}

	public void zoomChanged(double zoom) {
		IPreferenceStore store = UiPlugin.getDefault().getPreferenceStore();
		store.setValue(IScreenConstants.PREF_ZOOMING_FACTOR, zoom);
	}
}
