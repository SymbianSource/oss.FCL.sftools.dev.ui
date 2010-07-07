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
package com.nokia.tools.media.player.impl;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.RenderedImage;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.player.IPaintAdapter;

public class StillImagePlayer extends URLFramePlayer {
	private RenderedImage image;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.impl.URLFramePlayer#doLoad()
	 */
	@Override
	protected boolean doLoad() throws Exception {
		Dimension size = getSize();
		if (size == null) {
			return false;
		}
		image = CoreImage.create().load(getUrl(), size.width, size.height,
				CoreImage.SCALE_DOWN_TO_FIT).getAwt();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.impl.URLFramePlayer#doPaint(com.nokia.tools.media.utils.IPaintAdapter,
	 *      java.awt.Graphics)
	 */
	@Override
	protected void doPaint(IPaintAdapter original, Graphics g) throws Exception {
		((Graphics2D) g).drawRenderedImage(image, CoreImage.TRANSFORM_ORIGIN);
	}
}
