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
package com.nokia.tools.media.utils.layers;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.eclipse.core.runtime.IAdaptable;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.clipboard.FileTransferable;
import com.nokia.tools.media.utils.clipboard.PasteHelper;
import com.nokia.tools.resource.util.FileUtils;

public abstract class ImageHolder implements IImageHolder, Cloneable,
		IAdaptable {

	public static final String FILE_TYPE_BMP = IFileConstants.FILE_EXT_DOTBMP;

	public static final String FILE_TYPE_SVG = IFileConstants.FILE_EXT_DOTSVG;

	protected RenderedImage pi;

	protected boolean replaceFiles = true;

	public ImageHolder() {
	}

	abstract public File getImageFile();

	abstract public void setImageFile(File imageFile);

	abstract public File getMaskFile();

	abstract public void setMaskFile(File maskFile);

	abstract public File getWorkDir();

	abstract public int getWidth();

	abstract public int getHeight();

	abstract public boolean supportsMask();

	abstract public boolean supportsBitmap();

	abstract public boolean supportsSvg();

	abstract public String getIdentifier();

	public RenderedImage getImage() {
		if (pi == null) {
			try {
				CoreImage image = CoreImage.create().load(getImageFile(),
						getWidth(), getHeight(), CoreImage.SCALE_TO_FIT);
				if (supportsMask() && getMaskFile() != null) {
					CoreImage mask = CoreImage.create().load(getMaskFile(),
							getWidth(), getHeight(), CoreImage.SCALE_TO_FIT);
					image.applyMask(mask, true, false);
				}
				pi = image.getAwt();
			} catch (Exception e) {
				UtilsPlugin.error(e);
			}
		}

		return pi;
	}

	public RenderedImage getRAWImage(boolean applyMask) {
		CoreImage rawImage = CoreImage.create();
		try {
			rawImage.load(getImageFile(), getWidth(), getHeight(),
					CoreImage.KEEP_ORIGINAL);
			if (supportsMask() && getMaskFile() != null && applyMask) {
				CoreImage mask = CoreImage.create().load(getMaskFile(),
						getWidth(), getHeight(), CoreImage.KEEP_ORIGINAL);
				rawImage.applyMask(mask, true, false);
			}
		} catch (Exception e) {
			UtilsPlugin.error(e);
		}
		return rawImage.getAwt();
	}

	public RenderedImage getRAWImage(int width, int height, boolean applyMask) {
		CoreImage rawImage = CoreImage.create();
		try {
			rawImage.load(getImageFile(), width, height, CoreImage.STRETCH);
			if (supportsMask() && getMaskFile() != null && applyMask) {
				CoreImage mask = CoreImage.create().load(getMaskFile(), width,
						height, CoreImage.STRETCH);
				rawImage.applyMask(mask, true, false);
			}
		} catch (Exception e) {
			UtilsPlugin.error(e);
		}
		return rawImage.getAwt();
	}

	public boolean hasMask() {
		return getMaskFile() != null && getMaskFile().exists();
	}

	public RenderedImage getMask() {
		if (supportsMask() && getMaskFile() != null) {
			try {
				return CoreImage.create().load(getMaskFile(), getWidth(),
						getHeight(), CoreImage.KEEP_ORIGINAL).getAwt();
			} catch (Exception e) {
				UtilsPlugin.error(e);
			}
		}
		return null;
	}

	public void clearMask() {
		setMaskFile(null);
		pi = null;
	}

	public boolean isBitmap() {
		return getImageFile() != null
				&& getImageFile().getName().toLowerCase().endsWith(
						FILE_TYPE_BMP);
	}

	public boolean isSvg() {
		return getImageFile() != null
				&& getImageFile().getName().toLowerCase().endsWith(
						FILE_TYPE_SVG);
	}

	private void pasteImage(RenderedImage img) {
		String newFileName = (replaceFiles && getImageFile() != null && getImageFile()
				.getName().toLowerCase().endsWith(FILE_TYPE_BMP)) ? getImageFile()
				.getAbsolutePath()
				: getNewFileName(FILE_TYPE_BMP);

		saveImageAsBmpAndMask(img, newFileName);

		pi = null;

		// propsup.firePropertyChange(PROPERTY_STATE, null, null);
	}

	private void pasteImage(String absolutePath) {
		try {
			if (absolutePath.toLowerCase().endsWith(FILE_TYPE_SVG)) {

				String newFileName = (replaceFiles && getImageFile() != null && getImageFile()
						.getName().toLowerCase().endsWith(FILE_TYPE_SVG)) ? getImageFile()
						.getAbsolutePath()
						: getNewFileName(FILE_TYPE_SVG);

				FileUtils.copyFile(absolutePath, newFileName);

				setImageFile(new File(newFileName));
				setMaskFile(null);

				pi = null;

				// propsup.firePropertyChange(PROPERTY_STATE, null, null);
			} else {
				RenderedImage img = ImageIO.read(new File(absolutePath));

				String newFileName = (replaceFiles && getImageFile() != null && getImageFile()
						.getName().toLowerCase().endsWith(FILE_TYPE_BMP)) ? getImageFile()
						.getAbsolutePath()
						: getNewFileName(FILE_TYPE_BMP);

				saveImageAsBmpAndMask(img, newFileName);

				pi = null;

				// propsup.firePropertyChange(PROPERTY_STATE, null, null);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void pasteMask(Object data) throws Exception {
		new PasteHelper() {
			@Override
			protected void paste(File file) throws Exception {
				ImageHolder.this.pasteMask(file.getAbsolutePath());
			}

			@Override
			protected void paste(RenderedImage image) throws Exception {
				ImageHolder.this.pasteMask(image);
			}
		}.paste(data);
	}

	private void pasteMask(RenderedImage img) {
		String fileName = getImageFile().getName().toString();

		String maskNewFileName = (replaceFiles && getMaskFile() != null && getMaskFile()
				.getName().toLowerCase().endsWith(FILE_TYPE_BMP)) ? getMaskFile()
				.getAbsolutePath()
				: getNewFileName(fileName.substring(0, fileName
						.lastIndexOf("."))
						+ "_mask", FILE_TYPE_BMP);

		saveImageAsMask(CoreImage.create(img), maskNewFileName);

		pi = null;

		// propsup.firePropertyChange(PROPERTY_STATE, null, null);
	}

	private void pasteMask(String absolutePath) {
		try {
			if (absolutePath.toLowerCase().endsWith(
					IFileConstants.FILE_EXT_DOTSVG)) {

				// Map dimensions = ((S60Theme) getEntity().getRoot())
				// .getDimensions();
				// String identifier = getEntity().getIdentifier();
				//
				// absolutePath = SVGTUtil.convertToSVGT(identifier,
				// dimensions, absolutePath);

				String fileName = getImageFile().getName().toString();

				String maskNewFileName = (replaceFiles && getMaskFile() != null && getMaskFile()
						.getName().toLowerCase().endsWith(FILE_TYPE_BMP)) ? getMaskFile()
						.getAbsolutePath()
						: getNewFileName(fileName.substring(0, fileName
								.lastIndexOf("."))
								+ "_mask", FILE_TYPE_BMP);

				CoreImage img = CoreImage.create().load(new File(absolutePath),
						getWidth(), getHeight());

				saveImageAsMask(img, maskNewFileName);

				pi = null;

				// propsup.firePropertyChange(PROPERTY_STATE, null, null);
			} else {
				CoreImage img = CoreImage.create(ImageIO.read(new File(
						absolutePath)));

				String fileName = getImageFile().getName().toString();

				String maskNewFileName = (replaceFiles && getMaskFile() != null && getMaskFile()
						.getName().toLowerCase().endsWith(FILE_TYPE_BMP)) ? getMaskFile()
						.getAbsolutePath()
						: getNewFileName(fileName.substring(0, fileName
								.lastIndexOf("."))
								+ "_mask", FILE_TYPE_BMP);

				saveImageAsMask(img, maskNewFileName);

				pi = null;

				// propsup.firePropertyChange(PROPERTY_STATE, null, null);
			}
		} catch (Exception e) {
			UtilsPlugin.error(e);
		}
	}

	public void copyImageToClipboard(Clipboard clip) {
		try {
			if (clip == null) {
				handleCopyImage(Toolkit.getDefaultToolkit()
						.getSystemClipboard());
			} else {
				handleCopyImage(clip);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void saveImageAsBmpAndMask(RenderedImage image, String newFileName) {
		try {
			CoreImage main = CoreImage.create(image);
			CoreImage mask = main.copy().extractMask(true);

			if (mask != null) {

				String maskNewFileName = (replaceFiles && getMaskFile() != null && getMaskFile()
						.getName().toLowerCase().endsWith(FILE_TYPE_BMP)) ? getMaskFile()
						.getAbsolutePath()
						: getNewFileName(new File(newFileName).getName()
								.substring(
										0,
										new File(newFileName).getName()
												.lastIndexOf("."))
								+ "_mask", FILE_TYPE_BMP);

				mask.save(CoreImage.TYPE_BMP, new File(maskNewFileName));
				setMaskFile(new File(maskNewFileName));
			} else {
				setMaskFile(null);
			}

			main.convertToThreeBand();

			main.save(CoreImage.TYPE_BMP, new File(newFileName));
			setImageFile(new File(newFileName));
		} catch (Exception es) {
			UtilsPlugin.error(es);
		}
	}

	private void saveImageAsMask(CoreImage mask, String maskFileName) {
		try {
			if (mask != null) {
				mask.save(CoreImage.TYPE_BMP, FileUtils
						.createFileWithExtension(maskFileName,
								IFileConstants.FILE_EXT_BMP));
				setMaskFile(new File(maskFileName));
			} else {
				setMaskFile(null);
			}
		} catch (Exception es) {
			UtilsPlugin.error(es);
		}
	}

	private String getNewFileName(String fileType) {
		return getNewFileName(getIdentifier().replace('%', '_'), fileType);
	}

	private String getNewFileName(String prefix, String fileType) {
		String newFileName = prefix + fileType;
		File newFile = new File(getWorkDir(), newFileName);

		int i = 0;
		while (newFile.exists()) {
			i++;
			newFileName = prefix + i + fileType;
			newFile = new File(getWorkDir(), newFileName);
		}

		return newFile.getAbsolutePath().toString();
	}

	private boolean handleCopyImage(Clipboard clip) {
		if (getImageFile() == null) {
			return false;
		}

		final File image = getImageFile().getAbsoluteFile();
		if (image.exists()) {
			List<File> files = new ArrayList<File>();
			files.add(image);
			FileTransferable ft = new FileTransferable(files);
			clip.setContents(ft, ft);
			return true;
		} else {
			return false;
		}
	}

	public static String getExtension(String filename) {
		int i = filename.lastIndexOf('.');
		if (i != -1 && i < filename.length()) {
			return filename.substring(++i);
		}
		return null;
	}

	public void refresh() {
		pi = null;
	}

	public IImageHolder cloneImageHolder() {
		try {
			return (IImageHolder) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		ImageHolder clone = (ImageHolder) super.clone();

		clone.pi = null;

		return clone;
	}

	public Object getAdapter(Class adapter) {
		if (IPasteTargetAdapter.class == adapter) {
			return this;
		} else if (IPasteTargetAdapter.IPasteMaskAdapter.class == adapter) {
			return new IPasteTargetAdapter.IPasteMaskAdapter() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.nokia.tools.media.utils.layers.IPasteTargetAdapter#isPasteAvailable(java.awt.datatransfer.Clipboard,
				 *      java.lang.Object)
				 */
				public boolean isPasteAvailable(Clipboard clip, Object params) {
					return !isSvg()
							&& (null != ClipboardHelper.getClipboardContent(
									ClipboardHelper.CONTENT_TYPE_MASK, clip));
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.nokia.tools.media.utils.layers.IPasteTargetAdapter#isPasteAvailable(java.lang.Object,
				 *      java.lang.Object)
				 */
				public boolean isPasteAvailable(Object data, Object params) {
					try {
						return !isSvg() && (null != data)
								&& PasteHelper.isParameterUsableAsMask(data);
					} catch (Exception e) {
						
						e.printStackTrace();
					}
					return false;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.nokia.tools.media.utils.layers.IPasteTargetAdapter#paste(java.lang.Object,
				 *      java.lang.Object)
				 */
				public Object paste(Object data, Object targetInfo)
						throws Exception {
					ImageHolder.this.pasteMask(data);
					return null;
				}

			};
		}
		return null;
	}

	/* IPasteTargetAdapter impl */
	public boolean isPasteAvailable(Clipboard clip, Object params) {
		if (clip == null) {
			clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		}
		return ClipboardHelper.clipboardContainsData(
				ClipboardHelper.CONTENT_TYPE_SINGLE_IMAGE, clip);
	}

	public boolean isPasteAvailable(Object data, Object params) {
		if (data instanceof RenderedImage)
			return true;
		if (data instanceof File) {
			data = ((File) data).getAbsolutePath();
		}
		if (data instanceof String) {
			String path = (String) data;
			if (new File(path).exists()) {
				String ext = FileUtils.getExtension(path);
				// find out if image can be loaded
				if (IFileConstants.FILE_EXT_SVG.equalsIgnoreCase(ext)
						|| IFileConstants.FILE_EXT_SVG_TINY
								.equalsIgnoreCase(ext))
					return true;
				return ImageIO.getImageReadersByFormatName(ext.toLowerCase())
						.hasNext();
			}
		}
		return false;
	}

	public Object paste(Object data, Object targetInfo) throws Exception {
		paste(data);
		return null;
	}

	private void paste(Object data) throws Exception {

		// clearMask() canno be here
		// clearMask();

		new PasteHelper() {
			@Override
			protected void pasteMask(File maskFile) throws Exception {
				ImageHolder.this.pasteMask(maskFile.getAbsolutePath());
			}

			@Override
			protected void pasteMask(RenderedImage image) throws Exception {
				ImageHolder.this.pasteMask(image);
			}

			@Override
			protected void paste(File file) throws Exception {
				pasteImage(file.getAbsolutePath());
			}

			@Override
			protected void paste(RenderedImage image) throws Exception {
				pasteImage(image);
			}
		}.paste(data);
	}

	/* IPasteTargetAdapter impl end */
}
