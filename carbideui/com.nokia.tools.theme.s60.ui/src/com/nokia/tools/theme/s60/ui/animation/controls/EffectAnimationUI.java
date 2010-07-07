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
package com.nokia.tools.theme.s60.ui.animation.controls;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.nokia.tools.media.utils.layers.IEffectParameter;
import com.nokia.tools.media.utils.layers.IEffectParameterDescriptor;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.ITimeLine;
import com.nokia.tools.media.utils.timeline.ITimeListener;
import com.nokia.tools.media.utils.timeline.cp.IControlPoint;
import com.nokia.tools.media.utils.timeline.cp.IControlPointListener;
import com.nokia.tools.media.utils.timeline.impl.ExecutionThread;
import com.nokia.tools.theme.s60.editing.anim.PolyLineControlPointModel;
import com.nokia.tools.theme.s60.editing.anim.TSCPData;
import com.nokia.tools.theme.s60.effects.EffectObject;
import com.nokia.tools.theme.s60.effects.EffectParameter;
import com.nokia.tools.theme.s60.morphing.AnimationConstants;
import com.nokia.tools.theme.s60.ui.Activator;
import com.nokia.tools.theme.s60.ui.Messages;
import com.nokia.tools.theme.s60.ui.animation.AnimationPropertiesDialog;
import com.nokia.tools.theme.s60.ui.animation.EffectParameterUIContributor;

/**
 * Composite containig animation controls for timeline / graphics editor.
 * Listening on: - control point model - ILayerEffect - TimeLine Created by
 * GraphicsEditorPart
 * 
 */
