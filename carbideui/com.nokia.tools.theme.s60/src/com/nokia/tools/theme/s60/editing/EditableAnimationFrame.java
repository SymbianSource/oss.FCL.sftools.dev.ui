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

package com.nokia.tools.theme.s60.editing;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.clipboard.FileTransferable;
import com.nokia.tools.media.utils.clipboard.PasteHelper;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.IPasteTargetAdapter;
import com.nokia.tools.media.utils.timeline.impl.cp.ControlPoint;
import com.nokia.tools.platform.theme.BitmapProperties;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeConstants;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.theme.editing.SVG2SVTConverter;
import com.nokia.tools.theme.s60.S60ThemePlugin;
import com.nokia.tools.theme.s60.effects.imaging.Colorize;
import com.nokia.tools.theme.s60.model.S60Theme;


public class EditableAnimationFrame extends ControlPoint implements Cloneable,
		IAnimationFrame, IAdaptable {

	public static final String FILE_TYPE_BMP = IFileConstants.FILE_EXT_DOTBMP;

	public static final String FILE_TYPE_SVG = IFileConstants.FILE_EXT_DOTSVG;

	protected RenderedImage pi;

	protected EditableAnimatedEntity entity;

	protected ThemeGraphic graphic;

	protected long forceTime = -1;

	private Colorize COLORIZE;

	public EditableAnimationFrame(EditableAnimatedEntity entity, ThemeGraphic tg) {
		super(0);
		init(entity, tg);
	}

	public void init(EditableAnimatedEntity entity, ThemeGraphic tg) {
		this.entity = entity;
		this.graphic = tg;
		this.pi = null;
		this.COLORIZE = new Colorize();
	}

	public int getSeqNo() {
		String seqno = graphic.getAttribute(ThemeTag.ATTR_ANIMATE_SEQNO);
		return Integer.parseInt(seqno);
	}

	public String getName() {
		File file = getImageFile();
		return file.getName().substring(0, file.getName().lastIndexOf('.'));
	}

	@Override
	public long getTime() {
		Vector delays = entity.getTimeDelaysForAnimate();
		long time = 0;
		int idx = 0;
		int seqNo = getSeqNo();
		for (Object object : delays) {
			if (idx == seqNo) {
				return time;
			}
			Integer animTime = (Integer) object;
			time += animTime;
			idx++;
		}
		return 0;
	}

	@Override
	public boolean canBeDeleted() {
		return true;
	}

	@Override
	public boolean canBeMoved() {
		return true;
	}

	public long getForceTime() {
		if (forceTime < 0) {
			return getTime();
		}
		return forceTime;
	}

	void markForceTime() {
		this.forceTime = getTime();
	}

	void setForceTime(long time) {
		this.forceTime = time;
	}

	public long getAnimateTime() {
		String animateTime = graphic.getAttribute(ThemeTag.ATTR_ANIMATE_TIME);
		if (animateTime == null) {
			animateTime = ThemeTag.ATTR_ANIMATE_DEFAULT_TIME;
		}
		return Long.parseLong(animateTime);
	}

	public void setAnimateTime(long time) {
		entity.setAnimateTime(this, time);
	}

	public IAnimatedImage getParent() {
		return entity;
	}

	public File getFile() {
		return getImageFile();
	}

	public File getImageFile() {
		String imagePath = graphic.getImageFile();

		if (imagePath == null) {
			return null;
		}

		File imageFile = new File(imagePath);

		return imageFile.isAbsolute() ? imageFile : new File(getThemeDir(),
				imagePath);
	}

	public void setImageFile(File imageFile) {
		IPath imagePath = null;
		if (imageFile != null) {
			imagePath = new Path(imageFile.getAbsolutePath());
		}
		ImageLayer il = (ImageLayer) graphic.getImageLayers().get(0);
		if (imagePath != null) {
			il.setAttribute(ThemeTag.FILE_NAME, imagePath.toFile().toString());
		} else {
			il.removeAttribute(ThemeTag.FILE_NAME);
		}
		pi = null;
	}

	public File getMaskFile() {
		String imagePath = graphic
				.getMask(entity.getEntity().getToolBox().SoftMask);

		if (imagePath == null) {
			return null;
		}

		File imageFile = new File(imagePath);

		return imageFile.isAbsolute() ? imageFile : new File(getThemeDir(),
				imagePath);
	}

	public void setMaskFile(File maskFile) {
		IPath maskPath = null;
		if (maskFile != null) {
			maskPath = new Path(maskFile.getAbsolutePath());
		}
		ImageLayer il = (ImageLayer) graphic.getImageLayers().get(0);
		if (entity.getEntity().getToolBox().SoftMask) {
			if (maskPath != null) {
				il.setAttribute(ThemeTag.ATTR_SOFTMASK, maskPath.toFile()
						.toString());
			} else {
				il.removeAttribute(ThemeTag.ATTR_SOFTMASK);
			}
		} else {
			if (maskPath != null) {
				il.setAttribute(ThemeTag.ATTR_HARDMASK, maskPath.toFile()
						.toString());
			} else {
				il.removeAttribute(ThemeTag.ATTR_HARDMASK);
			}
		}
		pi = null;
	}

	public File getThemeDir() {
		return ((EditableAnimatedEntity) getParent()).getThemeDir();
	}

	public File getWorkDir() {
		return ((EditableAnimatedEntity) getParent()).getWorkDir();
	}

	public int getWidth() {
		return entity.getWidth();
	}

	public int getHeight() {
		return entity.getHeight();
	}

	public boolean supportsMask() {
		return true;
	}

	public boolean supportsBitmap() {
		return true;
	}

	public boolean supportsSvg() {
		return true;
	}

	public String getIdentifier() {
		return ((EditableAnimatedEntity) getParent()).getEntity()
				.getIdentifier();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImageHolder#getImage()
	 */
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
				pi = applyBitmapProperties(pi);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return pi;
	}

	public RenderedImage applyBitmapProperties(RenderedImage img) {
		SkinnableEntity entity = ((EditableEntityImage) getParent())
				.getEntity();
		if (entity.getAttribute().get(BitmapProperties.COLORIZE_SELECTED) != null
				|| entity.getAttribute().get(
						BitmapProperties.OPTIMIZE_SELECTION) != null) {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map
					.put(
							BitmapProperties.DITHER_SELECTED,
							entity.getAttribute().get(
									BitmapProperties.DITHER_SELECTED) == null ? Boolean.FALSE
									: entity.getAttribute().get(
											BitmapProperties.DITHER_SELECTED));
			map
					.put(
							BitmapProperties.COLORIZE_SELECTED,
							entity.getAttribute().get(
									BitmapProperties.COLORIZE_SELECTED) == null ? Boolean.FALSE
									: entity.getAttribute().get(
											BitmapProperties.COLORIZE_SELECTED));
			map.put(BitmapProperties.OPTIMIZE_SELECTION, entity.getAttribute()
					.get(BitmapProperties.OPTIMIZE_SELECTION) == null ? ""
					: entity.getAttribute().get(
							BitmapProperties.OPTIMIZE_SELECTION));
			map.put(BitmapProperties.COLOR, entity.getAttribute().get(
					BitmapProperties.COLOR) == null ? Color.WHITE : entity
					.getAttribute().get(BitmapProperties.COLOR));
			map.put(BitmapProperties.COLORIZE, entity.getAttribute().get(
					BitmapProperties.COLORIZE));
			
			map.put(ThemeConstants.RENDERED_IMAGE, img);

			if (((Boolean) map.get(BitmapProperties.COLORIZE_SELECTED))
					|| ((String) map.get(BitmapProperties.OPTIMIZE_SELECTION))
							.length() > 0) {
				map = COLORIZE.manipulate(map);
				img = (RenderedImage) map.get(BitmapProperties.RETURN_IMAGE);
			}
		}
		return img;
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
			S60ThemePlugin.error(e);
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
			S60ThemePlugin.error(e);
		}
		return rawImage.getAwt();
	}

	public boolean hasMask() {
		return getMaskFile() != null && getMaskFile().exists();
	}

	public RenderedImage getMask() {
		if (supportsMask() && getMaskFile() != null) {
			try {
				RenderedImage mask = CoreImage.create().load(getMaskFile(),
						getWidth(), getHeight(), CoreImage.KEEP_ORIGINAL)
						.getAwt();

				return mask;
			} catch (Exception e) {
				S60ThemePlugin.error(e);
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

	public Object paste(Object data, Object params) throws Exception {

		new PasteHelper() {

			@Override
			protected void pasteMask(File maskFile) throws Exception {
				EditableAnimationFrame.this.pasteMask(maskFile);
			}

			@Override
			protected void pasteMask(RenderedImage image) throws Exception {
				EditableAnimationFrame.this.pasteMask(image);
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
		return null;
	}

	private void pasteImage(RenderedImage img) {
		String newFileName = getNewFileName(FILE_TYPE_BMP);
		saveImageAsBmpAndMask(img, newFileName);
		pi = null;
		entity.getPropertyChangeSupport().firePropertyChange(
				IImage.PROPERTY_STATE, null, null);
	}

	private void pasteImage(String absolutePath) throws ThemeException {
		try {
			if (absolutePath.toLowerCase().endsWith(FILE_TYPE_SVG)) {

				Map dimensions = ((S60Theme) entity.getEntity().getRoot())
						.getDimensions();
				String identifier = getIdentifier();
				absolutePath = SVG2SVTConverter.convertToSVGT(identifier,
						dimensions, absolutePath);

				String newFileName = getNewFileName(FILE_TYPE_SVG);
				FileUtils.copyFile(absolutePath, newFileName);

				setImageFile(new File(newFileName));
				setMaskFile(null);

				entity.getPropertyChangeSupport().firePropertyChange(
						IImage.PROPERTY_STATE, null, null);
			} else {
				RenderedImage img = ImageIO.read(new File(absolutePath));
				String newFileName = getNewFileName(FILE_TYPE_BMP);
				saveImageAsBmpAndMask(img, newFileName);
				entity.getPropertyChangeSupport().firePropertyChange(
						IImage.PROPERTY_STATE, null, null);
			}
		} catch (final Exception e) {
			S60ThemePlugin.error(e);
			throw new ThemeException(e);
			
		}
	}

	public void pasteMask(Object data) throws Exception {
		new PasteHelper() {
			@Override
			protected void paste(File file) throws Exception {
				EditableAnimationFrame.this.pasteMask(file.getAbsolutePath());
			}

			@Override
			protected void paste(RenderedImage image) throws Exception {
				EditableAnimationFrame.this.pasteMask(image);
			}
		}.paste(data);
	}

	private void pasteMask(RenderedImage img) {
		String fileName = getImageFile().getName().toString();
		String maskNewFileName = getNewFileName(fileName.substring(0, fileName
				.lastIndexOf("."))
				+ "_mask", FILE_TYPE_BMP);
		saveImageAsMask(CoreImage.create(img), maskNewFileName);
		entity.getPropertyChangeSupport().firePropertyChange(
				IImage.PROPERTY_STATE, null, null);
	}

	private void pasteMask(String absolutePath) {
		try {
			if (absolutePath.toLowerCase().endsWith(
					IFileConstants.FILE_EXT_DOTSVG)) {

				Map dimensions = ((S60Theme) entity.getEntity().getRoot())
						.getDimensions();
				String identifier = getIdentifier();

				absolutePath = SVG2SVTConverter.convertToSVGT(identifier,
						dimensions, absolutePath);

				String fileName = getImageFile().getName().toString();

				String maskNewFileName = getNewFileName(fileName.substring(0,
						fileName.lastIndexOf("."))
						+ "_mask", FILE_TYPE_BMP);

				CoreImage img = CoreImage.create().load(new File(absolutePath),
						getWidth(), getHeight());

				saveImageAsMask(img, maskNewFileName);

				entity.getPropertyChangeSupport().firePropertyChange(
						IImage.PROPERTY_STATE, null, null);
			} else {

				CoreImage img = CoreImage.create(ImageIO.read(new File(
						absolutePath)));

				String fileName = getImageFile().getName().toString();

				String maskNewFileName = getNewFileName(fileName.substring(0,
						fileName.lastIndexOf("."))
						+ "_mask", FILE_TYPE_BMP);

				saveImageAsMask(img, maskNewFileName);

				entity.getPropertyChangeSupport().firePropertyChange(
						IImage.PROPERTY_STATE, null, null);
			}
		} catch (Exception e) {
			S60ThemePlugin.error(e);
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

	public boolean isPasteAvailable(Clipboard clip, Object params) {
		if (clip == null) {
			clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		}
		return ClipboardHelper.clipboardContainsData(
				ClipboardHelper.CONTENT_TYPE_SINGLE_IMAGE
						| ClipboardHelper.CONTENT_TYPE_IMAGE_WITH_MASK, clip);
	}

	public boolean isPasteAvailable(Object data, Object params) {
		return PasteHelper.isParameterUsableAsImage(data);
	}

	private void saveImageAsBmpAndMask(RenderedImage image, String newFileName) {
		try {
			CoreImage mask = null;
			CoreImage main = CoreImage.create(image);
			if (getMaskFile() == null)
				mask = main.copy().extractMask(true);

			if (mask != null) {
				String maskNewFileName = getNewFileName(new File(newFileName)
						.getName().substring(
								0,
								new File(newFileName).getName()
										.lastIndexOf("."))
						+ "_mask", FILE_TYPE_BMP);

				mask.save(CoreImage.TYPE_BMP, new File(maskNewFileName));
				setMaskFile(new File(maskNewFileName));
			}
			main.convertToThreeBand();

			main.save(CoreImage.TYPE_BMP, FileUtils.createFileWithExtension(
					newFileName, IFileConstants.FILE_EXT_BMP));
			setImageFile(new File(newFileName));
		} catch (Exception es) {
			S60ThemePlugin.error(es);
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
			es.printStackTrace();
		}
	}

	private String getNewFileName(String fileType) {
		return getNewFileName(getIdentifier(), fileType);
	}

	private String getNewFileName(String prefix, String fileType) {
		prefix = prefix.replace('%', '_');
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
		File image = getImageFile();
		if (image != null && image.exists()) {
			List<File> files = new ArrayList<File>();
			files.add(image);
			File maskImage = getMaskFile();
			if (maskImage != null && maskImage.exists()) {
				files.add(maskImage);
			}
			FileTransferable trans = new FileTransferable(files);
			clip.setContents(trans, trans);
			return true;
		} else {
			return ClipboardHelper.copyImageToClipboard(clip,
					getRAWImage(false));
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
		refresh(false);
	}

	public void refresh(boolean silent) {
		pi = null;
		if (!silent) {
			entity.getPropertyChangeSupport().firePropertyChange(
					IImage.PROPERTY_STATE, null, null);
		}
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
		EditableAnimationFrame clone = (EditableAnimationFrame) super.clone();

		clone.pi = null;
		clone.graphic = (ThemeGraphic) graphic.clone();

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
					return !isSvg() && (null != data);
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.nokia.tools.media.utils.layers.IPasteTargetAdapter#paste(java.lang.Object,
				 *      java.lang.Object)
				 */
				public Object paste(Object data, Object params)
						throws Exception {
					EditableAnimationFrame.this.pasteMask(data);
					return null;
				}

			};
		}
		return null;
	}
}
