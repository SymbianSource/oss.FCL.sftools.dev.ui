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
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.packaging.IPackager;
import com.nokia.tools.packaging.KeyPair;
import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingContext;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.screen.ui.branding.ISharedImageDescriptor;
import com.nokia.tools.screen.ui.dialogs.WizardMessages;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.theme.s60.packaging.util.SymbianUtil;
import com.nokia.tools.theme.s60.ui.wizards.ModelPlatformMapping;
import com.nokia.tools.theme.ui.util.ThemeModelUtil;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;
import com.nokia.tools.ui.branding.util.BrandedTitleAreaDialog;

/**
 * This dialog allowes creating, saving, editing and deleting keypairs can be
 * used in signing during a package creation process.
 * 
 */
public class KeyPairsDialog extends BrandedTitleAreaDialog {

	public static final String KEY_PAIRS_DIALOG_CONTEXT = "com.nokia.tools.s60.ide.keyPairsDialog_context"; //$NON-NLS-1$

	private Boolean dialogComplete = false;

	private Boolean contentChanged = false;

	private String defaultKeyPair;

	private String strPairName = "";

	private String strSelKey = "";

	private String strSelCer = "";

	private String strPassword = "";

	private String strSelKeyPair = "";

	private int dialogMode = 1; // 0 = Modes off, 1 = showing mode, 2 = creating

	// mode, 3 = editing mode

	Shell shell;

	private Button btnKeyBrowse, btnCerBrowse, btnNew, btnEdit, btnSave,
			btnCancel, btnDelete, chkSavePassword, btnKeys;

	private List lstKeyPairs;

	private Label lblKeyPairs, lblKeyPairName, lblPublicKeyFile, lblCerFile,
			lblPassword, lblSavePasswordInfoImage, lblSavePasswordInfo;

	private Text txtKeyPairName, txtPrivateKeyFile, txtCerFile, txtPassword;

	private String[] keyFilterExt = { "*.key; *.pem" };

	private String[] cerFilterExt = { "*.cer; *.p7c; *.der" };

	private int TEXT_LIMIT = 50;

	private Image warningImage, infoImage;

	private Composite warningContainer;

	private PackagingContext context;

	/**
	 * The constructor
	 */
	public KeyPairsDialog(Shell parentShell, String defaultKeyPair,
			PackagingContext context) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.defaultKeyPair = defaultKeyPair;
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.s60.wizards.BrandedTitleAreaDialog#getBannerIconDescriptor
	 * ()
	 */
	@Override
	protected ImageDescriptor getBannerIconDescriptor() {
		return UiPlugin.getImageDescriptor("icons/wizban/handle_keys.png");
	}

	protected Control createDialogArea(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent.getParent(),
				KeyPairsDialog.KEY_PAIRS_DIALOG_CONTEXT);

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		setTitle(WizardMessages.Key_Pairs_Banner_Title);
		setMessage(WizardMessages.Key_Pairs_Banner_Message);

		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 5;
		layout.marginHeight = 13;
		layout.marginWidth = 13;
		layout.verticalSpacing = 7;

		lblKeyPairs = new Label(container, SWT.NONE);
		lblKeyPairs.setText(WizardMessages.Key_Pairs_lblKeyPairs_Text);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.verticalSpan = 2;
		lblKeyPairs.setLayoutData(gd);

