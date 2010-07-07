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
package com.nokia.tools.media.player.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.net.URL;

import com.nokia.tools.media.player.IPaintAdapter;
import com.nokia.tools.media.player.IPlayer;

/**
 * Base class for the url based frame player. This class adjusts the player size
 * automatically when the drawing area changes.
 * 
 * 
 */
public abstract class URLFramePlayer extends IPlayer.Stub implements
		IPaintAdapter {
	protected static final Dimension DEFAULT_SIZE = new Dimension();
	private URL url;
	private Dimension size;
	private boolean isLoaded;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.IPlayer.Stub#doLoad(java.net.URL)
	 */
	@Override
	protected void doLoad(URL url) {
		dispose();

		this.url = url;
		try {
			isLoaded = doLoad();
		} catch (Exception e) {
			handleError(e);
		}
	}

	/**
	 * @return the url
	 */
	public URL getUrl() {
		return url;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.IPaintAdapter#paint(com.nokia.tools.media.utils.IPaintAdapter,
	 *      java.awt.Graphics)
	 */
	public final void paint(IPaintAdapter original, Graphics g) {
		Dimension newSize = null;

		if (original instanceof Component) {
			newSize = ((Component) original).getSize();
		} else if (g.getClipBounds() != null) {
			newSize = g.getClipBounds().getSize();
		}

		if (url == null || newSize == null || newSize.width <= 0
				|| newSize.height <= 0) {
			return;
		}

		if (size == null || (size != null && !size.equals(newSize))) {
			isLoaded = false;
		}

		size = newSize;

		if (!isLoaded) {
			try {
				isLoaded = doLoad();
			} catch (Throwable e) {
				handleError(e);
				isLoaded = true;
			}
		}

		try {
			if (!isPlayable() && original != null) {
				// use default renderer
				original.paint(null, g);
			} else {
				doPaint(original, g);
			}
		} catch (Throwable e) {
			handleError(e);
		}
	}

	/**
	 * Performs paint.
	 * 
	 * @param original the original painter.
	 * @param g the graphics context.
	 */
	protected abstract void doPaint(IPaintAdapter original, Graphics g)
			throws Exception;

	/**
	 * Loads the media content from the url.
	 */
	protected abstract boolean doLoad() throws Exception;

	/**
	 * @return true if the content is playable.
	 */
	protected boolean isContentPlayable() {
		return isLoaded;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.IPlayer#isPlayable()
	 */
	public final boolean isPlayable() {
		return super.isPlayable() && isContentPlayable();
	}

	public Dimension getSize() {
		return size;
	}
}
