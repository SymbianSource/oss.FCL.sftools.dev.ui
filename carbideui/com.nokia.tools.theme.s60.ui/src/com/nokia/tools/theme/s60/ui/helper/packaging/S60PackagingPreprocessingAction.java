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
package com.nokia.tools.theme.s60.ui.helper.packaging;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.screen.ui.extension.IPackagingPreprocessingAction;
import com.nokia.tools.theme.content.ThemeContent;
import com.nokia.tools.theme.s60.model.S60Theme;
import com.nokia.tools.theme.s60.model.tpi.TPIconConflictEntry;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconManager;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconWrapper;

/**
 * This class implements the IPackagingPreprocessingAction and defines the pre-processing
 * prior to the packagaing for an S60 theme. The pre-processing that is done here
 * is to check if there are any third party icons conflict in the theme tried to be packaged
 * and if there any it shows the preference page for the third party icons to resolve the conflicts.
 * Only if the conflicts are fully resolved this pre-packaging processor will notify that the packaging
 * can be proceeded with, else will return false.
 * 
 *
 */

public class S60PackagingPreprocessingAction implements IPackagingPreprocessingAction {

	/**
	 * Checks if the passed content is an S60Theme. If not then it simply returns true to
	 * indicate that this packaging pre-processor has no issues stopping it from packaging.
	 * If its an S60 theme it is checked to see if there any Third Party Icons conflict and
	 * if yes it shows the preference page for the resolution of the TPI conflicts and if the
	 * conflicts are resolved properly and no more conflicts are present then the method 
	 * returns true. If still there are conflicts present to be resolved then return false.
	 */
	public boolean performPackagingPreProcessing(IContent content) {
		
		if(content == null){
			return false;
		}
		else if(content instanceof ThemeContent){
			ThemeBasicData theme = ((ThemeContent) content).getData();
			if(theme instanceof S60Theme){
				S60Theme s60Theme = (S60Theme)theme;
				Map<ThirdPartyIconWrapper, List<TPIconConflictEntry>> conflictingIcons = 
					ThirdPartyIconManager.getConflictingIconListForPackaging(s60Theme);
				if (conflictingIcons != null && !conflictingIcons.isEmpty()) {
					if(showThirdPartyIconPreferencePage()){
						return true;
					}
					else{
						return false;
					}
				}
				else{
					return true;
				}
			}
			else{
				// Since we do not understand the content type, we allow to proceed for 
				// packaging as it may be processed by someone else and so we return true.
				return true;				
			}
		}
		else{
			// Since we do not understand the content type, we allow to proceed for 
			// packaging as it may be processed by someone else and so we return true.
			return true;
		}
	}

	private boolean showThirdPartyIconPreferencePage(){
		String linkAddress = "com.nokia.tools.theme.s60.ui.preferences.ThirdPartyIconsPrefPage";
		PreferenceDialog prefdlg = PreferencesUtil.createPreferenceDialogOn(
		    Display.getCurrent().getActiveShell(), linkAddress,
		    new String[] { linkAddress }, null);

		if (MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),
		    "Third Party Icons Conflict", "Cannot proceed with packaging. "
		        + "The third party icon definitions for the current theme has conflicts. Resolve them now? ")) {
			int openValue = prefdlg.open();
			prefdlg.close();
			prefdlg = null;
			return openValue == IDialogConstants.OK_ID;
		}
		prefdlg.close();
		prefdlg = null;
		return false;
	}
	
}
