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
package com.nokia.tools.theme.s60.editing.anim;

import java.util.ArrayList;
import java.util.List;

import com.nokia.tools.media.utils.timeline.cp.IControlPoint;
import com.nokia.tools.media.utils.timeline.cp.IControlPointListener;
import com.nokia.tools.media.utils.timeline.cp.IControlPointModel;

/**
 * ControlPointModel wrapper that is filtering control points to those, 
 * which has set values in selected parameters, given by index.
 *
 */
public class CPMFilterWrapper implements IControlPointModel {
	
	PolyLineControlPointModel wrapped;
	
	List<Integer> params;
	
	public CPMFilterWrapper(PolyLineControlPointModel c) {
		wrapped = c;
	}

	public IControlPoint createControlPoint(long time) {
		return wrapped.createControlPoint(time);
	}

	public void removeControlPoint(IControlPoint cp) {
		wrapped.removeControlPoint(cp);
	}

	public void removeAllControlPoints() {
		wrapped.removeAllControlPoints();
	}

	public List<IControlPoint> getControlPoints() {
		if (params == null) {
			return wrapped.getControlPoints();
		} else {
			List<IControlPoint> pts = wrapped.getControlPoints();
			List<IControlPoint> f = new ArrayList<IControlPoint>();
			for (IControlPoint x:pts) {
				TSCPData data = (TSCPData) x.getData();
				for (Integer index : params) {
					if (data.isSet(index.intValue())) {
						f.add(x);
						break;
					}
				}
			}
			return f;
		}
	}

	public IControlPoint getControlPoint(int index) {
		return getControlPoints().get(index);
	}

	public int getControlPointsCount() {
		return getControlPoints().size();
	}

	public void addControlPointListener(IControlPointListener listener) {
		wrapped.addControlPointListener(listener);
	}

	public void removeControlPointListener(IControlPointListener listener) {
		wrapped.removeControlPointListener(listener);
	}

	public void moveControlPoint(IControlPoint movedControlPoint, long time) {
		wrapped.moveControlPoint(movedControlPoint, time);
	}

	public IControlPoint findControlPointAt(long time) {
		return wrapped.findControlPointAt(time);
	}

	public void controlPointSelected(IControlPoint controlPoint) {
		wrapped.controlPointSelected(controlPoint);
	}

	public List<Integer> getParams() {
		return params;
	}

	public void setParams(List<Integer> params) {
		this.params = params;
		wrapped.setParams(params);
	}

	public PolyLineControlPointModel getWrapped() {
		return wrapped;
	}

	public void setWrapped(PolyLineControlPointModel wrapped) {
		this.wrapped = wrapped;
	}

}
