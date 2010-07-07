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
package com.nokia.tools.theme.s60.ui.animation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.nokia.tools.media.utils.layers.IEffectParameter;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.cp.IControlPoint;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.theme.s60.editing.EditableEntityImage;
import com.nokia.tools.theme.s60.editing.anim.EffectAvailabilityParser;
import com.nokia.tools.theme.s60.editing.anim.PolyLineControlPointModel;
import com.nokia.tools.theme.s60.effects.EffectParameter;
import com.nokia.tools.theme.s60.morphing.AnimationConstants;
import com.nokia.tools.theme.s60.morphing.AnimationFactory;
import com.nokia.tools.theme.s60.ui.Messages;
import com.nokia.tools.theme.s60.ui.animation.timing.ITimingModelUI;

public class TimingModelComposite extends Composite {

	private String timeModelInput[];

	private List<Map> timeModels = new ArrayList<Map>();

	private IEffectParameter parameter;

	private ComboViewer timeCombo;

	private Composite timingModelComposite;

	private ITimingModelUI timeContributor;
	
	private List<TimingModel> supportTimings = new ArrayList<TimingModel>();

	private boolean uiInitDone;

	public TimingModelComposite(Composite parent, int style) {
		super(parent, style);
		GridLayout layout = new GridLayout();
		setLayout(layout);
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		initModels();
		createControls();
	}

	/*
	 * Initializes combo box inputs
	 */
	private void initModels() {
		Map timeModelClassMap = AnimationFactory.getTimingModelClassMap();

		Iterator it = timeModelClassMap.values().iterator();
		ArrayList<String> ll = new ArrayList<String>();
		while (it.hasNext()) {
			Map m = (Map) it.next();
			String name = (String) m.get(AnimationConstants.ATTR_UINAME);
			if (existUIClass(m.get(AnimationConstants.ATTR_UICLASS))) {
				ll.add(name);
				timeModels.add(m);
			}
		}
		timeModelInput = (String[]) ll.toArray(new String[ll.size()]);
	}

	private boolean existUIClass(Object object) {
		try {
			String uiClass = (String) object;
			uiClass = uiClass.replace(
					"com.nokia.tools.theme.s60.morphing.valuemodels",
					"com.nokia.tools.theme.s60.ui.animation.models");
			uiClass = uiClass.replace("com.nokia.tools.theme.s60.morphing.timemodels",
					"com.nokia.tools.theme.s60.ui.animation.timing");
			Class.forName(uiClass).newInstance();
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	private void createControls() {
		Composite top = new Composite(this, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		top.setLayoutData(gd);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		top.setLayout(layout);
		Label label = new Label(top, SWT.NONE);
		label.setText(Messages.AnimDialog_timingModelLabel);

		createTimeModelCombo(top);

		Group timeGroup = new Group(this, SWT.NONE);
		timeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		timeGroup.setText(Messages.AnimDialog_timingGroupLabel);
		timeGroup.setLayout(new FillLayout());
		timingModelComposite = timeGroup;

		// init control state
		uiInitDone = true;
	}

	private void createTimeModelCombo(Composite root) {
		timeCombo = new ComboViewer(root, SWT.DROP_DOWN | SWT.READ_ONLY);
		timeCombo.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof String[]) {
					return (String[]) inputElement;
				}
				return null;
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}

			public void dispose() {
			}
		});
		timeCombo.setLabelProvider(new ILabelProvider() {
			public Image getImage(Object element) {
				return null;
			}

			public String getText(Object element) {
				return element.toString();
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}
		});

		timeCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				timeComboSelChanged();
			}
		});

		timeCombo.setInput(timeModelInput);
	}

	protected void timeComboSelChanged() {
		
		String timingUiName = timeCombo.getCombo().getText();		
		Map m = getTimingModel(timingUiName, AnimationConstants.ATTR_UINAME);

		clearComposite(timingModelComposite);
		contributeTimeModelUI(timingModelComposite, m);

		if (timeContributor != null && parameter != null) {
			String paramTimeModelName = (String) ((EffectParameter) parameter)
					.getTimingModelMap().get(AnimationConstants.ATTR_NAME);
			String selTimeModelName = (String) m.get(
					AnimationConstants.ATTR_NAME);
			if (selTimeModelName.equals(paramTimeModelName)) {
				try {
					timeContributor.setParameters(((EffectParameter) parameter)
							.getTimingModelMap());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void contributeTimeModelUI(Composite parent, Map m) {
		String uiClass = (String) m.get(AnimationConstants.ATTR_UICLASS);
		uiClass = uiClass.replace("com.nokia.tools.theme.s60.morphing.timemodels",
				"com.nokia.tools.theme.s60.ui.animation.timing");
		try {
			Object contrib = Class.forName(uiClass).newInstance();
			if (contrib instanceof ITimingModelUI) {
				timeContributor = (ITimingModelUI) contrib;
				timeContributor.createUI(parent);
				parent.layout();
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private void clearComposite(Composite parent) {
		if (parent == null)
			return;
		Control[] ch = parent.getChildren();
		for (int i = 0; i < ch.length; i++) {
			try {
				ch[i].dispose();
			} catch (Exception e) {
			}
		}
	}

	public void setData(ILayerEffect effect, IEffectParameter parameter) {
		//this.effect = effect;
		this.parameter = parameter;
		
		//init list of supported timings
		supportTimings.clear();
		for (TimingModel m: TimingModel.values()) {
			SkinnableEntity ent = ((EditableEntityImage)effect.getParent().getParent()).getEntity();
			if (EffectAvailabilityParser.INSTANCE.supportsAnimationTiming(ent, m))
				supportTimings.add(m);
		}
		String[] tModels = new String[supportTimings.size()];
		for (int i=0; i<supportTimings.size();i++)
			tModels[i] = supportTimings.get(i).name();
		timeCombo.setInput(tModels);
		
		if (uiInitDone) {
			updateUiState();
		}
	}

	private void updateUiState() {
		
		EffectParameter p = (EffectParameter) parameter;		
		String timeModelName = (String) p.getTimingModelMap().get(
				AnimationConstants.ATTR_NAME);
		
		Map tModel = getTimingModel(timeModelName, AnimationConstants.ATTR_NAME);
		if (tModel != null) {
			timeCombo.setSelection(new StructuredSelection((String) tModel.get(
					AnimationConstants.ATTR_UINAME)));
			timeComboSelChanged();
		}
		
	}

	/**
	 * called when dialog closes with OK updates effect object with data
	 */
	public void commitChanges() {
		EffectParameter par = (EffectParameter) parameter;
		if (timeContributor != null) {
			
			TimingModel oldModel = par.getTimingModel();
			par.setTimingModelMap(timeContributor.getParameters());
			TimingModel newModel = par.getTimingModel();
			if (oldModel != newModel) {
				
				/* clear control points for old timing model for this parameter */
				
				PolyLineControlPointModel pcm = par.getParent().getControlPointModel();
				List<IControlPoint> points = pcm.getControlPointForParam(par.getParameterIndex());
				for (IControlPoint p: points) {
					pcm.getMediator().unset(p.getData(), par.getParameterIndex());
				}
				pcm.removeUnusedPoints();
				/* if only one left, remove all, one make no sense */
				if (pcm.getControlPointsCount() < 2)
					pcm.removeAllControlPoints();
				
				/* if there is nothing animated in RealTime timing model, create default point at 1000 */
				if (TimingModel.RealTime == newModel && pcm.getControlPointsCount() == 0) {					
					pcm.createControlPoint(1000);
				}
			}
		}
	}
	
	private Map getTimingModel(String value, String byAttr) {
		for (Map m: timeModels) {
			if (value.equals(m.get(byAttr)))
				return m;			
		}
		return null;
	}
}
