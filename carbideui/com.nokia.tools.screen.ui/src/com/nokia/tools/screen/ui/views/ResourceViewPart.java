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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.PageSite;

import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentListener;
import com.nokia.tools.screen.ui.utils.EclipseUtils;

public class ResourceViewPart extends PageBookView implements
		ISetSelectionTarget, IAdaptable, IContributedContentsView

{
	public static final String RESOURCE_CONTEXT = "com.nokia.tools.s60.ide.resource_context"; //$NON-NLS-1$
	public static final String ID = ViewIDs.RESOURCE_VIEW_ID;

	private ResourceViewSelectionListenerManager resourceViewSelectionManager = new ResourceViewSelectionListenerManager();

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(resourceViewSelectionManager);
	}

	@Override
	public void dispose() {
		super.dispose();
		getSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(resourceViewSelectionManager);
	}

	@Override
	protected void showPageRec(PageRec pageRec) {
		
		super.showPageRec(pageRec);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#createDefaultPage(org.eclipse.ui.part.PageBook)
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
	 * @see org.eclipse.ui.part.PageBookView#doCreatePage(org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		IContentAdapter adapter = (IContentAdapter) part
				.getAdapter(IContentAdapter.class);

		ResourcePage page = new ResourcePage(adapter, part);
		// if(page.themeContentsIsEmpty()){
		// return null;
		// }
		// replaced with
		if (adapter.getContents().length < 1) {
			return null;
		}

		page.init(new PageSite(getViewSite()));
		page.createControl(getPageBook());

		// not needed as stack listener, content change events are used
		adapter.addContentListener(page);

		return new PageRec(part, page);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#doDestroyPage(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.ui.part.PageBookView.PageRec)
	 */
	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		IContentAdapter adapter = (IContentAdapter) part
				.getAdapter(IContentAdapter.class);
		adapter.removeContentListener((IContentListener) pageRecord.page);
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
	 * @see org.eclipse.ui.part.PageBookView#isImportant(org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		return (part instanceof IEditorPart && part
				.getAdapter(IContentAdapter.class) != null);
	}

	/**
	 * added synchronizes selection to element that contains item in
	 * <i>selection</i>.
	 * 
	 * @param selection
	 */
	public void showSelection(StructuredSelection selection) {
		if (null != selection && !selection.isEmpty()
				&& getCurrentPage() instanceof ResourcePage) {
			((ResourcePage) getCurrentPage()).showSelection(selection);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.ISetSelectionTarget#selectReveal(org.eclipse.jface.viewers.ISelection)
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
		if (page instanceof ResourcePage) {
			return Boolean.valueOf(((ResourcePage) page).isSynchronizing());
		}
		return null;
	}

	public void refresh() {
		((ResourcePage) getCurrentPage()).refresh();
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IContributedContentsView.class) {
			return this;
		}

		return super.getAdapter(adapter);
	}

	public IWorkbenchPart getContributingPart() {

		PageRec pr = getPageRec(getCurrentPage());
		if (pr != null) {

			// test to see if current editor is editor from page
			try {
				if (pr.part != EclipseUtils.getActiveSafeEditor()) {

					PageRec current1 = getPageRec(getCurrentPage());
					PageRec current2 = getPageRec(EclipseUtils
							.getActiveSafeEditor());

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
	public void partActivated(IWorkbenchPart part) {
		super.partActivated(part);

		// handle acuqiring of the selection listener from editor
		IResourceViewPartSelectionListenerSupport listenerSupport = (IResourceViewPartSelectionListenerSupport) part
				.getAdapter(IResourceViewPartSelectionListenerSupport.class);
		if (listenerSupport != null) {
			ISelectionListener activeSelectionListener = listenerSupport
					.getSelectionListenerForResourceView(((ResourcePage) getCurrentPage())
							.getViewer());
			resourceViewSelectionManager
					.setCurrentSelectionListener(activeSelectionListener);
		}

	}

	public void partDeactivated(IWorkbenchPart part) {
		// resourceViewSelectionManager.setCurrentSelectionListener(null);
	}

}

class ResourceViewSelectionListenerManager implements ISelectionListener {
	private ISelectionListener currentSelectionListener;

	public ResourceViewSelectionListenerManager() {

	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (currentSelectionListener != null) {
			currentSelectionListener.selectionChanged(part, selection);
		}

	}

	public ISelectionListener getCurrentSelectionListener() {
		return currentSelectionListener;
	}

	public void setCurrentSelectionListener(
			ISelectionListener currentSelectionListener) {
		this.currentSelectionListener = currentSelectionListener;
	}
}