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
package com.nokia.tools.screen.ui.editor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.SubCoolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.ui.editor.BaseGraphicalEditorPart;

/**
 * This contributor contributes the zooming and resolution change actions to the
 * main action bar and the menu.
 * 
 */
public abstract class BaseEditorContributor extends ActionBarContributor
		implements IPartListener, IPropertyListener {
	public static final int STATE_REFRESH = 0;
	public static final int STATE_ACTIVATE = 1;
	public static final int STATE_DEACTIVATE = 2;

	protected ICoolBarManager parentCoolBarManager;
	protected Set<ToolBarContributionItem> coolBarItems = new HashSet<ToolBarContributionItem>();
	private IEditorPart editor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#init(org.eclipse.ui.IActionBars)
	 */
	@Override
	public void init(IActionBars bars) {
		super.init(bars);
		getPage().addPartListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToCoolBar(org.eclipse.jface.action.ICoolBarManager)
	 */
	@Override
	public void contributeToCoolBar(ICoolBarManager coolBarManager) {
		super.contributeToCoolBar(coolBarManager);
		SubCoolBarManager manager = (SubCoolBarManager) coolBarManager;
		parentCoolBarManager = (ICoolBarManager) manager.getParent();
		contributeToParentCoolBar();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#dispose()
	 */
	@Override
	public void dispose() {
		getPage().removePartListener(this);
		if (getContentEditor() != null) {
			getContentEditor().removePropertyListener(this);
		}
		if (parentCoolBarManager != null && canDisposeCoolBarItems()) {
			for (ToolBarContributionItem item : coolBarItems) {
				IContributionItem realItem = parentCoolBarManager.find(item
						.getId());
				
				 // We should not call dispose method directly, unless we
				 // removed contribution item from the containing
			     // IContributionManager before the contribution lifecycle
			     // has ended.
				parentCoolBarManager.remove(realItem);
				if (realItem != null) {
					realItem.dispose();
				}
			}
			if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
					&& PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage() != null) {
				parentCoolBarManager.update(true);
			}
		}
		editor = null;
		super.dispose();
	}

	protected boolean canDisposeCoolBarItems() {
		 return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
		// restore startup problem
		if (null == PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				|| null == PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage()) {
			return;
		}

		IEditorPart newEditor = getContentEditor();

		if (newEditor == null) {
			for (IContributionItem item : coolBarItems) {
				parentCoolBarManager.remove(item);
			}
			coolBarItems.clear();
		} else {
			if (getActionBars() instanceof IActionBars2) {
				contributeToCoolBar(((IActionBars2) getActionBars())
						.getCoolBarManager());
			}
		}

		if (isEditorActive()) {
			getContentEditor().addPropertyListener(this);
		}

		if (newEditor == null) {
			refresh(STATE_DEACTIVATE);
		} else if (editor != newEditor) {
			refresh(STATE_ACTIVATE);
		} else {
			refresh(STATE_REFRESH);
		}

		try {
			parentCoolBarManager.update(true);
		} catch (IndexOutOfBoundsException ioe) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
		if (isEditorActive() && getContentEditor() == part) {
			getContentEditor().removePropertyListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
	}

	/**
	 * @return true if the editor is active.
	 */
	protected boolean isEditorActive() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActivePart() == getContentEditor()
				&& getContentEditor() != null;
	}

	/**
	 * @return the active content editor
	 */
	protected IEditorPart getContentEditor() {
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null
				|| PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage() == null) {
			return null;
		}
		IEditorPart editor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor == null || editor.getAdapter(IContentAdapter.class) == null) {
			return null;
		}
		return editor;
	}

	protected IToolBarManager contributeToolBarToCoolBar(IContributionItem item) {
		IToolBarManager toolBar = new ToolBarManager(parentCoolBarManager
				.getStyle());
		toolBar.add(item);

		ToolBarContributionItem toolBarItem = new ToolBarContributionItem(
				toolBar, item.getId());
		// this is used to remove the previous added item, otherwise the new
		// item won't appear :)
		parentCoolBarManager.remove(item.getId());
		parentCoolBarManager.add(toolBarItem);

		coolBarItems.add(toolBarItem);
		return toolBar;
	}

	protected IToolBarManager contributeToolBarToCoolBar(final IAction action) {
		IToolBarManager toolBar = new ToolBarManager(parentCoolBarManager
				.getStyle()) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.ToolBarManager#update(boolean)
			 */
			@Override
			public void update(boolean force) {
				super.update(force);
				if (action instanceof IMenuCreator) {
					action.run();
				}
			}

		};
		toolBar.add(action);

		ToolBarContributionItem toolBarItem = new ToolBarContributionItem(
				toolBar, action.getId());
		// this is used to remove the previous added item, otherwise the new
		// item won't appear :)
		parentCoolBarManager.remove(action.getId());
		parentCoolBarManager.add(toolBarItem);

		coolBarItems.add(toolBarItem);
		return toolBar;
	}

	/**
	 * Contributes new toolbar items to the parent coolbar.
	 */
	protected void contributeToParentCoolBar() {
	}

	/**
	 * Refreshes the action bar.
	 */
	abstract protected void refresh(int state);

	/*
	 * a (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object,
	 *      int)
	 */
	public void propertyChanged(Object source, int propId) {
		if (source != null && source == getContentEditor()
				&& propId == BaseGraphicalEditorPart.PROP_VIEWER) {
			refresh(STATE_ACTIVATE);
		}
	}

	public void setActiveEditor(IEditorPart editor) {
		try {
			super.setActiveEditor(editor);
		} catch (Exception e) {
		}
		getActionBars().updateActionBars();
	}
}
