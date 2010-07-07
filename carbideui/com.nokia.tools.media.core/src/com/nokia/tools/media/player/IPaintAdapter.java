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
package com.nokia.tools.media.player;

import java.awt.Graphics;

public interface IPaintAdapter {
	/**
	 * Performs painting routing. The default one can be invoked also by calling
	 * paint on the original painter.
	 * 
	 * @param original the orginal painter.
	 * @param g the graphics context.
	 */
	void paint(IPaintAdapter original, Graphics g);
}
