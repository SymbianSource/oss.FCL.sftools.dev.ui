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

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.AlignmentAction;
import org.eclipse.gef.ui.actions.MatchHeightAction;
import org.eclipse.gef.ui.actions.MatchWidthAction;
import org.eclipse.gef.ui.actions.PrintAction;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.internal.ViewIntroAdapterPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.ContentException;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.content.core.IContentService;
import com.nokia.tools.editing.jfc.JFCDiagramAdapter;
import com.nokia.tools.editing.model.EditDiagram;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.figure.ViewportCenterLayout;
import com.nokia.tools.editing.ui.part.DefaultEditPartFactory;
import com.nokia.tools.editing.ui.part.DiagramGraphicalEditPart;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.ThreadUtils;
import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.svg.ColorGroupsStore;
import com.nokia.tools.media.utils.tooltip.FloatingToolbar;
import com.nokia.tools.packaging.IPackager;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.resource.util.IInvokable;
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
import com.nokia.tools.s60.editor.actions.ElevenPieceConvertAction;
import com.nokia.tools.s60.editor.actions.NinePieceConvertAction;
import com.nokia.tools.s60.editor.actions.PasteContentDataAction;
import com.nokia.tools.s60.editor.actions.PasteImageAction;
import com.nokia.tools.s60.editor.actions.SetColorAction;
import com.nokia.tools.s60.editor.actions.SetStretchModeAction;
import com.nokia.tools.s60.editor.actions.ShowInComponentViewAction;
import com.nokia.tools.s60.editor.actions.ShowInResourceViewAction;
import com.nokia.tools.s60.editor.dnd.FileDropAdapter;
import com.nokia.tools.s60.editor.dnd.S60BaseDragListener;
import com.nokia.tools.s60.editor.ui.views.LayersView;
import com.nokia.tools.s60.ide.ContributedActionsResolver;
import com.nokia.tools.s60.ide.IS60IDEConstants;
import com.nokia.tools.s60.ide.PerspectiveUtil;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.ide.actions.OpenGraphicsEditorAction;
import com.nokia.tools.s60.internal.utils.CommandInspector;
import com.nokia.tools.s60.views.menu.GraphicalIconMenuProvider;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.actions.IKeyHandler;
import com.nokia.tools.screen.ui.actions.KeyActionFactory;
import com.nokia.tools.screen.ui.actions.MoveKeyHandler;
import com.nokia.tools.screen.ui.actions.PausePlayingAction;
import com.nokia.tools.screen.ui.actions.PlayAllAction;
import com.nokia.tools.screen.ui.actions.PlaySelectionAction;
import com.nokia.tools.screen.ui.actions.ResizeKeyHandler;
import com.nokia.tools.screen.ui.actions.SetPlayingSpeedAction;
import com.nokia.tools.screen.ui.actions.StopPlayingAction;
import com.nokia.tools.screen.ui.editor.ScreenEditorPart;
import com.nokia.tools.screen.ui.editor.embedded.IEmbeddedEditorContributor;
import com.nokia.tools.screen.ui.editor.embedded.IEmbeddedEditorCustomizer;
import com.nokia.tools.screen.ui.editor.embedded.IEmbeddedEditorPart;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IEmbeddedEditorDescriptor;
import com.nokia.tools.screen.ui.gallery.IGalleryAdapter;
import com.nokia.tools.screen.ui.gallery.IGalleryScreenProvider;
import com.nokia.tools.screen.ui.gallery.IGalleryScreenProvider.IGalleryScreen;
import com.nokia.tools.screen.ui.gef.IDoubleClickListener;
import com.nokia.tools.screen.ui.gef.PaletteManager;
import com.nokia.tools.screen.ui.gef.SComponentGraphicalEditPart;
import com.nokia.tools.screen.ui.gef.ScreenEditPartFactory;
import com.nokia.tools.screen.ui.gef.ZOrderScrollingGraphicalViewer;
import com.nokia.tools.screen.ui.menu.IIconMenuProvider;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.theme.content.ThemeUtil;
import com.nokia.tools.theme.s60.model.S60Theme;
import com.nokia.tools.theme.s60.model.tpi.TPIconConflictEntry;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconManager;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconWrapper;
import com.nokia.tools.ui.dialog.MessageDialogWithTextContent;
import com.nokia.tools.ui.view.BaseContentOutlinePage;

/**
 * This is the main content editor that other views are dependent on.<br/> This
 * editor can host multiple graphical viewers and display them on demand. Also
 * the editor can be reused by calling {@link #showEditorInput(IEditorInput)}.
 */
