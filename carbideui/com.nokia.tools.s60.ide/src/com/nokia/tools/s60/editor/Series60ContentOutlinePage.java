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
package com.nokia.tools.s60.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.TreeEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.s60.editor.actions.AbstractAction;
import com.nokia.tools.s60.editor.actions.BrowseForFileAction;
import com.nokia.tools.s60.editor.actions.ClearImageEditorAction;
import com.nokia.tools.s60.editor.actions.ConvertAndEditSVGInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.CopyImageAction;
import com.nokia.tools.s60.editor.actions.EditImageInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.EditImageInSVGEditorAction;
import com.nokia.tools.s60.editor.actions.EditInSystemEditorAction;
import com.nokia.tools.s60.editor.actions.EditMaskAction;
import com.nokia.tools.s60.editor.actions.EditMaskAction2;
import com.nokia.tools.s60.editor.actions.EditSoundInSoundEditorAction;
import com.nokia.tools.s60.editor.actions.ElevenPieceConvertAction;
import com.nokia.tools.s60.editor.actions.Messages;
import com.nokia.tools.s60.editor.actions.NinePieceConvertAction;
import com.nokia.tools.s60.editor.actions.PasteImageAction;
import com.nokia.tools.s60.editor.actions.SetColorAction;
import com.nokia.tools.s60.editor.actions.SetStretchModeAction;
import com.nokia.tools.s60.editor.actions.SetStretchModeDDown;
import com.nokia.tools.s60.editor.commands.Series60EditorCommand;
import com.nokia.tools.s60.editor.dnd.S60BaseDragListener;
import com.nokia.tools.s60.editor.dnd.S60BaseDropListener;
import com.nokia.tools.s60.ide.ContributedActionsResolver;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.ide.actions.AddBookmarkViewAction;
import com.nokia.tools.s60.ide.actions.AddTaskViewAction;
import com.nokia.tools.s60.ide.actions.OpenGraphicsEditorAction;
import com.nokia.tools.screen.core.ICategoryAdapter;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.editor.ScreenContentOutlinePage;
import com.nokia.tools.screen.ui.editor.embedded.IEmbeddedEditorContributor;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IEmbeddedEditorDescriptor;
import com.nokia.tools.screen.ui.utils.ISeries60Command;

/**
 * This outline page provides two views: the default component view and the
 * category view that groups the elements into the corresponding theme
 * categories.
 */
