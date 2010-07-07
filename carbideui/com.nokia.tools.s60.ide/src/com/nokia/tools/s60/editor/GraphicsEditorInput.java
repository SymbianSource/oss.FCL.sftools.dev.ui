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
package com.nokia.tools.s60.editor;

import java.util.List;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableElement;

import com.nokia.tools.s60.ide.S60WorkspacePlugin;

/**
 * 
 
 */
public class GraphicsEditorInput implements IEditorInput {
	
	//multiple selection activation
	private List<Object> data;
	
	private CommandStack stack;
	
	private IEditorPart parentEditor;
	
	public GraphicsEditorInput(List<Object> listInput, CommandStack stack, IEditorPart p) {
		data = listInput;
		this.stack = stack;
		parentEditor = p;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {		
		return data != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return S60WorkspacePlugin.getIconImageDescriptor("graphics_editor.gif",
				true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		return "Graphics editor";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return "Entity Graphics Editor";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	public List<Object> getData() {
		return data;
	}

	public CommandStack getCommandStack() {
		return stack;
	}

	public IEditorPart getParentEditor() {
		return parentEditor;
	}

	public void dispose() {
		data = null;
		stack = null;
		parentEditor = null;
	}
}
