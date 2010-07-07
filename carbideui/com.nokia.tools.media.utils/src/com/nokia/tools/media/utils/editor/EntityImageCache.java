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
package com.nokia.tools.media.utils.editor;

import java.awt.image.RenderedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.TimingModel;

public class EntityImageCache implements PropertyChangeListener {

	protected IImage image = null;

	protected WeakReference<Map<String, RenderedImage>> imageCache = new WeakReference<Map<String, RenderedImage>>(
			null);

	public EntityImageCache(IImage image) {
		this.image = image;
		this.image.addPropertyListener(this);
	}

	public RenderedImage getImage(TimingModel timing, long time, boolean preview) {
		if (image.isAnimationStarted()) {
			// look into cache
			Map<String, RenderedImage> cache = imageCache.get();
			if (cache != null) {
				String key = getImageCacheKey(timing, time, preview);
				RenderedImage img = cache.get(key);
				if (img != null) {
					// System.out.println("" + this + " cache hit: " + time);
					return img;
				}
			}

			// System.out.println("" + this + " cache miss: " + time);
			RenderedImage img = image.getAggregateImage(timing, time, preview);
			if (cache == null) {
				cache = new HashMap<String, RenderedImage>(50);
				imageCache = new WeakReference<Map<String, RenderedImage>>(
						cache);
			}
			String key = getImageCacheKey(timing, time, preview);
			cache.put(key, img);
			return img;
		} else {
			throw new RuntimeException("animation not started!");
		}
	}

	public void clear() {
		Map cache = imageCache.get();
		if (cache != null) {
			cache.clear();
		}
	}

	private String getImageCacheKey(TimingModel timing, long time,
			boolean preview) {
		StringBuffer sb = new StringBuffer();
		sb.append(timing).append(";").append(time).append(";").append(preview);
		return sb.toString();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		clear();
	}
}
