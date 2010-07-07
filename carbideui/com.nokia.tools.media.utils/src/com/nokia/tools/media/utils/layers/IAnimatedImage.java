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

import com.nokia.tools.media.utils.timeline.cp.IControlPointModel;

public interface IAnimatedImage extends IImage {

	public IAnimationFrame getAnimationFrame(long time);

	public IAnimationFrame[] getAnimationFrames();
	
	public IAnimationFrame createNewAnimationFrame();
	
	public IAnimationFrame createNewAnimationFrame(String imagePath);
	
	public void addAnimationFrame(IAnimationFrame frame);

	public void removeAnimationFrame(IAnimationFrame frame);

	public void moveAnimationFrame(IAnimationFrame frame, int newPosition);
	
	public IControlPointModel getControlPointModel();

	public long getDefaultAnimateTime();

	public void setDefaultAnimateTime(long time);

}
