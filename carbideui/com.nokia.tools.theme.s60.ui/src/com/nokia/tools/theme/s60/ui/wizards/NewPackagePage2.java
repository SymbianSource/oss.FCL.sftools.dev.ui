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
package com.nokia.tools.theme.s60.ui.wizards;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.packaging.KeyPair;
import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingContext;
import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.packaging.IPackager.Packager;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.screen.ui.wizards.AbstractNewPackagePage;
import com.nokia.tools.theme.s60.packaging.util.SymbianUtil;
import com.nokia.tools.theme.s60.ui.Activator;
import com.nokia.tools.theme.s60.ui.actions.KeyPairsAction;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;

/**
 * This is the second page of the deployment package creation wizard. Page
 * includes package destination and possible key pair selection to be used in
 * signing part of the packaging process. Key pair store can be also accessed
 * via this page.
 * 
 */
public class NewPackagePage2 extends AbstractNewPackagePage {

	Shell shell;

	private Text txtDestination;

	private Button btnDestBrowse;

	private String currentProjectPath = "", currentProjectName = "";

	private String themeName = "";

	private String[] packageFilterExt = { "*.sis" };

	protected String selDestination = "";

	private Combo cboKeyPairs;

	private Button btnModify, btnNoSign, btnSign;

	private Label lblKeyPair, lblPassword;

	private String selectedKeyPair;

	protected Text txtPassword;

	private Label label1;

	private Composite container, container2;

	private PackagingContext context;

	private int FILE_NAME_MAXLEN = 256;

	public NewPackagePage2() {
		setDescription(WizardMessages.New_Package_Banner_Message_Page2);
	}

	public void createControl(Composite parent) {
		currentProjectPath = ((IFileEditorInput) EclipseUtils
				.getActiveSafeEditor().getEditorInput()).getFile().getProject()
				.getLocation().toOSString();

		currentProjectName = ((IFileEditorInput) EclipseUtils
				.getActiveSafeEditor().getEditorInput()).getFile().getProject()
				.getName();

		container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;

		Label lblProjectText = new Label(container, SWT.NONE);
		lblProjectText
				.setText(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_lblProject_Text);

		Label lblProject = new Label(container, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 410;
		lblProject.setLayoutData(gd);
		lblProject.setText(currentProjectPath);

		// Dummy label to fill a column
		new Label(container, SWT.NONE);

		Label lblDestination = new Label(container, SWT.NONE);
		lblDestination
				.setText(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_lblDestination_Text);

		txtDestination = new Text(container, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 410;
		txtDestination.setLayoutData(gd);

		txtDestination.addListener(SWT.Modify, this);
		txtDestination.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				selDestination = txtDestination.getText();
				pageChanged();
			}
		});
		selDestination = txtDestination.getText();

		shell = this.getShell();
		class OpenDestination implements SelectionListener {
			public void widgetSelected(SelectionEvent event) {
				FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
				fileDialog
						.setText(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_fileDialog_Title);
				fileDialog.setFilterPath(ResourcesPlugin.getWorkspace()
						.getRoot().getLocation().toOSString());
				fileDialog.setFilterExtensions(packageFilterExt);
				if (selDestination != "")
					fileDialog.setFileName(selDestination);
				if (fileDialog.open() != null) {
					String separator = "";
					int length = fileDialog.getFilterPath().trim().length();
					if (length > 0
							&& fileDialog.getFilterPath().charAt(length - 1) != File.separatorChar)
						separator = File.separator;
					txtDestination.setText(new Path(fileDialog.getFilterPath()
							+ separator + fileDialog.getFileName())
							.toOSString());
				}
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}

		btnDestBrowse = new Button(container, SWT.NONE);
		initializeDialogUnits(btnDestBrowse);
		setButtonLayoutData(btnDestBrowse);
		btnDestBrowse
				.setText(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_btnDestBrowse_Text);
		btnDestBrowse.addSelectionListener(new OpenDestination());

		Group grpSigning = new Group(container, SWT.NONE);
		grpSigning.setText(WizardMessages.New_Package_Group_Signing_Title);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		grpSigning.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 5;
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		grpSigning.setLayout(layout);

