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
package com.nokia.tools.s60.ide.actions;

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

import com.nokia.tools.s60.views.GalleryPage;
import com.nokia.tools.s60.views.ViewMessages;
import com.nokia.tools.screen.ui.gallery.IGalleryScreenProvider.IGalleryScreen;

/**
 */
public class GalleryScreenContributionItem extends ContributionItem {
	private ToolItem toolitem;
	private Combo cboSelect;
	private GalleryPage gallery;

	public GalleryScreenContributionItem(GalleryPage gallery) {
		this.gallery = gallery;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void fill(Composite parent) {
		createControl(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.ToolBar,
	 *      int)
	 */
	@Override
	public void fill(ToolBar parent, int index) {
		toolitem = new ToolItem(parent, SWT.SEPARATOR, index);
		Control control = createControl(parent);
		toolitem.setControl(control);
	}

	protected Control createControl(Composite parent) {
		cboSelect = new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN);
		cboSelect.setToolTipText(ViewMessages.GalleryView_selectScreen_tooltip);
		cboSelect.setVisibleItemCount(10);
		cboSelect.setSize(cboSelect.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		cboSelect.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				String name = cboSelect.getItem(cboSelect.getSelectionIndex());
				for (IGalleryScreen screen : gallery.getScreens()) {
					if (name.equals(screen.getName())) {
						gallery.showScreen(screen, true);
						break;
					}
				}
			}
		});
		toolitem.setWidth((int) (computeWidth(cboSelect) * 1.5));
		return cboSelect;
	}

	/**
	 * Computes the width required by control
	 * 
	 * @param control The control to compute width
	 * @return int The width required
	 */
	protected int computeWidth(Control control) {
		int width = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
		if (SWT.getPlatform().equals("win32")) 
			width += FigureUtilities.getTextWidth("8", control.getFont()); 
		return width;
	}

	public void screenCreated(IGalleryScreen screen) {
		if (cboSelect == null || cboSelect.isDisposed()) {
			return;
		}
		cboSelect.add(screen.getName());
		if (screen.isActive()) {
			selectScreen(screen);
		}
	}

	public void screenDisposed(IGalleryScreen screen) {
		if (cboSelect == null || cboSelect.isDisposed()) {
			return;
		}
		int index = cboSelect.indexOf(screen.getName());
		if (index >= 0) {
			cboSelect.remove(screen.getName());
		}
	}

	public void selectScreen(IGalleryScreen screen) {
		if (cboSelect == null || cboSelect.isDisposed()) {
			return;
		}
		cboSelect.select(cboSelect.indexOf(screen.getName()));
	}

	public void clear() {
		if (cboSelect == null || cboSelect.isDisposed()) {
			return;
		}
		cboSelect.removeAll();
	}
}
