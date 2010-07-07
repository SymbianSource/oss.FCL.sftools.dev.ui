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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.resource.util.FileUtils;

public class ClipboardHelper {
	public static final Clipboard APPLICATION_CLIPBOARD = Toolkit
			.getDefaultToolkit().getSystemClipboard();

	public static final int CONTENT_TYPE_SINGLE_IMAGE = 1;

	public static final int CONTENT_TYPE_MULTIPLE_IMAGES = 2;

	public static final int CONTENT_TYPE_NINE_IMAGES = 4;

	public static final int CONTENT_TYPE_IMAGE_WITH_MASK = 8;

	/**
	 * Mask is identified either by file name with _mask in it or by image
	 * instance that is grayscale or black/white.
	 */
	public static final int CONTENT_TYPE_MASK = 16;

	public static boolean clipboardContainsData(int contentType, Clipboard clip) {

		if ((contentType & CONTENT_TYPE_NINE_IMAGES) > 0
				&& clipboardContainsMultipleImagesData(clip, 9)) {
			return true;
		}

		if ((contentType & CONTENT_TYPE_MULTIPLE_IMAGES) > 0
				&& clipboardContainsMultipleImagesData(clip, null)) {
			return true;
		}

		if ((contentType & CONTENT_TYPE_IMAGE_WITH_MASK) > 0
				&& clipboardContainsImageWithMaskData(clip)) {
			return true;
		}

		if ((contentType & CONTENT_TYPE_SINGLE_IMAGE) > 0
				&& clipboardContainsImageData(clip)) {
			return true;
		}

		if ((contentType & CONTENT_TYPE_MASK) > 0) {
			return getClipboardContent(CONTENT_TYPE_MASK, clip) != null;
		}

		return false;
	}

