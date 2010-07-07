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
package com.nokia.tools.platform.theme;

import java.awt.image.RenderedImage;
import java.io.File;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.preview.PreviewElement;


public class LayerUtils {
	/**
	 * loads image for given image layer
	 * 
	 * @param skindir
	 * @param iml
	 * @return
	 */
	public static RenderedImage getImage(String skindir, ImageLayer iml,
			ThemeGraphic tg, Layout elementLayout, boolean forceGenerateBG,
			SkinnableEntity entity, int _width, int _height) {
		return getImage(skindir, iml, tg, elementLayout, forceGenerateBG,
				entity, _width, _height, null);
	}

	/**
	 * loads image for given image layer
	 * 
	 * @param skindir
	 * @param iml
	 * @return
	 */
	public static RenderedImage getImage(String skindir, ImageLayer iml,
			ThemeGraphic tg, Layout elementLayout, boolean forceGenerateBG,
			SkinnableEntity entity, int _width, int _height,
			PreviewElement element) {

		boolean isSound = false, isColour = false;

		if (entity.isEntityType().equalsIgnoreCase(ThemeTag.ELEMENT_SOUND))
			isSound = true;
		if (entity.isEntityType().equalsIgnoreCase(ThemeTag.ELEMENT_COLOUR))
			isColour = true;

		Theme s60 = (Theme) entity.getRoot();
		String path = "";

		if (isColour) {
			try {
				return tg.getProcessedImage(entity, elementLayout, entity
						.getToolBox().SoftMask);
			} catch (ThemeException e) {
				PlatformCorePlugin.error(e);
			}
		}

		// If the element is not skinned, take the element
		// from the model
		if(!entity.isSkinned() && !(entity instanceof Part)){
			path = iml.getFileName(s60.getModel(),s60, element);
		}
		else{
			path = iml.getFileName(s60, element);
		}
		
		if (iml.isBackground() && path== null) {
			try {
				return s60.getThemePreview().getBackgroundLayerImage(
						entity.getIdentifier(),
						ThemeConstants.ELEMENT_IMAGE_PARAM_PREVIEW_IMAGE,
						elementLayout, element);
			} catch (Exception e) {
				PlatformCorePlugin.error(e);
			}
		}
		
		//path = iml.getFileName(s60, element);
		
		if (path == null)
			return null;

		String ext = path.substring(path.lastIndexOf('.') + 1, path.length());

		if (isSound
				|| entity.isEntityType().equalsIgnoreCase(
						ThemeTag.ELEMENT_EMBED_FILE)) {
			return ThemeappJaiUtil.getImageForFile(ext);
		}

		File file = new File(path);
		if (!file.exists())
			return null;

		try {
			return CoreImage.create().load(file, _width, _height).getAwt();
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
		}

		return null;
	}

	/**
	 * loads mask for given layer
	 * 
	 * @param skinsdir
	 * @param iml
	 * @return
	 */
	public static RenderedImage getMaskImage(String skinsdir, ImageLayer iml,
			SkinnableEntity entity, int _width, int _height) {
		RenderedImage mask = null;
		if (iml.getAttribute(ThemeTag.ATTR_SOFTMASK) != null
				|| iml.getAttribute(ThemeTag.ATTR_HARDMASK) != null) {
			String maskpath = null;

			maskpath = iml.getMaskFileName((Theme) entity.getRoot(), entity
					.getToolBox().SoftMask);
			try {
				File file;
				if (maskpath != null && (file = new File(maskpath)).exists()) {
					mask = CoreImage.create().load(file, _width, _height)
							.getAwt();
				}

			} catch (Exception e) {
				PlatformCorePlugin.error(e);
			}
		}
		return mask;
	}

}
