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
package com.nokia.tools.theme.s60.ui.dialogs;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.screen.ui.branding.ISharedImageDescriptor;
import com.nokia.tools.screen.ui.dialogs.WizardMessages;
import com.nokia.tools.theme.s60.packaging.util.DistinguishedNames;
import com.nokia.tools.theme.s60.packaging.util.SymbianUtil;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;
import com.nokia.tools.ui.branding.util.BrandedTitleAreaDialog;
import com.nokia.tools.ui.dialog.MessageDialogWithTextContent;

/**
 * This dialog allows creating private key and certificate files used in key
 * pairs creation for signing during a package creation process.
 * 
 */
public class MakeKeysDialog extends BrandedTitleAreaDialog implements Listener {

	Shell shell;

	public static final String MAKE_KEYS_DIALOG_CONTEXT = "com.nokia.tools.s60.ide.makeKeysDialog_context"; //$NON-NLS-1$

	private static final int INFORMATION_TEXT_LIMIT = 128;

	private static final int KEYLENGTH_DEFAULT = 1024;

	private static final int KEY_INCREMENT = 8;

	private static final int KEY_PAGE_INCREMENT = 256;

	private static final String DEFAULT_KEY_FILE_NAME = "keyfile";

	private static final String DEFAULT_CERTIFICATE_FILE_NAME = "cerfile";

	private static final String[] KEY_FILTER_EXT = { "*.key" };

	private static final String[] CERTIFICATE_FILTER_EXT = { "*.cer" };

	private static final String DEFAULT_FOLDER = System
			.getProperty("user.home");

	private Image labelImage, labelImage2;

	private Font infoFont;

	private Label lblConfirmPassword, lblYears;

	private Text txtName, txtUnit, txtOrganization, txtCountry, txtEmail,
			txtKeyDestination, txtCerDestination, txtPassword,
			txtConfirmPassword;

	private Spinner spnKeyLength, spnValidityTime;

	private Button btnGenerate, btnKeyBrowse, btnCerBrowse;

	private String strName = "";

	private String strUnit = "";

	private String strOrganization = "";

	private String strCountry = "";

	private String strEmail = "";

	private String strKeyDestination = "";

	private String strCerDestination = "";

	private String strPassword = "";

	private String strConfirmPassword = "";

	private Boolean dialogComplete = false;

	private int keyLength = KEYLENGTH_DEFAULT;

	private Composite warningContainer;

	private int FILE_NAME_MAXLEN = 256;

	private static final int VALID_MIN = 1;

	private static final int VALID_DEFAULT = 5;

	private static final int VALID_MAX = 20;

	private static final int VALID_INCREMENT = 1;

	private static final int VALID_PAGE_INCREMENT = 2;

	private int validityTime = VALID_DEFAULT;

	public MakeKeysDialog(Shell parentShell) {
		super(parentShell);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.wizards.BrandedTitleAreaDialog#getBannerIconDescriptor()
	 */
	@Override
	protected ImageDescriptor getBannerIconDescriptor() {
		return UiPlugin.getImageDescriptor("icons/wizban/make_keys.png");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.wizards.BrandedTitleAreaDialog#getTitle()
	 */
	@Override
	protected String getTitle() {
		return WizardMessages.Make_Keys_Title;
	}

	protected Control createDialogArea(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent.getParent(),
				MakeKeysDialog.MAKE_KEYS_DIALOG_CONTEXT);

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		setTitle(WizardMessages.Make_Keys_Banner_Title);
		setMessage(WizardMessages.Make_Keys_Banner_Message);

		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.marginHeight = 13;
		layout.marginWidth = 13;
		layout.verticalSpacing = 7;

		FontData fd = JFaceResources.getDefaultFont().getFontData()[0];
		infoFont = new Font(Display.getDefault(), fd.getName(), 7, SWT.ITALIC);

		Composite topContainer = new Composite(container, SWT.NULL);
		layout = new GridLayout();
		topContainer.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		topContainer.setLayoutData(gd);
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		layout.horizontalSpacing = 10;

		Group grpInformation = new Group(topContainer, SWT.NONE);
		grpInformation
				.setText(WizardMessages.Make_Keys_Group_Information_Title);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		grpInformation.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginWidth = 9;
		layout.marginTop = 5;
		layout.marginBottom = 2;
		layout.verticalSpacing = 7;
		grpInformation.setLayout(layout);

		Label lblName = new Label(grpInformation, SWT.NONE);
		lblName.setText(WizardMessages.Make_Keys_lblName_Text);

		txtName = new Text(grpInformation, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		txtName.setLayoutData(gd);
		txtName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				strName = txtName.getText();
				dialogChanged();
			}
		});
		txtName.setTextLimit(INFORMATION_TEXT_LIMIT);
		txtName.addListener(SWT.Modify, this);

