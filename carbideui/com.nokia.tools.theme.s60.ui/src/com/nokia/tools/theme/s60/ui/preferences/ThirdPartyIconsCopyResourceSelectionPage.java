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

package com.nokia.tools.theme.s60.ui.preferences;

import java.io.File;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.dialogs.FileResourceSelectionPage;
import com.nokia.tools.screen.ui.dialogs.IThemeResourcePageManager;
import com.nokia.tools.screen.ui.dialogs.ThirdPartyIconsResourceResult;
import com.nokia.tools.theme.s60.model.tpi.DefinedIcons;

public class ThirdPartyIconsCopyResourceSelectionPage
    extends FileResourceSelectionPage {

	private static final String ICONS_XML = "icons.xml";

	private TableViewer table;

	private ThirdPartyIconsTableBuilder thirdPartyIconsTableBuilder;

	private File themeTdfFile;

	public ThirdPartyIconsCopyResourceSelectionPage() {
	}

	public ThirdPartyIconsCopyResourceSelectionPage(boolean createImageLink) {
		super(createImageLink);
	}

	@Override
	public Control createPage(Composite parent) {
		super.createPage(parent);

		Composite tableComposite = new Composite(parent, SWT.NONE);
		FillLayout layout2 = new FillLayout();
		tableComposite.setLayout(layout2);
		layout2.marginHeight = 0;
		layout2.marginWidth = 2;
		GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
		gd.verticalSpan = 15;
		tableComposite.setLayoutData(gd);

		thirdPartyIconsTableBuilder = ThirdPartyIconsTableBuilder
		    .newInstance(resoursePageManager());
		thirdPartyIconsTableBuilder.buildTable(tableComposite);
		return parent;
	}

	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object[] resources = ((IStructuredSelection) selection).toArray();
			if (resources.length > 0) {
				handleThemeFileResource(resources[0]);
			}
		}
	}

	protected void handleThemeFileResource(Object fileResource) {
		if (fileResource instanceof File && isValidThemeFile(fileResource)) {
			File themeTdfFile = (File) fileResource;
			File iconsDotXml = iconsDotXmlInTheme(themeTdfFile);

			if (iconsDotXml.exists()) {
				try {
					DefinedIcons model = thirdPartyIconsTableBuilder
					    .populateTableFrom(iconsDotXml);
					resoursePageManager().setResult(
					    new ThirdPartyIconsResourceResult<DefinedIcons>(false,
					        model));
				} catch (Exception e) {
					UiPlugin.error(e);
				}
			}
			setThemeTdfFile(themeTdfFile);
		}
	}

	private IThemeResourcePageManager<DefinedIcons> resoursePageManager() {
		IThemeResourcePageManager<DefinedIcons> result = null;
		try {
			result = (IThemeResourcePageManager<DefinedIcons>) getManager();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return result;
	}

	private File iconsDotXmlInTheme(File themeTdfFile) {
		String parentDirectory = themeTdfFile.getParent();
		File iconsDotXml = new File(
		    patchToIconsDotXmlWithinTheme(parentDirectory));
		return iconsDotXml;
	}

	/**
	 * @param fileResource
	 * @return
	 */
	private boolean isValidThemeFile(Object fileResource) {
		File toCheck = (File) fileResource;
		if (null == themeTdfFile)
			return true;
		boolean isValid = !themeTdfFile.getAbsolutePath().equals(
		    toCheck.getAbsolutePath());
		return isValid;
	}

	private void setThemeTdfFile(File themeTdfFile) {
		this.themeTdfFile = themeTdfFile;
	}

	private String patchToIconsDotXmlWithinTheme(String parentDirectory) {
		return parentDirectory + File.separatorChar + ICONS_XML;
	}

	@Override
	public void dispose() {
		thirdPartyIconsTableBuilder = null;
		super.dispose();
	}

}