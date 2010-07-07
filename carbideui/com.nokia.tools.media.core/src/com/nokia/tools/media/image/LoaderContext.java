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
package com.nokia.tools.media.image;

import java.net.URL;

public class LoaderContext {
	private URL url;
	private int width;
	private int height;
	private int scaleMode;
	private boolean adjustSize;
	private int index;

	/**
	 * @return the url
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the scaleMode
	 */
	public int getScaleMode() {
		return scaleMode;
	}

	/**
	 * @param scaleMode the scaleMode to set
	 */
	public void setScaleMode(int scaleMode) {
		this.scaleMode = scaleMode;
	}

	/**
	 * @return the adjustSize
	 */
	public boolean isAdjustSize() {
		return adjustSize;
	}

	/**
	 * @param adjustSize the adjustSize to set
	 */
	public void setAdjustSize(boolean adjustSize) {
		this.adjustSize = adjustSize;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}
}
