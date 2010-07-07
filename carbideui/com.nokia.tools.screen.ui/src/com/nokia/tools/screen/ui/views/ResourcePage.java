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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ButtonModel;
import org.eclipse.draw2d.ChangeEvent;
import org.eclipse.draw2d.ChangeListener;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.internal.ui.palette.editparts.DrawerEditPart;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySource;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.content.core.IContentDelta;
import com.nokia.tools.content.core.IContentListener;
import com.nokia.tools.content.core.IContentService;
import com.nokia.tools.screen.core.ICategoryAdapter;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.UiPlugin;

public class ResourcePage extends Page implements IContentListener, IAdaptable {

	private static final int POSITION_WIDTH = 50;

	private ResourceViewer viewer;

	private Composite page;

	private ResourcePositionViewer positionViewer;

	/**
	 * internal flag for avoiding loop running condition and trigger refresh
	 * icon view two times from positionViewer selection changed and from select
	 * in editor -> showSelection -> Select in Resources ->1)selection change
	 * and showSelection also triggers
	 */
	private boolean _shouldRefreshIconView = true;

	/** 'parent' editor of this page */
	private IWorkbenchPart editorPart;

	private boolean suppressViewerEvents;

	private boolean suppressOpenEditorEvents;

	private IContentAdapter contentAdapter;

	/**
	 * Constructs a resource page for the specific contents.
	 * 
	 * @param adapter the adapter to content
	 * 
	 * @param editorPart
	 */
	public ResourcePage(IContentAdapter adapter, IWorkbenchPart editorPart) {
		contentAdapter = adapter;
		this.editorPart = editorPart;
	}

	private ISelectionListener synchronizeListener;

	// synchronize with selection in Editor flag
	private boolean synchronize;

