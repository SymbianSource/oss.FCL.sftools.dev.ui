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
package com.nokia.tools.media.utils.clipboard;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.resource.util.FileUtils;

/**
 * Helper class for pasting from unamed object. Usage: override pastexxx
 * methods. On paste call paste(Object), class will parse input and call methods
 * to be pastewxxx methods
 * 
 */
public abstract class PasteHelper {

	public final void paste(Object data) throws Exception {
		if (data instanceof String || data instanceof File) {
			File toPaste = extractFile(data);
			if (null == toPaste || !toPaste.exists())
				throw new IOException("Copy from file:"
						+ (toPaste == null ? null : toPaste.getAbsolutePath())
						+ " doesn't exist.");
			paste(toPaste);
		} else if (data instanceof RenderedImage) {
			paste(extractImage(data));
		} else if (data instanceof List) {

			List dataList = (List) data;

			/* solve case when mask is first and image second */
			if (dataList.size() == 2) {
				File first = extractFile(dataList.get(0));
				File second = extractFile(dataList.get(1));
				if (isParameterUsableAsImage(first)
						&& isParameterUsableAsImage(second)) {
					boolean firstMask = isParameterUsableAsMask(first);
					boolean secondMask = isParameterUsableAsMask(second);
					if (firstMask && !secondMask) {
						Object tmp = second;
						second = first;
						first = (File) tmp;
						secondMask = true;
					}
					paste(first);
					if (secondMask)
						pasteMask(second);
					return;
				}
			}

			if (dataList.size() > 0) {
				paste(dataList.get(0));
			}
			if (dataList.size() > 1) {
				Object second = extractFile(dataList.get(1));
				if (isParameterUsableAsMask(second)) {
					pasteMask((File) second);
				} else {
					second = extractImage(dataList.get(1));
					if (isParameterUsableAsMask(second)) {
						pasteMask((RenderedImage) second);
					}
				}
			}
		} else
			throw new Exception("Unsupported obj pasted:" + data);
	}

	protected final void paste(String file) throws Exception {
		paste(new File(file));
	}

	/**
	 * @param file
	 * @throws Exception
	 */
	protected abstract void paste(File file) throws Exception;

	/**
	 * @param image
	 * @throws Exception
	 */
	protected abstract void paste(RenderedImage image) throws Exception;

	protected void pasteMask(File maskFile) throws Exception {
	}

	
	protected void pasteMask(RenderedImage image) throws Exception {
	}

	/**
	 * Private helper for extracting file information from Object
	 * 
	 * @param data
	 * @return
	 */
	public static File extractFile(final Object source) {
		Object data = source;
		if (data instanceof List) {
			List dataList = (List) data;
			for (Object elem : dataList) {
				File extracted = extractFile(elem);
				if (null != extracted) {
					return extracted;
				}
			}
		}
		if (data instanceof Object[]) {
			Object[] dataList = (Object[]) data;
			for (Object elem : dataList) {
				File extracted = extractFile(elem);
				if (null != extracted) {
					return extracted;
				}
			}
		}
		if (data instanceof String) {
			String p = (String) data;
			if (p.length() <= 512) {
				if (new File((String) data).exists())
					return new File((String) data);
			}
		} else if (data instanceof File && ((File) data).exists()) {
			return (File) data;
		}
		return null;
	}

	/**
	 * helper methodfor geting RenderedImage from unamed object, used localy
	 * 
	 * @param data
	 * @return
	 */
	private RenderedImage extractImage(Object data) {
		if (data instanceof RenderedImage) {
			return (RenderedImage) data;
		}
		return null;
	}

	/**
	 * true if image is PlanarImage or file/string path that can be image
	 * readable by ImageIO or SVG image.
	 * 
	 * @param clip
	 * @return
	 */
	public static boolean isParameterUsableAsImage(Object image) {
		if (image instanceof RenderedImage)
			return true;
		image = PasteHelper.extractFile(image);
		if (null != image) {
			String path = ((File) image).getAbsolutePath();
			if (path.length() < 512)
				if (new File(path).exists()) {
					String ext = FileUtils.getExtension(path);
					// find out if image can be loaded
					if (IFileConstants.FILE_EXT_SVG.equalsIgnoreCase(ext)
							|| IFileConstants.FILE_EXT_SVG_TINY
									.equalsIgnoreCase(ext))
						return true;
					return ext == null ? false : ImageIO
							.getImageReadersByFormatName(ext.toLowerCase())
							.hasNext();
				}
		}
		return false;
	}

	/**
	 * mask is identified by '_mask' in filename or being grayscale image.
	 * 
	 * @param image
	 * @return
	 * @throws Exception
	 */
	public static boolean isParameterUsableAsMask(Object image)
			throws Exception {
		if (isParameterUsableAsImage(image)) {
			String imgPath = null;
			if (image instanceof File) {
				image = ((File) image).getAbsolutePath();
			}
			if (image instanceof String) {
				imgPath = (String) image;
				if (imgPath.indexOf("_mask") != -1) {
					return true;
				}
			}
			CoreImage maskInstance = CoreImage.create();
			if (image instanceof RenderedImage)
				maskInstance.init((RenderedImage) image);
			else if (imgPath != null) {
				maskInstance.load(new File(imgPath));
			}

			if (maskInstance.getAwt() != null)
				return maskInstance.isImageGrayScaleImage();
		}
		return false;
	}
}
