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

package com.nokia.tools.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.nokia.tools.ui.action.ShareableZoomInAction;
import com.nokia.tools.ui.action.ShareableZoomOutAction;
import com.nokia.tools.ui.view.BaseContentOutlinePage;

public abstract class BaseGraphicalEditorPart extends BaseGEFEditorPart {
	private BaseContentOutlinePage contentOutlinePage;
	private SelectionSynchronizer selectionSynchronizer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IContentOutlinePage.class) {
			return getContentOutlinePage();
		}
		if (adapter == EditPartViewer.class || adapter == GraphicalViewer.class) {
			return getEditPartViewer();
		}
		if (adapter == SelectionSynchronizer.class) {
			return getSelectionSynchronizer();
		}
		if (adapter == PaletteRoot.class) {
			PaletteViewer viewer = getEditDomain().getPaletteViewer();
			if (viewer != null) {
				return viewer.getPaletteRoot();
			}
			return null;
		}
		if (adapter == ZoomManager.class) {
			EditPartViewer viewer = getEditPartViewer();
			if (viewer == null) {
				return null;
			}
			if (viewer.getRootEditPart() instanceof ScalableFreeformRootEditPart) {
				return ((ScalableFreeformRootEditPart) viewer.getRootEditPart())
				    .getZoomManager();
			}
			return null;
		}
		return super.getAdapter(adapter);
	}

	protected final synchronized SelectionSynchronizer getSelectionSynchronizer() {
		if (selectionSynchronizer == null) {
			selectionSynchronizer = createSelectionSynchronizer();
		}
		return selectionSynchronizer;
	}

	protected SelectionSynchronizer createSelectionSynchronizer() {
		return new SelectionSynchronizer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.editor.BaseGEFEditorPart#createContextMenuProvider()
	 */
	@Override
	protected ContextMenuProvider createContextMenuProvider() {
		return new BaseGraphicalContextMenuProvider(getEditPartViewer());
	}

	protected final synchronized ContentOutlinePage getContentOutlinePage() {
		if (contentOutlinePage == null
		    || contentOutlinePage.getControl() == null
		    || contentOutlinePage.getControl().isDisposed()) {
			TreeViewer viewer = new TreeViewer();
			getEditDomain().addViewer(viewer);
			contentOutlinePage = createContentOutlinePage(viewer);
		}
		return contentOutlinePage;
	}

	protected BaseContentOutlinePage createContentOutlinePage(TreeViewer viewer) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.editor.BaseGEFEditorPart#initializeEditPartViewer(org.eclipse.gef.EditPartViewer)
	 */
	protected void initializeEditPartViewer(EditPartViewer viewer) {
		super.initializeEditPartViewer(viewer);
		if (viewer.getRootEditPart() instanceof ScalableFreeformRootEditPart) {
			ScalableFreeformRootEditPart rootEditPart = (ScalableFreeformRootEditPart) viewer
			    .getRootEditPart();
			// supported zooming schemes
			List<String> zoomLevels = new ArrayList<String>(3);
			zoomLevels.add(ZoomManager.FIT_ALL);
			zoomLevels.add(ZoomManager.FIT_WIDTH);
			zoomLevels.add(ZoomManager.FIT_HEIGHT);
			rootEditPart.getZoomManager().setZoomLevelContributions(zoomLevels);
			rootEditPart.getZoomManager().setZoomAnimationStyle(
			    ZoomManager.ANIMATE_ZOOM_IN_OUT);

			if (getActionRegistry().getAction(GEFActionConstants.ZOOM_IN) == null) {
				ShareableZoomInAction zoomIn = new ShareableZoomInAction(
				    rootEditPart.getZoomManager());
				getActionRegistry().registerAction(zoomIn);
				getSite().getKeyBindingService().registerAction(zoomIn);
			}

			if (getActionRegistry().getAction(GEFActionConstants.ZOOM_OUT) == null) {
				ShareableZoomOutAction zoomOut = new ShareableZoomOutAction(
				    rootEditPart.getZoomManager());
				getActionRegistry().registerAction(zoomOut);
				getSite().getKeyBindingService().registerAction(zoomOut);
			}
		}
	}

	protected void initializeViewers() {
		EditPartViewer viewer = getEditPartViewer();
		getSite().setSelectionProvider(viewer);
		viewer.setContextMenu(createContextMenuProvider());
		if (viewer.getRootEditPart() instanceof ScalableFreeformRootEditPart) {
			ScalableFreeformRootEditPart rootEditPart = (ScalableFreeformRootEditPart) viewer
			    .getRootEditPart();
			ZoomManager manager = rootEditPart.getZoomManager();
			ShareableZoomInAction zoomIn = (ShareableZoomInAction) getActionRegistry()
			    .getAction(GEFActionConstants.ZOOM_IN);
			ShareableZoomOutAction zoomOut = (ShareableZoomOutAction) getActionRegistry()
			    .getAction(GEFActionConstants.ZOOM_OUT);
			if (zoomIn != null) {
				zoomIn.setZoomManager(manager);
				zoomIn.zoomChanged(1.0);
			}
			if (zoomOut != null) {
				zoomOut.setZoomManager(manager);
				zoomOut.zoomChanged(1.0);
			}
		}
		getSelectionSynchronizer().addViewer(viewer);
		getSite().setSelectionProvider(viewer);

		if (contentOutlinePage != null) {
			contentOutlinePage.clearSelections();
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					contentOutlinePage.initializeOutlineViewer();
				}
			});
		}
		firePropertyChange(PROP_VIEWER);
	}
}
