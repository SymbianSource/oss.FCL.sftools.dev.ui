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
package com.nokia.tools.screen.ui.editor;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.texteditor.InfoForm;
import org.eclipse.ui.views.properties.IPropertySheetPage;

import com.nokia.tools.editing.ui.figure.ViewportCenterLayout;
import com.nokia.tools.screen.ui.IScreenConstants;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.actions.AddBookmarkAction;
import com.nokia.tools.screen.ui.actions.AddTaskAction;
import com.nokia.tools.screen.ui.propertysheet.tabbed.WidgetTabbedPropertySheetPage;
import com.nokia.tools.ui.editor.BaseGraphicalEditorPart;
import com.nokia.tools.ui.view.BaseContentOutlinePage;

/**
 * Base class for the editor part that will use the JEM model and VE's target VM
 * architecture.
 * 
 */
public abstract class ScreenEditorPart extends BaseGraphicalEditorPart {
	public static final String SCREEN = "screen";

	public static final String CONTRIBUTOR_ID = "com.nokia.tools.screen.ui.propertysheet.tabbed.TabbedProperty";

	private InfoForm errorForm;

	private CenterFigureListener listener;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.editor.BaseEditorPart#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);

		String error = checkInput();
		if (error != null) {
			return;
		}
		listener = new CenterFigureListener();
	}

	protected abstract String checkInput();

	protected void createErrorForm(Composite parent, String info) {
		createErrorForm(parent, null, null, info);
	}

	protected void createErrorForm(Composite parent, String header,
			String banner, String info) {
		errorForm = new InfoForm(parent);
		if (header != null) {
			errorForm.setHeaderText(header);
		}
		if (banner != null) {
			errorForm.setBannerText(banner);
		}
		if (info != null) {
			errorForm.setInfo(info);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (errorForm != null) {
			errorForm.getControl().setFocus();
		}
	}

	/**
	 * @return the project associated with the current editor input.
	 */
	public IProject getProject() {
		IEditorInput input = getEditorInput();
		if(null == input)
			return null;
		if (input instanceof IFileEditorInput) {
			return ((IFileEditorInput) input).getFile().getProject();
		}
		return (IProject) input.getAdapter(IProject.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.editor.BaseEditorPart#initializeViewers()
	 */
	protected void initializeViewers() {
		super.initializeViewers();
		IPreferenceStore store = UiPlugin.getDefault().getPreferenceStore();
		if (store.getBoolean(IScreenConstants.PREF_ZOOMING_GLOBAL)) {
			ZoomManager manager = (ZoomManager) getAdapter(ZoomManager.class);
			double factor = store
					.getDouble(IScreenConstants.PREF_ZOOMING_FACTOR);
			manager.setZoom(factor);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.editor.BaseEditorPart#createActions()
	 */
	protected void createActions() {
		super.createActions();

		// create actions from contributors for 'show in' submenu
		for (Class<WorkbenchPartAction> cl : ShowInContributionResolver.INSTANCE
				.getContributedActions()) {
			Object[] params = new Object[] { this };
			try {
				WorkbenchPartAction act = cl.getConstructor(
						new Class[] { IWorkbenchPart.class }).newInstance(
						params);
				if (getActionRegistry().getAction(act.getId()) == null)
					getActionRegistry().registerAction(act);
				ShowInContributionResolver.INSTANCE.addIdMapping(cl, act
						.getId());
			} catch (Exception e) {
				System.out.println("Cannot instantiate: " + cl.getName());
				e.printStackTrace();
			}
		}

		IAction action = new AddTaskAction(this); //$NON-NLS-1$
		action.setActionDefinitionId(IDEActionFactory.ADD_TASK.getId());
		action.setId(IDEActionFactory.ADD_TASK.getId());
		getActionRegistry().registerAction(action);
		getEditorSite().getActionBars().setGlobalActionHandler(
				IDEActionFactory.ADD_TASK.getId(), action);

		action = new AddBookmarkAction(this); //$NON-NLS-1$
		action.setActionDefinitionId(IDEActionFactory.BOOKMARK.getId());
		action.setId(IDEActionFactory.BOOKMARK.getId());
		getActionRegistry().registerAction(action);
		getEditorSite().getActionBars().setGlobalActionHandler(
				IDEActionFactory.BOOKMARK.getId(), action);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.editor.BaseEditorPart#createContentOutlinePage(org.eclipse.gef.ui.parts.TreeViewer)
	 */
	protected BaseContentOutlinePage createContentOutlinePage(TreeViewer viewer) {
		return new ScreenContentOutlinePage(viewer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.editor.BaseEditorPart#createPropertySheetPage()
	 */
	protected IPropertySheetPage createPropertySheetPage() {
		return new WidgetTabbedPropertySheetPage(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.ui.properties.internal.provisional.ITabbedPropertySheetPageContributor#getContributorId()
	 */
	public String getContributorId() {
		return CONTRIBUTOR_ID;
	}

	/**
	 * This listener listens to the events related to the figure resizing and
	 * zooming and then recalculate the position of the screen figure in order
	 * to center it in the viewport.
	 * 
	 */
	class CenterFigureListener implements FigureListener, ZoomListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.draw2d.FigureListener#figureMoved(org.eclipse.draw2d.IFigure)
		 */
		public void figureMoved(IFigure source) {
			FreeformViewport viewport = (FreeformViewport) getViewport();
			if (viewport == null) {
				return;
			}
			viewport.getHorizontalRangeModel().setValue(0);
			viewport.getVerticalRangeModel().setValue(0);
			GraphicalEditPart part = (GraphicalEditPart) getEditPartViewer()
					.getContents();
			part.getFigure().revalidate();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.gef.editparts.ZoomListener#zoomChanged(double)
		 */
		public void zoomChanged(double zoom) {
			((ViewportCenterLayout) ((GraphicalEditPart) getEditPartViewer()
					.getRootEditPart().getChildren().get(0)).getFigure()
					.getLayoutManager()).setZoom(zoom);
			figureMoved(getViewport());
		}
	}

	/**
	 * @return the viewport figure, on which the figure is centered.
	 */
	private IFigure getViewport() {
		if (getEditPartViewer() == null
				|| getEditPartViewer().getContents() == null) {
			return null;
		}
		IFigure fig = ((GraphicalEditPart) getEditPartViewer().getContents())
				.getFigure();
		while (!(fig instanceof FreeformViewport)) {
			fig = fig.getParent();
		}
		return fig;
	}

	/**
	 * Adds figure listeners for handling zooming/resizing events.
	 */
	protected void addFigureListeners() {
		if (getEditPartViewer() == null) {
			return;
		}
		IFigure viewport = getViewport();
		if (viewport != null) {
			viewport.addFigureListener(listener);
		}
		((ScalableFreeformRootEditPart) getEditPartViewer().getRootEditPart())
				.getZoomManager().addZoomListener(listener);
	}

	/**
	 * Removes figure listeners.
	 */
	protected void removeFigureListeners() {
		if (getEditPartViewer() == null) {
			return;
		}
		IFigure viewport = getViewport();
		if (viewport != null) {
			viewport.removeFigureListener(listener);
			((ScalableFreeformRootEditPart) getEditPartViewer()
					.getRootEditPart()).getZoomManager().removeZoomListener(
					listener);
		}
	}

}
