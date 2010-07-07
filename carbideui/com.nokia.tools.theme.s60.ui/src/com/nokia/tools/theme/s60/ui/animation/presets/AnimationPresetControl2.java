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
package com.nokia.tools.theme.s60.ui.animation.presets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import com.nokia.tools.media.utils.layers.IEffectParameterDescriptor;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.cp.IControlPoint;
import com.nokia.tools.theme.s60.editing.anim.IValueMediator;
import com.nokia.tools.theme.s60.editing.anim.PolyLineControlPointModel;
import com.nokia.tools.theme.s60.editing.anim.TSCPData;
import com.nokia.tools.theme.s60.editing.providers.TimeLabelProvider;
import com.nokia.tools.theme.s60.editing.utils.AnimationPresetsUtils;
import com.nokia.tools.theme.s60.effects.EffectObject;
import com.nokia.tools.theme.s60.effects.EffectParameter;
import com.nokia.tools.theme.s60.ui.Messages;

public class AnimationPresetControl2 extends Composite implements
		PropertyChangeListener {

	/** default animation time */
	private static final int DEFAULT_ANIM_TIME = 1000;

	private ILayerEffect effect;

	private EffectParameter param;

	private AnimationPresetControl canvas;

	private CCombo presetCombo;

	private Spinner animationTime;

	private int duration;

	private String minParamValue, maxParamValue;

	private IEffectParameterDescriptor paramDesc;

	private TimeLabelProvider labelProvider;

	public AnimationPresetControl2(Composite parentShell, int style,
			ILayerEffect effect, String paramName) {
		super(parentShell, style);
		this.effect = effect;
		this.param = (EffectParameter) effect.getParameter(paramName);
		this.param.updateAnimModels();

		labelProvider = new TimeLabelProvider(param.getTimingModel(), param
				.getTimeSpan());
		labelProvider.setAddUnitPostfix(true);

		paramDesc = effect.getDescriptor().getParameterDescriptor(paramName);
		minParamValue = paramDesc.getMinVal();
		maxParamValue = paramDesc.getMaxVal();

		GridLayout gl = new GridLayout();
		gl.marginWidth = gl.marginHeight = 0;
		setLayout(gl);
		createDialogArea(this);
	}

	protected Control createDialogArea(Composite parent) {
		createControls(parent);
		initializeValues();
		return parent;
	}

	@SuppressWarnings("unchecked")
	private void initializeValues() {
		EffectObject eff = (EffectObject) effect;
		EffectParameter ep = (EffectParameter) param;
		int paramIndex = ep.getParameterIndex();
		if (ep.isAnimated()) {

			PolyLineControlPointModel<TSCPData> model = eff
					.getControlPointModel();
			IValueMediator mediator = model.getMediator();
			long animDuration = 0;
			if (ep.getTimingModel() == TimingModel.RealTime) {
				animDuration = ep.getAnimationDuration(TimingModel.RealTime);
			} else {
				animDuration = ep.getAnimationDuration(ep.getTimeSpan());
			}

			if (ep.getTimingModel() == TimingModel.RealTime) {
				if (animDuration > 0) {
					animationTime.setSelection((int) animDuration);
					duration = (int) animDuration;
				}
			}

			// get data
			if (animDuration > 0) {
				List<IControlPoint> points = model
						.getControlPointForParam(paramIndex);
				if (points.size() > 1) {
					calculatePresetDataAndSetCorrespondingPresetNameInComboBox(
							eff, paramIndex, mediator, animDuration, points);
				} else {
					// set linear rise to combo
					// it was select(4)
					// and now its changed to select(0)
					// because linear rise is now the 0th item
					presetCombo.select(0);
				}
			}

			if (ep.getTimingModel() == TimingModel.Relative) {
				canvas.setLastPointMovableX(true);
			}
		}

		canvas.setMinLabel(minParamValue);
		canvas.setMaxLabel(maxParamValue);
		canvas.setAnimationDuration(duration);

		long duration = param.getAnimationDuration(param.getTimingModel());
		canvas.setStartLabel(labelProvider.getLabel(0));
		canvas.setEndLabel(labelProvider.getLabel(duration));
	}

	private void calculatePresetDataAndSetCorrespondingPresetNameInComboBox(
			EffectObject eff, int paramIndex, IValueMediator mediator,
			long animDuration, List<IControlPoint> points) {
		double[] x = new double[points.size()];
		double[] y = new double[points.size()];

		buildPresetData(eff, paramIndex, mediator, animDuration, points, x, y);
		canvas.setPresetData(x, y);

		String presetTextInComboBox = AnimationPresetsUtils.getPresetName(x, y);
		if (presetTextInComboBox == null)
			presetTextInComboBox = Messages.presetsDlg_customPreset;
		// set custom preset in combo
		presetCombo.setText(presetTextInComboBox);
	}

	private void buildPresetData(EffectObject eff, int paramIndex,
			IValueMediator mediator, long animDuration,
			List<IControlPoint> points, double[] x, double[] y) {
		for (int i = 0; i < points.size(); i++) {
			x[i] = points.get(i).getTime() / (double) animDuration;
			float value = mediator
					.getValue(points.get(i).getData(), paramIndex);
			String sMin = eff.getDescriptor()
					.getParameterDescriptor(paramIndex).getMinVal();
			String sMax = eff.getDescriptor()
					.getParameterDescriptor(paramIndex).getMaxVal();
			int max = Integer.parseInt(sMax == null ? "255" : sMax);
			int min = Integer.parseInt(sMin == null ? "0" : sMin);
			int base = min;
			int range = max - min;
			y[i] = (value - base) / (double) range;
		}
	}

	private void createControls(Composite container) {
		Composite root = new Composite(container, SWT.NONE);

		root.setLayout(new GridLayout(1, false));
		((GridLayout) root.getLayout()).marginWidth = 8;
		((GridLayout) root.getLayout()).marginHeight = 12;

		Composite controls = new Composite(root, SWT.NONE);
		controls.setLayout(new GridLayout(2, false));
		((GridLayout) controls.getLayout()).marginWidth = 0;
		((GridLayout) controls.getLayout()).marginHeight = 0;
		((GridLayout) controls.getLayout()).marginBottom = 10;
		controls.setLayoutData(new GridData(GridData.FILL_BOTH));

		new Label(controls, 0).setText(Messages.presetsDlg_presets);
		
		createPresetCombo(controls);

		if (param.getTimingModel() == TimingModel.RealTime) {
			new Label(controls, 0)
					.setText(Messages.presetsDlg_animationDuration);
			animationTime = new Spinner(controls, SWT.BORDER);
			animationTime.setMaximum(999999);
			animationTime.setSelection(DEFAULT_ANIM_TIME);
			duration = DEFAULT_ANIM_TIME;
			animationTime.setLayoutData(new GridData());
			((GridData) animationTime.getLayoutData()).widthHint = 60;
			((GridData) animationTime.getLayoutData()).minimumWidth = 60;

			animationTime.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					duration = animationTime.getSelection();
					canvas.setAnimationDuration(duration);
					canvas.setEndLabel(labelProvider.getLabel(duration));
					canvas.redraw();
				}
			});
		}

		Composite canvasBorder = new Composite(root, SWT.BORDER);
		GridLayout gl = new GridLayout(1, false);
		gl.marginHeight = gl.marginWidth = 0;
		canvasBorder.setLayout(gl);

		canvas = new AnimationPresetControl(canvasBorder, SWT.NONE) {
			@Override
			protected String computeTooltip(double xValue, double yValue) {
				try {
					int min = Integer.parseInt(minLabel);
					int max = Integer.parseInt(maxLabel);
					int value = (int) (min * (1 - yValue) + max * yValue);
					long duration = param.getAnimationDuration(param
							.getTimingModel());
					String timeLabel = labelProvider
							.getLabel((long) (duration * xValue));
					return value + "\n" + timeLabel;
				} catch (Exception ex) {
				}
				return "";
			}
		};

		GridData gd = new GridData();
		gd.widthHint = canvas.getSize().x;
		gd.heightHint = canvas.getSize().y;
		canvas.setLayoutData(gd);
		canvas.setCanvasListener(this);
	}

	private void createPresetCombo(Composite controls) {
		presetCombo = new CCombo(controls, SWT.SINGLE | SWT.BORDER | SWT.FLAT
				| SWT.READ_ONLY);
		presetCombo.setBackground(ColorConstants.white);
		presetCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handlePresetSelected(e);
			}
		});

		for (String p : AnimationPresetsUtils.getAvailablePresetNames()) {
			presetCombo.add(p);
		}

		presetCombo.setLayoutData(new GridData());
		((GridData) presetCombo.getLayoutData()).widthHint = 85;
		((GridData) presetCombo.getLayoutData()).minimumWidth = 85;
		presetCombo.setVisibleItemCount(6);
	}

	protected void handlePresetSelected(SelectionEvent e) {
		if (AnimationPresetsUtils.getAnimationPreset(presetCombo.getText()) != null) {
			canvas.setPresetData(AnimationPresetsUtils
					.getAnimationPreset(presetCombo.getText()));
			canvas.redraw();
		}
	}

	public double[][] getResultData() {
		return canvas.getDataTransformed(65535, 65535);
	}

	public int getAnimTime() {
		return duration;
	}

	// listening on canvas, gets called when user dragged some points =
	// customized preset.
	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName() == AnimationPresetControl.PROPERTY_POINTS) {
			// set custom text in combo
			presetCombo.setText(Messages.presetsDlg_customPreset);
		}
	}
}
