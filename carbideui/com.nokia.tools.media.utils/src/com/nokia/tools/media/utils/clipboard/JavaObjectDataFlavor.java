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
package com.nokia.tools.media.utils.clipboard;

import java.awt.datatransfer.DataFlavor;

/**
 * flavor for local objects passed in same JVM machine by reference
 *
 */
public class JavaObjectDataFlavor extends DataFlavor {
	
	private Class type;
	
	public JavaObjectDataFlavor(Class clazz, String humanName) {
		super(javaJVMLocalObjectMimeType, humanName);
		type = clazz;
	}
	
	@Override
	public Class<?> getRepresentationClass() {
		return type;
	}

}
