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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.packaging.IPackager;
import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.screen.ui.wizards.AbstractPackagingOperation;
import com.nokia.tools.theme.s60.ui.Activator;

/**
 * 
 */
public class PackagingOperation extends AbstractPackagingOperation {
	/**
	 * Theme installation to SDK emulator support. For default installation
	 * standard SDK Emulator value would be
	 * "C:/Symbian/9.1/S60_3rd/Epoc32/winscw/c" Emulator deployment parameters:
	 */
	public static final String PREF_EMULATOR_C_DRIVE = Activator.PLUGIN_ID
			+ ".emulator.c.drive";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.wizards.AbstractPackagingOperation#doPackaging(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doPackaging(IProgressMonitor monitor) throws Exception {
		IContent[] contents = (IContent[]) context.getInput();
		File workingDir = new File((String) context
				.getAttribute(PackagingAttribute.workingDir.name()));

		boolean isDRM = new Boolean((String) context
				.getAttribute(PackagingAttribute.themeDRM.name()));
		// builds drm package first
		if (isDRM) {
			File drmWorkingDir = new File(workingDir, "DRM");
			drmWorkingDir.mkdirs();
			context.setAttribute(PackagingAttribute.workingDir.name(),
					drmWorkingDir.getAbsolutePath());
			buildPackage(contents, monitor);
			context.setAttribute(PackagingAttribute.themeDRM.name(),
					Boolean.FALSE.toString());
			context.setAttribute(PackagingAttribute.workingDir.name(),
					workingDir.getAbsolutePath());
		}
		context.removeAttribute(PackagingAttribute.themeDRM.name());
		// then the normal sis file that is valid on the phone
		buildPackage(contents, monitor);
		if (isDRM) {
			context.setAttribute(PackagingAttribute.themeDRM.name(),
					Boolean.TRUE.toString());
		}
	}

	private void buildPackage(IContent[] contents, IProgressMonitor monitor)
			throws PackagingException {
		IContent primaryContent = null;
		List<IContent> embeddedContents = new ArrayList<IContent>();
		for (IContent content : contents) {
			if (ScreenUtil.isPrimaryContent(content)) {
				primaryContent = content;
			} else {
				embeddedContents.add(content);
			}
		}

		if (primaryContent == null) {
			context.setAttribute(PackagingAttribute.isStandalone.name(),
					Boolean.TRUE.toString());
		}
		if (!embeddedContents.isEmpty()) {
			context.setInput(embeddedContents
					.toArray(new IContent[embeddedContents.size()]));
			IPackager packager = (IPackager) embeddedContents.get(0)
					.getAdapter(IPackager.class);
			packager.buildPackage(context, monitor);
		}

		if (primaryContent != null) {
			IPackager packager = (IPackager) primaryContent
					.getAdapter(IPackager.class);
			context.setInput(primaryContent);
			context.setAttribute(PackagingAttribute.theme.name(),
					primaryContent);
			packager.buildPackage(context, monitor);
		}
	}

	@Override
	protected void doSilentModeDeployment() throws Exception {
		
	}

	@Override
	protected boolean isSilentModeDeploymentAvailable() throws Exception {
		
		return false;
	}
}
