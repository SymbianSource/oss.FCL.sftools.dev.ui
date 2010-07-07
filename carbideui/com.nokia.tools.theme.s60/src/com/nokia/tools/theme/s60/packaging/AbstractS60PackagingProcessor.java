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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.packaging.PackagingPlugin;
import com.nokia.tools.packaging.IPackagingProcessor.AbstractPackagingProcessor;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.theme.s60.packaging.pkg.EmbeddedSis;
import com.nokia.tools.theme.s60.packaging.pkg.InstallFile;
import com.nokia.tools.theme.s60.packaging.pkg.PackageFile;
import com.nokia.tools.theme.s60.packaging.util.SymbianUtil;

public abstract class AbstractS60PackagingProcessor extends
		AbstractPackagingProcessor {

	/**
	 * Executes the command with the command line arguments in the packaging
	 * plugin's runtime directory.
	 * 
	 * @param executable the executable to be invoked.
	 * @param commandList the command line arguments.
	 * @throws PackagingException if command execution failed.
	 */
	protected void exec(String executable, List<String> commandList)
			throws PackagingException {
		SymbianUtil.exec(PackagingPlugin.getDefault(), executable,
				commandList, new File(getWorkingDir()));
	}

	
	protected void copyExecutableFile(URL executableURL, String executableName)
			throws PackagingException{
		String destinationPath = SymbianUtil.getRuntimeDir(PackagingPlugin.getDefault()) + 
		                         File.separator +  executableName;
		try {
			FileUtils.copyFile(executableURL.openStream(), new File(destinationPath));
		} catch (IOException e) {
			throw new PackagingException(e);
		} 
	}
	
	/**
	 * Adds embedded files to given pkg file.
	 * 
	 * @param pkg the symbian package file.
	 * @throws PackagingException if error occurred.
	 */
	protected void addEmbeddedFiles(PackageFile pkg)
			throws PackagingException {
		String[] embeddedFiles = getEmbeddedFiles();
		String workingDir = getWorkingDir();

		if (embeddedFiles != null) {
			for (String embedded : embeddedFiles) {
				int uid = 0;
				try {
					uid = SymbianUtil.readUID(new File(workingDir, embedded));
				} catch (Exception e) {
				}
				if (uid != 0) {
					pkg.addStatement(new EmbeddedSis(embedded, "0x"
							+ Integer.toHexString(uid)));
				} else {
					pkg.addStatement(new InstallFile(embedded, "!:"
							+ SymbianUtil.DEFAULT_INSTALL_FOLDER
							+ new File(embedded).getName()));
				}
			}
		}
	}
}
