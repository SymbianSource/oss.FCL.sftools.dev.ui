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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardDataTransferPage;

import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.ui.actions.AbstractExportProjectOperation;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IContributorDescriptor;

public class ExportWizardPage1 extends WizardDataTransferPage {

	static final String PREF_LAST_EXPORT_LOCATION = "exportWizard.lastLoc";

	private List lstProjects;

	protected String[] strSelProjects;

	protected String exportType;

	protected String destination;

	private Group grp;

	private Button btnBrowse;

	private Label lblDestination;

	private Text destText;

	private boolean canFinish;

	private int FILE_NAME_MAXLEN = 256;

	private Button[] btnArrayExportAs;

	public static int DirType = 2;

	LinkedHashMap<String, String> exportTypes;

	private String selectedProjectName;

	public static final String EXPORT_THEME_CONTEXT = "com.nokia.tools.screen.ui.exportTheme_context"; //$NON-NLS-1$

	protected ExportWizardPage1(String pageName) {
		super(pageName);
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginWidth = 9;
		gl.marginHeight = 9;
		gl.verticalSpacing = 7;
		gl.horizontalSpacing = 10;
		root.setLayout(gl);

		Label lbl = new Label(root, SWT.NONE);
		lbl.setText(WizardMessages.ExpWizPg1_listTitle);

		lstProjects = new List(root, SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL
				| SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 130;
		gd.minimumHeight = 30;
		gd.widthHint = 439;
		lstProjects.setLayoutData(gd);
		
		lstProjects.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				strSelProjects = lstProjects.getSelection();
				if (strSelProjects != null) {
					if (!StringUtils.isEmpty(destination)) {
						selectedProjectName = strSelProjects[0];
						if (!AbstractExportProjectOperation.DIR
								.equals(exportType)) {
							if (new File(destination).isDirectory()) {
								destination = destination + File.separator
										+ strSelProjects[0] + getExtension();
								destination = destination.replace('/', '\\')
										.replace("\\\\", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
								destText.setText(destination);
							} else {
								destination = new File(destination).getParent();
								destination = destination + File.separator
										+ strSelProjects[0] + getExtension();
								destination = destination.replace('/', '\\')
										.replace("\\\\", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
								destText.setText(destination);
							}
						}
					} else {
						setDefaultDestination();
					}
					
					handleWizardMessages(); // change messages in export wizard according to if selected project is theme or plug-in

					enableExportOptions();

				}
				updatePageCompletion();
			}
		});
		
		listOpenWorkspaceProjects();
		grp = new Group(root, SWT.NONE);
		grp.setText(WizardMessages.ExpWizPg1_optionGrp);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalIndent = 2;
		grp.setLayoutData(gd);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		grp.setLayout(layout);

		if (exportTypes == null) {
			exportTypes = getExportAsTypes();
		}
		int exportTypeLength = exportTypes.size();
		btnArrayExportAs = new Button[exportTypeLength];
		Set<String> set = exportTypes.keySet();
		int index = 0;
		String[] tempArray = (String[]) set.toArray(new String[set.size()]);
		Arrays.sort(tempArray, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return -(o1.compareTo(o2));
			}
		});
		boolean isSelected = false;
		for (String value : tempArray) {
			btnArrayExportAs[index] = new Button(grp, SWT.RADIO);
			btnArrayExportAs[index].setText(exportTypes.get(value));
			if (!isSelected) {
				btnArrayExportAs[index].setSelection(true);
				isSelected = true;
				exportType = value;
			}
			final String temp = value;
			btnArrayExportAs[index]
					.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							exportType = temp;
							if (strSelProjects != null) {
								if (!StringUtils.isEmpty(destination)) {
									if (AbstractExportProjectOperation.DIR
											.equals(exportType)) {
										if (!(new File(destination)
												.isDirectory())) {
											destination = new File(destination)
													.getParent();
											destText.setText(destination);
										}
									} else {
										if (new File(destination).isDirectory()) {
											destination = destination
													+ File.separator
													+ strSelProjects[0]
													+ getExtension();
											destination = destination.replace(
													'/', '\\').replace(
													"\\\\", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
											destText.setText(destination);
										} else {
											String ext = getExtension();
											String filename = new File(
													destination).getName();
											if (!filename.endsWith(ext)) {
												// remove extension
												int i = filename
														.lastIndexOf('.');
												if (i > 0) {
													filename = filename
															.substring(0, i);
												}
												filename += ext;
												destination = new File(
														destination)
														.getParent()
														+ File.separator
														+ filename;
												destination = destination
														.replace('/', '\\')
														.replace("\\\\", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
												destText.setText(destination);
											}
										}
									}
								} else
									setDefaultDestination();
							}

							updatePageCompletion();
						}
					});

			index++;
		}

		Composite destContainer = new Composite(root, SWT.NONE);
		destContainer.setLayout(new GridLayout(3, false));
		((GridLayout) destContainer.getLayout()).marginWidth = 0;
		destContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		lblDestination = new Label(destContainer, SWT.NONE);
		lblDestination.setText(WizardMessages.ExpWizPg1_destLabel);

		final Text destination = destText = new Text(destContainer, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		destination.setLayoutData(gd);
		destination.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String text = destination.getText().trim();
				if (!text.equals(ExportWizardPage1.this.destination)) {
					ExportWizardPage1.this.destination = text;
					updateExportType(text);
					updatePageCompletion();
				}
			}
		});

		btnBrowse = new Button(destContainer, SWT.PUSH);
		initializeDialogUnits(btnBrowse);
		setButtonLayoutData(btnBrowse);
		btnBrowse.setText(WizardMessages.ExpWizPg1_browseBtn);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowseButtonPressed(destination);
				updatePageCompletion();
			}
		});
		initializeDialogUnits(root);
		setControl(root);
		setDescription(WizardMessages.ExpWizPg1_description);
		setTitle(WizardMessages.ExpWizPg1_pageTitle);

		updateStates();
		setDefaultDestination();
		enableExportOptions();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent.getShell(),
				EXPORT_THEME_CONTEXT);
		
		handleWizardMessages();

	}

	private void enableExportOptions() {
		ArrayList<String> supportedFormats = getSupportedExportFormats(getSelectedProjectName());
		for (int i = 0; i < btnArrayExportAs.length; i++) {
			if (supportedFormats.contains(btnArrayExportAs[i].getText())) {
				btnArrayExportAs[i].setEnabled(true);
			} else {
				btnArrayExportAs[i].setEnabled(false);
			}
		}
	}

	protected void setDefaultDestination() {

		String destinationDir = null;
		String defaultDestDir = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().makeAbsolute().toOSString();

		// try to recall last one
		IPreferenceStore iPreferenceStore = UtilsPlugin.getDefault()
				.getPreferenceStore();
		destinationDir = iPreferenceStore.getString(PREF_LAST_EXPORT_LOCATION);

		if (!StringUtils.isEmpty(destinationDir)) {
			File destFile = new File(destinationDir);
			if (destFile.isAbsolute() && destFile.isDirectory()) {
				// ok
			} else {
				destinationDir = defaultDestDir;
			}
		}

		if (StringUtils.isEmpty(destinationDir)) {
			destinationDir = defaultDestDir;
		}

		// create default destination
		if (strSelProjects != null) {
			destinationDir += File.separator + strSelProjects[0]
					+ getExtension();
		}

		this.destination = destinationDir;

		destText.setText(this.destination);
		updatePageCompletion();
	}

	private ArrayList<String> getSupportedExportFormats(String projectName) {
		LinkedHashMap<String, String> types = getExportAsTypes(getSelectedProjectName());
		ArrayList<String> suppportedList = new ArrayList<String>();
		Collection<String> list = types.values();
		for (String string : list) {
			suppportedList.add(string);
		}
		return suppportedList;
	}

	private void updateExportType(String dest) {
		Path path = new Path(dest);
		String ext = path.getFileExtension();
		if (exportTypes == null) {
			exportTypes = getExportAsTypes();
		}
		if (ext != null) {
			if (exportTypes.get(ext.trim()) != null) {
				exportType = ext.trim();
			}
		} else {
			if (exportType == null) {
				exportType = AbstractExportProjectOperation.DIR;
			}
		}
		Set<String> set = exportTypes.keySet();
		String[] array = (String[]) set.toArray(new String[set.size()]);
		Arrays.sort(array, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return -(o1.compareTo(o2));
			}
		});
		if (array.length == btnArrayExportAs.length) {
			for (int i = 0; i < btnArrayExportAs.length; i++) {
				btnArrayExportAs[i].setSelection(array[i].equals(exportType));
			}
		}

	}

	protected void handleBrowseButtonPressed(Text text) {
		if (AbstractExportProjectOperation.DIR.equals(exportType)) {
			DirectoryDialog dialog = new DirectoryDialog(getContainer()
					.getShell(), SWT.SAVE);
			dialog.setMessage(WizardMessages.ExpWizPg1_dirDlgTitle);
			dialog.setText(WizardMessages.ExpWizPg1_dirDlgMsg);

			String currentSourceString = destination == null ? "" : destination; //$NON-NLS-1$
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
				destination = selectedDirectoryName;
				text.setText(destination);
			}
		} else {

			FileDialog dialog = new FileDialog(getContainer().getShell(),
					SWT.SAVE);
			dialog.setFilterExtensions(new String[] { "*." + exportType }); //$NON-NLS-1$ //$NON-NLS-2$
			dialog.setText(WizardMessages.ExpWizPg1_fileDlgTitle);
			String currentSourceString = destination == null ? "" : destination; //$NON-NLS-1$
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
				destination = selectedFileName;
				updateExportType(destination);
				text.setText(destination);
			}
		}
	}

	private void listOpenWorkspaceProjects() {
		lstProjects.removeAll();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		Arrays.sort(projects, new Comparator<IProject>() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Comparator#compare(T, T)
			 */
			public int compare(IProject o1, IProject o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});

		for (IProject project : projects) {
			try {
				if (project.isOpen()) {
					lstProjects.add(project.getName());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (lstProjects.getItemCount() > 0) {
			lstProjects.select(0);
			strSelProjects = new String[] { lstProjects.getItem(0) };

			IStructuredSelection sel = ((ExportWizard) getWizard())
					.getSelection();
			if (sel != null && !sel.isEmpty()) {
				if (sel.getFirstElement() instanceof IResource) {
					IProject project = ((IResource) sel.getFirstElement())
							.getProject();
					int idx = Arrays.asList(lstProjects.getItems()).indexOf(
							project.getName());
					if (idx >= 0) {
						lstProjects.select(idx);
						strSelProjects = new String[] { lstProjects
								.getItem(idx) };
					}
				}
			}
		}

	}

	@Override
	protected boolean allowNewContainerName() {
		return false;
	}

	public void handleEvent(Event event) {
	}

	@Override
	protected boolean validateDestinationGroup() {
		try {
			for (int i = 0; i < btnArrayExportAs.length; i++) {
				if (!btnArrayExportAs[i].isEnabled()) {
					if (btnArrayExportAs[i].getSelection() == true) {
						setErrorMessage(WizardMessages.ExpWiz_optionnotavailable_msg);
						return false;
					}
				}
			}
			/* allowed chars check */
			if (destination != null && destination.trim().length() > 0) {
				String fName = destination;
				String parent = new File(fName).getParent();

				if (parent != null && parent.length() + 1 < fName.length()) {
					fName = fName.substring(parent.length() + 1);

					if (AbstractExportProjectOperation.DIR.equals(exportType)
							&& fName.endsWith("\\") || fName.endsWith("//"))
						fName = fName.substring(0, fName.length() - 1);
					if (!FileUtils.isFileValid(new Path(fName), IResource.FILE)) {
						setErrorMessage(WizardMessages.bind(
								WizardMessages.ExpWizPg1_errFileName, fName));
						return false;
					}
				}
			}

			if (destination.trim().length() == 0) {
				setErrorMessage(WizardMessages.ExpWizPg1_No_Destination_Error);
				return false;
			}
			if (exportTypes == null) {
				exportTypes = getExportAsTypes();
			}
			Set<String> set = exportTypes.keySet();

			for (String string : set) {
				if (!AbstractExportProjectOperation.DIR.equals(exportType)) {
					if (string.equals(exportType)) {
						String ext = "." + exportType;
						if (!destination.toLowerCase().endsWith(ext)) {
							setErrorMessage(WizardMessages.ExpWizPg1_Extension_Error
									+ "*" + ext);
							return false;
						}
					}
				}
			}

			if (!AbstractExportProjectOperation.DIR.equals(exportType)) {
				Path path = new Path(destination);
				String ext = path.getFileExtension();
				String fileName = path.lastSegment().toString();
				fileName = fileName.substring(0, (fileName.length() - 1)
						- ext.length());
				if (fileName.length() > FILE_NAME_MAXLEN) {
					setErrorMessage(WizardMessages.ExpWizPg1_Zip_Name_Too_Long_Error);
					return false;
				}
			}

			File dir = new File(destination);
			if (!AbstractExportProjectOperation.DIR.equals(exportType)
					&& (dir.isDirectory() || destination.endsWith("/") || destination
							.endsWith("\\"))) {
				setErrorMessage(null);
				return false;
			}
			return dir.isAbsolute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String getMessage() {
		if (lstProjects.getItemCount() > 0)
			try {
				File target = exportType
						.equals(AbstractExportProjectOperation.DIR) ? new File(
						destination) : new File(destination).getParentFile();
				if (!target.exists()) {
					return WizardMessages.ExpWizPg1_infoDirNotExists;
				}

				if (new File(destination).exists()) {
					if (exportType.equals(AbstractExportProjectOperation.DIR)) {
						if (new File(destination).isDirectory()) {
							if (new File(destination).list().length > 0) {
								return WizardMessages.ExpWizPg1_targetDirectoryAlreadyExists;
							}
						}
					} else {
						if (new File(destination).isFile()) {
							return WizardMessages.ExpWizPg1_targetFileAlreadyExists;
						}
					}

				}
			} catch (Exception e) {
			}
		return null;
	}

	@Override
	public int getMessageType() {
		return WARNING;
	}

	@Override
	protected boolean validateOptionsGroup() {
		return true;
	}

	@Override
	protected boolean validateSourceGroup() {
		return this.strSelProjects != null && this.strSelProjects.length == 1;
	}

	public boolean finish() {
		canFinish = true;
		ensureTargetIsValid();
		if (!canFinish)
			return false;

		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		for (IProject p : projects) {
			if (p.getName().equals(strSelProjects[0])) {
				AbstractExportProjectOperation operation = getExportAsOperation(getSelectedProjectName());
				operation.setThemeExportParameters(exportType, p, destination);
				try {
					getContainer().run(true, false, operation);
					return true;
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					Throwable t = e;
					while (t instanceof InvocationTargetException) {
						t = ((InvocationTargetException) t)
								.getTargetException();
					}
					if (t instanceof CoreException) {
						ErrorDialog.openError(getShell(),
								WizardMessages.ExpWiz_errMsgTitle, t
										.getLocalizedMessage(),
								((CoreException) t).getStatus());
					} else {
						MessageDialog.openError(getShell(),
								WizardMessages.ExpWiz_errMsgTitle, t
										.getLocalizedMessage());
					}
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}

				return false;
			}
		}
		return false;
	}

	private void ensureTargetIsValid() {

		if (exportType.equals(AbstractExportProjectOperation.DIR)) {
			if (!(new File(destination)).exists()) {
				File dir = new File(destination);
				if (dir.mkdirs())
					return;
				displayErrorDialog(WizardMessages.ExpWizPg1_errNoDir);
				canFinish = false;
			} else if (!(new File(destination)).isDirectory()) {
				displayErrorDialog(WizardMessages.ExpWizPg1_errInvalidDir);
				canFinish = false;
			}
		} else {

			if (!destination.toLowerCase().endsWith(getExtension())) {
				destination += getExtension();
			}

			// ensure that file's parent does exits
			File tmp = new File(destination);
			if (!tmp.getParentFile().exists()) {

				File dir = new File(destination).getParentFile();
				if (dir.mkdirs())
					return;

				displayErrorDialog(WizardMessages.ExpWizPg1_errInvalidFile);
				canFinish = false;
			}
		}
	}

	@Override
	protected String getErrorDialogTitle() {
		return WizardMessages.ExpWiz_errMsgTitle;
	}

	private String getExtension() {
		if (!AbstractExportProjectOperation.DIR.equals(exportType)) {
			return "." + exportType;
		} else
			return ""; //$NON-NLS-1$
	}

	/**
	 * Handles enabled/disabled state logic of components
	 */
	private void updateStates() {
		if (strSelProjects == null) {
			setTitle(WizardMessages.ExpWizPg1_pageTitleThemeOrPlugin);
			setErrorMessage(WizardMessages.ExpWizPg1_No_Themes_To_Export);
			grp.setEnabled(false);
			// chkTypeZIP.setEnabled(false);
			
			lblDestination.setEnabled(false);
			destText.setEnabled(false);
			btnBrowse.setEnabled(false);
			for (int i = 0; i < btnArrayExportAs.length; i++) {
				btnArrayExportAs[i].setEnabled(false);
			}
		} else {
			grp.setEnabled(true);
			// chkTypeZIP.setEnabled(true);
			lblDestination.setEnabled(true);
			destText.setEnabled(true);
			btnBrowse.setEnabled(true);
			for (int i = 0; i < btnArrayExportAs.length; i++) {
				btnArrayExportAs[i].setEnabled(true);
			}
		}
	}

	public LinkedHashMap<String, String> getExportAsTypes() {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		for (IContributorDescriptor desc : ExtensionManager
				.getContributorDescriptors()) {
			AbstractExportProjectOperation operation = (AbstractExportProjectOperation) desc
					.createOperation(IContributorDescriptor.OPERATION_EXPORT_PROJECT);
			if (operation != null) {
				map.putAll(operation.getExportAsOptions());
			}
		}
		return map;
	}

	public LinkedHashMap<String, String> getExportAsTypes(String selectedProject) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		for (IContributorDescriptor desc : ExtensionManager
				.getContributorDescriptors()) {
			AbstractExportProjectOperation operation = (AbstractExportProjectOperation) desc
					.createOperation(IContributorDescriptor.OPERATION_EXPORT_PROJECT);
			if (operation != null && operation.supportsProject(selectedProject)) {
				map.putAll(operation.getExportAsOptions());
			}
		}		
		return map;
	}
	
	public void handleWizardMessages() {
		if (selectedProjectName == null) return;
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(selectedProjectName);
		try {
			if (project.getDescription().hasNature("com.nokia.tools.content.core.S60DesignNature")) {
				setTitle(WizardMessages.ExpWizPg1_pageTitle);
				setDescription(WizardMessages.ExpWizPg1_description);
			}
			else {
				setTitle(WizardMessages.ExpWizPg1_pageTitlePlugin);
				setDescription(WizardMessages.ExpWizPg1_descriptionPlugin);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public String getSelectedProjectName() {
		if (selectedProjectName == null) {
			if (lstProjects.getSelection() != null
					&& lstProjects.getSelection().length > 0) {
				this.selectedProjectName = lstProjects.getSelection()[0];
			}
		}
		return selectedProjectName;
	}

	public AbstractExportProjectOperation getExportAsOperation(
			String selectedProject) {
		for (IContributorDescriptor desc : ExtensionManager
				.getContributorDescriptors()) {
			AbstractExportProjectOperation operation = (AbstractExportProjectOperation) desc
					.createOperation(IContributorDescriptor.OPERATION_EXPORT_PROJECT);
			if (operation != null && operation.supportsProject(selectedProject)) {
				return operation;
			}
		}
		return null;
	}
}
