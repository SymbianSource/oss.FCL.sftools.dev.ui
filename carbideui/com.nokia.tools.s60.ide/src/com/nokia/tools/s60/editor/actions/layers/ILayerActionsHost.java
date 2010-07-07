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
package com.nokia.tools.s60.editor.actions.layers;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.ILayer;

/**
 * context for actions operating on layers, 
 * GrpahicsEditorPart and ActiveLayersView implement this
 */
public interface ILayerActionsHost {
	
	void deleteSelected(Object sel);
	
	boolean canDeleteSelected(Object sel);
	
	IStructuredSelection getSelection();
	
	void refresh();
	
	Shell getShell();
	
	IWorkbenchPart getWorkbenchPart();
	
	IImage getActiveImage();
	
	void addSelectionListener(ISelectionChangedListener l);
	
	void removeSelectionListener(ISelectionChangedListener l);

	void selectActiveLayer(ILayer layer);
}
