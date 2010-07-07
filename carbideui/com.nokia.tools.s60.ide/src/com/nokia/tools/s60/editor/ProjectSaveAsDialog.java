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
package com.nokia.tools.s60.editor;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.screen.ui.branding.ISharedImageDescriptor;
import com.nokia.tools.ui.branding.util.BrandedTitleAreaDialog;

/**
 * Copied from WizardNewProjectCreationPage and SaveAsDialog.
 * 
 */
public class ProjectSaveAsDialog extends BrandedTitleAreaDialog {
	private static final String FORWARD_SLASH = "/";

	public static final String SAVE_AS_CONTEXT = "com.nokia.tools.s60.ide.saveAs_context"; 

	private static final int SIZING_TEXT_FIELD_WIDTH = 250;

	private String initialProjectFieldValue;

	private String initialLocationFieldValue;

	private boolean useDefaults = true;

	private String customLocationFieldValue;

	private Text projectNameField;

	private Text locationPathField;

	private Label locationLabel;

	private Button browseButton;

	private Button okButton;

	private IProject project;

	private IPath location;

	private int TEXT_LIMIT = 32;

	private Listener nameModifyListener = new Listener() {
		public void handleEvent(Event e) {
			boolean valid = validatePage();
			setDialogComplete(valid);
			if (valid)
				setLocationForSelection();
		}
	};

	private Listener locationModifyListener = new Listener() {
		public void handleEvent(Event e) {
			setDialogComplete(validatePage());
		}
	};

	/**
	 * Creates a new Save As dialog for no specific file.
	 * 
	 * @param parentShell
	 *            the parent shell
	 */
	public ProjectSaveAsDialog(Shell parentShell) {
		super(parentShell);
		customLocationFieldValue = "";
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setDialogComplete(false);
	}

