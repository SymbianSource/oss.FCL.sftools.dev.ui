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

public interface ITimeLineDataProvider {

	long getEndTime();

	long getStartTime();
	
	long getDisplayStart();

	long getDisplayWidth();

	long getInitialTime();
	
	boolean getShowGrid();
	
	boolean getShowGridHeader();
	
	int getMajorGridInterval();
	
	int getMinorGridInterval();
	
	int getClockIncrement();
	
	int getClockTimePerIncrement();
	
	boolean getClockAutorepeat();
	
	Object getInput();
	
	ITimeLabelProvider getTimeLabelProvider();
	
	ITimeLineGridLabelProvider getGridLabelProvider();
	
	void setShowAnimatedOnly(boolean b);

	IZoomProvider getZoomProvider();
}
