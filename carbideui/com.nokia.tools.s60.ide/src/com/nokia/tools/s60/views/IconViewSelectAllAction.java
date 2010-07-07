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

package com.nokia.tools.s60.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.ui.widgets.ImageLabel;

public class IconViewSelectAllAction extends Action {
	private static final ImageDescriptor SELECT_ALL_IMAGE = S60WorkspacePlugin
			.getImageDescriptor("icons/selectall.png");

	private IconViewPage page;

	public IconViewSelectAllAction(IconViewPage page) {
		super();
		this.page = page;
		setText(ViewMessages.IconView_SelectAll_text);
		setImageDescriptor(SELECT_ALL_IMAGE);
	}

	public void run() {
		if (page != null && page.getContainer() != null
				&& !page.getContainer().isDisposed()) {
			Cursor cursor = new Cursor(null, SWT.CURSOR_WAIT);
			try {
				List<IContentData> selection = new ArrayList<IContentData>();
				if (page.selectedItems != null) {
					selection.addAll(page.selectedItems);
				}
				page.getSite().getShell().setCursor(cursor);
				Control[] childs = page.getContainer().getChildren();
				for (int i = 0; i < childs.length; i++) {
					Control control = childs[i];
					if (!control.isDisposed() && control instanceof ImageLabel) {
						ImageLabel label = (ImageLabel) control;
						if (!selection.contains(label.getData())) {
							selection.add((IContentData) label.getData());
						}
					}
				}
				page.setSelection(new StructuredSelection(selection));
			} finally {
				page.getSite().getShell().setCursor(null);
				cursor.dispose();
			}
		}
	}
}