	/*
	 * (non-Javadoc) Method declared in Window.
	 */
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		setTitle(EditorMessages.Editor_SaveAsDialog_title);
		setMessage(EditorMessages.Editor_SaveAsDialog_message);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent.getParent(),
				ProjectSaveAsDialog.SAVE_AS_CONTEXT);

		setDialogComplete(false);

		return contents;
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);

		composite.setFont(parent.getFont());

		initializeDialogUnits(parent);

		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout.marginHeight = 13;
		layout.marginWidth = 13;
		layout.verticalSpacing = 7;

		createProjectNameGroup(composite);

		createProjectLocationGroup(composite);

		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout = new GridLayout();
		container.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;

		final Label separator = new Label(container, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return area;
	}

	public IPath getLocationPath() {
		if (useDefaults)
			return Platform.getLocation();

		return new Path(getProjectLocationFieldValue());
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void okPressed() {
		project = getProjectHandle();
		location = getLocationPath();
		close();
	}

	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
	}

	/**
	 * Sets the completion state of this dialog and adjusts the enable state of
	 * the Ok button accordingly.
	 * 
	 * @param value
	 *            <code>true</code> if this dialog is compelete, and
	 *            <code>false</code> otherwise
	 */
	protected void setDialogComplete(boolean value) {
		if (okButton != null) {
			okButton.setEnabled(value);
		}
	}

	/**
	 * Returns whether this page's controls currently all contain valid values.
	 * 
	 * @return <code>true</code> if all controls are valid, and
	 *         <code>false</code> if at least one is invalid
	 */
	protected boolean validatePage() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		String projectFieldContents = getProjectNameFieldValue();
		if (projectFieldContents.equals("")) { 
			setErrorMessage(EditorMessages.Error_Editor_SaveAsDialog_projectNameEmpty);
			return false;
		}

		IStatus nameStatus = workspace.validateName(projectFieldContents,
				IResource.PROJECT);
		if (!nameStatus.isOK()) {
			setErrorMessage(nameStatus.getMessage());
			return false;
		}

		String locationFieldContents = getProjectLocationFieldValue();

		if (locationFieldContents.equals("")) { 
			setErrorMessage(null);
			setErrorMessage(EditorMessages.Error_Editor_SaveAsDialog__projectLocationEmpty);
			return false;
		}

		IPath path = new Path(""); 
		if (!path.isValidPath(locationFieldContents)
				|| containsForwardSlash(locationFieldContents)) {
			setErrorMessage(EditorMessages.Error_Editor_SaveAsDialog_locationError);
			return false;
		}

		IPath projectPath = new Path(locationFieldContents);
		if (!useDefaults && Platform.getLocation().isPrefixOf(projectPath)) {
			setErrorMessage(EditorMessages.Error_Editor_SaveAsDialog_defaultLocationError);
			return false;
		}

		IProject handle = getProjectHandle();
		if (handle.exists()) {
			setErrorMessage(EditorMessages.Error_Editor_SaveAsDialog_projectExistsMessage);
			return false;
		}

		// this is just to check existence of project with different case
		// this is automatically detected during the project creation phase in
		// eclipse default behavior, but it might be user-friendly to prompt the
		// error beforehand
		File newDir = new File(workspace.getRoot().getLocation().toFile(),
				projectFieldContents);
		if (newDir.exists()) {
			setErrorMessage(MessageFormat.format(
					EditorMessages.Error_Editor_SaveAs_caseVariantExistsError,
					new Object[] { projectFieldContents }));
			return false;
		}

		/*
		 * If not using the default value validate the location.
		 */
		if (!useDefaults()) {
			IStatus locationStatus = workspace.validateProjectLocation(handle,
					projectPath);
			if (!locationStatus.isOK()) {
				setErrorMessage(locationStatus.getMessage()); 
				return false;
			}
		}

		setErrorMessage(null);
		setMessage(EditorMessages.Editor_SaveAsDialog_message);
		return true;
	}

	private boolean containsForwardSlash(String locationFieldContents) {
		return locationFieldContents.contains(FORWARD_SLASH);
	}

	/**
	 * Creates a project resource handle for the current project name field
	 * value.
	 * <p>
	 * This method does not create the project resource; this is the
	 * responsibility of <code>IProject::create</code> invoked by the new
	 * project resource wizard.
	 * </p>
	 * 
	 * @return the new project resource handle
	 */
	public IProject getProjectHandle() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(
				getProjectName());
	}

	/**
	 * @return the destination project.
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * @return the location.
	 */
	public IPath getLocation() {
		return location;
	}

	/**
	 * Returns the current project name as entered by the user, or its
	 * anticipated initial value.
	 * 
	 * @return the project name, its anticipated initial value, or
	 *         <code>null</code> if no project name is known
	 */
	public String getProjectName() {
		if (projectNameField == null)
			return initialProjectFieldValue;

		return getProjectNameFieldValue();
	}

	/**
	 * Returns the value of the project name field with leading and trailing
	 * spaces removed.
	 * 
	 * @return the project name in the field
	 */
	private String getProjectNameFieldValue() {
		if (projectNameField == null)
			return ""; 

		return projectNameField.getText().trim();
	}

	/**
	 * Returns the value of the project location field with leading and trailing
	 * spaces removed.
	 * 
	 * @return the project location directory in the field
	 */
	private String getProjectLocationFieldValue() {
		if (locationPathField == null)
			return ""; 

		return locationPathField.getText().trim();
	}

	/**
	 * Returns the useDefaults.
	 * 
	 * @return boolean
	 */
	public boolean useDefaults() {
		return useDefaults;
	}

	/**
	 * Creates the project location specification controls.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	private final void createProjectLocationGroup(Composite parent) {

		Font font = parent.getFont();
		// project specification group
		Group projectGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		projectGroup.setLayout(layout);
		layout.numColumns = 3;
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 9;

		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projectGroup.setFont(font);
		projectGroup
				.setText(EditorMessages.Editor_SaveAsDialog_projectContentsGroupLabel);

		final Button useDefaultsButton = new Button(projectGroup, SWT.CHECK
				| SWT.RIGHT);
		useDefaultsButton
				.setText(EditorMessages.Editor_SaveAsDialog_useDefaultLabel);
		useDefaultsButton.setSelection(useDefaults);
		useDefaultsButton.setFont(font);

		GridData buttonData = new GridData();
		buttonData.horizontalSpan = 3;
		useDefaultsButton.setLayoutData(buttonData);

		createUserSpecifiedProjectLocationGroup(projectGroup, !useDefaults);

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				useDefaults = useDefaultsButton.getSelection();
				browseButton.setEnabled(!useDefaults);
				locationPathField.setEnabled(!useDefaults);
				locationLabel.setEnabled(!useDefaults);
				if (useDefaults) {
					customLocationFieldValue = locationPathField.getText();
					setLocationForSelection();
				} else {
					locationPathField.setText(customLocationFieldValue);
				}
			}
		};
		useDefaultsButton.addSelectionListener(listener);
	}

	/**
	 * Creates the project name specification controls.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	private final void createProjectNameGroup(Composite parent) {
		// project specification group
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		projectGroup.setLayout(layout);
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.verticalSpacing = 9;

		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new project label
		Label projectLabel = new Label(projectGroup, SWT.NONE);
		projectLabel.setText(EditorMessages.Editor_SaveAsDialog_nameLabel);
		projectLabel.setFont(parent.getFont());

		// new project name entry field
		projectNameField = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		projectNameField.setLayoutData(data);
		projectNameField.setFont(parent.getFont());
		projectNameField.setTextLimit(TEXT_LIMIT);

		// Set the initial value first before listener
		// to avoid handling an event during the creation.
		if (initialProjectFieldValue != null)
			projectNameField.setText(initialProjectFieldValue);
		projectNameField.addListener(SWT.Modify, nameModifyListener);
	}

	/**
	 * Creates the project location specification controls.
	 * 
	 * @param projectGroup
	 *            the parent composite
	 * @param enabled
	 *            the initial enabled state of the widgets created
	 */
	private void createUserSpecifiedProjectLocationGroup(
			Composite projectGroup, boolean enabled) {

		Font font = projectGroup.getFont();

		// location label
		locationLabel = new Label(projectGroup, SWT.NONE);
		locationLabel.setText(EditorMessages.Editor_SaveAsDialog_locationLabel);
		locationLabel.setEnabled(enabled);
		locationLabel.setFont(font);

		// project location entry field
		locationPathField = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		locationPathField.setLayoutData(data);
		locationPathField.setEnabled(enabled);
		locationPathField.setFont(font);

		// browse button
		browseButton = new Button(projectGroup, SWT.PUSH);
		browseButton.setText(EditorMessages.Editor_SaveAsDialog_browseLabel);
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleLocationBrowseButtonPressed();
			}
		});

		browseButton.setEnabled(enabled);
		browseButton.setFont(font);
		setButtonLayoutData(browseButton);

		// Set the initial value first before listener
		// to avoid handling an event during the creation.
		if (initialLocationFieldValue == null)
			locationPathField.setText(Platform.getLocation().toOSString());
		else
			locationPathField.setText(initialLocationFieldValue);
		locationPathField.addListener(SWT.Modify, locationModifyListener);
	}

	/**
	 * Set the location to the default location if we are set to useDefaults.
	 */
	void setLocationForSelection() {
		if (useDefaults)
			locationPathField
					.setText(getDefaultLocationForName(getProjectNameFieldValue()));
	}

	/**
	 * Get the defualt location for the provided name.
	 * 
	 * @param nameValue
	 *            the name
	 * @return the location
	 */
	private String getDefaultLocationForName(String nameValue) {
		IPath defaultPath = Platform.getLocation().append(nameValue);
		return defaultPath.toOSString();
	}

	/**
	 * Open an appropriate directory browser
	 */
	void handleLocationBrowseButtonPressed() {
		DirectoryDialog dialog = new DirectoryDialog(locationPathField
				.getShell());
		dialog.setMessage(EditorMessages.Editor_SaveAsDialog_directoryLabel);

		String dirName = getProjectLocationFieldValue();
		if (!dirName.equals("")) { 
			File path = new File(dirName);
			if (path.exists())
				dialog.setFilterPath(new Path(dirName).toOSString());
		}

		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			customLocationFieldValue = selectedDirectory;
			//validatePage();
			locationPathField.setText(customLocationFieldValue);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.utils.BrandedTitleAreaDialog#getBannerIconDescriptor()
	 */
	@Override
	protected ImageDescriptor getBannerIconDescriptor() {
		return ISharedImageDescriptor.WIZBAN_CREATE_PROJECT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.utils.BrandedTitleAreaDialog#getTitle()
	 */
	@Override
	protected String getTitle() {
		return EditorMessages.Editor_SaveAsDialog_text;
	}
}
