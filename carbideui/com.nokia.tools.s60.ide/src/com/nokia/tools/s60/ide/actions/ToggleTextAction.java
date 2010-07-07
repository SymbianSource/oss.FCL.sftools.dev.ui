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
package com.nokia.tools.s60.ide.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.tools.s60.ide.IS60IDEConstants;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.views.IconViewPage;

/**
 */
public class ToggleTextAction extends Action {
	public static final String ID = IS60IDEConstants.PLUGIN_ID + ".toggle_text";

	private IconViewPage page;

	public ToggleTextAction(IconViewPage page) {
		this.page = page;
		setToolTipText(ActionMessages.ToggleTextAction_tooltip);
		setImageDescriptor(S60WorkspacePlugin.getIconImageDescriptor(
				"toggle_titles16x16.png", true));
		setDisabledImageDescriptor(S60WorkspacePlugin.getIconImageDescriptor(
				"toggle_titles16x16.png", false));
		setId(ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#getStyle()
	 */
	@Override
	public int getStyle() {
		return AS_CHECK_BOX;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		page.updateImageLabel();
		IPreferenceStore store = S60WorkspacePlugin.getDefault()
				.getPreferenceStore();
		store.setValue(IS60IDEConstants.PREF_SHOW_TITLE_TEXTS, isChecked());
	}
}
