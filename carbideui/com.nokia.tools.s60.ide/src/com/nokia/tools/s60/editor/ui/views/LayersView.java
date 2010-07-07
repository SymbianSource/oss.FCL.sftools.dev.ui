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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.PageSite;

import com.nokia.tools.content.core.IContentService;
import com.nokia.tools.s60.editor.GraphicsEditorPart;
import com.nokia.tools.s60.editor.Series60EditorPart;
import com.nokia.tools.s60.views.ColorView;
import com.nokia.tools.s60.views.IconView;
import com.nokia.tools.s60.views.SearchView;
import com.nokia.tools.screen.ui.utils.EclipseUtils;

/**
 */
public class LayersView extends PageBookView implements ISelectionListener,
		IContributedContentsView {

	public static final String LAYERS_CONTEXT = "com.nokia.tools.s60.ide" + '.' + "layers_context"; 

	public static final String ID = "com.nokia.tools.s60.editor.ui.views.LayersView";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#createDefaultPage(org.eclipse.ui.part.PageBook)
	 */
	@Override
	protected IPage createDefaultPage(PageBook book) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(book,
				LayersView.LAYERS_CONTEXT);
		MessagePage page = new MessagePage();
		initPage(page);
		page.createControl(book);
		page.setMessage("Layers is not available.");
		return page;
	}

	public LayersView() {
		super();
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService().addSelectionListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService().removeSelectionListener(this);
		if (getCurrentPage() == getDefaultPage()) {
			setDefaultName();
		}
		super.dispose();
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		Series60EditorPart editor = (Series60EditorPart) part
				.getAdapter(Series60EditorPart.class);
		if (editor != null) {
			LayersPage page = new LayersPage(editor);
			page.setParentView(this);
			page.init(new PageSite(getViewSite()));
			page.createControl(getPageBook());
			((IContentService) part.getAdapter(IContentService.class)).addContentListener(page);
			return new PageRec(part, page);
		} else if (part instanceof GraphicsEditorPart) {
			// animation editor - create LayerPage for this specific
			ActiveLayersPage page = new ActiveLayersPage(part);
			page.init(new PageSite(getViewSite()));
			page.createControl(getPageBook());
			return new PageRec(part, page);
		}
		return null;
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		pageRecord.page.dispose();
		pageRecord.dispose();

		// restores to default name when the part is closed, also handles
		// closing one editor while another is open
		updatePartName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#showPageRec(org.eclipse.ui.part.PageBookView.PageRec)
	 */
	@Override
	protected void showPageRec(PageRec pageRec) {
		super.showPageRec(pageRec);
		// here we update the title when switching the editors, the one based on
		// selection change doesn't work because selection comes early than the
		// current page changes
		updatePartName();
	}

	private void updatePartName() {
		
		if (getCurrentPage() instanceof ILayersPage) {
			ILayersPage cp = (ILayersPage) getCurrentPage();
			if (cp != null) {
				String partname = cp.getPartName(getDefaultName());
				setPartName(partname);
			}
		} else {
			setDefaultName();
		}
	}

	private String getDefaultName() {
		return getConfigurationElement().getAttribute("name");
	}

	private void setDefaultName() {
		String defaultName = getDefaultName();
		if (defaultName != null) {
			setPartName(defaultName);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#getBootstrapPart()
	 */
	@Override
	protected IWorkbenchPart getBootstrapPart() {
		IWorkbenchPage page = getSite().getPage();
		if (page != null) {
			return page.getActiveEditor();
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#isImportant(org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		return (part.getAdapter(Series60EditorPart.class) != null || part instanceof GraphicsEditorPart);
	}

	/*
	 * special handling of icon view - when displaying no content, we need to
	 * display default page (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#getPageRec(org.eclipse.ui.IWorkbenchPart)
	 */
	protected PageRec getPageRec(IWorkbenchPart part) {
		if (part instanceof IconView) {
			if (((IconView) part).getCurrentPage() == ((IconView) part)
					.getDefaultPage()) {
				// return null pagerec - for default page
				return null;
			}
		}
		PageRec pr = super.getPageRec(part);
		return pr;
	}

	public void addLayerSelectionListener(LayerSelectionListener listener) {
		((ILayersPage) getCurrentPage()).addLayerSelectionListener(listener);
	}

	public void removeLayerSelectionListener(LayerSelectionListener listener) {
		((ILayersPage) getCurrentPage()).removeLayerSelectionListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part != this && getCurrentPage() instanceof ISelectionListener) {
			if (getCurrentPage() instanceof ActiveLayersPage) {
				if (part instanceof GraphicsEditorPart) {
					((ISelectionListener) getCurrentPage()).selectionChanged(
							part, selection);
				}
			} else if (part instanceof ColorView || part instanceof SearchView) {
				return;
			} else {
				((ISelectionListener) getCurrentPage()).selectionChanged(part,
						selection);
			}
		}

		if (getCurrentPage() instanceof ILayersPage) {
			updatePartName();
		} else {
			setDefaultName();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IContributedContentsView.class) {
			return this;
		}
		
		return super.getAdapter(adapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.IContributedContentsView#getContributingPart()
	 */
	public IWorkbenchPart getContributingPart() {
		IEditorPart part = EclipseUtils.getActiveSafeEditor();
		if (part != null
				&& (part.getAdapter(Series60EditorPart.class) != null || part instanceof GraphicsEditorPart)) {
			return part;
		}
		return null;
	}
}