		Label lblNameExample = new Label(grpInformation, SWT.NONE);
		lblNameExample.setText(WizardMessages.Make_Keys_lblExampleName_Text);
		lblNameExample.setFont(infoFont);

		Label lblUnit = new Label(grpInformation, SWT.NONE);
		lblUnit.setText(WizardMessages.Make_Keys_lblUnit_Text);

		txtUnit = new Text(grpInformation, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		txtUnit.setLayoutData(gd);
		txtUnit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				strUnit = txtUnit.getText();
				dialogChanged();
			}
		});
		txtUnit.setTextLimit(INFORMATION_TEXT_LIMIT);
		txtUnit.addListener(SWT.Modify, this);

		Label lblUnitExample = new Label(grpInformation, SWT.NONE);
		lblUnitExample.setText(WizardMessages.Make_Keys_lblExampleUnit_Text);
		lblUnitExample.setFont(infoFont);

		Label lblOrganization = new Label(grpInformation, SWT.NONE);
		lblOrganization.setText(WizardMessages.Make_Keys_lblOrganization_Text);

		txtOrganization = new Text(grpInformation, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		txtOrganization.setLayoutData(gd);
		txtOrganization.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				strOrganization = txtOrganization.getText();
				dialogChanged();
			}
		});
		txtOrganization.setTextLimit(INFORMATION_TEXT_LIMIT);
		txtOrganization.addListener(SWT.Modify, this);

		Label lblOrganizationExample = new Label(grpInformation, SWT.NONE);
		lblOrganizationExample
				.setText(WizardMessages.Make_Keys_lblExampleOrganization_Text);
		lblOrganizationExample.setFont(infoFont);

		Label lblCountry = new Label(grpInformation, SWT.NONE);
		lblCountry.setText(WizardMessages.Make_Keys_lblCountry_Text);

		txtCountry = new Text(grpInformation, SWT.BORDER);
		gd = new GridData();
		gd.widthHint = 20;
		txtCountry.setLayoutData(gd);
		txtCountry.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				strCountry = txtCountry.getText();
				dialogChanged();
			}
		});
		txtCountry.setTextLimit(2);
		txtCountry.addListener(SWT.Modify, this);

		Label lblCountryExample = new Label(grpInformation, SWT.NONE);
		lblCountryExample
				.setText(WizardMessages.Make_Keys_lblExampleCountry_Text);
		lblCountryExample.setFont(infoFont);

		Label lblEmail = new Label(grpInformation, SWT.NONE);
		lblEmail.setText(WizardMessages.Make_Keys_lblEmail_Text);

		txtEmail = new Text(grpInformation, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		txtEmail.setLayoutData(gd);
		txtEmail.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				strEmail = txtEmail.getText();
				dialogChanged();
			}
		});
		txtEmail.setTextLimit(INFORMATION_TEXT_LIMIT);
		txtEmail.addListener(SWT.Modify, this);

		Label lblEmailExample = new Label(grpInformation, SWT.NONE);
		lblEmailExample.setText(WizardMessages.Make_Keys_lblExampleEmail_Text);
		lblEmailExample.setFont(infoFont);

		Group grpParameters = new Group(topContainer, SWT.NONE);
		grpParameters.setText(WizardMessages.Make_Keys_Group_Parameters_Title);
		gd = new GridData(GridData.FILL_VERTICAL);
		grpParameters.setLayoutData(gd);
		layout = new GridLayout();
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		grpParameters.setLayout(layout);

		Composite lengthContainer = new Composite(grpParameters, SWT.NONE);
		layout = new GridLayout();
		lengthContainer.setLayout(layout);
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalAlignment = SWT.BOTTOM;
		lengthContainer.setLayoutData(gd);
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Label lblLength = new Label(lengthContainer, SWT.NONE);
		lblLength.setText(WizardMessages.Make_Keys_lblKeyLength_Text);

		spnKeyLength = new Spinner(lengthContainer, SWT.BORDER);
		gd = new GridData();
		gd.widthHint = 25;
		spnKeyLength.setLayoutData(gd);
		spnKeyLength.setMaximum(SymbianUtil.MAX_KEY_LENGTH);
		spnKeyLength.setMinimum(SymbianUtil.MIN_KEY_LENGTH);
		spnKeyLength.setPageIncrement(KEY_PAGE_INCREMENT);
		spnKeyLength.setIncrement(KEY_INCREMENT);
		spnKeyLength.setSelection(keyLength);
		spnKeyLength.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				keyLength = spnKeyLength.getSelection();
				dialogChanged();
			}
		});

		spnKeyLength.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}

			public void widgetSelected(SelectionEvent e) {
				if (keyLength % KEY_INCREMENT != 0) {
					int valueBefore = (Math.round(keyLength / KEY_INCREMENT))
							* KEY_INCREMENT;
					int valueNext = valueBefore + KEY_INCREMENT;
					if (keyLength - valueBefore < valueNext - keyLength)
						spnKeyLength.setSelection(valueBefore);
					else
						spnKeyLength.setSelection(valueNext);
				}
			}
		});
		spnKeyLength.addListener(SWT.Modify, this);

		Label lblBits = new Label(lengthContainer, SWT.NONE);
		lblBits.setText(WizardMessages.Make_Keys_lblKeyBits_Text);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		lblBits.setLayoutData(gd);

		Composite infoContainer = new Composite(grpParameters, SWT.NONE);
		layout = new GridLayout();
		infoContainer.setLayout(layout);
		gd = new GridData(GridData.FILL_BOTH);
		infoContainer.setLayoutData(gd);
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Label lblInfoImage = new Label(infoContainer, SWT.NONE);
		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		lblInfoImage.setLayoutData(gd);
		labelImage = ISharedImageDescriptor.ICON16_INFO.createImage();
		lblInfoImage.setImage(labelImage);

		Label lblInfoText = new Label(infoContainer, SWT.WRAP);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 20;
		gd.heightHint = 67;
		lblInfoText.setLayoutData(gd);
		lblInfoText.setText(WizardMessages.Make_Keys_lblInfoText_Text);

		Composite validityContainer = new Composite(grpParameters, SWT.NONE);
		layout = new GridLayout();
		validityContainer.setLayout(layout);
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalAlignment = SWT.BOTTOM;
		validityContainer.setLayoutData(gd);
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Label lblValid = new Label(validityContainer, SWT.NONE);
		lblValid.setText(WizardMessages.Make_Keys_lblValidText_Text);

		spnValidityTime = new Spinner(validityContainer, SWT.BORDER);
		gd = new GridData();
		gd.widthHint = 15;
		spnValidityTime.setLayoutData(gd);
		spnValidityTime.setMaximum(VALID_MAX);
		spnValidityTime.setMinimum(VALID_MIN);
		spnValidityTime.setPageIncrement(VALID_PAGE_INCREMENT);
		spnValidityTime.setIncrement(VALID_INCREMENT);
		spnValidityTime.setSelection(VALID_DEFAULT);
		spnValidityTime.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validityTime = spnValidityTime.getSelection();
				if (validityTime == 1)
					lblYears.setText(WizardMessages.Make_Keys_lblYearText_Text);
				else
					lblYears
							.setText(WizardMessages.Make_Keys_lblYearsText_Text);
				dialogChanged();
			}
		});

		spnValidityTime.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}

			public void widgetSelected(SelectionEvent e) {
				if (validityTime > VALID_MAX)
					spnValidityTime.setSelection(VALID_MAX);
				if (validityTime < VALID_MIN)
					spnValidityTime.setSelection(VALID_MIN);
			}
		});
		spnValidityTime.addListener(SWT.Modify, this);

		lblYears = new Label(validityContainer, SWT.NONE);
		lblYears.setText(WizardMessages.Make_Keys_lblYearsText_Text);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		lblYears.setLayoutData(gd);

		Group grpDestination = new Group(container, SWT.NONE);
		grpDestination
				.setText(WizardMessages.Make_Keys_Group_Destination_Title);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 459;
		grpDestination.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 5;
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		grpDestination.setLayout(layout);

		Label lblKeyDestination = new Label(grpDestination, SWT.NONE);
		lblKeyDestination
				.setText(WizardMessages.Make_Keys_lblKeyDestination_Text);

		txtKeyDestination = new Text(grpDestination, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		txtKeyDestination.setLayoutData(gd);
		txtKeyDestination.setText(DEFAULT_FOLDER + File.separator
				+ DEFAULT_KEY_FILE_NAME + ".key");
		txtKeyDestination.addListener(SWT.Modify, this);
		txtKeyDestination.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				strKeyDestination = txtKeyDestination.getText();
				dialogChanged();
			}
		});
		strKeyDestination = txtKeyDestination.getText();

		class OpenKeyDestination implements SelectionListener {
			public void widgetSelected(SelectionEvent event) {
				FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
				fileDialog
						.setText(WizardMessages.Make_Keys_KeyFileDialog_Title);
				fileDialog.setFilterPath(ResourcesPlugin.getWorkspace()
						.getRoot().getLocation().toOSString());
				fileDialog.setFilterExtensions(KEY_FILTER_EXT);
				if (strKeyDestination != "")
					fileDialog.setFileName(strKeyDestination);
				if (fileDialog.open() != null) {
					String separator = "";
					int length = fileDialog.getFilterPath().trim().length();
					if (length > 0
							&& fileDialog.getFilterPath().charAt(length - 1) != File.separatorChar)
						separator = File.separator;
					strKeyDestination = new Path(fileDialog.getFilterPath()
							+ separator + fileDialog.getFileName())
							.toOSString();
					txtKeyDestination.setText(strKeyDestination);
				}
				updateStates();
				txtKeyDestination.setFocus();
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}

		btnKeyBrowse = new Button(grpDestination, SWT.NONE);
		initializeDialogUnits(btnKeyBrowse);
		setButtonLayoutData(btnKeyBrowse);
		btnKeyBrowse.setText(WizardMessages.Make_Keys_btnKeyBrowse_Text);
		btnKeyBrowse.addSelectionListener(new OpenKeyDestination());

		Label lblCerDestination = new Label(grpDestination, SWT.NONE);
		lblCerDestination
				.setText(WizardMessages.Make_Keys_lblCerDestination_Text);

		txtCerDestination = new Text(grpDestination, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		txtCerDestination.setLayoutData(gd);
		txtCerDestination.setText(DEFAULT_FOLDER + File.separator
				+ DEFAULT_CERTIFICATE_FILE_NAME + ".cer");
		txtCerDestination.addListener(SWT.Modify, this);
		txtCerDestination.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				strCerDestination = txtCerDestination.getText();
				dialogChanged();
			}
		});
		strCerDestination = txtCerDestination.getText();

		class OpenCerDestination implements SelectionListener {
			public void widgetSelected(SelectionEvent event) {
				FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
				fileDialog
						.setText(WizardMessages.Make_Keys_CerFileDialog_Title);
				fileDialog.setFilterPath(ResourcesPlugin.getWorkspace()
						.getRoot().getLocation().toOSString());
				fileDialog.setFilterExtensions(CERTIFICATE_FILTER_EXT);
				if (strCerDestination != "")
					fileDialog.setFileName(strCerDestination);
				if (fileDialog.open() != null) {
					String separator = "";
					int length = fileDialog.getFilterPath().trim().length();
					if (length > 0
							&& fileDialog.getFilterPath().charAt(length - 1) != File.separatorChar)
						separator = File.separator;
					strCerDestination = new Path(fileDialog.getFilterPath()
							+ separator + fileDialog.getFileName())
							.toOSString();
					txtCerDestination.setText(strCerDestination);
				}
				txtCerDestination.setFocus();
				updateStates();
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}

		btnCerBrowse = new Button(grpDestination, SWT.NONE);
		initializeDialogUnits(btnCerBrowse);
		setButtonLayoutData(btnCerBrowse);
		btnCerBrowse.setText(WizardMessages.Make_Keys_btnCerBrowse_Text);
		btnCerBrowse.addSelectionListener(new OpenCerDestination());

		Label lblPassword = new Label(grpDestination, SWT.NONE);
		lblPassword.setText(WizardMessages.Make_Keys_lblPassword_Text);

		txtPassword = new Text(grpDestination, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		txtPassword.setLayoutData(gd);
		txtPassword.setEchoChar('*');
		txtPassword.addListener(SWT.Modify, this);
		txtPassword.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				strPassword = txtPassword.getText();
				if (txtPassword.getText().trim().length() < 4)
					txtConfirmPassword.setText("");
				dialogChanged();
			}
		});

		warningContainer = new Composite(grpDestination, SWT.NULL);
		layout = new GridLayout();
		warningContainer.setLayout(layout);
		gd = new GridData(GridData.FILL_VERTICAL);
		gd.horizontalSpan = 2;
		gd.verticalSpan = 2;
		warningContainer.setLayoutData(gd);
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		if (new File(DEFAULT_FOLDER + File.separator + DEFAULT_KEY_FILE_NAME
				+ ".key").exists())
			warningContainer.setVisible(true);
		else
			warningContainer.setVisible(false);

		Label lblInfo2Image = new Label(warningContainer, SWT.NONE);
		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		lblInfo2Image.setLayoutData(gd);
		labelImage2 = ISharedImageDescriptor.ICON16_WARNING.createImage();
		lblInfo2Image.setImage(labelImage2);

		Label lblInfo2Text = new Label(warningContainer, SWT.WRAP);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 110;
		lblInfo2Text.setLayoutData(gd);
		lblInfo2Text.setText(WizardMessages.Make_Keys_lblInfo2Text_Text);

		// Dummy label to fill a column
		new Label(grpDestination, SWT.NONE);

		lblConfirmPassword = new Label(grpDestination, SWT.NONE);
		lblConfirmPassword
				.setText(WizardMessages.Make_Keys_lblConfirmPassword_Text);
		lblConfirmPassword.setEnabled(false);

		txtConfirmPassword = new Text(grpDestination, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		txtConfirmPassword.setLayoutData(gd);
		txtConfirmPassword.setEchoChar('*');
		txtConfirmPassword.setEnabled(false);
		txtConfirmPassword.addListener(SWT.Modify, this);
		txtConfirmPassword.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				strConfirmPassword = txtConfirmPassword.getText();
				dialogChanged();
			}
		});

		Composite container3 = new Composite(area, SWT.NONE);
		container3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout = new GridLayout();
		container3.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;

		final Label separator2 = new Label(container3, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		separator2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		shell = this.getShell();

		return area;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		btnGenerate = createButton(parent, IDialogConstants.OK_ID,
				WizardMessages.Make_Keys_btnGenerate_Text, true);
		btnGenerate.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	protected void generateKeyPair() throws Exception {
		String organization = txtOrganization.getText();
		String unit = txtUnit.getText();
		String country = txtCountry.getText();
		String email = txtEmail.getText();

		int expDays = validityTime * 365;

		DistinguishedNames names = new DistinguishedNames(strName);
		if ((organization = organization.trim()).length() > 0) {
			names.setName(DistinguishedNames.ORGANIZATION, organization);
		}
		if ((unit = unit.trim()).length() > 0) {
			names.setName(DistinguishedNames.ORGANIZATION_UNIT, unit);
		}
		if ((country = country.trim()).length() > 0) {
			names.setName(DistinguishedNames.COUNTRY, country);
		}
		if ((email = email.trim()).length() > 0) {
			names.setName(DistinguishedNames.EMAIL, email);
		}

		SymbianUtil.generateKeyPair(names, keyLength, expDays, strPassword,
				new File(strKeyDestination), new File(strCerDestination));
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			File cerFile = new File(strCerDestination);
			if (cerFile.exists()) {
				IBrandingManager branding = BrandingExtensionManager
						.getBrandingManager();
				Image image = null;
				if (branding != null) {
					image = branding.getIconImageDescriptor().createImage();
				}
				MessageDialog dialog = new MessageDialog(PlatformUI
						.getWorkbench().getDisplay().getActiveShell(),
						WizardMessages.Make_Keys_CerFile_Exist_MsgBox_Title,
						image,
						WizardMessages.Make_Keys_CerFile_Exist_MsgBox_Message,
						3, new String[] { IDialogConstants.YES_LABEL,
								IDialogConstants.NO_LABEL }, 0);
				int ret = dialog.open();
				if (image != null) {
					image.dispose();
				}
				if (ret != Window.OK) {
					return;
				}
			}

			shell.setEnabled(false);
			shell.setCursor(new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT));
			try {
				generateKeyPair();
			} catch (Exception e) {
				shell.setEnabled(true);
				shell
						.setCursor(new Cursor(shell.getDisplay(),
								SWT.CURSOR_ARROW));
				String message = null;
				if (e instanceof PackagingException) {
					message = ((PackagingException) e).getDetails();
					if (message != null) {
						MessageDialogWithTextContent
								.openError(
										getShell(),
										WizardMessages.Key_Pairs_Generate_Error_Title,
										WizardMessages.Key_Pairs_Generate_Error_Message,
										message);
					}
				}
				if (message == null) {
					MessageDialog.openError(getShell(),
							WizardMessages.Key_Pairs_Generate_Error_Title, e
									.getMessage());
				}
				return;
			}
			shell.setEnabled(true);
			shell.setCursor(new Cursor(shell.getDisplay(), SWT.CURSOR_ARROW));
		}

		if (buttonId == IDialogConstants.CANCEL_ID) {
			setMessage(null);
			this.close();
		}

		super.buttonPressed(buttonId);
	}

	@Override
	public boolean close() {
		if (labelImage != null) {
			labelImage.dispose();
		}
		if (labelImage2 != null) {
			labelImage2.dispose();
		}
		if (infoFont != null) {
			infoFont.dispose();
		}
		return super.close();
	}

	/**
	 * Validates the input and offers error messages
	 */
	private void dialogChanged() {
		if (strName.trim().length() == 0) {
			updateStatus(WizardMessages.Make_Keys_Name_Not_Exist_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (strUnit.trim().length() == 0
				&& strOrganization.trim().length() == 0
				&& strCountry.trim().length() == 0
				&& strEmail.trim().length() == 0) {
			updateStatus(WizardMessages.Make_Keys_Not_Enough_Attributes_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (strCountry.trim().length() > 0 && strCountry.trim().length() < 2){
			updateStatus(WizardMessages.Make_Keys_Country_Too_Short);
			dialogComplete = false;
			updateStates();
			return;
		}
		
		if (strEmail.trim().length() > 0) {
			Pattern p = Pattern
					.compile("(^[\\w]+\\+?\\w*(\\.[\\w]+)*@[\\w]{2,}\\.[\\w]{2,}(\\.[\\w]{2,})*$)");
			Matcher m = p.matcher(strEmail);
			if (!m.matches()) {
				updateStatus(WizardMessages.Make_Keys_Email_Wrong_Format_Error);
				dialogComplete = false;
				updateStates();
				return;
			}
		}

		if (strKeyDestination.trim().length() == 0) {
			updateStatus(WizardMessages.Make_Keys_No_Key_File_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (strCerDestination.trim().length() == 0) {
			updateStatus(WizardMessages.Make_Keys_No_Cer_File_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		Path path1 = new Path(strKeyDestination);
		File file1_1 = path1.removeLastSegments(1).toFile();
		File file1_2 = path1.removeLastSegments(0).toFile();
		String ext1 = path1.getFileExtension();

		Path path2 = new Path(strCerDestination);
		File file2_1 = path2.removeLastSegments(1).toFile();
		File file2_2 = path2.removeLastSegments(0).toFile();
		String ext2 = path2.getFileExtension();

		if (ext1 == null || (!ext1.equalsIgnoreCase("key"))) {
			updateStatus(WizardMessages.Make_Keys_Key_Extension_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (!FileUtils.isFileValid(file1_2)) {
			updateStatus(WizardMessages.Make_Keys_Key_Wrong_Char_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (!file1_1.canRead() || !file1_1.isDirectory()) {
			updateStatus(WizardMessages.Make_Keys_Key_Path_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (file1_2.isDirectory()) {
			updateStatus(WizardMessages.Make_Keys_Key_Directory_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		String keyFileName = path1.lastSegment().toString();
		keyFileName = keyFileName.substring(0, (keyFileName.length() - 1)
				- ext1.length());
		if (keyFileName.length() > FILE_NAME_MAXLEN) {
			updateStatus(WizardMessages.Make_Keys_Key_Name_Too_Long_Error);
			dialogComplete = false;
			return;
		}

		if (ext2 == null || (!ext2.equalsIgnoreCase("cer"))) {
			updateStatus(WizardMessages.Make_Keys_Cer_Extension_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (!FileUtils.isFileValid(file2_2)) {
			updateStatus(WizardMessages.Make_Keys_Cer_Wrong_Char_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (!file2_1.canRead() || !file2_1.isDirectory()) {
			updateStatus(WizardMessages.Make_Keys_Cer_Path_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (file2_2.isDirectory()) {
			updateStatus(WizardMessages.Make_Keys_Cer_Directory_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		String cerFileName = path2.lastSegment().toString();
		cerFileName = cerFileName.substring(0, (cerFileName.length() - 1)
				- ext2.length());
		if (cerFileName.length() > FILE_NAME_MAXLEN) {
			updateStatus(WizardMessages.Make_Keys_Cer_Name_Too_Long_Error);
			dialogComplete = false;
			return;
		}

		if (strPassword.trim().length() != 0
				&& strPassword.trim().length() < SymbianUtil.MIN_PASSWORD_LENGTH) {
			updateStatus(WizardMessages.Make_Keys_Password_Length_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (strPassword.trim().length() != 0
				&& strConfirmPassword.trim().length() == 0) {
			updateStatus(WizardMessages.Make_Keys_Password_Not_Exist_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (strConfirmPassword.trim().length() != 0
				&& (!strConfirmPassword.equals(strPassword))) {
			updateStatus(WizardMessages.Make_Keys_Passwords_Not_Matching_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		dialogComplete = true;
		updateStatus(null);
		updateStates();
	}

	/**
	 * Handles enabled/disabled state logic of components
	 */
	private void updateStates() {
		File keyFile = new File(strKeyDestination);
		Path keyPath = new Path(strKeyDestination);
		String keyExt = keyPath.getFileExtension();

		if (keyExt != null && keyFile.exists()
				&& keyExt.equalsIgnoreCase("key"))
			warningContainer.setVisible(true);
		else
			warningContainer.setVisible(false);
		if (dialogComplete)
			btnGenerate.setEnabled(true);
		else
			btnGenerate.setEnabled(false);
		if (txtPassword.getText().trim().length() > 3) {
			lblConfirmPassword.setEnabled(true);
			txtConfirmPassword.setEnabled(true);
		} else {
			txtConfirmPassword.setEnabled(false);
			lblConfirmPassword.setEnabled(false);
		}
	}

	/**
	 * Updates error messages in the banner area
	 */
	private void updateStatus(String message) {
		setErrorMessage(message);
	}

	public void handleEvent(Event event) {

	}

	/**
	 * @return Full path for private key file
	 */
	public String getKeyDestination() {
		return strKeyDestination;
	}

	/**
	 * @return Full path for certificate file
	 */
	public String getCerDestination() {
		return strCerDestination;
	}

	/**
	 * @return Password to be used in key and certificate generation
	 */
	public String getPassword() {
		return strPassword;
	}
}