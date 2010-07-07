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

package com.nokia.tools.packaging.optimization;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.packaging.PackagingContext;

public interface ThemePackageAccessor {

	void setWorkdir(String workingDir);
	
	List<File> getPackageContentFileList();

	void finish();

	void notifyReplaceFiles(Map<File, File> map);

	void notifyObsoleteFiles(List<File> obsoleteFiles);

	void setContext(PackagingContext context);
	
	public abstract class Stub implements ThemePackageAccessor {
		
		protected PackagingContext context;
		protected String workDir;
		protected IContent themeContent;
		
		public void setContext(PackagingContext context) {
			this.context = context;
		}		
		public void setWorkdir(String workingDir) {
			this.workDir = workingDir;
		}		
		public void setThemeContent(IContent theme) {
			this.themeContent = theme;
		}
	}

	void setThemeContent(IContent theme);

	void start();

}
