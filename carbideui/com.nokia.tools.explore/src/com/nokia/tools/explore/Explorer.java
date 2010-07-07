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

package com.nokia.tools.explore;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class Explorer {

	private static final String COMMAND = "explorer /select,";

	private Explorer() {
	}

	private static void locateOnDisk(String path) {
		try {
			Runtime.getRuntime().exec(Explorer.COMMAND + path);
		} catch (IOException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.OK, Activator.PLUGIN_ID, e.getMessage(),
							e));
		}
	}

	public static void locateOnDisk(List<String> pathList) {
		for (String path : pathList) {
			locateOnDisk(path);
		}
	}
}
