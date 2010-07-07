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

package com.nokia.tools.media.utils.timeline.impl;

import java.util.Comparator;

import com.nokia.tools.media.utils.timeline.ITimeLineNode;

public class TimeLineNodeComparator implements Comparator<ITimeLineNode> {

	public int compare(ITimeLineNode o1, ITimeLineNode o2) {
		long diff = o1.getStartTime() - o2.getStartTime();
		if (diff == 0) {
			diff = o1.getEndTime() - o2.getEndTime();
		}
		return (int) diff;
	}

}
