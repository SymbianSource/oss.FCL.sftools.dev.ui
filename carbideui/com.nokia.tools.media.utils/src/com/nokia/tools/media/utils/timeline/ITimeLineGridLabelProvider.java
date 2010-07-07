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
package com.nokia.tools.media.utils.timeline;

import org.eclipse.swt.graphics.Color;

public interface ITimeLineGridLabelProvider {

	/**
	 * in each timeline redraw, first is called getLabel()
	 * for start time - most left visible point on timeline.
	 * 
	 * In this call, provider have possibility to adjust major and minor tick interval, 
	 * in grid data, accroding to current display width.
	 * 
	 * @param time
	 * @param gridData
	 * @return
	 */
	String getLabel(long time, IGridSettings gridData);

	Color getBackground();

}
