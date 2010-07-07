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
package com.nokia.tools.theme.s60.effects;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

import com.nokia.tools.media.utils.layers.IEffectParameter;
import com.nokia.tools.media.utils.layers.IEffectParameterDescriptor;
import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.cp.IControlPoint;
import com.nokia.tools.platform.theme.EffectConstants;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.theme.s60.S60ThemePlugin;
import com.nokia.tools.theme.s60.editing.anim.IValueMediator;
import com.nokia.tools.theme.s60.editing.anim.PolyLineControlPointModel;
import com.nokia.tools.theme.s60.editing.anim.TSCPData;
import com.nokia.tools.theme.s60.morphing.AnimationConstants;
import com.nokia.tools.theme.s60.morphing.timemodels.RealTimingModel;
import com.nokia.tools.theme.s60.morphing.timemodels.RelativeTimingModel;

public class EffectParameter implements Cloneable, IEffectParameter {

	PropertyChangeSupport propSup = new PropertyChangeSupport(this);

	public void addPropertyListener(PropertyChangeListener ps) {
		propSup.addPropertyChangeListener(ps);
	}

	public void removePropertyChangeListener(PropertyChangeListener ps) {
		propSup.removePropertyChangeListener(ps);
	}

	private EffectObject parent;

	private HashMap<Object, Object> attributeMap = new HashMap<Object, Object>();

	// holds value from which has been imported data for control point model
	private HashMap<String, String> valueModelMap = new HashMap<String, String>();

	// holds timing model props except duration - duration is detected from
	// control point model
	private HashMap <String, String>timingModelMap = new HashMap<String, String>();

	// *** animation data for this parameter ****

	// animate
	private boolean isAnimated;

	// parameter index - for control point model. Decided from EffectDescriptor
	private int parameterIndex;

	// if anim init done
	private boolean animationInitDone;

	/**
	 * when animation is on and should return animated values if true,
	 * getValue() returns current animated value, if param is animated
	 */
	//private boolean animationInProgress;

	//public static Boolean isPreview = false;

