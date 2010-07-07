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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.properties.IPropertySource;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.content.core.IContentDelta;
import com.nokia.tools.content.core.IContentListener;
import com.nokia.tools.content.core.IContentService;
import com.nokia.tools.editing.core.InvocationAdapter;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.part.DiagramGraphicalEditPart;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.platform.theme.Component;
import com.nokia.tools.platform.theme.ComponentGroup;
import com.nokia.tools.platform.theme.Task;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.s60.editor.actions.AddToGroupAction;
import com.nokia.tools.s60.editor.actions.undo.SafeRedoAction;
import com.nokia.tools.s60.editor.actions.undo.SafeUndoAction;
import com.nokia.tools.s60.editor.dnd.S60BaseDropListener;
import com.nokia.tools.s60.ide.IS60IDEConstants;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.core.ICategoryAdapter;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.editor.ScreenEditorPart;
import com.nokia.tools.screen.ui.editor.embedded.IEmbeddedEditorCustomizer;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IEmbeddedEditorDescriptor;
import com.nokia.tools.screen.ui.propertysheet.color.CssColorDialog;
import com.nokia.tools.screen.ui.propertysheet.color.DraggedColorObject;
import com.nokia.tools.screen.ui.utils.SimpleSelectionProvider;
import com.nokia.tools.screen.ui.views.ColorResourceTableCategory;
import com.nokia.tools.screen.ui.views.ColorTableItem;
import com.nokia.tools.screen.ui.views.ContentOutlineViewPart;
import com.nokia.tools.screen.ui.views.IIDEConstants;
import com.nokia.tools.screen.ui.views.IResourceSection2;
import com.nokia.tools.screen.ui.views.IResourceTableCategoryItem;
import com.nokia.tools.screen.ui.views.IResourceViewerSelectionHelper2;
import com.nokia.tools.screen.ui.views.ResourceColorBoxesLine;
import com.nokia.tools.screen.ui.views.ResourceTableCategory;
import com.nokia.tools.screen.ui.views.ResourceTableInput;
import com.nokia.tools.screen.ui.views.ResourceTableItem;
import com.nokia.tools.screen.ui.views.ResourceTableMasterGroup;
import com.nokia.tools.screen.ui.views.ViewMessages;
import com.nokia.tools.theme.content.ContentDataAdapter;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.content.ThemeScreenData;
import com.nokia.tools.ui.widgets.ImageLabel;

