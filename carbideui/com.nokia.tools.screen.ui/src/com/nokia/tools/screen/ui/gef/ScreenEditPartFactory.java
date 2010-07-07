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

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.TreeEditPart;
import org.eclipse.jface.viewers.LabelProvider;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.model.EditDiagram;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.adapter.IFreeformElementAdapter;
import com.nokia.tools.editing.ui.part.DefaultEditPartFactory;
import com.nokia.tools.editing.ui.part.FreeformElementGraphicalEditPart;

public class ScreenEditPartFactory extends DefaultEditPartFactory {

	public ScreenEditPartFactory(int type) {
		super(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.part.DefaultEditPartFactory#createGraphicalEditPart(com.nokia.tools.editing.model.EditObject)
	 */
	@Override
	protected GraphicalEditPart createGraphicalEditPart(EditObject model) {
	
		EditObject current = model;
		while (current.getParent() != null
				&& !(current.getParent() instanceof EditDiagram)) {
			current = current.getParent();
		}
		IFreeformElementAdapter adapter = (IFreeformElementAdapter) EditingUtil
				.getAdapter(current, IFreeformElementAdapter.class);
		if (adapter != null && adapter.getFreeformElements().contains(model)) {
			return new FreeformElementGraphicalEditPart(model);
		}
		return new SComponentGraphicalEditPart(model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.part.DefaultEditPartFactory#createTreeEditPart(com.nokia.tools.editing.model.EditObject)
	 */
	@Override
	protected TreeEditPart createTreeEditPart(EditObject model) {
		return new SComponentTreeEditPart(model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.part.DefaultEditPartFactory#getLabelProvider()
	 */
	@Override
	protected LabelProvider getLabelProvider() {
		return new WidgetLabelProvider();
	}
}
