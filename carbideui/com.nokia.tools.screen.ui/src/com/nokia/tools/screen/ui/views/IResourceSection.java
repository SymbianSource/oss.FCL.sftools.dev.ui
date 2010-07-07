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
package com.nokia.tools.screen.ui.views;

import java.util.List;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.viewers.ISelectionProvider;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;

public interface IResourceSection {
	/**
	 * boolean returns whether the section supports given type
	 */
	public boolean supportsContent(IContent content); 
	public String getSectionHeading();
	public List<PaletteDrawer> getPaletteDrawers(IContentAdapter contentAdapter);
	public IResourceViewerSelectionHelper getSelectionHelper(IContentData data);
	public IMenuListener createResourcePositionViewerMenuListener(final ISelectionProvider selectionProvider, final CommandStack stack);
	public IMenuListener createResourceViewerMenuListener(final ISelectionProvider selectionProvider, final CommandStack stack);
}
