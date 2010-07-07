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

package com.nokia.tools.media.utils.timeline.impl.cp;

import org.eclipse.swt.graphics.Point;

import com.nokia.tools.media.utils.timeline.cp.IControlPoint;

public class ControlPoint implements IControlPoint {

	long time;

	Object data;

	Point coords = null;

	public ControlPoint(long time) {
		this.time = time;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long newTime) {
		this.time = newTime;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public long getTimeRelative() {
		return getTime();
	}

	public void setTimeRelative() {
		throw new RuntimeException("not_impl");
	}

	public boolean canBeDeleted() {
		return true;
	}

	public boolean canBeMoved() {
		return true;
	}

	public int compareTo(Object o) {
		if (o instanceof IControlPoint) {
			return getTime() > ((IControlPoint)o).getTime()?1:-1;
		}
		throw new RuntimeException("not-comparable");
	}
}