public class ResourceViewPage extends Page implements IContentListener,
		ISelectionProvider, IAdaptable, ImageLabel.SelectedListener,
		KeyListener {

	private static final String extensionPointId = "resourceSections";

	private static final String projectId = "com.nokia.tools.screen.ui";

	private static final int COLUMN0_WIDTH = 20;

	private static final int COLUMN1_WIDTH = 220;

	private static final int COLUMN2_WIDTH = 15;

	private static final int COLUMN3_WIDTH = 15;

	private static final int ROW_HEIGHT = 20;

	private static final int COLOR_BOX_SIZE_OUTER = 12;

	private static final int COLOR_BOX_SIZE_MIDDLE = 10;

	private static final int COLOR_BOX_SIZE_INNER = 8;

	private static final int COLOR_CELL_OFFSET = 14;

	private static final int COLOR_BOX_ROUND_ANGLE = 2;

	private static final int TREE_SUBELEMENT_SHIFT = 15;

	private static final int TOP_TREE_ELEMENT_SHIFT = 5;

	private static final Color FG_LINE_COLOR = ColorConstants.gray;

	private static final Color FG_TEXT_COLOR = ColorConstants.menuForeground;

	private static final Color FG_SELECTED_TEXT_COLOR = ColorConstants.menuForegroundSelected;

	private static final Color BG_COLOR = ColorConstants.menuBackground;

	private static final Color BG_SELECTED_COLOR = ColorConstants.menuBackgroundSelected;

	private static final String CHANGE_COLOR = "CHANGE_COLOR";

	private static final String SELECT_ITEM = "SELECT_ITEM";

	private List<IResourceSection2> resourceSections;

	private ResourceTableCategory selectedCategory;

	private ResourceTableMasterGroup selectedGroup;

	private ResourceTableItem selectedItem;

	private ImageLabel selectedIAL;

	private List<ImageLabel> ials = new ArrayList<ImageLabel>();

	private IEditorPart sourceEditor;

	private Table table;

	private Composite composite;

	private Composite form;

	private ScrolledComposite formComposite;

	private List<ResourceTableInput> inputs;

	protected IProgressMonitor monitor;

	// /Selection provider is made for contributed menu listeners
	private List<ISelectionChangedListener> selectionListeners = new ArrayList<ISelectionChangedListener>();

	/**
	 * internal flag for avoiding loop running condition and trigger refresh
	 * icon view two times from positionViewer selection changed and from select
	 * in editor -> showSelection -> Select in Resources ->1)selection change
	 * and showSelection also triggers
	 */
	// private boolean _shouldRefreshIconView = true;
	private boolean suppressViewerEvents;

	private boolean suppressOpenEditorEvents;

	private ISelectionListener synchronizeListener;

	// synchronize with selection in Editor flag
	private boolean synchronize;

	private Adapter refreshAdapter = new ContentDataAdapter() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.theme.content.ContentDataAdapter#contentDataModified(com.nokia.tools.content.core.IContentData)
		 */
		@Override
		protected void contentDataModified(IContentData modified) {
			resourceModified(modified);
		}
	};

	public static List<IResourceSection2> createResourceSections() {
		List<IResourceSection2> sections = new ArrayList<IResourceSection2>();
		IExtensionPoint extension = Platform.getExtensionRegistry()
				.getExtensionPoint(projectId, extensionPointId);
		IExtension[] extensions = extension.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] configElements = extensions[i]
					.getConfigurationElements();
			for (int j = 0; j < configElements.length; j++) {
				try {
					Object execExt = configElements[j]
							.createExecutableExtension("class");
					if (execExt instanceof IResourceSection2) {
						sections.add((IResourceSection2) execExt);
					}
				} catch (CoreException e) {
					S60WorkspacePlugin.error(e);
				}
			}
		}
		return sections;
	}

	public ResourceViewPage(IEditorPart sourceEd) {
		this.sourceEditor = sourceEd;
	}

	public IEditorPart getSourceEditor() {
		return sourceEditor;
	}

	private IContentAdapter getContentAdapter() {
		return (IContentAdapter) sourceEditor.getAdapter(IContentAdapter.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		IContentService service = (IContentService) sourceEditor
				.getAdapter(IContentService.class);
		if (service != null) {
			service.addContentListener(this);
		}

		Cursor handCursor = getSite().getShell().getDisplay().getSystemCursor(
				SWT.CURSOR_HAND);

		parent.setBackground(ColorConstants.blue);
		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		GridLayout gl = new GridLayout();
		gl.marginWidth = gl.marginHeight = 0;
		parent.setBackground(ColorConstants.menuBackground);
		composite.setBackground(ColorConstants.menuBackground);
		resourceSections = createResourceSections();
		if (Platform.getProduct().getId().equals(
				"com.nokia.tools.carbide.ui.licensee.product")) {
			gl.numColumns = 2;
			composite.setLayout(gl);
			formComposite = new ScrolledComposite(composite, SWT.BORDER
					| SWT.V_SCROLL);
			gl = new GridLayout();
			gl.marginWidth = gl.marginHeight = 0;
			formComposite.setLayout(gl);
			formComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
			formComposite.setBackground(ColorConstants.white);
			formComposite.setAlwaysShowScrollBars(true);

			form = new Composite(formComposite, SWT.NONE);
			gl = new GridLayout();
			gl.marginWidth = gl.marginHeight = gl.verticalSpacing = 0;
			gl.verticalSpacing = 1;
			form.setLayout(gl);
			form.setBackground(ColorConstants.listBackground);

			for (IResourceSection2 section : resourceSections) {
				List<ImageLabel> items = section.getNavigationControlItems(
						form, getContentAdapter());
				for (ImageLabel label : items) {
					label
							.setUnselectedBackground(ColorConstants.listBackground);
					label.setFillBackground(true);
					label.setSelectedTextColor(ColorConstants.white);
					label.setUnselectedTextColor(ColorConstants.menuForeground);
					ials.add(label);
				}
			}



			if (ials.get(0) != null) {
				this.selectedIAL = ials.get(0);
				ials.get(0).setSelected(true);
				Control parentComposite = ((Composite) selectedIAL).getParent();
				parentComposite
						.setBackground(ColorConstants.menuBackgroundSelected);
				for (ImageLabel label : ials) {
					label.addSelectionListener(this);
					label.addKeyListener(this);
					final ThumbListener thumbListener = new ThumbListener(label);
					label.addListener(SWT.MouseMove, thumbListener);
					label.addListener(SWT.MouseExit, thumbListener);
					label.setCursor(handCursor);
				}
			}

			formComposite.setContent(form);
			formComposite.addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e) {
					scrollToVisible((Composite) selectedIAL.getParent());
				}
			});
			formComposite.getVerticalBar().setIncrement(85);
			formComposite.getVerticalBar().setPageIncrement(
					2 * formComposite.getVerticalBar().getIncrement());
			form.setSize(form.computeSize(SWT.DEFAULT, SWT.DEFAULT));			
		} else {
			gl.numColumns = 1;
			composite.setLayout(gl);
		}
		createCompositeArea(composite);
		getSite().setSelectionProvider(this);

		// supported global actions
		IActionBars bars = getSite().getActionBars();
		// undo, redo
		ActionRegistry ar = (ActionRegistry) sourceEditor
				.getAdapter(ActionRegistry.class);
		if (ar != null) {
			bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), ar
					.getAction(ActionFactory.UNDO.getId()));
			bars.setGlobalActionHandler(ActionFactory.REDO.getId(), ar
					.getAction(ActionFactory.REDO.getId()));
		} else {
			IAction action = new SafeUndoAction(sourceEditor);
			bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), action);
			action = new SafeRedoAction(sourceEditor);
			bars.setGlobalActionHandler(ActionFactory.REDO.getId(), action);
		}
	}

	@Override
	public Control getControl() {
		return composite;
	}

	// Shows the selection by scrolling scrollbars automatically
	public void scrollToVisible(Control control) {
		if (control == null || form == null || form.isDisposed())
			return;

		Rectangle bounds = control.getBounds();
		if (bounds.height <= 0 || bounds.width <= 0)
			return;

		Rectangle area = formComposite.getClientArea();
		Point origin = formComposite.getOrigin();
		if (origin.x > bounds.x)
			origin.x = Math.max(0, bounds.x);
		if (origin.y > bounds.y)
			origin.y = Math.max(0, bounds.y);
		if (origin.x + area.width < bounds.x + bounds.width)
			origin.x = Math.max(0, bounds.x + bounds.width - area.width);
		if (origin.y + area.height < bounds.y + bounds.height)
			origin.y = Math.max(0, bounds.y + bounds.height - area.height);
		formComposite.setOrigin(origin);
	}

	public void scrollToVisible(ResourceTableItem item) {
		for (TableItem tableItem : table.getItems()) {
			if (tableItem.getData() == item) {
				table.showItem(tableItem);
			}
		}
	}

	protected class ThumbListener implements Listener {
		ImageLabel imageLabel;

		public ThumbListener(ImageLabel imageLabel) {
			this.imageLabel = imageLabel;
		}

		public void handleEvent(Event event) {
			if (SWT.MouseMove == event.type) {
				if (!imageLabel.getParent().getBackground().equals(
						ColorConstants.menuBackgroundSelected))
					imageLabel.getParent().setBackground(
							ColorConstants.menuBackgroundSelected);
			}

			if (SWT.MouseExit == event.type) {
				imageLabel.getParent()
						.setBackground(imageLabel.getBackground());
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		if (ials.size() < 2) {
			return;
		}

		switch (e.keyCode) {
		case SWT.ARROW_UP:
			for (int i = 0; i < ials.size(); i++) {
				if (((ImageLabel) ials.get(i)).equals((ImageLabel) e.widget)) {
					if (i > 0) {
						setSelectedThumb(ials.get(i - 1), true);
						return;
					} else {
						setSelectedThumb(ials.get(ials.size() - 1), true);
						return;
					}
				}
			}
			break;
		case SWT.ARROW_DOWN:
			for (int i = 0; i < ials.size(); i++) {
				if (((ImageLabel) ials.get(i)).equals((ImageLabel) e.widget)) {
					if (i < ials.size() - 1) {
						setSelectedThumb(ials.get(i + 1), true);
						return;
					} else {
						setSelectedThumb(ials.get(0), true);
						return;
					}
				}
			}
			break;
		case (SWT.PAGE_UP):
			setSelectedThumb(ials.get(0), true);
			break;
		case (SWT.HOME):
			setSelectedThumb(ials.get(0), true);
			break;
		case SWT.PAGE_DOWN:
			setSelectedThumb(ials.get(ials.size() - 1), true);
			break;
		case SWT.END:
			setSelectedThumb(ials.get(ials.size() - 1), true);
			break;
		case SWT.TAB:
			selectedIAL.traverse(SWT.TRAVERSE_TAB_NEXT);
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
		
	}

	private void setSelectedThumb(ImageLabel label, boolean setFocusOnThumb) {
		for (int i = 0; i < ials.size(); i++) {
			Control parent = (Composite) ials.get(i).getParent();
			if (ials.get(i).equals(label)) {
				parent.setBackground(ColorConstants.menuBackgroundSelected);
				selectedIAL = (ImageLabel) ials.get(i);
				selectedIAL.setSelected(true);
			} else {
				parent.setBackground(ColorConstants.white);
				((ImageLabel) ials.get(i)).setSelected(false);
			}
		}
		inputs = createInput2(selectedIAL);
		refreshKeepingIndex();
		if (setFocusOnThumb) {
			selectedIAL.setFocus();
		}
		scrollToVisible((Composite) selectedIAL.getParent());
	}

	/*
	 * Fill the resource view model with the data ImageAboveLabel - the control
	 * from the leftsidepanel for identifying the contents.
	 */
	public List<ResourceTableInput> createInput2(ImageLabel imageAboveLabel) {
		String contentType = "";
		if (imageAboveLabel != null) {
			contentType = (String) imageAboveLabel.getData();
		} else {
			if (ials != null && ials.size() > 0)
				contentType = (String) ials.get(0).getData();
		}

		List<ResourceTableInput> inputs = new ArrayList<ResourceTableInput>();

		List<ResourceTableMasterGroup> filteredGroups = new ArrayList<ResourceTableMasterGroup>();

		IContentAdapter contentAdapter = getContentAdapter();
		for (IResourceSection2 section : resourceSections) {
			if (contentType == null || "".equals(contentType)) {
				inputs.addAll(section.getPaletteDrawers(contentAdapter));
			} else {
				if (contentAdapter.getContents(contentType) != null
						&& contentAdapter.getContents(contentType).length > 0
						&& section.supportsContent(contentAdapter
								.getContents(contentType)[0])) {
					List<ResourceTableInput> tempInputs = section
							.getPaletteDrawers(contentAdapter);
					filteredGroups = section.filterInput(tempInputs,
							imageAboveLabel);
					// tempInputs=temp2Inputs;//section.filterInput(tempInputs,
					// imageAboveLabel);
					sortCategoriesforXMLUI(tempInputs);
					inputs.addAll(tempInputs);
				}
			}

		}

		for (ResourceTableMasterGroup grp : filteredGroups) {
			for (ResourceTableInput input : inputs) {
				for (ResourceTableMasterGroup group : input.getGroups()) {
					if (group == grp) {
						input.removeGroup(grp);
						break;
					}
				}
			}
		}

		if ((selectedCategory != null || selectedGroup != null || selectedItem != null)
				&& inputs.size() > 0) {
			String selectedCategoryName = null, selectedGroupName = null;

			if (selectedCategory != null) {
				selectedCategoryName = selectedCategory.getName();
			}
			if (selectedGroup != null) {
				selectedGroupName = selectedGroup.getMasterGroupName();
			}

			for (ResourceTableMasterGroup grp : inputs.get(0).getGroups()) {
				if (grp.getMasterGroupName().equals(selectedGroupName)) {
					selectedGroup = grp;
				}
				for (IResourceTableCategoryItem catItem : grp.getCategories()) {
					if (catItem.getName().equals(selectedCategoryName)) {
						selectedCategory = (ResourceTableCategory) catItem;
						if (catItem instanceof ResourceTableCategory) {
							((ResourceTableCategory) catItem).getParent()
									.setExtracted(true);
						} else if (catItem instanceof ColorResourceTableCategory) {
							((ColorResourceTableCategory) catItem)
									.setExtracted(true);
							((ColorResourceTableCategory) catItem).getParent()
									.setExtracted(true);
							selectedCategory = ((ColorResourceTableCategory) catItem);
						}
					}

					if (catItem instanceof ResourceTableCategory) {

						if (selectedItem != null) {
							for (ResourceTableItem item : ((ResourceTableCategory) catItem)
									.getItems()) {
								// if (item.getName().equals(selectedItemName))
								// {
								//									
								// item.getParent().setExtracted(true);
								// item.getParent().getParent().setExtracted(true);
								// selectedItem = item;
								// }

								if (item.getAssociatedContentData() == selectedItem
										.getAssociatedContentData()) {

									item.getParent().setExtracted(true);
									item.getParent().getParent().setExtracted(
											true);
									selectedItem = item;
								}
							}
						}
					}

				}
			}
		}

		return inputs;
	}

	/**

	 * 
	 * @param tempInputs
	 */
	private void sortCategoriesforXMLUI(List<ResourceTableInput> tempInputs) {

		if (null == tempInputs)
			return;
		for (int i = 0; i < tempInputs.size(); i++) {
			ResourceTableInput temp = tempInputs.get(i);
			if ("XMLUI".equalsIgnoreCase(temp.getType())) {
				for (ResourceTableMasterGroup masterGroup : temp.getGroups()) {
					// ResourceTableMasterGroup masterGroup =
					// temp.getGroups().get(i);
					List<IResourceTableCategoryItem> catergories = masterGroup
							.getCategories();
					class IResourceTableCategoryItemComparator implements
							Comparator<IResourceTableCategoryItem> {

						public int compare(IResourceTableCategoryItem object1,
								IResourceTableCategoryItem object2) {
							return object1.getName().compareToIgnoreCase(
									object2.getName());
						}

					}
					Collections.sort(catergories,
							new IResourceTableCategoryItemComparator());
					masterGroup.setCategories(catergories);
				}
			}
		}
	}

	public void fillTable() {
		for (ResourceTableInput input : inputs) {
			TableItem item;// = new TableItem(table, SWT.NONE);
			// item.setData(input);
			for (ResourceTableMasterGroup grp : input.getGroups()) {
				item = new TableItem(table, SWT.NONE);

				item.setData(grp);
				for (IResourceTableCategoryItem cat : grp.getCategories()) {
					if (cat instanceof ResourceTableCategory) {

						if (((ResourceTableCategory) cat).isVisible()) {
							item = new TableItem(table, SWT.NONE);
							item.setData(cat);
						}
						for (ResourceTableItem tabItem : ((ResourceTableCategory) cat)
								.getItems()) {
							if (tabItem.isVisible()) {
								item = new TableItem(table, SWT.NONE);
								item.setData(tabItem);
							}
						}
					} else if (cat instanceof ResourceColorBoxesLine) {
						if (((ResourceColorBoxesLine) cat).isVisible()) {
							item = new TableItem(table, SWT.NONE);
							item.setData(cat);
						}
					}
				}
			}
		}
	}

	private void addDragDropSupport(final Control table) {

		Transfer[] transfers = new Transfer[] {
				LocalSelectionTransfer.getTransfer(),
				FileTransfer.getInstance() };

		DropTargetListener dropListener = new S60BaseDropListener(null,
				getCommandStack()) {

			private Color background;

			private TableItem currentHighlighed = null;

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.s60.editor.dnd.S60BaseDropListener#dragLeave(org.eclipse.swt.dnd.DropTargetEvent)
			 */
			@Override
			public void dragLeave(DropTargetEvent event) {
				super.dragLeave(event);
				removeDragOverHighlight(event.item);
				currentHighlighed = null;
				background = null;
			}

			/**
			
			 * 
			 * @param widget
			 */
			protected void drawDragOverHighlight(Widget widget) {
				if (currentHighlighed != null && currentHighlighed != widget
						&& null != background) {
					currentHighlighed.setBackground(background);
				}
				if (widget instanceof TableItem && currentHighlighed != widget) {
					TableItem control = (TableItem) widget;
					if (background == null) {
						background = control.getBackground();
					}
					currentHighlighed = control;
					control.setBackground(Display.getDefault().getSystemColor(
							IS60IDEConstants.DRAG_HIGHLIGHT_COLOR));
				}
			}

			protected void removeDragOverHighlight(Widget widget) {
				if (currentHighlighed != null && null != background) {
					currentHighlighed.setBackground(background);
				}
				if (widget instanceof TableItem) {
					TableItem control = (TableItem) widget;
					if (background != null)
						control.setBackground(background);
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.s60.editor.dnd.S60BaseDropListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
			 */
			@Override
			public void drop(DropTargetEvent event) {
				Object dropdata = getDropData(event);
				if (dropdata instanceof DraggedColorObject
						&& event.item.getData() instanceof ColorResourceTableCategory) {
					IContentData data = ((ResourceTableCategory) event.item
							.getData()).getAssociatedContentData();
					AddToGroupAction action = new AddToGroupAction(
							new SimpleSelectionProvider(data),
							getCommandStack());
					DraggedColorObject drop = (DraggedColorObject) dropdata;
					action.setGroupName(drop.getName());
					action.setColorGroups(drop.getGrps());
					action.run();
				} else
					super.drop(event);
				removeDragOverHighlight(event.item);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.s60.editor.dnd.S60BaseDropListener#dragOver(org.eclipse.swt.dnd.DropTargetEvent)
			 */
			@Override
			public void dragOver(DropTargetEvent event) {
				super.dragOver(event);
				Object dropdata = getDropData(event);
				if (dropdata instanceof DraggedColorObject
						&& event.item != null
						&& event.item.getData() instanceof ColorResourceTableCategory) {
					event.detail = DND.DROP_COPY;
					drawDragOverHighlight(event.item);
					return;
				} else {
					super.dragOver(event);
				}
				removeDragOverHighlight(event.item);
			}

		};

		DropTarget target = new DropTarget(table, DND.DROP_COPY
				| DND.DROP_DEFAULT | DND.DROP_MOVE);
		target.setTransfer(transfers);
		target.addDropListener(dropListener);
	}

	private void clearSelection() {
		selectedCategory = null;
		selectedGroup = null;
		selectedItem = null;
	}

	public void createCompositeArea(Composite parent) {
		inputs = createInput2(null);

		table = new Table(parent, SWT.FULL_SELECTION | SWT.HIDE_SELECTION
				| SWT.H_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gd);
		table.setBackground(ColorConstants.menuBackground);
		// table.setBackground(ColorConstants.orange);
		table.setLinesVisible(false);
		table.setHeaderVisible(false);
		addDragDropSupport(table);

		// 1st column
		final TableColumn column0 = new TableColumn(table, SWT.CENTER, 0);

		column0.setWidth(COLUMN0_WIDTH);

		// 2nd column
		final TableColumn column = new TableColumn(table, SWT.CENTER, 1);

		column.setWidth(COLUMN1_WIDTH);

		// 3rd column
		final TableColumn column2 = new TableColumn(table, SWT.CENTER, 2);
		column2.setWidth(COLUMN2_WIDTH);

		// 4th column
		final TableColumn column3 = new TableColumn(table, SWT.CENTER, 3);

		fillTable();

		Listener paintListener = new Listener() {
			Boolean itemIsSelected;

			public void handleEvent(Event event) {
				GC gc = event.gc;
				TableItem item = (TableItem) event.item;
				Color foreground = gc.getForeground();
				Color background = gc.getBackground();
				gc.setForeground(FG_TEXT_COLOR);

				itemIsSelected = false;
				// gc.setBackground(BG_COLOR);

				switch (event.type) {

				case SWT.MeasureItem: {
					if (item.getData() instanceof ResourceTableMasterGroup) {
						event.height = ROW_HEIGHT;
					}
					if (event.index == 3) {
						int clientWidth = table.getClientArea().width;
						event.width = 2 * clientWidth;
						column3.setWidth(table.getBounds().width - event.x);
					}
					if (table.getItemCount() > 0
							&& table.getItem(table.getItemCount() - 1) != item) {
						// strange logic here, if breaks on the last item, the
						// last line is not drawn
						break;
					}
				}
				case SWT.PaintItem: {

					if (item.getData() instanceof ResourceTableInput) {
						gc.setBackground(item.getBackground());
					}

					if (item.getData() instanceof ResourceColorBoxesLine) {
						gc.setBackground(item.getBackground());
						drawColorBoxesLine(gc, event,
								(ResourceColorBoxesLine) item.getData());
						return;
					}
					if (event.index == 0) {
						// gc.setBackground(item.getBackground());
						if (item.getData() == selectedCategory
								&& (item.getData() instanceof ColorResourceTableCategory || (item
										.getData() instanceof ResourceTableCategory))) {

							itemIsSelected = true;
							gc.setBackground(BG_SELECTED_COLOR);
							gc.fillRectangle(event.x, event.y, table
									.getBounds().width, event.height);
						} else if (item.getData() == selectedItem
								|| item.getData() == selectedGroup) {

							itemIsSelected = true;
							gc.setBackground(BG_SELECTED_COLOR);
							gc.fillRectangle(event.x, event.y, table
									.getBounds().width, event.height);
						} else {
							gc.setBackground(item.getBackground());
							gc.fillRectangle(event.x, event.y, table
									.getBounds().width, event.height);
						}
						drawCheckIcon(gc, event, item.getData());
					}

					if (event.index == 2) {
						gc.setBackground(item.getBackground());
						if (item.getData() == selectedCategory
								&& (item.getData() instanceof ColorResourceTableCategory || (item
										.getData() instanceof ResourceTableCategory))) {

							itemIsSelected = true;
							gc.setBackground(BG_SELECTED_COLOR);
							gc.fillRectangle(event.x, event.y, table
									.getBounds().width, event.height);
						} else if (item.getData() == selectedItem
								|| item.getData() == selectedGroup) {
							itemIsSelected = true;
							gc.setBackground(BG_SELECTED_COLOR);
							gc.fillRectangle(event.x, event.y, table
									.getBounds().width, event.height);
						} else {
							gc.setBackground(item.getBackground());
							gc.fillRectangle(event.x, event.y, table
									.getBounds().width, event.height);
						}
						if (item.getData() instanceof ColorTableItem) {

							drawColorItemBox(gc, event, (ColorTableItem) item
									.getData());

						}

					}

					if (event.index == 3) {
						gc.setBackground(item.getBackground());
						if (item.getData() == selectedCategory
								&& (item.getData() instanceof ColorResourceTableCategory || (item
										.getData() instanceof ResourceTableCategory))) {
							itemIsSelected = true;
							gc.setBackground(BG_SELECTED_COLOR);
							gc.fillRectangle(event.x, event.y, table
									.getBounds().width, event.height);
						} else if (item.getData() == selectedItem
								|| item.getData() == selectedGroup) {
							itemIsSelected = true;
							gc.setBackground(BG_SELECTED_COLOR);
							gc.fillRectangle(event.x, event.y, table
									.getBounds().width, event.height);
						} else {
							gc.setBackground(item.getBackground());
							gc.fillRectangle(event.x, event.y, table
									.getBounds().width, event.height);
						}
						drawPin(gc, event, item.getData());

					}

					if (event.index == 1) {
						// if(table.getSelection()!=null&&table.getSelection().length>0&&
						// table.getSelection()[0]==item){
						// return;
						// }

						if (item.getData() == selectedCategory
								&& (item.getData() instanceof ColorResourceTableCategory || (item
										.getData() instanceof ResourceTableCategory))) {

							itemIsSelected = true;
							gc.setBackground(BG_SELECTED_COLOR);
							gc.fillRectangle(event.x, event.y, table
									.getBounds().width, event.height);
						} else if (item.getData() == selectedItem
								|| item.getData() == selectedGroup) {
							itemIsSelected = true;
							gc.setBackground(BG_SELECTED_COLOR);
							gc.fillRectangle(event.x, event.y, table
									.getBounds().width, event.height);
						} else {
							gc.setBackground(item.getBackground());
							gc.fillRectangle(event.x, event.y, table
									.getBounds().width, event.height);
						}
						drawMainText(gc, event, item.getData());
						drawIconToResourceTableCategory(gc, event, item
								.getData());

					}
					gc.setForeground(FG_LINE_COLOR);
					gc.drawLine(event.x, event.y, event.x
							+ table.getBounds().width, event.y);
					gc.drawLine(event.x, event.y + event.height, event.x
							+ table.getBounds().width, event.y + event.height);

				}
				}
				gc.setForeground(background);
				gc.setBackground(foreground);
			}

			/**
			 *
			 * 
			 * @param gc
			 * @param event
			 * @param objData
			 */
			private void drawIconToResourceTableCategory(GC gc, Event event,
					Object objData) {
				if (objData instanceof ResourceTableCategory) {
					ResourceTableCategory cat = (ResourceTableCategory) objData;

					int offset = COLUMN0_WIDTH;
					offset += TOP_TREE_ELEMENT_SHIFT;
					if (cat.isVisible()) {
						if (cat.getImageData() != null) {
							ImageData imageData = cat.getImageData();

							int y = event.y + (event.height - imageData.height)
									/ 2;

							Image img = new Image(null, imageData);
							gc.drawImage(img, event.x + offset - COLUMN0_WIDTH,
									y);
							img.dispose();
						}
					}
				}

			}

			private void drawMainText(GC gc, Event event, Object objData) {
				int offset = COLUMN0_WIDTH;
				String text = "";

				Font oldFont = gc.getFont();
				Font tempFont = null;

				ImageData data = null;
				if (objData instanceof ResourceTableCategory) {
					ResourceTableCategory cat = (ResourceTableCategory) objData;
					text = cat.getName();
					offset += TREE_SUBELEMENT_SHIFT + TOP_TREE_ELEMENT_SHIFT;
					if (cat.isVisible()) {
						if (cat.getItems() != null && cat.getItems().size() > 0) {
							if (((ResourceTableCategory) objData).isExtracted()) {
								data = getTreeExpandedDescriptor()
										.getImageData();

							} else {
								data = getTreeCollapsedDescriptor()
										.getImageData();
							}
						}
					} else {
						return;
					}

				}
				if (objData instanceof ResourceTableMasterGroup) {
					text = ((ResourceTableMasterGroup) objData)
							.getMasterGroupName();
					offset += TOP_TREE_ELEMENT_SHIFT;
					if (((ResourceTableMasterGroup) objData).isExtracted()) {
						data = getTreeExpandedDescriptor().getImageData();

					} else {
						data = getTreeCollapsedDescriptor().getImageData();
					}

					FontData fd = JFaceResources.getDefaultFont().getFontData()[0];
					tempFont = new Font(Display.getDefault(), fd.getName(), fd
							.getHeight(), SWT.BOLD);
					gc.setFont(tempFont);
				}

				if (objData instanceof ResourceTableItem) {
					ResourceTableItem item = (ResourceTableItem) objData;
					if (item.isVisible()) {

						text = ((ResourceTableItem) objData).getName();
						offset += TOP_TREE_ELEMENT_SHIFT + 2
								* TREE_SUBELEMENT_SHIFT;
						int positionInParentCategory = item.getParent()
								.getItems().indexOf(item);
						if (positionInParentCategory != item.getParent()
								.getItems().size() - 1) {
							gc.setLineDash(new int[] { 1, 1 });
							gc.drawLine(event.x + offset + 3 - 2
									* TREE_SUBELEMENT_SHIFT + 8, event.y,
									event.x + 3 + offset - 2
											* TREE_SUBELEMENT_SHIFT + 8,
									event.y + event.height);
							gc.drawLine(event.x + offset + 3 - 2
									* TREE_SUBELEMENT_SHIFT + 8, event.y
									+ event.height / 2, event.x + offset - 3,
									event.y + event.height / 2);
							gc.setLineStyle(SWT.LINE_SOLID);
						} else {
							gc.setLineDash(new int[] { 1, 1 });
							gc.drawLine(event.x + offset + 3 - 2
									* TREE_SUBELEMENT_SHIFT + 8, event.y,
									event.x + 3 + offset - 2
											* TREE_SUBELEMENT_SHIFT + 8,
									event.y + event.height / 2);
							gc.drawLine(event.x + offset + 3 - 2
									* TREE_SUBELEMENT_SHIFT + 8, event.y
									+ event.height / 2, event.x + offset - 3,
									event.y + event.height / 2);
							gc.setLineStyle(SWT.LINE_SOLID);
						}
					} else {
						return;
					}
				}

				if (objData instanceof ColorResourceTableCategory) {
					ColorResourceTableCategory colorCat = (ColorResourceTableCategory) objData;
					int positionInParentCategory = colorCat.getParent()
							.getCategories().indexOf(colorCat);
					if (positionInParentCategory != colorCat.getParent()
							.getCategories().size() - 1) {
						gc.setLineDash(new int[] { 1, 1 });
						gc.drawLine(event.x + offset - COLUMN0_WIDTH, event.y,
								event.x + offset - COLUMN0_WIDTH, event.y
										+ event.height);
						gc.drawLine(event.x + offset - COLUMN0_WIDTH, event.y
								+ event.height / 2, event.x + offset - 3,
								event.y + event.height / 2);
						gc.setLineStyle(SWT.LINE_SOLID);
					} else {
						gc.setLineDash(new int[] { 1, 1 });
						gc.drawLine(event.x + offset - COLUMN0_WIDTH, event.y,
								event.x + offset - COLUMN0_WIDTH, event.y
										+ event.height / 2);
						gc.drawLine(event.x + offset - COLUMN0_WIDTH, event.y
								+ event.height / 2, event.x + offset - 3,
								event.y + event.height / 2);
						gc.setLineStyle(SWT.LINE_SOLID);
					}
				}

				gc.setForeground(ColorConstants.gray);
				gc.drawLine(event.x, event.y, event.x + column.getWidth(),
						event.y);
				gc.drawLine(event.x, event.y + event.height, event.x
						+ column.getWidth(), event.y + event.height);

				gc.setForeground(itemIsSelected ? FG_SELECTED_TEXT_COLOR
						: FG_TEXT_COLOR);

				if (data != null) {

					int y = event.y + (event.height - data.height) / 2;

					Image img = new Image(null, data);
					gc.drawImage(img, event.x + offset - COLUMN0_WIDTH, y);
					img.dispose();
				}

				final Point extent = event.gc.stringExtent(text);
				int y = event.y + (event.height - extent.y) / 2;

				gc.drawText(text, event.x + offset, y, true);
				gc.setFont(oldFont);
				if (tempFont != null && !tempFont.isDisposed()) {
					tempFont.dispose();
				}

			}

			private void drawPin(GC gc, Event event, Object data) {
				ImageData imgData = null;

				if (data instanceof ResourceTableCategory) {
					if (((ResourceTableCategory) data == selectedCategory)) {

						imgData = getPinIconDescriptor().getImageData();
					}

				}

				if (imgData != null) {

					// gc.setBackground(bgSelectedColor);
					gc.fillRectangle(event.x, event.y, COLUMN3_WIDTH,
							event.height);

					// pin removed
					// int y = event.y + (event.height - imgData.height) / 2;
					// Image img = new Image(null, imgData);
					// gc.drawImage(img, event.x, y);
					// img.dispose();
				}
				gc.setForeground(FG_LINE_COLOR);
				gc.drawLine(event.x, event.y, event.x + event.width, event.y);
				gc.drawLine(event.x, event.y + event.height, event.x
						+ event.width, event.y + event.height);

			}

			// drawing method
			private void drawColorItemBox(GC gc, Event event,
					ColorTableItem item) {
				int y = event.y + (event.height - COLOR_BOX_SIZE_OUTER) / 2;
				Color backgroundColor = gc.getBackground();
				RGB rgb = item.getRGB();
				if (rgb != null && item.isLinked()) {
					gc.setAntialias(SWT.ON);
					Color color = new Color(null, rgb);
					gc.setBackground(FG_TEXT_COLOR);

					gc.fillOval(event.x + 1, y, COLOR_BOX_SIZE_OUTER,
							COLOR_BOX_SIZE_OUTER);

					gc.setBackground(ColorConstants.white);

					gc.fillOval(event.x + 2, y + 1, COLOR_BOX_SIZE_MIDDLE,
							COLOR_BOX_SIZE_MIDDLE);

					gc.setBackground(color);

					gc.fillOval(event.x + 3, y + 2, COLOR_BOX_SIZE_INNER,
							COLOR_BOX_SIZE_INNER);
					color.dispose();

				} else if (rgb != null && !item.isLinked()) {
					Color color = new Color(null, rgb);
					gc.setBackground(FG_TEXT_COLOR);
					gc.fillRoundRectangle(event.x + 1, y, COLOR_BOX_SIZE_OUTER,
							COLOR_BOX_SIZE_OUTER, COLOR_BOX_ROUND_ANGLE,
							COLOR_BOX_ROUND_ANGLE);

					gc.setBackground(ColorConstants.white);
					gc.fillRectangle(event.x + 2, y + 1, COLOR_BOX_SIZE_MIDDLE,
							COLOR_BOX_SIZE_MIDDLE);

					gc.setBackground(color);
					gc.fillRectangle(event.x + 3, y + 2, COLOR_BOX_SIZE_INNER,
							COLOR_BOX_SIZE_INNER);
					color.dispose();

				} else {

					gc.setBackground(ColorConstants.lightGray);
					gc.fillRoundRectangle(event.x + 1, y, COLOR_BOX_SIZE_OUTER,
							COLOR_BOX_SIZE_OUTER, COLOR_BOX_ROUND_ANGLE,
							COLOR_BOX_ROUND_ANGLE);

					gc.setBackground(ColorConstants.lightGray);
					gc.fillRectangle(event.x + 2, y + 1, COLOR_BOX_SIZE_MIDDLE,
							COLOR_BOX_SIZE_MIDDLE);

					gc.setBackground(ColorConstants.lightGray);
					gc.fillRectangle(event.x + 3, y + 2, COLOR_BOX_SIZE_INNER,
							COLOR_BOX_SIZE_INNER);
				}

				gc.setBackground(backgroundColor);

			}

			// drawing method
			private void drawCheckIcon(GC gc, Event event, Object item) {
				ImageData data = null;

				if (item instanceof ResourceTableCategory) {
					if (((ResourceTableCategory) item).isVisible()) {
						if (((ResourceTableCategory) item).isSkinned()) {
							data = getCheckDescriptor().getImageData();
						} else if (((ResourceTableCategory) item)
								.isHalfSkinned()) {
							data = getHalfCheckDescriptor().getImageData();
						} else {
							// data = getNotCheckDescriptor().getImageData();
						}
					} else {
						return;
					}

				}
				if (item instanceof ResourceTableMasterGroup) {
					if (((ResourceTableMasterGroup) item).isSkinned()) {
						data = getCheckDescriptor().getImageData();
					} else if (((ResourceTableMasterGroup) item)
							.isHalfSkinned()) {
						data = getHalfCheckDescriptor().getImageData();
					} else {
						if (((ResourceTableMasterGroup) item)
								.getAssociatedContentData() != null) {
							if (((ResourceTableMasterGroup) item).getAssociatedContentData().getImageDescriptor(16, 16)!=null){
							data = ((ResourceTableMasterGroup) item)
									.getAssociatedContentData()
									.getImageDescriptor(16, 16).getImageData();
							}
						} else {
							// data = getNotCheckDescriptor().getImageData();
						}
					}
				}

				if (item instanceof ResourceTableItem) {
					if (((ResourceTableItem) item).isVisible()) {
						if (((ResourceTableItem) item).isSkinned()) {
							data = getCheckDescriptor().getImageData();
						} else if (((ResourceTableItem) item).isHalfSkinned()) {
							data = getHalfCheckDescriptor().getImageData();
						} else {
							// data = getNotCheckDescriptor().getImageData();
						}
					} else {
						return;
					}
				}
				gc.fillRectangle(0, event.y, column0.getWidth(), event.height);
				if (data != null) {
					int y = event.y + (event.height - data.height) / 2;
					Image img = new Image(null, data);
					if (item instanceof ResourceTableMasterGroup)
						gc.drawImage(img, event.x, y);
					else
						gc.drawImage(img, event.x + 3, y);
					img.dispose();
				}
				gc.setForeground(FG_LINE_COLOR);
				gc.drawLine(event.x - 4, event.y, column0.getWidth(), event.y);
				gc.drawLine(event.x - 4, event.y + event.height, column0
						.getWidth(), event.y + event.height);

			}

			// drawing method
			private void drawColorBoxesLine(GC gc, Event event,
					ResourceColorBoxesLine line) {
				String string = line.getName();
				final Point extent = event.gc.stringExtent(string);

				int column1Offset = 0;
				if (event.index == 2) {
					// this helps to simulate column span
					column1Offset = COLUMN1_WIDTH;
				}
				int y = event.y + (event.height - extent.y) / 2;

				if ((event.index == 1) || event.index == 2) {
					int offset = TREE_SUBELEMENT_SHIFT + TOP_TREE_ELEMENT_SHIFT
							- column1Offset;

					int i = 0;
					List<RGB> rgbList = line.getRGBList();
					for (RGB rgb : rgbList) {

						// for linked colors draw oval
						if (line.getLinkedRGBList() != null
								&& line.getLinkedRGBList().contains(rgb)) {
							gc.setAntialias(SWT.ON);
							Color color = new Color(null, rgb);
							gc.setBackground(FG_TEXT_COLOR);

							gc.fillOval(event.x + 1 + i * COLOR_CELL_OFFSET
									+ offset, y + 1, COLOR_BOX_SIZE_OUTER,
									COLOR_BOX_SIZE_OUTER);

							gc.setBackground(ColorConstants.white);

							gc.fillOval(event.x + 2 + i * COLOR_CELL_OFFSET
									+ offset, y + 2, COLOR_BOX_SIZE_MIDDLE,
									COLOR_BOX_SIZE_MIDDLE);

							gc.setBackground(color);

							gc.fillOval(event.x + 3 + i * COLOR_CELL_OFFSET
									+ offset, y + 3, COLOR_BOX_SIZE_INNER,
									COLOR_BOX_SIZE_INNER);
							color.dispose();
						} else { // for not linked colors draw rounded
							// rectangles
							Color color = new Color(null, rgb);
							gc.setBackground(FG_TEXT_COLOR);
							gc.fillRoundRectangle(event.x + 1 + i
									* COLOR_CELL_OFFSET + offset, y + 1,
									COLOR_BOX_SIZE_OUTER, COLOR_BOX_SIZE_OUTER,
									COLOR_BOX_ROUND_ANGLE,
									COLOR_BOX_ROUND_ANGLE);

							gc.setBackground(ColorConstants.white);
							gc.fillRectangle(event.x + 2 + i
									* COLOR_CELL_OFFSET + offset, y + 2,
									COLOR_BOX_SIZE_MIDDLE,
									COLOR_BOX_SIZE_MIDDLE);

							gc.setBackground(color);

							gc.fillRectangle(event.x + 3 + i
									* COLOR_CELL_OFFSET + offset, y + 3,
									COLOR_BOX_SIZE_INNER, COLOR_BOX_SIZE_INNER);

							color.dispose();
						}

						i++;
					}
					gc.setBackground(table.getBackground());
				}

			}
		};

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent event) {
				Table table = (Table) event.widget;
				TableItem item = table.getItem(new Point(event.x, event.y));
				if (item == null) {
					clearSelection();
					// select root
					if (table.getItem(0).getData() instanceof ResourceTableMasterGroup) {
						ResourceTableMasterGroup rtmg = (ResourceTableMasterGroup) table
								.getItem(0).getData();
						if (rtmg != null
								&& rtmg.getAssociatedContentData() != null
								&& rtmg.getAssociatedContentData().getParent() != null) {
							setSelectionWithoutUpdatingListeners(new StructuredSelection(
									rtmg.getAssociatedContentData().getParent()));
						}
					}
					return;
				}
				if (event.button == SWT.MouseDown) { // right click serves
					// for context menu
					if (item.getData() instanceof ResourceTableCategory) {
						ResourceTableCategory cat = (ResourceTableCategory) item
								.getData();
						
						// only set selection for the context menu
						setSelectionWithoutUpdatingListeners(new StructuredSelection(
								cat.getAssociatedContentData()));
						return;
					} else if (item.getData() instanceof ResourceTableItem) {
						ResourceTableItem selectedItem = (ResourceTableItem) item
								.getData();
						
						// only set selection for the context menu
						setSelectionWithoutUpdatingListeners(new StructuredSelection(
								selectedItem.getAssociatedContentData()));
						return;
					} else if (item.getData() instanceof ResourceTableMasterGroup) {
						ResourceTableMasterGroup mg = (ResourceTableMasterGroup) item
								.getData();
						setSelectionWithoutUpdatingListeners(new StructuredSelection(
								mg.getAssociatedContentData()));
						return;
					} else {
						setSelection(null);
						return;
					}
				}
				if (!item.isDisposed()
						&& item.getData() instanceof ResourceTableCategory) {
					if (event.x > COLUMN0_WIDTH + TOP_TREE_ELEMENT_SHIFT + 15
							&& event.x < (COLUMN0_WIDTH
									+ TOP_TREE_ELEMENT_SHIFT + 30)) {
						// category is selected, call execute category selection
						ResourceTableCategory cat = (ResourceTableCategory) item
								.getData();

						if (!(cat instanceof ColorResourceTableCategory)) {
							if (cat.isExtracted()) {
								cat.setExtracted(false);
							} else {
								if (selectedCategory != null)
									selectedCategory.setExtracted(false);
								cat.setExtracted(true);
								clearSelection();
								selectedCategory = cat;
							}
						} else {
							clearSelection();
							selectedCategory = cat;
						}

						refreshKeepingIndex();

					} else if (event.x > COLUMN0_WIDTH + TOP_TREE_ELEMENT_SHIFT
							+ 30
							&& event.x < (COLUMN0_WIDTH + COLUMN1_WIDTH)) {
						ResourceTableCategory cat = (ResourceTableCategory) item
								.getData();

						if (!(cat instanceof ColorResourceTableCategory)) {
							if (cat.getAssociatedContentData() != null) {
								clearSelection();
								selectedCategory = cat;
								if (cat.getAssociatedContentData()
										.getAllChildren().length > 0) {
									if (cat.getAssociatedContentData()
											.getChildren()[0] != null) {
										StructuredSelection selection = new StructuredSelection(
												cat.getAssociatedContentData()
														.getChildren()[0]);
										setSelection(selection);

										IResourceViewerSelectionHelper2 helper = getResourceSelectionHelper(cat
												.getAssociatedContentData()
												.getChildren()[0]);
										helper.executeSelect(sourceEditor, cat,
												SELECT_ITEM, null);
									}
								} else {
									StructuredSelection selection = new StructuredSelection(
											cat.getAssociatedContentData());
									setSelection(selection);
								}
							} else
								setSelection(null);

							refreshKeepingIndex();
						} else {
							clearSelection();
							StructuredSelection selection = new StructuredSelection(
									cat.getAssociatedContentData()
											.getChildren()[0]);
							clearSelection();
							selectedCategory = cat;
							setSelection(selection);

							refreshKeepingIndex();
						}

					}
				} else if (!item.isDisposed()
						&& item.getData() instanceof ResourceTableItem) {
					if (event.x > COLUMN0_WIDTH
							&& event.x < (COLUMN0_WIDTH + COLUMN1_WIDTH)) {
						// item is selected, call execute item selection
						ResourceTableItem resItem = (ResourceTableItem) item
								.getData();
						// IResourceViewerSelectionHelper2 helper =
						// getResourceSelectionHelper(resItem
						// .getAssociatedContentData());

						clearSelection();
						selectedItem = resItem;
						// helper
						// .executeSelect(sourceEditor, resItem,
						// SELECT_ITEM);

						if (resItem.getAssociatedContentData().getAllChildren().length > 0) {
							StructuredSelection selection = new StructuredSelection(
									resItem.getAssociatedContentData()
											.getChildren()[0]);
							setSelection(selection);
						} else
							setSelection(null);
						refreshKeepingIndex();

					}

				} else if (!item.isDisposed()
						&& item.getData() instanceof ResourceTableMasterGroup) {
					if (event.x > COLUMN0_WIDTH
							&& event.x < (COLUMN0_WIDTH
									+ TOP_TREE_ELEMENT_SHIFT + 15)) {
						// group is expanded/collapsed, 
						// tree to show/hide child elements
						ResourceTableMasterGroup grp = (ResourceTableMasterGroup) item
								.getData();
						if (grp.isExtracted()) {
							grp.setExtracted(false);
						} else {
							grp.setExtracted(true);
							// collapse other expanded group
							for (ResourceTableMasterGroup group : inputs.get(0)
									.getGroups()) {
								if (group != grp) {
									group.setExtracted(false);
								}
							}

						}
						// IResourceViewerSelectionHelper2 helper =
						// getResourceSelectionHelper(grp
						// .getAssociatedContentData());
						// helper.executeSelect(sourceEditor, grp,
						// SELECT_ITEM);
						// if(grp.getAssociatedContentData()!=null&&grp.getAssociatedContentData().getChildren()[0]!=null){
						// StructuredSelection selection= new
						// StructuredSelection(grp.getAssociatedContentData().getChildren()[0]);
						// setSelection(selection);
						// }
						//						
						refreshKeepingIndex();

					} else if (event.x > (COLUMN0_WIDTH
							+ TOP_TREE_ELEMENT_SHIFT + 15)
							&& event.x < (COLUMN0_WIDTH + COLUMN1_WIDTH)) {
						// group is expanded/collapsed, 
						// tree to show/hide child elements
						ResourceTableMasterGroup grp = (ResourceTableMasterGroup) item
								.getData();

						if (grp.getAssociatedContentData() != null) {
							StructuredSelection selection = null;
							clearSelection();
							selectedGroup = grp;
							if (grp.getAssociatedContentData().getAllChildren().length > 0) {
								if (grp.getAssociatedContentData()
										.getChildren()[0] != null) {
									selection = new StructuredSelection(grp
											.getAssociatedContentData()
											.getChildren()[0]);
								}
							} else {
								selection = new StructuredSelection(grp
										.getAssociatedContentData());
							}
							setSelection(selection);
						} else
							setSelection(null);
						refreshKeepingIndex();

					}
				} else if (!item.isDisposed()
						&& item.getData() instanceof ResourceColorBoxesLine) {
					// item with color boxes
					int positionOffset = COLUMN0_WIDTH + TOP_TREE_ELEMENT_SHIFT
							+ TREE_SUBELEMENT_SHIFT;
					int offset = (event.x - positionOffset) % COLOR_CELL_OFFSET;
					int position = (event.x - positionOffset)
							/ COLOR_CELL_OFFSET;

					ResourceColorBoxesLine sep = (ResourceColorBoxesLine) item
							.getData();
					if (position >= 0 && position < sep.getRGBList().size()
							&& offset < COLOR_BOX_SIZE_OUTER - 1 && offset >= 0) {
						sep.setSelectedRGB(sep.getRGBList().get(position));
						IResourceViewerSelectionHelper2 helper = getResourceSelectionHelper(sep
								.getParent().getAssociatedContentData());

						CssColorDialog dialog = new CssColorDialog(PlatformUI
								.getWorkbench().getActiveWorkbenchWindow()
								.getShell());
						dialog.setRGBString(ColorUtil.asHashString(sep
								.getSelectedRGB()));

						if (dialog.open() == CssColorDialog.CANCEL) {
							return;
						}
						RGB newRGB = dialog.getRGB();

						helper.executeSelect(sourceEditor, sep, CHANGE_COLOR,
								newRGB);
						sep.setSelectedRGB(null);
						table.redraw();

					}

				}
				if (!item.isDisposed()
						&& item.getData() instanceof ColorTableItem) {
					if (event.x > (COLUMN0_WIDTH + COLUMN1_WIDTH)
							&& event.x < (COLUMN0_WIDTH + COLUMN1_WIDTH + COLUMN3_WIDTH)) {
						// color box next to category was selected
						ColorTableItem colorItem = (ColorTableItem) item
								.getData();

						if (colorItem instanceof ColorResourceTableCategory) {
							ColorResourceTableCategory colorCat = (ColorResourceTableCategory) colorItem;
							IContentData data = colorCat
									.getAssociatedContentData();
							if (colorCat != null && colorCat.getRGB() != null) {
								String hashColor = ColorUtil
										.asHashString(colorCat.getRGB());
								CssColorDialog dialog = new CssColorDialog(
										PlatformUI.getWorkbench()
												.getActiveWorkbenchWindow()
												.getShell());
								dialog.setRGBString(hashColor);

								if (dialog.open() == CssColorDialog.CANCEL) {
									return;
								}
								RGB newRGB = dialog.getRGB();

								IResourceViewerSelectionHelper2 helper = getResourceSelectionHelper(data);
								helper.executeSelect(sourceEditor, colorCat,
										CHANGE_COLOR, newRGB);
							}
						}
						table.redraw();
					}

				}

			}
		});
		table.addListener(SWT.MeasureItem, paintListener);
		table.addListener(SWT.PaintItem, paintListener);
		table.addControlListener(new ControlListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
			 */
			public void controlMoved(ControlEvent e) {
				update3rdColumn(e);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
			 */
			public void controlResized(ControlEvent e) {
				update3rdColumn(e);
			}

			private void update3rdColumn(ControlEvent e) {
				if (e.widget == table && table.getItemCount() > 0) {
					TableItem the3rdOne = table.getItem(0);
					if (the3rdOne != null) {
						Rectangle rect = the3rdOne.getBounds(3);
						if (null != rect)
							column3.setWidth(table.getBounds().width - rect.x);
					}
				}
			}
		});

		/* add copy element(s) action */
		MenuManager mmgr = new MenuManager();
		mmgr.setRemoveAllWhenShown(true);

		for (IResourceSection2 section : resourceSections) {
			IMenuListener listener = section.createResourceViewerMenuListener(
					this, getCommandStack());
			if (listener != null) {
				mmgr.addMenuListener(listener);
			}
		}
		Menu menu = mmgr.createContextMenu(table);
		table.setMenu(menu);
		table.setToolTipText(""); // Fix for a dummy tool tip coming in the resource view and occupying the space over the editor as well.

	}

	/**
	 * Special case of refreshing table used when colorizing using tooltip
	 * without previous selection of the color element. It prevents table from
	 * collapsing.
	 * 
	 * @param extractedGroupName
	 */
	private void refreshKeepingIndex(String extractedGroupName) {
		int index = table.getTopIndex();
		table.setRedraw(false);
		table.removeAll();
		if ("" != extractedGroupName) {
			for (ResourceTableMasterGroup grp : inputs.get(0).getGroups()) {
				if (grp.getMasterGroupName().equals(extractedGroupName)) {
					grp.setExtracted(true);
					for (IResourceTableCategoryItem item : grp.getCategories()) {
						if (item instanceof ColorResourceTableCategory) {
							((ColorResourceTableCategory) item)
									.setExtracted(true);
						}
					}
					break;
				}
			}
		}
		fillTable();
		table.setTopIndex(index);
		table.setRedraw(true);
		table.redraw();
	}

	private void refreshKeepingIndex() {
		int selectionIndex = table.getSelectionIndex();
		table.setRedraw(false);
		table.removeAll();

		fillTable();
		table.setTopIndex(selectionIndex);
		table.setSelection(selectionIndex);
		table.setRedraw(true);
		
		table.redraw();
	}

	public IResourceViewerSelectionHelper2 getResourceSelectionHelper(
			IContentData data) {
		IResourceViewerSelectionHelper2 helper = null;
		if (data != null) {
			for (IResourceSection2 section : this.resourceSections) {
				helper = section.getSelectionHelper(data);
				if (helper != null) {
					return helper;
				}
			}
		}
		return helper;
	}

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
				if (part == sourceEditor
						|| (part instanceof ContentOutlineViewPart)
						|| part == ResourceViewPage.this) {
					
					suppressViewerEvents = true;
					try {
						showSelection((IStructuredSelection) selection);
					} finally {
						suppressViewerEvents = false;
					}
				}
			}
		};

		
		// contribute toolbar toggle buttons
		

		IToolBarManager bars = pageSite.getActionBars().getToolBarManager();
		Action toggleSync = new WorkbenchPartAction(null,
				WorkbenchPartAction.AS_CHECK_BOX) {
			@Override
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.Page#dispose()
	 */
	@Override
	public void dispose() {
		getSite().getPage().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(synchronizeListener);
		IContentService service = (IContentService) sourceEditor
				.getAdapter(IContentService.class);
		if (service != null) {
			service.removeContentListener(this);
		}
		super.dispose();
	}

	public void refresh() {
		if (!suppressOpenEditorEvents) {

			if (Platform.getProduct().getId().equals(
					"com.nokia.tools.carbide.ui.licensee.product")) {
				String selectedTemp = selectedIAL.getText();
				selectedIAL = null;
				ials.clear();
				for (Control c : form.getChildren()) {
					c.dispose();
				}
				for (IResourceSection2 section : resourceSections) {
					List<ImageLabel> items = section.getNavigationControlItems(
							form, getContentAdapter());
					for (ImageLabel label : items) {
						label
								.setUnselectedBackground(ColorConstants.listBackground);
						label.setFillBackground(true);
						label.setSelectedTextColor(ColorConstants.white);
						label
								.setUnselectedTextColor(ColorConstants.menuForeground);
						ials.add(label);
					}
				}



				for (ImageLabel label : ials) {
					label.addSelectionListener(this);
					label.addKeyListener(this);
					final ThumbListener thumbListener = new ThumbListener(label);
					label.addListener(SWT.MouseMove, thumbListener);
					label.addListener(SWT.MouseExit, thumbListener);
					label.setCursor(getSite().getShell().getDisplay()
							.getSystemCursor(SWT.CURSOR_HAND));
				}

				for (ImageLabel tempIal : ials) {
					if (tempIal.getText().equals(selectedTemp)) {
						selectedIAL = tempIal;
						selectedIAL.setSelected(true);
						break;
					}
				}

				if (selectedIAL == null) {
					if (ials.get(0) != null) {
						this.selectedIAL = ials.get(0);
						ials.get(0).setSelected(true);
					}
				}

				formComposite.setContent(form);
				form.setSize(form.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				formComposite.layout(true, true);

			}

			if (selectedIAL != null) {
				inputs = createInput2(selectedIAL);
			} else {
				inputs = createInput2(null);
			}
			refreshKeepingIndex();
			
			// viewer.reset(contentAdapter);
		}
	}

	public CommandStack getCommandStack() {
		return (CommandStack) sourceEditor.getAdapter(CommandStack.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.Page#setFocus()
	 */
	@Override
	public void setFocus() {
		//viewer.getControl().setFocus();
	}

	private IContentData findModelItem(Object data) {
		List<IContentData> modelItems = new ArrayList<IContentData>();
		IContent[] contents = getContentAdapter().getContents();
		for (int i = 0; i < contents.length; i++) {
			IContent root = contents[i];
			IContentData item = JEMUtil.getContentData(data);
			String targetId = null;
			if (item != null) {
				root = item.getRoot();

				
				IContentData group = null;
				if (selection instanceof StructuredSelection) {
					Object selectedObject = ((StructuredSelection) selection)
							.getFirstElement();
					if (selectedObject instanceof IContentData) {
						group = ((IContentData) selectedObject).getParent();
					}
				}
				if (group == null && selectedItem != null) {
					group = selectedItem.getAssociatedContentData();
				}
				if (group == null && selectedCategory != null) {
					group = selectedCategory.getAssociatedContentData();
				}
				if (group != null) {
					ICategoryAdapter adapter = (ICategoryAdapter) item
							.getAdapter(ICategoryAdapter.class);
					if (adapter != null) {
						for (IContentData peer : adapter.getCategorizedPeers()) {
							if (peer.getParent() == group) {
								targetId = peer.getId();
								break;
							}
						}
					}
				}
			}
			if (targetId == null) {
				if (item != null) {
					targetId = item.getId();
				} else if (data instanceof String) {
					targetId = (String) data;
				}
			}

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
	 * Sets all items in the table as collapsed. <br/>
	 * <br/>
    *  or additional logic, items are marked as collapsed.  
	 
	 */
	private void setInputAsCollapsed() {
		for (ResourceTableInput input : inputs) {
			for (ResourceTableMasterGroup mg : input.getGroups()) {
				mg.setExtracted(false);
				for ( IResourceTableCategoryItem item : mg.getCategories()) {
					if (item instanceof ResourceTableCategory) {
						ResourceTableCategory rtc = ((ResourceTableCategory) item);
						rtc.setExtracted(false);
					}
				}
			}			
		}		
	}	
	
	/**
	 * shows and selects element (group), that contains item in selection
	 * 
	 * @param selection
	
	 */
	public void showSelection(IStructuredSelection sel) {
		if (inputs == null)
			return;
		if (!sel.isEmpty() && sel.getFirstElement() instanceof ScreenEditorPart) {
			return;
		}

		Object data = sel.isEmpty() ? null : sel.getFirstElement();
		if (sel.getFirstElement() instanceof DiagramGraphicalEditPart) {
			data = ((DiagramGraphicalEditPart) sel.getFirstElement())
					.getChildren().get(0);
		}
		// List<Object> iconViewSel = new ArrayList<Object>();
		IContentData modelItem = findModelItem(data);

		// iconViewSel.add(modelItem);

		if (modelItem instanceof ThemeScreenData) {
			// click on the editor screen - do not clean selection
			return;
		}
		
		setInputAsCollapsed();

		// clears the old selected item
		clearSelection();

		IResourceViewerSelectionHelper2 helper = getResourceSelectionHelper(modelItem);

		if (modelItem != null
				&& (selectedIAL == null || !((String) selectedIAL.getData())
						.equals(modelItem.getRoot().getType()))) {

			IEmbeddedEditorDescriptor desc = null;
			if(null != modelItem.getRoot())
				desc = ExtensionManager
						.getEmbeddedEditorDescriptorByContentType(modelItem
								.getRoot().getType());
			
			if (null != desc) {
				IEmbeddedEditorCustomizer customizer = desc
						.getEmbeddedEditorCustomizer();
				for (ImageLabel label : ials) {
					if (((String) label.getText()).equals(customizer
							.getCategoryName(modelItem.getRoot()))) {
						setSelectedThumb(label, false);
					}
				}
			} else {
				for (ImageLabel label : ials) {
					if (((String) label.getData()).equals(modelItem.getRoot()
							.getType())) {
						setSelectedThumb(label, false);
					}
				}
			}
		}
		if (modelItem != null
				&& (selectedIAL != null && ((String) selectedIAL.getData())
						.equals(modelItem.getRoot().getType()))) {
			String modelItemName = modelItem.getName();
			boolean selectedIALisWantedIAL = false;
			
			if (inputs.size() > 0 && inputs.get(0).getGroups().size() > 0) {
				for (IResourceTableCategoryItem cat : inputs.get(0).getGroups()
						.get(0).getCategories()) {
					if (cat instanceof ResourceTableCategory) {
						if (cat.getName().equals(modelItemName)) {
							selectedIALisWantedIAL = true;
							break;
						}
					}
				}
			}
			
			if (selectedIALisWantedIAL) {
				setSelectedThumb(selectedIAL, false);
			} else {
				
				IEmbeddedEditorDescriptor desc = ExtensionManager
						.getEmbeddedEditorDescriptorByContentType(modelItem
								.getRoot().getType());
				if (null != desc) {
					IEmbeddedEditorCustomizer customizer = desc
							.getEmbeddedEditorCustomizer();
					for (ImageLabel label : ials) {
						if (((String) label.getText()).equals(customizer
								.getCategoryName(modelItem.getRoot()))) {
							setSelectedThumb(label, false);
						}
					}
				} else {
					for (ImageLabel label : ials) {
						if (((String) label.getData()).equals(modelItem
								.getRoot().getType())) {
							setSelectedThumb(label, false);
						}
					}
				}
			}

		} else if (selectedIAL != null) {
			setSelectedThumb(selectedIAL, false);
		}

		if (helper != null) {
			ResourceTableCategory cat = helper.getCategoryToPin(inputs,
					modelItem);
			if (cat != null) {

				if (selectedCategory != null) {
					selectedCategory.setExtracted(false);
					selectedCategory.getParent().setExtracted(false);
					selectedCategory = (ResourceTableCategory) cat;
				} else {
					selectedCategory = (ResourceTableCategory) cat;
				}
				if (!(selectedCategory instanceof ColorResourceTableCategory)) {
					for (int i = 0; i < selectedCategory.getItems().size(); i++) {
						if (modelItem.getParent().getName().equals(
								selectedCategory.getItems().get(i).getName())) {
							selectedItem = selectedCategory.getItems().get(i);
							break;
						}
					}
				}
				((ResourceTableCategory) cat).setExtracted(true);
				((ResourceTableCategory) cat).getParent().setExtracted(true);
				if (selectedCategory != null) {
					if (!(selectedCategory instanceof ColorResourceTableCategory || selectedItem == null)) {
						selectedCategory = null;
						selectedGroup = null;
					}
				}
			}
		}
		// iconViewSel.add(Boolean.FALSE);
		// refreshIconView(new StructuredSelection(iconViewSel), false);

		// refresh even when no selection is there, to clear the selection
		refreshKeepingIndex();
		if (selectedItem != null) {
			scrollToVisible(selectedItem);
		}
		
		//update the new selection
		setSelectionWithoutUpdatingListeners(sel);
	}

	public int getIndex(ResourceTableCategory cat) {
		for (int i = 0; i < table.getItemCount(); i++) {
			if (table.getItem(i).getData() == cat) {
				return i;
			}

		}
		return -1;
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
	public void contentModified(IContentDelta delta) {
		
		resourceModified(null);
	}

	protected void resourceModified(IContentData modified) {
		// IContentData data = positionViewer.getData();
		// if (data != null) {

		String extractedGroupName = "";
		if (inputs != null && inputs.size() > 0) {
			for (ResourceTableMasterGroup grp : inputs.get(0).getGroups()) {
				if (grp.isExtracted()) {
					extractedGroupName = grp.getMasterGroupName();

				}
			}
		}

		if (selectedIAL != null) {
			inputs = createInput2(selectedIAL);
		} else {
			inputs = createInput2(null);
		}
		refreshKeepingIndex(extractedGroupName);

		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		
		if (adapter == IPropertySource.class) {
			return sourceEditor.getAdapter(adapter);
		}
		if (adapter == IContentAdapter.class
				|| adapter == IContentService.class)
			return this.sourceEditor.getAdapter(adapter);
		return null;
	}

	public void rootContentChanged(IContent content) {

		IContentAdapter adapter = (IContentAdapter) sourceEditor
				.getAdapter(IContentAdapter.class);

		// replaced with
		if (adapter.getContents().length < 1) {
			return;
		}
		inputs = createInput2(selectedIAL); 
		// content.getType()
		refreshKeepingIndex();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.wizards.BrandedTitleAreaDialog#getBannerIconDescriptor()
	 */
	private ImageDescriptor getPinIconDescriptor() {
		return UiPlugin.getImageDescriptor("icons/resview/pin.png");
	}

	private ImageDescriptor getTreeCollapsedDescriptor() {
		return UiPlugin
				.getImageDescriptor("icons/resview/notExtractedNewSmall.png");
	}

	private ImageDescriptor getTreeExpandedDescriptor() {
		return UiPlugin.getImageDescriptor("icons/resview/extractedSmall.png");
	}

	private ImageDescriptor getCheckDescriptor() {
		return UiPlugin.getImageDescriptor("icons/resview/check.png");
	}

	private ImageDescriptor getHalfCheckDescriptor() {
		return UiPlugin.getImageDescriptor("icons/resview/checkHalf.png");
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.add(listener);
	}

	ISelection selection = null;

	public ISelection getSelection() {
		return selection;
	}

	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		selectionListeners.remove(listener);

	}

	public void setSelectionWithoutUpdatingListeners(final ISelection selection) {
		this.selection = selection;
	}

	public void setSelection(final ISelection selection) {
		this.selection = selection;
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				for (ISelectionChangedListener listener : selectionListeners) {
					if (listener != null) {
						listener.selectionChanged(new SelectionChangedEvent(
								ResourceViewPage.this,
								selection == null ? StructuredSelection.EMPTY
										: selection));
					}
				}
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.widgets.ImageLabel.SelectionListener#selected(java.util.EventObject)
	 */
	public void selected(EventObject e) {
		for (ImageLabel label : ials) {
			Control parent = ((Composite) label).getParent();
			parent.setBackground(ColorConstants.white);
			label.setSelected(false);
		}
		selectedIAL = (ImageLabel) e.getSource();
		Control parent = ((Composite) selectedIAL).getParent();
		parent.setBackground(ColorConstants.menuBackgroundSelected);
		selectedIAL.setSelected(true);
		inputs = createInput2(selectedIAL);
		refreshKeepingIndex();
		selectedIAL.setFocus();
		scrollToVisible((Composite) selectedIAL.getParent());
	}

	public ResourceTableItem getSelectedItem() {
		return selectedItem;
	}

	/**
	 * Gets teble item, which is selected
	 * 
	 * @return TableItem which is selected
	 */
	public TableItem getSelectedTableItem() {
		Object element = ((IStructuredSelection) selection).getFirstElement();
		ThemeBasicData tbd = null;
		if (element instanceof ThemeData) {
			tbd = ((ThemeData) element).getData();
		}
		for (TableItem ti : table.getItems()) {
			Object data = ti.getData();
			IContentData icd = null;
			if (data instanceof ResourceTableCategory) {
				icd = ((ResourceTableCategory) data).getAssociatedContentData();
			}
			if (data instanceof ResourceTableMasterGroup) {
				icd = ((ResourceTableMasterGroup) data)
						.getAssociatedContentData();
			}
			if (data instanceof ResourceTableItem) {
				icd = ((ResourceTableItem) data).getAssociatedContentData();
			}
			if (icd instanceof ThemeData) {
				if (((ThemeData) icd).getData().equals(tbd)) {
					return ti;
				}
			}
		}
		return null;
	}

	/**
	 * Selects table item according to given editObject
	 * 
	 * @param editObject editObject to select
	 * @return true - item was successfully selected, false - item wasn't
	 *         selected
	 */
	private boolean selectTableItem(EditObject editObject) {
		resourceModified(null);
		ResourceTableInput rti = inputs.get(0);
		if (rti == null)
			return false;
		Object bean = null;
		for (Object ia : editObject.eAdapters()) {
			if (ia instanceof InvocationAdapter) {
				InvocationAdapter mia = (InvocationAdapter) ia;
				bean = mia.getBean();
				break;
			}
		}
		// find and selects bean, which should be selected, in the tree and then
		// extract all precedented groups
		if (bean != null) {
			List<ResourceTableMasterGroup> rtmgs = rti.getGroups();
			if (rtmgs == null)
				return false;
			for (ResourceTableMasterGroup r : rtmgs) {
				r.setExtracted(false);
			}
			if (bean instanceof Task) {
				for (ResourceTableMasterGroup r : rtmgs) {
					IContentData icd = r.getAssociatedContentData();
					if (icd instanceof ThemeData) {
						ThemeBasicData tbd = ((ThemeData) icd).getData();
						if (tbd.equals(bean)) {
							r.setExtracted(true);
							for (TableItem ti : table.getItems()) {
								Object data = ti.getData();
								if (data instanceof ResourceTableMasterGroup) {
									IContentData icd2 = ((ResourceTableMasterGroup) data)
											.getAssociatedContentData();
									if (icd2 instanceof ThemeData) {
										ThemeData td = (ThemeData) icd2;
										if (td.getData().equals(bean)) {
											clearSelection();
											selectedGroup = (ResourceTableMasterGroup) data;
											setSelectionWithoutUpdatingListeners(new StructuredSelection(
													icd2));
											refreshKeepingIndex();
											return true;
										}
									}
								}
							}
						}
					}

				}
			} else if (bean instanceof ComponentGroup) {
				for (ResourceTableMasterGroup r : rtmgs) {
					List<IResourceTableCategoryItem> rtcs = r.getCategories();
					if (rtcs == null)
						continue;
					for (IResourceTableCategoryItem rtci : rtcs) {
						if (!(rtci instanceof ResourceTableCategory))
							continue;
						ResourceTableCategory rtc = (ResourceTableCategory) rtci;
						IContentData icd = rtc.getAssociatedContentData();
						if (icd instanceof ThemeData) {
							if (((ThemeData) icd).getData().equals(bean)) {
								rtc.setExtracted(true);
								rtc.getParent().setExtracted(true);
								refreshKeepingIndex();
								for (TableItem ti : table.getItems()) {
									Object data = ti.getData();
									if (data instanceof ResourceTableCategory) {
										IContentData icd2 = ((ResourceTableCategory) data)
												.getAssociatedContentData();
										if (icd2 instanceof ThemeData) {
											ThemeData td = (ThemeData) icd2;
											if (td.getData().equals(bean)) {
												clearSelection();
												selectedCategory = (ResourceTableCategory) data;
												setSelectionWithoutUpdatingListeners(new StructuredSelection(
														icd2));
												refreshKeepingIndex();
												return true;
											}
										}
									}
								}
							}
						}
					}
				}
			} else if (bean instanceof Component) {
				for (ResourceTableMasterGroup r : rtmgs) {
					List<IResourceTableCategoryItem> rtcs = r.getCategories();
					if (rtcs == null)
						continue;
					for (IResourceTableCategoryItem rtci : rtcs) {
						if (!(rtci instanceof ResourceTableCategory))
							continue;
						ResourceTableCategory rtc = (ResourceTableCategory) rtci;
						List<ResourceTableItem> rtis = rtc.getItems();
						for (ResourceTableItem i : rtis) {
							IContentData icd = i.getAssociatedContentData();
							if (icd instanceof ThemeData) {
								if (((ThemeData) icd).getData().equals(bean)) {
									i.getParent().setExtracted(true);
									i.getParent().getParent()
											.setExtracted(true);
									refreshKeepingIndex();
									for (TableItem ti : table.getItems()) {
										Object data = ti.getData();
										if (data instanceof ResourceTableItem) {
											IContentData icd2 = ((ResourceTableItem) data)
													.getAssociatedContentData();
											if (icd2 instanceof ThemeData) {
												ThemeData td = (ThemeData) icd2;
												if (td.getData().equals(bean)) {
													clearSelection();
													selectedItem = (ResourceTableItem) data;
													setSelectionWithoutUpdatingListeners(new StructuredSelection(
															icd2));
													refreshKeepingIndex();
													return true;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Renames given editObeject
	 * 
	 * @param editObject
	 * @param renameAction
	 */
	public void rename(EditObject editObject, SelectionAction renameAction) {
		selectTableItem(editObject);
		renameAction.setSelectionProvider(this);
		renameAction.update();
		renameAction.run();
	}
}