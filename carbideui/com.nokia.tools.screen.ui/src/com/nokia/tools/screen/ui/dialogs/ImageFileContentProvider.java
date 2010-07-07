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

import javax.imageio.ImageIO;

import org.eclipse.swt.graphics.Image;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.image.RenderedImageDescriptor;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.screen.ui.UiPlugin;

public class ImageFileContentProvider extends IFileContentProvider.Adapter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IFileContentProvider#getImage(java.io.File,
	 *      int, int)
	 */
	public Image getImage(File file, int width, int height,
			boolean keepAspectRatio) {
		try {
			CoreImage image = CoreImage.create().load(file, width, height);
			if (image != null) {
				String name = file.getName();
				if (!name.toLowerCase()
						.endsWith(IFileConstants.FILE_EXT_DOTSVG)) {
					if (keepAspectRatio) {
						image.stretch(width, height,
								CoreImage.SCALE_TO_FIT);
					} else {
						image.scale(width, height);
					}
				}
				return new RenderedImageDescriptor(image.getAwt())
						.createImage();
			}
		} catch (Exception e) {
			UiPlugin.error(e);
		}
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
		if (name.toLowerCase().endsWith(IFileConstants.FILE_EXT_SVG)) {
			return true;
		}
		if (FileUtils.getExtension(name) == null) {
			return false;
		}
		return ImageIO.getImageReadersBySuffix(FileUtils.getExtension(name))
				.hasNext();
	}
}
