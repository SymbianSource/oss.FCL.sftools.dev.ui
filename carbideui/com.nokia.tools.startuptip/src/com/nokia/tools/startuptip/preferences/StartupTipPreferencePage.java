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

package com.nokia.tools.startuptip.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.startuptip.ui.Messages;

/**
 * Preferences page for Startup Tips.
 * 
 */
public class StartupTipPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private Button showTipOnStartup;
	
	public static final String PREFERENCES_CONTEXT = "com.nokia.tools.startuptip" + '.' + "startup_tip_preferences_context";

	public StartupTipPreferencePage() {
	}

	public StartupTipPreferencePage(String title) {
		super(title);
	}

	public StartupTipPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				PREFERENCES_CONTEXT);
		Composite generalComposite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		generalComposite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		generalComposite.setLayoutData(gd);

		setDescription(Messages.STARTUP_TIP_PREFERENCE_PAGE_DESCRIPTION);
		createDescriptionLabel(generalComposite);

		Group stPrefGroup = new Group(generalComposite, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalIndent = 5;
		gd.widthHint = 300;
		stPrefGroup.setLayoutData(gd);

		layout = new GridLayout();
		layout.numColumns = 3;
		stPrefGroup.setLayout(layout);

		showTipOnStartup = new Button(stPrefGroup, SWT.CHECK);
		showTipOnStartup.setText(Messages.SHOW_TIP_ON_STARTUP);
		showTipOnStartup.setSelection(StartupTipPreferences.getInstance()
				.showTipOnStartup());
		gd = new GridData(GridData.FILL);
		gd.horizontalSpan = 3;
		showTipOnStartup.setLayoutData(gd);

		return parent;
	}

	public void init(IWorkbench workbench) {
		
	}

	@Override
	protected void performDefaults() {
		initializeDefault();
	}

	private void initializeDefault() {
		showTipOnStartup.setSelection(true);

	}

	@Override
	public boolean performOk() {
		StartupTipPreferences.getInstance().setShowTipOnStartup(
				showTipOnStartup.getSelection());
		return true;
	}

}
