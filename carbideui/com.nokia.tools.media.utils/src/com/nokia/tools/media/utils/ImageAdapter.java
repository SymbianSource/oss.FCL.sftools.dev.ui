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

package com.nokia.tools.media.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;

public class ImageAdapter extends BufferedImage {

	public ImageAdapter(int inWidth, int inHeight, int inType) {
		super(inWidth, inHeight, inType);
	}

	public ImageAdapter(int inWidth, int inHeight) {
		super(inWidth, inHeight, BufferedImage.TYPE_3BYTE_BGR);
	}

	public ImageData toImageData() {
		int vnWidth = getWidth();
		int vnHeight = getHeight();
		int vnDepth = 24;
		PaletteData vpPalette = new PaletteData(0xff, 0xff00, 0xff0000);
		int vnScanlinePad = vnWidth * 3;
		WritableRaster vpRaster = getRaster();
		DataBufferByte vpBuffer = (DataBufferByte) vpRaster.getDataBuffer();
		byte[] vabData = vpBuffer.getData();
		ImageData vpImageData = new ImageData(vnWidth, vnHeight, vnDepth,
				vpPalette, vnScanlinePad, vabData);
		return vpImageData;
	}

	public Image toSwtImage() {
		ImageData vpImageData = toImageData();
		Image vpImage = new Image(Display.getDefault(), vpImageData);
		return vpImage;
	}
}