	@Override
	public void init(IPageSite pageSite) {
		super.init(pageSite);

		boolean syncState = false;

		// selection listener for synchronizing resource view with selection in
		// editor
		synchronizeListener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part,
					ISelection selection) {
				if (suppressViewerEvents) {
					return;
				}
				if (part == editorPart
						|| (part instanceof ContentOutlineViewPart)) {
					showSelection((IStructuredSelection) selection);
				}
			}
		};

		// ---------------------------------
		// contribute toolbar toggle buttons
		// ---------------------------------

		IToolBarManager bars = pageSite.getActionBars().getToolBarManager();
		Action toggleSync = new WorkbenchPartAction(null,
				WorkbenchPartAction.AS_CHECK_BOX) {
			public void run() {
				if (synchronize) {
					synchronize = false;
					getSite().getPage().getWorkbenchWindow()
							.getSelectionService().removeSelectionListener(
									synchronizeListener);
				} else {
					synchronize = true;
					getSite().getPage().getWorkbenchWindow()
							.getSelectionService().addSelectionListener(
									synchronizeListener);
					IEditorSite site = getSite().getPage().getActiveEditor()
							.getEditorSite();
					if (site == null || site.getSelectionProvider() == null) {
						// can happen when the editor failed to initialize
						return;
					}
					ISelection selection = site.getSelectionProvider()
							.getSelection();
					IStructuredSelection sel = (IStructuredSelection) selection;
					if (sel != null) {
						Object o = sel.getFirstElement();
						if (o instanceof IContentData || o instanceof EditPart)
							showSelection(sel);
					}
				}
				IPreferenceStore store = UiPlugin.getDefault()
						.getPreferenceStore();
				store
						.setValue(IIDEConstants.PREF_SYNC_WITH_EDITOR,
								synchronize);
			}

			@Override
			protected boolean calculateEnabled() {
				return true;
			}
		};

		ImageDescriptor i1 = UiPlugin.getIconImageDescriptor(
				"resview_toggle_synch.gif", true);
		toggleSync.setToolTipText(ViewMessages.ResView_toggleSync_tooltip);
		toggleSync.setImageDescriptor(i1);
		bars.add(toggleSync);

		// Restore last syncronization state
		IPreferenceStore store = UiPlugin.getDefault().getPreferenceStore();
		syncState = store.getBoolean(IIDEConstants.PREF_SYNC_WITH_EDITOR);
		synchronize = !syncState;
		toggleSync.setChecked(syncState);
		toggleSync.run();
	}

	@Override
	public void dispose() {
		getSite().getPage().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(synchronizeListener);
		super.dispose();
	}

	public void refresh() {
		if (!suppressOpenEditorEvents) {
			viewer.reset(contentAdapter);
		}
	}

	public CommandStack getCommandStack() {
		return (CommandStack) editorPart.getAdapter(CommandStack.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(2, false));

		viewer = new ResourceViewer(contentAdapter, this);
		Control control = viewer.createControl(page);

		getSite().setSelectionProvider(viewer);

		GridData data = new GridData(GridData.FILL_BOTH);
		control.setLayoutData(data);

		positionViewer = new ResourcePositionViewer(this);
		List<IMenuListener> listeners = new ArrayList<IMenuListener>();
		for (IResourceSection section : viewer.getResourceSections()) {
			IMenuListener listener = section.createResourceViewerMenuListener(
					positionViewer, getCommandStack());
			if (listener != null) {
				listeners.add(listener);
			}
		}
		positionViewer.addMenuListeners(listeners);

		positionViewer.setWidth(POSITION_WIDTH);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = POSITION_WIDTH + 8;
		positionViewer.createControl(page).setLayoutData(gd);
		updatePositionViewer(null);

		viewer.addChangeListener(new ChangeListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.draw2d.ChangeListener#handleStateChanged(org.eclipse.draw2d.ChangeEvent)
			 */
			public void handleStateChanged(ChangeEvent event) {
				if (event.getPropertyName().equals(
						ButtonModel.SELECTED_PROPERTY)
						&& event.getSource() instanceof DrawerEditPart
						&& ((DrawerEditPart) event.getSource()).isExpanded()) {
					DrawerEditPart part = (DrawerEditPart) event.getSource();
					List children = part.getChildren();
					if (!children.isEmpty()) {
						EditPart category = (EditPart) children.get(0);
						Object model = category.getModel();
						if (model instanceof CombinedTemplateCreationEntry) {
							if (!(((CombinedTemplateCreationEntry) model)
									.getTemplate() instanceof IContent)) {
								viewer.select(category);
								viewer.reveal(category);
								viewer
										.setActiveTool((CombinedTemplateCreationEntry) model);
							}
						}
					}
				}
			}

		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				ISelection selection = e.getSelection();
				if (selection instanceof IStructuredSelection) {
					EditPart editpart = (EditPart) ((IStructuredSelection) selection)
							.getFirstElement();
					Object model = editpart.getModel();
					if (model instanceof CombinedTemplateCreationEntry) {
						final IContentData data = (IContentData) ((CombinedTemplateCreationEntry) model)
								.getTemplate();

						final IResourceViewerSelectionHelper helper = viewer
								.getResourceSelectionHelper(data);
						if (helper != null) {
							if (helper.supportsPositionViewer()) {
								updatePositionViewer(data);
							} else {
								if (!suppressViewerEvents) {
									updatePositionViewer(null);
									Display.getDefault().asyncExec(
											new Runnable() {
												public void run() {
													suppressOpenEditorEvents = true;
													helper.executeSelect(data);
													suppressOpenEditorEvents = false;
												}
											});

								}
							}
						}
					}
				}
			}
		});
		positionViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent e) {
						ISelection selection = e.getSelection();
						if (selection instanceof IStructuredSelection) {
							EditPart editpart = (EditPart) ((IStructuredSelection) selection)
									.getFirstElement();
							Object model = editpart.getModel();
							if (model instanceof CombinedTemplateCreationEntry) {
								IContentData data = (IContentData) ((CombinedTemplateCreationEntry) model)
										.getTemplate();
								suppressViewerEvents = true;
								try {
									selectPosition(data);
								} finally {
									suppressViewerEvents = false;
								}
							}
						}
					}
				});
		getSite().setSelectionProvider(viewer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.Page#getControl()
	 */
	@Override
	public Control getControl() {
		return page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.Page#setFocus()
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * @param data
	 */
	private void updatePositionViewer(IContentData data) {
		if (page == null || page.isDisposed() || positionViewer == null
				|| positionViewer.getControl() == null) {
			return;
		}

		positionViewer.clearPositionData(); // for scroll and selection
		// persistence in position viewer

		IResourceViewerSelectionHelper helper = viewer
				.getResourceSelectionHelper(data);
		if (helper != null && helper.supportsPositionViewer()) {
			IResourceSection section = viewer.getResourceSection(data);
			if (section instanceof IResourceViewRectProvider) {
				positionViewer.rectProvider = (IResourceViewRectProvider) section;
			}
			positionViewer.setData(data.getRoot(), data);
		} else {
			IResourceSection section = viewer.getResourceSection(data);
			if (section instanceof IResourceViewRectProvider) {
				positionViewer.rectProvider = (IResourceViewRectProvider) section;
			}
			if (positionViewer.getData() != null) {
				positionViewer
						.setData(positionViewer.getData().getRoot(), data);
			}
		}
		if (data instanceof IContent || data == null) {
			((GridData) positionViewer.getControl().getLayoutData()).widthHint = 0;
			page.layout();
			clearIconView();
		} else {
			((GridData) positionViewer.getControl().getLayoutData()).widthHint = POSITION_WIDTH + 8;
			page.layout();
		}

	}

	private void clearIconView() {
		try {// for IconView
			ViewPart view = getIconView(false);
			if (view != null && view instanceof ISelectionListener) {
				((ISelectionListener) view).selectionChanged(null, null);
			}
		} catch (PartInitException e) {
			UiPlugin.error(e);
		}
	}

	protected IProgressMonitor monitor;

	/**
	 * @param picture
	 */
	private void selectPosition(final IContentData newData) {
		// replaced
		// if (themeContent != null && _shouldRefreshIconView) {

		// with ... to remove dependency on themeContent
		if (_shouldRefreshIconView && viewer.hasContent()) {
			try {
				refreshIconView(new StructuredSelection(
						newData.getChildren()[0]), true);
			} catch (Throwable ee) {
				UiPlugin.error(ee);
			}
		}
	}

	protected ViewPart getIconView(boolean restore) throws PartInitException {
		IWorkbenchPage page = getSite().getWorkbenchWindow().getActivePage();
		if (page == null) {
			return null;
		}
		ViewPart iconView = (ViewPart) page.findView(ViewIDs.ICON_VIEW_ID);
		/*
		 * This causes a strenge behaviour, why is it here? We should let the
		 * user to decide, if he/she wants to see some view or not.
		 *
		 */
		if (false /* restore */) {
			if (iconView == null) {
				iconView = (ViewPart) page.showView(ViewIDs.ICON_VIEW_ID, null,
						IWorkbenchPage.VIEW_CREATE);
			}
			page.showView(ViewIDs.ICON_VIEW_ID, null,
					IWorkbenchPage.VIEW_VISIBLE);
		}
		return iconView;

	}

	/**
	 * @param selection
	 * @param activate
	 */
	public void refreshIconView(final IStructuredSelection selection,
			boolean activate) {
		IWorkbenchPage page = getSite().getWorkbenchWindow().getActivePage();
		if (page != null) {
			try {
				// first - activate icon view
				ViewPart icv = getIconView(true);
				if (icv instanceof ISelectionListener) {
					if (activate) {
						// activates the view first otherwise twice selection
						// happens
						page.showView(ViewIDs.ICON_VIEW_ID, null,
								IWorkbenchPage.VIEW_ACTIVATE);
					}
					ISelectionListener listener = (ISelectionListener) icv;
					listener.selectionChanged(null, selection);
				}

			} catch (PartInitException e) {
				UiPlugin.error(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentListener#rootContentChanged(com.nokia.tools.content.core.IContent)
	 */
	public void rootContentChanged(IContent content) {
		IContentAdapter adapter = null;
		if (viewer != null && editorPart != null) {
			adapter = (IContentAdapter) editorPart
					.getAdapter(IContentAdapter.class);
		}

		// replaced with
		if (adapter == null || adapter.getContents().length < 1) {
			return;
		}
		if (content == null || content.getType().equals("XMLUI")) {
			refresh();
			updatePositionViewer(null);
		}
	}

	// private IContentData findModelItem(Object data, IContentData root) {
	private IContentData findModelItem(Object data) {
		List<IContentData> modelItems = new ArrayList<IContentData>();
		IContent[] contents = contentAdapter.getContents();
		for (int i = 0; i < contents.length; i++) {
			IContent root = contents[i];
			IContentData item = JEMUtil.getContentData(data);
			if (item != null) {
				root = item.getRoot();
			}
			String targetId = item != null ? item.getId()
					: data instanceof String ? (String) data : null;

			// targetId can be null when the text element is selected
			IContentData modelItem = null;
			if (targetId != null && root != null) {
				modelItem = root.findById(targetId);
			}
			if (modelItem == null && item != null) {
				ICategoryAdapter cat = (ICategoryAdapter) item
						.getAdapter(ICategoryAdapter.class);
				if (cat != null && cat.getCategorizedPeers() != null) {
					for (IContentData peer : cat.getCategorizedPeers()) {
						modelItem = root.findById(peer.getId());
						if (modelItem != null)
							break;
					}
				}
			}
			if (modelItem == null)
				modelItem = item;
			if (modelItem != null) {
				modelItems.add(modelItem);
			}
		}
		if (modelItems.size() > 1) {
			// System.out.println("more model items found");
			return modelItems.get(0);
		} else if (modelItems.size() == 1) {
			return modelItems.get(0);
		} else {
			return null;

		}
	}

	/**
	 * shows and selects element (group), that contains item in selection
	 * 
	 */
	public void showSelection(IStructuredSelection sel) {

		
		if (viewer == null)
			return;

		Object data = sel.isEmpty() ? null : sel.getFirstElement();

		// IContentData modelItem = findModelItem(data, themeContent);
		IContentData modelItem = findModelItem(data);
		IResourceViewerSelectionHelper helper = viewer
				.getResourceSelectionHelper(modelItem);

		if (modelItem != null) {
			// find Task and ComponentGroup, that contains selection
			IContentData task = helper.findTaskContaining(modelItem);

			if (task == null)
				return;

			final IContentData compGroup = helper.findGroupContaining(
					modelItem, task);
			final IContentData minorGroup = helper.findMinorGroup(modelItem,
					task, compGroup);

			// selects appropriate elements
			if (compGroup != null && minorGroup != null) {
				suppressViewerEvents = true;
				try {
					final Map map = viewer.getVisualPartMap();

					Iterator it = map.values().iterator();
					while (it.hasNext()) {
						EditPart element = (EditPart) it.next();
						if (element.getModel() instanceof CombinedTemplateCreationEntry) {
							CombinedTemplateCreationEntry entry = (CombinedTemplateCreationEntry) element
									.getModel();
							if (entry.getTemplate() == compGroup
									|| entry.getTemplate() == minorGroup) {

								// set viewer selection is the same no
								// action
								if (viewer.getSelectedEditParts().size() == 0
										|| viewer.getSelectedEditParts().get(0) != element) {
									viewer.reveal(element);
									viewer.select(element);
									viewer.setActiveTool(entry);
								}

								if (viewer
										// enter only if the section supports
										// position viewer
										.getResourceSelectionHelper(compGroup)
										.supportsPositionViewer()) {
									// update also selection in position viewer
									if (minorGroup != null) {
										it = positionViewer.getVisualPartMap()
												.values().iterator();
										while (it.hasNext()) {
											element = (EditPart) it.next();
											if (element.getModel() instanceof CombinedTemplateCreationEntry) {
												entry = (CombinedTemplateCreationEntry) element
														.getModel();
												if (entry.getTemplate() == minorGroup) {
													if (positionViewer
															.getSelectedEditParts()
															.size() == 0
															|| positionViewer
																	.getSelectedEditParts()
																	.get(0) != element) {
														IResourceSection section = viewer
																.getResourceSection(modelItem);
														if (section instanceof IResourceViewRectProvider) {
															positionViewer.rectProvider = (IResourceViewRectProvider) section;
														}
														positionViewer
																.reveal(element);
														_shouldRefreshIconView = false;
														positionViewer
																.select(element);
													}

													List<Object> iconViewSel = new ArrayList<Object>();
													for (Iterator iter = sel
															.iterator(); iter
															.hasNext();) {
														IContentData mItem = findModelItem(iter
																.next());
														if (mItem != null) {

															iconViewSel
																	.add(mItem);
														}
													}

													/*
													 * add parameter
													 * 'Boolean.FALSE' to
													 * selection so that icon
													 * view will be notified tot
													 * to try synchronize with
													 * editor and stuck in
													 * event-loop
													 */
													_shouldRefreshIconView = true;
													iconViewSel
															.add(Boolean.FALSE);
													refreshIconView(
															new StructuredSelection(
																	iconViewSel),
															false);
													return;
												}
											}
										}
									}
								} else { // the section does not support
									// position viewer, clear it
									updatePositionViewer(null);
									refreshIconView(new StructuredSelection(
											new Object[] { Boolean.FALSE }),
											false);
								}
								return;
							}
						}
					}
				} finally {
					suppressViewerEvents = false;
				}
			}
		} else {

			refreshIconView(new StructuredSelection(
					new Object[] { Boolean.FALSE }), false);
			return;
		}
	}

	/**
	 * returns true, when view is synchronizing with editor
	 * 
	 * @return
	 */
	public boolean isSynchronizing() {
		return synchronize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentListener#contentModified(com.nokia.tools.content.core.IContentDelta)
	 */
	/**
	 * Updates position viewer. Used for refreshing status(whether it is skinned
	 * or not) indication.
	 */
	public void contentModified(IContentDelta delta) {
		IContentData data = positionViewer.getData();
		if (data != null) {
			IResourceSection section = viewer.getResourceSection(data);
			if (section instanceof IResourceViewRectProvider) {
				positionViewer.rectProvider = (IResourceViewRectProvider) section;
			}
			positionViewer.setData(data.getRoot(), positionViewer.getData());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		// don't delegate property sheet page to the editor, the property sheet
		// will then have two reference counts due to this part and editor part,
		// therefore after closing editor, the property page is not disposed,
		// and then memory leak.
		// if (adapter == IPropertySheetPage.class)
		// return this.editorPart.getAdapter(adapter);

		// delegate to editor
		if (adapter == IPropertySource.class) {
			return editorPart.getAdapter(adapter);
		}
		if (adapter == IContentAdapter.class
				|| adapter == IContentService.class)
			return this.editorPart.getAdapter(adapter);
		return null;
	}

	public ResourceViewer getViewer() {
		return viewer;
	}
}