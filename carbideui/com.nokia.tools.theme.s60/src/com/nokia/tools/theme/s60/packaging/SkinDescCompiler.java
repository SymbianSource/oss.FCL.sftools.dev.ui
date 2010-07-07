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
package com.nokia.tools.theme.s60.packaging;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingConstants;
import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.packaging.PackagingMessages;
import com.nokia.tools.packaging.PackagingPlugin;
import com.nokia.tools.theme.s60.IThemeConstants;
import com.nokia.tools.theme.s60.S60ThemeContent;
import com.nokia.tools.theme.s60.model.S60Theme;

/**
 * This processor generates the files that will be used by the successive
 * processors based on the theme description file.<br/> Mandator attributes:
 * <ul>
 * <li>{@link PackagingAttribute#input} - the theme description file
 * <li>{@link PackagingAttribute#themeName}
 * <li>{@link PackagingAttribute#platform}
 * <li>{@link PackagingAttribute#themeDRM}
 * </ul>
 * Optional attributes:
 * <ul>
 * <li>{@link PackagingAttribute#themeItemListFile}
 * </ul>
 * Output: name of the content.
 */
public class SkinDescCompiler extends AbstractS60PackagingProcessor {
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.packaging.IPackagingProcessor.AbstractPackagingProcessor#processSpi()
	 */
	@Override
	protected Object processSpi() throws PackagingException {
		if (PackagingPlugin.getDefault().getPreferenceStore().getBoolean(
				PackagingConstants.PREF_BREAK_PACKAGING_BEFORE_MAKESIS)) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(PlatformUI.getWorkbench()
							.getDisplay().getActiveShell(),
							"Packaging - AknSkinDescCompiler paused",
							"Press OK to continue with skin compiling of:"
									+ getInput());
				}
			});
		}

		if (!Platform.OS_WIN32.equals(Platform.getOS())
				&& !Platform.OS_MACOSX.equals(Platform.getOS())) {
			throw new PackagingException(PackagingMessages.Error_osNotSupported);
		}
		String input = getInput(); // theme description file
		String themePackageName = checkThemePackageName();
		String itemListFile = getThemeItemListFile();
		boolean drm = isDRM();

		if (input == null) {
			throw new PackagingException(
					PackagingMessages.Error_themeDescriptionFileMissing);
		}

		List<String> list = new ArrayList<String>();
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			// mac doesn't support these
			if (drm) {
				list.add("--DRM");
			}
			if (itemListFile != null) {
				list.add("-i" + itemListFile);
			}
		}
		list.add(input);
		list.add(themePackageName);

		String exe = null;
		String modelID = null;
		String secondaryModelID = null;

        modelID = this.context.getAttribute(PackagingAttribute.primaryModelId.name()).toString();//theme.getModelId();
        secondaryModelID = this.context.getAttribute(PackagingAttribute.secondaryModelId.name()).toString();//theme.getModelId();
        URL packagingExecutablePath = PackagingExecutableProvider.getPackagingExecutablePath(modelID, PackagingExecutableType.SKIN_COMPILER, false);
        if(packagingExecutablePath == null && secondaryModelID != null){
        	packagingExecutablePath = PackagingExecutableProvider.getPackagingExecutablePath(secondaryModelID, PackagingExecutableType.SKIN_COMPILER, true);
        }
        if(packagingExecutablePath != null){
        	exe = PackagingExecutableType.SKIN_COMPILER.name() + ".exe";
        	copyExecutableFile(packagingExecutablePath, exe);
        }

		
		if(exe == null){
			if (Platform.OS_WIN32.equals(Platform.getOS())) {
				exe = "AknSkinDescCompiler.EXE";
				
			} else {
				exe = "AknSkinDescCompiler";
			}
		}
		exec(exe, list);
		return themePackageName;
	}
}
