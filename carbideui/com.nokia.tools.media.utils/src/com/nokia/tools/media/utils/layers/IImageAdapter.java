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
package com.nokia.tools.media.utils.layers;

import java.awt.Rectangle;

public interface IImageAdapter {

	/**
	 * Returns IImage 
	 * @return
	 */
	IImage getImage();
	
	/**
	 * Returns IImage 
	 * @return
	 */
	IImage getImage(boolean supressImageLoad);
	
	/**
	 * Returns IImage with preferred dimensions
	 * @param width
	 * @param height
	 * @return
	 */
	IImage getImage(int width, int height);
	
	/**
	 * returns isAnimated()
	 * @return
	 */
	boolean isAnimated();
	
	boolean canBeAnimated();
	
	/**
	 * returns container screen resolution
	 * @return
	 */
	Rectangle getContainerScreenResolution();
	
}
