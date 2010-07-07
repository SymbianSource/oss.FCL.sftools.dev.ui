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
package com.nokia.tools.theme.s60.editing;

import java.awt.image.RenderedImage;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.layers.EffectTypes;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.ITimeLineRow;
import com.nokia.tools.media.utils.timeline.cp.IControlPoint;
import com.nokia.tools.platform.theme.EffectConstants;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.LayerEffect;
import com.nokia.tools.platform.theme.ParameterModel;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.editing.BasicEntityImage;
import com.nokia.tools.theme.editing.BasicImageLayer;
import com.nokia.tools.theme.s60.editing.anim.EffectAvailabilityParser;
import com.nokia.tools.theme.s60.editing.anim.PolyLineControlPointModel;
import com.nokia.tools.theme.s60.editing.utils.LayerEffectOrderHelper;
import com.nokia.tools.theme.s60.effects.EffectObject;
import com.nokia.tools.theme.s60.effects.EffectObjectUtils;
import com.nokia.tools.theme.s60.effects.EffectParameter;
import com.nokia.tools.theme.s60.effects.OnLayerEffects;
import com.nokia.tools.theme.s60.internal.utilities.TSDataUtilities;
import com.nokia.tools.theme.s60.model.AnimatedParameterModel;
import com.nokia.tools.theme.s60.model.MorphedGraphic;
import com.nokia.tools.theme.s60.morphing.AnimationConstants;
import com.nokia.tools.theme.s60.morphing.timemodels.BaseTimingModelInterface;
import com.nokia.tools.theme.s60.morphing.valuemodels.BaseValueModelInterface;

/*
 * Structure changes are propagated to IML on-the-fly, NLayerData properties
 * also (but not images and masks!) Effect properties are not updated on-the-fly -
 * propagateChangesToModel() must be called
 */

/**
 * represents one layer in ThemeGraphics.
 */
public class EditableImageLayer extends BasicImageLayer {

	/* childs effect */
	private List<ILayerEffect> effects;

	private List<ILayerEffect> effect_unmodifiable;

	/* on during initialization */
	private boolean _supressEffectChecks;

	private boolean supressEventsPropagation = false;

	private ITimeLineRow timeLineRow;

	private boolean animatedInPreview = true;

