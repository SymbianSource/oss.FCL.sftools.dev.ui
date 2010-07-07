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

package com.nokia.tools.s60.views;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.widgets.Composite;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.tooltip.DynamicTooltip;
import com.nokia.tools.media.utils.tooltip.IDynamicTooltipUIContribution;
import com.nokia.tools.media.utils.tooltip.DynamicTooltip.ToolbarItemContribution;
import com.nokia.tools.screen.core.INamingAdapter;
import com.nokia.tools.ui.tooltip.CompositeTooltip;

public class IconTooltipUIContributor implements IDynamicTooltipUIContribution {

	Object selection;

	Object uiContainer;
	
	Object context;

	CompositeTooltip tooltip;

	CommandStack stack;

	public void setTooltip(CompositeTooltip tooltip) {
		this.tooltip = tooltip;
	}

	public void setSelection(Object selection) {
		this.selection = selection;
	}

	public void setUIContainer(Object uiContainer) {
		this.uiContainer = uiContainer;
	}
	
	public void setContext(Object context) {
		this.context = context;
	}

	public void setStack(CommandStack stack) {
		this.stack = stack;
	}

	public void createControls(Composite parent, boolean focusState) {
		final IContentData data = (IContentData) selection;
		
		String name = data.getName();
		
		INamingAdapter adapter = (INamingAdapter) data.getAdapter(INamingAdapter.class);
		if (adapter != null && adapter.getName() != null) {
			name = adapter.getName();
		}
		
		((DynamicTooltip) tooltip).setTitle(name);
	}

	public ToolbarItemContribution[] contributeToolbar() {
		return null;
	}
}
