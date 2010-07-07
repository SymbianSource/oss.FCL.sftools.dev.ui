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
package com.nokia.tools.theme.core;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.LabelProvider;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.platform.layout.LayoutNode;

public class LayoutLabelProvider extends LabelProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		LayoutNode node = (LayoutNode) EditingUtil.getBean((EObject) element);
		return node.getShortId();
	}

}
