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

public interface IControlPoint extends Comparable {

	/**
	 * returns relative offset of control point to start of current anim.
	 * segment
	 * 
	 * @return
	 */
	long getTimeRelative();

	/**
	 * sets relative offset of control point to start of current anim. segment
	 */
	void setTimeRelative();

	long getTime();

	void setTime(long newTime);

	Object getData();

	void setData(Object data);

	boolean canBeMoved();

	boolean canBeDeleted();
}
