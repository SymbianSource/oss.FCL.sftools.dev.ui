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
package com.nokia.tools.s60.editor.menus;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 */
public abstract class ComboContributionItem extends ContributionItem {
	private Combo combo;
	private ToolItem item;

	public ComboContributionItem(String id) {
		super(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.ToolBar,
	 *      int)
	 */
	@Override
	public void fill(ToolBar parent, int index) {
		item = new ToolItem(parent, SWT.SEPARATOR, index);
		Control control = createControl(parent);
		item.setControl(control);
	}

	/**
	 * @param parent
	 * @return
	 */
	protected Control createControl(Composite parent) {
		combo = new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setVisibleItemCount(10);
		combo.setItems(getItems());
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				itemSelected(combo.getText());
			}
		});
		item.setWidth(computeWidth(combo));
		update();
		return combo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		try {
			item.dispose();
			combo.dispose();
		} catch (Exception e) {
		}
	}

	protected abstract String[] getItems();

	protected abstract void itemSelected(String item);

	protected abstract String getNewItem();

	protected void comboEnabled() {
	}

	protected void comboDisabled() {
	}

	protected int getItemCount() {
		return combo.getItemCount();
	}

	protected String getSelectedItem() {
		return combo.getText();
	}

	/**
	 * @param item
	 */
	protected void setSelectedItem(String item) {
		combo.setText(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IContributionItem#update()
	 */
	public void update() {
		super.update();

		String text = getNewItem();
		if (combo != null && !combo.isDisposed()) {
			if (text == null) {
				combo.removeAll();
				combo.setEnabled(false);
				comboDisabled();
			} else {
				combo.setEnabled(true);
				combo.setItems(getItems());
				combo.setText(text);
				item.setWidth(computeWidth(combo));
				comboEnabled();
			}
		}
	}

	/**
	 * @param control
	 * @return
	 */
	protected int computeWidth(Control control) {
		int width = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
		
		if (SWT.getPlatform().equals("win32")) 
			width += FigureUtilities.getTextWidth("8", control.getFont()); 
		return width;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.Composite)
	 */
	public void fill(Composite composite) {
		createControl(composite);
	}
}
