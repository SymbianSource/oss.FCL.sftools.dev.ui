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
/*
 * Created on Dec 9, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.nokia.tools.theme.s60.effects;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nokia.tools.media.utils.layers.EffectTypes;
import com.nokia.tools.media.utils.layers.IEffectDescriptor;
import com.nokia.tools.media.utils.layers.IEffectFactory;
import com.nokia.tools.media.utils.layers.IEffectParameter;
import com.nokia.tools.media.utils.layers.IEffectParameterDescriptor;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.layers.LayerEffectRegistry;
import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.ITimeLineRow;
import com.nokia.tools.media.utils.timeline.cp.IControlPoint;
import com.nokia.tools.media.utils.timeline.cp.IControlPointListener;
import com.nokia.tools.media.utils.timeline.cp.IControlPointModel;
import com.nokia.tools.platform.theme.EffectConstants;
import com.nokia.tools.platform.theme.LayerEffect;
import com.nokia.tools.theme.s60.editing.EditableImageLayer;
import com.nokia.tools.theme.s60.editing.EffectDescriptor;
import com.nokia.tools.theme.s60.editing.EffectParameterDescriptor;
import com.nokia.tools.theme.s60.editing.anim.CPMFilterWrapper;
import com.nokia.tools.theme.s60.editing.anim.PolyLineControlPointModel;
import com.nokia.tools.theme.s60.editing.anim.TSCPData;
import com.nokia.tools.theme.s60.editing.anim.TSCPValueMediator;

/** 
 * <pre>
 * decription of changes - removed UI code, 
 * removed dependency on LayerDialog,EffectDialog,LayerModel,LayerData
 * partly refactored api:
 *   - added getParameterDefaultValue(..)
 *   - getParameterValue(..) always return number
 *   - added EffectDescriptor support
 * 	 - added ILayerEffect interface implemenatation
 * 	 - added link to parent, getParent()
 *   - added getParameterValueLiteral(..)
 *   - removed obsolete/duplicate method
 * </pre>
 * 
 * Notes on animation support - EffectObject holds control point model for animation.
 * It is parsed from LayerEffect's value model. each control point can hold 
 * value for one or more parameters. It holds two properties for each param - if
 * value is set at given CP, and value itself. ControlPointModel is implemented
 * through PolyLineControlPointModel. On save, control point model is exported for each 
 * parameter as PolyLine1DValueModel, ConstantValueModel, or LinearRangeValueModel, 
 * based of number of points that are defined. 
 */

//public class EffectObject implements ChangeListener,ItemListener, ActionListener, Cloneable,EventHandler,MouseListener,FocusListener {
public class EffectObject implements Cloneable, ILayerEffect, IEffectFactory, IControlPointListener {
	
	//parameters UI types
	public static final String UI_TYPE_SLIDER = "JSlider"; //$NON-NLS-1$
	public static final String UI_TYPE_TEXT = "JTextField"; //$NON-NLS-1$
	public static final String UI_TYPE_COMBO = "JComboBox"; //$NON-NLS-1$
	
	private PropertyChangeSupport propSup = new PropertyChangeSupport(this);
	
	private static List<ILayerEffect> list = new ArrayList<ILayerEffect>();

	private static List<ILayerEffect> onLayerlist = new ArrayList<ILayerEffect>();

	private static List<ILayerEffect> betweenLayerlist = new ArrayList<ILayerEffect>();

	public boolean invalidData;

	/** effect descriptor for this effect */
	private IEffectDescriptor effectDescriptor;

	private HashMap<Object,Object> attributeMap;

	private SortedMap<Object,Object> uiAttributesMap;

	private HashMap<Object,Object> outputAttributeMap;

	private HashMap<Object,Object> parameterList;
	

	//selected flag
	private boolean isSelected = false;

	/**
	 * Object holding original state before displaying in layer end effects dialog.
	 * Used for restore effect state if user cancels effect dialog.
	 */
	private EffectObject backupObject;

