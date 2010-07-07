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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.content.core.IContentStructureAdapter;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.core.TypedAdapter;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.s60.editor.EditorMessages;
import com.nokia.tools.s60.editor.ExternalEditorSupport;
import com.nokia.tools.s60.editor.actions.AbstractAction;
import com.nokia.tools.s60.editor.actions.BrowseForFileAction;
import com.nokia.tools.s60.editor.actions.ClearImageEditorAction;
import com.nokia.tools.s60.editor.actions.ConvertAndEditSVGInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.CopyContentDataAction;
import com.nokia.tools.s60.editor.actions.CopyImageAction;
import com.nokia.tools.s60.editor.actions.EditImageInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.EditImageInSVGEditorAction;
import com.nokia.tools.s60.editor.actions.EditInSystemEditorAction;
import com.nokia.tools.s60.editor.actions.EditMaskAction;
import com.nokia.tools.s60.editor.actions.EditMaskAction2;
import com.nokia.tools.s60.editor.actions.EditSoundInSoundEditorAction;
import com.nokia.tools.s60.editor.actions.ElevenPieceConvertAction;
import com.nokia.tools.s60.editor.actions.NinePieceConvertAction;
import com.nokia.tools.s60.editor.actions.PasteContentDataAction;
import com.nokia.tools.s60.editor.actions.PasteImageAction;
import com.nokia.tools.s60.editor.actions.SetColorAction;
import com.nokia.tools.s60.editor.actions.SetStretchModeAction;
import com.nokia.tools.s60.editor.actions.ThreePieceConvertAction;
import com.nokia.tools.s60.editor.actions.undo.SafeRedoAction;
import com.nokia.tools.s60.editor.actions.undo.SafeUndoAction;
import com.nokia.tools.s60.editor.dnd.S60BaseDragListener;
import com.nokia.tools.s60.editor.dnd.S60BaseDropListener;
import com.nokia.tools.s60.editor.ui.dialogs.WarningMessageDialogs;
import com.nokia.tools.s60.ide.IS60IDEConstants;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.ide.actions.AddBookmarkViewAction;
import com.nokia.tools.s60.ide.actions.AddTaskViewAction;
import com.nokia.tools.s60.ide.actions.OpenGraphicsEditorAction;
import com.nokia.tools.s60.ide.actions.ToggleTextAction;
import com.nokia.tools.s60.internal.utils.HideableMenuManager;
import com.nokia.tools.screen.core.ICategoryAdapter;
import com.nokia.tools.screen.core.IScreenAdapter;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.ISkinnableContentDataAdapter;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.menu.IIconMenuProvider;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.screen.ui.utils.SimpleSelectionProvider;
import com.nokia.tools.theme.content.ContentAdapter;
import com.nokia.tools.theme.content.ThemeScreenReferData;
import com.nokia.tools.theme.core.IContentLabelProvider;
import com.nokia.tools.ui.widgets.ImageLabel;

