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

package com.nokia.tools.screen.ui.dialogs;

import java.io.File;

import org.eclipse.swt.graphics.Image;

import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.resource.util.FileUtils;

public class ThemeFileContentProvider
    extends IFileContentProvider.Adapter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IFileContentProvider#getImage(java.io.File,
	 *      int, int)
	 */
	public Image getImage(File file, int width, int height,
	    boolean keepAspectRatio) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IFileContentProvider.Adapter#acceptFile(java.io.File)
	 */
	@Override
	protected boolean acceptFile(File file) {
		if (!file.isFile()) {
			return false;
		}
		String name = file.getName();
		if (name.toLowerCase().endsWith(IFileConstants.FILE_EXT_TDF)) {
			return true;
		}
		if (FileUtils.getExtension(name) == null) {
			return false;
		}
		return false;
	}
}
