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
package com.nokia.tools.theme.s60.packaging.deploy;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.nokia.tools.deploy.emul.IDeployer;
import com.nokia.tools.packaging.IPackagingProcessor;
import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingContext;
import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.theme.s60.packaging.pkg.InstallFile;
import com.nokia.tools.theme.s60.packaging.pkg.PackageFile;

/**
 * Based on pkg file, parsing content from it
 */
public class Deployment2Emulator implements IDeployer, IPackagingProcessor {
	/**
	 * Constructs a new deployer.
	 * 
	 * @param emulatorCDrivePath the path to the emulator's C drive.
	 */
	public Deployment2Emulator(String emulatorCDrivePath) {
		this.emulatorCDrivePath = emulatorCDrivePath;
	}

	private String emulatorCDrivePath;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.packaging.IPackagingProcessor#process(com.nokia.tools.packaging.PackagingContext)
	 */
	public Object process(PackagingContext context) throws PackagingException {
		deploy(getPkgFile(context));
		return context.getInput();
	}

	
	private String getPkgFile(PackagingContext context) {
		// take sis temp file and if there is no as other step take sis file
		String sisFile = (String) context
				.getAttribute(PackagingAttribute.sisTempFile.name());
		if (null == sisFile)
			sisFile = (String) context.getAttribute(PackagingAttribute.sisFile
					.name());
		Path sisPath = new Path(sisFile);
		Path pkgFile = (Path) (new Path((String) context
				.getAttribute(PackagingAttribute.workingDir.name())))
				.append(sisPath.lastSegment().replace(".sis", ".pkg"));
		return pkgFile.toString();
	}

	/*
	 * (non-Javadoc) From pkg file parses position of contained files and copies
	 * them to specified emulator folder
	 * 
	 * @see com.nokia.tools.deploy.emul.IDeployer#deploy(java.lang.String)
	 */
	public void deploy(String filename) {
		PackageFile pkg = new PackageFile();
		try {
			pkg.load(filename.replace(".sis", ".pkg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Path installFolder = new Path(emulatorCDrivePath);
		if (!installFolder.toFile().exists()) {
			Thread.dumpStack();
			FileUtils.isFileValid(emulatorCDrivePath);
			return;
		}

		for (Object statement : pkg.getStatements(InstallFile.class)) {
			InstallFile file = (InstallFile) statement;
			String destFile = file.getDestination().replaceFirst("!:", "");
			Path destination = (Path) installFolder.clone();
			destination = (Path) destination.append(new Path(destFile));
			if (file.getSource().length() == 0)
				continue;
			IPath source = new Path(file.getSource());
			if (!source.isAbsolute())
				source = new Path(filename).removeLastSegments(1)
						.append(source);
			try {
				FileUtils.copyFile(source.toString(), destination.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