public class Series60ContentOutlinePage extends ScreenContentOutlinePage
		implements ISelectionChangedListener, CommandStackEventListener,
		IDoubleClickListener {

	/** Containment view id. */
	private static final int ID_CONTAINMENT = 0;

	/** Category view id. */
	private static final int ID_CATEGORY = 1;

	private PageBook pageBook;

	private TreeViewer categoryViewer;

	private Action containmentLayoutAction, categoryLayoutAction;

	private boolean isCategoryDispatching, isViewerDispatching;

	private boolean categoryInitialized;

	private GraphicalViewer graphicalViewer;

	private CommandStack stack;

	private List<AbstractAction> selectionActions = new ArrayList<AbstractAction>();

	private int viewId = ID_CONTAINMENT;

	/* custom drop target listener */
	class CustomDropListener extends S60BaseDropListener {

		public CustomDropListener(Transfer t, CommandStack stack) {
			super(t, stack);
		}

		
		@Override
		protected ISkinnableEntityAdapter getSkinnableEntityAdapter(
				DropTargetEvent event) {
			ISkinnableEntityAdapter adapter = null;
			TreeEditPart part = (TreeEditPart) event.item.getData();
			Object model = part.getModel();
			IScreenElement screenElement = JEMUtil.getScreenElement(model);
			if (screenElement != null) {
				adapter = (ISkinnableEntityAdapter) screenElement.getData()
						.getAdapter(ISkinnableEntityAdapter.class);
			}
			return adapter;
		}

		@Override
		protected EditPart getSourceEditPart(DropTargetEvent event) {
			return (TreeEditPart) event.item.getData();
		}

		@Override
		protected IWorkbenchPart getWorkbenchPart() {
			return getSite().getPage().getActiveEditor();
		}

		@Override
		protected ISelectionProvider getSelectionProviderForGenericCommand(
				DropTargetEvent event) {
			return ExternalEditorSupport
					.getSelectionProvider(getSourceEditPart(event));
		}

	}

	/**
	 * Constructs an outline page.
	 * 
	 * @param editor the associated editor.
	 * @param viewer the default tree viewer.
	 */
	public Series60ContentOutlinePage(EditPartViewer viewer) {
		super(viewer);
		stack = (CommandStack) getEditorPart().getAdapter(CommandStack.class);
		if (stack != null) {
			stack.addCommandStackEventListener(this);
		}
	}

	private void setGlobalHandlers(IActionBars bars, ActionRegistry r,
			String... ids) {
		for (String s : ids) {
			IAction action = r.getAction(s);
			if (action != null) {
				bars.setGlobalActionHandler(action.getId(), action);
			}
			if (action instanceof AbstractAction) {
				selectionActions.add((AbstractAction) action);
				((AbstractAction) action).listenSelection();
			}
		}
	}

	@Override
	public void init(IPageSite pageSite) {
		super.init(pageSite);

		ArrayList<AbstractAction> l = new ArrayList<AbstractAction>();
		IActionBars bars = pageSite.getActionBars();
		ActionRegistry registry = (ActionRegistry) getEditorPart().getAdapter(
				ActionRegistry.class);

		setGlobalHandlers(bars, registry, EditImageInSVGEditorAction.ID,
				EditImageInBitmapEditorAction.ID,
				EditSoundInSoundEditorAction.ID,
				ConvertAndEditSVGInBitmapEditorAction.ID,
				EditInSystemEditorAction.ID,
				NinePieceConvertAction.ID_NINE,
				NinePieceConvertAction.ID_SINGLE,
				ElevenPieceConvertAction.ID_SINGLE,
				ElevenPieceConvertAction.ID_ELEVEN,
				/*ThreePieceConvertAction.ID_SINGLE,
				ThreePieceConvertAction.ID_THREE,*/
				ClearImageEditorAction.ID,
				EditMaskAction.ID, EditMaskAction2.ID,
				SetStretchModeAction.ID_Aspect,
				SetStretchModeAction.ID_Stretch, PasteImageAction.ID,
				CopyImageAction.ID);

		/* add task, bookmark */
		AbstractAction aa = new AddBookmarkViewAction(this);
		aa.listenSelection();
		bars.setGlobalActionHandler(aa.getId(), aa);
		l.add(aa);

		aa = new AddTaskViewAction(this);
		aa.listenSelection();
		bars.setGlobalActionHandler(aa.getId(), aa);
		l.add(aa);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.view.BaseContentOutlinePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		pageBook = new PageBook(parent, SWT.NONE);
		super.createControl(pageBook);
		categoryViewer = new CategoryTreeViewer(pageBook);
		categoryViewer.setContentProvider(new CategoryContentProvider());
		categoryViewer.setLabelProvider(new CategoryLabelProvider());
		categoryViewer.setSorter(new CategoryViewerSorter());
		categoryViewer.addSelectionChangedListener(this);
		categoryViewer.addDoubleClickListener(this);

		/* add context menu to category control */
		MenuManager menuMgr = new MenuManager("#PopupMenu"); 
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});

		Menu menu = menuMgr.createContextMenu(categoryViewer.getControl());
		categoryViewer.getControl().setMenu(menu);

		// DND support
		addDragAndDropSupport();
	}

	// DND support
	private void addDragAndDropSupport() {

		final LocalSelectionTransfer localTransfer = LocalSelectionTransfer
				.getInstance();
		final FileTransfer fileTransfer = FileTransfer.getInstance();

		/* DropTargetListener for category viewer */
		/*
		 * Transfer transfers[] = new Transfer[] { fileTransfer, localTransfer };
		 * categoryViewer.addDropSupport(DND.DROP_COPY | DND.DROP_DEFAULT |
		 * DND.DROP_MOVE, transfers, new CustomDropListener(null,
		 * getCommandStack()));
		 * 
		 * categoryViewer.addDragSupport(DND.DROP_COPY, transfers, new
		 * CustomDragListener(localTransfer, categoryViewer));
		 */

		/* add to edit part viewer */
		getViewer().addDropTargetListener(
				new CustomDropListener(localTransfer, getCommandStack()));

		getViewer().addDropTargetListener(
				new CustomDropListener(fileTransfer, getCommandStack()));

		getViewer().addDragSourceListener(
				new S60BaseDragListener(localTransfer, getViewer()));
		getViewer().addDragSourceListener(
				new S60BaseDragListener(fileTransfer, getViewer()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenContentOutlinePage#dispose()
	 */
	@Override
	public void dispose() {
		CommandStack stack = (CommandStack) getEditorPart().getAdapter(
				CommandStack.class);
		if (stack != null) {
			stack.removeCommandStackEventListener(this);
		}

		if (categoryViewer != null) {
			categoryViewer.removeSelectionChangedListener(this);
		}
		GraphicalViewer viewer = (GraphicalViewer) getEditorPart().getAdapter(
				GraphicalViewer.class);
		if (viewer != null) {
			viewer.removeSelectionChangedListener(this);
		}
		getSite().getActionBars().clearGlobalActionHandlers();
		for (AbstractAction a : selectionActions) {
			a.dispose();
		}
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenContentOutlinePage#getControl()
	 */
	@Override
	public Control getControl() {
		return pageBook;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenContentOutlinePage#configureOutlineViewer()
	 */
	@Override
	protected void configureOutlineViewer() {
		super.configureOutlineViewer();
		containmentLayoutAction = new Action() {
			public void run() {
				viewId = ID_CONTAINMENT;
				showPage();
			}
		};
		categoryLayoutAction = new Action() {
			public void run() {
				viewId = ID_CATEGORY;
				showPage();
			}
		};
		ImageDescriptor ed1 = S60WorkspacePlugin.getIconImageDescriptor(
				"containment_layout.gif", true);
		ImageDescriptor dd1 = S60WorkspacePlugin.getIconImageDescriptor(
				"containment_layout.gif", false);
		ImageDescriptor ed2 = S60WorkspacePlugin.getIconImageDescriptor(
				"category_layout.gif", true);
		ImageDescriptor dd2 = S60WorkspacePlugin.getIconImageDescriptor(
				"category_layout.gif", false);

		containmentLayoutAction
				.setToolTipText(Messages.ContainmentAction_tooltip);
		containmentLayoutAction.setImageDescriptor(ed1);
		containmentLayoutAction.setHoverImageDescriptor(ed1);
		containmentLayoutAction.setDisabledImageDescriptor(dd1);
		categoryLayoutAction.setToolTipText(Messages.CategoryAction_tooltip);
		categoryLayoutAction.setImageDescriptor(ed2);
		categoryLayoutAction.setHoverImageDescriptor(ed2);
		categoryLayoutAction.setDisabledImageDescriptor(dd2);
		IToolBarManager tbm = getSite().getActionBars().getToolBarManager();
		tbm.add(containmentLayoutAction);
		tbm.add(categoryLayoutAction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenContentOutlinePage#setActionBars(org.eclipse.ui.IActionBars)
	 */
	@Override
	public void setActionBars(IActionBars actionBars) {
		super.setActionBars(actionBars);
		ActionRegistry registry = (ActionRegistry) getEditorPart().getAdapter(
				ActionRegistry.class);
		if (registry != null) {
			actionBars.setGlobalActionHandler(OpenGraphicsEditorAction.ID,
					registry.getAction(OpenGraphicsEditorAction.ID));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenContentOutlinePage#initializeOutlineViewer()
	 */
	@Override
	public void initializeOutlineViewer() {
		super.initializeOutlineViewer();
		categoryInitialized = false;
		if (graphicalViewer != null) {
			graphicalViewer.removeSelectionChangedListener(this);
		}
		graphicalViewer = (GraphicalViewer) getEditorPart().getAdapter(
				GraphicalViewer.class);
		if (graphicalViewer != null) {
			graphicalViewer.addSelectionChangedListener(this);
		}
		showPage();
	}

	/**
	 * Initializes the category view.
	 */
	protected void initializeCategoryView() {
		if (!categoryInitialized) {
			categoryInitialized = true;
			stackChanged(null);
			getSite().setSelectionProvider(new ISelectionProvider() {
				public void addSelectionChangedListener(
						ISelectionChangedListener listener) {
				}

				public ISelection getSelection() {
					IStructuredSelection selection = (IStructuredSelection) categoryViewer
							.getSelection();
					List newSelection = new ArrayList();
					for (Iterator iter = selection.iterator(); iter.hasNext();) {
						TreeNode node = (TreeNode) iter.next();

						IScreenElement screenElem = null;
						if (node.getContent() instanceof IScreenElement) {
							screenElem = (IScreenElement) node.getContent();
						} else if (node.getContent() instanceof IContentData) {
							TreeNode last = node;
							while (last.getChildren().size() == 1) {
								last = last.getChildren().iterator().next();
							}
							if (last.getContent() instanceof IScreenElement) {
								screenElem = (IScreenElement) last.getContent();
							}
						}
						if (screenElem != null) {
							newSelection.add(screenElem.getWidget());
						}
					}
					return new StructuredSelection(newSelection);
				}

				public void removeSelectionChangedListener(
						ISelectionChangedListener listener) {
				}

				public void setSelection(ISelection selection) {
				}
			});
		}
	}

	/**
	 * Shows the page with the given id.
	 * 
	 * @param id id of the page.
	 */
	protected void showPage() {
		switch (viewId) {
		case ID_CONTAINMENT:
			containmentLayoutAction.setChecked(true);
			categoryLayoutAction.setChecked(false);
			pageBook.showPage(super.getControl());
			break;
		case ID_CATEGORY:
			getSite().setSelectionProvider(categoryViewer);
			containmentLayoutAction.setChecked(false);
			categoryLayoutAction.setChecked(true);
			initializeCategoryView();
			pageBook.showPage(categoryViewer.getControl());
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.view.BaseContentOutlinePage#unhookOutlineViewer()
	 */
	@Override
	protected void unhookOutlineViewer() {
		
		GraphicalViewer graphicalViewer = (GraphicalViewer) getEditorPart()
				.getAdapter(GraphicalViewer.class);
		MenuManager mgr = null;
		if (graphicalViewer != null) {
			mgr = graphicalViewer.getContextMenu();
		}
		super.unhookOutlineViewer();
		if (graphicalViewer != null) {
			graphicalViewer.setContextMenu(mgr);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.CommandStackEventListener#stackChanged(org.eclipse.gef.commands.CommandStackEvent)
	 */
	public void stackChanged(CommandStackEvent event) {
		if (event != null
				&& (event.getDetail() == CommandStack.POST_EXECUTE
						|| event.getDetail() == CommandStack.POST_REDO || event
						.getDetail() == CommandStack.POST_UNDO)) {
			Command command = event.getCommand();
			while (command instanceof CompoundCommand
					&& !((CompoundCommand) command).isEmpty()) {
				CompoundCommand cc = (CompoundCommand) command;
				if (cc.isEmpty()) {
					break;
				}
				if (cc.getCommands().size() > 1) {
					command = (Command) cc.getCommands().get(0);
				} else {
					command = cc.unwrap();
				}
			}
			if (command instanceof Series60EditorCommand
					|| command instanceof ISeries60Command) {
				if(categoryViewer != null)
					categoryViewer.refresh();
				return;
			}
		}
		if (categoryInitialized && categoryViewer != null) {
			List editParts = getViewer().getContents().getChildren();
			if (!editParts.isEmpty()) {
				EObject model = (EObject) ((EditPart) editParts.get(0))
						.getModel();
				IScreenElement screenElement = (IScreenElement) JEMUtil
						.getScreenElement(model);

				TreeNode oldTree = (TreeNode) categoryViewer.getInput();
				TreeNode newTree = buildTree(screenElement);

				if (oldTree != null) {
					updateTree(oldTree, newTree);
					categoryViewer.refresh();
				} else {
					categoryViewer.setInput(newTree);
					categoryViewer.expandToLevel(2);
				}
			}
		}

		// update actions
		if (selectionActions != null)
			for (AbstractAction aa : selectionActions) {
				if (aa.isListeningSelection())
					aa.selectionChanged(null);
			}
	}

	private void updateTree(TreeNode oldTree, TreeNode newTree) {
		List<TreeNode> toRemove = new ArrayList<TreeNode>();
		List<TreeNode> toAdd = new ArrayList<TreeNode>();
		for (TreeNode oldNode : oldTree.getChildren()) {
			boolean found = false;
			for (TreeNode newNode : newTree.getChildren()) {
				if (newNode.getContent() == oldNode.getContent()) {
					found = true;
					break;
				}
			}
			if (!found) {
				toRemove.add(oldNode);
			}
		}
		for (TreeNode newNode : newTree.getChildren()) {
			boolean found = false;
			for (TreeNode oldNode : oldTree.getChildren()) {
				if (oldNode.getContent() == newNode.getContent()) {
					found = true;
					break;
				}
			}
			if (!found) {
				toAdd.add(newNode);
			}
		}

		for (TreeNode node : toRemove) {
			oldTree.removeChild(node);
		}

		for (TreeNode node : toAdd) {
			oldTree.addChild(node);
		}

		for (TreeNode oldNode : oldTree.getChildren()) {
			for (TreeNode newNode : newTree.getChildren()) {
				if (newNode.getContent() == oldNode.getContent()) {
					updateTree(oldNode, newNode);
					break;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		if (getEditorPart() == null) {
			return;
		}
		if (!isViewerDispatching && event.getSource() == categoryViewer) {
			isCategoryDispatching = true;
			try {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				List selectedEditParts = selection.toList();
				List<Object> treeParts = new ArrayList<Object>(
						selectedEditParts.size());
				List<Object> graphicalParts = new ArrayList<Object>(
						selectedEditParts.size());
				for (Object obj : selectedEditParts) {
					if (obj instanceof TreeEditPart) {
						treeParts.add(obj);
					} else if (obj instanceof GraphicalEditPart) {
						graphicalParts.add(obj);
					}
				}
				if (!treeParts.isEmpty()) {
					getViewer()
							.setSelection(new StructuredSelection(treeParts));
				}
				if (!graphicalParts.isEmpty()) {
					GraphicalViewer viewer = (GraphicalViewer) getEditorPart()
							.getAdapter(GraphicalViewer.class);
					if (viewer != null) {
						viewer.setSelection(new StructuredSelection(
								graphicalParts));
					}
				}

				/* synchronize selection in editor with this */
				List newSelection = new ArrayList();
				for (Iterator iter = selection.iterator(); iter.hasNext();) {
					TreeNode node = (TreeNode) iter.next();

					IScreenElement screenElem = null;
					if (node.getContent() instanceof IScreenElement) {
						screenElem = (IScreenElement) node.getContent();
					} else if (node.getContent() instanceof IContentData) {
						TreeNode last = node;
						while (last.getChildren().size() == 1) {
							last = last.getChildren().iterator().next();
						}
						if (last.getContent() instanceof IScreenElement) {
							screenElem = (IScreenElement) last.getContent();
						}
					}
					if (screenElem != null && getViewer() != null) {
						EditPart part = (EditPart) getViewer()
								.getEditPartRegistry().get(
										screenElem.getWidget());
						if (part != null) {
							newSelection.add(part);
						}
					}
				}

				if (getViewer() != null) {
					if (!newSelection.isEmpty()) {
						getViewer().setSelection(
								new StructuredSelection(newSelection));
					} else {
						getViewer().deselectAll();
					}
				}
			} finally {
				isCategoryDispatching = false;
			}
		} else if (!isCategoryDispatching
				&& event.getSource() == getEditorPart().getAdapter(
						GraphicalViewer.class)) {
			isViewerDispatching = true;
			try {
				List selectedParts = ((IStructuredSelection) event
						.getSelection()).toList();
				List newSelection = new ArrayList();
				for (Object part : selectedParts) {
					EObject model = (EObject) ((EditPart) part).getModel();
					IScreenElement data = (IScreenElement) JEMUtil
							.getScreenElement(model);
					if (data != null) {
						// data can be null when the freeform is selected
						if (data.getWidget() != model) {
							IScreenElement[] elems = (IScreenElement[]) data
									.getData().getAdapter(
											IScreenElement[].class);
							if (elems != null) {
								for (int i = 0; i < elems.length; i++) {
									IScreenElement elem = elems[i];
									if (elem.getWidget() == model) {
										data = elem;
										break;
									}
								}
							}
						}
						TreeNode root = (TreeNode) categoryViewer.getInput();
						if (root != null) {
							TreeNode node = findNode(root, data);
							if (node != null) {
								newSelection.add(node);
								categoryViewer.expandToLevel(node, 0);
							}
						}
					}
				}

				categoryViewer.setSelection(new StructuredSelection(
						newSelection));
			} finally {
				isViewerDispatching = false;
			}
		}
	}

	/**
	 * Builds the category tree.
	 * 
	 * @param data the root screen data.
	 * @return the root tree node.
	 */
	protected TreeNode buildTree(IScreenElement data) {
		List<Object> path = getPath(data.getData());
		TreeNode root = new TreeNode(path.remove(0));
		buildTree(root, data);
		return root;
	}

	/**
	 * Builds the tree at the level that is relevant to the specific data.
	 * 
	 * @param root the root tree node.
	 * @param data the data that the tree node is built against.
	 */
	protected void buildTree(TreeNode root, IScreenElement data) {
		ICategoryAdapter adapter = (ICategoryAdapter) data.getData()
				.getAdapter(ICategoryAdapter.class);
		IContentData[] peers = null;
		if (adapter != null) {
			peers = adapter.getCategorizedPeers();
		}
		if (peers != null) {
			for (IContentData peer : peers) {
				List<Object> path = getPath(peer);
				path.remove(0);
				// appends the data to the last
				path.add(data);

				TreeNode parent = root;
				for (Object content : path) {
					TreeNode child = parent.getChild(content);
					if (child == null) {
						child = new TreeNode(content);
						parent.addChild(child);
					}
					parent = child;
				}
			}
		}

		// recursively builds the child tree nodes
		for (IScreenElement child : data.getChildren()) {
			buildTree(root, child);
		}
	}

	/**
	 * Returns the path to the specific tree node data.
	 * 
	 * @param data the tree node data.
	 * @return the path to the specific tree node data.
	 */
	protected List<Object> getPath(IContentData data) {
		List<Object> path = new LinkedList<Object>();

		IContentData d = data;
		while (d != null) {
			path.add(0, d);
			d = d.getParent();
		}
		return path;
	}

	/**
	 * Recursively traverses the tree to find a node that has corresponding data
	 * object.
	 * 
	 * @param node the node from where to start.
	 * @param data the data that the node should contain.
	 * @return the tree node having the given data or null if not found.
	 */
	TreeNode findNode(TreeNode node, Object data) {
		if (node.data.equals(data)) {
			return node;
		}
		for (TreeNode child : node.children) {
			TreeNode t = findNode(child, data);
			if (t != null) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Data structure for the category tree.
	 */
	public static class TreeNode {

		private Object data;

		private TreeNode parent;

		private Set<TreeNode> children;

		/**
		 * Constructs a tree node.
		 * 
		 * @param data the node data.
		 */
		TreeNode(Object data) {
			this.data = data;
			children = new HashSet<TreeNode>(32);
		}

		/**
		 * @return the parent node.
		 */
		public TreeNode getParent() {
			return parent;
		}

		/**
		 * @return the content.
		 */
		public Object getContent() {
			return data;
		}

		/**
		 * Adds a child node.
		 * 
		 * @param child the node to be added.
		 */
		void addChild(TreeNode child) {
			child.parent = this;
			children.add(child);
		}

		/**
		 * Removes the child node.
		 * 
		 * @param child the node to be removed.
		 */
		void removeChild(TreeNode child) {
			child.parent = null;
			children.remove(child);
		}

		/**
		 * Returns the child node that has the given data.
		 * 
		 * @param data the data to test.
		 * @return the child node containing the data.
		 */
		TreeNode getChild(Object data) {
			for (TreeNode child : children) {
				if (child.data.equals(data)) {
					return child;
				}
			}
			return null;
		}

		/**
		 * @return all child nodes.
		 */
		public Set<TreeNode> getChildren() {
			return children;
		}

		/**
		 * Tests if the given node is a child of this node.
		 * 
		 * @param child the node to test.
		 * @return true if the node is a child, false otherwise.
		 */
		boolean hasChild(TreeNode child) {
			return children.contains(child);
		}

		/**
		 * @return the text description of the node.
		 */
		String getText() {
			if (data instanceof IContentData) {
				return ((IContentData) data).getName();
			}
			return ((IScreenElement) data).getData().getName();
		}

		/**
		 * @return the image of the node.
		 */
		Image getImage() {
			if (data instanceof IContentData) {
				return ((IContentData) data).getIcon();
			}
			return ((IScreenElement) data).getData().getIcon();
		}
	}

	/**
	 * This class provides the content to the tree viewer.
	 */
	class CategoryContentProvider implements ITreeContentProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object parentElement) {
			return ((TreeNode) parentElement).getChildren().toArray();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			if (!(element instanceof TreeNode)) {
				return null;
			}
			return ((TreeNode) element).getParent();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object element) {
			return !((TreeNode) element).getChildren().isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * This class provides label to the tree viewer.
	 */
	class CategoryLabelProvider extends LabelProvider {
		List<Image> images = new ArrayList<Image>();

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
		 */
		@Override
		public Image getImage(Object element) {
			Image image = ((TreeNode) element).getImage();
			if (image != null) {
				images.add(image);
			}
			return image;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element) {
			return ((TreeNode) element).getText();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#dispose()
		 */
		@Override
		public void dispose() {
			super.dispose();
			for (Image image : images) {
				image.dispose();
			}
		}
	}

	/**
	 * This class is a customized tree viewer that converts the tree selection
	 * to the edit part.
	 */
	class CategoryTreeViewer extends TreeViewer {
		/**
		 * Constructs a tree viewer.
		 * 
		 * @param parent the parent widget.
		 */
		public CategoryTreeViewer(Composite parent) {
			super(parent);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.AbstractTreeViewer#getSelectionFromWidget()
		 */
		@Override
		protected List getSelectionFromWidget() {
			List selectedElements = super.getSelectionFromWidget();
			List<Object> selection = new ArrayList<Object>(selectedElements
					.size());
			for (Object element : selectedElements) {
				Object content = ((TreeNode) element).getContent();
				if (content instanceof IScreenElement) {
					// graphical part
					GraphicalViewer viewer = (GraphicalViewer) getEditorPart()
							.getAdapter(GraphicalViewer.class);
					if (viewer != null) {
						EditPart graphicalPart = (EditPart) viewer
								.getEditPartRegistry().get(
										((IScreenElement) content).getWidget());
						if (graphicalPart != null) {
							selection.add(graphicalPart);
						}
					}
				
				} else {
					selection.add(content);
				}
			}
			return selection;
		}
	}

	/**
	 * This class sorts the nodes in alphabetical order.
	 */
	class CategoryViewerSorter extends ViewerSorter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			TreeNode a = (TreeNode) e1;
			TreeNode b = (TreeNode) e2;

			return a.getText().compareToIgnoreCase(b.getText());
		}
	}

	/**
	 * fills context menu for category view
	 */
	protected void fillContextMenu(IMenuManager manager) {

		ISelectionProvider provider = categoryViewer;

		UndoAction undoAction = new UndoAction(getEditorPart());
		undoAction.update();
		manager.add(undoAction);

		RedoAction redoAction = new RedoAction(getEditorPart());
		redoAction.update();
		manager.add(redoAction);

		manager.add(new Separator());
		manager.add(new CopyImageAction(provider, null));
		manager.add(new PasteImageAction(provider, getCommandStack(), null));

		ActionRegistry registry = (ActionRegistry) getEditorPart().getAdapter(
				ActionRegistry.class);
		if (registry != null) {
			DeleteAction da = (DeleteAction) registry
					.getAction(DeleteAction.ID);
			if (da.isEnabled()) {
				manager.add(new Separator());
				manager.add(da);
			}
		}

		Separator spr = new Separator();
		spr = internalAddAction(manager, new AddTaskViewAction(provider), spr);
		spr = internalAddAction(manager, new AddBookmarkViewAction(provider),
				spr);

		/* edit actions */

		spr = new Separator();

		Action action = new BrowseForFileAction(provider, getCommandStack());
		spr = internalAddAction(manager, action, spr);

		spr = new Separator();

		action = new EditImageInBitmapEditorAction(
				(ISelectionProvider) provider, getCommandStack());
		spr = internalAddAction(manager, action, spr);

		action = new ConvertAndEditSVGInBitmapEditorAction(
				(ISelectionProvider) provider, getCommandStack());
		((ConvertAndEditSVGInBitmapEditorAction) action)
				.setSizeFromEditpart(false);
		spr = internalAddAction(manager, action, spr);

		action = new EditImageInSVGEditorAction((ISelectionProvider) provider,
				getCommandStack());
		spr = internalAddAction(manager, action, spr);

		action = new EditSoundInSoundEditorAction(provider, getCommandStack());
		spr = internalAddAction(manager, action, spr);
		
		action = new EditInSystemEditorAction(
				(ISelectionProvider) provider, getCommandStack());
		spr = internalAddAction(manager, action, spr);		

		/* mask actions */
		spr = new Separator();

		action = new EditMaskAction((ISelectionProvider) provider,
				getCommandStack());
		spr = internalAddAction(manager, action, spr);

		action = new EditMaskAction2((ISelectionProvider) provider,
				getCommandStack());

		spr = internalAddAction(manager, action, spr);

		/* set color action */
		action = new SetColorAction((ISelectionProvider) provider,
				getCommandStack());
		internalAddAction(manager, action, new Separator());

		/* clear action */
		action = new ClearImageEditorAction(provider, getCommandStack());
		internalAddAction(manager, action, new Separator());

		/* 9-piece related actions */
		spr = new Separator();
		action = new NinePieceConvertAction(provider, getCommandStack(),
				NinePieceConvertAction.TYPE_CONVERT2SINGLE);
		spr = internalAddAction(manager, action, spr);

		action = new NinePieceConvertAction(provider, getCommandStack(),
				NinePieceConvertAction.TYPE_CONVERT2NINE);
		spr = internalAddAction(manager, action, spr);

		/* 11 piece related actions*/
		
		spr = new Separator();
		action = new ElevenPieceConvertAction(provider, getCommandStack(),
				ElevenPieceConvertAction.TYPE_CONVERT2SINGLE);
		spr = internalAddAction(manager, action, spr);

		action = new ElevenPieceConvertAction(provider, getCommandStack(),
				ElevenPieceConvertAction.TYPE_ELEVEN_PIECE);
		spr = internalAddAction(manager, action, spr);
		
		
		// stretch mode for bitmaps:
		SetStretchModeDDown ddown = new SetStretchModeDDown(null, provider,
				getCommandStack());
		internalAddAction(manager, ddown, new Separator());

		/* edit/animate actions */
		addEditAnimateActionToContextMenu(manager, provider);

		for (IEmbeddedEditorDescriptor descriptor : ExtensionManager
				.getEmbeddedEditorDescriptors()) {
			IEmbeddedEditorContributor contributor = descriptor
					.getContributor();
			if (contributor != null) {
				try {
					contributor.contributeContextMenu(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage()
							.getActivePart(), manager);
				} catch (Exception e) {
					S60WorkspacePlugin.error(e);
				}
			}
		}

		/* contributed actions to content outline page */
		manager.add(new Separator());
		ContributedActionsResolver.getInstance().contributeActions(manager,
				"outline", (ActionRegistry) getEditorPart().getAdapter(
						ActionRegistry.class));

	}

	private void addEditAnimateActionToContextMenu(IMenuManager manager,
			ISelectionProvider provider) {
		OpenGraphicsEditorAction openGrEditorAction = new OpenGraphicsEditorAction(
				getEditorPart(), provider);
		internalAddAction(manager, openGrEditorAction, new Separator());
	}

	private CommandStack getCommandStack() {
		return (CommandStack) getAdapter(CommandStack.class);
	}

	/**
	 * adds action to manager if enabled and add separator if not null.
	 * @param manager
	 * @param action
	 * @param spr
	 * @return NULL or separator, if was not used
	 */
	private Separator internalAddAction(IMenuManager manager, Action action,
			Separator spr) {
		if (action.isEnabled()) {
			if (spr != null) {
				manager.add(spr);
				spr = null;
			}
			manager.add(action);
		}
		return spr;
	}

	public void doubleClick(DoubleClickEvent event) {
		if (event.getSource() == categoryViewer) {
			String ids[] = { EditImageInBitmapEditorAction.ID,
					EditImageInSVGEditorAction.ID,
					EditSoundInSoundEditorAction.ID,
					OpenGraphicsEditorAction.ID };
			for (String a : ids) {
				IAction action = getSite().getActionBars()
						.getGlobalActionHandler(a);
				if (action != null && action.isEnabled()) {
					action.run();
					return;
				}
			}
			
			SetColorAction sca = new SetColorAction(categoryViewer,
					getCommandStack());
			if (sca.isEnabled())
				sca.run();
		}
	}

}