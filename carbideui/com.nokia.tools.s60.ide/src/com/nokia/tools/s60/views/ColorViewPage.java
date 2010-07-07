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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.DelegatingDragAdapter;
import org.eclipse.jface.util.DelegatingDropAdapter;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroupItem;
import com.nokia.tools.media.utils.svg.ColorGroups;
import com.nokia.tools.media.utils.svg.ColorGroupsStore;
import com.nokia.tools.s60.editor.Series60EditorPart;
import com.nokia.tools.s60.editor.actions.ColorizeColorGroupAction;
import com.nokia.tools.screen.core.IScreenAdapter;
import com.nokia.tools.screen.core.IScreenFactory;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.propertysheet.color.ColorChangedLabelWrapper;
import com.nokia.tools.screen.ui.propertysheet.color.ColorPickerComposite;
import com.nokia.tools.screen.ui.propertysheet.color.IColorPickerListener;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.ui.tooltip.CompositeInformationControl;
import com.nokia.tools.ui.tooltip.CompositeTooltip;

public class ColorViewPage extends Page implements ISelectionListener,
		IMenuListener {

	private static String NAME_COLUMN = "name";

	private static String ID_COLUMN = "id";

	private static List<String> tableColumns;

	private static List<String> tableColumnsSortingOrder;
	static {
		tableColumns = new ArrayList<String>();
		tableColumnsSortingOrder = new ArrayList<String>();

		tableColumns.add(NAME_COLUMN);
		tableColumns.add(ID_COLUMN);

		tableColumnsSortingOrder.add(ID_COLUMN);
		tableColumnsSortingOrder.add(NAME_COLUMN);
	}

	private IEditorPart sourceEditor;

	private TreeViewer treeViewer;

	IDGroupContainer container;

	/**
	 * Container of IDGroups
	 * 
	 */
	class IDGroupContainer {
		/**
		 * pair of <Group name, Group>
		 */
		private HashMap<String, IDGroup> groupMap = new HashMap<String, IDGroup>();

		/**
		 * List of checked groups
		 */
		List<String> checkedGroupNames = new ArrayList<String>();

		public void checkGroup(String groupName) {
			if (!checkedGroupNames.contains(groupName)) {
				checkedGroupNames.add(groupName);
			}
		}

		public void uncheckGroup(String groupName) {
			if (checkedGroupNames.contains(groupName)) {
				checkedGroupNames.remove(groupName);
			}
		}

		public IDGroup getGroup(String groupName) {
			return groupMap.get(groupName);
		}

		public ArrayList<ElementTableItem> getAllAllowedItems() {
			// returns sorted list of all allowed items
			ArrayList<ElementTableItem> itemsFromCheckedGroups = getSortedItemsFromCheckedGroups(checkedGroupNames
					.toArray(new String[checkedGroupNames.size()]));
			Collections.sort(itemsFromCheckedGroups);
			return itemsFromCheckedGroups;
		}

		public void putGroup(String groupName, IDGroup group) {
			groupMap.put(groupName, group);
		}

		/**
		 * Returns all ids from checked groups
		 */
		public ArrayList<ElementTableItem> getSortedItemsFromCheckedGroups(
				String[] checkedGroups) {
			ArrayList<ElementTableItem> items = new ArrayList<ElementTableItem>();
			// if none of the groups is checked - it is the same as all groups
			// were checked
			if (checkedGroups.length == 0) {
				checkedGroups = IDGroupContainer.this.getGroupNames();
			}

			for (int i = 0; i < checkedGroups.length; i++) {
				IDGroup group = getGroup(checkedGroups[i]);
				SortedSet<ElementTableItem> allIds = group.getAll();
				items.addAll(allIds);
			}
			return items;
		}

		public String[] getGroupNames() {
			Set<String> groupNameSet = groupMap.keySet();
			String[] groupNames = groupNameSet.toArray(new String[groupNameSet
					.size()]);
			return groupNames;
		}

		public void setPropertyToNewValue(String itemName, String property,
				String propertyValue) {
			for (IDGroup grp : groupMap.values()) {

				SortedSet<ElementTableItem> items = grp.getAll();
				for (ElementTableItem item : items) {
					if (item.getProperty(NAME_COLUMN).equals(itemName)) {
						item.setProperty(property, propertyValue);
						break;
					}
				}
			}
		}
	}

	/**
	 * Container of Item ids
	 * 
	 */
	class IDGroup {
		public IDGroup(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		/**
		 * Group name
		 */
		private String name;

		private SortedSet<ElementTableItem> groupItems = new TreeSet<ElementTableItem>();

		public void add(ElementTableItem id) {
			groupItems.add(id);
		}

		public void addAll(SortedSet<ElementTableItem> idSet) {
			groupItems.addAll(idSet);
		}

		public SortedSet<ElementTableItem> getAll() {
			return groupItems;
		}
	}

	class ElementTableItem implements Comparable<ElementTableItem> {

		private boolean isSet;

		Map<String, String> propertiesMap = new HashMap<String, String>();

		List<String> propertyNamesSortingPriorityList = new ArrayList<String>();

		public ElementTableItem(List<String> propertyNames,
				List<String> propertyNamesSortingPriorityList) {
			for (String property : propertyNames) {
				if (property != null && property.length() > 0) {
					propertiesMap.put(property, "");
				}
			}
			this.propertyNamesSortingPriorityList = propertyNamesSortingPriorityList;
		}

		public void setProperty(String property, String propertyValue) {
			if (propertiesMap.containsKey(property)) {
				propertiesMap.put(property, propertyValue);
			} else if ("isSkinned".equals(property)) {
				if ("T".equals(propertyValue)) {
					setSet(true);
				} else if ("F".equals(propertyValue)) {
					setSet(false);
				}
			}

		}

		public String getProperty(String propertyName) {
			return propertiesMap.get(propertyName);
		}

		public boolean isSet() {
			return isSet;
		}

		public void setSet(boolean isSkinned) {
			this.isSet = isSkinned;
		}

		public int compareTo(ElementTableItem item) {
			for (String property : propertyNamesSortingPriorityList) {
				if (this.getProperty(property) != null) {
					if (this.getProperty(property).equals(
							item.getProperty(property))) {
						continue; // if the first match fix go to another
						// property
					} else {
						return this.getProperty(property).compareTo(
								item.getProperty(property));
					}
				} else {
					return "".compareTo(item.getProperty(property));
				}
			}

			return 0;
		}

		public int compareTo(String item) {

			for (String property : propertyNamesSortingPriorityList) {
				if (this.getProperty(property) != null) {
					if (this.getProperty(property).equals(item)) {
						continue; // if the first match fix go to another
						// property
					} else {
						return this.getProperty(property).compareTo(item);
					}
				} else {
					return "".compareTo(item);
				}
			}

			return 0;
		}

		public int compareToProperty(Object o, String propertyName) {
			if (o instanceof ElementTableItem) {
				ElementTableItem tmp = (ElementTableItem) o;

				return this.getProperty(propertyName).compareTo(
						tmp.getProperty(propertyName));

			} else if (o instanceof String) {
				return this.getProperty(propertyName).compareTo((String) o);
			} else {
				throw new ClassCastException();
			}
		}

		public int setCompareTo(Object o) {
			if (o instanceof ElementTableItem) {
				ElementTableItem tmp = (ElementTableItem) o;
				return new Boolean(this.isSet()).compareTo(tmp.isSet());
			} else {
				throw new ClassCastException();
			}
		}

	}

	public IDGroupContainer digTheTree(IContentData root) {
		IDGroupContainer groupContainer = new IDGroupContainer();
		for (IContentData n1 : root.getChildren()) {
			if (n1.getAdapter(IScreenAdapter.class) != null) {
				continue;
			}
			IDGroup group = new IDGroup(n1.getName());
			for (IContentData n2 : n1.getChildren()) {
				SortedSet<ElementTableItem> icons = new TreeSet<ElementTableItem>();
				for (IContentData n3 : n2.getChildren()) {
					for (IContentData n4 : n3.getChildren()) {
						// if(n4 instanceof ThemeBasicData){
						// ThemeBasicData data= (ThemeBasicData)n4;
						// data.supportsPlatform("");
						//							
						// }

						// ((ThemeContent)
						// n1.getRoot()).supportsPlatform(DevicePlatform.S60_2_0,
						// n4.getId());
						ElementTableItem eti = new ElementTableItem(
								tableColumns, tableColumnsSortingOrder);
						eti.setProperty(NAME_COLUMN, n4.getName());
						eti.setProperty(ID_COLUMN, n4.getId());

						icons.add(eti);
					}
				}
				group.addAll(icons);
			}

			groupContainer.putGroup(group.getName(), group);
		}
		return groupContainer;
	}

	// Creating input for table viewer
	private void createInput2() {
		IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();
		if (activeEd != null) {
			IContentAdapter adapter = (IContentAdapter) activeEd
					.getAdapter(IContentAdapter.class);
			if (adapter != null) {
				IContent[] cnt = adapter.getContents();
				IContent root = ScreenUtil.getPrimaryContent(cnt);

				if (root != null) {
					container = digTheTree(root);
				}
				/*
				 * listOfContentDataNames.addAll(setOfContentDataNames); // sort
				 * list to enable faster filtering
				 * Collections.sort(listOfContentDataNames);
				 */}
		}
	}

	public void generateColorGroupsForCurrentScreen() {
		IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();

		IContent root = null;
		String screenTitle = "";
		if (activeEd instanceof Series60EditorPart) {
			Series60EditorPart sep = (Series60EditorPart) activeEd;
			String screenTitleWithProjectName = sep.getTitle();
			String[] separatedScreenTitleWithProjectName = screenTitleWithProjectName
					.split(" - ");

			if (separatedScreenTitleWithProjectName.length == 2) {
				screenTitle = separatedScreenTitleWithProjectName[1];
			}
			IContent[] cnt = sep.getContents();

			root = ScreenUtil.getPrimaryContent(cnt);
		}

		ColorGroups grps = null;
		if (activeEd != null && ColorGroupsStore.isEnabled) {
			IFile original = ((FileEditorInput) sourceEditor.getEditorInput())
					.getFile();
			grps = ColorGroupsStore.getColorGroupsForProject(original
					.getProject());
		} else {
			IFile original = ((FileEditorInput) sourceEditor.getEditorInput())
					.getFile();
			grps = ColorGroupsStore.getColorGroupsForProject(original
					.getProject());
			return;
		}
		int groupNo = 0;
		IScreenFactory factory = (IScreenFactory) root
				.getAdapter(IScreenFactory.class);
		for (IContentData n1 : factory.getScreens()) {
			if (screenTitle.equals(n1.getId())) {

				for (IContentData n2 : n1.getChildren()) {

					String name = n2.getName();
					String id = n2.getId();

					IContentData data = root.findByName(name);
					if (id == null) {
						System.out.println(name + " <-name ->id " + id);
					}
					IImageAdapter imageAdapter = (IImageAdapter) data
							.getAdapter(IImageAdapter.class);
					IImage image = imageAdapter.getImage();
					if (image.getLayerCount() > 0) {
						ILayer layer = image.getLayer(0);
						Map<RGB, Integer> colors = layer.getColors();
						if (colors != null) {
							for (RGB rgb : colors.keySet()) {
								ColorGroupItem colorGroupItem;
								if (id != null) {
									colorGroupItem = new ColorGroupItem(id,
											null, null);
								} else {
									colorGroupItem = new ColorGroupItem(name,
											null, null);
								}
								ColorGroup grp = grps.getGroupByRGB(rgb);
								if (grp != null) { // if group with color
									// exist add the
									// item to it
									grp.addItemToGroup(colorGroupItem);
								} else { // else try to find group with
									// same hue
									grp = grps.getGroupByColorHue(rgb);
									if (grp != null) { // add to subgroup
										ColorGroup childGrp = new ColorGroup(
												rgb, "group" + groupNo++, grps);
										childGrp.addItemToGroup(colorGroupItem);
										childGrp.setParentGroupName(grp
												.getName());
										grp
												.addChildrenGroup(childGrp
														.getName());
										grps.addGroup(childGrp);
										// System.out.println("adding
										// subgroup");
									} else { // create new group
										grp = new ColorGroup(rgb, "group"
												+ groupNo++, grps);
										grp.addItemToGroup(colorGroupItem);
										grps.addGroup(grp);

										// int hue = (int) (rgb.getHSB()[0]
										// * 360);

										// System.out
										// .println("creating new group with
										// hue"
										// + hue);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public void generateColorGroups() {
		List<ElementTableItem> allItems = container.getAllAllowedItems();
		IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();

		IContent root = null;

		if (activeEd instanceof Series60EditorPart) {
			Series60EditorPart sep = (Series60EditorPart) activeEd;

			IContent[] cnt = sep.getContents();

			root = ScreenUtil.getPrimaryContent(cnt);
		}
		ColorGroups grps = null;
		if (activeEd != null && ColorGroupsStore.isEnabled) {
			IFile original = ((FileEditorInput) sourceEditor.getEditorInput())
					.getFile();
			grps = ColorGroupsStore.getColorGroupsForProject(original
					.getProject());
		} else {
			IFile original = ((FileEditorInput) sourceEditor.getEditorInput())
					.getFile();
			grps = ColorGroupsStore.getColorGroupsForProject(original
					.getProject());
			return;
		}
		int groupNo = 0;
		for (ElementTableItem item : allItems) {
			String name = item.getProperty(NAME_COLUMN);
			String id = item.getProperty(ID_COLUMN);

			IContentData data = root.findByName(name);
			IImageAdapter imageAdapter = (IImageAdapter) data
					.getAdapter(IImageAdapter.class);
			IImage image = imageAdapter.getImage();
			if (image.getLayerCount() > 0) {
				ILayer layer = image.getLayer(0);
				Map<RGB, Integer> colors = layer.getColors();
				if (colors != null) {
					for (RGB rgb : colors.keySet()) {
						ColorGroupItem colorGroupItem = new ColorGroupItem(id,
								null, null);
						ColorGroup grp = grps.getGroupByRGB(rgb);
						if (grp != null) { // if group with color exist add the
							// item to it
							grp.addItemToGroup(colorGroupItem);
						} else { // else try to find group with same hue
							grp = grps.getGroupByColorHue(rgb);
							if (grp != null) { // add to subgroup
								ColorGroup childGrp = new ColorGroup(rgb,
										"group" + groupNo++, grps);
								childGrp.addItemToGroup(colorGroupItem);
								childGrp.setParentGroupName(grp.getName());
								grp.addChildrenGroup(childGrp.getName());
								grps.addGroup(childGrp);
								// System.out.println("adding subgroup");
							} else { // create new group
								grp = new ColorGroup(rgb, "group" + groupNo++,
										grps);
								grp.addItemToGroup(colorGroupItem);
								grps.addGroup(grp);

								// int hue = (int) (rgb.getHSB()[0] * 360);

								// System.out
								// .println("creating new group with hue"
								// + hue);
							}
						}
					}
				}
			}
		}
	}

	public IEditorPart getSourceEditor() {
		return sourceEditor;
	}

	public Object[] EMPTY_ARRAY = new Object[0];

	public ColorViewPage(IEditorPart sourceEd) {
		this.sourceEditor = sourceEd;
	}

	Composite composite = null;

	@Override
	public void createControl(Composite parent) {
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
		// SearchAction.HLP_CTX);
		composite = new Composite(parent, SWT.NONE);
		composite.setBackground(ColorConstants.yellow);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		GridLayout gl = new GridLayout();
		gl.marginTop = gl.marginBottom = gl.marginLeft = gl.marginRight = gl.marginWidth = gl.marginHeight = 0;
		composite.setLayout(gl);

		createCompositeArea(composite);

		createActions();
		createToolbar();

	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void setFocus() {

		try {
			refreshViewer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	Action switchLayoutAction = null;

	ListViewer listViewer;

	/**
	 * Create toolbar.
	 */
	private void createToolbar() {
		IToolBarManager mgr = getSite().getActionBars().getToolBarManager();
		mgr.add(switchLayoutAction);

	}

	private void createActions() {
		switchLayoutAction = new Action("Generate color groups...") {
			public void run() {
				createInput2();
				// generateColorGroups();
				generateColorGroupsForCurrentScreen();
				refreshViewer();
			}

		};

	}

	private void recalculateLayout() {

	}

	public void createCompositeArea(Composite parent) {
		Composite dialogArea = parent;
		parent.getParent().addFocusListener(new FocusAdapter() {
			// this is to ensure that after opening when user selects pattern
			// input he can see
			// all items
			@Override
			public void focusGained(FocusEvent event) {
				try {
					treeViewer.getLabelProvider().dispose();
					treeViewer.setInput(createInput());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		treeViewer = new TreeViewer(dialogArea, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL);

		Tree tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		treeViewer.setContentProvider(new TreeViewContentProvider());

		treeViewer.setLabelProvider(new TreeViewLabelProvider());

		treeViewer.getLabelProvider().dispose();
		treeViewer.setInput(createInput());
		addDragAndDropSupport();
		hookContextMenu();
		createTooltip(treeViewer.getTree());

	}

	private CompositeTooltip createTooltip(final Tree tree) {

		CompositeTooltip tooltip = new CompositeTooltip() {
			TreeItem item;

			@Override
			public void initControl(
					CompositeInformationControl informationControl) {
				super.initControl(informationControl);

				if (item != null && !item.isDisposed()) {
					Rectangle rect = item.getBounds(0);
					Point size = new Point(
							informationControl.getBounds().width,
							informationControl.getBounds().height);
					Point pt = tree.toDisplay(rect.x - size.x + 10, rect.y + 5); // (-6*size.x/7,0);//
					if (pt.y + size.y > tree.getDisplay().getBounds().height) {
						pt.y = tree.getDisplay().getBounds().height - size.y;
					}
					informationControl.setLocation(pt);
				}
			}

			@Override
			protected Rectangle getControlBounds() {
				if (item != null && !item.isDisposed()) {
					Point treeLoc = tree.getLocation();
					Rectangle itemBounds = item.getBounds(0);
					return new Rectangle(treeLoc.x + itemBounds.x, treeLoc.y
							+ itemBounds.y, itemBounds.width, itemBounds.height);
				} else {
					return super.getControlBounds();
				}
			}

			@Override
			public void hide() {
				super.hide();
				item = null;
			}

			@Override
			protected void mouseExit(MouseEvent e) {
				super.mouseExit(e, 250);
			}

			@Override
			protected void mouseHover(MouseEvent e) {
				if (e.widget == tree) {
					TreeItem newItem = tree.getItem(new Point(e.x, e.y));

					if (newItem != item) {
						hide();
					}

					item = newItem;

					if (item != null) {
						if (item.getData() instanceof ColorGroup) {
							ColorGroup data = (ColorGroup) item.getData();
							if (!data.hasParent()) {
								super.mouseHover(e);
								return;
							}

						}
					}

					hide();
				}
			}

			@Override
			protected CompositeInformationControl createFocusedControl() {
				return createUnfocusedControl();
			}

			@Override
			protected CompositeInformationControl createUnfocusedControl() {
				final TreeItem currItem = item;

				RGB inputRgb = ((ColorGroup) currItem.getData())
						.getGroupColor();

				CompositeInformationControl cic = super
						.createUnfocusedControl();

				final Composite root = cic.getComposite();
				GridLayout gl = new GridLayout(1, false);
				gl.marginWidth = gl.marginHeight = 0;
				root.setLayout(gl);

				final ColorChangedLabelWrapper colorChangedLabelWrapper = new ColorChangedLabelWrapper();
				colorChangedLabelWrapper.setColorString(ColorUtil
						.asHashString(inputRgb));

				IColorPickerListener colorPickerListener = new IColorPickerListener() {
					public void selectionChanged() {
						okCloseDialog();
					}

					public void okCloseDialog() {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								hide(true);
							}
						});

						RGB newColor = colorChangedLabelWrapper
								.getColorDescriptor().getRGB();
						handleColorChange(newColor, (ColorGroup) currItem
								.getData());
						System.out.println("changing color");
					}
				};

				new ColorPickerComposite(root, SWT.NONE,
						colorChangedLabelWrapper, colorPickerListener);

				return cic;
			}
		};

		tooltip.setControl(tree);

		return tooltip;
	}

	public void handleColorChange(RGB newColor, ColorGroup grp) {

		ColorGroup referencedGroup = grp;
		RGB referencedColor = referencedGroup.getGroupColor();
		RGB changingRGB = newColor;
		final List<IContentData> items = findContentDataForGroup(referencedGroup);

		// final List<ColorGroupItem> items= ((ColorGroup)
		// data[2]).getGroupItems();
		ISelectionProvider sp = new ISelectionProvider() {
			public ISelection getSelection() {
				return new StructuredSelection(items);
			}

			public void addSelectionChangedListener(
					ISelectionChangedListener listener) {
			}

			public void removeSelectionChangedListener(
					ISelectionChangedListener listener) {
			}

			public void setSelection(ISelection selection) {
			}
		};

		ColorizeColorGroupAction colorGroupAction = new ColorizeColorGroupAction(
				sp, null, treeViewer);
		colorGroupAction.setColorGroups(getColorGroups());
		colorGroupAction.setColor(changingRGB, referencedColor, 255);
		if (colorGroupAction.isEnabled()) {
			colorGroupAction.setColorGroup(referencedGroup);
			colorGroupAction.run();
		}

		for (String childGroupName : referencedGroup.getChildrenGroups()) {
			ColorGroup childGroup = getColorGroups().getGroupByName(
					childGroupName);

			changingRGB = changeHueValue(childGroup.getGroupColor(),
					referencedGroup.getGroupColor());
			final List<IContentData> itemsInside = findContentDataForGroup(childGroup);

			sp = new ISelectionProvider() {
				public ISelection getSelection() {
					return new StructuredSelection(itemsInside);
				}

				public void addSelectionChangedListener(
						ISelectionChangedListener listener) {
				}

				public void removeSelectionChangedListener(
						ISelectionChangedListener listener) {
				}

				public void setSelection(ISelection selection) {
				}
			};

			colorGroupAction = new ColorizeColorGroupAction(sp, null,
					treeViewer);
			colorGroupAction.setColorGroups(getColorGroups());
			colorGroupAction.setColor(changingRGB, childGroup.getGroupColor(),
					255);
			if (colorGroupAction.isEnabled()) {
				colorGroupAction.setColorGroup(childGroup);
				colorGroupAction.run();
			}
		}

		refreshViewer();

	}

	public ColorGroups createInput() {
		IFile original = ((FileEditorInput) sourceEditor.getEditorInput())
				.getFile();

		ColorGroups grps = ColorGroupsStore.getColorGroupsForProject(original
				.getProject());

		return grps;

	}

	class TreeViewLabelProvider extends LabelProvider {

		private List<Image> images = new ArrayList<Image>();

		public Image getImage(Object obj) {
			Image image = null;
			if (obj instanceof ColorGroup) {
				ImageDescriptor desc = null;
				RGB rgb = ((ColorGroup) obj).getGroupColor();
				RGB white = new RGB(255, 255, 255);
				PaletteData paletteData = new PaletteData(new RGB[] { white,
						rgb });
				ImageData imageData = new ImageData(15, 15, 1, paletteData);
				// for (int x = 0; x < 15; x++) {
				// for (int y = 0; y < 15; y++) {
				// imageData.setPixel(x, y, 0);
				// }
				// }
				desc = ((ImageDescriptor.createFromImageData(imageData)));
				image = desc.createImage();

				GC gc = new GC(image);
				Color color = new Color(null, rgb);
				gc.setBackground(color);
				gc.fillRoundRectangle(0, 0, 15, 15, 10, 10);
				gc.dispose();
				color.dispose();
				images.add(image);
			} else if (obj instanceof ColorGroupItem) {
				if (sourceEditor instanceof Series60EditorPart) {
					Series60EditorPart sep = (Series60EditorPart) sourceEditor;

					IContent[] cnt = sep.getContents();
					IContent root = ScreenUtil.getPrimaryContent(cnt);
					IContentData data = root.findById(((ColorGroupItem) obj)
							.getItemId());

					if (data != null) {
						ImageDescriptor desc = data.getImageDescriptor(20, 20);
						image = desc.createImage();
						images.add(image);
					}

				}
			}
			return image;
		}

		public String getText(Object obj) {
			String text = "";

			if (obj instanceof ColorGroups) {
				text = ((ColorGroups) obj).getThemeName();
			} else if (obj instanceof ColorGroup) {
				ColorGroup grp = ((ColorGroup) obj);
				String countText = grp.getGroupItems().size() > 1 ? " (cnt "
						+ grp.getGroupItems().size() + ")" : "";
				text = grp.getName() + countText;

			} else if (obj instanceof ColorGroupItem) {
				Series60EditorPart sep = (Series60EditorPart) sourceEditor;

				IContent[] cnt = sep.getContents();
				IContent root = ScreenUtil.getPrimaryContent(cnt);
				IContentData data = root.findById(((ColorGroupItem) obj)
						.getItemId());
				if (data != null) {
					text = data.getName();
					if ("".equals(text)) {
						text = data.getId();
					}
				} else {
					text = ((ColorGroupItem) obj).getItemId();
				}

			}
			return text;
		}

		public void dispose() {
			for (Image img : images) {
				img.dispose();
				img = null;
			}
			images.clear();
		}

	}

	class TreeViewContentProvider implements IStructuredContentProvider,
			ITreeContentProvider {

		private ColorGroups grps = null;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			return null;
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ColorGroups) {
				grps = (ColorGroups) parentElement;
				return ((ColorGroups) parentElement).getParentGroups()
						.toArray();
			} else if (parentElement instanceof ColorGroup) {
				ColorGroup grp = (ColorGroup) parentElement;
				List<Object> objectsToReturn = new ArrayList<Object>();
				if (grp.hasChildrenGroup()) {
					for (String groupName : grp.getChildrenGroups()) {
						if (grps != null) {
							ColorGroup group = grps.getGroupByName(groupName);
							objectsToReturn.add(group);
						}
					}
				}

				Map<String, Object> mapForRemovingDuplicity = new HashMap<String, Object>();
				for (ColorGroupItem item : grp.getGroupItems()) {
					mapForRemovingDuplicity.put(item.getItemId(), item);
				}
				objectsToReturn.addAll(mapForRemovingDuplicity.values());

				return objectsToReturn.toArray();

			}
			return EMPTY_ARRAY;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
	}

	// DND support
	private void addDragAndDropSupport() {

		final LocalSelectionTransfer localTransfer = LocalSelectionTransfer
				.getInstance();
		final FileTransfer fileTransfer = FileTransfer.getInstance();

		DelegatingDragAdapter dragAdapter = new DelegatingDragAdapter();
		dragAdapter.addDragSourceListener(new ColorViewDragSourceListener(
				localTransfer, treeViewer));

		DelegatingDropAdapter dropAdapter = new DelegatingDropAdapter();
		dropAdapter.addDropTargetListener(new ColorViewDropTargetListener(
				localTransfer, null));

		Transfer transfers[] = new Transfer[] { localTransfer, fileTransfer };

		treeViewer.addDragSupport(DND.DROP_COPY, transfers, dragAdapter);
		treeViewer.addDropSupport(DND.DROP_COPY, transfers, dropAdapter);

	}

	class ColorViewDragSourceListener implements TransferDragSourceListener {

		private Transfer transfer;

		private ISelectionProvider provider;

		private Object eventData;

		private int eventDetail;

		private Object eventDataLocal;

		public ColorViewDragSourceListener(LocalSelectionTransfer t,
				ISelectionProvider provider) {
			this.transfer = t;
			this.provider = provider;
		}

		public Transfer getTransfer() {
			return transfer;
		}

		public void dragFinished(DragSourceEvent event) {
			

		}

		protected Object getSelectedElement(DragSourceEvent evt) {
			DragSource ds = (DragSource) evt.widget;
			if (ds.getControl() instanceof Tree) {
				Tree tree = (Tree) ds.getControl();
				TreeItem treeItem = tree.getSelection()[0];
				if (treeItem == null) {
					return null;
				} else {
					return treeItem.getData();
				}
			} else {
				IStructuredSelection selected = (IStructuredSelection) provider
						.getSelection();
				return selected.getFirstElement();
			}
		}

		public void dragStart(DragSourceEvent event) {

			event.detail = DND.DROP_NONE;
			if (transfer == LocalSelectionTransfer.getInstance()) {
				LocalSelectionTransfer.getInstance().setSelection(null);
			}

			Object selectedData = getSelectedElement(event);
			if (selectedData instanceof ColorGroup) {
				// add to the localselection transfer
				ISelection selection = new StructuredSelection(selectedData);
				LocalSelectionTransfer.getInstance().setSelection(selection);
			}
		}

		public void dragSetData(DragSourceEvent event) {
			if (transfer instanceof LocalSelectionTransfer
					&& eventDataLocal != null) {
				event.data = eventDataLocal;
				event.detail = eventDetail;
			}
		}

	}

	class ColorViewDropTargetListener implements TransferDropTargetListener {

		public ColorViewDropTargetListener(LocalSelectionTransfer t,
				CommandStack stack) {
			this.stack = stack;
			this.transfer = t;
		}

		private CommandStack stack;

		private Transfer transfer;

		public Transfer getTransfer() {
			return transfer;
		}

		public boolean isEnabled(DropTargetEvent event) {
			return true;
		}

		/**
		 * returns element that is tarrget of drop operation
		 * 
		 * @param evt
		 * @return
		 */
		protected Object getSelectedElement(DropTargetEvent evt) {

			DropTarget dt = (DropTarget) evt.widget;
			if (dt.getControl() instanceof Tree) {
				TreeItem treeItem = null;
				treeItem = (TreeItem) evt.item;
				if (treeItem == null) {
					return null;
				} else {
					return treeItem.getData();
				}
			} else {
				ISelectionProvider provider = getSelectionProviderForGenericCommand(evt);
				if (provider != null) {
					return ((IStructuredSelection) provider.getSelection())
							.getFirstElement();
				}
			}
			return null;
		}

		public void dragOver(DropTargetEvent event) {

			// set to none default, later enabled
			event.detail = DND.DROP_NONE;

			Object dropData = null;
			try {
				dropData = getDropData(event);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			if (dropData instanceof ColorGroup) {
				event.detail = DND.DROP_COPY;
				return;
			}

		}

		public void drop(DropTargetEvent event) {
			Object dropData = null;
			try {
				dropData = getDropData(event);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			if (dropData instanceof ColorGroup) {
				ColorGroup droppedGroup = (ColorGroup) dropData;

				Object selectedElement = getSelectedElement(event);
				if (selectedElement instanceof ColorGroup) {
					ColorGroup referencedGroup = (ColorGroup) selectedElement;
					if (referencedGroup.hasParent()) {// child groups cannot
						// have children
						referencedGroup = getColorGroups().getGroupByName(
								referencedGroup.getParentGroupName());
						if (referencedGroup == null) {
							System.out
									.println("group does not have existing parent, dropping failed");
							return;
						}
					}
					RGB referencedColor = referencedGroup.getGroupColor();

					List<ColorGroup> droppedGroups = new ArrayList<ColorGroup>();

					if (droppedGroup.hasChildrenGroup()) {
						droppedGroups.add(droppedGroup); // add to the list
						// the dropped group
						for (String childGrpName : droppedGroup
								.getChildrenGroups()) { // and all its children
							ColorGroup childGrp = getColorGroups()
									.getGroupByName(childGrpName);
							if (childGrp != null) {
								droppedGroups.add(childGrp);
							}

						}
					} else {
						droppedGroups.add(droppedGroup);
					}
					for (ColorGroup oneOfDroppedGroups : droppedGroups) {
						RGB droppedColor = oneOfDroppedGroups.getGroupColor();
						RGB changingRGB = changeHueValue(droppedColor,
								referencedColor);
						final List<IContentData> items = findContentDataForGroup(oneOfDroppedGroups);

						// final List<ColorGroupItem> items= ((ColorGroup)
						// data[2]).getGroupItems();
						ISelectionProvider sp = new ISelectionProvider() {
							public ISelection getSelection() {
								return new StructuredSelection(items);
							}

							public void addSelectionChangedListener(
									ISelectionChangedListener listener) {
							}

							public void removeSelectionChangedListener(
									ISelectionChangedListener listener) {
							}

							public void setSelection(ISelection selection) {
							}
						};

						ColorizeColorGroupAction colorGroupAction = new ColorizeColorGroupAction(
								sp, stack, treeViewer);
						colorGroupAction.setColorGroups(getColorGroups());
						colorGroupAction.setColor(changingRGB, droppedColor,
								255);
						if (colorGroupAction.isEnabled()) {
							colorGroupAction.setColorGroup(oneOfDroppedGroups);
							colorGroupAction.run();

							ColorGroup oldParentGroup = getColorGroups()
									.getGroupByName(
											oneOfDroppedGroups
													.getParentGroupName());
							if (oldParentGroup != null) {
								// remove old parent to child link
								oldParentGroup
										.removeChildrenGroup(oneOfDroppedGroups
												.getName());
							}
							// change child to parent link
							oneOfDroppedGroups
									.setParentGroupName(referencedGroup
											.getName());
							// create new parent to child link
							referencedGroup.addChildrenGroup(oneOfDroppedGroups
									.getName());
						}
					}

				}

				refreshViewer();
			}

		}

		public void dragEnter(DropTargetEvent event) {
			if (event.detail == DND.DROP_DEFAULT
					|| event.detail == DND.DROP_MOVE) {
				if ((event.operations & DND.DROP_COPY) != 0
						|| (event.operations & DND.DROP_MOVE) != 0) {
					event.detail = DND.DROP_COPY;
				} else {
					event.detail = DND.DROP_NONE;
				}
			}
		}

		public Object getDropData(DropTargetEvent evt) {
			Transfer t = null;

			if (LocalSelectionTransfer.getInstance().isSupportedType(
					evt.currentDataType))
				t = LocalSelectionTransfer.getInstance();

			if (t.isSupportedType(evt.currentDataType)) {

				if (t instanceof LocalSelectionTransfer) {
					Object data = evt.data;
					if (data == null) {
						data = ((LocalSelectionTransfer) t)
								.nativeToJava(evt.currentDataType);
					}
					if (data != null) {
						Object imageData = ((IStructuredSelection) data)
								.getFirstElement();
						return imageData;
					}
				}
			}

			return null;
		}

		public void dragOperationChanged(DropTargetEvent event) {
			if (event.detail == DND.DROP_DEFAULT) {
				if ((event.operations & DND.DROP_COPY) != 0
						|| (event.operations & DND.DROP_MOVE) != 0) {
					// ok
				} else {
					event.detail = DND.DROP_NONE;
				}
			}
		}

		public void dropAccept(DropTargetEvent event) {
		}

		public void dragLeave(DropTargetEvent event) {

		}

		

		protected ISkinnableEntityAdapter getSkinnableEntityAdapter(
				DropTargetEvent event) {
			return null;
		}

		protected IWorkbenchPart getWorkbenchPart() {
			return null;
		}

		protected EditPart getSourceEditPart(DropTargetEvent event) {
			return null;
		}

		protected ISelectionProvider getSelectionProviderForGenericCommand(
				DropTargetEvent event) {
			return null;
		}

		protected void highlightDragOver(Control control) {
		}

		protected void removeHighlight(Control control) {
		}

	}

	public RGB changeHueValue(RGB colorToChange, RGB colorWithReferenceHueValue) {
		// Convert target RGB to HSB
		float[] hsbToChange = colorToChange.getHSB();

		float saturationToChange = hsbToChange[1];
		float brightnessToChange = hsbToChange[2];

		// Convert reference RGB to HSB
		float[] hsbReference = colorWithReferenceHueValue.getHSB();
		float hueReference = hsbReference[0];

		// int rgb= Color.HSBtoRGB(hueReference, saturationToChange,
		// brightnessToChange);
		// int red = (rgb>>16)&0xFF;
		// int green = (rgb>>8)&0xFF;
		// int blue = rgb&0xFF;
		// RGB changedRGB= new RGB(red, green, blue);

		RGB changedRGB = new RGB(hueReference, saturationToChange,
				brightnessToChange);

		return changedRGB;
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		try {
			refreshViewer();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private List<IContentData> findContentDataForGroup(ColorGroup group) {
		List<IContentData> datas = new ArrayList<IContentData>();

		if (sourceEditor instanceof Series60EditorPart) {
			IContent[] cnt = ((Series60EditorPart) sourceEditor).getContents();
			IContent root = ScreenUtil.getPrimaryContent(cnt);
			for (ColorGroupItem item : group.getGroupItems()) {
				IContentData data = root.findById(item.getItemId());
				if (data == null) {
					data = root.findByName(item.getItemId());
				}
				datas.add(data);
			}
		}

		return datas;
	}

	private ColorGroups getColorGroups() {
		if (sourceEditor != null && ColorGroupsStore.isEnabled) {
			IFile original = ((FileEditorInput) sourceEditor.getEditorInput())
					.getFile();
			return ColorGroupsStore.getColorGroupsForProject(original
					.getProject());
		} else {
			return null;
		}
	}

	private MenuManager menuMgr;

	private void hookContextMenu() {
		menuMgr = new MenuManager("#PopupMenu"); 
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this);
		Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
	}

	public void menuAboutToShow(IMenuManager manager) {
		fillContextMenu(manager);

	}

	public void fillContextMenu(IMenuManager manager) {
		IStructuredSelection selection = (IStructuredSelection) treeViewer
				.getSelection();

		if (selection.getFirstElement() instanceof ColorGroup) {

			final ColorGroup selectedGroup = (ColorGroup) selection
					.getFirstElement();
			if (selectedGroup.hasParent()) {
				IAction action = new Action("Remove from parent") {
					@Override
					public void run() {
						ColorGroup parentGroup = getColorGroups()
								.getGroupByName(
										selectedGroup.getParentGroupName());
						selectedGroup.setParentGroupName("");
						parentGroup
								.removeChildrenGroup(selectedGroup.getName());
						refreshViewer();
					}
				};
				manager.add(action);

			}

		}

	}

	public void refreshViewer() {
		treeViewer.getLabelProvider().dispose();
		treeViewer.setInput(createInput());
		treeViewer.refresh();
	}

}
