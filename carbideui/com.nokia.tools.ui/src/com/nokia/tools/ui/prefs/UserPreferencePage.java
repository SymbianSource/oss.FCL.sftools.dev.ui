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
package com.nokia.tools.ui.prefs;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.nokia.tools.ui.Activator;

public class UserPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private String userData = "";
	private String realuserData = "";

	private static Label createLabel(final Composite parent, final String name) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(name);
		label.setLayoutData(new GridData());
		return label;
	}

	IPropertyChangeListener preferenceListener = new IPropertyChangeListener() {

		public void propertyChange(final PropertyChangeEvent event) {
			
		}

	};

	private Text realName;

	private Text user;

	private Composite createComposite(final Composite parent,
			final int numColumns) {
		final Composite composite = new Composite(parent, SWT.NONE);
		// GridLayout
		final GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		layout.numColumns = numColumns;
		composite.setLayout(layout);
		// GridData
		final GridData data = new GridData(GridData.FILL);
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		return composite;
	}

	@Override
	protected Control createContents(final Composite parent) {
		final Composite composite = createScrolledComposite(parent);
		UserPreferencePage.createLabel(composite, "&User:");

		createUserText(composite);
		UserPreferencePage.createLabel(composite, "&Real name:");
		createFirstNameText(composite);

		initializeValues();

		return composite;
	}

	private void createFirstNameText(final Composite composite) {
		realName = new Text(composite, SWT.BORDER);
		realName.setEditable(true);
		realName.setTextLimit(100);
		final GridData gridData = new GridData();
		gridData.widthHint = 100;
		realName.setLayoutData(gridData);

		realName.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {

				realuserData = realName.getText();
				pageChanged();
			}
		});

		realName.addVerifyListener(new VerifyListener() {
			public void verifyText(final VerifyEvent e) {
				final String string = e.text;
				final char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {

					if (Character.isSpaceChar(chars[i])) {
						e.doit = true;
						return;
					}

					if (!Character.isLetterOrDigit(chars[i])) {
						e.doit = false;
						return;
					}
				}
			}
		});
	}

	private Composite createScrolledComposite(final Composite parent) {
		final ScrolledComposite sc1 = new ScrolledComposite(parent,
				SWT.H_SCROLL | SWT.V_SCROLL);
		sc1.setLayoutData(new GridData(GridData.FILL_BOTH));
		final Composite composite = createComposite(sc1, 2);
		sc1.setContent(composite);
		setSize(composite);
		return composite;
	}

	private void createUserText(final Composite composite) {

		user = new Text(composite, SWT.BORDER);
		user.setEditable(true);
		user.setTextLimit(100);
		final GridData gridData = new GridData();
		gridData.widthHint = 100;
		user.setLayoutData(gridData);

		user.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {

				userData = user.getText();
				pageChanged();
			}
		});

		user.addVerifyListener(new VerifyListener() {
			public void verifyText(final VerifyEvent e) {
				final String string = e.text;
				final char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!Character.isLetterOrDigit(chars[i])) {
						e.doit = false;
						return;
					}
				}
			}
		});
	}

	/**
	 * Added the method to display the error message when the user related data
	 * field is empty.
	 */

	protected void pageChanged() {

		if ((userData.length() == 0) || (userData.trim().length() == 0)) {

			updateStatus(Messages.UserPreferencePage_UserDataNotExist);
			return;
		}
		if ((realuserData.length() == 0) || (realuserData.trim().length() == 0)) {

			updateStatus(Messages.UserPreferencePage_RealUserDataNotExist);
			return;
		}

		updateStatus(null);
	}

	/**
	 * Updates error messages
	 */
	private void updateStatus(final String message) {
		setErrorMessage(message);
		setValid(message == null);
	}

	@Override
	public void dispose() {
		super.dispose();
		Activator.getDefault().getPreferenceStore()
				.removePropertyChangeListener(preferenceListener);
		if ((user != null) && !user.isDisposed()) {
			user.dispose();
		}
		if ((realName != null) && !realName.isDisposed()) {
			realName.dispose();
		}

	}

	public String getDefaultValue(final String id) {
		final IPreferenceStore store = Activator.getDefault()
				.getPreferenceStore();
		return store.getDefaultString(id);
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	public void init(final IWorkbench arg0) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		getPreferenceStore().addPropertyChangeListener(preferenceListener);
	}

	private void initializeDefaultValues() {
		user.setText(getDefaultValue(UIPreferences.PREF_USER_LOGIN));
		realName.setText(getDefaultValue(UIPreferences.PREF_USER_NAME));

	}

	private void initializeValues() {
		user.setText(UIPreferencesUtil
				.getStringValue(UIPreferences.PREF_USER_LOGIN));
		realName.setText(UIPreferencesUtil
				.getStringValue(UIPreferences.PREF_USER_NAME));

	}

	@Override
	protected void performApply() {
		
		super.performApply();
		updateUserInfo();
		Activator.getDefault().savePluginPreferences();

	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		initializeDefaultValues();
	}

	@Override
	public boolean performOk() {
		updateUserInfo();
		Activator.getDefault().savePluginPreferences();
		return true;
	}

	public void setDataToPreferenceStore(final String id, final String value) {
		final IPreferenceStore store = Activator.getDefault()
				.getPreferenceStore();
		store.setValue(id, value);
	}

	private void setSize(final Composite composite) {
		if (composite != null) {
			applyDialogFont(composite);
			final Point minSize = composite.computeSize(SWT.DEFAULT,
					SWT.DEFAULT);
			composite.setSize(minSize);
			if (composite.getParent() instanceof ScrolledComposite) {
				final ScrolledComposite sc1 = (ScrolledComposite) composite
						.getParent();
				sc1.setMinSize(minSize);
				sc1.setExpandHorizontal(true);
				sc1.setExpandVertical(true);
			}
		}
	}

	private void setValue(final Text text, final String id) {
		String txt = text.getText();
		if (txt == null) {
			txt = UIPreferencesUtil.getStringValue(id);
		}
		setDataToPreferenceStore(id, txt.trim());
	}

	private void updateUserInfo() {
		setValue(user, UIPreferences.PREF_USER_LOGIN);
		setValue(realName, UIPreferences.PREF_USER_NAME);

	}
}
