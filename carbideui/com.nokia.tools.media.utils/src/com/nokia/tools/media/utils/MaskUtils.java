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
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Vector;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.image.RenderedImageDescriptor;
import com.nokia.tools.resource.util.FileUtils;

/**
 * Mask by color and wand algorithms
 * 
 */
public class MaskUtils {

	private static int maskPixelBlackValue = 0;

	private static void fillImageDataWithWhite(ImageData maskImageData) {
		for (int i = 0; i < maskImageData.width; i++) {
			for (int j = 0; j < maskImageData.height; j++) {
				maskImageData.setPixel(i, j, 255);
			}
		}
	}

	/**
	 * Algorithm to create the paint bucket Algorithm Paint-Bucket(row, col,
	 * old_color, new_color) if (row,col) is on the grid and the color of
	 * (row,col) is old_color, then: Change the color of (row,col) to new_color.
	 * Paint-Bucket(row,col-1,old_color,new_color)
	 * Paint-Bucket(row,col+1,old_color,new_color)
	 * Paint-Bucket(row-1,col,old_color,new_color)
	 * Paint-Bucket(row+1,col,old_color,new_color) end of if
	 */
	public static void paintBucket(Point currPoint, int pickedColour,
			int imageWidth, int imageHeight, ImageData copyImageData,
			ImageData maskImageData) {
		Vector<Point> paintBucketVector = new Vector<Point>();
		boolean[][] doubleSizeArray = new boolean[imageWidth][imageHeight];

		paintBucketVector.add(currPoint);
		doubleSizeArray[currPoint.x][currPoint.y] = true;
		Rectangle imageBounds = new Rectangle(0, 0, imageWidth, imageHeight);
		for (;;) {
			if (paintBucketVector.size() == 0)
				break;

			Point point = (Point) paintBucketVector.get(0);
			int curPosColour = copyImageData.getPixel(point.x, point.y);// getRGB(point.x,point.y);
			if (curPosColour == pickedColour) {
				if (point.x > maskImageData.width
						|| point.y > maskImageData.height) {
					paintBucketVector.remove(0);
					continue;
				}
				maskImageData.setPixel(point.x, point.y, maskPixelBlackValue);

				paintBucketVector.remove(0);
				Point pointLeft = new Point(point.x - 1, point.y);
				Point pointRight = new Point(point.x + 1, point.y);
				Point pointUp = new Point(point.x, point.y - 1);
				Point pointDown = new Point(point.x, point.y + 1);

				if (imageBounds.contains(pointLeft)
						&& !doubleSizeArray[pointLeft.x][pointLeft.y]) {
					paintBucketVector.add(pointLeft);
					doubleSizeArray[pointLeft.x][pointLeft.y] = true;
				}
				if (imageBounds.contains(pointRight)
						&& !doubleSizeArray[pointRight.x][pointRight.y]) {
					paintBucketVector.add(pointRight);
					doubleSizeArray[pointRight.x][pointRight.y] = true;
				}
				if (imageBounds.contains(pointUp)
						&& !doubleSizeArray[pointUp.x][pointUp.y]) {
					paintBucketVector.add(pointUp);
					doubleSizeArray[pointUp.x][pointUp.y] = true;
				}
				if (imageBounds.contains(pointDown)
						&& !doubleSizeArray[pointDown.x][pointDown.y]) {
					paintBucketVector.add(pointDown);
					doubleSizeArray[pointDown.x][pointDown.y] = true;
				}
			} else {
				paintBucketVector.remove(0);
			}

		}
		paintBucketVector.clear();

	}

	private static PaletteData createGrayscalePaletteData() {
		RGB[] eightBitGreyscale = new RGB[256];
		for (int i = 0; i < 256; i++) {
			eightBitGreyscale[i] = new RGB(i, i, i);
		}
		return new PaletteData(eightBitGreyscale);
	}

	public static ImageData getMaskBasedOnPixelSWT(int x, int y,
			RenderedImage imageAwt) {
		if (imageAwt != null) {

			RenderedImageDescriptor desc = new RenderedImageDescriptor(imageAwt);

			Image image = desc.createImage();
			ImageData data = image.getImageData();

			PaletteData palette = createGrayscalePaletteData();

			ImageData maskImageData = new ImageData(data.width, data.height, 8,
					palette);
			fillImageDataWithWhite(maskImageData);

			int width = data.width;
			int height = data.height;
			if ((x < width) && (y < height)) {
				int value = data.getPixel(x, y);
				paintBucket(new Point(x, y), value, width, height, data,
						maskImageData);

			}
			image.dispose();
			return maskImageData;
		}
		return null;
	}

	public static ImageData getMaskBasedOnPixelSWT(int x, int y,
			String imageFilePath) throws Exception {
		if (FileUtils.isFileValidAndAccessible(imageFilePath)) {
			return getMaskBasedOnPixelSWT(x, y, CoreImage.create().load(
					new File(imageFilePath)).getAwt());
		}
		return null;
	}

	public static BufferedImage getMaskBasedOnPixelAWT(int x, int y,
			String imageFilePath) throws Exception {
		ImageData data = getMaskBasedOnPixelSWT(x, y, imageFilePath);
		return imageDataToBufferedImage(data);
	}

	public static BufferedImage getMaskBasedOnPixelAWT(int x, int y,
			BufferedImage image) {
		ImageData data = getMaskBasedOnPixelSWT(x, y, image);
		return imageDataToBufferedImage(data);
	}

	// This works only for imageData with indexed palette
	public static BufferedImage imageDataToBufferedImage(ImageData data) {
		if (data == null) {
			return null;
		}
		ColorModel colorModel = null;
		PaletteData palette = data.palette;

		RGB[] colors = palette.getRGBs();
		byte redElements[] = new byte[colors.length];
		byte greenElements[] = new byte[colors.length];
		byte blueElements[] = new byte[colors.length];

		for (int i = 0; i < colors.length; i++) {
			RGB color = colors[i];
			redElements[i] = (byte) color.red;
			greenElements[i] = (byte) color.green;
			blueElements[i] = (byte) color.blue;
		}

		colorModel = new IndexColorModel(data.depth, colors.length,
				(byte[]) redElements, (byte[]) greenElements,
				(byte[]) blueElements);

		BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel
				.createCompatibleWritableRaster(data.width, data.height),
				false, null);
		WritableRaster raster = bufferedImage.getRaster();
		int[] pixels = new int[1];
		for (int x = 0; x < data.width; x++) {
			for (int y = 0; y < data.height; y++) {
				int pixel = data.getPixel(x, y);
				pixels[0] = pixel;
				raster.setPixel(x, y, pixels);
			}
		}
		return bufferedImage;

	}

}
