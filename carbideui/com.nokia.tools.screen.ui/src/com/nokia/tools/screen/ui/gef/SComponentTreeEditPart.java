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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.views.properties.IPropertySource;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.part.DefaultTreeEditPart;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.widget.SContainer;

public class SComponentTreeEditPart extends DefaultTreeEditPart {
	public SComponentTreeEditPart(EditObject model) {
		super(model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.EditPart#performRequest(org.eclipse.gef.Request)
	 */
	@Override
	public void performRequest(Request req) {
		// filter out doubleClick request
		if (req.getType() == RequestConstants.REQ_OPEN) {

			IEditorPart editorPart = ((DefaultEditDomain) getViewer()
					.getEditDomain()).getEditorPart();
			if (editorPart instanceof IDoubleClickListener) {
				((IDoubleClickListener) editorPart).doubleClick(this);
				return;
			}
		}
		super.performRequest(req);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ve.internal.jfc.core.ComponentGraphicalEditPart#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class type) {
		if (IPropertySource.class == type) {
			IScreenElement adapter = JEMUtil
					.getScreenElement((EObject) getModel());
			if (adapter != null && adapter.getTargetAdapter() != null) {
				return EcoreUtil.getRegisteredAdapter(adapter
						.getTargetAdapter().getWidget(), IPropertySource.class);
			}
		}
		return super.getAdapter(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.part.DefaultTreeEditPart#isContainer()
	 */
	@Override
	protected boolean isContainer() {
		return (EditingUtil.getBean((EditObject) getModel()) instanceof SContainer);
	}
}