	public EditableImageLayer(ImageLayer iml, BasicEntityImage parent, Theme s60)
			throws ThemeException {
		super(iml, parent, s60);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.BasicImageLayer#init(com.nokia.tools.platform.theme.ImageLayer,
	 *      com.nokia.tools.theme.editing.BasicEntityImage,
	 *      com.nokia.tools.platform.theme.Theme)
	 */
	protected void init(ImageLayer iml, BasicEntityImage parent, Theme s60)
			throws ThemeException {
		super.init(iml, parent, s60);
		/* create all possible effects in order to simplify ordering of effects */
		effects = new ArrayList<ILayerEffect>(EffectObject.getEffectList()
				.size());

		_supressEffectChecks = true;

		for (int i = 0; i < LayerEffectOrderHelper
				.getDefaultEffectProcessOrder().length; i++) {
			EffectObject e = (EffectObject) EffectObject
					.getEffect(LayerEffectOrderHelper
							.getDefaultEffectProcessOrder()[i]);
			e.setSelected(false);
			e.setParent(this);
			effects.add(e);
		}

		effect_unmodifiable = Collections.unmodifiableList(effects);

		/* childs creation */
		List layerEffects = iml.getLayerEffects();
		for (int i = layerEffects.size() - 1; i >= 0; i--) {
			LayerEffect effect = (LayerEffect) layerEffects.get(i);
			EffectObject effo = getEffectAsEO(effect.getEffetName());
			// make this effects in proper order in the list begin
			effects.remove(effo);
			if (effo.isBetweenLayerEffect()) {
				// add to the end of the list
				int pos = LayerEffectOrderHelper
						.getBetweenLayerEffectsStartPos();
				effects.add(pos == -1 ? 1 : pos, effo);
			} else {
				// onlayer - add to position 1 - after applyGraphics
				effects.add(1, effo);
			}

			effo.setSelected(true);
			updateEffectObject(effo, effect);
		}

		// apply graphics effect
		EffectObject applyGraphics = getEffectAsEO(EffectConstants.APPLYGRAPHICS);
		if (iml.getAttribute(EffectConstants.SCALEMODE) != null) {
			String scalemode = iml.getAttribute(EffectConstants.SCALEMODE);
			if (scalemode == null || scalemode.trim().length() == 0)
				scalemode = ""; // implies default
			if (isNumber(scalemode))
				applyGraphics.getParameter(EffectConstants.SCALEMODE).setValue(
						scalemode);
			else {
				try {
					String scaleModeValue = Integer.toString(EffectObjectUtils
							.getScaleModeValue(scalemode));
					applyGraphics.getParameter(EffectConstants.SCALEMODE)
							.setValue(scaleModeValue);
				} catch (Exception e) {
					// scale mode was not number nor valid literal value.
					applyGraphics.getParameter(EffectConstants.SCALEMODE)
							.setValue("");
				}
			}
		}

		applyGraphics.setSelected(hasImage());
		// disable apply graphics if apply_color active
		if (getEffect(EffectConstants.APPLYCOLOR).isSelected()) {
			applyGraphics.setSelected(false);
		}

		// add listeners to effects
		for (int i = 0; i < effects.size(); i++) {
			ILayerEffect e = effects.get(i);
			e.addPropertyListener(this);
		}

		_supressEffectChecks = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.BasicImageLayer#isEmpty()
	 */
	public boolean isEmpty() {
		for (ILayerEffect e : effects) {
			if (e.isSelected())
				return false;
		}
		return true;
	}

	public boolean isAnimated() {
		for (int i = 0; i < effects.size(); i++) {
			EffectObject eo = (EffectObject) effects.get(i);
			if (eo.isSelected())
				if (eo.isAnimated())
					return true;
		}
		return false;
	}

	public long getAnimationDuration() {
		long max = 0;
		for (int i = 0; i < effects.size(); i++) {
			EffectObject eo = (EffectObject) effects.get(i);
			if (eo.isSelected()) {
				long m = eo.getAnimationDuration();
				if (m > max)
					max = m;
			}
		}
		return max;
	}

	public void startAnimation() {
		// this.animationInProgress = true;
		for (int i = 0; i < effects.size(); i++) {
			EffectObject eo = (EffectObject) effects.get(i);
			// if (eo.isSelected()) {
			eo.startAnimation();
			// }
		}
	}

	public void endAnimation() {
		// this.animationInProgress = false;
		for (int i = 0; i < effects.size(); i++) {
			EffectObject eo = (EffectObject) effects.get(i);
			// if (eo.isSelected()) {
			eo.endAnimation();
			// }
		}
	}

	public void setAnimationStartLocation(long time) {
		for (int i = 0; i < effects.size(); i++) {
			EffectObject eo = (EffectObject) effects.get(i);
			// if (eo.isSelected()) {
			eo.setAnimationStartLocation(time);
			// }
		}
	}

	public boolean isAnimatedFor(TimingModel timingType) {
		for (int i = 0; i < effects.size(); i++) {
			EffectObject eo = (EffectObject) effects.get(i);
			if (eo.isSelected()) {
				if (eo.isAnimatedFor(timingType))
					return true;
			}
		}
		return false;
	}

	public long getAnimationDuration(TimingModel timing) {
		long max = 0;
		for (int i = 0; i < effects.size(); i++) {
			EffectObject eo = (EffectObject) effects.get(i);
			if (eo.isSelected()) {
				long m = eo.getAnimationDuration(timing);
				if (m > max)
					max = m;
			}
		}
		return max;
	}

	public long getAnimationDuration(TimeSpan span) {
		long max = 0;
		for (int i = 0; i < effects.size(); i++) {
			EffectObject eo = (EffectObject) effects.get(i);
			if (eo.isSelected()) {
				long m = eo.getAnimationDuration(span);
				if (m > max)
					max = m;
			}
		}
		return max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.BasicImageLayer#setAnimationTime(com.nokia.tools.media.utils.layers.TimingModel,
	 *      long)
	 */
	public void setAnimationTime(TimingModel model, long tOffs) {
		for (int i = 0; i < effects.size(); i++) {
			EffectObject eo = (EffectObject) effects.get(i);
			if (eo.isSelected()) {
				eo.setAnimationTime(model, tOffs);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#hasActiveEffect(java.lang.String)
	 */
	public boolean isEffectSelected(String name) {
		EffectObject obj = getEffectAsEO(name);
		return obj == null ? false : obj.isSelected();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#changeEffectOrder(com.nokia.tools.media.utils.layers.ILayerEffect,
	 *      int)
	 */
	public void changeEffectOrder(ILayerEffect effect, int delta) {

		// applyGraphics cannot be moved
		if (effect.getName().equals(EffectConstants.APPLYGRAPHICS))
			return;

		int pos = effects.indexOf(effect);
		if (pos == -1)
			return;
		int newpos = pos + delta;
		if (newpos >= 0 && newpos < effects.size()) {
			// do move
			if (newpos > pos) {

				// cannot do such move that applygraphics is moved
				ILayerEffect other = effects.get(newpos);
				if (other.getName().equals(EffectConstants.APPLYGRAPHICS))
					return;

				Object old = effects.get(pos);
				effects.add(newpos + 1, (ILayerEffect) old);
				effects.remove(pos);

				getPropertyChangeSupport().firePropertyChange(
						PROPERTY_EFFECT_ORDER, null, null);
				fireStateUpdateEvent();
			} else {

				// cannot do such move that applygraphics is moved
				ILayerEffect other = effects.get(newpos);
				if (other.getName().equals(EffectConstants.APPLYGRAPHICS))
					return;

				Object old = effects.get(pos);
				effects.remove(pos);
				effects.add(newpos, (ILayerEffect) old);

				getPropertyChangeSupport().firePropertyChange(
						PROPERTY_EFFECT_ORDER, null, null);
				fireStateUpdateEvent();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getActiveLayerEffects()
	 */
	public List<ILayerEffect> getSelectedLayerEffects() {
		List<ILayerEffect> list = new ArrayList<ILayerEffect>();
		Iterator it = effects.iterator();
		while (it.hasNext()) {
			EffectObject oe = (EffectObject) it.next();
			if (oe.isSelected())
				list.add(oe);
		}
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#addLayerEffect(java.lang.String)
	 */
	public ILayerEffect addLayerEffect(String effectName) {

		EffectObject effect = getEffectAsEO(effectName);
		if (effect != null) {
			if (!effect.isSelected()) {
				effect.setSelected(true);
				checkEffectState(effect);
			}
			return effect;
		}
		throw new RuntimeException("unknown effect"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#removeLayerEffect(java.lang.String)
	 */
	public void removeLayerEffect(String effectName) {
		EffectObject obj = getEffectAsEO(effectName);
		if (obj != null) {
			obj.setSelected(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getLayerEffects()
	 */
	public List<ILayerEffect> getLayerEffects() {
		return effect_unmodifiable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getEffect(java.lang.String)
	 */
	public ILayerEffect getEffect(String name) {
		return findEffect(name);
	}

	/**
	 * conveniencce method
	 * 
	 * @param name
	 * @return
	 */
	public EffectObject getEffectAsEO(String name) {
		return (EffectObject) findEffect(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.ILayer#getAvailableLayerEffects()
	 */
	public List<ILayerEffect> getAvailableLayerEffects() {
		return getAvailableLayerEffects(true);
	}

	protected List<ILayerEffect> getAvailableLayerEffects(
			boolean removeApplyGraphics) {

		

		List<ILayerEffect> result = new ArrayList<ILayerEffect>();

		EffectAvailabilityParser.IEffectMatcher matcher = new EffectAvailabilityParser.IEffectMatcher() {
			public boolean match(String effect, Map properties) {
				return ((hasImage() && !isEffectSelected(EffectConstants.APPLYCOLOR)) && "false"
						.equals(properties.get("multilayer")))
						|| (!hasImage() && "true".equals(properties
								.get("noimage")))
						|| (!isBackground() && "true".equals(properties
								.get("multilayer")));
			}
		};

		List<String> availableEffects = EffectAvailabilityParser.INSTANCE
				.getAvailableEffects(getParent().getId(), matcher);

		for (String effectName : availableEffects) {
			ILayerEffect effect = EffectObject.getEffect_original(effectName);
			result.add(effect);
		}

		if (removeApplyGraphics) {
			// remove apply graphics
			result.remove(EffectObject
					.getEffect_original(EffectConstants.APPLYGRAPHICS));
		}

		return result;
	}

	/**
	 * Sets ApplyGraphics=true, disables ApplyColor to this layer if possible.
	 * Sets default betweenLayer to AlphaBlend, BOverA
	 */
	public void disableNonDefaultEffects() {
		for (ILayerEffect e : effects) {
			if (e.getType() == EffectTypes.ON_LAYER_EFFECTS) {
				if (!e.getName().equals(EffectConstants.APPLYGRAPHICS)) {
					e.setSelected(false);
				}
			}
		}
		super.disableNonDefaultEffects();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.BasicImageLayer#updateEffectParameter()
	 */
	@Override
	protected void updateEffectParameter() {
		getEffect(EffectConstants.APPLYGRAPHICS).setEffectParameter(
				EffectConstants.SCALEMODE,
				Integer.toString(EffectObjectUtils.getIntegerValue(
						EffectConstants.APPLYGRAPHICS,
						EffectConstants.SCALEMODE,
						IMediaConstants.STRETCHMODE_ASPECT)));
	}

	public boolean isBitmapImage() {
		

		ILayerEffect applyColor = getEffect(IMediaConstants.APPLY_COLOR);
		if (applyColor.isSelected())
			return false;
		return super.isBitmapImage();
	}

	protected void setAttrEntityXY(int x, int y) {
		getImageLayer().setAttribute(ThemeTag.ATTR_ENTITY_X,
				Integer.toString(x));
		getImageLayer().setAttribute(ThemeTag.ATTR_ENTITY_Y,
				Integer.toString(y));
	}

	protected void setAttrImageXY(int x, int y) {
		getImageLayer()
				.setAttribute(ThemeTag.ATTR_IMAGE_X, Integer.toString(x));
		getImageLayer()
				.setAttribute(ThemeTag.ATTR_IMAGE_Y, Integer.toString(y));
	}

	/*
	 * Updates ImageLayer attrs as ENTITY_X / y, IMAGE_X/Y, FILE_PATH, etc.
	 */
	protected void setImageLayerAttrs(Map<Object, Object> attrs) {
		if (attrs.containsKey(ThemeTag.ATTR_NAME))
			attrs.remove(ThemeTag.ATTR_NAME);

		getImageLayer().getAttributes().putAll(attrs);
	}

	/** returns model object - ImageLayer associated with this obj. */
	protected LayerEffect getLayerEffect(EffectObject x) {
		Object le = getImageLayer().getLayerEffects(x.getName());
		if (le != null) {
			return getImageLayer().getLayerEffects(x.getName());
		} else {
			// create new layer effect
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(ThemeTag.ATTR_NAME, x.getName());
			getImageLayer().addEffect(map);
			return getLayerEffect(x);
		}
	}

	/**
	 * true if layer has enabled effects, other than applyGraphics
	 * 
	 * @return
	 */
	public boolean hasEffects() {
		return getSelectedLayerEffects().size() > 0;
	}

	public boolean hasImage() {
		if (isEffectSelected(IMediaConstants.APPLY_COLOR))
			return false;
		return super.hasImage();
	}

	public boolean hasMask() {

		if (isEffectSelected(IMediaConstants.APPLY_COLOR))
			return false;
		return super.hasMask();
	}

	/*
	 * update LayerEffect model objects with attributes from EffectObjects 1.
	 * remove old LayerEffect objects 2. create new LayerEffect in order of
	 * selected effects 3. update new LayerEffects
	 */
	public void propagateChangesToModel() throws ThemeException {
		// remove old
		List iml_effects = getImageLayer().getLayerEffects();
		iml_effects.clear();

		// effects update
		Iterator it = effects.iterator();
		while (it.hasNext()) {
			EffectObject eo = (EffectObject) it.next();
			if (!eo.getName().equals(EffectConstants.APPLYGRAPHICS)
					&& eo.isSelected()) {
				LayerEffect le = getLayerEffect(eo);
				updateLayerEffect(le, eo);
			} else if (eo.getName().equals(EffectConstants.APPLYGRAPHICS)) {
				// process apply graphics update - update stretchmode etc.
				String stretchMode = eo
						.getEffectParameter(EffectConstants.SCALEMODE);
				getImageLayer().setAttribute(
						EffectConstants.SCALEMODE,
						EffectObjectUtils.getScaleMode(Integer
								.parseInt(stretchMode)));
			}
		}
		if (getEffect(EffectConstants.APPLYCOLOR).isSelected()) {
			// remove image
			setCurrentImage(null);
			setCurrentMask(null);
			getImageLayer().removeAttribute(ThemeTag.FILE_NAME);
		}
	}

	
	/* finds effect of given name in the effect list */
	private ILayerEffect findEffect(String name) {
		for (int i = 0; i < effects.size(); i++) {
			if (effects.get(i).getName().equals(name))
				return effects.get(i);
		}
		return null;
	}

	private boolean isNumber(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Called when saving changes to model updates LayerEffect from EffectObject
	 */
	private void updateLayerEffect(LayerEffect le, EffectObject eObj) {
		le.setSelected(eObj.isSelected());

		Set set = eObj.getParameterList().keySet();
		Iterator iter = set.iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			EffectParameter param = (EffectParameter) eObj.getParameterList()
					.get(key);
			HashMap<Object, Object> paramAttrs = param.getAttributeMap();
			param.prepareAnimationModels();

			ParameterModel pm = le.getParameterModel(key);

			BasicEntityImage parent = (BasicEntityImage) getParent();
			if (pm == null) {
				// new param, create
				HashMap<Object, Object> pmAttrs = new HashMap<Object, Object>();
				pmAttrs.put(ThemeTag.ATTR_NAME, key);
				if (param.isAnimated()) {
					pm = new AnimatedParameterModel(parent.getThemeGraphics());
					param.getValueModelMap().remove(EffectConstants.LAYOUT);
					param.getTimingModelMap().remove(EffectConstants.LAYOUT);

					// set default value
					String defValue = (String) paramAttrs
							.get(EffectConstants.ATTR_DEFAULTVALUE);
					param.getValueModelMap().put(
							AnimationConstants.DEFAULTVALUE, defValue);

					HashMap vMap = ((MorphedGraphic) parent.getThemeGraphics())
							.appendAnimationModel(
									(HashMap<String, String>) param
											.getValueModelMap(), param
											.getTimingModelMap());

					String seqNo = (String) vMap.get(ThemeTag.ELEMENT_SEQNO);
					pmAttrs.put(ThemeTag.ELEMENT_VALUEMODEL_REF, seqNo);

					pm.setAttributes(pmAttrs);
				} else {
					pm = new ParameterModel(parent.getThemeGraphics());
					pmAttrs.put(ThemeTag.ATTR_VALUE, param.getValue());
					pm.setAttributes(pmAttrs);
				}
				le.setParameterModel(pm);
			} else if (param.isAnimated()) {
				param.getValueModelMap().remove(EffectConstants.LAYOUT);
				param.getTimingModelMap().remove(EffectConstants.LAYOUT);

				// set default value
				String defValue = (String) paramAttrs
						.get(EffectConstants.ATTR_DEFAULTVALUE);
				param.getValueModelMap().put(AnimationConstants.DEFAULTVALUE,
						defValue);

				HashMap vMap = ((MorphedGraphic) parent.getThemeGraphics())
						.appendAnimationModel(param.getValueModelMap(), param
								.getTimingModelMap());

				String seqNo = (String) vMap.get(ThemeTag.ELEMENT_SEQNO);
				paramAttrs.put(ThemeTag.ELEMENT_VALUEMODEL_REF, seqNo);

				pm.getAttributes().put(ThemeTag.ELEMENT_VALUEMODEL_REF, seqNo);
			}
			if ((paramAttrs.get(EffectConstants.ATTR_VALUE) != null && ((String) paramAttrs
					.get(EffectConstants.ATTR_VALUE)).trim().length() != 0)) {
				pm
						.setValue((String) paramAttrs
								.get(EffectConstants.ATTR_VALUE));
			} else {
				boolean mandatory = new Boolean((String) paramAttrs
						.get(EffectConstants.MANDATORY)).booleanValue();
				if (mandatory || true) {
					pm.setValue((String) paramAttrs
							.get(EffectConstants.ATTR_DEFAULTVALUE));
				}
			}
		}
	}

	/*
	 * check if other effect needs to be disabled in order to added effect
	 * @param x
	 */
	public void checkEffectState(EffectObject effect) {
		if (_supressEffectChecks)
			return;
		if (effect.isSelected()) {
			List<ILayerEffect> curentEff = getSelectedLayerEffects();
			List<ILayerEffect> availableEff = getAvailableLayerEffects(false);
			List<String> availableEffNames = new ArrayList<String>();
			for (ILayerEffect eff : availableEff) {
				availableEffNames.add(eff.getName());
			}
			for (ILayerEffect eff : curentEff) {
				if (!availableEffNames.contains(eff.getName())) {
					eff.setSelected(false);
				}
			}
		} else {
			// effect was removed
			if (effect.getName().equals(EffectConstants.APPLYCOLOR)
					&& hasImage()) {
				getEffect(EffectConstants.APPLYGRAPHICS).setSelected(true);
			}
		}
	}

	/**
	 * updates effect object from LayerEffect (model)
	 * 
	 * @param le
	 * @param obj
	 */
	private void updateEffectObject(EffectObject obj, LayerEffect le) {
		obj.setModel(le);

		Set set = obj.getParameterList().keySet();
		Iterator iter = set.iterator();
		BasicEntityImage parent = (BasicEntityImage) getParent();
		while (iter.hasNext()) {
			String s = (String) iter.next();
			EffectParameter param = (EffectParameter) obj.getParameterList()
					.get(s);
			ParameterModel model = le.getParameterModel(s);
			HashMap map = param.getAttributeMap();
			if (model != null) {
				if (model.isAnimatedModel()) {
					param.setAnimated(true);
					String valueRefNo = model
							.getAttribute(ThemeTag.ELEMENT_VALUEMODEL_REF);
					BaseValueModelInterface baseValue = ((MorphedGraphic) parent
							.getThemeGraphics()).getValueModel(valueRefNo);
					HashMap valueMap = baseValue.getParameters();
					param.setValueModelMap((HashMap) valueMap.clone());
					String animSeqNo = (String) valueMap
							.get(ThemeTag.ELEMENT_TIMINGMODEL_REF);
					BaseTimingModelInterface baseTime = ((MorphedGraphic) parent
							.getThemeGraphics()).getTimingModel(animSeqNo);
					param.setTimingModelMap((HashMap<String, String>) baseTime
							.getParameters().clone());
					// init animation model
					param.initAnimationModel();
					// fix first point - set all params as 'set' at that point
					PolyLineControlPointModel cpm = obj.getControlPointModel();
					if (cpm.getControlPointsCount() > 0) {
						IControlPoint first = cpm.findControlPointAt(0);
						if (first != null) {
							cpm.getMediator().setAll(first.getData());
						}
					}
				} else {
					if (model.getValue(s) != null)
						map.put(EffectConstants.ATTR_VALUE, model.getValue(s));
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.BasicImageLayer#processImage(java.awt.image.RenderedImage,
	 *      java.util.List, boolean)
	 */
	protected RenderedImage processImage(RenderedImage image, List effectList,
			boolean preview) {
		// synchronized (EffectParameter.isPreview) {
		try {
			HashMap<String, Object> map = null;
			BasicEntityImage parent = (BasicEntityImage) getParent();
			for (int i = 0; i < effectList.size(); i++) {

				Object effect = effectList.get(i);
				if (effect instanceof LayerEffect) {
					LayerEffect le = (LayerEffect) effectList.get(i);
					map = new HashMap<String, Object>();
					map.put(EffectConstants.EFFECTOBJECT, le);
					// skip between-layer effects
					if (EffectObject.isBetweenLayerEffect(le
							.getAttribute(ThemeTag.ATTR_NAME))
							|| le.getAttribute(ThemeTag.ATTR_NAME)
									.equalsIgnoreCase(
											EffectConstants.MOVINGLAYER))
						continue;
					map.put(EffectConstants.LAYOUT, parent.getElementLayout());
					map.put("LayerData", this); 

					// Putting the name of current theme which is
					// to be used in ApplyGraphics at a later stage
					// to remove the images corresponding to a particular
					// theme
					// when that theme is closed in the editor.
					map.put(EffectConstants.CURRENT_THEME, getTheme());
					
					image = OnLayerEffects.ProcessImage(le
							.getAttribute(ThemeTag.ATTR_NAME), image, map);
				} else if (effect instanceof EffectObject) {
					EffectObject le = (EffectObject) effectList.get(i);
					map = new HashMap<String, Object>();
					map.put(EffectConstants.EFFECTOBJECT, le);
					// skip between-layer effects
					if (le.isBetweenLayerEffect()
							|| le.getAttributeAsString(ThemeTag.ATTR_NAME)
									.equalsIgnoreCase(
											EffectConstants.MOVINGLAYER))
						continue;
					map.put(EffectConstants.LAYOUT, parent.getElementLayout());
					map.put("LayerData", this); //$NON-NLS-1$
					// Putting the current theme which is
					// to be used in ApplyGraphics at a later stage
					// to remove the images corresponding to a particular
					// theme
					// when that theme is closed in the editor.
					map.put(EffectConstants.CURRENT_THEME, getTheme());
				

					image = OnLayerEffects.ProcessImage(le
							.getAttributeAsString(ThemeTag.ATTR_NAME), image,
							map);
				}
			}

			if (getCurrentMask() != null) {

				CoreImage mask = CoreImage.create(getCurrentMask());
				int parentW = parent.getWidth();
				int parentH = parent.getHeight();
				if (parentW != 0
						&& parentH != 0
						&& (mask.getWidth() != parentW || mask.getHeight() != parentH)) {
					mask.stretch(getParent().getWidth(), getParent()
							.getHeight(), CoreImage.SCALE_TO_FIT);
				}
				image = CoreImage.create(image).applyMask(
						mask,
						((EditableEntityImage) getParent()).getEntity()
								.getToolBox().SoftMask).getAwt();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return image;
	}

	/**
	 * disables all effect with given type, when type is ON_LAYER, disables all
	 * but ApplyGraphics
	 * 
	 * @param between_layer_effects
	 */
	public void disableEffects(EffectTypes type) {
		for (int i = 0; i < effects.size(); i++) {
			if (effects.get(i).getType() == type
					&& !effects.get(i).getName().equals(
							EffectConstants.APPLYGRAPHICS)) {
				effects.get(i).setSelected(false);
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (supressEventsPropagation || !isEnabled())
			return;
		if (evt.getSource() instanceof ILayerEffect) {

			if (ILayerEffect.PROPERTY_STATE.equals(evt.getPropertyName())) {
				supressEventsPropagation = true;
				checkEffectState((EffectObject) evt.getSource());
				supressEventsPropagation = false;

				fireStateUpdateEvent();
			} else if (EffectObject.PROPERTY_ANIMATION_STATE.equals(evt
					.getPropertyName())) {
				// animation properties of child effect changed
				getPropertyChangeSupport().firePropertyChange(
						PROPERTY_ANIMATION_STATE, null, null);
			}
		}
	}

	public synchronized ITimeLineRow getTimeLineRow() {
		return timeLineRow;
	}

	public void effectSelectedOrUnselected(EffectObject object) {
		getPropertyChangeSupport().firePropertyChange(
				PROPERTY_STRUCTURE_CHANGE, null, null);
	}

	public boolean isAnimatedInPreview() {
		return animatedInPreview;
	}

	public void setAnimatedInPreview(boolean animatedInPreview) {
		this.animatedInPreview = animatedInPreview;
		for (int i = 0; i < effects.size(); i++) {
			EffectObject eo = (EffectObject) effects.get(i);
			eo.setAnimatedInPreview(animatedInPreview);
		}
	}

	/*
	 * @see com.nokia.tools.media.utils.layers.ILayer#getAnimatedImage(int,
	 *      boolean)
	 */
	public RenderedImage getProcessedImage(long timeOffset, boolean preview) {
		setAnimationTime(TimingModel.RealTime, timeOffset);
		return super.getProcessedImage(timeOffset, preview);
	}

	public void clearTimeLineNodes() {
		timeLineRow = null;
		for (Object c : effects) {
			((EffectObject) c).clearTimeLineNodes();
		}
	}

	public void setTimeLineRow(ITimeLineRow timeLineRow) {
		this.timeLineRow = timeLineRow;
	}

	@Override
	public String toString() {
		try {
			return "EditableImageLayer(" + getImageLayer().getAttribute("name") //$NON-NLS-1$ //$NON-NLS-2$
					+ ")"; //$NON-NLS-1$
		} catch (Exception e) {
			return super.toString();
		}
	}

	public String getStretchMode() {
		try {
			if (hasImage() && isEffectSelected(EffectConstants.APPLYGRAPHICS)) {
				EffectParameter par = (EffectParameter) getEffect(
						EffectConstants.APPLYGRAPHICS).getParameter(
						EffectConstants.SCALEMODE);
				if (par.isValueSet()) {
					String s = par.getValue();
					String mode = EffectObjectUtils.getValueLiteral(
							EffectConstants.APPLYGRAPHICS, par.getName(),
							Integer.parseInt(s));
					return mode;
				} else {
					return TSDataUtilities.getDefaultStretchMode(getParent()
							.getId());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.BasicImageLayer#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {

		return super.getAdapter(adapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.BasicImageLayer#setStretchMode(java.lang.String)
	 */
	@Override
	public void setStretchMode(String stretchMode) {
		EffectObject object = getEffectAsEO(EffectConstants.APPLYGRAPHICS);
		if (object != null) {
			try {
				object.setEffectParameter(EffectConstants.SCALEMODE, Integer
						.toString(EffectObjectUtils.getIntegerValue(
								EffectConstants.APPLYGRAPHICS,
								EffectConstants.SCALEMODE, stretchMode)));
			} catch (RuntimeException e) {
				// when stretch mode was not one of allowed options
				object.setEffectParameter(EffectConstants.SCALEMODE, "");
			}
		}
	}

}