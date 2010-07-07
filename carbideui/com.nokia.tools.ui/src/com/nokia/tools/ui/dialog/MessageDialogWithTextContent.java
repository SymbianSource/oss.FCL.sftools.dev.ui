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
package com.nokia.tools.ui.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;

public class MessageDialogWithTextContent extends MessageDialog {
	/**
	 * Size of the text in lines.
	 */
	private static final int TEXT_LINE_COUNT = 15;
	/**
	 * Text width
	 */
	private static final int TEXT_WIDTH = 500;

	private String content;

	public MessageDialogWithTextContent(Shell parentShell, String dialogTitle,
			Image dialogTitleImage, String dialogMessage, String content,
			int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
				dialogImageType, dialogButtonLabels, defaultIndex);
		this.content = content;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createCustomArea(Composite parent) {
		if (content == null) {
			return null;
		}

		Text text = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY);
		text.setFont(parent.getFont());

		text.setText(content);

		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL);

		data.widthHint = TEXT_WIDTH;
		data.heightHint = text.getLineHeight() * TEXT_LINE_COUNT;
		data.horizontalSpan = 2;
		text.setLayoutData(data);
		return text;
	}

	/**
	 * Convenience method to open a standard error dialog.
	 * 
	 * @param parent the parent shell of the dialog, or <code>null</code> if
	 *        none
	 * @param title the dialog's title, or <code>null</code> if none
	 * @param message the message
	 * @param content the message content.
	 */
	public static void openError(Shell parent, String title, String message,
			Throwable e) {
		String errorMessage = null;
		Throwable ex = e;
		if (e != null) {
			errorMessage = e.getMessage();
			if (e.getCause() != null) {
				errorMessage = e.getCause().getMessage();
				ex = e.getCause();
			}
		}
		openError(parent, title, message
				+ (errorMessage == null ? "" : "\n\n" + errorMessage),
				errorMessage != null ? null : StringUtils.dumpThrowable(ex));
	}

	/**
	 * Convenience method to open a standard error dialog.
	 * 
	 * @param parent the parent shell of the dialog, or <code>null</code> if
	 *        none
	 * @param title the dialog's title, or <code>null</code> if none
	 * @param message the message
	 * @param content the message content.
	 */
	public static void openError(Shell parent, String title, String message,
			String content) {
		IBrandingManager branding = BrandingExtensionManager.getBrandingManager();
		Image image = null;
		if (branding != null) {
			image = branding.getIconImageDescriptor().createImage();
		}
		MessageDialog dialog = new MessageDialogWithTextContent(parent, title,
				image, message, content, ERROR,
				new String[] { IDialogConstants.OK_LABEL }, 0);
		dialog.open();
		if (image != null) {
			image.dispose();
		}
		return;
	}
}
