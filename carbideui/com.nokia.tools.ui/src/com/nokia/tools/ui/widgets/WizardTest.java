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
package com.nokia.tools.ui.widgets;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

public class WizardTest extends WizardPage {

	/**
	 * Create the wizard
	 */
	public WizardTest() {
		super("wizardPage");
		setTitle("Wizard Page title");
		setDescription("Wizard Page description");
	}

	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		//
		ImageLabel label=new ImageLabel(container,SWT.NONE);
		label.setText("Component");
		label.setUnselectedBackground(ColorConstants.listBackground);
		
		label.setFillBackground(true);
		label.setSelectedTextColor(ColorConstants.menuForegroundSelected);
		label.setUnselectedTextColor(ColorConstants.menuForeground);
		label.setDisabledTextColor(ColorConstants.gray);
		setControl(container);

		final TableViewer tableViewer = new TableViewer(container, SWT.BORDER);
		final Table table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setBounds(47, 27, 100, 100);
	}

}
