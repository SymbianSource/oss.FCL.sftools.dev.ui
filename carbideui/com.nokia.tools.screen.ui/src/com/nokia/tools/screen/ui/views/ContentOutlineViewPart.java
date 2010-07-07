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
package com.nokia.tools.screen.ui.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.contentoutline.ContentOutline;

import com.nokia.tools.screen.ui.IPartCustomizer;

public class ContentOutlineViewPart extends ContentOutline implements
		PropertyChangeListener {

	public static final String OUTLINE_CONTEXT = "com.nokia.tools.screen.ui" + '.' + "outline_context"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.contentoutline.ContentOutline#doCreatePage(org.eclipse
	 * .ui.IWorkbenchPart)
	 */
	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		PageRec rec = super.doCreatePage(part);
		if (rec != null && rec.page instanceof IPartCustomizer) {
			((IPartCustomizer) rec.page).addPropertyChangeListener(this);
		}
		return rec;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.contentoutline.ContentOutline#doDestroyPage(org.
	 * eclipse.ui.IWorkbenchPart, org.eclipse.ui.part.PageBookView.PageRec)
	 */
	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec rec) {
		if (rec.page instanceof IPartCustomizer) {
			((IPartCustomizer) rec.page).removePropertyChangeListener(this);
		}
		super.doDestroyPage(part, rec);

		if (!(getCurrentPage() instanceof IPartCustomizer)) {
			setDefaultName();
		}
	}

	private void setDefaultName() {
		String defaultName = getConfigurationElement().getAttribute("name");
		if (defaultName != null) {
			setPartName(defaultName);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.contentoutline.ContentOutline#showPageRec(org.eclipse
	 * .ui.part.PageBookView.PageRec)
	 */
	@Override
	protected void showPageRec(PageRec pageRec) {
		super.showPageRec(pageRec);
		if (pageRec.page instanceof IPartCustomizer) {
			String partName = ((IPartCustomizer) pageRec.page)
					.getProperty(IPartCustomizer.PROP_NAME);
			if (partName != null) {
				setPartName(partName);
			}
		} else {
			setDefaultName();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
	 * PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (IPartCustomizer.PROP_NAME.equals(evt.getPropertyName())) {
			String partName = (String) evt.getNewValue();
			if (partName != null) {
				setPartName(partName);
			}
		}
	}
	
	public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getPageBook(),
                OUTLINE_CONTEXT);
    }

	@Override
	protected IPage createDefaultPage(PageBook book) {
		super.createDefaultPage(book);
		MessagePage page = new MessagePage();
		initPage(page);
		page.createControl(book);
		page.setMessage("Outline is not available.");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(book,
				ContentOutlineViewPart.OUTLINE_CONTEXT);
		return page;

	}

}
