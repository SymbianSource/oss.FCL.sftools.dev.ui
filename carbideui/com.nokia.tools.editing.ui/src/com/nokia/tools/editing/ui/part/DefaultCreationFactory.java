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

import org.eclipse.gef.requests.CreationFactory;

import com.nokia.tools.editing.core.IEditingModelFactory;
import com.nokia.tools.editing.ui.Activator;

public class DefaultCreationFactory implements CreationFactory {
	private IEditingModelFactory factory;
	private Class clazz;

	public DefaultCreationFactory(IEditingModelFactory factory, Class clazz) {
		this.factory = factory;
		this.clazz = clazz;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.requests.CreationFactory#getNewObject()
	 */
	public Object getNewObject() {
		try {
			return factory.createEditObject(clazz.newInstance());
		} catch (Exception e) {
			Activator.error(e);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.requests.CreationFactory#getObjectType()
	 */
	public Object getObjectType() {
		return clazz;
	}
}
