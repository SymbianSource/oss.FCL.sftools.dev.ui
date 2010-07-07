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
import com.nokia.tools.media.utils.tooltip.DynamicTooltip;
import com.nokia.tools.media.utils.tooltip.IDynamicTooltipContributionFilter;

public class IconTooltipFilter implements IDynamicTooltipContributionFilter {
	
	boolean forceFalse = false;

	public boolean accept(DynamicTooltip tooltip, Object entity, Object uiContainer,
			Object context, boolean focusState) {
		
		if (!(entity instanceof IContentData)) {
			return false;
		}
		
		// recursive check if there is one or more other tooltip contributions
				
		if (forceFalse) { 
			return false;
		}
		
		forceFalse = true;
		try {
			if (tooltip.getContributionsCount(false) == 0) {
				return false;
			}
		} finally {
			forceFalse = false;
		}
		
		return true;
	}

}
