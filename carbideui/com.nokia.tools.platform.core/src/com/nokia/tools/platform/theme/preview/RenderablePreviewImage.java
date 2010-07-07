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
package com.nokia.tools.platform.theme.preview;

import java.awt.image.RenderedImage;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.platform.core.Display;

/**
 *		   To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class RenderablePreviewImage {
	private String screenName;

	private RenderedImage image;

	// sound file name
	private String soundFileName;

	private Display resolution;

	private int noOfPreviewElements = -1;

	public RenderablePreviewImage(String screenName, RenderedImage image,
			Display resolution) {

		this.screenName = screenName;

		this.image = image;

		this.resolution = resolution;
	}

	/**
	 * @return
	 */
	public RenderedImage getImage() {
		return image;
	}

	/**
	 * @param image
	 */
	public void setImage(RenderedImage image) {
		this.image = image;
	}

	/**
	 * @return
	 */
	public String getScreenName() {
		return screenName;
	}

	/**
	 * @param string
	 */
	public void setScreenName(String string) {
		screenName = string;
	}

	/**
	 * @return
	 */
	public String getSoundFileName() {
		return soundFileName;
	}

	/**
	 * @param string
	 */
	public void setSoundFileName(String string) {
		soundFileName = string;
	}

	public void disposeImage() {
		CoreImage.dispose(image);
	}

	/**
	 * @return Returns the resolution.
	 */
	public Display getResolution() {
		return resolution;
	}

	/**
	 * @param resolution The resolution to set.
	 */
	public void setResolution(Display resolution) {
		this.resolution = resolution;
	}

	public int getNoOfPreviewElements() {
		return this.noOfPreviewElements;

	}

	public void setNoOfPreviewElements(int noOfPreviewElements) {
		this.noOfPreviewElements = noOfPreviewElements;

	}
}
