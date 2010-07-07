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

import org.eclipse.jface.viewers.Viewer;

import com.nokia.tools.media.utils.timeline.cp.IControlPoint;


public interface ITimeLine {

	public void initialize(long startTime, long endTime, long displayWidth);

	public void initialize(ITimeLineDataProvider dataProvider);

	public ITimeLineRow[] getRows();

	public void setRows(ITimeLineRow[] rows);

	public void addRow(ITimeLineRow row);

	public void addRow(ITimeLineRow row, int index);

	public void removeRow(ITimeLineRow row);

	public long getEndTime();

	public long getStartTime();

	public long getCurrentTime();

	public void setCurrentTime(long currentTime);

	public ITimer getTimer();

	public IDisplaySettings getDisplayData();

	public IGridSettings getGridData();

	public void setGridLabelProvider(ITimeLineGridLabelProvider provider);

	public void setTimeLabelProvider(ITimeLabelProvider timeLabelProvider);

	public void addTimeListener(ITimeListener listener);

	public void removeTimeListener(ITimeListener listener);

	public void repaint();

	public void addSelectionListener(ISelectionListener listener);

	public void removeSelectionListener(ISelectionListener listener);

	public Viewer getViewer();
	
	public IControlPoint getCurrentControlPoint();
	
	public ITimeLineDataProvider getDataProvider();

}
