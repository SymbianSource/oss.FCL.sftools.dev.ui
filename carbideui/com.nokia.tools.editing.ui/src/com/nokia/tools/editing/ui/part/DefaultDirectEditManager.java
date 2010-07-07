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
package com.nokia.tools.editing.ui.part;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;

import com.nokia.tools.editing.core.EditingUtil;

public class DefaultDirectEditManager extends DirectEditManager {

	public DefaultDirectEditManager(GraphicalEditPart source, Class editorType,
			CellEditorLocator locator, EStructuralFeature feature) {
		super(source, editorType, locator, feature);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.tools.DirectEditManager#initCellEditor()
	 */
	@Override
	protected void initCellEditor() {
		Object value = EditingUtil.getFeatureValue((EObject) getEditPart()
				.getModel(), (EStructuralFeature) getDirectEditFeature());
		if (value != null) {
			getCellEditor().setValue(value);
		}
	}
}