	public EffectParameter(EffectObject parent) {
		this.parent = parent;
		
		//_animationTime = 0;
		_calendarInstance = Calendar.getInstance();
		_calendarInstance.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public EffectParameter Clone() {
		try {			
			EffectParameter param = (EffectParameter) this.clone();			
			param.propSup = new PropertyChangeSupport(param);
			param._supressEvents  = true;
			param.parent = null;
			param.replaceValueMap((HashMap) param.getValueModelMap().clone());
			param.replaceTimeMap((HashMap) param.getTimingModelMap().clone());
			param._supressEvents  = false;
			return param;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public void setValue(String newValue) {
		Object old = attributeMap.get(EffectConstants.ATTR_VALUE);
		attributeMap.put(EffectConstants.ATTR_VALUE, newValue);
		propSup.firePropertyChange(PROPERTY_VALUE_NON_ANIMATED, old, newValue);
	}

	public int getValueAsInt() {
		try {
			return Integer.parseInt(getValue());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Parse error: " + getValue()
					+ " is not a number");
		}
	}

	public String getValue() {

		if (isAnimated) {
			// animation running
			return getValue(_animationTime);
		}
		
		//non -animated
		return _getValue();
	}

	/**
	 * returns value of parameter in given time
	 * 
	 * @param time
	 * @return
	 */
	public String getValue(long time) {
		if (isAnimated) {
			
			/*
			 * getTimingModel() - returns parameter's timing model, if specified
			 * _animationTiming - timing model of current animation we preview.
			 * (if animation's model is Relative, we don't show realtime effects) 
			 */
			TimingModel tModel = getTimingModel();
			
			if (tModel == TimingModel.RealTime || tModel == null) {
				//param is animated realtime - show animated only in _timing = realtime
				if (_animationTiming == TimingModel.RealTime || null == _animationTiming) {
					try {
						int val = (int) getParent().getControlPointModel().getValue(
								time, getParameterIndex());						
						return Integer.toString(val);
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			} else if (tModel == TimingModel.Relative) {
				
				//param is animated relative				
				long timestamp = _calendarInstance.getTimeInMillis();				
				long loopdur = getAnimationDuration(tModel);
				long offset = (timestamp == 0 ? timestamp : timestamp - 1) % loopdur;
				try {
					int val = (int) getParent().getControlPointModel().getValue(
							offset, getParameterIndex());
					return Integer.toString(val);
				} catch (Exception e) {
					e.printStackTrace();
				} 			
			}			
		}
		
		// non -animated
		return _getValue();
	}

	/**
	 * returns non-animated value
	 * 
	 * @return
	 */
	private String _getValue() {
		String value = (String) attributeMap.get(EffectConstants.ATTR_VALUE);
		if (StringUtils.isEmpty(value)) {			
			return (String) attributeMap.get(EffectConstants.ATTR_DEFAULTVALUE);
		} else {
			return value;
		}
	}

	public void setAttributes(HashMap<Object, Object> attMap) {
		attributeMap = attMap;
	}

	public HashMap<Object, Object> getAttributeMap() {
		return attributeMap;
	}

	public String getName() {
		return (String) attributeMap.get(EffectConstants.ATTR_UINAME);
	}

	public boolean isAnimated() {
		return isAnimated;
	}

	public void setAnimated(boolean isAnimated) {
		if (this.isAnimated != isAnimated) {
			this.isAnimated = isAnimated;
			
			if (isAnimated == true) {
				if (getTimingModel() == TimingModel.Undefined) {
					//set default timing model
					timingModelMap.put(AnimationConstants.ATTR_NAME, AnimationConstants.REAL_TIMING_MODEL);
				}
			} else {
				//clean control points from this parameter
				PolyLineControlPointModel cpm = parent.getControlPointModel();
				for (Object cp: cpm.getControlPointForParam(getParameterIndex())) {
					cpm.getMediator().unset(((IControlPoint)cp).getData(), getParameterIndex());
				}
				cpm.removeUnusedPoints();
				if (cpm.getControlPointsCount() < 2)
					cpm.removeAllControlPoints();
			}
			// notify parent
			if (!_supressEvents) {
				parent.parameterStateChanged(this);
				propSup.firePropertyChange(PROPERTY_ANIMATED, null, Boolean
					.toString(isAnimated));
			}
		}
	}

	public synchronized void initAnimationModel() {
		if (!animationInitDone) {
			
			// fill control point model
			long cpdata[][] = parseValueModelData();
			
			PolyLineControlPointModel cpm = getParent().getControlPointModel();
			for (int i = 0; i < cpdata.length; i++) {
				IControlPoint cp = cpm.findControlPointAt(cpdata[i][0]);
				if (cp == null) {
					cp = cpm.silentCreateControlPoint(cpdata[i][0]);
				}
				TSCPData data = (TSCPData) cp.getData();
				data.setValue(getParameterIndex(), (int) cpdata[i][1]);
			}
						
			animationInitDone = true;
		}
	}

	public HashMap<String, String> getValueModelMap() {
		return valueModelMap;
	}

	public void setValueModelMap(HashMap<String, String> animationMap) {		
		this.valueModelMap.putAll(animationMap);
		if (animationMap != null && !_supressEvents)
			if (animationMap.size() > 0)
				parent.parameterAnimationStateChanged(this);
	}

	public void replaceValueMap(HashMap<String, String> animationMap) {
		this.valueModelMap = animationMap;
	}

	public void replaceTimeMap(HashMap<String, String> animationMap) {
		this.timingModelMap = animationMap;
	}

	public HashMap<String, String> getTimingModelMap() {
		return timingModelMap;
	}
	
	/**
	 * returns timespan for relative timing model
	 * @return
	 */
	public TimeSpan getTimeSpan() {
		if (getTimingModel() == TimingModel.Relative) {
			try {
				int span = Integer.parseInt((String) timingModelMap.get(AnimationConstants.TIMESPAN));
				return TimeSpan.values()[span];
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		return null;
	}
	
	public TimingModel getTimingModel() {		
		if (timingModelMap == null || !isAnimated)
			return TimingModel.Undefined;
		if (AnimationConstants.REAL_TIMING_MODEL.equals(timingModelMap.get(AnimationConstants.ATTR_NAME)))
			return TimingModel.RealTime;
		if (AnimationConstants.RELATIVEMODEL_NAME.equals(timingModelMap.get(AnimationConstants.ATTR_NAME)))
			return TimingModel.Relative;
		return TimingModel.Undefined;
	}

	public void setTimingModelMap(HashMap<String, String> timingModelMap) {
		
		long oldDuration = getAnimationDuration(getTimingModel());
		TimingModel oldTimingModel = getTimingModel();		
		this.timingModelMap.putAll(timingModelMap);
		
		if (oldTimingModel == getTimingModel()) {
			//rescale Cp's			
			if (timingModelMap.get(AnimationConstants.TIMESPAN)!=null) { 
				//timespan changed
				//rescale control points
				if (parent != null)
					if (parent.getControlPointModel() != null) {
						long newDuration = getAnimationDuration(getTimingModel());
						if (oldDuration != newDuration && newDuration != 0 && oldDuration != 0) {
							float ratio = newDuration / (float)oldDuration;
							PolyLineControlPointModel cpm = parent.getControlPointModel();
							List<IControlPoint> list = cpm.getControlPointForParam(getParameterIndex());
							for (IControlPoint p: list) {
								long time = p.getTime();
								if (time > 0) {
									TSCPData data = (TSCPData) p.getData();
									data.unset(getParameterIndex());
									long newtime = (long) (time * ratio);
									//create new CP at that time
									IControlPoint point = cpm.silentCreateControlPoint(newtime);
									//set its value
									TSCPData newdata = (TSCPData) point.getData();
									newdata.setValue(getParameterIndex(), data.getValue(getParameterIndex()));
								}
							}
							//remove unused points
							cpm.removeUnusedPoints();
						}
					}
			}
		} else {
			//timing model changed - clear CP's?
			if (parent != null) {
				if (parent.getControlPointModel() != null) {
					List<IControlPoint> list = parent.getControlPointModel().getControlPointForParam(getParameterIndex());
					for (IControlPoint p: list) {									
						TSCPData data = (TSCPData) p.getData();
						data.unset(getParameterIndex());						
					}
					//remove unused points
					parent.getControlPointModel().removeUnusedPoints();
				}
			}			
		}
		
		if (!_supressEvents) {
			propSup.firePropertyChange(PROPERTY_TIMING, null, timingModelMap);
			if (timingModelMap != null)
				if (timingModelMap.size() > 0)
					parent.parameterAnimationStateChanged(this);
		}
	}

	/**
	 * keeps current timestamp for preview
	 * for realtime - animStartLocation + offset
	 * for relative - value set in setAnimationTime
	 */
	private Calendar _calendarInstance;
	/**
	 * for realtime - time offset
	 * for relative - 0
	 */
	private long _animationTime;
	
	/**
	 * if preview is in realtime, or relative timing. Rendering slightly differs
	 */
	private TimingModel _animationTiming;
	

	public void startAnimation() {		
	}

	public void endAnimation() {		
	}

	/**
	 * sets animation time to given offset - affect time and value models, and
	 * subsequent calls to getValue() return approprite value for this
	 * timeOffset
	 * 
	 * @param time
	 */
	public void setAnimationTime(TimingModel timing, long time) {	
		
		_animationTiming = timing;
		_animationTime = time;
							
		if (TimingModel.RealTime == timing) {
			//for realtime, timeOffset is offset only
			_animationTime = time;
			_calendarInstance.setTimeInMillis(getAnimationStartLocation()
					+ time);
		} else if (TimingModel.Relative == timing) {
			//relative timing, offset = timestamp
			_animationTime = 0;
			_calendarInstance.setTimeInMillis(time);
		}		
	}

	private long getAnimationStartLocation() {
		return parent.getAnimationStartLocation();
	}

	public EffectObject getParent() {
		return parent;
	}

	protected void setParent(EffectObject parent) {
		this.parent = parent;
		// find out param index for this param
		parameterIndex = getParent().getDescriptor().getParameters().indexOf(
				getParent().getDescriptor().getParameterDescriptor(getName()));
	}

	/**
	 * returns control point array for this parameter (it computes these values
	 * from ControlPointModel from parent) Values are - [0] represents time in
	 * millis, [1] value at given point
	 */
	protected int[][] getValueModelData() {
		if (isAnimated) {
			PolyLineControlPointModel cpm = getParent().getControlPointModel();
			int paramOffset = getParameterIndex();
			List<IControlPoint> involved = cpm
					.getControlPointForParam(paramOffset);
			int data[][] = new int[involved.size()][2];
			for (int x = 0; x < involved.size(); x++) {
				data[x][0] = (int) involved.get(x).getTime();
				data[x][1] = (int) cpm.getControlPointValue(involved.get(x),
						paramOffset);
			}
			return data;
		} else {
			throw new RuntimeException("not_animated");
		}
	}

	/**
	 * read value/time model and parse it's data for use with control point
	 * model
	 * 
	 * @return
	 */
	protected long[][] parseValueModelData() {

		if (valueModelMap.size() == 0) {
			// value map not set
			return new long[0][0];
		}
		
		long duration = 0;
		
		if (timingModelMap.size() > 0) {
			if (timingModelMap.get(AnimationConstants.ATTR_NAME) != null) {
				if (AnimationConstants.REAL_TIMING_MODEL.equals(timingModelMap.get(AnimationConstants.ATTR_NAME))) {
					// At this point, duration for real timing model must be parsed from timing model map, 
					//not from control points - that would return '0'. 
					duration = Integer.parseInt(timingModelMap.get(RealTimingModel.KAknsAlRealTimeDuration)); 
				} else {
					duration = getAnimationDuration(TimingModel.Relative); 
				}
			}
		} else return new long[0][0];

		if (duration == 0) 
			S60ThemePlugin.error("EffectParameter: Warning: anim. duration not parsed -  0");

		if (valueModelMap.get(AnimationConstants.POINTS) != null
				|| valueModelMap.get(AnimationConstants.ATTR_POINT) != null) {
			return _parsePolyLineValueModel(duration);
		} else if (containsValue(valueModelMap,
				AnimationConstants.LINEAR_RANGE_VALUE_MODEL)) {
			// linear
			return _parseLinearValueModel(duration);
		} else if (containsValue(valueModelMap,
				AnimationConstants.CONSTANT_VALUE_MODEL)) {
			return _parseConstantValueModel(duration);
		} else if (containsValue(valueModelMap,
				AnimationConstants.RANDOM_VALUE_MODEL)) {
			return _parseRandomValueModel(duration);
		} else {
			
			throw new RuntimeException("Uknown Value model");
		}
	}

	/**
	 * decides anim length from timingmodel hashmap.
	 * For RealTime timing model, decides length from timingModelMap properties, 
	 * not from control point model!
	 * 
	 * @param timingModelMap
	 * @return
	 */
	public long getAnimationDuration(TimingModel model) {
		
		if (model != getTimingModel())
			return 0;
		
		if (model == TimingModel.RealTime) {
			
			List<IControlPoint> points = parent.getControlPointModel().getControlPointForParam(getParameterIndex());
			if (points.size() > 1 ) {
				return points.get(points.size() - 1).getTime();
			}
			
			return 0;
			
			
		}
		if (model == TimingModel.Relative) {
			// handle relative
			if (timingModelMap.get(AnimationConstants.TIMESPAN) != null) {
				int timeSpan = Integer.parseInt((String) timingModelMap
						.get(AnimationConstants.TIMESPAN));
				int timeSlice = new Integer((String) timingModelMap
						.get(AnimationConstants.TIMESLICE)).intValue();
				long val = 0;

				try {
					val = getDurationFor(TimeSpan.values()[timeSpan]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				if (val == 0)
					throw new RuntimeException("invalid time span: "
							+ timingModelMap.get(AnimationConstants.TIMESPAN));

				long resultDuration = val / timeSlice;
				return resultDuration;
			}
		}
		return 0;
	}
	
	/**
	 * returns anim length for this span, or 0 if par. is not animated 
	 * in this span
	 * @param span
	 * @return
	 */
	public long getAnimationDuration(TimeSpan span) {
		if (getTimingModel() == TimingModel.Relative && getTimeSpan() == span) {
				
				int timeSlice = new Integer((String) timingModelMap
						.get(AnimationConstants.TIMESLICE)).intValue();
				long val = 0;

				try {
					val = getDurationFor(span);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				if (val == 0)
					throw new RuntimeException("invalid time span: "
							+ timingModelMap.get(AnimationConstants.TIMESPAN));

				long resultDuration = val / timeSlice;
				return resultDuration;			
		}
		return 0;
	}
	
	public static long getDurationFor(TimeSpan timeSpan) {		
		if (timeSpan == TimeSpan.EHour) // hour
			return 60l * 60 * 1000;
		else if (timeSpan == TimeSpan.EDay) // day
			return 24l * 60 * 60 * 1000;
		else if (timeSpan == TimeSpan.EWeek) // week
			return 7l * 24 * 60 * 60 * 1000;
		else /*if (timeSpan == TimeSpan.EMonth)*/ // month
			return 30l * 24 * 60 * 60 * 1000l;
	}

	// FACTOR USED in import of value model
	private final int VALUE_FACTOR = 255;

	//when need to supress events, set to true
	private boolean _supressEvents;

	private long[][] _parseConstantValueModel(long duration) {
		long daat[][] = new long[2][2];
		long c = Integer.parseInt((String) valueModelMap
				.get(AnimationConstants.CONSTANT));
		daat[0][0] = 0;
		daat[0][1] = c;
		daat[1][0] = duration;
		daat[1][1] = c;
		return daat;
	}
	
	/**
	 * Sets duration only to set cotrol point position
	 * @param duration
	 * @return
	 */
	private long[][] _parseRandomValueModel(long duration) {
		long daat[][] = new long[2][2];
		long c = Integer.parseInt((String) valueModelMap
				.get(AnimationConstants.MINIMUM));
		daat[0][0] = 0;
		daat[0][1] = c;
		daat[1][0] = duration;
		daat[1][1] = c;
		return daat;
	}

	
	private long[][] _parseLinearValueModel(long duration) {
		long daat[][] = new long[2][2];
		long start = Integer.parseInt((String) valueModelMap
				.get(AnimationConstants.START));
		long end = Integer.parseInt((String) valueModelMap
				.get(AnimationConstants.END));
		daat[0][0] = 0;
		daat[0][1] = start;
		daat[1][0] = duration;
		daat[1][1] = end;
		return daat;
	}

	private long[][] _parsePolyLineValueModel(long duration) {
		String points = (String) valueModelMap
				.get(AnimationConstants.ATTR_POINT);
		StringTokenizer tok = new StringTokenizer(points, ":");
		ArrayList<Object> list = new ArrayList<Object>();
		while (tok.hasMoreTokens()) {
			String pair = tok.nextToken();
			StringTokenizer tok12 = new StringTokenizer(pair, " ,;");
			long x = Integer.parseInt(tok12.nextToken());
			long y = Integer.parseInt(tok12.nextToken());
			list.add(new long[] { x, y });
		}
		
		//check that last two points are not identical - like 65534,Y, 65535,Y
		//this indicating that there were originally 3 points, and 4th was added during 
		//save to fullfill requirement of 4 points.
		if (list.size() == 4) {
			long[] last = (long[]) list.get(3);
			long[] pre_last = (long[]) list.get(2);
			if (last[1] == pre_last[1] && last[0] == 65535 && pre_last[0] == 65534)
				list.remove(2);
		}
		
		long daat[][] = new long[list.size()][2];
		for (int i = 0; i < daat.length; i++) {
			// compute one element
			long realTime, realValue;
			long in[] = (long[]) list.get(i);
			realTime =Math.round((in[0] * duration)/65535.0f);
			realValue = Math.round((in[1] * VALUE_FACTOR)/65535.0f);
			daat[i][0] = realTime;
			daat[i][1] = realValue;
		}			
		
		
		return daat;
	}

	private boolean containsValue(HashMap map, String val) {
		Iterator i = map.values().iterator();
		while (i.hasNext())
			if (val.equals(i.next()))
				return true;
		return false;
	}

	/**
	 * Called before saving edit changes to model - need to export values from
	 * PolyLineControlPointModel to TS value models - hashmap
	 */
	public void prepareAnimationModels() {
		if (isAnimated) {
			
			_supressEvents = true;
			
			List<IControlPoint> pts = parent.getControlPointModel()
					.getControlPointForParam(getParameterIndex());
			if (pts.size() > 1) { //when only one point defined, it is starting point - no valid 
				HashMap vm = _exportValueModel(pts, getParameterIndex(), parent.getControlPointModel());
				replaceValueMap(vm);				

				// set time model map to match anim. duration
				if (!getTimingModel().equals(TimingModel.Relative)) {
					int duration = (int) pts.get(pts.size() - 1).getTime();
					HashMap tm = _exportRealTimeModel(duration);
					setTimingModelMap(tm);
				}
				//in order to properly save, SEQ_NO must be removed!
				timingModelMap.remove(ThemeTag.ELEMENT_SEQNO);
				
				removeEmptyValues(valueModelMap);
				removeEmptyValues(timingModelMap);
				
				//add empty parameters as default values for timing model map - needed for packaging
				{
					if (this._animationTiming == TimingModel.RealTime) {
						//there must be for sure name and duration
						if (timingModelMap.get(RealTimingModel.KAknsAlRealTimeRepeatCount) == null)
							timingModelMap.put(RealTimingModel.KAknsAlRealTimeRepeatCount,"-1");
						if (timingModelMap.get(RealTimingModel.KAknsAlRealTimeRepeatDuration) == null)
							timingModelMap.put(RealTimingModel.KAknsAlRealTimeRepeatDuration,"-1");
						if (timingModelMap.get(RealTimingModel.KAknsAlRealTimeWrap) == null)
							timingModelMap.put(RealTimingModel.KAknsAlRealTimeWrap,"1");
					} else if (this._animationTiming == TimingModel.Relative) {
						if (timingModelMap.get(RelativeTimingModel.KAknsAlRelativeSlices) == null)
							timingModelMap.put(RelativeTimingModel.KAknsAlRelativeSlices,"1");
						if (timingModelMap.get(RelativeTimingModel.KAknsAlRelativeWrap) == null)
							timingModelMap.put(RelativeTimingModel.KAknsAlRelativeWrap,"1");
					}
				}
				
				_supressEvents = false;
				
			} else {
				valueModelMap.clear();
				timingModelMap.clear();
				isAnimated = false;
			}
		}
	}

	private void removeEmptyValues(HashMap m) {		
		Iterator it = m.keySet().iterator();
		ArrayList toremove = new ArrayList();
		while (it.hasNext()) {
			Object key = it.next();
			if ("".equals(m.get(key)))
				toremove.add(key);				
		}
		for (Object s:toremove)
			m.remove(s);
	}

	/**
	 * returns value map coding real time model with duration duration
	 * 
	 * @param duration
	 * @return
	 */
	private HashMap _exportRealTimeModel(int duration) {
		HashMap tm = new HashMap();
	
		tm.put(AnimationConstants.DURATION, Integer.toString(duration));
		tm.put(AnimationConstants.ATTR_NAME,
				AnimationConstants.REAL_TIMING_MODEL);

		return tm;
	}

	/*
	 * CP model has at least two points defined, when entering this method
	 * CP list already filtered for this param - pts
	 */
	private HashMap _exportValueModel(
			List<IControlPoint> pts, int paramIndex, PolyLineControlPointModel model) {
		
		int duration = (int) pts.get(pts.size() - 1).getTime();
		
		if (pts.size() == 2) {
			//export as linear range or constant - detect
			if (model.getValue(pts.get(0).getTime(), paramIndex) == model.getValue(pts.get(1).getTime(), paramIndex)){
				return _exportConstantValuemodel(pts, paramIndex, model, duration);
			} else {
				return _exportLinearRangeValuemodel(pts, paramIndex, model, duration);
			}
		} else {
			//3, 4 points or more
//			 construct points string
			
			String points = (String) createPointsString(pts, duration, paramIndex);
			String default_ = "0";
			String flavour = AnimationConstants.POLYLINE1D_VALUE_MODEL;
			String factor = VALUE_FACTOR + "," + VALUE_FACTOR;
			
			HashMap map = new HashMap();
			map.put(AnimationConstants.ATTR_POINT, points);
			map.put(AnimationConstants.DEFAULT, default_);
			map.put(AnimationConstants.FLAVOR_NAME, flavour);
			map.put(AnimationConstants.FACTOR, factor);
			return map;
		}		

	}

	private HashMap _exportLinearRangeValuemodel(List<IControlPoint> pts, int paramIndex, PolyLineControlPointModel model, int duration) {

		String default_ = "0";
		String flavour = AnimationConstants.LINEAR_RANGE_VALUE_MODEL;		
		
		String value1 = Integer.toString(Math.round(model.getValue(pts.get(0).getTime(),paramIndex)));
		String value2 = Integer.toString(Math.round(model.getValue(pts.get(1).getTime(),paramIndex)));
		
		HashMap map = new HashMap();
		map.put(AnimationConstants.DEFAULT, default_);
		map.put(AnimationConstants.FLAVOR_NAME, flavour);
		map.put(AnimationConstants.START, value1);
		map.put(AnimationConstants.END, value2);
		
		return map;
	}

	private HashMap _exportConstantValuemodel(List<IControlPoint> pts, int paramIndex, PolyLineControlPointModel model, int duration) {
		
		String default_ = "0";
		String flavour = AnimationConstants.CONSTANT_VALUE_MODEL;		
		
		String value = Integer.toString(Math.round(model.getValue(pts.get(0).getTime(), paramIndex)));
		
		HashMap map = new HashMap();
		map.put(AnimationConstants.DEFAULT, default_);
		map.put(AnimationConstants.FLAVOR_NAME, flavour);
		map.put(AnimationConstants.CONSTANT, value);
		
		return map;
	}

	private Object createPointsString(List<IControlPoint> pts, int duration,
			int index) {
		
		if (pts.size() == 0)
			return "";
		
		StringBuffer r = new StringBuffer();
		for (int i = 0; i < pts.size(); i++) {
			
			//if point.size == 3, create fake point to have 4 points. 
			if (pts.size() < 4 && i == pts.size() - 1) {
				IControlPoint pt = pts.get(pts.size() - 1);

				float time = pt.getTime() / (float) duration;
				float value = ((TSCPData) pt.getData()).getValue(index)
						/ (float) this.VALUE_FACTOR;										
				r.append(Integer.toString(((int) (time * 65535)) - 1));
				r.append(",");
				r.append(Integer.toString((int) (value * 65535)));			
				r.append(":");
			}			
			
			IControlPoint pt = pts.get(i);

			float time = pt.getTime() / (float) duration;
			float value = ((TSCPData) pt.getData()).getValue(index)
					/ (float) this.VALUE_FACTOR; 													

			r.append(Integer.toString((int) (time * 65535)));
			r.append(",");
			r.append(Integer.toString((int) (value * 65535)));
			if (i < pts.size() - 1) {
				r.append(":");
			}
		}			
		
		return r.toString() + ";";
	}

	public int getParameterIndex() {
		if (parameterIndex == -1) {
			if (parent != null) {
				// find out param index for this param
				parameterIndex = getParent().getDescriptor().getParameters()
						.indexOf(
								getParent().getDescriptor()
										.getParameterDescriptor(getName()));
			}
		}
		return parameterIndex;
	}


	/**
	 * updates properties of timing models from control point model - duration
	 * Called before animation dialog opens
	 */
	public void updateAnimModels() {
		if (isAnimated) {
			
			if (timingModelMap.get(AnimationConstants.ATTR_NAME) == null) {
//				none is set - set realtime
				timingModelMap.put(AnimationConstants.ATTR_NAME, AnimationConstants.REAL_TIMING_MODEL);
			}
			
			if (timingModelMap.get(AnimationConstants.ATTR_NAME).equals(AnimationConstants.RELATIVEMODEL_NAME)) {
				//relative
				//nothing to do
			}  else { //realtime				
				//set duration according to control points
				PolyLineControlPointModel cmp = getParent().getControlPointModel();
				List<IControlPoint> points = cmp.getControlPointForParam(parameterIndex);
				if (points.size() == 0) {
					timingModelMap.put(AnimationConstants.DURATION, "0");
				} else {
					timingModelMap.put(AnimationConstants.DURATION,points.get(points.size()-1).getTime() + "");
				}							
			}
						
		}
	}

	public boolean isValueSet() {
		String value = (String) attributeMap.get(EffectConstants.ATTR_VALUE);
		if (StringUtils.isEmpty(value)) {			
			return false;
		} else {
			return true;
		}
	}

	public void setAnimationData(double[][] results, int animTime) {
		long duration = animTime;
		if (getTimingModel() == TimingModel.Relative)
			duration = getAnimationDuration(getTimingModel());
		PolyLineControlPointModel<TSCPData> cpm = parent.getControlPointModel();
		IValueMediator mediator = cpm.getMediator();
		//step one - unset all cp's for this param
		for (IControlPoint cp: cpm.getControlPointForParam(getParameterIndex())) {
			mediator.unset(cp.getData(), getParameterIndex());
		}
		//make new control points
		IEffectParameterDescriptor d = parent.getDescriptor().getParameterDescriptor(getParameterIndex());
		int min = Integer.parseInt(d.getMinVal() == null ? "0" : d.getMinVal());
		int max = Integer.parseInt(d.getMaxVal() == null ? "255" : d.getMaxVal());
		int base = min;
		int range = max - min;
		for (int i = results.length - 1; i >= 0; i--) {
			double[] ddata = results[i];
			long time = (int) (ddata[0] * duration / (long)65535);
			double value = base + (ddata[1] * range / 65535);			
			/* 
			 * It points created for this animation must have set only value for this parameters, not other.
			 * Otherwise, animations for other parameters get corrupted
			 */
			IControlPoint cp = cpm.createControlPoint(time, false);
			mediator.setValue(cp.getData(), getParameterIndex(), (float) value);
		}
		cpm.removeUnusedPoints();
	}
	
	public IEffectParameterDescriptor getDescriptor() {
		try {
			return getParent().getDescriptor().getParameterDescriptor(getParameterIndex());
		} catch (Exception e) {
			
		}
		return null;
	}


}
