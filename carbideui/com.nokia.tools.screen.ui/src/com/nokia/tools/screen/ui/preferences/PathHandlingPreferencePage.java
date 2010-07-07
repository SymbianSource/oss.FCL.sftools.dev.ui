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
package com.nokia.tools.screen.ui.preferences;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.ui.dialogs.PathHandlingConfig;

public class PathHandlingPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage, SelectionListener {

	public static final String PREFERENCES_CONTEXT = "com.nokia.tools.screen.ui" + '.' + "image_folders_preferences_context"; //$NON-NLS-1$

	private List lstFolders;

	private Button chkCustomList, chkRecentList, btnRemove, btnAdd, btnUp,
			btnDown, btnClearList;

	private Label lblQuantity;

	private Spinner spnQuantity;

	private PathHandlingConfig data = null;

	private static final int QUANTITY_MAX = 20;

	private static final int QUANTITY_MIN = 0;

	private static final int QUANTITY_INCREMENT = 1;

	private static final int QUANTITY_PAGE_INCREMENT = 2;

	protected Control createDialogArea(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				PREFERENCES_CONTEXT);

		final Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		area.setLayout(layout);

		setTitle(Messages.PathHandlingPreferencePage_Title);

		setDescription(Messages.PathHandlingPreferencePage_Description);
		createDescriptionLabel(area);

