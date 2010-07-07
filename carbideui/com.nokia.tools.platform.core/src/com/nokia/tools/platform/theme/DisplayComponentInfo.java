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
package com.nokia.tools.platform.theme;

import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.layout.ComponentInfo;

public class DisplayComponentInfo extends ComponentInfo {
	private static final long serialVersionUID = 1L;
	
	private transient Display display;

	public DisplayComponentInfo(String name, int variety, String locId) {
		super(name, variety, locId);
	}

	public DisplayComponentInfo(String name, String variety, String locId) {
		super(name, variety, locId);
	}

	/**
	 * @return the display
	 */
	public Display getDisplay() {
		return display;
	}

	/**
	 * @param display the display to set
	 */
	public void setDisplay(Display display) {
		this.display = display;
	}
}
