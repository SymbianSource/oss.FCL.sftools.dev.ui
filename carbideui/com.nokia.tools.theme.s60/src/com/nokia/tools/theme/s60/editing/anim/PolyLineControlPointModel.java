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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.nokia.tools.media.utils.timeline.cp.IControlPoint;
import com.nokia.tools.media.utils.timeline.impl.cp.ControlPoint;
import com.nokia.tools.media.utils.timeline.impl.cp.ControlPointModel;
import com.nokia.tools.theme.s60.effects.EffectObject;
import com.nokia.tools.theme.s60.effects.EffectParameter;
;

/**
 * 
 * Control point model that implements linear interpolation between 
 * control points. 
 *
 */
public class PolyLineControlPointModel<T> extends ControlPointModel {
	
	//value mediator for interpolation computing
	private IValueMediator mediator;
	
	private EffectObject eObj;
	
	List<Integer> params;

	public PolyLineControlPointModel(EffectObject eObj, IValueMediator mediator) {
		this.mediator = mediator;
		this.eObj = eObj;
	}	
	
	@Override
	public synchronized IControlPoint createControlPoint(long time) {			
		return createControlPoint(time, true);
	}
	
	@Override
	public void moveControlPoint(IControlPoint movedControlPoint, long time) {
		
		if (movedControlPoint.getTime() == 0) //cannot move first
			return;
		
		if (time < 10) //cannot move point to close to 0 time
			return;		
		
		movedControlPoint.setTime(time);		
		Collections.sort(controlPoints);		
		notifyControlPointMoved(movedControlPoint);
	}
	
	@Override
	public void removeControlPoint(IControlPoint cp) {
		
		if (cp.getTime() == 0) {
			if (controlPoints.size() != 1)  {				
				removeUnusedPoints();
				if (controlPoints.size() != 1)
					return; //cannot delete first, when there are more than one left
							//first point is deleted by deleting last no-first point.
			}
		}
		
		super.removeControlPoint(cp);
		
		if (controlPoints.size() == 1) {
			//remove also first
			silentRemoveControlPoint(controlPoints.get(0));
		}
	}
	
	/**
	 * gets data from given point
	 * @param cpi
	 * @return
	 */
	public T getControlPointData(int cpi) {
		return (T) controlPoints.get(cpi).getData();
	}
	
	/**
	 * sets data for given point
	 * @param cpi
	 * @param data
	 */
	public void setControlPointData(int cpi, T data) {
		controlPoints.get(0).setData(data);
	}
	
	/**
	 * return value of parameter in given time using given mediator
	 * @param time
	 * @return
	 */
	public float getValue(long time) {
		IControlPoint points[] = getPointsInvolved(time);
		long vals[][] = new long[points.length][2];
		for (int x = 0; x < points.length; x++) {
			vals[x][0] = points[x].getTime();
			vals[x][1] = (long) mediator.getValue(points[x].getData());
		}
		float resultValue = LinearInterpolation.getInterpolatedValue(vals, (int) time);
		return Math.round(resultValue);
	}
	
	/**
	 * return value of parameter in given time using given mediator
	 * @param time
	 * @return
	 */
	public float getValue(long time, int paramPos) {					
		IControlPoint points[] = getPointsInvolved(time, paramPos);
		
		if (points.length == 0)
			return 0f;
		long vals[][] = new long[points.length][2];
		for (int x = 0; x < points.length; x++) {
			vals[x][0] = points[x].getTime();
			vals[x][1] = (long) mediator.getValue(points[x].getData(), paramPos);
		}
		float resultValue = LinearInterpolation.getInterpolatedValue(vals, time);
		
		return Math.round(resultValue);
	}
	
