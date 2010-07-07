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
package com.nokia.tools.media.symbian.mbm;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.image.IImageLoader;
import com.nokia.tools.media.image.LoaderContext;

/**
 */
public class MBMLoader implements IImageLoader {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.IImageLoader#load(com.nokia.tools.media.image.CoreImage,
	 *      com.nokia.tools.media.image.LoaderContext)
	 */
	public void load(CoreImage image, LoaderContext context) throws Exception {
		BitmapConverter converter = null;
		try {
			InputStream in = context.getUrl().openStream();
			converter = new BitmapConverter(in);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			converter.extract(context.getIndex(), out);
			RenderedImage awt = ImageIO.read(new ByteArrayInputStream(out
					.toByteArray()));
			image.setAwt(awt);
			image.stretch(context);
		} finally {
			if (converter != null) {
				converter.close();
			}
		}
	}

}
