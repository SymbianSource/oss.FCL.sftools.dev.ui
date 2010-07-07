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
package com.nokia.tools.theme.s60.ui.preferences;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.utils.IRefreshableObject;
import com.nokia.tools.platform.extension.IThemeContainerDescriptor;
import com.nokia.tools.platform.extension.PlatformExtensionManager;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.theme.s60.cstore.ComponentPoolConfig;
import com.nokia.tools.theme.s60.examplethemes.ExampleThemeProvider;

/**
 * Preference page for Component Store
 */
public class ComponentStorePrefPage extends PreferencePage implements
		IWorkbenchPreferencePage, IMenuListener, SelectionListener {

	public static final String PREFERENCES_CONTEXT = "com.nokia.tools.theme.s60.ui" + '.' + "component_store_preferences_context"; //$NON-NLS-1$

	private Button addExample, addWorkspace, addCustom;

	private List userList;

	private Button removeThemeButton, addTheme;

	private ComponentPoolConfig data = null;

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#
	 *      createDialogArea(org.eclipse.swt.widgets.Composite) Here we fill the
	 *      center area of the dialog
	 */
	protected Control createDialogArea(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				PREFERENCES_CONTEXT);

		Composite area = parent;

		// Create new composite as container
		final Composite container = new Composite(area, SWT.NONE);

		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		container.setLayout(layout);

		setTitle(Messages.ComponentPooDlg_title);

		/*
		 * If there are theme containers other that S60 theme container i.e.
		 * there are more than one theme containers then put a warning message
		 * in description message area saying that the preferences on this page
		 * are applicable for S60 Themes only.
		 */
		IThemeContainerDescriptor[] themeContainerDescriptors = PlatformExtensionManager.getThemeContainerDescriptors();
		if(themeContainerDescriptors.length >1){
			warningMessage = Messages.ComponentPooDlg_componentStrMsgPart;
		}
		
		setDescription(Messages.ComponentPooDlg_msg);
		
		GridData gd = null;
		setWarningMessage(container);

		createDescriptionText(container);
		
		if(ExampleThemeProvider.isExampleThemeAvailable()){
			addExample = new Button(container, SWT.CHECK);
			addExample.setText(Messages.ComponentPooDlg_showExamples);
			gd = new GridData();
			gd.verticalIndent = 5;
			gd.horizontalSpan = 2;
			addExample.setLayoutData(gd);
		}

		addWorkspace = new Button(container, SWT.CHECK);
		addWorkspace.setText(Messages.ComponentPooDlg_showWspace);
		gd = new GridData();
		gd.horizontalSpan = 2;
		addWorkspace.setLayoutData(gd);

		addCustom = new Button(container, SWT.CHECK);
		addCustom.setText(Messages.ComponentPooDlg_showcustom);
		gd = new GridData();
		gd.horizontalSpan = 2;
		addCustom.setLayoutData(gd);

		addCustom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doAddCustomChecked();
			}
		});

		userList = new List(container, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL
				| SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		gd.widthHint = 200;
		userList.setLayoutData(gd);

		if (ExampleThemeProvider.isExampleThemeAvailable()) {
			addExample.setSelection(data.addExampleThemes);
			addWorkspace.setSelection(data.addWorkspaceTheme);
			addCustom.setSelection(data.addCustomThemes);
		}

		userList.setItems(data.userThemeList.toArray(new String[0]));
		MenuManager manager = new MenuManager();
		manager.addMenuListener(this);
		userList.setMenu(manager.createContextMenu(userList));
		userList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					if (e.keyCode == SWT.DEL)
						if (!StringUtils.isEmpty(userList.getItem(userList
								.getSelectionIndex()))) {
							doRemoveThemeAction();
						}
				} catch (Exception es) {
				}
			}
		});
		userList.addSelectionListener(this);

		Composite userListBtContainer = new Composite(container, SWT.None);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		userListBtContainer.setLayout(layout);
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		userListBtContainer.setLayoutData(gd);

		addTheme = new Button(userListBtContainer, SWT.PUSH);
		addTheme.setText(Messages.ComponentPooDlg_add);
		addTheme.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		calculateButtonSize(addTheme);
		addTheme.addSelectionListener(this);

		removeThemeButton = new Button(userListBtContainer, SWT.PUSH);
		removeThemeButton.setText(Messages.ComponentPooDlg_delete);
		removeThemeButton.setEnabled(false);
		removeThemeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		calculateButtonSize(removeThemeButton);
		removeThemeButton.addSelectionListener(this);

		// proper init add custom themes check
		doAddCustomChecked();

		return area;
	}
	
	private String warningMessage;
	
	private Text messageText;
    private Composite messageComposite;
	private Label messageImageLabel;
	
	private Composite setWarningMessage(Composite parent) {
		if (warningMessage != null) {
			messageComposite = new Composite(parent, SWT.NONE);
			GridLayout messageLayout = new GridLayout();
			messageLayout.numColumns = 2;
			messageLayout.marginWidth = 0;
			messageLayout.marginHeight = 0;
			messageLayout.makeColumnsEqualWidth = false;
			messageComposite.setLayout(messageLayout);

			messageImageLabel = new Label(messageComposite, SWT.NONE);
			messageImageLabel.setImage(JFaceResources
					.getImage(Dialog.DLG_IMG_MESSAGE_WARNING));
			messageImageLabel.setLayoutData(new GridData(
					GridData.VERTICAL_ALIGN_BEGINNING));

			messageText = new Text(messageComposite, SWT.WRAP);
			messageText.setEditable(false);
			GridData textData = new GridData(GridData.GRAB_HORIZONTAL
					| GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
			messageText.setLayoutData(textData);
			messageText.setText(warningMessage);
		}
		return messageComposite;
	}
	
	private Text createDescriptionText(Composite parent) {
		Text warningMsgText = null;
		warningMsgText = new Text(parent, SWT.WRAP);
		warningMsgText.setEditable(false);
		GridData textData = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
		warningMsgText.setLayoutData(textData);
		warningMsgText.setText(getDescription());
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		warningMsgText.setLayoutData(gd);
		return warningMsgText;
	}

	protected void doAddCustomChecked() {
		removeThemeButton.setEnabled(userList.getSelectionIndex() > -1);
		pageChanged();
	}

	private void doAddThemeAction() {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
		fd.setText(Messages.ComponentPooDlg_select);
		fd.setFilterExtensions(new String[] { "*.tdf" }); //$NON-NLS-1$
		String path = fd.open();
		if (path != null && new File(path).exists()) {
			if ("tdf".equalsIgnoreCase(FileUtils.getExtension(path))) { //$NON-NLS-1$
				userList.add(path);
			}
		}
	}

	private void doRemoveThemeAction() {
		int index = userList.getSelectionIndex();
		userList.remove(userList.getSelectionIndex());
		addCustom.setSelection(userList.getItemCount() > 0);
		if (removeThemeButton != null)
			try {
				userList.setSelection(--index);
				removeThemeButton.setEnabled(!StringUtils.isEmpty(userList
						.getItem(userList.getSelectionIndex())));
			} catch (Exception e) {
				removeThemeButton.setEnabled(false);
			}
	}

	@Override
	protected Control createContents(Composite parent) {
		return createDialogArea(parent);
	}

	public void init(IWorkbench workbench) {
		data = ComponentPoolConfig.load();
	}

	@Override
	protected void performDefaults() {
		userList.removeAll();
		if(ExampleThemeProvider.isExampleThemeAvailable()){
			addExample.setSelection(true);
		}
		addWorkspace.setSelection(false);
		addCustom.setSelection(false);
		doAddCustomChecked();
	}

	@Override
	protected void performApply() {
		performOk();
	}

	@Override
	public boolean performOk() {
		if(ExampleThemeProvider.isExampleThemeAvailable()){
			data.addExampleThemes = addExample.getSelection();
		}
		data.addWorkspaceTheme = addWorkspace.getSelection();
		data.addCustomThemes = addCustom.getSelection();

		data.userThemeList.clear();
		if (userList.getItemCount() > 0)
			for (String a : userList.getItems())
				data.userThemeList.add(a);
		data.save();

		refreshComponentStore();
		
		return true;
	}
	
	// force refresh of C.Store View if displayed
	private void refreshComponentStore() {
		try {
			String viewId = "com.nokia.tools.theme.s60.ui.views.ComponentStore";
			IRefreshableObject instance = (IRefreshableObject) PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.findView(viewId);
			if (instance != null) {
				instance.notifyRefreshNeeded(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void menuAboutToShow(IMenuManager manager) {
		manager.removeAll();

		Action delete = new Action(Messages.ComponentPooDlg_delete) {
			@Override
			public void run() {
				doRemoveThemeAction();
			}
		};
		delete.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		try {
			delete.setEnabled(false);
			if (!StringUtils.isEmpty(userList.getItem(userList
					.getSelectionIndex()))) {
				delete.setEnabled(true);
			}
		} catch (Exception e) {
		}

		Action add = new Action(Messages.ComponentPooDlg_add) {
			@Override
			public void run() {
				doAddThemeAction();
			}
		};
		add.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));

		manager.add(add);
		manager.add(delete);
	}

	public void widgetSelected(SelectionEvent e) {
		try {
			if (e.getSource() == removeThemeButton) {
				doRemoveThemeAction();
			} else if (e.getSource() == addTheme) {
				doAddThemeAction();
			} else if (e.getSource() == userList) {
				removeThemeButton.setEnabled(!StringUtils.isEmpty(userList
						.getItem(userList.getSelectionIndex())));
			}
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		pageChanged();
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	private void pageChanged() {
		if (addCustom.getSelection() && userList.getItemCount() == 0) {
			updateStatus(Messages.ComponentPooDlg_list_empty);
			return;
		}
		setValid(true);
		updateStatus(null);
	}

	/**
	 * Updates error messages
	 */
	private void updateStatus(String message) {
		setErrorMessage(message);
		setValid(message == null);
	}

	private void calculateButtonSize(Button button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int wHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point mSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(wHint, mSize.x);
		button.setLayoutData(data);
	}
}
