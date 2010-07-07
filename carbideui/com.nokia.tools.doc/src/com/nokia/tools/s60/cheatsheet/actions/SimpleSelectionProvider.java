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

package com.nokia.tools.s60.cheatsheet.actions;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;

public class SimpleSelectionProvider implements ISelectionProvider {
	
	public ISelection sel;
	
	public SimpleSelectionProvider(Object data) {
		sel = new StructuredSelection(data);
	}
	
	public SimpleSelectionProvider(ISelection data) {
		sel = data;
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {		
	}

	public ISelection getSelection() {
		return sel;
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
	}

	public void setSelection(ISelection selection) {
	}

}
