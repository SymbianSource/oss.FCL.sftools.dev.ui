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

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.editor.AbstractMediaEditorPart;
import com.nokia.tools.media.utils.editor.frameanimation.FrameAnimActionFactory;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.ITimeLine;
import com.nokia.tools.media.utils.timeline.ITimeLineRow;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.s60.editor.actions.BrowseForFileAction;
import com.nokia.tools.s60.editor.actions.ConvertAndEditSVGInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.CopyImageAction;
import com.nokia.tools.s60.editor.actions.CreateAnimationFrameAction;
import com.nokia.tools.s60.editor.actions.DistributeAnimateTimeAction;
import com.nokia.tools.s60.editor.actions.EditImageInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.EditImageInSVGEditorAction;
import com.nokia.tools.s60.editor.actions.EditInSystemEditorAction;
import com.nokia.tools.s60.editor.actions.EditMaskAction;
import com.nokia.tools.s60.editor.actions.EditMaskAction2;
import com.nokia.tools.s60.editor.actions.GenericCommandAction;
import com.nokia.tools.s60.editor.actions.PasteImageAction;
import com.nokia.tools.s60.editor.actions.RemoveAnimationFrameAction;
import com.nokia.tools.s60.editor.actions.SetAnimateTimeAction;
import com.nokia.tools.s60.editor.actions.SetAnimationModeAction;
import com.nokia.tools.s60.editor.actions.SetStretchModeDDown;
import com.nokia.tools.s60.editor.actions.layers.AddBgLayerAction;
import com.nokia.tools.s60.editor.actions.layers.AddLayerAction;
import com.nokia.tools.s60.editor.actions.layers.BaseLayerAction;
import com.nokia.tools.s60.editor.actions.layers.ChangeOrderAction;
import com.nokia.tools.s60.editor.actions.layers.ClearLayerAction;
import com.nokia.tools.s60.editor.actions.layers.CustomizeAction;
import com.nokia.tools.s60.editor.actions.layers.DeleteSelectedAction;
import com.nokia.tools.s60.editor.actions.layers.ILayerActionsHost;
import com.nokia.tools.s60.editor.actions.layers.RenameLayerAction;
import com.nokia.tools.s60.editor.commands.SetThemeGraphicsCommand;
import com.nokia.tools.s60.editor.graphics.IImagePreviewComposite;
import com.nokia.tools.s60.editor.ui.views.AddEffectDropDown;
import com.nokia.tools.s60.editor.ui.views.EffectControlsEditorPage;
import com.nokia.tools.s60.editor.ui.views.EffectControlsEditorView;
import com.nokia.tools.s60.editor.ui.views.LayerSelectionListener;
import com.nokia.tools.s60.editor.ui.views.LayersView;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.views.IconView;
import com.nokia.tools.s60.views.IconViewPage;
import com.nokia.tools.screen.core.INamingAdapter;
import com.nokia.tools.screen.core.IScreenFactory;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.editor.IContentDependentEditor;
import com.nokia.tools.screen.ui.editor.ScreenEditorPart;
import com.nokia.tools.screen.ui.propertysheet.tabbed.DisposableTabbedPropertySheetPage;
import com.nokia.tools.theme.s60.ui.animation.controls.ILayerEffectPropertySheetPage;
import com.nokia.tools.theme.s60.ui.animation.controls.ITimeModelChangeListener;
import com.nokia.tools.ui.view.ViewUtil;

/**
 *  
 * <br/><br/><hr/><br/><br/> <table border="1">
 * <tr>
 * <th>Changes</th>
 * <th>Author</th>
 * <th>Date</th>
 * </tr>
 * <tr>
 *  </tr>
 * </table>
 */
