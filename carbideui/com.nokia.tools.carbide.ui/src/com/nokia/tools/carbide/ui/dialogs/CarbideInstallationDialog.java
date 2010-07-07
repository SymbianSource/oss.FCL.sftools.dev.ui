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

package com.nokia.tools.carbide.ui.dialogs;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.internal.ConfigurationInfo;
import org.eclipse.ui.internal.about.InstallationDialog;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Added Since 3.5 to display the Installation details
 * 
 */

public class CarbideInstallationDialog extends InstallationDialog{
	
	public static String ATT_FEATURE = "Features";
	public static String ATT_PLUGIN = "Plug-ins";
	public static String ATT_CONFIGURATION = "Configuration";
	
	public CarbideInstallationDialog(Shell parentShell, IServiceLocator locator) {
		super(parentShell, locator);
	}
	
	//Filter out the Other tabs
	@Override
	protected void createFolderItems(TabFolder folder) {
		IConfigurationElement[] elements = ConfigurationInfo
		.getSortedExtensions(loadElements());
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			
			String attName = element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
			if( (attName.equalsIgnoreCase(ATT_FEATURE)) 
					|| (attName.equalsIgnoreCase(ATT_PLUGIN)) 
					|| (attName.equalsIgnoreCase(ATT_CONFIGURATION))){
				TabItem item = new TabItem(folder, SWT.NONE);
				item.setText(attName);
				item.setData(element);
				item.setData(ID, element
						.getAttribute(IWorkbenchRegistryConstants.ATT_ID));

				Composite control = new Composite(folder, SWT.NONE);
				control.setLayout(new GridLayout());
				item.setControl(control);
			}
			
		}
	}
	
	private IConfigurationElement[] loadElements() {
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint("org.eclipse.ui", "installationPages"); //$NON-NLS-1$ //$NON-NLS-2$
		return point.getConfigurationElements();
	}

}
