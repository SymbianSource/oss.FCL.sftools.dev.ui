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
package com.nokia.tools.s60.editor.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.s60.editor.actions.NinePieceConvertAction;
import com.nokia.tools.s60.editor.ui.views.Messages;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;

/**
 * Dialog that appears when user convert 9-piece to single, and element has
 * already defined single-piece graphics.
*/
public class NinePieceOperationConfirmDialog extends
		com.nokia.tools.ui.branding.util.BrandedTitleAreaDialog {

	public static final int TYPE_2NINE = 9;

	public static final int TYPE_2SINGLE = 1;

	private int type;

	public NinePieceOperationConfirmDialog(Shell shell, int type) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.type = type;
	}

	private String getPrefKey() {
		return type == TYPE_2NINE ? IMediaConstants.PREF_SINGLE_PIECE_2NINE_ASK
				: IMediaConstants.PREF_NINE_PIECE_2SINGLE_ASK;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#
	 *      createDialogArea(org.eclipse.swt.widgets.Composite) Here we fill the
	 *      center area of the dialog
	 */
	protected Control createDialogArea(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent.getParent(),
				NinePieceConvertAction.HLP_CTX);

		Composite area = (Composite) super.createDialogArea(parent);

		// Create new composite as container
		final Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 13;
		layout.marginWidth = 13;
		layout.verticalSpacing = 7;
		composite.setLayout(layout);

		Composite container = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		container.setLayoutData(gd);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;

		setTitle(type == TYPE_2NINE ? Messages.NinePieceOperationConfirm_banner_title_9
				: Messages.NinePieceOperationConfirm_banner_title_1);
		setMessage(type == TYPE_2NINE ? Messages.NinePieceOperationConfirm_text2_9
				: Messages.NinePieceOperationConfirm_text2_1);

		Label ft = new Label(container, SWT.WRAP);
		ft
				.setText(type == TYPE_2NINE ? Messages.NinePieceOperationConfirm_banner_message_9
						: Messages.NinePieceOperationConfirm_banner_message_1);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 350;
		ft.setLayoutData(gd);

		// preserve mask check
		final Button checkbox = new Button(composite, SWT.CHECK);
		checkbox.setText(Messages.NinePieceOperationConfirm_checkLabel);
		checkbox.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean state = checkbox.getSelection();

				IPreferenceStore iPreferenceStore = UtilsPlugin.getDefault()
						.getPreferenceStore();
				iPreferenceStore.setValue(getPrefKey(), state);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		checkbox
				.setToolTipText(Messages.NinePieceOperationConfirm_checkLabelTooltip);
		
		IPreferenceStore iPreferenceStore = UtilsPlugin.getDefault()
				.getPreferenceStore();
		checkbox.setSelection(iPreferenceStore.getBoolean(getPrefKey()));

		Composite container2 = new Composite(area, SWT.NONE);
		container2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout = new GridLayout();
		container2.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;

		final Label separator = new Label(container2, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return area;
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			/*	|| buttonId == IDialogConstants.NO_ID) {*/
			setReturnCode(buttonId);
			close();
		} else
			super.buttonPressed(buttonId);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		/*createButton(parent, IDialogConstants.YES_ID,
				IDialogConstants.YES_LABEL, true);
		createButton(parent, IDialogConstants.NO_ID, IDialogConstants.NO_LABEL,
				false);*/
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected String getTitle() {
		return type == TYPE_2NINE ? Messages.NinePieceOperationConfirm_title_9
				: Messages.NinePieceOperationConfirm_title_1;
	}

	@Override
	protected ImageDescriptor getBannerIconDescriptor() {
		IBrandingManager manager = BrandingExtensionManager
				.getBrandingManager();
		ImageDescriptor bannerIcon = null;
		if (manager != null) {
			bannerIcon = (type == TYPE_2NINE ? S60WorkspacePlugin
					.getImageDescriptor("icons/wizban/switch_9_element.png")
					: S60WorkspacePlugin
							.getImageDescriptor("icons/wizban/switch_single_element.png"));
		}
		return bannerIcon;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}

}
