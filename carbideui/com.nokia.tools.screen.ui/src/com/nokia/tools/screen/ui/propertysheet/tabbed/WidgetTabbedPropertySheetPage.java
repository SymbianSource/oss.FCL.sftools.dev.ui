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

import java.lang.reflect.Method;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nokia.tools.screen.ui.UiPlugin;

public class WidgetTabbedPropertySheetPage extends
		DisposableTabbedPropertySheetPage {

	public WidgetTabbedPropertySheetPage(
			ITabbedPropertySheetPageContributor tabbedPropertySheetPageContributor) {
		super(tabbedPropertySheetPageContributor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.ui.properties.internal.provisional.TabbedPropertySheetPage#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {

		// when selection is empty, and part
		// is LayersView, we should skip super.selectionChanged(part,
		// selection);
		// in order to leave original content in property view
	
		if (part instanceof IContributedContentsView && selection.isEmpty())
			return;

		super.selectionChanged(part, selection);
	}

	public void refreshTitleBar() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (getControl() == null || getControl().isDisposed()) {
					return;
				}
				if (getCurrentTab() != null)
					if (getLabelProvider() != null) {
						getLabelProvider().setContextPage(
								WidgetTabbedPropertySheetPage.this);
					}
				try {
					// should refresh the title bar because the image might have
					// changed
					Method method = TabbedPropertySheetPage.class
							.getDeclaredMethod("refreshTitleBar");
					method.setAccessible(true);
					method.invoke(WidgetTabbedPropertySheetPage.this, null);
				} catch (Throwable e) {
					UiPlugin.error(e);
				} finally {
					if (getLabelProvider() != null) {
						getLabelProvider().setContextPage(null);
					}
					if (getCurrentTab() != null) {
						refresh();
					}
				}
			}
		});
	}
}
