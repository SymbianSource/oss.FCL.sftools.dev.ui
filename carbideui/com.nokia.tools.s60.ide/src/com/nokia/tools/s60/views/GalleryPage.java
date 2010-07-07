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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.WorkbenchPart;

import com.nokia.tools.s60.editor.ScreenModelMediator;
import com.nokia.tools.s60.ide.ContributedActionsResolver;
import com.nokia.tools.s60.ide.IS60IDEConstants;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.ide.actions.GalleryDropDownAction;
import com.nokia.tools.s60.ide.actions.GalleryLayoutAction;
import com.nokia.tools.s60.ide.actions.GalleryModeAction;
import com.nokia.tools.screen.ui.gallery.IGalleryAdapter;
import com.nokia.tools.screen.ui.gallery.IGalleryScreenProvider;
import com.nokia.tools.screen.ui.gallery.IGalleryAdapter.IGalleryListener;
import com.nokia.tools.screen.ui.gallery.IGalleryScreenProvider.IGalleryScreen;
import com.nokia.tools.screen.ui.gef.Thumbnail;
import com.nokia.tools.screen.ui.utils.EclipseUtils;

/**
 * This page displays the preview gallery and allows user to generate preview
 * from the predefined preview screens. When user clicks on the preview screen,
 * the selection is triggered and can be handled by the registered
 * {@link ISelectionChangedListener}.
 */
