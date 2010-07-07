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
package com.nokia.tools.theme.s60.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.LayerEffect;
import com.nokia.tools.platform.theme.ParameterModel;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.s60.morphing.AnimationFactory;
import com.nokia.tools.theme.s60.morphing.KErrArgument;
import com.nokia.tools.theme.s60.morphing.timemodels.BaseTimingModelInterface;
import com.nokia.tools.theme.s60.morphing.valuemodels.BaseValueModelInterface;

/**
 * The Class Defines the MorphedGraphic Object for a skinnableentity.
 */
public class MorphedGraphic extends ThemeGraphic {
	private List<Object> tmList;
	private List<Object> vmList;

	/**
	 * Constructor
	 */
	public MorphedGraphic(ThemeBasicData data) {
		super(data);
		tmList = new ArrayList<Object>();
		vmList = new ArrayList<Object>();
	}

	public Object clone() {
		MorphedGraphic obj = null;
		obj = (MorphedGraphic) super.clone();
		ArrayList<Object> al = null;
		HashMap<String, String> m = null;
		// / Cloning TimingModel and valuemodels
		List<Object> vms = this.vmList;
		al = new ArrayList<Object>(vms);
		al = (ArrayList) al.clone();
		for (int j = 0; j < al.size(); j++) {
			
			BaseValueModelInterface bvmi = (BaseValueModelInterface) al.get(j);
			HashMap<String, String> m1 = bvmi.getParameters();
			BaseValueModelInterface bvmi1 = AnimationFactory
					.getValueModelInstance(m1.get(ThemeTag.FLAVOUR_NAME)
							.toString());
			m = new HashMap<String, String>(m1);
			m = (HashMap) m.clone();
			try {
				bvmi1.setParameters(m);
			} catch (KErrArgument e) {
				
				e.printStackTrace();
			}
			al.set(j, bvmi1);
		}
		obj.vmList = (List<Object>) al;

		List<Object> tms = this.tmList;
		al = new ArrayList<Object>(tms);
		al = (ArrayList) al.clone();
		for (int j = 0; j < al.size(); j++) {
			
			BaseTimingModelInterface btmi = (BaseTimingModelInterface) al
					.get(j);
			HashMap<String, String> m1 = btmi.getParameters();
			BaseTimingModelInterface btmi1 = AnimationFactory
					.getTimingModelInstance(m1.get(ThemeTag.ATTR_NAME)
							.toString());
			m = new HashMap<String, String>(m1);
			m = (HashMap) m.clone();
			try {
				btmi1.setParameters(m);
			} catch (KErrArgument e) {
				
				e.printStackTrace();
			}
			al.set(j, btmi1);
		}
		obj.tmList = (List<Object>) al;
		return obj;
	}

	// APIs for ValueModel and Timing Models
	/*
	 * This will return a list of TimingModel Objects for this ThemeGraphic
	 */
	public List getTimingModels() {
		return tmList;
	}

	/*
	 * This will return a list of ValueModel Objects for this ThemeGraphic
	 */
	public List getValueModels() {
		return vmList;
	}

	public void addTimingModel(BaseTimingModelInterface btmi) {
		if (btmi == null)
			return;
		else
			tmList.add(btmi);
	}

	public void addValueModel(BaseValueModelInterface bvmi) {
		if (bvmi == null)
			return;
		else
			vmList.add(bvmi);
	}

	public BaseValueModelInterface getValueModel(String seq) {
		for (int i = 0; i < vmList.size(); i++) {
			BaseValueModelInterface bvmi = (BaseValueModelInterface) vmList
					.get(i);
			if (bvmi.getParameters().get(ThemeTag.ELEMENT_SEQNO) == null)
				continue;
			String seqnostr = (bvmi.getParameters())
					.get(ThemeTag.ELEMENT_SEQNO).toString();
			if (seqnostr.equalsIgnoreCase(seq))
				return bvmi;

		}
		return null;
	}

	public BaseTimingModelInterface getTimingModel(String seq) {
		for (int i = 0; i < tmList.size(); i++) {
			BaseTimingModelInterface btmi = (BaseTimingModelInterface) tmList
					.get(i);
			String seqnostr = (btmi.getParameters())
					.get(ThemeTag.ELEMENT_SEQNO).toString();
			if (seqnostr.equalsIgnoreCase(seq))
				return btmi;

		}
		return null;
	}

	private String generateValueModelSeqNo() {
		int max = 0;
		for (int i = 0; i < vmList.size(); i++) {
			BaseValueModelInterface bvmi = (BaseValueModelInterface) vmList
					.get(i);
			if ((bvmi.getParameters()).get(ThemeTag.ELEMENT_SEQNO) == null)
				continue;
			String seqnostr = (bvmi.getParameters())
					.get(ThemeTag.ELEMENT_SEQNO).toString();
			int seqno = Integer.parseInt(seqnostr);
			if (max <= seqno)
				max = Math.max(max, seqno);
		}

		max = max + 1;
		return max + "";
	}

