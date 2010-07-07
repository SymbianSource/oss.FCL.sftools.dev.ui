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
package com.nokia.tools.s60.editor.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.s60.editor.ui.views.Messages;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.s60.ui.cstore.ComponentStoreFactory;

/**
 */

public class AdjustThemeListAction extends AbstractAction implements
		IMenuCreator {

	@Override
	protected void init() {
		setId(ID);
		setToolTipText(Messages.cstore_customizeAction);
		setText("Adjust List...");
		setDescription(Messages.cstore_customizeAction);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/configs.gif"));

		super.init();
		setMenuCreator(this);
	}

	public static final String ID = "AdjustList";
	private static final String CONTENT_PARAM = "\\{0\\}";
	private static final String QUOTES = "\"";

	/**
	 * Constructor added due contributed actions functionality.
	 * 
	 * @param part
	 */
	public AdjustThemeListAction(IWorkbenchPart part) {
		super(part);
		ActionContributionItem aci = new ActionContributionItem(this);
		IMenuManager m = new MenuManager();
		m.add(new Action("only active") {
			public void run() {
			}
		});
		m.add(new Action("only obsolete") {
			public void run() {
			}
		});
		aci.fill(((MenuManager) m).getMenu(), 1);

	}

	public AdjustThemeListAction(ISelectionProvider provider) {
		super(null);
		setSelectionProvider(provider);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.AbstractAction#doRun(java.lang.Object)
	 */
	@Override
	protected void doRun(Object element) {
		String linkAddress = "com.nokia.tools.s60.preferences.ComponentStorePrefPage";

		PreferencesUtil.createPreferenceDialogOn(
				Display.getCurrent().getActiveShell(), linkAddress,
				new String[] { linkAddress }, null).open();

		ComponentStoreFactory.refreshComponentPool();
	}

	@Override
	protected boolean doCalculateEnabled(Object sel) {
		IContentData data = getContentData(sel);
		if (data != null && data instanceof ThemeData) {
			return true;
		}
		return false;
	}

	public Menu getMenu(Control parent) {
		return null;
	}

	/**
	 * Fill menu with other themes.
	 */
	public Menu getMenu(Menu parent) {
		renameParent(parent);
		addAction(parent);
		// renameParent(parent);
		return null;
	}

	/**
	 * Renaming parent menu.
	 * @param parent
	 */
	private void renameParent(Menu parent) {
		Object data = getSelection().getFirstElement();
		if (data instanceof ThemeData) {
			MenuItem parrentMenu = parent.getParentItem();
			if (parrentMenu != null) {
				parrentMenu.setText(parrentMenu.getText().replaceAll(
						CONTENT_PARAM,
						QUOTES.concat(((ThemeData) data).getName()).concat(
								QUOTES)));
			}
		}
	}

	/**
	 * Add actions related to exiting themes.
	 * 
	 * @param menu
	 */
	private void addAction(Menu menu) {

		IStructuredSelection sel = getSelection();
		if (sel.getFirstElement() instanceof IContentData) {
			IContentData cData = (IContentData) sel.getFirstElement();

			try {
				boolean separatorCreated = false;
				for (String name : ComponentStoreFactory.getComponentPool()
						.getThemeNames()) {
					if (ComponentStoreFactory.getComponentPool().isSkinned(
							cData.getId(), cData.getParent().getId(), name)) {
						//create only one separator
						if (!separatorCreated) {
							Separator separator = new Separator();
							separator.fill(menu, -1);
							separatorCreated = true;
						}
						SkinBySimilarAction action = new SkinBySimilarAction(
								getSelectionProvider(), null, name);
						addAction2Menu(menu, action);
					}
				}
			} catch (Exception e) {
				S60WorkspacePlugin.error(e);
			}

		}
	}

	/**
	 * Add method for new action to given menu.
	 * @param menu
	 * @param action
	 */
	private void addAction2Menu(Menu menu, SkinBySimilarAction action) {
		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(menu, -1);
	}

}
