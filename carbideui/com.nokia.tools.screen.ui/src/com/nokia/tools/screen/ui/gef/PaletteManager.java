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

package com.nokia.tools.screen.ui.gef;

import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;

public class PaletteManager {
	private PaletteRoot palette;
	private boolean required = true;

	public void initializePalette() {
		palette = new PaletteRoot();
		createToolsGroup();
	}

	public PaletteRoot getPaletteRoot() {
		return palette;
	}

	protected void createToolsGroup() {
		PaletteGroup toolGroup = new PaletteGroup("Tools");

		ToolEntry tool = new PanningSelectionToolEntry();
		toolGroup.add(tool);
		palette.setDefaultEntry(tool);

		toolGroup.add(new MarqueeToolEntry());
		palette.add(toolGroup);
	}
	
	public boolean isRequired(){
		return required;
	}
	
	public void setRequired(boolean required){
		this.required = required;
	}
}