public class EffectAnimationUI extends Composite implements
		PropertyChangeListener, ITimeListener, IControlPointListener {

	/**
	 * property name for control point model
	 */
	public static final String PROPERTY_CONTROLPOINTS = "ControlPointModel";

	// CPM we are working above
	private PolyLineControlPointModel<TSCPData> controlPointModel;

	// effect object representing selected row
	private ILayerEffect effect;

	// timeline - for timeChange Events
	private ITimeLine timeline;

	private static FormToolkit toolkit;

	private boolean disableListening;

	public EffectAnimationUI(Composite parent, int style, TimingModel m,
			TimeSpan s) {
		super(parent, style);
		this.timing = m;
		this.span = s;
		if (toolkit == null) {
			toolkit = new FormToolkit(Display.getDefault());
		}
	}

	private long currentTime;

	private IControlPoint currentControlPoint;

	/** lookup map for 'set' checkboxes */
	private Map<String, Button> setCheckButtons = new HashMap<String, Button>();

	// map mapping effect parameters to composites with it's controls
	private HashMap<String, Control[]> compositeMap = new HashMap<String, Control[]>();

	private List<IPropertyChangeListener> listeners = new ArrayList<IPropertyChangeListener>();

	private TimeSpan span;

	private TimingModel timing;

	public void init(ILayerEffect ef, ITimeLine timeLine) {
		this.effect = ef;
		this.timeline = timeLine;
		this.controlPointModel = ((EffectObject) effect).getControlPointModel();
		
		timeline.addTimeListener(this);
		// timeLine.addSelectionListener(this);
		controlPointModel.addControlPointListener(this);
		effect.addPropertyListener(this);
		// contribute controls according to effect props
		createDialogControls(this);

		timeChanged(timeLine.getCurrentTime());
	}

	@Override
	public void dispose() {
		super.dispose();
		if (effect != null) {
			effect.removePropertyChangeListener(this);
		}
		if (timeline != null) {
			timeline.removeTimeListener(this);
			// timeline.removeSelectionListener(this);
		}
		if (controlPointModel != null) {
			controlPointModel.removeControlPointListener(this);
		}
		compositeMap.clear();
		setCheckButtons.clear();

		// remove listeners from effect parameters
		List<IEffectParameterDescriptor> attrs = effect
				.getAttributeDescriptors();
		for (IEffectParameterDescriptor desc : attrs) {
			IEffectParameter par = effect.getParameter(desc.getUiName());
			for (Object x : listenersToRemove) {
				par.removePropertyChangeListener((PropertyChangeListener) x);
			}
		}
		listenersToRemove.clear();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		notityPropertyChangedListeners(evt.getPropertyName(),
				evt.getOldValue(), evt.getNewValue());
	}

	public List<IEffectParameterDescriptor> getAnimatedParams() {
		List<IEffectParameterDescriptor> attributes = effect
				.getAttributeDescriptors();
		for (int i = 0; i < attributes.size(); i++) {
			if (!compositeMap.containsKey(attributes.get(i).getUiName()))
				attributes.remove(i--);
		}
		return attributes;
	}

	public void timeChanged(final long newTime) {
		if (isDisposed()) {
			return;
		}

		if (Display.getCurrent() == null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					timeChanged(newTime);
				}
			});
			return;
		}

		currentControlPoint = controlPointModel.findControlPointAt(newTime);
		currentTime = newTime;
		if (currentControlPoint != null) {
			// we are at CP point
			TSCPData data = controlPointModel
					.getControlPointData(controlPointModel.getControlPoints()
							.indexOf(currentControlPoint));
			List<IEffectParameterDescriptor> attributes = effect
					.getAttributeDescriptors();
			for (int i = 0; i < attributes.size(); i++) {
				IEffectParameterDescriptor attribute = attributes.get(i);
				if (!compositeMap.containsKey(attribute.getUiName()))
					continue;
				if (effect.getParameter(attribute.getUiName()).isAnimated()) {
					long value;
					boolean set;
					if (data.isSet(i)) {
						// attribute is defined in this CP
						value = data.getValue(i);
						set = true;
					} else {
						// not defined
						value = (int) controlPointModel.getValue(newTime, i);
						set = false;
					}
					setControlsValue(attribute.getUiName(), value, set);
				}
			}
			updateRowsEnablementState();
		} else {
			List<IEffectParameterDescriptor> attributes = effect
					.getAttributeDescriptors();
			for (int i = 0; i < attributes.size(); i++) {
				IEffectParameterDescriptor attribute = attributes.get(i);
				if (!compositeMap.containsKey(attribute.getUiName()))
					continue;
				if (effect.getParameter(attribute.getUiName()).isAnimated()) {
					int value = (int) controlPointModel.getValue(newTime, i);
					setControlsValue(attribute.getUiName(), value, false);
					updateRowEnablementState(effect.getParameter(attribute
							.getUiName()));
				}
			}
		}
	}

	/**
	 * update value control's values, updates parameter value, and 'set' check
	 * state
	 * 
	 * @param attrName
	 * @param value
	 * @param isset
	 */
	private void setControlsValue(final String attrName, final long value,
			final boolean isset) {

		Control[] ctrl = (Control[]) compositeMap.get(attrName);
		if (ctrl != null) {
			// update value
			if (ctrl[1] instanceof Composite) {
				Composite valComp = (Composite) ctrl[1];
				Control[] child = valComp.getChildren();
				for (int i = 0; i < child.length; i++) {
					if (child[i] instanceof Slider) {
						IEffectParameterDescriptor attribute = (IEffectParameterDescriptor) child[i]
								.getData();
						final int min = new Integer(attribute.getMinVal())
								.intValue();
						((Slider) child[i]).setSelection((int) value
								+ Math.abs(min));
					} else if (child[i] instanceof Text) {
						((Text) child[i])
								.setText(Integer.toString((int) value));
					}
				}
			} else if (ctrl[1] instanceof Text) {
				((Text) ctrl[1]).setText(Integer.toString((int) value));
			}
			// update set check button state
			Button setcheck = setCheckButtons.get(attrName);
			if (setcheck.getSelection() != isset)
				setcheck.setSelection(isset);
		}
	}

	public static final String UI_TYPE_SLIDER = EffectObject.UI_TYPE_SLIDER; //$NON-NLS-1$

	public static final String UI_TYPE_TEXT = EffectObject.UI_TYPE_TEXT; //$NON-NLS-1$

	public static final String UI_TYPE_COMBO = EffectObject.UI_TYPE_COMBO; //$NON-NLS-1$

	private static final int MINIMUM_TEXT_LENGTH = 3;

	/**
	 * Creates dialog controls for layer effect UI properties.
	 */
	public Composite createDialogControls(Composite parent) {
		parent.setLayout(new GridLayout(5, false));
		((GridLayout) parent.getLayout()).marginLeft = 10;
		((GridLayout) parent.getLayout()).marginRight = 10;
		((GridLayout) parent.getLayout()).marginTop = 5;
		((GridLayout) parent.getLayout()).marginBottom = 5;
		((GridLayout) parent.getLayout()).marginHeight = 0;
		((GridLayout) parent.getLayout()).marginWidth = 0;
		((GridLayout) parent.getLayout()).verticalSpacing = 0;
		((GridLayout) parent.getLayout()).horizontalSpacing = 5;
		List<IEffectParameterDescriptor> attributes = effect
				.getAttributeDescriptors();
		for (int i = 0; i < attributes.size(); i++) {
			IEffectParameterDescriptor attribute = attributes.get(i);
			processAttribute(attribute, parent);
		}
		// set proper state according to state of parameters
		updateRowsEnablementState();

		return parent;
	}

	private void updateRowsEnablementState() {
		List<IEffectParameterDescriptor> attributes = getAnimatedParams();
		for (int i = 0; i < attributes.size(); i++) {
			IEffectParameterDescriptor attribute = attributes.get(i);
			// get effect parameter
			IEffectParameter param = effect.getParameter(attribute.getUiName());
			updateRowEnablementState(param);
		}
	}

	private void updateRowEnablementState(IEffectParameter param) {
		Control[] ctrl = (Control[]) compositeMap.get(param.getName());
		if (param.isAnimated()) {
			if (currentControlPoint != null) {
				// set animated and enabled
				if ((param.getTimingModel() == timing && (span == null || span == param
						.getTimeSpan()))) {
					((Button) ctrl[2])
							.setEnabled(currentControlPoint.getTime() > 0); // set
				} else {
					((Button) ctrl[2]).setEnabled(false); // set
				}

				// is this param set at this point?
				TSCPData data = (TSCPData) currentControlPoint.getData();
				int paramPos = effect.getAttributeDescriptors().indexOf(
						effect.getDescriptor().getParameterDescriptor(
								param.getName()));
				boolean isset = data.isSet(paramPos);
				((Control) ctrl[1]).setEnabled(isset);
			} else {
				// set animated and disabled
				((Control) ctrl[1]).setEnabled(false);
				((Button) ctrl[2]).setEnabled(false);
			}
		}
	}

	protected void updateControlsValue(IEffectParameter param) {
		Control[] ctrl = (Control[]) compositeMap.get(param.getName());
		if (ctrl[1] instanceof Composite) {
			Composite valComp = (Composite) ctrl[1];
			Control[] child = valComp.getChildren();
			for (int i = 0; i < child.length; i++) {
				if (child[i] instanceof Slider) {
					((Slider) child[i]).setSelection(Integer.parseInt(param
							.getValue()));
				} else if (child[i] instanceof Text) {
					((Text) child[i]).setText(param.getValue());
				}
			}
		} else if (ctrl[1] instanceof Text) {
			((Text) ctrl[1]).setText(param.getValue());
		}
	}

	/**
	 * Creates SWT control for specified UI property.
	 * 
	 * @param attribute
	 * @param parent
	 */
	private void processAttribute(final IEffectParameterDescriptor attribute,
			Composite parent) {
		String uiType = attribute.getUiType();
		String name = attribute.getUiName();

		Composite med = parent;

		if (UI_TYPE_SLIDER.equalsIgnoreCase(uiType)) {

			createSlider(attribute, med);
			createSetButton(attribute, med);

		} else if (UI_TYPE_TEXT.equalsIgnoreCase(uiType)) {

			createText(attribute, med);
			createSetButton(attribute, med);

		} else if (UI_TYPE_COMBO.equalsIgnoreCase(uiType)) {
			// combo cannot be animated
			return;
		} else {
			throw new UnsupportedOperationException(
					"Unsupported UI Type (effect: " //$NON-NLS-1$
							+ effect.getName() + ", parameter: " //$NON-NLS-1$
							+ attribute.getUiName() + ", type: " //$NON-NLS-1$
							+ attribute.getUiType() + ")"); //$NON-NLS-1$
		}

		// add last four control to composite map - four control represents one
		// parameter
		int c = 4;
		Control ctrl[] = new Control[c];
		Control real[] = parent.getChildren();
		int l = real.length - c; // start of copying
		for (int i = l; i < l + c; i++)
			ctrl[i - l] = real[i];
		compositeMap.put(name, ctrl);

		createAnimateButton(attribute, parent, ctrl);
	}

	private ArrayList<PropertyChangeListener> listenersToRemove = new ArrayList<PropertyChangeListener>();

	private void createAnimateButton(
			final IEffectParameterDescriptor attribute,
			Composite parametersComposite, final Control[] effectControls) {
		Composite cm = new Composite(parametersComposite, SWT.NONE);
		cm.setLayout(new GridLayout(2, false));
		((GridLayout) cm.getLayout()).marginHeight = 0;
		((GridLayout) cm.getLayout()).marginWidth = 0;

		final IEffectParameter param = getEffect().getParameter(
				attribute.getUiName());

		final Button check = new Button(cm, SWT.CHECK);
		check.setText(Messages.effectsControlComposite_animatedCheckText);

		final Button button = new Button(cm, SWT.PUSH);
		final Image image = Activator.getImageDescriptor(
				"icons/animation_timing16x16.png").createImage();
		button.setImage(image);
		button
				.setToolTipText(Messages.effectsControlComposite_animDialogButton_tooltipText);
		button.addDisposeListener(new DisposeListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				image.dispose();
			}

		});

		initAnimateButton(check, button, attribute, effectControls);

		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// show animation dialog
				Shell shell = button.getShell();
				try {
					AnimationPropertiesDialog dlg = null;
					if ((dlg = new AnimationPropertiesDialog(shell,
							getEffect(), param.getName())).open() == Window.OK) {
						((EffectParameter) param).setAnimationData(dlg
								.getResultData(), dlg.getAnimTime());
						
					}
				} catch (Exception ee) {
					ee.printStackTrace();
				}

				// DEBUG - uncomment this to see PresetDialog
				// AnimationPresetDialog dlg = new AnimationPresetDialog(shell,
				// effect, param.getName());
				// if (dlg.open() == Window.OK) {
				// ((EffectParameter)param).setAnimationData(dlg.getResultData(),dlg.getAnimTime());
				// }
			};
		});

		check.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean animated = !param.isAnimated();

				param.setAnimated(animated);

				try {
					disableListening = true;
					if (animated) {

						if (TimingModel.Relative == timing) {

							HashMap<String, String> map = new HashMap<String, String>();
							map.put(AnimationConstants.ATTR_NAME,
									AnimationConstants.RELATIVEMODEL_NAME);
							map.put(AnimationConstants.WRAP, Boolean.FALSE
									.toString());
							map.put(AnimationConstants.TIMESLICE, ((Integer) 1)
									.toString());
							map.put(AnimationConstants.TIMESPAN,
									AnimationConstants.HOURLY);

							if (TimeSpan.EHour == span) {
								map.put(AnimationConstants.TIMESPAN,
										AnimationConstants.HOURLY);
							}
							if (TimeSpan.EDay == span) {
								map.put(AnimationConstants.TIMESPAN,
										AnimationConstants.DAILY);
							}
							if (TimeSpan.EWeek == span) {
								map.put(AnimationConstants.TIMESPAN,
										AnimationConstants.WEEKLY);
							}
							if (TimeSpan.EMonth == span) {
								map.put(AnimationConstants.TIMESPAN,
										AnimationConstants.MONTHLY);
							}

							((EffectParameter) param).setTimingModelMap(map);
							
							if (controlPointModel.getControlPoints().size() < 1) {
								IControlPoint secondCp = controlPointModel
								.createControlPoint(timeline.getEndTime());
								
								IControlPoint firstCp = controlPointModel
										.findControlPointAt(0);

								TSCPData firstCpData = (TSCPData) firstCp
										.getData();
								TSCPData secondCpData = (TSCPData) secondCp
										.getData();
								int paramIndex = effect
										.getAttributeDescriptors().indexOf(
												attribute);

								firstCpData.setValue(paramIndex, (attribute
										.getMinVal() != null && attribute
										.getMinVal().length() > 0) ? Integer
										.parseInt(attribute.getMinVal()) : 0);

								secondCpData.setValue(paramIndex, (attribute
										.getMaxVal() != null && attribute
										.getMaxVal().length() > 0) ? Integer
										.parseInt(attribute.getMaxVal()) : 100);

								notityPropertyChangedListeners(attribute
										.getUiName(), null, null);
							
							}
							

						} else if (TimingModel.RealTime == timing) {
						
							HashMap<String, String> map = new HashMap<String, String>();
							map.put(AnimationConstants.WRAP, Boolean.FALSE
									.toString());
							map.put(AnimationConstants.ATTR_NAME,
									AnimationConstants.REAL_TIMING_MODEL);

							((EffectParameter) param).setTimingModelMap(map);

							if (controlPointModel.getControlPoints().size() < 2) {
							
								IControlPoint secondCp = controlPointModel
										.createControlPoint(1000);

								IControlPoint firstCp = controlPointModel
										.findControlPointAt(0);

								TSCPData firstCpData = (TSCPData) firstCp
										.getData();
								TSCPData secondCpData = (TSCPData) secondCp
										.getData();
								int paramIndex = effect
										.getAttributeDescriptors().indexOf(
												attribute);

								firstCpData.setValue(paramIndex, (attribute
										.getMinVal() != null && attribute
										.getMinVal().length() > 0) ? Integer
										.parseInt(attribute.getMinVal()) : 0);

								secondCpData.setValue(paramIndex, (attribute
										.getMaxVal() != null && attribute
										.getMaxVal().length() > 0) ? Integer
										.parseInt(attribute.getMaxVal()) : 255);

								notityPropertyChangedListeners(attribute
										.getUiName(), null, null);
						
							}
						}
					}
				} finally {
					disableListening = false;
				}
				button.setEnabled(param.isAnimated());
				for (Control control : effectControls) {
					control.setEnabled(!animated);
				}
				
			};
		});
	}

	private void initAnimateButton(final Button check, final Button button,
			final IEffectParameterDescriptor attribute,
			final Control[] effectControls) {

		final IEffectParameter param = getEffect().getParameter(
				attribute.getUiName());

		check.setSelection(param.isAnimated());

		if ((!param.isAnimated())
				|| (param.isAnimated() && param.getTimingModel() == timing && (span == null || span == param
						.getTimeSpan()))) {
			check.setEnabled(true);
			button.setEnabled(param.isAnimated());

			for (Control control : effectControls) {
				control.setEnabled(param.isAnimated());
			}
		} else {
			check.setEnabled(false);
			button.setEnabled(check.getSelection());

			for (Control control : effectControls) {
				control.setEnabled(false);
			}
		}

		final PropertyChangeListener listener = new PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent evt) {
				if (!disableListening) {
					getEffect().removePropertyChangeListener(this);
					initAnimateButton(check, button, attribute, effectControls);
				}
			};
		};

		button.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				getEffect().removePropertyChangeListener(listener);
			}
		});

		getEffect().addPropertyListener(listener);
	}

	private void createSetButton(IEffectParameterDescriptor attribute,
			final Composite parent) {

		final IEffectParameter param = effect.getParameter(attribute
				.getUiName());

		final Button check = new Button(parent, SWT.CHECK);
		check.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (check.getSelection()) {
					// enabled, set param as defined in this CP
					TSCPData data = (TSCPData) currentControlPoint.getData();
					int paramPos = effect.getAttributeDescriptors().indexOf(
							effect.getDescriptor().getParameterDescriptor(
									param.getName()));

					data.setValue(paramPos, data.getValue(paramPos));

					/* also make sure that point at '0' is settted for this attr */
					PolyLineControlPointModel model = EffectAnimationUI.this.controlPointModel;
					if (model.getControlPoint(0) != null) {
						TSCPData zero = (TSCPData) model.getControlPoint(0)
								.getData();
						if (!zero.isSet(paramPos))
							zero.setValue(paramPos, (int) model.getValue(0,
									paramPos));
					}

					setControlsValue(param.getName(), data.getValue(paramPos),
							check.getSelection());
				} else {
					// unset at this point
					TSCPData data = (TSCPData) currentControlPoint.getData();
					int paramPos = effect.getAttributeDescriptors().indexOf(
							effect.getDescriptor().getParameterDescriptor(
									param.getName()));
					data.unset(paramPos);
					// update slider val to interpolated state
					int value = (int) controlPointModel.getValue(currentTime,
							paramPos);
					setControlsValue(param.getName(), value, check
							.getSelection());
					// must unset another time, because setValue also sets mask
					// as set.
					data.unset(paramPos);
				}
				// when activated, enable value control
				updateRowEnablementState(param);
			};

			public void widgetDefaultSelected(SelectionEvent e) {
			};
		});
		check.setText(Messages.effectAnimComposite_setCheckText);
		check.setEnabled(false); // default state
		setCheckButtons.put(attribute.getUiName(), check);

		new Label(parent, SWT.NONE);

		PropertyChangeListener list = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (parent.isDisposed()) {
					IEffectParameter p = (IEffectParameter) evt.getSource();
					p.removePropertyChangeListener(this);
					return;
				}
				if (IEffectParameter.PROPERTY_ANIMATED.equals(evt
						.getPropertyName())) {
					// animation status changed
					// updateControlsEnabledState(param);
				} else if (IEffectParameter.PROPERTY_VALUE_NON_ANIMATED
						.equals(evt.getPropertyName())) {
					if (!param.isAnimated()) {
						// update value according to parameter
						updateControlsValue(param);
					}
				}
			}
		};
		listenersToRemove.add(list);

		param.addPropertyListener(list);
	}

	/**
	 * Creates SWT Slider.
	 * 
	 * @param attribute
	 * @param parent
	 */
	private void createSlider(final IEffectParameterDescriptor attribute,
			Composite parent) {

		Label label = new Label(parent, SWT.NONE);
		label.setText(attribute.getCaption() + ":"); //$NON-NLS-1$

		Composite sliderPanel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		sliderPanel.setLayout(layout);

		// because programatic value set to slider doesn't raise event,
		// for text update:
		final Slider slider = new Slider(sliderPanel, SWT.HORIZONTAL);
		slider.setData(attribute);
		final Text text = toolkit.createText(sliderPanel, "");

		text.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		toolkit.paintBordersFor(sliderPanel);

		final int max = new Integer(attribute.getMaxVal()).intValue();
		final int min = new Integer(attribute.getMinVal()).intValue();

		final int range = max - min;

		slider.setMinimum(0);
		slider.setMaximum(range + slider.getThumb());
		slider.setLayoutData(new GridData(128, SWT.DEFAULT));

		if (attribute.getDefaultVal() != null) {
			int defaultVal = new Integer(effect.getEffectParameter(
					attribute.getUiName()).toString()).intValue();

			slider.setSelection(defaultVal + Math.abs(min));
			text.setText(new Integer(defaultVal).toString());
		}

		int maxChars = Math.max(attribute.getMinVal().length(), attribute
				.getMaxVal().length()) + 1;

		text.setLayoutData(new GridData(text.computeSize(computeTextWidth(text,
				maxChars), SWT.DEFAULT).x, SWT.DEFAULT));

		slider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int newPropertyValue = slider.getSelection() - Math.abs(min);

				String newSliderValue = new Integer(newPropertyValue)
						.toString();
				if (!newSliderValue.equals(text.getText())) {
					text.setText(newSliderValue);
					// update value for this param at this control point
					if (currentControlPoint != null) {
						TSCPData data = (TSCPData) currentControlPoint
								.getData();
						int paramIndex = effect.getAttributeDescriptors()
								.indexOf(attribute);
						data.setValue(paramIndex, newPropertyValue);
						notityPropertyChangedListeners(attribute.getUiName(),
								null, newPropertyValue);
					}
				}
			}
		});

		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String newSliderValue = text.getText();

				if (newSliderValue.length() == 0) {
					return;
				}

				int newSliderIntValue;
				if (newSliderValue.equals("-")) {
					newSliderIntValue = 0;
				} else {
					newSliderIntValue = Integer.parseInt(newSliderValue);

					// range control
					int min = Integer.parseInt(attribute.getMinVal());
					int max = Integer.parseInt(attribute.getMaxVal());
					if (newSliderIntValue < min) {
						text.setText(Integer.toString(min));
						newSliderIntValue = min;
					} else if (newSliderIntValue > max) {
						text.setText(Integer.toString(max));
						newSliderIntValue = max;
					}
				}

				if (newSliderIntValue != slider.getSelection()) {
					slider.setSelection(newSliderIntValue + Math.abs(min));
					if (currentControlPoint != null) {
						TSCPData data = (TSCPData) currentControlPoint
								.getData();
						int paramIndex = effect.getAttributeDescriptors()
								.indexOf(attribute);
						data.setValue(paramIndex, newSliderIntValue);
						notityPropertyChangedListeners(attribute.getUiName(),
								null, newSliderIntValue);
					}
				}
			}
		});

		text.addVerifyListener(EffectParameterUIContributor.SLIDER_TEXT_VERIFY);

		text.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				String newSliderValue = text.getText();
				Integer newSliderIntValue = null;
				if (newSliderValue.equals("-")) {
					newSliderIntValue = 0;
				} else if (newSliderValue.length() == 0) {
					int min = Integer.parseInt(attribute.getMinVal());
					text.setText(attribute.getMinVal());
					newSliderIntValue = min;
				}

				if (newSliderIntValue != null) {
					if (newSliderIntValue.intValue() != slider.getSelection()) {
						slider.setSelection(newSliderIntValue + Math.abs(min));
					}
					if (currentControlPoint != null) {
						TSCPData data = (TSCPData) currentControlPoint
								.getData();
						int paramIndex = effect.getAttributeDescriptors()
								.indexOf(attribute);
						data.setValue(paramIndex, newSliderIntValue);
						notityPropertyChangedListeners(attribute.getUiName(),
								null, newSliderIntValue);
					}
				}
			}
		});
	}

	/**
	 * Creates SWT Text.
	 * 
	 * @param attribute
	 * @param parent
	 */
	private void createText(final IEffectParameterDescriptor attribute,
			Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(attribute.getCaption() + ":"); //$NON-NLS-1$

		Composite textPanel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		textPanel.setLayout(layout);

		final Text text = toolkit.createText(textPanel, "");

		text.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		toolkit.paintBordersFor(textPanel);

		if (attribute.getDefaultVal() != null) {
			text.setText(effect.getEffectParameter(attribute.getUiName())
					.toString());
		}
		int maxChars = Math.max(MINIMUM_TEXT_LENGTH, text.getText().length()) + 1;
		text.setLayoutData(new GridData(text.computeSize(computeTextWidth(text,
				maxChars), SWT.DEFAULT).x, SWT.DEFAULT));

		text.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {

			}

			public void focusLost(FocusEvent e) {

				// to be sure that minimum number is not bigger than
				// maximum

				Text widget = (Text) e.widget;
				String formerNumber = widget.getText();
				int verifiedInteger = Integer.parseInt(formerNumber);

				// if the integer is out of bounds give it the border values
				if ((verifiedInteger < 0)) {
					if ("max".equals(attribute.getUiName())) {
						verifiedInteger = 255;
					} else if ("min".equals(attribute.getUiName())) {
						verifiedInteger = 0;
					} else {
						verifiedInteger = 0;
					}

					if (currentControlPoint != null) {
						TSCPData data = (TSCPData) currentControlPoint
								.getData();
						int paramIndex = effect.getAttributeDescriptors()
								.indexOf(attribute);
						data.setValue(paramIndex, verifiedInteger);
						notityPropertyChangedListeners(attribute.getUiName(),
								null, verifiedInteger);
					}

					text.setText(new Integer(verifiedInteger).toString());

					return;
				} else if ((verifiedInteger > 255)) {
					if ("max".equals(attribute.getUiName())) {
						verifiedInteger = 255;
					} else if ("min".equals(attribute.getUiName())) {
						verifiedInteger = 0;
					} else {
						verifiedInteger = 255;
					}

					if (currentControlPoint != null) {
						TSCPData data = (TSCPData) currentControlPoint
								.getData();
						int paramIndex = effect.getAttributeDescriptors()
								.indexOf(attribute);
						data.setValue(paramIndex, verifiedInteger);
						notityPropertyChangedListeners(attribute.getUiName(),
								null, verifiedInteger);
					}

					text.setText(new Integer(verifiedInteger).toString());
					return;
				}

				if ("max".equals(attribute.getUiName())) {
					int min = Integer.parseInt((String) getEffect()
							.getEffectParameter("min"));
					if (min > verifiedInteger) {
						verifiedInteger = 255;

						if (currentControlPoint != null) {
							TSCPData data = (TSCPData) currentControlPoint
									.getData();
							int paramIndex = effect.getAttributeDescriptors()
									.indexOf(attribute);
							data.setValue(paramIndex, verifiedInteger);
							notityPropertyChangedListeners(attribute
									.getUiName(), null, verifiedInteger);
						}

						text.setText(new Integer(verifiedInteger).toString());
					}
				} else if ("min".equals(attribute.getUiName())) {
					int max = Integer.parseInt((String) getEffect()
							.getEffectParameter("max"));
					if (max < verifiedInteger) {
						verifiedInteger = 0;

						if (currentControlPoint != null) {
							TSCPData data = (TSCPData) currentControlPoint
									.getData();
							int paramIndex = effect.getAttributeDescriptors()
									.indexOf(attribute);
							data.setValue(paramIndex, verifiedInteger);
							notityPropertyChangedListeners(attribute
									.getUiName(), null, verifiedInteger);
						}

						text.setText(new Integer(verifiedInteger).toString());
					}
				}
			}
		});

		// added this verify listener to limit user input (disable
		// entering strings)
		text.addVerifyListener(EffectParameterUIContributor.TEXT_VERIFY);

		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String newTextValue = text.getText();
				if (newTextValue.length() == 0) {
					return;
				}
				int newTextIntValue = Integer.parseInt(newTextValue);
				// range control
				int min = 0;
				int max = 255;
				if (newTextIntValue < min) {
					text.setText(Integer.toString(min));
					newTextIntValue = min;
				} else if (newTextIntValue > max) {
					text.setText(Integer.toString(max));
					newTextIntValue = max;
				}
				if (currentControlPoint != null) {
					TSCPData data = (TSCPData) currentControlPoint.getData();
					int paramIndex = effect.getAttributeDescriptors().indexOf(
							attribute);
					if (data.isSet(paramIndex)) {
						data.setValue(paramIndex, newTextIntValue);
						notityPropertyChangedListeners(attribute.getUiName(),
								null, newTextIntValue);
					}
				}
			}
		});

	}

	/**
	 * Compute the text width.
	 */
	private int computeTextWidth(Drawable drawable, int textLength) {
		GC gc = new GC(drawable);
		try {
			return gc.getFontMetrics().getAverageCharWidth() * textLength;
		} finally {
			gc.dispose();
		}
	}

	public void controlPointCreated(IControlPoint point) {
		notityPropertyChangedListeners(PROPERTY_CONTROLPOINTS, null, null);
	}

	public void controlPointMoved(IControlPoint point) {
		notityPropertyChangedListeners(PROPERTY_CONTROLPOINTS, null, null);
	}

	public void controlPointRemoved(IControlPoint point) {
		notityPropertyChangedListeners(PROPERTY_CONTROLPOINTS, null, null);
	}

	public void controlPointSelected(IControlPoint point) {
		if (point != null)
			timeChanged(point.getTime());
	}

	public void addPropertyChangedListener(IPropertyChangeListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removePropertyChangedListener(IPropertyChangeListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	protected void notityPropertyChangedListeners(final String propertyName,
			final Object oldValue, final Object newValue) {

		final List<IPropertyChangeListener> listeners = new ArrayList<IPropertyChangeListener>(
				this.listeners);

		if (listeners.size() == 0) {
			return;
		}

		if (Thread.currentThread().getName().startsWith(ExecutionThread.NAME)) {
			synchronized (listeners) {
				for (IPropertyChangeListener listener : listeners) {
					listener
							.propertyChange(new org.eclipse.jface.util.PropertyChangeEvent(
									this, propertyName, oldValue, newValue));
				}
			}
		} else {
			ExecutionThread.INSTANCE.execute(new Runnable() {
				public void run() {
					synchronized (listeners) {
						for (IPropertyChangeListener listener : listeners) {
							listener
									.propertyChange(new org.eclipse.jface.util.PropertyChangeEvent(
											this, propertyName, oldValue,
											newValue));
						}
					}
				};
			});
		}
	}

	public ILayerEffect getEffect() {
		return effect;
	}

}
