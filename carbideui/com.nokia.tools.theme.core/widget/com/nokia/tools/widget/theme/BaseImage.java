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
package com.nokia.tools.widget.theme;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.RenderedImage;
import java.net.URL;

import com.nokia.tools.media.image.CoreImage;

/**
 * Base class for the image component. By default, the image contans a single
 * image source which is identified by the URL.
 * 
 */
public class BaseImage extends ThemeElement {
	static final long serialVersionUID = -849790252449006795L;

	private String url;
	private RenderedImage image;
	private ImageProvider provider;

	/**
	 * widget internal scale mode hint not to be mixed with stretchMode property
	 * which refers to skin model editing. This one initially created to hndle
	 * cases where static image needs to be tiled, stretched etc while
	 * repainting on widget side
	 */
	private String scaleMode;

	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url The url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
		image = createImage();
	}

	/**
	 * @return the provider
	 */
	public ImageProvider getProvider() {
		return provider;
	}

	/**
	 * @param provider the provider to set
	 */
	public void setProvider(ImageProvider provider) {
		this.provider = provider;
		image = null;
	}

	/**
	 * @return the image rendered on the particular device.
	 */
	public RenderedImage getImage() {
		return image;
	}

	public void setImage(RenderedImage image) {
		this.image = image;
	}

	/**
	 * Creates an image from the specific URL.
	 * 
	 * @param url the url of the image.
	 * @return the created image instance or null if the URL is invalid.
	 */
	protected RenderedImage createImage() {
		if (provider != null) {
			return provider.getImage();
		}
		if (url != null) {
			try {
				int scaleMode = CoreImage.KEEP_ORIGINAL;
				if ("stretch".equals(getScaleMode())) {
					scaleMode = CoreImage.STRETCH;
				} else if ("scale_best".equals(getScaleMode())) {
					scaleMode = CoreImage.SCALE_TO_BEST;
				} else if ("aspect_ratio".equals(getScaleMode())
						|| "scale_fit".equals(getScaleMode())) {
					scaleMode = CoreImage.SCALE_TO_FIT;
				}
				RenderedImage image = CoreImage.create().load(new URL(url),
						getBounds().width, getBounds().height, scaleMode)
						.getAwt();
				return image;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.widget.SComponent#paintDefault(java.awt.Graphics)
	 */
	protected void paintDefault(Graphics g) {
		if (!isVisible()) {
			return;
		}
		if (image == null) {
			image = createImage();
		}
		if (image != null) {
			((Graphics2D) g).drawRenderedImage(image,
					CoreImage.TRANSFORM_ORIGIN);
		}
	}

	/**
	 * @return the scaleMode
	 */
	public String getScaleMode() {
		return scaleMode;
	}

	/**
	 * @param scaleMode the scaleMode to set
	 */
	public void setScaleMode(String scaleMode) {
		if (scaleMode != this.scaleMode) {
			this.scaleMode = scaleMode;
			if (null != image && null != getUrl()) {
				setUrl(getUrl());
			}
		}
	}
}
