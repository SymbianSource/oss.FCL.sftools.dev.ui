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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.PageSite;

import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentService;
import com.nokia.tools.s60.editor.GraphicsEditorPart;
import com.nokia.tools.s60.editor.Series60EditorPart;
import com.nokia.tools.s60.editor.ui.views.ActiveLayersPage;
import com.nokia.tools.screen.ui.editor.IContentDependentEditor;
import com.nokia.tools.screen.ui.utils.EclipseUtils;

public class ColorsView extends PageBookView implements
		IContributedContentsView, ISelectionListener {

	public static final String ID = "com.nokia.tools.s60.views.ColorsView";
	public static final String COLOR_CONTEXT = "com.nokia.tools.s60.ide" + "." + "colours_context"; 

	public ColorsView() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService().addSelectionListener(this);
	}

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
		return super.getAdapter(adapter);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	// public void setFocus() {
	// if (getCurrentPage() instanceof ColorsViewPage) {
	// ColorsViewPage fevp = (ColorsViewPage) getCurrentPage();
	// fevp.setFocus();
	// }
	// }

	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage page = new MessagePage();
		initPage(page);
		page.createControl(book);
		page.setMessage("Some");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(book, COLOR_CONTEXT);
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
		if (part instanceof Series60EditorPart
				|| part.getAdapter(Series60EditorPart.class) != null) {
			ColorsViewPage page = new ColorsViewPage((IEditorPart) part);
			page.init(new PageSite(getViewSite()));
			page.createControl(getPageBook());
			((IContentService) part.getAdapter(IContentService.class))
					.addContentListener(page);
			return new PageRec(part, page);

		} else
			return null;
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

		if (getCurrentPage() instanceof ColorsViewPage) {
			ColorsViewPage lp = (ColorsViewPage) getCurrentPage();
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

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part != this && getCurrentPage() instanceof ISelectionListener) {
			if (getCurrentPage() instanceof ActiveLayersPage) {
				if (part instanceof GraphicsEditorPart) {
					((ISelectionListener) getCurrentPage()).selectionChanged(
							part, selection);
				}
			} else {
				((ISelectionListener) getCurrentPage()).selectionChanged(part,
						selection);
			}
		}

	}
}