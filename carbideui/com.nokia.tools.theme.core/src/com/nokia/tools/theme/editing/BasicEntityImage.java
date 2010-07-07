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
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.IPasteTargetAdapter;
import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.ITreeTimeLineDataProvider;
import com.nokia.tools.platform.layout.ComponentInfo;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.ColourGraphic;
import com.nokia.tools.platform.theme.EffectConstants;
import com.nokia.tools.platform.theme.Element;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.Part;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.platform.theme.preview.PreviewImage;
import com.nokia.tools.platform.theme.preview.PreviewTagConstants;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.SimpleCache;
import com.nokia.tools.theme.core.Activator;
import com.nokia.tools.theme.core.MultiPieceManager;

public class BasicEntityImage implements IImage, PropertyChangeListener,
		IAdaptable {
	public static final boolean CACHE_ALL_IMAGES = false;
	public static String MASK_SUFFIX = "_mask";

	// for rendering BG of images
	public static final Color BASE_COLOR = Color.WHITE;
	public static final String ELEVEN_PIECE_BITMAP = ThemeTag.ATTR_11_PIECE;
	public static final String THREE_PIECE_BITMAP = ThemeTag.ATTR_3_PIECE;
	public static final String NINE_PIECE_BITMAP = ThemeTag.ATTR_9_PIECE;
	public static final String SINGLE_PIECE_BITMAP = ThemeTag.ATTR_SINGLE_BITMAP;

	/* list of image layers */
	private ArrayList<BasicImageLayer> imageLayers;
	/* sk. entity */
	private SkinnableEntity entity;

	private boolean isPreview;

	/* layout */
	private Layout elementLayout;

	/* active theme gr. */
	private ThemeGraphic themeGraphics;

	/* element dimensions */
	private int width;
	private int height;

	private PreviewElement previewElement;

	protected PropertyChangeSupport propsup = new PropertyChangeSupport(this);

	private int opacity;

	private Rectangle bounds;

	private boolean animationStarted;

	// used for lazy initialization
	protected ComponentInfo prefComponent;
	protected PreviewImage prefScreen;
	protected ThemeGraphic prefGraphics;
	private int prefWidth, prefHeight;
	private boolean graphicsInitialized;
	private boolean layoutInitialized;

	public BasicEntityImage(SkinnableEntity entity,
			PreviewElement previewElement) throws ThemeException {
		this(entity, null, previewElement, 0, 0);
	}

	public BasicEntityImage(SkinnableEntity element,
			PreviewElement previewElement, int preferedWidth, int preferedHeight)
			throws ThemeException {
		this(element, null, previewElement, preferedWidth, preferedHeight);
	}

	public BasicEntityImage(SkinnableEntity entity, ThemeGraphic grahics,
			PreviewElement pElement, int prefWidth, int prefHeight)
			throws ThemeException {
		this(entity, grahics, pElement, prefWidth, prefHeight, 0);
	}

	public BasicEntityImage(SkinnableEntity entity, ThemeGraphic grahics,
			PreviewElement previewElement, int prefWidth, int prefHeight,
			int opacity) throws ThemeException {
		this(entity, grahics, previewElement, null, null, prefWidth,
				prefHeight, opacity);
	}

	public BasicEntityImage(SkinnableEntity entity, ThemeGraphic grahics,
			PreviewElement previewElement, ComponentInfo c, PreviewImage s,
			int prefWidth, int prefHeight, int opacity) throws ThemeException {
		this.opacity = opacity;
		this.entity = entity;
		this.previewElement = previewElement;
		this.prefGraphics = grahics;
		this.prefComponent = c;
		this.prefScreen = s;
		this.prefWidth = prefWidth;
		this.prefHeight = prefHeight;

		if (prefComponent == null && previewElement != null) {
			prefComponent = previewElement.getComponentInfo();
		}

		if (prefScreen == null && previewElement != null)
			prefScreen = (PreviewImage) previewElement.getParent();
	}

	protected void initThemeGraphics() {
		if (null == prefGraphics) {
			try {
				prefGraphics = entity.getThemeGraphic(
						previewElement == null ? null : previewElement
								.getDisplay(), prefScreen, prefComponent);
			} catch (Exception e) {
				Activator.error(e);
			}
		}

		try {
			if (prefGraphics != null)
				setThemeGraphics((ThemeGraphic) prefGraphics.clone());
		} catch (Exception e) {
			Activator.error(e);
		}

		if (elementLayout != null) {
			width = elementLayout.W();
			height = elementLayout.H();
		} else {
			/* TG can be null for component store 9-piece elements */
			if (themeGraphics != null) {
				if (themeGraphics.getAttribute(ThemeTag.LAYOUT_WIDTH) != null)
					width = new Integer(themeGraphics
							.getAttribute(ThemeTag.LAYOUT_WIDTH)).intValue();
				if (themeGraphics.getAttribute(ThemeTag.LAYOUT_HEIGHT) != null)
					height = new Integer(themeGraphics
							.getAttribute(ThemeTag.LAYOUT_HEIGHT)).intValue();
			}
		}
	}

	protected void initElementLayout() {
		if (previewElement != null) {
			try {
				elementLayout = entity.getLayoutInfoForPreview(previewElement
						.getDisplay(), prefComponent, prefScreen);
			} catch (Exception e) {
				Activator.error(e);
			}
		}
		if (elementLayout == null) {
			try {
				elementLayout = entity.getLayoutInfo();
			} catch (Exception e) {
				Activator.error(e);
			}
		}
		if (elementLayout != null && elementLayout.W() < 0)
			System.out.println("ERROR2: Negative width in layout information: "
					+ entity.toString());

		/* init width, height */
		initDimension();

	}

	protected void initDimension() {
		/* use pref. w & h ? */
		if (prefWidth != 0 && prefHeight != 0) {
			width = prefWidth;
			height = prefHeight;
		} else {
			if (elementLayout != null) {
				width = elementLayout.W();
				height = elementLayout.H();
			} else {
				/* TG can be null for component store 9-piece elements */
				ThemeGraphic themeGraphics = getThemeGraphics();
				if (themeGraphics != null) {
					if (themeGraphics.getAttribute(ThemeTag.LAYOUT_WIDTH) != null)
						width = new Integer(themeGraphics
								.getAttribute(ThemeTag.LAYOUT_WIDTH))
								.intValue();
					if (themeGraphics.getAttribute(ThemeTag.LAYOUT_HEIGHT) != null)
						height = new Integer(themeGraphics
								.getAttribute(ThemeTag.LAYOUT_HEIGHT))
								.intValue();
				}
			}
		}
		if (width <= 0 || height <= 0)
			Activator.error("Warning: cannot determine dimensions for "
					+ getId() + " (EditableEntityImage)");
	}

	protected BasicEntityImage createEntityImage(Element el)
			throws ThemeException {
		return new BasicEntityImage(el, null);
	}

	public synchronized void setThemeGraphics(ThemeGraphic graphic) {
		themeGraphics = graphic;
		graphicsInitialized = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#addLayer()
	 */
	public ILayer addLayer() {
		if (getLayerCount() >= getMaximumLayerCount()) {
			return null;
		}
		ILayer l = addLayer(getImageLayers().size());
		return l;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#addLayer(int)
	 */
	public ILayer addLayer(int index) {
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		String name = ThemeTag.ELEMENT_LAYER + getAvailableLayerNumber();

		map.put(EffectConstants.ATTR_NAME, name);
		map.put(ThemeTag.ATTR_ENTITY_X, new Integer(0).toString());
		map.put(ThemeTag.ATTR_ENTITY_Y, new Integer(0).toString());
		map.put(ThemeTag.ATTR_IMAGE_X, new Integer(0).toString());
		map.put(ThemeTag.ATTR_IMAGE_Y, new Integer(0).toString());
		map.put(ThemeTag.IN_EFFECT, EffectConstants.WithAspectRatio);
		ImageLayer iml = new ImageLayer(getThemeGraphics());
		iml.getAttributes().putAll(map);
		try {
			BasicImageLayer ld = createImageLayer(iml, (Theme) entity.getRoot());
			ld.addPropertyListener(this);
			getImageLayers().add(index, ld);
			getThemeGraphics().getImageLayers().add(index, iml);

			if (animationStarted)
				ld.startAnimation();

			// fire structure change event
			propsup.firePropertyChange(PROPERTY_STRUCTURE_CHANGE, null, null);
			propsup.firePropertyChange(PROPERTY_STATE, null, null);

			return ld;
		} catch (ThemeException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * return next available layer name - 'layerN', where N is number;
	 * 
	 * @return
	 */
	private int getAvailableLayerNumber() {
		int c = getImageLayers().size();
		while (true) {
			String name = ThemeTag.ELEMENT_LAYER + c;
			boolean found = false;
			found = getLayer(name) != null;
			if (!found)
				return c;
			c++;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#addPropertyListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyListener(PropertyChangeListener ps) {
		propsup.addPropertyChangeListener(ps);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#canBeAnimated()
	 */
	public boolean canBeAnimated() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#changeLayerOrder(com.nokia.tools.media.utils.layers.ILayer,
	 *      int)
	 */
	public void changeLayerOrder(ILayer layer, int positionChange)
			throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#clearPreviewImages()
	 */
	public void clearPreviewImages() {
		for (BasicImageLayer il : getImageLayers()) {
			il.clearPreviewImages();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#clearTimeLineNodes()
	 */
	public void clearTimeLineNodes() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#endAnimation()
	 */
	public synchronized void endAnimation() {
		if (animationStarted) {
			animationStarted = false;
			for (ILayer l : getImageLayers()) {
				((BasicImageLayer) l).endAnimation();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getAggregateImage()
	 */
	public RenderedImage getAggregateImage() {
		return getAggregateImage(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getAggregateImage(int,
	 *      int)
	 */
	public RenderedImage getAggregateImage(int width, int height) {
		RenderedImage img = getAggregateImage(true);
		if (img != null) {
			if (img.getWidth() != width || img.getHeight() != height) {
				img = CoreImage.create(img).scale(width, height).getAwt();
			}
		}
		return img;
	}

	/**
	 * @param refresh
	 * @return
	 */
	public RenderedImage getAggregateImage(boolean postProcess) {

		/* cache injection */

		String key = getId() + "!" + getWidth() + "x" + getHeight();
		Object group = entity.getRoot();
		if (CACHE_ALL_IMAGES) {
			RenderedImage cached = (RenderedImage) SimpleCache.getData(group,
					key);
			if (cached != null) {
				return cached;
			}
		}

		RenderedImage currentAggregateImage = computeAggregateImage(postProcess);
		if (CACHE_ALL_IMAGES)
			SimpleCache.cache(group, key, currentAggregateImage);

		return currentAggregateImage;
	}

	protected void stretchImage(CoreImage image) {
		List<BasicImageLayer> layers = getImageLayers();
		if (!layers.isEmpty()) {
			BasicImageLayer layer = layers.get(0);
			boolean stretch = new Boolean(layer.getImageLayer().getAttribute(
					ThemeTag.ATTR_STRETCH));
			if (stretch) {
				int scaleMode = CoreImage.STRETCH;
				if (previewElement != null) {
					String s = previewElement
							.getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_SCALE_MODE);
					if (PreviewTagConstants.ATTR_SCALE_BEST.equals(s)) {
						scaleMode = CoreImage.SCALE_TO_BEST;
					} else if (PreviewTagConstants.ATTR_ASPECT_RATIO.equals(s)
							|| PreviewTagConstants.ATTR_SCALE_FIT.equals(s)) {
						scaleMode = CoreImage.SCALE_TO_FIT;
					} else if (PreviewTagConstants.ATTR_SCALE_DOWN_FIT
							.equals(s)) {
						scaleMode = CoreImage.SCALE_DOWN_TO_FIT;
					}
				} else {
					scaleMode = CoreImage.SCALE_TO_FIT;
				}
				image.stretch(getWidth(), getHeight(), scaleMode).getAwt();
			}
		}
	}

	/**
	 *  postProcess seems to be for processing post the processing of 11 and 9 piece elements.
	 * @param postProcess
	 * @return image to be shown in components view.
	 */
	protected RenderedImage computeAggregateImage(boolean postProcess) {
		if (getThemeGraphics() != null) {
			ThemeGraphic tg = getThemeGraphics();
			if (isSound() || isColour() || isPlaceHolderForEmbeddedFile()) {
				try {
					return tg.getProcessedImage(entity, getElementLayout(),
							entity.getToolBox().SoftMask);
				} catch (ThemeException e1) {
					e1.printStackTrace();
				}
			}
		}
		CoreImage img = CoreImage.create();
		List<BasicImageLayer> layers = getImageLayers();
		if (!layers.isEmpty()) {
			BasicImageLayer layer = layers.get(0);
			img.init(layer.getCurrentImage(previewElement));
			if (img != null) {
				stretchImage(img);
			}
		}
		if (img == null) {
			// a transparent image instead of white
			return CoreImage.create().getBlankImage(getWidth(), getHeight(),
					BASE_COLOR, opacity, 4);
		}

		if (previewElement != null
				&& previewElement.getRotate() != null
				&& previewElement.getRotate().equalsIgnoreCase(
						PreviewTagConstants.ATTR_ROTATE_VALUE_180)) {
			img.rotate(180);
		}

		if (previewElement != null
				&& previewElement.getAttribute().get(
						ThemeTag.ATTR_SHAPE_LINK_ID) != null) {
			// image must be obtained from another entity
			String entityId = (String) previewElement.getAttribute().get(
					ThemeTag.ATTR_SHAPE_LINK_ID);
			Theme theme = (Theme) entity.getRoot();
			Element el = theme.getElementWithId(entityId);
			try {
				BasicEntityImage entity = createEntityImage(el);
				CoreImage entityImg = CoreImage.create(entity
						.getAggregateImage());

				if (previewElement.getRotate() != null
						&& previewElement.getRotate().equalsIgnoreCase(
								PreviewTagConstants.ATTR_ROTATE_VALUE_180)) {
					entityImg.rotate(180);
				}
				stretchImage(entityImg);

				entityImg.applyBackground(Color.WHITE);

				CoreImage bgMaskImg = img.copy().applyBackground(Color.WHITE);

				img = entityImg.applyMask(bgMaskImg, true);
			} catch (ThemeException e) {
				e.printStackTrace();
			}
		}

		// colour processing for indication items - defined as (SVG = shape) +
		// colour
		if (previewElement != null)
			try {
				String colourId = previewElement.getAttributeValue("colourId");
				if (colourId != null) {
					Theme root = (Theme) entity.getRoot();
					SkinnableEntity c = root.getSkinnableEntity(colourId);
					if (c != null
							&& c.getThemeGraphic() instanceof ColourGraphic) {
						String colour = ((ColourGraphic) c.getThemeGraphic())
								.getColour();
						int red = Integer.parseInt(colour.substring(2, 4), 16);
						int green = Integer
								.parseInt(colour.substring(4, 6), 16);
						int blue = Integer.parseInt(colour.substring(6, 8), 16);
						Color cc = new Color(red, green, blue);
						CoreImage mask = img.copy().extractMask();
						return CoreImage.create(
								CoreImage.getBlank3Image(mask.getWidth(), mask
										.getHeight(), cc))
								.applyMask(mask, true).getAwt();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		return img.getAwt();
	}

	public boolean isColour() {
		return entity.isEntityType().equalsIgnoreCase(ThemeTag.ELEMENT_COLOUR);
	}

	protected RenderedImage processNormal(RenderedImage upper,
			RenderedImage lower, BasicImageLayer lData, int entx, int enty,
			int imagex, int imagey) {
		BufferedImage layer0 = CoreImage.getBufferedImage(lower);
		Graphics2D g = (Graphics2D) layer0.getGraphics();
		g.drawRenderedImage(upper, CoreImage.TRANSFORM_ORIGIN);
		g.dispose();
		return layer0;
	}

	/*
	 * used in animation - retrieves animated image @param timeOffset @return
	 */
	private RenderedImage computeAggregateImage(TimingModel timing, long time,
			boolean preview) {

		isPreview = preview;
		try {
			for (BasicImageLayer layer : getImageLayers()) {
				layer.setAnimationTime(timing, time);
			}
			return computeAggregateImage(false);
		} finally {
			isPreview = false;
			for (BasicImageLayer layer : getImageLayers()) {
				layer.setAnimationTime(timing, 0);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getAggregateImage(com.nokia.tools.media.utils.layers.TimingModel,
	 *      long, boolean)
	 */
	public RenderedImage getAggregateImage(TimingModel timing, long time,
			boolean preview) {
		if (animationStarted) {
			return computeAggregateImage(timing, time, preview);
		} else {
			throw new RuntimeException("animation not started!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getAnimationDuration()
	 */
	public long getAnimationDuration() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getAnimationDuration(com.nokia.tools.media.utils.layers.TimingModel)
	 */
	public long getAnimationDuration(TimingModel timingType) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getAnimationDuration(com.nokia.tools.media.utils.layers.TimeSpan)
	 */
	public long getAnimationDuration(TimeSpan span) {
		return 0;
	}

	/**
	 * Returns another instance scaled to given dimensions
	 */
	public IImage getAnotherInstance(int w, int h) {
		try {
			return new BasicEntityImage(entity, getThemeGraphics(),
					previewElement, w, h, opacity);
		} catch (ThemeException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getHeight() {
		// initialize layout
		getElementLayout();
		return height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getId()
	 */
	public String getId() {
		if (entity != null)
			return entity.getId();
		else
			return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getLayer(int)
	 */
	public ILayer getLayer(int index) {
		List<BasicImageLayer> imageLayers = getImageLayers();
		if (index < 0 || index >= imageLayers.size()) {
			return null;
		}
		return imageLayers.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getLayer(java.lang.String)
	 */
	public ILayer getLayer(String name) {
		for (int i = 0; i < getImageLayers().size(); i++) {
			BasicImageLayer ld = getImageLayers().get(i);
			if (ld.getName().equals(name))
				return ld;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getLayerCount()
	 */
	public int getLayerCount() {
		if (getImageLayers() == null) {
			System.out.println(getId() + " is null layers!");
		}
		return getImageLayers().size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getLayers()
	 */
	public List<ILayer> getLayers() {
		ArrayList<ILayer> xx = new ArrayList<ILayer>();
		xx.addAll(getImageLayers());
		return xx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getMaximumLayerCount()
	 */
	public int getMaximumLayerCount() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getName()
	 */
	public String getName() {
		String name = entity.getAttributeValue(ThemeTag.ATTR_NAME);
		return name == null ? getId() : name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getPartInstances()
	 */
	public List<IImage> getPartInstances() throws Exception {
		return Collections.EMPTY_LIST;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getPartType()
	 */
	public String getPartType() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getRealTimeTimeModelTimeLineProvider()
	 */
	public ITreeTimeLineDataProvider getRealTimeTimeModelTimeLineProvider() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getRelativeTimeModelTimeLineProvider(com.nokia.tools.media.utils.layers.TimeSpan)
	 */
	public ITreeTimeLineDataProvider getRelativeTimeModelTimeLineProvider(
			TimeSpan timeSpan) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getSelectedLayerCount()
	 */
	public int getSelectedLayerCount() {
		int i = 0;
		for (ILayer x : getImageLayers()) {
			if (x.isEnabled())
				i++;
		}
		return i;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getSelectedLayers()
	 */
	public List<ILayer> getSelectedLayers() {
		ArrayList<ILayer> xx = new ArrayList<ILayer>();
		if (getImageLayers() != null) {
			for (int i = 0; i < getImageLayers().size(); i++) {
				if (getImageLayers().get(i).isEnabled()) {
					xx.add(getImageLayers().get(i));
				}
			}
		}
		return xx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getWidth()
	 */
	public int getWidth() {
		// initialize layout
		getElementLayout();
		return width;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#isAnimated()
	 */
	public boolean isAnimated() {
		for (ILayer l : getImageLayers()) {
			if (((BasicImageLayer) l).isAnimated())
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#isAnimatedFor(com.nokia.tools.media.utils.layers.TimingModel)
	 */
	public boolean isAnimatedFor(TimingModel timingType) {
		for (ILayer l : getImageLayers()) {
			if (l.isAnimatedFor(timingType))
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#isAnimationStarted()
	 */
	public boolean isAnimationStarted() {
		return animationStarted;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#isNinePiece()
	 */
	public boolean isNinePiece() {
		return false;
	}

	public boolean isMultiPiece() {		
		return false;
	}
	
	public int getPartCount() {		
		return 1;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#isPart()
	 */
	public boolean isPart() {
		return entity instanceof Part;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#removeLayer(com.nokia.tools.media.utils.layers.ILayer)
	 */
	public void removeLayer(ILayer layer) {
		if (layer == null)
			return;
		if (getImageLayers().remove(layer)) {
			getThemeGraphics().getImageLayers().remove(
					((BasicImageLayer) layer).getImageLayer());
			// remove self listener
			layer.removePropertyChangeListener(this);

			// fire structure change event
			propsup.firePropertyChange(PROPERTY_STRUCTURE_CHANGE, null, null);
			propsup.firePropertyChange(PROPERTY_STATE, null, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener ps) {
		propsup.removePropertyChangeListener(ps);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#setAnimationTimeLocation(long)
	 */
	public void setAnimationStartLocation(long time) {
		for (ILayer l : getImageLayers()) {
			((BasicImageLayer) l).setAnimationStartLocation(time);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#startAnimation()
	 */
	public synchronized void startAnimation() {
		if (!animationStarted) {
			for (ILayer l : getImageLayers()) {
				((BasicImageLayer) l).startAnimation();
			}
			animationStarted = true;
		} else
			throw new RuntimeException("Animation already in progress");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#supportsAnimationTiming(com.nokia.tools.media.utils.layers.TimingModel)
	 */
	public boolean supportsAnimationTiming(TimingModel timingModel) {
		String entityType = getEntity().isEntityType();
		if (ThemeTag.ELEMENT_MORPHING.equalsIgnoreCase(entityType)) {
			return TimingModel.Relative == timingModel;
		} else if (ThemeTag.ELEMENT_FASTANIMATION.equalsIgnoreCase(entityType)) {
			return TimingModel.RealTime == timingModel;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#updateControlPointsFiltering(com.nokia.tools.media.utils.layers.TimingModel,
	 *      com.nokia.tools.media.utils.layers.TimeSpan)
	 */
	public void updateControlPointsFiltering(TimingModel timeModel,
			TimeSpan span) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof ILayer) {
			if (ILayer.PROPERTY_IMAGE.equals(evt.getPropertyName())) {
				// layer image changed, fire aggregate image change
				propsup.firePropertyChange(PROPERTY_STATE, null, null);
				propsup.firePropertyChange(PROPERTY_IMAGE, null, null);
			} else if (ILayer.PROPERTY_STATE.equals(evt.getPropertyName())) {
				// layer changed it state
				propsup.firePropertyChange(PROPERTY_STATE, null, null);
			} else if (ILayer.PROPERTY_STRUCTURE_CHANGE.equals(evt
					.getPropertyName())) {
				// structure change - propagate
				propsup.firePropertyChange(PROPERTY_STATE, null, null);
				propsup.firePropertyChange(PROPERTY_STRUCTURE_CHANGE, null,
						null);
			} else if (ILayer.PROPERTY_ANIMATION_STATE.equals(evt
					.getPropertyName())) {
				// structure change - propagate
				propsup
						.firePropertyChange(PROPERTY_ANIMATION_STATE, null,
								null);
			} else if (ILayer.PROPERTY_ANIMATION_STATE.equals(evt
					.getPropertyName())) {
				// structure change - propagate
				propsup
						.firePropertyChange(PROPERTY_ANIMATION_STATE, null,
								null);
			} else if (ILayer.PROPERTY_EFFECT_ORDER.equals(evt
					.getPropertyName())) {
				// structure change - propagate
				propsup.firePropertyChange(ILayer.PROPERTY_EFFECT_ORDER, null,
						null);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IPasteTargetAdapter.class) {
			return new BasicCopyPasteSupport(getEntity())
					.getPasteTargetAdapter(this);
		}
		return null;
	}

	/**
	 * @return
	 */
	protected List<BasicImageLayer> getImageLayers() {
		if (imageLayers == null)
			lazyInit();
		return imageLayers;
	}

	/*
	 * lazy init of layers, parsed only when really used
	 */
	private void lazyInit() {

		/* child creation */
		imageLayers = new ArrayList<BasicImageLayer>();
		try {
			Theme theme = (Theme) entity.getRoot();
			if (getThemeGraphics() != null) {
				List imls = getThemeGraphics().getImageLayers();
				if (imls != null) {
					for (int i = 0; i < imls.size(); i++) {
						ImageLayer iml = (ImageLayer) imls.get(i);
						/* check name */
						if (iml.getAttribute(ThemeTag.ATTR_NAME) == null) {
							String name = ThemeTag.ELEMENT_LAYER + i;
							if (getThemeGraphics().getImageLayer(name) != null)
								name += "_2";
							iml.setAttribute(ThemeTag.ATTR_NAME, name);
						}

						BasicImageLayer ldata = createImageLayer(iml, theme);

						ldata.addPropertyListener(this);
						imageLayers.add(ldata);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected BasicImageLayer createImageLayer(ImageLayer iml, Theme theme)
			throws ThemeException {
		return new BasicImageLayer(iml, this, theme);
	}

	public SkinnableEntity getEntity() {
		return entity;
	}

	public PreviewElement getPreviewElement() {
		return previewElement;
	}

	public synchronized ThemeGraphic getThemeGraphics() {
		if (!graphicsInitialized) {
			initThemeGraphics();
			graphicsInitialized = true;
		}
		return themeGraphics;
	}

	/**
	 * return parts bounds relative to its parent
	 * 
	 * @return
	 */
	public Rectangle getBounds() {
		return bounds;
	}

	public synchronized Layout getElementLayout() {
		if (!layoutInitialized) {
			initElementLayout();
			layoutInitialized = true;
		}
		return elementLayout;
	}

	public int getIndexOf(BasicImageLayer ld) {
		return getImageLayers().indexOf(ld);
	}

	public boolean isSound() {
		return entity.isEntityType().equalsIgnoreCase(ThemeTag.ELEMENT_SOUND);
	}

	public boolean isPlaceHolderForEmbeddedFile() {
		return entity.isEntityType().equalsIgnoreCase(
				ThemeTag.ELEMENT_EMBED_FILE);
	}

	public int getOpacity() {
		return opacity;
	}

	public void createThemeGraphics() throws ThemeException {
		if (themeGraphics == null) {
			themeGraphics = entity.getThemeGraphic();
		}
	}

	public List getEntityProperties() {
		return entity.getProperties();
	}

	public boolean hasEntityProperty(String name) {
		if (entity != null && entity.getProperties() != null && name != null)
			for (Object p : entity.getProperties()) {
				if (p instanceof Map) {
					Iterator i = ((Map) p).values().iterator();
					if (i.hasNext() && name.equals(i.next()))
						return true;
				}
			}
		return false;
	}

	public boolean isMultiLayer() {
		return entity.getToolBox().multipleLayersSupport;
	}

	public boolean isPreview() {
		return isPreview;
	}

	public boolean isMultilayerSupport() {
		return entity.getToolBox().multipleLayersSupport;
	}

	/*
	 * Save images and updates ThemeGraphics
	 */
	public ThemeGraphic getThemeGraphics(boolean makePathsAbsolute)
			throws Exception {

		if (!makePathsAbsolute)
			return getThemeGraphics();

		Theme theme = (Theme) entity.getRoot();
		String themeDir = theme.getThemeDir();
		try {
			themeDir = new File(themeDir).getCanonicalPath();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (BasicImageLayer ld : getImageLayers()) {
			try {
				// resolve pasted images - abs paths
				if (ld.hasImage() && !ld.isBackground()) {
					String filename = ld.getFileName(true);
					ld.getImageLayer().setAttribute(ThemeTag.FILE_NAME,
							filename);
				}
				// resolve pasted masks - abs paths
				if (ld.hasMask()) {
					String filename = ld.getMaskFileName(true);
					ld
							.getImageLayer()
							.setAttribute(
									entity.getToolBox().SoftMask ? ThemeTag.ATTR_SOFTMASK
											: ThemeTag.ATTR_HARDMASK, filename);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return getThemeGraphics();
	}

	public ThemeGraphic getSavedThemeGraphics(boolean forceAbsolute)
			throws Exception {
		return getSavedThemeGraphics(forceAbsolute, true);
	}

	/**
	 * 
	 * @param forceAbsolute
	 * @param replaceFilenameWithId resolved paths will be formed based on id of
	 *            the element. if set to false, original filename is to be used
	 * @return
	 * @throws Exception
	 */
	public ThemeGraphic getSavedThemeGraphics(boolean forceAbsolute,
			boolean replaceFilenameWithId) throws Exception {
		try {
			propagateChangesToModel();
		} catch (ThemeException e1) {
			e1.printStackTrace();
		}
		Theme theme = (Theme) entity.getRoot();

		String themeDir = theme.getThemeDir();
		try {
			themeDir = new File(themeDir).getCanonicalPath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String themeName = theme.getAttributeValue("name1");
		String tempDir = FileUtils.getTemporaryDirectory() + File.separator
				+ themeName;
		File tempDirFile = new File(tempDir);
		if (!tempDirFile.exists()) {
			tempDirFile.mkdir();
		}
		// mark for delete on app exit
		FileUtils.addForCleanup(tempDirFile);

		resolveImages(theme, tempDir, themeDir, forceAbsolute,
				replaceFilenameWithId);

		// fire structure change event
		propsup.firePropertyChange(PROPERTY_STRUCTURE_CHANGE, null, null);
		propsup.firePropertyChange(PROPERTY_STATE, null, null);

		return getThemeGraphics();
	}

	/**
	 * update model classes with changes that are not updated on-the-fly
	 * 
	 * @throws ThemeException
	 */
	public void propagateChangesToModel() throws ThemeException {
		for (int i = 0; i < getImageLayers().size(); i++) {
			((BasicImageLayer) getLayer(i)).propagateChangesToModel();
		}
	}

	private void markForDeleteOnExit(String path) {
		try {
			File file = new File(path);
			file = file.getAbsoluteFile();
			FileUtils.addForCleanup(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Helper method, forms the name of the file. used internally from
	 * resolveImages()
	 * 
	 * @param layer
	 * @param replaceFilenameWithId
	 * @return - only name of the file
	 */
	private String getResolvedFileName(BasicImageLayer ld,
			boolean replaceFilenameWithId, String dir, String sufix) {
		if (replaceFilenameWithId || ld.isImageInMemory()) {
			return FileUtils.generateUniqueFileName(entity.getId() + sufix,
					dir, IFileConstants.FILE_EXT_BMP);
		}
		String extension = new Path(ld.getFileName(false)).getFileExtension();
		String filename = new Path(ld.getFileName(false)).removeFileExtension()
				.lastSegment();

		return FileUtils.generateUniqueFileName(filename + sufix, dir,
				extension);

	}

	/**
	 * resolve changed images and saves them to temporary diractory.
	 * 
	 * @param theme
	 * @param tmpDir
	 * @param themeDir
	 * @param forceAbsolute
	 */
	protected void resolveImages(Theme theme, String tmpDir, String themeDir,
			boolean forceAbsolute, boolean replaceFilenameWithId) {

		for (BasicImageLayer ld : getImageLayers()) {
			try {

				/*
				 * resolve masks immediately for bitmaps, in case there is no
				 * mask, create it
				 */
				if (ld.isBitmapImage() && ld.supportMask() && !ld.hasMask()) {
					// get mask from bitmap
					try {
						ld.pasteMask(CoreImage.create(ld.getCurrentImage())
								.extractMask(entity.getToolBox().SoftMask)
								.getAwt());
					} catch (Exception es) {
						es.printStackTrace();
					}
				}

				/* resolve bitmaps and inmemory images */
				if (ld.isBitmapImage() || ld.isImageInMemory()) {
					// save image under tmp name to tmp dir
					/*if (!ld.isBitmapImage())
						Thread.dumpStack();*/
					if (ld.hasImageFileNameChanged() || ld.isImageInMemory()
							|| forceAbsolute) {
						String tmpName = getResolvedFileName(ld,
								replaceFilenameWithId, tmpDir,
								(replaceFilenameWithId) ? "_" + ld.getName()
										: "");

						String absPath = tmpDir + File.separator + tmpName;
						if (ld.isImageInMemory())
							CoreImage.create(ld.getCurrentImage()).save(
									CoreImage.TYPE_BMP,
									FileUtils.createFileWithExtension(absPath,
											IFileConstants.FILE_EXT_BMP));
						else
							FileUtils.copyFile(ld.getFileName(true), absPath);

						// notify layerData that image was saved
						ld.notifyImageSaved(absPath);
						markForDeleteOnExit(absPath);
					}
				}

				/* masks for bitmaps */
				if ((ld.isBitmapImage() && ld.hasMask()) || ld.isMaskInMemory()) {
					// save image under tmp name to tmp dir
					// mask is bitmap, with id enough for now...
					String tmpName = FileUtils.generateUniqueFileName(entity
							.getId()
							+ "_" + ld.getName() + MASK_SUFFIX, tmpDir,
							IFileConstants.FILE_EXT_BMP);

					String absPath = tmpDir + File.separator + tmpName;
					// masks must be saved as BMP!
					CoreImage.create(ld.getCurrentMask()).save(
							CoreImage.TYPE_BMP, new File(absPath));

					// notify layerData that image was saved
					ld.notifyMaskSaved(absPath);
					markForDeleteOnExit(absPath);
				}

				if (ld.isBitmapImage())
					continue;

				/* handling for svg */

				// resolve pasted images - abs paths
				if (ld.hasImageFileNameChanged()
						|| (forceAbsolute && ld.hasImage() && !ld
								.isBackground())) {
					String curExt = FileUtils.getExtension(ld
							.getFileName(false));

					if (curExt != null) {
						// copy old file

						String tmpName = (isPlaceHolderForEmbeddedFile() || !replaceFilenameWithId) ? FileUtils
								.generateUniqueFileName(new Path(ld
										.getFileName(false))
										.removeFileExtension().lastSegment(),
										tmpDir, curExt)
								: FileUtils.generateUniqueFileName(entity
										.getId()
										+ (isSound() ? ""
												: ("_" + ld.getName())),
										tmpDir, curExt);

						String absPath = tmpDir + File.separator + tmpName;

						String sourcePath = ld.getFileName(false);
						if (!FileUtils.isAbsolutePath(sourcePath))
							sourcePath = themeDir + File.separator + sourcePath;
						FileUtils.copyFile(new File(sourcePath), new File(
								absPath));

						// notify layerData that image was saved
						ld.notifyImageSaved(absPath);
						markForDeleteOnExit(absPath);
					}
				}
				// resolve pasted masks - abs paths
				if (ld.hasMaskFileNameChanged()
						|| (forceAbsolute && ld.hasMask() && !ld.isBackground())) {
					String curExt = FileUtils.getExtension(ld
							.getMaskFileName(false));

					if (curExt != null) {
						// copy old file
						String tmpName = FileUtils.generateUniqueFileName(
								entity.getId() + "_" + ld.getName()
										+ MASK_SUFFIX, tmpDir, curExt);
						String absPath = tmpDir + File.separator + tmpName;

						String sourcePath = ld.getMaskFileName(false);
						if (!FileUtils.isAbsolutePath(sourcePath))
							sourcePath = themeDir + File.separator + sourcePath;
						FileUtils.copyFile(new File(sourcePath), new File(
								absPath));

						// notify layerData that image was saved
						ld.notifyMaskSaved(absPath);
						markForDeleteOnExit(absPath);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setElementLayout(Layout layout) {
		elementLayout = layout;
	}

	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	/*
	 * next selected layer
	 */
	public BasicImageLayer findNextLayer(int count) {
		count++;
		while (count < getImageLayers().size()) {
			if (getImageLayers().get(count).isEnabled())
				return getImageLayers().get(count);
			count++;
		}
		return null;
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		return propsup;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#isBackground()
	 */
	public boolean isBackground() {
		return false;
	}

	/* (non-Javadoc)
     * @see com.nokia.tools.media.utils.layers.IImage#isElevenPiece()
     */
    public boolean isElevenPiece() {
	    return false;
    }
    public boolean isThreePiece() {
	    return false;
    }  
	/**
	 * This is for specifying if the image is supporting 11 pic
	 */
	public boolean supportsElevenPiece() {
		return entity != null && entity.getChildren() != null
				&& entity.getChildren().size() == 11;
	}
	/**
	 * This is for specifying if the image is supporting 3 pic
	 */
	public boolean supportsThreePiece() {
		return entity != null && entity.getChildren() != null
				&& entity.getChildren().size() == 3;
	}
	
	public boolean supportsMultiPiece() {
		return entity != null && entity.getChildren() != null
				&&  MultiPieceManager.isMultiPiece(entity.getChildren().size());
	}
}
