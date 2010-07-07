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
package com.nokia.tools.media.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.RenderedImage;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

import com.nokia.tools.media.core.Activator;

public class RenderedImageDescriptor extends ImageDescriptor {
	public static final int NO_TRANSPARENT = -1;

	private ImageData imageData;
	private IRenderedImageProvider provider;
	private int mode;
	private RenderedImage image;

	public RenderedImageDescriptor(RenderedImage image) {
		this(image, image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_INT_ARGB, NO_TRANSPARENT);
	}

	public RenderedImageDescriptor(IRenderedImageProvider provider) {
		this(null, provider.getWidth(), provider.getHeight(),
				BufferedImage.TYPE_INT_ARGB, NO_TRANSPARENT);
		this.provider = provider;
	}

	public RenderedImageDescriptor(RenderedImage image, int width, int height,
			int mode, int transparentPixel) {
		this.image = image;
		this.mode = mode;

		imageData = new ImageData(width, height, 32, new PaletteData(0xFF0000,
				0x00FF00, 0x0000FF));
		if (mode != BufferedImage.TYPE_INT_ARGB
				&& transparentPixel != NO_TRANSPARENT)
			imageData.transparentPixel = transparentPixel;
	}

	/**
	 * @param mode - parameter passed for constructing BufferedImage, default
	 *            BufferedImage.TYPE_INT_ARGB
	 */
	private void populateImageData() {
		if (image == null && provider != null) {
			image = provider.getRenderedImage();
			if (image == null) {
				mode = BufferedImage.TYPE_INT_RGB;
				imageData.transparentPixel = 0;
			}
			/* provider can be cached and we not want to keep ref to provider... */
			provider = null;
		}
		if (image == null) {
			return;
		}

		int width = imageData.width;
		int height = imageData.height;

		BufferedImage bufferedImage = new BufferedImage(width, height, mode);
		Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		if (width != image.getWidth() || height != image.getHeight()) {
			g2d.drawRenderedImage(image, AffineTransform.getScaleInstance(width
					/ (double) image.getWidth(), height
					/ (double) image.getHeight()));
		} else {
			g2d.drawRenderedImage(image, AffineTransform.getTranslateInstance(
					0, 0));
		}
		g2d.dispose();

		int[] data = ((DataBufferInt) bufferedImage.getData().getDataBuffer())
				.getData();
		imageData.setPixels(0, 0, data.length, data, 0);

		if (mode == BufferedImage.TYPE_INT_ARGB)
			try {
				byte maskData[] = new byte[data.length];
				int sum = 0;
				byte mask = 0;
				for (int i = 0; i < maskData.length; i++) {
					mask = (byte) ((data[i] >> 24) & 0xff);
					maskData[i] = mask;
					sum += mask;
				}
				
				if (sum == 0)
					maskData[0] = 1;
				imageData.setAlphas(0, 0, maskData.length, maskData, 0);
			} catch (Exception e) {
				Activator.error(e);
			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.resource.ImageDescriptor#createImage(boolean,
	 *      org.eclipse.swt.graphics.Device)
	 */
	@Override
	public Image createImage(boolean returnMissingImageOnError, Device device) {
		populateImageData();
		return super.createImage(returnMissingImageOnError, device);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.resource.ImageDescriptor#getImageData()
	 */
	@Override
	public ImageData getImageData() {
		return imageData;
	}
}
