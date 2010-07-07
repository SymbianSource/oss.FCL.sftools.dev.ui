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
package com.nokia.tools.s60.ide.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.s60.editor.actions.AbstractAction;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.core.INamingAdapter;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.ui.actions.AddBookmarkAction;
import com.nokia.tools.screen.ui.editor.ScreenEditorPart;

/**
 * Add Bookmark for icon view action
 */
public class AddBookmarkViewAction extends AbstractAction {	

	public AddBookmarkViewAction(IWorkbenchPart part) {
		super(part);		
	}
	
	public AddBookmarkViewAction(ISelectionProvider p) {
		super(null);
		setSelectionProvider(p);
	}
	
	@Override
	protected void init() {
		setId(IDEActionFactory.BOOKMARK.getId());
		IWorkbenchAction dummy = IDEActionFactory.BOOKMARK.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		setText(dummy.getText());
		setToolTipText(dummy.getToolTipText());
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/etool16/bkmrk_nav.gif"));
		super.init();
	}

	@Override
	protected void doRun(Object _element) {
		
		IContentData cdata = getContentData(_element);

		if (cdata != null) {

			IResource resource = getResource();
			Map<String, Object> attribs = new HashMap<String, Object>(11);
						
			// init attributes

			IScreenElement screenEl = getScreenElement(_element);
			INamingAdapter ina = (INamingAdapter) cdata.getAdapter(INamingAdapter.class);
			String elementName = ina == null ? cdata.getName() : ina.getName();			
			if (screenEl != null && screenEl.getParent() != null) {
				String screenName = screenEl.getParent().getText();
				attribs.put(IMarker.MESSAGE, elementName + " ("
						+ screenName + ")");
				attribs.put(ScreenEditorPart.SCREEN, screenName);
			} else {
				String categoryName = cdata.getParent().getName();
				attribs.put(IMarker.MESSAGE, elementName + " (" + categoryName + ")");				
			} 
			attribs.put(ContentAttribute.NAME.name(), elementName);
			attribs.put(ContentAttribute.ID.name(), cdata.getId());					
			
			Shell shell = getActiveEditorPart().getSite().getShell();
			
			if (AddBookmarkAction.askForLabel(attribs, shell)) {
				// create bookmark and show bookmarks view
				try {
					MarkerUtilities.createMarker(resource, attribs,
							IMarker.BOOKMARK);
				} catch (CoreException x) {
					S60WorkspacePlugin.error(x);
				}

				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				if (page != null) {
					try {
						page.showView(IPageLayout.ID_BOOKMARKS, null,
								IWorkbenchPage.VIEW_CREATE);
						page.showView(IPageLayout.ID_BOOKMARKS, null,
								IWorkbenchPage.VIEW_ACTIVATE);

					} catch (PartInitException e) {						
					}
				}
			}
		}
	}	

}
