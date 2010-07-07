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
package com.nokia.tools.theme.s60.editing.providers.realtime;

import java.util.List;

import com.nokia.tools.media.utils.timeline.ITimeLineRow;
import com.nokia.tools.media.utils.timeline.cp.IControlPoint;
import com.nokia.tools.theme.s60.editing.providers.BaseTimeLineNode;

/**
 * 
 * TimeLineNode implementation with effect-specific features
 */
public class RealtimeTimeLineNode extends BaseTimeLineNode {

	public RealtimeTimeLineNode(ITimeLineRow row) {
		super(row);
	}

	@Override
	public long getStartTime() {
		return 0;
	}

	@Override
	public long getEndTime() {
		if (getControlPointModel() == null) {
			return 0;
		}
		List<IControlPoint> controlPoints = getControlPointModel()
				.getControlPoints();
		//points are sorted
		return controlPoints.size() == 0 ? 0 : controlPoints.get(controlPoints.size()-1).getTime();
	}	
	
}
