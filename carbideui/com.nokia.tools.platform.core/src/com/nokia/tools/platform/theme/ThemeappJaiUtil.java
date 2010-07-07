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
/*
 * File Name ThemeappJaiUtil.java Description File contains some utility methods
 * for JAI operation. 
 */
package com.nokia.tools.platform.theme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.layout.ComponentInfo;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.platform.theme.preview.PreviewImage;
import com.nokia.tools.resource.util.FileUtils;

public class ThemeappJaiUtil {
	public static final ResourceBundle soundBundle = ResourceBundle
			.getBundle("SoundImageProp");

	private static boolean CACHES_OFF = false;

	public static RenderedImage getElementImage(SkinnableEntity entity)
			throws ThemeException {

		return getElementImage(entity, entity.getDisplay(), 1);

	}

	public static RenderedImage getElementImage(SkinnableEntity entity,
			Display display, int elementImage) throws ThemeException {
		return getElementImage(entity, display, elementImage, true);
	}

	public static RenderedImage getElementImage(SkinnableEntity entity,
			Display display, int elementImage, boolean cacheImages)
			throws ThemeException {
		ThemeGraphic tGraphic = null;
		Layout layout = entity.getLayoutInfo(display);
		Theme s60 = (Theme) entity.getRoot();
		boolean multiPieceBitmap = entity.hasChildNodes();
		ThemeCache cache = null;

		tGraphic = getThemeGraphic(entity, elementImage);
		if (tGraphic != null)
			multiPieceBitmap = false;

		if (!multiPieceBitmap) {

			if (elementImage == 1) {
				cache = s60.getImageCache();
			} else {
				cache = s60.getPreviewCache();
			}
			if (cacheImages) {
				RenderedImage hit = (RenderedImage) cache
						.getElement(display, entity.getIdentifier(), entity
								.getDefaultLocId(display));
				if (null != hit)
					return hit;
			}

			if (tGraphic == null)
				return null;

			RenderedImage image = tGraphic.getProcessedImage(entity, layout,
					entity.getToolBox().SoftMask);
	
			if (cacheImages) {
				cache.putElement(display, entity.getIdentifier(), entity
						.getDefaultLocId(display), image);
			}
			return image;
		} else {

			List partInfo = entity.getLayeredChildren();
			if (partInfo.size() == 0)
				return null;

			BufferedImage img = CoreImage.create().init(layout.W(), layout.H(),
					Color.WHITE, 255).getBufferedImage();
			Graphics2D g2D = (Graphics2D) img.createGraphics();
			RenderedImage image = null;
			for (int i = 0; i < partInfo.size(); i++) {
				Part part = (Part) partInfo.get(i);
				tGraphic = getThemeGraphic(part, elementImage);
				Layout partlayout = part.getLayoutInfo(display);
				if (elementImage == 1) {
					// tGraphic=part.getThemeGraphic();
					cache = s60.getImageCache();
				} else if (elementImage == 3) {

					cache = s60.getPreviewCache();
				}

				image = (RenderedImage) cache.getElement(display, part
						.getIdentifier(), entity.getDefaultLocId(display));

				if (null == image) {
					image = tGraphic.getProcessedImage(part, partlayout, part
							.getToolBox().SoftMask);
				
					if (cacheImages) {
						cache.putElement(display, part.getIdentifier(), entity
								.getDefaultLocId(display), image);
					}
				}
				g2D.drawRenderedImage(image, AffineTransform
						.getTranslateInstance(partlayout.L() - layout.L(),
								partlayout.T() - layout.T()));

			}
			return img;
		}
	}

	/**
	 * @return Returns the imageForAU.
	 */
	public static RenderedImage getImageForFile(String ext) {
		int index = ext.lastIndexOf('.');
		if (index >= 0) {
			ext = ext.substring(index + 1);
		}

		String path = null;
		try {
			path = soundBundle.getString(ext.toUpperCase());
			if (path == null)
				path = soundBundle.getString(ThemeTag.ELEMENT_SOUND);
		} catch (MissingResourceException e) {
			try {
				path = soundBundle.getString("UN");
			} catch (Exception e1) {
				PlatformCorePlugin.error(e1);
			}
		}

		if (path != null) {
			try {
				return ImageIO.read(FileUtils.getURL(PlatformCorePlugin
						.getDefault(), path));
			} catch (Exception e) {
				PlatformCorePlugin.error(e);
			}
		}
		return null;
	}

