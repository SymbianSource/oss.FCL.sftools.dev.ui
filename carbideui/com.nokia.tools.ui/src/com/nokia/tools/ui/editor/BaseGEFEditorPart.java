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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;

import com.nokia.tools.ui.Activator;

public abstract class BaseGEFEditorPart extends EditorPart
    implements CommandStackEventListener, ISelectionChangedListener,
    IShowEditorInput, ITabbedPropertySheetPageContributor, IGotoMarker {
	public static final int PROP_VIEWER = 1001;
	protected static final int TOTAL_SAVING_WORK = 10000;

	private EditDomain editDomain;
	private ActionRegistry actionRegistry;
	private List<String> stackActions = new ArrayList<String>();
	private List<String> selectionActions = new ArrayList<String>();
	private ResourceTracker resourceListener = new ResourceTracker();
	private IPropertySheetPage propertySheetPage;
	private boolean isEditorSaving;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
	 * org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input)
	    throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
		initActionBars();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
	 */
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		superSetInput(input);
	}

	protected void superSetInput(IEditorInput input) {
		// The workspace never changes for an editor. So, removing and re-adding
		// the resourceListener is not necessary. But it is being done here for
		// the sake of proper implementation. Plus, the resourceListener needs
		// to be added to the workspace the first time around.
		if (getEditorInput() instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			file.getWorkspace().removeResourceChangeListener(resourceListener);
		}

		super.setInput(input);

		if (getEditorInput() instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			file.getWorkspace().addResourceChangeListener(resourceListener);
			setPartName(file.getName());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.IShowEditorInput#showEditorInput(org.eclipse.ui.IEditorInput
	 * )
	 */
	public void showEditorInput(IEditorInput editorInput) {
		showEditorInput(editorInput, false);
	}

	/**
	 * Shows the editor input.
	 * 
	 * @param editorInput the editor input to show.
	 * @param isNew true if the editor input is a new one so that there is no
	 *            need to refresh gallery because the gallery will be recreated
	 *            by the gallery view part, false if the editor input changed
	 *            but editor remains the same.
	 */
	protected void showEditorInput(IEditorInput input, boolean isNew) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return editDomain != null && editDomain.getCommandStack().isDirty();
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			isEditorSaving = true;
			if (monitor == null) {
				monitor = getProgressMonitor();
			}
			monitor.beginTask(Messages.Task_Saving, TOTAL_SAVING_WORK);
			performSave(monitor);
			getEditDomain().getCommandStack().markSaveLocation();
			firePropertyChange(PROP_DIRTY);
		} finally {
			isEditorSaving = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySheetPage.class) {
			return getPropertySheetPage();
		}
		if (adapter == CommandStack.class) {
			return getEditDomain().getCommandStack();
		}
		if (adapter == EditDomain.class) {
			return getEditDomain();
		}
		if (adapter == EditPartViewer.class) {
			return getEditPartViewer();
		}
		if (adapter == ActionRegistry.class) {
			return getActionRegistry();
		}
		if (adapter == ContextMenuProvider.class) {
			return createContextMenuProvider();
		}
		if (IGotoMarker.class == adapter) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	protected void closeEditor(boolean save) {
		getSite().getPage().closeEditor(this, save);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		try {
			if (actionRegistry != null) {
				actionRegistry.dispose();
			}
		} catch (Throwable e) {
			Activator.error(e);
		}
		try {
			// removes the AddTask and AddBookmark actions upon disposal
			getEditorSite().getActionBars().clearGlobalActionHandlers();
		} catch (Throwable e) {
			Activator.error(e);
		}

		try {
			if (getEditorInput() instanceof IFileEditorInput) {
				IFile file = ((IFileEditorInput) getEditorInput()).getFile();
				file.getWorkspace().removeResourceChangeListener(
				    resourceListener);
			}
		} catch (Throwable e) {
			Activator.error(e);
		}
		try {
			getSite().setSelectionProvider(null);
		} catch (Throwable e) {
			Activator.error(e);
		}
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.gef.commands.CommandStackEventListener#commandStackChanged
	 * (stackChanged(CommandStackEvent event)
	 */
	public void stackChanged(CommandStackEvent event) {
		if (event.getCommand() != null) {
			if ((event.getDetail() & (CommandStack.POST_EXECUTE
			    | CommandStack.POST_REDO | CommandStack.POST_UNDO)) != 0) {
				firePropertyChange(PROP_DIRTY);
				updateActions(getStackActions());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(
	 * org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		// postpone notification, because it seems that at this time selection
		// provider still returns old selection
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				updateActions(getSelectionActions());
			}
		});
	}

	/**
	 * Updates the specified actions.
	 * 
	 * @param actionIds ids of actions to be updated.
	 */
	protected void updateActions(List<String> actionIds) {
		ActionRegistry registry = getActionRegistry();
		for (String id : actionIds) {
			IAction action = registry.getAction(id);
			if (action instanceof UpdateAction) {
				((UpdateAction) action).update();
			}
		}
	}

	/**
	 * @return the stack actions.
	 */
	public List<String> getStackActions() {
		return stackActions;
	}

	/**
	 * @return the selection actions.
	 */
	public List<String> getSelectionActions() {
		return selectionActions;
	}

	/**
	 * Sets the action bars.
	 */
	protected void initActionBars() {
		ActionRegistry registry = getActionRegistry();
		IActionBars bars = ((IEditorSite) getSite()).getActionBars();
		String id = ActionFactory.UNDO.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.REDO.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.COPY.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.CUT.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.PASTE.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.DELETE.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
	}

	protected final synchronized ActionRegistry getActionRegistry() {
		if (actionRegistry == null) {
			actionRegistry = createActionRegistry();
			createActions();
		}
		return actionRegistry;
	}

	protected ActionRegistry createActionRegistry() {
		return new ActionRegistry();
	}

	protected void createActions() {
		ActionRegistry registry = getActionRegistry();
		IAction action = new UndoAction(this);
		registry.registerAction(action);
		getStackActions().add(action.getId());

		action = new RedoAction(this);
		registry.registerAction(action);
		getStackActions().add(action.getId());

		action = new DeleteAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
	}

	/**
	 * @param editDomain the editDomain to set
	 */
	public void setEditDomain(EditDomain editDomain) {
		if (this.editDomain != editDomain && editDomain != null) {
			this.editDomain = editDomain;
			editDomain.getCommandStack().addCommandStackEventListener(this);
		}
	}

	protected final synchronized EditDomain getEditDomain() {
		if (editDomain == null) {
			EditDomain domain = createEditDomain();
			setEditDomain(domain);
		}
		return editDomain;
	}

	protected EditDomain createEditDomain() {
		return new DefaultEditDomain(this);
	}

	protected final synchronized IPropertySheetPage getPropertySheetPage() {
		if (propertySheetPage == null || propertySheetPage.getControl() == null
		    || propertySheetPage.getControl().isDisposed()) {
			propertySheetPage = createPropertySheetPage();
		}
		return propertySheetPage;
	}

	protected IPropertySheetPage createPropertySheetPage() {
		return null;
	}

	protected ContextMenuProvider createContextMenuProvider() {
		return new BaseContextMenuProvider(getEditPartViewer());
	}

	protected KeyHandler createKeyHandler() {
		return new KeyHandler();
	}

	protected abstract EditPartViewer getEditPartViewer();

	protected void initializeEditPartViewer(EditPartViewer viewer) {
		viewer.setEditDomain(getEditDomain());
		viewer.addSelectionChangedListener(this);
		viewer.setKeyHandler(createKeyHandler());
		viewer.setContextMenu(createContextMenuProvider());
	}

	/**
	 * Performs the save action.
	 * 
	 * @param monitor the progress monitor, total work is defined as
	 *            {@value #TOTAL_SAVING_WORK}
	 */
	protected abstract void performSave(IProgressMonitor monitor);

	/**
	 * Disposes the content when the editor input file has been modified from
	 * outside the editor.
	 */
	protected abstract void disposeContent();

	/**
	 * Returns the progress monitor related to this editor. It should not be
	 * necessary to extend this method.
	 * 
	 * @return the progress monitor related to this editor
	 * @since 2.1
	 */
	public IProgressMonitor getProgressMonitor() {
		IProgressMonitor pm = null;

		IStatusLineManager manager = getStatusLineManager();
		if (manager != null)
			pm = manager.getProgressMonitor();

		return pm != null ? pm : new NullProgressMonitor();
	}

	/**
	 * Returns the status line manager of this editor.
	 * 
	 * @return the status line manager of this editor
	 * @since 2.0
	 */
	private IStatusLineManager getStatusLineManager() {

		IEditorActionBarContributor contributor = getEditorSite()
		    .getActionBarContributor();
		if (!(contributor instanceof EditorActionBarContributor))
			return null;

		IActionBars actionBars = ((EditorActionBarContributor) contributor)
		    .getActionBars();
		if (actionBars == null)
			return null;

		return actionBars.getStatusLineManager();
	}

	// This class listens to changes to the file system in the workspace, and
	// makes changes accordingly.
	// 1) An open, saved file gets deleted -> close the editor
	// 2) An open file gets renamed or moved -> change the editor's input
	// accordingly
	class ResourceTracker
	    implements IResourceChangeListener, IResourceDeltaVisitor {
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta delta = event.getDelta();
			try {
				if (delta != null)
					delta.accept(this);
			} catch (CoreException exception) {
				// What should be done here?
			}
		}

		public boolean visit(final IResourceDelta delta) {
			if (delta == null
			    || !delta.getResource().equals(
			        ((IFileEditorInput) getEditorInput()).getFile()))
				return true;

			if (delta.getKind() == IResourceDelta.REMOVED) {
				Display display = getSite().getShell().getDisplay();
				if ((IResourceDelta.MOVED_TO & delta.getFlags()) == 0) {
					// if the file was deleted
					// NOTE: The case where an open, unsaved file is deleted is
					// being handled by the
					// PartListener added to the Workbench in the initialize()
					// method.
					display.asyncExec(new Runnable() {
						public void run() {
							// no need to check dirty, close the edior anyway
							// because this is common eclipse editor behavior
							closeEditor(false);
						}
					});
				} else { // else if it was moved or renamed
					final IFile newFile = ResourcesPlugin.getWorkspace()
					    .getRoot().getFile(delta.getMovedToPath());
					display.asyncExec(new Runnable() {
						public void run() {
							superSetInput(new FileEditorInput(newFile));
						}
					});
				}
			} else if (delta.getKind() == IResourceDelta.CHANGED) {
				if (!isEditorSaving
				    && (0 != (delta.getFlags() & IResourceDelta.CONTENT))) {
					// the file was overwritten somehow

					Display display = getSite().getShell().getDisplay();
					display.asyncExec(new Runnable() {
						public void run() {
							if (MessageDialog.openQuestion(
							    getSite().getShell(),
							    Messages.FileChanged_Title,
							    Messages.FileChanged_Message)) {
								final IFile newFile = ResourcesPlugin
								    .getWorkspace().getRoot().getFile(
								        delta.getFullPath());
								// disposes any old contents related to the old
								// input
								disposeContent();
								showEditorInput(new FileEditorInput(newFile),
								    true);
								getEditDomain().getCommandStack().flush();
							}
						}
					});
				}
			}
			return false;
		}
	}
}