	/**
	 * Extracts image content from clibboard, returns different objects based on
	 * clipboard content. If clipboard contains list of files, it simply returns
	 * the list. List of images is needed for pasting to 9-piece element.
	 */
	public static Object getClipboardContent(int contentType, Clipboard cl) {
		try {

			if ((contentType & CONTENT_TYPE_NINE_IMAGES) > 0) {
				if (cl.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
					return getImageFiles((List) cl
							.getData(DataFlavor.javaFileListFlavor));
				}
			}

			if ((contentType & CONTENT_TYPE_MULTIPLE_IMAGES) > 0) {
				if (cl.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
					return getImageFiles((List) cl
							.getData(DataFlavor.javaFileListFlavor));
				}
			}

			if ((contentType & CONTENT_TYPE_IMAGE_WITH_MASK) > 0) {
				if (cl.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
					return (List) cl.getData(DataFlavor.javaFileListFlavor);
				}
			}

			if ((contentType & CONTENT_TYPE_SINGLE_IMAGE) > 0
					|| (contentType & CONTENT_TYPE_IMAGE_WITH_MASK) > 0) {

				if (cl.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
					return cl.getData(DataFlavor.imageFlavor);
				} else if (isImageDataFlavor(cl)) {
					InputStream in = (InputStream) cl.getData(cl
							.getAvailableDataFlavors()[0]);
					return ImageIO.read(in);
				}

				Object result = null;
				if (cl.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
					List list = (List) cl
							.getData(DataFlavor.javaFileListFlavor);
					result = list.get(0);
				} else if (cl.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
					result = cl.getData(DataFlavor.stringFlavor);
				}

				if (PasteHelper.isParameterUsableAsImage(result))
					return result;
			}

			if ((contentType & CONTENT_TYPE_MASK) > 0) {
				Object mask = getClipboardContent(CONTENT_TYPE_SINGLE_IMAGE, cl);
				if (PasteHelper.isParameterUsableAsMask(mask))
					return mask;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Extracts image content from clibboard, returns different objects based on
	 * clipboard content. If clipboard contains list of files, it simply returns
	 * the list. List of images is needed for pasting to 9-piece element.
	 */
	@Deprecated
	public static Object getSupportedClipboardContent(Clipboard cl) {
		return getClipboardContent(CONTENT_TYPE_IMAGE_WITH_MASK
				| CONTENT_TYPE_MULTIPLE_IMAGES | CONTENT_TYPE_NINE_IMAGES
				| CONTENT_TYPE_SINGLE_IMAGE, cl);
	}

	/**
	 * true if clipboard contains image data or image path, that exists. Image
	 * file can be image readable by ImageIO or SVG image.
	 * 
	 * @param clip
	 * @return
	 */
	public static boolean clipboardContainsImageWithMaskData(Clipboard clip) {
		try {
			if (clip.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
				// check that file exists and is it image
				List files = (List) clip.getData(DataFlavor.javaFileListFlavor);
				if (files.size() == 2) {
					File image = (File) files.get(0);
					File mask = (File) files.get(1);
					return PasteHelper.isParameterUsableAsImage(image)
							&& PasteHelper.isParameterUsableAsMask(mask);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return clipboardContainsImageData(clip);
	}

	/**
	 * true if clipboard contains image data or image path, that exists. Image
	 * file can be image readable by ImageIO or SVG image.
	 * 
	 * @param clip
	 * @return
	 */
	public static boolean clipboardContainsMultipleImagesData(Clipboard clip,
			Integer nImages) {
		try {
			if (clip.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
				// check that file exists and is it image
				List files = (List) clip.getData(DataFlavor.javaFileListFlavor);
				List<File> imageFiles = new ArrayList<File>();
				for (int i = 0; i < files.size(); i++) {
					File image = (File) files.get(i);
					if (PasteHelper.isParameterUsableAsImage(image)) {
						imageFiles.add(image);
					}
				}
				if (nImages == null) {
					return imageFiles.size() > 0;
				} else {
					//return nImages.intValue() <= imageFiles.size();
					//Fixed: should not mistake 3 piece for 9 piece	
					filterMasks(imageFiles);
					return nImages.intValue() == imageFiles.size();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * ImageList in clipboard may contain both masks and images. Note that images may have mask in their name.
	 * This method aims to remove masks from the list.
	 * @param imageFiles
	 */
	private static void filterMasks(List<File> imageFiles) {
		for (int i = imageFiles.size()-1; i >= 0 ; i--) {
			String fileName = ((File)imageFiles.get(i)).getName().toLowerCase();
			
			//Reverse filename 
			int j, len = fileName.length();
		    StringBuffer dest = new StringBuffer(len);
		    for (j = (len - 1); j >= 0; j--)
		      dest.append(fileName.charAt(j));
		    String reversedFileName = dest.toString();
			
			if ((fileName.contains("layer")) && (fileName.contains("mask")) && (FileUtils.getExtension(fileName).equals("bmp"))) {
				if (reversedFileName.indexOf("reyal") > reversedFileName.indexOf("ksam"))
					imageFiles.remove(i);
			}
		}
	}

	/**
	 * true if clipboard contains image data or image path, that exists. Image
	 * file can be image readable by ImageIO or SVG image.
	 * 
	 * @param clip
	 * @return
	 */
	public static boolean clipboardContainsFolderWithImagesData(Clipboard clip) {
		try {
			if (clip.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
				// check that file exists and is it image
				List files = (List) clip.getData(DataFlavor.javaFileListFlavor);
				for (Object object : files) {
					if (isImageOrFolderWithImages((File) object)) {
						return true;
					}
				}
			} else if (clip.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
				String file = (String) clip.getData(DataFlavor.stringFlavor);
				if (FileUtils.isFileValid(file)) {
					return (new File(file).isDirectory());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * true if clipboard contains image data or image path, that exists. Image
	 * file can be image readable by ImageIO or SVG image.
	 * 
	 * @param clip
	 * @return
	 */
	public static boolean clipboardContainsImageData(Clipboard clip) {
		if (clip.isDataFlavorAvailable(DataFlavor.imageFlavor))
			return true;
		if (isImageDataFlavor(clip))
			return true;
		try {
			if (clip.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
				// check that file exists and is it image
				List files = (List) clip.getData(DataFlavor.javaFileListFlavor);
				if (files.size() > 0) {
					for (Object image : files) {
						if (!PasteHelper.isParameterUsableAsImage(image)){
							return false;
							}
					}
					return true;
				}
			}
			if (clip.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
				String path = (String) clip.getData(DataFlavor.stringFlavor);
				return PasteHelper.isParameterUsableAsImage(path);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * test if data flavor has mime type x-java-image
	 */
	public static boolean isImageDataFlavor(Clipboard clip) {
		DataFlavor fl[] = clip.getAvailableDataFlavors();
		if (fl.length > 0) {
			DataFlavor f = fl[0];
			if ("image".equals(f.getPrimaryType())) {
				if (InputStream.class.getName().equals(
						f.getDefaultRepresentationClassAsString()))
					return true;
			}
		}
		return false;
	}

	public static boolean stringRepresentsFile(String path) {
		if (path != null && path.length() < 512)
			return new File(path).exists();
		return false;
	}

	public static File getFileFromString(String path) {
		if (path != null && path.length() < 512)
			return new File(path);
		return null;
	}

	/**
	 * Returns filename if clipboard contains it and the file exists, otherwise
	 * returns null
	 * 
	 * @param clip
	 */
	public static String getFilenameFromClipboard(Clipboard clip) {

		if (clip.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
			// check that file exists and is it image
			try {
				List files = (List) clip.getData(DataFlavor.javaFileListFlavor);
				if (files.size() == 1) {
					File file = (File) files.get(0);
					if (file.exists())
						return file.getAbsolutePath();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (clip.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
			try {
				String path = (String) clip.getData(DataFlavor.stringFlavor);
				if (path.length() < 512) {
					File file = new File(path);
					if (file.exists()) {
						return file.getAbsolutePath();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static boolean copyImageToClipboard(Clipboard clip, Object data) {

		if (clip == null)
			clip = Toolkit.getDefaultToolkit().getSystemClipboard();

		if (data instanceof String)
			data = new File((String) data);
		if (data instanceof File) {
			File image = (File) data;
			if (image.exists()) {
				List<File> files = new ArrayList<File>();
				files.add(image);
				FileTransferable trans = new FileTransferable(files);
				clip.setContents(trans, trans);
				return true;
			}
		} else if (data instanceof RenderedImage) {
			// system clipboard don't like planar images
			data = CoreImage.create((RenderedImage) data).getBufferedImage();
			GenericTransferable tr = new GenericTransferable(data,
					DataFlavor.imageFlavor);
			clip.setContents(tr, tr);
			return true;
		} else if (data instanceof ArrayList) {
			// array of files = image transferable?
			ArrayList _data = (ArrayList) data;
			if (_data.size() > 0) {
				for (Object item : _data)
					if (!(item instanceof File))
						return false;
				Transferable trans = new ImageTransferable(_data);
				clip.setContents(trans, null);
				return true;
			}
		}
		return false;
	}

	private static boolean isImageOrFolderWithImages(File file) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				boolean toRet = isImageOrFolderWithImages(child);
				if (toRet) {
					return true;
				}
			}
		} else {
			return PasteHelper.isParameterUsableAsImage(file);
		}
		return false;
	}

	private static List getImageFiles(List files) {
		List imageFiles = new ArrayList();
		for (Object object : files) {
			if (PasteHelper.isParameterUsableAsImage(object)) {
				imageFiles.add(object);
			}
		}
		return imageFiles;
	}
}