	public static ThemeGraphic getThemeGraphic(SkinnableEntity element,
			int elementImage) {
		ThemeGraphic tGraphic = null;
		try {

			if (elementImage == 1) {
				tGraphic = element.getThemeGraphic();
			} else {
				tGraphic = element.getPreviewThemeGraphic();
			}
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
		}
		return tGraphic;
	}

	public static ThemeGraphic getThemeGraphicForPreview(
			SkinnableEntity element, Display display, PreviewImage screen,
			ComponentInfo component, Layout layout, int elementImage) {
		ThemeGraphic tGraphic = null;
		try {

			if (elementImage == 1) {
				tGraphic = element.getThemeGraphic(display, screen, component);
			} else {
				tGraphic = element.getPreviewThemeGraphic(screen);
			}
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
		}
		return tGraphic;
	}

	public static RenderedImage getElementImage(SkinnableEntity entity,
			Display display, PreviewImage screen, ComponentInfo component,
			int elementImage) throws ThemeException {
		return getElementImage(entity, display, screen, component,
				elementImage, true);
	}

	// Support For Landscape Previews - start
	public static RenderedImage getElementImage(SkinnableEntity entity,
			Display display, PreviewImage screen, ComponentInfo component,
			int elementImage, boolean applyMask) throws ThemeException {
		// String entityType=element.isEntityType();
		Layout layout = entity.getLayoutInfoForPreview(display, component,
				screen);
		return getElementImage(entity, display, screen, component,
				elementImage, applyMask, layout, null);
	}

