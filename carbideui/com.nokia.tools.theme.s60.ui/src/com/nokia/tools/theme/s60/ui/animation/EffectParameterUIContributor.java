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

import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.nokia.tools.media.utils.layers.IEffectParameter;
import com.nokia.tools.media.utils.layers.IEffectParameterDescriptor;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.layers.ILayerEffectDialogContributor;
import com.nokia.tools.theme.s60.effects.EffectObject;

/**
 * Creates ui controls for customizing effect parameters based on their metadata
 * 
 */
public class EffectParameterUIContributor implements
		ILayerEffectDialogContributor {

	public static final String UI_TYPE_SLIDER = EffectObject.UI_TYPE_SLIDER; //$NON-NLS-1$

	public static final String UI_TYPE_TEXT = EffectObject.UI_TYPE_TEXT; //$NON-NLS-1$

	public static final String UI_TYPE_COMBO = EffectObject.UI_TYPE_COMBO; //$NON-NLS-1$

	private static final int MINIMUM_TEXT_LENGTH = 3;

	private static FormToolkit toolkit;

	private ILayerEffect layerEffect;

	private boolean disableListening = false;

	public static VerifyListener TEXT_VERIFY = new VerifyListener() {
		public void verifyText(VerifyEvent e) {
			e.doit = true;
			try {
				// adding numeral
				if (e.text != null && e.text.length() > 0) {
					// from now it does not accept negative numbers
					if (e.text.startsWith("-")) {
						e.doit = false;
						return;
					}

					Integer.parseInt(e.text);
				}

			} catch (NumberFormatException nfe) {
				// if (!(e.text.equals("-") && e.start == 0)) {
				// //$NON-NLS-1$
				e.doit = false;
				// }
			}
		}
	};

	public static VerifyListener SLIDER_TEXT_VERIFY = new VerifyListener() {
		public void verifyText(VerifyEvent e) {
			e.doit = true;
			try {
				// adding numeral
				if (e.text != null && e.text.length() > 0) {
					Integer.parseInt(e.text);
				}

			} catch (NumberFormatException nfe) {
				if (!(e.text.equals("-") && e.start == 0)) {
					e.doit = false;
				}
			}
		}
	};

	/**
	 * Constructs new LayerEffectDialogContributor with specified layerEffect.
	 * 
	 * @param layerEffect
	 */
	public EffectParameterUIContributor(ILayerEffect layerEffect) {
		setLayerEffect(layerEffect);

		if (toolkit == null) {
			// share a static toolkit, otherwise need to dispose the toolkit at
			// proper time
			toolkit = new FormToolkit(Display.getDefault());
		}
	}

	/**
	 * Sets dialog contributor layer effect.
	 */
	public void setLayerEffect(ILayerEffect layerEffect) {
		this.layerEffect = layerEffect;
	}

	/**
	 * Creates dialog controls for layer effect UI properties.
	 */
	public Composite createDialogControls(Composite parent) {
		List<IEffectParameterDescriptor> attributes = layerEffect
				.getAttributeDescriptors();

		for (int i = 0; i < attributes.size(); i++) {
			IEffectParameterDescriptor attribute = attributes.get(i);
			processAttribute(attribute, parent);
		}

		return parent;
	}

	public ILayerEffect getLayerEffect() {
		return layerEffect;
	}

	/**
	 * Creates SWT control for specified UI property.
	 * 
	 * @param attribute
	 * @param parametersComposite
	 */
	private void processAttribute(final IEffectParameterDescriptor attribute,
			Composite parametersComposite) {
		String uiType = attribute.getUiType();

		if (UI_TYPE_SLIDER.equalsIgnoreCase(uiType)) {

			createAnimateButton(attribute, parametersComposite, createSlider(
					attribute, parametersComposite));

		} else if (UI_TYPE_TEXT.equalsIgnoreCase(uiType)) {

			createAnimateButton(attribute, parametersComposite, createText(
					attribute, parametersComposite));

		} else if (UI_TYPE_COMBO.equalsIgnoreCase(uiType)) {

			createCombo(attribute, parametersComposite);
			// new Label(parametersComposite, SWT.NONE); // must fill blank grid
			// cell

		} else {
			throw new UnsupportedOperationException(
					"Unsupported UI Type (effect: " //$NON-NLS-1$
							+ getLayerEffect().getName() + ", parameter: " //$NON-NLS-1$
							+ attribute.getUiName() + ", type: " //$NON-NLS-1$
							+ attribute.getUiType() + ")"); //$NON-NLS-1$
		}
	}

	private void createAnimateButton(
			final IEffectParameterDescriptor attribute,
			Composite parametersComposite, final Control[] effectControls) {

		final IEffectParameter param = layerEffect.getParameter(attribute
				.getUiName());

		for (Control control : effectControls) {
			if (!control.isEnabled()) {
				control.setEnabled(true);
			}

			if (param.isAnimated()) {
				final Control ctrl = control;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (!ctrl.isDisposed()) {
							ctrl.setEnabled(!param.isAnimated());
						}
					}
				});
			}
		}

		final PropertyChangeListener listener = new PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent evt) {
				if (!disableListening) {
					IEffectParameter param = layerEffect.getParameter(attribute
							.getUiName());

					for (Control control : effectControls) {
						if (control.isEnabled() == param.isAnimated()) {
							control.setEnabled(!param.isAnimated());
						}
					}
				}
			};
		};

		effectControls[0].addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				layerEffect.removePropertyChangeListener(listener);
			}
		});

		layerEffect.addPropertyListener(listener);

	}

	/**
	 * Creates SWT Slider.
	 * 
	 * @param attribute
	 * @param parametersComposite
	 */
	private Control[] createSlider(final IEffectParameterDescriptor attribute,
			Composite parametersComposite) {
		Label label = new Label(parametersComposite, SWT.NONE);

		Composite sliderPanel = new Composite(parametersComposite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		sliderPanel.setLayout(layout);

		final Slider slider = new Slider(sliderPanel, SWT.HORIZONTAL);
		final Text text = toolkit.createText(sliderPanel, "");

		text.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		toolkit.paintBordersFor(sliderPanel);

		text.setTextLimit(4);
		final int max = new Integer(attribute.getMaxVal()).intValue();
		final int min = new Integer(attribute.getMinVal()).intValue();
		final int range = max - min;

		slider.setMinimum(0);
		slider.setMaximum(range + slider.getThumb());

		slider.setLayoutData(new GridData(128, SWT.DEFAULT));

		int maxChars = Math.max(attribute.getMinVal().length(), attribute
				.getMaxVal().length()) + 1;

		text.setLayoutData(new GridData(text.computeSize(computeTextWidth(text,
				maxChars), SWT.DEFAULT).x, SWT.DEFAULT));

		initializeSlider(label, slider, text, attribute);

		slider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (disableListening) {
					return;
				}

				int newPropertyValue = slider.getSelection() - Math.abs(min);

				String newSliderValue = new Integer(newPropertyValue)
						.toString();

				if (!newSliderValue.equals(text.getText())) {
					text.setText(newSliderValue);
					setEffectParameter(attribute, newSliderValue);
				}
			}
		});

		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (disableListening) {
					return;
				}

				String newSliderValue = text.getText();

				if (newSliderValue.length() == 0) {
					return;
				}

				int newSliderIntValue;
				if (newSliderValue.equals("-")) {
					newSliderValue = "0";
					newSliderIntValue = 0;
				} else {
					newSliderIntValue = Integer.parseInt(newSliderValue);

					// range control
					int min = Integer.parseInt(attribute.getMinVal());
					int max = Integer.parseInt(attribute.getMaxVal());
					if (newSliderIntValue < min) {
						text.setText(attribute.getMinVal());
						newSliderValue = attribute.getMinVal();
					} else if (newSliderIntValue > max) {
						text.setText(attribute.getMaxVal());
						newSliderValue = attribute.getMaxVal();
					}
				}

				if (newSliderIntValue + Math.abs(min) != slider.getSelection()) {
					slider.setSelection(newSliderIntValue + Math.abs(min));
					setEffectParameter(attribute, newSliderValue);
				}
			}
		});

		text.addVerifyListener(SLIDER_TEXT_VERIFY);

		text.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				if (disableListening) {
					return;
				}

				String newSliderValue = text.getText();
				if (newSliderValue.length() == 0) {
					// invalid text field value, restore old from slider
					int min = Integer.parseInt(attribute.getMinVal());
					text.setText("" + (slider.getSelection() - Math.abs(min)));
				} else if (newSliderValue.equals("-")) {
					text.setText("0");
				}
			}
		});

		return new Control[] { slider, text };
	}

	private void initializeSlider(final Label label, final Slider slider,
			final Text text, final IEffectParameterDescriptor attribute) {
		disableListening = true;
		try {
			int min = new Integer(attribute.getMinVal()).intValue();

			label.setText(attribute.getCaption() + ":"); //$NON-NLS-1$

			if (getLayerEffect()
					.getStaticEffectParameter(attribute.getUiName()) != null) {
				int value = new Integer((String) getLayerEffect()
						.getStaticEffectParameter(attribute.getUiName()))
						.intValue();
				slider.setSelection(value + Math.abs(min));
				text.setText(new Integer(value).toString());
			}

			final PropertyChangeListener listener = new PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent evt) {
					if (!disableListening) {
						layerEffect.removePropertyChangeListener(this);
						initializeSlider(label, slider, text, attribute);
					}
				};
			};

			slider.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					layerEffect.removePropertyChangeListener(listener);
				}
			});

			layerEffect.addPropertyListener(listener);
		} finally {
			disableListening = false;
		}
	}

	/**
	 * Creates SWT Text.
	 * 
	 * @param attribute
	 * @param parametersComposite
	 */
	private Control[] createText(final IEffectParameterDescriptor attribute,
			Composite parametersComposite) {
		Label label = new Label(parametersComposite, SWT.NONE);

		Composite textPanel = new Composite(parametersComposite, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		textPanel.setLayout(layout);

		final Text text = toolkit.createText(textPanel, "");

		text.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		toolkit.paintBordersFor(textPanel);

		int maxChars = Math.max(MINIMUM_TEXT_LENGTH, text.getText().length()) + 1;
		text.setLayoutData(new GridData(text.computeSize(computeTextWidth(text,
				maxChars), SWT.DEFAULT).x, SWT.DEFAULT));

		initializeText(label, text, attribute);

		text.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {

			}

			public void focusLost(FocusEvent e) {
				if (disableListening) {
					return;
				}

				// to be sure that minimum number is not bigger than
				// maximum

				Text widget = (Text) e.widget;
				String formerNumber = widget.getText();
				int verifiedInteger;
				try {
					verifiedInteger = Integer.parseInt(formerNumber);
				} catch (NumberFormatException nfe) {
					// invalid value, restore old from parameter
					widget.setText(layerEffect.getStaticEffectParameter(
							attribute.getUiName()).toString());
					return;
				}

				// if the integer is out of bounds give it the border values
				if ((verifiedInteger < 0)) {
					if ("max".equals(attribute.getUiName())) {
						verifiedInteger = 255;
					} else if ("min".equals(attribute.getUiName())) {
						verifiedInteger = 0;
					} else {
						verifiedInteger = 0;
					}
					setEffectParameter(attribute, new Integer(verifiedInteger)
							.toString());
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

					setEffectParameter(attribute, new Integer(verifiedInteger)
							.toString());
					text.setText(new Integer(verifiedInteger).toString());
					return;
				}

				if ("max".equals(attribute.getUiName())) {
					int min = Integer.parseInt((String) getLayerEffect()
							.getEffectParameter("min"));
					if (min > verifiedInteger) {
						verifiedInteger = 255;
						setEffectParameter(attribute, new Integer(
								verifiedInteger).toString());
						text.setText(new Integer(verifiedInteger).toString());
					}
				} else if ("min".equals(attribute.getUiName())) {
					int max = Integer.parseInt((String) getLayerEffect()
							.getEffectParameter("max"));
					if (max < verifiedInteger) {
						verifiedInteger = 0;
						setEffectParameter(attribute, new Integer(
								verifiedInteger).toString());
						text.setText(new Integer(verifiedInteger).toString());
					}
				}
			}
		});

		// added this verify listener to limit user input (disable
		// entering strings)
		text.addVerifyListener(TEXT_VERIFY);

		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (disableListening) {
					return;
				}

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
					newTextValue = Integer.toString(min);
				} else if (newTextIntValue > max) {
					text.setText(Integer.toString(max));
					newTextValue = Integer.toString(max);
				}

				setEffectParameter(attribute, newTextValue);

			}
		});

		return new Control[] { text };
	}

	private void initializeText(final Label label, final Text text,
			final IEffectParameterDescriptor attribute) {
		disableListening = true;
		try {
			label.setText(attribute.getCaption() + ":"); //$NON-NLS-1$

			if (getLayerEffect()
					.getStaticEffectParameter(attribute.getUiName()) != null) {
				text.setText(layerEffect.getStaticEffectParameter(
						attribute.getUiName()).toString());
			}

			final PropertyChangeListener listener = new PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent evt) {
					if (!disableListening) {
						layerEffect.removePropertyChangeListener(this);
						initializeText(label, text, attribute);
					}
				};
			};

			text.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					layerEffect.removePropertyChangeListener(listener);
				}
			});

			layerEffect.addPropertyListener(listener);
		} finally {
			disableListening = false;
		}
	}

	/**
	 * Creates SWT Combo.
	 * 
	 * @param attribute
	 * @param parametersComposite
	 */
	private Control[] createCombo(final IEffectParameterDescriptor attribute,
			Composite parametersComposite) {
		Label label = new Label(parametersComposite, SWT.NONE);

		Composite comboPanel = new Composite(parametersComposite, SWT.NONE);
		Layout layout = new RowLayout();
		comboPanel.setLayout(layout);

		final CCombo combo = new CCombo(comboPanel, SWT.FLAT);

		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		toolkit.paintBordersFor(comboPanel);

		initializeCombo(label, combo, attribute);

		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (disableListening) {
					return;
				}

				int selection = combo.getSelectionIndex();
				setEffectParameter(attribute, new Integer(selection).toString());
			}
		});

		return new Control[] { combo };
	}

	private void initializeCombo(final Label label, final CCombo combo,
			final IEffectParameterDescriptor attribute) {
		disableListening = true;
		try {
			label.setText(attribute.getCaption() + ":"); //$NON-NLS-1$

			String[] options = attribute.getOptions();
			combo.setItems(options);
			combo.select(getComboValueIndex(attribute));

			final PropertyChangeListener listener = new PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent evt) {
					if (!disableListening) {
						layerEffect.removePropertyChangeListener(this);
						initializeCombo(label, combo, attribute);
					}
				};
			};

			combo.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					layerEffect.removePropertyChangeListener(listener);
				}
			});

			layerEffect.addPropertyListener(listener);
		} finally {
			disableListening = false;
		}
	}

	private int getComboValueIndex(IEffectParameterDescriptor attribute) {
		String textValue = layerEffect
				.getEffectParameter(attribute.getUiName()).toString();

		int num = -1;
		try {
			num = attribute.getLiteralValueNumber(textValue);
		} catch (IllegalArgumentException e) {
			num = new Integer(textValue);
		}
		return num;
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

	private void setEffectParameter(IEffectParameterDescriptor attribute,
			Object value) {
		try {
			disableListening = true;
			getLayerEffect().setEffectParameter(attribute.getUiName(), value);
		} finally {
			disableListening = false;
		}
	}
}
