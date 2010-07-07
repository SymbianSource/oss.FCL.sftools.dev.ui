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
package com.nokia.tools.ui.ide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class for achieving hide show of specific defined in other plugins
 * toobar items for purpose of overriding (example is hiding of New drop down as
 * plugin introduced the same
 * 
 */
public class ToolbarHider {

	private List<IContributionItem> removedItems = new ArrayList<IContributionItem>();

	private List<RemovedGroup> removedGroups = new ArrayList<RemovedGroup>();

	private Control perspectiveBar;

	private IContributionItem coolbarContextMenuItem;

	private Set<String> idsToRem = new HashSet<String>();

	private Set<String> groupsToRem = new HashSet<String>();

	public void addToolbarId(String id) {
		idsToRem.add(id);
	}

	public void addToolbarGroup(String group) {
		groupsToRem.add(group);
	}

	protected void hidePerspectiveBar() {
		CBanner banner = getBanner();
		if (null == banner)
			return;
		if (null != banner.getRight()) {
			perspectiveBar = banner.getRight();
			perspectiveBar.setVisible(false);
			banner.setRight(null);
			toggleCoolbarCustomizePerspectiveCommand(false);
		}
	}

	protected void restorePerspectiveBar() {
		CBanner banner = getBanner();
		if (null == banner)
			return;
		if (null == banner.getRight() && null != perspectiveBar) {
			perspectiveBar.setVisible(true);
			banner.setRight(perspectiveBar);
			perspectiveBar = null;
			toggleCoolbarCustomizePerspectiveCommand(true);
		}
	}

	private void toggleCoolbarCustomizePerspectiveCommand(final boolean visible) {
		CoolBar coolBar = getMainCoolBar();
		if (null != coolBar.getMenu()) {
			coolBar.getMenu().addMenuListener(new MenuAdapter() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.MenuAdapter#menuShown(org.eclipse.swt.events.MenuEvent)
				 */
				@Override
				public void menuShown(MenuEvent e) {
					Menu menu = ((Menu) e.widget);
					if (!visible) {
						// removes customize perspective -command
						MenuItem[] items = menu.getItems();
						if (null == coolbarContextMenuItem)
							coolbarContextMenuItem = ((IContributionItem) items[items.length - 1]
							    .getData());
						if (items.length > 1)
							items[items.length - 1].dispose();
					} else {
						// restores customize perspective -command
						if (null != coolbarContextMenuItem)
							coolbarContextMenuItem.fill(menu, 1);
					}
				}
			});
		}
	}

	protected void hideExternToolBar() {
		setVisibleForListedIds(false);
		CoolBar coolBar = getMainCoolBar();
		if (null == coolBar)
			return;
		for (CoolItem item : ((CoolBar) coolBar).getItems()) {
			// check disposed, can happen when reset perspective
			if (!item.isDisposed()
			    && item.getData() instanceof ToolBarContributionItem) {
				ToolBarContributionItem toolbaritem = ((ToolBarContributionItem) item
				    .getData());

				if (shouldRemoveGroup(toolbaritem)) {
					CoolBarManager mngr = (CoolBarManager) toolbaritem
					    .getParent();
					if (null == mngr) {
						
						mngr = ((ApplicationWindow) PlatformUI.getWorkbench()
						    .getActiveWorkbenchWindow()).getCoolBarManager();
						toolbaritem.setParent(mngr);
						mngr.refresh();

					}
					IContributionItem remitem = mngr.remove(toolbaritem);
					if (null != remitem)
						removedItems.add(remitem);
					mngr.update(true);
				}

			}
		}
	}

	private void setVisibleForListedIds(boolean visible) {
		CoolBar coolBar = getMainCoolBar();
		if (null == coolBar)
			return;
		for (CoolItem item : ((CoolBar) coolBar).getItems()) {
			if (item.getData() instanceof ToolBarContributionItem) {
				ToolBarContributionItem toolbaritem = ((ToolBarContributionItem) item
				    .getData());

				IToolBarManager toolBarManager = toolbaritem
				    .getToolBarManager();
				IContributionItem[] items = toolBarManager.getItems();
				for (int i = 0; i < items.length; i++) {
					if (shouldRemove(items[i])) {
						if (items[i] instanceof Separator && !visible) {
							// hiding separator is not enough, the actions added
							// for the group will be shown anyway
							RemovedGroup removed = new RemovedGroup();
							removed.beforeId = i == 0 ? null : items[i - 1]
							    .getId();
							removed.item = items[i];
							removedGroups.add(removed);
							toolBarManager.remove(items[i]);
						} else {
							toolBarManager.remove(items[i]);
						}
					}
				}
				
				if (visible) {
					// restores the removed groups
					for (RemovedGroup group : removedGroups) {
						if (group.beforeId != null) {
							toolBarManager.insertAfter(group.beforeId,
							    group.item);
						} else {
							toolBarManager.add(group.item);
						}
					}
					removedGroups.clear();
				}
			}
		}
	}

	private boolean shouldRemove(IContributionItem item) {
		return (idsToRem.contains(item.getId()));
	}

	private boolean shouldRemoveGroup(IContributionItem item) {
		return (groupsToRem.contains(item.getId()));
	}

	public CoolBar getMainCoolBar() {
		final Shell shell = PlatformUI.getWorkbench()
		    .getActiveWorkbenchWindow().getShell();
		Control[] ctrl = shell.getChildren();
		Control[] ctrl1 = ((Composite) ctrl[0]).getChildren();
		Control[] ctrl2 = ((Composite) ctrl1[0]).getChildren();
		for (Control coolBar : ctrl2) {
			if (coolBar instanceof CoolBar) {
				return (CoolBar) coolBar;
			}
		}
		return null;
	}

	protected CBanner getBanner() {
		final Shell shell = PlatformUI.getWorkbench()
		    .getActiveWorkbenchWindow().getShell();
		Control[] ctrl = shell.getChildren();
		Control ctr=null;
		if(ctrl[0] instanceof CBanner ){ // before typecasting just check if it can be casted 
			ctr = (CBanner) ctrl[0];
		}
		if ((ctr != null && (ctr instanceof CBanner))) {
			return (CBanner) ctr;
		}
		return null;
	}

	protected void restoreExternToolBar() {
		setVisibleForListedIds(true);
		CoolBar coolBar = getMainCoolBar();
		if (null == coolBar)
			return;
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() instanceof ApplicationWindow) {
			IContributionManager coolbarmanager = ((ApplicationWindow) PlatformUI
			    .getWorkbench().getActiveWorkbenchWindow()).getCoolBarManager();
			for (IContributionItem item : removedItems) {
				coolbarmanager.add(item);
			}
			((CoolBarManager) coolbarmanager).update(true);
			removedItems.clear();
		}
	}

	class RemovedGroup {
		String beforeId;
		IContributionItem item;
	}
}
