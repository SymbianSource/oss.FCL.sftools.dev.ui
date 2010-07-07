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
package com.nokia.tools.theme.s60.ui.animation.timing;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import com.nokia.tools.theme.s60.morphing.AnimationConstants;
import com.nokia.tools.theme.s60.ui.Messages;

/*
 * RealTime UI implemenation for use in dialog
 */
public class RealTimingModelUI implements ITimingModelUI, SelectionListener {

	private Spinner duration, maxDuration, repeats;

	private String labelDuration, labelMaxDuration, labelRepeats,
			labelWrapCheckBoxButton;

	private Button wrapCheckBoxButton;
	
	private Button repeatsUnlimited, maxDurUnlimited;

	public RealTimingModelUI() {
		labelDuration = Messages.RealTimeUI_labelDuration;
		labelMaxDuration = Messages.RealTimeUI_labelMaxDuration;
		labelRepeats = Messages.RealTimeUI_labelRepeats;
		labelWrapCheckBoxButton = Messages.RealTimeUI_labelWrapCheckBoxButton;
	}

	public void createUI(Composite parent) {
		Composite buttons = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(3, false);
		gl.marginWidth = 9;
		gl.marginHeight = 9;
		gl.horizontalSpacing = 7;
		gl.verticalSpacing = 7;
		buttons.setLayout(gl);
		createButtons(buttons);
	}

	private void createButtons(Composite buttons) {
		// first line
		Label lbl = new Label(buttons, SWT.NONE);
		lbl.setText(labelDuration);
		duration = new Spinner(buttons, SWT.BORDER);
		duration.setEnabled(false);
		duration.setMaximum(Integer.MAX_VALUE);
		duration.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				maxDuration.forceFocus();
				e.doit = false;
			}
		});
		lbl.setToolTipText(Messages.RealTimingModelUI_hint);
		
		//wrap
		wrapCheckBoxButton = new Button(buttons, SWT.CHECK);
		wrapCheckBoxButton.setText(labelWrapCheckBoxButton);
		wrapCheckBoxButton.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				duration.forceFocus();
				e.doit = false;
			}
		});

		//max dur
		new Label(buttons, SWT.NONE).setText(labelMaxDuration);
		maxDuration = new Spinner(buttons, SWT.BORDER);
		maxDuration.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				repeats.forceFocus();
				e.doit = false;
			}
		});
		maxDuration.setMinimum(0);
		maxDuration.setMaximum(999999);
		
		maxDurUnlimited = new Button(buttons, SWT.CHECK);
		maxDurUnlimited.setText(Messages.RealTimingModelUI_unlim_check);
		maxDurUnlimited.addSelectionListener(this);

		//repeats
		new Label(buttons, SWT.NONE).setText(labelRepeats);
		repeats = new Spinner(buttons, SWT.BORDER);
		repeats.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				wrapCheckBoxButton.forceFocus();
				e.doit = false;
			}
		});
		repeats.setMinimum(0);
		repeats.setMaximum(999999);
		
		repeatsUnlimited = new Button(buttons, SWT.CHECK);
		repeatsUnlimited.setText(Messages.RealTimingModelUI_unlim_check);
		repeatsUnlimited.addSelectionListener(this);

		//set same size
		setStdSize(duration);
		setStdSize(repeats);
		setStdSize(maxDuration);
	}
	
	private void setStdSize(Control widget) {
		GridData g = new GridData();
		g.widthHint = 50;
		widget.setLayoutData(g);
	}

	public void setParameters(HashMap params) {
		
		maxDuration.setSelection(0);		
		repeats.setSelection(0);
		
		String durationStr = (String) params.get(AnimationConstants.DURATION);
		try {
			int v = Integer.parseInt(durationStr);
			duration.setSelection(v);
		} catch (Exception e) {			
		}

		if (params.get(AnimationConstants.REPEAT_COUNT) != null) {
			try {
				int v = Integer.parseInt((String) params
						.get(AnimationConstants.REPEAT_COUNT));
				repeatsUnlimited.setSelection(v < 0);				
				repeats.setSelection(v);
				repeats.setEnabled(!repeatsUnlimited.getSelection());
			} catch (Exception e) {				
			}			
		}
		if (params.get(AnimationConstants.REPEAT_DURATION) != null) {
			try {
				int v = Integer.parseInt((String) params
						.get(AnimationConstants.REPEAT_DURATION));
				maxDurUnlimited.setSelection(v < 0);
				maxDuration.setSelection(v);
				maxDuration.setEnabled(!maxDurUnlimited.getSelection());
			} catch (Exception e) {				
			}
		} 
		if (params.get(AnimationConstants.WRAP) != null) {
			wrapCheckBoxButton
					.setSelection(Boolean.parseBoolean((String) params
							.get(AnimationConstants.WRAP)));
		}		
	}

	public HashMap<String, String> getParameters() {
		String maxDur = Integer.toString(maxDuration.getSelection());
		String repeats = Integer.toString(this.repeats.getSelection());
		boolean wrap = wrapCheckBoxButton.getSelection();
		HashMap<String, String> map = new HashMap<String, String>();
		if (isNumber(maxDur) || "".equals(maxDur)) { //$NON-NLS-1$
			map.put(AnimationConstants.REPEAT_DURATION, maxDur);
		}
		if (isNumber(repeats) || "".equals(repeats)) { //$NON-NLS-1$
			map.put(AnimationConstants.REPEAT_COUNT, repeats);
		}
		map.put(AnimationConstants.WRAP, Boolean.toString(wrap));
		map.put(AnimationConstants.ATTR_NAME,
				AnimationConstants.REAL_TIMING_MODEL);
		map.put(AnimationConstants.DURATION, Integer.toString(duration.getSelection()));
		
		if (maxDurUnlimited.getSelection())
			map.put(AnimationConstants.REPEAT_DURATION, AnimationConstants.REPEAT_DURATION_UNLIMITED); //$NON-NLS-1$
		if (repeatsUnlimited.getSelection())
			map.put(AnimationConstants.REPEAT_COUNT, AnimationConstants.REPEAT_COUNT_UNLIMITED); //$NON-NLS-1$
		
		return map;
	}

	private boolean isNumber(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) {		
	}

	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == repeatsUnlimited) {
			if (repeatsUnlimited.getSelection()) {
				repeats.setEnabled(false);
			} else {
				repeats.setEnabled(true);
			}
		}
		if (e.getSource() == maxDurUnlimited) {
			if (maxDurUnlimited.getSelection()) {
				maxDuration.setEnabled(false);
			} else {
				maxDuration.setEnabled(true);
			}
		}
	}

}
