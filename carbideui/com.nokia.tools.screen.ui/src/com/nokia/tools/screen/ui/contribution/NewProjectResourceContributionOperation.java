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
package com.nokia.tools.screen.ui.contribution;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.actions.AbstractNewProjectOperation;
import com.nokia.tools.screen.ui.extension.IResourceContributionDescriptor;

/**
 * Moved here to provide general add resources to initial project contribution
 * 
 */
public class NewProjectResourceContributionOperation extends
		AbstractNewProjectOperation {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
			InvocationTargetException, InterruptedException {

		String currentPlatform = getThemeDescriptor().getId();
		/**
		 * @Externalize
		 */
		monitor.subTask("Copying files to project");//$NON-NLS-1$

		for (IResourceContributionDescriptor descriptor : ResourcesContributionExtension
				.getContributorDescriptors()) {
			//only copy default components, optional ones user will select
			//what to use
			
			if (descriptor.forPlatforms() != null && descriptor.forPlatforms().trim().length() > 0) {
				if (descriptor.forPlatforms().indexOf(currentPlatform) == -1)
					continue;
			}
			
			if (descriptor.compulsory())
				try {					
					FileUtils.copyDir(new File(descriptor.getFilePath()),
						new Path(ResourcesPlugin.getWorkspace().getRoot()
								.getLocation().toFile().getAbsolutePath())
								.append(getProject().getName()).append(
										descriptor.getDestinationPath())
								.toFile());
				} catch (IOException e) {
					UiPlugin.error(e);
				}
		}

	}
}
