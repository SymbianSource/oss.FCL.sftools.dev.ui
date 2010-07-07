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
package com.nokia.tools.media.svg;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.image.IImageLoader;
import com.nokia.tools.media.image.LoaderContext;

public class SVGLoader implements IImageLoader {
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.IImageLoader#load(com.nokia.tools.media.image.CoreImage,
	 *      com.nokia.tools.media.image.LoaderContext)
	 */
	public void load(CoreImage image, LoaderContext context) throws Exception {
		int width = context.getWidth();
		int height = context.getHeight();

		if (width == 0 || height == 0) {
			return;
		}

		int scaleMode = context.getScaleMode();
		boolean withAspectRatio = scaleMode == CoreImage.SCALE_TO_FIT
				|| scaleMode == CoreImage.SCALE_DOWN_TO_FIT
				|| scaleMode == CoreImage.SCALE_TO_BEST;

		SvgImage svg = new SvgImage(context.getUrl());
		if (withAspectRatio && context.isAdjustSize()) {
			width = height = Math.min(width, height);
		}
		svg.setSize(width, height);
		// otherwise use the default
		svg.setScalingMode(SvgImage.STRETCH);
		if (withAspectRatio) {
			svg.setScalingMode(SvgImage.ASPECT_RATIO);
		}
		image.setAwt(svg.getAsBufferedImage());
	}

}
