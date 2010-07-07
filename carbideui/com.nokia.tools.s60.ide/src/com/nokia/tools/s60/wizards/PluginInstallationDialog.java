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
package com.nokia.tools.s60.wizards;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import com.nokia.tools.platform.extension.PluginEntry;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.ui.branding.util.BrandedTitleAreaDialog;
import com.nokia.tools.ui.dialog.MessageDialogWithTextContent;

public class PluginInstallationDialog extends BrandedTitleAreaDialog implements
		Listener {
	private static final String[] EXTENSIONS = { "*.zip; *.jar" };

	private Text fileText;

	private PluginContentViewer contentViewer;

	private PluginEntry entry;

	public PluginInstallationDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(WizardMessages.Plugin_Installation_Banner_Title);
		setMessage(WizardMessages.Plugin_Installation_Banner_Message);

		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 13;
		layout.marginWidth = 13;
		layout.verticalSpacing = 7;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.NONE);
		label.setText(WizardMessages.Plugin_Installation_File_Text);

		fileText = new Text(composite, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 314;
		fileText.setLayoutData(data);
		fileText.addListener(SWT.Modify, this);

		Button browse = new Button(composite, SWT.PUSH);
		initializeDialogUnits(browse);
		setButtonLayoutData(browse);
		browse.setText(WizardMessages.Plugin_Installation_Browse_Text);
		browse.addSelectionListener(new SelectionAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setFilterExtensions(EXTENSIONS);
				String fileName = dialog.open();
				if (fileName != null) {
					fileText.setText(fileName);
				}
			}
		});

		Composite pluginComposite = new Composite(composite, SWT.NONE);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 3;
		data.minimumHeight = 28;
		data.heightHint = 306;
		pluginComposite.setLayoutData(data);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		pluginComposite.setLayout(layout);
		contentViewer = new PluginContentViewer(pluginComposite);

		Composite sepComposite = new Composite(area, SWT.NONE);
		sepComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout3 = new GridLayout();
		sepComposite.setLayout(layout3);
		layout3.numColumns = 1;
		layout3.marginHeight = 0;
		layout3.marginWidth = 0;
		layout3.verticalSpacing = 0;

		final Label separator = new Label(sepComposite, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		separator.setLayoutData(data);

		Dialog.applyDialogFont(composite);

		return composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.wizards.BrandedTitleAreaDialog#getBannerIconDescriptor()
	 */
	@Override
	protected ImageDescriptor getBannerIconDescriptor() {
		return S60WorkspacePlugin
				.getImageDescriptor("icons/wizban/install_plugin.png");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.wizards.BrandedTitleAreaDialog#getTitle()
	 */
	@Override
	protected String getTitle() {
		return WizardMessages.Plugin_Installation_Title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				WizardMessages.Plugin_Installation_Install_Button, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		getButton(IDialogConstants.OK_ID).setEnabled(false);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		try {
			new ProgressMonitorDialog(getShell()).run(true, false,
					new InstallPluginOperation(new File(fileText.getText()
							.trim())));
			super.okPressed();
		} catch (Exception e) {
			S60WorkspacePlugin.error(e);
			MessageDialogWithTextContent.openError(getShell(),
					WizardMessages.Plugin_Installation_Install_Error_Title,
					WizardMessages.Plugin_Installation_Install_Error_Message,
					StringUtils.dumpThrowable(e));
		}
	}

	public Bundle getBundle() {
		return entry == null ? null : Platform.getBundle(entry
				.getSymbolicName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		String errorMessage = null;
		try {
			String fileName = fileText.getText().trim();
			File file = new File(fileName);
			if (!FileUtils.isFileValidAndAccessible(file)) {
				errorMessage = WizardMessages.Plugin_Installation_File_Exist_Error;
				return;
			}
			if (!fileName.toLowerCase().endsWith(".zip")
					&& !fileName.toLowerCase().endsWith(".jar")) {
				errorMessage = WizardMessages.Plugin_Installation_File_Name_Error;
				return;
			}

			try {
				entry = new PluginEntry(file);
			} catch (Exception e) {
				errorMessage = WizardMessages.Plugin_Installation_File_Read_Error;
				return;
			}

			if (!entry.isValid()) {
				errorMessage = WizardMessages.Plugin_Installation_File_Error;
				return;
			}

			Bundle[] bundles = Platform.getBundles(entry.getSymbolicName(),
					entry.getVersion());
			if (bundles != null && bundles.length > 0) {
				errorMessage = MessageFormat.format(
						WizardMessages.Plugin_Installation_Plugin_Exist_Error,
						new Object[] {
								bundles[0].getSymbolicName(),
								bundles[0].getHeaders().get(
										Constants.BUNDLE_VERSION) });
			}
		} finally {
			contentViewer.setPlugin(entry);

			if (contentViewer.getContentProvider().hasConfigurationError()) {
				errorMessage = WizardMessages.Plugin_Installation_Configuration_Error;
			}

			setErrorMessage(errorMessage);

			if (errorMessage != null) {
				entry = null;
				if (!errorMessage
						.equals(WizardMessages.Plugin_Installation_Configuration_Error)) {
					contentViewer.setPlugin(null);
				}
			}
			getButton(IDialogConstants.OK_ID).setEnabled(errorMessage == null);
		}
	}
}
