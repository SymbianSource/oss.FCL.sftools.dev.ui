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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.RenderedImage;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.player.IPaintAdapter;
import com.nokia.tools.resource.util.FileUtils;

public class GIFPlayer extends URLFramePlayer {
	private ImageLoader loader;
	private ImageData[] frames;
	private Image swtImage;
	private GC gc;
	private int loop;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.impl.URLFramePlayer#doLoad()
	 */
	@Override
	protected boolean doLoad() throws Exception {
		loop = 0;
		frames = null;

		loader = new ImageLoader();
		InputStream in = null;
		try {
			in = getUrl().openStream();
			frames = loader.load(in);
		} finally {
			FileUtils.close(in);
		}
		return true;
	}

	int getDelay(ImageData data) {
		int delay = data.delayTime * 10;
		if (delay < 20)
			delay += 30;
		if (delay < 30)
			delay += 10;
		return delay;
	}

	private RenderedImage getImage(long time) {
		if (frames == null || frames.length == 0) {
			return null;
		}

		long loopDuration = 0;
		for (ImageData frame : frames) {
			loopDuration += getDelay(frame);
		}

		if (time > loopDuration) {
			if (++loop > loader.repeatCount) {
				stop();
				time = 0;
				loop = 0;
			}
		}
		if (swtImage == null || swtImage.isDisposed()) {
			swtImage = new Image(Display.getDefault(),
					loader.logicalScreenWidth, loader.logicalScreenHeight);
			gc = new GC(swtImage);
		}

		long passed = 0;
		for (int i = 0; i < frames.length; i++) {
			ImageData currFrame = frames[i];
			ImageData prevFrame = i == 0 ? frames[i] : frames[i - 1];
			passed += getDelay(currFrame);
			if (passed >= time) {
				switch (prevFrame.disposalMethod) {
				case SWT.DM_FILL_PREVIOUS:
					Image image = new Image(Display.getDefault(), frames[i - 1]);
					gc.drawImage(image, 0, 0, prevFrame.width,
							prevFrame.height, prevFrame.x, prevFrame.y,
							prevFrame.width, prevFrame.height);
					image.dispose();
					break;
				case SWT.DM_FILL_BACKGROUND:
					gc.fillRectangle(prevFrame.x, prevFrame.y, prevFrame.width,
							prevFrame.height);
					break;
				}

				Image image = new Image(Display.getDefault(), currFrame);
				gc.drawImage(image, 0, 0, currFrame.width, currFrame.height,
						currFrame.x, currFrame.y, currFrame.width,
						currFrame.height);
				image.dispose();

				return CoreImage.create().init(swtImage)
						.convertToTransparent(-1).stretch(getSize().width,
								getSize().height, CoreImage.SCALE_DOWN_TO_FIT)
						.getAwt();
			}
		}

	
		return CoreImage.create().init(swtImage).convertToTransparent(-1)
				.stretch(getSize().width, getSize().height,
						CoreImage.SCALE_DOWN_TO_FIT).getAwt();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.impl.URLFramePlayer#doPaint(com.nokia.tools.media.utils.IPaintAdapter,
	 *      java.awt.Graphics)
	 */
	@Override
	protected void doPaint(IPaintAdapter original, Graphics g) throws Exception {
		RenderedImage image = getImage(capture());
		if (image != null) {
			((Graphics2D) g).drawRenderedImage(image,
					CoreImage.TRANSFORM_ORIGIN);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.impl.URLFramePlayer#isContentPlayable()
	 */
	@Override
	public boolean isContentPlayable() {
		return super.isContentPlayable() && frames != null && frames.length > 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.IPlayer.Stub#doStop()
	 */
	@Override
	protected void doStop() {
		if (swtImage != null) {
			swtImage.dispose();
		}
		if (gc != null) {
			gc.dispose();
		}
	}
}
