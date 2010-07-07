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
package com.nokia.tools.widget.theme;

import com.nokia.tools.widget.SComponent;
import com.nokia.tools.widget.SContainer;

public class ThemeElement extends SContainer {
	static final long serialVersionUID = 8616840904324819458L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.widget.SContainer#isChildValid(com.nokia.tools.widget.SComponent)
	 */
	@Override
	public boolean isChildValid(SComponent child) {
		return child instanceof ThemeElement;
	}
}
