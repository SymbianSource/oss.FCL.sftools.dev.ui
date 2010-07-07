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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.framework.internal.core.BundleHost;
import org.osgi.framework.Bundle;

import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.resource.core.Activator;
import com.nokia.tools.resource.util.FileUtils;

public class UninstallPluginOperation implements IRunnableWithProgress {
	private Bundle bundle;

	public UninstallPluginOperation(Bundle bundle) {
		this.bundle = bundle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		try {
			monitor.beginTask(MessageFormat.format(
					WizardMessages.Plugin_Installation_Uninstall_Task,
					new Object[] { bundle.getSymbolicName() }), 2000);
			Activator.getDefault().uninstallBundle(bundle);
			BundleHost b = (BundleHost) bundle;
			File file = ((BaseData) b.getBundleData()).getBundleFile()
					.getBaseFile();
			if (file.isFile()) {
				file.delete();
			} else {
				FileUtils.deleteDirectory(file, monitor);
			}
			ThemePlatform.release();
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		}
		monitor.done();
	}

}
