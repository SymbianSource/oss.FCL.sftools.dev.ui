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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.TreeEditPart;
import org.eclipse.jface.viewers.LabelProvider;

import com.nokia.tools.editing.model.EditObject;

public abstract class DefaultEditPartFactory implements EditPartFactory {
	public static final int TYPE_GRAPHICAL = 0;
	public static final int TYPE_TREE = 1;

	private int type = TYPE_GRAPHICAL;

	public DefaultEditPartFactory(int type) {
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart,
	 *      java.lang.Object)
	 */
	public EditPart createEditPart(EditPart context, Object model) {
		if (model instanceof EditObject) {
			switch (type) {
			case TYPE_GRAPHICAL:
				return createGraphicalEditPart((EditObject) model);
			case TYPE_TREE:
				TreeEditPart part = createTreeEditPart((EditObject) model);
				if (part instanceof DefaultTreeEditPart
						&& getLabelProvider() != null) {
					((DefaultTreeEditPart) part)
							.setLabelProvider(getLabelProvider());
				}
				return part;
			}
		}
		return null;
	}

	protected GraphicalEditPart createGraphicalEditPart(EditObject model) {
		return new DefaultGraphicalEditPart(model);
	}

	protected TreeEditPart createTreeEditPart(EditObject model) {
		return new DefaultTreeEditPart((EditObject) model);
	}

	protected abstract LabelProvider getLabelProvider();
}