	private HashMap<String,String> ValueModelMap;

	private String prefferedValueModel;
	
	private ArrayList<String> allowedEntitiesList;
	
	private ArrayList<String> restrictedEntitiesList;

	private ILayer parent;
	
	private LayerEffect model;

	private ITimeLineRow timeLineRow;

	/** control point model where value models from child params are stored */
	private PolyLineControlPointModel<TSCPData> controlPointModel;

	private boolean animationInProgress;
	
	/**
	 * if has at least one animatable parameter, can be animated
	 */
	private boolean animationAllowed; 
	
	private boolean animatedInPreview = true;
	private long animationStartLocation;
	private CPMFilterWrapper controlPointModelWrapper;
	
	public static synchronized void release() {
		list.clear();
		onLayerlist.clear();
		betweenLayerlist.clear();
	}

	public LayerEffect getModel() {
		return model;
	}

	public void setModel(LayerEffect model) {
		this.model = model;
	}

	/**
	 * should not be called by api user.
	 * @param parent
	 */
	public void setParent(ILayer parent) {
		this.parent = parent;
	}

	/** constructor for cloning */
	public EffectObject() {
	}

	public EffectObject(Node node) {
		isSelected = true;

		attributeMap = new HashMap<Object,Object>();
		uiAttributesMap = new TreeMap<Object,Object>();
		outputAttributeMap = new HashMap<Object,Object>();
		parameterList = new HashMap<Object,Object>();

		_setValues(node);

		if (getAttributeAsString(EffectConstants.ATTR_TYPE).equalsIgnoreCase(
				EffectConstants.ON_LAYER_EFFECT)) {
			boolean found = false;
			for (Object o : onLayerlist) {
				if (((EffectObject) o).getName().equals(this.getName()))
					found = true;
			}
			if (!found)
				onLayerlist.add(this);
		}
		if (getAttributeAsString(EffectConstants.ATTR_TYPE).equalsIgnoreCase(
				EffectConstants.BETWEEN_LAYER_EFFECT)) {
			boolean found = false;
			for (Object o : betweenLayerlist) {
				if (((EffectObject) o).getName().equals(this.getName()))
					found = true;
			}
			if (!found)
				betweenLayerlist.add(this);
		}

		boolean found = false;
		for (Object o : list) {
			if (((EffectObject) o).getName().equals(this.getName()))
				found = true;
		}
		if (!found) {
			list.add(this);			
		}
		
		try {
			effectDescriptor = new EffectDescriptor(this);
			animationAllowed = _computeAnimationAllowed(effectDescriptor);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//fix bad defaultvalues - when are literals instead of numbers
		Iterator params = getParameterList().keySet().iterator();
		while(params.hasNext()) {
			String name = (String) params.next();

			if (getParameterDefaultValue(name) != null) {
				try {
					Integer.parseInt(getParameterDefaultValue(name));
				} catch (Exception e) {
					//not number - lookup number value
					String defVal = getParameterDefaultValue(name);
					int defValNumeric = getDescriptor().getParameterLiteralValueNumber(name, defVal);
					HashMap<Object, Object> map1=getParameter(name).getAttributeMap();
					map1.put(EffectConstants.ATTR_DEFAULTVALUE, Integer.toString(defValNumeric));
					((EffectParameterDescriptor)effectDescriptor.getParameterDescriptor(name)).setDefaultVal(Integer.toString(defValNumeric));					
				}
			}
		}
		
		//register this as IEffectFactory
		try {
			if (LayerEffectRegistry.getFactory(null) == null)
				LayerEffectRegistry.registerDefaultFactory(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		allowedEntitiesList = new ArrayList<String>();
		restrictedEntitiesList = new ArrayList<String>();					
	}

	/**
	 * returns true if effect has at least one animatable param
	 * @param d
	 * @return
	 */
	private boolean _computeAnimationAllowed(IEffectDescriptor d) {
		for (IEffectParameterDescriptor param: d.getParameters()) {
			if (UI_TYPE_SLIDER.equals(param.getUiType()) ||
					UI_TYPE_TEXT.equals(param.getUiType())) return true;
		}
		return false;
	}

	private void _setValues(Node xNode) {


		if ((xNode.getNodeName().equals(EffectConstants.TAG_EFFECT))) {
			NamedNodeMap attList = xNode.getAttributes();

			for (int i = 0; i < attList.getLength(); i++) {
				Node attr = attList.item(i);
				
				String attrVal = attr.getNodeValue();				
				attributeMap.put(attr.getNodeName(), attrVal);

			}			
		}
		if (xNode.hasChildNodes()) {
			NodeList child = xNode.getChildNodes();
			for (int j = 0; j < child.getLength(); j++) {
				HashMap<Object,Object> map = _setChildValues(child.item(j));
			}
		}
		if ((xNode.getNodeName().equals(EffectConstants.TAG_VALUEMODEL))) {

		}
	}

	public EffectObject Clone() {

		EffectObject eObj = null;
		try {
			eObj = (EffectObject) this.clone();			
			
			HashMap clone = (HashMap) ((HashMap<Object, Object>) eObj.getParameterList())
					.clone();
			HashMap pList = clone;
			Iterator iter1 = pList.keySet().iterator();
			while (iter1.hasNext()) {
				String key = (String) iter1.next();
				EffectParameter eParam = ((EffectParameter) pList.get(key)).Clone();
				eParam.setParent(eObj);
				HashMap paramMap = (HashMap) eParam.getAttributeMap().clone();
				eParam.setAttributes(paramMap);
				HashMap valueMap = eParam.getValueModelMap();
				if (valueMap != null)
					eParam.setValueModelMap((HashMap) valueMap.clone());
				HashMap<String, String> timeMap = eParam.getTimingModelMap();
				if (timeMap != null)
					eParam.setTimingModelMap( (HashMap) timeMap.clone());
				pList.put(key, eParam);
			}
			eObj.setAllowedEntitiesList((ArrayList<String>)this.allowedEntitiesList.clone());
			eObj.setRestrictedEntitiesList((ArrayList<String>)this.restrictedEntitiesList.clone());
			eObj.setParameterList(pList);
			eObj.effectDescriptor = effectDescriptor;
			
			//create fields that are not shared by clones
			eObj.controlPointModel = new PolyLineControlPointModel<TSCPData>(eObj, new TSCPValueMediator());
			eObj.controlPointModelWrapper = new CPMFilterWrapper(eObj.controlPointModel);
			//set number of params to mediator
			eObj.controlPointModel.getMediator().setParamCount(effectDescriptor.getParameters().size());
			eObj.controlPointModel.addControlPointListener(eObj);
			
			eObj.backupObject = null;
			eObj.timeLineRow = null;
			eObj.animationInProgress = false;
			eObj.propSup = new PropertyChangeSupport(eObj);
			eObj.parent = null;
			
			return eObj;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param node
	 */
	private HashMap<Object, Object> _setChildValues(Node node) {

		HashMap<Object,Object> attMap = new HashMap<Object,Object>();
//		List<Object> list = new ArrayList<Object>();
		if ((node.getNodeName().equalsIgnoreCase(EffectConstants.TAG_PARAM))) {
			NamedNodeMap attList = node.getAttributes();

			for (int i = 0; i < attList.getLength(); i++) {
				Node attr = attList.item(i);
				attMap.put(attr.getNodeName(), attr.getNodeValue());
			}

			ArrayList<Object> paramList = (ArrayList<Object>) attributeMap
					.get(EffectConstants.OUTPUT_PARAMS);
			if (paramList == null) {
				paramList = new ArrayList<Object>();
				attributeMap.put(EffectConstants.OUTPUT_PARAMS, paramList);
			}
			paramList.add(attMap);
			

			outputAttributeMap.put((String) attMap
					.get(EffectConstants.ATTR_NAME), attMap);

		} else if ((node.getNodeName()
				.equalsIgnoreCase(EffectConstants.TAG_UI_PARAM))) {
			NamedNodeMap attList = node.getAttributes();
			EffectParameter parameter = new EffectParameter(this);
			for (int i = 0; i < attList.getLength(); i++) {
				Node attr = attList.item(i);
				attMap.put(attr.getNodeName(), attr.getNodeValue());
			}
			parameter.setAttributes(attMap);
			parameterList.put(parameter.getName(), parameter);

			ArrayList uiList = (ArrayList) attributeMap
					.get(EffectConstants.UI_PARAMS);
			if (uiList == null) {
				uiList = new ArrayList();
				attributeMap.put(EffectConstants.UI_PARAMS, uiList);
			}
			uiList.add(attMap);
			//AttributeMap.put(EffectConstants.UI_PARAMS, uiList);

			uiAttributesMap.put((String) attMap
					.get(EffectConstants.ATTR_UINAME), attMap);

		}

		return attMap;
	}

	public Map getAttributeMap() {
		return attributeMap;
	}

	public String getName() {
		return (String) attributeMap.get(EffectConstants.ATTR_NAME);
	}

	public Object getAttribute(String attrName) {
		return attributeMap.get(attrName);
	}

	public String getAttributeAsString(String attrName) {
		return (String) attributeMap.get(attrName);
	}

	public void setAttribute(String name, Object value) {
		attributeMap.put(name, value);		
	}

	public String getParameterDefaultValue(String attrName) {
		EffectParameter param = getParameter(attrName);
		if (param != null) {
			return (String) param.getAttributeMap().get(
					EffectConstants.ATTR_DEFAULTVALUE);
		}
		return null;
	}

	public void removeAttribute(String key) {
		attributeMap.remove(key);
	}

	public static EffectObject getEffect(String name) {	
		for (int i = 0; i < list.size(); i++) {
			if (((EffectObject) list.get(i)).getName().equalsIgnoreCase(name))
				return ((EffectObject) list.get(i)).Clone();
		}
		return null;
	}

	/**
	 * returns original effect object from the list, not cloned instance
	 * @param name
	 * @return
	 */
	public static EffectObject getEffect_original(String name) {
		for (int i = 0; i < list.size(); i++) {
			if (((EffectObject) list.get(i)).getName().equalsIgnoreCase(name))
				return ((EffectObject) list.get(i));
		}
		return null;
	}

	/**
	 * @return
	 */
	public static List getEffectList() {
		return list;
	}

	/**
	 * @return
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * @param b
	 */
	public void setSelected(boolean b) {
		if (b == isSelected) 
			return;		
		isSelected = b;
		if (parent != null) {
			((EditableImageLayer)parent).effectSelectedOrUnselected(this);
		}
		fireStateUpdateEvent();
	}

	/**
	 * @return Returns the outputAttributeMap.
	 */
	public HashMap<Object, Object> getOutputAttributeMap() {
		return outputAttributeMap;
	}

	/**
	 * @return Returns the outputAttributeMap.
	 */
	public SortedMap getUiAttributeMap() {
		return uiAttributesMap;
	}

	public EffectParameter getParameter(String name) {
		return (EffectParameter) parameterList.get(name);
	}

	/**
	 * sets value, 'value' must be parseable to number.
	 * @param name
	 * @param value
	 */
	public void setParameterValue(String name, String value) {
		EffectParameter parameter = getParameter(name);
		if (parameter != null) {
			parameter.setValue(value);
			fireStateUpdateEvent();
		}		
	}

	/**
	 * gets parameter value, or default value if value is not present
	 * @param name
	 * @return
	 */
	public String getParameterValue(String name) {
		EffectParameter parameter = getParameter(name);
		if (parameter == null)
			return null;
		return parameter.getValue();
	}
	
	public String getParameterValue(String name, long time) {
		EffectParameter parameter = getParameter(name);
		if (parameter == null)
			return null;
		return parameter.getValue(time);
	}

	/**
	 * returns param value literal for combo-box value
	 * @param name
	 * @return
	 */
	public String getParameterValueLiteral(String name) {
		EffectParameter parameter = getParameter(name);
		if (parameter == null)
			return null;
		if (name.equalsIgnoreCase(EffectConstants.ATTR_MODE)) {
			if (getName().equalsIgnoreCase(EffectConstants.CHANNELBLENDING)
					|| getName()
							.equalsIgnoreCase(EffectConstants.ALPHABLENDING)
					|| getName().equalsIgnoreCase(EffectConstants.CONVOLUTION)) {
				String literal = effectDescriptor.getParameterLiteralValue(
						name, parameter.getValueAsInt());
				if (literal != null)
					return literal;
			}
		}
		if (name.equalsIgnoreCase(EffectConstants.SCALEMODE)) {
			if (getName().equalsIgnoreCase(EffectConstants.APPLYGRAPHICS)) {
				String literal = effectDescriptor.getParameterLiteralValue(
						getName(), parameter.getValueAsInt());
				if (literal != null)
					return literal;
			}
		}
		return parameter.getValue();
	}

	public Object getAttributeValue(String attributeName) {
		EffectParameter parameter = getParameter(attributeName);
		
		if (parameter == null)
			return getAttribute(attributeName);

		return getParameterValue(attributeName);
		
	}
	

	/**
	 * @return Returns the onLayerlist.
	 */
	public static List<ILayerEffect> getOnLayerlist() {
		return onLayerlist;
	}

	/**
	 * @return Returns the object.
	 */
	public void restoreObject() {
		if (backupObject != null)
			this.setParameterList((HashMap<Object, Object>) backupObject.getParameterList());
	}

	/**
	 * @param object The object to set.
	 */
	public void setBackupObject(EffectObject object) {
		this.backupObject = object;
	}

	/**
	 * return name of effect by ui effect name
	 * e.g. 'Channel Blend' -> 'ChannelBlending'
	 * @param selectedItem
	 * @return
	 */
	public static String getName(String selectedItem) {
		for (int i = 0; i < list.size(); i++) {
			if (((EffectObject) list.get(i)).getAttributeAsString(
					EffectConstants.ATTR_UINAME).equalsIgnoreCase(selectedItem))
				return ((EffectObject) list.get(i)).getName();
		}
		return null;
	}

	public Map<Object, Object> getParameterList() {
		return parameterList;
	}

	private void setParameterList(HashMap<Object, Object> parameterList) {
		this.parameterList = parameterList;		
	}

	public String getPrefferedValueModel() {
		return prefferedValueModel;
	}

	public void setPrefferedValueModel(String prefferedValueModel) {
		this.prefferedValueModel = prefferedValueModel;
	}

	public HashMap<String, String> getValueModelMap() {
		return ValueModelMap;
	}

	public void setValueModelMap(HashMap<String, String> valueModelMap) {
		ValueModelMap = valueModelMap;
	}

	public boolean isBetweenLayerEffect() {
		return getAttributeAsString(EffectConstants.ATTR_TYPE)
				.equalsIgnoreCase(EffectConstants.BETWEEN_LAYER_EFFECT);
	}

	public boolean isOnLayerEffect() {
		return getAttributeAsString(EffectConstants.ATTR_TYPE)
				.equalsIgnoreCase(EffectConstants.ON_LAYER_EFFECT);
	}

	public static List<ILayerEffect> getBetweenLayerlist() {
		return betweenLayerlist;
	}

	public static boolean isBetweenLayerEffect(String name) {
		return getEffect(name).isBetweenLayerEffect();
	}

	// ------------------------ dialog API impl -----------------------------

	public ILayer getParent() {
		return parent;
	}

	public boolean isApplicableWith(List<ILayerEffect> usedLayerEffects) {
		throw new RuntimeException("not_impl");
	}

	public EffectTypes getType() {
		return isOnLayerEffect() ? EffectTypes.ON_LAYER_EFFECTS
				: EffectTypes.BETWEEN_LAYER_EFFECTS;
	}

	public List<IEffectParameterDescriptor> getAttributeDescriptors() {
		return effectDescriptor.getParameters();
	}

	public IEffectDescriptor getDescriptor() {
		return effectDescriptor;
	}

	public String getEffectParameter(String key) {
		return getParameterValue(key);
	}
	
	public String getStaticEffectParameter(String key) {
		EffectParameter parameter = getParameter(key);
		if (parameter == null)
			return null;
		return parameter.getValue();
	}
	
	public String getEffectParameter(String key, long time) {
		return getParameterValue(key, time);
	}

	public void setEffectParameter(String key, Object value) {
		setParameterValue(key, (String) value);
	}

	//IEffectFactory info
	
	public List<IEffectDescriptor> getAvailableEffectsInfo() {
		List<IEffectDescriptor> _list = new ArrayList<IEffectDescriptor>();
		for (int i = 0; i < list.size(); i++) {
			_list.add(((EffectObject)list.get(i)).getDescriptor());
		}
		return _list;
	}

	public ILayerEffect getEffectInstance(String name) {
		return getEffect(name);
	}

	public ILayerEffect getEffectInstance(IEffectDescriptor d) {
		return getEffect(d.getName());
	}
	
	public void setAllowedEntitiesList(ArrayList<String> allowedEntitiesList) {
        this.allowedEntitiesList = allowedEntitiesList;
    }
	
	public ArrayList getAllowedEntitiesList() {
        return this.allowedEntitiesList;
    }
	
	public void setRestrictedEntitiesList(ArrayList<String> restrictedEntitiesList) {
        this.restrictedEntitiesList = restrictedEntitiesList;
    }
	
	public ArrayList<String> getRestrictedEntitiesList() {
        return this.restrictedEntitiesList;
    }
	
	private void fireStateUpdateEvent() {
		propSup.firePropertyChange(PROPERTY_STATE, null, null);
	}
	
	private void fireAnimationStateUpdateEvent() {
		propSup.firePropertyChange(PROPERTY_ANIMATION_STATE, null, null);
	}

	public void addPropertyListener(PropertyChangeListener ps) {
		propSup.addPropertyChangeListener(ps);
	}

	public void removePropertyChangeListener(PropertyChangeListener ps) {
		propSup.removePropertyChangeListener(ps);
	}

	public boolean isAnimated() {
		Iterator it = parameterList.values().iterator();
		while(it.hasNext()) {
			EffectParameter param = (EffectParameter) it.next();
			if (param.isAnimated())
				return true;
		}
		return false;
	}
	
	
	public long getAnimationDuration() {		
		return getAnimationDuration(TimingModel.RealTime); 
	}
	
	

	public void startAnimation() {
		Iterator it = parameterList.values().iterator();
		this.animationInProgress = true;
		while(it.hasNext()) {
			EffectParameter param = (EffectParameter) it.next();
			param.startAnimation();
		}	
	}
	
	public void endAnimation() {
		Iterator it = parameterList.values().iterator();
		this.animationInProgress = false;

		while(it.hasNext()) {
			EffectParameter param = (EffectParameter) it.next();
			param.endAnimation();
		}	
	}
	
	public void setAnimationTime(TimingModel timing, long offset) {
		Iterator it = parameterList.values().iterator();
		while(it.hasNext()) {
			EffectParameter param = (EffectParameter) it.next();
			if (param.isAnimated())
				param.setAnimationTime(timing, offset);
		}	
	}

	public void setAnimationStartLocation(long time) {
		animationStartLocation = time;
	}
	

	public boolean isAnimatedFor(TimingModel timingType) {
		if (!isAnimated())
			return false;		
		Iterator it = parameterList.values().iterator();
		while(it.hasNext()) {
			EffectParameter param = (EffectParameter) it.next();			
			if (param.isAnimated()) {			
				if (timingType.equals(param.getTimingModel()))
					return true;
			}
		}
		return false;	
	}
	
	public boolean isAnimatedFor(TimeSpan span) {
		if (!isAnimated())
			return false;		
		Iterator it = parameterList.values().iterator();
		while(it.hasNext()) {
			EffectParameter param = (EffectParameter) it.next();			
			if (param.isAnimated()) {		
				if (param.getTimeSpan() == span)
					return true;
			}
		}
		return false;	
	}


	public long getAnimationDuration(TimingModel timingType) {		
			if (!isAnimated())
				return 0;
			
			long animationTime = 0;
			Iterator it = parameterList.values().iterator();
			while(it.hasNext()) {
				EffectParameter param = (EffectParameter) it.next();
				if (param.isAnimated()) {
					long paramAnimTime = param.getAnimationDuration(timingType);
					if (paramAnimTime > animationTime)
						animationTime = paramAnimTime;
				}
			}
			return animationTime;	
	}
	
	public long getAnimationDuration(TimeSpan span) {		
		if (!isAnimated())
			return 0;
		long animationTime = 0;
		Iterator it = parameterList.values().iterator();
		while(it.hasNext()) {
			EffectParameter param = (EffectParameter) it.next();
			if (param.isAnimated()) {
				long paramAnimTime = param.getAnimationDuration(span);
				if (paramAnimTime > animationTime)
					animationTime = paramAnimTime;
			}
		}
		return animationTime;	
}
	
	protected void parameterStateChanged(EffectParameter par) {
		fireStateUpdateEvent();
	}
	
	protected void parameterAnimationStateChanged(EffectParameter par) {
		fireAnimationStateUpdateEvent();
	}
	
	/*
	 * time line row should be separated, but needs to be know by this class
	 *  - subsequent calls must return same object
	 */
	public ITimeLineRow getTimeLineRow() {
		return timeLineRow;
	}

	public PolyLineControlPointModel getControlPointModel() {
		return controlPointModel;
	}
	
	public IControlPointModel getControlPointModelWrapper() {
		return controlPointModelWrapper;
	}

	/**
	 * true if effect has at least one parameter, that can be animated
	 * @return
	 */
	public boolean animationAllowed() {
		return animationAllowed;
	}
	
	public void controlPointCreated(IControlPoint point) {		
		propSup.firePropertyChange(PROPERTY_ANIMATION_STATE, null, null);		
	}

	public void controlPointMoved(IControlPoint point) {
		propSup.firePropertyChange(PROPERTY_ANIMATION_STATE, null, null);
	}

	public void controlPointRemoved(IControlPoint point) {		
		propSup.firePropertyChange(PROPERTY_ANIMATION_STATE, null, null);
	}

	public void controlPointSelected(IControlPoint point) {	
	}

	public boolean isAnimatedInPreview() {
		return animatedInPreview;
	}

	public void setAnimatedInPreview(boolean animatedInPreview) {
		this.animatedInPreview = animatedInPreview;

		propSup.firePropertyChange(PROPERTY_ANIMATION_STATE, null, null);
	}

	public void setTimeLineRow(ITimeLineRow timeLineRow) {
		this.timeLineRow = timeLineRow;
	}

	public void clearTimeLineNodes() {
		timeLineRow = null;
	}

	public long getAnimationStartLocation() {
		return animationStartLocation;
	}

	public List<IEffectParameter> getParameters() {
		List<IEffectParameter> parameters = new ArrayList<IEffectParameter>();
		Iterator it = parameterList.values().iterator();
		while (it.hasNext())
			parameters.add((IEffectParameter) it.next());
		return parameters;
	}

	public boolean isParameterSet(String name) {
		EffectParameter parameter = getParameter(name);
		if (parameter == null)
			return false;
		return parameter.isValueSet();
	}
	
}
