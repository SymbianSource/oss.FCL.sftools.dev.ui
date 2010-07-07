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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.PageSite;

import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.screen.ui.editor.IContentDependentEditor;
import com.nokia.tools.screen.ui.editor.ScreenEditorPart;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.screen.ui.views.ViewIDs;
import com.nokia.tools.screen.ui.views.ViewMessages;

public class ResourceView2 extends PageBookView implements ISetSelectionTarget,
		IAdaptable, IContributedContentsView {

	public static final String RESOURCE_CONTEXT = "com.nokia.tools.s60.ide" + '.' + "resource_context"; 

	public static final String ID = ViewIDs.RESOURCE_VIEW2_ID;

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getSite().getWorkbenchWindow().getPartService().addPartListener(this);
		site.getActionBars().updateActionBars();
		

	}

	public CommandStack getCommandStack() {
		return (CommandStack) getAdapter(CommandStack.class);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */

	// @Override
	// public void setFocus() {
	//			
	// if (getCurrentPage() instanceof ResourceViewPage) {
	// ResourceViewPage fevp = (ResourceViewPage) getCurrentPage();
	// fevp.setFocus();
	// }
	// }

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
	protected void showPageRec(PageRec pageRec) {
		super.showPageRec(pageRec);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.PageBookView#createDefaultPage(org.eclipse.ui.part
	 * .PageBook)
	 */
	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage page = new MessagePage();
		initPage(page);
		page.createControl(book);
		page.setMessage(ViewMessages.ResourceView_defaultText);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(book,
				RESOURCE_CONTEXT);

		return page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.PageBookView#doCreatePage(org.eclipse.ui.IWorkbenchPart
	 * )
	 */
	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		
		ResourceViewPage page = new ResourceViewPage((IEditorPart) part);

		page.init(new PageSite(getViewSite()));
		page.createControl(getPageBook());

		return new PageRec(part, page);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.PageBookView#doDestroyPage(org.eclipse.ui.IWorkbenchPart
	 * , org.eclipse.ui.part.PageBookView.PageRec)
	 */
	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		pageRecord.page.dispose();
		pageRecord.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#getBootstrapPart()
	 */
	@Override
	protected IWorkbenchPart getBootstrapPart() {
		IWorkbenchPage page = getSite().getPage();
		if (page != null)
			return page.getActiveEditor();

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.PageBookView#isImportant(org.eclipse.ui.IWorkbenchPart
	 * )
	 */
	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		return (part instanceof IEditorPart && part
				.getAdapter(IContentAdapter.class) != null);
	}

	/**
	 * synchronizes selection to element that contains item in
	 * <i>selection</i>.
	 * 
	 * @param selection
	 */
	public void showSelection(StructuredSelection selection) {
		if (null != selection && !selection.isEmpty()
				&& getCurrentPage() instanceof ResourceViewPage) {
			((ResourceViewPage) getCurrentPage()).showSelection(selection);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.ISetSelectionTarget#selectReveal(org.eclipse.jface
	 * .viewers.ISelection)
	 */
	public void selectReveal(ISelection selection) {
		showSelection((StructuredSelection) selection);
	}

	/**
	 * returns true/false, depending if current resource page is synchronizing,
	 * or null if current page is not ResourcePage.
	 * 
	 * @return
	 */
	public Boolean isSynchronizing() {
		IPage page = getCurrentPage();
		if (page instanceof ResourceViewPage) {
			return Boolean.valueOf(((ResourceViewPage) page).isSynchronizing());
		}
		return null;
	}

	public void refresh() {
		((ResourceViewPage) getCurrentPage()).refresh();
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IContributedContentsView.class) {
			return this;
		} else if (CommandStack.class == adapter) {
			IWorkbenchPage page = getSite().getPage();
			if (page != null) {
				if (null != page.getActiveEditor()) {
					System.out.println(page.getActiveEditor().getClass()
							.getName());
					if (page.getActiveEditor() instanceof ScreenEditorPart) {
						return getSite().getPage().getActiveEditor()
								.getAdapter(CommandStack.class);
					}
					
					if (page.getActiveEditor() instanceof MultiPageEditorPart) {
						Object stack = getSite().getPage().getActiveEditor()
								.getAdapter(CommandStack.class);
						if (null != stack) {
							return stack;
						}
					}
				}
			}
		}
		return super.getAdapter(adapter);
	}

	public IWorkbenchPart getContributingPart() {

		PageRec pr = getPageRec(getCurrentPage());
		if (pr != null) {

			
			try {
				if (pr.part != EclipseUtils.getActiveSafeEditor()) {
					System.out
							.println("Warning: sourceEditor from icon view page is not the same as active editor!");
				}
			} catch (Exception e) {
			}

			return pr.part;
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