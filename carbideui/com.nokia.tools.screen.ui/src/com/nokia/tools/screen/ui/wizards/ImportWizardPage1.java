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
package com.nokia.tools.screen.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardDataTransferPage;

import com.nokia.tools.screen.ui.actions.AbstractImportProjectOperation;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IContributorDescriptor;

public class ImportWizardPage1 extends WizardDataTransferPage {
	private String importType;

	private String source;

	private Text srcText;

	private String titleMsg;

	private int titleMsgType;

	private Button[] btnArrayImportFrom;

	LinkedHashMap<String, String> importTypes;
	
	public static final String IMPORT_THEME_CONTEXT = "com.nokia.tools.screen.ui.importTheme_context"; //$NON-NLS-1$


	protected ImportWizardPage1(String pageName) {
		super(pageName);
	}

	@Override
	protected boolean allowNewContainerName() {
		return true;
	}

	public void handleEvent(Event event) {
	}

	public void createControl(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginWidth = 9;
		gl.marginHeight = 9;
		gl.verticalSpacing = 7;
		gl.horizontalSpacing = 10;
		root.setLayout(gl);

		Group grp = new Group(root, SWT.NONE);
		grp.setText(WizardMessages.ImpWizPg1_optionGrp);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalIndent = 2;
		gd.widthHint = 457;
		grp.setLayoutData(gd);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		grp.setLayout(layout);

		if (importTypes == null) {
			importTypes = getImportFromTypes();
		}
		int importTypeLength = importTypes.size();
		btnArrayImportFrom = new Button[importTypeLength];
		Set<String> set = importTypes.keySet();
		boolean isSelected = false;
		int index = 0;
		for (String value : set) {
			btnArrayImportFrom[index] = new Button(grp, SWT.RADIO);
			btnArrayImportFrom[index].setText(importTypes.get(value));
			if (!isSelected) {
				btnArrayImportFrom[index].setSelection(true);
				isSelected = true;
				importType = value;

			}
			final String temp = value;
			btnArrayImportFrom[index]
					.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							importType = temp;
							source = null;
							srcText.setText("");
							updatePageCompletion();
						}
					});

			index++;
		}

		Composite destContainer = new Composite(root, SWT.NONE);
		destContainer.setLayout(new GridLayout(3, false));
		((GridLayout) destContainer.getLayout()).marginWidth = 0;
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 211;
		destContainer.setLayoutData(gd);

		Label lbl = new Label(destContainer, SWT.NONE);
		lbl.setText(WizardMessages.ImpWizPg1_srcLabel);

		final Text destination = srcText = new Text(destContainer, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		destination.setLayoutData(gd);
		destination.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String text = destination.getText().trim();
				if (!text.equals(ImportWizardPage1.this.source)) {
					ImportWizardPage1.this.source = text;
					updateImportType(source);
					updatePageCompletion();
				}
			}
		});

		Button select = new Button(destContainer, SWT.PUSH);
		initializeDialogUnits(select);
		setButtonLayoutData(select);
		select.setText(WizardMessages.ImpWizPg1_browseBtn);
		select.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowseButtonPressed(destination);
				updatePageCompletion();
			}
		});
		initializeDialogUnits(root);
		setControl(root);
		setDescription(WizardMessages.ImpWizPg1_description);
		setTitle(WizardMessages.ImpWizPg1_pageTitle);

		setPageComplete(false);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent.getShell(),
				IMPORT_THEME_CONTEXT);
		destination.setFocus();
	}


	private void updateImportType(String source) {
		Path path = new Path(source);
		if (AbstractImportProjectOperation.DIR.equals(importType)) {
			return;
		}
		String ext = path.getFileExtension();
		LinkedHashMap<String, String> map = getImportFromTypes();
		if (ext != null) {
			if (map.containsKey(ext.trim())) {
				importType = ext.trim();
			}
			enableImportOptions();
		} else {
			if (importType == null) {
				importType = AbstractImportProjectOperation.DIR;
			}
		}
	}

	protected void handleBrowseButtonPressed(Text text) {
		if (AbstractImportProjectOperation.DIR.equals(importType)) {
			DirectoryDialog dialog = new DirectoryDialog(getContainer()
					.getShell(), SWT.OPEN);
			dialog.setMessage(WizardMessages.ImpWizPg1_dirDlgTitle);
			dialog.setText(WizardMessages.ImpWizPg1_dirDlgMsg);

			String currentSourceString = source == null ? "" : source; //$NON-NLS-1$
			int lastSeparatorIndex = currentSourceString
					.lastIndexOf(File.separator);
			if (new File(currentSourceString).isDirectory()) {
				dialog.setFilterPath(currentSourceString);
			} else if (lastSeparatorIndex != -1) {
				dialog.setFilterPath(currentSourceString.substring(0,
						lastSeparatorIndex));
			}
			String selectedDirectoryName = dialog.open();

			if (selectedDirectoryName != null) {
				setErrorMessage(null);
				source = selectedDirectoryName;
				text.setText(source);
			} else {
				updatePageCompletion();
				text.setFocus();
			}
		} else {
			FileDialog dialog = new FileDialog(getContainer().getShell(),
					SWT.OPEN);
			dialog.setFilterExtensions(getFilterExtensions()); //$NON-NLS-1$ //$NON-NLS-2$
			dialog.setText(WizardMessages.ImpWizPg1_fileDlgTitle);
			String currentSourceString = source == null ? "" : source; //$NON-NLS-1$
			int lastSeparatorIndex = currentSourceString
					.lastIndexOf(File.separator);
			if (new File(currentSourceString).isDirectory()) {
				dialog.setFilterPath(currentSourceString);
			} else if (lastSeparatorIndex != -1) {
				dialog.setFilterPath(currentSourceString.substring(0,
						lastSeparatorIndex));
			}
			String selectedFileName = dialog.open();

			if (selectedFileName != null) {
				setErrorMessage(null);
				source = selectedFileName;
				updateImportType(source);
				text.setText(source);
			} else {
				updatePageCompletion();
				text.setFocus();
			}
		}
		
		handleWizardMessages();		
	}

	@Override
	protected boolean validateSourceGroup() {
		if (source == null) {
			setErrorMessage(WizardMessages.ImpWizPg1_errNoSource);
			return false;
		}
		if (source.length() == 0) {
			setErrorMessage(WizardMessages.ImpWizPg1_errNoSource);
			return false;
		}

		if (AbstractImportProjectOperation.DIR.equals(importType)) {
			boolean valid = AbstractImportProjectOperation
					.isValidDotProjectDirectory(source);
			if (!valid) {
				if (!new File(source).exists()) {
					setErrorMessage(WizardMessages.ImpWizPg1_errNoDir);
				} else if (!new File(source).isDirectory()) {
					setErrorMessage(WizardMessages.ImpWizPg1_errNoDir);
				} else {
					setErrorMessage(WizardMessages.ImpWizPg1_errInvalidDir);
				}
				return valid;
			}
		} else {
			if (source != null) {
				if (!new File(source).exists()) {
					setErrorMessage(WizardMessages.ImpWizPg1_errNoFile);
					return false;
				}
			}
		}
		AbstractImportProjectOperation operation = getImportAsOperation();
		if (operation == null) {
			setErrorMessage(generateErrorMessage());
			return false;
		}
		HashMap<Boolean, String> map = operation.validate(source, importType);
		boolean passed = false;
		if (map != null && !map.isEmpty()) {
			passed = (Boolean) map.keySet().toArray()[0];
			if (!passed) {
				setErrorMessage(map.get(passed));
			}

		}
		return passed;
	}

	private String generateErrorMessage() {
		String[] extns = getFilterExtensions();
		if (extns != null && extns.length > 0) {
			String ext = extns[0];
			String[] exts = ext.split(";");
			StringBuffer buffer = new StringBuffer();
			buffer.append(WizardMessages.ImpWizPg1_errInvalidFormat);

			for (int i = 0; i < exts.length; i++) {
				buffer.append("*" + exts[i].substring(1));
				if (i != exts.length - 1) {
					buffer.append(" or ");
				}
			}
			return buffer.toString();
		}
		return WizardMessages.ImpWizPg1_errInvalidFile;
	}

	@Override
	protected boolean validateDestinationGroup() {
		titleMsg = null;
		titleMsgType = WARNING;
		String projectName = getImportAsOperation().getProjectName();
		if (projectName != null) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projectName);
			if (project.exists()) {

				// compute new name
				int pos = 2;
				while (project.exists()) {
					project = ResourcesPlugin.getWorkspace().getRoot()
							.getProject(projectName + "_" + pos++);
				}
				
				IProject existingProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				try {
					if (existingProject.getDescription().hasNature("com.nokia.tools.content.core.S60DesignNature")) {
						titleMsg = WizardMessages.bind(
								WizardMessages.ImpWizPg1_projectAlreadyExists,
								projectName, project.getName());
					}
					else {
						titleMsg = WizardMessages.bind(
								WizardMessages.ImpWizPg1_projectAlreadyExistsPlugin,
								projectName, project.getName());
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
				if (isCurrentPage()) {
					getContainer().updateMessage();
				}

			}
		}
		return true;
	}

	@Override
	protected void updatePageCompletion() {
		AbstractImportProjectOperation operation = getImportAsOperation();
		if (operation != null)
			operation.setThemeImportParameters(importType, source);
		titleMsg = null;
		
		handleWizardMessages();
		
		super.updatePageCompletion();
	}
	
	public void handleWizardMessages() {
		if (importType.compareTo("jar") == 0) {
			setDescription(WizardMessages.ImpWizPg1_descriptionPlugin);
			setTitle(WizardMessages.ImpWizPg1_pageTitlePlugin);
		}
		else {
			setTitle(WizardMessages.ImpWizPg1_pageTitle);
			setDescription(WizardMessages.ImpWizPg1_description);
		}
		
		if (source == null)	return;
	}

	@Override
	public String getMessage() {
		if (titleMsg != null)
			return titleMsg;
		return super.getMessage();
	}

	@Override
	public int getMessageType() {
		if (titleMsg != null)
			return titleMsgType;
		return super.getMessageType();
	}

	public LinkedHashMap<String, String> getImportFromTypes() {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		for (IContributorDescriptor desc : ExtensionManager
				.getContributorDescriptors()) {
			AbstractImportProjectOperation operation = (AbstractImportProjectOperation) desc
					.createOperation(IContributorDescriptor.OPERATION_IMPORT_PROJECT);
			if (operation != null) {
				map.putAll(operation.getImportFromOptions());
			}
		}
		return map;
	}

	private String[] getFilterExtensions() {
		ArrayList<String> extns = new ArrayList<String>();
		for (IContributorDescriptor desc : ExtensionManager
				.getContributorDescriptors()) {
			AbstractImportProjectOperation operation = (AbstractImportProjectOperation) desc
					.createOperation(IContributorDescriptor.OPERATION_IMPORT_PROJECT);
			if (operation != null) {
				extns.addAll(operation
						.getAlternateImportFileExtensions(importType));
			}
		}
		if (!AbstractImportProjectOperation.DIR.equals(importType)
				&& importType != null) {

			extns.add(importType);
			int index = 0;
			StringBuffer buffer = new StringBuffer();
			for (String string : extns) {
				buffer.append("*." + string);
				if (index != extns.size() - 1)
					buffer.append(";");
				index++;
			}

			return new String[] { buffer.toString() };
		} else {
			return new String[] {};
		}

	}

	private void enableImportOptions() {
		ArrayList<String> supportedFormats = getSupportedImportFormats();
		for (int i = 0; i < btnArrayImportFrom.length; i++) {
			if (supportedFormats.contains(btnArrayImportFrom[i].getText())) {
				btnArrayImportFrom[i].setEnabled(true);
			} else {
				btnArrayImportFrom[i].setEnabled(false);
			}
		}
	}

	private ArrayList<String> getSupportedImportFormats() {
		LinkedHashMap<String, String> types = getImportFromTypes();
		ArrayList<String> suppportedList = new ArrayList<String>();
		Collection<String> list = types.values();
		for (String string : list) {
			suppportedList.add(string);
		}
		return suppportedList;
	}

	public AbstractImportProjectOperation getImportAsOperation() {
		for (IContributorDescriptor desc : ExtensionManager
				.getContributorDescriptors()) {
			AbstractImportProjectOperation operation = (AbstractImportProjectOperation) desc
					.createOperation(IContributorDescriptor.OPERATION_IMPORT_PROJECT);
			if (null != source && operation != null
					&& operation.supportsPath(source, importType)) {
				operation.setThemeImportParameters(importType, source);
				return operation;
			}
		}
		return null;
	}

}