public class Series60EditorPart
    extends ContentEditorPart
    implements IShowEditorInput, IPartListener, IDoubleClickListener,
    ISetSelectionTarget, IPropertyListener {

	private GalleryAdapter galleryAdapter;

	private GraphicalViewer graphicalViewer;

	private TransferDragSourceListener[] currentDragListeners = null;

	private PageBook pageBook;

	private FloatingToolbar toolbar;
	
	private boolean cancelledNotification = false;

	
	public Series60EditorPart(){
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenEditorPart#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input)
	    throws PartInitException {
		super.init(site, input);
		mediator = createScreenModelMediator();
//		mediator.start();

		galleryAdapter = new GalleryAdapter();

		site.getWorkbenchWindow().getPartService().addPartListener(this);
		showEditorInput(input, true);
		
	}

	private ScreenModelMediator createScreenModelMediator() {
		ScreenModelMediator m = new ScreenModelMediator(this);
		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		try {
			activatePart(part);
			
			if(!cancelledNotification && part == this){
				synchronized(this){
					if(!cancelledNotification){
						Theme activeTheme = ThemeUtil.getCurrentActiveTheme();
						if(activeTheme instanceof S60Theme){
							Map<ThirdPartyIconWrapper, List<TPIconConflictEntry>> conflictingIconList = 
								ThirdPartyIconManager.getConflictingIconList((S60Theme)activeTheme);
							if(!conflictingIconList.isEmpty()){
								boolean confirmed = MessageDialog.openConfirm(part.getSite().getShell(), 
										"Third Party Icons Conflicts", 
										"Third party icon definitions for the current theme has conflicts. Resolve them now?");
		
								if(confirmed){
									if(IDialogConstants.CANCEL_ID == showThirdPartyIconPage())
										cancelledNotification = true;
								}
								else{
									cancelledNotification = true;
								}
							}
						}
					}
				}
			}
			
			
		} catch (RuntimeException e) {
			
			e.printStackTrace();
		}
	}

	private void activatePart(IWorkbenchPart part) {
		if (part == this) {
			IWorkbenchPage page = getSite().getWorkbenchWindow()
			    .getActivePage();
			if (page != null) {

				// activate layers view
				try {
					// if view is on page and not visible, activate
					if (page.findView(LayersView.ID) != null) {
						page.showView(LayersView.ID, null,
						    IWorkbenchPage.VIEW_VISIBLE);
					}
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}

			if (toolbar != null && toolbar.isStayOnTop()) {
				toolbar.show();
			}
		} else {
			// if another editor selected hide tooltip
			if (toolbar != null) {
				if (PerspectiveUtil.getActiveEditorPart() != this
				    || part instanceof ViewIntroAdapterPart) {
					toolbar.hide();
				} else {
					if (toolbar.isStayOnTop()) {
						toolbar.show();
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part == this && toolbar != null) {
			toolbar.hide();
		}
		if(part == this)
			cancelledNotification = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		String error = checkInput();
		if (error != null) {
			createErrorForm(parent, error);
			return;
		}
		IContent activeContent = mediator.getActiveContent();
		if (activeContent == null) {
			createErrorForm(parent,
			    EditorMessages.Editor_Error_ContentNotAvailable_Title, null,
			    NLS.bind(EditorMessages.Editor_Error_ContentNotAvailable_Info,
			        new Object[] { ((IFileEditorInput) getEditorInput())
			            .getFile().getLocation() }));
			return;
		}

		pageBook = new PageBook(parent, SWT.NONE);

		if (toolbar != null) {
			toolbar.dispose();
		}

		toolbar = new FloatingToolbar(null, this, "s60_screen_toolbar",
		    FloatingToolbar.EStyle.VERTICAL_VERTICAL) {

			@Override
			public boolean show(Point location) {
				IPreferenceStore store = S60WorkspacePlugin.getDefault()
				    .getPreferenceStore();
				Boolean showToolbar = store
				    .getBoolean(IS60IDEConstants.PREF_SHOW_EDITOR_TOOLTIP);
				if (showToolbar) {
					return super.show(location);
				}
				return false;
			}
		};
		toolbar.setControl(pageBook);

		if (!ScreenUtil.isPrimaryContent(activeContent)
		    && mediator.getEditor(graphicalViewer) instanceof IEmbeddedEditorPart) {
			IEmbeddedEditorPart embeddedEditor = (IEmbeddedEditorPart) mediator
			    .getEditor(graphicalViewer);
			createEmbeddedViewerControl(embeddedEditor, graphicalViewer);
			showEmbeddedEditor(embeddedEditor, graphicalViewer);
		} else {
			Control control = createViewerControl(graphicalViewer);
			pageBook.showPage(control);
		}
		PlatformUI.getWorkbench().getHelpSystem().setHelp(pageBook,
		    Series60EditorPart.THEME_CONTEXT);
	}

	protected PaletteViewer createPaletteViewer(IContent content) {
		if (content == null) {
			return null;
		}

		PaletteManager paletteManager = new PaletteManager();
		paletteManager.initializePalette();
		IEmbeddedEditorDescriptor descriptor = ExtensionManager
		    .getEmbeddedEditorDescriptorByContentType(content.getType());
		if (descriptor != null) {
			IEmbeddedEditorCustomizer customizer = descriptor
			    .getEmbeddedEditorCustomizer();
			if (customizer != null) {
				customizer.customizePalette(paletteManager, content);
			}
		}
		if (paletteManager.isRequired()) {
			PaletteRoot paletteRoot = paletteManager.getPaletteRoot();

			PaletteViewer paletteViewer = new PaletteViewer();
			paletteViewer.setPaletteRoot(paletteRoot);

			getEditDomain().setPaletteViewer(paletteViewer);
			return paletteViewer;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenEditorPart#createActionRegistry()
	 */
	@Override
	protected ActionRegistry createActionRegistry() {
		return new ActionRegistry() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.gef.ui.actions.ActionRegistry#getAction(java.lang.Object)
			 */
			@Override
			public IAction getAction(Object key) {
				
				if (pageBook != null && !pageBook.isDisposed()
				    && key instanceof String) {
					for (Control child : pageBook.getChildren()) {
						if (child.isVisible()) {
							if (child.getData() instanceof IEmbeddedEditorPart) {
								IAction action = ((IEmbeddedEditorPart) child
								    .getData()).getAction((String) key);
								if (action != null) {
									return action;
								}
							}
						}
					}
				}
				/*if(key.equals("createNew"))
				{
					return new CreateNewAction(instance);
				}	*/			
				return super.getAction(key);
			}
		};
	}

	private void internalAdd(ActionRegistry registry, AbstractAction aa) {
		aa.listenSelection();
		getSelectionActions().add(aa.getId());
		registry.registerAction(aa);
	}

	/**
	 * Creates the default actions that are associated with the editor.
	 */
	protected void createActions() {
		super.createActions();
		ActionRegistry registry = getActionRegistry();
		IAction action;
		IWorkbenchWindow window = PlatformUI.getWorkbench()
		    .getActiveWorkbenchWindow();

		// copy paste
		internalAdd(registry, new CopyImageAction(this, null));
		internalAdd(registry, new PasteImageAction(this, null));

		internalAdd(registry, new PasteContentDataAction(this, null,
		    ClipboardHelper.APPLICATION_CLIPBOARD));

		ActionFactory.IWorkbenchAction actionBookmarks = IDEActionFactory.BOOKMARK
		    .create(window);
		registry.registerAction(actionBookmarks);

		ActionFactory.IWorkbenchAction actionTasks = IDEActionFactory.ADD_TASK
		    .create(window);
		registry.registerAction(actionTasks);

		action = new PrintAction(this);
		registry.registerAction(action);

		IKeyHandler handler = new MoveKeyHandler(this);
		action = KeyActionFactory.CTRL_LEFT.create(this, handler);
		registry.registerAction(action);
		getStackActions().add(action.getId());
		action = KeyActionFactory.CTRL_RIGHT.create(this, handler);
		registry.registerAction(action);
		getStackActions().add(action.getId());
		action = KeyActionFactory.CTRL_UP.create(this, handler);
		registry.registerAction(action);
		getStackActions().add(action.getId());
		action = KeyActionFactory.CTRL_DOWN.create(this, handler);
		registry.registerAction(action);
		getStackActions().add(action.getId());

		handler = new ResizeKeyHandler(this);
		action = KeyActionFactory.CTRL_SHIFT_LEFT.create(this, handler);
		registry.registerAction(action);
		getStackActions().add(action.getId());
		action = KeyActionFactory.CTRL_SHIFT_RIGHT.create(this, handler);
		registry.registerAction(action);
		getStackActions().add(action.getId());
		action = KeyActionFactory.CTRL_SHIFT_UP.create(this, handler);
		registry.registerAction(action);
		getStackActions().add(action.getId());
		action = KeyActionFactory.CTRL_SHIFT_DOWN.create(this, handler);
		registry.registerAction(action);
		getStackActions().add(action.getId());

		action = new ShowInResourceViewAction(this);
		registry.registerAction(action);

		action = new ShowInComponentViewAction(this);
		registry.registerAction(action);

		// Alignment tools
		action = new AlignmentAction((IWorkbenchPart) this,
		    PositionConstants.LEFT);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
		    PositionConstants.RIGHT);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
		    PositionConstants.TOP);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
		    PositionConstants.BOTTOM);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
		    PositionConstants.CENTER);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
		    PositionConstants.MIDDLE);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new MatchWidthAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new MatchHeightAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new PlayAllAction(this);
		registry.registerAction(action);
		getStackActions().add(action.getId());

		action = new PlaySelectionAction(this);
		registry.registerAction(action);
		getStackActions().add(action.getId());

		action = new PausePlayingAction(this);
		registry.registerAction(action);
		getStackActions().add(action.getId());

		action = new StopPlayingAction(this);
		registry.registerAction(action);
		getStackActions().add(action.getId());

		action = new SetPlayingSpeedAction(this);
		registry.registerAction(action);
		getStackActions().add(action.getId());

		internalAdd(registry, new SetStretchModeAction(this,
		    IMediaConstants.STRETCHMODE_STRETCH));

		internalAdd(registry, new SetStretchModeAction(this,
		    IMediaConstants.STRETCHMODE_ASPECT));

		internalAdd(registry, new ConvertAndEditSVGInBitmapEditorAction(this));

		internalAdd(registry, new EditInSystemEditorAction(this));

		internalAdd(registry, new EditImageInBitmapEditorAction(this));

		internalAdd(registry, new EditImageInSVGEditorAction(this));

		internalAdd(registry, new BrowseForFileAction(this));

		internalAdd(registry, new ClearImageEditorAction(this));

		internalAdd(registry, new SetColorAction(this));

		/* masks */

		internalAdd(registry, new EditMaskAction2(this));

		internalAdd(registry, new EditMaskAction(this));

		/* nine-piece related actions */

		internalAdd(registry, new NinePieceConvertAction(this,
		    NinePieceConvertAction.TYPE_CONVERT2SINGLE));

		internalAdd(registry, new NinePieceConvertAction(this,
		    NinePieceConvertAction.TYPE_CONVERT2NINE));
		//11 pic related actions
		
		internalAdd(registry, new ElevenPieceConvertAction(this,
			    ElevenPieceConvertAction.TYPE_CONVERT2SINGLE));

		internalAdd(registry, new ElevenPieceConvertAction(this,
					ElevenPieceConvertAction.TYPE_ELEVEN_PIECE));
		
		AbstractAction aa = new OpenGraphicsEditorAction(this);
		internalAdd(registry, aa);
		getEditorSite().getActionBars().setGlobalActionHandler(
		    OpenGraphicsEditorAction.ID, aa);

		/* add contributed actions */
		ContributedActionsResolver.getInstance().addToRegistry(this, registry);
	}

	/**
	 * Creates the graphical viewer on top of the given EMF model.<br> <b>Note:
	 * the graphical viewer created doesn't have a control yet.</b>
	 * 
	 * @param model the model object.
	 * @return the graphical viewer.
	 */
	protected GraphicalViewer createGraphicalViewer(EditDiagram model) {
		final GraphicalViewer viewer = new ZOrderScrollingGraphicalViewer();
		initializeEditPartViewer(viewer);

		viewer.setEditPartFactory(new ScreenEditPartFactory(
		    DefaultEditPartFactory.TYPE_GRAPHICAL));

		viewer.setContents(new DiagramGraphicalEditPart(model) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.editing.ui.part.DiagramGraphicalEditPart#createFigure()
			 */
			@Override
			protected IFigure createFigure() {
				IFigure figure = super.createFigure();
				figure.setLayoutManager(new ViewportCenterLayout());
				return figure;
			}

		});
		JFCDiagramAdapter adapter = new JFCDiagramAdapter(viewer);
		model.eAdapters().add(adapter);

		
		viewer
		    .addDropTargetListener((TransferDropTargetListener) new FileDropAdapter(
		        viewer, LocalSelectionTransfer.getInstance()));
		viewer
		    .addDropTargetListener((TransferDropTargetListener) new FileDropAdapter(
		        viewer, FileTransfer.getInstance()));

		return viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.editor.BaseEditorPart#createContextMenuProvider()
	 */
	@Override
	protected ContextMenuProvider createContextMenuProvider() {
		// context menu
		ScreenEditorContextMenuProvider provider = new ScreenEditorContextMenuProvider(
		    graphicalViewer);
		for (IEmbeddedEditorDescriptor descriptor : ExtensionManager
		    .getEmbeddedEditorDescriptors()) {
			IEmbeddedEditorContributor contributor = descriptor
			    .getContributor();
			if (contributor != null) {
				try {
					contributor.contributeActions(this);
				} catch (Exception e) {
					S60WorkspacePlugin.error(e);
				}
				provider.addEmbeddedEditorContributor(contributor);
			}
		}
		return provider;
	}

	/**
	 * Creates the control for the graphical viewer.
	 * 
	 * @param viewer the graphical viewer.
	 * @return the control or null if the editor is not visiable yet or has been
	 *         disposed.
	 */
	protected Control createViewerControl(EditPartViewer viewer) {
		if (pageBook == null || pageBook.isDisposed() || viewer == null) {
			return null;
		}

		Composite composite = new Composite(pageBook, SWT.BORDER);
		StackLayout stack = new StackLayout();
		stack.topControl = viewer.createControl(composite);
		composite.setLayout(stack);

		return composite;
	}

	public Control createEmbeddedViewerControl(
	    IEmbeddedEditorPart embeddedEditor, EditPartViewer viewer) {
		if (embeddedEditor == null || pageBook == null) {
			return null;
		}

		if (embeddedEditor.getControl() == null) {
			embeddedEditor.createPartControl(pageBook);
			Control[] children = pageBook.getChildren();
			children[children.length - 1].setData(embeddedEditor);
		}

		embeddedEditor.createViewerControl(viewer);
		return viewer.getControl().getParent();
	}

	private void showEmbeddedEditor(IEmbeddedEditorPart embeddedEditor,
	    EditPartViewer viewer) {
		try {
			if (embeddedEditor.getControl() != null) {
				PaletteViewer paletteViewer = (PaletteViewer) embeddedEditor
				    .getAdapter(PaletteViewer.class);
				if (paletteViewer == null) {
					paletteViewer = createPaletteViewer(mediator
					    .extractContent(viewer));
					embeddedEditor.createPaletteControl(paletteViewer);
				}
				if (paletteViewer != null) {
					getEditDomain().setPaletteRoot(
					    paletteViewer.getPaletteRoot());
					getEditDomain().setPaletteViewer(paletteViewer);
				}
				//widget disposed exception is coming when we remove
				
				if(!pageBook.isDisposed() && !embeddedEditor.getControl().isDisposed())
					pageBook.showPage(embeddedEditor.getControl());
				embeddedEditor.showView(viewer);
			}
		} catch (SWTException e) {
			
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.editor.BaseEditorPart#createKeyHandler()
	 */
	protected KeyHandler createKeyHandler() {
		KeyHandler keyHandler = new KeyHandler();

		keyHandler.put(KeyStroke.getPressed((char) 3, 99, 262144), 
		    getActionRegistry().getAction(ActionFactory.COPY.getId()));

		keyHandler.put(KeyStroke.getPressed((char) 22, 118, 262144), 
		    getActionRegistry().getAction(ActionFactory.PASTE.getId()));

		keyHandler.put(KeyStroke.getPressed((char) 26, 122, 262144), 
		    getActionRegistry().getAction(ActionFactory.UNDO.getId()));

		keyHandler.put(KeyStroke.getPressed((char) 25, 121, 262144), 
		    getActionRegistry().getAction(ActionFactory.REDO.getId()));

		keyHandler.put(KeyActionFactory.CTRL_LEFT.getKeyStroke(),
		    getActionRegistry().getAction(KeyActionFactory.CTRL_LEFT_ID));
		keyHandler.put(KeyActionFactory.CTRL_RIGHT.getKeyStroke(),
		    getActionRegistry().getAction(KeyActionFactory.CTRL_RIGHT_ID));
		keyHandler.put(KeyActionFactory.CTRL_UP.getKeyStroke(),
		    getActionRegistry().getAction(KeyActionFactory.CTRL_UP_ID));
		keyHandler.put(KeyActionFactory.CTRL_DOWN.getKeyStroke(),
		    getActionRegistry().getAction(KeyActionFactory.CTRL_DOWN_ID));
		keyHandler.put(KeyActionFactory.CTRL_SHIFT_LEFT.getKeyStroke(),
		    getActionRegistry().getAction(KeyActionFactory.CTRL_SHIFT_LEFT_ID));
		keyHandler
		    .put(KeyActionFactory.CTRL_SHIFT_RIGHT.getKeyStroke(),
		        getActionRegistry().getAction(
		            KeyActionFactory.CTRL_SHIFT_RIGHT_ID));
		keyHandler.put(KeyActionFactory.CTRL_SHIFT_UP.getKeyStroke(),
		    getActionRegistry().getAction(KeyActionFactory.CTRL_SHIFT_UP_ID));
		keyHandler.put(KeyActionFactory.CTRL_SHIFT_DOWN.getKeyStroke(),
		    getActionRegistry().getAction(KeyActionFactory.CTRL_SHIFT_DOWN_ID));

		return keyHandler;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (pageBook != null) {
			pageBook.setFocus();
		} else {
			super.setFocus();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.editor.BaseEditorPart#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection)
			    .getFirstElement();
			if (element instanceof GraphicalViewer) {
				GraphicalViewer viewer = (GraphicalViewer) element;
				if (viewer.getContents() == null
				    || viewer.getContents().getModel() == null) {
					
					return;
				}
				bringToFront(false, viewer);
				// sets the focus only when the whole viewer is selected
				setFocus();
			}
		}
		super.selectionChanged(event);
	}

	/**
	 * 
	 */
	private void removeDragListeners() {
		if (graphicalViewer == null) {
			return;
		}
		if (currentDragListeners != null) {
			for (int i = 0; i < currentDragListeners.length; i++) {
				graphicalViewer
				    .removeDragSourceListener(currentDragListeners[i]);
			}
		}
	}

	/**
	 * 
	 */
	private void addDragListeners() {
		currentDragListeners = new TransferDragSourceListener[2];
		TransferDragSourceListener localTransferListener = new S60BaseDragListener(
		    LocalSelectionTransfer.getInstance(), graphicalViewer);
		TransferDragSourceListener fileTransferListener = new S60BaseDragListener(
		    FileTransfer.getInstance(), graphicalViewer);
		graphicalViewer.addDragSourceListener(localTransferListener);
		graphicalViewer.addDragSourceListener((fileTransferListener));
		currentDragListeners[0] = localTransferListener;
		currentDragListeners[1] = fileTransferListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.IGotoMarker#gotoMarker(org.eclipse.core.resources.IMarker)
	 */
	public void gotoMarker(IMarker marker) {
		String id = marker.getAttribute(ContentAttribute.ID.name(), null);
		final IContentData elemData;
		if (id != null) {
			elemData = mediator.getPrimaryContent().findById(id);
		} else {
			String name = marker.getAttribute(ContentAttribute.NAME.name(),
			    null);
			elemData = mediator.getPrimaryContent().findByName(name);
		}
		if (elemData != null) {
			String screen = marker.getAttribute(ScreenEditorPart.SCREEN, null);
		
			if (elemData != null) {
				if (screen != null)
					bringScreenToTop(screen);
			}

			if (!selectAndRevealData(elemData, screen)) {
				Display.getCurrent().asyncExec(new Runnable() {

					public void run() {
						((ShowInResourceViewAction) getActionRegistry()
						    .getAction(ShowInResourceViewAction.ID))
						    .doRun(elemData);
					}
				});
			}
			try {
				EditPart sel = ScreenUtil.findPartForMarkerAttrs(
				    getEditPartViewer().getContents(), marker.getAttributes());
				if (null != sel)
					getEditPartViewer().select(sel);
				else
					getEditPartViewer().deselectAll();
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return;
		} else {
			
			String screen = marker.getAttribute(ScreenEditorPart.SCREEN, null);
			if (screen != null) {
				bringScreenToTop(screen);
				return;
			}
		}
	}

	/**
	 * @return false if there is no preview screen and item can not be selectede
	 */
	private boolean selectAndRevealData(IContentData data, String preferedScreen) {
		return selectAndRevealData(new IContentData[] { data }, preferedScreen);
	}

	/**
	 * @return false if there is no preview screen and item can not be selectede
	 */
	private boolean selectAndRevealData(IContentData[] data,
	    String preferedScreen) {
		IContentData scrData = null;
		if (null == preferedScreen) {

			// if screen not defined check on current screen
			List<EditPart> sels = ScreenUtil.findPartsForData(
			    getEditPartViewer().getContents(), data);
			if (!sels.isEmpty()) {
				getEditPartViewer().setSelection(new StructuredSelection(sels));
				return true;
			}

			// if screen not defined get one that can display element with id
			int i = 0;
			while (preferedScreen == null && i < data.length) {

				scrData = ScreenUtil.getScreenForData(data[i], true);
				if (null != scrData) {
					break;
				}
				i++;
			}

			if (null == scrData) {
				getEditPartViewer().deselectAll();
				return false;
			}
		}
		if (scrData != null) {
			bringScreenToTop(scrData);
		}
		List<EditPart> sels = ScreenUtil.findPartsForData(getEditPartViewer()
		    .getContents(), data);
		if (sels.size() > 0)
			getEditPartViewer().setSelection(new StructuredSelection(sels));
		else
			getEditPartViewer().deselectAll();
		return true;
	}

	private void activateViewer() {
		IWorkbenchPage page = getSite().getWorkbenchWindow().getActivePage();
		IWorkbenchPart activePart = page.getActivePart();
		if (activePart == this) {
			// we need to reactivate the editor in order to have
			// the workbench
			// selection service hooked into the graphical
			// viewer immediately,
			for (IViewReference ref : page.getViewReferences()) {
				IViewPart part = ref.getView(false);
				if (part != null) {
					ISelectionProvider provider = ((IViewSite) part.getSite())
					    .getSelectionProvider();
					if (provider != null) {
						// clears the selection to prevent from re-selecting and
						// thus causing screen switching again
						((IViewSite) part.getSite()).getSelectionProvider()
						    .setSelection(StructuredSelection.EMPTY);
					}
					page.activate(part);
					break;
				}
			}
		}
		
		if (page.getActiveEditor() != null) {
			page.activate(this);
		}
	}

	/**
	 * Brings the given viewer to the front and hides the other viewers.
	 * 
	 * @param viewer the viewer to be shown.
	 */
	private void bringToFront(boolean isNew, GraphicalViewer viewer) {
		if (graphicalViewer == viewer) {
			return;
		}
		removeDragListeners();
		if (graphicalViewer != null) {
			getSelectionSynchronizer().removeViewer(graphicalViewer);
		}
		removeFigureListeners();

		graphicalViewer = viewer;

		
		EditPart contents = graphicalViewer.getContents();
		if (contents != null) {
			if (contents.getModel() instanceof EditDiagram) {
				EList<EditObject> list = ((EditDiagram) contents.getModel())
				    .getEditObjects();
				if (!list.isEmpty()) {
					Object part = graphicalViewer.getEditPartRegistry().get(
					    list.get(0));
					if (part instanceof SComponentGraphicalEditPart) {
						((SComponentGraphicalEditPart) part).removeContour();
					}
				}
			}
		}
		addFigureListeners();

		IEditorPart editor = mediator.getEditor(viewer);

		if (editor instanceof IEmbeddedEditorPart) {
			showEmbeddedEditor((IEmbeddedEditorPart) editor, viewer);
		} else if (viewer != null && viewer.getControl() != null) {
			pageBook.showPage(viewer.getControl().getParent());
		}

		if (graphicalViewer != null) {
			IContentData data = mediator.getScreenByViewer(graphicalViewer);
			if (data != null) {
				setPartName(getProject().getName() + " - " + data.getName());
			}
			if (ScreenUtil.isPrimaryContent(data)) {
				addDragListeners();
			}
		}

		if (!(editor instanceof IEmbeddedEditorPart)) {
			// resets the palette tool to select
			PaletteViewer palette = getEditDomain().getPaletteViewer();
			if (palette != null) {
				palette.setActiveTool(null);
			}
		}

		initializeViewers();

		fireContentRootChanged();

		if (!isNew) {
			activateViewer();
		}

		final PlayAllAction action = (PlayAllAction) getActionRegistry()
		    .getAction(PlayAllAction.ID);
		if (action != null && action.isEnabled()) {
			// queues after update has completed
			getSite().getShell().getDisplay().asyncExec(new Runnable() {

				public void run() {
					action.setPlayOnce(true);
					action.run();
					action.setPlayOnce(false);
				}
			});
		}
	}

	private void bringScreenToTop(String screenName) {
		for (IGalleryScreen screen : mediator.getGalleryScreens()) {
			if (screen.getName().equalsIgnoreCase(screenName)) {
				bringScreenToTop(screen.getData());
			}
		}
	}

	private void bringScreenToTop(IContentData scrData) {
		
		if (mediator.getActiveContent() != mediator.getPrimaryContent()) {
			IProject project = getProject();
			IFile file = project.getFile(project.getName() + ".tdf");
			if (!file.isLinked()) {
				IFolder folder = project.getFolder(project.getName());
				if (folder.exists()) {
					file = folder.getFile(project.getName() + ".tdf");
				}
			}
			if (file == null || !file.exists()) {
				return;
			}
			showEditorInput(new FileEditorInput(file), false);
		}
		IProgressMonitor monitor = getProgressMonitor();
		monitor.beginTask(EditorMessages.Editor_Task_CreatingGraphicalViewer,
		    ScreenModelMediator.TOTAL_WORK);
		try {
			final GraphicalViewer viewer = mediator.getViewer(scrData, false,
			    monitor);
			if (viewer == graphicalViewer) {
				return;
			}

			

			Series60EditorPart.this.getSite().getShell().getDisplay().syncExec(
			    new Runnable() {

				    public void run() {
					    bringToFront(false, viewer);
				    }
			    });
		} catch (IllegalStateException e) {
		} finally {
			monitor.done();
		}

		if (graphicalViewer != null) {
			IContentData data = mediator.getScreenByViewer(graphicalViewer);
			if (data != null) {
				setPartName(getProject().getName() + " - " + data.getName());
			}

			
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.editor.BaseGEFEditorPart#getEditPartViewer()
	 */
	@Override
	public EditPartViewer getEditPartViewer() {
		return graphicalViewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenEditorPart#dispose()
	 */
	@Override
	public void dispose() {
		try {
			getSite().getWorkbenchWindow().getPartService().removePartListener(
			    this);
		} catch (Throwable e) {
			S60WorkspacePlugin.error(e);
		}
		try {
			if (graphicalViewer != null) {
				getSelectionSynchronizer().removeViewer(graphicalViewer);
			}
		} catch (Throwable e) {
			S60WorkspacePlugin.error(e);
		}
		try {
			removeFigureListeners();
		} catch (Throwable e) {
			S60WorkspacePlugin.error(e);
		}

		try {
			if (mediator != null) {
				mediator.dispose();
			}
		} catch (Throwable e) {
			S60WorkspacePlugin.error(e);
		}

		ColorGroupsStore.disposeColorGroupsForProject(getProject());

		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenEditorPart#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (mediator == null) {
			return super.getAdapter(adapter);
		}
		if (adapter == IPackager.class) {
			if (mediator.getPrimaryContent() != null) {
				return mediator.getPrimaryContent().getAdapter(IPackager.class);
			}
		}

		if (adapter == IContent.class) {
			return mediator.getPrimaryContent();
		}

		if (adapter == IGalleryScreenProvider.class) {
			if (mediator.getActiveContent() == null) {
				return null;
			}
			return mediator;
		}
		if (adapter == IGalleryAdapter.class) {
			if (mediator.getActiveContent() == null) {
				return null;
			}
			return galleryAdapter;
		}
		if (adapter == IContentAdapter.class
		    || adapter == IContentService.class) {
			return this;
		}

		if (adapter == IIconMenuProvider.class) {
			return new GraphicalIconMenuProvider();
		}

		Object object = super.getAdapter(adapter);
		if (object != null) {
			return object;
		}

		
		IEditorPart editor = mediator.getEditor(getEditPartViewer());
		if (editor != null && editor != this) {
			return editor.getAdapter(adapter);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.editor.BaseEditorPart#stackChanged(org.eclipse.gef.commands.CommandStackEvent)
	 */
	@Override
	public void stackChanged(CommandStackEvent event) {
		super.stackChanged(event);

		if (event.getCommand() != null) {
			if ((event.getDetail() & (CommandStack.POST_EXECUTE
			    | CommandStack.POST_REDO | CommandStack.POST_UNDO)) != 0) {
				updateSelectionActions(getSelectionActions());

		
				Command command = event.getCommand();
				if(CommandInspector.fireContentModified(command)){
					String[] affectedElements = CommandInspector
					    .getAffectedElements(command);
					fireContentModified(affectedElements);
				}
			}
		}
	}

	private void updateSelectionActions(List<String> actionIds) {
		ActionRegistry registry = getActionRegistry();
		Iterator iter = actionIds.iterator();
		SelectionChangedEvent evt = new SelectionChangedEvent(
		    getEditPartViewer(), getEditPartViewer().getSelection());
		while (iter.hasNext()) {
			IAction action = registry.getAction(iter.next());
			if (action instanceof AbstractAction) {
				AbstractAction aa = (AbstractAction) action;
				if (aa.isListeningSelection())
					aa.selectionChanged(evt);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenEditorPart#disposeContent()
	 */
	@Override
	protected void disposeContent() {
		if (mediator != null) {
			mediator.disposeOldContents();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object,
	 *      int)
	 */
	public void propertyChanged(Object source, int propertyId) {
		firePropertyChange(propertyId);
	}

	public IEmbeddedEditorPart createEmbeddedEditor(
	    final IEmbeddedEditorDescriptor desc, final IEditorInput editorInput) {
		if (desc == null) {
			return null;
		}

		IEmbeddedEditorPart embeddedEditor = (IEmbeddedEditorPart) ThreadUtils
		    .syncDisplayExec(new IInvokable.Adapter() {

			    public Object invoke() {
				    return desc.createEditorPart(Series60EditorPart.this,
				        editorInput);
			    }
		    });
		embeddedEditor.addPropertyListener(this);
		IContentService adapter = (IContentService) embeddedEditor
		    .getAdapter(IContentService.class);
		if (adapter != null) {
			adapter.addContentListener(mediator);
		}
		return embeddedEditor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenEditorPart#superSetInput(org.eclipse.ui.IEditorInput)
	 */
	@Override
	protected void superSetInput(IEditorInput input) {
		super.superSetInput(input);
		setPartName(null != getProject() ? getProject().getName() : "");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenEditorPart#showEditorInput(org.eclipse.ui.IEditorInput,
	 *      boolean)
	 */
	public void showEditorInput(final IEditorInput editorInput,
	    final boolean isNew) {
		if (!isNew && getEditorInput().equals(editorInput)) {
			return;
		}
		try {
			new ProgressMonitorDialog(getSite().getShell()).run(!isNew, false,
			    new IRunnableWithProgress() {

				    public void run(final IProgressMonitor monitor)
				        throws InvocationTargetException, InterruptedException {
					    beginProgressMonitorProgress(editorInput, monitor);
					    final GraphicalViewer viewer = mediator.getViewer(
					        editorInput, monitor);
					    boolean refreshGallery = viewer != graphicalViewer;
					    if (refreshGallery) {
						    mediator.clearGalleryScreens();
					    }
					    if (viewer == null) {
						    try {
							    mediator.initialize(editorInput, monitor);
						    } catch (final Exception e) {
							    processException(e);
							    return;
						    }
						    setEditorInputWhileInProgressMonitor(editorInput);
					    }
					    monitor.worked(ScreenModelMediator.OTHER_WORK * 3 / 5);

					    refreshElements(isNew, monitor, viewer);
					    if (refreshGallery) {
						    refreshGallery();
					    }
				    }

			    });
		} catch (Exception e) {
			e.printStackTrace();
			S60WorkspacePlugin.error(e);
		}
	}

	private void beginProgressMonitorProgress(final IEditorInput editorInput,
	    final IProgressMonitor monitor) {
		monitor.beginTask(MessageFormat.format(
		    EditorMessages.Editor_LoadingTheme, editorInput.getName()),
		    ScreenModelMediator.TOTAL_WORK);
	}

	private void refreshElements(final boolean isNew,
	    final IProgressMonitor monitor, final GraphicalViewer viewer) {
		getSite().getShell().getDisplay().syncExec(new Runnable() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				if (viewer == null) {
					refreshEditor(isNew, monitor);
				} else {
					viewer.deselectAll();
					bringToFront(isNew, viewer);
				}
			}
		});
	}

	private void setEditorInputWhileInProgressMonitor(
	    final IEditorInput editorInput) {
		getSite().getShell().getDisplay().syncExec(new Runnable() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				setInput(editorInput);
			}
		});
	}

	private void processException(final Exception e) {
		S60WorkspacePlugin.error(e);
		getSite().getShell().getDisplay().syncExec(new Runnable() {

			public void run() {
				MessageDialogWithTextContent.openError(getSite().getShell(),
				    EditorMessages.Error_Editor_Initialize_Contents,
				    EditorMessages.Error_Editor_Initialize_Contents, e);
			}
		});
	}

	public List<IContentData> getSelectedData() {
		List<IContentData> data = new ArrayList<IContentData>();
		if (graphicalViewer != null) {
			for (Object part : graphicalViewer.getSelectedEditParts()) {
				IScreenElement element = JEMUtil.getScreenElement(part);
				if (element != null) {
					data.add(element.getData());
				}
			}
		}
		return data;
	}

	protected void reloadContent() {
		try {
			new ProgressMonitorDialog(getSite().getShell()).run(false, false,
			    new IRunnableWithProgress() {

				    /*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
					 */
				    public void run(IProgressMonitor monitor)
				        throws InvocationTargetException, InterruptedException {
					    monitor.beginTask(MessageFormat.format(
					        EditorMessages.Editor_ReloadTheme, getEditorInput()
					            .getName()), ScreenModelMediator.TOTAL_WORK);

					    refreshEditor(false, monitor);
					    refreshGallery();
					    monitor.done();
				    }
			    });
		} catch (Exception e) {
			S60WorkspacePlugin.error(e);
		}
	}

	/**
	 * based on the content data rebuilds the screen
	 */
	protected void refreshEditor(boolean isNew, IProgressMonitor monitor) {
		IContentData currentScreen = mediator
		    .getScreenByViewer(graphicalViewer);
		List<IContentData> selectedData = getSelectedData();

		removeFigureListeners();
		mediator.disposeViewers();

		try {
			mediator.rebuildModel(monitor);
			GraphicalViewer viewer = null;
			if (currentScreen != null) {
				for (IGalleryScreen screen : mediator.getGalleryScreens()) {
					if (screen.getName().equals(currentScreen.getName())) {
						viewer = mediator.getViewer(screen.getData(), false,
						    monitor);
						break;
					}
				}
			}
			if (viewer == null) {
				viewer = mediator.getDefaultViewer(monitor);
			}
			if (viewer == null) {
				MessageDialog.openError(getSite().getShell(),
				    EditorMessages.Editor_Error_ScreenNotAvailable_Title,
				    EditorMessages.Editor_Error_ScreenNotAvailable_Message);
				return;
			}

			bringToFront(isNew, viewer);

			if (viewer != null) {
				EditPart root = viewer.getContents();

				for (IContentData selected : selectedData) {
					EditPart part = ScreenUtil.findPartForData(root, selected);
					if (part != null) {
						viewer.select(part);
					} else if (selected.getId() != null) {
						for (Object obj : viewer.getEditPartRegistry().values()) {
							IScreenElement element = JEMUtil
							    .getScreenElement(obj);
							if (element != null
							    && selected.getId().equals(
							        element.getData().getId())) {
								viewer.select((EditPart) obj);
								break;
							}
						}
					}
				}
			}
		} catch (ContentException e) {
			S60WorkspacePlugin.error(e);
			MessageDialog.openError(getSite().getShell(),
			    EditorMessages.Error_Editor_No_Screens,
			    EditorMessages.Error_Editor_No_Screens);
			return;
		}
	}

	protected void refreshViewer(GraphicalViewer viewer) {
		bringToFront(false, viewer);
		refreshGallery();
	}

	/**
	 * Re-generates the gallery.
	 */
	protected void refreshGallery() {
		galleryAdapter.notifyGalleryChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenEditorPart#createContentOutlinePage(org.eclipse.gef.ui.parts.TreeViewer)
	 */
	@Override
	protected BaseContentOutlinePage createContentOutlinePage(TreeViewer viewer) {
		return new Series60ContentOutlinePage(viewer);
	}

	protected ScreenModelMediator getMediator() {
		return mediator;
	}


	/**
	 * Global gallery adapter used for notifying gallery changed events.
	 *
	 */
	class GalleryAdapter
	    implements IGalleryAdapter {

		private Set<IGalleryListener> listeners = new HashSet<IGalleryListener>(
		    1);

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.s60.views.IGalleryAdapter#addGalleryListener(com.nokia.tools.s60.views.IGalleryAdapter.IGalleryListener)
		 */
		public void addGalleryListener(IGalleryListener listener) {
			listeners.add(listener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.s60.views.IGalleryAdapter#removeGalleryListener(com.nokia.tools.s60.views.IGalleryAdapter.IGalleryListener)
		 */
		public void removeGalleryListener(IGalleryListener listener) {
			listeners.remove(listener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.s60.views.IGalleryAdapter#notifiyGalleryChanged()
		 */
		public void notifyGalleryChanged() {
			for (final IGalleryListener listener : listeners) {
				getSite().getShell().getDisplay().asyncExec(new Runnable() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see java.lang.Runnable#run()
					 */
					public void run() {
						listener.galleryChanged(mediator);
					}
				});
			}
		}
	}

	public void doubleClick(final EditPart editPart) {

		String ids[] = { OpenGraphicsEditorAction.ID,
		    EditImageInBitmapEditorAction.ID, EditImageInSVGEditorAction.ID,
		    SetColorAction.ID };
		for (String a : ids) {
			IAction action = getActionRegistry().getAction(a);
			if (action != null && action.isEnabled()) {
				action.run();
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.ISetSelectionTarget#selectReveal(org.eclipse.jface.viewers.ISelection)
	 */
	public void selectReveal(ISelection selection) {
		if (!selection.isEmpty()) {
			StructuredSelection select = (StructuredSelection) selection;
			if (select.getFirstElement() instanceof IContentData) {
				List<Object> data = new ArrayList<Object>();
				for (Iterator iter = select.iterator(); iter.hasNext();) {
					Object obj = iter.next();
					if (obj instanceof IContentData) {
						data.add(obj);
					}
				}

				if (!selectAndRevealData(data.toArray(new IContentData[0]),
				    null))
					throw new RuntimeException(
					    EditorMessages.Editor_Error_NoPreviewScreen);
			}
		} else {
			getEditPartViewer().deselectAll();
		}
	}

	public void fireRootContentChanged(IContent content) {
		if (contentService != null) {
			contentService.fireRootContentChanged(content);
		}
	}

	private int showThirdPartyIconPage() {
		String linkAddress = "com.nokia.tools.theme.s60.ui.preferences.ThirdPartyIconsPrefPage";
		PreferenceDialog prefdlg = PreferencesUtil.createPreferenceDialogOn(
				Display.getCurrent().getActiveShell(), linkAddress,
				new String[] { linkAddress }, null);

		int openStatus = prefdlg.open();
		prefdlg.close();
		return openStatus;
	}

}