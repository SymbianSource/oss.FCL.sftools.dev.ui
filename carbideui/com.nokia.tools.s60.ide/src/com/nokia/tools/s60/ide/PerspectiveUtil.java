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
package com.nokia.tools.s60.ide;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.ui.ide.IDEUtil;

/**
 * Activating perspective manager, accessed from various places to do the task
 * Linked with settings in preferences so based on user settings activate or not
 * the perspective

 */
public class PerspectiveUtil {

	private static boolean shouldSwitchperspective() {
		String str = S60WorkspacePlugin.getDefault().getPreferenceStore()
				.getString(IS60IDEConstants.PREF_ACTIVATE_PERSPECTIVE);
		if (str.equals(MessageDialogWithToggle.ALWAYS))
			return true;
		else if (str.equals(MessageDialogWithToggle.NEVER))
			return false;
		MessageDialogWithToggle dialog = MessageDialogWithToggle
				.openYesNoQuestion(getActiveWorkbenchWindow().getShell(),
						S60IDEMessages.ConfirmPerspectiveSwitchTitle,
						S60IDEMessages.InfoWhyToLounchPerspective, null, false,
						S60WorkspacePlugin.getDefault().getPreferenceStore(),
						IS60IDEConstants.PREF_ACTIVATE_PERSPECTIVE); 
		boolean bret = (dialog.getReturnCode() == IDialogConstants.YES_ID);
		return bret;
	}

	public static void openPerspective(final String perspectiveId) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!shouldSwitchperspective())
					return;
				try {

					PlatformUI.getWorkbench().showPerspective(perspectiveId,
							getActiveWorkbenchWindow()); 
				} catch (WorkbenchException e) {
					System.out.println("Perspective could not be opened. " + e); 
				}
			}

		});
	}

	/**
	 * During startup IWorkbench.getActiveWorkbenchWindow returns null and then
	 * method returns any - first workbenchwindow The main reason for this is to
	 * get safe Shell to display
	 * 
	 * @return IWorkbench.getActiveWorkbenchWindow and if this one is null
	 *         returns a
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return IDEUtil.getActiveWorkbenchWindow();
	}

	public static IEditorPart getActiveEditorPart() {
		IWorkbenchPage page = PerspectiveUtil.getActiveWorkbenchWindow()
				.getActivePage();
		if (null != page) {
			return EclipseUtils.getActiveSafeEditor();
		}
		return null;
	}

}
