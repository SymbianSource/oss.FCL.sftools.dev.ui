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
package com.nokia.tools.s60.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.service.datalocation.Location;

import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.resource.core.Activator;
import com.nokia.tools.resource.util.FileUtils;

public class InstallPluginOperation implements IRunnableWithProgress {
	private static final String PLUGINS_FOLDER = "plugins";

	private File file;

	public InstallPluginOperation(File file) {
		this.file = file;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		Location location = Platform.getInstallLocation();

		File dir = FileUtils.getFile(location.getURL());
		File pluginDir = new File(dir, PLUGINS_FOLDER);
		if (dir.isDirectory()) {
			try {
				ZipFile zip = null;
				int totalWork = 1;
				try {
					zip = new ZipFile(file);
					totalWork = zip.size();
				} finally {
					zip.close();
				}
				monitor.beginTask(MessageFormat.format(
						WizardMessages.Plugin_Installation_Install_Task,
						new Object[] { file.getAbsolutePath(),
								pluginDir.getAbsolutePath() }), totalWork);

				// extract because lots of theme APIs are dependant on the
				// actual files, later might change them to url for flexibility
				String name = file.getName();
				int index = name.lastIndexOf(".");
				if (index > 0) {
					name = name.substring(0, index);
				}
				File targetDir = new File(pluginDir, name);
				FileUtils.unzip(file, targetDir, monitor);

				// File targetFile = new File(pluginDir, file.getName());
				// FileUtils.copyFile(file, targetFile, monitor);
				Activator.getDefault().installBundle(targetDir.getName());
				// releases all cached information
				ThemePlatform.release();
			} catch (Exception e) {
				throw new InvocationTargetException(e);
			}
			monitor.done();
		}
	}
}
