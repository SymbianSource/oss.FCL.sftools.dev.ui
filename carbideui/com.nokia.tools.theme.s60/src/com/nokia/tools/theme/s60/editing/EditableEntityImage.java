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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.layers.EffectTypes;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.layers.IPasteTargetAdapter;
import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.ITreeTimeLineDataProvider;
import com.nokia.tools.platform.layout.ComponentInfo;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.BitmapProperties;
import com.nokia.tools.platform.theme.ColourGraphic;
import com.nokia.tools.platform.theme.EffectConstants;
import com.nokia.tools.platform.theme.Element;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.Part;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeConstants;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.platform.theme.preview.PreviewImage;
import com.nokia.tools.platform.theme.preview.PreviewTagConstants;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.core.MultiPieceManager;
import com.nokia.tools.theme.editing.BasicEntityImage;
import com.nokia.tools.theme.editing.BasicImageLayer;
import com.nokia.tools.theme.s60.S60ThemePlugin;
import com.nokia.tools.theme.s60.cstore.ComponentStore;
import com.nokia.tools.theme.s60.editing.anim.CPMFilterWrapper;
import com.nokia.tools.theme.s60.editing.anim.EffectAvailabilityParser;
import com.nokia.tools.theme.s60.editing.anim.EffectAvailabilityParser.LayerConstraints;
import com.nokia.tools.theme.s60.editing.providers.realtime.RealTimeTreeDataProvider;
import com.nokia.tools.theme.s60.editing.providers.relative.RelativeTimeTreeDataProvider;
import com.nokia.tools.theme.s60.editing.utils.SkinnableEntityCopyPasteSupport;
import com.nokia.tools.theme.s60.effects.EffectObject;
import com.nokia.tools.theme.s60.effects.EffectObjectUtils;
import com.nokia.tools.theme.s60.effects.EffectParameter;
import com.nokia.tools.theme.s60.effects.ImageProcessor;
import com.nokia.tools.theme.s60.effects.OnLayerEffects;
import com.nokia.tools.theme.s60.effects.imaging.Colorize;
import com.nokia.tools.theme.s60.internal.utilities.TSDataUtilities;
import com.nokia.tools.theme.s60.model.MorphedGraphic;
import com.nokia.tools.theme.s60.model.S60Theme;

/**
 * Editing layer root class, enables to manipulate properties of
 * SkinnableEntity's ThemeGraphics. After editing operations, updateModel() is
 * to be called in order to propagate all changes to the ThemeGraphics Structure
 * changes are propagated on-the-fly to underlying model, properties also.
 * Implements IImage interface, support animations - edit animation properties
 * and animation preview.
 */

public class EditableEntityImage extends BasicEntityImage {

	private static final Colorize COLORIZE = new Colorize();

	public static String DRAFT_SUFFIX = "_draft";

	public static String DEFAULT_IMAGE_EXTENSION = "png";

	public static String BMP_EXT = "bmp";

	private int defaultWidth = 10;

	private int defaultHeight = 10;

	public static final boolean DEBUG = true;

	private RenderedImage forceBgImg;

	private List<IImage> partsInstances = null;

	public EditableEntityImage(SkinnableEntity entity) throws ThemeException {
		super(entity, null);
	}

	public EditableEntityImage(SkinnableEntity entity,
			PreviewElement previewElement) throws ThemeException {
		super(entity, previewElement);
	}

	public EditableEntityImage(SkinnableEntity element,
			PreviewElement previewElement, int preferedWidth, int preferedHeight)
			throws ThemeException {
		super(element, previewElement, preferedWidth, preferedHeight);
	}

	public EditableEntityImage(SkinnableEntity entity, ThemeGraphic grahics,
			PreviewElement previewElement, int prefWidth, int prefHeight)
			throws ThemeException {
		super(entity, grahics, previewElement, prefWidth, prefHeight);
	}

	public EditableEntityImage(SkinnableEntity entity, ThemeGraphic grahics,
			PreviewElement previewElement, int prefWidth, int prefHeight,
			int opacity) throws ThemeException {
		super(entity, grahics, previewElement, prefWidth, prefHeight, opacity);
	}