		Composite container = new Composite(area, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.verticalIndent = 5;
		container.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		container.setLayout(layout);

		chkCustomList = new Button(container, SWT.RADIO);
		chkCustomList
				.setText(Messages.PathHandlingPreferencePage_chkCustomList_Label);
		gd = new GridData();
		gd.horizontalSpan = 2;
		chkCustomList.setLayoutData(gd);
		chkCustomList.addSelectionListener(this);

		lstFolders = new List(container, SWT.SINGLE | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		gd.widthHint = 200;
		lstFolders.setLayoutData(gd);

		lstFolders.setItems(data.predefinedPathList.toArray(new String[0]));
		lstFolders.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					if (e.keyCode == SWT.DEL)
						if (!StringUtils.isEmpty(lstFolders.getItem(lstFolders
								.getSelectionIndex()))) {
							doRemoveThemeAction();
						}
				} catch (Exception es) {
				}
			}
		});
		lstFolders.addSelectionListener(this);

		Composite userListBtContainer = new Composite(container, SWT.None);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		userListBtContainer.setLayout(layout);
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		userListBtContainer.setLayoutData(gd);

		btnAdd = new Button(userListBtContainer, SWT.PUSH);
		btnAdd.setText(Messages.PathHandlingPreferencePage_btnAdd_Label);
		calculateButtonSize(btnAdd);
		btnAdd.addSelectionListener(this);

		btnRemove = new Button(userListBtContainer, SWT.PUSH);
		btnRemove.setText(Messages.PathHandlingPreferencePage_btnRemove_Label);
		btnRemove.setEnabled(false);
		calculateButtonSize(btnRemove);
		btnRemove.addSelectionListener(this);

		btnUp = new Button(userListBtContainer, SWT.PUSH);
		btnUp.setText(Messages.PathHandlingPreferencePage_btnUp_Label);
		calculateButtonSize(btnUp);
		gd = (GridData) btnUp.getLayoutData();
		gd.verticalIndent = 9;
		btnUp.addSelectionListener(this);

		btnDown = new Button(userListBtContainer, SWT.PUSH);
		btnDown.setText(Messages.PathHandlingPreferencePage_btnDown_Label);
		calculateButtonSize(btnDown);
		gd = new GridData(SWT.FILL, SWT.TOP, false, false);
		btnDown.setLayoutData(gd);
		btnDown.addSelectionListener(this);

		Composite onlineListComposite = new Composite(area, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		onlineListComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 4;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		onlineListComposite.setLayout(layout);

		chkRecentList = new Button(onlineListComposite, SWT.RADIO);
		chkRecentList
				.setText(Messages.PathHandlingPreferencePage_chkRecentList_Label);
		chkRecentList.addSelectionListener(this);

		spnQuantity = new Spinner(onlineListComposite, SWT.BORDER);
		gd = new GridData();
		gd.widthHint = 15;
		spnQuantity.setLayoutData(gd);
		spnQuantity.setMaximum(QUANTITY_MAX);
		spnQuantity.setMinimum(QUANTITY_MIN);
		spnQuantity.setPageIncrement(QUANTITY_PAGE_INCREMENT);
		spnQuantity.setIncrement(QUANTITY_INCREMENT);
		spnQuantity.setSelection(data.recentCount);
		spnQuantity.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (spnQuantity.getSelection() == 1)
					lblQuantity
							.setText(Messages.PathHandlingPreferencePage_lblQuantity_One_Label);
				else
					lblQuantity
							.setText(Messages.PathHandlingPreferencePage_lblQuantity_Many_Label);
			}
		});

		spnQuantity.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			
			}

			public void widgetSelected(SelectionEvent e) {
			}
		});

		lblQuantity = new Label(onlineListComposite, SWT.NONE);
		if (spnQuantity.getSelection() == 1)
			lblQuantity
					.setText(Messages.PathHandlingPreferencePage_lblQuantity_One_Label);
		else
			lblQuantity
					.setText(Messages.PathHandlingPreferencePage_lblQuantity_Many_Label);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 5;
		lblQuantity.setLayoutData(gd);

		btnClearList = new Button(onlineListComposite, SWT.PUSH);
		btnClearList
				.setText(Messages.PathHandlingPreferencePage_btnClear_Label);
		calculateButtonSize(btnClearList);
		btnClearList.addSelectionListener(this);

		initialStates();
		if(chkCustomList.getSelection() == true || lstFolders.getItemCount() > 0 )
			lstFolders.setSelection(0);
		dialogChanged();
		noDefaultAndApplyButton();
		return area;
	}

	private void initialStates() {
		if (data.usePredefined)
			chkCustomList.setSelection(true);
		else
			chkRecentList.setSelection(true);
	}

	private void doAddThemeAction() {
		DirectoryDialog fd = new DirectoryDialog(getShell(), SWT.OPEN);
		fd.setText(Messages.PathHandlingPreferencePage_MsgBox_Text);
		fd.setMessage(Messages.PathHandlingPreferencePage_MsgBox_Message);
		IContainer rootElement = ResourcesPlugin.getWorkspace().getRoot();
		if (lstFolders.getSelectionCount() > 0 && new File(lstFolders.getSelection()[0]).exists())
				fd.setFilterPath(lstFolders.getSelection()[0]);
		else {
			IPath location = rootElement.getLocation();
			String name = location.toOSString();
			fd.setFilterPath(name);
		}
		String path = fd.open();
		if (path != null && new File(path).exists()) {
			int position = -1;
			if (lstFolders.getItemCount() > 0) {
				String[] items = lstFolders.getItems();				
				for (int i = lstFolders.getItemCount() - 1; i >= 0; i--) {
					if (items[i].equals(path)) {
						position = i;
						break;
					}
				}				
			}
			if (position == -1) {
				lstFolders.add(path);
				lstFolders.setSelection(lstFolders.getItemCount() - 1);
			} else {
				lstFolders.setSelection(position);
			}
			dialogChanged();
		}
	}

	private void doRemoveThemeAction() {
		int index = lstFolders.getSelectionIndex();
		lstFolders.remove(lstFolders.getSelectionIndex());
		if (btnRemove != null)
			try {
				lstFolders.setSelection(--index);
				dialogChanged();
			} catch (Exception e) {
				btnRemove.setEnabled(false);
			}
	}

	private void doMoveUpAction() {
		int index = lstFolders.getSelectionIndex();
		String path = lstFolders.getItem(index);
		lstFolders.remove(index);
		lstFolders.add(path, index - 1);
		lstFolders.select(index - 1);
		lstFolders.showSelection();
		dialogChanged();
	}

	private void doMoveDownAction() {
		int index = lstFolders.getSelectionIndex();
		String path = lstFolders.getItem(index);
		lstFolders.remove(index);
		lstFolders.add(path, index + 1);
		lstFolders.select(index + 1);
		lstFolders.showSelection();
		dialogChanged();
	}

	private void doClearHistoryAction() {
		data.clearRecentPathList();
	}

	@Override
	protected Control createContents(Composite parent) {
		return createDialogArea(parent);
	}

	public void init(IWorkbench workbench) {
		data = PathHandlingConfig.load();
	}

	@Override
	public boolean performOk() {
		if (chkCustomList.getSelection())
			data.usePredefined = true;
		else
			data.usePredefined = false;
		data.predefinedCount = lstFolders.getItemCount();
		data.predefinedPathList.clear();
		if (lstFolders.getItemCount() > 0)
			for (String a : lstFolders.getItems())
				data.predefinedPathList.add(a);
		data.recentCount = spnQuantity.getSelection();
		data.save();

		return true;
	}

	public void widgetSelected(SelectionEvent e) {
		try {
			if (e.getSource() == btnAdd) {
				doAddThemeAction();
			} else if (e.getSource() == btnRemove) {
				doRemoveThemeAction();
			} else if (e.getSource() == btnUp) {
				doMoveUpAction();
			} else if (e.getSource() == btnDown) {
				doMoveDownAction();
			} else if (e.getSource() == lstFolders) {
				dialogChanged();
			} else if (e.getSource() == chkCustomList) {
				chkRecentList.setSelection(false);
				dialogChanged();
			} else if (e.getSource() == chkRecentList) {
				chkCustomList.setSelection(false);
				dialogChanged();
			} else if (e.getSource() == btnClearList) {
				doClearHistoryAction();
			}
		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	private void dialogChanged() {
		if (chkCustomList.getSelection()) {
			lstFolders.setEnabled(true);
			spnQuantity.setEnabled(false);
			btnClearList.setEnabled(false);
			btnAdd.setEnabled(true);
			btnRemove.setEnabled(lstFolders.getSelectionIndex() > -1);
			btnUp.setEnabled(lstFolders.getSelectionIndex() > 0);
			btnDown.setEnabled(lstFolders.getSelectionIndex() > -1
					&& lstFolders.getSelectionIndex() < (lstFolders
							.getItemCount() - 1));
			if (lstFolders.getItemCount() < 1) {
				updateStatus(Messages.PathHandlingPreferencePage_Error_No_Folders);
				return;
			}
		} else if (chkRecentList.getSelection()) {
			lstFolders.setEnabled(false);
			spnQuantity.setEnabled(true);
			btnClearList.setEnabled(true);
			btnAdd.setEnabled(false);
			btnRemove.setEnabled(false);
			btnUp.setEnabled(false);
			btnDown.setEnabled(false);
		}
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
