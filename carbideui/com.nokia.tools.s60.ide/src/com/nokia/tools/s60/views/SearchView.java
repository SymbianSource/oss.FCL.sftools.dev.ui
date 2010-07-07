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

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.PageSite;

import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentListener;
import com.nokia.tools.content.core.IContentService;
import com.nokia.tools.s60.editor.ISearchSupportForEditor;
import com.nokia.tools.s60.editor.Series60EditorPart;
import com.nokia.tools.screen.ui.editor.IContentDependentEditor;
import com.nokia.tools.screen.ui.utils.EclipseUtils;

public class SearchView extends PageBookView implements
		IContributedContentsView {

	public static final String ID = "com.nokia.tools.s60.views.SearchView";
	public static final String ICON_CONTEXT = "com.nokia.tools.s60.ide.icon_context"; 
	public static final String SEARCH_CONTEXT = "com.nokia.tools.s60.ide.search_context"; 

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getSite().getWorkbenchWindow().getPartService().addPartListener(this);
		site.getActionBars().updateActionBars();
	}

	public CommandStack getCommandStack() {
		return (CommandStack) getAdapter(CommandStack.class);
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == CommandStack.class) {
			if (getCurrentPage() instanceof IconViewPage) {
				SearchViewPage lp = (SearchViewPage) getCurrentPage();
				return lp.getSourceEditor().getAdapter(adapter);
			}
		}
		if (adapter == ActionRegistry.class) {
			
			if (getContributingPart() != null)
				return getContributingPart().getAdapter(adapter);
		}
		if (adapter == IContributedContentsView.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (getCurrentPage() instanceof SearchViewPage) {
			SearchViewPage ivp = (SearchViewPage) getCurrentPage();
			ivp.setFocus();
		} else if (getCurrentPage() instanceof IPage) {
			IPage page = getCurrentPage();
			page.setFocus();
		}
	}

	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage page = new MessagePage();
		initPage(page);
		page.createControl(book);
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(book,
				SEARCH_CONTEXT);
		
		return page;
	}

	@Override
	protected PageRec getPageRec(IWorkbenchPart part) {
		if (part instanceof IContentDependentEditor) {
			IEditorPart parent = ((IContentDependentEditor) part)
					.getParentEditor();
			if (parent != null) {
				return getPageRec(parent);
			}
		}
		return super.getPageRec(part);
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {

		if (part.getAdapter(Series60EditorPart.class) != null) {
			SearchViewPage page = new SearchViewPage((IEditorPart) part);
			page.init(new PageSite(getViewSite()));
			page.createControl(getPageBook());
			return new PageRec(part, page);
		} else if (part instanceof ISearchSupportForEditor) {
			ISearchSupportForEditor searchSupport = (ISearchSupportForEditor) part;
			Page page = searchSupport.createSearchPage((IEditorPart) part);
			page.init(new PageSite(getViewSite()));
			page.createControl(getPageBook());
			if (page instanceof IContentListener) {
				((IContentService) part.getAdapter(IContentService.class))
						.addContentListener((IContentListener) page);
			}
			return new PageRec(part, page);
		} else {
			return null;
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

	@Override
	protected void showPageRec(PageRec pageRec) {
		super.showPageRec(pageRec);

	}

	public IWorkbenchPart getContributingPart() {

		if (getCurrentPage() instanceof SearchViewPage) {
			SearchViewPage lp = (SearchViewPage) getCurrentPage();
			return lp.getSourceEditor();
		}

		// return active ed.
		IEditorPart part = EclipseUtils.getActiveSafeEditor();
		if (part != null && part.getAdapter(IContentAdapter.class) != null) {
			return part;
		}
		return null;
	}

	@Override
	public void dispose() {
		try {
			getSite().getWorkbenchWindow().getPartService().removePartListener(
					this);
		} catch (Exception e) {
		}
		super.dispose();
	}

	@Override
	public void partClosed(IWorkbenchPart part) {

		if (!(part instanceof IContentDependentEditor)) {
			super.partClosed(part);
		}
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		super.partActivated(part);
	}
}