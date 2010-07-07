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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.nokia.tools.media.utils.timeline.cp.IControlPoint;
import com.nokia.tools.media.utils.timeline.cp.IControlPointListener;
import com.nokia.tools.media.utils.timeline.cp.IControlPointModel;

public class ControlPointModel implements IControlPointModel {

	protected List<IControlPoint> controlPoints = new ArrayList<IControlPoint>();

	protected List<IControlPointListener> listeners = new ArrayList<IControlPointListener>();
	
	protected boolean supressEvents;

	public IControlPoint createControlPoint(long time) {
		IControlPoint cp = new ControlPoint(time);
		synchronized (controlPoints) {
			controlPoints.add(cp);		
			notifyControlPointCreated(cp);
		}
		return cp;
	}

	public void removeControlPoint(IControlPoint cp) {
		synchronized (controlPoints) {
			controlPoints.remove(cp);
			notifyControlPointRemoved(cp);
		}
	}
	
	/**
	 * remove without property change event
	 * @param cp
	 */
	public void silentRemoveControlPoint(IControlPoint cp) {
		synchronized (controlPoints) {
			controlPoints.remove(cp);			
		}
	}

	public void removeAllControlPoints() {
		synchronized (controlPoints) {
			Iterator iter = controlPoints.iterator();
			while (iter.hasNext()) {
				IControlPoint cp = (IControlPoint) iter.next();
				iter.remove();
				notifyControlPointRemoved(cp);
			}
		}
	}

	public List<IControlPoint> getControlPoints() {
		synchronized (controlPoints) {
			return new ArrayList<IControlPoint>(controlPoints);
		}
	}

	public void addControlPointListener(IControlPointListener listener) {
		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}

	public void removeControlPointListener(IControlPointListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	protected void notifyControlPointCreated(final IControlPoint point) {
		List<IControlPointListener> listeners = new ArrayList<IControlPointListener>(this.listeners);
		
		if (listeners.size() == 0 || supressEvents) {					
			return;
		}			
		
		for (IControlPointListener listener : listeners) {
			try {
				listener.controlPointCreated(point);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void notifyControlPointMoved(final IControlPoint point) {
		List<IControlPointListener> listeners = new ArrayList<IControlPointListener>(this.listeners);
		
		if (listeners.size() == 0 || supressEvents) {
			return;
		}
		
		for (IControlPointListener listener : listeners) {
			try {
				listener.controlPointMoved(point);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void notifyControlPointRemoved(final IControlPoint point) {
		List<IControlPointListener> listeners = new ArrayList<IControlPointListener>(this.listeners);
		
		if (listeners.size() == 0 || supressEvents) {
			return;
		}
		
		for (IControlPointListener listener : listeners) {
			try {
				listener.controlPointRemoved(point);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void notifyControlPointSelected(final IControlPoint point) {
		List<IControlPointListener> listeners = new ArrayList<IControlPointListener>(this.listeners);
		
		if (listeners.size() == 0 || supressEvents) {
			return;
		}
		
		for (IControlPointListener listener : listeners) {
			try {
				listener.controlPointSelected(point);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void moveControlPoint(IControlPoint movedControlPoint, long time) {
		movedControlPoint.setTime(time);
		notifyControlPointMoved(movedControlPoint);
	}

	/* (non-Javadoc)
	 * @see com.nokia.tools.media.utils.timeline.cp.IControlPointModel#getControlPoint(int)
	 */
	public IControlPoint getControlPoint(int index) {
		return controlPoints.get(index);
	}

	public int getControlPointsCount() {
		return controlPoints.size();
	}

	/* (non-Javadoc)
	 * @see com.nokia.tools.media.utils.timeline.cp.IControlPointModel#findControlPointAt(long)
	 */
	public IControlPoint findControlPointAt(long time) {
		for (IControlPoint p:controlPoints) {
			if (p.getTime() == time) {
				return p;
			}
		}
		return null;
	}

	public void controlPointSelected(IControlPoint controlPoint) {
		notifyControlPointSelected(controlPoint);
	}
	
}