		btnNoSign = new Button(grpSigning, SWT.RADIO);
		btnNoSign.setText(WizardMessages.New_Package_NoSign_Text);
		btnNoSign.setSelection(true);
		gd = new GridData();
		gd.horizontalSpan = 4;
		btnNoSign.setLayoutData(gd);
		btnNoSign.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pageChanged();
				updateStates();
			}
		});
		btnNoSign.addListener(SWT.Selection, this);

		btnSign = new Button(grpSigning, SWT.RADIO);
		btnSign.setText(WizardMessages.New_Package_Sign_Text);
		gd = new GridData();
		gd.horizontalSpan = 4;
		btnSign.setLayoutData(gd);
		btnSign.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pageChanged();
				updateStates();
			}
		});
		btnSign.addListener(SWT.Selection, this);

		container2 = new Composite(grpSigning, SWT.NULL);
		layout = new GridLayout();
		container2.setLayout(layout);
		layout.numColumns = 5;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 5;
		container2.setLayoutData(gd);

		lblKeyPair = new Label(container2, SWT.NONE);
		lblKeyPair.setText(WizardMessages.New_Package_lblKeyPair_Text);

		cboKeyPairs = new Combo(container2, SWT.READ_ONLY | SWT.BORDER
				| SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		cboKeyPairs.setLayoutData(gd);
		cboKeyPairs.setVisibleItemCount(10);
		cboKeyPairs.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateStates();
				pageChanged();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		cboKeyPairs.addListener(SWT.Selection, this);

		class OpenKeyPairs implements SelectionListener {
			public void widgetSelected(SelectionEvent event) {
				int index = cboKeyPairs.getSelectionIndex();
				if (index >= 0) {
					selectedKeyPair = cboKeyPairs.getItem(index);
				}
				KeyPairsAction actionKeyPairs = new KeyPairsAction(
						selectedKeyPair, context);
				actionKeyPairs.run();
				selectedKeyPair = actionKeyPairs.getSelectedKeyPair();
				actionKeyPairs.dispose();
				listKeyPairs();
				updateStates();
				pageChanged();
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}

		btnModify = new Button(container2, SWT.NONE);
		initializeDialogUnits(btnModify);
		setButtonLayoutData(btnModify);
		btnModify.setText(WizardMessages.New_Package_btnModify_Text);
		btnModify.addSelectionListener(new OpenKeyPairs());

		listKeyPairs();
		pageChanged();
		updateStates();

		setControl(container);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent.getShell(),
				getHelpContextId());
	}

	/**
	 * Lists all keypairs and selects the default one
	 */
	private void listKeyPairs() {
		cboKeyPairs.removeAll();
		try {
			KeyPair[] keyPairs = KeyPair.getKeyPairs();
			if (keyPairs != null && keyPairs.length > 0) {
				for (int i = 0; i < keyPairs.length; i++) {
					cboKeyPairs.add(keyPairs[i].getName());
					if (keyPairs[i].isDefault()) {
						selectedKeyPair = keyPairs[i].getName();
					}
				}
				int index = Math.max(0, selectedKeyPair == null ? 0
						: cboKeyPairs.indexOf(selectedKeyPair));
				cboKeyPairs.select(index);
			}
		} catch (Exception e) {
			Activator.error(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.screen.ui.wizards.AbstractNewPackagePage#updateDefaultStates
	 * ()
	 */
	public void updateDefaultStates() {
		themeName = ((String) context.getAttribute(PackagingAttribute.themeName
				.name())).trim();
		String destination = txtDestination.getText().trim();
		String ext = ".sis";
		Path path = new Path(destination);
		File file = new File(destination);
		if (destination.length() > 1) {
			if (!path.isEmpty() && destination.substring(1, 2).equals(":")) {
				if (file.isDirectory()) {
					destination = destination + File.separator + themeName
							+ ext;
					destination = destination.replace('/', '\\').replace(
							"\\\\", "\\");
				} else {
					destination = file.getParent() + File.separator + themeName
							+ ext;
					destination = destination.replace('/', '\\').replace(
							"\\\\", "\\");
				}
				txtDestination.setText(destination);
			} else
				setDefaultDestination();
		} else {
			setDefaultDestination();
		}

		IPlatform platform = (IPlatform) context
				.getAttribute(PackagingAttribute.platform.name());
		if (Packager.isSigningRequired(platform)) {
			btnSign.setSelection(true);
			btnNoSign.setSelection(false);
		} else {
			btnSign.setSelection(false);
			btnNoSign.setSelection(true);
		}
		pageChanged();
		updateStates();
		txtDestination.setSelection(txtDestination.getText().length(),
				txtDestination.getText().length());
	}

	private void setDefaultDestination() {
		if (themeName != "")
			txtDestination.setText(currentProjectPath + File.separator
					+ themeName + ".sis");
		else
			txtDestination.setText(currentProjectPath + File.separator
					+ currentProjectName + ".sis");
	}

	private void showPassword(boolean isVisible) {
		if (isVisible && lblPassword == null) {
			lblPassword = new Label(container2, SWT.NONE);
			lblPassword.setText(WizardMessages.New_Package_lblPassword_Text);
			lblPassword.setLayoutData(new GridData());

			txtPassword = new Text(container2, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			txtPassword.setLayoutData(gd);
			txtPassword.setEchoChar('*');
			txtPassword.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					pageChanged();
					updateStates();
				}
			});

			// Dummy label to fill a column
			label1 = new Label(container2, SWT.NONE);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.widthHint = 200;
			label1.setLayoutData(gd);
		} else if (!isVisible && lblPassword != null) {
			lblPassword.dispose();
			txtPassword.dispose();
			label1.dispose();
			lblPassword = null;
			txtPassword = null;
			label1 = null;
		}
		container.layout();
	}

	/**
	 * Handles enabled/disabled state logic of components
	 */
	private void updateStates() {
		if (btnNoSign.getSelection() == true) {
			lblKeyPair.setEnabled(false);
			cboKeyPairs.setEnabled(false);
			btnModify.setEnabled(false);
			showPassword(false);
		} else {
			lblKeyPair.setEnabled(true);
			cboKeyPairs.setEnabled(true);
			btnModify.setEnabled(true);
			if (cboKeyPairs.getText() != "") {
				try {
					KeyPair[] keyPairs = KeyPair.getKeyPairs();
					KeyPair selectedKeyPair = keyPairs[cboKeyPairs.getSelectionIndex()];
					
					if (selectedKeyPair.isSavePassword() == false
							/*
					 * && ("".equals(keyPairs[cboKeyPairs
					 * .getSelectionIndex()].getPassword()))
					 */) {
						showPassword(true);
						txtPassword.setFocus();
						if (selectedKeyPair.equals(txtPassword.getText())) {
							warn("Please enter the correct password.");
							setPageComplete(true);
						} else {
							warn("Please enter the correct password.");
						}
					} else {
						showPassword(false);
					}
				} catch (Exception e) {
					Activator.error(e);
				}
			} else {
				showPassword(false);
			}
		}
		updateModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.screen.ui.wizards.AbstractNewPackagePage#pageChanged()
	 */
	protected void pageChanged() {
		error(null);
		warn(null);

		if ((selDestination = selDestination.trim()).length() == 0) {
			error(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_No_File_Error);
			return;
		}

		Path path = new Path(selDestination);

		File file1 = path.removeLastSegments(1).toFile();
		File file2 = path.removeLastSegments(0).toFile();

		String ext = path.getFileExtension();

		if (ext == null || (!ext.equalsIgnoreCase("sis"))) {
			error(WizardMessages.New_Package_Extension_Error);
			return;
		}

		if (!file1.exists()) {
			error(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Not_Exist_Error);
			return;
		}

		if (ext == null)
			if (!file2.exists()) {
				error(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Not_Exist_Error);
				return;
			}

		if (!FileUtils.isFileValid(selDestination)) {
			error(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Wrong_Char_Error);
			return;
		}

		String fileName = path.lastSegment().toString();
		fileName = fileName
				.substring(0, (fileName.length() - 1) - ext.length());
		if (fileName.length() > FILE_NAME_MAXLEN) {
			error(com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Too_Long_Name_Error);
			return;
		}

		// if (selDestination.trim().length() > 0) {
		// String destination = selDestination.trim();
		// for (int i = 0; i < destination.length(); i++) {
		// if (destination.charAt(i) > 127) {
		// updateStatus(WizardMessages.New_Package_Wrong_Char_Error);
		// setPageComplete(false);
		// return;
		// }
		// }
		// }

		if (btnNoSign.getSelection() == true) {
			IPlatform platform = (IPlatform) context
					.getAttribute(PackagingAttribute.platform.name());
			if (Packager.isSigningRequired(platform))
				warn(WizardMessages.New_Package_NoSign_Warning_Text);
		}

		if (btnSign.getSelection() == true) {
			if (cboKeyPairs.getText() == "") {
				error(WizardMessages.New_Package_No_KeyPair_Error);
				return;
			}

			if (txtPassword != null && txtPassword.getVisible() == true
					&& txtPassword.getText() == "") {
				error(WizardMessages.New_Package_No_Password_Error);
				return;
			}
		}
		return;
	}

	/**
	 * Updates error messages in the banner area
	 */
	private void error(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	/**
	 * Updates warning message in the banner area
	 * 
	 * @param message
	 */
	private void warn(String message) {
		setMessage(message, WARNING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.wizards.AbstractNewPackagePage#updateModel()
	 */
	@Override
	protected void updateModel() {
		context = getContext();
		context.setAttribute(PackagingAttribute.sisFile.name(), txtDestination
				.getText());
		context.setAttribute(PackagingAttribute.signPackage.name(),
				new Boolean(btnSign.getSelection()).toString());
		if (btnSign.getSelection() && cboKeyPairs.getSelectionIndex() >= 0) {
			try {
				KeyPair keyPair = KeyPair.getKeyPair(cboKeyPairs
						.getItem(cboKeyPairs.getSelectionIndex()));
				if (keyPair != null) {
					context.setAttribute(PackagingAttribute.privateKeyFile
							.name(), keyPair.getPrivateKeyFile());
					context.setAttribute(PackagingAttribute.certificateFile
							.name(), keyPair.getCertificateFile());
					if (txtPassword != null && txtPassword.getVisible())
						context.setAttribute(PackagingAttribute.passphrase
								.name(), txtPassword.getText());
					else
						context.setAttribute(PackagingAttribute.passphrase
								.name(), keyPair.getPassword());
				}
			} catch (Exception e) {
				Activator.error(e);
			}
		} else {
			context.removeAttribute(PackagingAttribute.certificateFile.name());
			context.removeAttribute(PackagingAttribute.privateKeyFile.name());
			context.removeAttribute(PackagingAttribute.passphrase.name());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#performHelp()
	 */
	public void performHelp() {
		PlatformUI.getWorkbench().getHelpSystem().displayHelp(
				getHelpContextId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.wizards.AbstractNewPackagePage#performFinish()
	 */
	public boolean performFinish() {
		File file = new File(selDestination);
		IBrandingManager branding = BrandingExtensionManager
				.getBrandingManager();
		Image image = null;
		if (branding != null) {
			image = branding.getIconImageDescriptor().createImage();
		}

		// Check for password in keypairs
		KeyPair[] keyPairs = null;
		try {
			keyPairs = KeyPair.getKeyPairs();
		} catch (PackagingException e1) {
			e1.printStackTrace();
		}
		if (btnNoSign.getSelection() == false) {
			if (keyPairs.length > 0) {
				if (keyPairs[cboKeyPairs.getSelectionIndex()].getPassword() != null
						&& !(""
								.equals(keyPairs[cboKeyPairs
										.getSelectionIndex()].getPassword()
										.equals("")))) {
					if (txtPassword != null
							&& !(keyPairs[cboKeyPairs.getSelectionIndex()]
									.getPassword()
									.equals(txtPassword.getText()))) {
						MessageDialog dialog1 = new MessageDialog(
								PlatformUI.getWorkbench().getDisplay()
										.getActiveShell(),
								com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Mismatch_Password_Error_Message_Title,
								image,
								com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Mismatch_Password_Error_Message,
								1, new String[] { IDialogConstants.OK_LABEL },
								0);
						getShell().setCursor(
								new Cursor(getShell().getDisplay(),
										SWT.CURSOR_ARROW));
						dialog1.open();
						if (image != null) {
							image.dispose();
						}
						return false;
					}
				}
			}
		}
		// End key pairs password checks.

		if (file.exists()) {

			MessageDialog dialog = new MessageDialog(
					PlatformUI.getWorkbench().getDisplay().getActiveShell(),
					com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Package_Exist_MsgBox_Title,
					image,
					com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Package_Exist_MsgBox_Message,
					3, new String[] { IDialogConstants.YES_LABEL,
							IDialogConstants.NO_LABEL }, 0);
			int ret = dialog.open();
			if (image != null) {
				image.dispose();
			}
			if (ret != Window.OK) {
				return false;
			}
		}

		if (txtPassword != null) {
			getShell().setCursor(
					new Cursor(getShell().getDisplay(), SWT.CURSOR_WAIT));
			String cerFile = (String) context
					.getAttribute(PackagingAttribute.certificateFile.name());
			String keyFile = (String) context
					.getAttribute(PackagingAttribute.privateKeyFile.name());
			String password = (String) context
					.getAttribute(PackagingAttribute.passphrase.name());
			try {
				SymbianUtil.testKey(cerFile, keyFile, password, context);
			} catch (Exception e) {
				Activator.error(e);
				IBrandingManager branding1 = BrandingExtensionManager
						.getBrandingManager();
				Image image1 = null;
				if (branding1 != null) {
					image1 = branding1.getIconImageDescriptor().createImage();
				}
				MessageDialog dialog = new MessageDialog(
						PlatformUI.getWorkbench().getDisplay().getActiveShell(),
						com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Create_MsgBox_Error_Title,
						image1,
						com.nokia.tools.screen.ui.wizards.WizardMessages.New_Package_Create_MsgBox_Error_Message,
						1, new String[] { IDialogConstants.OK_LABEL }, 0);
				getShell().setCursor(
						new Cursor(getShell().getDisplay(), SWT.CURSOR_ARROW));
				dialog.open();
				if (image1 != null) {
					image1.dispose();
				}
				return false;
			}
			getShell().setCursor(
					new Cursor(getShell().getDisplay(), SWT.CURSOR_ARROW));
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.screen.ui.wizards.AbstractNewPackagePage#getSelectedPlatforms
	 * ()
	 */
	@Override
	protected IPlatform[] getSelectedPlatforms() {
		return new IPlatform[] { (IPlatform) context
				.getAttribute(PackagingAttribute.platform.name()) };
	}
}
