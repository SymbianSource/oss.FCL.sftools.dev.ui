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

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener3;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The class as all other in package perspectivehack is created for product
 * level customizing when plugin is packaged in standalone tool Then based on
 * chenges in perspective toolbar and menu are hidden
 * 
 */
public class PerspectiveHackManager
    implements IPerspectiveListener3 {
	private String perspectiveId;
	private ActionSetCustomizer actionSetCustomizer;
	private ToolbarHider toolbarHider;
	private MenuCustomizer menuCustomizer;
	private IPartListener partListener;

	/**
	 * @return the perspectiveId
	 */
	public String getPerspectiveId() {
		return perspectiveId;
	}

	/**
	 * @param perspectiveId the perspectiveId to set
	 */
	public void setPerspectiveId(String perspectiveId) {
		this.perspectiveId = perspectiveId;
	}

	/**
	 * @return the actionSetCustomizer
	 */
	public ActionSetCustomizer getActionSetCustomizer() {
		return actionSetCustomizer;
	}

	/**
	 * @param actionSetCustomizer the actionSetCustomizer to set
	 */
	public void setActionSetCustomizer(ActionSetCustomizer actionSetCustomizer) {
		this.actionSetCustomizer = actionSetCustomizer;
	}

	/**
	 * @return the toolbarHider
	 */
	public ToolbarHider getToolbarHider() {
		return toolbarHider;
	}

	/**
	 * @param toolbarHider the toolbarHider to set
	 */
	public void setToolbarHider(ToolbarHider toolbarHider) {
		this.toolbarHider = toolbarHider;
	}

	/**
	 * @return the menuCustomizer
	 */
	public MenuCustomizer getMenuCustomizer() {
		return menuCustomizer;
	}

	/**
	 * @param menuCustomizer the menuCustomizer to set
	 */
	public void setMenuCustomizer(MenuCustomizer menuCustomizer) {
		this.menuCustomizer = menuCustomizer;
	}

	public void init() {
		IDEUtil.getActiveWorkbenchWindow().addPerspectiveListener(
		    PerspectiveHackManager.this);
		IPartService partService = IDEUtil.getActiveWorkbenchWindow()
		    .getPartService();
		partListener = new ProjectWatchDog();
		partService.addPartListener(partListener);
	}

	public void deinit() {
		IWorkbenchWindow window = IDEUtil.getActiveWorkbenchWindow();
		if (window != null) {
			window.removePerspectiveListener(PerspectiveHackManager.this);
			if (partListener != null) {
				window.getPartService().removePartListener(partListener);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.
	 * ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveActivated(IWorkbenchPage page,
	    IPerspectiveDescriptor perspective) {
		if (perspective.getId().startsWith(perspectiveId)) {
			initializePerspective();
		} else {
			deinitializePerspective();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui
	 * .IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page,
	    IPerspectiveDescriptor perspective, String changeId) {
		if (perspective.getId().startsWith(perspectiveId)) {
			initializePerspective();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.IPerspectiveListener3#perspectiveClosed(org.eclipse.ui
	 * .IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveClosed(IWorkbenchPage page,
	    IPerspectiveDescriptor perspective) {
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.IPerspectiveListener3#perspectiveDeactivated(org.eclipse
	 * .ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveDeactivated(IWorkbenchPage page,
	    IPerspectiveDescriptor perspective) {
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.IPerspectiveListener3#perspectiveOpened(org.eclipse.ui
	 * .IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveOpened(IWorkbenchPage page,
	    IPerspectiveDescriptor perspective) {
		if (perspective.getId().startsWith(perspectiveId)) {
			initializePerspective();
		} else {
			deinitializePerspective();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.IPerspectiveListener3#perspectiveSavedAs(org.eclipse.ui
	 * .IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor,
	 * org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveSavedAs(IWorkbenchPage page,
	    IPerspectiveDescriptor oldPerspective,
	    IPerspectiveDescriptor newPerspective) {
		

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.IPerspectiveListener2#perspectiveChanged(org.eclipse.ui
	 * .IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor,
	 * org.eclipse.ui.IWorkbenchPartReference, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page,
	    IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef,
	    String changeId) {
		if (perspective.getId().startsWith(perspectiveId)) {
			initializePerspective();
		} else {
			deinitializePerspective();
		}
	}

	public void initializePerspective() {
		
		if (actionSetCustomizer != null) {
			actionSetCustomizer.turnOffActionSets();
		}
		if (toolbarHider != null) {
			toolbarHider.hideExternToolBar();
			toolbarHider.hidePerspectiveBar();
		}
		if (menuCustomizer != null) {
			menuCustomizer.hideExternMenus();
		}
	}

	
	public void deinitializePerspective() {
		if (toolbarHider != null) {
			toolbarHider.restorePerspectiveBar();
			toolbarHider.restoreExternToolBar();
		}
		if (menuCustomizer != null) {
			menuCustomizer.restoreExternMenus();
		}
	}
}
