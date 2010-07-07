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

package com.nokia.tools.s60.wizards;

import java.util.ArrayList;
import java.util.List;

public class PluginProperties {
	private List<Object[]> list = new ArrayList<Object[]>();

	public void title(Object name) {
		list.add(new Object[] { name });
	}

	public void separator() {
		list.add(new Object[0]);
	}

	public void property(Object name, Object value) {
		list.add(new Object[] { name, value });
	}

	public Object[] getProperties() {
		return list.toArray();
	}
}