	/**
	 * gets control point value in given point, for single-param 
	 * @param cpIndex
	 * @return
	 */
	public float getControlPointValue(int cpIndex) {
		try {
			return mediator.getValue(controlPoints.get(cpIndex).getData());
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * gets control point value in given point, for single-param 
	 * @param cpIndex
	 * @return
	 */
	public float getControlPointValue(IControlPoint point) {
		try {
			return mediator.getValue(point.getData());
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * gets control point value in given point, for multi-params 
	 * @param cpIndex
	 * @return
	 */
	public float getControlPointValue(int cpIndex, int paramPos) {
		try {
			return mediator.getValue(controlPoints.get(cpIndex).getData(), paramPos);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * gets control point value in given point, for multi-params 
	 * @param cpIndex
	 * @return
	 */
	public float getControlPointValue(IControlPoint point, int paramPos) {
		try {
			return mediator.getValue(point.getData(), paramPos);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * Returns index of control point, whiich is nearest to given time
	 */
	protected int getNearestControlPointIndex(long time) {		
		return getNearestControlPointIndex(controlPoints, time);
	}
	
	/**
	 * Returns index of control point, whiich is nearest to given time
	 */
	protected int getNearestControlPointIndex(List<IControlPoint> cps, long time) {		
		if (cps.size() == 0)
			return -1;
		if (cps.size() == 1)
			return 0;
		int selected = 0;
		long delta = Math.abs(time - cps.get(0).getTime());
		for (IControlPoint p:cps) {
			long d = Math.abs(time - p.getTime());
			if (d < delta) {
				delta = d;
				selected = cps.indexOf(p);
			}
		}
		return selected;
	}
	
	/**
	 * Returns index of control point, which is nearest to given time
	 */
	protected int getNearestControlPointIndex(List<IControlPoint> cps, long time, int paramPos) {		
		if (cps.size() == 0)
			return -1;
		if (cps.size() == 1)
			if (mediator.isParamSet(cps.get(0).getData(), paramPos))
				return 0;
		int selected = 0;
		boolean set = false;
		long delta = Math.abs(time - cps.get(0).getTime());
		for (IControlPoint p:cps) {
			if (mediator.isParamSet(p.getData(),paramPos)) {
				long d = Math.abs(time - p.getTime());
				if (d < delta) {
					delta = d;
					selected = cps.indexOf(p);
					set = true;
				}
			}
		}
		if (set)
			return selected;
		else 
			return -1;
	}
	
	/**
	 * Returns index of control point, which is nearest to given time
	 */
	protected int getNearestControlPointIndex(long time, int paramPos) {		
		return getNearestControlPointIndex(controlPoints, time, paramPos);
	}
	
	/* 
	 * returns list of CP's involved for param with given index.
	 */
	public List<IControlPoint> getControlPointForParam(int ppos) {
		ArrayList<IControlPoint> list = new ArrayList<IControlPoint>();
		for (IControlPoint xx: controlPoints) {
			if (mediator.isParamSet(xx.getData(), ppos))
				list.add(xx);
		}
		return list;
	}
	
	private static final IControlPoint[] EMPTY_CP_SET = new IControlPoint[0];

	/**
	 * returns point in given list involved in interpolation for given time
	 */
	protected IControlPoint[] getPointsInvolved(List<IControlPoint> cps, long time) {
		int nearest = getNearestControlPointIndex(cps, time);
		if (nearest == -1)
			return EMPTY_CP_SET;
		if (cps.size() == 1) {
			return new IControlPoint[]{cps.get(0)};
		}

		IControlPoint np = cps.get(nearest);
		if (np.getTime() > time) {
			//time point before CP
			if (nearest > 0) {
				return new IControlPoint[]{cps.get(nearest - 1), np};
			} else {
				//nearest == first
				return new IControlPoint[]{np};
			}
		} else {
			//time point after CP
			if (nearest < cps.size() - 1) {
				return new IControlPoint[]{np, cps.get(nearest + 1)};
			} else {
				//nearest == last
				return new IControlPoint[]{np};
			}
		}
	}
	
	/**
	 * returns point involved in interpolation for given time for given param
	 */
	protected IControlPoint[] getPointsInvolved(long time, int paramPos) {
		return getPointsInvolved(getControlPointForParam(paramPos), time);
	}
	
	/**
	 * returns point involved in interpolation for given time
	 */
	protected IControlPoint[] getPointsInvolved(long time) {
		return getPointsInvolved(controlPoints, time);
	}

	public IValueMediator getMediator() {
		return mediator;
	}

	/*
	 * silent create, without checks and special handling for first point.
	 */
	public IControlPoint silentCreateControlPoint(long time) {		
		IControlPoint cp = new ControlPoint(time);
		cp.setData(mediator.createCPDataObject());
		synchronized (controlPoints) {
			controlPoints.add(cp);
			Collections.sort(controlPoints);		
		}
		return cp;
	}
	
	public void removeUnusedPoints() {
		Stack<IControlPoint> t = new Stack<IControlPoint>();
		for (IControlPoint cp : controlPoints) {
			if (mediator.isEmpty(cp.getData())) {
				t.push(cp);
			}
		}
		while(!t.isEmpty())
			controlPoints.remove(t.pop());
	}

	public List<Integer> getParams() {
		return params;
	}

	public void setParams(List<Integer> params) {
		this.params = params;
	}

	/**
	 * 'initialize' indicating that in newly created points, 
	 * they shoud be marked as 'set' for animated parameters, with value = 0;
	 * @param time
	 * @param initialize
	 * @return
	 */
	public synchronized IControlPoint createControlPoint(long time, boolean initialize) {			
		
		if (findControlPointAt(time) != null)
			return findControlPointAt(time);
		
		/* forbidden to create CP's at '0' */
		if (time == 0) 
			return null;
		
		IControlPoint cp = new ControlPoint(time);
		cp.setData(mediator.createCPDataObject());
		
		synchronized (controlPoints) {
			controlPoints.add(cp);
			Collections.sort(controlPoints);
			notifyControlPointCreated(cp);
		}

		if (initialize) {
			Map<Object, Object> parList =  eObj.getParameterList();
			for (Object obj : parList.keySet()) {
				EffectParameter param = eObj.getParameter((String) obj);
				if (param.isAnimated()) {
					int index = param.getParameterIndex();
					if (params.contains(index)) {
						if (!((TSCPData) cp.getData()).isSet(index)) {
							((TSCPData) cp.getData()).setValue(index, 0);
						}
					}
				}
			}
		}
				
		if (findControlPointAt(0) == null) {
			//create another CP at time offset '0' and set there all parameters as set
			IControlPoint zero = new ControlPoint(0);
			zero.setData(mediator.createCPDataObject());
			mediator.setAll(zero.getData(), 0f);
			synchronized (controlPoints) {
				controlPoints.add(0, zero); //always first				
			}
		}
		
		return cp;		
	}
	
}
