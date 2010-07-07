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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.s60.ide.IS60IDEConstants;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.views.ViewMessages;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.screen.ui.dialogs.MessageDialogWithCheckBox;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;

public class WarningMessageDialogs {

	public static void noPreviewAvailableMessageBox() {
		IPreferenceStore store = S60WorkspacePlugin.getDefault()
				.getPreferenceStore();
		Boolean showState = store
				.getBoolean(IS60IDEConstants.PREF_NO_PREVIEW_HIDE_MESSAGEBOX);
		if (showState)
			return;

		IBrandingManager branding = BrandingExtensionManager
				.getBrandingManager();
		Image image = null;
		if (branding != null) {
			image = branding.getIconImageDescriptor().createImage();
		}
		MessageDialog messageDialog = new MessageDialogWithCheckBox(PlatformUI
				.getWorkbench().getDisplay().getActiveShell(),
				ViewMessages.IconView_Preview_MsgBox_Title, image,
				ViewMessages.IconView_Preview_MsgBox_Message,
				ViewMessages.IconView_Preview_MsgBox_ShowAgain, false, null,
				null, null, 2, new String[] { IDialogConstants.OK_LABEL },
				0);

		messageDialog.open();
		
		if (image != null) {
			image.dispose();
		}

		store.setValue(IS60IDEConstants.PREF_NO_PREVIEW_HIDE_MESSAGEBOX,
				((MessageDialogWithCheckBox) messageDialog).getCheckBoxValue());

	}

}
