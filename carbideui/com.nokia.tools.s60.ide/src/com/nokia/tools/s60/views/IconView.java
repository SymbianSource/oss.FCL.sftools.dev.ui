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

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.PageSite;

import com.nokia.tools.content.core.IContentService;
import com.nokia.tools.s60.editor.actions.EditImageInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.EditImageInSVGEditorAction;
import com.nokia.tools.s60.editor.ui.views.LayersView;
import com.nokia.tools.screen.ui.editor.IContentDependentEditor;
import com.nokia.tools.screen.ui.editor.ScreenEditorPart;
import com.nokia.tools.screen.ui.utils.EclipseUtils;

/**
 */
public class IconView extends PageBookView implements ISelectionListener,
		ISelectionProvider, IContributedContentsView, ISetSelectionTarget {

	public static final String ID = "com.nokia.tools.s60.views.IconView";
	//public static final String ICON_CONTEXT = "com.nokia.tools.s60.ide.icon_context"; 
	public static final String ICON_CONTEXT = "com.nokia.tools.s60.ide" + '.' + "icon_context";
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getSite().getWorkbenchWindow().getPartService().addPartListener(this);
		getSite().setSelectionProvider(this);
		getSite().getPage().addSelectionListener(this);
		site.getActionBars().setGlobalActionHandler(
				EditImageInSVGEditorAction.ID,
				new EditImageInSVGEditorAction((ISelectionProvider) this,
						getCommandStack()));
		site.getActionBars().setGlobalActionHandler(
				EditImageInBitmapEditorAction.ID,
				new EditImageInBitmapEditorAction((ISelectionProvider) this,
						getCommandStack()));
		site.getActionBars().updateActionBars();
		getSite().getWorkbenchWindow().getSelectionService()
				.addPostSelectionListener(this);
	}
	
	

	public CommandStack getCommandStack() {
		return (CommandStack) getAdapter(CommandStack.class);
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == CommandStack.class || adapter == ActionRegistry.class) {
			if (getContributingPart() != null) {
				return getContributingPart().getAdapter(adapter);
			}
		}
		if (adapter == IContributedContentsView.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	/**
	 * Called from resource view to set icon-set to be displayed
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IPage page = getCurrentPage();
		if (page instanceof IconViewPage) {
			if (selection == null) {
				((IconViewPage) page).clearArea();
			} else {
				((IconViewPage) page).selectionChanged(part, selection);
			}
			updatePartName();
		}
	}

	private void updatePartName() {
		IPage page = getCurrentPage();
		if (page instanceof IconViewPage) {
			String name = ((IconViewPage) page).getPartName();
			if (name == null)
				name = ViewMessages.IconsView_partNameDefault;
			setPartName(name);
		} else {
			setPartName(ViewMessages.IconsView_partNameDefault);
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (getCurrentPage() instanceof IconViewPage) {
			IconViewPage ivp = (IconViewPage) getCurrentPage();
			ivp.setFocus();
		}
	}

	public ISelection getSelection() {
		IPage page = getCurrentPage();
		if (page instanceof ISelectionProvider) {
			return ((ISelectionProvider) page).getSelection();
		}
		return StructuredSelection.EMPTY;
	}

	private List<ISelectionChangedListener> selListeners = new ArrayList<ISelectionChangedListener>();

	protected void fireSelectionChangeEvent() {
		SelectionChangedEvent event = new SelectionChangedEvent(this,
				getSelection());
		for (ISelectionChangedListener l : selListeners
				.toArray(new ISelectionChangedListener[selListeners.size()])) {
			try {
				l.selectionChanged(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (!selListeners.contains(listener))
			selListeners.add(listener);
	}

	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		selListeners.remove(listener);
	}

	public void setSelection(ISelection selection) {
		
	}

	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage page = new MessagePage();
		initPage(page);
		page.createControl(book);
		page.setMessage("Component is not available.");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(book,
				IconViewPage.ICON_CONTEXT);
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
		IContentService service = (IContentService) part
				.getAdapter(IContentService.class);
		if (service != null) {
			IconViewPage page = new IconViewPage((IEditorPart) part);
			page.setParent(this);
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
		// this is here to clear the selection on the properties page when the
		// editor is closed
		fireSelectionChangeEvent();
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
		if (part instanceof ScreenEditorPart) {
			return true;
		}
		// effectively filters out the audio editor but keeps the multipage
		// editor
		
		return part.getAdapter(ScreenEditorPart.class) != null;
	}

	@Override
	protected void showPageRec(PageRec pageRec) {
		super.showPageRec(pageRec);
		updatePartName();
	}

	public IWorkbenchPart getContributingPart() {

		PageRec pr = getPageRec(getCurrentPage());
		if (pr != null) {

			
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
	public void dispose() {
		try {
			getSite().getWorkbenchWindow().getPartService().removePartListener(
					this);
			getSite().getWorkbenchWindow().getSelectionService()
					.removePostSelectionListener(this);
		} catch (Exception e) {
		}
		super.dispose();
	}

	@Override
	public void partClosed(IWorkbenchPart part) {

		/*
		 * when graphics editor closed, do not dispose, it has borrowed page
		 * from parent ed.
		 */
		if (!(part instanceof IContentDependentEditor)) {
			super.partClosed(part);
		}
		/*
		 * Because IconView can get activated before editor actually closes,
		 * LayerView can get out of sync
		 */
		if (part instanceof ScreenEditorPart) {
			// if there is a layers view, refresh it contents
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			if (page != null) {
				LayersView view = (LayersView) page.findView(LayersView.ID);
				if (view != null) {
					try {
						// this forces LayersView to update to current state
						view.partActivated(this);
					} catch (Exception e) {
					}
				}
			}
		}
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		super.partActivated(part);
	}

	public void selectReveal(ISelection selection) {
		selectionChanged(null, selection);
	}
}