public class GraphicsEditorPart extends AbstractMediaEditorPart implements
		LayerSelectionListener, IPartListener, CommandStackListener,
		ILayerActionsHost, IContentDependentEditor {

	public static final String HLP_CTX = "com.nokia.tools.s60.ide" + '.' + "entityGraphicsEditor_context"; //$NON-NLS-1$
	public static final String HLP_CTX_EFFECTS = "com.nokia.tools.s60.ide.effectcontrols_context";
	public static final String ID = "com.nokia.tools.s60.graphicseditor";

	// parent editor
	private IEditorPart callerEditor;

	
	private IContentData sourceContentData;
	private EditPart sourceEditPart;

	private boolean isDirty;

	// screen area composite
	private Composite screenAreaComposite;
	private LayerEffectPropertySheetPage propertySheetPage;
	private Menu menu;

	/* actions that needs to be updated after cmdStack change */
	private List<IAction> stackActions = new ArrayList<IAction>();
	/* action that listens selections must be disposed explicitly */
	private List<WorkbenchPartAction> actionsToDispose = new ArrayList<WorkbenchPartAction>();

	private static final Dimension DEFAULT_SIZE = new Dimension(40, 40);

	/** Drop operation is not allowed main composite, but children can override. */
	private DropTarget screenAreaCompositeDropTarget;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		if (isDirty()) {

			try {
				if (!(getActiveImage() instanceof IAnimatedImage)) {
					getActiveImage().clearPreviewImages();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			SetThemeGraphicsCommand cmd = new SetThemeGraphicsCommand(
					getActiveImage(), sourceContentData, sourceEditPart, null);

			
			GenericCommandAction action = new GenericCommandAction(
					callerEditor,
					ExternalEditorSupport
							.getSelectionProvider(sourceEditPart == null ? sourceContentData
									: sourceEditPart),
					(CommandStack) callerEditor.getAdapter(CommandStack.class),
					cmd);

			action.run();
			
			// do set attribute ThemeTag.ATTR_ANIM_MODE
			saveAnimationMode();

			isDirty = false;
			if (Display.getCurrent() == null)
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						firePropertyChange(GraphicsEditorPart.PROP_DIRTY);
					}
				});
			else
				firePropertyChange(GraphicsEditorPart.PROP_DIRTY);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		throw new RuntimeException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);

		GraphicsEditorInput in = (GraphicsEditorInput) input;

		// callerStack = in.getCommandStack();
		callerEditor = in.getParentEditor();

		if (in.getData().get(0) instanceof EditPart) {
			sourceEditPart = (EditPart) in.getData().get(0);
			sourceContentData = JEMUtil.getContentData(sourceEditPart);

		} else if (in.getData().get(0) instanceof IContentData) {
			sourceEditPart = null;
			sourceContentData = (IContentData) in.getData().get(0);
		}

		IImageAdapter imageAdapter = (IImageAdapter) sourceContentData
				.getAdapter(IImageAdapter.class);

		setImage(imageAdapter.getImage());

		INamingAdapter ina = (INamingAdapter) sourceContentData
				.getAdapter(INamingAdapter.class);
		if (ina != null) {
			setPartName(ina.getName());
		} else
			setPartName(sourceContentData.getId());

		site.getWorkbenchWindow().getPartService().addPartListener(this);

		/* undo / redo retargets */
		UndoAction undoAction = new UndoAction(this);
		stackActions.add(undoAction);
		getActionRegistry().registerAction(undoAction);
		RedoAction redoAction = new RedoAction(this);
		stackActions.add(redoAction);
		getActionRegistry().registerAction(redoAction);

		
		try {
			IWorkbenchPage page = getSite().getWorkbenchWindow()
					.getActivePage();
			if (page.findView("org.eclipse.ui.views.PropertySheet") == null)
				page.showView("org.eclipse.ui.views.PropertySheet", null,
						IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException e) {
			e.printStackTrace();
		}

		getCommandStack().addCommandStackListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenEditorPart#commandStackChanged(java.util.EventObject)
	 */
	public void commandStackChanged(EventObject event) {
		updateActions(stackActions);
	}

	/**
	 * Updates the specified actions.
	 * 
	 * @param actions
	 *            actions to be updated.
	 */
	protected void updateActions(List<IAction> actions) {
		Iterator<IAction> iter = actions.iterator();
		while (iter.hasNext()) {
			IAction action = iter.next();
			if (action instanceof UpdateAction)
				((UpdateAction) action).update();
		}
	}

	@Override
	public void dispose() {
		if (propertySheetPage != null) {
			((LayerEffectPropertySheetPage) propertySheetPage)
					.removePropertyChangedListener(this);
		}
		try {
			getSite().getWorkbenchWindow().getPartService().removePartListener(
					this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// clears reference to the main editor
		if (getEditorInput() instanceof GraphicsEditorInput) {
			((GraphicsEditorInput) getEditorInput()).dispose();
		}
		// disposes the menu that is associated to the shell
		if (menu != null) {
			menu.dispose();
		}
		for (WorkbenchPartAction action : actionsToDispose) {
			action.dispose();
		}

		getEditorSite().getActionBars().clearGlobalActionHandlers();
		if (screenAreaCompositeDropTarget != null) {
			screenAreaCompositeDropTarget.dispose();
		}
		super.dispose();
	}

	/**
	 * Refreshes UI state with respect to active object
	 * 
	 * @param selection
	 */
	public void refresh(IImage selection) {
		super.refresh(selection);
		refreshLayersView(selection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return isDirty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	protected Object getSelectedElement() {
		Object obj = ((IStructuredSelection) GraphicsEditorPart.this
				.getTimeline().getViewer().getSelection()).getFirstElement();
		return obj;
	}

	@Override
	protected void contributeTimeLineMenu(MenuManager menuManager) {
		super.contributeTimeLineMenu(menuManager);

		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(
					org.eclipse.jface.action.IMenuManager manager) {

				if (getSelectedElement() instanceof ILayer) {

					final ILayer layer = (ILayer) getSelectedElement();

					CopyImageAction copyImageAction = new CopyImageAction(
							getTimeline().getViewer(), null);
					PasteImageAction pasteImageAction = new PasteImageAction(
							getTimeline().getViewer(), getCommandStack(), null);

					manager.add(new Separator());
					manager.add(copyImageAction);
					manager.add(pasteImageAction);
					manager.add(new Separator());

					// add layer
					IAction action = new AddLayerAction(getActiveImage(),
							GraphicsEditorPart.this, false);
					manager.add(action);

					// add bg
					if (layer.getParent().getLayerCount() > 0
							&& !layer.getParent().getLayer(0).isBackground()) {
						action = new AddBgLayerAction(getActiveImage(),
								GraphicsEditorPart.this, false);
						if (action.isEnabled())
							manager.add(action);
					}

					// remove action
					action = new DeleteSelectedAction(GraphicsEditorPart.this,
							null, false);
					manager.add(action);

					// rename action
					action = new RenameLayerAction(getActiveImage(),
							GraphicsEditorPart.this, false);
					manager.add(action);

					action = new AddEffectDropDown(GraphicsEditorPart.this);
					manager.add(new Separator());
					manager.add(action);

					SetStretchModeDDown sstretch = new SetStretchModeDDown(
							null, getTimeline().getViewer(), getCommandStack());
					if (sstretch.isEnabled()) {
						manager.add(new Separator());
						manager.add(sstretch);
					}

					// layer
					manager.add(new Separator());
					action = new EditImageInBitmapEditorAction(getTimeline()
							.getViewer(), getCommandStack());
					if (action.isEnabled()) {
						manager.add(action);
					}
					action = new ConvertAndEditSVGInBitmapEditorAction(
							getTimeline().getViewer(), getCommandStack());
					if (action.isEnabled()) {
						manager.add(action);
					}
					action = new EditImageInSVGEditorAction(getTimeline()
							.getViewer(), getCommandStack());
					if (action.isEnabled()) {
						manager.add(action);
					}
					action = new EditInSystemEditorAction(getTimeline()
							.getViewer(), getCommandStack());
					if (action.isEnabled()) {
						manager.add(action);
					}

					// mask actions
					manager.add(new Separator());
					action = new EditMaskAction(getTimeline().getViewer(),
							getCommandStack());
					if (action.isEnabled()) {
						manager.add(action);
					}

					// clear layer
					action = new ClearLayerAction(getActiveImage(),
							GraphicsEditorPart.this, false);
					if (action.isEnabled()) {
						manager.add(new Separator());
						manager.add(action);
					}
				} else if (getSelectedElement() instanceof ILayerEffect) {
					IAction act = new DeleteSelectedAction(
							GraphicsEditorPart.this, null, false);
					manager.add(act);

					act = new CustomizeAction(getActiveImage(),
							GraphicsEditorPart.this, null, false);
					if (act.isEnabled())
						manager.add(act);
				}

			};
		});

		/* add key shortcuts on timeline tree */
		getTimeline().getViewer().getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					DeleteSelectedAction d = new DeleteSelectedAction(
							GraphicsEditorPart.this, null, false);
					if (d.isEnabled())
						d.run();
				}
			}
		});
	}

	/**
	 * 
	 * @param tl
	 */
	@Override
	protected void contributeToTimeModelCombo(final ToolBar tb,
			final Label sceneLabel) {

		new ToolItem(tb, SWT.SEPARATOR);

		final Image image = S60WorkspacePlugin.getImageDescriptor(
				"icons/animation_timing16x16.png").createImage();
		final ToolItem timeModelItem = new ToolItem(tb, SWT.DROP_DOWN);
		timeModelItem.setImage(image);
		timeModelItem.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				image.dispose();
			}
		});

		menu = new Menu(tb.getShell(), SWT.POP_UP);

		boolean supportsRelative = getActiveImage().supportsAnimationTiming(
				TimingModel.Relative), supportsRealtime = getActiveImage()
				.supportsAnimationTiming(TimingModel.RealTime);

		if (supportsRelative) {
			MenuItem relativeTMItem = new MenuItem(menu, SWT.PUSH);
			relativeTMItem
					.setText(EditorMessages.GraphicsEditor_Timeline_RelativeTime_Hour);
			relativeTMItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					refreshTimeLine(TimingModel.Relative, TimeSpan.EHour,
							getActiveImage());
					IPropertySheetPage propPage = (IPropertySheetPage) getAdapter(IPropertySheetPage.class);
					if (propPage instanceof LayerEffectPropertySheetPage) {
						((LayerEffectPropertySheetPage) propPage)
								.notityTimeModelChangedListeners();
					}
				}
			});

			relativeTMItem = new MenuItem(menu, SWT.PUSH);
			relativeTMItem
					.setText(EditorMessages.GraphicsEditor_Timeline_RelativeTime_Day);
			relativeTMItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					refreshTimeLine(TimingModel.Relative, TimeSpan.EDay,
							getActiveImage());
					IPropertySheetPage propPage = (IPropertySheetPage) getAdapter(IPropertySheetPage.class);
					if (propPage instanceof LayerEffectPropertySheetPage) {
						((LayerEffectPropertySheetPage) propPage)
								.notityTimeModelChangedListeners();
					}
				}
			});

			relativeTMItem = new MenuItem(menu, SWT.PUSH);
			relativeTMItem
					.setText(EditorMessages.GraphicsEditor_Timeline_RelativeTime_Week);
			relativeTMItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					refreshTimeLine(TimingModel.Relative, TimeSpan.EWeek,
							getActiveImage());
					IPropertySheetPage propPage = (IPropertySheetPage) getAdapter(IPropertySheetPage.class);
					if (propPage instanceof LayerEffectPropertySheetPage) {
						((LayerEffectPropertySheetPage) propPage)
								.notityTimeModelChangedListeners();
					}
				}
			});

			relativeTMItem = new MenuItem(menu, SWT.PUSH);
			relativeTMItem
					.setText(EditorMessages.GraphicsEditor_Timeline_RelativeTime_Month);
			relativeTMItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					refreshTimeLine(TimingModel.Relative, TimeSpan.EMonth,
							getActiveImage());
					IPropertySheetPage propPage = (IPropertySheetPage) getAdapter(IPropertySheetPage.class);
					if (propPage instanceof LayerEffectPropertySheetPage) {
						((LayerEffectPropertySheetPage) propPage)
								.notityTimeModelChangedListeners();
					}
				}
			});

		}

		if (supportsRealtime) {
			MenuItem realTimeTMItem = new MenuItem(menu, SWT.PUSH);
			realTimeTMItem
					.setText(EditorMessages.GraphicsEditor_Timeline_RealTime);
			realTimeTMItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					refreshTimeLine(TimingModel.RealTime, null,
							getActiveImage());
					IPropertySheetPage propPage = (IPropertySheetPage) getAdapter(IPropertySheetPage.class);
					if (propPage instanceof LayerEffectPropertySheetPage) {
						((LayerEffectPropertySheetPage) propPage)
								.notityTimeModelChangedListeners();
					}
				}
			});
		}

		timeModelItem.setEnabled(menu.getItemCount() > 1);


		timeModelItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.ARROW) {
					Rectangle rect = timeModelItem.getBounds();
					Point pt = new Point(rect.x, rect.y + rect.height);
					pt = tb.toDisplay(pt);
					menu.setLocation(pt.x, pt.y);
					menu.setVisible(true);
				}
			}
		});

		// refresh time line accrding to model supported
		if (supportsRelative && !supportsRealtime) {
			refreshTimeLine(TimingModel.Relative, TimeSpan.EHour,
					getActiveImage());
		}
	}

	private boolean restoreComponentsView;
	private Menu animModeMenu;

	private Point getControlLoc(Control c) {
		int x = 0;
		int y = 0;
		while ((c = c.getParent()) != null) {
			x += c.getLocation().x;
			y += c.getLocation().y;
		}
		return new Point(x, y);
	}

	protected void hideComponentsView() {

		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		final IconView part = (IconView) page.findView(IconView.ID);
		if (part != null) {
			
			if (page.getViewStack(part).length > 1)
				return; // is stacked, no action needed
		}

		if (part != null) {

			if (part.getCurrentPage() instanceof IconViewPage) {

				Control viewRoot = part.getCurrentPage().getControl();
				Point compViewLoc = getControlLoc(viewRoot);
				Point editorLoc = getControlLoc(getRootControl());

								
				if (editorLoc.x == compViewLoc.x && editorLoc.y < compViewLoc.y
						&& viewRoot.getSize().x == getRootControl().getSize().x) {
					try {
						ViewUtil.minimizeView(IconView.ID);
						restoreComponentsView = true;
					} catch (Exception e) {
						S60WorkspacePlugin.error(e);
					}
				}
			}
		}
	}

	protected void restoreComponentsView() {
		if (restoreComponentsView) {
			try {
				ViewUtil.restoreView(IconView.ID);
				restoreComponentsView = false;
			} catch (Exception e) {
				S60WorkspacePlugin.error(e);
			}
		}
	}

	private EffectControlsEditorPage getEffectControlsEditorPage() {
		IWorkbenchPage page = getSite().getWorkbenchWindow().getActivePage();
		if (page != null) {
			EffectControlsEditorView effectControlsView = null;
			effectControlsView = (EffectControlsEditorView) page
					.findView(EffectControlsEditorView.ID);

			if (effectControlsView != null) {

				effectControlsView.partActivated(this);
				EffectControlsEditorPage effectsPage = ((EffectControlsEditorPage) effectControlsView
						.getCurrentPage());
				return effectsPage;
			}
		}
		return null;
	}

	protected void refreshLayersView(IImage image) {
		IWorkbenchPage page = getSite().getWorkbenchWindow().getActivePage();

		if (page != null) {
			LayersView layersView;
			try {
				layersView = (LayersView) page.findView(LayersView.ID);

				if (layersView == null) {
					layersView = (LayersView) page.showView(LayersView.ID,
							null, IWorkbenchPage.VIEW_VISIBLE);
				}

				if (layersView != null) {

					layersView.partActivated(this);
					layersView.removeLayerSelectionListener(this);
					layersView.addLayerSelectionListener(this);
				}
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == CommandStack.class) {
			return getCommandStack();
		}
		if (adapter == IPropertySheetPage.class) {
			if (propertySheetPage == null
					|| propertySheetPage.getControl() == null
					|| propertySheetPage.getControl().isDisposed()) {
				ITabbedPropertySheetPageContributor contributor = new ITabbedPropertySheetPageContributor() {
					public String getContributorId() {
						return "com.nokia.tools.theme.s60.ui.animation.LayerAnimationControls";
					};
				};
				// creates page only when the property sheet is activated, not
				// in createControl part.
				propertySheetPage = new LayerEffectPropertySheetPage(
						contributor);
				propertySheetPage.addPropertyChangedListener(this);
			}
			return propertySheetPage;
		}
		if (adapter == ScreenEditorPart.class)
			return callerEditor;
		if (adapter == ActionRegistry.class) {
			return getActionRegistry();
		}
		return super.getAdapter(adapter);
	}

	// property changes from IImage
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() instanceof IImage) {

			if (IImage.PROPERTY_STATE.equals(evt.getPropertyName())
					|| IImage.PROPERTY_ANIMATION_STATE.equals(evt
							.getPropertyName())) {
				if (!isDirty) {
					if(ILayerEffect.PROPERTY_ANIMATION_STATE != evt.getPropertyName()){
						isDirty = true;
					}
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							firePropertyChange(GraphicsEditorPart.PROP_DIRTY);
						}
					});
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.ui.views.LayersView.LayerSelectionListener#layerSelected(com.nokia.tools.media.utils.layers.ILayer)
	 */
	public void layerSelected(ILayer layer) {

		imagePreviews.get(0).setSelectedLayer(layer);
		imagePreviews.get(0).refreshPreviewImage(timeModel,
				getTimeline().getCurrentTime());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.ui.views.LayersEditorView.LayerSelectionListener#effectSelected(com.nokia.tools.media.utils.layers.ILayerEffect)
	 */
	public void effectSelected(final ILayerEffect e) {
		return;
	}

	/**
	 * synchronizes selection from timeline with selection in views
	 * 
	 * @param activeTimeLineRow2
	 */
	private void synchronizeSelection(final ITimeLineRow row) {
		if (Display.getCurrent() == null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					synchronizeSelection(row);
				};
			});
			return;
		}

		if (row.getSource() instanceof ILayerEffect) {
			EffectControlsEditorPage page = getEffectControlsEditorPage();
			if (page != null) {
				if (page.getEffect() != row.getSource()) {
					page.inputChanged((ILayerEffect) row.getSource());
				}
			}
		} else {
			EffectControlsEditorPage page = getEffectControlsEditorPage();
			if (page != null) {
				if (page.getEffect() != row.getSource()) {
					page.inputChanged(null);
				}
			}
		}

		if (row.getSource() instanceof ILayerEffect) {
			layerSelected(((ILayerEffect) row.getSource()).getParent());
		}
		if (row.getSource() instanceof ILayer) {
			layerSelected((ILayer) row.getSource());
		}
	}

	public void partActivated(IWorkbenchPart part) {

		if (part == this) {
			IWorkbenchPage page = getSite().getWorkbenchWindow()
					.getActivePage();
			if (page != null) {
				try {
					// if view is in page and not visible, show it
					if (page.findView(LayersView.ID) == null) {
						page.showView(LayersView.ID, null,
								IWorkbenchPage.VIEW_VISIBLE);
					}

				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void partBroughtToTop(IWorkbenchPart part) {
		if (part == this) {
			hideComponentsView();
		} else if (!(part instanceof GraphicsEditorPart)
				&& (part instanceof ScreenEditorPart || part
						.getAdapter(ScreenEditorPart.class) != null)) {
			restoreComponentsView();
		}
	}

	public void partDeactivated(IWorkbenchPart part) {

	}

	public void partClosed(IWorkbenchPart part) {
		if (part == ((GraphicsEditorInput) getEditorInput()).getParentEditor()) {
			
			getSite().getPage().closeEditor(this, false);
		}
	}

	public class LayerEffectPropertySheetPage extends
			DisposableTabbedPropertySheetPage implements
			ILayerEffectPropertySheetPage {
		GraphicsEditorPart editor;

		protected List<IPropertyChangeListener> listeners = new ArrayList<IPropertyChangeListener>();

		protected List<ITimeModelChangeListener> timeModelChangedListeners = new ArrayList<ITimeModelChangeListener>();

		public LayerEffectPropertySheetPage(
				ITabbedPropertySheetPageContributor tabbedPropertySheetPageContributor) {
			super(tabbedPropertySheetPageContributor);
		}

		public TimingModel getTimeModel() {
			return timeModel;
		}

		public TimeSpan getTimeSpan() {
			return timeSpan;
		}

		public ITimeLine getTimeLine() {
			return getTimeline();
		}

		public void addPropertyChangedListener(IPropertyChangeListener listener) {
			synchronized (listeners) {
				if (!listeners.contains(listener)) {
					listeners.add(listener);
				}
			}
		}

		public void removePropertyChangedListener(
				IPropertyChangeListener listener) {
			synchronized (listeners) {
				listeners.remove(listener);
			}
		}

		protected void notityPropertyChangedListeners(
				org.eclipse.jface.util.PropertyChangeEvent evt) {
			synchronized (listeners) {
				for (IPropertyChangeListener listener : listeners) {
					listener.propertyChange(evt);
				}
			}
		}

		public void propertyChange(
				org.eclipse.jface.util.PropertyChangeEvent event) {
			notityPropertyChangedListeners(event);
		}

		public void addTimeModelChangedListener(
				ITimeModelChangeListener listener) {
			synchronized (timeModelChangedListeners) {
				if (!timeModelChangedListeners.contains(listener)) {
					timeModelChangedListeners.add(listener);
				}
			}
		}

		public void removeTimeModelChangedListener(
				ITimeModelChangeListener listener) {
			synchronized (timeModelChangedListeners) {
				timeModelChangedListeners.remove(listener);
			}
		}

		protected void notityTimeModelChangedListeners() {
			synchronized (timeModelChangedListeners) {
				for (ITimeModelChangeListener listener : timeModelChangedListeners) {
					listener.timeModelChanged(getTimeModel(), getTimeSpan());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.editor.AbstractMediaEditorPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(
				parent.getChildren()[0], GraphicsEditorPart.HLP_CTX);

		
		GraphicsEditorPart editor = this;

		BaseLayerAction a = new AddBgLayerAction(editor.getActiveImage(),
				editor, true);
		((BaseLayerAction) a).listenSelection();
		actionsToDispose.add(a);
		getActionRegistry().registerAction(a);

		a = new AddLayerAction(editor.getActiveImage(), editor, true);
		((BaseLayerAction) a).listenSelection();
		actionsToDispose.add(a);
		getActionRegistry().registerAction(a);

		a = new RenameLayerAction(editor.getActiveImage(), editor, true);
		((BaseLayerAction) a).listenSelection();
		actionsToDispose.add(a);
		getActionRegistry().registerAction(a);

		a = new DeleteSelectedAction(editor, null, true);
		((BaseLayerAction) a).listenSelection();
		actionsToDispose.add(a);
		getActionRegistry().registerAction(a);

		a = new CustomizeAction(editor.getActiveImage(), editor, null, true);
		((BaseLayerAction) a).listenSelection();
		actionsToDispose.add(a);
		getActionRegistry().registerAction(a);

		a = new ChangeOrderAction(editor, ChangeOrderAction.UP, true);
		((BaseLayerAction) a).listenSelection();
		actionsToDispose.add(a);
		getActionRegistry().registerAction(a);

		a = new ChangeOrderAction(editor, ChangeOrderAction.DOWN, true);
		((BaseLayerAction) a).listenSelection();
		actionsToDispose.add(a);
		getActionRegistry().registerAction(a);

		a = new ClearLayerAction(editor.getActiveImage(), this, true);
		((BaseLayerAction) a).listenSelection();
		actionsToDispose.add(a);
		getActionRegistry().registerAction(a);

		CopyImageAction copy = new CopyImageAction(((IWorkbenchPart) this), null);
		copy.listenSelection();
		actionsToDispose.add(copy);
		getActionRegistry().registerAction(copy);

		PasteImageAction paste = new PasteImageAction(((IWorkbenchPart) this), null);
		paste.listenSelection();
		actionsToDispose.add(paste);
		getActionRegistry().registerAction(paste);


		ITimeLine t = getTimeline();
		if (t != null) {
			ITimeLineRow[] rows = t.getRows();
			if (rows != null) {
				if (rows.length > 0) {
					selectionChanged(new StructuredSelection(rows[0]));

				}
			}
		}

		/*
		 * try to find animated parameters and set timeming model so it will be
		 * visible at start
		 */
		if (getActiveImage() != null
				&& !getActiveImage().isAnimatedFor(TimingModel.RealTime)) {
			for (ILayer l : getActiveImage().getSelectedLayers()) {
				for (ILayerEffect eff : l.getSelectedLayerEffects()) {
					if (eff.isAnimatedFor(TimingModel.Relative)) {
						for (TimeSpan span : TimeSpan.values()) {
							if (eff.isAnimatedFor(span)) {
								try {
									refreshTimeLine(TimingModel.Relative, span,
											getActiveImage());
								} catch (Exception e) {
									e.printStackTrace();
								}
								return;
							}
						}
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.editor.AbstractMediaEditorPart#createPreviewComposite(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createPreviewComposite(Composite parent) {
		
		if (screenAreaCompositeDropTarget != null) {
			screenAreaCompositeDropTarget.dispose();
		}
		screenAreaCompositeDropTarget = new DropTarget(parent, DND.DropAccept);
		IContentData data = sourceContentData;
		IContentData root = data.getRoot();
		com.nokia.tools.screen.core.IScreenFactory factory = (IScreenFactory) root
				.getAdapter(IScreenFactory.class);
		if (factory != null) {
			data = factory.getScreenForData(data, false);
		}
		if (data == null) {
			data = root;
		}
		java.awt.Rectangle screenSize = (java.awt.Rectangle) data
				.getAttribute(ContentAttribute.BOUNDS.name());

		if (screenSize == null) {
			screenSize = new java.awt.Rectangle(0, 0, 354, 416);
			S60WorkspacePlugin
					.error("GraphicsEditorPart: cannot determine screen size from element, using default 354x416");
		}

		ScrolledComposite scrolled = new ScrolledComposite(parent, SWT.V_SCROLL
				| SWT.H_SCROLL);

		Composite whiteBG = new Composite(scrolled, SWT.NONE);
		whiteBG.setLayout(new GridLayout(1, false));
		whiteBG.setBackground(ColorConstants.white);

		Composite black = new Composite(whiteBG, SWT.NONE);
		black.setBackground(ColorConstants.black);
		GridLayout l = new GridLayout();
		l.marginHeight = 1;
		l.marginWidth = 1;
		black.setLayout(l);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.CENTER;
		gd.verticalAlignment = GridData.CENTER;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;

		gd.widthHint = screenSize.width;
		gd.heightHint = screenSize.height;

		black.setLayoutData(gd);

		screenAreaComposite = new Composite(black, SWT.NONE);

		screenAreaComposite.setBackground(ColorConstants.white);
		screenAreaComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		screenAreaComposite.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Pattern oldPat = e.gc.getBackgroundPattern();
				Pattern pat = new Pattern(null, 0, 0, 1, 1,
						ColorConstants.white, ColorConstants.lightGray);
				e.gc.setBackgroundPattern(pat);
				e.gc.fillRectangle(screenAreaComposite.getBounds());
				e.gc.setBackgroundPattern(oldPat);
				pat.dispose();
			}
		});

		// add image preview
		IImagePreviewComposite imagePreview = new IImagePreviewComposite(
				screenAreaComposite, SWT.NONE, getActiveImage());
		java.awt.Rectangle bounds = (java.awt.Rectangle) sourceContentData
				.getAttribute(ContentAttribute.BOUNDS.name());
		if (bounds != null) {
			imagePreview.setBounds(bounds.x, bounds.y, bounds.width,
					bounds.height);
		} else {
			S60WorkspacePlugin.error("Missing layout info for: "
					+ sourceContentData.getName());
		}
		imagePreviews.add(imagePreview);

		// set scrolled properties
		scrolled.setContent(whiteBG);
		scrolled.setExpandHorizontal(true);
		scrolled.setExpandVertical(true);
		scrolled.setMinSize(whiteBG.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		scrolled.getHorizontalBar().setIncrement(DEFAULT_SIZE.width);
		scrolled.getVerticalBar().setIncrement(DEFAULT_SIZE.height);
		scrolled.getHorizontalBar().setPageIncrement(
				2 * scrolled.getHorizontalBar().getIncrement());
		scrolled.getVerticalBar().setPageIncrement(
				2 * scrolled.getVerticalBar().getIncrement());
	}

	public void partOpened(IWorkbenchPart part) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.editor.AbstractMediaEditorPart#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void selectionChanged(IStructuredSelection selection) {
		super.selectionChanged(selection);
		Iterator it = selection.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof ITimeLineRow) {
				// synchronize selection
				synchronizeSelection((ITimeLineRow) o);
			}
		}
	}

	public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
		super.propertyChange(event);
		if (!isDirty) {
			isDirty = true;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					firePropertyChange(GraphicsEditorPart.PROP_DIRTY);
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.editor.AbstractMediaEditorPart#getTimeModelInfo(com.nokia.tools.media.utils.layers.TimingModel)
	 */
	@Override
	protected String getTimeModelInfo(TimingModel model) {
		if (timeModel == TimingModel.RealTime) {
			return EditorMessages.GraphicsEditor_Timeline_RealTime;
		}
		if (timeSpan == TimeSpan.EHour) {
			return EditorMessages.GraphicsEditor_Timeline_RelativeTime_Hour;
		}
		if (timeSpan == TimeSpan.EDay) {
			return EditorMessages.GraphicsEditor_Timeline_RelativeTime_Day;
		}
		if (timeSpan == TimeSpan.EWeek) {
			return EditorMessages.GraphicsEditor_Timeline_RelativeTime_Week;
		}
		if (timeSpan == TimeSpan.EMonth) {
			return EditorMessages.GraphicsEditor_Timeline_RelativeTime_Month;
		}
		return super.getTimeModelInfo(model);
	}

	private void _addAction(IContributionManager m, IAction c,
			boolean separator, boolean dispose) {
		if (c != null && c.isEnabled()) {
			if (separator)
				m.add(new Separator());
			m.add(c);
			if (dispose && c instanceof WorkbenchPartAction)
				actionsToDispose.add((WorkbenchPartAction) c);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.editor.AbstractMediaEditorPart#getFrameAnimationEditingActionFactory()
	 */
	@Override
	protected FrameAnimActionFactory getFrameAnimationEditingActionFactory() {
		return new FrameAnimActionFactory() {

			@Override
			public IContributionManager getPopupMenuContribution(
					ISelectionProvider provider) {
				IContributionManager manager = new ContributionManager() {
					public void update(boolean force) {
					}
				};

				manager.add(new Separator());
				manager.add(new CopyImageAction(provider, null));
				manager.add(new PasteImageAction(provider, getCommandStack(),
						null));
				manager.add(new Separator());
				BrowseForFileAction fileAction = new BrowseForFileAction(
						GraphicsEditorPart.this);
				fileAction.setSelectionProvider(provider);
				manager.add(fileAction);

				_addAction(manager, new SetAnimateTimeAction(provider,
						getCommandStack()), true, false);

				manager.add(new Separator());
				_addAction(manager, new EditImageInSVGEditorAction(provider,
						getCommandStack()), false, true);
				_addAction(manager, new EditImageInBitmapEditorAction(provider,
						getCommandStack()), false, true);
				_addAction(manager, new ConvertAndEditSVGInBitmapEditorAction(
						provider, getCommandStack()), false, true);
				_addAction(manager, new EditInSystemEditorAction(provider,
						getCommandStack()), false, true);

				manager.add(new Separator());
				_addAction(manager, new EditMaskAction(provider,
						getCommandStack()), false, true);
				_addAction(manager, new EditMaskAction2(provider,
						getCommandStack()), false, true);

				_addAction(manager, new RemoveAnimationFrameAction(provider,
						getCommandStack()), true, false);

				return manager;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.media.utils.editor.frameanimation.FrameAnimActionFactory#getCopyFrameAction(org.eclipse.jface.viewers.ISelectionProvider)
			 */
			@Override
			public Action getCopyFrameAction(ISelectionProvider provider) {
				return new CopyImageAction(provider, null);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.media.utils.editor.frameanimation.FrameAnimActionFactory#getPasteFrameAction(org.eclipse.jface.viewers.ISelectionProvider)
			 */
			@Override
			public Action getPasteFrameAction(ISelectionProvider provider) {
				return new PasteImageAction(provider, getCommandStack(), null);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.media.utils.editor.frameanimation.FrameAnimActionFactory#getDistributeAnimationTimeAction(org.eclipse.jface.viewers.ISelectionProvider)
			 */
			@Override
			public Action getDistributeAnimationTimeAction(
					ISelectionProvider provider) {
				return new DistributeAnimateTimeAction(provider,
						getCommandStack());
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.media.utils.editor.frameanimation.FrameAnimActionFactory#getNewFrameAction(org.eclipse.jface.viewers.ISelectionProvider)
			 */
			@Override
			public Action getNewFrameAction(ISelectionProvider provider) {
				return new CreateAnimationFrameAction(provider,
						getCommandStack());
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.media.utils.editor.frameanimation.FrameAnimActionFactory#getRemoveFrameAction(org.eclipse.jface.viewers.ISelectionProvider)
			 */
			@Override
			public Action getRemoveFrameAction(ISelectionProvider provider) {
				return new RemoveAnimationFrameAction(provider,
						getCommandStack());
			}

		};

	}

	/**
	 * Starts the animation. simulates that user clicked on 'play' button
	 */
	public void control_startAnimation() {
		getTimeline().getTimer().setSpeed(
				getTimeline().getDataProvider().getClockTimePerIncrement(),
				getTimeline().getDataProvider().getClockIncrement());
		getTimeline().getTimer().start();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.layers.ILayerActionsHost#deleteSelected(java.lang.Object)
	 */
	public void deleteSelected(Object sel) {
		Object selection = getSelection().getFirstElement();
		IImage image = getActiveImage();
		if (sel != null)
			selection = sel;
		if (selection instanceof ILayer) {
			ILayer layer = (ILayer) selection;
			
			if (layer.isEnabled()) {
				if (image.getSelectedLayerCount() <= 1)
					return; 

			}
			image.removeLayer(layer);
		} else if (selection instanceof ILayerEffect) {
			((ILayerEffect) selection).setSelected(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.layers.ILayerActionsHost#canDeleteSelected(java.lang.Object)
	 */
	public boolean canDeleteSelected(Object sel) {
		IImage image = getActiveImage();
		if (image instanceof IAnimatedImage)
			return false;

		Object selection = getSelection().getFirstElement();
		if (sel != null)
			selection = sel;
		if (selection instanceof ILayer) {
			return image.getLayerCount() > 1;
		} else if (selection instanceof ILayerEffect) {
			return !((ILayerEffect) selection).getName().equals(
					IMediaConstants.APPLY_GRAPHICS);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.layers.ILayerActionsHost#getSelection()
	 */
	public IStructuredSelection getSelection() {
		return (IStructuredSelection) super.getSelection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.layers.ILayerActionsHost#refresh()
	 */
	public void refresh() {
		getTimeline().repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.layers.ILayerActionsHost#getShell()
	 */
	public Shell getShell() {
		return getShell();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.layers.ILayerActionsHost#getWorkbenchPart()
	 */
	public IWorkbenchPart getWorkbenchPart() {
		return this;
	}

	public void addSelectionListener(ISelectionChangedListener l) {
		
		getTimeline().getViewer().addSelectionChangedListener(l);
	}

	public void removeSelectionListener(ISelectionChangedListener l) {
		// delegate to timeline treeviewer
		getTimeline().getViewer().removeSelectionChangedListener(l);
	}

	public IEditorPart getParentEditor() {
		return callerEditor;
	}

	public void selectActiveLayer(ILayer layer) {
		if (layer != null) {
			getTimeline().getViewer().setSelection(
					new StructuredSelection(layer));
		}

	}
	
	@Override
	protected void contributeToCombo(final ToolBar tb) {
		new ToolItem(tb, SWT.SEPARATOR);
		final String selectedMode = getAnimationMode();
		sourceContentData.setAttribute(ThemeTag.ATTR_ANIM_MODE, selectedMode);	
		final Image image = S60WorkspacePlugin.getImageDescriptor(
				"icons/AnimationMode.png").createImage();
		final ToolItem animModeItem = new ToolItem(tb, SWT.DROP_DOWN);
		animModeItem.setImage(image);
		animModeItem.setToolTipText("Set Animation Mode");
		animModeItem.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				image.dispose();
			}
		});

		animModeMenu = new Menu(tb.getShell(), SWT.POP_UP);

		MenuItem animationModeItem = new MenuItem(animModeMenu, SWT.RADIO);

		animationModeItem
				.setText(EditorMessages.GraphicsEditor_AnimationMode_Play);
		animationModeItem
				.setSelection(selectedMode
						.equalsIgnoreCase(EditorMessages.GraphicsEditor_AnimationMode_Play));
		animationModeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MenuItem item = (MenuItem) e.widget;
				if (item.getSelection()) {
					SetAnimationModeAction animAction = new SetAnimationModeAction(
							null, getCommandStack(),
							EditorMessages.GraphicsEditor_AnimationMode_Play,
							animModeMenu);
					animAction.doRun(sourceContentData);
					propertyChange(new org.eclipse.jface.util.PropertyChangeEvent(
							this, "AnimationMode", sourceContentData
									.getAttribute(ThemeTag.ATTR_ANIM_MODE),
							EditorMessages.GraphicsEditor_AnimationMode_Play));
				}
			}
		});
		animationModeItem = new MenuItem(animModeMenu, SWT.RADIO);
		animationModeItem
				.setText(EditorMessages.GraphicsEditor_AnimationMode_Cycle);
		animationModeItem
				.setSelection(selectedMode
						.equalsIgnoreCase(EditorMessages.GraphicsEditor_AnimationMode_Cycle));
		animationModeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MenuItem item = (MenuItem) e.widget;
				if (item.getSelection()) {
					SetAnimationModeAction animAction = new SetAnimationModeAction(
							null, getCommandStack(),
							EditorMessages.GraphicsEditor_AnimationMode_Cycle,
							animModeMenu);
					animAction.doRun(sourceContentData);
					propertyChange(new org.eclipse.jface.util.PropertyChangeEvent(
							this, "AnimationMode", sourceContentData
									.getAttribute(ThemeTag.ATTR_ANIM_MODE),
							EditorMessages.GraphicsEditor_AnimationMode_Cycle));
				}
			}
		});
		animationModeItem = new MenuItem(animModeMenu, SWT.RADIO);
		animationModeItem
				.setText(EditorMessages.GraphicsEditor_AnimationMode_Bounce);
		animationModeItem
				.setSelection(selectedMode
						.equalsIgnoreCase(EditorMessages.GraphicsEditor_AnimationMode_Bounce));
		animationModeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MenuItem item = (MenuItem) e.widget;
				if (item.getSelection()) {
					SetAnimationModeAction animAction = new SetAnimationModeAction(
							null, getCommandStack(),
							EditorMessages.GraphicsEditor_AnimationMode_Bounce,
							animModeMenu);
					animAction.doRun(sourceContentData);
					propertyChange(new org.eclipse.jface.util.PropertyChangeEvent(
							this, "AnimationMode", sourceContentData
									.getAttribute(ThemeTag.ATTR_ANIM_MODE),
							EditorMessages.GraphicsEditor_AnimationMode_Bounce));
				}
			}
		});

		animModeItem.setEnabled(animModeMenu.getItemCount() > 1);

		animModeItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.ARROW) {
					Rectangle rect = animModeItem.getBounds();
					Point pt = new Point(rect.x, rect.y + rect.height);
					pt = tb.toDisplay(pt);
					animModeMenu.setLocation(pt.x, pt.y);
					animModeMenu.setVisible(true);
				}
			}
		});
		
	}
	/**
	 * Set animationMode attribute to ThemeBasicData, in order to write in .tdf file
	 * and same will be used in packaging process
	 */
	private void saveAnimationMode() {
		final ISkinnableEntityAdapter adp = (ISkinnableEntityAdapter) sourceContentData
				.getAdapter(ISkinnableEntityAdapter.class);
		final Map<Object, Object> map = adp.getAttributes();
		final String selectedMode = (sourceContentData
				.getAttribute(ThemeTag.ATTR_ANIM_MODE) != null) ? sourceContentData
				.getAttribute(ThemeTag.ATTR_ANIM_MODE).toString()
				: EditorMessages.GraphicsEditor_AnimationMode_Cycle;
		final String entityType = (String)map.get(ThemeTag.ATTR_ENTITY_TYPE);
		final String type = (String)map.get(ThemeTag.ATTR_TYPE);
		if((entityType !=null && entityType.equalsIgnoreCase(ThemeTag.ELEMENT_BMPANIM))
				|| (type !=null && type.equalsIgnoreCase(ThemeTag.ATTR_BMPANIM))){			
			map.put(ThemeTag.ATTR_ANIM_MODE, selectedMode);
			adp.setAttributes(map);
		}
	}
	/**
	 * Retrives animationMode from ThemeBasicData
	 * @return animationMode
	 */
	private String getAnimationMode(){
		final ISkinnableEntityAdapter adp = (ISkinnableEntityAdapter) sourceContentData.getAdapter(ISkinnableEntityAdapter.class);
		final Map<Object, Object> map = adp.getAttributes();
		return (map.get(ThemeTag.ATTR_ANIM_MODE) != null)? map.get(ThemeTag.ATTR_ANIM_MODE).toString():EditorMessages.GraphicsEditor_AnimationMode_Cycle;
	}

}
