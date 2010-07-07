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
package com.nokia.tools.screen.ui.wizards;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;

public class ExportWizard extends Wizard implements IExportWizard {

	private IWizardPage mainPage;

	private ImageDescriptor bannerIcon;

	private IStructuredSelection selection;

	/**
	 * Creates a wizard for exporting workspace resources to the local file
	 * system.
	 */
	public ExportWizard() {
		setWindowTitle(WizardMessages.ExportWizard_title);
		IBrandingManager manager = BrandingExtensionManager
				.getBrandingManager();
		if (manager != null) {
			bannerIcon = UiPlugin
					.getImageDescriptor("icons/wizban/export_theme.png");
			setDefaultPageImageDescriptor(manager
					.getBannerImageDescriptor(bannerIcon));
		}
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform
				.getPlugin(PlatformUI.PLUGIN_ID);
		IDialogSettings workbenchSettings = plugin.getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("ExportWizard");//$NON-NLS-1$
		if (section == null) {
			section = workbenchSettings.addNewSection("ExportWizard");//$NON-NLS-1$
		}
		setDialogSettings(section);
		setNeedsProgressMonitor(true);
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	public void addPages() {
		mainPage = new ExportWizardPage1("mainpage"); //$NON-NLS-1$
		addPage(mainPage);
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbenchWizard.
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		selection = currentSelection;
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	public boolean performFinish() {
		return ((ExportWizardPage1) mainPage).finish();
	}

	@Override
	public boolean canFinish() {
		return mainPage.isPageComplete();
	}

	public IStructuredSelection getSelection() {
		return selection;
	}

}
