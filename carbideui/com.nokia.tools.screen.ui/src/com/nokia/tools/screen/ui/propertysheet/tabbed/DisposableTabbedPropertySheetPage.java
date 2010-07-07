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
package com.nokia.tools.screen.ui.propertysheet.tabbed;

import java.lang.reflect.Field;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyRegistry;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nokia.tools.screen.ui.UiPlugin;

/**
 * Workaround for the current tabbed property sheet page that never disposes any
 * images.
 * 
 */
public class DisposableTabbedPropertySheetPage extends TabbedPropertySheetPage {
	private DisposableLabelProvider labelProvider;

	public DisposableTabbedPropertySheetPage(
			ITabbedPropertySheetPageContributor tabbedPropertySheetPageContributor) {
		super(tabbedPropertySheetPageContributor);
	}

	protected DisposableLabelProvider getLabelProvider() {
		if (labelProvider == null) {
			try {
				Field field = TabbedPropertySheetPage.class
						.getDeclaredField("registry");
				field.setAccessible(true);
				TabbedPropertyRegistry registry = (TabbedPropertyRegistry) field
						.get(this);
				if (registry != null) {
					ILabelProvider provider = registry.getLabelProvider();
					if (provider instanceof DisposableLabelProvider) {
						labelProvider = (DisposableLabelProvider) provider;
					}
				}
			} catch (Exception e) {
				UiPlugin.error(e);
			}
		}
		return labelProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (getLabelProvider() != null) {
			labelProvider.setContextPage(this);
		}
		try {
			super.selectionChanged(part, selection);
		} finally {
			if (labelProvider != null) {
				labelProvider.setContextPage(null);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (labelProvider != null) {
			labelProvider.pageDisposed(this);
		}
	}
}
