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
package com.nokia.tools.theme.s60;

import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.editing.IEntityImageFactory;
import com.nokia.tools.theme.editing.ImageAdapter;
import com.nokia.tools.theme.s60.editing.EditableEntityImageFactory;


public class S60ImageAdapter extends ImageAdapter {

	public S60ImageAdapter(ThemeData data) {
		super(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.ImageAdapter#getEntityImageFactory()
	 */
	@Override
	protected IEntityImageFactory getEntityImageFactory() {
		return EditableEntityImageFactory.getInstance();
	}
}