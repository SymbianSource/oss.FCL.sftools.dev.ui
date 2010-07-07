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
import java.net.URL;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.nokia.tools.screen.ui.dialogs.IThemeResourcePageManager;
import com.nokia.tools.screen.ui.dialogs.ThirdPartyIconsResourceResult;
import com.nokia.tools.theme.s60.model.tpi.DefinedIcons;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIcon;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconManager;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconType;

public class ThirdPartyIconsTableBuilder {

	private TableViewer viewer;

	private TableColumn thirdPartyIconTypeColumn;

	private TableColumn thirdPartyIconNameColumn;

	private TableColumn thirdPartyIconUidColumn;

	private TableColumn thirdPartyIconMajorIdColumn;

	private TableColumn thirdPartyIconMinorIdColumn;

	boolean updateColSize = true;

	private IThemeResourcePageManager<DefinedIcons> themeResourcePageManager;

	private DefinedIcons selectedThirdPartyIcons = new DefinedIcons();

	public static ThirdPartyIconsTableBuilder newInstance(
	    IThemeResourcePageManager<DefinedIcons> themeResourcePageManager) {
		ThirdPartyIconsTableBuilder thirdPartyIconsTableBuilder = new ThirdPartyIconsTableBuilder();
		thirdPartyIconsTableBuilder.setContainer(themeResourcePageManager);
		return thirdPartyIconsTableBuilder;
	}

	private void setContainer(
	    IThemeResourcePageManager<DefinedIcons> themeResourcePageManager) {
		this.themeResourcePageManager = themeResourcePageManager;
	}

	public TableViewer buildTable(Composite tableComposite) {
		viewer = createTableViewer(tableComposite);
		Table table = viewer.getTable();
		createColumns(table);
		setPropertiesOnTable(table);
		viewer
		    .setColumnProperties(ThirdPartyIconsPrefPage.thirdPartyIconColumnNames);
		attachContentProvider();
		attachLabelProvider();
		attachResizeListener();
		table.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (e.item.getData() instanceof ThirdPartyIcon) {
					ThirdPartyIcon selectedThirdPartyIcon = (ThirdPartyIcon) e.item
					    .getData();
					if (selectedThirdPartyIcons
					    .contains(selectedThirdPartyIcon)) {
						selectedThirdPartyIcons.remove(selectedThirdPartyIcon);
					} else {
						selectedThirdPartyIcons.add(selectedThirdPartyIcon);
					}
					themeResourcePageManager
					    .setResult(new ThirdPartyIconsResourceResult<DefinedIcons>(
					        false, selectedThirdPartyIcons));
				}
			}

		});
		return viewer;
	}

	public DefinedIcons populateTableFrom(File themeIconsDotXml)
	    throws Exception {
		DefinedIcons model = createModel(themeIconsDotXml.toURL());
		viewer.setInput(model);
		return model;
	}

	private DefinedIcons createModel(URL iconsDotXmlUrl) throws Exception {
		DefinedIcons model = ThirdPartyIconManager.loadThirdPartyIcons(iconsDotXmlUrl, ThirdPartyIconType.THEME_SPECIFIC);
		return model;
	}

	private TableViewer createTableViewer(Composite tableComposite) {
		return new TableViewer(tableComposite, SWT.MULTI | SWT.FULL_SELECTION
		    | SWT.BORDER);
	}

	private void createColumns(Table table) {
		thirdPartyIconTypeColumn = new TableColumn(table, SWT.LEFT);
		thirdPartyIconTypeColumn
		    .setText(ThirdPartyIconsPrefPage.thirdPartyIconColumnNames[0]);

		thirdPartyIconNameColumn = new TableColumn(table, SWT.LEFT);
		thirdPartyIconNameColumn
		    .setText(ThirdPartyIconsPrefPage.thirdPartyIconColumnNames[1]);

		thirdPartyIconUidColumn = new TableColumn(table, SWT.LEFT);
		thirdPartyIconUidColumn
		    .setText(ThirdPartyIconsPrefPage.thirdPartyIconColumnNames[2]);

		thirdPartyIconMajorIdColumn = new TableColumn(table, SWT.LEFT);
		thirdPartyIconMajorIdColumn
		    .setText(ThirdPartyIconsPrefPage.thirdPartyIconColumnNames[3]);

		thirdPartyIconMinorIdColumn = new TableColumn(table, SWT.LEFT);
		thirdPartyIconMinorIdColumn
		    .setText(ThirdPartyIconsPrefPage.thirdPartyIconColumnNames[4]);
	}

	private void setPropertiesOnTable(Table table) {
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	private void attachContentProvider() {
		viewer.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				return ((DefinedIcons) inputElement).toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
			    Object newInput) {
			}

		});
	}

	private void attachLabelProvider() {
		viewer.setLabelProvider(new ITableLabelProvider() {

			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				ThirdPartyIcon i = (ThirdPartyIcon) element;
				switch (columnIndex) {
					case 0:
						return i.isApplication() ? "Application"
						    : "Non-Application";
					case 1:
						return i.getName();
					case 2:
						return i.getAppUid();
					case 3:
						return i.getMajorId();
					case 4:
						return i.getMinorId();
				}
				return null;
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener listener) {
			}

		});
	}

	private void attachResizeListener() {
		viewer.getTable().addListener(SWT.Resize, new Listener() {

			public void handleEvent(Event event) {
				if (updateColSize) {
					Table tbl = viewer.getTable();
					int w = tbl.getClientArea().width;
					int c1 = 85;
					int c2 = 100;
					int c3 = 85;
					int c4 = 75;
					updateColSize = false;
					thirdPartyIconTypeColumn.setWidth(c1);
					thirdPartyIconNameColumn.setWidth(c2);
					thirdPartyIconUidColumn.setWidth(c3);
					thirdPartyIconMajorIdColumn.setWidth(c4);
					thirdPartyIconMinorIdColumn.setWidth(w - c1 - c2 - c3 - c4);
					updateColSize = true;
				}
			}
		});
	}

}
