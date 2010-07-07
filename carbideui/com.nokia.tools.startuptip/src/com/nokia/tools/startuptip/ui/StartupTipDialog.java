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

package com.nokia.tools.startuptip.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.nokia.tools.startuptip.Settings;
import com.nokia.tools.startuptip.TipSelector;
import com.nokia.tools.startuptip.branding.BrandingUtil;
import com.nokia.tools.startuptip.preferences.StartupTipPreferences;

/**
 * The specialized startup tip dialog which shows the tips on startup.
 * 
 */
public class StartupTipDialog extends TrayDialog {

	private static final int PREVIOUS_ID = -1;
	private static final String PREVIOUS_LABEL = Messages.STARTUP_TIP_DIALOG_NEXT_LABEL;
	private Browser tipBrowser;
	private Button showSTipsOnStartup;
	private TipSelector tipSelector;
	private Image titlebarImage;

	/**
	 * Creates the instance of <code>StartupTipDialog</code>. Invoke
	 * <code>open()</code> method to display the dialog.
	 * 
	 * @param parent
	 */
	public StartupTipDialog(Shell parent) {
		super(parent);
		tipBrowser = new Browser(parent, SWT.NONE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite area = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.marginBottom = 0;
		layout.marginHeight = 0;
		area.setLayout(layout);
		
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		setHelpAvailable(false);

		layout = new GridLayout();
		container.setLayout(layout);

		tipBrowser = new Browser(container, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		tipBrowser.setLayoutData(gd);
		int width = Integer.parseInt(Settings.STARTUP_TIP_DIALOG_WIDTH);
		int height = Integer.parseInt(Settings.STARTUP_TIP_DIALOG_HEIGHT);
		tipBrowser.setSize(width, height);

		tipSelector = new TipSelector();
		tipBrowser.setUrl(tipSelector.getNextTip());

		showSTipsOnStartup = new Button(container, SWT.CHECK);
		showSTipsOnStartup.setText(Messages.SHOW_TIP_ON_STARTUP);
		showSTipsOnStartup.setEnabled(true);
		showSTipsOnStartup.setSelection(StartupTipPreferences.getInstance().showTipOnStartup());

		showSTipsOnStartup.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				StartupTipPreferences.getInstance().setShowTipOnStartup(
						((Button) e.getSource()).getSelection());

			}
		});
		gd = new GridData();
		//gd.
		showSTipsOnStartup.setLayoutData(gd);

		Label separator = new Label(area, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		getShell().setText(Messages.STARTUP_TIP_DIALOG_TITLE);
		getShell().setImage(
				titlebarImage = BrandingUtil.getDialogTitleBarImage());
		return area;

	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId) {
			okPressed();
		} else if (IDialogConstants.NEXT_ID == buttonId) {
			nextPressed();
		} else if (StartupTipDialog.PREVIOUS_ID == buttonId) {
			previousPressed();
		}
	}

	private void previousPressed() {
		tipBrowser.setUrl(tipSelector.getPreviousTip());
		if (!tipSelector.previousAvailable()) {
			previousButton.setEnabled(false);
		}

	}

	protected void nextPressed() {
		tipBrowser.setUrl(tipSelector.getNextTip());
		previousButton.setEnabled(true);

	}

	private Button previousButton;

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		previousButton = createButton(parent, StartupTipDialog.PREVIOUS_ID,
				StartupTipDialog.PREVIOUS_LABEL, false);
		if (!tipSelector.previousAvailable()) {
			previousButton.setEnabled(false);
		}
		createButton(parent, IDialogConstants.NEXT_ID,
				IDialogConstants.NEXT_LABEL, false);
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}

	@Override
	public boolean close() {
		Browser.clearSessions();
		tipBrowser = null;
		if (titlebarImage != null) {
			titlebarImage.dispose();
		}
		return super.close();
	}
}