		lstKeyPairs = new List(container, SWT.V_SCROLL | SWT.BORDER
				| SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		gd.verticalSpan = 2;
		gd.widthHint = 280;
		gd.heightHint = 77;
		lstKeyPairs.setLayoutData(gd);
		lstKeyPairs.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (lstKeyPairs.getSelectionIndex() != -1) {
					strSelKeyPair = lstKeyPairs.getItem(lstKeyPairs
							.getSelectionIndex());
					updateKeyPairContent(strSelKeyPair);
					updateStates();
				}
			}
		});

		btnEdit = new Button(container, SWT.NONE);
		initializeDialogUnits(btnEdit);
		setButtonLayoutData(btnEdit);
		btnEdit.setText(WizardMessages.Key_Pairs_btnEdit_Text);
		gd = new GridData(SWT.FILL, SWT.TOP, false, false);
		btnEdit.setLayoutData(gd);
		btnEdit.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				strSelKeyPair = lstKeyPairs.getItem(lstKeyPairs
						.getSelectionIndex());
				contentChanged = false;
				dialogMode = 3;
				updateStates();
				txtKeyPairName.setFocus();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnDelete = new Button(container, SWT.NONE);
		initializeDialogUnits(btnDelete);
		setButtonLayoutData(btnDelete);
		btnDelete.setText(WizardMessages.Key_Pairs_btnDelete_Text);
		gd = new GridData(SWT.FILL, SWT.TOP, false, false);
		btnDelete.setLayoutData(gd);
		btnDelete.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				IBrandingManager branding = BrandingExtensionManager
						.getBrandingManager();
				Image image = null;
				if (branding != null) {
					image = branding.getIconImageDescriptor().createImage();
				}
				MessageDialog dialog = new MessageDialog(PlatformUI
						.getWorkbench().getDisplay().getActiveShell(),
						WizardMessages.Key_Pairs_Delete_MsgBox_Title, image,
						WizardMessages.Key_Pairs_Delete_MsgBox_Message, 3,
						new String[] { IDialogConstants.YES_LABEL,
								IDialogConstants.NO_LABEL }, 0);
				if (dialog.open() == 0) {
					int index = lstKeyPairs.getSelectionIndex();
					strSelKeyPair = lstKeyPairs.getItem(lstKeyPairs
							.getSelectionIndex());
					deleteKeyPair(index, strSelKeyPair);
					updateKeyPairList();
					dialogMode = 1;
					if (lstKeyPairs.getItemCount() > 0) {
						lstKeyPairs.select(0);
						updateKeyPairContent(lstKeyPairs.getItem(lstKeyPairs
								.getSelectionIndex()));
						updateStates();
					} else {
						clearTextBoxes();
					}
					dialogChanged();
				}
				if (image != null) {
					image.dispose();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		final Label separator = new Label(container, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 5;
		separator.setLayoutData(gd);

		lblKeyPairName = new Label(container, SWT.NONE);
		lblKeyPairName.setText(WizardMessages.Key_Pairs_lblKeyPairName_Text);

		txtKeyPairName = new Text(container, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		gd.widthHint = 280;
		txtKeyPairName.setLayoutData(gd);
		txtKeyPairName.setTextLimit(TEXT_LIMIT);
		txtKeyPairName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				strPairName = txtKeyPairName.getText().trim();
				if (dialogMode == 3)
					contentChanged = true;
				dialogChanged();
			}
		});

		// Dummy label to fill a column
		new Label(container, SWT.NONE);

		lblPublicKeyFile = new Label(container, SWT.NONE);
		lblPublicKeyFile
				.setText(WizardMessages.Key_Pairs_lblPublicKeyFile_Text);

		txtPrivateKeyFile = new Text(container, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 282;
		gd.horizontalSpan = 3;
		txtPrivateKeyFile.setLayoutData(gd);
		txtPrivateKeyFile.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				strSelKey = txtPrivateKeyFile.getText();
				if (dialogMode == 3)
					contentChanged = true;
				dialogChanged();
			}
		});

		shell = this.getShell();
		class OpenKey implements SelectionListener {
			public void widgetSelected(SelectionEvent event) {
				FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
				fileDialog
						.setText(WizardMessages.Key_Pairs_keyFileDialog_Title);
				fileDialog.setFilterPath(ResourcesPlugin.getWorkspace()
						.getRoot().getLocation().toOSString());
				fileDialog.setFilterExtensions(keyFilterExt);
				if (strSelKey != "")
					fileDialog.setFileName(strSelKey);
				if (fileDialog.open() != null) {
					String separator = "";
					int length = fileDialog.getFilterPath().trim().length();
					if (length > 0
							&& fileDialog.getFilterPath().charAt(length - 1) != File.separatorChar)
						separator = File.separator;
					strSelKey = new Path(fileDialog.getFilterPath() + separator
							+ fileDialog.getFileName()).toOSString();
					txtPrivateKeyFile.setText(strSelKey);
				}
				txtPrivateKeyFile.setFocus();
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}

		btnKeyBrowse = new Button(container, SWT.NONE);
		initializeDialogUnits(btnKeyBrowse);
		setButtonLayoutData(btnKeyBrowse);
		btnKeyBrowse.setText(WizardMessages.Key_Pairs_btnKeyBrowse_Text);
		btnKeyBrowse.addSelectionListener(new OpenKey());

		lblCerFile = new Label(container, SWT.NONE);
		lblCerFile.setText(WizardMessages.Key_Pairs_lblCerFile_Text);

		txtCerFile = new Text(container, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 280;
		gd.horizontalSpan = 3;
		txtCerFile.setLayoutData(gd);
		txtCerFile.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				strSelCer = txtCerFile.getText();
				if (dialogMode == 3)
					contentChanged = true;
				dialogChanged();
			}
		});

		class OpenCer implements SelectionListener {
			public void widgetSelected(SelectionEvent event) {
				FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
				fileDialog
						.setText(WizardMessages.Key_Pairs_cerFileDialog_Title);
				fileDialog.setFilterPath(ResourcesPlugin.getWorkspace()
						.getRoot().getLocation().toOSString());
				fileDialog.setFilterExtensions(cerFilterExt);
				if (strSelCer != "")
					fileDialog.setFileName(strSelCer);
				if (fileDialog.open() != null) {
					String separator = "";
					int length = fileDialog.getFilterPath().length();
					if (length > 0
							&& fileDialog.getFilterPath().charAt(length - 1) != File.separatorChar)
						separator = File.separator;
					strSelCer = new Path(fileDialog.getFilterPath() + separator
							+ fileDialog.getFileName()).toOSString();
					txtCerFile.setText(strSelCer);
				}
				txtCerFile.setFocus();
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}

		btnCerBrowse = new Button(container, SWT.NONE);
		initializeDialogUnits(btnCerBrowse);
		setButtonLayoutData(btnCerBrowse);
		btnCerBrowse.setText(WizardMessages.Key_Pairs_btnCerBrowse_Text);
		btnCerBrowse.addSelectionListener(new OpenCer());

		lblPassword = new Label(container, SWT.NONE);
		lblPassword.setText(WizardMessages.Key_Pairs_lblPassword_Text);

		txtPassword = new Text(container, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		txtPassword.setLayoutData(gd);
		txtPassword.setEchoChar('*');
		txtPassword.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				strPassword = txtPassword.getText();
				if (dialogMode == 3)
					contentChanged = true;
				dialogChanged();
			}
		});

		warningContainer = new Composite(container, SWT.NONE);
		layout = new GridLayout();
		warningContainer.setLayout(layout);
		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.verticalSpan = 2;
		warningContainer.setLayoutData(gd);
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		warningContainer.setVisible(false);

		warningImage = ISharedImageDescriptor.ICON16_WARNING.createImage();
		infoImage = ISharedImageDescriptor.ICON16_INFO.createImage();

		Label lblInfo2Image = new Label(warningContainer, SWT.NONE);
		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		lblInfo2Image.setLayoutData(gd);
		lblInfo2Image.setImage(warningImage);

		Label lblInfo2Text = new Label(warningContainer, SWT.WRAP);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 150;
		lblInfo2Text.setLayoutData(gd);
		lblInfo2Text.setText(WizardMessages.Key_Pairs_lblInfo2Text_Text);

		// Dummy labels to fill columns
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		chkSavePassword = new Button(container, SWT.CHECK);
		chkSavePassword.setText(WizardMessages.Key_Pairs_chkSavePassword_Text);
		chkSavePassword.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (dialogMode == 3) {
					contentChanged = true;
				}
				dialogChanged();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// Dummy label to fill a column
		Label label4 = new Label(container, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		label4.setLayoutData(gd);

		// Dummy label to fill a column
		new Label(container, SWT.NONE);

		lblSavePasswordInfoImage = new Label(container, SWT.NONE);
		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		lblSavePasswordInfoImage.setLayoutData(gd);
		lblSavePasswordInfoImage.setImage(warningImage);

		lblSavePasswordInfo = new Label(container, SWT.WRAP);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 300;
		gd.horizontalSpan = 3;
		lblSavePasswordInfo.setLayoutData(gd);
		lblSavePasswordInfo
				.setText(WizardMessages.Key_Pairs_chkSavePasswordWarning_Text);

		// Dummy label to fill a column
		new Label(container, SWT.NONE);

		Composite container2 = new Composite(container, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 5;
		gd.verticalIndent = 5;
		container2.setLayoutData(gd);
		GridLayout layout2 = new GridLayout();
		container2.setLayout(layout2);
		layout2.numColumns = 5;
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		layout2.verticalSpacing = 7;

		// Dummy label to fill a column
		Label label5 = new Label(container2, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		label5.setLayoutData(gd);

		btnNew = new Button(container2, SWT.NONE);
		initializeDialogUnits(btnNew);
		setButtonLayoutData(btnNew);
		btnNew.setText(WizardMessages.Key_Pairs_btnNew_Text);
		btnNew.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				clearTextBoxes();
				dialogMode = 2;
				updateStates();
				txtKeyPairName.setFocus();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnKeys = new Button(container2, SWT.NONE);
		initializeDialogUnits(btnKeys);
		setButtonLayoutData(btnKeys);
		btnKeys.setText(WizardMessages.Key_Pairs_btnKeys_Text);
		btnKeys.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				MakeKeysDialog makeKeysDialog = new MakeKeysDialog(getShell());
				makeKeysDialog.create();
				makeKeysDialog.open();
				if (makeKeysDialog.getReturnCode() == 0) {
					strSelKey = makeKeysDialog.getKeyDestination();
					txtPrivateKeyFile.setText(strSelKey);
					strSelCer = makeKeysDialog.getCerDestination();
					txtCerFile.setText(strSelCer);
					strPassword = makeKeysDialog.getPassword();
					if (strPassword.equals("")) {
						txtPassword.setText("");
						txtPassword.setEnabled(false);
					} else
						txtPassword.setText(strPassword);

					if (dialogMode == 3)
						contentChanged = true;
					dialogChanged();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnSave = new Button(container2, SWT.NONE);
		initializeDialogUnits(btnSave);
		setButtonLayoutData(btnSave);
		btnSave.setText(WizardMessages.Key_Pairs_btnSave_Text);
		btnSave.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				// Case creating a new key pair
				if (dialogMode == 2) {
					shell.setEnabled(false);
					shell.setCursor(shell.getDisplay().getSystemCursor(
							SWT.CURSOR_WAIT));
					txtKeyPairName.setText(strPairName);
					if (validateKeyPairContent(strSelKey, strSelCer,
							strPassword)) {
						saveKeyPair(strPairName, strSelKey, strSelCer,
								strPassword);
						updateKeyPairList();
						dialogMode = 1;
						shell.setEnabled(true);
						shell.setCursor(shell.getDisplay().getSystemCursor(
								SWT.CURSOR_ARROW));
						lstKeyPairs.select(lstKeyPairs.indexOf(strPairName));
						dialogChanged();
//						if (chkSavePassword.getSelection() != true)
//							txtPassword.setText("");
						return;
					}
					shell.setEnabled(true);
					shell.setCursor(new Cursor(shell.getDisplay(),
							SWT.CURSOR_ARROW));
					IBrandingManager branding = BrandingExtensionManager
							.getBrandingManager();
					Image image = null;
					if (branding != null) {
						image = branding.getIconImageDescriptor().createImage();
					}
					MessageDialog dialog = new MessageDialog(
							PlatformUI.getWorkbench().getDisplay()
									.getActiveShell(),
							WizardMessages.Key_Pairs_Generate_MsgBox_Error_Title,
							image,
							WizardMessages.Key_Pairs_Generate_MsgBox_Error_Message,
							1, new String[] { IDialogConstants.OK_LABEL }, 0);
					dialog.open();
					if (image != null) {
						image.dispose();
					}
				}

				// saving an existing key pair
				if (dialogMode == 3) {
					shell.setEnabled(false);
					shell.setCursor(new Cursor(shell.getDisplay(),
							SWT.CURSOR_WAIT));
					txtKeyPairName.setText(strPairName);
					if (validateKeyPairContent(strSelKey, strSelCer,
							strPassword)) {
						int index = lstKeyPairs.getSelectionIndex();
						strSelKeyPair = lstKeyPairs.getItem(lstKeyPairs
								.getSelectionIndex());
						deleteKeyPair(index, strSelKeyPair);
						updateKeyPairList();
						saveKeyPair(strPairName, strSelKey, strSelCer,
								strPassword);
						updateKeyPairList();
						dialogMode = 1;
						shell.setEnabled(true);
						shell.setCursor(new Cursor(shell.getDisplay(),
								SWT.CURSOR_ARROW));
						lstKeyPairs.select(lstKeyPairs.indexOf(strPairName));
						dialogChanged();
//						if (chkSavePassword.getSelection() != true)
//							txtPassword.setText("");
						return;
					}
					shell.setEnabled(true);
					shell.setCursor(new Cursor(shell.getDisplay(),
							SWT.CURSOR_ARROW));

					IBrandingManager branding = BrandingExtensionManager
							.getBrandingManager();
					Image image = null;
					if (branding != null) {
						image = branding.getIconImageDescriptor().createImage();
					}
					MessageDialog dialog = new MessageDialog(PlatformUI
							.getWorkbench().getDisplay().getActiveShell(),
							WizardMessages.Key_Pairs_Save_MsgBox_Error_Title,
							image,
							WizardMessages.Key_Pairs_Save_MsgBox_Error_Message,
							1, new String[] { IDialogConstants.OK_LABEL }, 0);
					dialog.open();
					if (image != null) {
						image.dispose();
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnCancel = new Button(container2, SWT.NONE);
		initializeDialogUnits(btnCancel);
		setButtonLayoutData(btnCancel);
		btnCancel.setText(WizardMessages.Key_Pairs_btnCancel_Text);
		btnCancel.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (dialogMode == 2) {
					if (lstKeyPairs.getSelectionIndex() != -1) {
						strSelKeyPair = lstKeyPairs.getItem(lstKeyPairs
								.getSelectionIndex());
						updateKeyPairContent(strSelKeyPair);
					} else if (lstKeyPairs.getItemCount() > 0) {
						lstKeyPairs.select(0);
						updateKeyPairContent(lstKeyPairs.getItem(lstKeyPairs
								.getSelectionIndex()));
					} else
						clearTextBoxes();
				} else
					updateKeyPairContent(lstKeyPairs.getItem(lstKeyPairs
							.getSelectionIndex()));
				dialogMode = 1;
				dialogChanged();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Composite container3 = new Composite(area, SWT.NONE);
		container3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout3 = new GridLayout();
		container3.setLayout(layout3);
		layout3.numColumns = 1;
		layout3.marginHeight = 0;
		layout3.marginWidth = 0;
		layout3.verticalSpacing = 0;

		final Label separator2 = new Label(container3, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		separator2.setLayoutData(gd);

		updateKeyPairList();

		if (lstKeyPairs.getItemCount() > 0) {
			updateKeyPairContent(lstKeyPairs.getItem(lstKeyPairs
					.getSelectionIndex()));
		}

		updateStates();
		return area;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CLOSE_ID,
				IDialogConstants.CLOSE_LABEL, false);
	}

	@Override
	public boolean close() {
		if (warningImage != null) {
			warningImage.dispose();
		}
		if (infoImage != null) {
			infoImage.dispose();
		}
		if (lstKeyPairs.getSelectionIndex() != -1)
			strSelKeyPair = lstKeyPairs
					.getItem(lstKeyPairs.getSelectionIndex());
		else if (lstKeyPairs.getItemCount() > 0)
			strSelKeyPair = lstKeyPairs.getItem(0);
		else
			strSelKeyPair = "";
		defaultKeyPair = strSelKeyPair;
		try {
			KeyPair keyPair = KeyPair.getKeyPair(defaultKeyPair);
			if (keyPair != null) {
				keyPair.setDefault(true);
				keyPair.save();
			}
		} catch (Exception e) {
			UiPlugin.error(e);
		}
		return super.close();
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.CLOSE_ID) {
			setMessage(null);
			this.close();
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * Clears all text boxes
	 */
	private void clearTextBoxes() {
		dialogMode = 0;
		txtKeyPairName.setText("");
		txtPrivateKeyFile.setText("");
		txtCerFile.setText("");
		txtPassword.setText("");
		chkSavePassword.setSelection(false);
		dialogMode = 1;
	}

	/**
	 * Handles enabled/disabled state logic of components
	 */
	private void updateStates() {
		switch (dialogMode) {
		case 0: // Null mode
			break;

		case 1: // Showing mode
			lstKeyPairs.setEnabled(true);
			if (lstKeyPairs.getSelectionIndex() != -1) {
				btnEdit.setEnabled(true);
				btnDelete.setEnabled(true);
			} else {
				btnDelete.setEnabled(false);
				btnEdit.setEnabled(false);
			}
			txtKeyPairName.setEditable(false);
			txtPrivateKeyFile.setEditable(false);
			btnKeyBrowse.setEnabled(false);
			txtCerFile.setEditable(false);
			btnCerBrowse.setEnabled(false);
			txtPassword.setEditable(false);
			btnNew.setEnabled(true);
			btnSave.setEnabled(false);
			btnCancel.setEnabled(false);
			btnKeys.setEnabled(false);
			chkSavePassword.setEnabled(false);
			lblSavePasswordInfoImage.setVisible(false);
			lblSavePasswordInfo.setVisible(false);
			updateKeyExistingWarningState();
			break;

		case 2: // Creating mode
			lstKeyPairs.setEnabled(false);
			txtKeyPairName.setEditable(true);
			txtPrivateKeyFile.setEditable(true);
			btnKeyBrowse.setEnabled(true);
			txtCerFile.setEditable(true);
			btnCerBrowse.setEnabled(true);
			txtPassword.setEditable(true);
			btnNew.setEnabled(false);
			btnEdit.setEnabled(false);
			btnCancel.setEnabled(true);
			btnKeys.setEnabled(true);
			btnDelete.setEnabled(false);
			if (dialogComplete)
				btnSave.setEnabled(true);
			else
				btnSave.setEnabled(false);
			if (txtPassword.getText() == "")
				chkSavePassword.setSelection(false);
			if (txtPassword.getText() != "") {
				chkSavePassword.setEnabled(true);
				lblSavePasswordInfoImage.setVisible(true);
				lblSavePasswordInfo.setVisible(true);
			} else {
				chkSavePassword.setEnabled(false);
				lblSavePasswordInfoImage.setVisible(false);
				lblSavePasswordInfo.setVisible(false);
			}
			if (chkSavePassword.getSelection()) {
				lblSavePasswordInfoImage.setImage(warningImage);
				lblSavePasswordInfo
						.setText(WizardMessages.Key_Pairs_chkSavePasswordWarning_Text);

			} else {
				lblSavePasswordInfoImage.setImage(infoImage);
				lblSavePasswordInfo
						.setText(WizardMessages.Key_Pairs_chkSavePasswordInfo_Text);
			}
			lblSavePasswordInfo.getParent().layout();
			updateKeyExistingWarningState();
			break;

		case 3: // Editing mode
			lstKeyPairs.setEnabled(false);
			txtKeyPairName.setEditable(true);
			txtPrivateKeyFile.setEditable(true);
			btnKeyBrowse.setEnabled(true);
			txtCerFile.setEditable(true);
			btnCerBrowse.setEnabled(true);
			txtPassword.setEditable(true);
			btnNew.setEnabled(false);
			btnEdit.setEnabled(false);
			if (dialogComplete && contentChanged)
				btnSave.setEnabled(true);
			else
				btnSave.setEnabled(false);
			btnCancel.setEnabled(true);
			btnKeys.setEnabled(true);
			btnDelete.setEnabled(false);
			if (txtPassword.getText() == "")
				chkSavePassword.setSelection(false);
			if (txtPassword.getText() != "") {
				chkSavePassword.setEnabled(true);
				lblSavePasswordInfoImage.setVisible(true);
				lblSavePasswordInfo.setVisible(true);
			} else {
				chkSavePassword.setEnabled(false);
				lblSavePasswordInfoImage.setVisible(false);
				lblSavePasswordInfo.setVisible(false);
			}
			if (chkSavePassword.getSelection()) {
				lblSavePasswordInfoImage.setImage(warningImage);
				lblSavePasswordInfo
						.setText(WizardMessages.Key_Pairs_chkSavePasswordWarning_Text);

			} else {
				lblSavePasswordInfoImage.setImage(infoImage);
				lblSavePasswordInfo
						.setText(WizardMessages.Key_Pairs_chkSavePasswordInfo_Text);
			}
			lblSavePasswordInfo.getParent().layout();
			updateKeyExistingWarningState();
			break;

		default:
			break;
		}
	}

	/**
	 * Validates the input and offers error messages
	 */
	private void dialogChanged() {
		if (dialogMode == 1) {
			dialogComplete = true;
			updateStatus(null);
			updateStates();
			return;
		}

		IStatus nameStatus = ResourcesPlugin.getWorkspace().validateName(
				strPairName, IResource.FOLDER);
		IStatus passwordStatus = ResourcesPlugin.getWorkspace().validateName(
				strPassword, IResource.FOLDER);

		KeyPair[] keyPairs = getKeyPairs();

		Path keyPath = new Path(strSelKey);
		String keyExt = keyPath.getFileExtension();

		Path cerPath = new Path(strSelCer);
		String cerExt = cerPath.getFileExtension();

		if (strPairName.trim().length() == 0) {
			updateStatus(WizardMessages.Key_Pairs_No_Name_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (strPairName.trim().length() != 0 && keyPairs != null) {
			for (int i = 0; i < keyPairs.length; i++)
				if (keyPairs[i].getName().equalsIgnoreCase(strPairName))
					if (dialogMode != 3
							|| !strPairName.equalsIgnoreCase(strSelKeyPair)) {
						updateStatus(WizardMessages.Key_Pairs_Name_Exist_Error);
						dialogComplete = false;
						updateStates();
						return;
					}
		}

		if (!nameStatus.isOK()) {
			updateStatus(WizardMessages.Key_Pairs_Name_Wrong_Char_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (strSelKey == "") {
			updateStatus(WizardMessages.Key_Pairs_Key_No_File_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (keyExt == null
				|| (!keyExt.equalsIgnoreCase("key") && !keyExt
						.equalsIgnoreCase("pem"))) {
			updateStatus(WizardMessages.Key_Pairs_Key_Extension_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (!FileUtils.isFileValidAndAccessible(keyPath)) {
			updateStatus(WizardMessages.Key_Pairs_Key_Not_Exist_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (strSelCer == "") {
			updateStatus(WizardMessages.Key_Pairs_Cer_No_File_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (cerExt == null
				|| (!cerExt.equalsIgnoreCase("cer") && !cerExt
						.equalsIgnoreCase("p7c"))
				&& !cerExt.equalsIgnoreCase("der")) {
			updateStatus(WizardMessages.Key_Pairs_Cer_Extension_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (!FileUtils.isFileValidAndAccessible(cerPath)) {
			updateStatus(WizardMessages.Key_Pairs_Cer_Not_Exist_Error);
			dialogComplete = false;
			updateStates();
			return;
		}

		if (strPassword != "")
			if (!passwordStatus.isOK()) {
				updateStatus(WizardMessages.Key_Pairs_Password_Wrong_Char_Error);
				dialogComplete = false;
				updateStates();
				return;
			}

		dialogComplete = true;
		updateStatus(null);
		updateStates();
	}

	/**
	 * Updates the key pair list
	 */
	private void updateKeyPairList() {
		lstKeyPairs.removeAll();
		KeyPair[] keyPairs = getKeyPairs();
		if (keyPairs.length > 0 && keyPairs[0].getName().length() != 0) {
			String[] names = new String[keyPairs.length];
			for (int i = 0; i < keyPairs.length; i++) {
				names[i] = keyPairs[i].getName();
			}
			lstKeyPairs.setItems(names);
			if (defaultKeyPair == null) {
				for (KeyPair keyPair : keyPairs) {
					if (keyPair.isDefault()) {
						defaultKeyPair = keyPair.getName();
					}
				}
			}
			int index = Math.max(0, defaultKeyPair == null ? 0 : lstKeyPairs
					.indexOf(defaultKeyPair));
			lstKeyPairs.select(index);
		}
	}

	/**
	 * Updates key pair content in text boxes
	 */
	private void updateKeyPairContent(String keyPairName) {
		KeyPair keyPair = getKeyPair(keyPairName);
		if (keyPair != null) {
			txtKeyPairName.setText(keyPair.getName());
			txtPrivateKeyFile.setText(keyPair.getPrivateKeyFile());
			txtCerFile.setText(keyPair.getCertificateFile());
			txtPassword.setText(keyPair.getPassword() == null ? "" : keyPair
					.getPassword());
			chkSavePassword.setSelection(keyPair.isSavePassword() == true);
		}
	}

	/**
	 * @param Key
	 *            pair name
	 * @return Content of a key pair
	 */
	public KeyPair getKeyPair(String keyPairName) {
		try {
			return KeyPair.getKeyPair(keyPairName);
		} catch (Exception e) {
			UiPlugin.error(e);
			return null;
		}
	}

	/**
	 * @return All key pairs from the store
	 */
	public KeyPair[] getKeyPairs() {
		try {
			return KeyPair.getKeyPairs();
		} catch (Exception e) {
			UiPlugin.error(e);
			return null;
		}
	}

	/**
	 * @param key
	 *            file, certificate file, password
	 * @return FALSE if public key, certificate and password are mismatching,
	 *         otherwise TRUE
	 */
	public boolean validateKeyPairContent(String keyFile, String cerFile,
			String password) {
		try {

			// If no theme is open, we can not test keypair
			IEditorPart editor = EclipseUtils.getActiveSafeEditor();
			if (editor == null) {
				return true;
			}

			if (context == null) {
				createDummyPackagingContext();
			}

			try {
				SymbianUtil.testKey(cerFile, keyFile, password, context);
				return true;
			} catch (Exception e) {
				UtilsPlugin.error(e);
				return false;
			}

//			try {
//				SymbianUtil.testKey(cerFile, keyFile, "", context);
//				if (password == null || password.trim().length() != 0)
//					return false;
//				else
//					return true;
//			} catch (Exception e) {
//				UtilsPlugin.error(e);
//				return true;
//			}
		} catch (Exception e) {
			e.printStackTrace();

			return false;
		}
	}

	private void createDummyPackagingContext() {
		getModelList();
		IEditorPart editor = EclipseUtils.getActiveSafeEditor();
		IContentAdapter adapter = (IContentAdapter) editor
				.getAdapter(IContentAdapter.class);
		IContent[] contents = adapter.getContents();
		// filters out all contents that are not packageable
		ArrayList<IContent> list = new ArrayList<IContent>(contents.length);
		for (IContent content : contents) {
			if (content.getAdapter(IPackager.class) != null) {
				list.add(content);
			}
		}
		contents = list.toArray(new IContent[list.size()]);

		context = new PackagingContext();
		context.setInput(contents);

		context.setAttribute(PackagingAttribute.primaryModelId.name(),
				getPrimaryModelID());
		context.setAttribute(PackagingAttribute.secondaryModelId.name(),
				(getSecondaryModelId() == null) ? getPrimaryModelID()
						: getSecondaryModelId());
	}

	private String getPrimaryModelID() {
		return modelDescList.get(0).getId();
	}

	private String getSecondaryModelId() {
		String primaryModelId = getPrimaryModelID();
		Object platformId = ModelPlatformMapping.getInstance().get(
				primaryModelId);
		return (platformId == null) ? null : platformId.toString();
	}

	private void saveKeyPair(String strPairName, String strSelKey,
			String strSelCer, String strPassword) {
		deleteKeyPair(strPairName);
		KeyPair keyPair = new KeyPair();
		keyPair.setName(strPairName);
		keyPair.setPrivateKeyFile(strSelKey);
		keyPair.setCertificateFile(strSelCer);
		if (strPassword.length() == 0) {
			keyPair.setSavePassword(true);
			keyPair.setPassword("");
		} else if (!chkSavePassword.getSelection()) {
			keyPair.setSavePassword(false);
			keyPair.setPassword(strPassword);
		} else {
			keyPair.setSavePassword(true);
			keyPair.setPassword(strPassword);
		}
		saveKeyPair(keyPair);
	}

	java.util.List<IThemeModelDescriptor> modelDescList;

	private String[] getModelList() {
		modelDescList = ThemeModelUtil.getAllThemeModelDescriptor();
		String[] modelList = new String[modelDescList.size()];
		int i = 0;
		for (IThemeModelDescriptor themeModelDescriptor : modelDescList) {
			modelList[i++] = getDisplayName(themeModelDescriptor.getName());
		}
		return modelList;
	}

	private String getDisplayName(String modelName) {
		modelName = modelName.replace("BaseGraphics", "");
		modelName = modelName.trim();
		modelName = modelName.replace("FP", "Feature Pack ");
		return modelName;
	}

	/**
	 * Saves key pair in the store
	 */
	private void saveKeyPair(KeyPair keyPair) {
		try {
			keyPair.save();
		} catch (Exception e) {
			UiPlugin.error(e);
		}
	}

	private void deleteKeyPair(String strKeyName) {
		try {
			KeyPair.delete(strKeyName);
		} catch (Exception e) {
			UiPlugin.error(e);
		}
	}

	/**
	 * Deletes key pair content from the store
	 */
	private void deleteKeyPair(int index, String strKeyName) {
		deleteKeyPair(strKeyName);
		lstKeyPairs.remove(index);
	}

	/**
	 * Updates error messages in the banner area
	 */
	private void updateStatus(String message) {
		setErrorMessage(message);
	}

	/**
	 * Updates visibility state of warning of existing key file
	 */
	private void updateKeyExistingWarningState() {
		Path keyPath = new Path(strSelKey);
		File keyFile = new File(strSelKey);
		String keyExt = keyPath.getFileExtension();

		if (dialogMode > 1
				&& keyExt != null
				&& keyFile.exists()
				&& (keyExt.equalsIgnoreCase("key") || (keyExt
						.equalsIgnoreCase("pem"))))
			warningContainer.setVisible(true);
		else
			warningContainer.setVisible(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.wizards.BrandedTitleAreaDialog#getTitle()
	 */
	@Override
	protected String getTitle() {
		return WizardMessages.Key_Pairs_Title;
	}

	public String getDefaultKeyPair() {
		return defaultKeyPair;
	}
}
