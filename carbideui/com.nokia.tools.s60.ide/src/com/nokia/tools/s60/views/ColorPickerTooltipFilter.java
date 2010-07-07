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

package com.nokia.tools.s60.views;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.tooltip.DynamicTooltip;
import com.nokia.tools.media.utils.tooltip.IDynamicTooltipContributionFilter;

public class ColorPickerTooltipFilter implements
		IDynamicTooltipContributionFilter {

	public boolean accept(DynamicTooltip tooltip, Object entity,
			Object uiContainer, Object context, boolean focusState) {
		if (entity instanceof IContentData) {
			IContentData data = (IContentData) entity;
			IColorAdapter colorAdapter = (IColorAdapter) data
					.getAdapter(IColorAdapter.class);
			return colorAdapter != null;
		}
		return false;
	}
}
