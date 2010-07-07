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

package com.nokia.tools.platform.theme;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.util.List;

import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.preview.PreviewElement;

public interface ILayeredImageCompositor {
	Color BASE_COLOR = Color.WHITE;
	
	RenderedImage ProcessList(SkinnableEntity entity, Layout layout,
			Display display, List list, boolean softMask, boolean applyMask,
			int elementParam, PreviewElement preElem) throws ThemeException;
}
