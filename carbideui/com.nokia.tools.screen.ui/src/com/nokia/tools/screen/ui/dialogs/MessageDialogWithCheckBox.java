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
package com.nokia.tools.screen.ui.dialogs;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Open a customized messagebox with normal message and optionally any
 * compinations of message with a link to preferences, checkbox with text and
 * access to help system via Help -button.
 * 
 */
public class MessageDialogWithCheckBox extends MessageDialog {

	private String chkButtonMessage;

	private boolean chkButtonState;

	private String helpID;

	private String linkMessage;

	private String linkAddress;

	private Link lnkMessage;

	private Button chkButton;

	private IAction pendingAction;

	public MessageDialogWithCheckBox(Shell parentShell, String dialogTitle,
			Image dialogTitleImage, String dialogMessage,
			String chkButtonMessage, boolean chkButtonState, String helpID,
			String linkMessage, String linkAddress, int dialogImageType,
			String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
				dialogImageType, dialogButtonLabels, defaultIndex);
		this.chkButtonMessage = chkButtonMessage;
		this.chkButtonState = chkButtonState;
		this.helpID = helpID;
		this.linkMessage = linkMessage;
		this.linkAddress = linkAddress;
	}

	/**
	 * @return Returns the pendingAction.
	 */
	public IAction getPendingAction() {
		return pendingAction;
	}

	/**
	 * @param pendingAction The pendingAction to set.
	 */
	public void setPendingAction(IAction pendingAction) {
		this.pendingAction = pendingAction;
	}

	@Override
	protected Control createMessageArea(Composite composite) {
		super.createMessageArea(composite);

		if (helpID != null)
			PlatformUI.getWorkbench().getHelpSystem().setHelp(
					composite.getParent() == null ? composite : composite
							.getParent(), helpID);

		if (linkMessage != null && linkAddress != null) {
			GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER
					| GridData.VERTICAL_ALIGN_BEGINNING);
			gd.verticalSpan = 2;
			imageLabel.setLayoutData(gd);

			lnkMessage = new Link(composite, SWT.WRAP);
			lnkMessage.setText(linkMessage);
			gd = new GridData(GridData.GRAB_HORIZONTAL
					| GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_BEGINNING);
			gd.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			gd.verticalIndent = -6;
			lnkMessage.setLayoutData(gd);
			lnkMessage.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					close();

					PreferenceDialog prefdlg = PreferencesUtil
							.createPreferenceDialogOn(Display.getCurrent()
									.getActiveShell(), linkAddress,
									new String[] { linkAddress }, null);
					prefdlg.open();

					if (pendingAction != null) {
						pendingAction.run();
					}
				}

				public void widgetDefaultSelected(SelectionEvent e) {
			
				}
			});

		}
		return composite;
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		if (chkButtonMessage != null) {
			chkButton = new Button(parent, SWT.CHECK | SWT.WRAP);
			chkButton.setText(chkButtonMessage);
			chkButton.setSelection(chkButtonState);
			chkButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					chkButtonState = chkButton.getSelection();
				}

				public void widgetDefaultSelected(SelectionEvent e) {
					
				}
			});
			if (helpID != null) {
				// Dummy label to fill extra line
				new Label(parent, SWT.NONE);
			}
			return parent;
		}
		return null;
	}

	@Override
	public void create() {
		super.create();
		this.getButton(0).setFocus();
	}

	public boolean getCheckBoxValue() {
		return chkButtonState;
	}

	public void setCheckBoxValue(boolean value) {
		chkButtonState = value;
		chkButton.setSelection(chkButtonState);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (helpID != null) {
			Image helpImage = JFaceResources.getImage(DLG_IMG_HELP);
			if (helpImage != null) {
				createHelpImageButton(parent, helpImage);
				((GridLayout) parent.getLayout()).numColumns++;
				// Dummy label to fill space
				Label label = new Label(parent, SWT.NONE);
				GridData gd = new GridData(GridData.FILL_HORIZONTAL
						| GridData.HORIZONTAL_ALIGN_BEGINNING);
				label.setLayoutData(gd);
				super.createButtonsForButtonBar(parent);
				((GridLayout) parent.getLayout()).makeColumnsEqualWidth = false;
				((GridData) parent.getLayoutData()).widthHint = 420;
			}
		} else
			super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
	}

	private ToolBar createHelpImageButton(Composite parent, Image image) {
		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
		((GridLayout) parent.getLayout()).numColumns++;
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		toolBar.setLayoutData(gd);
		final Cursor cursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		toolBar.setCursor(cursor);
		toolBar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				cursor.dispose();
			}
		});
		ToolItem item = new ToolItem(toolBar, SWT.NONE);
		item.setImage(image);
		item.setToolTipText(JFaceResources.getString("helpToolTip")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp(helpID);
			}
		});
		return toolBar;
	}

	/**
	 * Convenience method to open a customized messagebox with normal message
	 * and optionally any compinations of message with a link to preferences,
	 * checkbox with text and access to help system via Help -button.
	 * 
	 * @param parent the parent shell of the dialog, or <code>null</code> if
	 *        none
	 * @param title the dialog's title, or <code>null</code> if none
	 * @param message the message
	 * @param chkButtonMessage the message for the checkbox, or
	 *        <code>null</code> if none
	 * @param chkButtonState the initial state for the checkbox
	 * @param helpID help ID, or <code>null</code> if none
	 * @param linkMessage the message including a link, or <code>null</code>
	 *        if none
	 * @param linkAddress the preference address for the link, or
	 *        <code>null</code> if none
	 * @return <code>true</code> if the user presses the OK button,
	 *         <code>false</code> otherwise
	 */
	public static boolean openQuestion(Shell parent, String title,
			String message, String chkButtonMessage, boolean chkButtonState,
			String helpID, String linkMessage, String linkAddress) {
		MessageDialog dialog = new MessageDialogWithCheckBox(parent, title,
				null, message, chkButtonMessage, chkButtonState, helpID,
				linkMessage, linkAddress, QUESTION,
				new String[] { IDialogConstants.YES_LABEL,
						IDialogConstants.NO_LABEL }, 0);
		return dialog.open() == 0;
	}
}
