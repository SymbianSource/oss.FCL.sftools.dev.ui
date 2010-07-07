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

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.s60.editor.ui.views.Messages;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;

/**
 * dropdown / submenu for set stretch mode action
 */
public class SetStretchModeDDown extends Action implements IMenuCreator {

	private Menu menu;

	private IWorkbenchPart part;

	private ISelectionProvider provider;

	private CommandStack stack;

	private SetStretchModeAction[] actions;

	/**
	 * Construxts an action.
	 * 
	 * @param view
	 *            the current icon page.
	 */
	public SetStretchModeDDown(IWorkbenchPart part,
			ISelectionProvider provider, CommandStack stack) {
		this.part = part;
		this.provider = provider;
		this.stack = stack;
		setMenuCreator(this);
		setText(Messages.submenu_StretchMode_label);
		setToolTipText(Messages.submenu_StretchMode_label);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/stretchMode.png"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (menu != null) {
			menu.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		if (menu != null) {
			menu.dispose();
		}
		menu = new Menu(parent);
		_build();
		return menu;
	}

	private void _build() {

		actions = new SetStretchModeAction[IMediaConstants.STRETCHMODES_VALID.length];
		
		if (provider != null) {
			int c = 0;
			for (String mode : IMediaConstants.STRETCHMODES_VALID) {
				actions[c++] = new SetStretchModeAction(provider, stack, mode);
			}
		} else {
			int c = 0;
			for (String mode : IMediaConstants.STRETCHMODES_VALID) {
				actions[c++] = new SetStretchModeAction(part, mode);
			}
		}
		if (actions != null && actions.length > 0) {
			for (SetStretchModeAction a : actions) {
				if (a.isEnabled())
					new ActionContributionItem(a).fill(menu, -1);
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return new SetStretchModeAction(provider != null ? provider : part
				.getSite().getSelectionProvider(), stack,
				IMediaConstants.STRETCHMODE_ASPECT).isEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		if (menu != null) {
			menu.dispose();
		}
		menu = new Menu(parent);
		_build();
		return menu;
	}
}
