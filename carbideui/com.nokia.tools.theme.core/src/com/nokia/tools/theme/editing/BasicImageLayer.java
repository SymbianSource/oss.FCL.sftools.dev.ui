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
package com.nokia.tools.theme.editing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.RGB;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.image.RenderedImageDescriptor;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.clipboard.PasteHelper;
import com.nokia.tools.media.utils.layers.EffectTypes;
import com.nokia.tools.media.utils.layers.IEffectDescriptor;
import com.nokia.tools.media.utils.layers.IEffectParameter;
import com.nokia.tools.media.utils.layers.IEffectParameterDescriptor;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.layers.IPasteTargetAdapter;
import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroups;
import com.nokia.tools.media.utils.svg.ColorGroupsStore;
import com.nokia.tools.media.utils.svg.ColorizeSVGFilter;
import com.nokia.tools.platform.theme.ColourGraphic;
import com.nokia.tools.platform.theme.EffectConstants;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.LayerUtils;
import com.nokia.tools.platform.theme.Part;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.core.Activator;

public class BasicImageLayer implements ILayer, PropertyChangeListener,
		IAdaptable {
	private PropertyChangeSupport propsup = new PropertyChangeSupport(this);

	private PropertyChangeSupport propsupEmpty = new PropertyChangeSupport(this);

	private BasicEntityImage parent;

	private Theme theme;

	/* ImageLayer associated with this */
	private ImageLayer imageLayer;

	/* true when image is new bitmap only in memory and needs to be saved */
	private boolean isImageInMemory;

	/* true when mask is new bitmap only in memory and needs to be saved */
	private boolean isMaskInMemory;

	/* flag when user pasted image path to this layer */
	private boolean imageFileNameChanged;

	private boolean maskFileNameChanged;

	/* current layer image in original state - not rotated, scaled, etc. */
	private RenderedImage origImage;

	/* current layer image in currennt state - not rotated, scaled, etc. */
	private RenderedImage currentImage;

	/* mask image */
	private RenderedImage origMask;

	/* current mask image */
	private RenderedImage currentMask;

	/* small preview image */
	private ImageDescriptor iconImage;

	/* mask preview icon */
	private ImageDescriptor maskIconImage;

	/* raw image icon image */
	private ImageDescriptor rawIconImage;

	/* flag if images was already loaded - perf. optimization. */
	private boolean imagesWasLoaded;

	private List<ILayerEffect> effects;

	public BasicImageLayer(ImageLayer iml, BasicEntityImage parent, Theme theme)
			throws ThemeException {
		init(iml, parent, theme);
	}

	protected void init(ImageLayer iml, BasicEntityImage parent, Theme theme)
			throws ThemeException {
		this.parent = parent;
		this.imageLayer = iml;
		this.theme = theme;

		setEnabled(iml.isSelected());

		String name = iml.getAttribute(ThemeTag.ATTR_NAME);
		if (name == null) {
			name = ThemeTag.ELEMENT_LAYER + hashCode();
			iml.setAttribute(ThemeTag.ATTR_NAME, name);
		}

		effects = new ArrayList<ILayerEffect>(1);
		effects.add(new ApplyGraphics());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#addLayerEffect(java.lang.String)
	 */
	public ILayerEffect addLayerEffect(String effectName) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#addPropertyListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyListener(PropertyChangeListener ps) {
		propsup.addPropertyChangeListener(ps);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#changeEffectOrder(com.nokia.tools.media.utils.layers.ILayerEffect,
	 *      int)
	 */
	public void changeEffectOrder(ILayerEffect effect, int delta) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#clearLayer()
	 */
	public void clearLayer() {
		// disable multiple firing of events
		Object origPropsup = propsup;
		propsup = propsupEmpty;

		for (ILayerEffect e : getLayerEffects())
			e.setSelected(false);
		clearMask();
		clearImage();
		clearPreviewImages();

		propsup = (PropertyChangeSupport) origPropsup;

		// update icon images
		updateIconImages();
		propsup.firePropertyChange(PROPERTY_STRUCTURE_CHANGE, null, null);
		propsup.firePropertyChange(PROPERTY_IMAGE, null, null);
	}

	public void clearPreviewImages() {
		iconImage = null;
		rawIconImage = null;
		maskIconImage = null;
	}

	public void clearImage() {
		setOrigImage(null);
		isImageInMemory = false;
		imageLayer.getAttributes().remove(ThemeTag.FILE_NAME);
		propsup.firePropertyChange(PROPERTY_IMAGE, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#clearMask()
	 */
	public void clearMask() {
		setOrigMask(null);
		isMaskInMemory = false;
		imageLayer.getAttributes().remove(getMaskAttribute());
		propsup.firePropertyChange(PROPERTY_IMAGE, null, null);
	}

	protected void setCurrentImage(RenderedImage currentImage) {
		this.currentImage = currentImage;
	}

	protected void setCurrentMask(RenderedImage currentMask) {
		this.currentMask = currentMask;
	}

	protected void setOrigImage(RenderedImage origImage) {
		this.origImage = origImage;
		currentImage = null;
	}

	protected void setOrigMask(RenderedImage origMask) {
		this.origMask = origMask;
		currentMask = null;
	}

	protected boolean hasImageFileNameChanged() {
		return imageFileNameChanged;
	}

	protected void notifyImageSaved(String filePath) {
		isImageInMemory = false;
		imageFileNameChanged = false;
		imageLayer.setAttribute(ThemeTag.FILE_NAME, filePath);
		// set TMP image flag
		setImageLayerAttr(ThemeTag.ATTR_TMP_IMAGE, "true");
	}

	protected void notifyMaskSaved(String filePath) {
		isMaskInMemory = false;
		maskFileNameChanged = false;
		imageLayer.setAttribute(getMaskAttribute(), filePath);
		// set TMP image flag
		setImageLayerAttr(ThemeTag.ATTR_TMP_MASK_IMAGE, "true");
	}

	protected void setImageLayerAttr(String name, String value) {
		imageLayer.setAttribute(name, value);
	}

	public String getImageLayerAttr(String name) {
		return imageLayer.getAttribute(name);
	}

	protected String getMaskAttribute() {
		return supportSoftMask() ? ThemeTag.ATTR_SOFTMASK
				: ThemeTag.ATTR_HARDMASK;
	}

	private String makeAbsolutePath(String s) {
		if (StringUtils.isEmpty(s))
			return s;
		if (new File(s).isAbsolute()) {
			return s;
		} else {
			try {
				return FileUtils.makeAbsolutePath(theme.getThemeDir(), s);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return s;
	}

	protected RenderedImage getCurrentImage() {
		return getCurrentImage(null);
	}

	public RenderedImage getCurrentImage(PreviewElement previewElement) {
		loadImages(previewElement);
		return currentImage == null ? getOrigImage() : currentImage;
	}

	public RenderedImage getCurrentMask() {
		loadImages();
		return currentMask == null ? getOrigMask() : currentMask;
	}

	protected boolean isImageInMemory() {
		return isImageInMemory;
	}

	protected boolean isMaskInMemory() {
		return isMaskInMemory;
	}

	protected RenderedImage getOrigImage() {
		return origImage;
	}

	protected RenderedImage getOrigMask() {
		return origMask;
	}

	private void updateIconImages() {

		if (!imagesWasLoaded)
			return;

		RenderedImage result = processImage();
		BufferedImage result2 = new BufferedImage(18, 18,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) ((BufferedImage) result2).getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, 18, 18);
		g.dispose();
		// iconImage = new RenderedImageDescriptor(result);
		iconImage = new RenderedImageDescriptor(result, 18, 18,
				BufferedImage.TYPE_INT_ARGB,
				RenderedImageDescriptor.NO_TRANSPARENT);

		if (origImage != null) {
			rawIconImage = new RenderedImageDescriptor(origImage, 18, 18,
					BufferedImage.TYPE_INT_ARGB,
					RenderedImageDescriptor.NO_TRANSPARENT);
		} else {
			rawIconImage = null;
		}

		if (origMask != null) {
			maskIconImage = new RenderedImageDescriptor(origMask, 18, 18,
					BufferedImage.TYPE_INT_RGB,
					RenderedImageDescriptor.NO_TRANSPARENT);
		} else {
			maskIconImage = null;
		}
	}

	/**
	 * processes image and returns
	 * 
	 * @return
	 */
	private RenderedImage processImage() {
		return processImage(false);
	}

	/**
	 * processes image and returns
	 * 
	 * @return
	 */
	private RenderedImage processImage(boolean preview) {
		loadImages();

		RenderedImage img = processImage(getCurrentImage(),
				getSelectedLayerEffects(), preview);
		if (img == null && parent.getWidth() != 0 && parent.getHeight() != 0) {
			img = CoreImage.create().getBlankImage(parent.getWidth(),
					parent.getHeight(), Color.WHITE);
		}
		return img;
	}

	/**
	 * Applies effects (LayerEffects) in list to image
	 * 
	 * @param image
	 * @param effectList
	 * @return
	 */
	protected RenderedImage processImage(RenderedImage image, List effectList,
			boolean preview) {
		return image;
	}

	private void loadImages() {
		loadImages(parent.getPreviewElement());
	}

	private synchronized void loadImages(PreviewElement previewElement) {
		if (imagesWasLoaded)
			return;

		try {

			if (!isImageInMemory())
				setOrigImage(LayerUtils.getImage(theme.getThemeDir(),
						imageLayer, parent.getThemeGraphics(), parent
								.getElementLayout(), true, parent.getEntity(),
						parent.getWidth(), parent.getHeight(), previewElement));

			if (!isMaskInMemory())
				setOrigMask(LayerUtils.getMaskImage(theme.getThemeDir(),
						imageLayer, parent.getEntity(), parent.getWidth(),
						parent.getHeight()));
			imagesWasLoaded = true;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#copyImageToClipboard(java.awt.datatransfer.Clipboard)
	 */
	public void copyImageToClipboard(Clipboard clip) {
		handleCopyImage(clip == null ? Toolkit.getDefaultToolkit()
				.getSystemClipboard() : clip);
	}

	private boolean handleCopyImage(Clipboard clip) {

		if (isBackground()) {
			// special handle
			return handleCopyImageForBg(clip);
		}

		if (isImageInMemory()) {
			return ClipboardHelper
					.copyImageToClipboard(clip, getCurrentImage());
		} else {

			String filename = getFileName(true);
			String mask = getMaskFileName(true);

			File image = new File(filename);
			if (image.exists()) {
				File maskFile = null;
				if (mask != null)
					maskFile = new File(mask);

				Object imgObject = null;
				if (maskFile != null && maskFile.exists()) {
					imgObject = new ArrayList<File>();
					((ArrayList) imgObject).add(image);
					((ArrayList) imgObject).add(maskFile);
				} else {
					imgObject = image;
				}
				return ClipboardHelper.copyImageToClipboard(clip, imgObject);
			}
		}
		return false;
	}

	private boolean handleCopyImageForBg(Clipboard clip) {
		try {

			BasicEntityImage emodel = (BasicEntityImage) getParent();
			SkinnableEntity entity = emodel.getEntity();
			Theme theme = (Theme) entity.getRoot();
			ImageLayer iml = getImageLayer();

			RenderedImage image = LayerUtils.getImage(theme.getThemeDir(), iml,
					emodel.getThemeGraphics(), emodel.getElementLayout(), true,
					parent.getEntity(), parent.getWidth(), parent.getHeight());
			return ClipboardHelper.copyImageToClipboard(clip, image);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/** return underlying model object */
	public ImageLayer getImageLayer() {
		return imageLayer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#disableNonDefaultEffects()
	 */
	public void disableNonDefaultEffects() {
		propsup.firePropertyChange(PROPERTY_IMAGE, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#endAnimation()
	 */
	public void endAnimation() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getAnimatedImage(long,
	 *      boolean)
	 */
	public RenderedImage getProcessedImage(long timeOffset, boolean preview) {
		return processImage(preview);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getAnimationDuration()
	 */
	public long getAnimationDuration() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getAnimationDuration(com.nokia.tools.media.utils.layers.TimingModel)
	 */
	public long getAnimationDuration(TimingModel timingType) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getAnimationDuration(com.nokia.tools.media.utils.layers.TimeSpan)
	 */
	public long getAnimationDuration(TimeSpan span) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getAttributes()
	 */
	public Map getAttributes() {
		return imageLayer.getAttributes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getAvailableLayerEffects()
	 */
	public List<ILayerEffect> getAvailableLayerEffects() {
		return Collections.EMPTY_LIST;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getColors()
	 */
	public Map<RGB, Integer> getColors() {
		List<RGB> colors = new ArrayList<RGB>();
		if (getFileName(true) == null) {
			// qsn_bg_area_control, etc.
			return Collections.EMPTY_MAP;
		} else if (getAttributes().get(ThemeTag.ATTR_COLOUR_RGB) != null) {
			String colour = (String) getAttributes().get(
					ThemeTag.ATTR_COLOUR_RGB);
			Color c = ColorUtil.toColor(colour);
			colors.add(new RGB(c.getRed(), c.getGreen(), c.getBlue()));
		} else {
			if (FileUtils.getExtension(getFileName(true)).equalsIgnoreCase(
					"svg")) {
				colors = ColorizeSVGFilter.getColors2(new File(
						getFileName(true)));
			}
		}
		final List<RGB> finalColors = colors;
		TreeMap<RGB, Integer> colorsWithoutDuplicatesMap = new TreeMap<RGB, Integer>(
				new Comparator<RGB>() {
					public int compare(RGB o1, RGB o2) {
						return finalColors.indexOf(o1)
								- finalColors.indexOf(o2);
					}
				});

		for (RGB rgb : finalColors) {
			if (!colorsWithoutDuplicatesMap.containsKey(rgb)) {
				colorsWithoutDuplicatesMap.put(rgb, 1);
			} else {
				Integer currentCount = colorsWithoutDuplicatesMap.get(rgb);
				colorsWithoutDuplicatesMap.put(rgb, currentCount + 1);
			}
		}

		return colorsWithoutDuplicatesMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getEffect(java.lang.String)
	 */
	public ILayerEffect getEffect(String name) {
		if (name != null) {
			for (ILayerEffect effect : getLayerEffects()) {
				if (name.equals(effect.getName())) {
					return effect;
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getFileName(boolean)
	 */
	public String getFileName(boolean makeAbsolute) {
		if (!isImageInMemory()) {
			String fileName = "";
			
			if(!parent.getEntity().isSkinned() && !(parent.getEntity() instanceof Part)){
				fileName = imageLayer.getFileName((Theme) parent.getEntity()
					.getRoot().getModel(),(Theme) parent.getEntity()
					.getRoot(), parent.getPreviewElement());
			}else{
				fileName = imageLayer.getFileName((Theme) parent.getEntity()
					.getRoot(), parent.getPreviewElement());
			}
			
			if (makeAbsolute) {
				return makeAbsolutePath(fileName);
			} else {
				return fileName;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getGroups()
	 */
	public List<ColorGroup> getGroups() {
		return getReferencedColors(parent.getEntity(), (this.getParent()
				.getLayerCount() > 1) ? this.getName() : null);
	}

	/**
	 * Utility method service needed for other
	 */
	public static List<ColorGroup> getReferencedColors(ThemeBasicData data,
			String layerName) {
		List<ColorGroup> relevantGroups = new ArrayList<ColorGroup>();
		if (ColorGroupsStore.isEnabled) {
			Theme theme = (Theme) data.getRoot();
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
					.findFilesForLocation(
							new Path(theme.getThemeFile().getAbsolutePath()));

			IProject project = (files != null && files.length > 0) ? files[0]
					.getProject() : null;

			if (project != null) {
				if (ColorGroupsStore.getColorGroupsForProject(project) != null) {
					for (ColorGroup grp : ColorGroupsStore
							.getColorGroupsForProject(project).getGroups()) {
						if (grp.containsItemWithIdAndLayerName(data
								.getIdentifier(), layerName)) {
							relevantGroups.add(grp);
						}
					}
				}
			}
		}
		return relevantGroups;
	}

	/**
	 * Utility method service needed for other.
	 */
	public static ColorGroups getAvailableColors(IContentData data) {
		if (data instanceof ThemeData && ColorGroupsStore.isEnabled) {
			Theme theme = (Theme) ((ThemeData) data).getData().getRoot();
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
					.findFilesForLocation(
							new Path(theme.getThemeFile().getAbsolutePath()));

			IProject project = (files != null && files.length > 0) ? files[0]
					.getProject() : null;

			if (project != null) {
				return ColorGroupsStore.getColorGroupsForProject(project);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getLayerEffects()
	 */
	public List<ILayerEffect> getLayerEffects() {
		return effects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getLayerIconImage()
	 */
	public ImageDescriptor getLayerIconImage() {
		loadImages();
		if (iconImage == null)
			updateIconImages();
		return iconImage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getLayerIconImage()
	 */
	public ImageDescriptor getLayerRawImageIcon() {

		loadImages();

		if (iconImage == null)
			updateIconImages();

		return rawIconImage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getLayerIconImage()
	 */
	public ImageDescriptor getLayerMaskImageIcon() {

		loadImages();
		if (iconImage == null)
			updateIconImages();
		return maskIconImage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getMaskFileName(boolean)
	 */
	public String getMaskFileName(boolean makeAbsolute) {
		if (!isMaskInMemory()) {
			String path = imageLayer.getAttribute(getMaskAttribute());
			if (makeAbsolute)
				return makeAbsolutePath(path);
			else
				return path;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getMaskImage()
	 */
	public RenderedImage getMaskImage() {
		return getOrigMask();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getName()
	 */
	public String getName() {
		return imageLayer.getAttribute(ThemeTag.ATTR_NAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getOrderIndex()
	 */
	public int getOrderIndex() {
		return parent.getIndexOf(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getParent()
	 */
	public IImage getParent() {
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getProcessedImage()
	 */
	public RenderedImage getProcessedImage() {
		return processImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getRAWImage()
	 */
	public RenderedImage getRAWImage() {
		loadImages();
		return getOrigImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getSelectedLayerEffects()
	 */
	public List<ILayerEffect> getSelectedLayerEffects() {
		return Collections.EMPTY_LIST;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getStretchMode()
	 */
	public String getStretchMode() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#hasImage()
	 */
	public boolean hasImage() {
		// bg has special handling
		if (imageLayer.isBackground())
			return true;

		if (imagesWasLoaded || isImageInMemory())
			return getCurrentImage() != null;
		return !StringUtils.isEmpty(getFileName(false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#hasMask()
	 */
	public boolean hasMask() {
		if (imagesWasLoaded || isMaskInMemory())
			return getCurrentMask() != null;
		else
			return !StringUtils.isEmpty(getMaskFileName(false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#isAnimated()
	 */
	public boolean isAnimated() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#isAnimatedFor(com.nokia.tools.media.utils.layers.TimingModel)
	 */
	public boolean isAnimatedFor(TimingModel timingType) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#isAnimatedInPreview()
	 */
	public boolean isAnimatedInPreview() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#isBackground()
	 */
	public boolean isBackground() {
		return imageLayer.isBackground();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#isBitmapImage()
	 */
	public boolean isBitmapImage() {
		if (parent.isSound() || parent.isPlaceHolderForEmbeddedFile())
			return false;

		if (isImageInMemory() && getCurrentImage() != null)
			return true;

		if (isBackground())
			return false;

		boolean check = false;
		ImageLayer iml = imageLayer;
		if (iml != null) {
			String fileName = iml.getFileName((Theme) parent.getEntity()
					.getRoot(), parent.getPreviewElement());
			if (fileName != null) {
				String ext = FileUtils.getExtension(fileName);
				if (!ext.equalsIgnoreCase(IFileConstants.FILE_EXT_SVG))
					check = true;
			}
		}
		return check;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#isEffectSelected(java.lang.String)
	 */
	public boolean isEffectSelected(String name) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#isEnabled()
	 */
	public boolean isEnabled() {
		return imageLayer.isSelected();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#isPasteImageAvailable(java.lang.Object)
	 */
	public boolean isPasteImageAvailable(Object object) {
		if (parent.isPlaceHolderForEmbeddedFile() || parent.isSound()) {
			return isPasteAllowed(parent.getEntity(), object);
		} else {
			if (isBackground())
				return false;
			else
				return PasteHelper.isParameterUsableAsImage(object);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#isPasteImageAvailable(java.awt.datatransfer.Clipboard)
	 */
	public boolean isPasteImageAvailable(Clipboard clip) {
		if (clip == null) {
			clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		}
		if (parent.isPlaceHolderForEmbeddedFile() || parent.isSound()) {
			return isPasteAllowed(parent.getEntity(), ClipboardHelper
					.getFilenameFromClipboard(clip));
		} else {
			if (isBackground())
				return false;
			else
				return ClipboardHelper.clipboardContainsData(
						ClipboardHelper.CONTENT_TYPE_SINGLE_IMAGE, clip);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#isSvgImage()
	 */
	public boolean isSvgImage() {
		if (parent.isSound() || parent.isPlaceHolderForEmbeddedFile())
			return false;
		boolean svg = false;

		ILayerEffect applyColor = getEffect(IMediaConstants.APPLY_COLOR);
		if (applyColor != null && applyColor.isSelected())
			return false;

		ImageLayer iml = imageLayer;
		if (iml != null) {
			if (iml.getThemeGraphic() instanceof ColourGraphic)
				return false;
			String fileName = iml.getFileName((Theme) parent.getEntity()
					.getRoot(), parent.getPreviewElement());
			if (fileName != null) {
				String ext = FileUtils.getExtension(fileName);
				if (ext.equalsIgnoreCase(IFileConstants.FILE_EXT_SVG)) //$NON-NLS-1$
					svg = true;
			}
		}
		return svg;
	}

	public void paste(Object data) throws Exception {
		new PasteHelper() {

			@Override
			protected void paste(File file) throws Exception {
				doPasteImage(file.getAbsolutePath());
			}

			@Override
			protected void paste(RenderedImage image) throws Exception {
				doPasteImage(image);
			}

			@Override
			protected void pasteMask(File maskFile) throws Exception {
				doPasteMask(maskFile, false);
			}

			@Override
			protected void pasteMask(RenderedImage image) throws Exception {
				doPasteMask(image, false);
			}

		}.paste(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#pasteMask(java.lang.Object)
	 */
	public void pasteMask(Object data) throws Exception {
		new PasteHelper() {

			@Override
			protected void paste(File file) throws Exception {
				doPasteMask(file, false);
			}

			@Override
			protected void paste(RenderedImage image) throws Exception {
				doPasteMask(image, false);
			}

		}.paste(data);
	}

	/*
	 * pastes image path to element
	 */
	private void doPasteImage(String path) throws ThemeException {
		try {

			/* when paste object is not image, only update attributes and return */
			if (ThemePlatform.isSoundFile(path)
					|| parent.isPlaceHolderForEmbeddedFile()) {
				getImageLayer().setAttribute(ThemeTag.FILE_NAME, path);
				imageFileNameChanged = true;
				updateIconImages();
				return;
			}

			/* convert svg to svgt? */
			boolean svg = false;
			if (path.toLowerCase().endsWith(IFileConstants.FILE_EXT_DOTSVG)) { //$NON-NLS-1$
				Map dimensions = ((Theme) ((BasicEntityImage) getParent())
						.getEntity().getRoot()).getDimensions();
				String identifier = getParent().getId();
				path = SVG2SVTConverter.convertToSVGT(identifier, dimensions,
						path);
				svg = true;
			}

			/* paste image, update attrs, refresh icons.. */
			RenderedImage image = CoreImage.create().load(new File(path),
					parent.getWidth(), parent.getHeight()).getAwt();

			if (image != null) {
				Object _image = null;

				/* check if image needs reduction to three band */
				if (!svg)
					_image = reduceTo3Band(image);
				else
					clearMask(); // old mask might remain not allowed for SVG

				if (_image != image && _image != null) {
					doPasteImage((RenderedImage) _image);
					// imageFileNameChanged is not changed
					return;
				}

				getImageLayer().setAttribute(ThemeTag.TILE_WIDTH,
						new Integer(image.getWidth()).toString());
				getImageLayer().setAttribute(ThemeTag.TILE_HEIGHT,
						new Integer(image.getHeight()).toString());

				// update iml props
				getImageLayer().setAttribute(ThemeTag.FILE_NAME, path);
				// reset scale mode
				getImageLayer().setAttribute(EffectConstants.SCALEMODE,
						IMediaConstants.STRETCHMODE_ASPECT);

				updateEffectParameter();

				imageFileNameChanged = true;
				isImageInMemory = false;
				setOrigImage(image);

				// turn off applyColor
				removeLayerEffect(EffectConstants.APPLYCOLOR);
				// turn on apply graphics
				// (must be after setOrigImage() );
				ILayerEffect effect = addLayerEffect(EffectConstants.APPLYGRAPHICS);
				if (effect != null) {
					effect.setSelected(true);
				}

				updateIconImages();

				propsup.firePropertyChange(PROPERTY_IMAGE, null, null);
			} else {
				Activator.warn("Paste: Image not found: " + path); //$NON-NLS-1$
				getImageLayer().setAttribute(ThemeTag.FILE_NAME, path);
				imageFileNameChanged = true;
			}
		} catch (final Throwable e) {
			Activator.error(e);
			throw new ThemeException(e);
		}
	}

	/**
	 * Pastes mask (image or path) to this layer. when autoConvert is true,
	 * ThemeAppJaiUtils.convertSoftMaskToHard() is called When pasting image
	 * instance, it is checked to conform to grayscale.
	 */
	private void doPasteMask(Object mask, boolean autoConvert)
			throws ThemeException {

		try {
			if (!parent.getEntity().getToolBox().Mask)
				return;
		} catch (Exception e) {
		}

		if (mask instanceof String) {
			mask = new File((String) mask);
		}

		if (mask instanceof RenderedImage) {
			// we are pasting an image
			try {
				CoreImage image = CoreImage.create((RenderedImage) mask);
				if (image != null) {
					if (supportSoftMask()) {
						// test for grayscale
						if (!image.isImageGrayScaleImage()) {
							image.convertToGray();
						}
					} else {
						// hard mask
						if (!image.isImageBlackWhiteImage()) {
							if (image.isImageGrayScaleImage())
								autoConvert = true;
							image.convertSoftMaskToHard(autoConvert);
						}
					}

					if (image.getNumBands() > 1) {
						image.convertToGrayScale();
					}

					imageLayer.setAttribute(getMaskAttribute(), null);
					setOrigMask(image.getAwt());
					maskFileNameChanged = false;
					isMaskInMemory = true;

				}
			} catch (Exception e) {
				throw new ThemeException(e);
			}
		} else if (mask instanceof File) {

			File mFile = (File) mask;
			CoreImage bim = null;

			try {
				bim = CoreImage.create().load(mFile);
			} catch (Exception e) {
			}
			if (bim == null) {
				return;
			}

			// we are pasting path
			try {
				imageLayer.setAttribute(getMaskAttribute(), (String) mFile
						.getAbsolutePath());
				setOrigMask(bim.getAwt());
				maskFileNameChanged = true;
				isMaskInMemory = false;

				boolean imgChanged = false;

				if (supportSoftMask()) {
					// test for grayscale
					if (!bim.isImageGrayScaleImage()) {
						bim.convertToGray();
						imgChanged = true;
					}
				} else {
					// hard mask
					if (!bim.isImageBlackWhiteImage()) {
						if (bim.isImageGrayScaleImage())
							autoConvert = true;
						bim.convertSoftMaskToHard(autoConvert);
						imgChanged = true;
					}
				}

				if (imgChanged) {
					if (bim.getNumBands() > 1) {
						bim.convertToGrayScale();
					}

					imageLayer.setAttribute(getMaskAttribute(), null);
					setOrigMask(bim.getAwt());
					maskFileNameChanged = false;
					isMaskInMemory = true;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// remove alpha channel from layer image, if any
		CoreImage oi = CoreImage.create(origImage);
		if (origImage != null && oi.getNumBands() == 4) {
			setOrigImage(oi.convertToThreeBand().getAwt());
			isImageInMemory = true;
		}

		// force icon to refresh
		updateIconImages();
		propsup.firePropertyChange(PROPERTY_IMAGE, null, null);
	}

	/**
	 * if this element does not supports mask, reduces to opaque img
	 * 
	 * @param img
	 * @return
	 */
	private RenderedImage reduceTo3Band(RenderedImage img) {

		// Parts can have incorrect Mask informations, image is never modified
		if (parent.getEntity() instanceof Part) {
			return img;
		}

		if (!supportMask()) {
			CoreImage ci = CoreImage.create(img);
			if (!ci.hasTransparency()) {
				return ci.reduceToThreeBand().getAwt();
			}
			try {
				if (ci.getNumBands() == 4) {
					// draw image on white bg - default behaviour
					return ci.applyBackground(Color.WHITE).reduceToThreeBand()
							.getAwt();
				}
			} catch (Exception es) {
				es.printStackTrace();
			}
		}
		return img;
	}

	private void doPasteImage(RenderedImage image) throws ThemeException {

		setOrigImage(image);
		isImageInMemory = true;

		imageLayer.setAttribute(ThemeTag.TILE_WIDTH, new Integer(image
				.getWidth()).toString());
		imageLayer.setAttribute(ThemeTag.TILE_HEIGHT, new Integer(image
				.getHeight()).toString());

		// set stretch scale mode as default
		imageLayer.setAttribute(EffectConstants.SCALEMODE,
				IMediaConstants.STRETCHMODE_ASPECT);
		updateEffectParameter();
		// turn off applyColor
		removeLayerEffect(EffectConstants.APPLYCOLOR);
		// turn on apply graphics
		ILayerEffect effect = addLayerEffect(EffectConstants.APPLYGRAPHICS);
		if (effect != null) {
			effect.setSelected(true);
		}

		updateIconImages();

		propsup.firePropertyChange(PROPERTY_IMAGE, null, null);
	}

	protected void updateEffectParameter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#removeLayerEffect(java.lang.String)
	 */
	public void removeLayerEffect(String effectName) {
	}

	public void removePropertyChangeListener(PropertyChangeListener ps) {
		propsup.removePropertyChangeListener(ps);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#setAnimatedInPreview(boolean)
	 */
	public void setAnimatedInPreview(boolean animatedInPreview) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#setAnimationStartLocation(long)
	 */
	public void setAnimationStartLocation(long time) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#setAttributes(java.util.Map)
	 */
	public void setAttributes(Map<Object, Object> attrs) {
		imageLayer.setAttributes(attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		imageLayer.setSelected(enabled);
		fireStateUpdateEvent();
	}

	public void setFileName(String filename) throws Exception {
		/*
		 * if (StringUtils.isEmpty(filename)) throw new Exception("invalid
		 * filename -must not be empty"); if (parent.getLayer(filename) != null)
		 * throw new Exception("invalid name - file named '" + filename + "'
		 * already exists");
		 */
		imageLayer.setAttribute(ThemeTag.FILE_NAME, filename);
		fireStateUpdateEvent();
	}

	protected void fireStateUpdateEvent() {
		propsup.firePropertyChange(PROPERTY_STATE, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#renameLayer(java.lang.String)
	 */
	public void setName(String newName) throws Exception {
		if (StringUtils.isEmpty(newName))
			throw new Exception("Invalid name - must not be empty!"); //$NON-NLS-1$
		if (parent.getLayer(newName) != null)
			throw new Exception("Invalid name - layer named '" + newName //$NON-NLS-1$
					+ "' already exists!"); //$NON-NLS-1$

		if (newName.length() > LAYER_NAME_LIMIT)
			newName = newName.substring(0, LAYER_NAME_LIMIT);

		imageLayer.setAttribute(ThemeTag.ATTR_NAME, newName);

		if (IMediaConstants.BackgroundLayer.equalsIgnoreCase(newName)) {
			try {
				imagesWasLoaded = false;
				loadImages();
				updateIconImages();
			} catch (Exception e) {
			}
		}

		fireStateUpdateEvent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#startAnimation()
	 */
	public void startAnimation() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#supportMask()
	 */
	public boolean supportMask() {
		if (((BasicEntityImage) getParent()).getPreviewElement() != null
				&& ((BasicEntityImage) getParent()).getPreviewElement()
						.getAttribute().get(ThemeTag.ATTR_SPLMASKID) != null) {
			return false;
		}

		return parent.getEntity().getToolBox().Mask
				|| parent.getEntity().getToolBox().SoftMask;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#supportSoftMask()
	 */
	public boolean supportSoftMask() {
		if (getParent() != null
				&& ((BasicEntityImage) getParent()).getPreviewElement() != null
				&& ((BasicEntityImage) getParent()).getPreviewElement()
						.getAttribute().get(ThemeTag.ATTR_SPLMASKID) != null) {
			return false;
		}

		return parent.getEntity().getToolBox().SoftMask;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IPasteTargetAdapter.IPasteMaskAdapter.class == adapter) {
			return new IPasteTargetAdapter.IPasteMaskAdapter() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.nokia.tools.media.utils.layers.IPasteTargetAdapter#isPasteAvailable(java.awt.datatransfer.Clipboard,
				 *      java.lang.Object)
				 */
				public boolean isPasteAvailable(Clipboard clip, Object params) {
					return supportMask()
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
					return supportMask() && (null != data);
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.nokia.tools.media.utils.layers.IPasteTargetAdapter#paste(java.lang.Object,
				 *      java.lang.Object)
				 */
				public Object paste(Object data, Object params)
						throws Exception {
					pasteMask(data);
					return null;
				}

			};
		}
		return null;
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		return propsup;
	}

	public Theme getTheme() {
		return theme;
	}

	protected boolean hasMaskFileNameChanged() {
		return maskFileNameChanged;
	}

	public void setAnimationTime(TimingModel model, long tOffs) {
	}

	public void propagateChangesToModel() throws ThemeException {
	}

	public boolean isEmpty() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#setStretchMode(java.lang.String)
	 */
	public void setStretchMode(String stretchMode) {
	}

	public static boolean isPasteAllowed(SkinnableEntity entity,
			Object pastedObject) {
		if (null == pastedObject)
			return false;

		File file = PasteHelper.extractFile(pastedObject);
		if (null == file)
			return false;
		String filePath = file.getAbsolutePath();
		if (!filePath.contains("."))
			return false;
		String extension = "." + new Path(filePath).getFileExtension();
		Set<String> exts = entity.getSupportedExtensions();
		if (null != exts && exts.contains(extension))
			return true;
		if (null != exts && exts.contains(extension.toUpperCase()))
			return true;
		return false;
	}

	class ApplyGraphics implements ILayerEffect {
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#addPropertyListener(java.beans.PropertyChangeListener)
		 */
		public void addPropertyListener(PropertyChangeListener ps) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#animationAllowed()
		 */
		public boolean animationAllowed() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#endAnimation()
		 */
		public void endAnimation() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#getAnimationDuration()
		 */
		public long getAnimationDuration() {
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#getAnimationDuration(com.nokia.tools.media.utils.layers.TimingModel)
		 */
		public long getAnimationDuration(TimingModel timingType) {
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#getAttributeDescriptors()
		 */
		@SuppressWarnings("unchecked")
		public List<IEffectParameterDescriptor> getAttributeDescriptors() {
			return Collections.EMPTY_LIST;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#getDescriptor()
		 */
		public IEffectDescriptor getDescriptor() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#getEffectParameter(java.lang.String,
		 *      long)
		 */
		public Object getEffectParameter(String key, long time) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#getEffectParameter(java.lang.String)
		 */
		public Object getEffectParameter(String key) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#getName()
		 */
		public String getName() {
			return IMediaConstants.APPLY_GRAPHICS;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#getParameter(java.lang.String)
		 */
		public IEffectParameter getParameter(String name) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#getParameters()
		 */
		@SuppressWarnings("unchecked")
		public List<IEffectParameter> getParameters() {
			return Collections.EMPTY_LIST;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#getParent()
		 */
		public ILayer getParent() {
			return BasicImageLayer.this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#getStaticEffectParameter(java.lang.String)
		 */
		public Object getStaticEffectParameter(String key) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#getType()
		 */
		public EffectTypes getType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#isAnimated()
		 */
		public boolean isAnimated() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#isAnimatedFor(com.nokia.tools.media.utils.layers.TimeSpan)
		 */
		public boolean isAnimatedFor(TimeSpan span) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#isAnimatedFor(com.nokia.tools.media.utils.layers.TimingModel)
		 */
		public boolean isAnimatedFor(TimingModel timingType) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#isAnimatedInPreview()
		 */
		public boolean isAnimatedInPreview() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#isParameterSet(java.lang.String)
		 */
		public boolean isParameterSet(String name) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#isSelected()
		 */
		public boolean isSelected() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#removePropertyChangeListener(java.beans.PropertyChangeListener)
		 */
		public void removePropertyChangeListener(PropertyChangeListener ps) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#setAnimatedInPreview(boolean)
		 */
		public void setAnimatedInPreview(boolean animatedInPreview) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#setEffectParameter(java.lang.String,
		 *      java.lang.Object)
		 */
		public void setEffectParameter(String key, Object value) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#setSelected(boolean)
		 */
		public void setSelected(boolean selected) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.ILayerEffect#startAnimation()
		 */
		public void startAnimation() {
		}
	}
}