	public EditableEntityImage(SkinnableEntity entity, ThemeGraphic grahics,
			PreviewElement previewElement, ComponentInfo cInfo,
			PreviewImage screen, int prefWidth, int prefHeight, int opacity)
			throws ThemeException {
		super(entity, grahics, previewElement, cInfo, screen, prefWidth,
				prefHeight, opacity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.BasicEntityImage#initThemeGraphics()
	 */
	protected void initThemeGraphics() {
		if (null == prefGraphics) {
			try {
				if (ComponentStore.isComponentStoreElement(getId())) {
					/*
					 * we don't want to call getThemeGraphic() because it
					 * created default graphics if not present and store
					 * elements have it's own id, not contained in theme
					 * temnplate -> exception
					 */
					prefGraphics = getEntity().getActualThemeGraphic();
				} else {
					prefGraphics = getEntity().getThemeGraphic(
							getPreviewElement() == null ? null
									: getPreviewElement().getDisplay(),
							prefScreen, prefComponent);
				}
			} catch (Exception e) {
				S60ThemePlugin.error(e);
			}
		}

		try {
			if (prefGraphics != null)
				setThemeGraphics((ThemeGraphic) prefGraphics.clone());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.BasicEntityImage#initElementLayout()
	 */
	@Override
	protected void initElementLayout() {
		/*
		 * because component store elements has random generated ID's, layout
		 * computation for it has no sense
		 */
		if (!ComponentStore.isComponentStoreElement(getId())) {
			super.initElementLayout();
		} else

			initDimension();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.theme.s60.editing.layers.api.IImage#changeLayerOrder(
	 * com.nokia.tools.theme.s60.editing.layers.api.ILayer, int)
	 */
	public void changeLayerOrder(ILayer ilayer, int positionChange)
			throws Exception {
		EditableImageLayer layer = (EditableImageLayer) ilayer;
		int curIndex = getIndexOf((EditableImageLayer) layer);
		int newIndex = curIndex + positionChange;
		if (newIndex >= 0 && newIndex < getImageLayers().size()) {
			getImageLayers().remove(curIndex);
			getThemeGraphics().getImageLayers().remove(layer.getImageLayer());
			if (newIndex < curIndex) {
				getImageLayers().add(newIndex, layer);
				getThemeGraphics().getImageLayers().add(newIndex,
						layer.getImageLayer());
			} else {
				getImageLayers().add(newIndex, layer);
				getThemeGraphics().getImageLayers().add(newIndex,
						layer.getImageLayer());
			}

			/* validate layer effects on all layers */
			validateLayerEffects();

		} else {
			throw new Exception("Invalid index: " + newIndex);
		}

		propsup.firePropertyChange(PROPERTY_STATE, null, null);
		propsup.firePropertyChange(PROPERTY_STRUCTURE_CHANGE, null, null);
	}

	/**
	 * removes invalid effects and effect combinations with respect to current
	 * layer setup
	 */
	public void validateLayerEffects() {
		// 1. check that 1.st layer has disable between-layer
		List<ILayer> list = getSelectedLayers();
		boolean first = true;
		for (ILayer ll : list) {
			if (first) {
				first = false;
				((EditableImageLayer) ll)
						.disableEffects(EffectTypes.BETWEEN_LAYER_EFFECTS);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getAnimationDuration()
	 */
	public long getAnimationDuration() {
		long max = 0;
		for (ILayer l : getImageLayers()) {
			long m = ((EditableImageLayer) l).getAnimationDuration();
			if (m > max)
				max = m;
		}
		return max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.media.utils.layers.IImage#getAnimationDuration(com.nokia
	 * .tools.media.utils.layers.TimingModel)
	 */
	public long getAnimationDuration(TimingModel timingType) {
		long max = 0;
		for (ILayer l : getImageLayers()) {
			long m = ((EditableImageLayer) l).getAnimationDuration(timingType);
			if (m > max)
				max = m;
		}
		return max;
	}

	public ITreeTimeLineDataProvider getRealTimeTimeModelTimeLineProvider() {
		return new RealTimeTreeDataProvider(this);
	}

	public ITreeTimeLineDataProvider getRelativeTimeModelTimeLineProvider(
			TimeSpan span) {
		return new RelativeTimeTreeDataProvider(this, span);
	}

	/*
	 * Save images and updates ThemeGraphics
	 */
	public ThemeGraphic getSavedThemeGraphics(boolean forceAbsolute)
			throws Exception {
		ThemeGraphic themeGraphics = getThemeGraphics();
		if (themeGraphics instanceof MorphedGraphic) {
			{
				((MorphedGraphic) themeGraphics).removeUnusedValueModels();
				((MorphedGraphic) themeGraphics).removeUnusedTimeModels();
			}
		}
		return super.getSavedThemeGraphics(forceAbsolute);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.theme.editing.BasicEntityImage#computeAggregateImage(
	 * boolean)
	 */
	protected RenderedImage computeAggregateImage(boolean postProcess) {

		// if TG is null, compute 3 piece or 9-piece or 11-piece graphics
		boolean hasParts = this.getEntity().hasChildNodes();
		int partCount = 0;
		if (hasParts) {
			if (getEntity().getDesignRectangles() != null) {
				partCount = getEntity().getDesignRectangles().size();
			}
		}

		if (MultiPieceManager.isMultiPiece(getEntity().getCurrentProperty())
				|| MultiPieceManager.isMultiPiece(partCount)) {
			if (!isConvertedMultiPiece()) {
				return computeAggregateImageMultiPiece(postProcess);
			}
		}

		ThemeGraphic themeGraphics = getThemeGraphics();
		SkinnableEntity entity = getEntity();
		Layout elementLayout = getElementLayout();
		PreviewElement previewElement = getPreviewElement();

		if (getEntity().getParent() != null) {
			if (getEntity().getParent().getDesignRectangles() != null) {
				EditableEntityImage eeiParent = null;
				try {
					eeiParent = new EditableEntityImage(entity.getParent()
							.getSkinnableEntity());

				} catch (ThemeException e1) {

					e1.printStackTrace();
				}
				int parentPartCount = getEntity().getParent()
						.getDesignRectangles().size();
				if (MultiPieceManager.isMultiPiece(parentPartCount)) {
					try {
						return themeGraphics.getProcessedImage(entity
								.getParent().getSkinnableEntity(),
								elementLayout, entity.getToolBox().SoftMask);

					} catch (ThemeException e) {

						e.printStackTrace();
						return null;
					}
				}
			}
		}

		int width = getWidth();
		int height = getHeight();

		if (themeGraphics != null) {
			ThemeGraphic tg = themeGraphics;
			if (isSound() || isColour() || isPlaceHolderForEmbeddedFile()) {
				try {
					return tg.getProcessedImage(entity, elementLayout, entity
							.getToolBox().SoftMask);
				} catch (ThemeException e1) {
					e1.printStackTrace();
				}
			}
		}

		boolean multiLayerElement = entity.getToolBox().multipleLayersSupport;
		if (!multiLayerElement && ComponentStore.isMultilayerElement(getId()))
			multiLayerElement = true;

		RenderedImage previousImage = null;
		RenderedImage img = null;

		int count = 0;
		Color bgColor = BASE_COLOR;

		boolean isComponentStore = ComponentStore
				.isComponentStoreElement(getId());

		/**
		 * semi-transparent means that if element has non-ng non-empty layer, is
		 * opaque, otherwise transparent
		 */
		List<ILayer> selLayers = getSelectedLayers();
		boolean semiTransparent = false;
		{
			if (multiLayerElement) {
				// find out if element has only BG layer, or it is have another
				// non-empty layer
				for (ILayer l : selLayers) {
					if (!l.isBackground() && !((BasicImageLayer) l).isEmpty()) {
						semiTransparent = true;
						break;
					}
				}
			}
		}

		// find out if image has Alpha blending defined
		if (selLayers.size() > 0 && isMultiLayer()) {
			for (ILayer first : selLayers)
				if (first.getEffect(IMediaConstants.ALPHABLENDING).isSelected()) {
					semiTransparent = true;
					bgColor = Color.BLACK;
					break;
				}
		}

		// create base transparent image
		previousImage = CoreImage.create().getBlankImage(Math.max(1, width),
				Math.max(1, height), BASE_COLOR, 0, 4);

		bgColor = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor
				.getBlue(), semiTransparent ? 255 : getOpacity());

		for (BasicImageLayer layer : getImageLayers()) {

			EditableImageLayer lData = (EditableImageLayer) layer;

			if (!lData.isEnabled()) {
				count++;
				continue;
			}
			if (!lData.hasImage()
					&& !lData.isEffectSelected(EffectConstants.APPLYCOLOR)) {
				// empty layer
				count++;
				continue;
			}

			/*
			 * when in animation preview, filter out elements that are not
			 * selected for preview
			 */
			if (isPreview() && !lData.isAnimatedInPreview())
				continue;

			if (forceBgImg != null && lData.isBackground()) {
				img = forceBgImg;
				forceBgImg = null;
			} else {

				if (layer.isBackground() && isComponentStore) {
					// create dummy bg
					img = CoreImage.create().getBlankImage(getWidth(),
							getHeight(), Color.white, 0);
				} else
					img = lData.getCurrentImage(previewElement);
			}

			if (img == null
					&& (!lData.isEffectSelected(EffectConstants.APPLYCOLOR) || (lData
							.isEffectSelected(EffectConstants.APPLYCOLOR) && (isPreview() && !lData
							.getEffectAsEO(EffectConstants.APPLYCOLOR)
							.isAnimatedInPreview())))) {
				// empty layer
				continue;
			}

			boolean hasAlphaBlending = lData
					.isEffectSelected(EffectConstants.ALPHABLENDING);
			boolean hasChannelBlending = lData
					.isEffectSelected(EffectConstants.CHANNELBLENDING);

			// find out if next layer has alpha blending
			BasicImageLayer next = findNextLayer(count);
			boolean nextAlphaBlending = next != null
					&& next.isEffectSelected(EffectConstants.ALPHABLENDING);

			boolean bgFillWhite = !hasAlphaBlending && !nextAlphaBlending;
			if (multiLayerElement && img != null && bgFillWhite) {

				EffectObject ag = lData
						.getEffectAsEO(EffectConstants.APPLYGRAPHICS);
				if (ag.isSelected()) {
					String scaleMode;
					if (ag.isParameterSet(EffectConstants.SCALEMODE)) {
						int scaleModeValue = Integer.parseInt((String) ag
								.getAttributeValue(EffectConstants.SCALEMODE));
						scaleMode = EffectObjectUtils
								.getScaleMode(scaleModeValue);
					} else {
						scaleMode = TSDataUtilities
								.getDefaultStretchMode(getId());
					}
					if (EffectConstants.Stretch.equals(scaleMode)) {
						img = CoreImage.create(img)
								.applyBackground(Color.WHITE).getAwt();
						bgFillWhite = false;
					}
				}
			}

			List<ILayerEffect> eff = lData.getSelectedLayerEffects();
			HashMap<Object, Object> m = new HashMap<Object, Object>();
			for (ILayerEffect effect : eff) {
				if (!effect.isSelected()
						|| (effect.getName().equalsIgnoreCase(
								EffectConstants.ALPHABLENDING) || effect
								.getName().equalsIgnoreCase(
										EffectConstants.CHANNELBLENDING))) {
					continue;
				}

				/*
				 * when in animation preview, filter out elements that are not
				 * selected for preview
				 */
				if (isPreview() && !effect.isAnimatedInPreview())
					continue;

				m.clear();
				EffectObject eo = (EffectObject) effect;
				putEffectIn(m, eo, elementLayout, img, previousImage, lData);

				// determine if draw image onto white BG in this layer
				if (bgFillWhite) {
					m.put(EffectConstants.ATTR_FILL_BACKGROUND, Boolean.TRUE);
				}

				// process image
				try {
					img = OnLayerEffects.ProcessImage(eo.getName(), img, m);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// apply channel blending if present
			if (hasAlphaBlending) {
				EffectObject eo = (EffectObject) lData
						.getEffect(EffectConstants.ALPHABLENDING);

				/*
				 * when in animation preview, filter out elements that are not
				 * selected for preview
				 */
				if (!isPreview() || (isPreview() && eo.isAnimatedInPreview())) {

					putEffectIn(m, eo, elementLayout, img, previousImage, lData);

					// process image
					try {
						img = OnLayerEffects.ProcessImage(eo.getName(), img, m);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			// apply channel blending if present
			if (hasChannelBlending) {
				EffectObject eo = (EffectObject) lData
						.getEffect(EffectConstants.CHANNELBLENDING);

				/*
				 * when in animation preview, filter out elements that are not
				 * selected for preview
				 */
				if (!isPreview() || (isPreview() && eo.isAnimatedInPreview())) {

					putEffectIn(m, eo, elementLayout, img, previousImage, lData);

					// process image
					try {
						img = OnLayerEffects.ProcessImage(eo.getName(), img, m);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			RenderedImage mask = null;

			if (previewElement != null
					&& previewElement.getAttribute().get(
							ThemeTag.ATTR_SPLMASKID) != null) {
				// mask must be obtained from another entity
				String maskEntityId = (String) previewElement.getAttribute()
						.get(ThemeTag.ATTR_SPLMASKID);
				S60Theme theme = (S60Theme) entity.getRoot();
				SkinnableEntity el = theme.getSkinnableEntity(maskEntityId);
				EditableEntityImage maskEntityImg;
				try {
					maskEntityImg = new EditableEntityImage(el);
					mask = maskEntityImg.getAggregateImage();
					CoreImage maskImage = CoreImage.create(mask).stretch(
							img.getWidth(), img.getHeight(), CoreImage.STRETCH)
							.applyBackground(Color.WHITE);
					mask = maskImage.getAwt();
					CoreImage bgImage = CoreImage.create(img).applyBackground(
							Color.WHITE);

					img = bgImage.applyMask(maskImage, true).getAwt();
				} catch (ThemeException e) {
					e.printStackTrace();
				}
			} else {
				if (layer.isBackground() && isComponentStore)
					// prevent loading bg
					mask = null;
				else
					mask = lData.getCurrentMask();

				if (mask != null) {
					EffectObject eObj = (EffectObject) lData
							.getEffectAsEO(EffectConstants.APPLYGRAPHICS);
					if (eObj != null && eObj.isSelected()) {
						CoreImage maskImage = CoreImage.create(mask).stretch(
								lData.getParent().getWidth(),
								lData.getParent().getHeight(),
								CoreImage.STRETCH);
						mask = maskImage.getAwt();
						img = CoreImage.create(img).applyMask(maskImage,
								entity.getToolBox().SoftMask).getAwt();
					}
				}
			}

			if (!hasChannelBlending && !hasAlphaBlending) {
				// not alpha blend nor channel blend - default over mode BOverA
				img = processNormal(img, previousImage, lData, 0, 0, 0, 0);
			}

			previousImage = img;
			count++;
		}

		// in component store always draw element with transparency on,
		// background is irrelevant
		boolean transparent = bgColor.getAlpha() == 0 || isComponentStore;
		if (!transparent) {
			// draw result on background
			RenderedImage bgImage = new BufferedImage(previousImage.getWidth(),
					previousImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

			// draw result onto background
			Graphics2D g = (Graphics2D) ((BufferedImage) bgImage).getGraphics();
			g.setColor(bgColor);
			g.fillRect(0, 0, bgImage.getWidth(), bgImage.getHeight());
			g.drawRenderedImage(previousImage, CoreImage.TRANSFORM_ORIGIN);
			g.dispose();

			previousImage = bgImage;
		}

		CoreImage result = CoreImage.create(previousImage);
		if (previewElement != null
				&& previewElement.getRotate() != null
				&& previewElement.getRotate().equalsIgnoreCase(
						PreviewTagConstants.ATTR_ROTATE_VALUE_180)) {
			result.rotate(180);
		}

		if (previewElement != null
				&& previewElement.getAttribute().get(
						ThemeTag.ATTR_SHAPE_LINK_ID) != null) {
			// image must be obtained from another entity
			String entityId = (String) previewElement.getAttribute().get(
					ThemeTag.ATTR_SHAPE_LINK_ID);
			S60Theme theme = (S60Theme) entity.getRoot();
			Element el = theme.getElementWithId(entityId);
			try {
				EditableEntityImage en = new EditableEntityImage(el);
				CoreImage entityImg = CoreImage.create(en.getAggregateImage());

				if (previewElement.getRotate() != null
						&& previewElement.getRotate().equalsIgnoreCase(
								PreviewTagConstants.ATTR_ROTATE_VALUE_180)) {
					entityImg.rotate(180);
				}

				if (entityImg.getWidth() != result.getWidth()
						|| entityImg.getHeight() != result.getHeight()) {
					entityImg.stretch(result.getWidth(), result.getHeight(),
							CoreImage.STRETCH);
				}

				entityImg.applyBackground(Color.WHITE);

				CoreImage bgMaskImg = result.copy()
						.applyBackground(Color.WHITE);
				result = entityImg.applyMask(bgMaskImg, true);
			} catch (ThemeException e) {
				e.printStackTrace();
			}
		}

		// colour processing for indication items - defined as (SVG = shape) +
		// colour
		// fix, if line graphic then colorize is not applied
		if (previewElement != null
				&& !previewElement.getSkinnableEntity().isLine()) {
			try {
				String colourId = previewElement.getAttributeValue("colourId");
				if (colourId != null) {
					Theme root = (S60Theme) entity.getRoot();
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
						boolean inverted = true;

						CoreImage mask = null;
						if (getLayer(0).isSvgImage()) {
							mask = result.copy().extractMask();
						} else if (getLayer(0).isBitmapImage()) {
							if (!getLayer(0).hasMask())
								/*
								 * special processing for bitmaps - bitmap
								 * itself is mask , black = visible
								 */
								try {
									mask = result
											.copy()
											.extractMaskForColourIndicationItem();
								} catch (Exception e) {
								}
							else
								mask = CoreImage.create(getLayer(0)
										.getMaskImage());

							inverted = false;
						}

						return CoreImage.create(
								CoreImage.getBlank3Image(mask.getWidth(), mask
										.getHeight(), cc)).applyMask(mask,
								inverted).getAwt();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// handle / apply colorize and optimize
		return postProcess ? applyBitmapProperties(result.getAwt()) : result
				.getAwt();
	}

	private boolean isConvertedMultiPiece() {
		String currProperty = getEntity().getCurrentProperty();
		int partCount = 0;
		if (getEntity().getDesignRectangles() != null) {
			partCount = getEntity().getDesignRectangles().size();
		}
		if ((currProperty != null)
				&& (currProperty.equals(ThemeTag.ATTR_SINGLE_BITMAP))) {
			if (partCount > 1) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected BasicEntityImage createEntityImage(Element el)
			throws ThemeException {
		return new EditableEntityImage(el);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.theme.editing.BasicEntityImage#createImageLayer(com.nokia
	 * .tools.platform.theme.ImageLayer, com.nokia.tools.platform.theme.Theme)
	 */
	@Override
	protected BasicImageLayer createImageLayer(ImageLayer iml, Theme theme)
			throws ThemeException {
		return new EditableImageLayer(iml, this, theme);
	}

	private void putEffectIn(Map<Object, Object> m, EffectObject eo,
			Object elementLayout, Object img, Object prevImage, Object lData) {
		m.clear();
		m.put(EffectConstants.EFFECTOBJECT, eo);
		m.put(EffectConstants.LAYOUT, elementLayout);
		m.put(EffectConstants.SOURCE2, img);
		m.put(EffectConstants.SOURCE1, prevImage);
		m.put(EffectConstants.X, 0);
		m.put(EffectConstants.Y, 0);
		m.put(ImageProcessor.ATTR_LDATA, lData);
		m.put(EffectConstants.CURRENT_THEME, getEntity().getRoot());
	}

	private RenderedImage applyBitmapProperties(RenderedImage img) {
		SkinnableEntity entity = getEntity();

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
			// map.put(ThemeConstants.COLORMARK, Boolean.TRUE);
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

	/**
	 * used when this entity is 3-piece bitmap
	 * 
	 * @param postProcess
	 */
	private RenderedImage computeAggregateImageMultiPiece(boolean postProcess) {

		BufferedImage buff = null;

		/* component store elements has not proper layout */
		if (!ComponentStore.isComponentStoreElement(getId())
				&& getElementLayout() != null)
			buff = new BufferedImage(getElementLayout().W(), getElementLayout()
					.H(), BufferedImage.TYPE_INT_ARGB);
		else if (getElementLayout() == null && getWidth() <= 0) {
			S60ThemePlugin
					.error(
							getId()
									+ ".computeAggregateImageMultiPiece() - cannot get dimensions of element. Using default dimesions.",
							new ThemeException(
									"Cannot get dimensions of element. Using default dimesions."));
			buff = new BufferedImage(defaultWidth, defaultHeight,
					BufferedImage.TYPE_INT_ARGB);
		} else {
			buff = new BufferedImage(getWidth(), getHeight(),
					BufferedImage.TYPE_INT_ARGB);
		}
		Graphics2D gg = (Graphics2D) buff.getGraphics();
		if (partsInstances == null) {
			try {
				getPartInstances();
			} catch (Exception e) {
				S60ThemePlugin.error(e);
				if (DEBUG)
					e.printStackTrace();
			}
			if (partsInstances == null
					|| !MultiPieceManager.isMultiPiece(partsInstances.size())) {
				// error
				S60ThemePlugin
						.error(
								getId()
										+ ".computeAggregateImageMultiPiece() - cannot get part instances. Returning white image.",
								new ThemeException(
										"Cannot get part instances. Returning white image."));
				if (getWidth() <= 0) {
					S60ThemePlugin
							.error(
									getId()
											+ ".computeAggregateImageMultiPiece() - cannot get dimensions of element. Using default dimesions.",
									new ThemeException(
											"Cannot get dimensions of element. Using default dimesions."));
					return CoreImage.create().getBlankImage(defaultWidth,
							defaultHeight, Color.WHITE);
				}

				return CoreImage.create().getBlankImage(getWidth(),
						getHeight(), Color.WHITE);
			}
		}

		for (IImage part_ : partsInstances) {
			EditableEntityImage part = (EditableEntityImage) part_;
			RenderedImage image = part.getAggregateImage();
			gg.drawRenderedImage(image, AffineTransform.getTranslateInstance(
					part.getBounds().x, part.getBounds().y));
		}

		return postProcess ? applyBitmapProperties(buff) : buff;
	}

	/**
	 * used when this entity is 9-piece bitmap
	 * 
	 * @param postProcess
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#clearTimeLineNodes()
	 */
	public void clearTimeLineNodes() {
		for (Object x : getLayers()) {
			((EditableImageLayer) x).clearTimeLineNodes();
		}
	}

	/**
	 * @param span
	 * @return
	 */
	public long getAnimationDuration(TimeSpan span) {
		long max = 0;
		for (ILayer l : getImageLayers()) {
			long m = ((EditableImageLayer) l).getAnimationDuration(span);
			if (m > max)
				max = m;
		}
		return max;
	}

	/*
	 * for each effect: 1. find all atributes, that are animated in given time
	 * model 2. set CPMFilterWraper properties to display CP's only from this
	 * 
	 * @param img @param timeModel @param span
	 */
	public void updateControlPointsFiltering(TimingModel timeModel,
			TimeSpan span) {
		for (ILayer l : getLayers()) {
			for (ILayerEffect f : l.getSelectedLayerEffects()) {
				EffectObject obj = (EffectObject) f;
				List<Integer> indexes = new ArrayList<Integer>();
				// find which params of this effect are animated in this timing
				// model / span
				for (Object p : obj.getParameters()) {
					EffectParameter par = (EffectParameter) p;
					if (par.getTimingModel() == timeModel) {
						if (span == null || span == par.getTimeSpan()) {
							indexes.add(par.getParameterIndex());
						}
					}
				}
				((CPMFilterWrapper) obj.getControlPointModelWrapper())
						.setParams(indexes);
			}
		}

	}

	@Override
	public boolean isMultiPiece() {
		if (MultiPieceManager.isMultiPiece((getEntity().getCurrentProperty()))) {
			if (getEntity().getChildren() != null
					&& MultiPieceManager.isMultiPiece(getEntity().getChildren()
							.size())) {
				return true;
			}
		}
		return false;
	}

	public int getPartCount() {
		if (getEntity().getChildren() != null) {
			return getEntity().getChildren().size();
		}
		return 1;
	}

	public boolean isElement() {
		return getEntity() instanceof Element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.theme.editing.BasicEntityImage#getAnotherInstance(int,
	 * int)
	 */
	public IImage getAnotherInstance(int w, int h) {
		try {
			return new EditableEntityImage(getEntity(), getThemeGraphics(),
					getPreviewElement(), w, h, getOpacity());
		} catch (ThemeException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * when element is multi-piece returns list of EditableEntityImages, for its
	 * childrens - parts
	 * 
	 * @return
	 */
	public synchronized List<IImage> getPartInstances() throws Exception {
		SkinnableEntity entity = getEntity();
		PreviewElement previewElement = getPreviewElement();

		/*
		 * entity can be type 'single bitmap', but have empty childs indicating
		 * that can be converted to nine-piece.
		 */
		if (entity.getChildren() == null)
			return null;

		if (partsInstances == null) {

			List<Layout> layouts = new ArrayList<Layout>();

			for (Object part0 : entity.getChildren()) {
				Part part = (Part) part0;

				Layout layout;
				if (previewElement == null) {
					layout = part.getLayoutInfo();

				} else {
					layout = part.getLayoutInfo(previewElement.getDisplay(),
							previewElement);
				}

				layouts.add(layout);
			}

			Dimension[] dims = null;
			if (getWidth() != 0 && getHeight() != 0 && layouts != null) {
				String currProp = entity.getCurrentProperty();
				if (currProp != null)
					dims = MultiPieceManager.patchPieceLayout(layouts,
							getWidth(), getHeight(), currProp);
				else
					dims = MultiPieceManager.patchPieceLayout(layouts,
							getWidth(), getHeight());
			}

			partsInstances = new ArrayList<IImage>();
			int i = 0;

			for (Object part0 : entity.getChildren()) {
				Part part = (Part) part0;

				Layout layout = layouts.get(i);
				int pW = layout.W(), pH = layout.H();
				if (dims != null) {
					pW = dims[i].width;
					pH = dims[i++].height;
				}

				EditableEntityImage eei = new EditableEntityImage(part, null,
						null, pW, pH, getOpacity());
				eei.setElementLayout(layout);
				partsInstances.add(eei);
			}

			if (entity.getCurrentProperty() != null)
				MultiPieceManager.setDimensions(partsInstances, entity
						.getCurrentProperty());
			else
				MultiPieceManager.setDimensions(partsInstances);

		}

		return partsInstances;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IImage#getPartType()
	 */
	public String getPartType() {
		if (getEntity() instanceof Part)
			return getFriendlyName(getEntity());
		else {
			throw new RuntimeException(getEntity().getAttributeValue("name")
					+ ": Not a Part");
		}
	}

	public static String getFriendlyName(ThemeBasicData data) {
		String s = data.getAttributeValue("name");

		String result = MultiPieceManager.getPartName(s, data.getParent()
				.getCurrentProperty());
		return result.toString();
	}

	public static int getPartIndex(ThemeBasicData data, int partCount) {
		String name = getFriendlyName(data);
		return MultiPieceManager.getPartIndex(name, partCount);
		// return getPartIndex(name);
	}

	public void setSinglePieceBitmap() {
		getEntity().setCurrentProperty(SINGLE_PIECE_BITMAP);
	}

	public void setNinePieceBitmap() {
		getEntity().setCurrentProperty(NINE_PIECE_BITMAP);
	}

	public void setMultiPieceBitmap() {
		int partCount = 0;
		if ((getEntity() != null) && (getEntity().getChildren() != null))
			partCount = getEntity().getChildren().size();

		getEntity().setCurrentProperty(
				MultiPieceManager.getElementTypeId(partCount));
	}

	/**
	 * for detection change in bitmap props setting
	 * 
	 * @return
	 */
	public String getBitmapPropertiesSignature() {

		StringBuffer result = new StringBuffer();
		SkinnableEntity entity = getEntity();
		if (entity.getAttribute().get(BitmapProperties.COLORIZE_SELECTED) != null
				|| entity.getAttribute().get(
						BitmapProperties.OPTIMIZE_SELECTION) != null) {

			result.append(entity.getAttribute().get(
					BitmapProperties.DITHER_SELECTED) == null ? Boolean.FALSE
					: entity.getAttribute().get(
							BitmapProperties.DITHER_SELECTED));

			result.append(entity.getAttribute().get(
					BitmapProperties.COLORIZE_SELECTED) == null ? Boolean.FALSE
					: entity.getAttribute().get(
							BitmapProperties.COLORIZE_SELECTED));
			result.append(entity.getAttribute().get(
					BitmapProperties.OPTIMIZE_SELECTION) == null ? "" : entity
					.getAttribute().get(BitmapProperties.OPTIMIZE_SELECTION));

			Color color = (Color) (entity.getAttribute().get(
					BitmapProperties.COLOR) == null ? Color.WHITE : entity
					.getAttribute().get(BitmapProperties.COLOR));
			result.append(color.getRed());
			result.append(color.getGreen());
			result.append(color.getBlue());
			result.append(entity.getAttribute().get(BitmapProperties.COLORIZE));

			return result.toString();
		} else {
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.BasicEntityImage#canBeAnimated()
	 */
	public boolean canBeAnimated() {
		return EffectAvailabilityParser.INSTANCE.canBeAnimated(getEntity());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.theme.editing.BasicEntityImage#getMaximumLayerCount()
	 */
	public int getMaximumLayerCount() {
		SkinnableEntity entity = getEntity();

		LayerConstraints cons = EffectAvailabilityParser.INSTANCE
				.getLayerConstraintsForEntity(entity.getId());
		if (cons == null) {
			String entityType = entity.isEntityType();
			cons = EffectAvailabilityParser.INSTANCE
					.getLayerConstraintsForEntityType(entityType);
		}

		if (cons != null) {
			return cons.getMaxLayerCount();
		} else {
			return Integer.MAX_VALUE;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.theme.editing.BasicEntityImage#getAdapter(java.lang.Class
	 * )
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter == IPasteTargetAdapter.class) {
			return new SkinnableEntityCopyPasteSupport(getEntity())
					.getPasteTargetAdapter(this);
		}
		if (adapter == ISkinnableEntityAdapter.class) {
			return getEntity();
		}
		return null;
	}

	public void forceBackgroundImage(RenderedImage bgImg) {
		this.forceBgImg = bgImg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.BasicEntityImage#isBackground()
	 */
	@Override
	public boolean isBackground() {
		return TSDataUtilities.isBackgroundElementId(getId());
	}

	public void setElevenPieceBitmap() {
		getEntity().setCurrentProperty(ELEVEN_PIECE_BITMAP);
	}

	public void setThreePieceBitmap() {
		getEntity().setCurrentProperty(THREE_PIECE_BITMAP);
	}

	public SkinnableEntity getEntity() {
		return super.getEntity();
	}

}