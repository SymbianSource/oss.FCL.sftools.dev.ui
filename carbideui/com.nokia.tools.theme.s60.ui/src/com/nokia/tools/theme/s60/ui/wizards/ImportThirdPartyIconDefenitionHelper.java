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
package com.nokia.tools.theme.s60.ui.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.nokia.tools.theme.s60.S60ThemePlugin;
import com.nokia.tools.theme.s60.model.tpi.DefinedIcons;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIcon;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconChangesPublisher;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconManager;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconType;

public class ImportThirdPartyIconDefenitionHelper {

	
	public static void importThirdpartyIconDefenition(IFile file) {
		if (file.exists()) {
			try {
				DefinedIcons model = ThirdPartyIconManager.loadThirdParyIcons(file.getContents(), ThirdPartyIconType.TOOL_SPECIFIC);
				DefinedIcons current = ThirdPartyIconManager.getToolSpecificThirdPartyIcons(true);
				DefinedIcons oldBackup = current.clone();
				Boolean retainCurrentToolsTPIDefinitions = null;
				for (ThirdPartyIcon currentIcon : model) {
					
					String id = currentIcon.getId(); // Id cannot be null.
					ThirdPartyIcon icon = current.getIconById(id);
					
					if(icon == null){
						// Icon with this Id is not present and so we can 
						current.add(currentIcon);
					}
					else{
						if(icon.equals(currentIcon)){
							continue;
						}
						if(retainCurrentToolsTPIDefinitions == null){
							Shell shell = Display.getCurrent().getActiveShell();
							retainCurrentToolsTPIDefinitions = MessageDialog.openQuestion(shell, title, message);							
						}

						if(!retainCurrentToolsTPIDefinitions){
								updateTPIDefinition(currentIcon, icon);
						}
					}
				}
				ThirdPartyIconManager.storeThirdPartyIcons(current, ThirdPartyIconManager.getToolSpecificThirdPartyIconUrl());
				ThirdPartyIconChangesPublisher.refresh3rdPartyIcons(oldBackup, current, false, null, true);
				file.delete(true, new NullProgressMonitor());	
			} catch (Exception e) {
				S60ThemePlugin.error(e);
			}
		}
	}
	
	private static void updateTPIDefinition(ThirdPartyIcon source, ThirdPartyIcon target){
		target.setId(source.getId());
		target.setName(source.getName());
		target.setAppUid(source.getAppUid());
		target.setMajorId(source.getMajorId());
		target.setMinorId(source.getMinorId());
	}

	private static final String title = "Third Party Icon Conflict";
	private static final String message = "Conflicting data for tool specific third party elements found.\n"+
										  "Click 'Yes' to retain tools information. " +
										  "Click 'No' to replace with themes information.";
}
