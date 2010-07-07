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
package com.nokia.tools.theme.s60.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.EffectConstants;
import com.nokia.tools.platform.theme.ILayeredImageCompositor;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.LayerEffect;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.platform.theme.preview.PreviewException;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.theme.s60.S60ThemePlugin;
import com.nokia.tools.theme.s60.editing.EditableEntityImage;
import com.nokia.tools.theme.s60.effects.EffectObjectUtils;
import com.nokia.tools.theme.s60.effects.ImageProcessor;
import com.nokia.tools.theme.s60.effects.OnLayerEffects;


public class S60LayeredImageCompositor implements ILayeredImageCompositor {
	private ThemeGraphic graphic;

	public S60LayeredImageCompositor(ThemeGraphic graphic) {
		this.graphic = graphic;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ILayeredImageCompositor#ProcessList(com.nokia.tools.platform.theme.SkinnableEntity,
	 *      com.nokia.tools.platform.layout.Layout,
	 *      com.nokia.tools.platform.core.Display, java.util.List, boolean,
	 *      boolean, int)
	 */
	public RenderedImage ProcessList(SkinnableEntity entity, Layout layout,
			Display display, List list, boolean softMask, boolean applyMask,
			int elementParam, PreviewElement preElem) throws ThemeException {

		boolean multiLayerElement = entity.getToolBox().multipleLayersSupport;
		Color bgColor = BASE_COLOR;

	

		EditableEntityImage eeiInstance = new EditableEntityImage(entity,
				graphic, null, 0, 0);

		/**
		 * semi-transparent means that if element has non-ng non-empty layer, is
		 * opaque, otherwise transparent
		 */
		List<ImageLayer> selLayers = new ArrayList<ImageLayer>(graphic
				.getImageLayers());
		for (Iterator iter = selLayers.iterator(); iter.hasNext();) {
			ImageLayer layer = (ImageLayer) iter.next();
			if (!layer.isSelected()) {
				iter.remove();
			}
		}
		boolean semiTransparent = false;
		{
			if (multiLayerElement) {
				// find out if element has only BG layer, or it is have another
				// non-empty layer
				for (ImageLayer l : selLayers) {
					if (!l.isBackground()) {
						List<Object> effects = l.getLayerEffects();
						int cnt = 0;
						for (Object effect : effects) {
							if (((LayerEffect) effect).isSelected()) {
								cnt++;
								break;
							}
						}
						if (cnt > 0) {
							semiTransparent = true;
							break;
						}
					}
				}
			}
		}

		// find out if image has Alpha blending defined
		if (selLayers.size() > 0 && entity.getToolBox().multipleLayersSupport) {
			for (ImageLayer first : selLayers)
				if (first.getLayerEffects(IMediaConstants.ALPHABLENDING) != null
						&& first.getLayerEffects(IMediaConstants.ALPHABLENDING)
								.isSelected()) {
					semiTransparent = true;
					bgColor = Color.BLACK;
					break;
				}
		}

		// create base transparent image
		CoreImage previousImage = CoreImage.create().init(
				Math.max(1, layout.W()), Math.max(1, layout.H()), BASE_COLOR,
				0, 4);

		BufferedImage bgImage = new BufferedImage(previousImage.getWidth(),
				previousImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		bgColor = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor
				.getBlue(), semiTransparent ? 255 : 0);

		if (list.size() == 0)
			return null;

		CoreImage img = null;
		for (int m = 0; m < list.size(); m++) {

			ImageLayer iml = (ImageLayer) list.get(m);
			ILayer iLayerInstance = eeiInstance.getLayer(m);

			if (!iml.isSelected())
				continue;

			img = getImage(entity, layout, display, iml, 0, 0, elementParam,
					preElem);

			boolean hasAlphaBlending = iml
					.getLayerEffects(EffectConstants.ALPHABLENDING) != null
					&& iml.getLayerEffects(EffectConstants.ALPHABLENDING)
							.isSelected();
			boolean hasChannelBlending = iml
					.getLayerEffects(EffectConstants.CHANNELBLENDING) != null
					&& iml.getLayerEffects(EffectConstants.CHANNELBLENDING)
							.isSelected();

			// find out if next layer has alpha blending
			ImageLayer next = findNextLayer(m);
			boolean nextAlphaBlending = next != null
					&& next.getLayerEffects(EffectConstants.ALPHABLENDING) != null
					&& next.getLayerEffects(EffectConstants.ALPHABLENDING)
							.isSelected();

			boolean bgFillWhite = !hasAlphaBlending && !nextAlphaBlending;

			if (multiLayerElement && img != null && bgFillWhite) {
				LayerEffect ag = iml
						.getLayerEffects(EffectConstants.APPLYGRAPHICS);
				if (ag != null && ag.isSelected()) {
					int scaleModeValue = Integer.parseInt((String) ag
							.getAttribute(EffectConstants.SCALEMODE));
					String scaleMode = EffectObjectUtils
							.getScaleMode(scaleModeValue);
					if (EffectConstants.Stretch.equals(scaleMode)) {
						img.applyBackground(Color.WHITE);
						bgFillWhite = false;
					}
				}
			}

			if (bgFillWhite && img != null) {

								
				//Use layout dimensions as a special case here if img dimensions are missing.
				//CoreImage cannot create a blank image of width or height as 0.
				int width = img.getWidth();
				if (width == 0) width = layout.W();
				int height = img.getHeight();
				if (height == 0) height = layout.H();
				
				BufferedImage finalImg = CoreImage.create().init(
						width, height, Color.WHITE,
						multiLayerElement ? 255 : 0).getBufferedImage();
				Graphics2D g2d = finalImg.createGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				g2d.drawRenderedImage(img.getAwt(), CoreImage.TRANSFORM_ORIGIN);
				g2d.dispose();
				img.init(finalImg);
			}

			HashMap<Object, Object> map = new HashMap<Object, Object>();
			List l = iml.getLayerEffects();
			for (int i = 0; i < l.size(); i++) {
				LayerEffect le = (LayerEffect) l.get(i);
				if (!le.isSelected())
					continue;
				String effect = le.getEffetName();
				if (effect.equalsIgnoreCase(EffectConstants.ALPHABLENDING)
						|| effect
								.equalsIgnoreCase(EffectConstants.CHANNELBLENDING)) {
					continue;
				}
				ILayerEffect iEffectInstance = iLayerInstance.getEffect(effect);

				putEffectIn(map, iEffectInstance, layout, img.getAwt(),
						previousImage.getAwt(), entity.getRoot(),
						iLayerInstance);
				map.put(EffectConstants.OPACITY, new Integer(255));

				img
						.init(OnLayerEffects.ProcessImage(effect, img.getAwt(),
								map));
			}

			// apply channel blending if present
			if (hasAlphaBlending) {
				ILayerEffect iEffectInstance = iLayerInstance
						.getEffect(IMediaConstants.ALPHABLENDING);

				putEffectIn(map, iEffectInstance, layout, img.getAwt(),
						previousImage.getAwt(), entity.getRoot(),
						iLayerInstance);
				String mode = iEffectInstance.getParameter(
						EffectConstants.ATTR_MODE).getValue();
				if (StringUtils.isEmpty(mode))
					mode = EffectConstants.BOVERA;
				map.put(EffectConstants.BLENDMODE, mode);

				// process image
				try {
					img.init(OnLayerEffects.ProcessImage(iEffectInstance
							.getName(), img.getAwt(), map));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// apply channel blending if present
			if (hasChannelBlending) {
				ILayerEffect iEffectInstance = iLayerInstance
						.getEffect(IMediaConstants.CHANNELBLENDING);

				putEffectIn(map, iEffectInstance, layout, img.getAwt(),
						previousImage.getAwt(), entity.getRoot(),
						iLayerInstance);
				String mode = iEffectInstance.getParameter(
						EffectConstants.ATTR_MODE).getValue();
				if (StringUtils.isEmpty(mode))
					mode = EffectConstants.Normal;
				map.put(EffectConstants.BLENDMODE, mode);

				// process image
				try {
					img.init(OnLayerEffects.ProcessImage(iEffectInstance
							.getName(), img.getAwt(), map));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (img == null)
				continue;

			if (applyMask) {
				CoreImage mask = getMaskImage(iml, (Theme) entity.getRoot(),
						layout, 0, 0);
				if (mask != null)
					img.applyMask(mask, softMask);
			}

			String tileProperty = iml.getAttribute(ThemeTag.ATTR_TILE);

			if (tileProperty != null) {
				int width = layout.W();
				int height = layout.H();

				if (tileProperty.equalsIgnoreCase(ThemeTag.ATTR_TILEX)) {
					img.scale(((float) height / img.getHeight()),
							((float) height / img.getHeight()), 0, 0)
							.tileImage(0, 0, width, height);
				} else if (tileProperty.equalsIgnoreCase(ThemeTag.ATTR_TILEY)) {
					img.scale(((float) width / img.getWidth()),
							((float) width / img.getWidth()), 0, 0).tileImage(
							0, 0, width, height);
				} else
					img.tileImage(0, 0, width, height);
			}

			if (!hasAlphaBlending && !hasChannelBlending) {
				// process normal
				BufferedImage layer0 = previousImage.getBufferedImage();
				Graphics2D g = (Graphics2D) layer0.getGraphics();
				g.drawRenderedImage(img.getAwt(), CoreImage.TRANSFORM_ORIGIN);
				g.dispose();
				img.init(layer0);
			}
			previousImage.init(img.getAwt());
		}

		// draw result onto background
		Graphics2D g = (Graphics2D) bgImage.getGraphics();
		g.drawRenderedImage(previousImage.getAwt(), CoreImage.TRANSFORM_ORIGIN);
		g.dispose();

		return bgImage;
	}

	private void putEffectIn(Map<Object, Object> m, Object effect,
			Object elementLayout, RenderedImage img, RenderedImage prevImage,
			Object root, ILayer lData) {
		m.clear();
		m.put(EffectConstants.EFFECTOBJECT, effect);
		m.put(EffectConstants.LAYOUT, elementLayout);
		m.put(EffectConstants.SOURCE2, img);
		m.put(EffectConstants.SOURCE1, prevImage);
		m.put(EffectConstants.X, 0);
		m.put(EffectConstants.Y, 0);
		m.put(EffectConstants.CURRENT_THEME, root);
		m.put(ImageProcessor.ATTR_LDATA, lData);
	}

	/*
	 * next selected layer
	 */
	private ImageLayer findNextLayer(int count) {
		count++;
		List<ImageLayer> imageLayers = graphic.getImageLayers();
		while (count < imageLayers.size()) {
			if (imageLayers.get(count).isSelected())
				return imageLayers.get(count);
			count++;
		}
		return null;
	}

	private CoreImage getImage(SkinnableEntity entity, Layout layout,
			Display display, ImageLayer iml, int imagex, int imagey,
			int elementImage, PreviewElement preElem) {
		Theme theme = (Theme) entity.getRoot();

		String path = iml.getFileName((Theme) entity.getRoot(), preElem);

		
		if (iml.isBackground()) {
			try {

				// getImageFrom preview API
				RenderedImage image = null;
				if (layout == null) {
					image = theme.getThemePreview().getBackgroundLayerImage(
							entity.getIdentifier(), elementImage, preElem);
				} else {
					image = theme.getThemePreview().getBackgroundLayerImage(
							entity.getIdentifier(), elementImage, layout,
							preElem);
				}
				return CoreImage.create(image);
			} catch (PreviewException e1) {
				e1.printStackTrace();
			}
		}

		CoreImage img = CoreImage.create();
		if (path != null && (new File(path)).exists()) {
			
			
			String tileProperty = iml.getTile();
		
			int width = 0, height = 0;
			if (graphic.getAttribute(ThemeTag.LAYOUT_WIDTH) != null)
				width = new Integer(graphic.getAttribute(ThemeTag.LAYOUT_WIDTH))
						.intValue();
			else
				width = layout.W();

			if (graphic.getAttribute(ThemeTag.LAYOUT_HEIGHT) != null)
				height = new Integer(graphic
						.getAttribute(ThemeTag.LAYOUT_HEIGHT)).intValue();
			else
				height = layout.H();

			try {
				if (((imagex == 0) && (imagey == 0)) && tileProperty == null) {

					String scaleMode = iml
							.getAttribute(EffectConstants.SCALEMODE);

					// When background is default (cleared),
					// this is indicated by scaleMode = null,
					// it behaves like Stretch

					boolean notSet = scaleMode == null
							|| scaleMode
									.equalsIgnoreCase(EffectConstants.Normal);

					if (notSet
							|| scaleMode
									.equalsIgnoreCase(EffectConstants.Stretch)) {
						if (entity.getToolBox().multipleLayersSupport)
							img.load(new File(path), width, height,
									CoreImage.STRETCH);
						else
							img.load(new File(path), width, height,
									CoreImage.STRETCH);
					} else {
						// aspect ratio
						if (entity.getToolBox().multipleLayersSupport)
							img.load(new File(path), width, height,
									CoreImage.SCALE_TO_FIT);
						else
							img.load(new File(path), width, height,
									CoreImage.SCALE_TO_FIT);
					}

				} else {
					img.load(new File(path), width, height);
				}

			} catch (Exception e) {
				S60ThemePlugin.error(e);
			}

		}
		return img;
	}

	private CoreImage getMaskImage(ImageLayer iml, Theme theme, Layout layout,
			int imagex, int imagey) {

		String maskpath;
		if (iml.getAttribute(ThemeTag.ATTR_SOFTMASK) != null)
			maskpath = iml.getMaskFileName(theme, true);
		else
			maskpath = iml.getMaskFileName(theme, false);

		if (maskpath == null) {
			return null;
		}

		String tileProperty = iml.getTile();
		
		CoreImage img = CoreImage.create();
		try {

			int width = 0, height = 0;
			if (graphic.getAttribute(ThemeTag.LAYOUT_WIDTH) != null)
				width = new Integer(graphic.getAttribute(ThemeTag.LAYOUT_WIDTH))
						.intValue();
			else
				width = layout.W();

			if (graphic.getAttribute(ThemeTag.LAYOUT_HEIGHT) != null)
				height = new Integer(graphic
						.getAttribute(ThemeTag.LAYOUT_HEIGHT)).intValue();
			else
				height = layout.H();

			if (((imagex == 0) && (imagey == 0)) && tileProperty == null) {
				img = CoreImage.create()
						.load(new File(maskpath), width, height);
				String scaleMode = iml.getAttribute(EffectConstants.SCALEMODE);
				if (scaleMode != null && scaleMode.length() != 0) {
					if (scaleMode.equalsIgnoreCase(EffectConstants.Stretch))
						img.load(new File(maskpath), width, height,
								CoreImage.STRETCH);
					else
						img.load(new File(maskpath), width, height,
								CoreImage.SCALE_TO_FIT);
				} else
					img.load(new File(maskpath), width, height,
							CoreImage.STRETCH);

			} else {
				img.load(new File(maskpath), width, height);
			}
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		}
		
		return img;
	}
}
