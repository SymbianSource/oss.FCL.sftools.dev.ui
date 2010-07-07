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
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import com.nokia.tools.theme.s60.morphing.AnimationConstants;
import com.nokia.tools.theme.s60.ui.Messages;

public class RelativeTimingModelUI implements ITimingModelUI {

	public static final String HOURLY = "Hourly";

	public static final String WEEKLY = "Weekly";

	public static final String DAILY = "Daily";

	public static final String MONTHLY = "Monthly";

	public static final String PERIODICITY[] = { HOURLY, DAILY, WEEKLY, MONTHLY };

	Combo periodicity;

	private Spinner timeSlice;

	private String labelTimeSlice, labelWrapCheckBoxButton;

	private Button wrapCheckBoxButton;

	public RelativeTimingModelUI() {
		labelTimeSlice = Messages.RelativeTimeUI_labelTimeSlice;
		labelWrapCheckBoxButton = Messages.RelativeTimeUI_labelWrapCheckBoxButton;		
	}

	public void createUI(Composite parent) {
		Composite buttons = new Composite(parent, SWT.NONE);
		createButtons(buttons);
	}

	private void createButtons(Composite buttons) {
		GridLayout gl = new GridLayout(3, false);
		gl.marginWidth = 9;
		gl.marginHeight = 8;
		gl.horizontalSpacing = 7;
		gl.verticalSpacing = 7;
		buttons.setLayout(gl);

		// first line
		new Label(buttons, SWT.NONE)
				.setText(Messages.RelativeTimeUI_labelRepetition);
		periodicity = new Combo(buttons, SWT.SINGLE | SWT.READ_ONLY);
		periodicity.setItems(PERIODICITY);
		periodicity.select(0);

		GridData g = new GridData();
		g.horizontalSpan = 2;
		g.widthHint = 60;
		periodicity.setLayoutData(g);

		// second line
		new Label(buttons, SWT.NONE).setText(labelTimeSlice);
		timeSlice = new Spinner(buttons, SWT.BORDER);
		timeSlice.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				wrapCheckBoxButton.forceFocus();
				e.doit = false;
			}
		});
		timeSlice.setSelection(1);
		timeSlice.setMinimum(1);
		timeSlice.setMaximum(999);
		g = new GridData();
		g.widthHint = 60;
		timeSlice.setLayoutData(g);

		wrapCheckBoxButton = new Button(buttons, SWT.CHECK);
		wrapCheckBoxButton.setText(labelWrapCheckBoxButton);
		wrapCheckBoxButton.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				timeSlice.forceFocus();
				e.doit = false;
			}
		});

	}

	public void setParameters(HashMap params) {
		String wrap = (String) params.get(AnimationConstants.WRAP);
		if (wrap != null)
			wrapCheckBoxButton.setSelection(Boolean.parseBoolean(wrap));
		String slices = (String) params.get(AnimationConstants.TIMESLICE);
		if (isNumber(slices))
			timeSlice.setSelection(Integer.parseInt(slices));
		String span = (String) params.get(AnimationConstants.TIMESPAN);
		if (isNumber(span)) {
			setPeriodicityValue(PERIODICITY[Integer.parseInt(span)]);
		}
	}

	private void setPeriodicityValue(String string) {

		for (int i = 0; i < PERIODICITY.length; i++) {
			if (PERIODICITY[i].equals(string))
				periodicity.select(i);
		}
	}

	public HashMap<String, String> getParameters() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AnimationConstants.ATTR_NAME,
				AnimationConstants.RELATIVEMODEL_NAME);
		map.put(AnimationConstants.WRAP, Boolean.toString(wrapCheckBoxButton
				.getSelection()));

		String slices = Integer.toString(timeSlice.getSelection());

		map.put(AnimationConstants.TIMESLICE, slices);
		String periodicity = getPeriodicity();
		if (periodicity != null)
			map
					.put(
							AnimationConstants.TIMESPAN,
							HOURLY.equals(periodicity) ? AnimationConstants.HOURLY
									: (DAILY.equals(periodicity) ? AnimationConstants.DAILY
											: (WEEKLY.equals(periodicity) ? AnimationConstants.WEEKLY
													: AnimationConstants.MONTHLY)));
		return map;
	}

	private String getPeriodicity() {
		return PERIODICITY[periodicity.getSelectionIndex()];
	}

	private boolean isNumber(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// public static void main(String[] args) {
	// Display display = new Display();
	// Shell shell = new Shell(display);
	// shell.setLayout(new FillLayout());
	// RelativeTimingModelUI ui = new RelativeTimingModelUI();
	// ui.createUI(shell);
	// shell.pack();
	// shell.open();
	// while (!shell.isDisposed()) {
	// if (!display.readAndDispatch())
	// display.sleep();
	// }
	// display.dispose();
	//
	// }
}
