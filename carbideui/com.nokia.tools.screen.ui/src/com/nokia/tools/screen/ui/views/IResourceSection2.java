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
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.ui.widgets.ImageLabel;

public interface IResourceSection2 {
	/**
	 * boolean returns whether the section supports given type
	 */
	public boolean supportsContent(IContent content);

	public String getSectionHeading();

	public List<ResourceTableInput> getPaletteDrawers(
			IContentAdapter contentAdapter);

	public IResourceViewerSelectionHelper2 getSelectionHelper(IContentData data);

	public IMenuListener createResourceViewerMenuListener(
			final ISelectionProvider selectionProvider, final CommandStack stack);

	public List<ImageLabel> getNavigationControlItems(Composite parent,
			IContentAdapter adapter);

	public List<ResourceTableMasterGroup> filterInput(
			List<ResourceTableInput> tempInputs, ImageLabel imageAboveLabel);
}
