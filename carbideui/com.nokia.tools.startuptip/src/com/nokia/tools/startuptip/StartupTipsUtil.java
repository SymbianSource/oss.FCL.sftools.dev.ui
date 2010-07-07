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

package com.nokia.tools.startuptip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

public class StartupTipsUtil {
	private static String tipRootFolder;
	private static StartupTipsUtil instance;
	private static String[] categories;

	static {
		try {
			tipRootFolder = FileLocator.toFileURL(
					FileLocator.find(StartupTipPlugin.getDefault().getBundle(),
							new Path("$nl$/"
									+ Settings.STARTUP_TIPS_ROOT_FOLDER),
							Collections.EMPTY_MAP)).getPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static StartupTipsUtil getInsatnce() {
		if (instance == null) {
			instance = new StartupTipsUtil();
		}
		return instance;
	}

	public static String getStartupTipsRootFolder() {
		return tipRootFolder;
	}

	public static String[] getCategories() {
		if (categories != null) {
			return categories;
		}
		ArrayList<String> categories = new ArrayList<String>();
		categories.add("All");
		File[] list = new File(tipRootFolder).listFiles();
		for (File file : list) {
			if (file.isDirectory()) {
				categories.add(file.getName());
			}
		}
		return categories.toArray(new String[] {});
	}
}
