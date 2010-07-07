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
package com.nokia.tools.theme.s60.editing.providers;

import com.nokia.tools.media.utils.timeline.IZoomProvider;

public class SimpleZoomProvider implements IZoomProvider {
	
	static final int LOWER_LIMIT = 100;
	static final int UPPER_LIMIT = 1000 * 60 * 60; //hour
	
	public void zoomIn(com.nokia.tools.media.utils.timeline.IDisplaySettings data) {
		long width = data.getDisplayWidthInTime();	
		width /= 2; 
		data.setDisplayWidthInTime(width > LOWER_LIMIT ? width : LOWER_LIMIT);
	};

	public void zoomOut(com.nokia.tools.media.utils.timeline.IDisplaySettings data) {
		long width = data.getDisplayWidthInTime();	
		width *= 2; 
		data.setDisplayWidthInTime(width < UPPER_LIMIT ? width : UPPER_LIMIT);
	};

}