public class GalleryPage extends Page implements ISelectionProvider,
		IGalleryListener {
	public static final int MODE_USER = 0;

	public static final int MODE_ALL = 1;

	private static final int MAX_RUNNING_JOBS = 2;

	private static final int TAB_HEIGHT = 20;

	private IGalleryAdapter adapter;

	private IGalleryScreenProvider screenProvider;

	private ScrolledComposite scroll;

	private Composite composite;

	private Composite overview;

	private Set<ISelectionChangedListener> selectionListeners;

	private ISelection selection;

	private GalleryDropDownAction dropDownAction;

	// private GalleryScreenContributionItem activeScreens;
	private JobController controller;

	private int mode = MODE_USER;

	private boolean internalLayout;
	
	private boolean contributed;
	
	private boolean refreshSelectedScreen=false;
	
	/**
	 * Constructs the page with the specific gallery adapter.
	 * 
	 * @param adapter
	 *            the gallery adapter that is used for notifying the gallery
	 *            changed event.
	 */
	public GalleryPage(IGalleryAdapter adapter) {
		IPreferenceStore store = S60WorkspacePlugin.getDefault()
				.getPreferenceStore();
		
		mode = store.getInt(IS60IDEConstants.PREF_GALLERY_MODE);
		this.adapter = adapter;
		adapter.addGalleryListener(this);
		selectionListeners = new HashSet<ISelectionChangedListener>(8);
		controller = new JobController();
		controller.start();
		
		
	}
	private void contributeToActionBars() {
		IActionBars bars = getSite().getActionBars();
		

		
		

		/* set undo/redo handlers from parent editor */
		
		ActionRegistry registry = (ActionRegistry) (getSite().getWorkbenchWindow().getActivePage().getActiveEditor())
				.getAdapter(ActionRegistry.class);
		bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), registry
				.getAction(ActionFactory.UNDO.getId()));
		bars.setGlobalActionHandler(ActionFactory.REDO.getId(), registry
				.getAction(ActionFactory.REDO.getId()));

		

		

		

		bars.updateActionBars();
		contributed=true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		
		composite = new Composite(parent, SWT.NONE);
		
		composite.setLayout(new FillLayout());
		
		scroll = new ScrolledComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = true;
		data.verticalAlignment = SWT.FILL;
		scroll.setLayoutData(data);
		scroll.setLayout(new FillLayout());
		scroll.addListener(SWT.Resize, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				
				updateLayout();
				if (overview != null && !overview.isDisposed()) {
					for (Control child : overview.getChildren()) {
						Control canvas = ((Composite) child).getChildren()[0];
						Thumbnail thumbnail = (Thumbnail) canvas.getData();
						thumbnail.setDirty(true);
					}
				}
			}
		});
		
		

		// activeScreens = new GalleryScreenContributionItem(this);
		GalleryLayoutAction layoutAction = new GalleryLayoutAction(this);
		GalleryModeAction modeAction = new GalleryModeAction(this);
		dropDownAction = new GalleryDropDownAction(this);
		IActionBars actionBars = getSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		// toolBar.add(activeScreens);
		// toolBar.add(new Separator());
		toolBar.add(dropDownAction);
		toolBar.add(modeAction);
		toolBar.add(new Separator());
		toolBar.add(layoutAction);
		
		
		}
		
		
	

	private void createOverview() {
		int type = getType();
		
		if (overview != null) {
			overview.dispose();
		}
		overview = new Composite(scroll, SWT.CENTER);
		
		scroll.setContent(overview);
		setType(type);
		
		/* add context menu */
		final MenuManager menuMgr = new MenuManager("#PopupMenu"); 
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager, null);
			}
		});
		Menu canvasMenu = menuMgr.createContextMenu(overview);
		overview.setMenu(canvasMenu);
		Menu scrollMenu = menuMgr.createContextMenu(scroll);
		scroll.setMenu(scrollMenu);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.Page#getControl()
	 */
	@Override
	public Control getControl() {
		return composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.Page#setFocus()
	 */
	@Override
	public void setFocus() {
		overview.setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.Page#dispose()
	 */
	@Override
	public void dispose() {
		adapter.removeGalleryListener(this);
		selectionListeners.clear();
		controller.stop();
		super.dispose();
	}

	/**
	 * Initializes the gallery.
	 */
	public void init(IGalleryScreenProvider screenProvider) {
		if (composite == null || composite.isDisposed()) {
			return;
		}
		controller.clear();
		createOverview();

		selectionListeners.remove(this.screenProvider);
		if (screenProvider != null) {
			selectionListeners.add(screenProvider);
		}
		this.screenProvider = screenProvider;

		overview.setSize(overview.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scroll.layout();

		// activeScreens.clear();

		if (screenProvider != null) {
			((GalleryLayout) overview.getLayout()).setMaxWidth(screenProvider
					.getSize().width);
			((GalleryLayout) overview.getLayout()).setMaxHeight(screenProvider
					.getSize().height);

			showScreen(screenProvider.getGalleryScreens());
			
		}
		
	}

	
	
	private void showScreen(final List<IGalleryScreen> galleryScreens) {

		if (overview.isDisposed()) {
			return;
		}

//		final String message = MessageFormat.format(
//				ViewMessages.GalleryView_generatePreview, new Object[] { screen
//						.getName() });
		
		final String message = "Generating Gallery Preview Screens...";
		Job job = new Job(message) {
			Object targetOverview = overview;

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				try {
					Display display = getSite().getShell().getDisplay();
					if (display == null) {
						return Status.OK_STATUS;
					}

					display.syncExec(new Runnable() {
						public void run() {
							monitor.beginTask(message,
									ScreenModelMediator.TOTAL_WORK * galleryScreens.size());
						}
					});
					if (overview != targetOverview) {
						// overview changed
						return Status.OK_STATUS;
					}
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					if (overview != targetOverview) {
						// overview changed
						return Status.OK_STATUS;
					}

					for (final IGalleryScreen iGalleryScreen : galleryScreens) {
						if (mode == MODE_ALL || iGalleryScreen.isDefault()) {
							monitor.setTaskName(MessageFormat.format(
									ViewMessages.GalleryView_generatePreview,
									new Object[] { iGalleryScreen.getName() }));
							final EditPartViewer viewer = iGalleryScreen.getViewer(monitor);
							display.syncExec(new Runnable() {
								/*
								 * (non-Javadoc)
								 * 
								 * @see java.lang.Runnable#run()
								 */
								public void run() {
									try {
										createScreen(iGalleryScreen, iGalleryScreen.isActive(), viewer);
									} catch (IllegalStateException e) {
										screenProvider.galleryDisposed();
									} 
								}
							});
							}
					}
					
					return Job.ASYNC_FINISH;
				} finally {
					getSite().getShell().getDisplay().syncExec(new Runnable() {

						public void run() {
							updateLayout();
							done(Status.OK_STATUS);
							
						}
						
					});
					if (screenProvider != null && screenProvider.isDisposed()) {
						screenProvider.galleryDisposed();
					}
				}
			}
		};
		controller.queue(job);
	
		
	}
	/**
	 * Returns all gallery screens.
	 * 
	 * @return all gallery screens.
	 */
	public List<IGalleryScreen> getScreens() {
		if (screenProvider == null) {
			return new ArrayList<IGalleryScreen>(0);
		}
		return screenProvider.getGalleryScreens();
	}

	/**
	 * Generates preview for one specific screen and displays it on the top of
	 * gallery page.
	 * 
	 * @param screen
	 *            the screen from where the preview is to be generated.
	 * @param moveToTop
	 *            true to move the screen to the top.
	 */
	public void showScreen(final IGalleryScreen screen, final boolean moveToTop) {
		if (overview.isDisposed()) {
			return;
		}
		
		final String message = MessageFormat.format(
				ViewMessages.GalleryView_generatePreview, new Object[] { screen
						.getName() });
		Job job = new Job(message) {
			Object targetOverview = overview;

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				try {
					Display display = getSite().getShell().getDisplay();
					if (display == null) {
						return Status.OK_STATUS;
					}

					display.syncExec(new Runnable() {
						public void run() {
							monitor.beginTask(message,
									ScreenModelMediator.TOTAL_WORK);
						}
					});
					if (overview != targetOverview) {
						// overview changed
						return Status.OK_STATUS;
					}
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					try {
						screen.getViewer(monitor);
					} catch (Throwable e) {
						return Status.OK_STATUS;
					}
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					if (overview != targetOverview) {
						// overview changed
						return Status.OK_STATUS;
					}

					
					display.syncExec(new Runnable() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see java.lang.Runnable#run()
						 */
						public void run() {
							try {
								createScreen(screen, moveToTop, screen.getViewer(null));
							} catch (IllegalStateException e) {
								screenProvider.galleryDisposed();
							} finally {
								done(Status.OK_STATUS);
							}
						}
					});
					return Job.ASYNC_FINISH;
				} finally {
					if (screenProvider != null && screenProvider.isDisposed()) {
						screenProvider.galleryDisposed();
					}
				}
			}
		};
		controller.queue(job);
	}

	/**
	 * Called when user selects one screen and the registered listeners will be
	 * notified.
	 * 
	 * @param viewer
	 *            the selected viewer.
	 */
	public void selectionChanged(EditPartViewer viewer) {
		
		selection = new StructuredSelection(viewer);
		SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
		for (ISelectionChangedListener listener : selectionListeners) {
			listener.selectionChanged(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		
		return selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		selectionListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		this.selection = selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.views.IGalleryAdapter.IGalleryListener#galleryChanged(com.nokia.tools.s60.views.IGalleryScreenProvider)
	 */
	public void galleryChanged(IGalleryScreenProvider screenProvider) {
		
		galleryChanged(screenProvider, true);
	}

	protected void galleryChanged(IGalleryScreenProvider screenProvider,
			boolean force) {
		Set<IGalleryScreen> screensToKeep = new HashSet<IGalleryScreen>();
		if (!force && screenProvider != null) {
			for (IGalleryScreen screen : screenProvider.getGalleryScreens()) {
				if (screen.getControl() != null
						&& !screen.getControl().isDisposed()) {
					screensToKeep.add(screen);
				}
			}
		}
		if (screensToKeep.isEmpty()) {
			init(screenProvider);
		} else if (overview != null && !overview.isDisposed()) {
			// disposes the old screens
			for (Control child : overview.getChildren()) {
				IGalleryScreen screen = (IGalleryScreen) child.getData();
				if (!screensToKeep.contains(screen)) {
					hideScreen(screen);
				}
			}
			IGalleryScreen activeScreen = null;
			// then creates new screens
			for (IGalleryScreen screen : screenProvider.getGalleryScreens()) {
				if (activeScreen == null && screen.isActive()) {
					activeScreen = screen;
				}
				if (screen.isDefault()
						&& (screen.getControl() == null || screen.getControl()
								.isDisposed())) {
					showScreen(screen);
				}
			}
			selectScreen(activeScreen == null ? null : activeScreen
					.getControl());
		}
	}

	public void screenCreated(IGalleryScreen screen) {
		if (screenProvider != null && mode != MODE_ALL) {
			screenProvider.screenCreated(screen);
		}
	}

	public void screenDisposed(IGalleryScreen screen) {
		if (screenProvider != null && mode != MODE_ALL) {
			screenProvider.screenDisposed(screen);
		}
	}

	public void setMode(int mode) {
		if (screenProvider == null || this.mode == mode) {
			return;
		}
		this.mode = mode;
		if (mode == MODE_ALL) {
//			for (IGalleryScreen screen : screenProvider.getGalleryScreens()) {
//				showScreen(screen, false);
//			}
			showScreen(screenProvider.getGalleryScreens());
		} else {
			galleryChanged(screenProvider, true);
		}
		updateLayout();
	}

	public int getMode() {
		return mode;
	}

	/**
	 * @param type
	 *            the style to set, can be either FIT_PAGE or MAXIMUM_STRETCH.
	 */
	public void setType(int type) {
		if (overview != null && !overview.isDisposed()) {
			GalleryLayout layout = (GalleryLayout) overview.getLayout();
			if (layout == null) {
				layout = new GalleryLayout(type);
				layout.setBorderHeight(TAB_HEIGHT);
				overview.setLayout(layout);
			} else {
				if (layout.getType() == type) {
					return;
				}
				layout.setType(type);
				IPreferenceStore store = S60WorkspacePlugin.getDefault()
						.getPreferenceStore();
				store.setValue(IS60IDEConstants.PREF_GALLERY_LAYOUT_TYPE, type);
			}
			updateLayout();
		}
	}

	/**
	 * @return the style, can be either FIT_PAGE or MAXIMUM_STRETCH.
	 */
	public int getType() {
		if (overview != null && !overview.isDisposed()) {
			GalleryLayout layout = (GalleryLayout) overview.getLayout();
			if (null != layout)
				return layout.getType();
		}
		IPreferenceStore store = S60WorkspacePlugin.getDefault()
				.getPreferenceStore();
		return store.getInt(IS60IDEConstants.PREF_GALLERY_LAYOUT_TYPE);
	}

	
	public void showScreen(IGalleryScreen screen) {
		showScreen(screen, true);
	}

	/**
	 * This method creates the screen and refresh the gallery view
	 * by displaying the selected screen at the top.
	 * @param screen
	 * 		selected screen from the gallery's drop down menu.
	 */
	public void showSingleScreen(IGalleryScreen screen){
		refreshSelectedScreen=true;
		showScreen(screen, true);
	}
	
	
	public void hideScreen(IGalleryScreen screen) {
		for (Control comp : overview.getChildren()) {
			if (comp.getData() == screen) {
				comp.dispose();
				screenDisposed(screen);
				updateLayout();
			}
		}
	}

	/**
	 * Creates the preview screen control.
	 * 
	 * @param screen
	 *            the gallery screen from where the control is created.
	 * @param moveToTop
	 *            true to move the created element to the top.
	 * @param viewer 
	 */
	private void createScreen(final IGalleryScreen screen, boolean moveToTop, final EditPartViewer viewer) {
//		final EditPartViewer viewer = screen.getViewer(null);
		if (overview.isDisposed() || viewer == null) {
			return;
		}
		boolean isNewScreen = screen.getControl() == null
				|| screen.getControl().isDisposed();
		if (isNewScreen) {
			final CTabFolder comp = new CTabFolder(overview, SWT.TOP
					| SWT.SINGLE | SWT.FLAT);
			comp.setMinimumCharacters(0);
			comp.setSimple(false);
			comp.setCursor(Display.getDefault()
					.getSystemCursor(SWT.CURSOR_HAND));
			final CTabItem item = new CTabItem(comp, SWT.CLOSE);
			item.setText(screen.getName());
			
			comp.addMouseListener(new MouseAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
				 */
				@Override
				public void mouseUp(MouseEvent e) {
					
					if (comp.getItemCount() == 0) {
						hideScreen(screen);
					} else {
						selectionChanged(viewer);
						// activeScreens.selectScreen(screen);
					}
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
				 */
				@Override
				public void mouseDown(MouseEvent e) {
					selectScreen(screen.getControl());
					
				}
			});

			/* add context menu */
			final MenuManager menuMgr = new MenuManager("#PopupMenu"); 
			menuMgr.setRemoveAllWhenShown(true);
			menuMgr.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					fillContextMenu(manager, screen);
				}
			});

			Menu menu = menuMgr.createContextMenu(comp);
			comp.setMenu(menu);

			comp.setSelection(0);
			IFigure figure = (IFigure) ((GraphicalEditPart) viewer
					.getContents()).getFigure().getChildren().get(0);
			Canvas canvas = new Canvas(comp, SWT.NONE);
			item.setControl(canvas);
			canvas.setToolTipText(screen.getName());
			canvas.addMouseListener(new MouseAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
				 */
				@Override
				public void mouseUp(MouseEvent e) {
					Platform.run(new SafeRunnable() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see org.eclipse.core.runtime.ISafeRunnable#run()
						 */
						public void run() throws Exception {
							selectionChanged(viewer);
							// activeScreens.selectScreen(screen);
						}
					});
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
				 */
				@Override
				public void mouseDown(MouseEvent e) {
					selectScreen(screen.getControl());
					
				}
			});

			/* add context menu */
			final MenuManager menuMgr2 = new MenuManager("#PopupMenu"); 
			menuMgr2.setRemoveAllWhenShown(true);
			menuMgr2.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					fillContextMenu(manager, screen);
				}
			});

			Menu canvasMenu = menuMgr2.createContextMenu(canvas);
			canvas.setMenu(canvasMenu);

			final Thumbnail thumbnail = new Thumbnail(figure);
			final LightweightSystem lws = new LightweightSystem(canvas);
			lws.setContents(thumbnail);
			screen.setControl(comp);
			comp.setData(screen);
			canvas.setData(thumbnail);
			canvas.addDisposeListener(new DisposeListener() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
				 */
				public void widgetDisposed(DisposeEvent e) {
					thumbnail.deactivate();
					lws.getUpdateManager().dispose();
				}
			});
		}

		if (moveToTop && isNewScreen) {
			// moves the new screen to the top
			screen.getControl().moveAbove(null);
		}

		if(refreshSelectedScreen){
			//The selected screen appears on the gallery view.
			updateLayout();
		}
		
		if (moveToTop) {
			if (isNewScreen) {
				scroll.setOrigin(0, 0);
			} else {
				scroll.setOrigin(screen.getControl().getLocation());
			}
		}

		if (screen.isActive()) {
			selectScreen(screen.getControl());
		}
	}

	private void fillContextMenu(IMenuManager manager, IGalleryScreen screen) {
		ActionRegistry registry = new ActionRegistry();
		IAction[] actions = ContributedActionsResolver.getInstance().getActions(
				"gallery", (WorkbenchPart)EclipseUtils.getActiveSafeEditor());
		if(!contributed){
			contributeToActionBars();
		}
		for (IAction action : actions) {
//			if (action instanceof IGalleryScreenAction) {
//				IGalleryScreenAction galleryAction = (IGalleryScreenAction) action;
//				galleryAction.setGalleryPage(GalleryPage.this);
//				galleryAction.setScreen(screen);
//			}
			registry.registerAction(action);
		}
		
		ContributedActionsResolver.getInstance().contributeActions(manager,
				"gallery", registry);
	}

	private void updateLayout() {
		if(!overview.isDisposed()){
			if (internalLayout || overview.getLayout() == null) {
				return;
			}
			internalLayout = true;
	
			try {
				overview.setSize(overview.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				overview.layout();
				scroll.layout();
				
				((GalleryLayout) overview.getLayout()).setUpdate(true);
				overview.setSize(overview.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				scroll.layout();
				((GalleryLayout) overview.getLayout()).setUpdate(false);
	
				// refresh needed for updating the background color of the tabfolder
				for (Control child : overview.getChildren()) {
					child.redraw();
				}
			} finally {
				/**Once the selected screen is created and displayed on the top most
				 * position of the gallery view, refreshSelectedScreen should be false.*/
				refreshSelectedScreen=false;
				internalLayout = false;
			}
		}
	}

	public void selectScreen(Control screen) {
		Display display = Display.getCurrent();
		for (Control control : overview.getChildren()) {
			CTabFolder folder = (CTabFolder) control;
			if (screen == control) {
				folder.setSelectionBackground(new Color[] {
						display.getSystemColor(SWT.COLOR_BLACK),
						display.getSystemColor(SWT.COLOR_WHITE) },
						new int[] { 100 });
				folder.setSelectionForeground(display
						.getSystemColor(SWT.COLOR_WHITE));
			} else {
				folder.setSelectionBackground(new Color[] {
						display.getSystemColor(SWT.COLOR_WHITE),
						display.getSystemColor(SWT.COLOR_WHITE) },
						new int[] { 100 });
				folder.setSelectionForeground(display
						.getSystemColor(SWT.COLOR_BLACK));
			}
		}
	}

	class JobController extends JobChangeAdapter implements Runnable {
		BlockingQueue<Job> queue = new LinkedBlockingQueue<Job>();

		volatile Thread thread;

		Object lock = new Object();

		int runningJobs;

		Set<Job> activeJobs = new HashSet<Job>(16);

		synchronized void start() {
			if (thread != null) {
				return;
			}
			thread = new Thread(this, "Gallery job controller");
			thread.start();
		}

		synchronized void stop() {
			if (thread == null) {
				return;
			}
			clear();
			Thread tmp = thread;
			thread = null;
			tmp.interrupt();
		}

		synchronized void queue(Job job) {
			job.addJobChangeListener(this);
			activeJobs.add(job);
			try {
				queue.put(job);
			} catch (Exception e) {
				S60WorkspacePlugin.error(e);
			}
		}

		synchronized void clear() {
			queue.clear();
			for (Job job : activeJobs) {
				job.cancel();
			}
			activeJobs.clear();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			while (Thread.currentThread() == thread) {
				Job job = null;
				try {
					job = queue.take();
				} catch (Exception e) {
					return;
				}
				while (runningJobs >= MAX_RUNNING_JOBS) {
					synchronized (lock) {
						try {
							lock.wait(100);
						} catch (Exception e) {
							return;
						}
					}
				}
				runningJobs++;
				job.schedule();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
		 */
		@Override
		public void done(IJobChangeEvent event) {
			synchronized (lock) {
				try {
					runningJobs--;
					lock.notifyAll();
				} finally {
					if (screenProvider != null && screenProvider.isDisposed()) {
						screenProvider.galleryDisposed();
					}
				}
			}
		}
	}
	
	public interface IGalleryScreenAction {
		void setGalleryPage(GalleryPage page);

		void setScreen(IGalleryScreen screen);
	}

}
