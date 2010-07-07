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
package com.nokia.tools.ui.dialog;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.swt.graphics.Image;

public interface IFileContentProvider {
	Image getImage(File file, int width, int height, boolean keepAspectRatio);

	String getName(File file);

	File[] getFiles(File dir);

	abstract class Adapter implements IFileContentProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.dialogs.IFileContentProvider#getName(java.io.File)
		 */
		public String getName(File file) {
			return file.getName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.dialogs.IFileContentProvider#getFiles(java.io.File)
		 */
		public File[] getFiles(File dir) {
			File[] files = dir.listFiles(new FileFilter() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.io.FileFilter#accept(java.io.File)
				 */
				public boolean accept(File pathname) {
					return acceptFile(pathname);
				}

			});
			if (files == null) {
				return new File[0];
			}
			return files;
		}

		protected abstract boolean acceptFile(File file);
	}
}
