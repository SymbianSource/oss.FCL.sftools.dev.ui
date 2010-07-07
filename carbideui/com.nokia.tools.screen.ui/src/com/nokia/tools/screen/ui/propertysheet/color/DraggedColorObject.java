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

package com.nokia.tools.screen.ui.propertysheet.color;

import org.eclipse.swt.graphics.RGB;

import com.nokia.tools.media.utils.svg.ColorGroups;

public class DraggedColorObject {
	String name;
	RGB color;
	ColorGroups grps;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RGB getColor() {
		return color;
	}

	public void setColor(RGB color) {
		this.color = color;
	}

	public ColorGroups getGrps() {
		return grps;
	}

	public void setGrps(ColorGroups grps) {
		this.grps = grps;
	}
}