public class IconViewPage extends Page implements ISelectionListener,
		ISelectionProvider, KeyListener {

	public static final String ICON_CONTEXT = "com.nokia.tools.s60.ide" + '.' + "icon_context"; 

	private static final Dimension DEFAULT_SIZE = new Dimension(40, 40);

	private static final int MIN_SIZE = 10;

	private ScrolledComposite scrollableComposite;

	private Composite container;

	private Composite stackContainer;

	private Canvas selectionPaintCanvas;

	/*
	 * contains root of last selection, setted in repopulateContents();
	 */
	protected IContentData currentCategory;

	protected List<IContentData> selectedItems = new ArrayList<IContentData>();

	protected List<IContentData> lastSelectedItems;

	protected IStructuredSelection lastSelection;

	protected String partName;

	protected PageBookView parent;

	/* toolbar actions */
	protected OpenGraphicsEditorAction animateAction;

	protected ClearImageEditorAction clearAction;

	protected CopyImageAction copyAction;

	protected PasteImageAction pasteAction;

	protected IconViewDropDownToolbarAction externalToolsDropDown;

	private ToggleTextAction toggleTextAction;

	protected IconViewSelectAllAction selectAllAction;

	protected BrowseForFileAction browseForFileAction;

	private boolean suppressSelectEvent;

	private boolean isNoPreviewMessageBoxShowing;

	private List<WorkbenchPartAction> actionsToDispose = new ArrayList<WorkbenchPartAction>();

	private Dimension resolution;

	private IEditorPart sourceEditor;

	private boolean mouseRightClicked = false;

	private boolean itemReSelected = false;

	private boolean ctrl_active;

	private boolean shift_active;

	private IContentData selectionStart;

	protected MouseEvent lastMouseDown;

	protected MouseEvent lastMouseMoveEvent;

	protected Image containerImage;

	private Set<EditObject> resources = new HashSet<EditObject>();

	protected ControlMoveAdapter controlMoveAdapter;

	// synchronize with selection in Editor flag
	private boolean synchronize;

	private Listener selectionModeKeyListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.type == SWT.KeyDown) {
				if ((event.keyCode & SWT.CONTROL) != 0
						|| (event.stateMask & SWT.CONTROL) != 0) {
					ctrl_active = true;
				} else {
					ctrl_active = false;
				}
				if ((event.keyCode & SWT.SHIFT) != 0
						|| (event.stateMask & SWT.SHIFT) != 0) {
					shift_active = true;
				} else {
					shift_active = false;
				}
			}
			if (event.type == SWT.KeyUp) {
				if (event.keyCode == SWT.CONTROL
						|| event.stateMask != SWT.CONTROL) {
					ctrl_active = false;
				}
				if (event.keyCode == SWT.SHIFT || event.stateMask != SWT.SHIFT) {
					shift_active = false;
				}
			}
		}
	};

	protected enum SelectionMode {
		NORMAL, CTRL, SHIFT, CTRL_SHIFT
	};

	public IconViewPage(IEditorPart sourceEd) {
		this.sourceEditor = sourceEd;
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createControl(Composite parent) {
		scrollableComposite = new ScrolledComposite(parent, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);
		scrollableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true, 1, 1));
		GridLayout gl = new GridLayout();
		gl.marginTop = gl.marginBottom = gl.marginLeft = gl.marginRight = gl.marginWidth = gl.marginHeight = 0;
		scrollableComposite.setLayout(gl);

		stackContainer = new Composite(scrollableComposite, SWT.NULL);
		stackContainer.setLayout(new StackLayout());
		controlMoveAdapter = new ControlMoveAdapter();
		stackContainer.addControlListener(controlMoveAdapter);

		selectionPaintCanvas = new Canvas(stackContainer, SWT.NO_BACKGROUND);
		selectionPaintCanvas.addKeyListener(this);
		selectionPaintCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (lastMouseDown != null && lastMouseDown.button == 1
						&& lastMouseMoveEvent != null) {

					Point origin = new Point(scrollableComposite.getOrigin().x,
							scrollableComposite.getOrigin().y);
					if (lastMouseMoveEvent.x < origin.x) {
						origin.x = Math.max(0, lastMouseMoveEvent.x);
					}
					if (lastMouseMoveEvent.y < origin.y) {
						origin.y = Math.max(0, lastMouseMoveEvent.y);
					}
					if (lastMouseMoveEvent.x > (origin.x + scrollableComposite
							.getBounds().width)) {
						origin.x = Math
								.min(
										stackContainer.getClientArea().width
												- scrollableComposite
														.getClientArea().width,
										lastMouseMoveEvent.x
												- scrollableComposite
														.getBounds().width);
					}
					if (lastMouseMoveEvent.y > (origin.y + scrollableComposite
							.getBounds().height)) {
						origin.y = Math
								.min(
										stackContainer.getClientArea().height
												- scrollableComposite
														.getClientArea().height,
										lastMouseMoveEvent.y
												- scrollableComposite
														.getBounds().height);
					}

					GC gc = e.gc;
					if (containerImage != null) {
						gc.drawImage(containerImage, 0, 0);
					}

					int lmex = Math.min(origin.x
							+ scrollableComposite.getClientArea().width - 1,
							Math.max(origin.x, lastMouseMoveEvent.x));
					int lmey = Math.min(origin.y
							+ scrollableComposite.getClientArea().height - 1,
							Math.max(origin.y, lastMouseMoveEvent.y));

					int lmdx = Math.min(
							stackContainer.getClientArea().width - 1, Math.max(
									0, lastMouseDown.x));
					int lmdy = Math.min(
							stackContainer.getClientArea().height - 1, Math
									.max(0, lastMouseDown.y));

					int x = Math.min(lmdx, lmex);
					int y = Math.min(lmdy, lmey);
					int width = Math.abs(lmex - lmdx);
					int height = Math.abs(lmey - lmdy);

					gc.setForeground(ColorConstants.black);
					gc.setLineWidth(1);
					gc.setLineStyle(SWT.LINE_SOLID);
					gc.drawRectangle(x, y, width, height);

					if (!scrollableComposite.getOrigin().equals(origin)) {
						scrollableComposite.setOrigin(origin);
					}
				}
			}
		});

		container = new Composite(stackContainer, SWT.NULL);
		container.addKeyListener(this);

		((StackLayout) stackContainer.getLayout()).topControl = container;

		MouseMoveListener mouseMoveListener = new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				mouseMoved(e);
			}
		};

		container.addMouseMoveListener(mouseMoveListener);
		selectionPaintCanvas.addMouseMoveListener(mouseMoveListener);

		MouseAdapter mouseAdapter = new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				mouseButtonDown(e);
			}

			@Override
			public void mouseUp(MouseEvent e) {
				mouseButtonUp(e);
			}
		};

		container.addMouseListener(mouseAdapter);
		selectionPaintCanvas.addMouseListener(mouseAdapter);

		scrollableComposite.setContent(stackContainer);
		scrollableComposite.setExpandHorizontal(true);
		scrollableComposite.setExpandVertical(true);
		scrollableComposite.getHorizontalBar().setIncrement(DEFAULT_SIZE.width);
		scrollableComposite.getVerticalBar().setIncrement(DEFAULT_SIZE.height);
		scrollableComposite.getHorizontalBar().setPageIncrement(
				2 * scrollableComposite.getHorizontalBar().getIncrement());
		scrollableComposite.getVerticalBar().setPageIncrement(
				2 * scrollableComposite.getVerticalBar().getIncrement());

		scrollableComposite.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				scrollToVisible();
			}
		});

		final GridLayout layout = new GridLayout();
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 2;
		container.setLayout(layout);

		container.addListener(SWT.Paint, new Listener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				int totalWidth = 0;
				Control[] children = container.getChildren();
				for (Control child : children) {
					totalWidth += child.getSize().x;
				}

				int aveWidth = children.length == 0 ? DEFAULT_SIZE.width
						: totalWidth / children.length;
				int numColumns = Math
						.min(
								children.length,
								Math
										.max(
												1,
												(scrollableComposite
														.getClientArea().width - (2 * layout.marginWidth))
														/ (aveWidth + layout.horizontalSpacing)));
				if (layout.numColumns != numColumns) {
					layout.numColumns = numColumns;
					scrollableComposite.setMinSize(container.computeSize(
							SWT.DEFAULT, SWT.DEFAULT));
					container.layout();
					scrollableComposite.layout();
					scrollToVisible();
				}
			}
		});

		initializeIconViewActions();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(container,
				IconView.ICON_CONTEXT);

		/* ctx menu for whole view */
		MenuManager mmgr = new MenuManager();
		mmgr.setRemoveAllWhenShown(true);
		mmgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				/* EXTENDED copy/paste */
				ISelectionProvider simple = new SimpleSelectionProvider(
						currentCategory);
				{
					HideableMenuManager cpSubmenu = new HideableMenuManager(
							"Extended Copy/Paste");
					AbstractAction cg = new CopyContentDataAction(simple, null);
					cg.setText(cg.getText() + " in Category");
					cpSubmenu.add(cg);
					cg = new PasteContentDataAction(simple, IconViewPage.this
							.getCommandStack(), null);
					cpSubmenu.add(cg);
					if (cpSubmenu.isEnabled()) {
						manager.add(cpSubmenu);
					}
				}
			}
		});
		container.setMenu(mmgr.createContextMenu(container));

		Display.getDefault().addFilter(SWT.KeyDown, selectionModeKeyListener);
		Display.getDefault().addFilter(SWT.KeyUp, selectionModeKeyListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.IPage#getControl()
	 */
	@Override
	public Control getControl() {
		return scrollableComposite;
	}

	public Composite getContainer() {
		return container;
	}

	public IContentData getCurrentCategory() {
		return currentCategory;
	}

	protected void initializePage(IStructuredSelection ssel) {
		final Object entity = ssel.getFirstElement();

		if (entity instanceof IContentData) {

			IContentData _currentCategory = ((IContentData) entity).getParent();
			com.nokia.tools.platform.core.Display _resolution = (com.nokia.tools.platform.core.Display) _currentCategory
					.getRoot().getAttribute(ContentAttribute.DISPLAY.name());

			initializePage(ssel, _currentCategory, new Dimension(_resolution
					.getWidth(), _resolution.getHeight()));
			repopulateContents(ssel);
		}
	}

	protected MouseEvent convertMouseEventCoords(MouseEvent e, Control control) {
		Event event = new Event();
		event.button = e.button;
		event.data = e.data;
		event.display = e.display;
		event.stateMask = e.stateMask;
		event.time = e.time;
		event.widget = e.widget;
		if (control != null
				&& (control != container || control != selectionPaintCanvas)) {
			event.x = e.x + control.getLocation().x;
			event.y = e.y + control.getLocation().y;
		} else {
			event.x = e.x;
			event.y = e.y;
		}
		return new MouseEvent(event);
	}

	protected void mouseButtonDown(MouseEvent e) {
		if ((e.stateMask & SWT.CONTROL) == 0) {
			ctrl_active = false;
		} else {
			ctrl_active = true;
		}
		if ((e.stateMask & SWT.SHIFT) == 0) {
			shift_active = false;
		} else {
			shift_active = true;
		}

		if (!(e.widget instanceof Control)) {
			return;
		}

		if (e.widget != container && e.widget != selectionPaintCanvas) {
			e = convertMouseEventCoords(e, (Control) e.widget);
		}

		if (e.button == 1) {
			lastMouseDown = e;
		}
	}

	protected boolean mouseButtonUp(MouseEvent e) {
		if (e.button == 1) {
			StackLayout layout = ((StackLayout) stackContainer.getLayout());
			if ((shift_active || ctrl_active) && lastMouseDown != null
					&& lastMouseDown.button == 1) {
				if (layout.topControl == selectionPaintCanvas) {
					int lmex = Math.min(stackContainer.getClientArea().width,
							Math.max(scrollableComposite.getOrigin().x,
									lastMouseMoveEvent.x));
					int lmey = Math.min(stackContainer.getClientArea().height,
							Math.max(scrollableComposite.getOrigin().y,
									lastMouseMoveEvent.y));

					int lmdx = Math.min(stackContainer.getClientArea().width,
							Math.max(0, lastMouseDown.x));
					int lmdy = Math.min(stackContainer.getClientArea().height,
							Math.max(0, lastMouseDown.y));

					int x = Math.min(lmdx, lmex);
					int y = Math.min(lmdy, lmey);
					int width = Math.abs(lmex - lmdx);
					int height = Math.abs(lmey - lmdy);

					List<IContentData> selectedElements = new ArrayList<IContentData>();

					if (ctrl_active) {
						selectedElements.addAll(selectedItems);
					}

					Rectangle selectionRectangle = new Rectangle(x, y, width,
							height);
					Control[] children = container.getChildren();
					for (int i = 0; i < children.length; i++) {
						Control child = children[i];
						Rectangle childBounds = child.getBounds();
						Rectangle intersect = selectionRectangle
								.intersection(childBounds);
						if (intersect.width > 0 && intersect.height > 0) {
							IContentData data = (IContentData) child.getData();
							if (selectedElements.contains(data)) {
								selectedElements.remove(data);
							}
							selectedElements.add(data);
							selectionStart = data;
						}
					}

					setSelection(new StructuredSelection(selectedElements));

					switchTopStackControl(container);

					lastMouseDown = null;
					return true;
				}
			}
			lastMouseDown = null;
		}

		return false;
	}

	protected void mouseMoved(MouseEvent e) {
		if (!(e.widget instanceof Control)) {
			return;
		}

		if (e.widget != container && e.widget != selectionPaintCanvas) {
			e = convertMouseEventCoords(e, (Control) e.widget);
		}

		lastMouseMoveEvent = e;

		if ((shift_active || ctrl_active) && lastMouseDown != null
				&& lastMouseDown.button == 1) {
			switchTopStackControl(selectionPaintCanvas);
			selectionPaintCanvas.redraw();
		} else {
			switchTopStackControl(container);
		}
	}

	protected void switchTopStackControl(Control control) {
		final StackLayout layout = ((StackLayout) stackContainer.getLayout());
		if (layout.topControl != control) {
			layout.topControl = control;
			stackContainer.layout();

			if (control != selectionPaintCanvas) {
				if (containerImage != null) {
					containerImage.dispose();
					containerImage = null;
				}
			} else {
				if (containerImage != null) {
					containerImage.dispose();
					containerImage = null;
				}

				containerImage = new Image(null, selectionPaintCanvas
						.getBounds().width,
						selectionPaintCanvas.getBounds().height);

				GC containerImageGC = new GC(containerImage);
				try {
					containerImageGC.setBackground(container.getBackground());
					containerImageGC.fillRectangle(0, 0, containerImage
							.getBounds().width,
							containerImage.getBounds().height);

					Rectangle rect = new Rectangle(scrollableComposite
							.getOrigin().x, scrollableComposite.getOrigin().y,
							scrollableComposite.getClientArea().width,
							scrollableComposite.getClientArea().height);
					controlMoveAdapter.lastRectangle = rect;
					controlMoveAdapter.painted.clear();

					Image visibleAreaImg = new Image(null, rect.width,
							rect.height);
					try {
						GC containerGC = new GC(container);
						try {
							containerGC
									.copyArea(visibleAreaImg, rect.x, rect.y);
						} finally {
							containerGC.dispose();
						}
						containerImageGC.drawImage(visibleAreaImg, rect.x,
								rect.y);
					} finally {
						visibleAreaImg.dispose();
					}
				} finally {
					containerImageGC.dispose();
				}
			}
			stackContainer.redraw();
		}
	}

	protected void initializePage(final IStructuredSelection ssel,
			final IContentData _currentCategory, final Dimension _resolution) {
		// run this in sync mode to get the part name update properly
		currentCategory = _currentCategory;
		resolution = _resolution;
		partName = _currentCategory == null ? null : _currentCategory.getName();
	}

	/**
	 * Returns the items to be shown in the components view
	 * 
	 * Invoked when a task/component/component group is selected on resources view.
	 * @return list of elements 
	 */
	protected List<IContentData> getItems() {
		List<IContentData> childrenList = new ArrayList<IContentData>();
		IContentData[] children = null;
		IContentStructureAdapter adapter = (IContentStructureAdapter) (currentCategory == null ? null
				: currentCategory.getAdapter(IContentStructureAdapter.class));
		if (currentCategory != null) {
			if (adapter == null) {
				children = currentCategory.getChildren();
			} else {
				children = adapter.getChildren();
			}
		}
		if (children != null) {
			for (IContentData data : children) {
				/*if (children.length == 3) {
					System.out.println("");
				}*/
				ISkinnableEntityAdapter skAdapter1 = (ISkinnableEntityAdapter) data
						.getAdapter(ISkinnableEntityAdapter.class);
				
				/*
				   On clicking the task over the resource view, we need to
                   select the right children and add to childrenlist. Note that
                   during this addition, we should not add nine piece or 11 piece elements.
                   If these pieces are added, then they show up as individual elements in components view.
				*/
				/*if (null != skAdapter1 && (skAdapter1.supportsNinePiece() || skAdapter1.supportsElevenPiece()
					 || skAdapter1.supportsThreePiece())) {
				*/	
					if (null != skAdapter1 && (skAdapter1.supportsMultiPiece())) {
					
					childrenList.add(data);
					continue;
				}
				adapter = (IContentStructureAdapter) data
						.getAdapter(IContentStructureAdapter.class);
				if (adapter != null) {
					IContentData[] items = adapter.getChildren();
					if (items.length > 0) {
						for (IContentData subData : items) {
							/*boolean supportsThreePiece = false;
							boolean supportsNinePiece = false;
							boolean supportsElevenPiece = false;*/
							
							//System.out.println(subData.getName());
							
							ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) subData
									.getAdapter(ISkinnableEntityAdapter.class);
							
							/*if (skAdapter != null
								&& skAdapter.supportsThreePiece()) {
							supportsThreePiece = true;
						}

							if (skAdapter != null
									&& skAdapter.supportsNinePiece()) {
								supportsNinePiece = true;
							}
							
							if (skAdapter != null
									&& skAdapter.supportsElevenPiece()) {
								supportsElevenPiece = true;
							}*/

							adapter = (IContentStructureAdapter) subData
									.getAdapter(IContentStructureAdapter.class);
							//if (adapter != null && !supportsNinePiece && !supportsElevenPiece && !supportsThreePiece) {
							if (adapter != null && !skAdapter.supportsMultiPiece()) {
								IContentData[] subItems = adapter.getChildren();
								if (subItems.length > 0) {
									for (IContentData subItem : subItems) {
										childrenList.add(subItem);
									}
									continue;
								}
							}
							childrenList.add(subData);
						}
						continue;
					}
				}
				childrenList.add(data);
			}
		}
		return childrenList;
	}

	protected void repopulateContents(final IStructuredSelection ssel) {
		// keeps the last selection, so we can refresh the entire category when
		// some children have been added or removed
		lastSelection = ssel;
		for (EditObject resource : resources) {
			ComponentAdapter adapter = (ComponentAdapter) EcoreUtil
					.getExistingAdapter(resource, ComponentAdapter.class);
			resource.eAdapters().remove(adapter);
		}
		resources.clear();

		if (container == null || container.isDisposed()) {
			return;
		}
		clearArea();
		Set<IContentData> selection = new HashSet<IContentData>();
		if (ssel != null) {
			for (Iterator iter = ssel.iterator(); iter.hasNext();) {
				Object obj = iter.next();
				if (obj instanceof IContentData) {
					selection.add((IContentData) obj);
				}
			}
		}

		List<Object> newSelection = new ArrayList<Object>();

		// populate all leafs of category
		try {
			List<IContentData> childrenList = getItems();
			for (final IContentData child : childrenList) {
				final ImageLabel control = new ImageLabel(container, SWT.NONE);
				control.setUnselectedBackground(container.getBackground());
				control.setSelectedBackground(container.getBackground());
				control.setData(child);
				control.addKeyListener(IconViewPage.this);
				EditObject resource = (EditObject) child
						.getAdapter(EditObject.class);
				resource.eAdapters().add(new ComponentAdapter(control));
				resources.add(resource);
				IContentData parent = child.getParent();
				if (parent != null) {
					EditObject parentResource = (EditObject) parent
							.getAdapter(EditObject.class);
					if (parentResource != null
							&& EcoreUtil.getExistingAdapter(parentResource,
									ComponentAdapter.class) == null) {
						parentResource.eAdapters().add(
								new ComponentAdapter(null));
						resources.add(parentResource);
					}
				}
				if (toggleTextAction != null) {
					control
							.setLines(toggleTextAction.isChecked() ? IS60IDEConstants.IMAGE_LABEL_TEXT_LINES
									: 0);
				}
				ISkinnableEntityAdapter ska = (ISkinnableEntityAdapter) child
						.getAdapter(ISkinnableEntityAdapter.class);
				control.setModified(ska != null && ska.isSkinned());
				ISkinnableContentDataAdapter scda = (ISkinnableContentDataAdapter) child
						.getAdapter(ISkinnableContentDataAdapter.class);
				if (scda != null) {
					control.setModified(scda.isElementSkinned());
					control.redraw();
				}

				if (selection.contains(child)) {
					newSelection.add(child);
					control.setSelected(true);
				}
				mouseRightClicked = false;

				// look if we can get right image, if not,
				// get default
				control.setImageDescriptor(getImageDescriptor(child));
				control.setText(child.getName());

				IconTooltip tooltip = new IconTooltip(findScreenData(child),
						this, getCommandStack());
				control.setData(tooltip.getClass().getName(), tooltip);
				if (tooltip.getContributionsCount(false) == 0) {
					// no tooltip contribution 
					// so use standard tooltip
					control.setToolTipText(child.getName());
				} else {
					tooltip.setControl(control);
				}

				
				control.addSelectionListener(new ImageLabel.SelectedListener() {
					public void selected(EventObject e) {
						if (!selectedItems
								.contains(((ImageLabel) e.getSource())
										.getData())) {
							((ImageLabel) e.getSource()).setSelected(false);
						}
					}
				});
				

				control.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent e) {
						mouseButtonDown(e);

						if (e.button == 3) {
							mouseRightClicked = true;
						}

						
						final IconTooltip tooltip = (IconTooltip) control
								.getData(IconTooltip.class.getName());

						if (tooltip != null) {
							if (e.button == 3) {
								tooltip.hide();
							}
							tooltip.disable();
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									if (tooltip != null) {
										tooltip.enable();
									}
								}
							});
						}
					}

					@Override
					public void mouseUp(MouseEvent e) {
						if (mouseButtonUp(e)) {
							return;
						}
						if (e.button == 1) {
							if (ctrl_active && shift_active) {
								updateSelection(child, SelectionMode.CTRL_SHIFT);
							} else if (shift_active) {
								updateSelection(child, SelectionMode.SHIFT);
							} else if (ctrl_active) {
								updateSelection(child, SelectionMode.CTRL);
							} else {
								selectionStart = child;
								selfSetSelection(new StructuredSelection(child));
							}
						} else if (e.button == 3) {
							if (selectedItems == null
									|| !selectedItems.contains(child)) {
								selectionStart = child;
								updateSelection(child, SelectionMode.NORMAL);
							}
						}
						mouseRightClicked = false;
					}

					@Override
					public void mouseDoubleClick(MouseEvent e) {
						if (e.count == 2) {
							handleDoubleClick(child);
						}
					}
				});

				MouseMoveListener mouseMoveListener = new MouseMoveListener() {
					public void mouseMove(MouseEvent e) {
						mouseMoved(e);
					}
				};

				control.addMouseMoveListener(mouseMoveListener);

				MenuManager menuMgr = new MenuManager("#PopupMenu"); 
				menuMgr.setRemoveAllWhenShown(true);
				menuMgr.addMenuListener(new IMenuListener() {
					public void menuAboutToShow(IMenuManager manager) {
						IIconMenuProvider imp = (IIconMenuProvider) sourceEditor
								.getAdapter(IIconMenuProvider.class);
						if (imp != null) {
							imp.fillIconContextMenu(manager,
									IconViewPage.this.parent, "components",
									getCommandStack(), getSite()
											.getActionBars());
						}
					}
				});

				Menu menu = menuMgr.createContextMenu(control);
				control.setMenu(menu);

				// drag and drop support
				addDragDropSupport(control, child);
			}

			if (container != null && !container.isDisposed()) {
				container.layout();
				scrollableComposite.setMinSize(container.computeSize(
						SWT.DEFAULT, SWT.DEFAULT));
				container.redraw();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add flags about synchronize with editor
		if (ssel.size() > 0 && ssel.toArray()[ssel.size() - 1] == Boolean.FALSE)
			newSelection.add(Boolean.FALSE);
		setSelection(new StructuredSelection(newSelection));
	}

	private void addDragDropSupport(final Control visualElement,
			final IContentData element) {

		Transfer[] transfers = new Transfer[] {
				LocalSelectionTransfer.getInstance(),
				FileTransfer.getInstance() };

		final DragSource source = new DragSource(visualElement, DND.DROP_COPY);
		source.setTransfer(transfers);
		source.addDragListener(new IconViewDragListener(FileTransfer
				.getInstance(), IconViewPage.this, getCommandStack()));
		source.addDragListener(new IconViewDragListener(LocalSelectionTransfer
				.getInstance(), IconViewPage.this, getCommandStack()));

		
		DropTargetListener dropListener = new S60BaseDropListener(null,
				getCommandStack()) {

			private Color background;

			private Color selectedBackground;

			private Color unselectedBackground;

			protected void highlightDragOver(Control control) {
				if (background == null) {
					background = control.getBackground();
				}
				if (control instanceof ImageLabel) {
					if (selectedBackground == null)
						selectedBackground = ((ImageLabel) control)
								.getSelectedBackground();
					if (unselectedBackground == null)
						unselectedBackground = ((ImageLabel) control)
								.getUnselectedBackground();
					((ImageLabel) control).setSelectedBackground(Display
							.getDefault().getSystemColor(
									IS60IDEConstants.DRAG_HIGHLIGHT_COLOR));
					((ImageLabel) control).setUnselectedBackground(Display
							.getDefault().getSystemColor(
									IS60IDEConstants.DRAG_HIGHLIGHT_COLOR));
					control.redraw();
				}
			}

			protected void removeHighlight(Control control) {
				if (background != null)
					control.setBackground(background);

				if (control instanceof ImageLabel) {
					if (selectedBackground != null)
						((ImageLabel) control)
								.setSelectedBackground(selectedBackground);
					if (unselectedBackground != null)
						((ImageLabel) control)
								.setUnselectedBackground(unselectedBackground);
					control.redraw();
				}
			}

			@Override
			protected ISkinnableEntityAdapter getSkinnableEntityAdapter(
					DropTargetEvent event) {
				return (ISkinnableEntityAdapter) element
						.getAdapter(ISkinnableEntityAdapter.class);
			}

			@Override
			protected EditPart getSourceEditPart(DropTargetEvent event) {
				return null;
			}

			@Override
			protected IWorkbenchPart getWorkbenchPart() {
				return parent;
			}

			@Override
			protected ISelectionProvider getSelectionProviderForGenericCommand(
					DropTargetEvent event) {
				return ExternalEditorSupport.getSelectionProvider(element);
			}

			@Override
			public boolean isEnabled(DropTargetEvent event) {
				if (shift_active || ctrl_active) {
					return false;
				}
				return super.isEnabled(event);
			}
		};

		DropTarget target = new DropTarget(visualElement, DND.DROP_COPY
				| DND.DROP_DEFAULT | DND.DROP_MOVE);
		target.setTransfer(transfers);
		target.addDropListener(dropListener);
	}

	protected void handleDoubleClick(IContentData data) {
		String ids[] = { OpenGraphicsEditorAction.ID,
				EditImageInBitmapEditorAction.ID,
				EditImageInSVGEditorAction.ID, EditSoundInSoundEditorAction.ID };
		for (String a : ids) {
			IAction action = getSite().getActionBars()
					.getGlobalActionHandler(a);
			if (action != null && action.isEnabled()) {
				action.run();
				return;
			}
		}
		
		IAction sca = new SetColorAction(parent);
		if (sca.isEnabled()) {
			sca.run();
			return;
		}
		
		sca = new BrowseForFileAction(parent);
		if (sca.isEnabled())
			sca.run();
	}

	/**
	 * adds action to manager if enabled and add separator if not null.
	 * 
	 * @param manager
	 * @param action
	 * @param spr
	 * @return NULL or separator, if was not used
	 */
	private Separator _internalAdd(IMenuManager manager, IAction action,
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

	private IContentData findModelItem(Object data) {
		if (data instanceof IContentData) {
			return (IContentData) data;
		}
		if (data instanceof IScreenElement) {
			return ((IScreenElement) data).getData();
		}

		List<IContentData> modelItems = new ArrayList<IContentData>();
		IContentAdapter contentAdapter = (IContentAdapter) sourceEditor
				.getAdapter(IContentAdapter.class);
		if (contentAdapter != null) {
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
		}

		if (modelItems.size() > 1) {
			// System.out.println("more model items found");
			return modelItems.get(0);
		} else if (modelItems.size() == 1) {
			return modelItems.get(0);
		} else {
			// get data from new resource tree,
			if (data instanceof EditPart) {
				EditPart ep = (EditPart) data;
				if (ep.getChildren().size() > 0) {
					ep = (EditPart) ep.getChildren().get(0);
				}
				Object untypedModel = ep.getModel();
				if (untypedModel instanceof EditObject) {
					ContentAdapter contAdapt = null;
					for (Object ca : ((EditObject) untypedModel).eAdapters()) {
						if (ca instanceof ContentAdapter) {
							contAdapt = (ContentAdapter) ca;
							break;
						}
					}
					if (contAdapt == null) {
						return null;
					}
					return contAdapt.getData();
				}
			}
			return null;
		}
	}

	/**
	 * Called from resource view to set icon-set to be displayed
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!listenOnSelection || part instanceof IconView) {
			return;
		}

		//  selections from resource viewer
		// if (part instanceof ResourceViewPart || part == null) {
		// if (selection instanceof IStructuredSelection && !(part instanceof
		// IconView)) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (sel.size() == 0) {
				return;
			}

			// convert edit parts to content data
			// filters out the duplicates
			Set<Object> set = new HashSet<Object>(sel.size());
			for (Object object : sel.toArray()) {
				IContentData data = null;
				if (object instanceof EditPart
						|| object instanceof IScreenElement) {
					data = findModelItem(object);
				} else if (object instanceof IContentData) {
					data = (IContentData) object;
				}
				if (data != null) {
					ISkinnableEntityAdapter ca = (ISkinnableEntityAdapter) data
							.getAdapter(ISkinnableEntityAdapter.class);
					if (ca != null) {
						// cares only about the skinnable entities
						set.add(data);
					}
				}
			}

			if (set.isEmpty()) {
				//  clears the view
				initializePage(StructuredSelection.EMPTY, null, null);
				repopulateContents(StructuredSelection.EMPTY);
				return;
			}
			List<Object> cdSel = new ArrayList<Object>(set.size());
			for (Object obj : set) {
				cdSel.add(obj);
			}

			// selection raised from editor,
			// editor
			if (part == sourceEditor) {
				cdSel.add(Boolean.FALSE);
			}

			sel = new StructuredSelection(cdSel);

			List<Object> newSelection = new ArrayList<Object>();

			if (ctrl_active || shift_active) {
				// preserve selection
				// add non-visible elements from current component view
				// selection
				List<IContentData> nonVisuals = new ArrayList<IContentData>();
				for (int i = 0; selectedItems != null
						&& i < selectedItems.size(); i++) {
					IContentData data = selectedItems.get(i);
					if (!isVisibleOnScreen(data)) {
						nonVisuals.add(data);
					}
				}

				List<Object> _selection = new ArrayList<Object>();
				_selection.addAll(Arrays.asList(sel.toArray()));

				if (selectedItems != null) {
					newSelection.addAll(selectedItems);
				}

				// add added elements
				for (int i = 0; i < _selection.size(); i++) {
					Object obj = _selection.get(i);
					if (!newSelection.contains(obj) || obj instanceof Boolean) {
						newSelection.add(obj);
					}
				}

				// remove removed elements
				for (int i = 0; selectedItems != null
						&& i < selectedItems.size(); i++) {
					Object obj = selectedItems.get(i);
					if (!_selection.contains(obj)) {
						// preserve non-visible
						if (!nonVisuals.contains(obj)) {
							newSelection.remove(obj);
						}
					}
				}
			} else {
				newSelection = sel.toList();
			}

			final StructuredSelection newSel = new StructuredSelection(
					newSelection);

			if (sel.size() == 0
					|| (sel.size() > 0 && sel.getFirstElement() instanceof IContentData)) {
				IContentData tmpData = (IContentData) (sel.getFirstElement());
				if (tmpData != null
						&& (tmpData.getParent() instanceof IContent || tmpData
								.getAdapter(IScreenAdapter.class) != null)
						|| tmpData instanceof ThemeScreenReferData) {
					return;
				}
				IContentAdapter adapter = (IContentAdapter) sourceEditor
						.getAdapter(IContentAdapter.class);
				IContent content = ScreenUtil.getPrimaryContent(adapter
						.getContents());
				if (tmpData != null && !(tmpData.getRoot() instanceof IContent)) {
					// removed from the content
					tmpData = content.findById(tmpData.getId());
				}
				final IContentData newCategory = sel.size() == 0
						|| tmpData == null ? null : tmpData.getParent();
				com.nokia.tools.platform.core.Display newDisplay = (com.nokia.tools.platform.core.Display) content
						.getAttribute(ContentAttribute.DISPLAY.name());
				final Dimension newResolution = newDisplay == null ? null
						: new Dimension(newDisplay.getWidth(), newDisplay
								.getHeight());

				if (currentCategory != newCategory
						|| (newResolution != null && !newResolution
								.equals(resolution)) || currentCategory == null
						|| getItems().size() != container.getChildren().length) {
					initializePage(newSel, newCategory, newResolution);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							repopulateContents(newSel);
						}
					});

										return;
				}
			}
			// always sets the selection because the selection on the main
			// editor might have changed
			setSelection(newSel);
		}
		

	}

	/**
	 * repopulates contents
	 */
	public void refresh() {
		if (!scrollableComposite.isDisposed()) {
			if (selectedItems != null)
				initializePage(new StructuredSelection(selectedItems));
		}
	}

	public void clearArea() {
		refreshIconViewActionsState();
		clearArea(container);
		getSite().getActionBars().getToolBarManager().update(true);
	}

	/**
	 * Clears the composite area (disposes all it's children)
	 */
	private void clearArea(final Composite comp) {
		if (Display.getCurrent() == null) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					clearArea(comp);
				}
			});
		} else {
			if (comp == null || comp.isDisposed()) {
				return;
			}
			Control[] controls = comp.getChildren();
			for (int i = 0; i < controls.length; i++) {
				controls[i].dispose();
			}
			// comp.layout();
			scrollableComposite.setMinSize(comp.computeSize(SWT.DEFAULT,
					SWT.DEFAULT));

			if (partName == null) {
				partName = ViewMessages.IconsView_partNameDefault;
			}
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		Control[] selected = getSelectedControls();
		if (selected.length > 0) {
			selected[0].setFocus();
		} else if (container != null && !container.isDisposed()) {
			container.setFocus();
		}
	}

	private Control[] getSelectedControls() {
		if (container == null || container.isDisposed()) {
			return new Control[0];
		}
		List<Control> toRet = new ArrayList<Control>();
		for (Control child : container.getChildren()) {
			if (!child.isDisposed() && selectedItems != null
					&& selectedItems.contains(child.getData())) {
				toRet.add(child);
			}
		}

		return toRet.toArray(new Control[toRet.size()]);
	}

	private IContentData[] findScreenData(List<IContentData> dataList) {
		List<IContentData> toRet = new ArrayList<IContentData>();
		for (IContentData data : dataList) {
			IContentData newData = findScreenData(data);
			if (newData != null) {
				toRet.add(newData);
			}
		}
		return toRet.toArray(new IContentData[toRet.size()]);
	}

	/**
	 * @param data
	 * @return
	 */
	private IContentData findScreenData(IContentData data) {

		
		if (true)
			return data;

		ISkinnableEntityAdapter ska = (ISkinnableEntityAdapter) data
				.getAdapter(ISkinnableEntityAdapter.class);
		if (ska != null) {
			if (ska.isColour()) {
				return data;
			}
		}

		if (sourceEditor != null) {
			EditPartViewer viewer = (EditPartViewer) sourceEditor
					.getAdapter(EditPartViewer.class);
			if (viewer != null) {
				EditPart sel = ScreenUtil.findPartForData(viewer.getContents(),
						data);

				IScreenElement sec = JEMUtil.getScreenElement(sel);
				if (sec != null) {
					return sec.getData();
				}
			}

			EditObject instance = (EditObject) data
					.getAdapter(EditObject.class);
			if (instance != null) {
				IScreenElement elem = JEMUtil.getScreenElement(instance);
				if (elem != null) {
					return elem.getData();
				}
			}
		}
		return data;
	}

	private boolean isVisibleOnScreen(IContentData data) {

		if (sourceEditor != null) {
			EditPartViewer viewer = (EditPartViewer) sourceEditor
					.getAdapter(EditPartViewer.class);
			if (viewer != null) {
				EditPart sel = ScreenUtil.findPartForData(viewer.getContents(),
						data);
				return sel != null;
			}
		}
		return false;
	}

	public ISelection getSelection() {
		if (lastSelectedItems == selectedItems) {
			return lastSelection;
		}
		if (selectedItems != null) {
			IContentData[] selectedData = findScreenData(selectedItems);
			if (selectedData.length > 0) {
				lastSelection = new StructuredSelection(selectedData);
				lastSelectedItems = selectedItems;
				return lastSelection;
			}
		}
		lastSelection = StructuredSelection.EMPTY;
		lastSelectedItems = selectedItems;
		return lastSelection;
	}

	List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();

	private boolean listenOnSelection = true;

	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	private void selfSetSelection(ISelection selection) {
		setSelection(selection);
		setFocus();
	}

	public void setSelection(final ISelection selection) {
		if (container == null || container.isDisposed()) {
			return;
		}
		if (suppressSelectEvent) {
			return;
		}
		if (currentCategory == null) {
			refreshIconViewActionsState();
			return;
		}

		if (selection instanceof IStructuredSelection) {

			final IStructuredSelection sselection = (IStructuredSelection) selection;

			
			/**
			 * Should not be used as then when selection changes in editor the
			 * same element can not be reselected but have to visit other
			 * element first
			 */

			if (selectedItems != null
					&& selectedItems.size() == 1
					&& !selection.isEmpty()
					&& selectedItems.get(0) == ((IStructuredSelection) selection)
							.getFirstElement()) {
				itemReSelected = true;
			} else {
				itemReSelected = false;
			}

			List<IContentData> newSelection = new ArrayList<IContentData>();
			for (Iterator iter = sselection.iterator(); iter.hasNext();) {
				Object obj = iter.next();
				if (obj instanceof IContentData) {

					
					IContentData modelData = currentCategory
							.findById(((IContentData) obj).getId());
					if (modelData == null)
						continue;
					// set screen context to theme data, will be needed to
					// creating
					// bean-widget for elements that don't have screen
					// element
					
					modelData.getAdapter(EditObject.class);
					newSelection.add(modelData);
				} else {
					// select from screen widgets
					IScreenElement se = JEMUtil.getScreenElement(obj);
					if (se != null) {
						// handles colors
						ICategoryAdapter ca = (ICategoryAdapter) se.getData()
								.getAdapter(ICategoryAdapter.class);
						IContentData[] peers = ca.getCategorizedPeers();
						for (IContentData peer : peers) {
							newSelection.add(peer);
						}
					}
				}
			}

			selectedItems = newSelection;
			if (!selectedItems.contains(selectionStart)) {
				selectionStart = null;
			}

			// notifies listeners before revealing in the editor because the
			// statue line may otherwise be changed by other views
			suppressSelectEvent = true;
			try {
				SelectionChangedEvent event = new SelectionChangedEvent(this,
						getSelection());
				for (ISelectionChangedListener l : listeners) {
					try {
						l.selectionChanged(event);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} finally {
				suppressSelectEvent = false;
			}

			Control[] controls = container.getChildren();

			IContentData last = newSelection.isEmpty() ? null : newSelection
					.get(newSelection.size() - 1);

			for (Control ctrl : controls) {
				ImageLabel label = (ImageLabel) ctrl;

				if (ctrl.getData() == last) {
					scrollToVisible(ctrl);
				}

				if (newSelection.contains(ctrl.getData())) {
					label.setSelected(true);
				} else {
					label.setSelected(false);
				}
			}

			if (synchronize) {
				
				if (!(sselection.size() > 0 && sselection.toArray()[sselection
						.size() - 1] == Boolean.FALSE)) {
					suppressSelectEvent = true;
					try {
						showSelectionInEditor((IStructuredSelection) selection);
					} finally {
						suppressSelectEvent = false;
					}
				}
			}
		}

		suppressSelectEvent = true;
		try {
			
			((IconView) parent).fireSelectionChangeEvent();
		} finally {
			suppressSelectEvent = false;
		}

		refreshIconViewActionsState();
	}

	public String getPartName() {
		return partName;
	}

	public PageBookView getParent() {
		return parent;
	}

	public void setParent(PageBookView parent) {
		this.parent = parent;
	}

	private ImageDescriptor getImageDescriptor(IContentData data) {
		final Dimension size = getIconSize(data);
		IContentLabelProvider ida = (IContentLabelProvider) data
				.getAdapter(IContentLabelProvider.class);
		if (ida != null) {
			ImageDescriptor imgDesc = ida.getImageDescriptor(data, false, size.width, size.height);
			return imgDesc;
		} else
			return data.getImageDescriptor(size.width, size.height);
	}

	private Dimension getIconSize(IContentData data) {
		java.awt.Rectangle bounds = (java.awt.Rectangle) data
				.getAttribute(ContentAttribute.BOUNDS.name());
		if (bounds == null) {
			return DEFAULT_SIZE;
		}

		double scale = bounds.width / (double) bounds.height;
		int w, h;
		if (bounds.width > bounds.height) {
			w = DEFAULT_SIZE.width;
			h = Math.max(MIN_SIZE, (int) (w / scale));
		} else {
			h = DEFAULT_SIZE.height;
			w = Math.max(MIN_SIZE, (int) (h * scale));
		}
		return new Dimension(w, h);
	}

	private void refreshImageSkinned(Control child) {
		IContentData icd = (IContentData) child.getData();
		ImageLabel label = (ImageLabel) child;
		label.setImageDescriptor(getImageDescriptor(icd));
		label.setText(icd.getName()); 
		ISkinnableEntityAdapter ska = (ISkinnableEntityAdapter) icd
				.getAdapter(ISkinnableEntityAdapter.class);
		label.setModified(ska != null && ska.isSkinned());

		ISkinnableContentDataAdapter scda = (ISkinnableContentDataAdapter) icd
				.getAdapter(ISkinnableContentDataAdapter.class);
		if (scda != null) {
			label.setModified(scda.isElementSkinned());
			label.redraw();
		}

		IconTooltip tooltip = (IconTooltip) child.getData(IconTooltip.class
				.getName());
		if (tooltip != null) {
			tooltip.dispose();
		}

		tooltip = new IconTooltip(findScreenData(icd), this, getCommandStack());
		child.setData(tooltip.getClass().getName(), tooltip);
		if (tooltip.getContributionsCount(false) == 0) {
			child.setToolTipText(icd.getName());
		} else {
			tooltip.setControl(child);
		}
	}

	/**
	 * Based on selection of category from the active editor / project Brings
	 * the corresponding editor to top if the selected resource is open.
	
	 */
	public void showSelectionInEditor(IStructuredSelection selection) {
		if (mouseRightClicked) {
			return;
		}
		listenOnSelection = false;
		try {
			ISetSelectionTarget target = (ISetSelectionTarget) sourceEditor
					.getAdapter(ISetSelectionTarget.class);
			if (target != null) {
				try {
					// reveals only when selection has skinnable entities
					boolean hasValidContents = false;
					for (Object obj : selection.toArray()) {
						if (obj instanceof IContentData) {
							ICategoryAdapter adapter = (ICategoryAdapter) ((IContentData) obj)
									.getAdapter(ICategoryAdapter.class);
							if (adapter != null
									&& adapter.getCategorizedPeers().length > 0) {
								hasValidContents = true;
								break;
							}
						}
					}

					if (hasValidContents) {
						target.selectReveal(selection);
					}
					
					getSite().getActionBars().getStatusLineManager()
							.setMessage(null);
					getSite().getActionBars().getStatusLineManager()
							.setErrorMessage(null);
					getSite().getActionBars().getStatusLineManager().update(
							true);
				} catch (final RuntimeException e) {
					if (e.getMessage() != null
							&& e
									.getMessage()
									.equals(
											EditorMessages.Editor_Error_NoPreviewScreen)) {
						// updates the status message asynchronously
						
						Display.getCurrent().asyncExec(new Runnable() {
							public void run() {
								getSite().getActionBars()
										.getStatusLineManager()
										.setErrorMessage(e.getMessage());
							}
						});
						if (itemReSelected && mouseRightClicked)
							return;
						else
							noPreviewAvailableMessageBox();
					} else
						e.printStackTrace();
				}
			}
		} finally {
			listenOnSelection = true;
		}
	}

	private void noPreviewAvailableMessageBox() {
		if (!isNoPreviewMessageBoxShowing) {
			isNoPreviewMessageBoxShowing = true;
			try {
				WarningMessageDialogs.noPreviewAvailableMessageBox();
			} finally {
				isNoPreviewMessageBoxShowing = false;
			}
		}
	}

	private void initializeIconViewActions() {
		IActionBars bars = getSite().getActionBars();
		IToolBarManager tbm = bars.getToolBarManager();

		tbm.removeAll();

		selectAllAction = new IconViewSelectAllAction(this);
		tbm.add(selectAllAction);

		tbm.add(new Separator());

		copyAction = new CopyImageAction(IconViewPage.this.parent, null);
		tbm.add(copyAction);

		pasteAction = new PasteImageAction(IconViewPage.this.parent, null);
		tbm.add(pasteAction);

		tbm.add(new Separator());

		browseForFileAction = new BrowseForFileAction(IconViewPage.this.parent);
		tbm.add(browseForFileAction);

		tbm.add(new Separator());

		animateAction = new OpenGraphicsEditorAction(parent, this);
		animateAction.listenSelection();
		animateAction.selectionChanged(null);
		tbm.add(animateAction);

		externalToolsDropDown = new IconViewDropDownToolbarAction(this);
		tbm.add(externalToolsDropDown);

		tbm.add(new Separator());

		clearAction = new ClearImageEditorAction(IconViewPage.this.parent);
		tbm.add(clearAction);

		tbm.add(new Separator());

		toggleTextAction = new ToggleTextAction(this);
		IPreferenceStore store = S60WorkspacePlugin.getDefault()
				.getPreferenceStore();
		toggleTextAction.setChecked(store
				.getBoolean(IS60IDEConstants.PREF_SHOW_TITLE_TEXTS));
		tbm.add(toggleTextAction);

		Action toggleSync = new WorkbenchPartAction(null,
				WorkbenchPartAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (synchronize) {
					synchronize = false;
				} else {
					synchronize = true;
					IStructuredSelection sselection = (IStructuredSelection) getSelection();
					if (sselection != null) {
						if (!(sselection.size() > 0 && sselection.toArray()[sselection
								.size() - 1] == Boolean.FALSE)) {
							suppressSelectEvent = true;
							try {
								showSelectionInEditor((IStructuredSelection) getSelection());
							} finally {
								suppressSelectEvent = false;
							}
						}
					}
				}
				IPreferenceStore store = S60WorkspacePlugin.getDefault()
						.getPreferenceStore();
				store.setValue(IS60IDEConstants.PREF_SYNC_WITH_EDITOR,
						synchronize);
			}

			@Override
			protected boolean calculateEnabled() {
				return true;
			}
		};

		ImageDescriptor i1 = UiPlugin.getIconImageDescriptor(
				"resview_toggle_synch.gif", true);
		toggleSync.setToolTipText(ViewMessages.IconView_toggleSync_tooltip);
		toggleSync.setImageDescriptor(i1);
		tbm.add(new Separator());
		tbm.add(toggleSync);

		// Restore last synchronization state
		boolean syncState = store
				.getBoolean(IS60IDEConstants.PREF_SYNC_WITH_EDITOR);
		synchronize = !syncState;
		toggleSync.setChecked(syncState);
		toggleSync.run();

		/* global action handlers */

		setGlobalHandler(bars, new PasteContentDataAction(this,
				getCommandStack(), null));
		setGlobalHandler(bars, new EditImageInSVGEditorAction(this,
				getCommandStack()));
		setGlobalHandler(bars, new EditImageInBitmapEditorAction(this,
				getCommandStack()));
		setGlobalHandler(bars, new NinePieceConvertAction(this,
				getCommandStack(), 9));
		setGlobalHandler(bars, new NinePieceConvertAction(this,
				getCommandStack(), 1));
		
		setGlobalHandler(bars, new ElevenPieceConvertAction(this,
				getCommandStack(), 11));
		setGlobalHandler(bars, new ElevenPieceConvertAction(this,
				getCommandStack(), 1));

		/*setGlobalHandler(bars, new ThreePieceConvertAction(this,
				getCommandStack(), 3));
		setGlobalHandler(bars, new ThreePieceConvertAction(this,
				getCommandStack(), 1));*/
		
		
		
		setGlobalHandler(bars, new ConvertAndEditSVGInBitmapEditorAction(this,
				getCommandStack()));
		setGlobalHandler(bars, new EditInSystemEditorAction(this,
				getCommandStack()));
		setGlobalHandler(bars, new EditMaskAction(this, getCommandStack()));
		setGlobalHandler(bars, new EditMaskAction2(this, getCommandStack()));
		setGlobalHandler(bars, new EditSoundInSoundEditorAction(this,
				getCommandStack()));
		setGlobalHandler(bars, new BrowseForFileAction(this, getCommandStack()));
		setGlobalHandler(bars, new SetStretchModeAction(this,
				getCommandStack(), IMediaConstants.STRETCHMODE_STRETCH));
		setGlobalHandler(bars, new SetStretchModeAction(this,
				getCommandStack(), IMediaConstants.STRETCHMODE_ASPECT));

		// undo, redo

		ActionRegistry ar = (ActionRegistry) sourceEditor
				.getAdapter(ActionRegistry.class);
		if (ar != null) {
			bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), ar
					.getAction(ActionFactory.UNDO.getId()));
			bars.setGlobalActionHandler(ActionFactory.REDO.getId(), ar
					.getAction(ActionFactory.REDO.getId()));
		} else {
			IAction action = new SafeUndoAction(parent);
			bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), action);
			action = new SafeRedoAction(parent);
			bars.setGlobalActionHandler(ActionFactory.REDO.getId(), action);
		}

		/* add task, add bookmark */
		setGlobalHandler(bars, new AddTaskViewAction(parent));
		setGlobalHandler(bars, new AddBookmarkViewAction(parent));

		// clear element, edit/animate, copy, paste
		bars.setGlobalActionHandler(OpenGraphicsEditorAction.ID, animateAction);
		bars.setGlobalActionHandler(ClearImageEditorAction.ID, clearAction);
		bars.setGlobalActionHandler(PasteImageAction.ID, pasteAction);
		bars.setGlobalActionHandler(CopyImageAction.ID, copyAction);
	}

	private CommandStack getCommandStack() {
		return (CommandStack) sourceEditor.getAdapter(CommandStack.class);
	}

	private void setGlobalHandler(IActionBars bars, AbstractAction aa) {
		if (aa != null) {
			aa.listenSelection();
			actionsToDispose.add(aa);
			bars.setGlobalActionHandler(aa.getId(), aa);
		}
	}

	private void refreshIconViewActionsState() {
		if (scrollableComposite == null || scrollableComposite.isDisposed()) {
			// disposed
			return;
		}

		copyAction.update();
		pasteAction.update();
		clearAction.update();
		browseForFileAction.update();
		animateAction.update();

		updateImageLabel();
	}

	public void updateImageLabel() {
		for (Control child : getContainer().getChildren()) {
			ImageLabel label = (ImageLabel) child;
			int newLines = toggleTextAction.isChecked() ? IS60IDEConstants.IMAGE_LABEL_TEXT_LINES
					: 0;
			if (label.getLines() == newLines) {
				return;
			}
			label.setLines(newLines);
		}
		((ScrolledComposite) getContainer().getParent().getParent())
				.setMinSize(getContainer()
						.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		getContainer().getParent().layout(true, true);
		scrollToVisible();
	}

	// Shows the selection by scrolling scrollbars automatically
	public void scrollToVisible() {
		if (container == null || container.isDisposed()) {
			return;
		}
		Control[] childs = getSelectedControls();
		if (childs.length == 0 || selectedItems.size() == 0) {
			return;
		}

		IContentData lastData = selectedItems.get(selectedItems.size() - 1);
		for (int i = 0; i < childs.length; i++) {
			if (childs[i].getData() == lastData) {
				scrollToVisible(childs[i]);
				break;
			}
		}
	}

	// Shows the selection by scrolling scrollbars automatically
	public void scrollToVisible(Control control) {
		if (control == null || container == null || container.isDisposed()) {
			return;
		}

		Rectangle bounds = control.getBounds();
		if (bounds.height <= 0 || bounds.width <= 0)
			return;
		Rectangle area = scrollableComposite.getClientArea();
		Point origin = scrollableComposite.getOrigin();
		if (origin.x > bounds.x)
			origin.x = Math.max(0, bounds.x);
		if (origin.y > bounds.y)
			origin.y = Math.max(0, bounds.y);
		if (origin.x + area.width < bounds.x + bounds.width)
			origin.x = Math.max(0, bounds.x + bounds.width - area.width);
		if (origin.y + area.height < bounds.y + bounds.height)
			origin.y = Math.max(0, bounds.y + bounds.height - area.height);
		scrollableComposite.setOrigin(origin);
	}

	@Override
	public void dispose() {
		repopulateContents(null);
		Display.getDefault()
				.removeFilter(SWT.KeyDown, selectionModeKeyListener);
		Display.getDefault().removeFilter(SWT.KeyUp, selectionModeKeyListener);

		for (WorkbenchPartAction action : actionsToDispose) {
			action.dispose();
		}

		try {
			actionsToDispose.clear();
			copyAction.dispose();
			pasteAction.dispose();
			animateAction.dispose();
			externalToolsDropDown.dispose();
			clearAction.dispose();
		} catch (Exception e) {
		}

		selectAllAction = null;
		copyAction = null;
		pasteAction = null;
		animateAction = null;
		externalToolsDropDown = null;
		clearAction = null;
		toggleTextAction = null;

		getSite().getActionBars().clearGlobalActionHandlers();
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		if (e.character == 0x01 && ctrl_active) {
			selectAllAction.run();
			return;
		}

		IContentData lastSelectedItem = null;

		if (selectedItems != null && selectedItems.size() > 0) {
			lastSelectedItem = selectedItems.get(selectedItems.size() - 1);
		}

		if (lastSelectedItem == null) {
			return;
		}

		Control[] children = container.getChildren();
		if (children.length < 2) {
			return;
		}

		GridLayout layout = (GridLayout) container.getLayout();
		int cols = layout.numColumns;
		int rows = children.length / cols;
		if (children.length % cols != 0) {
			rows++;
		}
		int col = 0, row = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				int index = i * cols + j;
				if (index > children.length - 1) {
					break;
				}
				if (children[index].getData() == lastSelectedItem) {
					row = i;
					col = j;
					break;
				}
			}
		}

		IContentData data = null;
		switch (e.keyCode) {
		case SWT.ARROW_LEFT:
			for (int i = 0; i < children.length; i++) {
				if (children[i].getData() == lastSelectedItem) {
					if (i == 0) {
						data = (IContentData) children[children.length - 1]
								.getData();
					} else {
						data = (IContentData) children[i - 1].getData();
					}
				}
			}
			if (!shift_active) {
				selectionStart = null;
			}
			break;
		case SWT.ARROW_RIGHT:
			for (int i = 0; i < children.length; i++) {
				if (children[i].getData() == lastSelectedItem) {
					if (i == children.length - 1) {
						data = (IContentData) children[0].getData();
					} else {
						data = (IContentData) children[i + 1].getData();
					}
				}
			}
			if (!shift_active) {
				selectionStart = null;
			}
			break;
		case SWT.ARROW_DOWN:
			if (row == rows - 1) {
				row = 0;
			} else {
				row++;
			}
			int index = row * cols + col;
			if (index >= children.length) {
				data = (IContentData) children[children.length - 1].getData();
			} else {
				data = (IContentData) children[index].getData();
			}
			if (!shift_active) {
				selectionStart = null;
			}
			break;
		case SWT.ARROW_UP:
			if (row == 0) {
				row = rows - 1;
			} else {
				row--;
			}
			index = row * cols + col;
			if (index >= children.length) {
				data = (IContentData) children[children.length - 1].getData();
			} else {
				data = (IContentData) children[index].getData();
			}
			if (!shift_active) {
				selectionStart = null;
			}
			break;
		case SWT.HOME:
			index = 0;
			if (index >= children.length) {
				data = (IContentData) children[children.length - 1].getData();
			} else {
				data = (IContentData) children[index].getData();
			}
			if (!shift_active) {
				selectionStart = null;
			}
			break;
		case SWT.END:
			index = children.length;
			if (index >= children.length) {
				data = (IContentData) children[children.length - 1].getData();
			} else {
				data = (IContentData) children[index].getData();
			}
			if (!shift_active) {
				selectionStart = null;
			}
			break;
		}
		if (e.stateMask == 0) {
			updateSelection(data, SelectionMode.NORMAL);
		} else if (ctrl_active && shift_active) {
			updateSelection(data, SelectionMode.CTRL_SHIFT);
		} else if (shift_active) {
			updateSelection(data, SelectionMode.SHIFT);
		}
	}

	protected void updateSelection(IContentData newEnd, SelectionMode mode) {
		// System.out.println("updateSelection(): element:" + newEnd + " mode:"
		// + mode);

		IContentData lastSelectedItem = null;

		if (selectedItems != null && selectedItems.size() > 0) {
			lastSelectedItem = selectedItems.get(selectedItems.size() - 1);
		}

		if (lastSelectedItem == null) {
			if (newEnd != null) {
				selectionStart = newEnd;
				setSelection(new StructuredSelection(newEnd));
			}
			return;
		}

		if (selectionStart == null) {
			selectionStart = lastSelectedItem;
		}

		if (mode == SelectionMode.NORMAL) {
			if (newEnd != null && newEnd != lastSelectedItem) {
				setSelection(new StructuredSelection(newEnd));
			}
		}

		if (mode == SelectionMode.CTRL_SHIFT) {
			selectionStart = lastSelectedItem;
			mode = SelectionMode.SHIFT;
		}

		if (mode == SelectionMode.CTRL) {
			if (newEnd != null) {
				List<IContentData> newSelection = new ArrayList<IContentData>(
						selectedItems);

				if (selectedItems.contains(newEnd)) {
					if (selectionStart == newEnd) {
						selectionStart = null;
					}
					newSelection.remove(newEnd);
				} else {
					newSelection.add(newEnd);
					selectionStart = newEnd;
				}

				setSelection(new StructuredSelection(newSelection));
			}
		}

		if (mode == SelectionMode.SHIFT) {
			if (newEnd != null && newEnd != lastSelectedItem) {
				List<IContentData> newSelection = new ArrayList<IContentData>(
						selectedItems);
				int idxSel = indexOf(lastSelectedItem);
				int idxData = indexOf(newEnd);
				int idxStart = indexOf(selectionStart);
				if (idxSel != -1 && idxData != -1) {
					Control[] controls = container.getChildren();
					if (idxSel > idxData) {
						// System.out.println("backward");
						if (idxSel > idxStart && idxData < idxStart) {
							// System.out.println("backward over start");
							for (int i = idxSel; i > idxStart; i--) {
								IContentData child = (IContentData) controls[i]
										.getData();
								// System.out.println("removing:" + child);
								newSelection.remove(child);
							}
						}
						if (idxStart <= idxData) {
							for (int i = idxSel; i > Math
									.max(idxData, idxStart); i--) {
								IContentData child = (IContentData) controls[i]
										.getData();
								// System.out.println("removing:" + child);
								newSelection.remove(child);
							}
						} else {
							for (int i = Math.min(idxSel - 1, idxStart - 1); i >= idxData; i--) {
								IContentData child = (IContentData) controls[i]
										.getData();
								// System.out.println("adding:" + child);
								newSelection.add(child);
							}
						}
					} else {
						// System.out.println("forward");
						if (idxSel < idxStart && idxData > idxStart) {
							// System.out.println("forward over start");
							for (int i = idxSel; i < idxStart; i++) {
								IContentData child = (IContentData) controls[i]
										.getData();
								// System.out.println("removing:" + child);
								newSelection.remove(child);
							}
						}
						if (idxStart >= idxData) {
							for (int i = idxSel; i < Math
									.min(idxData, idxStart); i++) {
								IContentData child = (IContentData) controls[i]
										.getData();
								// System.out.println("removing:" + child);
								newSelection.remove(child);
							}
						} else {
							for (int i = Math.max(idxSel + 1, idxStart + 1); i <= idxData; i++) {
								IContentData child = (IContentData) controls[i]
										.getData();
								// System.out.println("adding:" + child);
								newSelection.add(child);
							}
						}
					}
				}

				setSelection(new StructuredSelection(newSelection));
			}
		}
	}

	protected int indexOf(IContentData data) {
		Control[] controls = container.getChildren();
		for (int idx = 0; idx < controls.length; idx++) {
			if (data == controls[idx].getData()) {
				return idx;
			}
		}

		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
	}

	public IEditorPart getSourceEditor() {
		return sourceEditor;
	}

	class IconViewDragListener extends S60BaseDragListener {

		public IconViewDragListener(Transfer transfer,
				ISelectionProvider provider, CommandStack stack) {
			super(transfer, provider);
		}

		@Override
		protected Object getSelectedElement(DragSourceEvent evt) {
			Control control = ((DragSource) evt.widget).getControl();
			return control.getData();
		}

		@Override
		public void dragStart(DragSourceEvent event) {
			if (shift_active || ctrl_active) {
				event.doit = false;
				return;
			}
			super.dragStart(event);
		}
	}

	class ControlMoveAdapter extends ControlAdapter {
		Rectangle lastRectangle = null;

		List<String> painted = new ArrayList<String>();

		@Override
		public void controlMoved(ControlEvent e) {
			if (containerImage == null) {
				return;
			}

			if (((StackLayout) stackContainer.getLayout()).topControl != selectionPaintCanvas) {
				return;
			}

			GC containerImageGC = new GC(containerImage);
			try {
				Rectangle rect = new Rectangle(
						scrollableComposite.getOrigin().x, scrollableComposite
								.getOrigin().y, scrollableComposite
								.getClientArea().width, scrollableComposite
								.getClientArea().height);

				Control[] labels = container.getChildren();
				for (int i = 0; i < labels.length; i++) {
					ImageLabel lbl = (ImageLabel) labels[i];
					if (!painted.contains(lbl.getData().toString())) {
						Rectangle intersection = rect.intersection(lbl
								.getBounds());
						if (intersection.width > 0 && intersection.height > 0) {
							if (lastRectangle != null) {
								Rectangle lastIntersection = lastRectangle
										.intersection(lbl.getBounds());
								if (lastIntersection.width == lbl.getBounds().width
										&& lastIntersection.height == lbl
												.getBounds().height) {
									painted.add(lbl.getData().toString());
									continue;
								}
							}

							Image img = new Image(null, lbl.getSize().x, lbl
									.getSize().y);
							try {
								GC imgGC = new GC(img);
								try {
									imgGC.setBackground(container
											.getBackground());
									imgGC.fillRectangle(0, 0,
											img.getBounds().width, img
													.getBounds().height);
									lbl.paint(imgGC);
									containerImageGC.drawImage(img, lbl
											.getLocation().x,
											lbl.getLocation().y);
								} finally {
									imgGC.dispose();
								}
							} finally {
								img.dispose();
							}
							painted.add(lbl.getData().toString());
						}
					}
				}

				lastRectangle = rect;
			} finally {
				containerImageGC.dispose();
			}
		}
	}

	class ComponentAdapter extends TypedAdapter {
		ImageLabel label;

		ComponentAdapter(ImageLabel label) {
			this.label = label;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
		 */
		public void notifyChanged(Notification notification) {
			if (EditingUtil.isRemovingAdapter(notification, this)) {
				return;
			}
			if (label == null) {
				if (EditingUtil.getContainmentFeature((EObject) notification
						.getNotifier()) == notification.getFeature()) {
					// parent adapter
					repopulateContents(lastSelection);
				}
			} else {
				refreshImageSkinned(label);
				if (label.isSelected()) {
					refreshIconViewActionsState();
				}
			}
		}
	}
}