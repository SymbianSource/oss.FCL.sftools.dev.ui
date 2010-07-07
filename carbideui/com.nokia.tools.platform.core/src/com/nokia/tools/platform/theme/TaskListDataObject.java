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

package com.nokia.tools.platform.theme;

public class TaskListDataObject {

	private boolean selected = false;
	private boolean enabled = true;
	private String name = null;
	private ThemeBasicData sbd = null;

	public TaskListDataObject(String name, boolean selected) {
		this.selected = selected;
		this.name = name;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setTransferSelection(boolean selected) {
		setSelected(selected);
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String toString() {
		return name;
	}

	public void setSkinData(ThemeBasicData sbd) {
		this.sbd = sbd;
	}

	public ThemeBasicData getSkinData() {
		return sbd;
	}

}