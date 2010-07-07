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

import java.awt.datatransfer.Clipboard;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelectionProvider;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.utils.SimpleSelectionProvider;
import com.nokia.tools.theme.s60.ui.cstore.ComponentStoreFactory;
import com.nokia.tools.theme.s60.ui.cstore.IComponentPool;

/**
 * Takes as input list of SkinnableEntities and pastes it's content to accrding theme elements.
 * Used for 'Skin From Examples (Similar Components)'.
*/
public class SkinBySimilarAction extends AbstractAction {

	public static final String ID = "SimilarElements";
	
	private String sourceTheme;

	@Override
	protected void init() {
		setId(ID);		
	}

	public SkinBySimilarAction(ISelectionProvider provider, CommandStack stack, String themeName) {
		super(null);
		setSelectionProvider(provider);
		this.stack = stack;
		this.sourceTheme = themeName;
		setText(sourceTheme);
	}

	boolean incompatibleContent = false;
	
	@Override
	public void doRun(Object sel) {			
		IContentData data = getContentData(sel);
		
		if (data != null) {
			
			
			IComponentPool cSup = ComponentStoreFactory.getComponentPool();
			IContentData themeData = cSup.getComponentFromPool(data.getId(), data.getParent().getId(), sourceTheme);
			ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data
				.getAdapter(ISkinnableEntityAdapter.class);
			if (themeData != null && skAdapter != null) {
				Clipboard c = new Clipboard("");
				CopyContentDataAction copy = new CopyContentDataAction(new SimpleSelectionProvider(themeData),c);
				copy.setSyncExec(true);
				copy.run();
				
				PasteContentDataAction paste = new PasteContentDataAction(getSelectionProvider(), stack, c);
				paste.setAsk(false);
				if (paste.isEnabled())
					paste.run();
			}			
		}
	}

	@Override
	protected boolean doCalculateEnabled(Object sel) {
		IContentData data = getContentData(sel);
		if (data != null) {
			return true;
		}
		return false;
	}

}
