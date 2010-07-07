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
package com.nokia.tools.screen.core;

import com.nokia.tools.editing.beaninfo.BeaninfoModelFactory;
import com.nokia.tools.editing.core.InvocationAdapter;

public class ScreenEditingModelFactory extends BeaninfoModelFactory {
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.beaninfo.BeaninfoModelFactory#createInvocationAdapter(java.lang.Object)
	 */
	@Override
	protected InvocationAdapter createInvocationAdapter(Object object) {
		return new ScreenInvocationAdapter(object);
	}

}
