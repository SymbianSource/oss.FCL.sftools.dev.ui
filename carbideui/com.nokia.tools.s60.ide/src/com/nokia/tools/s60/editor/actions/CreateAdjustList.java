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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroups;
import com.nokia.tools.media.utils.svg.ColorizeSVGFilter;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.s60.editor.commands.AddToGroupCommand;
import com.nokia.tools.s60.editor.ui.views.Messages;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.views.ColorsView;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.editing.BasicImageLayer;
import com.nokia.tools.theme.s60.ui.cstore.ComponentStoreFactory;

public class CreateAdjustList extends AbstractAction {

	private static final String ID = "createAdjustList";
	



	public CreateAdjustList(IWorkbenchPart part) {
		super(part);
		setId(ID);
		setText("Adjust List");
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/configs.gif"));
		setLazyEnablementCalculation(true);
		super.init();
		
		
		}

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
		//return true;
	}

}
