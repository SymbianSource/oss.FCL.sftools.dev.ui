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
/*
*/
package com.nokia.tools.s60.editor.ui.views;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.PageSite;

import com.nokia.tools.s60.editor.GraphicsEditorPart;

/**
 */
public class EffectControlsEditorView extends PageBookView {

	public static final String ID = "com.nokia.tools.s60.editor.ui.views.EffectControlsView";

	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage page = new MessagePage();
		initPage(page);
		page.createControl(book);
		return page;
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {

		if (!(part instanceof GraphicsEditorPart)) {
			// return default page
			return null;
		} else {
			EffectControlsEditorPage page = new EffectControlsEditorPage();
			page.init(new PageSite(getViewSite()));
			page.createControl(getPageBook());
			return new PageRec(part, page);
		}
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		pageRecord.page.dispose();
		pageRecord.dispose();
	}

	@Override
	protected IWorkbenchPart getBootstrapPart() {
		IWorkbenchPage page = getSite().getPage();
		if (page != null) {
			return page.getActiveEditor();
		} else {
			return null;
		}
	}

	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		return (part instanceof IEditorPart);
	}
}