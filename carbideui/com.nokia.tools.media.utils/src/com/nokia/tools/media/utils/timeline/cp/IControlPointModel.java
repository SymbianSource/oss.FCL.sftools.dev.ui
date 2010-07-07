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

package com.nokia.tools.media.utils.timeline.cp;

import java.util.List;

public interface IControlPointModel {

	IControlPoint createControlPoint(long time);

	void removeControlPoint(IControlPoint cp);

	void removeAllControlPoints();

	List<IControlPoint> getControlPoints();
	
	IControlPoint getControlPoint(int index);
	
	int getControlPointsCount();

	void addControlPointListener(IControlPointListener listener);

	void removeControlPointListener(IControlPointListener listener);

	void moveControlPoint(IControlPoint movedControlPoint, long time);
	
	IControlPoint findControlPointAt(long time);

	void controlPointSelected(IControlPoint controlPoint);
}
