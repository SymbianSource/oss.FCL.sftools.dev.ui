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
package com.nokia.tools.media.utils.svg;

import java.util.List;

/**
 * Composite object which contains colors as well as gradients
 * 
 */

public class SvgColorComposite {

	private List<SvgColor> svgColors;

	private List<SvgGradient> svgGradients;

	public List<SvgColor> getSvgColors() {
		return svgColors;
	}

	public void setSvgColors(List<SvgColor> svgColors) {
		this.svgColors = svgColors;
	}

	public List<SvgGradient> getSvgGradients() {
		return svgGradients;
	}

	public void setSvgGradients(List<SvgGradient> svgGradients) {
		this.svgGradients = svgGradients;
	}

}