	public static RenderedImage getElementImage(SkinnableEntity entity,
			Display display, PreviewImage screen, ComponentInfo component,
			int elementImage, boolean applyMask, Layout layout,
			PreviewElement preElem) throws ThemeException {
		ThemeGraphic tGraphic = null;

		Theme s60 = (Theme) entity.getRoot();
		if (display == null) {
			display = screen.getDisplay();
		}
		String locId = component.getLocId();
		boolean multiPieceBitmap = entity.hasChildNodes();
		ThemeCache cache = null;

		tGraphic = getThemeGraphicForPreview(entity, display, screen,
				component, layout, elementImage);
		if (tGraphic != null) {
			multiPieceBitmap = false;
		}

		if (!multiPieceBitmap) {

			if (elementImage == 1) {
				cache = s60.getImageCache();
			} else {
				cache = s60.getPreviewCache();
			}
			String key = locId + applyMask;

			String keyPreviewCache = locId + "@v:" + component.getVariety()
					+ applyMask;

			if (!CACHES_OFF) {
				Object hitImage = null;
				if (cache.getName().equals(ThemeConstants.PREVIEW_CACHE_NAME)) {
					hitImage = cache.getElement(display,
							entity.getIdentifier(), keyPreviewCache);
				} else {
					hitImage = cache.getElement(display,
							entity.getIdentifier(), key);
				}
				if (null != hitImage)
					return (RenderedImage) hitImage;

			}

			if (tGraphic == null)
				return null;

			RenderedImage image = tGraphic.getProcessedImage(entity, layout,
					display, entity.getToolBox().SoftMask, applyMask,
					elementImage, preElem);

			if (cache.getName().equals(ThemeConstants.PREVIEW_CACHE_NAME)) {
				cache.putElement(display, entity.getIdentifier(),
						keyPreviewCache, image);
			} else {
				cache.putElement(display, entity.getIdentifier(), key, image);
			}
			return image;
		} else {

			List partInfo = entity.getLayeredChildren();
			// layout=element.getLayoutInfo();
			BufferedImage img = new BufferedImage(layout.W(), layout.H(),
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2D = (Graphics2D) img.getGraphics();
			RenderedImage image = null;

			locId = (locId != null && locId.trim().length() > 0 ? locId : "")
					+ "<LC>" + component.getName() + "@V:"
					+ component.getVariety() + "#R:1</LC>";
			if (partInfo.size() == 0)
				return null;
			for (int i = 0; i < partInfo.size(); i++) {
				Part part = (Part) partInfo.get(i);

				ComponentInfo partComponent = part.getComponentInfo();

				tGraphic = getThemeGraphicForPreview(part, display, screen,
						partComponent, layout, elementImage);
				// Support For Landscape Previews - start
				// Layout partlayout=part.getLayoutInfoForPreview(phoneModel,new
				// Display(width,height),orientation,locId,
				// pState.getScreenName());
				Layout partlayout = part.getLayoutInfoForPreview(display,
						locId, screen);
				// Support For Landscape Previews - end
				if (elementImage == 1) {
					cache = s60.getImageCache();
				} else {
					cache = s60.getPreviewCache();
				}
				image = (RenderedImage) cache.getElement(display, part
						.getIdentifier(), locId);

				if (null == image) {
					try {
						image = tGraphic.getProcessedImage(part, partlayout,
								display, part.getToolBox().SoftMask, applyMask,
								elementImage, preElem);
					} catch (Exception e) {
						continue;
					}
					cache.putElement(display, part.getIdentifier(), locId,
							image);
				}
				g2D.drawRenderedImage(image, AffineTransform
						.getTranslateInstance(partlayout.L() - layout.L(),
								partlayout.T() - layout.T()));

				// createDialog(image);
			}
			return img;
		}

	}

	public static RenderedImage TileImage(RenderedImage image,
			String alignment, int width, int height) {
		int X = 0;
		int Y = 0;
		if (alignment.equalsIgnoreCase(ThemeConstants.TOPLEFT)) {
			X = 0;
			Y = 0;
		} else if (alignment.equalsIgnoreCase(ThemeConstants.TOPRIGHT)) {
			X = width - image.getWidth();
			Y = 0;
		} else if (alignment.equalsIgnoreCase(ThemeConstants.BOTTOMLEFT)) {
			X = 0;
			Y = width + image.getHeight();
		} else if (alignment.equalsIgnoreCase(ThemeConstants.BOTTOMRIGHT)) {
			X = width - image.getWidth();
			Y = height - image.getHeight();
		} else if (alignment.equalsIgnoreCase(ThemeConstants.CENTER)) {
			X = (width / 2) - (image.getWidth() / 2);
			Y = (height / 2) - (image.getHeight() / 2);
		}

		if (image.getWidth() >= width && image.getHeight() >= height)
			return image;

		BufferedImage buf = CoreImage.create().init(width, height, Color.WHITE)
				.getBufferedImage();

		Graphics2D gd = (Graphics2D) buf.getGraphics();

		if (X != 0) {
			do {
				X = X - image.getWidth();
			} while (X > 0);

		}
		if (Y != 0) {
			do {
				Y = Y - image.getHeight();
			} while (Y > 0);
		}
		if ((image.getWidth() <= width) || (image.getHeight() <= height)) {
			for (int i = X; i <= width; i += image.getWidth()) {
				for (int j = Y; j <= height; j += image.getHeight()) {
					gd.drawRenderedImage(image, AffineTransform
							.getTranslateInstance(i, j));
				}
			}

		}
		return buf;
	}


	public static boolean hasBackgroundLayer(SkinnableEntity element,
			int elementImage) {
		ThemeGraphic tg = getThemeGraphic(element, elementImage);

		boolean result = false;

		if (tg != null && tg.hasBackground())
			result = true;

		return result;
	}

	public static RenderedImage Tile(RenderedImage img, String tileProperty,
			int width, int height) {
		if (tileProperty.equalsIgnoreCase(ThemeTag.ATTR_TILEX)) {
			return CoreImage.create().init(img).scale(
					((float) height / img.getHeight()),
					((float) height / img.getHeight()), 0, 0).tileImage(0, 0,
					width, height).getAwt();
		}
		if (tileProperty.equalsIgnoreCase(ThemeTag.ATTR_TILEY)) {
			return CoreImage.create().init(img).scale(
					((float) width / img.getWidth()),
					((float) width / img.getWidth()), 0, 0).tileImage(0, 0,
					width, height).getAwt();
		}
		return CoreImage.create().init(img).tileImage(0, 0, width, height)
				.getAwt();
	}
}
