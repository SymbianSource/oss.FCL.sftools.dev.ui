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
package com.nokia.tools.resource.util;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * The util class for conversion between the awt a swt bounds
 */
public class BoundsConverterUtil {

	/**
	 * Converts the AWT Rectangle to the SWT one
	 * 
	 * @param awtRect
	 * @return
	 */
	public static Rectangle toSWTRectangle(java.awt.Rectangle awtRect) {
		return new Rectangle(awtRect.x, awtRect.y, awtRect.width,
				awtRect.height);
	}

	public static Rectangle toSWTRectangle(
			org.eclipse.draw2d.geometry.Rectangle draw2dRect) {
		return new Rectangle(draw2dRect.x, draw2dRect.y, draw2dRect.width,
				draw2dRect.height);
	}

	/**
	 * Converts the AWT Point to the SWT one
	 * 
	 * @param point
	 * @return
	 */
	public static Point toSWTPoint(java.awt.Point point) {
		return new Point(point.x, point.y);
	}
}