	private String generateTimingModelSeqNo() {
		int max = 0;
		for (int i = 0; i < tmList.size(); i++) {
			BaseTimingModelInterface btmi = (BaseTimingModelInterface) tmList
					.get(i);

			String seqnostr = (btmi.getParameters())
					.get(ThemeTag.ELEMENT_SEQNO).toString();
			int seqno = Integer.parseInt(seqnostr);
			if (max <= seqno)
				max = Math.max(max, seqno);
		}
		max++;
		return max + "";
	}

	/*
	 * This takes 2 maps valuemodel mapo and timingmodel map and create
	 * respective objects and adds to the respective list
	 */

	public HashMap appendAnimationModel(HashMap<String, String> vm,
			HashMap<String, String> tm) {
		BaseValueModelInterface bvmi = AnimationFactory
				.getValueModelInstance(vm.get(ThemeTag.FLAVOUR_NAME).toString());
		BaseTimingModelInterface btmi = AnimationFactory
				.getTimingModelInstance(tm.get(ThemeTag.ATTR_NAME).toString());

		if (vm.containsKey(ThemeTag.ELEMENT_SEQNO)) {
			vmList.remove(getValueModel(vm.get(ThemeTag.ELEMENT_SEQNO)
					.toString()));
			
		} else {
			String seqno = generateValueModelSeqNo();
			vm.put(ThemeTag.ELEMENT_SEQNO, seqno);
		}

		if (tm.containsKey(ThemeTag.ELEMENT_SEQNO)) {
			tmList.remove(getTimingModel(tm.get(ThemeTag.ELEMENT_SEQNO)
					.toString()));
			
		} else {
			String seqno = generateTimingModelSeqNo();
			tm.put(ThemeTag.ELEMENT_SEQNO, seqno);
			vm.put(ThemeTag.ELEMENT_TIMINGMODEL_REF, seqno);
		}

		try {
			if (vm.get(ThemeTag.DEFAULT) == null)
				vm.put(ThemeTag.DEFAULT, "0");
			bvmi.setParameters(vm);
			btmi.setParameters(tm);
			vmList.add(bvmi);
			tmList.add(btmi);

			return vm;

		} catch (KErrArgument e) {
			
			e.printStackTrace();
		}

		return null;

	}

	public void removeUnusedValueModels() {
		try {
			List<Object> usedList = new ArrayList<Object>();
			for (int i = 0; i < getImageLayers().size(); i++) {
				ImageLayer iml = (ImageLayer) getImageLayers().get(i);
				for (int j = 0; j < iml.getLayerEffects().size(); j++) {
					LayerEffect le = (LayerEffect) iml.getLayerEffects().get(j);
					for (int k = 0; k < le.getParameterModels().size(); k++) {
						ParameterModel pm = (ParameterModel) le
								.getParameterModels().get(k);
						String vRef = pm
								.getAttribute(ThemeTag.ELEMENT_VALUEMODEL_REF);
						if (vRef != null) {
							BaseValueModelInterface vModel = getValueModel(vRef);
							if (vModel != null && !usedList.contains(vModel)) {
								usedList.add(vModel);
							}
						}
					}
				}
			}
			vmList = usedList;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeUnusedTimeModels() {
		List<Object> usedList = new ArrayList<Object>();
		for (int i = 0; i < vmList.size(); i++) {
			BaseValueModelInterface value = (BaseValueModelInterface) vmList
					.get(i);
			BaseTimingModelInterface time = getTimingModel((String) value
					.getParameters().get(ThemeTag.ELEMENT_TIMINGMODEL_REF));
			if (!usedList.contains(time))
				usedList.add(time);
		}
		tmList = usedList;
	}

	public void Tick(HashMap map) {
		for (int j = 0; j < tmList.size(); j++) {
			BaseTimingModelInterface timeModel = (BaseTimingModelInterface) tmList
					.get(j);
			timeModel.Tick(map);
		}
		for (int i = 0; i < vmList.size(); i++) {
			BaseValueModelInterface value = (BaseValueModelInterface) vmList
					.get(i);
			String timeModelNo = (String) value.getParameters().get(
					ThemeTag.ELEMENT_TIMINGMODEL_REF);
			BaseTimingModelInterface timeModel = getTimingModel(timeModelNo);
			if (timeModel != null)
				value.Tick(-1, timeModel.getTimeValue());
		}
	}
}
